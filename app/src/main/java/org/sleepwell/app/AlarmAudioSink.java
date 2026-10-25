package org.sleepwell.app;

import android.content.*;
import android.media.*;
import android.net.Uri;
import android.os.*;
import org.json.*;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.concurrent.atomic.AtomicInteger;

/** Decoder/provider calls never block the main loop. Each bounded attempt owns its player thread. */
final class AlarmAudioSink implements AlarmPlayback.Sink {
    private static final Object VOLUME_LOCK=new Object();
    private final Context context;private final Handler main;private final BooleanSupplier alive;
    private final List<Player> players=new ArrayList<>();private final AlarmVolumeLease volume;
    private volatile boolean closed;private volatile long volumeRevision;
    AlarmAudioSink(Context context,Handler main,String occurrence,BooleanSupplier alive){
        this.context=context.getApplicationContext();this.main=main;this.alive=alive;AudioManager audio=context.getSystemService(AudioManager.class);
        volume=new AlarmVolumeLease(new AlarmVolumeLease.Port(){public int read(){return audio.getStreamVolume(AudioManager.STREAM_ALARM);}public int maximum(){return audio.getStreamMaxVolume(AudioManager.STREAM_ALARM);}public void write(int value){audio.setStreamVolume(AudioManager.STREAM_ALARM,value,0);}},new AlarmVolumeLease.Journal(){
            public AlarmVolumeLease.State load(){try{JSONObject j=new JSONObject(Prefs.text(context,"alarmVolumeLease",""));return new AlarmVolumeLease.State(j.getString("occurrence"),j.getString("owner"),j.getInt("original"),j.getInt("last"),j.optBoolean("overridden"));}catch(JSONException e){return null;}}
            @android.annotation.SuppressLint("ApplySharedPref") // Worker-only durability barrier before changing the global stream.
            public boolean save(AlarmVolumeLease.State state){try{JSONObject j=new JSONObject().put("occurrence",state.occurrence).put("owner",state.owner).put("original",state.original).put("last",state.last).put("overridden",state.overridden);return Prefs.of(context).edit().putString("alarmVolumeLease",j.toString()).commit();}catch(JSONException e){return false;}}
            public void clear(String owner){AlarmVolumeLease.State current=load();if(current!=null&&owner.equals(current.owner))Prefs.of(context).edit().remove("alarmVolumeLease").apply();}
        },occurrence,UUID.randomUUID().toString());
    }
    @Override public AlarmPlayback.Layer create(String tone){if(closed)throw new IllegalStateException("Closed alarm audio");Player player=new Player(tone);players.add(player);return player;}
    @Override public void volume(AlarmPlayback.Layer layer,AlarmWakePolicy policy,boolean backup){
        // Queue on the pending player's owner, before its queued start. No stream I/O on main.
        long revision=++volumeRevision;Player player=(Player)layer;player.worker.post(()->{if(!player.available()||revision!=volumeRevision)return;try{synchronized(VOLUME_LOCK){if(!player.available()||revision!=volumeRevision)return;player.digitalScale=volume.acquire(policy,backup);}}catch(RuntimeException error){player.digitalScale=policy.targetPercent/100f;error("Android did not allow the alarm volume change.");}});
    }
    @Override public void stopped(){
        closed=true;
        // Only players that reached start need a quiet barrier. A blocked, cancelled
        // provider attempt can never start later and cannot stall volume restoration.
        AtomicInteger remaining=new AtomicInteger(players.size()+1);Runnable quiet=()->{if(remaining.decrementAndGet()==0)new Thread(()->{try{synchronized(VOLUME_LOCK){volume.release();}}catch(RuntimeException error){error("Alarm volume could not be restored.");}},"SleepWell volume restore").start();};
        for(Player player:players)player.whenQuiet(quiet);quiet.run();
    }
    @Override public void failed(){error("Alarm sound and its fallback could not be played. The challenge remains available.");}
    private void error(String message){Prefs.put(context,"lastError",message);}
    private final class Player implements AlarmPlayback.Layer {
        final String tone;final HandlerThread decoder=new HandlerThread("SleepWell alarm decoder");final Handler worker;
        volatile boolean cancelled,started,finished;volatile float latestGain,digitalScale;MediaPlayer player;boolean prepared;AlarmPlayback.Ready callback;final java.util.concurrent.atomic.AtomicReference<Runnable> quiet=new java.util.concurrent.atomic.AtomicReference<>();
        Player(String tone){this.tone=tone;decoder.start();worker=new Handler(decoder.getLooper());}
        boolean available(){return !cancelled&&!closed&&alive.getAsBoolean();}
        @Override public void prepare(AlarmPlayback.Ready callback){this.callback=callback;worker.post(()->{
            if(!available()){dispose();return;}
            try{
                player=new MediaPlayer();player.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());player.setVolume(0,0);player.setLooping(true);
                player.setOnPreparedListener(value->{if(!available()){dispose();return;}prepared=true;main.post(()->{if(available())callback.ready();else close();});});
                player.setOnErrorListener((value,what,extra)->{if(available())main.post(()->{if(available())callback.failed();});return true;});
                if(tone.startsWith("content:"))player.setDataSource(context,Uri.parse(tone));
                else if(tone.equals("system"))player.setDataSource(context,RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
                else try(android.content.res.AssetFileDescriptor fd=context.getAssets().openFd(AlarmTones.asset(tone))){player.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength());}
                if(!available()){dispose();return;}player.prepareAsync();
            }catch(Exception failure){dispose();main.post(()->{if(available())callback.failed();});}
        });}
        @Override public void gain(float gain){latestGain=Math.max(0,Math.min(1,gain));worker.post(()->{if(available()&&prepared)try{float gainNow=latestGain*digitalScale;player.setVolume(gainNow,gainNow);}catch(RuntimeException failure){main.post(()->{if(available())callback.failed();});}});}
        @Override public void start(AlarmPlayback.Ready activated){worker.post(()->{
            // Volatile ordering: stop sees started=true, or this observes cancelled
            // and cannot start. A quiet barrier follows any raced native start.
            started=true;if(!available()||!prepared){started=false;dispose();return;}
            try{float gainNow=latestGain*digitalScale;player.setVolume(gainNow,gainNow);if(available()){player.start();main.post(()->{if(available())activated.ready();else close();});}else dispose();}
            catch(RuntimeException failure){dispose();main.post(()->{if(available())activated.failed();});}
        });}
        @Override public void close(){cancelled=true;worker.post(this::dispose);}
        void whenQuiet(Runnable callback){cancelled=true;finished=true;quiet.set(callback);if(!started)notifyQuiet();worker.post(this::dispose);}
        void notifyQuiet(){Runnable callback=quiet.getAndSet(null);if(callback!=null)callback.run();}
        void dispose(){boolean silent=player==null;try{if(player!=null){try{player.setVolume(0,0);silent=true;}catch(RuntimeException ignored){}try{player.release();silent=true;}finally{player=null;}}}catch(RuntimeException failure){error("Alarm decoder cleanup failed.");}finally{prepared=false;if(silent){started=false;notifyQuiet();}if(finished)decoder.quitSafely();}}
    }
}
