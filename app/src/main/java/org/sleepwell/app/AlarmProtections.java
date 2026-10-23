package org.sleepwell.app;

import android.content.*;
import org.json.*;

/** Existing global alarmGuard key is preserved; both protections apply to every real ringing alarm. */
final class AlarmProtections {
    static final String LEAVING="alarmGuard",POWER="alarmPowerGuard";
    interface Factory {SharedPreferences open(Context context);}
    static Factory factory=Prefs::of;
    static boolean enabled(Context context,String key){return factory.open(context).getBoolean(key,false);}
    static void set(Context context,String key,boolean value){factory.open(context).edit().putBoolean(key,value).apply();}
    static boolean realAlarm(Context context){if(!AlarmService.active||!Prefs.flag(context,"ringing"))return false;try{return PowerMenuGuard.activeAlarm(AlarmService.active,true,new JSONObject(Prefs.text(context,"active_alarm","{}")).optInt("id"));}catch(JSONException e){return false;}}
}
