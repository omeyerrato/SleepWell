package org.sleepwell.app;

/** Optional monotonic alarm behavior. Zero timeout/duration preserve the original unbounded behavior. */
final class AlarmTiming {
    static final AlarmTiming DEFAULT=new AlarmTiming(0,0,5,0);
    final int backupSeconds,backupRepeats,backupGapMinutes,timeoutMinutes;
    AlarmTiming(int seconds,int repeats,int gap,int timeout){if(seconds!=0&&(seconds<30||seconds>300)||repeats<0||repeats>5||gap<1||gap>15||timeout<0||timeout>120||seconds==0&&repeats!=0)throw new IllegalArgumentException("Unsupported alarm timing");backupSeconds=seconds;backupRepeats=repeats;backupGapMinutes=gap;timeoutMinutes=timeout;}
    AlarmTiming duration(int seconds){return new AlarmTiming(seconds,seconds==0?0:backupRepeats,backupGapMinutes,timeoutMinutes);}
    AlarmTiming repeats(int count){return new AlarmTiming(backupSeconds,count,backupGapMinutes,timeoutMinutes);}
    AlarmTiming gap(int minutes){return new AlarmTiming(backupSeconds,backupRepeats,minutes,timeoutMinutes);}
    AlarmTiming timeout(int minutes){return new AlarmTiming(backupSeconds,backupRepeats,backupGapMinutes,minutes);}
    @Override public boolean equals(Object other){return other instanceof AlarmTiming that&&backupSeconds==that.backupSeconds&&backupRepeats==that.backupRepeats&&backupGapMinutes==that.backupGapMinutes&&timeoutMinutes==that.timeoutMinutes;}
    @Override public int hashCode(){return java.util.Objects.hash(backupSeconds,backupRepeats,backupGapMinutes,timeoutMinutes);}
}
