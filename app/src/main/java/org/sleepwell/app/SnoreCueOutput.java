package org.sleepwell.app;

import android.content.Context;
import android.media.*;
import android.os.*;
import java.util.function.BooleanSupplier;

/** Short media-stream cues; deliberately never touches global stream volume or alarm output. */
final class SnoreCueOutput implements SnoreCuePolicy.Device {
    private final Context context;private final HandlerThread thread=new HandlerThread("SleepWell-snore-cue");private final Handler handler;
    private volatile boolean closed;private ToneGenerator tone;private Vibrator vibrator;private final BooleanSupplier allowed;
    SnoreCueOutput(Context c){this(c,()->Prefs.flag(c.getApplicationContext(),"snoreCues"));}
    SnoreCueOutput(Context c,BooleanSupplier allowed){context=c.getApplicationContext();this.allowed=allowed;thread.start();handler=new Handler(thread.getLooper());}
    static SnoreCuePolicy.Settings settings(Context c){int mode=Prefs.number(c,"snoreCueMode",1);return new SnoreCuePolicy.Settings(Prefs.flag(c,"snoreCues"),mode==0?SnoreCuePolicy.Mode.SOUND:mode==2?SnoreCuePolicy.Mode.BOTH:SnoreCuePolicy.Mode.VIBRATION,Prefs.number(c,"snoreCueIntensity",1),Prefs.number(c,"snoreCueRepeats",1),Prefs.number(c,"snoreCueSensitivity",1));}
    @Override public void play(SnoreCuePolicy.Settings settings){if(closed)return;handler.post(()->{if(closed||!allowed.getAsBoolean()||Prefs.flag(context,"ringing"))return;release();try{
        if(settings.mode!=SnoreCuePolicy.Mode.VIBRATION)tone=new ToneGenerator(AudioManager.STREAM_MUSIC,new int[]{10,20,35}[settings.intensity-1]);
        if(settings.mode!=SnoreCuePolicy.Mode.SOUND){vibrator=context.getSystemService(Vibrator.class);if(vibrator==null||!vibrator.hasVibrator())throw new IllegalStateException("Vibration is unavailable");}
        for(int n=0;n<settings.repeats;n++)handler.postDelayed(()->{if(closed||!allowed.getAsBoolean()||Prefs.flag(context,"ringing"))return;try{if(tone!=null)tone.startTone(ToneGenerator.TONE_PROP_BEEP,250);if(vibrator!=null)vibrator.vibrate(VibrationEffect.createOneShot(new int[]{60,120,200}[settings.intensity-1],new int[]{40,80,140}[settings.intensity-1]));}catch(RuntimeException error){Prefs.put(context,"snoreCueError","A snoring cue could not play.");}},n*900L);
        handler.postDelayed(this::release,settings.repeats*900L);
    }catch(RuntimeException error){release();Prefs.put(context,"snoreCueError","Snoring cues are unavailable on this device.");}});}
    private void release(){if(tone!=null){try{tone.release();}catch(RuntimeException ignored){}tone=null;}if(vibrator!=null){try{if(!Prefs.flag(context,"ringing"))vibrator.cancel();}catch(RuntimeException ignored){}vibrator=null;}}
    @Override public void close(){closed=true;handler.removeCallbacksAndMessages(null);handler.post(()->{release();thread.quitSafely();});}
}
