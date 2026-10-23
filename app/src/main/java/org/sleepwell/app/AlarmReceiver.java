package org.sleepwell.app;

import android.app.*;
import android.content.*;
import org.json.*;

public class AlarmReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c,Intent intent) {
        Notifications.channels(c); String action=intent.getAction(); int id=intent.getIntExtra("id",0);
        if(Scheduler.BACKUP.equals(action)) {
            try{Alarm current=Alarm.read(new JSONObject(Prefs.text(c,"active_alarm","{}")));String run=Prefs.text(c,"alarmRun","");if(backupMatches(Prefs.flag(c,"ringing"),id,current.id,run,intent.getStringExtra("alarmRun"))){Prefs.put(c,"forceBackup",true);c.startForegroundService(new Intent(c,AlarmService.class));}}catch(JSONException ignored){}return;
        }
        if(Scheduler.BED.equals(action)) {
            long deadline=Prefs.of(c).getLong("bedReminderAt",0);if(!SleepPlans.load(c).reminder||deadline<=0||System.currentTimeMillis()<deadline-1000)return;
            if(TrackingService.running){Scheduler.bedtime(c);return;}
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
    static boolean backupMatches(boolean ringing,int incoming,int active,String run,String incomingRun){return ringing&&active>0&&incoming==active&&!run.isEmpty()&&run.equals(incomingRun);}
}
