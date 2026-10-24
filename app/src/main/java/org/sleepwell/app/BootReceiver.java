package org.sleepwell.app;

import android.content.*;
import java.util.Set;

public class BootReceiver extends BroadcastReceiver {
    private static final Set<String> TRUSTED=Set.of(Intent.ACTION_BOOT_COMPLETED,Intent.ACTION_TIME_CHANGED,Intent.ACTION_TIMEZONE_CHANGED,Intent.ACTION_MY_PACKAGE_REPLACED,"android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED");
    @Override public void onReceive(Context c,Intent i) {
        if(i.getAction()==null || !TRUSTED.contains(i.getAction())) return;
        try { Scheduler.all(c); } catch(SecurityException e) { Prefs.put(c,"lastError","Exact alarms need permission after restart."); }
        if(Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())) {
            Prefs.put(c,"ringing",false); Prefs.put(c,"tracking",false);Prefs.put(c,"trackingPaused",false); Prefs.put(c,"session",0L);
            try(Store s=new Store(c)) {
                s.recoverInterrupted();
            }
        }
        WakeChecks.reconcile(c);
    }
}
