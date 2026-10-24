package org.sleepwell.app;

import android.app.job.*;
import android.content.*;
import android.os.CancellationSignal;
import android.provider.CalendarContract;
import java.time.*;

/** Refreshes selected calendar timing, then reconciles future repeating alarms. */
final class CalendarSync {
    interface Port {void apply(Context context);}
    static Port port=CalendarSync::apply;
    private static void apply(Context c){try{if(CalendarSettings.load(c).enabled)observe(c);refresh(c,new CancellationSignal());if(!CalendarSettings.prefs(c).getString("error","").isEmpty())throw new IllegalStateException("Calendar rules need attention");}catch(RuntimeException failure){fallback(c);throw failure;}}
    static final int NOW=5832,CHANGES=5833,PERIODIC=5834;
    static void request(Context c){try{if(!CalendarSettings.load(c).enabled||!CalendarSettings.prefs(c).getString("error","").isEmpty())return;JobScheduler jobs=c.getSystemService(JobScheduler.class);if(jobs.getPendingJob(NOW)==null&&jobs.schedule(new JobInfo.Builder(NOW,new ComponentName(c,CalendarSyncJob.class)).setOverrideDeadline(0).build())!=JobScheduler.RESULT_SUCCESS)throw new IllegalStateException("Calendar refresh unavailable");}catch(RuntimeException unavailable){CalendarSettings.prefs(c).edit().putString("error","Calendar refresh could not be scheduled. Reopen Calendar rules.").commit();}}
    static void observe(Context c){JobScheduler jobs=c.getSystemService(JobScheduler.class);ComponentName service=new ComponentName(c,CalendarSyncJob.class);int changes=jobs.schedule(new JobInfo.Builder(CHANGES,service).addTriggerContentUri(new JobInfo.TriggerContentUri(CalendarContract.Events.CONTENT_URI,JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS)).setTriggerContentUpdateDelay(2000).setTriggerContentMaxDelay(30000).build()),periodic=jobs.schedule(new JobInfo.Builder(PERIODIC,service).setPeriodic(15*60000L).setPersisted(true).build());if(changes!=JobScheduler.RESULT_SUCCESS||periodic!=JobScheduler.RESULT_SUCCESS)throw new IllegalStateException("Calendar refresh could not be scheduled");}
    static void cancel(Context c){JobScheduler jobs=c.getSystemService(JobScheduler.class);for(int id:new int[]{NOW,CHANGES,PERIODIC})jobs.cancel(id);CalendarAlarms.snapshot=null;android.content.SharedPreferences.Editor edit=CalendarSettings.prefs(c).edit();for(String key:CalendarSettings.prefs(c).getAll().keySet())if(key.startsWith("receipt_"))edit.remove(key);edit.commit();}
    static void refresh(Context c,CancellationSignal cancel){try{
        CalendarPolicy.Rules rules=CalendarSettings.load(c);if(!rules.enabled){cancel(c);reconcile(c);return;}
        CalendarPolicy.Snapshot loaded=CalendarAccess.factory.open(c).snapshot(rules,System.currentTimeMillis(),ZoneId.systemDefault(),cancel);cancel.throwIfCanceled();
        if(!rules.revision.equals(CalendarSettings.load(c).revision))return;CalendarAlarms.snapshot=loaded;reconcile(c);CalendarSettings.prefs(c).edit().putLong("refreshed",loaded.readAt).remove("error").commit();
    }catch(RuntimeException failure){fallback(c);}}
    static void fallback(Context c){CalendarAlarms.snapshot=null;CalendarSettings.prefs(c).edit().putString("error","Calendar refresh failed. Ordinary alarm times are used where scheduling access is available.").commit();try{reconcile(c);}catch(RuntimeException scheduling){CalendarSettings.prefs(c).edit().putString("error","Calendar refresh and alarm scheduling need attention. Review calendar access and alarm readiness.").commit();}}
    private static void reconcile(Context c){synchronized(Scheduler.class){try(Store store=Records.open(c)){for(Alarm alarm:store.alarms())if(alarm.enabled&&!alarm.days.isEmpty()&&alarm.nap==null)Scheduler.schedule(c,alarm);}}}
    private CalendarSync(){}
}
