package org.sleepwell.app;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import org.json.JSONObject;
import java.util.Objects;

/** User overlays never overwrite model evidence and never invoke capture/cue code. */
final class SoundLabels {
    static String category(JSONObject event){String chosen=event.optString("user_category","");return SoundCategories.valid(chosen)?chosen:event.optString("category","noise");}
    static boolean manual(JSONObject event){return !event.isNull("user_category")&&SoundCategories.valid(event.optString("user_category"));}
    static String name(String value){switch(value){case "snoring":return "Snoring";case "speech":return "Speech";case "cough":return "Cough / throat clear";case "baby_cry":return "Baby crying";case "laughter":return "Laughter";default:return "Other sound";}}
    static String title(JSONObject event){return manual(event)?name(category(event))+" · Your label":SoundCategories.title(category(event));}
    static final class Snapshot {
        final JSONObject event;final long updated,start,end;
        Snapshot(JSONObject event,long updated,long start,long end){try{this.event=new JSONObject(event.toString());}catch(Exception e){throw new IllegalArgumentException("Unreadable sound event",e);}this.updated=updated;this.start=start;this.end=end;}
    }
    static void revision(JSONObject actual,JSONObject expected){if(actual.optLong("label_revision")!=expected.optLong("label_revision")||!Objects.equals(actual.opt("user_category"),expected.opt("user_category")))throw new IllegalArgumentException("This label changed. Reopen the sound event.");}
    static JSONObject check(Store store,Snapshot expected){
        JSONObject actual=SoundClips.event(store,expected.event.optLong("session"),expected.event.optLong("id"));SoundClips.eventIdentity(actual,expected.event);revision(actual,expected.event);
        if(Double.compare(actual.optDouble("score"),expected.event.optDouble("score"))!=0||!sameClip(actual,expected.event))throw new IllegalArgumentException("This sound event changed. Reopen it.");
        RecordEntry record=Records.get(store,actual.optLong("session"));long from=actual.optLong("time"),duration=actual.optLong("duration_ms");
        if(record.manual()||record.updated!=expected.updated||record.edit.start!=expected.start||record.edit.end!=expected.end||duration<=0||Math.addExact(from,duration)<=record.edit.start||from>=record.edit.end)throw new IllegalArgumentException("The sleep period changed. Reopen the sound event.");return actual;
    }
    // Store.rows projects INTEGERs as doubles; JSON snapshots may parse the same ID as Integer/Long.
    private static boolean sameClip(JSONObject actual,JSONObject expected){if(!actual.has("clip")||!expected.has("clip"))return false;if(actual.isNull("clip")||expected.isNull("clip"))return actual.isNull("clip")&&expected.isNull("clip");Object first=actual.opt("clip"),second=expected.opt("clip");if(!(first instanceof Number)||!(second instanceof Number))return false;double a=((Number)first).doubleValue(),b=((Number)second).doubleValue();return Double.isFinite(a)&&Double.isFinite(b)&&a>0&&b>0&&a<=9007199254740991L&&b<=9007199254740991L&&a==Math.rint(a)&&b==Math.rint(b)&&((Number)first).longValue()==((Number)second).longValue();}
    // Store.rows projects numbers through JSON doubles: keep the CAS counter exactly portable.
    static void save(Store store,Snapshot expected,String selected){
        if(selected!=null&&!SoundCategories.valid(selected))throw new IllegalArgumentException("Choose a sound label.");
        synchronized(SoundClips.LOCK){SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();try{JSONObject actual=check(store,expected);long previous=actual.optLong("label_revision");if(previous<0||previous>=9007199254740991L)throw new IllegalArgumentException("Invalid label revision");ContentValues values=new ContentValues();if(selected==null)values.putNull("user_category");else values.put("user_category",selected);values.put("label_revision",previous+1);
            if(db.update("sound_events",values,"id=? AND session=? AND label_revision=?",new String[]{""+actual.optLong("id"),""+actual.optLong("session"),""+previous})!=1)throw new IllegalArgumentException("This label changed. Reopen the sound event.");db.setTransactionSuccessful();}finally{db.endTransaction();}}
    }
    private SoundLabels(){}
}
