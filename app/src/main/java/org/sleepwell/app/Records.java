package org.sleepwell.app;

import android.content.*;
import android.database.sqlite.SQLiteDatabase;
import org.json.*;
import java.time.ZoneId;
import java.util.*;

/** Storage boundary for effective records. Tests inject an isolated named database. */
final class Records {
    interface Factory {Store open(Context context);}
    static Factory factory=Store::new;
    static Store open(Context context){return factory.open(context);}
    static RecordDraft draft(JSONObject value) throws JSONException {
        RecordDraft d=new RecordDraft(value.getLong("start"),value.getLong("end"));d.note=value.optString("note","");d.rating=value.optInt("rating",0);d.rated=value.optInt("rated",0)!=0;
        JSONArray tags=new JSONArray(value.optString("tags","[]")),awake=new JSONArray(value.optString("awake","[]"));
        for(int i=0;i<tags.length();i++)d.tags.add(tags.getString(i));for(int i=0;i<awake.length();i++){JSONArray s=awake.getJSONArray(i);if(s.length()!=2)throw new JSONException("Invalid awake interval");d.awake.add(new RecordIntervals.Span(s.getLong(0),s.getLong(1)));}return d;
    }
    static ContentValues values(long id,RecordDraft d,long updated){
        ContentValues v=new ContentValues();v.put("session",id);v.put("start",d.start);v.put("end",d.end);v.put("note",d.note);v.put("rating",d.rating);v.put("rated",d.rated?1:0);v.put("updated",updated);v.put("reverted",0);
        v.put("tags",new JSONArray(d.tags).toString());JSONArray awake=new JSONArray();for(RecordIntervals.Span s:d.awake)awake.put(new JSONArray().put(s.start).put(s.end));v.put("awake",awake.toString());return v;
    }
    static RecordEntry entry(JSONObject s,JSONObject overlay,List<RecordIntervals.Span> pauses){
        if(s.isNull("end"))throw new IllegalArgumentException("End the recording before editing it.");
        try{RecordDraft d;if(overlay==null||overlay.optInt("reverted",0)!=0){d=new RecordDraft(s.getLong("start"),s.getLong("end"));d.note=s.optString("note","");d.rating=s.optInt("rating",0);d.rated=d.rating>0;java.util.regex.Matcher tags=java.util.regex.Pattern.compile("(?<!\\S)#([^\\s#]+)").matcher(d.note);while(tags.find()&&d.tags.size()<24)try{d.addTag(tags.group(1));}catch(IllegalArgumentException ignored){}}else d=draft(overlay);
            return new RecordEntry(s.getLong("id"),s.getLong("start"),s.getLong("end"),s.isNull("active_ms")?null:s.getLong("active_ms"),s.optString("source","legacy"),s.optString("origin","legacy"),s.optString("zone",""),s.optInt("interrupted"),s.optInt("clock_changed"),d,overlay!=null&&overlay.optInt("reverted",0)==0,overlay==null?0:overlay.getLong("updated"),pauses);
        }catch(JSONException e){throw new IllegalArgumentException("Record metadata is unreadable.",e);}
    }
    static List<RecordEntry> all(Store store){
        SQLiteDatabase db=store.getReadableDatabase();db.beginTransactionNonExclusive();try{
            Map<Long,JSONObject> edits=new HashMap<>();for(JSONObject e:store.rows("SELECT * FROM record_edits"))edits.put(e.optLong("session"),e);
            Map<Long,List<RecordIntervals.Span>> pauses=new HashMap<>();for(JSONObject p:store.rows("SELECT * FROM tracking_pauses WHERE end IS NOT NULL"))pauses.computeIfAbsent(p.optLong("session"),k->new ArrayList<>()).add(new RecordIntervals.Span(p.optLong("start"),p.optLong("end")));
            List<RecordEntry> out=new ArrayList<>();for(JSONObject s:store.rows("SELECT * FROM sessions WHERE end IS NOT NULL ORDER BY start DESC"))out.add(entry(s,edits.get(s.optLong("id")),pauses.getOrDefault(s.optLong("id"),List.of())));
            out.sort(Comparator.comparingLong((RecordEntry r)->r.edit.start).reversed());db.setTransactionSuccessful();return out;
        }finally{db.endTransaction();}
    }
    static RecordEntry get(Store store,long id){List<JSONObject> rows=store.rows("SELECT * FROM sessions WHERE id=?",""+id);if(rows.isEmpty())throw new IllegalArgumentException("This record no longer exists.");List<JSONObject> overlays=store.rows("SELECT * FROM record_edits WHERE session=?",""+id);List<RecordIntervals.Span> pauses=new ArrayList<>();for(JSONObject p:store.rows("SELECT start,end FROM tracking_pauses WHERE session=? AND end IS NOT NULL",""+id))pauses.add(new RecordIntervals.Span(p.optLong("start"),p.optLong("end")));return entry(rows.get(0),overlays.isEmpty()?null:overlays.get(0),pauses);}
    static RecordEntry latest(Store store){SQLiteDatabase db=store.getReadableDatabase();db.beginTransactionNonExclusive();try{List<JSONObject> rows=store.rows("SELECT s.id FROM sessions s LEFT JOIN record_edits e ON e.session=s.id WHERE s.end IS NOT NULL ORDER BY CASE WHEN e.reverted=0 THEN e.start ELSE s.start END DESC LIMIT 1");RecordEntry result=rows.isEmpty()?null:get(store,rows.get(0).optLong("id"));db.setTransactionSuccessful();return result;}finally{db.endTransaction();}}
    static List<JSONObject> window(List<JSONObject> epochs,long start,long end){List<JSONObject> visible=new ArrayList<>();for(JSONObject epoch:epochs){long to=epoch.optLong("time"),from=epoch.isNull("start")?to-60000:epoch.optLong("start");if(to<=start||from>=end)continue;try{JSONObject copy=new JSONObject(epoch.toString());copy.put("start",Math.max(start,from)).put("time",Math.min(end,to));visible.add(copy);}catch(JSONException e){throw new IllegalArgumentException("Sensor record is unreadable.",e);}}return visible;}
    static String csv(Store store){StringBuilder out=new StringBuilder("start_iso,end_iso,duration_minutes,interrupted,rating,notes,source,origin,edited,zone,original_start_iso,original_end_iso,tags,awake_periods_utc\r\n");for(RecordEntry r:all(store)){out.append(java.time.Instant.ofEpochMilli(r.edit.start)).append(',').append(java.time.Instant.ofEpochMilli(r.edit.end)).append(',').append(r.duration()/60000).append(',').append(r.interrupted).append(',').append(r.edit.rated?r.edit.rating:"").append(',').append(SleepMath.csv(r.edit.note)).append(',').append(r.source).append(',').append(r.origin).append(',').append(r.edited).append(',').append(SleepMath.csv(r.zone)).append(',').append(java.time.Instant.ofEpochMilli(r.rawStart)).append(',').append(java.time.Instant.ofEpochMilli(r.rawEnd)).append(',').append(SleepMath.csv(new JSONArray(r.edit.tags).toString())).append(',');JSONArray awake=new JSONArray();for(RecordIntervals.Span s:r.edit.awake)awake.put(new JSONArray().put(java.time.Instant.ofEpochMilli(s.start).toString()).put(java.time.Instant.ofEpochMilli(s.end).toString()));out.append(SleepMath.csv(awake.toString())).append("\r\n");}for(JSONObject s:store.rows("SELECT * FROM sessions WHERE end IS NULL ORDER BY start"))out.append(java.time.Instant.ofEpochMilli(s.optLong("start"))).append(",,,,,").append(SleepMath.csv(s.optString("note",""))).append(',').append(s.optString("source","legacy")).append(',').append(s.optString("origin","legacy")).append(",false,").append(SleepMath.csv(s.optString("zone",""))).append(',').append(java.time.Instant.ofEpochMilli(s.optLong("start"))).append(",,[],[]\r\n");return out.toString();}
    static long create(Store store,RecordDraft draft,ZoneId zone,long now){
        return create(store,draft,zone,now,UUID.randomUUID().toString());
    }
    static long create(Store store,RecordDraft draft,ZoneId zone,long now,String requestToken){
        if(requestToken==null||requestToken.isEmpty())throw new IllegalArgumentException("Missing save request.");SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();try{
            List<JSONObject> prior=store.rows("SELECT session FROM record_create_requests WHERE token=?",requestToken);if(!prior.isEmpty()){long id=prior.get(0).optLong("session");get(store,id);db.setTransactionSuccessful();return id;}draft.validate(draft.start,draft.end,true,now);
            ContentValues v=new ContentValues();v.put("start",draft.start);v.put("end",draft.end);v.put("source","manual");v.put("origin","local");v.put("zone",zone.getId());long id=db.insertOrThrow("sessions",null,v);db.insertOrThrow("record_edits",null,values(id,draft,now));ContentValues request=new ContentValues();request.put("token",requestToken);request.put("session",id);db.insertOrThrow("record_create_requests",null,request);db.setTransactionSuccessful();return id;
        }finally{db.endTransaction();}
    }
    static void save(Store store,long id,RecordDraft draft,long expectedUpdated,long now){
        SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();try{RecordEntry original=get(store,id);if(original.updated!=expectedUpdated)throw new IllegalArgumentException("This record changed while it was open. Reopen it before saving.");draft.validate(original.rawStart,original.rawEnd,original.manual(),now);if(db.insertWithOnConflict("record_edits",null,values(id,draft,Math.max(now,original.updated+1)),SQLiteDatabase.CONFLICT_REPLACE)<0)throw new android.database.sqlite.SQLiteException("Could not save record changes.");db.setTransactionSuccessful();}finally{db.endTransaction();}
    }
    static void reset(Store store,long id,long expectedUpdated){SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();try{RecordEntry r=get(store,id);if(r.updated!=expectedUpdated)throw new IllegalArgumentException("This record changed. Reopen it first.");if(r.manual())throw new IllegalArgumentException("A manual entry has no measured original to restore.");List<JSONObject> raw=store.rows("SELECT * FROM sessions WHERE id=?",""+id);RecordDraft original=entry(raw.get(0),null,r.pauses).edit;ContentValues restored=values(id,original,Math.max(System.currentTimeMillis(),r.updated+1));restored.put("reverted",1);if(db.insertWithOnConflict("record_edits",null,restored,SQLiteDatabase.CONFLICT_REPLACE)<0)throw new android.database.sqlite.SQLiteException("Could not restore original record.");db.setTransactionSuccessful();}finally{db.endTransaction();}}
    static List<String> delete(Store store,long id,long expectedUpdated){
        SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();try{RecordEntry r=get(store,id);if(r.updated!=expectedUpdated)throw new IllegalArgumentException("This record changed. Reopen it first.");List<String> clips=new ArrayList<>();for(JSONObject clip:store.rows("SELECT path FROM clips WHERE session=?",""+id))clips.add(clip.optString("path"));for(String table:new String[]{"record_edits","clips","epochs","tracking_pauses"})db.delete(table,"session=?",new String[]{""+id});db.execSQL("UPDATE record_create_requests SET session=0 WHERE session=?",new Object[]{id});db.delete("sessions","id=?",new String[]{""+id});db.setTransactionSuccessful();return clips;}finally{db.endTransaction();}
    }
    static JSONObject display(RecordEntry r){JSONObject s=new JSONObject();try{s.put("id",r.id).put("start",r.edit.start).put("end",r.edit.end).put("note",r.edit.note).put("rating",r.edit.rating).put("interrupted",r.interrupted).put("clock_changed",r.clockChanged).put("duration_limited",r.durationLimited()).put("source",r.source).put("origin",r.origin).put("edited",r.edited).put("zone",r.zone).put("provenance",r.provenance());}catch(JSONException impossible){throw new IllegalStateException(impossible);}return s;}
}
