package org.sleepwell.app;

import android.content.Context;
import org.json.JSONObject;
import java.io.*;
import java.util.*;

/** Worker snapshots. A classifier estimate never implies an audio recording exists. */
final class SoundEventRows {
    static final class Row {
        final JSONObject event,clip;final String key,category,title;final long start,duration;final boolean favorite;ClipTarget target;float[] peaks;String audioError="";
        Row(JSONObject event,JSONObject clip,long start,long duration){this.event=event;this.clip=clip;this.start=start;this.duration=duration;key=event==null?"clip:"+clip.optLong("id"):"event:"+event.optLong("id");category=event==null?"noise":event.optString("category","noise");title=event==null?"Saved recording":category.equals("snoring")?"Snoring estimate":category.equals("speech")?"Speech estimate":"Other sound";favorite=(event==null?clip:event).optInt("favorite")==1;}
    }
    static List<Row> metadata(Context context,Store store,RecordEntry record){
        Map<Long,JSONObject> clips=new HashMap<>();for(JSONObject clip:store.rows("SELECT * FROM clips WHERE session=? ORDER BY time",""+record.id))clips.put(clip.optLong("id"),clip);
        List<Row> rows=new ArrayList<>();Set<Long> linked=new HashSet<>();
        for(JSONObject event:store.rows("SELECT * FROM sound_events WHERE session=? AND time+duration_ms>CAST(? AS INTEGER) AND time<CAST(? AS INTEGER) ORDER BY time",""+record.id,""+record.edit.start,""+record.edit.end)){
            long start=Math.max(record.edit.start,event.optLong("time")),end=Math.min(record.edit.end,event.optLong("time")+event.optLong("duration_ms"));JSONObject clip=event.isNull("clip")?null:clips.get(event.optLong("clip"));if(clip!=null)linked.add(clip.optLong("id"));rows.add(new Row(event,clip,start,end-start));
        }
        // Legacy recordings have no invented inferred category.
        for(JSONObject clip:clips.values())if(!linked.contains(clip.optLong("id"))){long duration=clip.optLong("duration_ms");try{duration=ClipWave.read(FileSafety.clip(context,clip.optString("path"))).durationMs;}catch(IOException|RuntimeException missing){/* Unknown legacy timing remains an unavailable recording. */}long end=Math.min(record.edit.end,clip.optLong("time")),start=Math.max(record.edit.start,clip.optLong("time")-duration);if(end>start)rows.add(new Row(null,clip,start,end-start));else if(duration<=0&&clip.optLong("time")>=record.edit.start&&clip.optLong("time")<=record.edit.end)rows.add(new Row(null,clip,clip.optLong("time"),0));}
        rows.sort(Comparator.comparingLong(row->row.start));return rows;
    }
    static boolean matches(Row row,String filter){return filter.equals("all")||filter.equals("favorites")&&row.favorite||filter.equals(row.category);}
    static List<Row> prepare(Context context,Store store,List<Row> metadata,String filter,int limit){List<Row> visible=new ArrayList<>();for(Row row:metadata){if(!matches(row,filter))continue;if(visible.size()>=limit)break;if(row.clip!=null){try{if(SoundClips.pending(store,row.clip.optLong("id")))throw new IOException("Deletion pending");File file=FileSafety.clip(context,row.clip.optString("path"));ClipWave full=ClipWave.read(file);RecordingSlice slice=RecordingSlice.contained(row.clip.optLong("time"),full.durationMs,row.start,row.duration);ClipWave part=ClipWave.read(file,slice.fromMs,slice.fromMs+slice.durationMs);row.target=new ClipTarget(row.clip,row.event,slice.fromMs,slice.durationMs,full.durationMs);row.peaks=part.peaks;}catch(IOException|RuntimeException unavailable){row.audioError="Recording unavailable";}}visible.add(row);}return visible;}
}
