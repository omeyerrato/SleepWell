package org.sleepwell.app;

import java.time.*;
import java.util.*;

/** One chosen local calendar occurrence, not a repeating exception or a relative nap. */
final class AlarmSkip {
 final String nonce,zone;final LocalDate date;final int hour,minute;final Set<Integer> days;final long solarAt;final String solarKey;
 AlarmSkip(String nonce,LocalDate date,int hour,int minute,Set<Integer> days,String zone){
  this(nonce,date,hour,minute,days,zone,0,"");
 }
 AlarmSkip(String nonce,LocalDate date,int hour,int minute,Set<Integer> days,String zone,long solarAt,String solarKey){
  if(!UUID.fromString(nonce).toString().equals(nonce)||date.getYear()<1970||date.getYear()>9999||hour<0||hour>23||minute<0||minute>59||days.stream().anyMatch(d->d<1||d>7))throw new IllegalArgumentException("Invalid skipped occurrence");
  ZoneId.of(zone);if(!days.isEmpty()&&!days.contains(date.getDayOfWeek().getValue()))throw new IllegalArgumentException("Skipped date is outside alarm weekdays");
  this.nonce=nonce;this.date=date;this.hour=hour;this.minute=minute;this.days=Collections.unmodifiableSet(new TreeSet<>(days));this.zone=zone;if(solarKey==null||solarKey.length()>160||solarKey.isEmpty()!=(solarAt==0)||!solarKey.isEmpty()&&(!solarKey.startsWith("solar-v1:")||solarAt<=0||!Instant.ofEpochMilli(solarAt).atZone(ZoneId.of(zone)).toLocalDate().equals(date)))throw new IllegalArgumentException("Invalid solar skip");this.solarAt=solarAt;this.solarKey=solarKey;
 }
 boolean matches(int hour,int minute,Set<Integer> days){return this.hour==hour&&this.minute==minute&&this.days.equals(days);}
 boolean matches(int hour,int minute,Set<Integer> days,SunriseSchedule solar,LocalDate selected){return matches(hour,minute,days)&&(solar==null?solarKey.isEmpty():solar.key().equals(solarKey)&&(selected==null||selected.equals(date))&&solar.on(date,hour,minute,ZoneId.of(zone)).at==solarAt);}
 long at(ZoneId zone){return solarKey.isEmpty()?date.atTime(hour,minute).atZone(zone).toInstant().toEpochMilli():solarAt;}
 boolean excludes(long at,ZoneId zone){return at==at(zone);}
 static long next(int hour,int minute,Set<Integer> days,long now,ZoneId zone,AlarmSkip skip){
  long at=SleepMath.nextOccurrence(hour,minute,days,now,zone);
  return skip!=null&&skip.matches(hour,minute,days)&&skip.excludes(at,zone)?SleepMath.nextOccurrence(hour,minute,days,at,zone):at;
 }
 static boolean occurs(int hour,int minute,Set<Integer> days,long at,ZoneId zone){LocalDate date=Instant.ofEpochMilli(at).atZone(zone).toLocalDate();return (days.isEmpty()||days.contains(date.getDayOfWeek().getValue()))&&date.atTime(hour,minute).atZone(zone).toInstant().toEpochMilli()==at;}
}
