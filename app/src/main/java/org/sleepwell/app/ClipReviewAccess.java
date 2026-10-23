package org.sleepwell.app;

import java.io.IOException;
import org.json.JSONObject;

/** Worker-only point-in-time authorization of a selected interval, never a playback allocation. */
final class ClipReviewAccess {
    static void check(Store store,ClipTarget target)throws IOException {
        android.database.sqlite.SQLiteDatabase db=store.getReadableDatabase();db.beginTransactionNonExclusive();
        try {
            JSONObject actual=SoundClips.get(store,target.clip.optLong("session"),target.clip.optLong("id"));SoundClips.identity(actual,target.clip);
            if(SoundClips.pending(store,actual.optLong("id")))throw new IOException("Recording is pending deletion.");
            if(target.wholeDurationMs<=0||target.fromMs<0||target.durationMs<=0||Math.addExact(target.fromMs,target.durationMs)>target.wholeDurationMs)throw new IOException("The recording interval changed. Reopen this night.");
            long start=Math.addExact(Math.subtractExact(actual.optLong("time"),target.wholeDurationMs),target.fromMs),end=Math.addExact(start,target.durationMs);
            RecordEntry record=Records.get(store,actual.optLong("session"));
            if(record.manual()||start<record.edit.start||end>record.edit.end)throw new IOException("The sleep period changed. Reopen this night before playing audio.");
            if(target.event!=null){
                JSONObject event=SoundClips.event(store,target.event.optLong("session"),target.event.optLong("id"));SoundClips.eventIdentity(event,target.event);
                if(event.isNull("clip")||event.optLong("clip")!=actual.optLong("id"))throw new IOException("The event recording is no longer available.");
                if(start<event.optLong("time")||end>Math.addExact(event.optLong("time"),event.optLong("duration_ms")))throw new IOException("The event interval changed. Reopen this night.");
            }
            db.setTransactionSuccessful();
        }catch(ArithmeticException invalid){throw new IOException("The recording interval is invalid. Reopen this night.",invalid);}
        finally{db.endTransaction();}
    }
    private ClipReviewAccess(){}
}
