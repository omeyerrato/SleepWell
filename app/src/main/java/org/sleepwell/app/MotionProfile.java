package org.sleepwell.app;

/** Configurable heuristic gain. Raw recorded acceleration remains in its original units. */
final class MotionProfile {
    final int level;final double gain;
    MotionProfile(int level){if(level<0||level>2)throw new IllegalArgumentException("Choose Low, Medium or High sensitivity.");this.level=level;gain=level==0?.5:level==2?2:1;}
    String label(){return level==0?"Low":level==2?"High":"Medium";}
    double inference(double raw){return !Double.isFinite(raw)||raw<0?Double.NaN:raw*gain;}
    int activeMinutes(double raw,int previous){return inference(raw)>=.12?Math.min(5,previous+1):0;}
    boolean smartWake(long now,long deadline,int window,double raw,int recent,int samples){return SleepMath.smartWake(now,deadline,window,inference(raw),recent,samples);}
}
