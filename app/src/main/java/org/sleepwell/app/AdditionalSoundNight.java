package org.sleepwell.app;

import android.content.Context;
import android.os.Process;
import android.os.SystemClock;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

/** Experimental estimates from already-admitted PCM. Capture never waits for inference or SQLite. */
final class AdditionalSoundNight {
    interface Model extends AutoCloseable {
        RecordedSoundAnalyzer.Result analyze(short[] pcm,BooleanSupplier cancelled) throws Exception;
        void close() throws Exception;
    }
    interface ModelFactory {Model open() throws Exception;}
    static Model bundled(Context context) throws Exception {
        RecordedSoundAnalyzer analyzer=new RecordedSoundAnalyzer(context);
        return new Model(){public RecordedSoundAnalyzer.Result analyze(short[] pcm,BooleanSupplier cancelled)throws Exception{return analyzer.analyze(pcm,cancelled,(a,b)->{});}public void close()throws Exception{analyzer.close();}};
    }
    static final int WINDOW_SAMPLES=160000;
    private static final long MAX_SAMPLES=12L*3600*16000;
    private final Context app;
    private final Records.Factory factory;
    private final BooleanSupplier identity;
    private final long session,rawStart;
    private final String token=UUID.randomUUID().toString(),source;
    private final long historyEpoch=SleepSummaryFiles.epoch();
    private final String databasePath;
    private final boolean registered;
    private final ArrayBlockingQueue<Block> queue=new ArrayBlockingQueue<>(1);
    private final AtomicReference<Segment> segment=new AtomicReference<>();
    private final AtomicLong captured=new AtomicLong();
    private volatile boolean finished,disabled;
    private volatile String issue="",phase="pending";
    private long analyzed,sequence;
    private final Thread thread;
    private final ModelFactory models;
    private volatile int runningWindows;

    static AdditionalSoundNight open(Context context,Records.Factory factory,long session,long rawStart,
                                    boolean microphoneAdmitted,String source,BooleanSupplier identity) {
        if(!AdditionalSoundSettings.requested(context))return null;
        try{return new AdditionalSoundNight(context,factory,session,rawStart,microphoneAdmitted,source,identity);}
        catch(RuntimeException|LinkageError|OutOfMemoryError unavailable){return null;}
    }
    /** Explicit constructor allows isolated Android fixtures to request a night without changing preferences. */
    AdditionalSoundNight(Context context,Records.Factory factory,long session,long rawStart,
                         boolean microphoneAdmitted,String source,BooleanSupplier identity) {
        this(context,factory,session,rawStart,microphoneAdmitted,source,identity,()->bundled(context));
    }
    AdditionalSoundNight(Context context,Records.Factory factory,long session,long rawStart,
                         boolean microphoneAdmitted,String source,BooleanSupplier identity,ModelFactory models) {
        if(factory==null||identity==null||session<=0||rawStart<=0||!java.util.Set.of(AdditionalSoundStore.SOURCE_MOTION,AdditionalSoundStore.SOURCE_SONAR).contains(source))throw new IllegalArgumentException("Additional sound capture identity is missing.");
        app=context.getApplicationContext();this.factory=factory;this.session=session;this.rawStart=rawStart;this.source=source;this.identity=identity;this.models=java.util.Objects.requireNonNull(models);
        if(!microphoneAdmitted){disabled=true;phase="unavailable";issue="The existing sound-analysis microphone was off or unavailable. Additional analysis does not enable it.";}
        // Admission happens once, before capture begins (or on the existing sonar storage owner).
        // Later worker startup cannot accidentally enroll an identical ID recreated by erase/restore.
        String path="";boolean admitted=false;
        try(Store store=factory.open(app)) {
            if(!currentIdentity())throw new IllegalStateException("History or storage identity changed.");
            path=app.getDatabasePath(store.getDatabaseName()).getCanonicalPath();
            AdditionalSoundStore.begin(store,session,rawStart,token,source);admitted=true;
        }catch(Exception failure){disabled=true;finished=true;phase="error";issue="Additional analysis could not enroll this recording. Base sound analysis is unaffected.";}
        databasePath=path;registered=admitted;
        thread=new Thread(this::work,"SleepWell-additional-sounds");thread.start();
    }
    static final class Segment {
        final String nonce=UUID.randomUUID().toString();
        // A successful CAS of the current revision is the linearization point for a completed result.
        // Retirement never waits for SQLite/native inference; a result already accepted before it may persist.
        final AtomicLong revision=new AtomicLong(1);
        short[] accumulating=new short[WINDOW_SAMPLES];int count;long start,expected;
    }
    private static final class Block {
        final Segment segment;final long revision,at;final short[] pcm;
        Block(Segment owner,long revision,long at,short[] pcm){segment=owner;this.revision=revision;this.at=at;this.pcm=pcm;}
    }
    Segment beginSegment() {
        if(finished||disabled||!currentIdentity())return null;
        try{Segment next=new Segment(),old=segment.getAndSet(next);if(old!=null)retire(old);phase="active";return next;}
        catch(OutOfMemoryError unavailable){unavailable("Additional capture memory is unavailable. Base sound analysis continues.");return null;}
    }
    /** The caller owns these PCM samples only until return. No raw PCM is written to disk. */
    void accept(Segment owner,short[] pcm,int count,long firstWall) {
        if(owner==null||finished||owner!=segment.get()||owner.revision.get()==0)return;
        if(pcm==null||count<1||count>pcm.length||firstWall<rawStart){unavailable("The admitted microphone interval is invalid.");return;}
        long total=captured.addAndGet(count);if(total>MAX_SAMPLES){unavailable("Additional sound capture reached its 12-hour coverage limit.");return;}
        if(disabled)return;
        synchronized(owner) {
            if(owner!=segment.get()||owner.revision.get()==0||finished)return;
            if(owner.expected!=0&&Math.abs(firstWall-owner.expected)>250){gap(owner);}
            if(owner.count==0)owner.start=firstWall;
            int offset=0;
            while(offset<count) {
                int take=Math.min(WINDOW_SAMPLES-owner.count,count-offset);
                System.arraycopy(pcm,offset,owner.accumulating,owner.count,take);owner.count+=take;offset+=take;
                if(owner.count==WINDOW_SAMPLES) {
                    Block block=new Block(owner,owner.revision.get(),owner.start,owner.accumulating);
                    try{owner.accumulating=new short[WINDOW_SAMPLES];}
                    catch(OutOfMemoryError unavailable){unavailable("Additional capture memory is unavailable. Unanalyzed intervals are skipped.");break;}
                    owner.count=0;owner.start=firstWall+offset*1000L/16000;
                    if(!queue.offer(block)){Arrays.fill(block.pcm,(short)0);unavailable("The additional-analysis queue could not keep up. Unanalyzed captured intervals are reported as skipped.");break;}
                }
            }
            owner.expected=firstWall+count*1000L/16000;
        }
    }
    /** Clears partial/context data across a real gap; already committed estimates remain historical. */
    void gap(Segment owner) {
        if(owner==null||owner!=segment.get())return;
        synchronized(owner){if(owner.revision.get()!=0){owner.revision.addAndGet(2);discard(owner);clearQueued(owner);}}
    }
    void endSegment(Segment owner) {
        if(owner==null)return;segment.compareAndSet(owner,null);retire(owner);if(!finished&&!disabled)phase="paused";
    }
    void pause(){Segment owner=segment.getAndSet(null);if(owner!=null)retire(owner);if(!finished&&!disabled)phase="paused";}
    private void retire(Segment owner) {
        owner.revision.set(0);synchronized(owner){discard(owner);clearQueued(owner);}
    }
    private static void discard(Segment owner){Arrays.fill(owner.accumulating,(short)0);owner.count=0;owner.expected=0;}
    private void clearQueued(Segment owner){Block waiting=queue.peek();if(waiting!=null&&waiting.segment==owner&&queue.remove(waiting))Arrays.fill(waiting.pcm,(short)0);}
    void unavailable(String reason) {
        if(issue.isEmpty())issue=reason;disabled=true;phase="error";Segment owner=segment.get();if(owner!=null)gap(owner);
    }
    /** No join: base capture/service teardown never waits on a supplementary native call. */
    void finish(boolean interrupted) {
        if(finished)return;finished=true;Segment old=segment.getAndSet(null);if(old!=null)retire(old);
        if(interrupted&&issue.isEmpty()){phase="interrupted";issue="The recording or additional analysis was interrupted. Only committed windows are available.";}
        thread.interrupt();
    }
    boolean released(){return !thread.isAlive();}
    int queuedWindows(){return queue.size();}
    int runningWindows(){return runningWindows;}
    private boolean currentIdentity(){return identity.getAsBoolean()&&SleepSummaryFiles.current(historyEpoch);}
    private boolean live(Block block){return !finished&&!disabled&&currentIdentity()&&segment.get()==block.segment&&block.segment.revision.get()==block.revision;}
    private static long heap(){Runtime r=Runtime.getRuntime();return r.totalMemory()-r.freeMemory();}
    private void work() {
        Model analyzer=null;
        try(Store store=factory.open(app)) {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            if(!registered||!currentIdentity()||!databasePath.equals(app.getDatabasePath(store.getDatabaseName()).getCanonicalPath()))return;
            long savedCaptured=-1,savedAnalyzed=-1;String savedState="",savedIssue="";
            while(true) {
                if(!currentIdentity())break;
                Block block=null;try{block=queue.poll(500,TimeUnit.MILLISECONDS);}catch(InterruptedException ignored){/* Retirement is checked below. */}
                if(block!=null) {
                    try {
                        if(live(block)) {
                            if(sequence>=AdditionalSoundStore.MAX_WINDOWS){unavailable("Additional sound result limit reached; remaining captured intervals are skipped.");}
                            else {
                                runningWindows=1;if(analyzer==null)analyzer=models.open();
                                long before=SystemClock.elapsedRealtime();
                                Block acceptedBlock=block;
                                RecordedSoundAnalyzer.Result result=analyzer.analyze(block.pcm,()->!live(acceptedBlock));
                                long elapsed=SystemClock.elapsedRealtime()-before;
                                if(elapsed>=10000){unavailable("Additional inference took longer than its capture window. Further estimates stopped to protect recording.");}
                                else if(live(block)&&result.windows.size()==1&&result.analyzedMs==10000&&block.segment.revision.compareAndSet(block.revision,block.revision)) {
                                    // The accepted receipt remains valid if capture retires after this CAS;
                                    // erase/restore/ID reuse still fail the durable token+raw-session transaction.
                                    AdditionalSoundStore.append(store,session,rawStart,token,++sequence,block.segment.nonce,block.at,result.windows.get(0),elapsed,heap());
                                    analyzed+=WINDOW_SAMPLES;
                                }
                            }
                        }
                    }catch(CancellationException retired){/* No stale candidate is persisted. */}
                    catch(Exception|LinkageError|OutOfMemoryError failure){unavailable("The experimental analyzer is unavailable or could not save a result. Base sound analysis continues.");}
                    finally{runningWindows=0;Arrays.fill(block.pcm,(short)0);}
                }
                // Release failed optional models on their worker after native inference returns.
                // Coverage/status can continue without retaining the shared inference lease.
                if(disabled&&analyzer!=null) {
                    Model retired=analyzer;analyzer=null;
                    try{retired.close();}catch(Exception|LinkageError cleanup){phase="error";issue="Additional model cleanup could not be confirmed. Base recording is unaffected.";}
                }
                long seen=captured.get();String state=phase;
                if(finished&&!java.util.Set.of("error","unavailable","interrupted").contains(state))state=analyzed>0?"complete":"insufficient";
                if(seen!=savedCaptured||analyzed!=savedAnalyzed||!state.equals(savedState)||!issue.equals(savedIssue)) {
                    AdditionalSoundStore.status(store,session,rawStart,token,state,issue,seen,analyzed,Math.max(0,seen-analyzed));
                    savedCaptured=seen;savedAnalyzed=analyzed;savedState=state;savedIssue=issue;
                }
                if(finished)break;
            }
        }catch(Exception ignored){/* Exact record/token loss retires this private derived owner; base capture is unaffected. */}
        finally {
            finished=true;disabled=true;
            Segment old=segment.getAndSet(null);if(old!=null)retire(old);
            Block waiting;while((waiting=queue.poll())!=null)Arrays.fill(waiting.pcm,(short)0);
            if(analyzer!=null)try{analyzer.close();}catch(Exception ignored){/* Lease remains held through native completion and close. */}
        }
    }
}
