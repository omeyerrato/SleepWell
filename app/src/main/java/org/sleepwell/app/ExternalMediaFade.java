package org.sleepwell.app;

import android.content.*;
import android.media.*;
import android.os.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

/** Explicit, observable-only ownership of reductions to the device MEDIA stream. Never restores volume. */
final class ExternalMediaFade {
 static final String FILE="external_media_fade_v1";
 private static final Object SETTINGS=new Object();
 private static final ExecutorService writer=Executors.newSingleThreadExecutor(r->new Thread(r,"External media fade settings"));
 private static final Handler main=new Handler(Looper.getMainLooper());
 private static volatile Run current;
 private static volatile String message="Off. No media volume changes.";
 private static volatile long messageAt;
 static final class Selection {
  static final Selection OFF=new Selection(false,0,"");
  final boolean enabled;final long revision;final String issue;
  Selection(boolean enabled,long revision,String issue){this.enabled=enabled;this.revision=revision;this.issue=issue;}
 }
 static SharedPreferences prefs(Context c){return c.getApplicationContext().getSharedPreferences(FILE,Context.MODE_PRIVATE);}
 static Selection selection(Context c){synchronized(SETTINGS){try{SharedPreferences p=prefs(c);long r=p.getLong("revision",0);if(r<0)throw new IllegalStateException();return new Selection(p.getBoolean("enabled",false),r,"");}catch(RuntimeException bad){return new Selection(false,-1,"Settings are unreadable; external media fade is disabled.");}}}
 static void save(Context c,long expected,boolean enabled,boolean accepted,Consumer<String> completed){Context app=c.getApplicationContext();writer.execute(()->{
  String error="";long saved=-1;
  try{synchronized(SETTINGS){Selection before=selection(app);if(before.revision!=expected||before.revision<0)throw new IllegalStateException("Settings changed. Reopen this screen before saving.");if(before.revision==Long.MAX_VALUE)throw new IllegalStateException("Settings revision is unavailable.");if(enabled&&!accepted)throw new IllegalStateException("Confirm device media-volume control before enabling.");if(enabled&&Build.VERSION.SDK_INT<33)throw new IllegalStateException("External media fade requires Android 13 or newer.");saved=before.revision+1;if(!prefs(app).edit().putBoolean("enabled",enabled).putLong("revision",saved).commit())throw new IllegalStateException("Settings could not be saved.");}}
  catch(RuntimeException failure){error=failure.getMessage()==null?"Settings could not be saved.":failure.getMessage();}
  String result=error;long revision=saved;main.post(()->{if(revision>=0){Run owner=current;if(owner!=null&&owner.selection.revision<revision)owner.close("Settings changed; this recording will not fade media again.");if(current==null)publish(app,enabled?"Enabled for a new manual phone-movement recording. Start your media player first.":"Off. Any previously lowered volume stays at its current level.");}completed.accept(result);});
 });}
 static String status(Context c){Run r=current;if(r!=null)return r.status;Selection selected=selection(c);if(!selected.issue.isEmpty())return selected.issue;if(messageAt>0)return message;try{String previous=prefs(c).getString("lastStatus","");if(prefs(c).getBoolean("wasActive",false))return "The previous fade was interrupted. Volume was not restored; start a new recording to use it again.";if(!previous.isEmpty())return previous;}catch(RuntimeException ignored){}return selected.enabled?"Enabled for the next manual phone-movement recording.":"Off. No media volume changes.";}
 private static void publish(Context c,String text){message=text;messageAt=System.currentTimeMillis();boolean active=current!=null;writer.execute(()->{try{prefs(c).edit().putString("lastStatus",text).putBoolean("wasActive",active).putLong("updated",System.currentTimeMillis()).commit();}catch(RuntimeException ignored){}});}
 static void start(Context c,long session,String identity,Selection selected,BooleanSupplier live){
  if(Looper.myLooper()!=Looper.getMainLooper())throw new IllegalStateException("Media fade admission must use the tracking owner.");
  Run old=current;if(old!=null)old.close("Recording replaced; media fade stopped.");
  if(!selected.enabled)return;
  Run owner=new Run(c,session,identity,selected,live);current=owner;owner.open();
 }
 static void end(long session,String identity,String why){Run r=current;if(r!=null&&r.session==session&&r.identity.equals(identity))r.close(why+" Volume was not restored. A new recording is required.");}
 static String token(){Run r=current;return r==null?"":r.token;}
 static void cancel(String token){Run r=current;if(r!=null&&r.token.equals(token))r.close("Stopped for this recording. Raise media volume manually if needed.");}
 private static boolean ownSounds(Context c){SleepSoundService.Snapshot s=SleepSoundService.snapshot();return s!=null&&(s.state==SoundPlayback.State.LOADING||s.state==SoundPlayback.State.PLAYING||s.state==SoundPlayback.State.PAUSED)||SoundPreferences.load(c).withTracking;}
 private static final class Run {
  final Context context;final long session;final String identity,token=UUID.randomUUID().toString();final Selection selection;final BooleanSupplier live;
  final Handler handler=new Handler(Looper.getMainLooper());
  AudioManager audio;StillnessSoundFade.Admission admission;boolean closed,devicesRegistered,playbackRegistered,noisyRegistered,modeRegistered;
  int original,last,min,max;String routes,devices,status="Checking media fade prerequisites…",lastPublished="";List<AudioPlaybackConfiguration> configs;
  final AudioAttributes media=new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
  final AudioDeviceCallback deviceCallback=new AudioDeviceCallback(){@Override public void onAudioDevicesAdded(AudioDeviceInfo[] d){observed();}@Override public void onAudioDevicesRemoved(AudioDeviceInfo[] d){observed();}};
  final AudioManager.AudioPlaybackCallback playbackCallback=new AudioManager.AudioPlaybackCallback(){@Override public void onPlaybackConfigChanged(List<AudioPlaybackConfiguration> c){observed();}};
  final AudioManager.OnModeChangedListener modeCallback=mode->{if(!closed&&mode!=AudioManager.MODE_NORMAL)close("A call or audio-mode change cancelled media fade. Volume was not restored.");};
  final BroadcastReceiver noisy=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){if(!closed&&AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(i.getAction()))close("Audio output changed; media fade cancelled. Volume was not restored.");}};
  final Runnable tick=new Runnable(){public void run(){if(closed)return;try{check();double gain=StillnessSoundFade.gain(admission,SystemClock.elapsedRealtime());if(gain<1){int target=Math.max(min,Math.min(last,min+(int)Math.floor((original-min)*gain)));if(target<last){check(); // Final observable ownership check; Android offers no atomic compare-and-set stream volume.
    audio.setStreamVolume(AudioManager.STREAM_MUSIC,target,0);
    int readback=audio.getStreamVolume(AudioManager.STREAM_MUSIC);last=target;if(readback!=target)throw new IllegalStateException("Android did not confirm the requested media volume; fade stopped.");
   }if(target==min){close("Media volume lowered after a movement estimate. The player may still be running; raise volume manually afterward.");return;}}
   update(StillnessSoundFade.status(admission,SystemClock.elapsedRealtime())+" · device media volume "+last+" / "+max);handler.postDelayed(this,1000);
  }catch(RuntimeException unavailable){close(reason(unavailable));}}};
  Run(Context c,long session,String identity,Selection selected,BooleanSupplier live){context=c.getApplicationContext();this.session=session;this.identity=identity;selection=selected;this.live=live;}
  void open(){try{
   if(Build.VERSION.SDK_INT<33)throw new IllegalStateException("Android 13 or newer is required. No volume was changed.");
   audio=context.getSystemService(AudioManager.class);if(audio==null)throw new IllegalStateException("Media volume control is unavailable.");
   basic();min=audio.getStreamMinVolume(AudioManager.STREAM_MUSIC);max=audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);original=last=audio.getStreamVolume(AudioManager.STREAM_MUSIC);
   if(min<0||max<=min||original<=min||original>max)throw new IllegalStateException("Media volume is already at its minimum or cannot be controlled.");
   routes=route();devices=inventory();configs=List.copyOf(audio.getActivePlaybackConfigurations());if(configs.isEmpty())throw new IllegalStateException("Active playback cannot be observed. Media fade is inactive.");
   admission=StillnessSoundFade.claimExternal(session,SystemClock.elapsedRealtime());if(admission==null)throw new IllegalStateException("Built-in playback or recording ownership prevents external media fade.");
   audio.registerAudioDeviceCallback(deviceCallback,handler);devicesRegistered=true;audio.registerAudioPlaybackCallback(playbackCallback,handler);playbackRegistered=true;
   audio.addOnModeChangedListener(command->handler.post(command),modeCallback);modeRegistered=true;
   androidx.core.content.ContextCompat.registerReceiver(context,noisy,new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY),androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED);noisyRegistered=true;
   update("Waiting for 10 fresh measured stillness minutes with the screen off. Device media volume is unchanged.");handler.post(tick);
  }catch(RuntimeException unavailable){close(reason(unavailable));}}
  void basic(){
   if(closed||current!=this||!live.getAsBoolean())throw new IllegalStateException("The active recording changed; media fade stopped.");
   Selection now=selection(context);if(!now.enabled||now.revision!=selection.revision)throw new IllegalStateException("Media fade settings changed; start a new recording to use it again.");
   if(audio.isVolumeFixed())throw new IllegalStateException("This device uses fixed media volume. Media fade is unavailable.");
   if(AlarmService.active||Prefs.flag(context,"ringing")||audio.getMode()!=AudioManager.MODE_NORMAL)throw new IllegalStateException("An alarm, call or audio-mode change cancelled media fade.");
   if(ownSounds(context))throw new IllegalStateException("Built-in SleepWell sounds take priority. External media fade stopped.");
   if(!audio.isMusicActive())throw new IllegalStateException("Media is not actively playing. Start your player, then begin a new recording.");
  }
  void check(){basic();if(!StillnessSoundFade.owns(admission))throw new IllegalStateException("Movement-estimate ownership changed; media fade stopped.");if(audio.getStreamMinVolume(AudioManager.STREAM_MUSIC)!=min||audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)!=max)throw new IllegalStateException("The media volume range changed; media fade stopped.");if(audio.getStreamVolume(AudioManager.STREAM_MUSIC)!=last)throw new IllegalStateException("Media volume was changed by you or another owner. No further changes will be made this recording.");if(!routes.equals(route())||!devices.equals(inventory()))throw new IllegalStateException("An observed audio route changed; media fade stopped.");if(!configs.equals(audio.getActivePlaybackConfigurations()))throw new IllegalStateException("Observed playback changed; media fade stopped.");}
  void observed(){if(closed)return;try{check();}catch(RuntimeException changed){close(reason(changed));}}
  String route(){if(Build.VERSION.SDK_INT<33)throw new IllegalStateException("Android 13 or newer is required. No volume was changed.");List<AudioDeviceInfo> anticipated=audio.getAudioDevicesForAttributes(media);if(anticipated==null||anticipated.isEmpty())throw new IllegalStateException("The anticipated media route is unavailable. No fade will run.");List<String> ids=new ArrayList<>();for(AudioDeviceInfo d:anticipated)ids.add(d.getId()+":"+d.getType());Collections.sort(ids);return String.join("|",ids);}
  String inventory(){List<String> ids=new ArrayList<>();for(AudioDeviceInfo d:audio.getDevices(AudioManager.GET_DEVICES_OUTPUTS))ids.add(d.getId()+":"+d.getType());Collections.sort(ids);return String.join("|",ids);}
  void update(String text){status=text;if(!text.equals(lastPublished)){lastPublished=text;publish(context,text);}}
  void close(String why){if(closed)return;closed=true;handler.removeCallbacksAndMessages(null);StillnessSoundFade.release(admission);admission=null;
   if(audio!=null){if(devicesRegistered){devicesRegistered=false;try{audio.unregisterAudioDeviceCallback(deviceCallback);}catch(RuntimeException ignored){}}if(playbackRegistered){playbackRegistered=false;try{audio.unregisterAudioPlaybackCallback(playbackCallback);}catch(RuntimeException ignored){}}if(modeRegistered&&Build.VERSION.SDK_INT>=31){modeRegistered=false;try{audio.removeOnModeChangedListener(modeCallback);}catch(RuntimeException ignored){}}}
   if(noisyRegistered){noisyRegistered=false;try{context.unregisterReceiver(noisy);}catch(RuntimeException ignored){}}
   if(current==this)current=null;update(why);
  }
 }
 private static String reason(RuntimeException e){return (e.getMessage()==null?"Android media controls are unavailable.":e.getMessage())+" No automatic volume restoration.";}
 private ExternalMediaFade(){}
}