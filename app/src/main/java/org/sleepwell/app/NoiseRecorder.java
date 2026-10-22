package org.sleepwell.app;

import android.content.Context;
import android.media.*;
import java.io.*;

/** On-device energy detection, not a snore/speech classifier. Never transmits audio. */
final class NoiseRecorder implements AutoCloseable {
    private volatile boolean running=true;volatile boolean capturing=false;private volatile AudioRecord activeRecorder; volatile double level=0; private final Thread thread;private final SignalWindow window=new SignalWindow();
    Double takeEpoch(){return window.take();}
    NoiseRecorder(Context c,Store store,long session,ClipBudget budget,java.util.function.LongSupplier timeline) {
        thread=new Thread(()->capture(c,store,session,budget,timeline),"SleepWell-audio"); thread.start();
    }
    private void capture(Context c,Store store,long session,ClipBudget budget,java.util.function.LongSupplier timeline) {
        AudioRecord recorder=null;
        try {
            if(c.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)!=android.content.pm.PackageManager.PERMISSION_GRANTED)throw new SecurityException("Microphone permission is not granted");
            int size=Math.max(4096,AudioRecord.getMinBufferSize(16000,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT));
            recorder=new AudioRecord(MediaRecorder.AudioSource.MIC,16000,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,size);
            if(recorder.getState()!=AudioRecord.STATE_INITIALIZED) throw new IOException("Microphone unavailable");
            activeRecorder=recorder;if(!running)return;recorder.startRecording();capturing=true;short[] frame=new short[1024]; ByteArrayOutputStream clip=null;
            double peak=0;
            while(running) {
                int n=recorder.read(frame,0,frame.length);
                if(!running)break;
                if(n<0) throw new IOException("Microphone read error "+n);
                if(n==0) continue;
                level=SleepMath.rms(frame,n);window.add(level,n);
                double threshold=Prefs.number(c,"noiseThreshold",20)/1000.0;
                if(clip==null && budget.canStart(android.os.SystemClock.elapsedRealtime()) && level>=threshold && Prefs.flag(c,"saveClips")) { clip=new ByteArrayOutputStream(); peak=0; }
                if(clip!=null) {
                    peak=Math.max(peak,level);
                    for(int i=0;i<n;i++) { clip.write(frame[i]&255); clip.write((frame[i]>>8)&255); }
                    if(clip.size()>=16000*2*10) {
                        File dir=new File(c.getFilesDir(),"clips"); if(!dir.exists() && !dir.mkdirs()) throw new IOException("Could not create clips directory");
                        long time=timeline.getAsLong();File file=new File(dir,session+"-"+time+".wav");
                        try{writeWave(file,clip.toByteArray());store.clip(session,time,file.getName(),peak);}catch(Exception e){file.delete();throw e;}budget.saved(android.os.SystemClock.elapsedRealtime());clip=null;
                    }
                }
            }
        } catch(SecurityException e) { Prefs.put(c,"audioError","Microphone permission was denied or revoked."); }
        catch(Exception e) { Prefs.put(c,"audioError",e.getMessage()==null?e.toString():e.getMessage()); }
        finally {capturing=false;activeRecorder=null;if(recorder!=null) { try { recorder.stop(); } catch(IllegalStateException ignored) {} recorder.release(); } }
    }
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
