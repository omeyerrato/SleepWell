package org.sleepwell.app;

import android.content.Context;
import android.hardware.*;
import android.os.SystemClock;

/** Native sensor boundary. Tests replace the factory before launching the configuration Activity. */
interface MotionPort {
    interface Sink{void sample(long time,float x,float y,float z,int accuracy);}
    interface Factory{MotionPort create(Context c);}
    Factory PLATFORM=AndroidMotionPort::new;
    boolean available();boolean begin(Sink sink);void close();long now();
}
final class AndroidMotionPort implements MotionPort,SensorEventListener {
    private final SensorManager manager;private final Sensor sensor;private Sink sink;
    AndroidMotionPort(Context c){manager=c.getSystemService(SensorManager.class);sensor=manager==null?null:manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);}
    public boolean available(){return sensor!=null;}
    public boolean begin(Sink sink){close();this.sink=sink;boolean registered=sensor!=null&&manager.registerListener(this,sensor,200000);if(!registered)this.sink=null;return registered;}
    public void close(){if(manager!=null)manager.unregisterListener(this);sink=null;}
    public long now(){return SystemClock.elapsedRealtime();}
    public void onSensorChanged(SensorEvent event){Sink target=sink;if(target!=null&&event.values.length>=3)target.sample(event.timestamp/1000000L,event.values[0],event.values[1],event.values[2],event.accuracy);}
    public void onAccuracyChanged(Sensor sensor,int accuracy){}
}
