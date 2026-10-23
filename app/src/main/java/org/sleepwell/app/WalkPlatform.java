package org.sleepwell.app;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.hardware.*;
import android.os.*;

/** Optional real Android step detector/counter only. No activity service, microphone or accelerometer. */
final class WalkPlatform implements WalkSession.Port {
    interface Factory {WalkSession.Port create(Context context);}
    static Factory factory=WalkPlatform::new;
    private final Context context;private final SensorManager manager;private final Sensor sensor;private SensorEventListener listener;
    WalkPlatform(Context context){this.context=context.getApplicationContext();manager=context.getSystemService(SensorManager.class);Sensor detector=manager==null?null:manager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);sensor=detector!=null?detector:manager==null?null:manager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);}
    public WalkSteps.Mode mode(){return sensor==null?WalkSteps.Mode.NONE:sensor.getType()==Sensor.TYPE_STEP_DETECTOR?WalkSteps.Mode.DETECTOR:WalkSteps.Mode.COUNTER;}
    public boolean allowed(){return context.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)==PackageManager.PERMISSION_GRANTED;}
    public long now(){return SystemClock.elapsedRealtimeNanos();}
    public boolean begin(WalkSession.Sink sink){close();if(sensor==null||!allowed())return false;listener=new SensorEventListener(){public void onSensorChanged(SensorEvent event){if(event.values.length>0)sink.sample(event.timestamp,event.values[0]);}public void onAccuracyChanged(Sensor sensor,int accuracy){}};boolean registered=manager.registerListener(listener,sensor,SensorManager.SENSOR_DELAY_NORMAL,new Handler(Looper.getMainLooper()));if(!registered)close();return registered;}
    public void close(){SensorEventListener closing=listener;listener=null;if(closing!=null)manager.unregisterListener(closing);}
}
