package org.sleepwell.app;

import android.content.*;
import android.media.*;
import android.net.Uri;
import android.os.*;
import java.io.IOException;

/** No AlarmAudioSink/volume lease/global-volume writes. One bounded, cancel-fenced decoder owner. */
final class AlarmToneAudio implements AlarmTonePreview.Port {
    interface Provider {AlarmTonePreview.Port create(Context context);}
    static Provider provider=AlarmToneAudio::new;
    private final Context context;private final Handler main=new Handler(Looper.getMainLooper());private volatile boolean closed;
    AlarmToneAudio(Context context){this.context=context.getApplicationContext();}
    private boolean occupied(){SleepSoundService.Snapshot sound=SleepSoundService.snapshot();return AlarmService.active||Prefs.flag(context,"ringing")||TrackingService.running||sound!=null&&(sound.state==SoundPlayback.State.PLAYING||sound.state==SoundPlayback.State.LOADING);}
    @Override public AlarmTonePreview.Session open(String tone,float gain,AlarmTonePreview.Events events){if(closed)throw new IllegalStateException("Preview closed");ToneDecoderLease lease=ToneDecoderLease.acquire();try{Attempt attempt=new Attempt(tone,gain,events,lease);current=attempt;attempt.worker.post(attempt::prepare);main.postDelayed(attempt.timeout,8000);return attempt;}catch(RuntimeException|Error failure){lease.close();throw failure;}}
    private volatile Attempt current;
    private final class Attempt implements AlarmTonePreview.Session {
        final String tone;final AlarmTonePreview.Events events;final ToneDecoderLease lease;final HandlerThread thread=new HandlerThread("SleepWell quiet tone preview");final Handler worker;final CancellationSignal cancellation=new CancellationSignal();volatile boolean ended;volatile float gain;MediaPlayer player;AudioManager audio;AudioFocusRequest focus;boolean prepared,playing,released;long firstPlay;
        final Runnable timeout=()->fail("Preview took too long to open. Try another sound.");
        Attempt(String tone,float gain,AlarmTonePreview.Events events,ToneDecoderLease lease){this.tone=tone;this.gain=clamp(gain);this.events=events;this.lease=lease;thread.start();worker=new Handler(thread.getLooper());}
        boolean live(){return !closed&&!ended;}
        void notify(Runnable action){main.post(()->{if(live())action.run();});}
        AudioAttributes attributes(){return new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();}
        void prepare(){if(!live()){release();return;}try{if(occupied())throw new IOException("Pause tracking or other audio before previewing.");player=new MediaPlayer();player.setAudioAttributes(attributes());player.setVolume(0,0);player.setLooping(false);player.setOnPreparedListener(p->{if(!live()){release();return;}prepared=true;main.removeCallbacks(timeout);notify(events::ready);});player.setOnErrorListener((p,what,extra)->{fail("This sound could not be previewed.");return true;});player.setOnCompletionListener(p->{if(live()){main.post(events::ended);close();}});
            if(tone.startsWith("content:")||tone.equals("system")){Uri uri=tone.equals("system")?RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM):Uri.parse(tone);if(!"content".equals(uri.getScheme()))throw new IOException("Unsupported audio location");try(android.content.res.AssetFileDescriptor fd=context.getContentResolver().openAssetFileDescriptor(uri,"r",cancellation)){if(fd==null)throw new IOException("Audio unavailable");if(fd.getDeclaredLength()>=0)player.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getDeclaredLength());else player.setDataSource(fd.getFileDescriptor());}}
            else if(AlarmTones.bundled(tone))try(android.content.res.AssetFileDescriptor fd=context.getAssets().openFd(AlarmTones.asset(tone))){player.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength());}else throw new IOException("Unsupported sound");if(live())player.prepareAsync();else release();
        }catch(Exception failure){fail("Audio unavailable. Import it again or choose a built-in sound.");}}
        public void play(){worker.post(()->{if(!live()||!prepared)return;try{if(occupied())throw new IOException("Pause tracking or other audio before previewing.");if(focus==null){audio=context.getSystemService(AudioManager.class);focus=new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).setAudioAttributes(attributes()).setWillPauseWhenDucked(true).setOnAudioFocusChangeListener(change->{if(change!=AudioManager.AUDIOFOCUS_GAIN)pause();},worker).build();if(audio.requestAudioFocus(focus)!=AudioManager.AUDIOFOCUS_REQUEST_GRANTED)throw new IOException("Audio is busy. Try again later.");}if(!live())return;player.setVolume(gain,gain);player.start();playing=true;if(firstPlay==0)firstPlay=SystemClock.elapsedRealtime();notify(events::playing);worker.removeCallbacks(tick);worker.post(tick);}catch(Exception failure){fail(failure.getMessage());}});}
        public void pause(){worker.post(()->{if(!live())return;try{if(player!=null&&playing)player.pause();playing=false;abandon();notify(events::paused);}catch(RuntimeException failure){fail("Preview could not pause.");}});}
        public void gain(float value){gain=clamp(value);worker.post(()->{if(live()&&prepared)try{player.setVolume(gain,gain);}catch(RuntimeException failure){fail("Preview volume unavailable.");}});}
        final Runnable tick=new Runnable(){public void run(){if(!live())return;if(occupied()){fail("Preview stopped because other audio started.");return;}if(firstPlay>0&&SystemClock.elapsedRealtime()-firstPlay>=20000){main.post(()->events.ended());close();return;}worker.postDelayed(this,100);}};
        void abandon(){if(focus!=null&&audio!=null){AudioFocusRequest request=focus;focus=null;try{audio.abandonAudioFocusRequest(request);}catch(RuntimeException ignored){}}}
        void fail(String message){if(!live())return;main.post(()->events.failed(message));close();}
        public void close(){if(ended)return;ended=true;try{cancellation.cancel();}catch(RuntimeException ignored){}main.removeCallbacks(timeout);worker.post(this::release);}
        void release(){if(released)return;released=true;worker.removeCallbacks(tick);MediaPlayer prior=player;player=null;boolean disposed=prior==null;if(prior!=null){try{prior.setVolume(0,0);}catch(RuntimeException ignored){}try{prior.release();disposed=true;}catch(RuntimeException ignored){/* Keep process lease: cleanup was not proven. */}}prepared=false;playing=false;abandon();if(current==this)current=null;if(disposed)lease.close();thread.quitSafely();}
    }
    static float clamp(float value){return Float.isFinite(value)?Math.max(0,Math.min(.25f,value)):0;}
    public void close(){closed=true;Attempt prior=current;if(prior!=null)prior.close();}
}
