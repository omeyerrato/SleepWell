package org.sleepwell.app;
import java.time.*;

/** One local calendar date. Zone/DST resolution matches ordinary local-time alarms. */
final class AlarmDate {
    static LocalDate read(Object raw){if(!(raw instanceof String))throw new IllegalArgumentException("Alarm date must be an ISO date");try{LocalDate date=LocalDate.parse((String)raw);if(date.getYear()<1970||date.getYear()>9999||!date.toString().equals(raw))throw new IllegalArgumentException("Unsupported alarm date");return date;}catch(DateTimeException invalid){throw new IllegalArgumentException("Invalid alarm date",invalid);}}
    static long at(LocalDate date,int hour,int minute,ZoneId zone){return date.atTime(hour,minute).atZone(zone).toInstant().toEpochMilli();}
    static long next(LocalDate date,int hour,int minute,long now,ZoneId zone){long at=at(date,hour,minute,zone);return at>now?at:0;}
    private AlarmDate(){}
}
