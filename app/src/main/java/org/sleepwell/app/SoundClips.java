package org.sleepwell.app;

import android.content.*;
import android.database.sqlite.SQLiteDatabase;
import org.json.JSONObject;
import java.io.*;
import java.util.*;

/** Durable identity-checked mutations; failed deletion remains visible and retryable. */
final class SoundClips {
    static final Object LOCK=new Object();
    static JSONObject get(Store store,long session,long id){List<JSONObject> rows=store.rows("SELECT c.* FROM clips c JOIN sessions s ON s.id=c.session WHERE c.id=? AND c.session=? AND s.end IS NOT NULL",""+id,""+session);if(rows.isEmpty())throw new IllegalArgumentException("This recording changed. Reopen sound events.");return rows.get(0);}
    static void identity(JSONObject actual,JSONObject expected){if(actual.optLong("id")!=expected.optLong("id")||actual.optLong("session")!=expected.optLong("session")||actual.optLong("time")!=expected.optLong("time")||!actual.optString("path").equals(expected.optString("path")))throw new IllegalArgumentException("This recording changed. Reopen sound events.");}
    static boolean pending(Store store,long id){return !store.rows("SELECT clip FROM pending_clip_deletes WHERE clip=?",""+id).isEmpty();}
    static void favorite(Store store,JSONObject expected,boolean value){synchronized(LOCK){JSONObject actual=get(store,expected.optLong("session"),expected.optLong("id"));identity(actual,expected);if(pending(store,actual.optLong("id")))throw new IllegalArgumentException("Finish the pending deletion first.");ContentValues values=new ContentValues();values.put("favorite",value?1:0);if(store.getWritableDatabase().update("clips",values,"id=? AND session=? AND path=? AND time=?",new String[]{""+actual.optLong("id"),""+actual.optLong("session"),actual.optString("path"),""+actual.optLong("time")})!=1)throw new IllegalArgumentException("Recording no longer available");}}
    static JSONObject event(Store store,long session,long id){List<JSONObject> rows=store.rows("SELECT e.* FROM sound_events e JOIN sessions s ON s.id=e.session WHERE e.id=? AND e.session=? AND s.end IS NOT NULL",""+id,""+session);if(rows.isEmpty())throw new IllegalArgumentException("This sound event changed. Reopen the night.");return rows.get(0);}
    static void eventIdentity(JSONObject actual,JSONObject expected){for(String name:new String[]{"id","session","time","duration_ms"})if(actual.optLong(name)!=expected.optLong(name))throw new IllegalArgumentException("This sound event changed. Reopen the night.");for(String name:new String[]{"category","model"})if(!actual.optString(name).equals(expected.optString(name)))throw new IllegalArgumentException("This sound event changed. Reopen the night.");}
    static void favoriteEvent(Store store,JSONObject expected,boolean favorite){synchronized(LOCK){JSONObject actual=event(store,expected.optLong("session"),expected.optLong("id"));eventIdentity(actual,expected);ContentValues values=new ContentValues();values.put("favorite",favorite?1:0);if(store.getWritableDatabase().update("sound_events",values,"id=? AND session=? AND time=? AND duration_ms=?",new String[]{""+actual.optLong("id"),""+actual.optLong("session"),""+actual.optLong("time"),""+actual.optLong("duration_ms")})!=1)throw new IllegalArgumentException("Sound event no longer available");}}
    static void deleteEvent(Store store,JSONObject expected){synchronized(LOCK){JSONObject actual=event(store,expected.optLong("session"),expected.optLong("id"));eventIdentity(actual,expected);store.getWritableDatabase().delete("sound_events","id=? AND session=? AND time=? AND duration_ms=?",new String[]{""+actual.optLong("id"),""+actual.optLong("session"),""+actual.optLong("time"),""+actual.optLong("duration_ms")});}}
    static void delete(Context context,Store store,JSONObject expected)throws IOException{
        synchronized(LOCK){JSONObject clip=get(store,expected.optLong("session"),expected.optLong("id"));identity(clip,expected);FileSafety.clip(context,clip.optString("path"));SQLiteDatabase db=store.getWritableDatabase();
            ContentValues intent=new ContentValues();intent.put("clip",clip.optLong("id"));intent.put("session",clip.optLong("session"));intent.put("path",clip.optString("path"));intent.put("time",clip.optLong("time"));
            // Commit intent before touching bytes: a crash cannot silently orphan retained audio.
            stage(store,clip);
            finish(context,store,intent.getAsLong("clip"));
        }
    }
    private static void finish(Context context,Store store,long id)throws IOException{
        List<JSONObject> pending=store.rows("SELECT * FROM pending_clip_deletes WHERE clip=?",""+id);if(pending.isEmpty())return;JSONObject intent=pending.get(0);
        List<JSONObject> rows=store.rows("SELECT * FROM clips WHERE id=?",""+id);
        boolean matching=!rows.isEmpty()&&rows.get(0).optLong("session")==intent.optLong("session")&&rows.get(0).optLong("time")==intent.optLong("time")&&rows.get(0).optString("path").equals(intent.optString("path"));
        store.recognitionSourceChanging(intent.optLong("session"),id);
        String path=intent.optString("path");File file=FileSafety.clip(context,path);
        // Legacy shared paths remain until their last reference is removed.
        if(store.rows("SELECT id FROM clips WHERE path=?"+(matching?" AND id!=?":""),matching?new String[]{path,""+id}:new String[]{path}).isEmpty()&&file.exists()&&!file.delete())throw new IOException("Deletion pending: the audio file could not be removed. Try again.");
        SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();try{db.delete("clips","id=? AND session=? AND path=? AND time=?",new String[]{""+id,""+intent.optLong("session"),path,""+intent.optLong("time")});db.delete("pending_clip_deletes","clip=?",new String[]{""+id});db.setTransactionSuccessful();}finally{db.endTransaction();}
    }
    static void stage(Store store,JSONObject clip){ContentValues intent=new ContentValues();intent.put("clip",clip.optLong("id"));intent.put("session",clip.optLong("session"));intent.put("path",clip.optString("path"));intent.put("time",clip.optLong("time"));List<JSONObject> prior=store.rows("SELECT * FROM pending_clip_deletes WHERE clip=?",""+clip.optLong("id"));if(!prior.isEmpty()){JSONObject p=prior.get(0);if(p.optLong("session")!=clip.optLong("session")||p.optLong("time")!=clip.optLong("time")||!p.optString("path").equals(clip.optString("path")))throw new IllegalArgumentException("Finish an earlier pending audio deletion before deleting this record.");}else store.getWritableDatabase().insertOrThrow("pending_clip_deletes",null,intent);}
    static void recover(Context context,Store store){synchronized(LOCK){for(JSONObject intent:store.rows("SELECT clip FROM pending_clip_deletes"))try{finish(context,store,intent.optLong("clip"));}catch(IOException|RuntimeException ignored){/* Visible pending row is retried, never relabelled as deleted. */}}}
    static boolean recoverSession(Context context,Store store,long session){synchronized(LOCK){for(JSONObject intent:store.rows("SELECT clip FROM pending_clip_deletes WHERE session=?",""+session))try{finish(context,store,intent.optLong("clip"));}catch(IOException|RuntimeException ignored){/* The owned journal remains retryable. */}return store.rows("SELECT clip FROM pending_clip_deletes WHERE session=?",""+session).isEmpty();}}
    static void prune(Context context,Store store,long oldest,boolean keepFavorites){synchronized(LOCK){recover(context,store);for(JSONObject clip:store.rows("SELECT c.* FROM clips c JOIN sessions s ON s.id=c.session WHERE c.time<? AND s.end IS NOT NULL"+(keepFavorites?" AND c.favorite=0 AND NOT EXISTS(SELECT 1 FROM sound_events e WHERE e.clip=c.id AND e.favorite=1)":""),""+oldest))try{delete(context,store,clip);}catch(IOException|RuntimeException e){Prefs.put(context,"audioRetentionError","Some expired recordings could not be deleted. Review sound events.");}}}
    static void stageAll(Store store,Long session){for(JSONObject clip:session==null?store.rows("SELECT * FROM clips"):store.rows("SELECT * FROM clips WHERE session=?",""+session))stage(store,clip);}
    static boolean eraseHistory(Context context,Store store){return eraseHistory(context,store,()->{});}
    static boolean eraseHistory(Context context,Store store,Runnable afterDatabaseErase){if(TrackingService.running)throw new IllegalStateException("End tracking before deleting history.");synchronized(LOCK){store.eraseHistory();afterDatabaseErase.run();recover(context,store);return store.rows("SELECT clip FROM pending_clip_deletes").isEmpty();}}
    static void prune(Context context,Store store){int days=Prefs.number(context,"audioRetentionDays",7);if(days==0){recover(context,store);return;}days=Math.max(1,Math.min(365,days));prune(context,store,System.currentTimeMillis()-days*86400000L,Prefs.of(context).getBoolean("keepAudioFavorites",true));}
}
