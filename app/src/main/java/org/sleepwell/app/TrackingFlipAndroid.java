package org.sleepwell.app;
import android.content.*;import android.hardware.*;import android.os.*;
/** Separate low-rate posture listener. No tracking samples or permission prompts. */
final class TrackingFlipAndroid implements TrackingFlipSession.Port,SensorEventListener {
 interface Factory{TrackingFlipSession.Port create(Context c);}static Factory factory=TrackingFlipAndroid::new;final SensorManager manager;final PowerManager power;Sensor sensor;TrackingFlipSession.Sink sink;boolean registered,reliable=true;PowerManager.WakeLock wake;
 TrackingFlipAndroid(Context c){manager=c.getSystemService(SensorManager.class);power=c.getSystemService(PowerManager.class);sensor=manager==null?null:manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);}
 public boolean available(){return sensor!=null;}
 public boolean start(TrackingFlipSession.Sink next){if(registered)return false;if(sensor==null)return false;sink=next;reliable=true;try{registered=manager.registerListener(this,sensor,100000,new Handler(Looper.getMainLooper()));if(!registered)sink=null;return registered;}catch(RuntimeException failure){registered=true;stop();throw failure;}}
 public boolean stop(){sink=null;if(!registered)return true;try{manager.unregisterListener(this);registered=false;return true;}catch(RuntimeException failure){return false;}}
 public void hold(long milliseconds){if(wake==null)wake=power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"SleepWell:flip-pause");wake.setReferenceCounted(false);wake.acquire(milliseconds);}
 public void release(){if(wake!=null&&wake.isHeld())wake.release();}
 @Override public void onSensorChanged(SensorEvent event){TrackingFlipSession.Sink callback=sink;if(registered&&callback!=null&&event.values.length>=3)callback.sample(event.timestamp,SystemClock.elapsedRealtimeNanos(),event.values[0],event.values[1],event.values[2],reliable);}
 @Override public void onAccuracyChanged(Sensor sensor,int accuracy){reliable=accuracy!=SensorManager.SENSOR_STATUS_UNRELIABLE;}
}
