package org.sleepwell.app;
import android.content.*;
/** Private immutable PendingIntent destination. Consumes receipts before silent notification output. */
public final class TripSleepReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent){if(intent==null||!TripSleepRuntime.ACTION.equals(intent.getAction()))return;String token=intent.getIdentifier();if(token==null||token.length()>160)return;PendingResult result=goAsync();Context app=context.getApplicationContext();new Thread(()->{try{TripSleepRuntime.receive(app,token);}catch(RuntimeException failure){try{new TripSleepStore(app).error("Travel sleep reminders need attention. Open the plan to retry.");}catch(RuntimeException ignored){android.util.Log.e("SleepWell","Could not store travel sleep reminder error",failure);}}finally{result.finish();}},"SleepWell-travel-sleep").start();}
}
