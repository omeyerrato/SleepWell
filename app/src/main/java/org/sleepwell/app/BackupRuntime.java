package org.sleepwell.app;

import android.content.Context;
import java.util.concurrent.atomic.AtomicLong;

/** Non-blocking admission signal. Runtime entry never waits for document work. */
final class BackupRuntime {
    private static final AtomicLong admissions=new AtomicLong();
    static void admitted(){admissions.getAndUpdate(value->value==Long.MAX_VALUE?Long.MAX_VALUE:value+1);}
    static long generation(){return admissions.get();}
    static boolean alarmDelivery(String action,int id){return id>0&&(Scheduler.FIRE.equals(action)||Scheduler.SNOOZE.equals(action)||Scheduler.CHECK.equals(action))||id==-1&&Scheduler.TEST.equals(action);}
    static boolean nonce(String value){if(value==null)return false;try{return java.util.UUID.fromString(value).toString().equals(value);}catch(IllegalArgumentException invalid){return false;}}
    static void requireIdle(Context context,long expected){
        if(expected==Long.MAX_VALUE||expected!=generation()||TrackingService.running||AlarmService.active||Prefs.flag(context,"ringing")||Prefs.flag(context,"tracking"))throw new IllegalStateException("Tracking or an alarm started. Review the backup again after it ends.");
    }
    private BackupRuntime(){}
}
