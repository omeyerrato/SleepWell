package org.sleepwell.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.json.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.*;
import static org.junit.Assert.*;

/** Temporary uniquely named databases only. No production rows, preferences or AlarmManager calls. */
@RunWith(AndroidJUnit4.class)
public class StorageMigrationTest {
    private Context context(){return InstrumentationRegistry.getInstrumentation().getTargetContext();}
    private String name(){return "sleepwell-verification-"+UUID.randomUUID()+".db";}
    @Test public void failedProgressWriteRollsBackTheInsertedEpochBeforeRetry() throws Exception {
        Context c=context();String name=name();try(Store store=new Store(c,name){boolean fail=true;@Override void active(long id,long elapsed){if(fail){fail=false;throw new android.database.sqlite.SQLiteDiskIOException("Injected after epoch insert");}super.active(id,elapsed);}}){
            long id=store.begin();long start=store.rows("SELECT start FROM sessions WHERE id=?",""+id).get(0).getLong("start");try{store.checkpoint(id,start,start+1000,.1,null,null,10,1000,true);fail("Checkpoint must fail");}catch(android.database.sqlite.SQLiteDiskIOException expected){}
            assertTrue(store.rows("SELECT * FROM epochs").isEmpty());assertEquals(0,store.rows("SELECT active_ms FROM sessions").get(0).getLong("active_ms"));store.checkpoint(id,start,start+1000,.1,null,null,10,1000,true);assertEquals(1,store.rows("SELECT * FROM epochs").size());assertEquals(1000,store.rows("SELECT active_ms FROM sessions").get(0).getLong("active_ms"));
        }finally{c.deleteDatabase(name);}
    }
    @Test public void legacyV1MigrationRetainsEveryExistingValue() throws Exception {
        Context c=context();String name=name();c.getDatabasePath(name).getParentFile().mkdirs();
        try{
            SQLiteDatabase old=SQLiteDatabase.openOrCreateDatabase(c.getDatabasePath(name),null);old.execSQL("CREATE TABLE alarms(id INTEGER PRIMARY KEY,data TEXT NOT NULL)");old.execSQL("CREATE TABLE sessions(id INTEGER PRIMARY KEY,start INTEGER NOT NULL,end INTEGER,note TEXT DEFAULT '',rating INTEGER DEFAULT 0,interrupted INTEGER DEFAULT 0)");old.execSQL("CREATE TABLE epochs(id INTEGER PRIMARY KEY,session INTEGER NOT NULL,time INTEGER NOT NULL,movement REAL,noise REAL,light REAL,samples INTEGER)");old.execSQL("CREATE INDEX epochs_session ON epochs(session,time)");old.execSQL("CREATE TABLE clips(id INTEGER PRIMARY KEY,session INTEGER NOT NULL,time INTEGER NOT NULL,path TEXT NOT NULL,level REAL)");Alarm alarm=new Alarm(91);String encoded=alarm.json().toString();old.execSQL("INSERT INTO alarms VALUES(?,?)",new Object[]{91,encoded});old.execSQL("INSERT INTO sessions VALUES(92,100000,220000,'legacy note',4,0)");old.execSQL("INSERT INTO epochs VALUES(93,92,160000,0.125,NULL,2.5,300)");old.setVersion(1);old.close();
            try(Store upgraded=new Store(c,name)){assertEquals(4,upgraded.getReadableDatabase().getVersion());assertEquals(encoded,upgraded.rows("SELECT data FROM alarms WHERE id=91").get(0).getString("data"));JSONObject session=upgraded.rows("SELECT * FROM sessions").get(0),epoch=upgraded.rows("SELECT * FROM epochs").get(0);assertEquals("legacy note",session.getString("note"));assertEquals(4,session.getInt("rating"));assertTrue(session.isNull("active_ms"));assertEquals(.125,epoch.getDouble("movement"),0);assertTrue(epoch.isNull("noise"));assertTrue(epoch.isNull("start"));assertEquals(120000,upgraded.recordedMillis(session));assertTrue(upgraded.rows("SELECT * FROM tracking_pauses").isEmpty());assertEquals("ok",upgraded.rows("PRAGMA quick_check").get(0).getString("quick_check"));Backup.validate(upgraded.backup());}
        }finally{assertTrue(name.startsWith("sleepwell-verification-"));c.deleteDatabase(name);}
    }
    @Test public void versionTwoBackupRoundTripsPausesAndExactEpochStarts() throws Exception {
        Context c=context();String source=name(),target=name();try(Store a=new Store(c,source);Store b=new Store(c,target)){
            a.getWritableDatabase().execSQL("INSERT INTO sessions(id,start,end,active_ms,note) VALUES(1,100000,300000,120000,'paused record')");a.getWritableDatabase().execSQL("INSERT INTO tracking_pauses(session,start,end) VALUES(1,120000,200000)");a.epoch(1,100000L,120000,0d,null,2d,50);a.epoch(1,200000L,300000,1d,null,3d,400);
            JSONObject backup=a.backup();assertEquals(3,backup.getInt("format"));Backup.restoreData(b,backup);assertEquals(120000,b.recordedMillis(b.rows("SELECT * FROM sessions").get(0)));assertEquals(200000,b.rows("SELECT start FROM epochs ORDER BY time DESC").get(0).getLong("start"));assertEquals(1,b.rows("SELECT * FROM tracking_pauses").size());
            JSONObject invalid=new JSONObject(backup.toString());invalid.getJSONArray("pauses").getJSONObject(0).put("end",110000);try{Backup.restoreData(b,invalid);fail("Invalid pause must reject before replacement");}catch(JSONException expected){}assertEquals("paused record",b.rows("SELECT note FROM sessions").get(0).getString("note"));
        }finally{c.deleteDatabase(source);c.deleteDatabase(target);}
    }
    @Test public void formatOneAndInterruptedPausedSessionsRemainReadable() throws Exception {
        Context c=context();String name=name();try(Store store=new Store(c,name)){
            JSONObject legacy=new JSONObject().put("format",1).put("alarms",new JSONArray()).put("sessions",new JSONArray().put(new JSONObject().put("id",1).put("start",100000).put("end",JSONObject.NULL))).put("epochs",new JSONArray().put(new JSONObject().put("session",1).put("time",160000).put("movement",0d)));
            Backup.restoreData(store,legacy);JSONObject imported=store.rows("SELECT * FROM sessions").get(0);assertEquals(160000,imported.getLong("end"));assertEquals(1,imported.getInt("interrupted"));assertTrue(imported.isNull("active_ms"));
            store.getWritableDatabase().execSQL("UPDATE sessions SET end=NULL,active_ms=60000 WHERE id=1");store.pause(1,160000);store.pause(1,170000);assertEquals(1,store.rows("SELECT * FROM tracking_pauses").size());store.recoverInterrupted();JSONObject recovered=store.rows("SELECT * FROM sessions").get(0);assertEquals(160000,recovered.getLong("end"));assertEquals(60000,store.recordedMillis(recovered));assertEquals(160000,store.rows("SELECT end FROM tracking_pauses").get(0).getLong("end"));
            // A process death immediately after resume still retains the closed pause's endpoint.
            store.getWritableDatabase().execSQL("DELETE FROM tracking_pauses");store.getWritableDatabase().execSQL("UPDATE sessions SET end=NULL WHERE id=1");store.pause(1,160000);store.resume(1,240000);store.clockChanged(1);store.recoverInterrupted();recovered=store.rows("SELECT * FROM sessions").get(0);assertEquals(240000,recovered.getLong("end"));assertEquals(1,recovered.getInt("clock_changed"));assertEquals(60000,store.recordedMillis(recovered));Backup.validate(store.backup());
        }finally{c.deleteDatabase(name);}
    }
}
