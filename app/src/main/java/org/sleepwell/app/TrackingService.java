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
    private final AutomationTracking automationEvents=new AutomationTracking((kind,id)->Automations.emit(this,kind,id));
    static final String PAUSE="PAUSE",RESUME="RESUME",STOP="STOP",ARM="ARM",SONAR="SONAR_MANUAL",DELAY="MANUAL_DELAY",DELAY_NOW="MANUAL_DELAY_NOW";
    private boolean flipChosen,flipLight;private TrackingFlipSession flip;private String flipIssue="";
    private TrackingDelay delayPlan;private boolean delayClaiming;private TrackingDelayRuntime.Job delayJob;
    private volatile String dndRun="";private boolean dndRequested;
    static volatile boolean running=false;private static volatile TrackingService current;
    interface StoreFactory{Store open(Context c);}
    static final StoreFactory PRODUCTION_STORE=Store::new;
    static StoreFactory storeFactory=PRODUCTION_STORE;
    private boolean backupProduction;private int foregroundTypes;
    private enum Phase{STARTING,ARMED,DELAYED,ACTIVE,PAUSING,PAUSED,STOPPING,BLOCKED,CLOSED}
    private volatile Phase phase=Phase.STARTING;private boolean stopRequested,resumeRequested,interrupted;private volatile boolean destroyed;private volatile TravelCueRuntime travelCues;private volatile String manualToken="";
    private SensorManager sensors;private PowerManager.WakeLock wake;private Store store;private NoiseRecorder audio;private boolean soundAttempted,automaticRun;private AutomaticSleep automatic;private AutomaticCapture automaticCapture;private AutomaticTransition automaticTransition;private ArmedWaitConditions armedConditions=ArmedWaitConditions.NONE;private volatile String armedBlocked="";private AutomaticSleep.Mode automaticMode;private GoogleSleepSignal googleSleep;
    private final Handler handler=new Handler(Looper.getMainLooper());private final ExecutorService closer=Executors.newSingleThreadExecutor();
    private String recurringToken;private RecurringTracking.Port recurringOwner;
    private volatile boolean sonarMode;private volatile SonarNight sonarNight;private SonarConfig sonarConfig;private boolean sonarAnalysis,sonarClips;private SonarClipPolicy sonarClipPolicy=SonarClipPolicy.OFF;
    private boolean clockChanged;private TrackingClock clock;private volatile long session;private long start,windowStart;private double sum;private Double light;private int count,activeMinutes,pairActiveMinutes;private boolean motionRegistered,lightRegistered;
    private ClipBudget clipBudget=new ClipBudget();
    private boolean batteryAwareRequested,samplingLightWanted,samplingReduced;private BatterySampling batterySampling;private String samplingIssue="";private long samplingBoundary,lastMotionNanos,lastLightNanos;
    private final MotionFilter motionFilter=new MotionFilter();private MotionProfile motionProfile=new MotionProfile(1);
    private final Runnable minuteTick=this::minute,limitTick=()->stop(false),liveTick=this::liveSample,automaticTick=this::automaticTick;
    private final List<org.json.JSONObject> liveReadings=new ArrayList<>();private int previewCount;private double previewSum;private long previewAt;
    static final class Snapshot {
        final boolean flipEnabled,flipListening;final String flipStatus;final long flipRemaining;final long id,start,active,windowStart,now;final String phase,automaticState,googleSignal,samplingStatus;final String delayOwner;final long delayRemaining;final boolean delayed;final boolean sonar;final float sonarGain;final String sonarError;final SoundAnalysisStatus soundAnalysis;final List<SonarFrame> sonarReadings;final long quietMillis;final boolean motion,light,mic,automatic,soundsRequested,keepClips,filteredSounds,soundSettingsBlocked;final int samples;final Double movement,lightLevel;final List<org.json.JSONObject> readings;
        Snapshot(SonarNight.State n){this(n,SoundSensitivity.DEFAULT);}
        Snapshot(SonarNight.State n,SoundSensitivity policy){this(n,policy,false);}
        Snapshot(SonarNight.State n,SoundSensitivity policy,boolean clips){samplingStatus="Battery-aware sampling does not change contactless capture";googleSignal="";soundSettingsBlocked=!policy.available();filteredSounds=!policy.allIncluded();flipListening=false;flipEnabled=false;flipStatus="Unavailable with contactless tracking";flipRemaining=0;delayOwner="";delayed=false;delayRemaining=0;soundAnalysis=n.analysis;sonar=true;sonarGain=n.gain;sonarError=n.error;sonarReadings=n.readings;id=n.id;start=n.start;now=n.now;active=n.active;windowStart=n.start;phase=n.phase;automaticState="";quietMillis=0;soundsRequested=n.analysis.requested;keepClips=clips;automatic=motion=light=false;mic=n.microphone;samples=0;movement=lightLevel=null;readings=List.of();}
        Snapshot(TrackingService s){samplingStatus=s.samplingStatus();googleSignal=s.googleSleep==null?"":s.googleSleep.status();soundSettingsBlocked=!s.clipBudget.analysisAllowed();filteredSounds=!s.clipBudget.sensitivity.allIncluded();flipListening=s.flip!=null&&s.flip.listening;flipEnabled=s.flipChosen;flipStatus=!s.flipIssue.isEmpty()?s.flipIssue:s.sonarMode?"Unavailable with contactless tracking":s.flip==null?s.flipChosen?"Waiting for recording to start":"Off":s.flip.label();flipRemaining=s.flip==null?0:s.flip.remaining();delayOwner=s.delayPlan==null?"":s.delayPlan.token;delayed=s.delayPlan!=null;delayRemaining=s.phase==Phase.DELAYED?s.delayPlan.remaining(SystemClock.elapsedRealtime()):0;soundAnalysis=null;sonar=s.sonarMode;sonarGain=s.sonarConfig==null?0:s.sonarConfig.gain;sonarError="";sonarReadings=List.of();id=s.session;start=s.start;now=s.timestamp();active=s.phase==Phase.DELAYED||s.clock==null?0:s.clock.active(SystemClock.elapsedRealtime());windowStart=s.windowStart;phase=s.phase.name();automaticState=!s.armedBlocked.isEmpty()?s.armedBlocked:s.automatic==null?"":s.automatic.state().name();quietMillis=s.automatic==null?0:s.automatic.quietMillis(SystemClock.elapsedRealtime());automatic=s.automaticRun;soundsRequested=s.sonarMode?s.sonarAnalysis:s.automaticCapture!=null&&s.automaticCapture.requested();keepClips=s.sonarMode?s.sonarClips:s.clipBudget.permitsClips(s.automaticCapture!=null?s.automaticCapture.keepClips():Prefs.flag(s,"saveClips"));motion=s.motionRegistered;light=s.lightRegistered;mic=s.audio!=null&&s.audio.capturing;samples=s.count;movement=s.count==0?null:Math.sqrt(s.sum/s.count);lightLevel=s.light;readings=new ArrayList<>(s.liveReadings);}
        Snapshot(TrackingDelay p,long elapsed){samplingStatus="Waiting · no movement or light capture";googleSignal="";soundSettingsBlocked=false;filteredSounds=false;flipListening=false;flipEnabled=false;flipStatus="Off during delayed start";flipRemaining=0;delayOwner=p.token;delayed=true;delayRemaining=p.remaining(elapsed);soundAnalysis=null;sonar=p.sonar;sonarGain=0;sonarError="";sonarReadings=List.of();id=0;start=p.wall;now=p.wall+Math.max(0,elapsed-p.elapsed);active=0;windowStart=0;phase="DELAYED";automaticState="";quietMillis=0;automatic=false;soundsRequested=p.sounds;keepClips=p.clips;motion=light=mic=false;samples=0;movement=lightLevel=null;readings=List.of();}
        boolean paused(){return phase.equals("PAUSED");}
    }
    static TravelCues.Manual travelManual(){TrackingService s=current;return s!=null&&s.phase==Phase.ACTIVE&&running&&!s.destroyed&&!s.automaticRun&&s.recurringToken==null&&!s.manualToken.isEmpty()&&(!s.sonarMode||s.sonarNight!=null&&s.sonarNight.state().phase.equals("ACTIVE"))?new TravelCues.Manual(s.session,s.manualToken):null;}
    static void armTravel(TravelCues.Manual expected,long revision){if(Looper.myLooper()!=Looper.getMainLooper())throw new IllegalStateException("Open tracking to arm light cues.");TrackingService s=current;TravelCues.Manual manual=travelManual();if(expected==null||manual==null||expected.session!=manual.session||!expected.token.equals(manual.token))throw new IllegalStateException("Use the current active manual recording to arm cues.");TravelCueRuntime old=s.travelCues;if(old!=null&&old.busy())throw new IllegalStateException("Light cues are already armed. Stop them before arming again.");String token=manual.token;long id=manual.session;s.travelCues=new TravelCueRuntime(s,revision,()->s==current&&s.phase==Phase.ACTIVE&&running&&!s.destroyed&&!s.automaticRun&&s.recurringToken==null&&s.session==id&&token.equals(s.manualToken),()->s.handler.post(()->{if(s==current)s.refreshTravelNotification();}));s.refreshTravelNotification();}
    static boolean travelLive(String nonce){TrackingService s=current;TravelCueRuntime r=s==null?null:s.travelCues;return r!=null&&r.nonce.equals(nonce)&&r.busy();}
    static TravelCueSession.State travelStatus(){TrackingService s=current;return s==null||s.travelCues==null?null:s.travelCues.state();}
    static void stopTravel(String nonce,String reason){TrackingService s=current;TravelCueRuntime r=s==null?null:s.travelCues;if(r!=null&&(nonce==null||r.nonce.equals(nonce)))r.stop(reason);}
    static PairMotionRuntime.Manual pairManual(){TrackingService s=current;return s!=null&&running&&!s.destroyed&&s.phase==Phase.ACTIVE&&!s.sonarMode&&!s.automaticRun&&s.recurringToken==null&&s.motionRegistered&&s.backupProduction&&!s.manualToken.isEmpty()?new PairMotionRuntime.Manual(s.session,s.start,s.timestamp(),SystemClock.elapsedRealtime(),s.manualToken):null;}
    static void pairTransportChanged(){if(Looper.myLooper()!=Looper.getMainLooper())throw new IllegalStateException("Open pairing to change nearby transport.");TrackingService s=current;if(s!=null&&running&&!s.destroyed&&s.phase==Phase.ACTIVE)s.retypePairTransport();}
    static boolean ownsDelay(String token){TrackingService s=current;return running&&s!=null&&!s.destroyed&&s.phase==Phase.DELAYED&&s.delayPlan!=null&&s.delayPlan.token.equals(token);}
    static boolean holdsDelayJournal(String token){TrackingService s=current;return running&&s!=null&&!s.destroyed&&s.phase!=Phase.CLOSED&&s.phase!=Phase.STOPPING&&s.phase!=Phase.BLOCKED&&s.delayPlan!=null&&s.delayPlan.token.equals(token);}
    static String delayToken(){TrackingService s=current;return s==null||s.delayPlan==null?"":s.delayPlan.token;}
    static boolean ownsDnd(String token){TrackingService service=current;return running&&service!=null&&!service.destroyed&&service.phase==Phase.ACTIVE&&token!=null&&token.equals(service.dndRun);}
    private void dndActive(){handler.removeCallbacks(dndPulse);TrackingDndRuntime.active(getApplicationContext(),dndRun,dndRequested);handler.postDelayed(dndPulse,60000);}
    private void dndStop(){handler.removeCallbacks(dndPulse);TrackingDndRuntime.stop(getApplicationContext(),dndRun);}
    private final Runnable dndPulse=new Runnable(){public void run(){if(!ownsDnd(dndRun))return;TrackingDndRuntime.renew(getApplicationContext(),dndRun);handler.postDelayed(this,60000);}};
    static boolean ownsRecurring(String token){TrackingService service=current;return running&&service!=null&&token!=null&&token.equals(service.recurringToken);}
    static void cancelRecurring(String token){TrackingService service=current;if(service==null||token==null||!token.equals(service.recurringToken))return;Runnable cancel=()->{if(token.equals(service.recurringToken))service.stop(false);};if(Looper.myLooper()==Looper.getMainLooper())cancel.run();else service.handler.post(cancel);}
    private int rejectRecurring(){phase=Phase.CLOSED;closer.shutdown();stopSelf();return START_NOT_STICKY;}
    private AwakeRuntime awake;private PirNight pir;
    private void beginPir(){if(pir!=null||automaticRun||recurringToken!=null||manualToken.isEmpty()||storeFactory!=PRODUCTION_STORE||Records.factory!=Records.PRODUCTION)return;long id=session;String token=manualToken;pir=PirNight.start(this,id,()->current==this&&running&&!destroyed&&phase==Phase.ACTIVE&&session==id&&manualToken.equals(token),()->sonarMode&&sonarNight!=null?sonarNight.state().now:timestamp());}
    private void closePir(){PirNight prior=pir;pir=null;if(prior!=null)prior.close();}
    static Snapshot snapshot(){TrackingService s=current;return s==null?null:s.sonarMode&&s.sonarNight!=null?new Snapshot(s.sonarNight.state(),s.clipBudget.sensitivity,s.sonarClips):new Snapshot(s);}
    @Override public IBinder onBind(Intent intent){return null;}
    @Override public void onCreate(){super.onCreate();Notifications.channels(this);}
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        String action=intent==null?STOP:intent.getAction();
        if(STOP.equals(action)){if(intent!=null&&intent.hasExtra("delayOwner")&&!ownsDelay(intent.getStringExtra("delayOwner")))return START_NOT_STICKY;stop(false);return START_NOT_STICKY;}
        if(DELAY_NOW.equals(action)){if(intent!=null&&ownsDelay(intent.getStringExtra("delayOwner")))claimDelay(true);return START_NOT_STICKY;}
        if(PAUSE.equals(action)){if(flip!=null)flip.manualPause();if(running)pause();else stopSelf();return START_NOT_STICKY;}
        if(RESUME.equals(action)){if(flip!=null&&(phase==Phase.PAUSED||phase==Phase.PAUSING))flip.manualPause();if(running){if(sonarMode){if(sonarNight==null||!sonarNight.state().phase.equals("PAUSED"))return START_NOT_STICKY;SonarEntry.Ticket admission=SonarEntry.consume(intent.getStringExtra("sonarAdmission"),SystemClock.elapsedRealtime(),session);if(!SonarEntry.sameRun(admission,session,sonarConfig,sonarAnalysis,sonarClips))return START_NOT_STICKY;try{foreground(false);}catch(RuntimeException failure){String message="Contactless resume needs attention: "+failure.getMessage();Prefs.put(this,"trackingError",message);sonarNight.resumeFailed(message);return START_NOT_STICKY;}}resume();}else stopSelf();return START_NOT_STICKY;}
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
        if(batterySampling!=null)batterySampling.close();batterySampling=null;batteryAwareRequested=false;samplingReduced=false;samplingIssue="";
        sonarMode=false;sonarNight=null;sonarConfig=null;sonarAnalysis=false;sonarClips=false;sonarClipPolicy=SonarClipPolicy.OFF;
        if(SONAR.equals(action)){
            if(intent==null||intent.hasExtra("recurringToken"))return rejectRecurring();
            SonarEntry.Ticket admission=SonarEntry.consume(intent.getStringExtra("sonarAdmission"),SystemClock.elapsedRealtime(),0);
            if(admission==null){Prefs.put(this,"trackingError","Contactless start expired. Open tracking and tap Start again.");return rejectRecurring();}
            sonarConfig=admission.config;sonarAnalysis=admission.analysisRequested;sonarClips=admission.clipsRequested;sonarMode=true;session=0;start=System.currentTimeMillis();phase=Phase.STARTING;
        }
        delayPlan=null;delayJob=null;delayClaiming=false;
        if(DELAY.equals(action)||intent!=null&&intent.hasExtra("delayAdmission")){
            TrackingDelay candidate=TrackingDelayEntry.consume(intent==null?null:intent.getStringExtra("delayAdmission"),SystemClock.elapsedRealtime());
            if(candidate==null||candidate.sonar!=sonarMode||sonarMode&&(candidate.sounds!=sonarAnalysis||candidate.clips!=sonarClips)||recurringToken!=null||ARM.equals(action)){Prefs.put(this,"trackingError","Delayed start expired. Open tracking and tap Start again.");return rejectRecurring();}
            delayPlan=candidate;delayJob=TrackingDelayRuntime.get(this);start=System.currentTimeMillis();session=0;
        }
        flipChosen=false;flipLight=true;try{flipChosen=TrackingFlipSettings.enabled(this);if(flipChosen)flipLight=Prefs.of(this).getBoolean("recordLight",true);}catch(RuntimeException invalid){flipChosen=false;flipIssue="Review flip pause settings. Manual Pause still works.";}
        dndRun=UUID.randomUUID().toString();dndRequested=TrackingDndRuntime.requested(this);
        awake=new AwakeRuntime(getApplicationContext(),storeFactory);
        BackupRuntime.admitted();
        clipBudget=new ClipBudget(SoundSensitivitySettings.snapshot(getApplicationContext()));
        current=this;running=true;clock=new TrackingClock(SystemClock.elapsedRealtime());
        try{
            if(sonarMode){if(sonarClips&&(!sonarAnalysis||!clipBudget.analysisAllowed()||!clipBudget.sensitivity.allIncluded()))throw new IllegalStateException("Sound clips require explicit analysis consent and all sound categories.");sonarClipPolicy=sonarClips?SonarClipPolicy.create(true,Prefs.number(this,"noiseThreshold",20)/1000d,clipBudget):SonarClipPolicy.OFF;}if(!sonarMode){motionProfile=MotionSettings.load(this);batteryAwareRequested=BatterySampling.requested(this);}automaticRun=ARM.equals(action);if(automaticRun||!sonarMode&&(delayPlan!=null||flipChosen)){automaticCapture=new AutomaticCapture(delayPlan!=null?delayPlan.sounds:automaticRun?recurringToken==null&&intent.getBooleanExtra("recordSounds",false):Prefs.flag(this,"noiseEnabled"),delayPlan!=null?delayPlan.clips:automaticRun?recurringToken==null&&intent.getBooleanExtra("keepClips",false):Prefs.flag(this,"saveClips"));if(automaticCapture.requested()&&checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)throw new SecurityException("Microphone permission is unavailable. Arm again with sound recording off.");}foreground(false);
            sensors=getSystemService(SensorManager.class);wake=getSystemService(PowerManager.class).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"SleepWell:tracking");
            if(delayPlan!=null){beginDelay();return START_NOT_STICKY;}
            if(automaticRun){arm(intent);return START_NOT_STICKY;}
            if(sonarMode)beginSonar();else beginRecording();
        }catch(Exception e){Prefs.put(this,"trackingError","Tracking could not start: "+e.getMessage());stop(true);}
        return START_NOT_STICKY;
    }
    private void beginDelay(){
        phase=Phase.DELAYED;clock=new TrackingClock(SystemClock.elapsedRealtime());foreground(false);wake.acquire(Math.max(1,delayPlan.remaining(SystemClock.elapsedRealtime())+60000));
        delayJob.arm(delayPlan,p->{if(ownsDelay(p.token))handler.post(delayTick);},error->{if(ownsDelay(delayPlan.token)){Prefs.put(this,"trackingError",error);stop(true);}});
    }
    private final Runnable delayTick=()->{if(delayPlan==null||!ownsDelay(delayPlan.token))return;if(delayPlan.remaining(SystemClock.elapsedRealtime())==0)claimDelay(false);else handler.postDelayed(this.delayTick,1000);};
    private void claimDelay(boolean force){
        if(delayClaiming||delayPlan==null||!ownsDelay(delayPlan.token))return;delayClaiming=true;String token=delayPlan.token;
        delayJob.claim(token,SystemClock.elapsedRealtime(),TrackingDndRuntime.boot(this),force,p->{
            if(!ownsDelay(token))return;delayClaiming=false;if(p==null){Prefs.put(this,"trackingError","The delayed start is no longer pending. Start again explicitly.");stop(true);return;}
            if(p.phase==TrackingDelay.Phase.WAITING){handler.postDelayed(delayTick,1000);return;}
            if(p.phase!=TrackingDelay.Phase.CLAIMED){Prefs.put(this,"trackingError",p.issue);stop(true);return;}
            long actual=SystemClock.elapsedRealtime();if(p.boot!=TrackingDndRuntime.boot(this)||actual<p.elapsed||actual>p.deadline&&actual-p.deadline>TrackingDelay.LATE){delayJob.finish(token,TrackingDelay.Phase.INTERRUPTED,0,"The start callback arrived late. Start again explicitly.");Prefs.put(this,"trackingError","Delayed start was interrupted before capture.");stop(true);return;}
            handler.removeCallbacks(delayTick);phase=Phase.STARTING;
            try{if(sonarMode){start=System.currentTimeMillis();beginSonar();}else{beginRecording();delayJob.finish(token,TrackingDelay.Phase.STARTED,session,"");}}
            catch(RuntimeException failure){delayJob.finish(token,session>0?TrackingDelay.Phase.STARTED:TrackingDelay.Phase.FAILED,session>0?session:0,"Start failed: "+failure.getMessage());Prefs.put(this,"trackingError","Delayed recording could not start: "+failure.getMessage());stop(true);}
        },error->{if(ownsDelay(token)){delayClaiming=false;Prefs.put(this,"trackingError",error);stop(true);}});
    }
    private void cancelDelay(boolean interrupted){handler.removeCallbacks(delayTick);if(delayPlan!=null&&delayJob!=null&&session==0&&(phase==Phase.DELAYED||phase==Phase.STARTING))delayJob.finish(delayPlan.token,interrupted?TrackingDelay.Phase.INTERRUPTED:TrackingDelay.Phase.CANCELLED,0,interrupted?"The waiting service ended. Start again explicitly.":"");}
    private void beginSonar(){
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)throw new SecurityException("Allow microphone access before starting contactless recording.");
        manualToken=UUID.randomUUID().toString();
        Prefs.of(this).edit().putString("trackingError","").putString("audioError","").putString("snoreCueError","").putString("snoreCueRouteError","").apply();
        SonarNight.Clock time=new SonarNight.Clock(){public long millis(){return SystemClock.elapsedRealtime();}public long wall(){return System.currentTimeMillis();}public void idle()throws InterruptedException{Thread.sleep(5);}};
        SonarNight[] owner={null};
        StoreFactory storage=storeFactory;
        SonarNight night=new SonarNight(sonarConfig,(config,cancelled)->SonarAudio.openNight(getApplicationContext(),config,cancelled,()->current==this&&running&&!destroyed&&sonarMode&&sonarNight==owner[0]),new SonarNightStore(getApplicationContext(),storage,time,awake,sonarClipPolicy),time,handler::post,state->{
            if(current!=this||sonarNight!=owner[0])return;
            if(!state.phase.equals("ACTIVE"))DreamCues.cancelRun(session,manualToken,state.phase.equals("PAUSED")||state.phase.equals("PAUSING")?"paused":"ended");Phase previousDndPhase=phase;session=state.id;start=state.start;phase=Phase.valueOf(state.phase);if(phase==Phase.ACTIVE&&previousDndPhase!=Phase.ACTIVE){dndActive();if(pir==null)beginPir();else pir.resume();if(delayPlan!=null)delayJob.finish(delayPlan.token,TrackingDelay.Phase.STARTED,state.id,"");}else if(phase!=Phase.ACTIVE){dndStop();if(pir!=null)pir.pause();}
            if(delayPlan!=null&&state.id==0&&(state.phase.equals("CLOSED")||state.phase.equals("BLOCKED")))delayJob.finish(delayPlan.token,TrackingDelay.Phase.FAILED,0,state.error.isEmpty()?"Contactless recording did not begin.":state.error);
            if(!state.error.isEmpty())Prefs.put(this,"trackingError",state.error);
            Prefs.of(this).edit().putLong("session",session).putBoolean("tracking",state.id>0&&!state.phase.equals("CLOSED")&&!state.phase.equals("BLOCKED")).putBoolean("trackingPaused",state.phase.equals("PAUSED")).apply();
            if(state.phase.equals("PAUSED")){stopSensors();if(!destroyed)foreground(true);}
            else if(state.phase.equals("ACTIVE")){automationEvents.started(state.id,state.start);if(wake!=null&&!wake.isHeld())wake.acquire(Math.max(1,12*3600000L));}
            else if(state.phase.equals("CLOSED")){
                closePir();automationEvents.stopped();
                if(travelCues!=null)travelCues.stop("The recording ended. Light cues stopped.");
                handler.removeCallbacks(limitTick);stopSensors();running=false;current=null;Prefs.of(this).edit().putBoolean("tracking",false).putBoolean("trackingPaused",false).putLong("session",0).apply();closer.shutdown();if(!destroyed){stopForeground(STOP_FOREGROUND_REMOVE);stopSelf();}
            }else if(state.phase.equals("BLOCKED")){if(travelCues!=null)travelCues.stop("Audio cleanup needs attention. Light cues stopped.");if(!destroyed)getSystemService(NotificationManager.class).notify(102,trackingNotification(false));}
        },sonarAnalysis,()->new YamnetClassifier(getApplicationContext()),clipBudget.sensitivity,new SonarSnoreCues(getApplicationContext(),sonarAnalysis,()->current==this&&running&&!destroyed&&sonarMode&&sonarNight==owner[0]&&phase==Phase.ACTIVE),Prefs.flag(this,"sonarPeriodicity"),sonarClipPolicy);
        owner[0]=night;sonarNight=night;wake.acquire(12*3600000L);night.start();handler.postDelayed(limitTick,12*3600000L);
    }
    private void beginRecording(){
            if(batterySampling==null){batterySampling=new BatterySampling(this,batteryAwareRequested,this::refreshSampling);batterySampling.start();}
            StoreFactory storage=storeFactory;store=storage.open(this);backupProduction=storage==PRODUCTION_STORE&&Records.factory==Records.PRODUCTION&&"sleepwell.db".equals(store.getDatabaseName());session=store.begin(SensorProvenance.MOTION,batteryAwareRequested?BatterySampling.config(motionProfile.config()):motionProfile.config());start=store.rows("SELECT start FROM sessions WHERE id=?",""+session).get(0).optLong("start");windowStart=start;clock=new TrackingClock(SystemClock.elapsedRealtime());
            Prefs.of(this).edit().putLong("session",session).putBoolean("tracking",true).putBoolean("trackingPaused",false).putInt("epochCount",0).putString("audioError","").putString("trackingError","").apply();
            if(!automaticRun&&recurringToken==null)manualToken=UUID.randomUUID().toString();phase=Phase.ACTIVE;dndActive();beginPir();automationEvents.started(session,start);if(automaticCapture!=null)automaticCapture.recordingBegan();collect();if(awake!=null)awake.begin(session,start,SystemClock.elapsedRealtime(),lightRegistered);handler.postDelayed(minuteTick,60000);handler.postDelayed(limitTick,12*60*60*1000L);
    }
    private void arm(Intent intent){
        automaticMode=AutomaticSleep.Mode.valueOf(intent.getStringExtra("mode"));
        long elapsed=SystemClock.elapsedRealtime(),delay=Math.max(0,intent.getLongExtra("scheduled",System.currentTimeMillis())-System.currentTimeMillis());
        armedConditions=recurringToken==null?ArmedWaitSettings.intent(intent):ArmedWaitConditions.NONE;armedBlocked="";automatic=new AutomaticSleep(automaticMode,elapsed,elapsed+delay);automaticTransition=new AutomaticTransition(automatic,this::triggerAutomatic,this::automaticAllowed,()->googleSleep==null||googleSleep.ready());start=System.currentTimeMillis();phase=Phase.ARMED;
        if(intent.getBooleanExtra(GoogleSleepSignal.EXTRA,false)){if(recurringToken!=null||automaticMode!=AutomaticSleep.Mode.STILLNESS)throw new IllegalArgumentException("Google sleep estimates require an explicitly armed settling-down wait.");googleSleep=GoogleSleepSignal.open(this,()->{if(current==this&&!destroyed&&phase==Phase.ARMED)try{getSystemService(NotificationManager.class).notify(102,trackingNotification(false));}catch(RuntimeException ignored){}});}
        Sensor sensor=sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        motionRegistered=sensor!=null&&sensors.registerListener(this,sensor,200000);
        if(!motionRegistered)throw new IllegalStateException("Movement sensor is unavailable.");
        wake.acquire(AutomaticSleep.MAX_WAIT);
        Prefs.of(this).edit().putBoolean("automaticArmed",true).putString("automaticResult","").putString("trackingError","").apply();
        foreground(false);handler.post(automaticTick);
    }
    private boolean automaticAllowed(){String previous=armedBlocked;try{boolean charging=true;if(armedConditions.charging){BatteryManager battery=getSystemService(BatteryManager.class);if(battery==null)throw new IllegalStateException("Charging state unavailable");charging=battery.isCharging();}boolean interactive=false;if(armedConditions.screenOff){PowerManager power=getSystemService(PowerManager.class);if(power==null)throw new IllegalStateException("Screen state unavailable");interactive=power.isInteractive();}armedBlocked=armedConditions.blocked(charging,interactive);}catch(RuntimeException unavailable){armedBlocked="WAIT_CONDITIONS_UNAVAILABLE";}if(!previous.equals(armedBlocked)&&phase==Phase.ARMED)handler.post(()->{if(phase==Phase.ARMED&&!destroyed)try{getSystemService(NotificationManager.class).notify(102,trackingNotification(false));}catch(RuntimeException unavailable){android.util.Log.w("SleepWell","Armed wait notification could not refresh");}});return armedBlocked.isEmpty();}
    private void automaticTick(){
        if(phase!=Phase.ARMED)return;
        AutomaticSleep.State state=automaticTransition.tick(SystemClock.elapsedRealtime(),getSystemService(PowerManager.class).isInteractive());
        if(phase!=Phase.ARMED)return;
        if(state==AutomaticSleep.State.EXPIRED){Prefs.put(this,"automaticResult","The four-hour waiting period ended. "+(recurringToken==null?"Arm again when you are ready for bed.":"Your nightly settings are kept."));stop(false);return;}
        handler.postDelayed(automaticTick,1000);
    }
    private void triggerAutomatic(){
        boolean googleEstimate=googleSleep!=null;closeGoogleSleep();handler.removeCallbacks(automaticTick);stopSensors();Prefs.put(this,"automaticArmed",false);
        try{beginRecording();Prefs.put(this,"automaticResult",googleEstimate?"Recording started after Google sleep confidence and stillness checks. Both are estimates.":automaticMode==AutomaticSleep.Mode.STILLNESS?"Recording started after a still, screen-off period. This is a phone-idle estimate.":"Recording started at your selected time.");}
        catch(Exception failure){Prefs.put(this,"trackingError","Automatic tracking could not start: "+failure.getMessage());stop(true);}
    }
    static String armedWaitMessage(String blocked){return blocked.equals("WAITING_CHARGING")?"Waiting for charging. No sleep record or audio yet.":blocked.equals("WAITING_SCREEN_OFF")?"Waiting for screen off. No sleep record or audio yet.":blocked.equals("WAIT_CONDITIONS_UNAVAILABLE")?"Wait conditions unavailable. Open tracking to cancel or retry.":"Automatic start is armed. Open to check progress or cancel.";}
    private Notification trackingNotification(boolean paused){
        boolean waitingDelay=phase==Phase.DELAYED||delayPlan!=null&&phase==Phase.STARTING&&session==0;
        Intent open=new Intent(this,TrackingActivity.class);PendingIntent content=PendingIntent.getActivity(this,41,open,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        Intent stopping=new Intent(this,TrackingService.class).setAction(STOP);if(phase==Phase.DELAYED&&delayPlan!=null)stopping.setData(android.net.Uri.parse("sleepwell://cancel-tracking-delay/"+delayPlan.token)).putExtra("delayOwner",delayPlan.token);
        PendingIntent stop=PendingIntent.getService(this,5,stopping,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent toggle=paused?PendingIntent.getActivity(this,42,new Intent(this,TrackingActivity.class).setAction(RESUME),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT):PendingIntent.getService(this,43,new Intent(this,TrackingService.class).setAction(PAUSE),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        androidx.core.app.NotificationCompat.Builder builder=new androidx.core.app.NotificationCompat.Builder(this,Notifications.TRACK).setSmallIcon(R.drawable.ic_moon).setContentTitle(waitingDelay?"SleepWell · delayed start":phase==Phase.ARMED?"SleepWell · waiting to track":paused?"SleepWell · tracking paused":"SleepWell · recording your night").setContentText(waitingDelay?"No sensors or audio are recording. Open to start now or cancel.":phase==Phase.ARMED?armedWaitMessage(armedBlocked)+(googleSleep==null?"":" "+googleSleep.status()):paused?"Recording capture is paused. Open tracking for resume controls.":"Phone tracking is active. Open to see sensor status.").setContentIntent(content).setOngoing(true).setSilent(true).setOnlyAlertOnce(true).addAction(new androidx.core.app.NotificationCompat.Action(0,waitingDelay?"Cancel delayed start":phase==Phase.ARMED?"Cancel automatic start":"End night",stop));
        if(phase!=Phase.ARMED&&!waitingDelay)builder.addAction(new androidx.core.app.NotificationCompat.Action(0,paused?"Resume":"Pause",toggle));
        TravelCueRuntime cues=travelCues;if(cues!=null&&cues.busy()){builder.setContentText("Experimental light cues armed. Open to review or stop.");Intent cancelCues=new Intent(this,TravelCueStopReceiver.class).setAction(TravelCueStopReceiver.STOP).setData(android.net.Uri.parse("sleepwell://stop-travel-cues/"+cues.nonce)).putExtra("nonce",cues.nonce);PendingIntent stopCues=PendingIntent.getBroadcast(this,68,cancelCues,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);builder.addAction(new androidx.core.app.NotificationCompat.Action(0,"Stop light cues",stopCues));}
        if(PairMotionRuntime.transportOwned(session,manualToken)){PairMotionRuntime.State pair=PairMotionRuntime.state();Intent cancelPair=new Intent(this,PairMotionStopReceiver.class).setAction(PairMotionStopReceiver.ACTION).setData(android.net.Uri.parse("sleepwell://stop-pair/"+pair.link)).putExtra("link",pair.link);PendingIntent stopPair=PendingIntent.getBroadcast(this,243,cancelPair,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);builder.addAction(new androidx.core.app.NotificationCompat.Action(0,"Stop pairing",stopPair));}
        return builder.build();
    }
    private void refreshTravelNotification(){if(current!=this||!running||phase!=Phase.ACTIVE||destroyed||Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return;try{getSystemService(NotificationManager.class).notify(102,trackingNotification(false));}catch(RuntimeException ignored){/* The review screen remains the explicit stop path. */}}
    private void retypePairTransport(){int types=(foregroundTypes&~ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)|(PairMotionRuntime.transportOwned(session,manualToken)?ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE:0);startForeground(102,trackingNotification(false),types);foregroundTypes=types;}
    private void foreground(boolean paused){
        Notification notification=trackingNotification(paused);
        boolean granted=checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED;boolean mic=sonarMode?!paused&&granted:automaticCapture!=null?automaticCapture.foregroundType(granted):!paused&&Prefs.flag(this,"noiseEnabled")&&granted;
        int nearby=PairMotionRuntime.transportOwned(session,manualToken)?ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE:0;
        int types=(Build.VERSION.SDK_INT>=34?ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE:0)|(mic?ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE:0)|nearby;startForeground(102,notification,types);foregroundTypes=types;
        if(automaticCapture!=null&&phase==Phase.STARTING)automaticCapture.foregroundEstablished(mic);
    }
    private void collect(){
        resetWindow();handler.removeCallbacks(liveTick);handler.postDelayed(liveTick,1000);foreground(false);boolean granted=checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED;boolean mic=automaticCapture!=null?automaticCapture.allowsCapture(granted):Prefs.flag(this,"noiseEnabled")&&granted;
        samplingLightWanted=recurringToken==null&&(delayPlan!=null?delayPlan.light:flipChosen?flipLight:Prefs.of(this).getBoolean("recordLight",true));
        registerSampling(batterySampling!=null&&batterySampling.reduced());
        if(!motionRegistered){if(recurringToken!=null)throw new IllegalStateException("Movement sensor could not be registered.");Prefs.put(this,"lastError","Movement sensor unavailable; motion and smart wake cannot be used.");}
        long remaining=Math.max(1,12*60*60*1000L-clock.lifetime(SystemClock.elapsedRealtime()));wake.acquire(remaining);
        if(mic)audio=automaticCapture!=null?new NoiseRecorder(this,store,session,clipBudget,this::timestamp,automaticCapture.keepClips()):new NoiseRecorder(this,store,session,clipBudget,this::timestamp);else if(automaticCapture!=null?automaticCapture.requested():Prefs.flag(this,"noiseEnabled"))Prefs.put(this,"audioError","Microphone permission is unavailable; sound is not being recorded.");
        if(!automaticRun){if(!soundAttempted){soundAttempted=true;SleepSoundService.trackingStart(this,session,clock.lifetime(SystemClock.elapsedRealtime()));}else SleepSoundService.trackingResume(session);}
        activateFlip();
    }
    private void registerSampling(boolean reduced){
        samplingReduced=reduced;samplingIssue="";samplingBoundary=SystemClock.elapsedRealtimeNanos();lastMotionNanos=lastLightNanos=0;motionFilter.reset();
        Sensor motion=sensors==null?null:sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),brightness=sensors==null?null:sensors.getDefaultSensor(Sensor.TYPE_LIGHT);
        try{motionRegistered=motion!=null&&sensors.registerListener(this,motion,reduced?BatterySampling.REDUCED:BatterySampling.NORMAL);}catch(RuntimeException unavailable){motionRegistered=false;}
        if(reduced&&motion!=null&&!motionRegistered){samplingReduced=false;samplingIssue="Reduced motion rate unavailable; normal rate requested";try{motionRegistered=sensors.registerListener(this,motion,BatterySampling.NORMAL);}catch(RuntimeException unavailable){motionRegistered=false;}}
        try{lightRegistered=samplingLightWanted&&brightness!=null&&sensors.registerListener(this,brightness,samplingReduced?BatterySampling.LIGHT:SensorManager.SENSOR_DELAY_NORMAL);}catch(RuntimeException unavailable){lightRegistered=false;}
        if(!motionRegistered)samplingIssue="Movement sensor unavailable; no motion-based early wake";
        if(samplingLightWanted&&!lightRegistered)samplingIssue+=(samplingIssue.isEmpty()?"":" · ")+"Light sensor unavailable";
    }
    private void refreshSampling(){
        if(current!=this||!running||destroyed||sonarMode||phase!=Phase.ACTIVE||batterySampling==null)return;
        boolean reduced=batterySampling.reduced();if(reduced==samplingReduced)return;
        // This listener owns only motion/light. Never stop/recreate audio, sonar, the FGS or its admission.
        try{sensors.unregisterListener(this);}catch(RuntimeException unavailable){samplingIssue="Sampling change unavailable; previous rate retained";return;}
        motionRegistered=lightRegistered=false;light=null;
        try{registerSampling(reduced);}catch(RuntimeException unavailable){samplingIssue="Motion/light sampling could not be updated; capture unavailable";}
    }
    private String samplingStatus(){
        if(sonarMode)return "Battery-aware sampling does not change contactless capture";
        if(phase==Phase.ARMED)return "Automatic waiting keeps normal movement sampling";
        if(phase==Phase.DELAYED)return "Waiting · no movement or light capture";
        String state=batterySampling==null?"Normal sampling":batterySampling.label();
        if(phase==Phase.PAUSED||phase==Phase.PAUSING)return "Paused · movement/light capture stopped";
        return state+(samplingIssue.isEmpty()?"":" · "+samplingIssue);
    }
    private void activateFlip(){if(!flipChosen||sonarMode)return;try{if(flip==null){long ownedSession=session;String ownedRun=dndRun;flip=new TrackingFlipSession(TrackingFlipAndroid.factory.create(getApplicationContext()),new TrackingFlipSession.Effects(){public boolean current(){return current==TrackingService.this&&running&&!destroyed&&!sonarMode&&session==ownedSession&&ownedRun.equals(dndRun)&&(phase==Phase.ACTIVE||phase==Phase.PAUSING||phase==Phase.PAUSED);}public void pause(){TrackingService.this.pause();}public void resume(){TrackingService.this.resume();}},SystemClock::elapsedRealtimeNanos);}flip.active();handler.removeCallbacks(flipTick);handler.postDelayed(flipTick,1000);}catch(RuntimeException unsupported){flipIssue="Gesture input is unavailable. Use Pause manually.";}}
    private final Runnable flipTick=new Runnable(){public void run(){if(flip==null||current!=TrackingService.this||!running||destroyed)return;flip.tick();handler.postDelayed(this,1000);}};
    private void closeFlip(){handler.removeCallbacks(flipTick);if(flip!=null)flip.close();}
    private long timestamp(){return clock==null?System.currentTimeMillis():clock.timestamp(start,SystemClock.elapsedRealtime());}
    private void resetWindow(){sum=0;count=0;light=null;motionFilter.reset();activeMinutes=pairActiveMinutes=0;resetPreview(timestamp());}
    private void resetPreview(long now){liveReadings.clear();previewSum=0;previewCount=0;previewAt=now;}
    private void liveSample(){if(phase!=Phase.ACTIVE)return;long now=timestamp();int measured=count-previewCount;PairMotionRuntime.sample(pairManual(),previewAt,now,SystemClock.elapsedRealtime(),measured>0?Math.sqrt(Math.max(0,sum-previewSum)/measured):null,Math.max(0,measured));if(measured>0)try{liveReadings.add(new org.json.JSONObject().put("start",previewAt).put("time",now).put("movement",Math.sqrt(Math.max(0,sum-previewSum)/measured)));}catch(org.json.JSONException ignored){}previewAt=now;previewCount=count;previewSum=sum;handler.postDelayed(liveTick,1000);}
    private void checkClock(long now){if(store!=null&&session>0&&!clockChanged&&Math.abs(System.currentTimeMillis()-now)>2000){store.clockChanged(session);clockChanged=true;}}
    private void stopSensors(){if(sensors!=null)sensors.unregisterListener(this);motionRegistered=lightRegistered=false;if(wake!=null&&wake.isHeld())wake.release();}
    @Override public void onSensorChanged(SensorEvent event){
        if(phase!=Phase.ACTIVE&&phase!=Phase.ARMED)return;
        if(batteryAwareRequested&&phase==Phase.ACTIVE&&(event.timestamp<samplingBoundary||event.timestamp>SystemClock.elapsedRealtimeNanos()+50000000L||SystemClock.elapsedRealtimeNanos()-event.timestamp>2000000000L))return;
        if(event.sensor.getType()==Sensor.TYPE_LIGHT){if(lightRegistered&&event.values.length>0){if(batteryAwareRequested&&(event.timestamp<=lastLightNanos||!Float.isFinite(event.values[0])||event.values[0]<0||samplingReduced&&lastLightNanos>0&&event.timestamp-lastLightNanos<BatterySampling.LIGHT*1000L))return;lastLightNanos=event.timestamp;light=(double)event.values[0];if(awake!=null)awake.light(light,event.timestamp);}return;}
        if(!motionRegistered)return;
        if(DreamCues.masks(event.timestamp,event.timestamp+1)){motionFilter.reset();lastMotionNanos=0;return;}
        if(event.values.length<3)return;
        long interval=lastMotionNanos==0?0:event.timestamp-lastMotionNanos;
        if(batteryAwareRequested&&phase==Phase.ACTIVE){if(lastMotionNanos>0&&(interval<=0||samplingReduced&&interval<BatterySampling.REDUCED*1000L))return;if(interval>2000000000L)motionFilter.reset();lastMotionNanos=event.timestamp;}
        Double raw=batteryAwareRequested&&phase==Phase.ACTIVE&&samplingReduced?motionFilter.sample(event.values[0],event.values[1],event.values[2],interval):motionFilter.sample(event.values[0],event.values[1],event.values[2]);if(raw==null)return;
        if(phase==Phase.ARMED){automaticTransition.sample(SystemClock.elapsedRealtime(),motionProfile.inference(raw),getSystemService(PowerManager.class).isInteractive());return;}sum+=raw*raw;count++;
    }
    @Override public void onAccuracyChanged(Sensor sensor,int accuracy){}
    private void flush(long now,NoiseRecorder closingAudio,boolean recordEmpty){
        if(store==null||session==0)return;checkClock(now);Double noise=closingAudio==null?null:closingAudio.takeEpoch();
        boolean measured=now>=windowStart&&(recordEmpty&&!batteryAwareRequested||count>0||light!=null||noise!=null);store.checkpoint(session,windowStart,now,count==0?null:Math.sqrt(sum/count),noise,light,count,clock.active(SystemClock.elapsedRealtime()),measured);if(measured)Prefs.put(this,"epochCount",Prefs.number(this,"epochCount",0)+1);
        windowStart=now;sum=0;count=0;if(batteryAwareRequested)light=null;resetPreview(now);
    }
    private void minute(){
        if(phase!=Phase.ACTIVE)return;try{long now=timestamp();double movement=count==0?0:Math.sqrt(sum/count);int measured=count;
        double pairedInference=PairMotionRuntime.inference(session,manualToken,windowStart,now,movement);pairActiveMinutes=motionProfile.activeMinutes(pairedInference,pairActiveMinutes);activeMinutes=motionProfile.activeMinutes(movement,activeMinutes);flush(now,audio,true);Prefs.put(this,"movement",String.format(Locale.ROOT,"%.3f",movement));
        for(Alarm alarm:store.alarms()){
            if(alarm.nap!=null){if(Naps.movement(this,alarm,motionProfile.inference(movement),activeMinutes,measured))break;continue;}
            long deadline=Prefs.of(this).getLong("deadline_"+alarm.id,0);boolean goalMet=!Prefs.flag(this,"smartNeedsGoal")||SleepPlans.load(this).goalMet(clock.active(SystemClock.elapsedRealtime()));
            if(alarm.enabled&&goalMet&&motionProfile.smartWake(System.currentTimeMillis(),deadline,alarm.smartWindow,pairedInference,pairActiveMinutes,measured)){Scheduler.fire(this,alarm,true);break;}
        }
        if(clock.lifetime(SystemClock.elapsedRealtime())>=12*60*60*1000L){stop(false);return;}handler.postDelayed(minuteTick,60000);}catch(Exception e){Prefs.put(this,"trackingError","Could not save sensor readings: "+e.getMessage());stop(true);}
    }
    private void closeAudio(NoiseRecorder recorder,Runnable complete){
        if(recorder==null){complete.run();return;}recorder.requestStop();closer.execute(()->{recorder.close();handler.post(complete);});
    }
    private void pause(){
        DreamCues.cancelRun(session,manualToken,"paused");
        if(pir!=null)pir.pause();
        PairMotionRuntime.recordingStopped(session,manualToken,"PAUSED");
        dndStop();
        if(sonarMode&&sonarNight!=null){if(travelCues!=null)travelCues.stop("Tracking paused. Arm remaining cues explicitly after resuming.");sonarNight.pause();return;}
        if(phase!=Phase.ACTIVE)return;if(awake!=null)awake.pause();phase=Phase.PAUSING;if(travelCues!=null)travelCues.stop("Tracking paused. Arm remaining cues explicitly after resuming.");if(automaticCapture!=null)automaticCapture.pause();long pausedAt=timestamp();clock.pause(SystemClock.elapsedRealtime());handler.removeCallbacks(minuteTick);handler.removeCallbacks(liveTick);stopSensors();if(!automaticRun)SleepSoundService.trackingPause(session);NoiseRecorder recorder=audio;audio=null;
        closeAudio(recorder,()->{try{flush(pausedAt,recorder,false);store.pause(session,pausedAt);resetWindow();phase=Phase.PAUSED;Prefs.put(this,"trackingPaused",true);if(!destroyed)foreground(true);if(flip!=null)flip.paused();if(stopRequested)stop(interrupted);else if(resumeRequested)resume();}catch(Exception e){phase=Phase.PAUSED;Prefs.put(this,"trackingError","Could not save the pause: "+e.getMessage());stop(true);}});
    }
    private void resume(){
        if(sonarMode&&sonarNight!=null){sonarNight.resume();return;}
        if(phase==Phase.PAUSING){resumeRequested=true;return;}if(phase!=Phase.PAUSED)return;resumeRequested=false;
        if(clock.lifetime(SystemClock.elapsedRealtime())>=12*60*60*1000L){stop(false);return;}
        try{long now=timestamp();checkClock(now);store.resume(session,now);windowStart=now;clock.resume(SystemClock.elapsedRealtime());phase=Phase.ACTIVE;if(pir!=null)pir.resume();if(awake!=null)awake.resume();dndActive();Prefs.put(this,"trackingPaused",false);if(automaticCapture!=null)automaticCapture.recordingBegan();collect();handler.postDelayed(minuteTick,60000);}catch(Exception e){Prefs.put(this,"trackingError","Tracking could not resume: "+e.getMessage());stop(true);}
    }
    private void closeGoogleSleep(){GoogleSleepSignal old=googleSleep;googleSleep=null;if(old!=null)old.close();}
    private void stop(boolean wasInterrupted){
        DreamCues.cancelRun(session,manualToken,wasInterrupted?"process":"ended");
        closePir();
        PairMotionRuntime.recordingStopped(session,manualToken,"ENDED");
        if(batterySampling!=null)batterySampling.close();closeGoogleSleep();        closeFlip();dndStop();cancelDelay(wasInterrupted);if(awake!=null)awake.stopInputs();
        if(sonarMode){if(sonarNight!=null){if(travelCues!=null)travelCues.stop("The recording ended. Light cues stopped.");sonarNight.stop(wasInterrupted);}else closeSonarAdmission();return;}
        interrupted|=wasInterrupted;if(automaticCapture!=null)automaticCapture.close();if(phase==Phase.CLOSED||phase==Phase.STOPPING)return;if(phase==Phase.PAUSING){stopRequested=true;return;}
        if(travelCues!=null)travelCues.stop("The recording ended. Light cues stopped.");boolean wasArmed=phase==Phase.ARMED,waitExpired=automatic!=null&&automatic.state()==AutomaticSleep.State.EXPIRED;if(wasArmed&&automaticTransition!=null){automaticTransition.cancel();if(Prefs.text(this,"automaticResult","").isEmpty())Prefs.put(this,"automaticResult",wasInterrupted?"Automatic waiting was interrupted. Arm again before bed.":"Automatic start cancelled. No recording was created.");}handler.removeCallbacks(automaticTick);Prefs.put(this,"automaticArmed",false);boolean wasPaused=phase==Phase.PAUSED;phase=Phase.STOPPING;long endedAt=timestamp();if(clock!=null)clock.pause(SystemClock.elapsedRealtime());handler.removeCallbacks(minuteTick);handler.removeCallbacks(liveTick);handler.removeCallbacks(limitTick);stopSensors();if(!automaticRun&&session>0)SleepSoundService.trackingStop(session);NoiseRecorder recorder=audio;audio=null;
        closeAudio(recorder,()->finishAwake(endedAt,()->{try{if(store!=null&&session>0){checkClock(endedAt);if(!wasPaused)flush(endedAt,recorder,false);store.end(session,interrupted,clock.active(SystemClock.elapsedRealtime()),Math.max(start,endedAt));if(!interrupted)AutoCloudBackups.ended(getApplicationContext(),session,start,Math.max(start,endedAt),backupProduction);}}catch(Exception e){Prefs.put(this,"trackingError","Could not finalize the recording. Saved readings remain; review the interrupted record. "+e.getMessage());}finally{
            try{if(store!=null)store.close();}catch(Exception e){Prefs.put(this,"trackingError","Could not close recording storage: "+e.getMessage());}finally{automationEvents.stopped();store=null;phase=Phase.CLOSED;if(recurringToken!=null)try{RecurringTracking.ended(this,recurringOwner,recurringToken,interrupted?"Nightly tracking was interrupted. Open tracking for details.":wasArmed?(waitExpired?"The stillness waiting period ended. Your nightly settings are kept.":"Tonight’s automatic start was cancelled. Your nightly settings are kept."):"Nightly recording ended. Your nightly settings are kept.");}catch(RuntimeException ignored){}if(current==this){running=false;current=null;Prefs.of(this).edit().putBoolean("tracking",false).putBoolean("trackingPaused",false).putLong("session",0).apply();}closer.shutdown();if(!destroyed){stopForeground(STOP_FOREGROUND_REMOVE);stopSelf();}}
        }}));
    }
    private void finishAwake(long at,Runnable complete){if(awake==null)complete.run();else awake.finish(at,interrupted,complete);}
    /** No Night has opened: this admission owns no Store, sound mixer or NoiseRecorder. */
    private void closeSonarAdmission(){if(phase==Phase.CLOSED)return;phase=Phase.CLOSED;handler.removeCallbacks(limitTick);stopSensors();if(current==this){running=false;current=null;Prefs.of(this).edit().putBoolean("tracking",false).putBoolean("trackingPaused",false).putLong("session",0).apply();}closer.shutdown();if(!destroyed){stopForeground(STOP_FOREGROUND_REMOVE);stopSelf();}}
    @Override public void onDestroy(){if(batterySampling!=null)batterySampling.close();closeFlip();dndStop();destroyed=true;if(sonarMode)stopSensors();stop(true);super.onDestroy();}
}
