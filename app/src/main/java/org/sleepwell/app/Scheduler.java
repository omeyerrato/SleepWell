package org.sleepwell.app;

import android.app.*;
import android.content.*;
import java.time.*;

public final class Scheduler {
    static final String FIRE="org.sleepwell.FIRE", TEST="org.sleepwell.TEST", SNOOZE="org.sleepwell.SNOOZE", CHECK="org.sleepwell.CHECK", BED="org.sleepwell.BED", BACKUP="org.sleepwell.BACKUP";
    static boolean allowed(Context c) { return android.os.Build.VERSION.SDK_INT<31 || c.getSystemService(AlarmManager.class).canScheduleExactAlarms(); }
    static PendingIntent intent(Context c,int id,String action) {
        return intent(c,id,action,Prefs.text(c,"alarmRun",""));
    }
    static PendingIntent intent(Context c,int id,String action,String run) {
        Intent intent=new Intent(c,AlarmReceiver.class).setAction(action).putExtra("id",id);if(BACKUP.equals(action))intent.setIdentifier(backupIdentity(id,run)).putExtra("alarmRun",run);
        return PendingIntent.getBroadcast(c,id,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    static String backupIdentity(int id,String run){return "sleepwell-backup:"+id+":"+run;}
    static void cancelBackup(Context c,int id,String run){c.getSystemService(AlarmManager.class).cancel(intent(c,id,BACKUP,run));}
    static void at(Context c,int id,String action,long time) {
        if(!allowed(c)) throw new SecurityException("Enable Alarms & reminders in Setup first.");
        PendingIntent operation=FIRE.equals(action)?PendingIntent.getBroadcast(c,id,new Intent(c,AlarmReceiver.class).setAction(action).putExtra("id",id).putExtra("occurrence",time),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE):intent(c,id,action);
        c.getSystemService(AlarmManager.class).setAlarmClock(new AlarmManager.AlarmClockInfo(time,Notifications.home(c)),operation);
    }
    static synchronized void schedule(Context c,Alarm a) {try{
        cancel(c,a.id,FIRE);
        if(a.nap!=null){Naps.reconcile(c,a.id);return;}
        if(a.napInvalid){Prefs.put(c,"lastError","This nap timer could not be read. Review it before starting.");return;}
        if(a.solarInvalid){Prefs.put(c,"deadline_"+a.id,0L);throw new IllegalArgumentException("This sunrise time needs review.");}
        if(a.dateInvalid){Prefs.put(c,"deadline_"+a.id,0L);throw new IllegalArgumentException("This alarm date needs review.");}
        if(a.skipInvalid){Prefs.put(c,"deadline_"+a.id,0L);Prefs.put(c,"lastError","A skipped alarm occurrence needs review.");return;}
        if(a.enabled) { long now=System.currentTimeMillis(),next=a.next(Math.max(now,Prefs.of(c).getLong("lastOccurrence_"+a.id,0)));if(next<=0){CalendarAlarms.scheduled(c,a,CalendarPolicy.ordinary(0,""));Prefs.put(c,"deadline_"+a.id,0L);throw new IllegalArgumentException("That alarm date has passed. Choose a future date.");}CalendarPolicy.Plan plan=CalendarAlarms.plan(c,a,next,now);CalendarAlarms.scheduled(c,a,plan);at(c,a.id,FIRE,plan.deadline);Prefs.put(c,"deadline_"+a.id,plan.deadline); }
        else {CalendarAlarms.scheduled(c,a,CalendarPolicy.ordinary(0,""));Prefs.put(c,"deadline_"+a.id,0L);}
    }finally{HomeSurfaces.changed(c);}}
    static void cancel(Context c,int id,String action) { if(CHECK.equals(action))WakeChecks.cancelForAlarm(c,id);c.getSystemService(AlarmManager.class).cancel(intent(c,id,action)); }
    static void all(Context c) {
        try(Store s=new Store(c)) { for(Alarm a:s.alarms())try{schedule(c,a);}catch(RuntimeException failed){Prefs.put(c,"lastError","Alarm "+a.id+" needs scheduling attention.");} }
        bedtime(c);
    }
    static void bedtime(Context c) {
        cancel(c,0,BED);
        SleepPlan plan=SleepPlans.load(c);
        SmartBedtimeRuntime.reconcile(c);if(plan.smartBedtime)return;
        if(!plan.reminder){Prefs.of(c).edit().remove("bedReminderAt").apply();return;}
        if(!allowed(c)){Prefs.put(c,"bedReminderAt",0L);return;}
        long next=plan.nextReminder(System.currentTimeMillis(),ZoneId.systemDefault());
        c.getSystemService(AlarmManager.class).setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next,intent(c,0,BED));Prefs.put(c,"bedReminderAt",next);
    }
    static void test(Context c,Alarm source) {
        Alarm a=Alarm.read(source.json()); a.id=-1; a.volume=Math.min(25,a.volume); a.rampSeconds=0; a.awakeCheck=0; a.pin=false;a.backupMinutes=0;a.vibrate=false;a.extraLoud=false;a.sunrise=false;a.flashlight=false;a.soundDelaySeconds=0;a.inputs=AlarmInputs.DEFAULT;a.inputsInvalid=false;a.soundMode=AlarmSoundMode.SINGLE;a.soundModeInvalid=false;a.timing=AlarmTiming.DEFAULT;a.timingInvalid=false;
        Prefs.put(c,"snoozes_-1",0);Prefs.put(c,"test_alarm",a.json().toString()); at(c,-1,TEST,System.currentTimeMillis()+10000);
    }
    static void fire(Context c,Alarm a,boolean mainAlarm) {
        BackupRuntime.admitted();
        // Reserving the current occurrence before starting audio prevents smart/deadline races.
        synchronized(Scheduler.class) {
            boolean alreadyRinging=Prefs.flag(c,"ringing");
            if(mainAlarm) {
                // Re-read under the same lock as Skip/Undo: stale smart-wake snapshots cannot bypass a saved skip.
                try(Store store=Records.open(c)){Alarm current=store.alarm(a.id);long deadline=Prefs.of(c).getLong("deadline_"+a.id,0),now=System.currentTimeMillis();if(!CalendarAlarms.eligible(c,current,deadline,now,Prefs.of(c).getLong("lastOccurrence_"+a.id,0),ZoneId.systemDefault()))return;a=current;}
                long occurrence=Prefs.of(c).getLong("deadline_"+a.id,System.currentTimeMillis());Prefs.put(c,"lastOccurrence_"+a.id,Math.max(CalendarAlarms.consumed(c,a,occurrence),System.currentTimeMillis()));
                cancel(c,a.id,FIRE); cancel(c,a.id,SNOOZE); cancel(c,a.id,CHECK);
                Prefs.put(c,"snoozes_"+a.id,0);
                try(Store s=new Store(c)) {
                    Alarm stored=s.alarm(a.id);
                    if(stored!=null) { AlarmSkips.clear(stored);if(stored.days.isEmpty()) stored.enabled=false; s.save(stored); schedule(c,stored); }
                }
            }
            // Overlapping scheduled alarms share the currently active challenge, but both schedules advance.
            if(alreadyRinging)return;
            WakeChecks.supersede(c);
            Prefs.put(c,"challengeCompleted",0);Prefs.put(c,"mathTask","");Prefs.put(c,"retypeTask","");
            Prefs.of(c).edit().putLong("alarmStartedElapsed",android.os.SystemClock.elapsedRealtime()).putString("alarmRun",java.util.UUID.randomUUID().toString()).putBoolean("forceBackup",false).putString("active_alarm",a.json().toString()).putBoolean("ringing",true).apply();
            if(a.backupMinutes>0 && allowed(c))at(c,a.id,BACKUP,System.currentTimeMillis()+a.backupMinutes*60000L);
            c.startForegroundService(new Intent(c,AlarmService.class));
        }
    }
}
