package org.sleepwell.app;

import android.app.*;
import android.content.*;
import java.time.*;

public final class Scheduler {
    static final String FIRE="org.sleepwell.FIRE", TEST="org.sleepwell.TEST", SNOOZE="org.sleepwell.SNOOZE", CHECK="org.sleepwell.CHECK", BED="org.sleepwell.BED";
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
        if(a.enabled) { long next=a.next(System.currentTimeMillis()); at(c,a.id,FIRE,next); Prefs.put(c,"deadline_"+a.id,next); }
        else Prefs.put(c,"deadline_"+a.id,0L);
    }
    static void cancel(Context c,int id,String action) { c.getSystemService(AlarmManager.class).cancel(intent(c,id,action)); }
    static void all(Context c) {
        try(Store s=new Store(c)) { for(Alarm a:s.alarms()) schedule(c,a); }
        bedtime(c);
    }
    static void bedtime(Context c) {
        cancel(c,0,BED);
        if(!Prefs.flag(c,"bedReminder") || !allowed(c)) return;
        int t=Prefs.number(c,"bedtime",22*60+30);
        long next=SleepMath.nextOccurrence(t/60,t%60,java.util.Set.of(),System.currentTimeMillis(),ZoneId.systemDefault());
        c.getSystemService(AlarmManager.class).setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next,intent(c,0,BED));
    }
    static void test(Context c,Alarm source) {
        Alarm a=Alarm.read(source.json()); a.id=-1; a.volume=Math.min(25,a.volume); a.rampSeconds=0; a.awakeCheck=0; a.pin=false;
        Prefs.put(c,"test_alarm",a.json().toString()); at(c,-1,TEST,System.currentTimeMillis()+10000);
    }
    static void fire(Context c,Alarm a,boolean mainAlarm) {
        // Reserving the current occurrence before starting audio prevents smart/deadline races.
        synchronized(Scheduler.class) {
            if(Prefs.flag(c,"ringing")) return;
            if(mainAlarm) {
                cancel(c,a.id,FIRE); cancel(c,a.id,SNOOZE); cancel(c,a.id,CHECK);
                Prefs.put(c,"snoozes_"+a.id,0);
                try(Store s=new Store(c)) {
                    Alarm stored=s.alarm(a.id);
                    if(stored!=null) { if(stored.days.isEmpty()) stored.enabled=false; s.save(stored); schedule(c,stored); }
                }
            }
            Prefs.put(c,"active_alarm",a.json().toString()); Prefs.put(c,"ringing",true);
            c.startForegroundService(new Intent(c,AlarmService.class));
        }
    }
}
