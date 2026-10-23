package org.sleepwell.app;

import android.content.Context;
import android.media.*;
import android.os.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** One native owner thread; private descriptors, interval boundaries and generation-fenced callbacks. */
final class ClipPlayback implements ClipPlaybackPort {
    interface Provider {ClipPlaybackPort create(Context context,Records.Factory factory);}
    static Provider provider=ClipPlayback::new;
    private final Context context;private final Records.Factory factory;private final HandlerThread thread=new HandlerThread("SleepWell-clip-review");private final Handler worker,main=new Handler(Looper.getMainLooper());
    private final Set<NativeSession> sessions=ConcurrentHashMap.newKeySet();private volatile boolean closed;
    ClipPlayback(Context context,Records.Factory factory){this.context=context.getApplicationContext();this.factory=factory;thread.start();worker=new Handler(thread.getLooper());}
    @Override public ClipReviewPlayer.Session open(ClipReviewPlayer.Target value,ClipReviewPlayer.Events events){if(closed||!(value instanceof ClipTarget))throw new IllegalStateException("Player is unavailable");NativeSession session=new NativeSession((ClipTarget)value,events);sessions.add(session);main.postDelayed(session.timeout,8000);worker.post(session::prepare);return session;}
    private boolean occupied(){SleepSoundService.Snapshot sound=SleepSoundService.snapshot();return Prefs.flag(context,"ringing")||TrackingService.running||sound!=null&&(sound.state==SoundPlayback.State.PLAYING||sound.state==SoundPlayback.State.LOADING);}
    private AudioAttributes attributes(){return new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build();}
    private final class NativeSession implements ClipReviewPlayer.Session {
        final ClipTarget target;final ClipReviewPlayer.Events events;volatile boolean ended,wanted;MediaPlayer player;AudioManager audio;AudioFocusRequest focus;boolean prepared,seeking,playing;long desired,seekTarget;int focusGeneration;
        final Runnable timeout=()->{if(!ended)fail("Recording took too long to open. Try again.");};
        NativeSession(ClipTarget target,ClipReviewPlayer.Events events){this.target=target;this.events=events;}
        boolean live(){return !closed&&!ended;}
        void send(Runnable action){main.post(()->{if(live())action.run();});}
        void prepare(){if(!live())return;try{
            if(occupied())throw new IOException("Pause tracking or sleep sounds before reviewing audio.");
            try(Store store=factory.open(context)){
                ClipReviewAccess.check(store,target);
            }
            File file=FileSafety.clip(context,target.clip.optString("path"));ClipWave wave=ClipWave.read(file);if(wave.durationMs!=target.wholeDurationMs||target.fromMs<0||target.durationMs<=0||target.fromMs+target.durationMs>wave.durationMs)throw new IOException("The recording interval changed. Reopen this night.");
            if(!live())return;player=new MediaPlayer();MediaPlayer owned=player;owned.setAudioAttributes(attributes());
            try(FileInputStream input=new FileInputStream(file)){owned.setDataSource(input.getFD());}
            owned.setOnPreparedListener(p->{if(!live()||p!=player)return;try{if(target.fromMs+target.durationMs>p.getDuration())throw new IOException("The complete event recording is unavailable.");prepared=true;main.removeCallbacks(timeout);send(events::ready);}catch(Exception error){fail(error.getMessage());}});
            owned.setOnErrorListener((p,what,extra)->{if(p==player)fail("This recording could not play.");return true;});
            owned.setOnSeekCompleteListener(p->{if(!live()||p!=player||!seeking)return;seeking=false;if(desired!=seekTarget){seekNow();return;}publishPosition();if(desired>=target.durationMs){complete();return;}if(wanted)begin();else pausedAck();});
            owned.setOnCompletionListener(p->{if(live()&&p==player)complete();});if(!live()){release();return;}owned.prepareAsync();
        }catch(Exception error){fail(error.getMessage()==null?"Recording unavailable.":error.getMessage());}}
        @Override public void play(long position){wanted=true;worker.post(()->{if(!live()||!prepared||!wanted)return;desired=bound(position);seekNow();});}
        @Override public void pause(){wanted=false;worker.post(()->{if(!live())return;try{if(player!=null&&playing)player.pause();playing=false;worker.removeCallbacks(tick);publishPosition();abandon();pausedAck();}catch(RuntimeException error){fail("Playback could not pause.");}});}
        void pausedAck(){send(()->{if(!wanted)events.paused();});}
        @Override public void seek(long position){worker.post(()->{if(!live()||!prepared)return;desired=bound(position);seekNow();});}
        long bound(long value){return Math.max(0,Math.min(target.durationMs,value));}
        void seekNow(){if(!live()||!prepared||seeking)return;try{if(playing)player.pause();playing=false;worker.removeCallbacks(tick);seekTarget=desired;seeking=true;player.seekTo(target.fromMs+seekTarget,MediaPlayer.SEEK_CLOSEST);}catch(RuntimeException error){fail("The recording could not seek.");}}
        void begin(){if(!live()||!wanted||!prepared||playing)return;try{
            if(occupied())throw new IOException("Pause tracking or sleep sounds before reviewing audio.");
            try(Store store=factory.open(context)){ClipReviewAccess.check(store,target);}
            if(focus==null){audio=context.getSystemService(AudioManager.class);int request=++focusGeneration;focus=new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).setAudioAttributes(attributes()).setWillPauseWhenDucked(true).setOnAudioFocusChangeListener(change->{if(request==focusGeneration&&change!=AudioManager.AUDIOFOCUS_GAIN)pause();},worker).build();
            if(audio.requestAudioFocus(focus)!=AudioManager.AUDIOFOCUS_REQUEST_GRANTED)throw new IOException("Audio is busy. Try again later.");}
            if(!live()||!wanted){abandon();return;}player.start();playing=true;send(events::playing);worker.removeCallbacks(tick);worker.post(tick);
        }catch(Exception error){fail(error.getMessage()==null?"Playback stopped unexpectedly.":error.getMessage());}}
        void publishPosition(){if(!live()||!prepared||player==null)return;long position=bound(player.getCurrentPosition()-target.fromMs);send(()->events.position(position));}
        final Runnable tick=new Runnable(){public void run(){if(!live()||!playing)return;try{if(occupied()){pause();return;}long position=bound(player.getCurrentPosition()-target.fromMs);if(position>=target.durationMs){complete();return;}send(()->events.position(position));worker.postDelayed(this,Math.max(1,Math.min(100,target.durationMs-position)));}catch(RuntimeException error){fail("Playback stopped unexpectedly.");}}};
        void complete(){if(!live())return;wanted=false;playing=false;worker.removeCallbacks(tick);try{if(player!=null)player.pause();}catch(RuntimeException ignored){}abandon();send(()->{if(!wanted)events.ended();});}
        void fail(String message){if(!live())return;ended=true;wanted=false;main.removeCallbacks(timeout);worker.post(this::release);main.post(()->{if(!closed)events.failed(message);});}
        void abandon(){focusGeneration++;if(focus!=null&&audio!=null){AudioFocusRequest prior=focus;focus=null;try{audio.abandonAudioFocusRequest(prior);}catch(RuntimeException ignored){}}}
        void release(){worker.removeCallbacks(tick);prepared=false;playing=false;MediaPlayer prior=player;player=null;if(prior!=null)try{prior.release();}catch(RuntimeException ignored){}abandon();sessions.remove(this);}
        @Override public void close(){ended=true;wanted=false;main.removeCallbacks(timeout);worker.post(this::release);}
    }
    @Override public void close(){if(closed)return;closed=true;for(NativeSession session:sessions)session.close();worker.post(thread::quitSafely);}
    @Override public boolean awaitClosed(long timeout)throws InterruptedException{thread.join(timeout);return !thread.isAlive();}
}
