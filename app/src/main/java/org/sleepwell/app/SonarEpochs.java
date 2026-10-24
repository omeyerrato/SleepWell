package org.sleepwell.app;
/** Ordered actual capture intervals. No averaging across a quality change, gap or pause. */
final class SonarEpochs {
 interface Sink {void write(long from,long to,Double movement,String quality,int samples,long active)throws Exception;}
 private final long wall,elapsed,segment,activeBase;private final Sink sink;private long cursor,pendingFrom,pendingTo;private SonarFrame.Quality quality;private double weighted;private int samples;private boolean observed;
 SonarEpochs(long wall,long elapsed,long segment,long activeBase,Sink sink){if(wall<=0||elapsed<0||segment<elapsed||activeBase<0)throw new IllegalArgumentException("Night clock anchor");this.wall=wall;this.elapsed=elapsed;this.segment=segment;this.activeBase=activeBase;this.sink=sink;cursor=pendingFrom=pendingTo=segment;}
 long cursor(){return cursor;}
 void add(SonarObservation observation,long through)throws Exception{long from=observation.from(),to=observation.to();if(from>=through||to>through)return;if(from<cursor)throw new IllegalStateException("Capture arrived after its interval was checkpointed.");if(from>cursor)gap(from,observed?SonarFrame.Quality.UNAVAILABLE:SonarFrame.Quality.CALIBRATING);observed=true;append(from,to,observation.frame.movement,observation.frame.quality,1);}
 void through(long to,SonarFrame.Quality missing)throws Exception{if(to>cursor)gap(to,missing);}
 void finish(long to,SonarFrame.Quality missing)throws Exception{through(to,missing);flush();}
 private void gap(long to,SonarFrame.Quality value)throws Exception{while(cursor<to){long end=Math.min(to,cursor+5000);append(cursor,end,null,value,0);}}
 private void append(long from,long to,Double value,SonarFrame.Quality next,int count)throws Exception{if(from!=cursor||to<=from||to-from>5000)throw new IllegalStateException("Noncontiguous sonar interval");if(quality!=null&&(quality!=next||to-pendingFrom>5000))flush();if(quality==null){quality=next;pendingFrom=from;weighted=0;samples=0;}pendingTo=cursor=to;samples+=count;if(value!=null)weighted+=value*(to-from);if(pendingTo-pendingFrom==5000)flush();}
 private void flush()throws Exception{if(quality==null)return;long from=Math.addExact(wall,pendingFrom-elapsed),to=Math.addExact(wall,pendingTo-elapsed),active=Math.addExact(activeBase,pendingTo-segment);Double movement=quality==SonarFrame.Quality.VALID?weighted/(pendingTo-pendingFrom):null;if(movement!=null&&samples==0)throw new IllegalStateException("Measured interval without samples");sink.write(from,to,movement,quality.name(),samples,active);quality=null;weighted=0;samples=0;pendingFrom=pendingTo=cursor;}
}
