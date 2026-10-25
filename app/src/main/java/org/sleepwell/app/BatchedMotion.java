package org.sleepwell.app;

import android.content.Context;
import android.hardware.*;
import android.os.*;
import java.util.*;
import java.util.function.*;
import org.json.*;

/** Manual wake-up accelerometer FIFO owner. Epoch boundaries use capture timestamps, never delivery time. */
final class BatchedMotion implements SensorEventListener2 {
 static final String KEY="batchMotionTracking",ALGORITHM="wake_fifo_motion_v1";
 static final int PERIOD_US=200000,LATENCY_US=15000000,MIN_FIFO=150;
 static final long MINUTE=60000000000L,MAX_AGE=90000000000L,FLUSH_TIMEOUT=8000;
 interface Sink{void frame(Frame frame);void failed(String issue);void limit();}
 static final class Frame{final long from,to;final double movement;final int samples;Frame(long from,long to,double movement,int samples){this.from=from;this.to=to;this.movement=movement;this.samples=samples;}}
 static boolean requested(Context c){return Prefs.of(c).getBoolean(KEY,false);}
 static Sensor supported(Context c){SensorManager manager=c.getSystemService(SensorManager.class);Sensor sensor=manager==null?null:manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER,true);if(sensor==null||!sensor.isWakeUpSensor()||sensor.getReportingMode()!=Sensor.REPORTING_MODE_CONTINUOUS||sensor.getFifoReservedEventCount()<MIN_FIFO||sensor.getFifoMaxEventCount()<MIN_FIFO)throw new IllegalStateException("Sensor batching needs a wake-up accelerometer with at least 150 reserved FIFO events. This phone does not report that support.");return sensor;}
 static void selection(Context c,boolean sonar,boolean automatic,boolean delayed,boolean microphone,boolean light,boolean adaptive,boolean flip){
  if(sonar||automatic||delayed)throw new IllegalStateException("Sensor batching supports immediate manual phone movement only. Turn it Off for contactless, automatic or delayed starts.");
  if(microphone||light||adaptive||flip)throw new IllegalStateException("For sensor batching, turn Record sounds, Record light, Battery-aware sampling and Flip pause Off first.");
  if(SoundPreferences.load(c).withTracking)throw new IllegalStateException("Turn sleep sounds with tracking Off before using sensor batching.");
  supported(c);
 }
 private final SensorManager manager;private final Sensor sensor;private final Handler main;private final BooleanSupplier owns;private final Sink sink;private final MotionFilter filter=new MotionFilter();private final PowerManager.WakeLock drainWake;
 final long wall,anchor,deadline;private long lastSeen,lastAccepted,bucket=-1,from,to;private int count;private double sum;private Frame latest;
 private boolean registered,closing,closed,failed,limitSent,missing;private long cut=Long.MAX_VALUE;private Consumer<Boolean> done;private String issue="";
 BatchedMotion(Context c,long wall,long anchor,long deadline,BooleanSupplier owns,Sink sink){this.wall=wall;this.anchor=anchor;this.deadline=deadline;this.owns=owns;this.sink=sink;manager=c.getSystemService(SensorManager.class);sensor=supported(c);main=new Handler(Looper.getMainLooper());PowerManager power=c.getSystemService(PowerManager.class);if(power==null)throw new IllegalStateException("Sensor flush wake ownership is unavailable.");drainWake=power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"SleepWell:batch-drain");}
 void start(){if(closed||registered)throw new IllegalStateException("This batching segment already started.");registered=true;registered=manager.registerListener(this,sensor,PERIOD_US,LATENCY_US,main);if(!registered)throw new IllegalStateException("The wake-up sensor rejected FIFO batching. Turn batching Off to use normal tracking.");}
 private long stamp(long captured){return wall+(captured-anchor)/1000000L;}
 @Override public void onSensorChanged(SensorEvent event){
  if(closed||failed||!owns.getAsBoolean())return;long now=SystemClock.elapsedRealtimeNanos(),captured=event.timestamp;
  if(captured<anchor||captured<=lastSeen||captured>now+50000000L)return;lastSeen=captured;
  if(captured>Math.min(cut,deadline)){if(!closing&&captured>=deadline)limit();return;}
  if(now-captured>MAX_AGE){gap("Sensor batch arrived too late; missing measurements remain blank");return;}
  if(event.values.length<3||event.accuracy==SensorManager.SENSOR_STATUS_UNRELIABLE||!Float.isFinite(event.values[0])||!Float.isFinite(event.values[1])||!Float.isFinite(event.values[2])||Math.abs(event.values[0])>100||Math.abs(event.values[1])>100||Math.abs(event.values[2])>100){gap("Unreliable movement input; missing measurements remain blank");return;}
  if(lastAccepted>0&&captured-lastAccepted<PERIOD_US*1000L)return;
  try{
   long previous=lastAccepted,interval=previous==0?0:captured-previous;
   if(interval>1000000000L){emit();filter.reset();previous=0;interval=0;missing=true;issue="A capture gap was left blank";}
   Double movement=filter.sample(event.values[0],event.values[1],event.values[2],interval);lastAccepted=captured;
   if(movement==null)return;long index=(captured-anchor)/MINUTE;
   if(bucket!=index){emit();bucket=index;from=Math.max(anchor+index*MINUTE,previous==0?captured:previous);}
   to=captured;sum+=movement*movement;count++;
   if(!closing&&now>=deadline)limit();
  }catch(RuntimeException storage){fail("Batched movement could not be saved. Existing data was kept.");}
 }
 private void gap(String message){missing=true;try{emit();}catch(RuntimeException storage){fail("Batched movement could not be saved. Existing data was kept.");}filter.reset();lastAccepted=0;issue=message;}
 private void emit(){Frame frame=count>0&&to>from?new Frame(stamp(from),stamp(to),Math.sqrt(sum/count),count):null;bucket=-1;count=0;sum=0;from=to=0;if(frame!=null){sink.frame(frame);latest=frame;}}
 private void limit(){if(limitSent)return;limitSent=true;main.post(()->{if(!closed&&owns.getAsBoolean())sink.limit();});}
 private void fail(String message){if(failed)return;failed=true;issue=message;main.post(()->{if(!closed&&owns.getAsBoolean())sink.failed(message);});}
 @Override public void onAccuracyChanged(Sensor sensor,int accuracy){}
 @Override public void onFlushCompleted(Sensor flushed){if(closing&&!closed&&flushed==sensor)complete(true);}
 /** No join/wait: a bounded wake lease holds only while the explicit FIFO flush is acknowledged. */
 void finish(long capturedCut,Consumer<Boolean> complete){if(closed){if(registered){closed=false;done=complete;complete(false);}else complete.accept(false);return;}if(closing)return;closing=true;cut=Math.max(anchor,Math.min(deadline,capturedCut));done=complete;try{drainWake.acquire(FLUSH_TIMEOUT+2000);if(!registered||!manager.flush(this)){issue="Pending sensor data could not be flushed; unreceived tail was not reconstructed";complete(false);return;}main.postDelayed(timeout,FLUSH_TIMEOUT);}catch(RuntimeException unavailable){issue="Sensor flush failed; unreceived tail was not reconstructed";complete(false);}}
 private final Runnable timeout=()->{if(closing&&!closed){issue="Sensor flush timed out; unreceived tail was not reconstructed";complete(false);}};
 private void complete(boolean acknowledged){if(closed)return;main.removeCallbacks(timeout);boolean saved=acknowledged&&!failed&&!missing;try{emit();}catch(RuntimeException storage){saved=false;issue="Final received sensor data could not be saved";}try{if(registered){manager.unregisterListener(this);registered=false;}}catch(RuntimeException cleanup){saved=false;issue="Sensor listener cleanup needs attention";}closed=true;try{if(drainWake.isHeld())drainWake.release();}finally{Consumer<Boolean> callback=done;done=null;if(callback!=null)callback.accept(saved);}}
 boolean released(){return !registered;}
 int samples(){return count>0?count:latest==null?0:latest.samples;}
 Double movement(){return count>0?Math.sqrt(sum/count):latest==null?null:latest.movement;}
 long latestEnd(){return count>0?stamp(to):latest==null?wall:latest.to;}
 String label(){return closed?issue.isEmpty()?"Batch segment closed":"Batch segment closed · "+issue:closing?"Draining pending sensor readings…":failed?issue:"Wake-up FIFO · 5 Hz requested · delivery up to 15 s"+(issue.isEmpty()?"":" · "+issue);}
 List<JSONObject> pending(){if(count==0||to<=from)return List.of();try{return List.of(new JSONObject().put("start",stamp(from)).put("time",stamp(to)).put("movement",Math.sqrt(sum/count)).put("samples",count));}catch(JSONException impossible){throw new IllegalStateException(impossible);}}
 static String config(String calibration){try{String raw=new JSONObject().put("version",1).put("algorithm",ALGORITHM).put("period_us",PERIOD_US).put("latency_us",LATENCY_US).put("minimum_reserved_fifo",MIN_FIFO).put("calibration",calibration).toString();validate(raw);return raw;}catch(JSONException invalid){throw new IllegalArgumentException("Batched motion configuration could not be saved",invalid);}}
 static void validate(String raw){if(raw==null||raw.length()>1024)throw new IllegalArgumentException("Invalid batched motion configuration");try{JSONObject j=new JSONObject(raw);Set<String> fields=Set.of("version","algorithm","period_us","latency_us","minimum_reserved_fifo","calibration");if(j.length()!=fields.size())throw new JSONException("Unexpected batched configuration fields");for(Iterator<String> it=j.keys();it.hasNext();)if(!fields.contains(it.next()))throw new JSONException("Unknown batched configuration field");if(SensorProvenance.whole(j,"version")!=1||!ALGORITHM.equals(SensorProvenance.text(j,"algorithm"))||SensorProvenance.whole(j,"period_us")!=PERIOD_US||SensorProvenance.whole(j,"latency_us")!=LATENCY_US||SensorProvenance.whole(j,"minimum_reserved_fifo")!=MIN_FIFO)throw new JSONException("Unsupported batching policy");String calibration=SensorProvenance.text(j,"calibration");if(!calibration.isEmpty())MotionCalibration.read(calibration);}catch(JSONException invalid){throw new IllegalArgumentException("Invalid batched motion configuration",invalid);}}
}
