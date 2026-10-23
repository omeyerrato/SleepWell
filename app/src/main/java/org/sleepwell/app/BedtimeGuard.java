package org.sleepwell.app;

import android.accessibilityservice.*;
import android.content.*;
import android.view.accessibility.*;
import java.time.*;
import java.util.*;

/** Window package/class/ID metadata only. No content retrieval, key logging, or uninstall blocking. */
public class BedtimeGuard extends AccessibilityService {
    private long lastRedirect=0;
    private final android.os.Handler main=new android.os.Handler(android.os.Looper.getMainLooper());
    private PowerMenuGuard powerMenu;private boolean connected;
    @Override protected void onServiceConnected(){super.onServiceConnected();if(powerMenu!=null)powerMenu.close();main.removeCallbacksAndMessages(null);connected=true;powerMenu=new PowerMenuGuard((delay,callback)->main.postDelayed(callback,delay),()->connected&&AlarmProtections.realAlarm(this)&&AlarmProtections.enabled(this,AlarmProtections.POWER),()->performGlobalAction(GLOBAL_ACTION_BACK),this::returnToAlarm,android.os.SystemClock::elapsedRealtime);}
    private void returnToAlarm(){if(!connected||!AlarmProtections.realAlarm(this))return;try{startActivity(new Intent(this,AlarmActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));}catch(RuntimeException e){Prefs.put(this,"lastError","The system did not allow returning to the alarm screen.");}}
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if(event.getEventType()==AccessibilityEvent.TYPE_VIEW_FOCUSED){if(powerMenu!=null)powerMenu.focus(event.getWindowId());return;}
        if(event.getEventType()!=AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)return;
        if(powerMenu!=null)powerMenu.window(event.getPackageName()==null?null:event.getPackageName().toString(),event.getClassName()==null?null:event.getClassName().toString(),event.getWindowId());
        if(event.getPackageName()==null)return;
        String pkg=event.getPackageName().toString();
        if(pkg.equals(getPackageName()) || PowerMenuGuard.systemPackage(pkg) || pkg.equals("com.android.settings") || pkg.contains("dialer") || pkg.contains("incallui") || pkg.contains("permissioncontroller") || pkg.contains("emergency")) return;
        if(android.os.SystemClock.elapsedRealtime()-lastRedirect<1500) return;
        Set<String> blocked=Prefs.of(this).getStringSet("blocked",Set.of());
        if(AlarmProtections.realAlarm(this) && AlarmProtections.enabled(this,AlarmProtections.LEAVING)) {
            lastRedirect=android.os.SystemClock.elapsedRealtime();
            returnToAlarm();return;
        }
        SleepPlan plan=SleepPlans.load(this);LocalTime now=LocalTime.now(); boolean bedtime=plan.guardActive(now.getHour()*60+now.getMinute());
        if(Prefs.flag(this,"bedGuard") && bedtime && blocked.contains(pkg) && System.currentTimeMillis()>Prefs.of(this).getLong("focusPauseUntil",0)) {
            lastRedirect=android.os.SystemClock.elapsedRealtime();
            startActivity(new Intent(this,FocusActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
        }
    }
    @Override public void onInterrupt() {if(powerMenu!=null)powerMenu.cancel();}
    @Override public boolean onUnbind(Intent intent){connected=false;if(powerMenu!=null)powerMenu.close();main.removeCallbacksAndMessages(null);return super.onUnbind(intent);}
    @Override public void onDestroy(){connected=false;if(powerMenu!=null)powerMenu.close();main.removeCallbacksAndMessages(null);super.onDestroy();}
}
