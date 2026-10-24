package org.sleepwell.app;
import android.content.Context;import android.hardware.*;import android.os.*;import java.util.function.BooleanSupplier;

/** Separate listener so input detection cannot unregister a Shake/Walk challenge. No sensor on construction. */
final class AlarmFlipSensor implements SensorEventListener,AutoCloseable {
    private final SensorManager manager;private final BooleanSupplier current;private final Runnable snooze;private FlipSnooze posture;private boolean registered,closed;private long armedAt;
    AlarmFlipSensor(Context context,BooleanSupplier current,Runnable snooze){manager=manager(context);this.current=current;this.snooze=snooze;}
    private static SensorManager manager(Context context){try{return context.getSystemService(SensorManager.class);}catch(RuntimeException unavailable){return null;}}
    boolean start(){if(closed||!current.getAsBoolean())return false;if(registered){armedAt=SystemClock.elapsedRealtimeNanos();posture=new FlipSnooze(armedAt);return true;}if(manager==null)return false;try{Sensor sensor=manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);if(sensor==null)return false;armedAt=SystemClock.elapsedRealtimeNanos();posture=new FlipSnooze(armedAt);registered=manager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_UI,new Handler(Looper.getMainLooper()));return registered;}catch(RuntimeException unavailable){registered=true;pause();return false;}}
    @Override public void onSensorChanged(SensorEvent event){if(!registered||closed||!current.getAsBoolean()||posture==null||event.timestamp<armedAt||event.values.length<3)return;if(posture.sample(event.timestamp,SystemClock.elapsedRealtimeNanos(),event.values[0],event.values[1],event.values[2])){pause();if(!registered&&current.getAsBoolean())snooze.run();}}
    @Override public void onAccuracyChanged(Sensor sensor,int accuracy){}
    boolean closed(){return closed;}
    boolean released(){return !registered;}
    boolean pause(){posture=null;if(!registered)return true;try{manager.unregisterListener(this);registered=false;posture=null;return true;}catch(RuntimeException failure){return false;}}
    @Override public void close(){closed=true;pause();}
}
