package org.sleepwell.app;

import java.time.*;
import java.util.Set;

/** User-selected schedule; it never changes a wake-up alarm. */
final class SleepPlan {
    int goal=480,bedtime=1350,wake=420,windDown=30,reminderAdvance=30;
    boolean reminder;
    long revision;
    SleepPlan copy(){SleepPlan p=new SleepPlan();p.goal=goal;p.bedtime=bedtime;p.wake=wake;p.windDown=windDown;p.reminderAdvance=reminderAdvance;p.reminder=reminder;p.revision=revision;return p;}
    void duration(int minutes){goal=minutes;bedtime=Math.floorMod(wake-goal,1440);}
    void bedtime(int minutes){bedtime=minutes;wake=Math.floorMod(bedtime+goal,1440);}
    void wake(int minutes){wake=minutes;bedtime=Math.floorMod(wake-goal,1440);}
    void validate(){
        if(goal<20||goal>810)throw new IllegalArgumentException("Choose a duration from 20 minutes to 13 h 30 min.");
        if(bedtime<0||bedtime>=1440||wake<0||wake>=1440)throw new IllegalArgumentException("Choose a valid bedtime and wake time.");
        if(windDown<0||windDown>120||reminderAdvance<0||reminderAdvance>120)throw new IllegalArgumentException("Choose an advance of at most two hours.");
    }
    long nextReminder(long now,ZoneId zone){LocalDate date=Instant.ofEpochMilli(now).atZone(zone).toLocalDate();for(int n=0;n<3;n++){long at=date.plusDays(n).atTime(bedtime/60,bedtime%60).atZone(zone).toInstant().minusSeconds(reminderAdvance*60L).toEpochMilli();if(at>now)return at;}throw new IllegalArgumentException("No future bedtime reminder.");}
    boolean guardActive(int minute){return SleepMath.inBedtime(minute,Math.floorMod(bedtime-windDown,1440),wake);}
    boolean goalMet(long active){return active>=goal*60000L;}
}
