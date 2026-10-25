package org.sleepwell.app;
import android.content.*;
/** Restores only future explicitly saved reminders after protected system events. */
public final class TripSleepBootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent){if(intent==null||!TravelBootReceiver.ACTIONS.contains(intent.getAction()))return;PendingResult result=goAsync();Context app=context.getApplicationContext();new Thread(()->{try{TripSleepRuntime.reconcile(app);}catch(RuntimeException failure){try{new TripSleepStore(app).error("Travel sleep reminders need attention after the system changed.");}catch(RuntimeException ignored){android.util.Log.e("SleepWell","Travel sleep recovery unavailable",failure);}}finally{result.finish();}},"SleepWell-travel-sleep-restore").start();}
}
