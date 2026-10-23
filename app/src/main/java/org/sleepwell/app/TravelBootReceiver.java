package org.sleepwell.app;

import android.content.*;
import java.util.Set;

/** Restores future saved reminders after protected system events; missed cues are never backfilled. */
public final class TravelBootReceiver extends BroadcastReceiver {
    static final Set<String> ACTIONS=Set.of(Intent.ACTION_BOOT_COMPLETED,Intent.ACTION_MY_PACKAGE_REPLACED,Intent.ACTION_TIME_CHANGED,Intent.ACTION_TIMEZONE_CHANGED);
    @Override public void onReceive(Context context,Intent intent){if(intent==null||intent.getAction()==null||!ACTIONS.contains(intent.getAction()))return;PendingResult result=goAsync();Context app=context.getApplicationContext();new Thread(()->{try{if(TravelPlans.repository(app).load(TravelReminders.clock.getAsLong()).revision>0)TravelReminders.apply(app);}catch(Exception e){TravelPlans.factory.open(app).edit().putString("travelDeliveryError","Travel reminders need attention after a system change.").apply();}finally{result.finish();}},"SleepWell-travel-restore").start();}
}
