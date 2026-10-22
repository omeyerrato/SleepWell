package org.sleepwell.app;

import android.content.*;
import android.database.sqlite.SQLiteDatabase;
import org.json.*;
import java.util.*;

final class Backup {
    static void validate(JSONObject root) throws JSONException {
        int format=root.getInt("format");if(format!=1&&format!=2)throw new JSONException("Unsupported backup format");
        JSONArray alarms=root.getJSONArray("alarms"),sessions=root.getJSONArray("sessions"),epochs=root.getJSONArray("epochs");
        Set<Long> ids=new HashSet<>(),alarmIds=new HashSet<>();Map<Long,JSONObject> byId=new HashMap<>();
        for(int i=0;i<alarms.length();i++){
            Alarm a=Alarm.read(alarms.getJSONObject(i));if(a.id<=0||!alarmIds.add((long)a.id)||a.hour<0||a.hour>23||a.minute<0||a.minute>59||a.volume<10||a.volume>100||a.difficulty<1||a.difficulty>6||a.smartWindow<0||a.smartWindow>60||a.rampSeconds<0||a.rampSeconds>120||a.snoozeMinutes<1||a.snoozeMinutes>15||a.snoozeLimit<0||a.snoozeLimit>5||a.awakeCheck<0||a.awakeCheck>15)throw new JSONException("Invalid alarm");
            for(int day:a.days)if(day<1||day>7)throw new JSONException("Invalid weekday");
        }
        for(int i=0;i<sessions.length();i++){
            JSONObject s=sessions.getJSONObject(i);long id=s.getLong("id"),start=s.getLong("start");
            if(id<=0||!ids.add(id)||start<=0||(!s.isNull("end")&&s.getLong("end")<start)||(!s.isNull("active_ms")&&s.getLong("active_ms")<0))throw new JSONException("Invalid session");byId.put(id,s);
        }
        for(int i=0;i<epochs.length();i++){
            JSONObject e=epochs.getJSONObject(i);if(!ids.contains(e.getLong("session"))||e.getLong("time")<=0||(!e.isNull("start")&&(e.getLong("start")<=0||e.getLong("start")>e.getLong("time"))))throw new JSONException("Invalid sensor record");
            for(String k:new String[]{"movement","noise","light"})if(!e.isNull(k)&&(!Double.isFinite(e.getDouble(k))||e.getDouble(k)<0))throw new JSONException("Invalid sensor value");
        }
        if(format==2){
            JSONArray pauses=root.getJSONArray("pauses");Set<Long> pauseIds=new HashSet<>();Map<Long,Long> previousEnd=new HashMap<>();List<JSONObject> sorted=new ArrayList<>();
            for(int i=0;i<pauses.length();i++)sorted.add(pauses.getJSONObject(i));sorted.sort(Comparator.comparingLong(p->p.optLong("start")));
            for(JSONObject p:sorted){long id=p.getLong("id"),session=p.getLong("session"),from=p.getLong("start"),to=p.isNull("end")?Long.MAX_VALUE:p.getLong("end");JSONObject s=byId.get(session);
                if(id<=0||!pauseIds.add(id)||s==null||from<s.getLong("start")||to<from||(!s.isNull("end")&&(p.isNull("end")||to>s.getLong("end")))||from<previousEnd.getOrDefault(session,Long.MIN_VALUE))throw new JSONException("Invalid pause interval");previousEnd.put(session,to);
            }
        }
    }
    static void restore(Context c,Store store,JSONObject root) throws JSONException {
        List<Alarm> previous=store.alarms();List<JSONObject> clips=store.rows("SELECT path FROM clips");restoreData(store,root);
        for(Alarm a:previous){Scheduler.cancel(c,a.id,Scheduler.FIRE);Scheduler.cancel(c,a.id,Scheduler.SNOOZE);Scheduler.cancel(c,a.id,Scheduler.CHECK);Scheduler.cancel(c,a.id,Scheduler.BACKUP);}
        Scheduler.cancel(c,-1,Scheduler.TEST);Prefs.put(c,"checkPending",0);for(JSONObject clip:clips)FileSafety.deleteClip(c,clip.optString("path"));
    }
    static void restoreData(Store store,JSONObject root) throws JSONException {
        validate(root);SQLiteDatabase db=store.getWritableDatabase();
        Map<Long,Long> endpoints=new HashMap<>();Set<Long> unfinished=new HashSet<>();JSONArray sessions=root.getJSONArray("sessions"),epochs=root.getJSONArray("epochs"),pauses=root.getInt("format")==2?root.getJSONArray("pauses"):null;
        for(int i=0;i<sessions.length();i++){JSONObject s=sessions.getJSONObject(i);endpoints.put(s.getLong("id"),s.isNull("end")?s.getLong("start"):s.getLong("end"));if(s.isNull("end"))unfinished.add(s.getLong("id"));}
        for(int i=0;i<epochs.length();i++){JSONObject e=epochs.getJSONObject(i);long id=e.getLong("session");if(unfinished.contains(id))endpoints.put(id,Math.max(endpoints.get(id),e.getLong("time")));}
        if(pauses!=null)for(int i=0;i<pauses.length();i++){JSONObject p=pauses.getJSONObject(i);long id=p.getLong("session");endpoints.put(id,Math.max(endpoints.get(id),p.isNull("end")?p.getLong("start"):p.getLong("end")));}
        db.beginTransaction();try {
            db.delete("alarms",null,null);db.delete("epochs",null,null);db.delete("tracking_pauses",null,null);db.delete("sessions",null,null);db.delete("clips",null,null);
            JSONArray alarms=root.getJSONArray("alarms");for(int i=0;i<alarms.length();i++){Alarm a=Alarm.read(alarms.getJSONObject(i));a.enabled=false;if(a.tone.startsWith("content:"))a.tone="daybreak";store.save(a);}
            for(int i=0;i<sessions.length();i++){
                JSONObject j=sessions.getJSONObject(i);ContentValues v=new ContentValues();long id=j.getLong("id");v.put("id",id);v.put("start",j.getLong("start"));v.put("end",endpoints.get(id));v.put("note",j.optString("note",""));v.put("rating",Math.max(0,Math.min(5,j.optInt("rating"))));v.put("interrupted",j.isNull("end")?1:j.optInt("interrupted"));v.put("clock_changed",j.optInt("clock_changed",0)==0?0:1);if(!j.isNull("active_ms"))v.put("active_ms",j.getLong("active_ms"));db.insertOrThrow("sessions",null,v);
            }
            for(int i=0;i<epochs.length();i++){JSONObject e=epochs.getJSONObject(i);store.epoch(e.getLong("session"),e.isNull("start")?null:e.getLong("start"),e.getLong("time"),e.isNull("movement")?null:e.getDouble("movement"),e.isNull("noise")?null:e.getDouble("noise"),e.isNull("light")?null:e.getDouble("light"),e.optInt("samples",0));}
            if(pauses!=null)for(int i=0;i<pauses.length();i++){JSONObject p=pauses.getJSONObject(i);ContentValues v=new ContentValues();v.put("id",p.getLong("id"));v.put("session",p.getLong("session"));v.put("start",p.getLong("start"));v.put("end",p.isNull("end")?endpoints.get(p.getLong("session")):p.getLong("end"));db.insertOrThrow("tracking_pauses",null,v);}
            db.setTransactionSuccessful();
        }finally {db.endTransaction();}
    }
}
