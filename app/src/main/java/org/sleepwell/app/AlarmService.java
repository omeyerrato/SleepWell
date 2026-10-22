package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.media.*;
import android.net.Uri;
import android.os.*;
import org.json.*;

public class AlarmService extends Service {
    private MediaPlayer player; private Vibrator vibrator; private AudioManager audio;
    private PowerManager.WakeLock wake; private final Handler handler=new Handler(Looper.getMainLooper());
    private int originalVolume=-1, lastVolume=-1; private long started; private Alarm alarm;
    @Override public IBinder onBind(Intent intent) { return null; }
    @Override public void onCreate() { super.onCreate(); Notifications.channels(this); }
    @Override public int onStartCommand(Intent i,int flags,int startId) {
        if(!Prefs.flag(this,"ringing")) { stopSelf(); return START_NOT_STICKY; }
        if(player!=null) return START_STICKY;
        try { alarm=Alarm.read(new JSONObject(Prefs.text(this,"active_alarm","{}"))); } catch(JSONException e) { stopSelf(); return START_NOT_STICKY; }
        Intent screen=new Intent(this,AlarmActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent launch=PendingIntent.getActivity(this,1,screen,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification notification=new Notification.Builder(this,Notifications.ALARM).setSmallIcon(R.drawable.ic_moon)
            .setContentTitle(alarm.label).setContentText("Complete your "+alarm.challenge.toLowerCase()+" challenge")
            .setCategory(Notification.CATEGORY_ALARM).setVisibility(Notification.VISIBILITY_PUBLIC).setOngoing(true)
            .setContentIntent(launch).setFullScreenIntent(launch,true).build();
        if(Build.VERSION.SDK_INT>=29) startForeground(101,notification,ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK); else startForeground(101,notification);
        wake=getSystemService(PowerManager.class).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"SleepWell:alarm"); wake.acquire(2*60*60*1000L);
        audio=getSystemService(AudioManager.class); originalVolume=audio.getStreamVolume(AudioManager.STREAM_ALARM);
        started=SystemClock.elapsedRealtime(); updateVolume();
        try { player=makePlayer(alarm.tone); player.start(); }
        catch(Exception first) {
            try { player=makePlayer("daybreak"); player.start(); }
            catch(Exception fallback) { Prefs.put(this,"lastError","Alarm audio failed: "+fallback.getMessage()); }
        }
        if(alarm.vibrate) {
            vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
            if(vibrator!=null && vibrator.hasVibrator()) vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0,600,400,600,1400},0));
        }
        return START_STICKY;
    }
    private MediaPlayer makePlayer(String tone) throws Exception {
        MediaPlayer p=new MediaPlayer();
        try {
            p.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
            if(tone.startsWith("content:")) p.setDataSource(this,Uri.parse(tone));
            else if(tone.equals("system")) p.setDataSource(this,RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
            else try(android.content.res.AssetFileDescriptor fd=getAssets().openFd(tone.equals("pulse")?"pulse.wav":"daybreak.wav")) { p.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength()); }
            p.setLooping(true); p.prepare(); return p;
        } catch(Exception e) { p.release(); throw e; }
    }
    private void updateVolume() {
        if(audio==null || alarm==null) return;
        int max=audio.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        double progress=alarm.rampSeconds==0?1:Math.min(1,(SystemClock.elapsedRealtime()-started)/(1000.0*alarm.rampSeconds));
        lastVolume=Math.max(1,(int)Math.round(max*Math.max(0.05,alarm.volume/100.0)*(0.20+0.80*progress)));
        try { audio.setStreamVolume(AudioManager.STREAM_ALARM,lastVolume,0); } catch(SecurityException e) { Prefs.put(this,"lastError","System denied alarm volume change."); }
        handler.postDelayed(this::updateVolume,1000);
    }
    static void stop(Context c) { Prefs.put(c,"ringing",false); c.stopService(new Intent(c,AlarmService.class)); }
    @Override public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if(player!=null) { player.release(); player=null; } if(vibrator!=null) vibrator.cancel();
        if(audio!=null && originalVolume>=0 && audio.getStreamVolume(AudioManager.STREAM_ALARM)==lastVolume) audio.setStreamVolume(AudioManager.STREAM_ALARM,originalVolume,0);
        if(wake!=null && wake.isHeld()) wake.release(); super.onDestroy();
    }
}
