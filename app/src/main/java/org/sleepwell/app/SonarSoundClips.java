package org.sleepwell.app;
import java.io.ByteArrayOutputStream;
import java.util.function.Consumer;
/** Worker-owned exact16k FIR audio only; no padding or partial clips across any real/cue gap. */
final class SonarSoundClips {
 static final int SAMPLES=160000;
 static final class Clip {final long start,end,duration=10000,firstNanos,endNanos;final byte[] pcm;final double peak;Clip(long wall,long anchor,long first,byte[] pcm,double peak){if(pcm.length!=SAMPLES*2||first<anchor)throw new IllegalArgumentException("A complete captured ten-second clip is required");firstNanos=first;endNanos=Math.addExact(first,10000000000L);start=Math.addExact(wall,(first-anchor)/1000000L);end=Math.addExact(start,duration);this.pcm=pcm;this.peak=peak;}}
 private final SonarClipPolicy policy;private final Consumer<Clip> sink;private final long wall,anchor;private ByteArrayOutputStream pcm;private long first,expected=-1;private double peak;
 SonarSoundClips(SonarClipPolicy policy,Consumer<Clip> sink,long wall,long anchor){this.policy=policy;this.sink=sink;this.wall=wall;this.anchor=anchor;}
 void accept(SonarDownsampler.Output output){if(!policy.enabled||output==null)return;if(expected>=0&&output.fromNanos!=expected)gap();expected=output.toNanos;double level=SleepMath.rms(output.pcm,output.pcm.length);int at=0;while(at<output.pcm.length){long time=output.fromNanos+at*62500L;if(pcm==null){if(level<policy.threshold||!policy.claim(time/1000000L))return;pcm=new ByteArrayOutputStream(SAMPLES*2);first=time;peak=0;}int n=Math.min(output.pcm.length-at,(SAMPLES*2-pcm.size())/2);double energy=0;for(int k=0;k<n;k++){short value=output.pcm[at+k];pcm.write(value&255);pcm.write((value>>8)&255);double normalized=value/32768d;energy+=normalized*normalized;}if(n>0)peak=Math.max(peak,Math.sqrt(energy/n));at+=n;if(pcm.size()==SAMPLES*2){Clip clip=new Clip(wall,anchor,first,pcm.toByteArray(),peak);pcm=null;try{sink.accept(clip);}catch(RuntimeException failure){policy.abandon();throw failure;}}}}
 void gap(){if(pcm!=null){pcm=null;policy.abandon();}expected=-1;peak=0;}
}
