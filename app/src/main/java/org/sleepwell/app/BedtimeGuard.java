package org.sleepwell.app;

import android.accessibilityservice.*;
import android.content.*;
import android.view.accessibility.*;
import java.time.*;
import java.util.*;

/** Window package names only. No content retrieval, key logging, or uninstall blocking. */
public class BedtimeGuard extends AccessibilityService {
    private long lastRedirect=0;
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if(event.getPackageName()==null || event.getEventType()!=AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        String pkg=event.getPackageName().toString();
        if(pkg.equals(getPackageName()) || pkg.startsWith("com.android.systemui") || pkg.equals("com.android.settings") || pkg.contains("dialer") || pkg.contains("incallui") || pkg.contains("permissioncontroller") || pkg.contains("emergency")) return;
        if(android.os.SystemClock.elapsedRealtime()-lastRedirect<1500) return;
        Set<String> blocked=Prefs.of(this).getStringSet("blocked",Set.of());
        if(Prefs.flag(this,"ringing") && Prefs.flag(this,"alarmGuard")) {
            lastRedirect=android.os.SystemClock.elapsedRealtime();
            startActivity(new Intent(this,AlarmActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)); return;
        }
        LocalTime now=LocalTime.now(); boolean bedtime=SleepMath.inBedtime(now.getHour()*60+now.getMinute(),Prefs.number(this,"bedtime",1350),Prefs.number(this,"wakeTime",420));
        if(Prefs.flag(this,"bedGuard") && bedtime && blocked.contains(pkg) && System.currentTimeMillis()>Prefs.of(this).getLong("focusPauseUntil",0)) {
            lastRedirect=android.os.SystemClock.elapsedRealtime();
            startActivity(new Intent(this,FocusActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
        }
    }
    @Override public void onInterrupt() {}
}
