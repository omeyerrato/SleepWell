package org.sleepwell.app;

import android.content.Context;
import android.hardware.*;

/** Foreground illumination only. No camera, wake lock, service or permission. */
interface RoomLightPort {
    interface Sink {void sample(long elapsed,float lux,boolean reliable);}
    interface Factory {RoomLightPort create(Context app);}
    Factory PLATFORM=AndroidRoomLight::new;
    boolean available();boolean begin(Sink sink);void close();
}
final class AndroidRoomLight implements RoomLightPort,SensorEventListener {
    private final SensorManager manager;private final Sensor sensor;private Sink sink;
    AndroidRoomLight(Context app){manager=app.getSystemService(SensorManager.class);sensor=manager==null?null:manager.getDefaultSensor(Sensor.TYPE_LIGHT);}
    public boolean available(){return sensor!=null;}
    public boolean begin(Sink value){close();sink=value;boolean started=sensor!=null&&manager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_NORMAL);if(!started)sink=null;return started;}
    public void close(){if(manager!=null)manager.unregisterListener(this);sink=null;}
    public void onSensorChanged(SensorEvent event){Sink owner=sink;if(owner!=null&&event.values.length>0)owner.sample(event.timestamp/1000000L,event.values[0],event.accuracy!=SensorManager.SENSOR_STATUS_UNRELIABLE);}
    public void onAccuracyChanged(Sensor source,int accuracy){if(sink!=null&&accuracy==SensorManager.SENSOR_STATUS_UNRELIABLE)sink.sample(android.os.SystemClock.elapsedRealtime(),Float.NaN,false);}
}
