package org.sleepwell.app;

/** Per-alarm movement and recorded-duration gates; inheritance preserves older alarms. */
final class AlarmSmartWake {
    static final AlarmSmartWake DEFAULT=new AlarmSmartWake(-1,-1);
    final int sensitivity,goal;
    AlarmSmartWake(int sensitivity,int goal){
        if(sensitivity< -1||sensitivity>2)throw new IllegalArgumentException("Choose inherited, Low, Medium or High movement sensitivity.");
        if(goal< -1||goal>1)throw new IllegalArgumentException("Choose inherited, Off or On duration goal.");
        this.sensitivity=sensitivity;this.goal=goal;
    }
    double gain(MotionProfile tracking){if(sensitivity<0){if(tracking==null)throw new IllegalArgumentException("Missing inherited tracking sensitivity.");return tracking.gain;}return sensitivity==0?.5:sensitivity==2?2:1;}
    boolean requiresGoal(boolean inherited){return goal<0?inherited:goal==1;}
    String config(){return "smart-wake-v1:"+sensitivity+":"+goal;}
    @Override public boolean equals(Object other){return other instanceof AlarmSmartWake that&&sensitivity==that.sensitivity&&goal==that.goal;}
    @Override public int hashCode(){return 31*sensitivity+goal;}
}
