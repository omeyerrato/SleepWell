package org.sleepwell.app;

import android.database.sqlite.SQLiteDatabase;
import org.json.JSONObject;
import java.time.ZoneId;
import java.util.*;

/** Checked manual creation; overlap and idempotence checks share the insertion transaction. */
final class MissedNightRecords {
    static List<MissedNight.Interval> occupied(Store store){
        List<MissedNight.Interval> out=new ArrayList<>();for(RecordEntry record:Records.all(store))out.add(new MissedNight.Interval(record.edit.start,record.edit.end));
        for(JSONObject row:store.rows("SELECT start FROM sessions WHERE end IS NULL")){long start=row.optLong("start");if(start>0)out.add(new MissedNight.Interval(start,Long.MAX_VALUE));}return out;
    }
    static long prior(Store store,String token){
        List<JSONObject> rows=store.rows("SELECT session FROM record_create_requests WHERE token=?",token);if(rows.isEmpty())return 0;
        long id=rows.get(0).optLong("session");if(id<=0)throw new IllegalArgumentException("This saved estimate was deleted. Check the night again before creating another.");Records.get(store,id);return id;
    }
    static long save(Store store,MissedNightUsagePort usage,MissedNight.Interval evidence,RecordDraft draft,ZoneId zone,long begin,long end,long now,String token){
        long existing=prior(store,token);if(existing>0)return existing;
        if(!usage.allowed())throw new SecurityException("Usage Access is off. Allow it and check the night again.");
        List<MissedNight.Interval> current=MissedNight.pairs(usage.query(begin,end),begin,end);
        if(current.stream().noneMatch(evidence::same))throw new IllegalArgumentException("Phone activity changed. Check the night again before saving.");
        SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();try{
            existing=prior(store,token);if(existing>0){db.setTransactionSuccessful();return existing;}
            MissedNight.validateReview(draft.start,draft.end,begin,end,now);draft.validate(draft.start,draft.end,true,now);
            if(MissedNight.overlaps(draft.start,draft.end,occupied(store)))throw new IllegalArgumentException("A sleep record now overlaps this estimate. Check your history before saving.");
            long id=Records.create(store,draft,zone,now,token);db.setTransactionSuccessful();return id;
        }finally{db.endTransaction();}
    }
    private MissedNightRecords(){}
}
