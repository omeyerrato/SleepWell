package org.sleepwell.app;

import android.content.*;
import android.os.*;
import android.speech.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/** Visible, explicit on-device-only recognition. No capture survives its visible owner intentionally. */
final class PhraseRecognition implements RecognitionListener {
    enum State { CHECKING,QUIETING,LISTENING,CLOSING,MATCHED,ERROR,CANCELLED,BLOCKED }
    private static final AtomicReference<PhraseRecognition> OWNER=new AtomicReference<>();
    interface Owner {Context context();String run();int unit();int rounds();boolean preview();boolean current();}
    private final Context app;private final Owner owner;private final PhraseRecognizer.Access access;private WeakReference<Runnable> observer;
    final String run;final int unit,rounds;final boolean preview;
    private final Handler main=new Handler(Looper.getMainLooper());private final long deadline;
    private PhraseRecognizer recognizer;private MicrophoneLease microphone;private SpeakRuntime.Quiet quiet;
    private boolean quietArmed,readyPending,captureRequested,closing,cancelled,matched,apiClosed,terminal;private long cleanSince=-1;
    State state=State.CHECKING;String error="",heard="";long captured=-1;
    private PhraseRecognition(Owner owner,PhraseRecognizer.Access access,Runnable observer){app=owner.context().getApplicationContext();this.owner=owner;this.access=access;this.observer=new WeakReference<>(observer);run=owner.run();unit=owner.unit();rounds=owner.rounds();preview=owner.preview();deadline=access.now()+15000;}
    static String unavailable(Context context,boolean preview){return new PhraseRecognizer.Native(context).unavailable(preview);}
    static boolean stopBeforeFallback(String run,int unit){PhraseRecognition operation=OWNER.get();if(operation==null||!operation.run.equals(run)||operation.unit!=unit)return true;if(!operation.released())operation.cancel();return operation.released();}
    static PhraseRecognition existing(String run,int unit,int rounds,Runnable changed){PhraseRecognition old=OWNER.get();if(old==null||!old.run.equals(run)||old.unit!=unit||old.rounds!=rounds||old.released())return null;old.observer=new WeakReference<>(changed);return old;}
    static PhraseRecognition begin(PhraseChallengeActivity activity,Runnable changed){
        Context context=activity.getApplicationContext();WeakReference<PhraseChallengeActivity> view=new WeakReference<>(activity);String run=activity.run;int unit=activity.unit,rounds=activity.rounds;boolean preview=activity.preview;
        Owner owner=new Owner(){public Context context(){return context;}public String run(){return run;}public int unit(){return unit;}public int rounds(){return rounds;}public boolean preview(){return preview;}public boolean current(){PhraseChallengeActivity a=view.get();return a!=null&&a.admission()&&a.unit==unit&&a.rounds==rounds&&run.equals(a.run);}};
        return begin(owner,new PhraseRecognizer.Native(activity),changed);
    }
    static PhraseRecognition begin(Owner owner,PhraseRecognizer.Access access,Runnable changed){
        if(Looper.myLooper()!=Looper.getMainLooper()||!owner.current()||!access.allowed())throw new IllegalStateException("Tap Listen while the phrase challenge is visible.");
        String unavailable=access.unavailable(owner.preview());if(!unavailable.isEmpty())throw new IllegalStateException(unavailable);
        PhraseRecognition operation=new PhraseRecognition(owner,access,changed);if(!OWNER.compareAndSet(null,operation))throw new IllegalStateException("Previous recognizer cleanup is still pending.");
        operation.prepare();return operation;
    }
    private boolean current(){return !cancelled&&owner.current()&&owner.unit()==unit&&owner.rounds()==rounds&&owner.run().equals(run);}
    private boolean valid(){return current()&&access.now()<deadline;}
    private void changed(){main.post(()->{Runnable update=observer.get();if(update!=null)update.run();});}
    private void prepare(){
        main.postDelayed(expire,15000);
        try{if(!access.supported()||!valid())throw new IllegalStateException();recognizer=access.create(this);checkEnglish();
        }catch(RuntimeException failure){error="On-device recognition could not start. Use Math instead.";cleanup();}
    }
    static Intent request(){return new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM).putExtra(RecognizerIntent.EXTRA_LANGUAGE,"en-US").putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1).putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,false);}
    private void checkEnglish(){recognizer.english(installed->{if(closing||!valid()){cancel();return;}if(!installed){error="Offline English (United States) is not installed. Install it in your device recognizer settings, or use Math. SleepWell will not download a model.";cleanup();return;}acquire();},code->{if(closing)return;error="Installed offline English support could not be verified. Use Math instead.";cleanup();});}
    private void acquire(){
        if(closing||!valid()){cancel();return;}
        try{access.beforeCapture();microphone=MicrophoneLease.acquire("spoken-phrase");if(microphone==null)throw new IllegalStateException();state=State.QUIETING;changed();
            if(preview){if(AlarmService.active||Prefs.flag(app,"ringing"))throw new IllegalStateException();quiet=()->{};quietArmed=true;main.post(this::listen);}
            else{quiet=AlarmService.quietForSpeak(run,()->{if(!quietArmed)readyPending=true;else listen();});quietArmed=true;if(readyPending)listen();}
        }catch(RuntimeException failure){error="Microphone or alarm-sound exclusion is unavailable. Use Math instead.";cleanup();}
    }
    private void listen(){
        if(closing)return;if(!valid()){cancel();return;}
        try{if(microphone==null||!microphone.current())throw new SecurityException();access.beforeListen();state=State.LISTENING;captureRequested=true;recognizer.listen(request());changed();main.postDelayed(pulse,200);}
        catch(RuntimeException failure){error="Listening could not begin. Stop other playback or use Math.";cleanup();}
    }
    private final Runnable pulse=new Runnable(){public void run(){if(closing)return;if(!valid()||!access.allowed()){cancel();return;}main.postDelayed(this,200);}};
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
        long began=access.now();main.post(new Runnable(){public void run(){if(state!=State.CLOSING)return;boolean quiet=false;try{quiet=access.recordingIdle();}catch(RuntimeException unknown){}long now=access.now();if(quiet){if(cleanSince<0)cleanSince=now;if(now-cleanSince>=400){release();return;}}else cleanSince=-1;
            if(now-began>=3000){state=State.BLOCKED;error="Recognizer stopped, but microphone availability is still uncertain. Retry cleanup. Alarm sound stays paused for this request.";changed();return;}main.postDelayed(this,200);}});
    }
    private void release(){
        if(microphone!=null){microphone.released();microphone=null;}if(quiet!=null){SpeakRuntime.Quiet owned=quiet;quiet=null;owned.closed();}
        state=matched&&!cancelled&&current()?State.MATCHED:error.isEmpty()?State.CANCELLED:State.ERROR;OWNER.compareAndSet(this,null);changed();
    }
    @Override public void onResults(Bundle results){if(closing||!valid())return;terminal=true;ArrayList<String> texts=results==null?null:results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);String top=texts==null||texts.isEmpty()?"":texts.get(0);heard=top==null?"":top.length()>240?top.substring(0,240):top;matched=PhraseRule.matches(PhraseRule.target(run,unit),top);if(matched)captured=access.now();else error="That did not match the displayed phrase. Try again or use Math.";cleanup();}
    @Override public void onError(int code){if(closing)return;terminal=true;error=switch(code){case SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE->"Offline English is unavailable. Install it in device recognizer settings, or use Math. No model download was requested.";case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS->"Microphone permission was denied or revoked. Use Math.";case SpeechRecognizer.ERROR_NO_MATCH,SpeechRecognizer.ERROR_SPEECH_TIMEOUT->"No matching phrase was recognized. Try again or use Math.";default->"On-device recognition failed. Try again or use Math.";};cleanup();}
    @Override public void onReadyForSpeech(Bundle parameters){if(!valid())cancel();else changed();}
    @Override public void onBeginningOfSpeech(){}
    @Override public void onRmsChanged(float rms){}
    @Override public void onBufferReceived(byte[] buffer){}
    @Override public void onEndOfSpeech(){if(!closing)changed();}
    @Override public void onPartialResults(Bundle partialResults){}
    @Override public void onEvent(int type,Bundle parameters){}
}
