package org.sleepwell.app;

import android.content.Context;

/** Explicit scheduling boundary; configuration-only device tests substitute a silent recorder. */
final class AlarmScheduling {
    interface Port {boolean allowed(Context context);void schedule(Context context,Alarm alarm);}
    static Port port=new Port(){public boolean allowed(Context context){return Scheduler.allowed(context);}public void schedule(Context context,Alarm alarm){Prefs.put(context,"lastOccurrence_"+alarm.id,0L);Scheduler.schedule(context,alarm);}};
}
