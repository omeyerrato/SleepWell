package org.sleepwell.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import org.json.JSONObject;
import java.util.*;
import java.util.function.BooleanSupplier;

/** Ends and conditionally removes one explicitly opted-in untouched automatic start, never another night. */
final class AutomaticFalseStartStore {
    static final class Identity {
        final long id,start;final String sensor,config,zone;
        Identity(Store store,long id){List<JSONObject> rows=store.rows("SELECT * FROM sessions WHERE id=?",""+id);if(rows.size()!=1)throw new IllegalStateException("Recording identity is unavailable.");JSONObject row=rows.get(0);if(!row.isNull("end")||!"tracked".equals(row.optString("source"))||!"local".equals(row.optString("origin"))||!SensorProvenance.MOTION.equals(row.optString("sensor")))throw new IllegalStateException("This is not a new phone-movement recording.");this.id=id;start=row.optLong("start");sensor=row.optString("sensor");config=row.optString("sensor_config");zone=row.optString("zone");}
        boolean matches(JSONObject row){return row.optLong("id")==id&&row.optLong("start")==start&&"tracked".equals(row.optString("source"))&&"local".equals(row.optString("origin"))&&sensor.equals(row.optString("sensor"))&&config.equals(row.optString("sensor_config"))&&zone.equals(row.optString("zone"))&&row.optString("health_connect","").isEmpty();}
    }
    static String finish(Context app,Store store,Identity expected,long end,long active,BooleanSupplier owned){
        return finish(app,store,expected,end,active,owned,null);
    }
    static String finish(Context app,Store store,Identity expected,long end,long active,BooleanSupplier owned,String soundAnalysis){
        if(expected==null)return "Possible awake start stopped. Its interrupted record was kept for review.";
        boolean removed=false;SQLiteDatabase db=store.getWritableDatabase();
        synchronized(SoundClips.LOCK){db.beginTransaction();try{
            List<JSONObject> rows=store.rows("SELECT * FROM sessions WHERE id=?",""+expected.id);
            if(rows.size()!=1||!expected.matches(rows.get(0))||!rows.get(0).isNull("end"))throw new IllegalStateException("Recording identity changed; no record was discarded.");
            JSONObject before=rows.get(0);
            String finalAnalysis=soundAnalysis;if(finalAnalysis==null){SoundAnalysisStatus saved=SoundAnalysisProvenance.read(before.optString("sound_analysis",""));if(saved!=null)finalAnalysis=SoundAnalysisProvenance.encode(saved.interrupted());}
            if(finalAnalysis==null)store.end(expected.id,true,active,Math.max(expected.start,end));else store.end(expected.id,true,active,Math.max(expected.start,end),finalAnalysis);
            JSONObject ended=store.rows("SELECT * FROM sessions WHERE id=?",""+expected.id).get(0);
            if(owned.getAsBoolean()&&!MicrophoneLease.occupied()&&end>=expected.start&&end-expected.start<AutomaticFalseStart.WINDOW&&active>=0&&active<AutomaticFalseStart.WINDOW&&before.optInt("clock_changed")==0&&expected.matches(ended)&&ended.optLong("end")==end&&ended.optInt("interrupted")==1&&untouched(store,expected.id,ended)){
                Records.delete(store,expected.id,0);if(!owned.getAsBoolean()||MicrophoneLease.occupied())throw new IllegalStateException("Recording ownership changed during cleanup; saved data was retained.");removed=true;
            }
            db.setTransactionSuccessful();
        }finally{db.endTransaction();}}
        if(!removed)return "Possible awake start stopped. Its interrupted record was kept because it changed or could not be safely discarded. Review it in Report.";
        boolean filesGone=SoundClips.recoverSession(app,store,expected.id);
        return filesGone?"Possible awake start discarded after sustained phone use. Automatic tracking will not restart tonight.":"Possible awake start discarded. Some sound files are pending deletion; review sound recording storage. Automatic tracking will not restart tonight.";
    }
    private static boolean none(Store store,String sql,long id){return store.rows(sql,""+id).isEmpty();}
    private static boolean untouched(Store store,long id,JSONObject row){
        if(!row.optString("note","").isEmpty()||row.optInt("rating")!=0)return false;
        try{SoundAnalysisProvenance.read(row.optString("sound_analysis",""));}catch(IllegalArgumentException unreadable){return false;}
        for(String table:new String[]{"record_edits","tracking_pauses","record_create_requests","health_imports","health_exports","pair_links","pir_runs","echo_runs","dream_cue_runs"})if(!none(store,"SELECT 1 FROM "+table+" WHERE session=? LIMIT 1",id))return false;
        return none(store,"SELECT 1 FROM clips WHERE session=? AND favorite!=0 LIMIT 1",id)
            &&none(store,"SELECT 1 FROM sound_events WHERE session=? AND (favorite!=0 OR user_category IS NOT NULL OR label_revision!=0) LIMIT 1",id)
            &&none(store,"SELECT 1 FROM awake_runs WHERE session=? AND (revision!=0 OR state NOT IN ('off','interrupted')) LIMIT 1",id)
            &&none(store,"SELECT 1 FROM awake_estimates WHERE session=? AND ignored!=0 LIMIT 1",id);
    }
}
