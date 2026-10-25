package org.sleepwell.app;
import java.time.*;
/** A reviewed copy of local plan clock values, never a following/elapsed-duration alarm. */
final class GoalAlarmTime {
 final int bedtime,goal,minute,dayOffset;
 GoalAlarmTime(int bedtime,int goal){if(bedtime<0||bedtime>=1440||goal<20||goal>810)throw new IllegalArgumentException("Saved bedtime or sleep goal needs review.");this.bedtime=bedtime;this.goal=goal;minute=(bedtime+goal)%1440;dayOffset=(bedtime+goal)/1440;}
 static final class Dated {final LocalDate bedDate,wakeDate;final ZonedDateTime bedtime,wake;final long elapsed;final boolean gap,overlap;Dated(LocalDate bedDate,LocalDate wakeDate,ZonedDateTime bedtime,ZonedDateTime wake,long elapsed,boolean gap,boolean overlap){this.bedDate=bedDate;this.wakeDate=wakeDate;this.bedtime=bedtime;this.wake=wake;this.elapsed=elapsed;this.gap=gap;this.overlap=overlap;}}
 Dated on(LocalDate wakeDate,ZoneId zone){if(wakeDate==null||zone==null)throw new IllegalArgumentException("Choose a date and time zone");LocalDate bedDate=wakeDate.minusDays(dayOffset);LocalDateTime requested=wakeDate.atTime(minute/60,minute%60);ZonedDateTime end=requested.atZone(zone),start=bedDate.atTime(bedtime/60,bedtime%60).atZone(zone);int offsets=zone.getRules().getValidOffsets(requested).size();return new Dated(bedDate,wakeDate,start,end,Duration.between(start,end).toMinutes(),offsets==0,offsets==2);}
}
