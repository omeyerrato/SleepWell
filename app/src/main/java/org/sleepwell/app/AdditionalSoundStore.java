package org.sleepwell.app;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Separate, read-only derived estimates. No rows enter sound_events, cue dispatch or automations. */
final class AdditionalSoundStore {
    static final int MAX_WINDOWS = 4320;
    static final String SOURCE_MOTION = "phone_microphone", SOURCE_SONAR = "contactless_filtered_microphone";
    static final Set<String> STATES = Set.of("pending", "active", "paused", "complete", "insufficient", "unavailable", "error", "interrupted");
    static void create(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE additional_sound_nights(session INTEGER PRIMARY KEY,raw_start INTEGER NOT NULL,token TEXT NOT NULL,source TEXT NOT NULL,model TEXT NOT NULL,policy TEXT NOT NULL,state TEXT NOT NULL,issue TEXT NOT NULL DEFAULT '',captured_samples INTEGER NOT NULL DEFAULT 0,analyzed_samples INTEGER NOT NULL DEFAULT 0,skipped_samples INTEGER NOT NULL DEFAULT 0,windows INTEGER NOT NULL DEFAULT 0,laughter INTEGER NOT NULL DEFAULT 0,instruments INTEGER NOT NULL DEFAULT 0,max_inference_ms INTEGER NOT NULL DEFAULT 0,max_java_heap_bytes INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE additional_sound_windows(session INTEGER NOT NULL,token TEXT NOT NULL,sequence INTEGER NOT NULL,segment TEXT NOT NULL,time INTEGER NOT NULL,duration_ms INTEGER NOT NULL,payload TEXT NOT NULL,inference_ms INTEGER NOT NULL,java_heap_bytes INTEGER NOT NULL,PRIMARY KEY(session,sequence))");
        db.execSQL("CREATE TRIGGER additional_sound_session_deleted AFTER DELETE ON sessions BEGIN DELETE FROM additional_sound_windows WHERE session=OLD.id; DELETE FROM additional_sound_nights WHERE session=OLD.id; END");
    }
    static final class Snapshot {
        final boolean requested;
        final String state, issue, source, modelVersion, policyVersion, token;
        final long session, rawStart, capturedMs, analyzedMs, skippedMs, maxInferenceMs, maxJavaHeapBytes;
        final int windowCount, laughterCount, instrumentCount;
        Snapshot(long id) {
            requested=false;session=id;rawStart=0;state="not_requested";
            issue="No additional sound analysis was requested for this recording, or its local derived results were not included in a restored backup.";
            source=modelVersion=policyVersion=token="";capturedMs=analyzedMs=skippedMs=maxInferenceMs=maxJavaHeapBytes=0;windowCount=laughterCount=instrumentCount=0;
        }
        Snapshot(JSONObject row, boolean ended) {
            requested=true;session=row.optLong("session");rawStart=row.optLong("raw_start");token=row.optString("token");
            String stored=row.optString("state");boolean lost=ended&&Set.of("pending","active","paused").contains(stored);
            state=lost?"interrupted":stored;issue=lost?"Additional analysis did not acknowledge completion; only saved windows are available.":row.optString("issue");
            source=row.optString("source");modelVersion=row.optString("model");policyVersion=row.optString("policy");
            capturedMs=row.optLong("captured_samples")/16;analyzedMs=row.optLong("analyzed_samples")/16;skippedMs=row.optLong("skipped_samples")/16;
            windowCount=row.optInt("windows");laughterCount=row.optInt("laughter");instrumentCount=row.optInt("instruments");
            maxInferenceMs=row.optLong("max_inference_ms");maxJavaHeapBytes=row.optLong("max_java_heap_bytes");
        }
    }
    static final class Window {
        final long sequence, start, durationMs, inferenceMs, javaHeapBytes;
        final String segment, candidate, instrumentLabel, modelVersion, policyVersion;
        final JSONObject estimates;
        Window(JSONObject row, Snapshot owner) {
            sequence=row.optLong("sequence");start=row.optLong("time");durationMs=row.optLong("duration_ms");
            inferenceMs=row.optLong("inference_ms");javaHeapBytes=row.optLong("java_heap_bytes");segment=row.optString("segment");
            try { estimates=new JSONObject(row.getString("payload")); }
            catch(Exception invalid){throw new IllegalStateException("Additional sound estimates are unreadable.",invalid);}
            candidate=estimates.optString("candidate","uncertain");instrumentLabel=estimates.optString("instrumentLabel");
            modelVersion=owner.modelVersion;policyVersion=owner.policyVersion;
        }
    }
    static Snapshot read(Store store,long session) {
        List<JSONObject> rows=store.rows("SELECT a.*,s.end FROM additional_sound_nights a JOIN sessions s ON s.id=a.session AND s.start=a.raw_start WHERE a.session=?",""+session);
        return rows.isEmpty()?new Snapshot(session):new Snapshot(rows.get(0),!rows.get(0).isNull("end"));
    }
    /** Pinned receipt is required; an old page can never retarget a reused record ID. */
    static List<Window> page(Store store,Snapshot expected,int offset,int limit) {
        if(expected==null||offset<0||offset>MAX_WINDOWS||limit<1||limit>20)throw new IllegalArgumentException("Choose a valid additional-sounds page.");
        SQLiteDatabase db=store.getReadableDatabase();db.beginTransactionNonExclusive();
        try {Snapshot now=read(store,expected.session);if(!now.requested||!now.token.equals(expected.token)||now.rawStart!=expected.rawStart)throw new IllegalStateException("This recording changed. Reopen additional sounds.");
            List<Window> out=new ArrayList<>();for(JSONObject row:store.rows("SELECT * FROM additional_sound_windows WHERE session=? AND token=? ORDER BY sequence LIMIT ? OFFSET ?",""+now.session,now.token,""+limit,""+offset))out.add(new Window(row,now));
            db.setTransactionSuccessful();return List.copyOf(out);
        }finally{db.endTransaction();}
    }
    static void begin(Store store,long session,long rawStart,String token,String source) {
        SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();try{
            List<JSONObject> rows=store.rows("SELECT start FROM sessions WHERE id=? AND end IS NULL AND source='tracked' AND origin='local'",""+session);
            if(rows.size()!=1||rows.get(0).optLong("start")!=rawStart)throw new IllegalStateException("Additional sound recording changed.");
            ContentValues v=new ContentValues();v.put("session",session);v.put("raw_start",rawStart);v.put("token",token);v.put("source",source);v.put("model",RecordedSoundAnalyzer.MODEL_VERSION);v.put("policy",RecordedSoundPolicy.VERSION);v.put("state","pending");
            db.insertOrThrow("additional_sound_nights",null,v);db.setTransactionSuccessful();
        }finally{db.endTransaction();}
    }
    private static void require(Store store,long session,long start,String token) {
        if(store.rows("SELECT a.session FROM additional_sound_nights a JOIN sessions s ON s.id=a.session AND s.start=a.raw_start WHERE a.session=? AND a.raw_start=? AND a.token=?",""+session,""+start,token).size()!=1)throw new IllegalStateException("Additional sound recording was deleted or replaced.");
    }
    static void status(Store store,long session,long start,String token,String state,String issue,long captured,long analyzed,long skipped) {
        if(!STATES.contains(state)||captured<0||analyzed<0||skipped<0||analyzed+skipped>captured)throw new IllegalArgumentException("Invalid additional sound coverage.");
        SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();try{require(store,session,start,token);ContentValues v=new ContentValues();v.put("state",state);v.put("issue",issue);v.put("captured_samples",captured);v.put("analyzed_samples",analyzed);v.put("skipped_samples",skipped);db.update("additional_sound_nights",v,"session=? AND token=?",new String[]{""+session,token});db.setTransactionSuccessful();}finally{db.endTransaction();}
    }
    static void append(Store store,long session,long start,String token,long sequence,String segment,long at,RecordedSoundAnalyzer.Window window,long inferenceMs,long heap) throws Exception {
        if(sequence<1||sequence>MAX_WINDOWS||at<start||window.durationMs!=10000||inferenceMs<0||heap<0)throw new IllegalArgumentException("Invalid additional sound window.");
        SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();try{require(store,session,start,token);ContentValues v=new ContentValues();v.put("session",session);v.put("token",token);v.put("sequence",sequence);v.put("segment",segment);v.put("time",at);v.put("duration_ms",window.durationMs);v.put("payload",window.json().toString());v.put("inference_ms",inferenceMs);v.put("java_heap_bytes",heap);db.insertOrThrow("additional_sound_windows",null,v);
            db.execSQL("UPDATE additional_sound_nights SET windows=windows+1,laughter=laughter+?,instruments=instruments+?,max_inference_ms=MAX(max_inference_ms,?),max_java_heap_bytes=MAX(max_java_heap_bytes,?) WHERE session=? AND token=?",new Object[]{window.candidate.equals("laughter")?1:0,window.candidate.equals("instrument")?1:0,inferenceMs,heap,session,token});db.setTransactionSuccessful();
        }finally{db.endTransaction();}
    }
    private AdditionalSoundStore() {}
}
