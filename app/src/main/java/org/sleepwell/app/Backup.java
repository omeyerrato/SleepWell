package org.sleepwell.app;

import android.content.*;
import android.database.sqlite.SQLiteDatabase;
import org.json.*;
import java.util.*;

final class Backup {
    static void validate(JSONObject root) throws JSONException {
        int format=root.getInt("format");if(format<1||format>13)throw new JSONException("Unsupported backup format");
        JSONArray alarms=root.getJSONArray("alarms"),sessions=root.getJSONArray("sessions"),epochs=root.getJSONArray("epochs");
        Set<Long> ids=new HashSet<>(),alarmIds=new HashSet<>();Map<Long,JSONObject> byId=new HashMap<>();
        for(int i=0;i<alarms.length();i++){
            Alarm a=Alarm.read(alarms.getJSONObject(i));if(a.repeatInvalid||a.weeklyNapOwnerInvalid||a.bedtimeInvalid||a.excludedDatesInvalid||a.solarInvalid||a.soundModeInvalid||a.timingInvalid||a.inputsInvalid||a.dateInvalid||a.skipInvalid||a.napInvalid||a.routineInvalid||a.snoozeTotalInvalid||a.snoozeTotalMinutes<0||a.snoozeTotalMinutes>300||a.id<=0||!alarmIds.add((long)a.id)||a.hour<0||a.hour>23||a.minute<0||a.minute>59||a.volume<10||a.volume>100||a.difficulty<1||a.difficulty>6||a.smartWindow<0||a.smartWindow>60||a.rampSeconds<0||a.rampSeconds>1440||a.soundDelaySeconds<0||a.soundDelaySeconds>1440||a.vibrationDelaySeconds<0||a.vibrationDelaySeconds>1440||a.snoozeMinutes<1||a.snoozeMinutes>60||a.snoozeLimit<0||a.snoozeLimit>5||a.awakeCheck<0||a.awakeCheck>15)throw new JSONException("Invalid alarm");
            for(int day:a.days)if(day<1||day>7)throw new JSONException("Invalid weekday");
        }
        for(int i=0;i<sessions.length();i++){
            JSONObject s=sessions.getJSONObject(i);long id=s.getLong("id"),start=s.getLong("start");
            if(id<=0||!ids.add(id)||start<=0||(!s.isNull("end")&&s.getLong("end")<start)||(!s.isNull("active_ms")&&s.getLong("active_ms")<0))throw new JSONException("Invalid session");byId.put(id,s);
            if(format>=3){if(!Set.of("tracked","manual","legacy").contains(s.getString("source"))||!Set.of("local","imported","legacy").contains(s.getString("origin")))throw new JSONException("Invalid record provenance");String zone=s.getString("zone");if(!zone.isEmpty())try{java.time.ZoneId.of(zone);}catch(java.time.DateTimeException e){throw new JSONException("Invalid record time zone");}}
            if(format>=3&&s.getString("source").equals("manual")&&(!s.isNull("active_ms")||s.isNull("end")||s.getLong("end")<=start||s.getLong("end")>System.currentTimeMillis()||s.getLong("end")-start>36*3600000L))throw new JSONException("Invalid manual record");
        }
        for(int i=0;i<epochs.length();i++){
            JSONObject e=epochs.getJSONObject(i);if(!ids.contains(e.getLong("session"))||e.getLong("time")<=0||(!e.isNull("start")&&(e.getLong("start")<=0||e.getLong("start")>e.getLong("time"))))throw new JSONException("Invalid sensor record");
            if(format>=3&&byId.get(e.getLong("session")).getString("source").equals("manual"))throw new JSONException("Manual records cannot contain sensor measurements");
            for(String k:new String[]{"movement","noise","light"})if(!e.isNull(k)&&(!Double.isFinite(e.getDouble(k))||e.getDouble(k)<0))throw new JSONException("Invalid sensor value");
        }
        if(format>=2){
            JSONArray pauses=root.getJSONArray("pauses");Set<Long> pauseIds=new HashSet<>();Map<Long,Long> previousEnd=new HashMap<>();List<JSONObject> sorted=new ArrayList<>();
            for(int i=0;i<pauses.length();i++)sorted.add(pauses.getJSONObject(i));sorted.sort(Comparator.comparingLong(p->p.optLong("start")));
            for(JSONObject p:sorted){long id=p.getLong("id"),session=p.getLong("session"),from=p.getLong("start"),to=p.isNull("end")?Long.MAX_VALUE:p.getLong("end");JSONObject s=byId.get(session);
                if(id<=0||!pauseIds.add(id)||s==null||from<s.getLong("start")||to<from||(!s.isNull("end")&&(p.isNull("end")||to>s.getLong("end")))||from<previousEnd.getOrDefault(session,Long.MIN_VALUE))throw new JSONException("Invalid pause interval");previousEnd.put(session,to);
                if(format>=3&&s.getString("source").equals("manual"))throw new JSONException("Manual records cannot contain tracking pauses");
            }
        }
        if(format>=3){Set<Long> edits=new HashSet<>();JSONArray overlays=root.getJSONArray("edits");for(int i=0;i<overlays.length();i++){JSONObject e=overlays.getJSONObject(i);long id=e.getLong("session");JSONObject s=byId.get(id);if(!edits.add(id)||s==null||s.isNull("end")||e.getLong("updated")<=0||e.optInt("reverted",0)<0||e.optInt("reverted",0)>1)throw new JSONException("Invalid record edit");if(s.getString("source").equals("manual")&&e.optInt("reverted",0)!=0)throw new JSONException("Manual records cannot be reverted");try{Records.draft(e).validate(s.getLong("start"),s.getLong("end"),s.getString("source").equals("manual"),System.currentTimeMillis());}catch(IllegalArgumentException bad){throw new JSONException(bad.getMessage());}}for(JSONObject s:byId.values())if(s.getString("source").equals("manual")&&!edits.contains(s.getLong("id")))throw new JSONException("Manual record details are missing");}
        if(format>=4)SoundEventBackup.validate(root,byId);
        if(format>=5)SensorBackup.validate(root,byId);
        if(format>=6)SoundAnalysisProvenance.validateExport(root);
        if(format>=7)HealthSleepCodec.validate(root);
        if(format>=8)AwakeBackup.validate(root);if(format>=9)EchoPeriods.validate(root);if(format>=10)PairMotionBackup.validate(root,byId);if(format>=11)PirStore.validate(root);if(format>=12)DreamCueBackup.validate(root,byId);
    }
    static void restore(Context c,Store store,JSONObject root) throws JSONException {
        if(TrackingService.running||Prefs.flag(c,"ringing"))throw new JSONException("End tracking and alarms before restoring.");
        boolean weeklyReplacement=Records.factory==Records.PRODUCTION&&"sleepwell.db".equals(store.getDatabaseName());if(weeklyReplacement)NapProfilesWeekly.replacing(c);
        try{
        List<Alarm> previous=store.alarms();List<JSONObject> clips=store.rows("SELECT path FROM clips");restoreData(store,root);if(weeklyReplacement)NapProfilesWeekly.replaced(c);
        for(Alarm a:previous){Naps.cancelJob(c,a);Scheduler.cancel(c,a.id,Scheduler.FIRE);Scheduler.cancel(c,a.id,Scheduler.SNOOZE);Scheduler.cancel(c,a.id,Scheduler.CHECK);Scheduler.cancel(c,a.id,Scheduler.BACKUP);}
        Scheduler.cancel(c,-1,Scheduler.TEST);Prefs.put(c,"checkPending",0);for(JSONObject clip:clips)FileSafety.deleteClip(c,clip.optString("path"));
        }finally{if(weeklyReplacement)NapProfilesWeekly.replacementFinished();}
    }
    static void restoreData(Store store,JSONObject root) throws JSONException {
        restoreData(store,root,()->{});
    }
    /** Guard is run under the replacement transaction, before mutation, between rows and before commit. */
    static void restoreData(Store store,JSONObject root,Runnable guard) throws JSONException {
        validate(root);SQLiteDatabase db=store.getWritableDatabase();
        int format=root.getInt("format");Map<Long,Long> endpoints=new HashMap<>();Set<Long> unfinished=new HashSet<>();JSONArray sessions=root.getJSONArray("sessions"),epochs=root.getJSONArray("epochs"),pauses=format>=2?root.getJSONArray("pauses"):null;
        for(int i=0;i<sessions.length();i++){JSONObject s=sessions.getJSONObject(i);endpoints.put(s.getLong("id"),s.isNull("end")?s.getLong("start"):s.getLong("end"));if(s.isNull("end"))unfinished.add(s.getLong("id"));}
        for(int i=0;i<epochs.length();i++){JSONObject e=epochs.getJSONObject(i);long id=e.getLong("session");if(unfinished.contains(id))endpoints.put(id,Math.max(endpoints.get(id),e.getLong("time")));}
        if(pauses!=null)for(int i=0;i<pauses.length();i++){JSONObject p=pauses.getJSONObject(i);long id=p.getLong("session");endpoints.put(id,Math.max(endpoints.get(id),p.isNull("end")?p.getLong("start"):p.getLong("end")));}
        if(format>=8)for(int n=0;n<root.getJSONArray("awake_estimates").length();n++){JSONObject e=root.getJSONArray("awake_estimates").getJSONObject(n);long id=e.getLong("session");if(unfinished.contains(id))endpoints.put(id,Math.max(endpoints.get(id),e.getLong("end")));}
        if(format>=9)for(int n=0;n<root.getJSONArray("echo_periods").length();n++){JSONObject e=root.getJSONArray("echo_periods").getJSONObject(n);long id=e.getLong("session");if(unfinished.contains(id))endpoints.put(id,Math.max(endpoints.get(id),e.getLong("end")));}
        if(format>=11)for(int n=0;n<root.getJSONArray("pir_samples").length();n++){JSONObject e=root.getJSONArray("pir_samples").getJSONObject(n);long id=e.getLong("session");if(unfinished.contains(id))endpoints.put(id,Math.max(endpoints.get(id),e.getLong("time")));}
        db.beginTransaction();try {
            guard.run();AwakeStore.erase(db);HealthSleepTables.restored(db);
            for(JSONObject clip:store.rows("SELECT * FROM clips")){guard.run();SoundClips.stage(store,clip);}db.delete("sound_events",null,null);db.delete("record_create_requests",null,null);db.delete("alarms",null,null);db.delete("epochs",null,null);db.delete("tracking_pauses",null,null);db.delete("record_edits",null,null);db.delete("sessions",null,null);db.delete("clips",null,null);
            JSONArray alarms=root.getJSONArray("alarms");for(int i=0;i<alarms.length();i++){guard.run();JSONObject portable=new JSONObject(alarms.getJSONObject(i).toString());portable.remove("weeklyNapOwner");Alarm a=Alarm.read(portable);a.enabled=false;AlarmSkips.clear(a);Naps.restored(a);if(a.tone.startsWith("content:"))a.tone="daybreak";store.save(a);}
            for(int i=0;i<sessions.length();i++){
                guard.run();
                JSONObject j=sessions.getJSONObject(i);ContentValues v=new ContentValues();long id=j.getLong("id");v.put("id",id);v.put("start",j.getLong("start"));v.put("end",endpoints.get(id));v.put("note",j.optString("note",""));v.put("rating",Math.max(0,Math.min(5,j.optInt("rating"))));v.put("interrupted",j.isNull("end")?1:j.optInt("interrupted"));v.put("clock_changed",j.optInt("clock_changed",0)==0?0:1);if(!j.isNull("active_ms"))v.put("active_ms",j.getLong("active_ms"));db.insertOrThrow("sessions",null,v);
                ContentValues provenance=new ContentValues();provenance.put("source",format>=3?j.getString("source"):"legacy");provenance.put("origin","imported");provenance.put("zone",format>=3?j.getString("zone"):"");provenance.put("sensor",format>=5?j.getString("sensor"):format>=3&&j.getString("source").equals("manual")?"none":"unspecified");provenance.put("sensor_config",format>=5?j.getString("sensor_config"):"");provenance.put("sound_analysis",format>=6?(j.isNull("end")?SoundAnalysisProvenance.recovered(j.getString("sound_analysis")):j.getString("sound_analysis")):"");provenance.put("health_connect",format>=7?j.getString("health_connect"):"");db.update("sessions",provenance,"id=?",new String[]{""+id});
            }
            for(int i=0;i<epochs.length();i++){guard.run();JSONObject e=epochs.getJSONObject(i);if(format>=5){SensorBackup.restoreEpoch(store,e);continue;}store.epoch(e.getLong("session"),e.isNull("start")?null:e.getLong("start"),e.getLong("time"),e.isNull("movement")?null:e.getDouble("movement"),e.isNull("noise")?null:e.getDouble("noise"),e.isNull("light")?null:e.getDouble("light"),e.optInt("samples",0));}
            if(pauses!=null)for(int i=0;i<pauses.length();i++){guard.run();JSONObject p=pauses.getJSONObject(i);ContentValues v=new ContentValues();v.put("id",p.getLong("id"));v.put("session",p.getLong("session"));v.put("start",p.getLong("start"));v.put("end",p.isNull("end")?endpoints.get(p.getLong("session")):p.getLong("end"));db.insertOrThrow("tracking_pauses",null,v);}
            if(format>=3){JSONArray edits=root.getJSONArray("edits");for(int i=0;i<edits.length();i++){guard.run();JSONObject edit=edits.getJSONObject(i);ContentValues v=Records.values(edit.getLong("session"),Records.draft(edit),edit.getLong("updated"));v.put("reverted",edit.optInt("reverted",0));db.insertOrThrow("record_edits",null,v);}}
            if(format>=8)AwakeBackup.restore(store,root,guard);if(format>=9)EchoPeriods.restore(store,root,guard);if(format>=10)PairMotionBackup.restore(store,root.getJSONObject("paired_motion"),guard);if(format>=11)PirStore.restore(store,root,guard);if(format>=12)DreamCueBackup.restore(store,root.getJSONObject("dream_cues"),guard);
            if(format>=4)SoundEventBackup.restore(store,root.getJSONArray("sound_events"),guard);
            guard.run();
            db.setTransactionSuccessful();
        }finally {db.endTransaction();}
    }
}
