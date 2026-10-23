package org.sleepwell.app;

import android.content.Context;
import org.json.*;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.zip.*;

/** Explicit complete-clip export. Generated ZIP names never contain private source paths. */
final class NightAudioArchive {
    static final long MAX_BYTES=64L*1024*1024;static final int MAX_CLIPS=60,MAX_EVENTS=3600;
    static final class Audio {final JSONObject row;final long bytes,duration;final String name,sha256;Audio(JSONObject row,long bytes,long duration,String name,String sha256){this.row=row;this.bytes=bytes;this.duration=duration;this.name=name;this.sha256=sha256;}}
    static final class Plan {final RecordEntry record;final List<Audio> audio;final List<JSONObject> events;final long bytes;Plan(RecordEntry record,List<Audio> audio,List<JSONObject> events,long bytes){this.record=record;this.audio=List.copyOf(audio);this.events=List.copyOf(events);this.bytes=bytes;}}
    interface Progress {void copied()throws Exception;}
    static void active(BooleanSupplier active)throws InterruptedIOException{if(Thread.currentThread().isInterrupted()||!active.getAsBoolean())throw new InterruptedIOException("Export interrupted");}
    static String hash(byte[] value){StringBuilder result=new StringBuilder();for(byte item:value)result.append(String.format(Locale.ROOT,"%02x",item&255));return result.toString();}
    private static String copy(RandomAccessFile source,OutputStream output,long bytes,BooleanSupplier active,Progress progress)throws Exception{
        source.seek(0);MessageDigest digest=MessageDigest.getInstance("SHA-256");byte[] buffer=new byte[65536];long total=0;
        for(int count;(count=source.read(buffer))!=-1;){active(active);total+=count;if(total>bytes)throw new IOException("A recording changed. Reopen sound events.");digest.update(buffer,0,count);if(output!=null)output.write(buffer,0,count);if(progress!=null){progress.copied();progress=null;}}
        if(total!=bytes||source.length()!=bytes)throw new IOException("A recording changed. Reopen sound events.");return hash(digest.digest());
    }
    static Plan prepare(Context context,Store store,long id,long updated)throws Exception{
        synchronized(SoundClips.LOCK){
            RecordEntry record=Records.get(store,id);if(record.updated!=updated)throw new IOException("This night changed. Reopen sound events.");List<JSONObject> clips=store.rows("SELECT * FROM clips WHERE session=? ORDER BY time,id",""+id),events=store.rows("SELECT * FROM sound_events WHERE session=? ORDER BY time,id",""+id);
            if(clips.size()>MAX_CLIPS||events.size()>MAX_EVENTS)throw new IOException("This night exceeds the supported export size.");List<Audio> audio=new ArrayList<>();long bytes=0;
            for(JSONObject clip:clips){active(()->true);if(SoundClips.pending(store,clip.optLong("id")))throw new IOException("Finish pending audio deletions before exporting.");try(RandomAccessFile source=new RandomAccessFile(checked(context,clip),"r")){long size=source.length();bytes=Math.addExact(bytes,size);if(bytes>MAX_BYTES)throw new IOException("This night's audio exceeds the 64 MB export limit.");ClipWave wave=ClipWave.read(source,0,Long.MAX_VALUE);String digest=copy(source,null,size,()->true,null);audio.add(new Audio(clip,size,wave.durationMs,String.format(Locale.ROOT,"recordings/%03d.wav",audio.size()+1),digest));}}
            return new Plan(record,audio,events,bytes);
        }
    }
    private static File checked(Context context,JSONObject clip)throws IOException{try{return FileSafety.clip(context,clip.optString("path"));}catch(IOException invalid){throw new IOException("A saved recording has an invalid location.",invalid);}}
    private static void same(RecordEntry actual,Plan plan)throws IOException{if(actual.updated!=plan.record.updated||actual.rawStart!=plan.record.rawStart||actual.rawEnd!=plan.record.rawEnd)throw new IOException("This night changed. Reopen sound events.");}
    private static void sameEvents(Store store,Plan plan)throws IOException{List<JSONObject> actual=store.rows("SELECT * FROM sound_events WHERE session=? ORDER BY time,id",""+plan.record.id);if(actual.size()!=plan.events.size())throw new IOException("Sound estimates changed. Reopen export.");try{for(int n=0;n<actual.size();n++){JSONObject expected=plan.events.get(n),row=actual.get(n);SoundClips.eventIdentity(row,expected);for(String name:new String[]{"clip","favorite","score"})if(!Objects.equals(row.opt(name),expected.opt(name)))throw new IllegalArgumentException();}}catch(IllegalArgumentException changed){throw new IOException("Sound estimates changed. Reopen export.",changed);}}
    private static void sameAudio(Store store,Plan plan)throws IOException{
        List<JSONObject> actual=store.rows("SELECT * FROM clips WHERE session=? ORDER BY time,id",""+plan.record.id);if(actual.size()!=plan.audio.size())throw new IOException("Recordings changed. Reopen sound events.");
        for(int n=0;n<actual.size();n++){JSONObject row=actual.get(n);SoundClips.identity(row,plan.audio.get(n).row);if(row.optInt("favorite")!=plan.audio.get(n).row.optInt("favorite")||SoundClips.pending(store,row.optLong("id")))throw new IOException("Recordings changed. Reopen sound events.");}
    }
    static final class Snapshot implements AutoCloseable {
        final AudioArchiveWorkspace workspace;final Plan plan;final byte[] manifest;
        Snapshot(AudioArchiveWorkspace workspace,Plan plan,byte[] manifest){this.workspace=workspace;this.plan=plan;this.manifest=manifest;}
        void write(OutputStream destination,BooleanSupplier running)throws Exception{
            active(running);try(ZipOutputStream zip=new ZipOutputStream(destination,java.nio.charset.StandardCharsets.UTF_8)){zip.setLevel(Deflater.BEST_SPEED);
                for(int n=0;n<plan.audio.size();n++){active(running);Audio audio=plan.audio.get(n);ZipEntry entry=new ZipEntry(audio.name);entry.setTime(0);zip.putNextEntry(entry);try(RandomAccessFile input=new RandomAccessFile(workspace.file(n),"r")){if(!audio.sha256.equals(copy(input,zip,audio.bytes,running,null)))throw new IOException("Private export snapshot changed.");}zip.closeEntry();}
                active(running);ZipEntry entry=new ZipEntry("manifest.json");entry.setTime(0);zip.putNextEntry(entry);zip.write(manifest);zip.closeEntry();zip.finish();
            }
        }
        @Override public void close()throws IOException{workspace.close();}
    }
    static Snapshot stage(Context context,Store store,Plan plan,BooleanSupplier running,Progress progress)throws Exception{
        if(plan.audio.isEmpty())throw new IOException("There are no saved recordings to export.");
        if(plan.audio.size()>MAX_CLIPS||plan.events.size()>MAX_EVENTS)throw new IOException("This night exceeds the supported export size.");long total=0;for(Audio audio:plan.audio){if(audio.bytes<44||audio.bytes>16L*1024*1024||!audio.sha256.matches("[0-9a-f]{64}"))throw new IOException("Invalid recording export plan.");total=Math.addExact(total,audio.bytes);if(total>MAX_BYTES)throw new IOException("This night's audio exceeds the 64 MB export limit.");}
        AudioArchiveWorkspace workspace=AudioArchiveWorkspace.create(context);
        try{
            // Only bounded private disk work holds the deletion lock. Never provider I/O.
            synchronized(SoundClips.LOCK){active(running);same(Records.get(store,plan.record.id),plan);sameAudio(store,plan);sameEvents(store,plan);
                for(int n=0;n<plan.audio.size();n++){Audio audio=plan.audio.get(n);try(RandomAccessFile source=new RandomAccessFile(checked(context,audio.row),"r");OutputStream out=new FileOutputStream(workspace.file(n))){if(source.length()!=audio.bytes||!audio.sha256.equals(copy(source,out,audio.bytes,running,progress)))throw new IOException("A recording changed. Reopen sound events.");}progress=null;}
                same(Records.get(store,plan.record.id),plan);sameAudio(store,plan);sameEvents(store,plan);
            }
            for(int n=0;n<plan.audio.size();n++){active(running);if(ClipWave.read(workspace.file(n)).durationMs!=plan.audio.get(n).duration)throw new IOException("Invalid private recording snapshot.");}
            JSONArray clips=new JSONArray();Map<Long,String> names=new HashMap<>();for(Audio audio:plan.audio){names.put(audio.row.optLong("id"),audio.name);clips.put(new JSONObject().put("file",audio.name).put("original_clip_id",audio.row.optLong("id")).put("end_time",audio.row.optLong("time")).put("duration_ms",audio.duration).put("favorite",audio.row.optInt("favorite")==1).put("bytes",audio.bytes).put("sha256",audio.sha256));}
            JSONArray events=new JSONArray();for(JSONObject event:plan.events){JSONObject value=new JSONObject(event.toString());value.remove("session");long clip=value.optLong("clip");value.remove("clip");value.put("recording",names.containsKey(clip)?names.get(clip):JSONObject.NULL);events.put(value);}
            JSONObject manifest=new JSONObject().put("format","sleepwell-sound-archive").put("version",1).put("scope","complete_raw_clips").put("score_note","Uncalibrated model estimates; not proof of sleeper talking or medical findings.").put("night",new JSONObject().put("raw_start",plan.record.rawStart).put("raw_end",plan.record.rawEnd).put("visible_start",plan.record.edit.start).put("visible_end",plan.record.edit.end).put("zone",plan.record.zone)).put("recordings",clips).put("sound_events",events);
            byte[] metadata=manifest.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8);if(metadata.length>4*1024*1024)throw new IOException("Sound metadata exceeds the export limit.");active(running);return new Snapshot(workspace,plan,metadata);
        }catch(Exception failure){try{workspace.close();}catch(IOException cleanup){failure.addSuppressed(cleanup);}throw failure;}
    }
    static Snapshot stage(Context context,Store store,Plan plan,BooleanSupplier running)throws Exception{return stage(context,store,plan,running,null);}
    static void write(Context context,Store store,Plan plan,OutputStream destination)throws Exception{try(Snapshot snapshot=stage(context,store,plan,()->true)){snapshot.write(destination,()->true);}}
}
