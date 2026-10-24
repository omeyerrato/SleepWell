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
    static final String PAUSE="PAUSE",RESUME="RESUME",STOP="STOP",ARM="ARM";
    static volatile boolean running=false;private static volatile TrackingService current;
    interface StoreFactory{Store open(Context c);}
    static StoreFactory storeFactory=Store::new;
    private enum Phase{STARTING,ARMED,ACTIVE,PAUSING,PAUSED,STOPPING,CLOSED}
    private Phase phase=Phase.STARTING;private boolean stopRequested,resumeRequested,interrupted,destroyed;
    private SensorManager sensors;private PowerManager.WakeLock wake;private Store store;private NoiseRecorder audio;private boolean soundAttempted,automaticRun;private AutomaticSleep automatic;private AutomaticCapture automaticCapture;private AutomaticTransition automaticTransition;private AutomaticSleep.Mode automaticMode;
    private final Handler handler=new Handler(Looper.getMainLooper());private final ExecutorService closer=Executors.newSingleThreadExecutor();
    private String recurringToken;private RecurringTracking.Port recurringOwner;
    private boolean clockChanged;private TrackingClock clock;private long session,start,windowStart;private double sum;private Double light;private int count,activeMinutes;private boolean motionRegistered,lightRegistered;
    private final ClipBudget clipBudget=new ClipBudget();
    private final MotionFilter motionFilter=new MotionFilter();private MotionProfile motionProfile=new MotionProfile(1);
    private final Runnable minuteTick=this::minute,limitTick=()->stop(false),liveTick=this::liveSample,automaticTick=this::automaticTick;
    private final List<org.json.JSONObject> liveReadings=new ArrayList<>();private int previewCount;private double previewSum;private long previewAt;
    static final class Snapshot {
        final long id,start,active,windowStart,now;final String phase,automaticState;final long quietMillis;final boolean motion,light,mic,automatic,soundsRequested,keepClips;final int samples;final Double movement,lightLevel;final List<org.json.JSONObject> readings;
        Snapshot(TrackingService s){id=s.session;start=s.start;now=s.timestamp();active=s.clock==null?0:s.clock.active(SystemClock.elapsedRealtime());windowStart=s.windowStart;phase=s.phase.name();automaticState=s.automatic==null?"":s.automatic.state().name();quietMillis=s.automatic==null?0:s.automatic.quietMillis(SystemClock.elapsedRealtime());automatic=s.automaticRun;soundsRequested=s.automaticCapture!=null&&s.automaticCapture.requested();keepClips=s.automaticCapture!=null&&s.automaticCapture.keepClips();motion=s.motionRegistered;light=s.lightRegistered;mic=s.audio!=null&&s.audio.capturing;samples=s.count;movement=s.count==0?null:Math.sqrt(s.sum/s.count);lightLevel=s.light;readings=new ArrayList<>(s.liveReadings);}
        boolean paused(){return phase.equals("PAUSED");}
    }
    static boolean ownsRecurring(String token){TrackingService service=current;return running&&service!=null&&token!=null&&token.equals(service.recurringToken);}
    static void cancelRecurring(String token){TrackingService service=current;if(service==null||token==null||!token.equals(service.recurringToken))return;Runnable cancel=()->{if(token.equals(service.recurringToken))service.stop(false);};if(Looper.myLooper()==Looper.getMainLooper())cancel.run();else service.handler.post(cancel);}
    private int rejectRecurring(){phase=Phase.CLOSED;closer.shutdown();stopSelf();return START_NOT_STICKY;}
    static Snapshot snapshot(){TrackingService s=current;return s==null?null:new Snapshot(s);}
    @Override public IBinder onBind(Intent intent){return null;}
    @Override public void onCreate(){super.onCreate();Notifications.channels(this);}
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        String action=intent==null?STOP:intent.getAction();
        if(STOP.equals(action)){stop(false);return START_NOT_STICKY;}
        if(PAUSE.equals(action)){if(running)pause();else stopSelf();return START_NOT_STICKY;}
        if(RESUME.equals(action)){if(running)resume();else stopSelf();return START_NOT_STICKY;}
        if(running){if(intent!=null&&intent.hasExtra("recurringToken")&&!Objects.equals(recurringToken,intent.getStringExtra("recurringToken")))try{RecurringTracking.occupied(this,RecurringTracking.port,intent.getStringExtra("recurringToken"));}catch(RuntimeException statusFailure){android.util.Log.e("SleepWell","Could not save skipped nightly start",statusFailure);}if(current!=this)stopSelf();return START_NOT_STICKY;}
        recurringToken=null;recurringOwner=null; // A new idle entry cannot inherit the previous run’s schedule ownership.
        if(intent!=null&&intent.hasExtra("recurringToken")){
            if(!ARM.equals(action))return rejectRecurring();
            recurringOwner=RecurringTracking.port;
            RecurringTracking.Run owned;
            try{owned=RecurringTracking.accept(this,recurringOwner,intent);}catch(RuntimeException invalid){return rejectRecurring();}
            if(owned==null)return rejectRecurring();
            recurringToken=owned.token;intent=owned.intent(this); // Ignore caller sound, clip, mode and schedule extras.
        }
        BackupRuntime.admitted();
        current=this;running=true;clock=new TrackingClock(SystemClock.elapsedRealtime());
        try{
            motionProfile=MotionSettings.load(this);automaticRun=ARM.equals(action);if(automaticRun){automaticCapture=new AutomaticCapture(recurringToken==null&&intent.getBooleanExtra("recordSounds",false),recurringToken==null&&intent.getBooleanExtra("keepClips",false));if(automaticCapture.requested()&&checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)throw new SecurityException("Microphone permission is unavailable. Arm again with sound recording off.");}foreground(false);
            sensors=getSystemService(SensorManager.class);wake=getSystemService(PowerManager.class).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"SleepWell:tracking");
            if(automaticRun){arm(intent);return START_NOT_STICKY;}
            beginRecording();
        }catch(Exception e){Prefs.put(this,"trackingError","Tracking could not start: "+e.getMessage());stop(true);}
        return START_NOT_STICKY;
    }
    private void beginRecording(){
            store=storeFactory.open(this);session=store.begin();start=store.rows("SELECT start FROM sessions WHERE id=?",""+session).get(0).optLong("start");windowStart=start;clock=new TrackingClock(SystemClock.elapsedRealtime());
            Prefs.of(this).edit().putLong("session",session).putBoolean("tracking",true).putBoolean("trackingPaused",false).putInt("epochCount",0).putString("audioError","").putString("trackingError","").apply();
            phase=Phase.ACTIVE;if(automaticCapture!=null)automaticCapture.recordingBegan();collect();handler.postDelayed(minuteTick,60000);handler.postDelayed(limitTick,12*60*60*1000L);
    }
    private void arm(Intent intent){
        automaticMode=AutomaticSleep.Mode.valueOf(intent.getStringExtra("mode"));
        long elapsed=SystemClock.elapsedRealtime(),delay=Math.max(0,intent.getLongExtra("scheduled",System.currentTimeMillis())-System.currentTimeMillis());
        automatic=new AutomaticSleep(automaticMode,elapsed,elapsed+delay);automaticTransition=new AutomaticTransition(automatic,this::triggerAutomatic);start=System.currentTimeMillis();phase=Phase.ARMED;
        Sensor sensor=sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        motionRegistered=sensor!=null&&sensors.registerListener(this,sensor,200000);
        if(!motionRegistered)throw new IllegalStateException("Movement sensor is unavailable.");
        wake.acquire(AutomaticSleep.MAX_WAIT);
        Prefs.of(this).edit().putBoolean("automaticArmed",true).putString("automaticResult","").putString("trackingError","").apply();
        foreground(false);handler.post(automaticTick);
    }
    private void automaticTick(){
        if(phase!=Phase.ARMED)return;
        AutomaticSleep.State state=automaticTransition.tick(SystemClock.elapsedRealtime(),getSystemService(PowerManager.class).isInteractive());
        if(phase!=Phase.ARMED)return;
        if(state==AutomaticSleep.State.EXPIRED){Prefs.put(this,"automaticResult","The four-hour waiting period ended. "+(recurringToken==null?"Arm again when you are ready for bed.":"Your nightly settings are kept."));stop(false);return;}
        handler.postDelayed(automaticTick,1000);
    }
    private void triggerAutomatic(){
        handler.removeCallbacks(automaticTick);stopSensors();Prefs.put(this,"automaticArmed",false);
        try{beginRecording();Prefs.put(this,"automaticResult",automaticMode==AutomaticSleep.Mode.STILLNESS?"Recording started after a still, screen-off period. This is a phone-idle estimate.":"Recording started at your selected time.");}
        catch(Exception failure){Prefs.put(this,"trackingError","Automatic tracking could not start: "+failure.getMessage());stop(true);}
    }
    private void foreground(boolean paused){
        Intent open=new Intent(this,TrackingActivity.class);PendingIntent content=PendingIntent.getActivity(this,41,open,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent stop=PendingIntent.getService(this,5,new Intent(this,TrackingService.class).setAction(STOP),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent toggle=paused?PendingIntent.getActivity(this,42,new Intent(this,TrackingActivity.class).setAction(RESUME),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT):PendingIntent.getService(this,43,new Intent(this,TrackingService.class).setAction(PAUSE),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        androidx.core.app.NotificationCompat.Builder builder=new androidx.core.app.NotificationCompat.Builder(this,Notifications.TRACK).setSmallIcon(R.drawable.ic_moon).setContentTitle(phase==Phase.ARMED?"SleepWell · waiting to track":paused?"SleepWell · tracking paused":"SleepWell · recording your night").setContentText(phase==Phase.ARMED?"Automatic start is armed. Open to check progress or cancel.":paused?"Sensors and microphone are stopped. Resume when ready.":"Phone tracking is active. Open to see sensor status.").setContentIntent(content).setOngoing(true).setSilent(true).setOnlyAlertOnce(true).addAction(new androidx.core.app.NotificationCompat.Action(0,phase==Phase.ARMED?"Cancel automatic start":"End night",stop));
        if(phase!=Phase.ARMED)builder.addAction(new androidx.core.app.NotificationCompat.Action(0,paused?"Resume":"Pause",toggle));
        Notification notification=builder.build();
        boolean granted=checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED;boolean mic=automaticRun?automaticCapture!=null&&automaticCapture.foregroundType(granted):!paused&&Prefs.flag(this,"noiseEnabled")&&granted;
        if(Build.VERSION.SDK_INT>=34)startForeground(102,notification,ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE|(mic?ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE:0));else startForeground(102,notification,mic?ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE:ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE);
        if(automaticCapture!=null&&phase==Phase.STARTING)automaticCapture.foregroundEstablished(mic);
    }
    private void collect(){
        resetWindow();handler.removeCallbacks(liveTick);handler.postDelayed(liveTick,1000);foreground(false);boolean granted=checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED;boolean mic=automaticRun?automaticCapture.allowsCapture(granted):Prefs.flag(this,"noiseEnabled")&&granted;
        Sensor motion=sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);motionRegistered=motion!=null&&sensors.registerListener(this,motion,200000);
        Sensor brightness=sensors.getDefaultSensor(Sensor.TYPE_LIGHT);lightRegistered=recurringToken==null&&Prefs.of(this).getBoolean("recordLight",true)&&brightness!=null&&sensors.registerListener(this,brightness,SensorManager.SENSOR_DELAY_NORMAL);
        if(!motionRegistered){if(recurringToken!=null)throw new IllegalStateException("Movement sensor could not be registered.");Prefs.put(this,"lastError","Movement sensor unavailable; motion and smart wake cannot be used.");}
        long remaining=Math.max(1,12*60*60*1000L-clock.lifetime(SystemClock.elapsedRealtime()));wake.acquire(remaining);
        if(mic)audio=automaticRun?new NoiseRecorder(this,store,session,clipBudget,this::timestamp,automaticCapture.keepClips()):new NoiseRecorder(this,store,session,clipBudget,this::timestamp);else if(automaticRun?automaticCapture.requested():Prefs.flag(this,"noiseEnabled"))Prefs.put(this,"audioError","Microphone permission is unavailable; sound is not being recorded.");
        if(!automaticRun){if(!soundAttempted){soundAttempted=true;SleepSoundService.trackingStart(this,session,clock.lifetime(SystemClock.elapsedRealtime()));}else SleepSoundService.trackingResume(session);}
    }
    private long timestamp(){return clock==null?System.currentTimeMillis():clock.timestamp(start,SystemClock.elapsedRealtime());}
    private void resetWindow(){sum=0;count=0;light=null;motionFilter.reset();activeMinutes=0;resetPreview(timestamp());}
    private void resetPreview(long now){liveReadings.clear();previewSum=0;previewCount=0;previewAt=now;}
    private void liveSample(){if(phase!=Phase.ACTIVE)return;long now=timestamp();int measured=count-previewCount;if(measured>0)try{liveReadings.add(new org.json.JSONObject().put("start",previewAt).put("time",now).put("movement",Math.sqrt(Math.max(0,sum-previewSum)/measured)));}catch(org.json.JSONException ignored){}previewAt=now;previewCount=count;previewSum=sum;handler.postDelayed(liveTick,1000);}
    private void checkClock(long now){if(store!=null&&session>0&&!clockChanged&&Math.abs(System.currentTimeMillis()-now)>2000){store.clockChanged(session);clockChanged=true;}}
    private void stopSensors(){if(sensors!=null)sensors.unregisterListener(this);motionRegistered=lightRegistered=false;if(wake!=null&&wake.isHeld())wake.release();}
    @Override public void onSensorChanged(SensorEvent event){
        if(phase!=Phase.ACTIVE&&phase!=Phase.ARMED)return;
        if(event.sensor.getType()==Sensor.TYPE_LIGHT){if(lightRegistered)light=(double)event.values[0];return;}
        if(!motionRegistered)return;
        if(event.values.length<3)return;Double raw=motionFilter.sample(event.values[0],event.values[1],event.values[2]);if(raw==null)return;
        if(phase==Phase.ARMED){automaticTransition.sample(SystemClock.elapsedRealtime(),motionProfile.inference(raw),getSystemService(PowerManager.class).isInteractive());return;}sum+=raw*raw;count++;
    }
    @Override public void onAccuracyChanged(Sensor sensor,int accuracy){}
    private void flush(long now,NoiseRecorder closingAudio,boolean recordEmpty){
        if(store==null||session==0)return;checkClock(now);Double noise=closingAudio==null?null:closingAudio.takeEpoch();
        boolean measured=now>=windowStart&&(recordEmpty||count>0||light!=null||noise!=null);store.checkpoint(session,windowStart,now,count==0?null:Math.sqrt(sum/count),noise,light,count,clock.active(SystemClock.elapsedRealtime()),measured);if(measured)Prefs.put(this,"epochCount",Prefs.number(this,"epochCount",0)+1);
        windowStart=now;sum=0;count=0;resetPreview(now);
    }
    private void minute(){
        if(phase!=Phase.ACTIVE)return;try{long now=timestamp();double movement=count==0?0:Math.sqrt(sum/count);int measured=count;
        activeMinutes=motionProfile.activeMinutes(movement,activeMinutes);flush(now,audio,true);Prefs.put(this,"movement",String.format(Locale.ROOT,"%.3f",movement));
        for(Alarm alarm:store.alarms()){
            if(alarm.nap!=null){if(Naps.movement(this,alarm,motionProfile.inference(movement),activeMinutes,measured))break;continue;}
            long deadline=Prefs.of(this).getLong("deadline_"+alarm.id,0);boolean goalMet=!Prefs.flag(this,"smartNeedsGoal")||SleepPlans.load(this).goalMet(clock.active(SystemClock.elapsedRealtime()));
            if(alarm.enabled&&goalMet&&motionProfile.smartWake(System.currentTimeMillis(),deadline,alarm.smartWindow,movement,activeMinutes,measured)){SleepSoundService.alarmStarting();Scheduler.fire(this,alarm,true);break;}
        }
        if(clock.lifetime(SystemClock.elapsedRealtime())>=12*60*60*1000L){stop(false);return;}handler.postDelayed(minuteTick,60000);}catch(Exception e){Prefs.put(this,"trackingError","Could not save sensor readings: "+e.getMessage());stop(true);}
    }
    private void closeAudio(NoiseRecorder recorder,Runnable complete){
        if(recorder==null){complete.run();return;}recorder.requestStop();closer.execute(()->{recorder.close();handler.post(complete);});
    }
    private void pause(){
        if(phase!=Phase.ACTIVE)return;phase=Phase.PAUSING;if(automaticCapture!=null)automaticCapture.pause();long pausedAt=timestamp();clock.pause(SystemClock.elapsedRealtime());handler.removeCallbacks(minuteTick);handler.removeCallbacks(liveTick);stopSensors();if(!automaticRun)SleepSoundService.trackingPause(session);NoiseRecorder recorder=audio;audio=null;
        closeAudio(recorder,()->{try{flush(pausedAt,recorder,false);store.pause(session,pausedAt);resetWindow();phase=Phase.PAUSED;Prefs.put(this,"trackingPaused",true);if(!destroyed)foreground(true);if(stopRequested)stop(interrupted);else if(resumeRequested)resume();}catch(Exception e){phase=Phase.PAUSED;Prefs.put(this,"trackingError","Could not save the pause: "+e.getMessage());stop(true);}});
    }
    private void resume(){
        if(phase==Phase.PAUSING){resumeRequested=true;return;}if(phase!=Phase.PAUSED)return;resumeRequested=false;
        if(clock.lifetime(SystemClock.elapsedRealtime())>=12*60*60*1000L){stop(false);return;}
        try{long now=timestamp();checkClock(now);store.resume(session,now);windowStart=now;clock.resume(SystemClock.elapsedRealtime());phase=Phase.ACTIVE;Prefs.put(this,"trackingPaused",false);if(automaticCapture!=null)automaticCapture.recordingBegan();collect();handler.postDelayed(minuteTick,60000);}catch(Exception e){Prefs.put(this,"trackingError","Tracking could not resume: "+e.getMessage());stop(true);}
    }
    private void stop(boolean wasInterrupted){
        interrupted|=wasInterrupted;if(automaticCapture!=null)automaticCapture.close();if(phase==Phase.CLOSED||phase==Phase.STOPPING)return;if(phase==Phase.PAUSING){stopRequested=true;return;}
        boolean wasArmed=phase==Phase.ARMED,waitExpired=automatic!=null&&automatic.state()==AutomaticSleep.State.EXPIRED;if(wasArmed&&automaticTransition!=null){automaticTransition.cancel();if(Prefs.text(this,"automaticResult","").isEmpty())Prefs.put(this,"automaticResult",wasInterrupted?"Automatic waiting was interrupted. Arm again before bed.":"Automatic start cancelled. No recording was created.");}handler.removeCallbacks(automaticTick);Prefs.put(this,"automaticArmed",false);boolean wasPaused=phase==Phase.PAUSED;phase=Phase.STOPPING;long endedAt=timestamp();if(clock!=null)clock.pause(SystemClock.elapsedRealtime());handler.removeCallbacks(minuteTick);handler.removeCallbacks(liveTick);handler.removeCallbacks(limitTick);stopSensors();if(!automaticRun)SleepSoundService.trackingStop(session);NoiseRecorder recorder=audio;audio=null;
        closeAudio(recorder,()->{try{if(store!=null&&session>0){checkClock(endedAt);if(!wasPaused)flush(endedAt,recorder,false);store.end(session,interrupted,clock.active(SystemClock.elapsedRealtime()),Math.max(start,endedAt));}}catch(Exception e){Prefs.put(this,"trackingError","Could not finalize the recording. Saved readings remain; review the interrupted record. "+e.getMessage());}finally{
            try{if(store!=null)store.close();}catch(Exception e){Prefs.put(this,"trackingError","Could not close recording storage: "+e.getMessage());}finally{store=null;phase=Phase.CLOSED;if(recurringToken!=null)try{RecurringTracking.ended(this,recurringOwner,recurringToken,interrupted?"Nightly tracking was interrupted. Open tracking for details.":wasArmed?(waitExpired?"The stillness waiting period ended. Your nightly settings are kept.":"Tonight’s automatic start was cancelled. Your nightly settings are kept."):"Nightly recording ended. Your nightly settings are kept.");}catch(RuntimeException ignored){}if(current==this){running=false;current=null;Prefs.of(this).edit().putBoolean("tracking",false).putBoolean("trackingPaused",false).putLong("session",0).apply();}closer.shutdown();if(!destroyed){stopForeground(STOP_FOREGROUND_REMOVE);stopSelf();}}
        }});
    }
    @Override public void onDestroy(){destroyed=true;stop(true);super.onDestroy();}
}
