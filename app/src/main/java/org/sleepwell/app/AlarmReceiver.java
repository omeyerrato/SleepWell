package org.sleepwell.app;

import android.app.*;
import android.content.*;
import org.json.*;

public class AlarmReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c,Intent intent) {
        if(intent==null)return;
        if(BackupRuntime.alarmDelivery(intent.getAction(),intent.getIntExtra("id",0)))BackupRuntime.admitted();
        Notifications.channels(c); String action=intent.getAction(); int id=intent.getIntExtra("id",0);
        if(Scheduler.CHECK.equals(action)){WakeChecks.legacy(c,id);return;}
        if(Scheduler.BACKUP.equals(action)) {
            try{Alarm current=Alarm.read(new JSONObject(Prefs.text(c,"active_alarm","{}")));String run=Prefs.text(c,"alarmRun","");if(backupMatches(Prefs.flag(c,"ringing"),id,current.id,run,intent.getStringExtra("alarmRun"))){Prefs.put(c,"forceBackup",true);c.startForegroundService(new Intent(c,AlarmService.class));}}catch(JSONException ignored){}return;
        }
        if(Scheduler.BED.equals(action)) {PendingResult pending=goAsync();SmartBedtimeRuntime.deliverFixed(c,Prefs.of(c).getLong("bedReminderAt",0),pending::finish);return;}

        if(Scheduler.SNOOZE.equals(action)){try{synchronized(Scheduler.class){Alarm snoozed=SnoozeDelivery.admit(c,id,intent,System.currentTimeMillis());if(snoozed!=null)Scheduler.fire(c,snoozed,false,true);}}catch(Exception failure){Prefs.put(c,"lastError","Snooze delivery could not be admitted. Review this alarm.");}return;}
        try(Store s=new Store(c)) {
            Alarm a;
            if(Scheduler.TEST.equals(action)) a=Alarm.read(new JSONObject(Prefs.text(c,"test_alarm","{}")));
            else if(Scheduler.FIRE.equals(action)) { a=s.alarm(id); if(a==null || !a.enabled) return; if(a.nap!=null||a.napInvalid){if(a.nap!=null)Naps.reconcile(c,a.id);return;} }
            else return;
            synchronized(Scheduler.class){
                if(Scheduler.FIRE.equals(action)){long deadline=Prefs.of(c).getLong("deadline_"+id,0);long delivered=intent.getLongExtra("occurrence",0);if(!acceptCalendar(delivered,deadline,System.currentTimeMillis()))return;}
                Scheduler.fire(c,a,Scheduler.FIRE.equals(action),Scheduler.SNOOZE.equals(action));
            }
        } catch(Exception e) { Prefs.put(c,"lastError","Alarm delivery: "+e.getMessage()); }
    }
    static boolean acceptCalendar(long delivered,long deadline,long now){return deadline>0&&(delivered==0||delivered==deadline)&&now>=deadline-1000;}
    static boolean backupMatches(boolean ringing,int incoming,int active,String run,String incomingRun){return ringing&&active>0&&incoming==active&&!run.isEmpty()&&run.equals(incomingRun);}
}
