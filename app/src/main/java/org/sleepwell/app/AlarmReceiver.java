package org.sleepwell.app;

import android.app.*;
import android.content.*;
import org.json.*;

public class AlarmReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c,Intent intent) {
        Notifications.channels(c); String action=intent.getAction(); int id=intent.getIntExtra("id",0);
        if(Scheduler.BACKUP.equals(action)) {
            if(Prefs.flag(c,"ringing")){Prefs.put(c,"forceBackup",true);c.startForegroundService(new Intent(c,AlarmService.class));}return;
        }
        if(Scheduler.BED.equals(action)) {
            c.getSystemService(NotificationManager.class).notify(300,new Notification.Builder(c,Notifications.BED)
                .setSmallIcon(R.drawable.ic_moon).setContentTitle("Tomorrow starts tonight")
                .setContentText("Put your phone down. Start your SleepWell night.").setContentIntent(Notifications.home(c)).setAutoCancel(true).build());
            Scheduler.bedtime(c); return;
        }
        try(Store s=new Store(c)) {
            Alarm a;
            if(Scheduler.TEST.equals(action)) a=Alarm.read(new JSONObject(Prefs.text(c,"test_alarm","{}")));
            else if(Scheduler.SNOOZE.equals(action)) a=Alarm.read(new JSONObject(Prefs.text(c,"snoozed_"+id,"{}")));
            else if(Scheduler.CHECK.equals(action)) {
                if(Prefs.number(c,"checkPending",0)!=id) return;
                Prefs.put(c,"checkPending",0); a=Alarm.read(new JSONObject(Prefs.text(c,"check_alarm","{}")));
            } else if(Scheduler.FIRE.equals(action)) { a=s.alarm(id); if(a==null || !a.enabled) return; }
            else return;
            Scheduler.fire(c,a,Scheduler.FIRE.equals(action));
        } catch(Exception e) { Prefs.put(c,"lastError","Alarm delivery: "+e.getMessage()); }
    }
}
