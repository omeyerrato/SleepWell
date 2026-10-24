package org.sleepwell.app;

import android.content.Context;
import org.json.JSONObject;
import java.io.*;
import java.util.*;

/** Global stars are independent metadata, never evidence that PCM exists. Worker-only queries. */
final class FavoriteSoundRows {
    static final int PAGE_SIZE=50;
    static final class Filter {final int days;final String category;Filter(int days,String category){this.days=days==7||days==30?days:0;this.category=SoundBrowseFilter.filter(category,false);}long cutoff(long now){return days==0?0:Math.max(0,now-days*86400000L);}}
    static final class Item {final RecordEntry night;final SoundEventRows.Row row;final boolean outside;Item(RecordEntry night,SoundEventRows.Row row,boolean outside){this.night=night;this.row=row;this.outside=outside;}}
    static final class Page {final List<Item> items;final Map<String,Integer> counts;final int page,total;Page(List<Item> items,Map<String,Integer> counts,int page,int total){this.items=List.copyOf(items);this.counts=Map.copyOf(counts);this.page=page;this.total=total;}int count(String category){return counts.getOrDefault(category,0);}boolean older(){return (long)(page+1)*PAGE_SIZE<total;}}
    private static final String UNION="SELECT 'event' AS kind,e.id,e.session,e.time,CASE WHEN e.user_category IN ('snoring','speech','cough','baby_cry','laughter','noise') THEN e.user_category ELSE e.category END AS category FROM sound_events e JOIN sessions s ON s.id=e.session WHERE e.favorite=1 AND s.end IS NOT NULL UNION ALL SELECT 'clip' AS kind,c.id,c.session,c.time,'noise' AS category FROM clips c JOIN sessions s ON s.id=c.session WHERE c.favorite=1 AND s.end IS NOT NULL";
    private static final class Raw {final RecordEntry night;final JSONObject event,clip;Raw(RecordEntry night,JSONObject event,JSONObject clip){this.night=night;this.event=event;this.clip=clip;}}
    static Page read(Context context,Store store,Filter filter,int requested,long now)throws Exception{
        List<Raw> raw=new ArrayList<>();Map<String,Integer> counts=new HashMap<>();int page,total;
        android.database.sqlite.SQLiteDatabase db=store.getReadableDatabase();db.beginTransactionNonExclusive();try{
            String since="SELECT * FROM ("+UNION+") WHERE time>=CAST(? AS INTEGER)";String cutoff=""+filter.cutoff(now);int all=0;for(JSONObject row:store.rows("SELECT category,COUNT(*) AS amount FROM ("+since+") GROUP BY category",cutoff)){int count=row.getInt("amount");String category=row.optString("category");category=SoundBrowseFilter.category(category);counts.put(category,counts.getOrDefault(category,0)+count);all=Math.addExact(all,count);}counts.put("all",all);total=counts.getOrDefault(filter.category,0);page=Math.max(0,Math.min(requested,Math.max(0,(total-1)/PAGE_SIZE)));
            List<String> params=new ArrayList<>(List.of(cutoff));String query=since;if(filter.category.equals("noise"))query+=" AND category NOT IN ('snoring','speech','cough','baby_cry','laughter')";else if(!filter.category.equals("all")){query+=" AND category=?";params.add(filter.category);}query+=" ORDER BY time DESC,kind,id DESC LIMIT "+PAGE_SIZE+" OFFSET "+((long)page*PAGE_SIZE);
            Map<Long,RecordEntry> nights=new HashMap<>();for(JSONObject key:store.rows(query,params.toArray(new String[0]))){long session=key.getLong("session");RecordEntry night=nights.get(session);if(night==null){night=Records.get(store,session);nights.put(session,night);}JSONObject event=null,clip=null;
                if(key.getString("kind").equals("event")){event=SoundClips.event(store,session,key.getLong("id"));if(!event.isNull("clip")){List<JSONObject> clips=store.rows("SELECT * FROM clips WHERE id=? AND session=?",""+event.getLong("clip"),""+session);if(!clips.isEmpty())clip=clips.get(0);}}
                else clip=SoundClips.get(store,session,key.getLong("id"));raw.add(new Raw(night,event,clip));
            }
            db.setTransactionSuccessful();
        }finally{db.endTransaction();}
        // WAV decoding never holds the metadata transaction or a deletion lock.
        List<Item> items=new ArrayList<>();for(Raw value:raw){RecordEntry night=value.night;JSONObject event=value.event,clip=value.clip;long start,end,duration;
            if(event!=null){start=event.getLong("time");duration=event.getLong("duration_ms");if(duration<=0)throw new IOException("Invalid favorite interval");end=Math.addExact(start,duration);}else{duration=clip.optLong("duration_ms");try{duration=ClipWave.read(FileSafety.clip(context,clip.getString("path"))).durationMs;}catch(IOException|RuntimeException missing){/* Preserve unavailable stars. */}end=clip.getLong("time");start=duration>0?Math.subtractExact(end,duration):end;}
            boolean outside=end<=night.edit.start||start>=night.edit.end;long visibleStart=outside?start:Math.max(start,night.edit.start),visibleEnd=outside?end:Math.min(end,night.edit.end);SoundEventRows.Row row=new SoundEventRows.Row(event,clip,visibleStart,Math.max(0,visibleEnd-visibleStart));if(outside)row.audioError="Outside edited sleep period";else SoundEventRows.prepare(context,store,List.of(row),"all",1);items.add(new Item(night,row,outside));
        }return new Page(items,counts,page,total);
    }
}
