package org.sleepwell.app;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.BooleanSupplier;
import org.json.*;

/** Review actual effective coverage. Records remain separate; only Trends unions overlapping time. */
final class RecordOverlap {
    static final int MAX_PERIODS=512,MAX_INTERVALS=20000;
    static final class Review {
        final int count;final long millis;final String fingerprint;final List<String> rows;
        Review(int count,long millis,String fingerprint,List<String> rows){this.count=count;this.millis=millis;this.fingerprint=fingerprint;this.rows=List.copyOf(rows);}
    }
    private static void active(BooleanSupplier active)throws IOException{if(!active.getAsBoolean()||Thread.currentThread().isInterrupted())throw new IOException("Overlap review was interrupted. Review before saving again.");}
    private static String hash(String text)throws Exception{return NightAudioArchive.hash(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));}
    private static JSONArray spans(List<RecordIntervals.Span> spans)throws JSONException{JSONArray out=new JSONArray();for(RecordIntervals.Span span:spans)out.put(new JSONArray().put(span.start).put(span.end));return out;}
    private static String draftKey(RecordDraft draft)throws Exception{return hash(new JSONObject().put("start",draft.start).put("end",draft.end).put("note",draft.note).put("rated",draft.rated).put("rating",draft.rating).put("tags",TagCodec.encode(draft)).put("awake",spans(draft.awake)).toString());}
    private static List<RecordIntervals.Span> intersect(List<RecordIntervals.Span> a,List<RecordIntervals.Span> b){List<RecordIntervals.Span> out=new ArrayList<>();int i=0,j=0;while(i<a.size()&&j<b.size()){RecordIntervals.Span x=a.get(i),y=b.get(j);long start=Math.max(x.start,y.start),end=Math.min(x.end,y.end);if(end>start)out.add(new RecordIntervals.Span(start,end));if(x.end<=y.end)i++;else j++;}return out;}
    /** Caller holds the same transaction used for its subsequent mutation, or a read-only review transaction. */
    static Review inspect(Store store,long self,RecordDraft draft,BooleanSupplier live)throws Exception{
        active(live);draft.validate(draft.start,draft.end,true,System.currentTimeMillis());
        List<RecordIntervals.Span> selected=RecordIntervals.subtract(List.of(new RecordIntervals.Span(draft.start,draft.end)),draft.awake),shared=new ArrayList<>();
        String end=""+draft.end,start=""+draft.start;
        List<JSONObject> candidates=store.rows("SELECT s.id FROM sessions s LEFT JOIN record_edits e ON e.session=s.id WHERE s.end IS NOT NULL AND s.id!=CAST(? AS INTEGER) AND ((s.start<CAST(? AS INTEGER) AND s.end>CAST(? AS INTEGER)) OR (e.reverted=0 AND e.start<CAST(? AS INTEGER) AND e.end>CAST(? AS INTEGER))) ORDER BY s.id LIMIT 513",""+self,end,start,end,start);
        if(candidates.size()>MAX_PERIODS)throw new IOException("More than 512 saved periods meet this interval. Shorten it before reviewing overlaps.");
        JSONArray identities=new JSONArray();List<String> rows=new ArrayList<>();int count=0,intervals=0;long inputs=0;
        for(JSONObject candidate:candidates){active(live);long id=candidate.getLong("id");inputs+=store.rows("SELECT (SELECT COUNT(*) FROM tracking_pauses WHERE session=?) + (SELECT COUNT(*) FROM awake_estimates WHERE session=?) AS n",""+id,""+id).get(0).getLong("n");if(inputs>MAX_INTERVALS)throw new IOException("Too many saved intervals meet this period. Shorten it before reviewing overlaps.");RecordEntry record=Records.get(store,id);List<RecordIntervals.Span> coverage=record.coverage();intervals+=coverage.size();if(intervals>MAX_INTERVALS)throw new IOException("Too many saved intervals meet this period. Shorten it before reviewing overlaps.");List<RecordIntervals.Span> overlap=intersect(selected,coverage);if(overlap.isEmpty())continue;count++;shared.addAll(overlap);if(shared.size()>MAX_INTERVALS)throw new IOException("Too many shared intervals meet this period. Shorten it before reviewing overlaps.");
            identities.put(new JSONObject().put("id",record.id).put("updated",record.updated).put("rawStart",record.rawStart).put("rawEnd",record.rawEnd).put("source",record.provenance()).put("zone",record.zone).put("start",record.edit.start).put("end",record.edit.end).put("coverage",spans(coverage)));
            if(rows.size()<8)rows.add(RecordUi.date(record.edit.start,record.zone())+" "+java.time.Instant.ofEpochMilli(record.edit.start).atZone(record.zone()).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss XXX",Locale.getDefault()))+" → "+RecordUi.date(record.edit.end,record.zone())+" "+java.time.Instant.ofEpochMilli(record.edit.end).atZone(record.zone()).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss XXX",Locale.getDefault()))+" · "+(record.zone.isEmpty()?"time zone unknown":record.zone)+"\n"+record.provenance()+" · "+RecordUi.duration(RecordIntervals.length(overlap))+" shared");
        }
        active(live);return new Review(count,RecordIntervals.length(shared),hash(new JSONObject().put("draft",draftKey(draft)).put("others",identities).toString()),rows);
    }
    private RecordOverlap(){}
}
