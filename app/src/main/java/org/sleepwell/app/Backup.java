package org.sleepwell.app;

import android.content.*;
import android.database.sqlite.SQLiteDatabase;
import org.json.*;

final class Backup {
    static void validate(JSONObject j) throws JSONException {
        if(j.getInt("format")!=1)throw new JSONException("Unsupported backup format");
        JSONArray alarms=j.getJSONArray("alarms"),sessions=j.getJSONArray("sessions"),epochs=j.getJSONArray("epochs");
        java.util.Set<Long> ids=new java.util.HashSet<>(), alarmIds=new java.util.HashSet<>();
        for(int i=0;i<alarms.length();i++){Alarm a=Alarm.read(alarms.getJSONObject(i));if(a.id<=0||!alarmIds.add((long)a.id)||a.hour<0||a.hour>23||a.minute<0||a.minute>59||a.volume<10||a.volume>100||a.difficulty<1||a.difficulty>6||a.smartWindow<0||a.smartWindow>60||a.rampSeconds<0||a.rampSeconds>120||a.snoozeMinutes<1||a.snoozeMinutes>15||a.snoozeLimit<0||a.snoozeLimit>5||a.awakeCheck<0||a.awakeCheck>15)throw new JSONException("Invalid alarm");for(int day:a.days)if(day<1||day>7)throw new JSONException("Invalid weekday");}
        for(int i=0;i<sessions.length();i++){JSONObject s=sessions.getJSONObject(i);long id=s.getLong("id"),start=s.getLong("start");if(id<=0||!ids.add(id)||start<=0||(!s.isNull("end")&&s.getLong("end")<start))throw new JSONException("Invalid session");}
        for(int i=0;i<epochs.length();i++){JSONObject e=epochs.getJSONObject(i);if(!ids.contains(e.getLong("session"))||e.getLong("time")<=0)throw new JSONException("Invalid sensor record");for(String k:new String[]{"movement","noise","light"})if(!e.isNull(k)&&(!Double.isFinite(e.getDouble(k))||e.getDouble(k)<0))throw new JSONException("Invalid sensor value");}
    }
    static void restore(Context c,Store store,JSONObject root) throws JSONException {
        validate(root);SQLiteDatabase db=store.getWritableDatabase();java.util.List<Alarm> previous=store.alarms();java.util.List<JSONObject> clips=store.rows("SELECT path FROM clips");
        db.beginTransaction();try {
            db.delete("alarms",null,null);db.delete("epochs",null,null);db.delete("sessions",null,null);db.delete("clips",null,null);
            JSONArray alarms=root.getJSONArray("alarms");for(int i=0;i<alarms.length();i++){Alarm a=Alarm.read(alarms.getJSONObject(i));a.enabled=false;if(a.tone.startsWith("content:"))a.tone="daybreak";store.save(a);}
            JSONArray sessions=root.getJSONArray("sessions");for(int i=0;i<sessions.length();i++){JSONObject j=sessions.getJSONObject(i);ContentValues v=new ContentValues();v.put("id",j.getLong("id"));v.put("start",j.getLong("start"));v.put("end",j.isNull("end")?j.getLong("start"):j.getLong("end"));v.put("note",j.optString("note",""));v.put("rating",Math.max(0,Math.min(5,j.optInt("rating"))));v.put("interrupted",j.isNull("end")?1:j.optInt("interrupted"));db.insertOrThrow("sessions",null,v);}
            JSONArray epochs=root.getJSONArray("epochs");for(int i=0;i<epochs.length();i++){JSONObject e=epochs.getJSONObject(i);store.epoch(e.getLong("session"),e.getLong("time"),e.isNull("movement")?null:e.getDouble("movement"),e.isNull("noise")?null:e.getDouble("noise"),e.isNull("light")?null:e.getDouble("light"),e.optInt("samples",0));}
            db.setTransactionSuccessful();
        }finally {db.endTransaction();}
        for(Alarm a:previous){Scheduler.cancel(c,a.id,Scheduler.FIRE);Scheduler.cancel(c,a.id,Scheduler.SNOOZE);Scheduler.cancel(c,a.id,Scheduler.CHECK);}
        Prefs.put(c,"checkPending",0);for(JSONObject clip:clips)FileSafety.deleteClip(c,clip.optString("path"));
    }
}
