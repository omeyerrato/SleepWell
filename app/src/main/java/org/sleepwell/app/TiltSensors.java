package org.sleepwell.app;

import android.content.Context;import android.hardware.*;import android.os.*;

/** Test injection happens before UI creation. Construction never registers a real sensor. */
final class TiltSensors {
    interface Listener {void sample(long time,double x,double y,double z);}
    interface Port {boolean start(Listener listener);boolean stop();long now();int rotation();}
    interface Factory {Port create(Context context);}
    static Factory factory=Platform::new;
    private static final class Platform implements Port,SensorEventListener {
        final Context context;final SensorManager manager;Listener listener;boolean registered;
        Platform(Context context){this.context=context;manager=context.getSystemService(SensorManager.class);}
        public boolean start(Listener listener){if(registered)return false;this.listener=listener;try{Sensor sensor=manager==null?null:manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);if(sensor==null)return false;registered=manager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_GAME,new Handler(Looper.getMainLooper()));return registered;}catch(RuntimeException failure){registered=true;stop();return false;}}
        public boolean stop(){listener=null;if(!registered)return true;try{manager.unregisterListener(this);registered=false;return true;}catch(RuntimeException failure){return false;}}
        public long now(){return SystemClock.elapsedRealtimeNanos();}
        public int rotation(){return context.getDisplay()==null?0:context.getDisplay().getRotation();}
        public void onSensorChanged(SensorEvent event){if(registered&&listener!=null&&event.values.length>=3)listener.sample(event.timestamp,event.values[0],event.values[1],event.values[2]);}
        public void onAccuracyChanged(Sensor sensor,int accuracy){}
    }
}
