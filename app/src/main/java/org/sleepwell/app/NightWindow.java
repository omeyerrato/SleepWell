package org.sleepwell.app;

import java.time.*;

/** Clock time in a planned rest window. Never an observation of sleep. */
final class NightWindow {
    // Inclusive only for this clock display. Scheduler remains strictly future.
    static long nextAlarm(int hour,int minute,java.util.Set<Integer> days,long now,ZoneId zone){return SleepMath.nextOccurrence(hour,minute,days,now-1,zone);}
    final long start,end,now;final ZoneId zone;final boolean valid,active;final double fraction;
    private NightWindow(long start,long end,long now,ZoneId zone,boolean valid){this(start,end,now,zone,valid,false);}
    private NightWindow(long start,long end,long now,ZoneId zone,boolean valid,boolean retainElapsed){this.start=start;this.end=end;this.now=now;this.zone=zone;this.valid=valid;active=valid&&now>=start&&now<=end;fraction=valid&&(active||retainElapsed&&now>end)?Math.max(0,Math.min(1,(double)(now-start)/(end-start))):0;}
    static NightWindow at(int bed,int wake,long alarmAt,long now,ZoneId zone){
        if(bed<0||bed>=1440||wake<0||wake>=1440||bed==wake)return new NightWindow(0,Math.max(0,alarmAt),now,zone,false);
        LocalDate today=Instant.ofEpochMilli(now).atZone(zone).toLocalDate();
        long end=alarmAt;
        if(end<=0){end=resolve(today,wake,zone);if(end<now)end=resolve(today.plusDays(1),wake,zone);}
        LocalDate endDay=Instant.ofEpochMilli(end).atZone(zone).toLocalDate();
        // Choose the intended calendar night first. A DST gap may collapse or
        // reverse a short daytime window; never turn that into an extra day.
        long start=resolve(bed<wake?endDay:endDay.minusDays(1),bed,zone);
        return new NightWindow(start,end,now,zone,end>start&&now<=end);
    }
    static NightWindow nap(NapPlan plan,NapPlan.Clock clock,ZoneId zone){long now=clock.wall(),end=Math.addExact(now,plan.remaining(clock));return new NightWindow(end-plan.minutes*60000L,end,now,zone,true,true);}
    private static long resolve(LocalDate day,int minute,ZoneId zone){return day.atTime(minute/60,minute%60).atZone(zone).toInstant().toEpochMilli();}
    long durationMinutes(){return valid?(end-start)/60000:0;}
    long remainingMinutes(){return valid?Math.max(0,((active?end:start)-now+59999)/60000):0;}
}
