package org.sleepwell.app;

import android.content.Context;
import android.media.*;
import android.os.*;
import java.util.function.BooleanSupplier;

/** Short media-stream cues; deliberately never touches global stream volume or alarm output. */
final class SnoreCueOutput implements SnoreCuePolicy.Device {
    interface Headphones {HeadphoneCue.Sink create(Context context,Handler handler);}
    private final Context context;private final HandlerThread thread=new HandlerThread("SleepWell-snore-cue");private final Handler handler;
    private volatile boolean closed;private ToneGenerator tone;private Vibrator vibrator;private HeadphoneCue headphoneCue;private final BooleanSupplier allowed;private final Headphones headphones;
    SnoreCueOutput(Context c){this(c,()->Prefs.flag(c.getApplicationContext(),"snoreCues"));}
    SnoreCueOutput(Context c,BooleanSupplier allowed){this(c,allowed,HeadphoneSound::new);}
    SnoreCueOutput(Context c,BooleanSupplier allowed,Headphones headphones){context=c.getApplicationContext();this.allowed=allowed;this.headphones=headphones;thread.start();handler=new Handler(thread.getLooper());}
    static SnoreCuePolicy.Settings settings(Context c){int mode=Prefs.number(c,"snoreCueMode",1);return new SnoreCuePolicy.Settings(Prefs.flag(c,"snoreCues"),mode==0?SnoreCuePolicy.Mode.SOUND:mode==2?SnoreCuePolicy.Mode.BOTH:SnoreCuePolicy.Mode.VIBRATION,Prefs.number(c,"snoreCueIntensity",1),Prefs.number(c,"snoreCueRepeats",1),Prefs.number(c,"snoreCueSensitivity",1),Prefs.flag(c,"snoreHeadphoneSound"));}
    @Override public void play(SnoreCuePolicy.Settings settings){if(closed)return;handler.post(()->{if(closed||!allowed.getAsBoolean()||Prefs.flag(context,"ringing"))return;release();try{
        if(settings.mode!=SnoreCuePolicy.Mode.VIBRATION){if(settings.headphoneSound){headphoneCue=new HeadphoneCue(headphones.create(context,handler),new HeadphoneCue.Scheduler(){public void later(Runnable task,long delay){handler.postDelayed(task,delay);}public long now(){return SystemClock.elapsedRealtime();}},()->!closed&&allowed.getAsBoolean()&&!Prefs.flag(context,"ringing"),message->Prefs.put(context,"snoreCueRouteError",message));headphoneCue.start(settings);}else tone=new ToneGenerator(AudioManager.STREAM_MUSIC,new int[]{10,20,35}[settings.intensity-1]);}
        if(settings.mode!=SnoreCuePolicy.Mode.SOUND){vibrator=context.getSystemService(Vibrator.class);if(vibrator==null||!vibrator.hasVibrator())throw new IllegalStateException("Vibration is unavailable");}
        for(int n=0;n<settings.repeats;n++)handler.postDelayed(()->{if(closed||!allowed.getAsBoolean()||Prefs.flag(context,"ringing"))return;try{if(tone!=null)tone.startTone(ToneGenerator.TONE_PROP_BEEP,250);if(vibrator!=null)vibrator.vibrate(VibrationEffect.createOneShot(new int[]{60,120,200}[settings.intensity-1],new int[]{40,80,140}[settings.intensity-1]));}catch(RuntimeException error){Prefs.put(context,"snoreCueError","A snoring cue could not play.");}},n*900L);
        handler.postDelayed(this::releaseLegacy,settings.repeats*900L);
    }catch(RuntimeException error){release();Prefs.put(context,"snoreCueError","Snoring cues are unavailable on this device.");}});}
    private void release(){if(headphoneCue!=null){try{headphoneCue.close();}catch(RuntimeException ignored){}headphoneCue=null;}releaseLegacy();}
    private void releaseLegacy(){if(tone!=null){try{tone.release();}catch(RuntimeException ignored){}tone=null;}if(vibrator!=null){try{if(!Prefs.flag(context,"ringing"))vibrator.cancel();}catch(RuntimeException ignored){}vibrator=null;}}
    @Override public void close(){if(closed)return;closed=true;handler.removeCallbacksAndMessages(null);handler.post(()->{release();thread.quitSafely();});}
    boolean awaitClosed(long timeout)throws InterruptedException{thread.join(timeout);return !thread.isAlive();}
    boolean awaitIdle(long timeout)throws InterruptedException{java.util.concurrent.CountDownLatch idle=new java.util.concurrent.CountDownLatch(1);return handler.post(idle::countDown)&&idle.await(timeout,java.util.concurrent.TimeUnit.MILLISECONDS);}
}
