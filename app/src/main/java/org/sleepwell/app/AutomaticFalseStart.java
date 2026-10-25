package org.sleepwell.app;

import android.app.KeyguardManager;
import android.content.*;
import android.hardware.SensorManager;
import android.os.*;
import androidx.core.content.ContextCompat;
import java.util.function.BooleanSupplier;

/** Optional phone-use heuristic, not a sleep or wake classifier. Borrows existing motion samples only. */
final class AutomaticFalseStart implements AutoCloseable {
    static final String SETTING="automaticRejectFalseStarts";
    static final long WINDOW=45*60000L;
    private static AutomaticFalseStart current;
    private final Context app;private final Handler main=new Handler(Looper.getMainLooper());
    private final BooleanSupplier owned;private final Runnable reject;private final long began;
    private boolean closed,registered;private long used=-1,last=-1,upright=-1,moving=-1;
    static boolean requested(SharedPreferences preferences){try{return preferences.getBoolean(SETTING,false);}catch(RuntimeException invalid){return false;}}
    AutomaticFalseStart(Context context,BooleanSupplier owned,Runnable reject){
        if(Looper.myLooper()!=Looper.getMainLooper())throw new IllegalStateException("Automatic start review requires the tracking owner.");
        app=context.getApplicationContext();this.owned=owned;this.reject=reject;began=SystemClock.elapsedRealtime();
        try{IntentFilter filter=new IntentFilter(Intent.ACTION_USER_PRESENT);filter.addAction(Intent.ACTION_SCREEN_OFF);ContextCompat.registerReceiver(app,receiver,filter,ContextCompat.RECEIVER_NOT_EXPORTED);registered=true;current=this;main.postDelayed(tick,1000);}
        catch(RuntimeException failure){close();Prefs.put(app,"trackingError","Recording continues. Awake-start rejection is unavailable; the record will be kept.");}
    }
    private boolean usingPhone(){try{PowerManager power=app.getSystemService(PowerManager.class);KeyguardManager lock=app.getSystemService(KeyguardManager.class);return power!=null&&power.isInteractive()&&lock!=null&&!lock.isKeyguardLocked();}catch(RuntimeException unavailable){return false;}}
    private boolean eligible(long now){return !closed&&owned.getAsBoolean()&&now>=began&&now-began<WINDOW&&!AlarmService.active&&!Prefs.flag(app,"ringing");}
    private void reset(){used=last=upright=moving=-1;}
    private void interaction(){long now=SystemClock.elapsedRealtime();if(!eligible(now)||!usingPhone())return;if(used<0)used=now;}
    static void explicitUse(){AutomaticFalseStart run=current;if(run!=null)run.interaction();}
    private final BroadcastReceiver receiver=new BroadcastReceiver(){public void onReceive(Context c,Intent intent){String action=intent==null?null:intent.getAction();if(Intent.ACTION_USER_PRESENT.equals(action))interaction();else if(Intent.ACTION_SCREEN_OFF.equals(action))reset();}};
    void sample(long nanos,int accuracy,float x,float y,float z,double movement){
        long now=SystemClock.elapsedRealtime(),age=SystemClock.elapsedRealtimeNanos()-nanos;
        if(!eligible(now)){close();return;}
        if(!usingPhone()){reset();return;}
        if(used<0)return;
        if(nanos<=0||age<0||age>1000000000L||accuracy<=SensorManager.SENSOR_STATUS_UNRELIABLE||!Float.isFinite(x)||!Float.isFinite(y)||!Float.isFinite(z)||!Double.isFinite(movement)||movement<0){last=upright=moving=-1;return;}
        long at=nanos/1000000L;if(at<used)return;
        if(last>=0&&at<=last)return;
        if(last>=0&&at-last>2000)upright=moving=-1;
        last=at;double gravity=Math.sqrt((double)x*x+(double)y*y+(double)z*z);
        boolean held=gravity>=7&&gravity<=12.5&&Math.abs(z)/gravity<.55;
        upright=held?(upright<0?at:upright):-1;
        moving=movement>=.12?(moving<0?at:moving):-1;
    }
    private final Runnable tick=new Runnable(){public void run(){
        long now=SystemClock.elapsedRealtime();if(!eligible(now)){close();return;}
        if(!usingPhone())reset();
        if(used>=0&&now-used>=120000&&last>=0&&now-last<=1000&&((upright>=0&&last-upright>=60000)||(moving>=0&&last-moving>=20000))){close();reject.run();return;}
        main.postDelayed(this,1000);
    }};
    @Override public void close(){if(closed)return;closed=true;if(current==this)current=null;main.removeCallbacks(tick);if(registered){registered=false;try{app.unregisterReceiver(receiver);}catch(RuntimeException failure){android.util.Log.w("SleepWell","Awake-start listener cleanup",failure);}}reset();}
}
