package org.sleepwell.app;

import android.content.*;

/** Private PendingIntent destination; never starts playback, a foreground service, or a flashlight. */
public final class TravelReminderReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent){if(intent==null||!TravelReminders.ACTION.equals(intent.getAction()))return;PendingResult result=goAsync();Context app=context.getApplicationContext();new Thread(()->{try{TravelReminders.receive(app,new TravelReminders.Request(intent.getLongExtra("revision",0),intent.getLongExtra("at",0)));}catch(Exception e){TravelPlans.factory.open(app).edit().putString("travelDeliveryError","Travel reminders need attention. Open your travel plan.").apply();}finally{result.finish();}},"SleepWell-travel-reminder").start();}
}
