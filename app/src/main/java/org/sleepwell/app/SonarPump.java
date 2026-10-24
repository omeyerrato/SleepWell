package org.sleepwell.app;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
/** One bounded duplex owner. IO must be nonblocking; no UI thread waits for it. */
final class SonarPump {
 interface Clock {long millis();void idle()throws InterruptedException;}
 static final class Timing {final double txAtRxZero;Timing(double value){if(!Double.isFinite(value))throw new IllegalArgumentException("Audio timing");txAtRxZero=value;}}
 interface Io extends AutoCloseable {void startMuted()throws Exception;void check()throws Exception;boolean routed();void confirmRoutes()throws Exception;Timing timing()throws Exception;int write(short[] values,int from,int count)throws Exception;int read(short[] values,int from,int count)throws Exception;void gain(float gain)throws Exception;void close();}
 static final class CleanupFailure extends RuntimeException {CleanupFailure(Throwable cause){super("Audio cleanup could not be confirmed.",cause);}}
 static final class Failure extends Exception {final SonarFrame.Quality quality;Failure(SonarFrame.Quality quality,String message){super(message);this.quality=quality;}}
 static SonarFrame run(Io io,SonarConfig config,AtomicBoolean cancelled,Clock clock,Consumer<SonarFrame> frames)throws Exception{
  long begun=clock.millis(),writeProgress=begun,readProgress=begun;long written=0,read=0,origin=0;int outputAt=0,readAt=0;boolean emitting=false;short[] output=new short[480],input=new short[1920];SonarDsp dsp=new SonarDsp();Timing timing=null;SonarFrame latest=null;Throwable failure=null;
  try{if(cancelled.get())return null;io.startMuted();while(!cancelled.get()&&clock.millis()-begun<20000){io.check();long now=clock.millis();if(!emitting&&now-begun>=2000)throw new Failure(SonarFrame.Quality.TIMING_LOST,"Audio timing or built-in routing could not be established.");
    Timing observed=io.timing();if(emitting&&(observed==null||Math.abs(observed.txAtRxZero-timing.txAtRxZero)>2))throw new Failure(SonarFrame.Quality.TIMING_LOST,"Audio timing changed. Test again.");
    if(!emitting&&written>=1920&&io.routed()&&observed!=null){timing=observed;io.confirmRoutes();io.check();if(cancelled.get())break;origin=written;emitting=true;outputAt=0;fill(output,written-origin);io.gain(config.gain);}
    if(emitting&&outputAt==output.length){outputAt=0;fill(output,written-origin);}
    if(cancelled.get())break;int w=io.write(output,outputAt,output.length-outputAt);if(w<0||w>output.length-outputAt)throw new Failure(SonarFrame.Quality.UNAVAILABLE,"Speaker write failed.");if(w>0){written+=w;outputAt+=w;writeProgress=now;}if(!emitting&&outputAt==output.length)outputAt=0;
    int n=io.read(input,readAt,input.length-readAt);if(n<0||n>input.length-readAt)throw new Failure(SonarFrame.Quality.UNAVAILABLE,"Microphone read failed.");if(n>0){long first=read-readAt;read+=n;readAt+=n;readProgress=now;if(readAt==input.length){if(emitting&&timing.txAtRxZero+first>=origin){latest=dsp.accept(input,timing.txAtRxZero+first-origin);frames.accept(latest);}readAt=0;}}
    if(now-writeProgress>600||now-readProgress>600)throw new Failure(SonarFrame.Quality.UNAVAILABLE,"Speaker or microphone stopped making progress.");clock.idle();
  }return latest;}catch(Exception|Error e){failure=e;throw e;}finally{try{io.close();}catch(RuntimeException cleanup){if(failure!=null)failure.addSuppressed(cleanup);else throw cleanup;}}
 }
 private static void fill(short[] pcm,long first){SonarChirp.fill(pcm,0,pcm.length,first);for(int n=0;n<pcm.length;n++)pcm[n]=(short)Math.round(pcm[n]*Math.min(1,(first+n)/2400d));}
}
