package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.media.*;
import android.media.session.*;
import android.net.Uri;
import android.os.*;
import java.util.Map;

/** User-started, local-only media playback. No stream-volume or alarm scheduling changes. */
public final class SleepSoundService extends Service {
    static final String PLAY="sound.PLAY",PAUSE="sound.PAUSE",STOP="sound.STOP";
    private static volatile SleepSoundService current;
    private static volatile Snapshot last;
    private final Handler main=new Handler(Looper.getMainLooper());
    private final HandlerThread decoder=new HandlerThread("SleepWell sound decoder");
    private Handler worker;private SoundPlayback playback;private AudioManager audio;private AudioFocusRequest focus;private AudioAttributes attributes;private int focusGeneration;
    private PowerManager.WakeLock wake;private MediaSession media;private boolean hasFocus,foreground,destroyed;
    private final AutomationPlayback soundEvents=new AutomationPlayback((kind,occurrence,at)->Automations.confirmedAt(this,kind,occurrence,at));private AutomationPlayback.Segment soundSegment;
    private final SoundOwnership ownership=new SoundOwnership();private long fadeOwner;private boolean stillnessArmed,stillnessClaimed;private StillnessSoundFade.Admission stillnessAdmission;
    static final class Snapshot {
        final SoundPlayback.State state;final long elapsed;final String error,trackTitle,finished,stillness;final int track,tracks;final boolean rotation;
        Snapshot(SleepSoundService service){state=service.playback.state;elapsed=service.playback.elapsed();error=service.playback.error;trackTitle=service.playback.trackTitle();rotation=service.playback.rotation();track=service.playback.trackNumber();tracks=service.playback.trackCount();finished=service.playback.finishedMessage();stillness=service.stillnessLabel();}
    }
    static Snapshot snapshot(){SleepSoundService s=current;return s==null||s.playback.state==SoundPlayback.State.STOPPED?last:new Snapshot(s);}
    static String error(Context c){return SoundPreferences.error(c);}
    static void command(Context c,String action){
        Intent intent=new Intent(c,SleepSoundService.class).setAction(action);
        try{if(PLAY.equals(action))c.startForegroundService(intent);else if(current!=null)c.startService(intent);}
        catch(RuntimeException e){SoundPreferences.error(c,"Playback could not start. Open Sleep sounds and try again.");}
    }
    static void pauseForSelection(){SleepSoundService s=current;if(s!=null)s.manualPause();}
    static void update(Context c){SleepSoundService s=current;if(s==null){last=null;return;}try{SoundMix updated=SoundPreferences.load(c);if(!updated.withTracking)s.clearStillness();s.playback.update(updated);s.publish();}catch(RuntimeException e){s.failure("Your sound settings could not be loaded.");}}
    static void stillnessSettingChanged(Context context){SleepSoundService service=current;if(service!=null){if(!SoundPreferences.stillnessFade(context)){service.stillnessArmed=false;StillnessSoundFade.release(service.stillnessAdmission);service.stillnessAdmission=null;}service.publish();}}
    private void clearStillness(){StillnessSoundFade.release(stillnessAdmission);stillnessAdmission=null;fadeOwner=0;stillnessArmed=false;stillnessClaimed=false;}
    private void refreshStillness(){if(!stillnessArmed||fadeOwner<=0)return;if(!SoundPreferences.stillnessFade(this)){stillnessArmed=false;StillnessSoundFade.release(stillnessAdmission);stillnessAdmission=null;return;}if(!stillnessClaimed&&playback.state==SoundPlayback.State.PLAYING){stillnessClaimed=true;stillnessAdmission=StillnessSoundFade.claim(fadeOwner,SystemClock.elapsedRealtime());}if(stillnessAdmission==null)return;TrackingService.Snapshot recording=TrackingService.snapshot();if(recording==null||recording.id!=fadeOwner||recording.sonar||!recording.phase.equals("ACTIVE"))return;playback.attenuate((float)StillnessSoundFade.gain(stillnessAdmission,SystemClock.elapsedRealtime()));}
    private String stillnessLabel(){if(stillnessArmed&&fadeOwner>0&&stillnessAdmission!=null)return StillnessSoundFade.status(stillnessAdmission,SystemClock.elapsedRealtime());return playback!=null&&playback.attenuation()<1?"Reduced sound level is held until a new playback session":"";}
    static void trackingStart(Context c,long id,long elapsed){
        try{SoundMix mix=SoundPreferences.load(c);if(!mix.withTracking)return;
            SleepSoundService s=current;if(s!=null){s.ownership.claim(id,true);return;}
            c.startForegroundService(new Intent(c,SleepSoundService.class).setAction(PLAY).putExtra("trackingOwner",id).putExtra("offset",elapsed));
        }catch(RuntimeException e){SoundPreferences.error(c,"Sleep sound could not start. Tracking continues without playback.");}
    }
    static void trackingPause(long id){SleepSoundService s=current;if(s!=null&&s.ownership.pause(id)){s.playback.pause();s.publish();}}
    static void trackingResume(long id){SleepSoundService s=current;if(s!=null&&s.ownership.resume(id))s.resume();}
    static void trackingStop(long id){SleepSoundService s=current;if(s!=null&&s.ownership.stop(id))s.end();}
    static void alarmStarting(){SleepSoundService s=current;if(s!=null)s.end();}
    @Override public IBinder onBind(Intent intent){return null;}
    @Override public void onCreate(){
        super.onCreate();last=null;Notifications.channels(this);decoder.start();worker=new Handler(decoder.getLooper());audio=getSystemService(AudioManager.class);
        attributes=new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
        wake=getSystemService(PowerManager.class).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"SleepWell:sounds");
        playback=new SoundPlayback(SystemClock::elapsedRealtime,source->new PlayerLayer(source,attributes));
        media=new MediaSession(this,"SleepWell sleep sounds");media.setCallback(new MediaSession.Callback(){
            @Override public void onPlay(){ownership.manual();clearStillness();resume();}
            @Override public void onPause(){manualPause();}
            @Override public void onStop(){end();}
        },main);media.setActive(true);
        androidx.core.content.ContextCompat.registerReceiver(this,noisy,new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY),androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED);current=this;
    }
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        if(intent==null){end();return START_NOT_STICKY;}
        String action=intent.getAction();if(STOP.equals(action)){end();return START_NOT_STICKY;}
        if(PAUSE.equals(action)){manualPause();return START_NOT_STICKY;}
        if(!PLAY.equals(action)){end();return START_NOT_STICKY;}
        if(Prefs.flag(this,"ringing")){failure("Finish your alarm before playing sleep sounds.");return START_NOT_STICKY;}
        long owner=intent.getLongExtra("trackingOwner",0);
        boolean occupied=playback.state!=SoundPlayback.State.STOPPED&&playback.state!=SoundPlayback.State.FINISHED&&playback.state!=SoundPlayback.State.ERROR;
        if(owner>0){TrackingService.Snapshot session=TrackingService.snapshot();if(session==null||session.id!=owner||!session.phase.equals("ACTIVE")){if(!occupied)end();return START_NOT_STICKY;}}
        if(owner>0&&!ownership.claim(owner,occupied))return START_NOT_STICKY;
        if(owner==0){ownership.manual();clearStillness();}
        if(occupied){if(playback.state==SoundPlayback.State.PAUSED)resume();return START_NOT_STICKY;}
        try{
            ensureForeground();if(!requestFocus()){failure("Another app is using audio. Tap Play when it is available.");return START_NOT_STICKY;}
            SoundMix selected=SoundPreferences.load(this);if(owner>0&&!selected.withTracking){end();return START_NOT_STICKY;}clearStillness();fadeOwner=owner>0&&selected.withTracking?owner:0;stillnessArmed=fadeOwner>0&&SoundPreferences.stillnessFade(this);SoundPreferences.error(this,"");soundEvents.closing(soundSegment);soundSegment=soundEvents.begin();playback.play(selected,intent.getLongExtra("offset",0));publish();
        }catch(RuntimeException e){failure("Sleep sound is unavailable. Check the selected file and try again.");}
        return START_NOT_STICKY;
    }
    private boolean requestFocus(){
        abandonFocus();int generation=++focusGeneration;
        focus=new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).setAudioAttributes(attributes).setAcceptsDelayedFocusGain(false).setOnAudioFocusChangeListener(change->{
            if(generation!=focusGeneration||!hasFocus)return;
            if(change==AudioManager.AUDIOFOCUS_GAIN)playback.duck(false);
            else if(change==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)playback.duck(true);
            else {ownership.manual();clearStillness();playback.pause();publish();}
        },main).build();
        hasFocus=audio.requestAudioFocus(focus)==AudioManager.AUDIOFOCUS_REQUEST_GRANTED;if(hasFocus)playback.duck(false);return hasFocus;
    }
    private void abandonFocus(){focusGeneration++;if(focus!=null){audio.abandonAudioFocusRequest(focus);focus=null;}hasFocus=false;}
    private void ensureForeground(){if(!foreground){startForeground(104,notification(),ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);foreground=true;}}
    private void resume(){
        if(playback.state!=SoundPlayback.State.PAUSED)return;
        if(Prefs.flag(this,"ringing")){failure("Finish your alarm before playing sleep sounds.");return;}
        try{ensureForeground();if(requestFocus()){SoundPreferences.error(this,"");soundEvents.closing(soundSegment);soundSegment=soundEvents.begin();playback.resume();publish();}else failure("Another app is using audio. Tap Play when it is available.");}
        catch(RuntimeException e){failure("Playback could not resume. Tap Play to try again.");}
    }
    private void manualPause(){ownership.manual();clearStillness();playback.pause();publish();}
    private final BroadcastReceiver noisy=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){manualPause();}};
    private void failure(String text){playback.fail();SoundPreferences.error(this,text);publish();}
    private void publish(){
        if(destroyed)return;refreshStillness();main.removeCallbacks(tick);
        SoundPlayback.State state=playback.state;boolean active=state==SoundPlayback.State.PLAYING||state==SoundPlayback.State.LOADING;
        if(state==SoundPlayback.State.ERROR){if(SoundPreferences.error(this).isEmpty())SoundPreferences.error(this,playback.error);}
        if(!active){soundEvents.closing(soundSegment);if(wake.isHeld())wake.release();abandonFocus();}
        else if(!wake.isHeld())wake.acquire(245*60000L);
        media.setPlaybackState(new PlaybackState.Builder().setActions(PlaybackState.ACTION_PLAY|PlaybackState.ACTION_PAUSE|PlaybackState.ACTION_STOP).setState(state==SoundPlayback.State.PLAYING?PlaybackState.STATE_PLAYING:state==SoundPlayback.State.LOADING?PlaybackState.STATE_BUFFERING:PlaybackState.STATE_PAUSED,playback.elapsed(),state==SoundPlayback.State.PLAYING?1:0).build());
        if(state==SoundPlayback.State.FINISHED||state==SoundPlayback.State.ERROR||state==SoundPlayback.State.STOPPED){end();return;}
        if(foreground)getSystemService(NotificationManager.class).notify(104,notification());
        main.postDelayed(tick,500);
    }
    private final Runnable tick=()->{playback.tick();publish();};
    private Notification notification(){
        boolean active=playback!=null&&(playback.state==SoundPlayback.State.PLAYING||playback.state==SoundPlayback.State.LOADING);
        PendingIntent open=PendingIntent.getActivity(this,104,new Intent(this,SleepSoundsActivity.class),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        Intent toggleIntent=new Intent(this,SleepSoundService.class).setAction(active?PAUSE:PLAY);
        PendingIntent toggle=active?PendingIntent.getService(this,105,toggleIntent,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT):PendingIntent.getForegroundService(this,107,toggleIntent,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent stop=PendingIntent.getService(this,106,new Intent(this,SleepSoundService.class).setAction(STOP),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        SoundMix mix=playback==null?null:playback.mix();String label=mix==null?"Sleep sounds":mix.label();
        Notification.Builder b=new Notification.Builder(this,Notifications.SOUND).setSmallIcon(R.drawable.ic_moon).setContentTitle(label).setContentText(active?(playback.trackCount()>0?(playback.rotation()?"Rotating ":"Track ")+playback.trackNumber()+" of "+playback.trackCount()+" · "+playback.trackTitle():"Sleep sounds playing"):"Paused · tap Play to continue").setContentIntent(open).setDeleteIntent(stop).setOnlyAlertOnce(true).setSound(null).setVibrate(null).setOngoing(active).setVisibility(Notification.VISIBILITY_PUBLIC)
            .addAction(new Notification.Action.Builder(active?android.R.drawable.ic_media_pause:android.R.drawable.ic_media_play,active?"Pause":"Play",toggle).build()).addAction(new Notification.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel,"Stop",stop).build());
        if(media!=null)b.setStyle(new Notification.MediaStyle().setMediaSession(media.getSessionToken()).setShowActionsInCompactView(0,1));return b.build();
    }
    private void end(){
        last=playback.state==SoundPlayback.State.FINISHED||playback.state==SoundPlayback.State.ERROR?new Snapshot(this):null;clearStillness();
        main.removeCallbacks(tick);soundEvents.closing(soundSegment);playback.stop();if(wake.isHeld())wake.release();abandonFocus();
        stopForeground(STOP_FOREGROUND_REMOVE);foreground=false;stopSelf();
    }
    @Override public void onDestroy(){destroyed=true;clearStillness();main.removeCallbacksAndMessages(null);if(current==this)current=null;soundEvents.closing(soundSegment);playback.stop();if(wake.isHeld())wake.release();abandonFocus();unregisterReceiver(noisy);media.release();decoder.quitSafely();super.onDestroy();}

    /** All provider IO / decoder calls are on one worker. close() invalidates queued starts immediately. */
    private final class PlayerLayer implements SoundPlayback.Layer {
        final String source;final AudioAttributes attributes;private AutomationPlayback.Segment playingSegment;volatile boolean closed;MediaPlayer player;boolean ready;volatile boolean finite,ended;volatile Runnable completed;volatile SoundPlaylistAudio snapshot;
        PlayerLayer(String s,AudioAttributes a){source=s;attributes=a;}
        @Override public void finite(Runnable completion){finite=true;completed=completion;}
        @Override public void prepare(SoundPlayback.Ready callback){worker.post(()->{
            if(closed)return;
            if(finite&&source.startsWith("content://")){
                try{snapshot=SoundPlaylistAudio.acquire(SleepSoundService.this);if(closed){release();return;}snapshot.readAsync(source,file->worker.post(()->{if(closed){release();return;}preparePlayer(callback,file);}),error->worker.post(()->{release();main.post(()->{if(!destroyed&&!closed)callback.failed();});}));}
                catch(Exception error){release();main.post(()->{if(!destroyed&&!closed)callback.failed();});}
            }else preparePlayer(callback,null);
        });}
        private void preparePlayer(SoundPlayback.Ready callback,java.io.File document){
            if(closed)return;
            try{
                player=new MediaPlayer();player.setAudioAttributes(attributes);player.setLooping(!finite);player.setVolume(0,0);player.setOnCompletionListener(p->{Runnable completion=completed;if(finite&&!closed&&completion!=null){ended=true;main.post(()->{if(!destroyed&&!closed)completion.run();});}});
                player.setOnPreparedListener(p->{if(closed){release();return;}ready=true;main.post(()->{if(!destroyed&&!closed)callback.ready();else close();});});
                player.setOnErrorListener((p,what,extra)->{main.post(()->{if(!destroyed&&!closed)callback.failed();});return true;});
                if(document!=null){try(java.io.FileInputStream input=new java.io.FileInputStream(document)){player.setDataSource(input.getFD());}}
                else if(source.startsWith(BinauralTones.PREFIX)){java.io.File file=BinauralTones.prepare(SleepSoundService.this,source,()->!closed&&!destroyed);try(java.io.FileInputStream input=new java.io.FileInputStream(file)){player.setDataSource(input.getFD());}}
                else if(source.startsWith("content://"))player.setDataSource(SleepSoundService.this,Uri.parse(source));
                else {String file=Map.of("Rain","rain.wav","Ocean","ocean.wav","Fireplace","fireplace.wav","Wind","wind.wav","Brown noise","brown.wav").get(source);if(file==null)throw new IllegalArgumentException("Unknown sound");
                    try(android.content.res.AssetFileDescriptor fd=getAssets().openFd(file)){player.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength());}}
                if(!closed)player.prepareAsync();else release();
            }catch(Exception e){release();main.post(()->{if(!destroyed&&!closed)callback.failed();});}
        }
        @Override public void gain(float value){worker.post(()->{if(!closed&&ready&&player!=null)try{player.setVolume(value,value);}catch(IllegalStateException e){main.post(()->{if(!destroyed&&!closed)failure("Playback stopped. Please select the sound again.");});}});}
        @Override public void start(){AutomationPlayback.Segment requested=soundSegment;soundEvents.starting(requested,this);if(!worker.post(()->{try{if(!closed&&ready&&player!=null&&(!finite||!ended)){player.start();if(player.isPlaying()){playingSegment=requested;soundEvents.started(requested,this);}}}catch(IllegalStateException e){main.post(()->{if(!destroyed&&!closed)failure("Playback stopped. Please select the sound again.");});}finally{soundEvents.settled(requested,this);}}))soundEvents.settled(requested,this);}
        @Override public void pause(){worker.post(()->{if(!closed&&ready&&player!=null)try{player.pause();if(!player.isPlaying()){soundEvents.quiet(playingSegment,this);playingSegment=null;}}catch(IllegalStateException error){main.post(()->{if(!destroyed&&!closed)failure("Playlist could not pause. Select it again before playing.");});}});}
        @Override public void close(){closed=true;SoundPlaylistAudio reading=snapshot;if(reading!=null)reading.cancel();worker.post(this::release);}
        private void release(){ready=false;try{if(player!=null){try{player.stop();}catch(IllegalStateException ignored){}player.release();player=null;soundEvents.quiet(playingSegment,this);playingSegment=null;}SoundPlaylistAudio reading=snapshot;if(reading!=null){reading.close();snapshot=null;}}catch(Exception cleanup){SoundPreferences.error(SleepSoundService.this,"Playlist cleanup needs attention. Restart SleepWell before trying another local file.");}}

    }
}
