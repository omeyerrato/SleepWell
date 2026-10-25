package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.os.*;
import org.json.*;

/** Foreground alarm lifetime. Output timing survives same-occurrence service restarts. */
public class AlarmService extends Service {
    static volatile boolean active;private static AlarmService currentService;private SpeakRuntime.Quiet listening;
    private AlarmVibration vibration;private PowerManager.WakeLock wake;private AlarmPlayback playback;private AlarmTorch torch;private SmartLightAlarm bedroomLight;private AlarmLifetime lifetime;private boolean backupPhase;private int latestStartId;
    private final Handler handler=new Handler(Looper.getMainLooper());private Alarm alarm;private String occurrence;private long started;private volatile boolean destroyed;
    @Override public IBinder onBind(Intent intent){return null;}
    @Override public void onCreate(){super.onCreate();Notifications.channels(this);}
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        latestStartId=startId;
        if(!Prefs.flag(this,"ringing")){releaseOutputs();stopSelf(latestStartId);return START_NOT_STICKY;}
        if(lifetime!=null){if(occurrence.equals(Prefs.text(this,"alarmRun",""))){if(!timing())return START_NOT_STICKY;return START_STICKY;}releaseOutputs();}
        try{alarm=Alarm.read(new JSONObject(Prefs.text(this,"active_alarm","{}")));}catch(JSONException error){stopSelf();return START_NOT_STICKY;}
        occurrence=Prefs.text(this,"alarmRun","");started=Prefs.of(this).getLong("alarmStartedElapsed",0);long now=SystemClock.elapsedRealtime();
        if(occurrence.isEmpty()||started<=0||started>now){occurrence=java.util.UUID.randomUUID().toString();started=now;Prefs.of(this).edit().putString("alarmRun",occurrence).putLong("alarmStartedElapsed",started).apply();}
        final String lifetimeRun=occurrence;lifetime=new AlarmLifetime(alarm,lifetimeRun,new AlarmLifetime.Owner(){public boolean current(String run){return alive(run);}public boolean backupIfCurrent(String run,boolean backup){synchronized(Scheduler.class){if(!alive(run))return false;backupPhase=backup;if(playback!=null)playback.backup(backup);return true;}}public boolean timeoutIfCurrent(String run){return expire(run);}});
        if(!timing())return START_NOT_STICKY;
        DreamCues.alarmStarting();
        SleepSoundService.alarmStarting();
        Intent screen=new Intent(this,AlarmActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent launch=PendingIntent.getActivity(this,1,screen,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification notification=new androidx.core.app.NotificationCompat.Builder(this,Notifications.ALARM).setSmallIcon(R.drawable.ic_moon).setContentTitle(alarm.label)
            .setContentText("Complete your "+alarm.challenge.toLowerCase(java.util.Locale.getDefault())+" challenge").setSilent(!alarm.soundModeInvalid&&alarm.soundMode.silent()).setCategory(androidx.core.app.NotificationCompat.CATEGORY_ALARM).setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC).setOngoing(true).setContentIntent(launch).setFullScreenIntent(launch,true).build();
        startForeground(101,notification,ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);active=true;
        TrackingService.stopTravel(null,"An alarm took priority.");
        try{wake=getSystemService(PowerManager.class).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"SleepWell:alarm");wake.acquire(2*60*60*1000L);}catch(RuntimeException error){Prefs.put(this,"lastError","Android did not allow the alarm wake lock.");}
        final String ownRun=occurrence;currentService=this;startPlayback();
        try{bedroomLight=SmartLightAlarm.start(this,alarm,occurrence,started,()->alive(ownRun));}catch(RuntimeException failure){Prefs.put(this,"lastError","Bedroom light unavailable. Phone alarm continues.");}
        if(new AlarmWakePolicy(alarm).flashlight)torch=new AlarmTorch(this,()->alive(ownRun));
        AlarmWakePolicy policy=new AlarmWakePolicy(alarm);
        final long vibrationStarted=started;
        if(policy.vibration)vibration=new AlarmVibration(policy.vibrationDelayMillis,()->Math.max(0,SystemClock.elapsedRealtime()-vibrationStarted),()->alive(ownRun),()->new AlarmVibration.Port(){
            private final Vibrator nativeVibrator=getSystemService(Vibrator.class);
            public boolean start(){if(nativeVibrator==null||!nativeVibrator.hasVibrator())return false;nativeVibrator.vibrate(VibrationEffect.createWaveform(new long[]{0,600,400,600,1400},0),new android.media.AudioAttributes.Builder().setUsage(android.media.AudioAttributes.USAGE_ALARM).setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION).build());return true;}
            public void cancel(){if(nativeVibrator!=null)nativeVibrator.cancel();}
        },()->Prefs.put(this,"lastError","Alarm vibration is unavailable."));
        tick.run();return START_STICKY;
    }
    private void startPlayback(){if(listening!=null||playback!=null||!alive(occurrence)||!alarm.soundModeInvalid&&alarm.soundMode.silent())return;final String run=occurrence;playback=new AlarmPlayback(alarm,new AlarmAudioSink(this,handler,run,()->alive(run)),()->Math.max(0,SystemClock.elapsedRealtime()-started),SystemClock::elapsedRealtime);playback.start(backupPhase);}
    static SpeakRuntime.Quiet quietForSpeak(String run,Runnable acknowledged){if(Looper.myLooper()!=Looper.getMainLooper())throw new IllegalStateException("Listening admission must be visible");AlarmService service=currentService;if(service==null||!active||!service.alive(run)||service.listening!=null)throw new IllegalStateException("Alarm sound cannot pause for this request");SpeakRuntime.Quiet[] token=new SpeakRuntime.Quiet[1];token[0]=()->service.handler.post(()->{if(service.listening!=token[0])return;service.listening=null;if(service.alive(run))service.startPlayback();});service.listening=token[0];AlarmPlayback sound=service.playback;service.playback=null;Runnable quiet=()->{if(service.listening==token[0]&&service.alive(run)&&AlarmAudioSink.captureQuiet())acknowledged.run();};if(sound==null)service.handler.post(quiet);else sound.stop(quiet);return token[0];}
    private boolean alive(String run){return !destroyed&&run.equals(occurrence)&&Prefs.flag(this,"ringing")&&run.equals(Prefs.text(this,"alarmRun",""));}
    private final Runnable tick=new Runnable(){public void run(){if(!alive(occurrence)){releaseOutputs();stopSelf(latestStartId);return;}if(!timing())return;if(playback!=null)playback.tick();try{if(bedroomLight!=null)bedroomLight.tick();}catch(RuntimeException failure){Prefs.put(AlarmService.this,"lastError","Bedroom light unavailable. Phone alarm continues.");}if(vibration!=null)vibration.tick(Prefs.flag(AlarmService.this,"forceBackup"));handler.postDelayed(this,100);}};
    private boolean timing(){AlarmLifetime owned=lifetime;if(owned!=null&&owned.tick(Math.max(0,SystemClock.elapsedRealtime()-started),Prefs.flag(this,"forceBackup")))return true;releaseOutputs();stopSelf(latestStartId);return false;}
    private boolean expire(String run){synchronized(Scheduler.class){if(!alive(run))return false;Prefs.of(this).edit().putBoolean("ringing",false).putBoolean("forceBackup",false).putString("lastError","Alarm stopped at your configured ringing timeout. Challenges were not completed.").apply();try{Scheduler.cancelBackup(this,alarm.id,run);}catch(RuntimeException ignored){/* Cleared ringing ownership fences a queued backup even if cancel fails. */}releaseOutputs();stopSelf(latestStartId);return true;}}
    static void stop(Context context){
        stop(context,Prefs.text(context,"alarmRun",""));
    }
    static boolean current(Context context,String expected){return Prefs.flag(context,"ringing")&&expected.equals(Prefs.text(context,"alarmRun",""));}
    static boolean stop(Context context,String expected){synchronized(Scheduler.class){if(!current(context,expected))return false;try{Alarm alarm=Alarm.read(new JSONObject(Prefs.text(context,"active_alarm","{}")));Scheduler.cancelBackup(context,alarm.id,expected);}catch(JSONException ignored){}Prefs.of(context).edit().putBoolean("ringing",false).putBoolean("forceBackup",false).apply();context.stopService(new Intent(context,AlarmService.class));return true;}}
    private void releaseOutputs(){
        handler.removeCallbacksAndMessages(null);listening=null;if(lifetime!=null)lifetime.close();lifetime=null;backupPhase=false;
        try{if(playback!=null)playback.stop();}catch(RuntimeException ignored){}
        try{if(bedroomLight!=null)bedroomLight.close();}catch(RuntimeException ignored){}
        try{if(torch!=null)torch.close();}catch(RuntimeException ignored){}
        try{if(vibration!=null)vibration.close();}catch(RuntimeException ignored){}
        try{if(wake!=null&&wake.isHeld())wake.release();}catch(RuntimeException ignored){}
        playback=null;bedroomLight=null;torch=null;vibration=null;wake=null;
    }
    @Override public void onDestroy(){if(currentService==this)currentService=null;active=false;destroyed=true;releaseOutputs();super.onDestroy();}
}
