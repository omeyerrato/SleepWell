package org.sleepwell.app;

import android.content.Context;
import android.os.SystemClock;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

/** One explicitly started foreground RMS preview. Cleanup stays on its worker, never on the UI thread. */
final class RoomSoundLevel {
    interface Capture {void start()throws Exception;int read(short[] values)throws Exception;void close()throws Exception;}
    interface Factory {Capture open(Context app,BooleanSupplier current)throws Exception;}
    static final class OpenFailure extends Exception {final Capture capture;OpenFailure(Capture capture,Throwable cause){super(cause);this.capture=capture;}}
    enum State {STARTING,MEASURING,CLOSING,CLOSED,ERROR,BLOCKED}
    private static RoomSoundLevel owner;
    private final Context app;private final Factory factory;private final AtomicBoolean admitted;
    private final AtomicBoolean cancelled=new AtomicBoolean();
    private Capture retained;private boolean cleanupRunning;
    volatile State state=State.STARTING;volatile Double level;volatile long sampledAt;volatile String issue="";
    private RoomSoundLevel(Context app,Factory factory,AtomicBoolean admitted){this.app=app.getApplicationContext();this.factory=factory;this.admitted=admitted;}
    static synchronized RoomSoundLevel start(Context app,Factory factory,AtomicBoolean admitted){
        if(owner!=null)return null;RoomSoundLevel next=new RoomSoundLevel(app,factory,admitted);owner=next;
        try{new Thread(next::run,"SleepWell-room-level").start();}catch(RuntimeException failure){owner=null;next.state=State.ERROR;next.issue="Microphone worker could not start. Tap to try again.";}return next;
    }
    static synchronized RoomSoundLevel current(){return owner;}
    private static synchronized void released(RoomSoundLevel expected){if(owner==expected)owner=null;}
    private boolean valid(){return admitted.get()&&!cancelled.get();}
    void cancel(){cancelled.set(true);level=null;if(state==State.STARTING||state==State.MEASURING)state=State.CLOSING;}
    private void run(){
        Capture capture=null;short[] values=new short[1024];long started=SystemClock.elapsedRealtime(),latest=started;
        try{
            if(!valid())return;capture=factory.open(app,this::valid);retained=capture;if(!valid())return;capture.start();
            while(valid()){
                int count=capture.read(values);long now=SystemClock.elapsedRealtime();
                if(count<0||count>values.length)throw new IllegalStateException("Invalid microphone read");
                if(!valid())break;
                if(count>0){level=SleepMath.rms(values,count);sampledAt=latest=now;state=State.MEASURING;Arrays.fill(values,(short)0);}
                else if(now-latest>2500)throw new IllegalStateException("No microphone readings received");
                Thread.sleep(20);
            }
        }catch(Exception|LinkageError failure){if(failure instanceof OpenFailure uncertain){capture=uncertain.capture;retained=capture;}if(valid())issue=failure instanceof SecurityException?"Microphone access was denied or revoked. Tap to review access.":"Microphone unavailable or interrupted. Tap to try again.";
        }finally{
            Arrays.fill(values,(short)0);level=null;state=State.CLOSING;
            try{if(capture!=null)capture.close();retained=null;state=issue.isEmpty()?State.CLOSED:State.ERROR;released(this);}
            catch(Exception|LinkageError failure){state=State.BLOCKED;issue="Microphone cleanup is not confirmed. Retry cleanup before measuring again.";}
        }
    }
    synchronized void retryCleanup(){
        if(state!=State.BLOCKED||retained==null||cleanupRunning)return;cleanupRunning=true;state=State.CLOSING;
        try{new Thread(()->{try{retained.close();retained=null;issue="Microphone stopped. Tap Microphone analysis to measure again.";state=State.CLOSED;released(this);}catch(Exception|LinkageError failure){issue="Microphone cleanup is not confirmed. Retry cleanup.";state=State.BLOCKED;}finally{synchronized(RoomSoundLevel.this){cleanupRunning=false;}}},"SleepWell-room-cleanup").start();}
        catch(RuntimeException unavailable){cleanupRunning=false;state=State.BLOCKED;issue="Cleanup worker could not start. Retry cleanup.";}
    }
}
