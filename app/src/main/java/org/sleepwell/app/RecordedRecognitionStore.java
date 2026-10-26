package org.sleepwell.app;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import org.json.*;
import java.util.*;

/** Independent bounded estimate summaries. No audio, URI grant, path or source label mutation. */
final class RecordedRecognitionStore extends SQLiteOpenHelper {
    static final Object LOCK=new Object();static final int MAX_RESULTS=50;
    RecordedRecognitionStore(Context c){super(c.getApplicationContext(),"recorded-sound-estimates-v1.db",null,1);}
    @Override public void onCreate(SQLiteDatabase db){db.execSQL("CREATE TABLE state (id INTEGER PRIMARY KEY CHECK(id=1),revision INTEGER NOT NULL)");db.execSQL("INSERT INTO state VALUES(1,0)");db.execSQL("CREATE TABLE results (id TEXT PRIMARY KEY,created INTEGER NOT NULL,source TEXT NOT NULL,result TEXT NOT NULL)");}
    @Override public void onUpgrade(SQLiteDatabase db,int old,int next){throw new IllegalStateException("Unsupported recorded-estimate database version.");}
    long revision(){try(Cursor c=getReadableDatabase().rawQuery("SELECT revision FROM state WHERE id=1",null)){if(!c.moveToFirst())throw new IllegalStateException("Estimate storage unavailable.");return c.getLong(0);}}
    static final class Entry {final String id;final long created;final JSONObject source,result;Entry(String id,long created,JSONObject source,JSONObject result){this.id=id;this.created=created;this.source=source;this.result=result;}}
    List<Entry> entries()throws JSONException {synchronized(LOCK){List<Entry> out=new ArrayList<>();try(Cursor c=getReadableDatabase().rawQuery("SELECT id,created,source,result FROM results ORDER BY created DESC,id LIMIT 50",null)){while(c.moveToNext())out.add(new Entry(c.getString(0),c.getLong(1),new JSONObject(c.getString(2)),new JSONObject(c.getString(3))));}return out;}}
    Entry save(long expected,JSONObject source,JSONObject result)throws Exception {synchronized(LOCK){String encoded=result.toString(),provenance=source.toString();if(encoded.length()>65536||provenance.length()>4096||result.getInt("schema")!=1)throw new IllegalArgumentException("The estimate result exceeds the supported format.");SQLiteDatabase db=getWritableDatabase();db.beginTransaction();try {
        if(revision()!=expected)throw new IllegalStateException("Saved estimates or sleep history changed. Start a new review.");try(Cursor c=db.rawQuery("SELECT COUNT(*) FROM results",null)){c.moveToFirst();if(c.getInt(0)>=MAX_RESULTS)throw new IllegalStateException("50 analyses are saved. Delete an old result before analyzing another file.");}
        Entry entry=new Entry(UUID.randomUUID().toString(),System.currentTimeMillis(),new JSONObject(provenance),new JSONObject(encoded));ContentValues v=new ContentValues();v.put("id",entry.id);v.put("created",entry.created);v.put("source",provenance);v.put("result",encoded);db.insertOrThrow("results",null,v);db.setTransactionSuccessful();return entry;
    }finally{db.endTransaction();}}}
    void delete(String id){synchronized(LOCK){getWritableDatabase().delete("results","id=?",new String[]{id});}}
    private void invalidate(){SQLiteDatabase db=getWritableDatabase();db.beginTransaction();try{long next=Math.addExact(revision(),1);ContentValues v=new ContentValues();v.put("revision",next);if(db.update("state",v,"id=1",null)!=1)throw new IllegalStateException("Estimate invalidation failed.");db.delete("results",null,null);db.setTransactionSuccessful();}finally{db.endTransaction();}}
    /** Root calls before replacement/erase; throws on persistence failure so that mutation can abort. */
    static void historyChanging(Context context){RecordedRecognitionJob.invalidateAll();synchronized(LOCK){try(RecordedRecognitionStore store=new RecordedRecognitionStore(context)){store.invalidate();}}}
    /** Before deleting a record or one retained clip. Nonmatching summaries are kept. */
    static void sourceChanging(Context context,long recordId,long clipId){if(recordId<=0)throw new IllegalArgumentException("Missing source record.");RecordedRecognitionJob.invalidateSource(recordId,clipId);synchronized(LOCK){try(RecordedRecognitionStore store=new RecordedRecognitionStore(context)){SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();try{ContentValues v=new ContentValues();v.put("revision",Math.addExact(store.revision(),1));if(db.update("state",v,"id=1",null)!=1)throw new IllegalStateException("Estimate invalidation failed.");for(Entry entry:store.entries())if(entry.source.optLong("record")==recordId&&(clipId<=0||entry.source.optLong("clip")==clipId))db.delete("results","id=?",new String[]{entry.id});db.setTransactionSuccessful();}catch(JSONException malformed){throw new IllegalStateException("Saved analysis metadata could not be cleared.",malformed);}finally{db.endTransaction();}}}}
    static void clear(Context context){historyChanging(context);}
}
