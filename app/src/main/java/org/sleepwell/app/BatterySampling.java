package org.sleepwell.app;

import android.content.*;
import android.os.BatteryManager;
import java.util.*;
import org.json.*;

/** Per-admitted-run sampling policy. It cannot start capture or change alarm deadlines. */
final class BatterySampling implements AutoCloseable {
 static final String KEY="batteryAwareTracking",ALGORITHM="battery_motion_sampling_v1";
 static final int NORMAL=200000,REDUCED=500000,LIGHT=1000000,LOW=20,RECOVER=25;
 final boolean enabled;private final Context context;private final Runnable changed;
 private boolean registered,closed,known,reduced,charging;private int percent=-1;
 private final BroadcastReceiver receiver=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent intent){read(intent);}};
 BatterySampling(Context context,boolean enabled,Runnable changed){this.context=context.getApplicationContext();this.enabled=enabled;this.changed=changed;}
 static boolean requested(Context c){return Prefs.of(c).getBoolean(KEY,false);}
 void start(){if(!enabled||closed||registered)return;try{Intent sticky=androidx.core.content.ContextCompat.registerReceiver(context,receiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED),androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED);registered=true;if(sticky!=null)read(sticky);else read(null);}catch(RuntimeException unavailable){known=false;reduced=false;changed.run();}}
 private void read(Intent value){if(closed)return;boolean before=reduced;int level=value==null?-1:value.getIntExtra(BatteryManager.EXTRA_LEVEL,-1),scale=value==null?-1:value.getIntExtra(BatteryManager.EXTRA_SCALE,-1),status=value==null?-1:value.getIntExtra(BatteryManager.EXTRA_STATUS,-1),plugged=value==null?-1:value.getIntExtra(BatteryManager.EXTRA_PLUGGED,-1);known=value!=null&&value.getBooleanExtra(BatteryManager.EXTRA_PRESENT,true)&&scale>0&&level>=0&&level<=scale&&plugged>=0&&Set.of(BatteryManager.BATTERY_STATUS_CHARGING,BatteryManager.BATTERY_STATUS_FULL,BatteryManager.BATTERY_STATUS_DISCHARGING,BatteryManager.BATTERY_STATUS_NOT_CHARGING).contains(status);percent=known?(int)(level*100L/scale):-1;charging=known&&(plugged!=0||status==BatteryManager.BATTERY_STATUS_CHARGING||status==BatteryManager.BATTERY_STATUS_FULL);reduced=enabled&&known&&!charging&&(percent<=LOW||before&&percent<RECOVER);changed.run();}
 boolean reduced(){return enabled&&known&&reduced&&!closed;}
 String label(){if(!enabled)return "Normal sampling · battery-aware mode off";if(!known)return "Normal sampling · battery state unavailable";if(reduced())return "Battery-aware · "+percent+"% · motion 0.5 s / light 1 s requested";return "Normal sampling · "+percent+"%"+(charging?" · charging":" · reduces at 20%");}
 @Override public void close(){closed=true;if(registered){registered=false;try{context.unregisterReceiver(receiver);}catch(RuntimeException ignored){}}}
 static String config(String calibration){if(calibration==null)throw new IllegalArgumentException("Missing motion calibration");try{String raw=new JSONObject().put("version",1).put("algorithm",ALGORITHM).put("normal_motion_us",NORMAL).put("reduced_motion_us",REDUCED).put("reduced_light_us",LIGHT).put("low_percent",LOW).put("resume_percent",RECOVER).put("calibration",calibration).toString();validate(raw);return raw;}catch(JSONException impossible){throw new IllegalArgumentException("Sampling configuration could not be saved",impossible);}}
 static void validate(String raw){if(raw==null||raw.length()>1024)throw new IllegalArgumentException("Invalid battery sampling configuration");try{JSONObject j=new JSONObject(raw);Set<String> fields=Set.of("version","algorithm","normal_motion_us","reduced_motion_us","reduced_light_us","low_percent","resume_percent","calibration");if(j.length()!=fields.size())throw new JSONException("Unexpected sampling fields");for(Iterator<String> it=j.keys();it.hasNext();)if(!fields.contains(it.next()))throw new JSONException("Unknown sampling field");if(SensorProvenance.whole(j,"version")!=1||!ALGORITHM.equals(SensorProvenance.text(j,"algorithm"))||SensorProvenance.whole(j,"normal_motion_us")!=NORMAL||SensorProvenance.whole(j,"reduced_motion_us")!=REDUCED||SensorProvenance.whole(j,"reduced_light_us")!=LIGHT||SensorProvenance.whole(j,"low_percent")!=LOW||SensorProvenance.whole(j,"resume_percent")!=RECOVER)throw new JSONException("Unsupported sampling policy");String calibration=SensorProvenance.text(j,"calibration");if(!calibration.isEmpty())MotionCalibration.read(calibration);}catch(JSONException invalid){throw new IllegalArgumentException("Invalid battery sampling configuration",invalid);}}
}
