package org.sleepwell.app;

/** Counts only step-sensor events received after activation; never accelerometer/shake samples. */
final class WalkSteps {
    enum Mode { NONE, DETECTOR, COUNTER }
    final int target;private int earned;private long armed,last=-1;private double baseline=Double.NaN;private Mode mode=Mode.NONE;
    WalkSteps(int target,int earned){if(target<5||target>100)throw new IllegalArgumentException("Choose 5–100 steps");this.target=target;this.earned=Math.max(0,Math.min(target,earned));}
    void arm(Mode mode,long now){this.mode=mode;armed=now;last=-1;baseline=Double.NaN;}
    int earned(){return earned;}
    boolean done(){return earned>=target;}
    boolean sample(long timestamp,double value){if(mode==Mode.NONE||done()||timestamp<armed||timestamp<=last||!Double.isFinite(value)||value<0||value!=Math.rint(value))return false;if(mode==Mode.DETECTOR&&value!=1)return false;last=timestamp;int before=earned;if(mode==Mode.DETECTOR)earned++;else if(Double.isNaN(baseline)||value<baseline)baseline=value;else{double delta=value-baseline;baseline=value;earned+=(int)Math.min(target-earned,delta);}return earned!=before;}
}
