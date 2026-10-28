package org.sleepwell.app;

import android.content.*;

/** Private immutable PendingIntent receiver; posts only through the silent reminder port. */
public final class TravelTrackingReminderReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c,Intent intent){if(intent==null||!TravelTrackingReminders.ACTION.equals(intent.getAction()))return;TravelTrackingReminders.Request request;try{request=TravelTrackingReminders.Request.parse(intent.getStringExtra("request"));if(!request.token().equals(intent.getIdentifier()))return;}catch(RuntimeException invalid){return;}PendingResult pending=goAsync();TravelTrackingReminders.receive(c,request,pending::finish);}
}
