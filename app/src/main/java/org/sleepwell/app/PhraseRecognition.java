package org.sleepwell.app;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.*;
import android.speech.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/** Visible, explicit on-device-only recognition. No capture survives its visible owner intentionally. */
final class PhraseRecognition implements RecognitionListener {
    enum State { CHECKING,QUIETING,LISTENING,CLOSING,MATCHED,ERROR,CANCELLED,BLOCKED }
    private static final AtomicReference<PhraseRecognition> OWNER=new AtomicReference<>();
    private final Context app;private final WeakReference<PhraseChallengeActivity> activity;private WeakReference<Runnable> observer;
    final String run;final int unit,rounds;final boolean preview;
    private final Handler main=new Handler(Looper.getMainLooper());private final long deadline=SystemClock.elapsedRealtime()+15000;
    private SpeechRecognizer recognizer;private MicrophoneLease microphone;private SpeakRuntime.Quiet quiet;
    private boolean quietArmed,readyPending,captureRequested,closing,cancelled,matched,apiClosed,terminal;private long cleanSince=-1;
    State state=State.CHECKING;String error="",heard="";long captured=-1;
    private PhraseRecognition(PhraseChallengeActivity activity,Runnable observer){app=activity.getApplicationContext();this.activity=new WeakReference<>(activity);this.observer=new WeakReference<>(observer);run=activity.run;unit=activity.unit;rounds=activity.rounds;preview=activity.preview;}
    static String unavailable(Context context,boolean preview){
        if(Build.VERSION.SDK_INT<31)return "On-device phrase recognition requires Android 12 or newer. Use Math instead.";
        try{if(!SpeechRecognizer.isOnDeviceRecognitionAvailable(context))return "No on-device recognizer is installed. Use Math instead.";
            if(preview&&(AlarmService.active||Prefs.flag(context,"ringing")))return "Close the active alarm before using this preview.";
            if(TrackingService.running||MicrophoneLease.occupied())return "Finish the existing microphone capture before listening.";
            AudioManager audio=context.getSystemService(AudioManager.class);if(audio==null||audio.getMode()!=AudioManager.MODE_NORMAL)return "Listening is unavailable during a call.";
            if(!audio.getActiveRecordingConfigurations().isEmpty())return "Another microphone capture is active.";
            SleepSoundService.Snapshot sound=SleepSoundService.snapshot();if(sound!=null&&sound.state==SoundPlayback.State.PLAYING)return "Pause Sleep sounds before listening.";
            return "";
        }catch(RuntimeException unavailable){return "On-device recognition availability could not be checked. Use Math instead.";}
    }
    static boolean stopBeforeFallback(String run,int unit){PhraseRecognition operation=OWNER.get();if(operation==null||!operation.run.equals(run)||operation.unit!=unit)return true;if(!operation.released())operation.cancel();return operation.released();}
    static PhraseRecognition existing(String run,int unit,int rounds,Runnable changed){PhraseRecognition old=OWNER.get();if(old==null||!old.run.equals(run)||old.unit!=unit||old.rounds!=rounds||old.released())return null;old.observer=new WeakReference<>(changed);return old;}
    static PhraseRecognition begin(PhraseChallengeActivity activity,Runnable changed){
        if(Looper.myLooper()!=Looper.getMainLooper()||!activity.admission()||activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)throw new IllegalStateException("Tap Listen while the phrase challenge is visible.");
        String unavailable=unavailable(activity,activity.preview);if(!unavailable.isEmpty())throw new IllegalStateException(unavailable);
        PhraseRecognition operation=new PhraseRecognition(activity,changed);if(!OWNER.compareAndSet(null,operation))throw new IllegalStateException("Previous recognizer cleanup is still pending.");
        operation.prepare();return operation;
    }
    private boolean current(){PhraseChallengeActivity owner=activity.get();return !cancelled&&owner!=null&&owner.admission()&&owner.unit==unit&&owner.rounds==rounds&&owner.run.equals(run);}
    private boolean valid(){return current()&&SystemClock.elapsedRealtime()<deadline;}
    private void changed(){main.post(()->{Runnable update=observer.get();if(update!=null)update.run();});}
    private void prepare(){
        main.postDelayed(expire,15000);
        try{if(Build.VERSION.SDK_INT<31||!valid())throw new IllegalStateException();recognizer=SpeechRecognizer.createOnDeviceSpeechRecognizer(app);recognizer.setRecognitionListener(this);
            if(Build.VERSION.SDK_INT>=33)checkEnglish();else acquire();
        }catch(RuntimeException failure){error="On-device recognition could not start. Use Math instead.";cleanup();}
    }
    private Intent request(){return new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM).putExtra(RecognizerIntent.EXTRA_LANGUAGE,"en-US").putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1).putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,false);}
    @android.annotation.TargetApi(33) private void checkEnglish(){recognizer.checkRecognitionSupport(request(),app.getMainExecutor(),new RecognitionSupportCallback(){
        @Override public void onSupportResult(RecognitionSupport support){if(closing||!valid()){cancel();return;}boolean installed=false;for(String language:support.getInstalledOnDeviceLanguages())if(language!=null&&language.equalsIgnoreCase("en-US")){installed=true;break;}if(!installed){error="Offline English (United States) is not installed. Install it in your device recognizer settings, or use Math. SleepWell will not download a model.";cleanup();return;}acquire();}
        @Override public void onError(int code){if(closing)return;error="Installed offline English support could not be verified. Use Math instead.";cleanup();}
    });}
    private void acquire(){
        if(closing||!valid()){cancel();return;}
        try{if(app.checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED||TrackingService.running)throw new SecurityException();AudioManager audio=app.getSystemService(AudioManager.class);if(audio==null||audio.getMode()!=AudioManager.MODE_NORMAL||!audio.getActiveRecordingConfigurations().isEmpty())throw new IllegalStateException();microphone=MicrophoneLease.acquire("spoken-phrase");if(microphone==null)throw new IllegalStateException();state=State.QUIETING;changed();
            if(preview){if(AlarmService.active||Prefs.flag(app,"ringing"))throw new IllegalStateException();quiet=()->{};quietArmed=true;main.post(this::listen);}
            else{quiet=AlarmService.quietForSpeak(run,()->{if(!quietArmed)readyPending=true;else listen();});quietArmed=true;if(readyPending)listen();}
        }catch(RuntimeException failure){error="Microphone or alarm-sound exclusion is unavailable. Use Math instead.";cleanup();}
    }
    private void listen(){
        if(closing)return;if(!valid()){cancel();return;}
        try{if(app.checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED||TrackingService.running||!microphone.current()||!AlarmAudioSink.captureQuiet())throw new SecurityException();AudioManager audio=app.getSystemService(AudioManager.class);if(audio==null||audio.getMode()!=AudioManager.MODE_NORMAL||!audio.getActiveRecordingConfigurations().isEmpty())throw new IllegalStateException();if(!audio.getActivePlaybackConfigurations().isEmpty())throw new IllegalStateException();state=State.LISTENING;captureRequested=true;recognizer.startListening(request());changed();main.postDelayed(pulse,200);}
        catch(RuntimeException failure){error="Listening could not begin. Stop other playback or use Math.";cleanup();}
    }
    private final Runnable pulse=new Runnable(){public void run(){if(closing)return;if(!valid()||app.checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED||TrackingService.running){cancel();return;}main.postDelayed(this,200);}};
    private final Runnable expire=()->{if(!released()&&state!=State.BLOCKED){error="No matching phrase was accepted in 15 seconds. Try again or use Math.";cancel();}};
    void cancel(){cancelled=true;if(released())return;cleanup();}
    void retry(){if(state!=State.BLOCKED)return;cleanSince=-1;cleanup();}
    boolean released(){return state==State.MATCHED||state==State.ERROR||state==State.CANCELLED;}
    private void cleanup(){
        if(closing&&state==State.CLOSING)return;closing=true;state=State.CLOSING;main.removeCallbacks(expire);main.removeCallbacks(pulse);changed();
        if(!apiClosed){try{if(recognizer!=null){if(!terminal)try{recognizer.cancel();}catch(RuntimeException ignored){}recognizer.destroy();recognizer=null;}apiClosed=true;}catch(RuntimeException failure){state=State.BLOCKED;error="Recognizer cleanup could not be confirmed. Retry cleanup before continuing.";changed();return;}}
        // destroy() is the public API cleanup contract, not a physical-microphone attestation.
        // Active configurations are an additional veto/diagnostic, never a claim of hardware proof.
        if(!captureRequested){release();return;}
        long began=SystemClock.elapsedRealtime();main.post(new Runnable(){public void run(){if(state!=State.CLOSING)return;boolean quiet=false;try{AudioManager audio=app.getSystemService(AudioManager.class);quiet=audio!=null&&audio.getActiveRecordingConfigurations().isEmpty();}catch(RuntimeException unknown){}long now=SystemClock.elapsedRealtime();if(quiet){if(cleanSince<0)cleanSince=now;if(now-cleanSince>=400){release();return;}}else cleanSince=-1;
            if(now-began>=3000){state=State.BLOCKED;error="Recognizer stopped, but microphone availability is still uncertain. Retry cleanup. Alarm sound stays paused for this request.";changed();return;}main.postDelayed(this,200);}});
    }
    private void release(){
        if(microphone!=null){microphone.released();microphone=null;}if(quiet!=null){SpeakRuntime.Quiet owned=quiet;quiet=null;owned.closed();}
        state=matched&&!cancelled&&current()?State.MATCHED:error.isEmpty()?State.CANCELLED:State.ERROR;OWNER.compareAndSet(this,null);changed();
    }
    @Override public void onResults(Bundle results){if(closing||!valid())return;terminal=true;ArrayList<String> texts=results==null?null:results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);String top=texts==null||texts.isEmpty()?"":texts.get(0);heard=top==null?"":top.length()>240?top.substring(0,240):top;matched=PhraseRule.matches(PhraseRule.target(run,unit),top);if(matched)captured=SystemClock.elapsedRealtime();else error="That did not match the displayed phrase. Try again or use Math.";cleanup();}
    @Override public void onError(int code){if(closing)return;terminal=true;error=switch(code){case SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE->"Offline English is unavailable. Install it in device recognizer settings, or use Math. No model download was requested.";case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS->"Microphone permission was denied or revoked. Use Math.";case SpeechRecognizer.ERROR_NO_MATCH,SpeechRecognizer.ERROR_SPEECH_TIMEOUT->"No matching phrase was recognized. Try again or use Math.";default->"On-device recognition failed. Try again or use Math.";};cleanup();}
    @Override public void onReadyForSpeech(Bundle parameters){if(!valid())cancel();else changed();}
    @Override public void onBeginningOfSpeech(){}
    @Override public void onRmsChanged(float rms){}
    @Override public void onBufferReceived(byte[] buffer){}
    @Override public void onEndOfSpeech(){if(!closing)changed();}
    @Override public void onPartialResults(Bundle partialResults){}
    @Override public void onEvent(int type,Bundle parameters){}
}
