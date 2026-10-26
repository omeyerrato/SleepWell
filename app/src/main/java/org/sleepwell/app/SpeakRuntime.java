package org.sleepwell.app;
import java.util.concurrent.atomic.*;

/** One explicit listening attempt. No file storage; model/capture close on the inference worker. */
final class SpeakRuntime {
    interface Capture {void start()throws Exception;int read(short[] samples)throws Exception;void close()throws Exception;}
    static final class OpenFailure extends Exception {final Capture capture;OpenFailure(Capture capture,Throwable cause){super(cause);this.capture=capture;}}
    interface Quiet {void closed();}
    interface Port {long now();boolean current();void check()throws Exception;Quiet quiet(Runnable ready)throws Exception;Capture open()throws Exception;SoundAnalysisStream.Model model();void execute(Runnable work);void idle()throws Exception;void changed();}
    enum State { IDLE,QUIETING,LISTENING,CLOSING,DONE,ERROR,CANCELLED,BLOCKED }
    final Port port;private final AtomicBoolean cancelled=new AtomicBoolean();private Quiet quiet;private volatile Capture retained;private boolean started,workerQueued,quietArmed,readyRequested;private long deadline;volatile State state=State.IDLE;volatile String error="";volatile int accepted;
    private final AudioChallengeMode mode;volatile String progress="";
    SpeakRuntime(Port port){this(port,AudioChallengeMode.SPEECH);}
    SpeakRuntime(Port port,AudioChallengeMode mode){this.port=port;this.mode=mode;}
    synchronized boolean start(){
        if(started||cancelled.get()||!port.current())return false;
        started=true;deadline=port.now()+15000;state=State.QUIETING;port.changed();
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
        try{port.execute(this::run);}catch(RuntimeException rejected){workerQueued=false;error="Speech worker is unavailable. Use Math or try again.";finish(false,true);}
    }
    void cancel(){cancelled.set(true);synchronized(this){if(state==State.DONE||state==State.ERROR||state==State.CANCELLED||state==State.BLOCKED)return;if(workerQueued){state=State.CLOSING;port.changed();}else finish(false,true);}}
    void tick(){if(started&&port.now()>=deadline&&state!=State.DONE&&state!=State.ERROR&&state!=State.BLOCKED&&state!=State.CANCELLED){error="No "+mode.noun+" was accepted in 15 seconds. Use Math or try again.";cancel();}}
    boolean released(){return state==State.DONE||state==State.ERROR||state==State.CANCELLED;}
    private boolean valid(){return !cancelled.get()&&port.current()&&port.now()<deadline;}
    private void run(){SoundAnalysisStream.Model model=null;Capture capture=null;boolean detected=false,closed=true;try{synchronized(this){if(!valid())return;}port.check();if(mode.model())model=port.model();if(!valid())return;port.check();capture=port.open();retained=capture;closed=false;if(!valid())return;capture.start();state=State.LISTENING;port.changed();AudioChallengeMode.Rule rule=new AudioChallengeMode.Rule(mode);short[] samples=new short[1024];float[] frame=new float[15600];int used=0;while(valid()){port.check();int count=capture.read(samples);if(count<0||count>samples.length)throw new IllegalStateException("Invalid microphone sample count");if(!valid())break;if(count==0){port.idle();continue;}for(int n=0;n<count&&valid();n++){frame[used++]=samples[n]/32768f;if(used==frame.length){detected=rule.accept(frame,model==null?null:model.infer(frame));accepted=rule.accepted();progress=rule.progress();port.changed();used=0;if(detected)break;}}if(detected)break;}}catch(Exception|LinkageError failure){if(failure instanceof OpenFailure uncertain){capture=uncertain.capture;retained=capture;closed=false;}error=failure instanceof AudioChallengeMode.BaselineFailure?failure.getMessage():failure instanceof SecurityException?"Microphone access was denied or revoked. Use Math.":"Sound detection is unavailable. Use Math or try again.";}finally{if(capture!=null)try{capture.close();closed=true;retained=null;}catch(Exception failure){error="Microphone cleanup is not confirmed. Retry cleanup before continuing.";}if(model!=null)try{model.close();}catch(RuntimeException|LinkageError failure){error="Speech model cleanup failed. Use Math.";}if(port.now()>=deadline&&error.isEmpty())error=detected?"Listening expired before cleanup finished. Use Math or try again.":"No "+mode.noun+" was accepted in 15 seconds. Use Math or try again.";finish(detected&&valid()&&error.isEmpty(),closed);}}
    private synchronized void finish(boolean detected,boolean closed){if(!closed){state=State.BLOCKED;port.changed();return;}if(quiet!=null){Quiet owned=quiet;quiet=null;owned.closed();}state=detected?State.DONE:error.isEmpty()?State.CANCELLED:State.ERROR;port.changed();}
    synchronized void retryCleanup(){if(state!=State.BLOCKED||retained==null)return;state=State.CLOSING;port.changed();Capture owned=retained;try{port.execute(()->{boolean closed=false;try{owned.close();closed=true;retained=null;error="Microphone stopped. Use Math or start a new attempt.";}catch(Exception failure){error="Microphone cleanup is not confirmed. Retry cleanup.";}finish(false,closed);});}catch(RuntimeException rejected){state=State.BLOCKED;error="Cleanup worker is unavailable. Microphone ownership remains blocked.";port.changed();}}
}
