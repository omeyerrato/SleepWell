package org.sleepwell.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.hardware.*;
import android.net.Uri;
import android.os.*;
import java.time.ZoneId;
import java.util.UUID;

/** Private durable recurring schedule. Production entry is main-looper-only, so admission and service effects cannot race disable. */
final class RecurringTracking {
    static final String FILE="recurring-movement",ACTION="org.sleepwell.app.RECURRING_MOVEMENT";
    static final Object LOCK=new Object();
    interface Port {
        SharedPreferences preferences(Context c);long now();ZoneId zone();
        boolean exact(Context c);boolean notifications(Context c);boolean movement(Context c);boolean charging(Context c);boolean occupied(Context c);boolean ownsRun(Context c,String token);
        void schedule(Context c,long at,String token);void cancel(Context c,String token);
        void launch(Context c,Run run);void stopOwned(Context c,String token);
    }
    static final Port PLATFORM=new Port(){
        public SharedPreferences preferences(Context c){return c.getSharedPreferences(FILE,Context.MODE_PRIVATE);}
        public long now(){return System.currentTimeMillis();}public ZoneId zone(){return ZoneId.systemDefault();}
        public boolean exact(Context c){return Build.VERSION.SDK_INT<31||c.getSystemService(AlarmManager.class).canScheduleExactAlarms();}
        public boolean notifications(Context c){NotificationManager manager=c.getSystemService(NotificationManager.class);NotificationChannel channel=manager.getNotificationChannel(Notifications.TRACK);return RecurringNight.notificationsVisible(manager.areNotificationsEnabled(),Build.VERSION.SDK_INT<33||c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED,channel==null?null:channel.getImportance());}
        public boolean movement(Context c){SensorManager s=c.getSystemService(SensorManager.class);return s!=null&&s.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!=null;}
        public boolean charging(Context c){BatteryManager battery=c.getSystemService(BatteryManager.class);return battery!=null&&battery.isCharging();}
        public boolean occupied(Context c){return TrackingService.running||AlarmService.active||Prefs.flag(c,"ringing");}
        public boolean ownsRun(Context c,String token){return TrackingService.ownsRecurring(token);}
        public void schedule(Context c,long at,String token){c.getSystemService(AlarmManager.class).setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pending(c,token,PendingIntent.FLAG_UPDATE_CURRENT));}
        public void cancel(Context c,String token){if(token.isEmpty())return;PendingIntent pending=pending(c,token,PendingIntent.FLAG_NO_CREATE);if(pending!=null){c.getSystemService(AlarmManager.class).cancel(pending);pending.cancel();}}
        public void launch(Context c,Run run){c.startForegroundService(run.intent(c));}
        public void stopOwned(Context c,String token){TrackingService.cancelRecurring(token);}
        private PendingIntent pending(Context c,String token,int flags){return PendingIntent.getBroadcast(c,761,new Intent(c,RecurringTrackingReceiver.class).setAction(ACTION).setData(Uri.parse("sleepwell-recurring://occurrence/"+token)),flags|PendingIntent.FLAG_IMMUTABLE);}
    };
    static volatile Port port=PLATFORM;
    static final class Config {
        final long revision;final boolean enabled,stillness,charging;final int minute;
        Config(long revision,boolean enabled,int minute,boolean stillness,boolean charging){if(revision<0||minute<0||minute>1439)throw new IllegalArgumentException("Invalid nightly settings");this.revision=revision;this.enabled=enabled;this.minute=minute;this.stillness=stillness;this.charging=charging;}
    }
    static final class Run {
        final String token;final long revision,scheduled;final boolean stillness;
        Run(String token,long revision,long scheduled,boolean stillness){this.token=token;this.revision=revision;this.scheduled=scheduled;this.stillness=stillness;}
        Intent intent(Context c){return new Intent(c,TrackingService.class).setAction(TrackingService.ARM).putExtra("recurringToken",token).putExtra("recurringRevision",revision).putExtra("mode",(stillness?AutomaticSleep.Mode.STILLNESS:AutomaticSleep.Mode.BEDTIME).name()).putExtra("scheduled",scheduled).putExtra("recordSounds",false).putExtra("keepClips",false);}
    }
    static Config load(SharedPreferences p,int bedtime){return new Config(p.getLong("revision",0),p.getBoolean("enabled",false),p.getInt("minute",bedtime),p.getBoolean("stillness",false),p.getBoolean("charging",false));}
    static String status(Port owner,Context c){return owner.preferences(c).getString("status","");}
    static Config save(Context c,Port owner,Config draft,long expected){requireMain(owner);synchronized(LOCK){SharedPreferences p=owner.preferences(c);Config old=load(p,draft.minute);if(old.revision!=expected)throw new IllegalStateException("Nightly settings changed. Reopen this screen.");if(expected==Long.MAX_VALUE)throw new IllegalStateException("Settings revision is exhausted.");if(draft.enabled)requireAvailable(c,owner);String pending=p.getString("pending","");String active=p.getString("run","");boolean preserve=draft.enabled&&"STARTED".equals(p.getString("runState",""));long revision=expected+1;
        SharedPreferences.Editor edit=p.edit().putLong("revision",revision).putBoolean("enabled",draft.enabled).putInt("minute",draft.minute).putBoolean("stillness",draft.stillness).putBoolean("charging",draft.charging).remove("pending").remove("pendingAt").putString("scheduleStatus",draft.enabled?"":"Nightly tracking is off.");
        if(!preserve)edit.remove("run").remove("runState").putString("status","");commit(edit);
        if(!preserve&&!active.isEmpty())owner.stopOwned(c,active);safeCancel(c,owner,pending);if(draft.enabled)reconcile(c,owner);return load(p,draft.minute);
    }}
    /** Visible/system reconciliation can retire a dead process claim; it never replays its start. */
    static void recover(Context c,Port owner,boolean reboot){requireMain(owner);synchronized(LOCK){SharedPreferences p=owner.preferences(c);String token=p.getString("run","");String state=p.getString("runState","");if(!token.isEmpty()&&(reboot||("STARTED".equals(state)&&!owner.ownsRun(c,token))||("CLAIMED".equals(state)&&!RecurringNight.due(owner.now(),p.getLong("runAt",0)))))ended(c,owner,token,"The previous nightly attempt stopped. Its start will not be replayed.");reconcile(c,owner);}}
    static void reconcile(Context c,Port owner){requireMain(owner);synchronized(LOCK){SharedPreferences p=owner.preferences(c);Config config=load(p,0);String old=p.getString("pending","");if(!config.enabled){safeCancel(c,owner,old);return;}try{requireAvailable(c,owner);RecurringNight.Occurrence next=RecurringNight.next(owner.now(),owner.zone(),config.minute,p.getString("lastDate",""));String token=UUID.randomUUID().toString();
        commit(p.edit().putString("pending",token).putLong("pendingAt",next.at).putString("pendingDate",next.date).putString("pendingZone",next.zone).putString("scheduleStatus","Scheduled for "+next.date+"."));safeCancel(c,owner,old);
        try{owner.schedule(c,next.at,token);}catch(RuntimeException failure){commit(p.edit().remove("pending").remove("pendingAt").putString("scheduleStatus","Nightly tracking needs attention: "+message(failure)));}
    }catch(RuntimeException failure){safeCancel(c,owner,old);commit(p.edit().remove("pending").remove("pendingAt").putString("scheduleStatus",message(failure)));}}}
    static void receive(Context c,Port owner,String token){requireMain(owner);synchronized(LOCK){SharedPreferences p=owner.preferences(c);Config config=load(p,0);if(!config.enabled||token==null||!token.equals(p.getString("pending","")))return;long at=p.getLong("pendingAt",0),now=owner.now();if(!owner.zone().getId().equals(p.getString("pendingZone",""))){safeCancel(c,owner,token);commit(p.edit().remove("pending").remove("pendingAt").putString("status","Time zone changed; the next local start was rescheduled."));reconcile(c,owner);return;}if(now<at){owner.schedule(c,at,token);return;}
        String date=p.getString("pendingDate","");String skip="";if(!RecurringNight.due(now,at))skip="Skipped a delayed start.";
        else try{requireAvailable(c,owner);if(owner.occupied(c))skip="Skipped: an alarm or tracking session was already active.";else if(config.charging&&!owner.charging(c))skip="Skipped: the phone was not charging at the start time.";}catch(RuntimeException failure){skip=message(failure);}
        commit(p.edit().remove("pending").remove("pendingAt").putString("lastDate",date).putString("status",skip.isEmpty()?"Starting movement-only tracking…":skip));
        Run run=null;if(skip.isEmpty()){run=new Run(token,config.revision,at,config.stillness);commit(p.edit().putString("run",token).putLong("runRevision",config.revision).putLong("runAt",at).putBoolean("runStillness",config.stillness).putString("runState","CLAIMED"));}
        reconcile(c,owner); // Schedule only a future occurrence; never backfill another start.
        if(run!=null)try{owner.launch(c,run);}catch(RuntimeException failure){ended(c,owner,token,"Nightly tracking could not start: "+message(failure));}
        else commit(p.edit().putString("status",skip));
    }}
    /** Called inside the service before foreground, sensors, wake locks or storage. */
    static Run accept(Context c,Port owner,Intent intent){requireMain(owner);synchronized(LOCK){SharedPreferences p=owner.preferences(c);Config config=load(p,0);String token=intent.getStringExtra("recurringToken");long revision=intent.getLongExtra("recurringRevision",-1);if(!config.enabled||token==null||!token.equals(p.getString("run",""))||revision!=config.revision||revision!=p.getLong("runRevision",-1)||!"CLAIMED".equals(p.getString("runState","")))return null;if(!RecurringNight.due(owner.now(),p.getLong("runAt",0))){ended(c,owner,token,"Skipped: the nightly request arrived outside its allowed start time.");return null;}
        try{requireAvailable(c,owner);if(owner.occupied(c))throw new IllegalStateException("Another alarm or tracking session is active.");}catch(RuntimeException failure){ended(c,owner,token,message(failure));return null;}
        Run run=new Run(token,revision,p.getLong("runAt",0),p.getBoolean("runStillness",false));commit(p.edit().putString("runState","STARTED").putString("status",run.stillness?"Waiting for a still, screen-off period. Movement only.":"Movement tracking started at the saved time."));return run;
    }}
    static void ended(Context c,Port owner,String token,String status){requireMain(owner);synchronized(LOCK){SharedPreferences p=owner.preferences(c);if(token!=null&&token.equals(p.getString("run","")))commit(p.edit().remove("run").remove("runState").putString("status",status));}}
    static void occupied(Context c,Port owner,String token){requireMain(owner);synchronized(LOCK){SharedPreferences p=owner.preferences(c);if(token!=null&&token.equals(p.getString("run",""))&&"CLAIMED".equals(p.getString("runState","")))ended(c,owner,token,"Skipped: another tracking session started before the nightly request arrived.");}}
    private static void requireMain(Port owner){if(owner==PLATFORM&&Looper.myLooper()!=Looper.getMainLooper())throw new IllegalStateException("Recurring tracking production entry must run on the main thread.");}
    static void requireAvailable(Context c,Port owner){if(!owner.exact(c))throw new IllegalStateException("Allow exact alarms to schedule nightly tracking.");if(!owner.notifications(c))throw new IllegalStateException("Allow notifications so the tracking cancel control stays visible.");if(!owner.movement(c))throw new IllegalStateException("A working movement sensor is required.");}
    private static void safeCancel(Context c,Port owner,String token){if(!token.isEmpty())owner.cancel(c,token);}
    private static void commit(SharedPreferences.Editor edit){if(!edit.commit())throw new IllegalStateException("Nightly settings could not be saved. Try again.");}
    private static String message(Throwable failure){String text=failure.getMessage();return text==null?"Nightly tracking is unavailable. Reopen settings.":text;}
    private RecurringTracking(){}
}
