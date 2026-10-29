package org.sleepwell.app;

import android.content.Context;
import android.hardware.*;
import android.os.*;

/** Per-controller foreground sensor handle. Construction never registers a listener. */
final class VisibleTaskSensors {
    enum Kind { LIGHT, ROTATION, ACCELERATION }
    interface Sink { void sample(long time,float[] values,int accuracy); void accuracy(int value); }
    interface Port {
        boolean available(); float range(); int reportingMode(); long now();
        boolean start(Sink sink); boolean stop();
    }
    static Port create(Context context,Kind kind){return new Platform(context,kind);}
    private static final class Platform implements Port,SensorEventListener {
        final SensorManager manager;final Sensor sensor;final Kind kind;
        Sink sink;boolean registered;
        Platform(Context context,Kind kind){
            this.kind=kind;manager=context.getSystemService(SensorManager.class);
            Sensor candidate=manager==null?null:manager.getDefaultSensor(kind==Kind.LIGHT?Sensor.TYPE_LIGHT:kind==Kind.ROTATION?Sensor.TYPE_GAME_ROTATION_VECTOR:Sensor.TYPE_ACCELEROMETER);
            sensor=candidate!=null?candidate:kind==Kind.ROTATION&&manager!=null?manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR):null;
        }
        public boolean available(){return sensor!=null;}
        public float range(){return sensor==null?0:sensor.getMaximumRange();}
        public int reportingMode(){return sensor==null?-1:sensor.getReportingMode();}
        public long now(){return SystemClock.elapsedRealtimeNanos();}
        public boolean start(Sink sink){
            if(Looper.myLooper()!=Looper.getMainLooper())throw new IllegalStateException("Sensor ownership belongs to the UI thread");
            if(!stop()||sensor==null)return false;this.sink=sink;
            // Preserve an uncertain registration until this exact listener confirms cleanup.
            registered=true;
            try{boolean accepted=manager.registerListener(this,sensor,kind==Kind.LIGHT?SensorManager.SENSOR_DELAY_NORMAL:SensorManager.SENSOR_DELAY_GAME,new Handler(Looper.getMainLooper()));if(!accepted)stop();return accepted;}
            catch(RuntimeException failure){stop();throw failure;}
        }
        public boolean stop(){sink=null;if(!registered)return true;try{manager.unregisterListener(this);registered=false;return true;}catch(RuntimeException failure){return false;}}
        public void onSensorChanged(SensorEvent event){Sink current=sink;if(registered&&current!=null)current.sample(event.timestamp,event.values.clone(),event.accuracy);}
        public void onAccuracyChanged(Sensor sensor,int accuracy){Sink current=sink;if(registered&&current!=null)current.accuracy(accuracy);}
    }
    private VisibleTaskSensors(){}
}
