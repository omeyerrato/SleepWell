package org.sleepwell.app;

import android.content.ContentValues;
import org.json.*;
import java.util.*;

/** Portable analysis-only metadata: never audio paths, links or a playback authorization. */
final class SoundEventBackup {
    static final int MAX_EVENTS=100000,MAX_JSON=8*1024*1024;
    static JSONArray export(Store store){
        Map<Long,JSONObject> sessions=new HashMap<>();for(JSONObject night:store.rows("SELECT * FROM sessions WHERE end IS NOT NULL"))sessions.put(night.optLong("id"),night);
        JSONArray out=new JSONArray();for(JSONObject row:store.rows("SELECT e.id,e.session,e.time,e.duration_ms,e.category,e.score,e.model,e.favorite,e.user_category,e.label_revision FROM sound_events e JOIN sessions s ON s.id=e.session WHERE s.end IS NOT NULL ORDER BY e.id LIMIT 100001"))try{JSONObject night=sessions.get(row.optLong("session"));long end=Math.addExact(row.optLong("time"),row.optLong("duration_ms"));row.put("interval_scope",end>night.optLong("end")?"interrupted_tail":"raw_session");out.put(row);}catch(Exception invalid){throw new IllegalArgumentException("Saved sound metadata needs review before backup.",invalid);}
        try{validate(new JSONObject().put("sound_events",out),sessions);}catch(JSONException invalid){throw new IllegalArgumentException("Saved sound metadata needs review before backup: "+invalid.getMessage(),invalid);}return out;
    }
    static boolean oversized(JSONArray rows){return oversized(rows.toString());}
    static void checkPortableSize(JSONObject root)throws JSONException{if(oversized(root.toString(2)))throw new IllegalArgumentException("Backup exceeds the 8 MB import limit.");}
    private static boolean oversized(String encoded){if(encoded.length()>MAX_JSON)return true;long bytes=0;for(int n=0;n<encoded.length();n++){char value=encoded.charAt(n);if(value<=0x7f)bytes++;else if(value<=0x7ff)bytes+=2;else if(Character.isHighSurrogate(value)&&n+1<encoded.length()&&Character.isLowSurrogate(encoded.charAt(n+1))){bytes+=4;n++;}else bytes+=3;if(bytes>MAX_JSON)return true;}return false;}
    static long whole(JSONObject value,String key)throws JSONException{Object number=value.get(key);if(!(number instanceof Number))throw new JSONException("Invalid sound integer");double d=((Number)number).doubleValue();long n=((Number)number).longValue();if(!Double.isFinite(d)||d!=n||Math.abs(d)>9007199254740991d)throw new JSONException("Invalid sound integer");return n;}
    static void validate(JSONObject root,Map<Long,JSONObject> sessions)throws JSONException{
        JSONArray rows=root.getJSONArray("sound_events");if(rows.length()>MAX_EVENTS||oversized(rows))throw new JSONException("Sound metadata exceeds the backup limit");Set<Long> ids=new HashSet<>();
        for(int n=0;n<rows.length();n++){JSONObject row=rows.getJSONObject(n);for(Iterator<String> keys=row.keys();keys.hasNext();){String key=keys.next();if(!Set.of("id","session","time","duration_ms","category","score","model","favorite","user_category","label_revision","interval_scope").contains(key))throw new JSONException("Unexpected sound metadata");}
            long id=whole(row,"id"),session=whole(row,"session"),start=whole(row,"time"),duration=whole(row,"duration_ms"),revision=whole(row,"label_revision"),favorite=whole(row,"favorite");JSONObject night=sessions.get(session);
            if(id<=0||!ids.add(id)||night==null||night.isNull("end")||night.optString("source").equals("manual")||start<night.getLong("start")||duration<=0||duration>36*3600000L||start>Long.MAX_VALUE-duration||revision<0||favorite<0||favorite>1)throw new JSONException("Invalid sound interval or provenance");
            long interrupted=whole(night,"interrupted");boolean tail=start+duration>night.getLong("end");Object scope=row.get("interval_scope");if(interrupted<0||interrupted>1||!(scope instanceof String)||!scope.equals(tail?"interrupted_tail":"raw_session")||tail&&(interrupted!=1||start+duration-night.getLong("start")>36*3600000L))throw new JSONException("Invalid interrupted sound interval scope");
            Object category=row.get("category"),model=row.get("model"),score=row.get("score");if(!(category instanceof String)||!SoundCategories.valid((String)category)||!(model instanceof String)||((String)model).isBlank()||((String)model).length()>128||!(score instanceof Number)||!Double.isFinite(((Number)score).doubleValue())||((Number)score).doubleValue()<0||((Number)score).doubleValue()>1)throw new JSONException("Invalid model estimate");
            if(!row.has("user_category")||!row.isNull("user_category")&&(!(row.get("user_category") instanceof String)||!SoundCategories.valid(row.getString("user_category"))||revision<=0))throw new JSONException("Invalid user label");
        }
    }
    static void restore(Store store,JSONArray rows)throws JSONException{restore(store,rows,()->{});}
    static void restore(Store store,JSONArray rows,Runnable guard)throws JSONException{for(int n=0;n<rows.length();n++){guard.run();JSONObject row=rows.getJSONObject(n);ContentValues values=new ContentValues();for(String key:new String[]{"id","session","time","duration_ms","favorite","label_revision"})values.put(key,row.getLong(key));for(String key:new String[]{"category","model"})values.put(key,row.getString(key));values.put("score",row.getDouble("score"));if(!row.isNull("user_category"))values.put("user_category",row.getString("user_category"));values.putNull("clip");store.getWritableDatabase().insertOrThrow("sound_events",null,values);}}
    private SoundEventBackup(){}
}
