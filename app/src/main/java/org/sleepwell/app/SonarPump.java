package org.sleepwell.app;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
/** One bounded duplex owner. IO must be nonblocking; no UI thread waits for it. */
final class SonarPump {
 interface Clock {long millis();void idle()throws InterruptedException;}
 static final class Timing {final double txAtRxZero;final long captureZeroElapsedNanos;
  Timing(double value){this(value,-1);}
  Timing(double value,long captureZeroElapsedNanos){if(!Double.isFinite(value)||captureZeroElapsedNanos< -1)throw new IllegalArgumentException("Audio timing");txAtRxZero=value;this.captureZeroElapsedNanos=captureZeroElapsedNanos;}
 }
 interface NightSink {void prepared(String capture)throws Exception;void started();void frame(SonarObservation observation);default void pcm(short[] pcm,int count,long firstNanos,boolean valid){} }

 interface Io extends AutoCloseable {default String captureSource(){return "";}void startMuted()throws Exception;void check()throws Exception;boolean routed();void confirmRoutes()throws Exception;Timing timing()throws Exception;int write(short[] values,int from,int count)throws Exception;int read(short[] values,int from,int count)throws Exception;void gain(float gain)throws Exception;void close();}
 static final class CleanupFailure extends RuntimeException {CleanupFailure(Throwable cause){super("Audio cleanup could not be confirmed.",cause);}}
 static final class Failure extends Exception {final SonarFrame.Quality quality;Failure(SonarFrame.Quality quality,String message){super(message);this.quality=quality;}}
 static SonarFrame run(Io io,SonarConfig config,AtomicBoolean cancelled,Clock clock,Consumer<SonarFrame> frames)throws Exception{
  return run(io,config,cancelled,clock,frames,null,20000);
 }
 static SonarFrame runNight(Io io,SonarConfig config,AtomicBoolean cancelled,Clock clock,NightSink sink)throws Exception{return run(io,config,cancelled,clock,f->{},sink,12*3600000L);}
 private static SonarFrame run(Io io,SonarConfig config,AtomicBoolean cancelled,Clock clock,Consumer<SonarFrame> frames,NightSink night,long limit)throws Exception{
  long begun=clock.millis(),writeProgress=begun,readProgress=begun;long written=0,read=0,origin=0;int outputAt=0,readAt=0;boolean emitting=false;short[] output=new short[480],input=new short[1920];SonarDsp dsp=new SonarDsp();Timing timing=null;SonarFrame latest=null;Throwable failure=null;
  try{if(cancelled.get())return null;if(night!=null)night.prepared(io.captureSource());if(cancelled.get())return null;begun=clock.millis();writeProgress=readProgress=begun;io.startMuted();if(night!=null)night.started();while(!cancelled.get()&&clock.millis()-begun<limit){io.check();long now=clock.millis();if(!emitting&&now-begun>=2000)throw new Failure(SonarFrame.Quality.TIMING_LOST,"Audio timing or built-in routing could not be established.");
    Timing observed=io.timing();if(emitting&&(observed==null||Math.abs(observed.txAtRxZero-timing.txAtRxZero)>2))throw new Failure(SonarFrame.Quality.TIMING_LOST,"Audio timing changed. Test again.");
    if(!emitting&&written>=1920&&io.routed()&&observed!=null){timing=observed;io.confirmRoutes();io.check();if(cancelled.get())break;origin=written;emitting=true;outputAt=0;fill(output,written-origin);io.gain(config.gain);}
    if(emitting&&outputAt==output.length){outputAt=0;fill(output,written-origin);}
    if(cancelled.get())break;int w=io.write(output,outputAt,output.length-outputAt);if(w<0||w>output.length-outputAt)throw new Failure(SonarFrame.Quality.UNAVAILABLE,"Speaker write failed.");if(w>0){written+=w;outputAt+=w;writeProgress=now;}if(!emitting&&outputAt==output.length)outputAt=0;
    int n=io.read(input,readAt,input.length-readAt);if(n<0||n>input.length-readAt)throw new Failure(SonarFrame.Quality.UNAVAILABLE,"Microphone read failed.");if(n>0){long first=read-readAt;read+=n;readAt+=n;readProgress=now;if(readAt==input.length){if(emitting&&timing.txAtRxZero+first>=origin){latest=dsp.accept(input,timing.txAtRxZero+first-origin);frames.accept(latest);if(night!=null){if(timing.captureZeroElapsedNanos<0)throw new Failure(SonarFrame.Quality.TIMING_LOST,"Capture timestamps are unavailable for night recording.");long from=timing.captureZeroElapsedNanos+Math.round(first*(1e9/SonarConfig.RATE));night.frame(new SonarObservation(from,from+40000000L,latest));night.pcm(input,input.length,from,soundPcmValid(latest.quality));}}readAt=0;}}
    if(now-writeProgress>600||now-readProgress>600)throw new Failure(SonarFrame.Quality.UNAVAILABLE,"Speaker or microphone stopped making progress.");clock.idle();
  }return latest;}catch(Exception|Error e){failure=e;throw e;}finally{try{io.close();}catch(RuntimeException cleanup){if(failure!=null)failure.addSuppressed(cleanup);else throw cleanup;}}
 }
 static boolean soundPcmValid(SonarFrame.Quality quality){return quality==SonarFrame.Quality.VALID||quality==SonarFrame.Quality.CALIBRATING||quality==SonarFrame.Quality.NEEDS_MOVEMENT||quality==SonarFrame.Quality.NO_ECHO;}
 private static void fill(short[] pcm,long first){SonarChirp.fill(pcm,0,pcm.length,first);for(int n=0;n<pcm.length;n++)pcm[n]=(short)Math.round(pcm[n]*Math.min(1,(first+n)/2400d));}
}
