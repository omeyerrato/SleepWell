package org.sleepwell.app;
import android.content.*;
import java.util.Set;
/** Protected system events restore future schedules, never start tracking directly. */
public final class RecurringTrackingBootReceiver extends BroadcastReceiver {
    static final Set<String> ACTIONS=Set.of(Intent.ACTION_BOOT_COMPLETED,Intent.ACTION_MY_PACKAGE_REPLACED,Intent.ACTION_TIME_CHANGED,Intent.ACTION_TIMEZONE_CHANGED,"android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED");
    @Override public void onReceive(Context context,Intent intent){if(intent==null||intent.getAction()==null||!ACTIONS.contains(intent.getAction()))return;try{RecurringTracking.recover(context.getApplicationContext(),RecurringTracking.port,Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()));}catch(RuntimeException failure){android.util.Log.e("SleepWell","Nightly schedule reconciliation failed",failure);}}
}
