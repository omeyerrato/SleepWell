package org.sleepwell.app;

import android.app.*;
import android.content.*;

final class Notifications {
    static final String ALARM="alarm_v1", TRACK="tracking_v1", BED="bedtime_v1", SOUND="sleep_sounds_v1";
    static void channels(Context c) {
        NotificationManager nm=c.getSystemService(NotificationManager.class);
        NotificationChannel alarm=new NotificationChannel(ALARM,"Wake-up alarms",NotificationManager.IMPORTANCE_HIGH);
        alarm.setDescription("Wake-up screen; sound and vibration are controlled by the alarm service.");
        alarm.setSound(null,null); alarm.enableVibration(false); alarm.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        nm.createNotificationChannel(alarm);
        nm.createNotificationChannel(new NotificationChannel(TRACK,"Overnight tracking",NotificationManager.IMPORTANCE_LOW));
        NotificationChannel sounds=new NotificationChannel(SOUND,"Sleep sounds",NotificationManager.IMPORTANCE_LOW);
        sounds.setSound(null,null);sounds.enableVibration(false);nm.createNotificationChannel(sounds);
        nm.createNotificationChannel(new NotificationChannel(BED,"Bedtime reminders",NotificationManager.IMPORTANCE_DEFAULT));
    }
    static PendingIntent home(Context c) { return PendingIntent.getActivity(c,0,new Intent(c,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE); }
}
