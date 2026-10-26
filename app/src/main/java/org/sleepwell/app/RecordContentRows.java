package org.sleepwell.app;

import android.content.Context;
import android.database.*;
import android.database.sqlite.SQLiteDatabase;
import android.os.*;
import org.json.JSONObject;
import java.time.*;
import java.util.*;

/** Bounded completed-record pages using the same edit/awake/pause projection as reports. */
final class RecordContentRows {
    static MatrixCursor read(Context app,RecordContentContract.Request request,String[] projection,CancellationSignal cancellation,Runnable guard){
        MatrixCursor result=new MatrixCursor(projection,request.limit);long now=System.currentTimeMillis(),last=0;int rejected=0;boolean more=false;
        try(Store store=Records.PRODUCTION.open(app)){SQLiteDatabase db=store.getReadableDatabase();db.beginTransactionNonExclusive();try{
            active(cancellation,guard);String where="end IS NOT NULL AND id>0 AND id<=9007199254740991";List<String> args=new ArrayList<>();if(request.id>0){where+=" AND id=?";args.add(""+request.id);}else if(request.before>0){where+=" AND id<CAST(? AS INTEGER)";args.add(""+request.before);}
            List<Long> ids=new ArrayList<>();try(Cursor cursor=db.rawQuery("SELECT id FROM sessions WHERE "+where+" ORDER BY id DESC LIMIT "+(request.id>0?1:request.limit+1),args.toArray(new String[0]),cancellation)){while(cursor.moveToNext()){if(ids.size()==request.limit){more=true;break;}ids.add(cursor.getLong(0));}}
            int intervals=0;for(long id:ids){active(cancellation,guard);last=id;try{int count=bounded(store,id);intervals+=count;if(intervals>16384)throw new IllegalStateException("This page has too many saved intervals. Request a smaller limit.");RecordEntry record=Records.get(store,id);if(record.rawStart<=0||record.rawEnd<=record.rawStart||record.edit.start<=0||record.edit.end<=record.edit.start||record.edit.end>now||record.edit.end-record.edit.start>SensorProvenance.MAX_SESSION)throw new IllegalArgumentException("Invalid completed interval");long duration=record.duration();if(duration<0||duration>record.edit.end-record.edit.start)throw new IllegalArgumentException("Invalid duration");Map<String,Object> row=new HashMap<>();row.put("_id",record.id);row.put("start_ms",record.edit.start);row.put("end_ms",record.edit.end);row.put("reviewed_duration_ms",duration);row.put("active_ms",record.active==null||record.active<0?null:record.active);String zone=null;try{if(!record.zone.isEmpty())zone=ZoneId.of(record.zone).getId();}catch(DateTimeException unknown){}row.put("recorded_zone",zone);row.put("source",!record.healthConnect.isEmpty()?"provider_import":record.manual()?"user_reported":record.source.equals("tracked")?"tracked":record.source.equals("legacy")?"legacy_timing":"unknown");row.put("origin",Set.of("local","imported","legacy").contains(record.origin)?record.origin:"unknown");row.put("sensor",record.sonar()?"contactless_echo_change":record.sensor.equals(SensorProvenance.MOTION)?"phone_accelerometer":record.manual()?"none":"unknown");row.put("edited",record.edited?1:0);row.put("interrupted",record.interrupted!=0?1:0);row.put("clock_changed",record.clockChanged!=0?1:0);row.put("duration_limited",record.durationLimited()?1:0);Object[] values=new Object[projection.length];for(int n=0;n<projection.length;n++)values[n]=row.get(projection[n]);result.addRow(values);
                }catch(IllegalArgumentException unreadable){rejected++;}}
            active(cancellation,guard);db.setTransactionSuccessful();
        }finally{db.endTransaction();}}
        Bundle extras=new Bundle();extras.putInt("contract_version",1);extras.putBoolean("has_more",more);if(more&&last>0)extras.putLong("next_before_id",last);extras.putInt("omitted_invalid_records",rejected);extras.putString("order","_id DESC");extras.putString("duration_basis","Reviewed coverage after pauses and accepted/user/provider awake intervals; unmarked awake time may remain. Not a validated sleep measurement.");result.setExtras(extras);active(cancellation,guard);return result;
    }
    private static void active(CancellationSignal cancellation,Runnable guard){if(cancellation!=null)cancellation.throwIfCanceled();guard.run();}
    private static int bounded(Store store,long id){SQLiteDatabase db=store.getReadableDatabase();long intervals=0;for(String table:List.of("tracking_pauses","awake_estimates")){try(Cursor c=db.rawQuery("SELECT COUNT(*) FROM "+table+" WHERE session=?",new String[]{""+id})){if(!c.moveToFirst()||c.getLong(0)>4096)throw new IllegalArgumentException("Too many saved intervals");intervals+=c.getLong(0);}}
        try(Cursor c=db.rawQuery("SELECT length(note),length(sensor_config),length(sound_analysis),length(health_connect),length(source),length(origin),length(zone) FROM sessions WHERE id=?",new String[]{""+id})){if(!c.moveToFirst())throw new IllegalArgumentException("Missing record");int[] limits={20000,32768,32768,HealthSleepCodec.MAX,64,64,128};for(int n=0;n<limits.length;n++)if(c.getLong(n)>limits[n])throw new IllegalArgumentException("Record metadata is too large");}
        try(Cursor c=db.rawQuery("SELECT length(note),length(tags),length(awake) FROM record_edits WHERE session=?",new String[]{""+id})){if(c.moveToFirst()&&(c.getLong(0)>20000||c.getLong(1)>16384||c.getLong(2)>16384))throw new IllegalArgumentException("Edit metadata is too large");}
        try(Cursor c=db.rawQuery("SELECT length(policy),length(error) FROM awake_runs WHERE session=?",new String[]{""+id})){if(c.moveToFirst()&&(c.getLong(0)>32768||c.getLong(1)>1024))throw new IllegalArgumentException("Awake metadata is too large");}
        return (int)intervals;
    }
    private RecordContentRows(){}
}
