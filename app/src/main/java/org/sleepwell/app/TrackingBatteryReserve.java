package org.sleepwell.app;

import android.content.*;
import android.os.BatteryManager;
import java.util.*;

/** One admitted run owns one optional stop threshold; this class cannot start or restart tracking. */
final class TrackingBatteryReserve implements AutoCloseable {
 static final String KEY="trackingBatteryReserve",RESULT="trackingBatteryReserveResult";
 static final int[] OPTIONS={0,5,10,15,20,30};
 final int threshold;private final Context context;private final Runnable changed;
 private boolean registered,closed,known,plugged,claimed;private int percent=-1;
 private final BroadcastReceiver receiver=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent intent){read(intent);}};
 TrackingBatteryReserve(Context context,int threshold,Runnable changed){validate(threshold);this.context=context.getApplicationContext();this.threshold=threshold;this.changed=changed;}
 static int requested(Context context){try{int threshold=Prefs.of(context).getInt(KEY,0);validate(threshold);return threshold;}catch(RuntimeException invalid){throw new IllegalStateException("Review the battery reserve setting before tracking. Choose Off or a supported percentage.",invalid);}}
 private static void validate(int value){if(value!=0&&value!=5&&value!=10&&value!=15&&value!=20&&value!=30)throw new IllegalArgumentException("Choose Off, 5%, 10%, 15%, 20% or 30%.");}
 void start(){if(threshold==0||closed||registered)return;try{Intent sticky=androidx.core.content.ContextCompat.registerReceiver(context,receiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED),androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED);registered=true;read(sticky);}catch(RuntimeException unavailable){known=false;changed.run();}}
 private void read(Intent battery){if(closed)return;int level=battery==null?-1:battery.getIntExtra(BatteryManager.EXTRA_LEVEL,-1),scale=battery==null?-1:battery.getIntExtra(BatteryManager.EXTRA_SCALE,-1),status=battery==null?-1:battery.getIntExtra(BatteryManager.EXTRA_STATUS,-1),connection=battery==null?-1:battery.getIntExtra(BatteryManager.EXTRA_PLUGGED,-1);known=battery!=null&&battery.getBooleanExtra(BatteryManager.EXTRA_PRESENT,true)&&level>=0&&scale>0&&level<=scale&&connection>=0&&Set.of(BatteryManager.BATTERY_STATUS_CHARGING,BatteryManager.BATTERY_STATUS_FULL,BatteryManager.BATTERY_STATUS_DISCHARGING,BatteryManager.BATTERY_STATUS_NOT_CHARGING).contains(status);percent=known?(int)(level*100L/scale):-1;plugged=known&&(connection!=0||status==BatteryManager.BATTERY_STATUS_CHARGING||status==BatteryManager.BATTERY_STATUS_FULL);changed.run();}
 boolean due(){return threshold>0&&!closed&&!claimed&&known&&!plugged&&percent<=threshold;}
 boolean claim(){if(!due())return false;claimed=true;return true;}
 int percent(){return percent;}
 String label(){if(threshold==0)return "Battery reserve off";if(claimed)return "Battery reserve reached · ending tracking";if(!known)return "Reserve "+threshold+"% · battery state unavailable; no automatic stop";return "Reserve "+threshold+"% · battery "+percent+"%"+(plugged?" · connected":" · stops tracking at threshold");}
 @Override public void close(){closed=true;if(registered){registered=false;try{context.unregisterReceiver(receiver);}catch(RuntimeException ignored){}}}
}
