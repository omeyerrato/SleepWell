package org.sleepwell.app;

import java.time.*;
import java.util.List;

/** Calendar triggers are calculated once while arming, then run against elapsed realtime. */
final class AutomaticStart {
    interface Projection {long next(Alarm alarm,long now,ZoneId zone);}
    static long schedule(AutomaticSleep.Mode mode,long now,ZoneId zone,int bedtime,List<Alarm> alarms){return schedule(mode,now,zone,bedtime,alarms,Alarm::next);}
    static long schedule(AutomaticSleep.Mode mode,long now,ZoneId zone,int bedtime,List<Alarm> alarms,Projection projection){
        if(mode==AutomaticSleep.Mode.STILLNESS)return now;
        if(mode==AutomaticSleep.Mode.BEDTIME){
            ZonedDateTime current=Instant.ofEpochMilli(now).atZone(zone);
            ZonedDateTime target=current.toLocalDate().atTime(bedtime/60,bedtime%60).atZone(zone);
            if(target.toInstant().toEpochMilli()<now)target=current.toLocalDate().plusDays(1).atTime(bedtime/60,bedtime%60).atZone(zone);
            return target.toInstant().toEpochMilli();
        }
        long target=Long.MAX_VALUE;
        for(Alarm alarm:alarms)if(alarm.enabled&&alarm.smartWindow>0&&alarm.nap==null&&!alarm.napInvalid&&!alarm.dateInvalid&&!alarm.solarInvalid&&!alarm.skipInvalid){
            long deadline=projection.next(alarm,now,zone);
            if(deadline<=now)continue; // Missing, expired or already-consumed occurrences cannot admit a recording.
            long at=deadline-(alarm.smartWindow+45L)*60_000;
            // An already-open lead-in can start immediately, but never attach to a past alarm.
            target=Math.min(target,Math.max(now,at));
        }
        if(target==Long.MAX_VALUE)throw new IllegalArgumentException("Enable an alarm with a smart wake-up window first.");
        return target;
    }
}
