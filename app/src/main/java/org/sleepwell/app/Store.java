package org.sleepwell.app;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import org.json.*;
import java.util.*;

public class Store extends SQLiteOpenHelper {
    public Store(Context c) { this(c,"sleepwell.db"); }
    Store(Context c,String name) { super(c,name,null,5); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE alarms(id INTEGER PRIMARY KEY, data TEXT NOT NULL)");
        db.execSQL("CREATE TABLE sessions(id INTEGER PRIMARY KEY, start INTEGER NOT NULL, end INTEGER, note TEXT DEFAULT '', rating INTEGER DEFAULT 0, interrupted INTEGER DEFAULT 0, active_ms INTEGER, clock_changed INTEGER DEFAULT 0)");
        db.execSQL("CREATE TABLE epochs(id INTEGER PRIMARY KEY, session INTEGER NOT NULL, time INTEGER NOT NULL, movement REAL, noise REAL, light REAL, samples INTEGER, start INTEGER)");
        db.execSQL("CREATE INDEX epochs_session ON epochs(session,time)");
        db.execSQL("CREATE TABLE clips(id INTEGER PRIMARY KEY, session INTEGER NOT NULL, time INTEGER NOT NULL, path TEXT NOT NULL, level REAL)");
        createPauses(db);
        createRecords(db);db.execSQL("ALTER TABLE sessions ADD COLUMN source TEXT NOT NULL DEFAULT 'legacy'");db.execSQL("ALTER TABLE sessions ADD COLUMN origin TEXT NOT NULL DEFAULT 'legacy'");db.execSQL("ALTER TABLE sessions ADD COLUMN zone TEXT NOT NULL DEFAULT ''");
        createRequests(db);createSoundEvents(db);
    }
    private static void createSoundEvents(SQLiteDatabase db){
        db.execSQL("ALTER TABLE clips ADD COLUMN category TEXT NOT NULL DEFAULT 'noise'");
        db.execSQL("ALTER TABLE clips ADD COLUMN score REAL NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE clips ADD COLUMN duration_ms INTEGER NOT NULL DEFAULT 10000");
        db.execSQL("CREATE TABLE sound_events(id INTEGER PRIMARY KEY,session INTEGER NOT NULL,time INTEGER NOT NULL,duration_ms INTEGER NOT NULL,category TEXT NOT NULL,score REAL NOT NULL,model TEXT NOT NULL,clip INTEGER)");
        db.execSQL("CREATE INDEX sound_events_session ON sound_events(session,time)");
        db.execSQL("CREATE TRIGGER unlink_sound_clip AFTER DELETE ON clips BEGIN UPDATE sound_events SET clip=NULL WHERE clip=OLD.id; END");
    }
    long soundEvent(long session,long start,long duration,String category,double score,Long clip){
        if(session<=0||start<=0||duration<=0||!java.util.Set.of("snoring","speech","noise").contains(category)||!Double.isFinite(score)||score<0||score>1)throw new IllegalArgumentException("Invalid sound event");
        if(clip==null){List<JSONObject> saved=rows("SELECT id FROM clips WHERE session=? AND time-duration_ms<=? AND time>=? ORDER BY time LIMIT 1",""+session,""+start,""+(start+duration));if(!saved.isEmpty())clip=saved.get(0).optLong("id");}
        ContentValues v=new ContentValues();v.put("session",session);v.put("time",start);v.put("duration_ms",duration);v.put("category",category);v.put("score",score);v.put("model",YamnetClassifier.MODEL_ID);if(clip!=null)v.put("clip",clip);return getWritableDatabase().insertOrThrow("sound_events",null,v);
    }
    private static void createPauses(SQLiteDatabase db){db.execSQL("CREATE TABLE tracking_pauses(id INTEGER PRIMARY KEY, session INTEGER NOT NULL, start INTEGER NOT NULL, end INTEGER)");db.execSQL("CREATE INDEX pauses_session ON tracking_pauses(session,start)");}
    private static void createRequests(SQLiteDatabase db){db.execSQL("CREATE TABLE record_create_requests(token TEXT PRIMARY KEY,session INTEGER NOT NULL)");}
    private static void createRecords(SQLiteDatabase db){db.execSQL("CREATE TABLE record_edits(session INTEGER PRIMARY KEY,start INTEGER NOT NULL,end INTEGER NOT NULL,note TEXT NOT NULL DEFAULT '',tags TEXT NOT NULL DEFAULT '[]',awake TEXT NOT NULL DEFAULT '[]',rating INTEGER NOT NULL DEFAULT 0,rated INTEGER NOT NULL DEFAULT 0,updated INTEGER NOT NULL,reverted INTEGER NOT NULL DEFAULT 0)");}
    @Override public void onUpgrade(SQLiteDatabase db,int old,int next) {if(old<2){db.execSQL("ALTER TABLE sessions ADD COLUMN active_ms INTEGER");db.execSQL("ALTER TABLE sessions ADD COLUMN clock_changed INTEGER DEFAULT 0");db.execSQL("ALTER TABLE epochs ADD COLUMN start INTEGER");createPauses(db);}if(old<3){createRecords(db);db.execSQL("ALTER TABLE sessions ADD COLUMN source TEXT NOT NULL DEFAULT 'legacy'");db.execSQL("ALTER TABLE sessions ADD COLUMN origin TEXT NOT NULL DEFAULT 'legacy'");db.execSQL("ALTER TABLE sessions ADD COLUMN zone TEXT NOT NULL DEFAULT ''");}if(old<4)createRequests(db);if(old<5)createSoundEvents(db);if(next>5)throw new IllegalStateException("Unknown database version");}
    public synchronized List<Alarm> alarms() {
        List<Alarm> result=new ArrayList<>();
        try(Cursor c=getReadableDatabase().rawQuery("SELECT data FROM alarms ORDER BY id",null)) {
            while(c.moveToNext()) { try { result.add(Alarm.read(new JSONObject(c.getString(0)))); } catch(JSONException ignored) {} }
        } return result;
    }
    public Alarm alarm(int id) { for(Alarm a:alarms()) if(a.id==id) return a; return null; }
    public void save(Alarm a) { ContentValues v=new ContentValues(); v.put("id",a.id); v.put("data",a.json().toString()); getWritableDatabase().insertWithOnConflict("alarms",null,v,SQLiteDatabase.CONFLICT_REPLACE); }
    public void deleteAlarm(int id) { getWritableDatabase().delete("alarms","id=?",new String[]{""+id}); }
    public long begin() {
        SQLiteDatabase db=getWritableDatabase(); long now=System.currentTimeMillis();
        recoverInterrupted();ContentValues v=new ContentValues(); v.put("start",now);v.put("active_ms",0);v.put("source","tracked");v.put("origin","local");v.put("zone",java.time.ZoneId.systemDefault().getId());return db.insertOrThrow("sessions",null,v);
    }
    public void end(long id, boolean interrupted) { ContentValues v=new ContentValues(); v.put("end",System.currentTimeMillis()); v.put("interrupted",interrupted?1:0); getWritableDatabase().update("sessions",v,"id=?",new String[]{""+id}); }
    void end(long id,boolean interrupted,long active,long now){resume(id,now);ContentValues v=new ContentValues();v.put("end",now);v.put("active_ms",active);v.put("interrupted",interrupted?1:0);getWritableDatabase().update("sessions",v,"id=?",new String[]{""+id});}
    void clockChanged(long id){ContentValues v=new ContentValues();v.put("clock_changed",1);getWritableDatabase().update("sessions",v,"id=?",new String[]{""+id});}
    void active(long id,long elapsed){ContentValues v=new ContentValues();v.put("active_ms",Math.max(0,elapsed));getWritableDatabase().update("sessions",v,"id=?",new String[]{""+id});}
    void pause(long session,long now){if(!rows("SELECT id FROM tracking_pauses WHERE session=? AND end IS NULL",""+session).isEmpty())return;ContentValues v=new ContentValues();v.put("session",session);v.put("start",now);getWritableDatabase().insertOrThrow("tracking_pauses",null,v);}
    void resume(long session,long now){getWritableDatabase().execSQL("UPDATE tracking_pauses SET end=MAX(start,?) WHERE session=? AND end IS NULL",new Object[]{now,session});}
    void recoverInterrupted(){for(JSONObject s:rows("SELECT * FROM sessions WHERE end IS NULL")){long id=s.optLong("id"),last=s.optLong("start");for(JSONObject e:rows("SELECT MAX(time) AS time FROM epochs WHERE session=?",""+id))last=Math.max(last,e.optLong("time"));for(JSONObject p:rows("SELECT MAX(COALESCE(end,start)) AS time FROM tracking_pauses WHERE session=?",""+id))last=Math.max(last,p.optLong("time"));resume(id,last);ContentValues v=new ContentValues();v.put("end",last);v.put("interrupted",1);getWritableDatabase().update("sessions",v,"id=?",new String[]{""+id});}}
    long recordedMillis(JSONObject s){List<long[]> pauses=new ArrayList<>();long end=s.isNull("end")?System.currentTimeMillis():s.optLong("end");for(JSONObject p:rows("SELECT start,end FROM tracking_pauses WHERE session=?",""+s.optLong("id")))pauses.add(new long[]{p.optLong("start"),p.isNull("end")?end:p.optLong("end")});return SessionTiming.recorded(s.optLong("start"),end,s.isNull("active_ms")?null:s.optLong("active_ms"),pauses);}
    public void annotate(long id,String note,int rating) { ContentValues v=new ContentValues(); v.put("note",note); v.put("rating",rating); getWritableDatabase().update("sessions",v,"id=?",new String[]{""+id}); }
    public void epoch(long session,long time,Double movement,Double noise,Double light,int samples) {
        epoch(session,null,time,movement,noise,light,samples);
    }
    void epoch(long session,Long start,long time,Double movement,Double noise,Double light,int samples){ContentValues v=new ContentValues();v.put("session",session);v.put("start",start);v.put("time",time);v.put("movement",movement);v.put("noise",noise);v.put("light",light);v.put("samples",samples);getWritableDatabase().insertOrThrow("epochs",null,v);}
    void checkpoint(long session,Long start,long time,Double movement,Double noise,Double light,int samples,long elapsed,boolean measured){SQLiteDatabase db=getWritableDatabase();db.beginTransaction();try{if(measured)epoch(session,start,time,movement,noise,light,samples);active(session,elapsed);db.setTransactionSuccessful();}finally{db.endTransaction();}}
    public void clip(long session,long time,String path,double level) {clip(session,time,path,level,10000);}
    void clip(long session,long end,String path,double level,long duration){
        if(duration<=0)throw new IllegalArgumentException("Invalid recording duration");SQLiteDatabase db=getWritableDatabase();db.beginTransaction();try{
            ContentValues v=new ContentValues();v.put("session",session);v.put("time",end);v.put("path",path);v.put("level",level);v.put("duration_ms",duration);long id=db.insertOrThrow("clips",null,v);
            db.execSQL("UPDATE sound_events SET clip=? WHERE session=? AND clip IS NULL AND time>=? AND time+duration_ms<=?",new Object[]{id,session,end-duration,end});db.setTransactionSuccessful();
        }finally{db.endTransaction();}
    }
    public List<JSONObject> rows(String sql,String...args) {
        List<JSONObject> rows=new ArrayList<>();
        try(Cursor c=getReadableDatabase().rawQuery(sql,args)) {
            while(c.moveToNext()) { JSONObject j=new JSONObject(); for(int i=0;i<c.getColumnCount();i++) try { j.put(c.getColumnName(i), c.isNull(i)?JSONObject.NULL:(c.getType(i)==Cursor.FIELD_TYPE_STRING?c.getString(i):c.getDouble(i))); } catch(JSONException ignored) {} rows.add(j); }
        } return rows;
    }
    public JSONObject backup() {
        SQLiteDatabase db=getReadableDatabase();db.beginTransactionNonExclusive();try{JSONObject root=new JSONObject(); JSONArray savedAlarms=new JSONArray();for(Alarm a:alarms())savedAlarms.put(a.json());try { root.put("format",3).put("alarms",savedAlarms)
            .put("sessions",new JSONArray(rows("SELECT * FROM sessions"))).put("epochs",new JSONArray(rows("SELECT * FROM epochs"))).put("pauses",new JSONArray(rows("SELECT * FROM tracking_pauses"))).put("edits",new JSONArray(rows("SELECT * FROM record_edits"))); } catch(JSONException e) { throw new IllegalStateException(e); } db.setTransactionSuccessful();return root;}finally{db.endTransaction();}
    }
    public void eraseHistory() { SQLiteDatabase db=getWritableDatabase(); db.beginTransaction(); try { db.execSQL("UPDATE record_create_requests SET session=0");db.delete("sound_events",null,null);db.delete("clips",null,null); db.delete("epochs",null,null);db.delete("tracking_pauses",null,null);db.delete("record_edits",null,null); db.delete("sessions",null,null); db.setTransactionSuccessful(); } finally { db.endTransaction(); } }
}
