package org.sleepwell.app;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.SystemClock;
import java.util.function.BooleanSupplier;

/** Preview-only readiness/capture composition. Factories never grant an actual alarm admission. */
final class PracticeAudioAccess {
    interface Ports {
        SpeakAccess.Permission permission();void request();boolean ownersBusy();String unavailable(String type);
        long now();void check();
        SpeakRuntime.Capture speech(BooleanSupplier current)throws Exception;
        NoteRuntime.Capture note(BooleanSupplier current)throws Exception;
        SoundAnalysisStream.Model model();PhraseRecognizer.Access phrase();
    }
    interface Factory {Ports create(Activity owner);}
    static Factory factory=Native::new;
    private static final class Native implements Ports {
        private final java.lang.ref.WeakReference<Activity> owner;private final android.content.Context app;
        private final SpeakAccess.Native permission=new SpeakAccess.Native();
        Native(Activity owner){this.owner=new java.lang.ref.WeakReference<>(owner);app=owner.getApplicationContext();}
        public SpeakAccess.Permission permission(){Activity activity=owner.get();return activity==null?SpeakAccess.Permission.SETTINGS:permission.permission(activity);}
        public void request(){Activity activity=owner.get();if(activity==null)throw new IllegalStateException("Practice owner closed");permission.request(activity);}
        public boolean ownersBusy(){try{return AlarmService.active||TrackingService.running||Prefs.flag(app,"ringing");}catch(RuntimeException unreadable){return true;}}
        public long now(){return SystemClock.elapsedRealtime();}
        public String unavailable(String type){
            try{
                if(ownersBusy())return "Finish the active alarm or sleep tracking before listening.";
                if(permission()!=SpeakAccess.Permission.ALLOWED)return "Microphone access is off. Allow access, then tap Listen.";
                if(type.equals(PhraseRule.TYPE))return phrase().unavailable(true);
                if(MicrophoneLease.occupied())return "Another microphone owner is still active or closing.";
                AudioManager audio=app.getSystemService(AudioManager.class);
                if(audio==null||audio.getMode()!=AudioManager.MODE_NORMAL)return "Listening is unavailable during a call.";
                if(!audio.getActiveRecordingConfigurations().isEmpty())return "Another microphone capture is active.";
                SleepSoundService.Snapshot sound=SleepSoundService.snapshot();if(sound!=null&&sound.state==SoundPlayback.State.PLAYING)return "Pause Sleep sounds before listening.";
                if(!AlarmAudioSink.captureQuiet()||!audio.getActivePlaybackConfigurations().isEmpty())return "Stop other playback before listening.";
                return "";
            }catch(RuntimeException unreadable){return "Microphone readiness could not be checked. Retry or choose Math.";}
        }
        public void check(){
            if(ownersBusy()||app.checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)throw new SecurityException("Practice admission lost");
            AudioManager audio=app.getSystemService(AudioManager.class);if(audio==null||audio.getMode()!=AudioManager.MODE_NORMAL)throw new IllegalStateException("Microphone is busy");
            SleepSoundService.Snapshot sound=SleepSoundService.snapshot();if(sound!=null&&sound.state==SoundPlayback.State.PLAYING||!AlarmAudioSink.captureQuiet()||!audio.getActivePlaybackConfigurations().isEmpty())throw new IllegalStateException("Playback started during listening");
        }
        public SpeakRuntime.Capture speech(BooleanSupplier current)throws Exception{return SpeakAudio.open(app,current);}
        public NoteRuntime.Capture note(BooleanSupplier current)throws Exception{return NoteAudio.open(app,current);}
        public SoundAnalysisStream.Model model(){return new YamnetClassifier(app);}
        public PhraseRecognizer.Access phrase(){return new PhraseRecognizer.Native(app);}
    }
    private PracticeAudioAccess(){}
}
