package org.sleepwell.app;

/** Independent clock handles. Unwrapped minutes prevent a jump when a drag crosses midnight. */
final class NightDialRange {
    static final int MIN=20,MAX=810,STEP=5;
    private int start,end;private boolean wake;private double pointer,unwrapped;
    NightDialRange(int bed,int wake){start=bed;end=bed+Math.floorMod(wake-bed,1440);if(bed<0||bed>=1440||wake<0||wake>=1440||duration()<MIN||duration()>MAX)throw new IllegalArgumentException("Choose a rest window from 20 minutes to 13 h 30 min.");}
    void begin(boolean wake,double minute){this.wake=wake;pointer=minute;unwrapped=wake?end:start;}
    boolean move(double minute){double delta=minute-pointer;if(delta>720)delta-=1440;if(delta< -720)delta+=1440;pointer=minute;unwrapped+=delta;int snapped=(int)Math.round(unwrapped/STEP)*STEP;int before=wake?end:start;if(wake){end=Math.max(start+MIN,Math.min(start+MAX,snapped));if(end!=snapped)unwrapped=end;}else{start=Math.max(end-MAX,Math.min(end-MIN,snapped));if(start!=snapped)unwrapped=start;}return before!=(wake?end:start);}
    int bedtime(){return Math.floorMod(start,1440);}int wake(){return Math.floorMod(end,1440);}int duration(){return end-start;}
    static double minute(float x,float y){return ((Math.toDegrees(Math.atan2(y,x))+90+360)%360)*4;}
}
