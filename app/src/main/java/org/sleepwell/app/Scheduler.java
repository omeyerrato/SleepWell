package org.sleepwell.app;

import android.app.*;
import android.content.*;
import java.time.*;

public final class Scheduler {
    static final String FIRE="org.sleepwell.FIRE", TEST="org.sleepwell.TEST", SNOOZE="org.sleepwell.SNOOZE", CHECK="org.sleepwell.CHECK", BED="org.sleepwell.BED", BACKUP="org.sleepwell.BACKUP";
    static boolean allowed(Context c) { return android.os.Build.VERSION.SDK_INT<31 || c.getSystemService(AlarmManager.class).canScheduleExactAlarms(); }
    static PendingIntent intent(Context c,int id,String action) {
        return PendingIntent.getBroadcast(c, id, new Intent(c,AlarmReceiver.class).setAction(action).putExtra("id",id),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    static void at(Context c,int id,String action,long time) {
        if(!allowed(c)) throw new SecurityException("Enable Alarms & reminders in Setup first.");
        c.getSystemService(AlarmManager.class).setAlarmClock(new AlarmManager.AlarmClockInfo(time,Notifications.home(c)),intent(c,id,action));
    }
    static void schedule(Context c,Alarm a) {
        cancel(c,a.id,FIRE);
        if(a.enabled) { long next=a.next(Math.max(System.currentTimeMillis(),Prefs.of(c).getLong("lastOccurrence_"+a.id,0))); at(c,a.id,FIRE,next); Prefs.put(c,"deadline_"+a.id,next); }
        else Prefs.put(c,"deadline_"+a.id,0L);
    }
    static void cancel(Context c,int id,String action) { c.getSystemService(AlarmManager.class).cancel(intent(c,id,action)); }
    static void all(Context c) {
        try(Store s=new Store(c)) { for(Alarm a:s.alarms()) schedule(c,a); }
        bedtime(c);
    }
    static void bedtime(Context c) {
        cancel(c,0,BED);
        SleepPlan plan=SleepPlans.load(c);
        if(!plan.reminder){Prefs.of(c).edit().remove("bedReminderAt").apply();return;}
        if(!allowed(c)){Prefs.put(c,"bedReminderAt",0L);return;}
        long next=plan.nextReminder(System.currentTimeMillis(),ZoneId.systemDefault());
        c.getSystemService(AlarmManager.class).setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next,intent(c,0,BED));Prefs.put(c,"bedReminderAt",next);
    }
    static void test(Context c,Alarm source) {
        Alarm a=Alarm.read(source.json()); a.id=-1; a.volume=Math.min(25,a.volume); a.rampSeconds=0; a.awakeCheck=0; a.pin=false;a.backupMinutes=0;a.vibrate=false;
        Prefs.put(c,"snoozes_-1",0);Prefs.put(c,"test_alarm",a.json().toString()); at(c,-1,TEST,System.currentTimeMillis()+10000);
    }
    static void fire(Context c,Alarm a,boolean mainAlarm) {
        // Reserving the current occurrence before starting audio prevents smart/deadline races.
        synchronized(Scheduler.class) {
            boolean alreadyRinging=Prefs.flag(c,"ringing");
            if(mainAlarm) {
                long occurrence=Prefs.of(c).getLong("deadline_"+a.id,System.currentTimeMillis());Prefs.put(c,"lastOccurrence_"+a.id,Math.max(occurrence,System.currentTimeMillis()));
                cancel(c,a.id,FIRE); cancel(c,a.id,SNOOZE); cancel(c,a.id,CHECK);
                Prefs.put(c,"snoozes_"+a.id,0);
                try(Store s=new Store(c)) {
                    Alarm stored=s.alarm(a.id);
                    if(stored!=null) { if(stored.days.isEmpty()) stored.enabled=false; s.save(stored); schedule(c,stored); }
                }
            }
            // Overlapping scheduled alarms share the currently active challenge, but both schedules advance.
            if(alreadyRinging)return;
            Prefs.put(c,"challengeCompleted",0);Prefs.put(c,"mathTask","");Prefs.put(c,"retypeTask","");
            Prefs.put(c,"forceBackup",false);Prefs.put(c,"active_alarm",a.json().toString()); Prefs.put(c,"ringing",true);
            if(a.backupMinutes>0 && allowed(c))at(c,a.id,BACKUP,System.currentTimeMillis()+a.backupMinutes*60000L);
            c.startForegroundService(new Intent(c,AlarmService.class));
        }
    }
}
