package org.sleepwell.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.hardware.*;
import android.media.*;
import android.os.*;
import java.util.*;
import java.util.concurrent.*;

public class TrackingService extends Service implements SensorEventListener {
    static final String PAUSE="PAUSE",RESUME="RESUME",STOP="STOP";
    static volatile boolean running=false;private static volatile TrackingService current;
    interface StoreFactory{Store open(Context c);}
    static StoreFactory storeFactory=Store::new;
    private enum Phase{STARTING,ACTIVE,PAUSING,PAUSED,STOPPING,CLOSED}
    private Phase phase=Phase.STARTING;private boolean stopRequested,resumeRequested,interrupted,destroyed;
    private SensorManager sensors;private PowerManager.WakeLock wake;private Store store;private NoiseRecorder audio;private MediaPlayer lullaby;
    private final Handler handler=new Handler(Looper.getMainLooper());private final ExecutorService closer=Executors.newSingleThreadExecutor();
    private boolean clockChanged;private TrackingClock clock;private long session,start,windowStart;private double sum;private Double light;private int count,activeMinutes;private boolean motionRegistered,lightRegistered,gravityReady;
    private final ClipBudget clipBudget=new ClipBudget();
    private final float[] gravity=new float[3];
    private final Runnable minuteTick=this::minute,limitTick=()->stop(false),soundTick=this::stopSound,liveTick=this::liveSample;
    private final List<org.json.JSONObject> liveReadings=new ArrayList<>();private int previewCount;private double previewSum;private long previewAt;
    static final class Snapshot {
        final long id,start,active,windowStart,now;final String phase;final boolean motion,light,mic;final int samples;final Double movement,lightLevel;final List<org.json.JSONObject> readings;
        Snapshot(TrackingService s){id=s.session;start=s.start;now=s.timestamp();active=s.clock==null?0:s.clock.active(SystemClock.elapsedRealtime());windowStart=s.windowStart;phase=s.phase.name();motion=s.motionRegistered;light=s.lightRegistered;mic=s.audio!=null&&s.audio.capturing;samples=s.count;movement=s.count==0?null:Math.sqrt(s.sum/s.count);lightLevel=s.light;readings=new ArrayList<>(s.liveReadings);}
        boolean paused(){return phase.equals("PAUSED");}
    }
    static Snapshot snapshot(){TrackingService s=current;return s==null?null:new Snapshot(s);}
    @Override public IBinder onBind(Intent intent){return null;}
    @Override public void onCreate(){super.onCreate();Notifications.channels(this);}
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        String action=intent==null?STOP:intent.getAction();
        if(STOP.equals(action)){stop(false);return START_NOT_STICKY;}
        if(PAUSE.equals(action)){if(running)pause();else stopSelf();return START_NOT_STICKY;}
        if(RESUME.equals(action)){if(running)resume();else stopSelf();return START_NOT_STICKY;}
        if(running){if(current!=this)stopSelf();return START_NOT_STICKY;}
        current=this;running=true;clock=new TrackingClock(SystemClock.elapsedRealtime());
        try{
            foreground(false);store=storeFactory.open(this);session=store.begin();start=store.rows("SELECT start FROM sessions WHERE id=?",""+session).get(0).optLong("start");windowStart=start;clock=new TrackingClock(SystemClock.elapsedRealtime());
            Prefs.of(this).edit().putLong("session",session).putBoolean("tracking",true).putBoolean("trackingPaused",false).putInt("epochCount",0).putString("audioError","").putString("trackingError","").apply();
            sensors=getSystemService(SensorManager.class);wake=getSystemService(PowerManager.class).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"SleepWell:tracking");
            phase=Phase.ACTIVE;collect();if(audio!=null&&Prefs.flag(this,"saveClips"))pruneClips();handler.postDelayed(minuteTick,60000);handler.postDelayed(limitTick,12*60*60*1000L);
            handler.postDelayed(soundTick,Prefs.number(this,"lullabyMinutes",30)*60000L);
        }catch(Exception e){Prefs.put(this,"trackingError","Tracking could not start: "+e.getMessage());stop(true);}
        return START_NOT_STICKY;
    }
    private void foreground(boolean paused){
        Intent open=new Intent(this,TrackingActivity.class);PendingIntent content=PendingIntent.getActivity(this,41,open,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent stop=PendingIntent.getService(this,5,new Intent(this,TrackingService.class).setAction(STOP),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent toggle=paused?PendingIntent.getActivity(this,42,new Intent(this,TrackingActivity.class).setAction(RESUME),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT):PendingIntent.getService(this,43,new Intent(this,TrackingService.class).setAction(PAUSE),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification=new androidx.core.app.NotificationCompat.Builder(this,Notifications.TRACK).setSmallIcon(R.drawable.ic_moon).setContentTitle(paused?"SleepWell · tracking paused":"SleepWell · recording your night").setContentText(paused?"Sensors and microphone are stopped. Resume when ready.":"Phone tracking is active. Open to see sensor status.").setContentIntent(content).setOngoing(true).setSilent(true).setOnlyAlertOnce(true).addAction(new androidx.core.app.NotificationCompat.Action(0,paused?"Resume":"Pause",toggle)).addAction(new androidx.core.app.NotificationCompat.Action(0,"End night",stop)).build();
        boolean mic=!paused&&Prefs.flag(this,"noiseEnabled")&&checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED;
        if(Build.VERSION.SDK_INT>=34)startForeground(102,notification,ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE|(mic?ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE:0));else startForeground(102,notification);
    }
    private void collect(){
        resetWindow();handler.removeCallbacks(liveTick);handler.postDelayed(liveTick,1000);boolean mic=Prefs.flag(this,"noiseEnabled")&&checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED;foreground(false);
        Sensor motion=sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);motionRegistered=motion!=null&&sensors.registerListener(this,motion,200000);
        Sensor brightness=sensors.getDefaultSensor(Sensor.TYPE_LIGHT);lightRegistered=Prefs.of(this).getBoolean("recordLight",true)&&brightness!=null&&sensors.registerListener(this,brightness,SensorManager.SENSOR_DELAY_NORMAL);
        if(!motionRegistered)Prefs.put(this,"lastError","Movement sensor unavailable; motion and smart wake cannot be used.");
        long remaining=Math.max(1,12*60*60*1000L-clock.lifetime(SystemClock.elapsedRealtime()));wake.acquire(remaining);
        if(mic)audio=new NoiseRecorder(this,store,session,clipBudget,this::timestamp);else if(Prefs.flag(this,"noiseEnabled"))Prefs.put(this,"audioError","Microphone permission is unavailable; sound is not being recorded.");
        if(clock.lifetime(SystemClock.elapsedRealtime())<Prefs.number(this,"lullabyMinutes",30)*60000L)startSound();
    }
    private long timestamp(){return clock==null?System.currentTimeMillis():clock.timestamp(start,SystemClock.elapsedRealtime());}
    private void resetWindow(){sum=0;count=0;light=null;gravityReady=false;Arrays.fill(gravity,0);activeMinutes=0;resetPreview(timestamp());}
    private void resetPreview(long now){liveReadings.clear();previewSum=0;previewCount=0;previewAt=now;}
    private void liveSample(){if(phase!=Phase.ACTIVE)return;long now=timestamp();int measured=count-previewCount;if(measured>0)try{liveReadings.add(new org.json.JSONObject().put("start",previewAt).put("time",now).put("movement",Math.sqrt(Math.max(0,sum-previewSum)/measured)));}catch(org.json.JSONException ignored){}previewAt=now;previewCount=count;previewSum=sum;handler.postDelayed(liveTick,1000);}
    private void checkClock(long now){if(store!=null&&session>0&&!clockChanged&&Math.abs(System.currentTimeMillis()-now)>2000){store.clockChanged(session);clockChanged=true;}}
    private void stopSensors(){if(sensors!=null)sensors.unregisterListener(this);motionRegistered=lightRegistered=false;if(wake!=null&&wake.isHeld())wake.release();}
    @Override public void onSensorChanged(SensorEvent event){
        if(phase!=Phase.ACTIVE)return;
        if(event.sensor.getType()==Sensor.TYPE_LIGHT){if(lightRegistered)light=(double)event.values[0];return;}
        if(!motionRegistered)return;
        if(!gravityReady){System.arraycopy(event.values,0,gravity,0,3);gravityReady=true;return;}
        double energy=0;for(int k=0;k<3;k++){gravity[k]=.85f*gravity[k]+.15f*event.values[k];double difference=event.values[k]-gravity[k];energy+=difference*difference;}sum+=energy;count++;
    }
    @Override public void onAccuracyChanged(Sensor sensor,int accuracy){}
    private void flush(long now,NoiseRecorder closingAudio,boolean recordEmpty){
        if(store==null||session==0)return;checkClock(now);Double noise=closingAudio==null?null:closingAudio.takeEpoch();
        boolean measured=now>=windowStart&&(recordEmpty||count>0||light!=null||noise!=null);store.checkpoint(session,windowStart,now,count==0?null:Math.sqrt(sum/count),noise,light,count,clock.active(SystemClock.elapsedRealtime()),measured);if(measured)Prefs.put(this,"epochCount",Prefs.number(this,"epochCount",0)+1);
        windowStart=now;sum=0;count=0;resetPreview(now);
    }
    private void minute(){
        if(phase!=Phase.ACTIVE)return;try{long now=timestamp();double movement=count==0?0:Math.sqrt(sum/count);int measured=count;
        activeMinutes=movement>=.12?Math.min(5,activeMinutes+1):0;flush(now,audio,true);Prefs.put(this,"movement",String.format(Locale.ROOT,"%.3f",movement));
        for(Alarm alarm:store.alarms()){
            long deadline=Prefs.of(this).getLong("deadline_"+alarm.id,0);boolean goalMet=!Prefs.flag(this,"smartNeedsGoal")||SleepPlans.load(this).goalMet(clock.active(SystemClock.elapsedRealtime()));
            if(alarm.enabled&&goalMet&&SleepMath.smartWake(System.currentTimeMillis(),deadline,alarm.smartWindow,movement,activeMinutes,measured)){stopSound();Scheduler.fire(this,alarm,true);break;}
        }
        if(clock.lifetime(SystemClock.elapsedRealtime())>=12*60*60*1000L){stop(false);return;}handler.postDelayed(minuteTick,60000);}catch(Exception e){Prefs.put(this,"trackingError","Could not save sensor readings: "+e.getMessage());stop(true);}
    }
    private void closeAudio(NoiseRecorder recorder,Runnable complete){
        if(recorder==null){complete.run();return;}recorder.requestStop();closer.execute(()->{recorder.close();handler.post(complete);});
    }
    private void pause(){
        if(phase!=Phase.ACTIVE)return;phase=Phase.PAUSING;long pausedAt=timestamp();clock.pause(SystemClock.elapsedRealtime());handler.removeCallbacks(minuteTick);handler.removeCallbacks(liveTick);stopSensors();stopSound();NoiseRecorder recorder=audio;audio=null;
        closeAudio(recorder,()->{try{flush(pausedAt,recorder,false);store.pause(session,pausedAt);resetWindow();phase=Phase.PAUSED;Prefs.put(this,"trackingPaused",true);if(!destroyed)foreground(true);if(stopRequested)stop(interrupted);else if(resumeRequested)resume();}catch(Exception e){phase=Phase.PAUSED;Prefs.put(this,"trackingError","Could not save the pause: "+e.getMessage());stop(true);}});
    }
    private void resume(){
        if(phase==Phase.PAUSING){resumeRequested=true;return;}if(phase!=Phase.PAUSED)return;resumeRequested=false;
        if(clock.lifetime(SystemClock.elapsedRealtime())>=12*60*60*1000L){stop(false);return;}
        try{long now=timestamp();checkClock(now);store.resume(session,now);windowStart=now;clock.resume(SystemClock.elapsedRealtime());phase=Phase.ACTIVE;Prefs.put(this,"trackingPaused",false);collect();handler.postDelayed(minuteTick,60000);}catch(Exception e){Prefs.put(this,"trackingError","Tracking could not resume: "+e.getMessage());stop(true);}
    }
    private void stop(boolean wasInterrupted){
        interrupted|=wasInterrupted;if(phase==Phase.CLOSED||phase==Phase.STOPPING)return;if(phase==Phase.PAUSING){stopRequested=true;return;}
        boolean wasPaused=phase==Phase.PAUSED;phase=Phase.STOPPING;long endedAt=timestamp();if(clock!=null)clock.pause(SystemClock.elapsedRealtime());handler.removeCallbacks(minuteTick);handler.removeCallbacks(liveTick);handler.removeCallbacks(limitTick);handler.removeCallbacks(soundTick);stopSensors();stopSound();NoiseRecorder recorder=audio;audio=null;
        closeAudio(recorder,()->{try{if(store!=null&&session>0){checkClock(endedAt);if(!wasPaused)flush(endedAt,recorder,false);store.end(session,interrupted,clock.active(SystemClock.elapsedRealtime()),Math.max(start,endedAt));}}catch(Exception e){Prefs.put(this,"trackingError","Could not finalize the recording. Saved readings remain; review the interrupted record. "+e.getMessage());}finally{
            try{if(store!=null)store.close();}catch(Exception e){Prefs.put(this,"trackingError","Could not close recording storage: "+e.getMessage());}finally{store=null;phase=Phase.CLOSED;if(current==this){running=false;current=null;Prefs.of(this).edit().putBoolean("tracking",false).putBoolean("trackingPaused",false).putLong("session",0).apply();}closer.shutdown();if(!destroyed){stopForeground(STOP_FOREGROUND_REMOVE);stopSelf();}}
        }});
    }
    private void pruneClips(){long oldest=System.currentTimeMillis()-7*24*60*60*1000L;for(org.json.JSONObject clip:store.rows("SELECT id,path FROM clips WHERE time<?",""+oldest)){FileSafety.deleteClip(this,clip.optString("path"));store.getWritableDatabase().delete("clips","id=?",new String[]{""+clip.optLong("id")});}}
    private void startSound(){String sound=Prefs.text(this,"lullaby","Off");if(sound.equals("Off"))return;try{lullaby=new MediaPlayer();lullaby.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build());try(android.content.res.AssetFileDescriptor fd=getAssets().openFd(sound.equals("Rain")?"rain.wav":"brown.wav")){lullaby.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength());}lullaby.setLooping(true);lullaby.setVolume(.2f,.2f);lullaby.prepare();lullaby.start();}catch(Exception e){stopSound();Prefs.put(this,"lastError","Sleep sound unavailable: "+e.getMessage());}}
    private void stopSound(){if(lullaby!=null){lullaby.release();lullaby=null;}}
    @Override public void onDestroy(){destroyed=true;stop(true);super.onDestroy();}
}
