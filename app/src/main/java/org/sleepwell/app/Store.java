package org.sleepwell.app;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import org.json.*;
import java.util.*;

public final class Store extends SQLiteOpenHelper {
    public Store(Context c) { super(c,"sleepwell.db",null,1); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE alarms(id INTEGER PRIMARY KEY, data TEXT NOT NULL)");
        db.execSQL("CREATE TABLE sessions(id INTEGER PRIMARY KEY, start INTEGER NOT NULL, end INTEGER, note TEXT DEFAULT '', rating INTEGER DEFAULT 0, interrupted INTEGER DEFAULT 0)");
        db.execSQL("CREATE TABLE epochs(id INTEGER PRIMARY KEY, session INTEGER NOT NULL, time INTEGER NOT NULL, movement REAL, noise REAL, light REAL, samples INTEGER)");
        db.execSQL("CREATE INDEX epochs_session ON epochs(session,time)");
        db.execSQL("CREATE TABLE clips(id INTEGER PRIMARY KEY, session INTEGER NOT NULL, time INTEGER NOT NULL, path TEXT NOT NULL, level REAL)");
    }
    @Override public void onUpgrade(SQLiteDatabase db,int old,int next) { throw new IllegalStateException("Migration required"); }
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
        ContentValues interrupted=new ContentValues(); interrupted.put("end",now); interrupted.put("interrupted",1);
        db.update("sessions",interrupted,"end IS NULL",null);
        ContentValues v=new ContentValues(); v.put("start",now); return db.insertOrThrow("sessions",null,v);
    }
    public void end(long id, boolean interrupted) { ContentValues v=new ContentValues(); v.put("end",System.currentTimeMillis()); v.put("interrupted",interrupted?1:0); getWritableDatabase().update("sessions",v,"id=?",new String[]{""+id}); }
    public void annotate(long id,String note,int rating) { ContentValues v=new ContentValues(); v.put("note",note); v.put("rating",rating); getWritableDatabase().update("sessions",v,"id=?",new String[]{""+id}); }
    public void epoch(long session,long time,double movement,double noise,double light,int samples) {
        ContentValues v=new ContentValues(); v.put("session",session); v.put("time",time); v.put("movement",movement); v.put("noise",noise); v.put("light",light); v.put("samples",samples); getWritableDatabase().insertOrThrow("epochs",null,v);
    }
    public void clip(long session,String path,double level) { ContentValues v=new ContentValues(); v.put("session",session); v.put("time",System.currentTimeMillis()); v.put("path",path); v.put("level",level); getWritableDatabase().insertOrThrow("clips",null,v); }
    public List<JSONObject> rows(String sql,String...args) {
        List<JSONObject> rows=new ArrayList<>();
        try(Cursor c=getReadableDatabase().rawQuery(sql,args)) {
            while(c.moveToNext()) { JSONObject j=new JSONObject(); for(int i=0;i<c.getColumnCount();i++) try { j.put(c.getColumnName(i), c.isNull(i)?JSONObject.NULL:(c.getType(i)==Cursor.FIELD_TYPE_STRING?c.getString(i):c.getDouble(i))); } catch(JSONException ignored) {} rows.add(j); }
        } return rows;
    }
    public JSONObject backup() {
        JSONObject root=new JSONObject(); JSONArray savedAlarms=new JSONArray();for(Alarm a:alarms())savedAlarms.put(a.json());try { root.put("format",1).put("alarms",savedAlarms)
            .put("sessions",new JSONArray(rows("SELECT * FROM sessions"))).put("epochs",new JSONArray(rows("SELECT * FROM epochs"))); } catch(JSONException e) { throw new IllegalStateException(e); } return root;
    }
    public void eraseHistory() { SQLiteDatabase db=getWritableDatabase(); db.beginTransaction(); try { db.delete("clips",null,null); db.delete("epochs",null,null); db.delete("sessions",null,null); db.setTransactionSuccessful(); } finally { db.endTransaction(); } }
}
