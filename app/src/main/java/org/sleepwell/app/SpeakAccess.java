package org.sleepwell.app;
import android.Manifest;import android.app.*;import android.content.*;import android.content.pm.PackageManager;import android.media.AudioManager;import android.net.Uri;import android.os.SystemClock;import java.lang.ref.WeakReference;import java.util.concurrent.*;import java.util.concurrent.atomic.*;

/** Private one-use visible foreground admission. UI fixtures replace this factory, never run a Service. */
final class SpeakAccess {
    static final int PERMISSION=772;enum Permission { ALLOWED,ASK,SETTINGS }
    interface Operation {SpeakRuntime.State state();String error();int accepted();void cancel();void retry();boolean released();default String progress(){return accepted()+" of 2 speech windows";}}
    interface Factory {boolean current(Context context,String run,int unit);Permission permission(Activity activity);void request(Activity activity);String unavailable(Activity activity);Operation begin(SpeakChallengeActivity activity,String run,int unit,Runnable changed);default Operation existing(String run,int unit,Runnable changed){return null;}}
    static Factory factory=new Native();private static final AtomicReference<NativeOperation> OWNER=new AtomicReference<>(),PENDING=new AtomicReference<>();
    static NativeOperation claim(String id){NativeOperation pending=PENDING.get();if(pending==null||!pending.id.equals(id)||!PENDING.compareAndSet(pending,null))return null;if(!pending.admission.claim(SystemClock.elapsedRealtime())||!pending.visible()||!pending.current()){pending.fail("Listening request expired. Tap Listen again.");return null;}return pending;}
    static void reject(String id){NativeOperation pending=PENDING.get();if(pending!=null&&pending.id.equals(id)&&PENDING.compareAndSet(pending,null))pending.fail("A previous listening service is still closing. Tap Listen again.");}
    /** Switching tasks never bypasses a matching pending admission or unconfirmed microphone close. */
    static boolean stopBeforeFallback(String run,int unit){
        NativeOperation owner=OWNER.get(),pending=PENDING.get();
        boolean ownerMatches=owner!=null&&owner.run.equals(run)&&owner.unit==unit;
        boolean pendingMatches=pending!=null&&pending.run.equals(run)&&pending.unit==unit;
        if(pendingMatches&&pending!=owner&&!pending.released())pending.cancel();
        if(ownerMatches&&!owner.released())owner.cancel();
        return (!ownerMatches||owner.released())&&(!pendingMatches||pending.released());
    }
    static final class Native implements Factory {
        public boolean current(Context context,String run,int unit){return task(context,run,unit);}
        public Operation existing(String run,int unit,Runnable changed){NativeOperation op=OWNER.get();if(op==null||!op.run.equals(run)||op.unit!=unit||op.released())return null;op.observer=new WeakReference<>(changed);return op;}
        public Permission permission(Activity a){return a.checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED?Permission.ALLOWED:!a.getPreferences(0).getBoolean("speakRequested",false)||a.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)?Permission.ASK:Permission.SETTINGS;}
        public void request(Activity a){if(permission(a)==Permission.SETTINGS)a.startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+a.getPackageName())));else{a.getPreferences(0).edit().putBoolean("speakRequested",true).apply();a.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},PERMISSION);}}
        public String unavailable(Activity a){AudioManager manager=a.getSystemService(AudioManager.class);return TrackingService.running||MicrophoneLease.occupied()?"Finish sleep recording before using the microphone.":manager==null||manager.getMode()!=AudioManager.MODE_NORMAL?"Microphone is unavailable during a call.":!manager.getActiveRecordingConfigurations().isEmpty()?"Another microphone capture is active.":"";}
        public Operation begin(SpeakChallengeActivity a,String run,int unit,Runnable changed){if(!a.admission()||!current(a,run,unit)||permission(a)!=Permission.ALLOWED)throw new IllegalStateException("Tap Listen while this challenge is visible.");String issue=unavailable(a);if(!issue.isEmpty())throw new IllegalStateException(issue);NativeOperation op=new NativeOperation(a,run,unit,changed);if(!OWNER.compareAndSet(null,op))throw new IllegalStateException("Previous microphone cleanup is still pending.");if(!PENDING.compareAndSet(null,op)){OWNER.compareAndSet(op,null);throw new IllegalStateException("A listening request is already pending.");}op.expirePending();try{androidx.core.content.ContextCompat.startForegroundService(a,new Intent(a,SpeakCaptureService.class).putExtra("ticket",op.id));}catch(RuntimeException failure){PENDING.compareAndSet(op,null);op.fail("Android did not allow microphone listening. Use Math.");}return op;}
    }
    static final class NativeOperation implements Operation {
        final String id=java.util.UUID.randomUUID().toString(),run;final int unit;final SpeakAdmission admission=new SpeakAdmission(SystemClock.elapsedRealtime());final AudioChallengeMode mode;final Context context;final WeakReference<SpeakChallengeActivity> activity;volatile java.lang.ref.WeakReference<Runnable> observer;final AtomicBoolean cancelled=new AtomicBoolean();final android.os.Handler main=new android.os.Handler(android.os.Looper.getMainLooper());final ExecutorService worker=Executors.newSingleThreadExecutor();volatile SpeakRuntime runtime;volatile String failure="";Runnable finished=()->{};
        NativeOperation(SpeakChallengeActivity a,String run,int unit,Runnable observer){context=a.getApplicationContext();activity=new WeakReference<>(a);this.observer=new WeakReference<>(observer);this.run=run;this.unit=unit;mode=AudioChallengeMode.from(type(context,run,unit));}
        void expirePending(){main.postDelayed(()->{if(runtime==null&&admission.expired(SystemClock.elapsedRealtime())&&PENDING.compareAndSet(this,null))fail("Listening request expired. Tap Listen again.");},Math.max(1,admission.until-SystemClock.elapsedRealtime()+1));}
        boolean visible(){SpeakChallengeActivity a=activity.get();return a!=null&&a.admission();}
        boolean current(){SpeakChallengeActivity a=activity.get();return !cancelled.get()&&a!=null&&a.foreground&&mode.type.equals(type(context,run,unit));}
        void start(Runnable finished){this.finished=finished;runtime=new SpeakRuntime(new SpeakRuntime.Port(){public long now(){return SystemClock.elapsedRealtime();}public boolean current(){return NativeOperation.this.current();}public void check(){if(!current()||context.checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)throw new SecurityException();if(TrackingService.running)throw new IllegalStateException("Tracking started");}public SpeakRuntime.Quiet quiet(Runnable ready){return AlarmService.quietForSpeak(run,ready);}public SpeakRuntime.Capture open()throws Exception{return SpeakAudio.open(context,NativeOperation.this::current);}public SoundAnalysisStream.Model model(){return new YamnetClassifier(context);}public void execute(Runnable work){worker.execute(work);}public void idle()throws Exception{Thread.sleep(10);}public void changed(){notifyChanged();}},mode);runtime.start();}
        void notifyChanged(){main.post(()->{Runnable update=observer.get();if(update!=null)update.run();if(released()){OWNER.compareAndSet(this,null);PENDING.compareAndSet(this,null);finished.run();worker.shutdown();}});}
        void fail(String error){failure=error;if(runtime!=null){runtime.error=error;runtime.cancel();}else OWNER.compareAndSet(this,null);notifyChanged();}
        public SpeakRuntime.State state(){return runtime==null?failure.isEmpty()?SpeakRuntime.State.QUIETING:SpeakRuntime.State.ERROR:runtime.state;}public String error(){return runtime==null?failure:runtime.error;}public int accepted(){return runtime==null?0:runtime.accepted;}public String progress(){return runtime==null?"Waiting for microphone":runtime.progress;}public boolean released(){return runtime==null?!failure.isEmpty():runtime.released();}
        public void cancel(){cancelled.set(true);PENDING.compareAndSet(this,null);if(runtime!=null)runtime.cancel();else fail("Listening was cancelled.");}public void retry(){if(runtime!=null)runtime.retryCleanup();}
    }
    static String type(Context c,String run,int unit){if(unit<0||run==null||run.isEmpty()||!AlarmService.current(c,run)||Prefs.number(c,"challengeCompleted",0)!=unit)return "";try{ChallengeRoutine.Position current=Alarm.read(new org.json.JSONObject(Prefs.text(c,"active_alarm","{}"))).challenges().at(unit);return current==null?"":current.task.type;}catch(Exception invalid){return "";}}
    private static boolean task(Context c,String run,int unit){return AudioChallengeMode.supported(type(c,run,unit));}

    private SpeakAccess(){}
}
