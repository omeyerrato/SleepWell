package org.sleepwell.app;

import android.content.SharedPreferences;

/** The one configured-to-consumed comparison used by display and actual delivery. */
final class SnoozeSource {
    static boolean valid(Alarm alarm){return alarm!=null&&alarm.id>0&&!alarm.nextOverrideInvalid&&!alarm.repeatInvalid&&alarm.repeatRule.validFor(alarm)&&!alarm.napInvalid&&!alarm.skipInvalid&&!alarm.routineInvalid&&!alarm.dateInvalid&&!alarm.solarInvalid&&!alarm.excludedDatesInvalid&&!alarm.snoozeTotalInvalid&&!alarm.weeklyNapOwnerInvalid;}
    static boolean same(SharedPreferences prefs,Alarm source,Alarm saved){return same(RandomChallenges.configured(prefs,source),saved);}
    private static boolean same(Alarm source,Alarm saved){
        if(!valid(source)||!valid(saved)||source.id!=saved.id)return false;
        if(source.nap!=null)return source.nap.phase==NapPlan.Phase.CLAIMED&&!source.enabled&&source.json().toString().equals(saved.json().toString());
        Alarm consumed=Alarm.read(source.json());AlarmSkips.consume(consumed);if(consumed.days.isEmpty())consumed.enabled=false;
        return consumed.json().toString().equals(saved.json().toString());
    }
    private SnoozeSource(){}
}
