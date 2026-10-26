package org.sleepwell.app;

import android.content.Context;
import android.media.*;
import java.io.*;

/** Local PCM capture and bundled YAMNet estimates. Never transmits audio. */
final class NoiseRecorder implements AutoCloseable {
    private volatile boolean running=true;volatile boolean capturing=false;private volatile AudioRecord activeRecorder; volatile double level=0; private final Thread thread;private final Boolean saveOverride;private final SignalWindow window=new SignalWindow();private final MotionSoundAnalysis progress;private final MotionSoundAnalysis.Segment segment;
    Double takeEpoch(){return window.take();}
    NoiseRecorder(Context c,Store store,long session,ClipBudget budget,java.util.function.LongSupplier timeline) {
        this(c,store,session,budget,timeline,(Boolean)null,new MotionSoundAnalysis(true));
    }
    NoiseRecorder(Context c,Store store,long session,ClipBudget budget,java.util.function.LongSupplier timeline,boolean saveClips){this(c,store,session,budget,timeline,Boolean.valueOf(saveClips),new MotionSoundAnalysis(true));}
    NoiseRecorder(Context c,Store store,long session,ClipBudget budget,java.util.function.LongSupplier timeline,boolean saveClips,MotionSoundAnalysis progress){this(c,store,session,budget,timeline,Boolean.valueOf(saveClips),progress);}
    NoiseRecorder(Context c,Store store,long session,ClipBudget budget,java.util.function.LongSupplier timeline,MotionSoundAnalysis progress){this(c,store,session,budget,timeline,(Boolean)null,progress);}
    private NoiseRecorder(Context c,Store store,long session,ClipBudget budget,java.util.function.LongSupplier timeline,Boolean saveClips,MotionSoundAnalysis progress){
        if(progress==null)throw new IllegalArgumentException("Missing per-night sound owner");this.progress=progress;segment=progress.begin();saveOverride=saveClips;thread=new Thread(()->capture(c,store,session,budget,timeline),"SleepWell-audio");thread.start();
    }
    private boolean savesClips(Context c,ClipBudget budget){return budget.permitsClips(saveOverride==null?Prefs.flag(c,"saveClips"):saveOverride);}
    private void capture(Context c,Store store,long session,ClipBudget budget,java.util.function.LongSupplier timeline) {
        AudioRecord recorder=null;MicrophoneLease microphone=null;SoundAnalysisStream analysis=null;String stage="capture";String[] eventIssue={""};SnoreCuePolicy cues=new SnoreCuePolicy(budget.snoreCues,()->new SnoreCueOutput(c,()->running&&Prefs.flag(c,"snoreCues"),"sleepwell.db".equals(store.getDatabaseName())),()->diagnostic(c,"snoreCueError","A snoring cue could not play."));
        try {
            if(!budget.analysisAllowed()){progress.fail("capture");diagnostic(c,"audioError","Sound settings are unreadable. Review and save Sound sensitivity before recording sound.");return;}
            if(c.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)!=android.content.pm.PackageManager.PERMISSION_GRANTED)throw new SecurityException("Microphone permission is not granted");
            microphone=MicrophoneLease.acquire("sleep-noise");if(microphone==null)throw new IOException("Microphone is in use by another SleepWell capture.");
            if(savesClips(c,budget))prune(c,store,budget);
            int size=Math.max(4096,AudioRecord.getMinBufferSize(16000,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT));
            recorder=new AudioRecord(MediaRecorder.AudioSource.MIC,16000,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,size);
            if(recorder.getState()!=AudioRecord.STATE_INITIALIZED) throw new IOException("Microphone unavailable");
            if(!running)return;
            diagnostic(c,"analysisError","");
            if(progress.analyzing(segment)){analysis=new SoundAnalysisStream(()->new YamnetClassifier(c),(at,count,category,score)->{if(!budget.saveEvent()){eventIssue[0]="limit";throw new IllegalStateException("Sound event limit reached");}long event=store.soundEvent(session,at,count*975L,category,score,null);if("sleepwell.db".equals(store.getDatabaseName()))Automations.soundRecognized(c,session,event,at,category);if(running&&!DreamCues.masks(android.os.SystemClock.elapsedRealtimeNanos(),android.os.SystemClock.elapsedRealtimeNanos()+1))cues.event(category,score,android.os.SystemClock.elapsedRealtime(),SnoreCueOutput.settings(c));},()->diagnostic(c,"analysisError","Sound labels unavailable. Room sound levels and optional recordings continue."),budget.sensitivity);updateAnalysis(analysis,eventIssue[0]);}
            activeRecorder=recorder;if(!running)return;recorder.startRecording();if(recorder.getRecordingState()!=AudioRecord.RECORDSTATE_RECORDING)throw new IOException("Microphone did not start");capturing=true;short[] frame=new short[1024]; ByteArrayOutputStream clip=null;
            double peak=0;
            while(running) {
                int n=recorder.read(frame,0,frame.length);
                if(!running)break;
                if(n<0) throw new IOException("Microphone read error "+n);
                if(n==0) continue;
                AudioRecordingConfiguration configuration=recorder.getActiveRecordingConfiguration();if(configuration!=null&&configuration.isClientSilenced())throw new IOException("Android silenced microphone capture");
                long pcmEnd=android.os.SystemClock.elapsedRealtimeNanos();if(DreamCues.masks(pcmEnd-n*1000000000L/16000,pcmEnd)){if(analysis!=null){analysis.gap();updateAnalysis(analysis,eventIssue[0]);}clip=null;continue;}
                level=SleepMath.rms(frame,n);window.add(level,n);
                if(analysis!=null&&progress.analyzing(segment)){analysis.accept(frame,n,timeline.getAsLong());updateAnalysis(analysis,eventIssue[0]);}
                double threshold=Prefs.number(c,"noiseThreshold",20)/1000.0;
                if(!savesClips(c,budget))clip=null;
                if(clip==null && budget.canStart(android.os.SystemClock.elapsedRealtime()) && level>=threshold && savesClips(c,budget)) { prune(c,store,budget);clip=new ByteArrayOutputStream(); peak=0; }
                if(clip!=null) {
                    peak=Math.max(peak,level);
                    for(int i=0;i<n;i++) { clip.write(frame[i]&255); clip.write((frame[i]>>8)&255); }
                    if(clip.size()>=16000*2*10) {
                        stage="storage";
                        File dir=new File(c.getFilesDir(),"clips"); if(!dir.exists() && !dir.mkdirs()) throw new IOException("Could not create clips directory");
                        long time=timeline.getAsLong();File file=new File(dir,session+"-"+time+".wav");
                        try{writeWave(file,clip.toByteArray());store.clip(session,time,file.getName(),peak,clip.size()*1000L/32000);}catch(Exception e){file.delete();throw e;}budget.saved(android.os.SystemClock.elapsedRealtime());clip=null;stage="capture";
                    }
                }
            }
        } catch(SecurityException e) { updateAnalysis(analysis,eventIssue[0]);progress.fail("capture");diagnostic(c,"audioError","Microphone permission was denied or revoked."); }
        catch(Exception|LinkageError e) {updateAnalysis(analysis,eventIssue[0]);progress.fail(e instanceof android.database.sqlite.SQLiteException?"storage":stage);diagnostic(c,"audioError",e.getMessage()==null?e.toString():e.getMessage()); }
        finally {
            try{cues.close();}catch(RuntimeException|LinkageError cleanup){progress.fail("cleanup");}
            try{if(analysis!=null)analysis.close();}catch(RuntimeException|LinkageError e){updateAnalysis(analysis,eventIssue[0]);progress.fail("storage");diagnostic(c,"analysisError","Could not save the final sound event.");}
            updateAnalysis(analysis,eventIssue[0]);capturing=false;activeRecorder=null;
            if(recorder!=null){try{recorder.stop();}catch(RuntimeException ignored){}try{recorder.release();if(microphone!=null)microphone.released();}catch(RuntimeException|LinkageError cleanup){progress.fail("cleanup");diagnostic(c,"audioError","Microphone cleanup could not be confirmed. Restart SleepWell before recording again.");}}
            else if(microphone!=null)microphone.released();
            progress.closed(segment,analysis==null?0:analysis.frames(),eventIssue[0].isEmpty()?(analysis==null?"":analysis.issue()):eventIssue[0]);
        }
    }
    private static void diagnostic(Context context,String key,String message){try{Prefs.put(context,key,message);}catch(RuntimeException ignored){/* Historical evidence is owned by progress, never by diagnostic preferences. */}}
    private void updateAnalysis(SoundAnalysisStream analysis,String issue){if(analysis!=null)progress.update(segment,analysis.frames(),issue.isEmpty()?analysis.issue():issue);}
    private static void prune(Context c,Store store,ClipBudget budget){if(budget.pruneOnce())try{SoundClips.prune(c,store);}catch(RuntimeException error){diagnostic(c,"audioRetentionError","Expired audio cleanup needs attention. New recording can continue.");}}
    private static void little(DataOutputStream out,int value,int bytes) throws IOException { for(int i=0;i<bytes;i++) out.writeByte(value>>(8*i)); }
    static void writeWave(File file,byte[] pcm) throws IOException {
        try(DataOutputStream out=new DataOutputStream(new FileOutputStream(file))) {
            out.writeBytes("RIFF"); little(out,pcm.length+36,4); out.writeBytes("WAVEfmt "); little(out,16,4); little(out,1,2); little(out,1,2);
            little(out,16000,4); little(out,32000,4); little(out,2,2); little(out,16,2); out.writeBytes("data"); little(out,pcm.length,4); out.write(pcm);
        }
    }
    void requestStop(){running=false;}
    /** Call on the service's worker; return only when no further database writes are possible. */
    @Override public void close() {requestStop();AudioRecord recorder=activeRecorder;if(recorder!=null)try{recorder.stop();}catch(RuntimeException ignored){}boolean interrupted=false;while(thread.isAlive())try{thread.join(250);}catch(InterruptedException e){interrupted=true;}if(interrupted)Thread.currentThread().interrupt();}
}
