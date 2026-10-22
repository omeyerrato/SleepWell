package org.sleepwell.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.hardware.*;
import android.media.*;
import android.os.*;
import java.util.*;

public class TrackingService extends Service implements SensorEventListener {
    static volatile boolean running=false;
    private SensorManager sensors; private PowerManager.WakeLock wake; private Store store; private NoiseRecorder audio; private MediaPlayer lullaby;
    private final Handler handler=new Handler(Looper.getMainLooper()); private long session,start;
    private double sum=0,light=0; private int count=0,activeMinutes=0; private final float[] gravity=new float[3]; private boolean gravityReady=false,manualStop=false;
    @Override public IBinder onBind(Intent intent) { return null; }
    @Override public void onCreate() { super.onCreate(); Notifications.channels(this); }
    @Override public int onStartCommand(Intent intent,int flags,int startId) {
        if(intent!=null && "STOP".equals(intent.getAction())) { manualStop=true; stopSelf(); return START_NOT_STICKY; }
        if(running) return START_NOT_STICKY;
        boolean mic=Prefs.flag(this,"noiseEnabled") && checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED;
        PendingIntent stop=PendingIntent.getService(this,5,new Intent(this,TrackingService.class).setAction("STOP"),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        Notification n=new Notification.Builder(this,Notifications.TRACK).setSmallIcon(R.drawable.ic_moon).setContentTitle("SleepWell · tracking your night")
            .setContentText(mic?"Movement, room light & microphone active. Stored only on this phone.":"Movement & room light · microphone off")
            .setContentIntent(Notifications.home(this)).setOngoing(true).addAction(new Notification.Action.Builder(null,"End night",stop).build()).build();
        if(Build.VERSION.SDK_INT>=34) startForeground(102,n,ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE|(mic?ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE:0));
        else startForeground(102,n);
        store=new Store(this); session=store.begin(); start=System.currentTimeMillis(); running=true;
        Prefs.put(this,"session",session); Prefs.put(this,"tracking",true); Prefs.put(this,"audioError","");
        // Hold a bounded CPU lock so sensor delivery continues while the display sleeps.
        wake=getSystemService(PowerManager.class).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"SleepWell:tracking"); wake.acquire(12*60*60*1000L);
        sensors=getSystemService(SensorManager.class);
        Sensor movement=sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(movement!=null) sensors.registerListener(this,movement,200000);
        else Prefs.put(this,"lastError","No accelerometer: movement and smart wake are unavailable.");
        Sensor brightness=sensors.getDefaultSensor(Sensor.TYPE_LIGHT);
        if(brightness!=null) sensors.registerListener(this,brightness,SensorManager.SENSOR_DELAY_NORMAL);
        if(mic) audio=new NoiseRecorder(this,store,session);
        String sound=Prefs.text(this,"lullaby","Off");
        if(!sound.equals("Off")) {
            try {
                lullaby=new MediaPlayer(); lullaby.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build());
                try(android.content.res.AssetFileDescriptor fd=getAssets().openFd(sound.equals("Rain")?"rain.wav":"brown.wav")) { lullaby.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength()); }
                lullaby.setLooping(true); lullaby.setVolume(.2f,.2f); lullaby.prepare(); lullaby.start();
            } catch(Exception e) { Prefs.put(this,"lastError","Sleep sound unavailable: "+e.getMessage()); }
        }
        pruneClips(); handler.postDelayed(this::minute,60000); return START_NOT_STICKY;
    }
    private void pruneClips() {
        long oldest=System.currentTimeMillis()-7*24*60*60*1000L;
        for(org.json.JSONObject j:store.rows("SELECT id,path FROM clips WHERE time<?",""+oldest)) {
            FileSafety.deleteClip(this,j.optString("path")); store.getWritableDatabase().delete("clips","id=?",new String[]{""+j.optLong("id")});
        }
    }
    @Override public void onSensorChanged(SensorEvent e) {
        if(e.sensor.getType()==Sensor.TYPE_LIGHT) { light=e.values[0]; return; }
        if(!gravityReady) { System.arraycopy(e.values,0,gravity,0,3); gravityReady=true; return; }
        double energy=0;
        for(int k=0;k<3;k++) { gravity[k]=.85f*gravity[k]+.15f*e.values[k]; double d=e.values[k]-gravity[k]; energy+=d*d; }
        sum+=energy; count++;
    }
    @Override public void onAccuracyChanged(Sensor sensor,int accuracy) {}
    private void minute() {
        if(!running) return;
        double movement=count==0?0:Math.sqrt(sum/count); long now=System.currentTimeMillis();
        store.epoch(session,now,movement,audio==null?0:audio.level,light,count);
        activeMinutes=movement>=.12?Math.min(5,activeMinutes+1):0;
        Prefs.put(this,"movement",String.format(Locale.ROOT,"%.3f",movement)); Prefs.put(this,"epochCount",Prefs.number(this,"epochCount",0)+1);
        for(Alarm a:store.alarms()) {
            long deadline=Prefs.of(this).getLong("deadline_"+a.id,0);
            boolean goalMet=!Prefs.flag(this,"smartNeedsGoal") || now-start>=Prefs.number(this,"goal",480)*60000L;
            if(a.enabled && goalMet && SleepMath.smartWake(now,deadline,a.smartWindow,movement,activeMinutes,count)) {
                stopSound(); Scheduler.fire(this,a,true); break;
            }
        }
        sum=0;count=0;
        if(lullaby!=null && now-start>=Prefs.number(this,"lullabyMinutes",30)*60000L) stopSound();
        if(now-start>=12*60*60*1000L) { manualStop=true; stopSelf(); return; }
        handler.postDelayed(this::minute,60000);
    }
    private void stopSound() { if(lullaby!=null) { lullaby.release(); lullaby=null; } }
    @Override public void onDestroy() {
        running=false; handler.removeCallbacksAndMessages(null);
        if(sensors!=null) sensors.unregisterListener(this); if(audio!=null) audio.close(); stopSound();
        if(store!=null && session>0) { if(count>0) store.epoch(session,System.currentTimeMillis(),Math.sqrt(sum/count),audio==null?0:audio.level,light,count); store.end(session,!manualStop); store.close(); }
        if(wake!=null && wake.isHeld()) wake.release(); Prefs.put(this,"tracking",false); Prefs.put(this,"session",0L); super.onDestroy();
    }
}
