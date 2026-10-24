package org.sleepwell.app;
import android.database.sqlite.SQLiteDatabase;import android.content.ContentValues;import java.util.UUID;
final class HealthSleepTables {
 static final String[] TABLES={"health_imports","health_exports","health_state"};
 static void create(SQLiteDatabase db){db.execSQL("ALTER TABLE sessions ADD COLUMN health_connect TEXT NOT NULL DEFAULT ''");db.execSQL("CREATE TABLE health_imports(remote_id TEXT PRIMARY KEY,origin TEXT NOT NULL,session INTEGER NOT NULL DEFAULT 0,modified INTEGER NOT NULL DEFAULT 0,payload TEXT NOT NULL DEFAULT '',fingerprint TEXT NOT NULL DEFAULT '',status TEXT NOT NULL DEFAULT 'linked')");db.execSQL("CREATE TABLE health_exports(client_id TEXT PRIMARY KEY,session INTEGER NOT NULL,birth TEXT NOT NULL,version INTEGER NOT NULL,fingerprint TEXT NOT NULL,payload TEXT NOT NULL,remote_id TEXT NOT NULL DEFAULT '',acknowledged INTEGER NOT NULL DEFAULT 0)");db.execSQL("CREATE INDEX health_exports_session ON health_exports(session)");db.execSQL("CREATE TABLE health_state(key TEXT PRIMARY KEY,value TEXT NOT NULL)");resetEpoch(db);}
 static void resetEpoch(SQLiteDatabase db){db.delete("health_state",null,null);ContentValues v=new ContentValues();v.put("key","epoch");v.put("value",UUID.randomUUID().toString());db.insertOrThrow("health_state",null,v);}
 static void restored(SQLiteDatabase db){db.delete("health_imports",null,null);db.delete("health_exports",null,null);resetEpoch(db);}
 static void erased(SQLiteDatabase db){db.execSQL("UPDATE health_imports SET session=0,payload='',fingerprint='',status='hidden'");db.delete("health_exports",null,null);resetEpoch(db);}
 static void localDeleted(SQLiteDatabase db,long id){db.execSQL("UPDATE health_imports SET session=0,payload='',fingerprint='',status='hidden' WHERE session=?",new Object[]{id});}
 private HealthSleepTables(){}
}
