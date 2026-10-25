package org.sleepwell.app;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import org.json.*;
import java.util.*;

/** Additive measurement tables. Never owns a recorder, radio, permission or alarm. */
final class PairMotionTables {
    static final Set<String> PACKET_QUALITY=Set.of("ALIGNED","UNALIGNED","STALE","MISSING");
    static final Set<String> WINDOW_QUALITY=Set.of("RAW_ONLY","WAITING_SYNC","PEER_MISSING","FRESH_RAW","COMMON_REDUCED","LOW_COVERAGE","TIMING_LOST");
    static final Set<String> CLOSE=Set.of("USER","PAUSED","ENDED","DISCONNECTED","ERROR");
    static void create(SQLiteDatabase db){
        db.execSQL("CREATE TABLE IF NOT EXISTS pair_links(link TEXT PRIMARY KEY,session INTEGER NOT NULL,record_start INTEGER NOT NULL,started INTEGER NOT NULL,ended INTEGER,local_stream TEXT NOT NULL,peer_stream TEXT NOT NULL,filter_requested INTEGER NOT NULL,protocol INTEGER NOT NULL,close_reason TEXT NOT NULL DEFAULT '')");
        db.execSQL("CREATE INDEX IF NOT EXISTS pair_links_session ON pair_links(session)");
        db.execSQL("CREATE TABLE IF NOT EXISTS pair_packets(link TEXT NOT NULL,seq INTEGER NOT NULL,remote_start INTEGER NOT NULL,remote_end INTEGER NOT NULL,received INTEGER NOT NULL,aligned_start INTEGER,aligned_end INTEGER,movement REAL,samples INTEGER NOT NULL,offset_ms INTEGER,uncertainty_ms INTEGER,quality TEXT NOT NULL,PRIMARY KEY(link,seq))");
        db.execSQL("CREATE TABLE IF NOT EXISTS pair_windows(link TEXT NOT NULL,start INTEGER NOT NULL,time INTEGER NOT NULL,own REAL,own_samples INTEGER NOT NULL,peer_seq INTEGER,peer REAL,filtered REAL,quality TEXT NOT NULL,uncertainty_ms INTEGER,PRIMARY KEY(link,start,time))");
        db.execSQL("CREATE TRIGGER IF NOT EXISTS pair_delete_session BEFORE DELETE ON sessions BEGIN DELETE FROM pair_packets WHERE link IN(SELECT link FROM pair_links WHERE session=OLD.id); DELETE FROM pair_windows WHERE link IN(SELECT link FROM pair_links WHERE session=OLD.id); DELETE FROM pair_links WHERE session=OLD.id; END");
    }
    static void start(Store store,JSONObject link)throws JSONException{SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();try{JSONObject night=owner(store,link,false);PairMotionBackup.link(link,night);db.insertOrThrow("pair_links",null,values(link));db.setTransactionSuccessful();}finally{db.endTransaction();}}
    static void packet(Store store,String id,JSONObject row)throws JSONException{write(store,id,"pair_packets",row);}
    static void window(Store store,String id,JSONObject row)throws JSONException{write(store,id,"pair_windows",row);}
    private static void write(Store store,String id,String table,JSONObject row)throws JSONException{SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();try{List<JSONObject> links=store.rows("SELECT * FROM pair_links WHERE link=?",id);if(links.size()!=1||!links.get(0).isNull("ended"))throw new IllegalStateException("The paired recording has ended.");JSONObject link=links.get(0),night=owner(store,link,true);if(!id.equals(row.getString("link")))throw new IllegalArgumentException("Paired link changed");if(table.equals("pair_packets"))PairMotionBackup.packet(row,link,night);else{Map<String,JSONObject> packets=new HashMap<>();if(!row.isNull("peer_seq")){String seq=row.get("peer_seq").toString();for(JSONObject peer:store.rows("SELECT * FROM pair_packets WHERE link=? AND seq=?",id,seq))packets.put(id+":"+peer.getLong("seq"),peer);}PairMotionBackup.window(row,link,night,packets);}db.insertOrThrow(table,null,values(row));db.setTransactionSuccessful();}finally{db.endTransaction();}}
    static void end(Store store,String id,long at,String reason)throws JSONException{if(!CLOSE.contains(reason))throw new IllegalArgumentException("Invalid pair close state");SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();try{List<JSONObject> links=store.rows("SELECT * FROM pair_links WHERE link=?",id);if(links.isEmpty())return;JSONObject link=links.get(0);owner(store,link,true);if(link.isNull("ended")){ContentValues value=new ContentValues();value.put("ended",Math.max(at,link.getLong("started")));value.put("close_reason",reason);db.update("pair_links",value,"link=? AND ended IS NULL",new String[]{id});}db.setTransactionSuccessful();}finally{db.endTransaction();}}
    private static JSONObject owner(Store store,JSONObject link,boolean allowClosed)throws JSONException{long session=link.getLong("session");List<JSONObject> rows=store.rows("SELECT * FROM sessions WHERE id=?",""+session);if(rows.size()!=1)throw new IllegalStateException("The original recording no longer exists.");JSONObject night=rows.get(0);if(night.getLong("start")!=link.getLong("record_start")||!night.optString("source").equals("tracked")||!night.optString("sensor").equals(SensorProvenance.MOTION)||!allowClosed&&!night.isNull("end"))throw new IllegalStateException("The original manual movement recording changed.");return night;}
    static ContentValues values(JSONObject row)throws JSONException{ContentValues values=new ContentValues();for(Iterator<String> keys=row.keys();keys.hasNext();){String key=keys.next();Object raw=row.get(key);if(raw==JSONObject.NULL)values.putNull(key);else if(raw instanceof Byte||raw instanceof Short||raw instanceof Integer||raw instanceof Long)values.put(key,((Number)raw).longValue());else if(raw instanceof Number)values.put(key,((Number)raw).doubleValue());else if(raw instanceof String)values.put(key,(String)raw);else throw new JSONException("Unsupported paired value");}return values;}
    private PairMotionTables(){}
}
