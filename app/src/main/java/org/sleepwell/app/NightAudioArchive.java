package org.sleepwell.app;

import android.content.Context;
import org.json.*;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.*;

/** Explicit complete-clip export. Generated ZIP names never contain private source paths. */
final class NightAudioArchive {
    static final long MAX_BYTES=64L*1024*1024;static final int MAX_CLIPS=60,MAX_EVENTS=3600;
    static final class Audio {final JSONObject row;final long bytes,duration;final String name;Audio(JSONObject row,long bytes,long duration,String name){this.row=row;this.bytes=bytes;this.duration=duration;this.name=name;}}
    static final class Plan {final RecordEntry record;final List<Audio> audio;final List<JSONObject> events;final long bytes;Plan(RecordEntry record,List<Audio> audio,List<JSONObject> events,long bytes){this.record=record;this.audio=List.copyOf(audio);this.events=List.copyOf(events);this.bytes=bytes;}}
    static Plan prepare(Context context,Store store,long id,long updated)throws Exception{
        RecordEntry record=Records.get(store,id);if(record.updated!=updated)throw new IOException("This night changed. Reopen sound events.");List<JSONObject> clips=store.rows("SELECT * FROM clips WHERE session=? ORDER BY time,id",""+id),events=store.rows("SELECT * FROM sound_events WHERE session=? ORDER BY time,id",""+id);
        if(clips.size()>MAX_CLIPS||events.size()>MAX_EVENTS)throw new IOException("This night exceeds the supported export size.");List<Audio> audio=new ArrayList<>();long bytes=0;
        for(JSONObject clip:clips){if(SoundClips.pending(store,clip.optLong("id")))throw new IOException("Finish pending audio deletions before exporting.");File file=checked(context,clip);ClipWave wave;try{wave=ClipWave.read(file);}catch(IOException invalid){throw new IOException("A saved recording is missing or invalid. Review sound events.",invalid);}long size=file.length();bytes=Math.addExact(bytes,size);if(bytes>MAX_BYTES)throw new IOException("This night's audio exceeds the 64 MB export limit.");audio.add(new Audio(clip,size,wave.durationMs,String.format(Locale.ROOT,"recordings/%03d.wav",audio.size()+1)));}
        return new Plan(record,audio,events,bytes);
    }
    private static File checked(Context context,JSONObject clip)throws IOException{try{return FileSafety.clip(context,clip.optString("path"));}catch(IOException invalid){throw new IOException("A saved recording has an invalid location.",invalid);}}
    private static void same(RecordEntry actual,Plan plan)throws IOException{if(actual.updated!=plan.record.updated||actual.rawStart!=plan.record.rawStart||actual.rawEnd!=plan.record.rawEnd)throw new IOException("This night changed. Reopen sound events.");}
    private static void sameEvents(Store store,Plan plan)throws IOException{List<JSONObject> actual=store.rows("SELECT * FROM sound_events WHERE session=? ORDER BY time,id",""+plan.record.id);if(actual.size()!=plan.events.size())throw new IOException("Sound estimates changed. Reopen export.");try{for(int n=0;n<actual.size();n++)SoundClips.eventIdentity(actual.get(n),plan.events.get(n));}catch(IllegalArgumentException changed){throw new IOException("Sound estimates changed. Reopen export.",changed);}}
    static void write(Context context,Store store,Plan plan,OutputStream destination)throws Exception{
        if(plan.audio.isEmpty())throw new IOException("There are no saved recordings to export.");same(Records.get(store,plan.record.id),plan);sameEvents(store,plan);
        // Validate the complete plan before emitting any bytes, including stale row identities.
        for(Audio audio:plan.audio){JSONObject actual=SoundClips.get(store,plan.record.id,audio.row.optLong("id"));SoundClips.identity(actual,audio.row);if(SoundClips.pending(store,actual.optLong("id")))throw new IOException("An audio deletion is pending.");File file=checked(context,actual);if(file.length()!=audio.bytes||ClipWave.read(file).durationMs!=audio.duration)throw new IOException("A recording changed. Reopen sound events.");}
        JSONArray clips=new JSONArray();Map<Long,String> names=new HashMap<>();byte[] buffer=new byte[65536];
        try(ZipOutputStream zip=new ZipOutputStream(destination,java.nio.charset.StandardCharsets.UTF_8)){zip.setLevel(Deflater.BEST_SPEED);
            for(Audio audio:plan.audio){if(Thread.currentThread().isInterrupted())throw new InterruptedIOException("Export interrupted");JSONObject actual=SoundClips.get(store,plan.record.id,audio.row.optLong("id"));SoundClips.identity(actual,audio.row);if(SoundClips.pending(store,actual.optLong("id")))throw new IOException("An audio deletion is pending.");ZipEntry entry=new ZipEntry(audio.name);entry.setTime(0);zip.putNextEntry(entry);MessageDigest digest=MessageDigest.getInstance("SHA-256");long bytes=0;
                try(InputStream input=new FileInputStream(checked(context,actual))){for(int count;(count=input.read(buffer))!=-1;){if(Thread.currentThread().isInterrupted())throw new InterruptedIOException("Export interrupted");bytes+=count;if(bytes>audio.bytes)throw new IOException("A recording changed during export.");digest.update(buffer,0,count);zip.write(buffer,0,count);}}if(bytes!=audio.bytes)throw new IOException("A recording changed during export.");zip.closeEntry();names.put(audio.row.optLong("id"),audio.name);StringBuilder hash=new StringBuilder();for(byte value:digest.digest())hash.append(String.format(Locale.ROOT,"%02x",value&255));clips.put(new JSONObject().put("file",audio.name).put("original_clip_id",audio.row.optLong("id")).put("end_time",audio.row.optLong("time")).put("duration_ms",audio.duration).put("favorite",audio.row.optInt("favorite")==1).put("bytes",bytes).put("sha256",hash.toString()));
            }
            same(Records.get(store,plan.record.id),plan);sameEvents(store,plan);JSONArray events=new JSONArray();for(JSONObject event:plan.events){JSONObject copy=new JSONObject(event.toString());copy.remove("session");long clip=copy.optLong("clip");copy.remove("clip");copy.put("recording",names.containsKey(clip)?names.get(clip):JSONObject.NULL);events.put(copy);}
            JSONObject manifest=new JSONObject().put("format","sleepwell-sound-archive").put("version",1).put("scope","complete_raw_clips").put("score_note","Uncalibrated model estimates; not proof of sleeper talking or medical findings.").put("night",new JSONObject().put("raw_start",plan.record.rawStart).put("raw_end",plan.record.rawEnd).put("visible_start",plan.record.edit.start).put("visible_end",plan.record.edit.end).put("zone",plan.record.zone)).put("recordings",clips).put("sound_events",events);
            // A missing final manifest marks a failed/partial archive; never claim partial output succeeded.
            ZipEntry entry=new ZipEntry("manifest.json");entry.setTime(0);zip.putNextEntry(entry);zip.write(manifest.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));zip.closeEntry();zip.finish();
        }
    }
}
