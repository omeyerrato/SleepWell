package org.sleepwell.app;

import java.time.*;

/** Calendar occurrence calculation, independent of wall-clock polling and Android services. */
final class RecurringNight {
    static final long GRACE=5*60_000L;
    static final class Occurrence {
        final String date,zone;final long at;
        Occurrence(LocalDate day,ZoneId zone,int minute){this.date=day.toString();this.zone=zone.getId();at=day.atTime(minute/60,minute%60).atZone(zone).toInstant().toEpochMilli();}
    }
    static Occurrence next(long now,ZoneId zone,int minute,String lastDate){
        if(minute<0||minute>=1440)throw new IllegalArgumentException("Invalid start time");
        LocalDate day=Instant.ofEpochMilli(now).atZone(zone).toLocalDate();
        if(lastDate!=null&&!lastDate.isEmpty()){LocalDate last=LocalDate.parse(lastDate);if(!day.isAfter(last))day=last.plusDays(1);}
        Occurrence next=new Occurrence(day,zone,minute);
        if(next.at<=now)next=new Occurrence(day.plusDays(1),zone,minute);
        return next;
    }
    static boolean due(long now,long scheduled){return scheduled>0&&now>=scheduled&&now-scheduled<=GRACE;}
    static boolean notificationsVisible(boolean globallyAllowed,boolean permission,Integer channelImportance){return globallyAllowed&&permission&&(channelImportance==null||channelImportance>0);}
    private RecurringNight(){}
}
