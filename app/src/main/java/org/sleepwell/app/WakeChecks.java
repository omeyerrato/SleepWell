package org.sleepwell.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.provider.Settings;
import java.util.UUID;
import org.json.*;

/** Private check coordinator. All production entry points use the existing occurrence lock. */
final class WakeChecks {
 static final String PROMPT="org.sleepwell.app.WAKE_CHECK_PROMPT",EXPIRE="org.sleepwell.app.WAKE_CHECK_EXPIRE",CHANNEL="wake_check_silent_v1";
 static final int REQUEST=780,NOTIFICATION=780;
 interface Factory {WakeCheckEngine open(Context context);}
 static java.util.function.Function<Context,WakeCheckState.Clock> clocks=WakeChecks::platformClock;
 static Factory factory=WakeChecks::platform;
 private static WakeCheckState.Clock platformClock(Context c){return new WakeCheckState.Clock(){public long wall(){return System.currentTimeMillis();}public long elapsed(){return SystemClock.elapsedRealtime();}public long boot(){return bootCount(c);}};}
 static long bootCount(Context c){return c==null?0:Settings.Global.getInt(c.getContentResolver(),Settings.Global.BOOT_COUNT,0);}
 private static WakeCheckEngine platform(Context context){Context c=context.getApplicationContext();return new WakeCheckEngine(new WakeCheckStorage(Prefs.of(c)),new Platform(c),time(c));}
 static WakeCheckEngine open(Context c){return factory.open(c.getApplicationContext());}
 static WakeCheckState snapshot(Context c){synchronized(Scheduler.class){try{return open(c).read();}catch(RuntimeException error){return null;}}}
 static String error(Context c){try{WakeCheckState s=open(c).read();return s==null?"":s.phase==WakeCheckState.Phase.CLAIMED&&s.error.isEmpty()?"The check expired. Its alarm restart could not be verified.":s.error;}catch(RuntimeException error){return "Saved wake-up check could not be read.";}}
 static boolean arm(Context c,Alarm alarm,String run){synchronized(Scheduler.class){try{WakeCheckState s=WakeCheckState.waiting(UUID.randomUUID().toString(),run,alarm.json().toString(),alarm.label,alarm.id,alarm.awakeCheck*60000L,false,time(c));open(c).arm(s);return error(c).isEmpty();}catch(RuntimeException error){Prefs.put(c,"lastError","Wake-up check could not be saved or scheduled.");return false;}}}
 static WakeCheckState.Clock time(Context c){return clocks.apply(c.getApplicationContext());}
 static void receive(Context c,String nonce,WakeCheckEngine.Stage stage){synchronized(Scheduler.class){try{open(c).receive(nonce,stage);}catch(RuntimeException failure){Prefs.put(c,"lastError","Wake-up check delivery needs attention. Open the check.");}}}
 static void reconcile(Context c){synchronized(Scheduler.class){try{open(c).reconcile();}catch(RuntimeException failure){Prefs.put(c,"lastError","Wake-up check needs attention. Open the check.");}}}
 static void cancelForAlarm(Context c,int id){synchronized(Scheduler.class){try{WakeCheckEngine engine=open(c);engine.edited(id,false);if(engine.read()==null&&Prefs.number(c,"checkPending",0)==id)Prefs.put(c,"checkPending",0);}catch(RuntimeException failure){Prefs.put(c,"lastError","The wake-up check cancellation could not be saved.");}}}
 static void alarmEdited(Context c,Store store,Alarm alarm){if(alarm.awakeCheck!=0)return;synchronized(Scheduler.class){Alarm saved=store.alarm(alarm.id);if(saved!=null&&saved.awakeCheck==0)cancelForAlarm(c,alarm.id);}}
 static void supersede(Context c){synchronized(Scheduler.class){try{WakeCheckEngine engine=open(c);WakeCheckState state=engine.read();engine.supersede();int legacy=Prefs.number(c,"checkPending",0);if(state==null&&legacy>0){Prefs.put(c,"checkPending",0);c.getSystemService(AlarmManager.class).cancel(Scheduler.intent(c,legacy,Scheduler.CHECK));}}catch(RuntimeException failure){Prefs.put(c,"lastError","A previous wake-up check could not be cleared.");}}}
 static void legacy(Context c,int id){synchronized(Scheduler.class){try{WakeCheckEngine engine=open(c);if(engine.read()!=null||id<=0||Prefs.number(c,"checkPending",0)!=id)return;Alarm alarm=Alarm.read(new JSONObject(Prefs.text(c,"check_alarm","{}")));Alarm active=Alarm.read(new JSONObject(Prefs.text(c,"active_alarm","{}")));if(!WakeCheckState.legacyEligible(id,alarm.id,active.id,Prefs.flag(c,"ringing"))||alarm.awakeCheck<=0){Prefs.put(c,"checkPending",0);Prefs.put(c,"lastError","An old wake-up check could not be linked to the dismissed alarm and was skipped.");return;}WakeCheckState s=WakeCheckState.waiting(UUID.randomUUID().toString(),Prefs.text(c,"alarmRun",""),alarm.json().toString(),alarm.label,id,0,true,time(c));engine.arm(s);engine.receive(s.nonce,WakeCheckEngine.Stage.PROMPT);}catch(RuntimeException|JSONException failure){Prefs.put(c,"lastError","The legacy wake-up check could not be restored.");}}}
 static Intent intent(Context c,String nonce){return new Intent(c,WakeCheckActivity.class).putExtra("nonce",nonce);}
 static void review(Activity activity){WakeCheckState s=snapshot(activity);activity.startActivity(intent(activity,s==null?"":s.nonce));}
 static void channel(Context c){NotificationChannel channel=new NotificationChannel(CHANNEL,"Silent wake-up checks",NotificationManager.IMPORTANCE_HIGH);channel.setDescription("A silent two-minute confirmation after alarm dismissal.");channel.setSound(null,null);channel.enableVibration(false);channel.enableLights(false);channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);c.getSystemService(NotificationManager.class).createNotificationChannel(channel);}
 private static final class Platform implements WakeCheckEngine.Port {
  final Context c;Platform(Context c){this.c=c;}
  public boolean owner(WakeCheckState s){if(Prefs.flag(c,"ringing")||!s.run.equals(Prefs.text(c,"alarmRun","")))return false;try(Store store=Records.open(c)){Alarm current=store.alarm(s.alarmId);return current!=null&&current.awakeCheck>0;}}
  public String access(){channel(c);if(!Scheduler.allowed(c))return "Enable Alarms & reminders for the wake-up check.";NotificationManager manager=c.getSystemService(NotificationManager.class);if(Build.VERSION.SDK_INT>=33&&c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED||!manager.areNotificationsEnabled())return "Allow notifications to receive the silent check.";NotificationChannel channel=manager.getNotificationChannel(CHANNEL);if(channel==null||channel.getImportance()<NotificationManager.IMPORTANCE_HIGH)return "Set Silent wake-up checks to High importance in notification settings.";return "";}
  private PendingIntent pending(WakeCheckState s,WakeCheckEngine.Stage stage,int flags){String action=stage==WakeCheckEngine.Stage.PROMPT?PROMPT:EXPIRE;Intent intent=new Intent(c,WakeCheckReceiver.class).setAction(action).setIdentifier("wake-check:"+s.nonce+":"+stage).putExtra("nonce",s.nonce);return PendingIntent.getBroadcast(c,REQUEST,intent,flags|PendingIntent.FLAG_IMMUTABLE);}
  public void schedule(WakeCheckState s,WakeCheckEngine.Stage stage,long delay){if(!Scheduler.allowed(c))throw new SecurityException();AlarmManager manager=c.getSystemService(AlarmManager.class);PendingIntent operation=pending(s,stage,PendingIntent.FLAG_UPDATE_CURRENT);if(stage==WakeCheckEngine.Stage.EXPIRE){PendingIntent review=PendingIntent.getActivity(c,REQUEST,intent(c,s.nonce).setIdentifier("wake-check-open:"+s.nonce),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);manager.setAlarmClock(new AlarmManager.AlarmClockInfo(Math.addExact(System.currentTimeMillis(),delay),review),operation);}else manager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,Math.addExact(SystemClock.elapsedRealtime(),delay),operation);}
  public void cancel(WakeCheckState s){for(WakeCheckEngine.Stage stage:WakeCheckEngine.Stage.values()){PendingIntent pending=pending(s,stage,PendingIntent.FLAG_NO_CREATE);if(pending!=null){c.getSystemService(AlarmManager.class).cancel(pending);pending.cancel();}}c.getSystemService(NotificationManager.class).cancel(s.nonce,NOTIFICATION);}
  public void show(WakeCheckState s){if(!access().isEmpty())throw new SecurityException();PendingIntent activity=PendingIntent.getActivity(c,REQUEST,intent(c,s.nonce).setIdentifier("wake-check-open:"+s.nonce),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);androidx.core.app.NotificationCompat.Builder builder=new androidx.core.app.NotificationCompat.Builder(c,CHANNEL).setSmallIcon(Icons.resource("sun")).setContentTitle("Still awake?").setContentText("Confirm within 2 minutes, or your alarm rings again.").setCategory(Notification.CATEGORY_ALARM).setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC).setOngoing(true).setSilent(true).setOnlyAlertOnce(true).setContentIntent(activity).setTimeoutAfter(Math.max(1,s.remaining(time(c))));NotificationManager manager=c.getSystemService(NotificationManager.class);if(Build.VERSION.SDK_INT<34||manager.canUseFullScreenIntent())builder.setFullScreenIntent(activity,true);manager.notify(s.nonce,NOTIFICATION,builder.build());}
  public void ring(WakeCheckState s){if(!owner(s))throw new IllegalStateException("Check ownership changed.");Alarm alarm;try{alarm=Alarm.read(new JSONObject(s.alarm));}catch(JSONException invalid){throw new IllegalStateException(invalid);}Scheduler.fire(c,alarm,false);}
 }
 private WakeChecks(){}
}
