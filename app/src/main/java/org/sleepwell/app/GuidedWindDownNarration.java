package org.sleepwell.app;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

/** Explicit foreground narration using only an installed, engine-declared offline English voice. */
final class GuidedWindDownNarration implements AutoCloseable {
    interface Events {void status(String message,boolean failed);}
    private static final AtomicReference<GuidedWindDownNarration> OWNER=new AtomicReference<>();
    private final Context app;private final BooleanSupplier foreground;private final Events events;
    private final Handler main=new Handler(Looper.getMainLooper());
    private final HandlerThread thread=new HandlerThread("SleepWell guide narration");private final Handler worker;
    private final AudioAttributes attributes=new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build();
    private final String run=UUID.randomUUID().toString();private volatile boolean closed;private boolean ready,failed;private TextToSpeech speech;private AudioManager audio;private AudioFocusRequest focus;private Voice voice;private String pending,spoken="",utterance="";private int serial,focusGeneration;
    GuidedWindDownNarration(Context context,BooleanSupplier foreground,Events events,String first){
        app=context.getApplicationContext();this.foreground=foreground;this.events=events;pending=first;
        if(!OWNER.compareAndSet(null,this))throw new IllegalStateException("Previous narration is still stopping. Try again shortly.");
        try{thread.start();worker=new Handler(thread.getLooper());worker.post(this::initialize);}catch(RuntimeException failure){OWNER.compareAndSet(this,null);throw failure;}
    }
    private boolean allowed(){return !closed&&OWNER.get()==this&&foreground.getAsBoolean()&&!TrackingService.running&&!AlarmService.active&&!Prefs.flag(app,"ringing");}
    private void status(String message,boolean error){main.post(()->{if(OWNER.get()==this&&!closed)events.status(message,error);});}
    private void initialize(){
        if(!allowed()){close();return;}
        status("Preparing an installed offline English voice…",false);
        worker.postDelayed(initializationTimeout,10000);
        try{speech=new TextToSpeech(app,value->worker.post(()->initialized(value)));worker.post(watch);}catch(RuntimeException unavailable){fail("Text-to-speech is unavailable. The silent guide still works.");}
    }
    private final Runnable initializationTimeout=()->fail("Offline voice initialization timed out. The silent guide still works.");
    private void initialized(int result){
        if(!allowed()){close();return;}worker.removeCallbacks(initializationTimeout);
        if(result!=TextToSpeech.SUCCESS||speech==null){fail("No working text-to-speech provider. Install an offline English voice in Android settings.");return;}
        try{
            Set<Voice> available=speech.getVoices();ArrayList<Voice> local=new ArrayList<>();
            if(available!=null)for(Voice candidate:available)if(offline(candidate))local.add(candidate);
            local.sort(Comparator.comparingInt(Voice::getQuality).reversed().thenComparing(Voice::getName));
            if(local.isEmpty()){fail("No installed offline English voice. Install one in Android text-to-speech settings, then retry.");return;}
            voice=local.get(0);
            if(speech.setVoice(voice)!=TextToSpeech.SUCCESS||!selectedVoice()){fail("The installed provider could not select an offline voice. The silent guide still works.");return;}
            if(speech.setAudioAttributes(attributes)!=TextToSpeech.SUCCESS||speech.setSpeechRate(.90f)!=TextToSpeech.SUCCESS){fail("The speech provider could not configure narration.");return;}
            if(speech.setOnUtteranceProgressListener(new UtteranceProgressListener(){
                public void onStart(String id){worker.post(()->{if(!allowed()){close();return;}if(id.equals(utterance))status("Reading with an installed offline voice.",false);});}
                public void onDone(String id){worker.post(()->{if(!closed&&id.equals(utterance)){utterance="";worker.removeCallbacks(utteranceTimeout);abandon();status("Narration on · the next step will be read when it begins.",false);}});}
                public void onError(String id){onError(id,TextToSpeech.ERROR);}
                public void onError(String id,int code){worker.post(()->{if(!closed&&id.equals(utterance))fail(code==TextToSpeech.ERROR_NETWORK||code==TextToSpeech.ERROR_NETWORK_TIMEOUT?"The provider requested network speech. Narration stopped; use an installed offline voice.":code==TextToSpeech.ERROR_NOT_INSTALLED_YET?"The selected voice is not installed. Install an offline English voice, then retry.":"Narration could not play. The silent guide still works.");});}
            })!=TextToSpeech.SUCCESS){fail("The speech provider cannot report narration playback.");return;}
            ready=true;readPending();
        }catch(RuntimeException unavailable){fail("Offline narration is unavailable. The silent guide still works.");}
    }
    private static boolean offline(Voice value){return value!=null&&value.getLocale()!=null&&"en".equals(value.getLocale().getLanguage())&&!value.isNetworkConnectionRequired()&&(value.getFeatures()==null||!value.getFeatures().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED));}
    private boolean selectedVoice(){Voice selected=speech.getVoice();return offline(selected)&&voice.getName().equals(selected.getName());}
    void step(String text){if(closed)return;worker.post(()->{if(!allowed()){close();return;}pending=text;if(ready)readPending();});}
    private void readPending(){
        if(!allowed()){close();return;}if(!ready||pending==null||pending.equals(spoken))return;
        try{
            if(!selectedVoice()){fail("The provider changed away from the selected offline voice. Narration stopped.");return;}
            speech.stop();worker.removeCallbacks(utteranceTimeout);abandon();audio=app.getSystemService(AudioManager.class);
            int requestedFocus=++focusGeneration;focus=new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).setAudioAttributes(attributes).setWillPauseWhenDucked(true).setAcceptsDelayedFocusGain(false).setOnAudioFocusChangeListener(value->{if(requestedFocus==focusGeneration&&value!=AudioManager.AUDIOFOCUS_GAIN&&!closed)fail("Narration paused when audio focus changed. Press Play narration to continue.");},worker).build();
            if(audio==null||audio.requestAudioFocus(focus)!=AudioManager.AUDIOFOCUS_REQUEST_GRANTED){fail("Audio focus is busy. Pause other audio and press Play narration again.");return;}
            if(!allowed()){close();return;}
            Bundle parameters=new Bundle();parameters.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME,.65f);parameters.putString(TextToSpeech.Engine.KEY_FEATURE_EMBEDDED_SYNTHESIS,"true");
            utterance=run+":"+(++serial);spoken=pending;
            if(speech.speak(spoken,TextToSpeech.QUEUE_FLUSH,parameters,utterance)!=TextToSpeech.SUCCESS){fail("The speech provider rejected narration. The silent guide still works.");return;}
            worker.postDelayed(utteranceTimeout,60000);
        }catch(RuntimeException unavailable){fail("Narration could not play. The silent guide still works.");}
    }
    private final Runnable utteranceTimeout=()->fail("Narration timed out and stopped. The silent guide still works.");
    private final Runnable watch=new Runnable(){public void run(){if(!allowed()){fail("Narration stopped because the guide lost focus or an alarm or recording took priority.");return;}if(!closed)worker.postDelayed(this,150);}};
    private void fail(String message){
        if(closed||failed)return;failed=true;closed=true;
        try{stopNative();}finally{OWNER.compareAndSet(this,null);thread.quitSafely();}
        // The Activity checks its own request generation before accepting this terminal error.
        // Cleanup does not depend on a visible Activity receiving the callback.
        main.post(()->events.status(message,true));
    }
    private void abandon(){++focusGeneration;AudioFocusRequest owned=focus;focus=null;if(owned!=null&&audio!=null)try{audio.abandonAudioFocusRequest(owned);}catch(RuntimeException ignored){}}
    private void stopNative(){worker.removeCallbacksAndMessages(null);ready=false;utterance="";TextToSpeech owned=speech;speech=null;if(owned!=null){try{owned.stop();}catch(RuntimeException ignored){}try{owned.shutdown();}catch(RuntimeException ignored){}}abandon();}
    @Override public void close(){if(closed)return;closed=true;worker.post(()->{try{stopNative();}finally{OWNER.compareAndSet(this,null);thread.quitSafely();}});}
}
