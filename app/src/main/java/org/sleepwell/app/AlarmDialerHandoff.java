package org.sleepwell.app;

import android.content.Context;
import android.os.SystemClock;

/** A process-local exact-dialer exception for an explicit current-occurrence handoff. */
final class AlarmDialerHandoff {
    static final long MAX_MILLIS=120000;
    record Lease(String run,int unit,String pkg,long issued){boolean matches(String current,int completed,String packageName,long now){return run.equals(current)&&unit==completed&&pkg.equals(packageName)&&now>=issued&&now-issued<MAX_MILLIS;}}
    private static volatile Lease lease;
    static void begin(Context context,String run,int unit,String pkg){if(!AlarmService.current(context,run)||Prefs.number(context,"challengeCompleted",0)!=unit)throw new IllegalStateException("This alarm occurrence changed. Return to its current task.");lease=new Lease(run,unit,pkg,SystemClock.elapsedRealtime());}
    static boolean allows(Context context,String pkg){Lease current=lease;if(current==null)return false;boolean allowed=current.matches(Prefs.text(context,"alarmRun",""),Prefs.number(context,"challengeCompleted",0),pkg,SystemClock.elapsedRealtime())&&AlarmService.current(context,current.run);if(!allowed&&(SystemClock.elapsedRealtime()-current.issued>=MAX_MILLIS||!AlarmService.current(context,current.run)||Prefs.number(context,"challengeCompleted",0)!=current.unit))clear(current.run);return allowed;}
    static void clear(String run){Lease current=lease;if(current!=null&&current.run.equals(run))lease=null;}
    private AlarmDialerHandoff(){}
}
