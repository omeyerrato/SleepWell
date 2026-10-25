package org.sleepwell.app;

import java.time.*;import java.util.*;

/** Optional time basis. Ordinary hour/minute is an explicitly acknowledged same-zone fallback. */
final class SunriseSchedule {
    final double latitude,longitude;final int offset;
    SunriseSchedule(double latitude,double longitude,int offset,boolean acknowledged){if(!acknowledged||!Double.isFinite(latitude)||!Double.isFinite(longitude)||latitude < -90||latitude>90||longitude < -180||longitude>180||offset < -120||offset>120)throw new IllegalArgumentException("Enter valid coordinates, a −120 to +120 min offset and confirm the fallback time.");this.latitude=latitude==0?0:latitude;this.longitude=longitude==0?0:longitude;this.offset=offset;}
    String key(){return "solar-v1:"+Double.toHexString(latitude)+":"+Double.toHexString(longitude)+":"+offset;}
    static final class Result {final long at;final boolean fallback;final String reason;Result(long at,boolean fallback,String reason){this.at=at;this.fallback=fallback;this.reason=reason;}}
    Result on(LocalDate date,int hour,int minute,ZoneId zone){Long sunrise=SunriseMath.at(date,latitude,longitude,zone);if(sunrise!=null){long at=sunrise+offset*60000L;if(Instant.ofEpochMilli(at).atZone(zone).toLocalDate().equals(date))return new Result(at,false,"Calculated sunrise"+ (offset==0?"":offset<0?" − "+(-offset)+" min":" + "+offset+" min"));return fallback(date,hour,minute,zone,"Offset crosses the selected date");}return fallback(date,hour,minute,zone,date.getYear()<2000||date.getYear()>2100?"Outside supported solar years":"No calculated sunrise on this date");}
    private Result fallback(LocalDate date,int hour,int minute,ZoneId zone,String reason){return new Result(AlarmDate.at(date,hour,minute,zone),true,reason+" · using your fallback time");}
    long next(int hour,int minute,Set<Integer> days,LocalDate date,long now,ZoneId zone,AlarmSkip skip){if(date!=null){long at=on(date,hour,minute,zone).at;return at>now&&(skip==null||!skip.excludes(at,zone))?at:0;}LocalDate start=Instant.ofEpochMilli(now).atZone(zone).toLocalDate();for(int n=0;n<16;n++){LocalDate day=start.plusDays(n);if(!day.atStartOfDay(zone).toLocalDate().equals(day)||!days.isEmpty()&&!days.contains(day.getDayOfWeek().getValue()))continue;long at=on(day,hour,minute,zone).at;if(at>now&&(skip==null||!skip.excludes(at,zone)))return at;}throw new IllegalArgumentException("No valid sunrise weekday");}
    boolean occurs(int hour,int minute,Set<Integer> days,LocalDate selected,long at,ZoneId zone){LocalDate day=selected==null?Instant.ofEpochMilli(at).atZone(zone).toLocalDate():selected;return (selected==null||selected.equals(day))&&(days.isEmpty()||days.contains(day.getDayOfWeek().getValue()))&&on(day,hour,minute,zone).at==at;}
}
