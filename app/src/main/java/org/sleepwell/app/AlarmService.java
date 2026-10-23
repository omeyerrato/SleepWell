package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.os.*;
import org.json.*;

/** Foreground alarm lifetime. Output timing survives same-occurrence service restarts. */
public class AlarmService extends Service {
    static volatile boolean active;
    private Vibrator vibrator;private PowerManager.WakeLock wake;private AlarmPlayback playback;private AlarmTorch torch;
    private final Handler handler=new Handler(Looper.getMainLooper());private Alarm alarm;private String occurrence;private long started;private volatile boolean destroyed;
    @Override public IBinder onBind(Intent intent){return null;}
    @Override public void onCreate(){super.onCreate();Notifications.channels(this);}
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        if(!Prefs.flag(this,"ringing")){stopSelf();return START_NOT_STICKY;}
        if(playback!=null){if(occurrence.equals(Prefs.text(this,"alarmRun",""))){if(Prefs.flag(this,"forceBackup"))playback.backup();return START_STICKY;}releaseOutputs();}
        SleepSoundService.alarmStarting();
        try{alarm=Alarm.read(new JSONObject(Prefs.text(this,"active_alarm","{}")));}catch(JSONException error){stopSelf();return START_NOT_STICKY;}
        occurrence=Prefs.text(this,"alarmRun","");started=Prefs.of(this).getLong("alarmStartedElapsed",0);long now=SystemClock.elapsedRealtime();
        if(occurrence.isEmpty()||started<=0||started>now){occurrence=java.util.UUID.randomUUID().toString();started=now;Prefs.of(this).edit().putString("alarmRun",occurrence).putLong("alarmStartedElapsed",started).apply();}
        Intent screen=new Intent(this,AlarmActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent launch=PendingIntent.getActivity(this,1,screen,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification notification=new Notification.Builder(this,Notifications.ALARM).setSmallIcon(R.drawable.ic_moon).setContentTitle(alarm.label)
            .setContentText("Complete your "+alarm.challenge.toLowerCase(java.util.Locale.getDefault())+" challenge").setCategory(Notification.CATEGORY_ALARM).setVisibility(Notification.VISIBILITY_PUBLIC).setOngoing(true).setContentIntent(launch).setFullScreenIntent(launch,true).build();
        startForeground(101,notification,ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);active=true;
        try{wake=getSystemService(PowerManager.class).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"SleepWell:alarm");wake.acquire(2*60*60*1000L);}catch(RuntimeException error){Prefs.put(this,"lastError","Android did not allow the alarm wake lock.");}
        final String ownRun=occurrence;playback=new AlarmPlayback(alarm,new AlarmAudioSink(this,handler,occurrence,()->alive(ownRun)),()->Math.max(0,SystemClock.elapsedRealtime()-started),SystemClock::elapsedRealtime);playback.start();
        if(new AlarmWakePolicy(alarm).flashlight)torch=new AlarmTorch(this,()->alive(ownRun));
        if(alarm.vibrate&&alarm.id>0)try{vibrator=getSystemService(Vibrator.class);if(vibrator!=null&&vibrator.hasVibrator())vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0,600,400,600,1400},0));}catch(RuntimeException error){Prefs.put(this,"lastError","Alarm vibration is unavailable.");}
        tick.run();return START_STICKY;
    }
    private boolean alive(String run){return !destroyed&&run.equals(occurrence)&&Prefs.flag(this,"ringing")&&run.equals(Prefs.text(this,"alarmRun",""));}
    private final Runnable tick=new Runnable(){public void run(){if(!alive(occurrence)){stopSelf();return;}if(Prefs.flag(AlarmService.this,"forceBackup"))playback.backup();playback.tick();handler.postDelayed(this,100);}};
    static void stop(Context context){
        stop(context,Prefs.text(context,"alarmRun",""));
    }
    static boolean current(Context context,String expected){return Prefs.flag(context,"ringing")&&expected.equals(Prefs.text(context,"alarmRun",""));}
    static boolean stop(Context context,String expected){synchronized(Scheduler.class){if(!current(context,expected))return false;try{Alarm alarm=Alarm.read(new JSONObject(Prefs.text(context,"active_alarm","{}")));Scheduler.cancelBackup(context,alarm.id,expected);}catch(JSONException ignored){}Prefs.of(context).edit().putBoolean("ringing",false).putBoolean("forceBackup",false).apply();context.stopService(new Intent(context,AlarmService.class));return true;}}
    private void releaseOutputs(){
        handler.removeCallbacksAndMessages(null);
        try{if(playback!=null)playback.stop();}catch(RuntimeException ignored){}
        try{if(torch!=null)torch.close();}catch(RuntimeException ignored){}
        try{if(vibrator!=null)vibrator.cancel();}catch(RuntimeException ignored){}
        try{if(wake!=null&&wake.isHeld())wake.release();}catch(RuntimeException ignored){}
        playback=null;torch=null;vibrator=null;wake=null;
    }
    @Override public void onDestroy(){active=false;destroyed=true;releaseOutputs();super.onDestroy();}
}
