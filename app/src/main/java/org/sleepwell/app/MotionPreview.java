package org.sleepwell.app;

import java.util.*;

/** Fifteen seconds of real samples, with missing bins kept null rather than fabricated as zero. */
final class MotionPreview {
    static final long WINDOW=15000,BIN=500;
    static final class Sample{final long time;final double value;Sample(long time,double value){this.time=time;this.value=value;}}
    private final Deque<Sample> samples=new ArrayDeque<>();private final MotionFilter filter=new MotionFilter();private long last=-1;
    void sample(long time,float x,float y,float z){if(time<last)return;Double value=filter.sample(x,y,z);last=time;if(value!=null){samples.addLast(new Sample(time,value));trim(time);}}
    private void trim(long now){while(!samples.isEmpty()&&samples.peekFirst().time<=now-WINDOW)samples.removeFirst();}
    void clear(){samples.clear();filter.reset();last=-1;}
    boolean recent(long now){trim(now);return !samples.isEmpty()&&now-samples.peekLast().time<=2000;}
    List<Double> bins(long now,MotionProfile profile){trim(now);double[] sums=new double[30];int[] count=new int[30];for(Sample s:samples){int index=(int)((s.time-(now-WINDOW))/BIN);if(index>=0&&index<30){sums[index]+=s.value*s.value;count[index]++;}else if(index==30){sums[29]+=s.value*s.value;count[29]++;}}List<Double> values=new ArrayList<>();for(int n=0;n<30;n++)values.add(count[n]==0?null:profile.inference(Math.sqrt(sums[n]/count[n])));return values;}
}
