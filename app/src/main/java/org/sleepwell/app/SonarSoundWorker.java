package org.sleepwell.app;

import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/** Bounded analysis-only worker. Capture submission and teardown never wait for inference. */
final class SonarSoundWorker {
    static final int CAPACITY=64;
    static final class Result {final long frames;final String issue;Result(long frames,String issue){this.frames=frames;this.issue=issue;}}
    private static final class Block {final short[] pcm;final int count;final long first;final boolean valid;Block(short[] pcm,int count,long first,boolean valid){this.pcm=pcm;this.count=count;this.first=first;this.valid=valid;}}
    private final ArrayBlockingQueue<Block> queue=new ArrayBlockingQueue<>(CAPACITY);private final AtomicReference<String> failure=new AtomicReference<>("");private final Executor completion;
    private final Thread thread;private volatile boolean stopping,done;private volatile long cutoff=Long.MAX_VALUE,frames;private volatile Result result;private Consumer<Result> finished;
    SonarSoundWorker(SoundAnalysisStream.Factory factory,SoundAnalysis.Group.Sink events,long wall,long elapsedNanos,Executor completion){this(factory,events,wall,elapsedNanos,completion,SoundSensitivity.DEFAULT);}
    SonarSoundWorker(SoundAnalysisStream.Factory factory,SoundAnalysis.Group.Sink events,long wall,long elapsedNanos,Executor completion,SoundSensitivity sensitivity){
        if(completion==null||sensitivity==null)throw new IllegalArgumentException("Missing analysis completion owner or grouping policy");this.completion=completion;
        thread=new Thread(()->run(factory,events,wall,elapsedNanos,sensitivity),"SleepWell-sonar-analysis");thread.start();
    }
    /** Actual AudioRecord block is copied before return; the capture buffer may immediately be reused. */
    synchronized boolean offer(short[] pcm,int count,long firstNanos,boolean valid){
        if(stopping||done||!failure.get().isEmpty())return false;
        if(pcm==null||count<1||count>pcm.length||count>SonarDownsampler.MAX_INPUT||firstNanos<0){reject("capture");return false;}
        Block block=new Block(valid?Arrays.copyOf(pcm,count):new short[0],count,firstNanos,valid);
        if(queue.offer(block))return true;reject("queue");return false;
    }
    private void reject(String issue){failure.compareAndSet("",issue);stopping=true;queue.clear();}
    synchronized void disable(String issue){if(!SoundAnalysisStatus.ISSUES.contains(issue))throw new IllegalArgumentException("Invalid analysis failure");if(!done)reject(issue);}
    long frames(){return frames;}boolean released(){return done;}String issue(){Result ended=result;return ended==null?failure.get():ended.issue;}
    /** A single immutable cut/completion is accepted; callers must wait asynchronously for acknowledged release. */
    synchronized void finish(long cutoffNanos,Consumer<Result> finished){
        if(cutoffNanos<0||finished==null)throw new IllegalArgumentException("Invalid analysis cutoff");
        if(this.finished!=null)throw new IllegalStateException("Analysis finish already requested");this.finished=finished;cutoff=cutoffNanos;stopping=true;
        if(done){Result value=result;completion.execute(()->finished.accept(value));}
    }
    private void run(SoundAnalysisStream.Factory factory,SoundAnalysis.Group.Sink events,long wall,long elapsedNanos,SoundSensitivity sensitivity){
        SonarSoundPipeline pipeline=null;
        try{
            pipeline=new SonarSoundPipeline(factory,events,wall,elapsedNanos,sensitivity);
            while(true){if(!failure.get().isEmpty()){pipeline.fail(failure.get());break;}Block block;
                try{block=queue.poll(100,TimeUnit.MILLISECONDS);}catch(InterruptedException interrupted){continue;}
                if(block==null){if(stopping)break;continue;}
                long cut=cutoff;if(block.first>=cut)continue;
                int length=block.count;if(cut!=Long.MAX_VALUE){long available=cut-block.first;if(available<40000000L)length=(int)Math.min(length,available*48/1000000L);}
                if(length<=0)continue;
                if(!block.valid)pipeline.gap();else pipeline.accept(block.pcm,length,block.first,true);
                frames=pipeline.frames();if(!pipeline.issue().isEmpty()){failure.compareAndSet("",pipeline.issue());break;}
            }
        }catch(RuntimeException|LinkageError unexpected){failure.compareAndSet("","capture");}
        finally{
            if(pipeline!=null){try{pipeline.close();}catch(RuntimeException|LinkageError cleanup){failure.compareAndSet("","cleanup");}finally{frames=pipeline.frames();if(!pipeline.issue().isEmpty())failure.compareAndSet("",pipeline.issue());}}
            Consumer<Result> callback;Result value=new Result(frames,failure.get());
            synchronized(this){queue.clear();result=value;done=true;callback=finished;}
            if(callback!=null)completion.execute(()->callback.accept(value));
        }
    }
}
