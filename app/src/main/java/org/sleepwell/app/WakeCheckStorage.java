package org.sleepwell.app;

import android.content.SharedPreferences;
import org.json.*;

/** Versioned additive state and old checkPending/check_alarm mirrors commit atomically in one preferences file. */
final class WakeCheckStorage implements WakeCheckEngine.Store {
 static final String KEY="wakeCheckV1";final SharedPreferences prefs;
 WakeCheckStorage(SharedPreferences prefs){this.prefs=prefs;}
 public WakeCheckState load(){String raw=prefs.getString(KEY,"");if(raw.isEmpty())return null;try{JSONObject j=new JSONObject(raw);if(j.getInt("version")!=1)throw new JSONException("Unknown check state");WakeCheckState s=new WakeCheckState(j.getString("nonce"),j.getString("run"),j.getString("alarm"),j.getString("label"),j.getInt("id"),WakeCheckState.Phase.valueOf(j.getString("phase")),j.getBoolean("legacy"),j.getBoolean("delivered"),j.getLong("promptWall"),j.getLong("promptElapsed"),j.getLong("promptBoot"),j.getLong("deadlineWall"),j.getLong("deadlineElapsed"),j.getLong("deadlineBoot"),j.getString("error"));Alarm alarm=Alarm.read(new JSONObject(s.alarm));if(alarm.id!=s.alarmId||alarm.awakeCheck<=0)throw new JSONException("Invalid saved alarm");if(s.active()&&prefs.getInt("checkPending",0)!=s.alarmId)return s.update(WakeCheckState.Phase.CANCELLED,s.delivered,"");return s;}catch(Exception invalid){throw new IllegalStateException("Saved wake-up check could not be read.",invalid);}}
 public void save(WakeCheckState s){try{JSONObject j=new JSONObject().put("version",1).put("nonce",s.nonce).put("run",s.run).put("alarm",s.alarm).put("label",s.label).put("id",s.alarmId).put("phase",s.phase.name()).put("legacy",s.legacy).put("delivered",s.delivered).put("promptWall",s.promptWall).put("promptElapsed",s.promptElapsed).put("promptBoot",s.promptBoot).put("deadlineWall",s.deadlineWall).put("deadlineElapsed",s.deadlineElapsed).put("deadlineBoot",s.deadlineBoot).put("error",s.error);if(!prefs.edit().putString(KEY,j.toString()).putInt("checkPending",s.active()?s.alarmId:0).putString("check_alarm",s.alarm).commit())throw new IllegalStateException("Check state could not be saved.");}catch(JSONException impossible){throw new IllegalStateException(impossible);}}
}
