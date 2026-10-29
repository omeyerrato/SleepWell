package org.sleepwell.app;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.*;
import android.speech.*;
import java.util.function.*;

/** Per-attempt recognition API and readiness boundary; construction never starts listening. */
interface PhraseRecognizer {
    void english(Consumer<Boolean> installed,IntConsumer failure);
    void listen(Intent request);
    void cancel();
    void destroy();
    interface Access {
        long now();boolean supported();boolean allowed();String unavailable(boolean preview);
        void beforeCapture();void beforeListen();boolean recordingIdle();
        PhraseRecognizer create(RecognitionListener listener);
    }
    final class Native implements Access {
        private final Context app;
        Native(Context context){app=context.getApplicationContext();}
        public long now(){return SystemClock.elapsedRealtime();}
        public boolean supported(){return Build.VERSION.SDK_INT>=31;}
        public boolean allowed(){return app.checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED&&!TrackingService.running;}
        public String unavailable(boolean preview){
            if(!supported())return "On-device phrase recognition requires Android 12 or newer. Use Math instead.";
            try{if(!SpeechRecognizer.isOnDeviceRecognitionAvailable(app))return "No on-device recognizer is installed. Use Math instead.";
                if(preview&&(AlarmService.active||Prefs.flag(app,"ringing")))return "Close the active alarm before using this preview.";
                if(TrackingService.running||MicrophoneLease.occupied())return "Finish the existing microphone capture before listening.";
                AudioManager audio=app.getSystemService(AudioManager.class);if(audio==null||audio.getMode()!=AudioManager.MODE_NORMAL)return "Listening is unavailable during a call.";
                if(!audio.getActiveRecordingConfigurations().isEmpty())return "Another microphone capture is active.";
                SleepSoundService.Snapshot sound=SleepSoundService.snapshot();if(sound!=null&&sound.state==SoundPlayback.State.PLAYING)return "Pause Sleep sounds before listening.";
                return "";
            }catch(RuntimeException unavailable){return "On-device recognition availability could not be checked. Use Math instead.";}
        }
        public void beforeCapture(){if(!allowed())throw new SecurityException();AudioManager audio=app.getSystemService(AudioManager.class);if(audio==null||audio.getMode()!=AudioManager.MODE_NORMAL||!audio.getActiveRecordingConfigurations().isEmpty())throw new IllegalStateException();}
        public void beforeListen(){beforeCapture();AudioManager audio=app.getSystemService(AudioManager.class);if(!AlarmAudioSink.captureQuiet()||audio==null||!audio.getActivePlaybackConfigurations().isEmpty())throw new IllegalStateException();}
        public boolean recordingIdle(){try{AudioManager audio=app.getSystemService(AudioManager.class);return audio!=null&&audio.getActiveRecordingConfigurations().isEmpty();}catch(RuntimeException unknown){return false;}}
        public PhraseRecognizer create(RecognitionListener listener){
            SpeechRecognizer recognizer=SpeechRecognizer.createOnDeviceSpeechRecognizer(app);recognizer.setRecognitionListener(listener);
            return new PhraseRecognizer(){
                public void english(Consumer<Boolean> installed,IntConsumer failure){if(Build.VERSION.SDK_INT<33){installed.accept(true);return;}recognizer.checkRecognitionSupport(PhraseRecognition.request(),app.getMainExecutor(),new RecognitionSupportCallback(){
                    public void onSupportResult(RecognitionSupport support){boolean found=false;for(String language:support.getInstalledOnDeviceLanguages())if(language!=null&&language.equalsIgnoreCase("en-US")){found=true;break;}installed.accept(found);}
                    public void onError(int code){failure.accept(code);}
                });}
                public void listen(Intent request){recognizer.startListening(request);}
                public void cancel(){recognizer.cancel();}
                public void destroy(){recognizer.destroy();}
            };
        }
    }
}
