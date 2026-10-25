package org.sleepwell.app;
import android.content.*;
import org.json.*;
import java.util.function.BooleanSupplier;

/** Shared gate prevents a stale decoder checkpoint overwriting a newer occurrence. Worker writes only. */
final class AlarmPlaylistJournal {
    static int read(SharedPreferences preferences,String run,String key){synchronized(AlarmPlaylistJournal.class){try{JSONObject saved=new JSONObject(preferences.getString("alarmPlaylistPosition",""));Object raw=saved.get("position");if(!(raw instanceof Number))return 0;double position=((Number)raw).doubleValue();return run.equals(saved.getString("run"))&&key.equals(saved.getString("key"))&&Double.isFinite(position)&&position==Math.rint(position)&&position>=0&&position<8?(int)position:0;}catch(JSONException|RuntimeException missing){return 0;}}}
    @android.annotation.SuppressLint("ApplySharedPref")
    static boolean save(SharedPreferences preferences,String run,String key,int position,BooleanSupplier current){synchronized(AlarmPlaylistJournal.class){if(!current.getAsBoolean())return false;try{return preferences.edit().putString("alarmPlaylistPosition",new JSONObject().put("run",run).put("key",key).put("position",position).toString()).commit()&&current.getAsBoolean();}catch(JSONException|RuntimeException unavailable){return false;}}}
}
