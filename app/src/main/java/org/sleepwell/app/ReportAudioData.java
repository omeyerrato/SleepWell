package org.sleepwell.app;

import android.content.Context;
import org.json.JSONObject;
import java.io.*;
import java.util.*;

/** Worker-only real event interval selection; no recording is inferred from its sound label. */
final class ReportAudioData {
    static final int CANDIDATES=4;
    static final class Selection {
        final String category,issue;final long bytes,modified;final ClipTarget target;final float[] peaks;final SoundLabels.Snapshot label;
        Selection(String category,String issue,ClipTarget target,float[] peaks,SoundLabels.Snapshot label,long bytes,long modified){this.category=category;this.issue=issue;this.target=target;this.peaks=peaks;this.label=label;this.bytes=bytes;this.modified=modified;}
    }
    static String category(String value){return value.equals("snoring")||value.equals("speech")?value:"other";}
    static RecordEntry record(Store store,long id,long start,long end){RecordEntry record=Records.get(store,id);if(record.manual()||record.edit.start!=start||record.edit.end!=end)throw new IllegalArgumentException("The sleep period changed. Reopen this report.");return record;}
    static List<Selection> read(Context app,Store store,long id,long start,long end,ReportSounds expected)throws IOException {
        RecordEntry record=record(store,id,start,end);Map<String,Integer> counts=new HashMap<>();
        for(JSONObject row:store.rows("SELECT COALESCE(user_category,category) AS category,COUNT(*) AS count FROM sound_events WHERE session=? AND duration_ms>0 AND time+duration_ms>CAST(? AS INTEGER) AND time<CAST(? AS INTEGER) GROUP BY COALESCE(user_category,category)",""+id,""+start,""+end))counts.merge(category(row.optString("category")),row.optInt("count"),Integer::sum);
        if(counts.getOrDefault("snoring",0)!=expected.snoring||counts.getOrDefault("speech",0)!=expected.speech||counts.getOrDefault("other",0)!=expected.other)throw new IOException("Sound labels changed. Reopen this report.");
        List<Selection> result=new ArrayList<>();for(String category:List.of("snoring","speech","other")){if(counts.getOrDefault(category,0)==0)continue;active();String filter=category.equals("other")?"COALESCE(user_category,category) NOT IN ('snoring','speech')":"COALESCE(user_category,category)='"+category+"'";
            List<JSONObject> candidates=store.rows("SELECT * FROM sound_events WHERE session=? AND duration_ms>0 AND clip IS NOT NULL AND time+duration_ms>CAST(? AS INTEGER) AND time<CAST(? AS INTEGER) AND "+filter+" ORDER BY time,id LIMIT "+CANDIDATES,""+id,""+start,""+end);Selection selected=null;
            for(JSONObject event:candidates){active();try{JSONObject clip=SoundClips.get(store,id,event.optLong("clip"));if(SoundClips.pending(store,clip.optLong("id")))throw new IOException("Recording is being removed");File file=FileSafety.clip(app,clip.optString("path"));ClipWave whole=ClipWave.read(file);long from=Math.max(start,event.optLong("time")),through=Math.min(end,Math.addExact(event.optLong("time"),event.optLong("duration_ms")));RecordingSlice slice=RecordingSlice.contained(clip.optLong("time"),whole.durationMs,from,through-from);ClipTarget target=new ClipTarget(clip,event,slice.fromMs,slice.durationMs,whole.durationMs);ClipWave part=ClipWave.read(file,slice.fromMs,slice.fromMs+slice.durationMs);SoundLabels.Snapshot label=new SoundLabels.Snapshot(event,record.updated,start,end);Selection candidate=new Selection(category,"",target,part.peaks,label,file.length(),file.lastModified());check(app,store,candidate);selected=candidate;break;}catch(InterruptedIOException interrupted){throw interrupted;}catch(IOException|RuntimeException unavailable){/* Try the next bounded actual candidate, never a fabricated sample. */}}
            result.add(selected==null?new Selection(category,candidates.isEmpty()?"Analysis only · no linked recording":"Preview unavailable · open sound events",null,null,null,0,0):selected);
        }return List.copyOf(result);
    }
    static void check(Context app,Store store,Selection selected)throws IOException{if(selected==null||selected.target==null||selected.label==null)throw new IOException("No recording is available.");try{SoundLabels.check(store,selected.label);if(!category(SoundLabels.category(selected.label.event)).equals(selected.category))throw new IOException("This sound label changed. Reopen the report.");ClipReviewAccess.check(store,selected.target);File file=FileSafety.clip(app,selected.target.clip.optString("path"));if(!file.isFile()||file.length()!=selected.bytes||file.lastModified()!=selected.modified)throw new IOException("The audio file changed. Reopen the report.");}catch(RuntimeException changed){throw new IOException("The record, sound label or recording changed. Reopen this report.",changed);}}
    private static void active()throws InterruptedIOException{if(Thread.currentThread().isInterrupted())throw new InterruptedIOException("Report audio loading cancelled");}
    private ReportAudioData(){}
}
