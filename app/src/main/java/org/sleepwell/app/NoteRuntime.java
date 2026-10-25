package org.sleepwell.app;
import java.util.concurrent.atomic.*;

/** One explicit listening attempt. No file storage; model/capture close on the inference worker. */
final class NoteRuntime {
    interface Capture {void start()throws Exception;int read(short[] samples)throws Exception;long captured();void close()throws Exception;}
    static final class OpenFailure extends Exception {final Capture capture;OpenFailure(Capture capture,Throwable cause){super(cause);this.capture=capture;}}
    interface Quiet {void closed();}
    interface Port {long now();boolean current();void check()throws Exception;Quiet quiet(Runnable ready)throws Exception;Capture open()throws Exception;void execute(Runnable work);void idle()throws Exception;void changed();}
    enum State { IDLE,QUIETING,LISTENING,CLOSING,DONE,ERROR,CANCELLED,BLOCKED }
    final Port port;final String run;final int unit,level;volatile double pitch=Double.NaN;volatile long matchedAt;private final AtomicBoolean cancelled=new AtomicBoolean();private Quiet quiet;private volatile Capture retained;private boolean started,workerQueued,quietArmed,readyRequested;private long deadline;volatile State state=State.IDLE;volatile String error="";volatile int accepted;
    NoteRuntime(Port port,String run,int unit,int level){NotePitch.tolerance(level);this.port=port;this.run=run;this.unit=unit;this.level=level;}
    synchronized boolean start(){
        if(started||cancelled.get()||!port.current())return false;
        started=true;deadline=port.now()+20000;state=State.QUIETING;port.changed();
        try{
            port.check();if(!valid()){finish(false,true);return true;}
            quiet=port.quiet(this::ready);quietArmed=true;
            if(cancelled.get())finish(false,true);else if(readyRequested)ready();
        }catch(Exception|LinkageError failure){error="Listening could not start. Use Math or try again.";finish(false,true);}
        return true;
    }
    private synchronized void ready(){
        if(!quietArmed){readyRequested=true;return;}
        if(cancelled.get()||!port.current()||port.now()>=deadline){cancel();return;}
        if(workerQueued||released())return;
        workerQueued=true;
        try{port.execute(this::run);}catch(RuntimeException rejected){workerQueued=false;error="Pitch worker is unavailable. Use Math or try again.";finish(false,true);}
    }
    void cancel(){cancelled.set(true);synchronized(this){if(state==State.DONE||state==State.ERROR||state==State.CANCELLED||state==State.BLOCKED)return;if(workerQueued){state=State.CLOSING;port.changed();}else finish(false,true);}}
    void tick(){if(started&&port.now()>=deadline&&state!=State.DONE&&state!=State.ERROR&&state!=State.BLOCKED&&state!=State.CANCELLED){error="No sustained target note was accepted in 20 seconds. Use Math or try again.";cancel();}}
    boolean released(){return state==State.DONE||state==State.ERROR||state==State.CANCELLED;}
    private boolean valid(){return !cancelled.get()&&port.current()&&port.now()<deadline;}
    private void run(){Capture capture=null;boolean detected=false,closed=true;short[] samples=new short[1024],frame=new short[NotePitch.SIZE];try{synchronized(this){if(!valid())return;}port.check();capture=port.open();retained=capture;closed=false;if(!valid())return;capture.start();state=State.LISTENING;port.changed();NotePitch estimator=new NotePitch();NoteHold hold=new NoteHold(run,unit,level);int used=0;long firstAt=-1;while(valid()){port.check();int count=capture.read(samples);if(count<0||count>samples.length)throw new IllegalStateException("Invalid microphone sample count");if(!valid())break;if(count==0){port.idle();continue;}long at=capture.captured(),now=port.now();if(at<0||at>now||now-at>400){used=0;firstAt=-1;hold.reset();accepted=0;continue;}if(firstAt<0)firstAt=at;for(int n=0;n<count;n++){frame[used++]=samples[n];if(used==frame.length){if(at-firstAt>500){hold.reset();pitch=Double.NaN;}else{pitch=estimator.frequency(frame);detected=hold.accept(pitch,at,now);if(detected)matchedAt=at;}accepted=hold.progress();port.changed();used=0;firstAt=-1;java.util.Arrays.fill(frame,(short)0);if(detected)break;}}java.util.Arrays.fill(samples,(short)0);if(detected)break;}}catch(Exception|LinkageError failure){if(failure instanceof OpenFailure uncertain){capture=uncertain.capture;retained=capture;closed=false;}error=failure instanceof SecurityException?"Microphone access was denied or revoked. Use Math.":"Fresh pitch detection is unavailable. Use Math or try again.";}finally{java.util.Arrays.fill(samples,(short)0);java.util.Arrays.fill(frame,(short)0);if(capture!=null)try{capture.close();closed=true;retained=null;}catch(Exception failure){error="Microphone cleanup is not confirmed. Retry cleanup before continuing.";}if(port.now()>=deadline&&error.isEmpty())error=detected?"Listening expired before cleanup finished. Try again.":"No sustained target note was accepted in 20 seconds. Use Math or try again.";finish(detected&&valid()&&error.isEmpty(),closed);}}
    private synchronized void finish(boolean detected,boolean closed){if(!closed){state=State.BLOCKED;port.changed();return;}if(quiet!=null){Quiet owned=quiet;quiet=null;owned.closed();}state=detected?State.DONE:error.isEmpty()?State.CANCELLED:State.ERROR;port.changed();}
    synchronized void retryCleanup(){if(state!=State.BLOCKED||retained==null)return;state=State.CLOSING;port.changed();Capture owned=retained;try{port.execute(()->{boolean closed=false;try{owned.close();closed=true;retained=null;error="Microphone stopped. Use Math or start a new attempt.";}catch(Exception failure){error="Microphone cleanup is not confirmed. Retry cleanup.";}finish(false,closed);});}catch(RuntimeException rejected){state=State.BLOCKED;error="Cleanup worker is unavailable. Microphone ownership remains blocked.";port.changed();}}
}
