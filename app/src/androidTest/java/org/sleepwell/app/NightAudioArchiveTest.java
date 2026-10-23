package org.sleepwell.app;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import org.json.*;
import org.junit.Test;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import static org.junit.Assert.*;

/** Real private WAV/SQLite/ZIP bytes only; no recorder, media player or external provider. */
public class NightAudioArchiveTest {
    private File audio(Context c,List<File> files)throws Exception{File dir=new File(c.getFilesDir(),"clips");assertTrue(dir.isDirectory()||dir.mkdirs());File file=new File(dir,"verification-export-"+UUID.randomUUID()+".wav");byte[] bytes=new byte[32000];for(int n=0;n<bytes.length;n++)bytes[n]=(byte)(n%127);NoiseRecorder.writeWave(file,bytes);files.add(file);return file;}
    private void clean(Context c,String db,List<File> files){c.deleteDatabase(db);for(File file:files)if(file.exists())assertTrue(file.delete());}
    @Test public void zipRoundTripContainsExactWavHashesAndHonestEventLinks()throws Exception{
        Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();String database="sleepwell-verification-"+UUID.randomUUID()+".db";List<File> files=new ArrayList<>();try(Store store=new Store(c,database)){
            long id=store.begin(),start=store.rows("SELECT start FROM sessions").get(0).getLong("start");File file=audio(c,files);store.clip(id,start+1000,file.getName(),.2,1000);store.soundEvent(id,start+100,200,"snoring",.9,null);store.soundEvent(id,start+2000,200,"speech",.8,null);store.end(id,false,3000,start+3000);RecordEntry record=Records.get(store,id);NightAudioArchive.Plan plan=NightAudioArchive.prepare(c,store,id,record.updated);ByteArrayOutputStream bytes=new ByteArrayOutputStream();NightAudioArchive.write(c,store,plan,bytes);Map<String,byte[]> entries=new LinkedHashMap<>();try(ZipInputStream zip=new ZipInputStream(new ByteArrayInputStream(bytes.toByteArray()))){for(ZipEntry entry;(entry=zip.getNextEntry())!=null;){ByteArrayOutputStream data=new ByteArrayOutputStream();byte[] chunk=new byte[4096];for(int count;(count=zip.read(chunk))!=-1;)data.write(chunk,0,count);entries.put(entry.getName(),data.toByteArray());}}
            assertEquals(List.of("recordings/001.wav","manifest.json"),new ArrayList<>(entries.keySet()));assertArrayEquals(java.nio.file.Files.readAllBytes(file.toPath()),entries.get("recordings/001.wav"));JSONObject manifest=new JSONObject(new String(entries.get("manifest.json"),java.nio.charset.StandardCharsets.UTF_8));assertEquals("complete_raw_clips",manifest.getString("scope"));assertEquals(1,manifest.getInt("version"));assertEquals(2,manifest.getJSONArray("sound_events").length());assertEquals("recordings/001.wav",manifest.getJSONArray("sound_events").getJSONObject(0).getString("recording"));assertTrue(manifest.getJSONArray("sound_events").getJSONObject(1).isNull("recording"));assertFalse(manifest.toString().contains(file.getName()));StringBuilder hash=new StringBuilder();for(byte value:java.security.MessageDigest.getInstance("SHA-256").digest(entries.get("recordings/001.wav")))hash.append(String.format(Locale.ROOT,"%02x",value&255));assertEquals(hash.toString(),manifest.getJSONArray("recordings").getJSONObject(0).getString("sha256"));assertEquals(1,store.rows("SELECT id FROM clips").size());assertTrue(file.exists());
        }finally{clean(c,database,files);}
    }
    @Test public void staleIdentityAndMalformedAudioRejectBeforeWritingAnyBytes()throws Exception{
        Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();String database="sleepwell-verification-"+UUID.randomUUID()+".db";List<File> files=new ArrayList<>();try(Store store=new Store(c,database)){
            long id=store.begin(),start=store.rows("SELECT start FROM sessions").get(0).getLong("start");File first=audio(c,files),second=audio(c,files);store.clip(id,start+1000,first.getName(),.2,1000);store.end(id,false,3000,start+3000);RecordEntry record=Records.get(store,id);NightAudioArchive.Plan plan=NightAudioArchive.prepare(c,store,id,record.updated);store.getWritableDatabase().delete("clips",null,null);store.clip(id,start+2000,second.getName(),.2,1000);ByteArrayOutputStream bytes=new ByteArrayOutputStream();try{NightAudioArchive.write(c,store,plan,bytes);fail("Reused clip IDs must reject");}catch(IllegalArgumentException expected){}assertEquals(0,bytes.size());assertTrue(first.exists());assertTrue(second.exists());try(RandomAccessFile truncate=new RandomAccessFile(second,"rw")){truncate.setLength(44);}try{NightAudioArchive.prepare(c,store,id,record.updated);fail("Truncated WAV must reject");}catch(IOException expected){}assertEquals(1,store.rows("SELECT id FROM clips").size());
        }finally{clean(c,database,files);}
    }
    @Test public void destinationFailureAndEmptyNightNeverClaimSuccessfulExport()throws Exception{
        Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();String database="sleepwell-verification-"+UUID.randomUUID()+".db";List<File> files=new ArrayList<>();try(Store store=new Store(c,database)){
            long id=store.begin(),start=store.rows("SELECT start FROM sessions").get(0).getLong("start");store.end(id,false,3000,start+3000);RecordEntry record=Records.get(store,id);ByteArrayOutputStream empty=new ByteArrayOutputStream();try{NightAudioArchive.write(c,store,NightAudioArchive.prepare(c,store,id,record.updated),empty);fail("Empty audio cannot be exported");}catch(IOException expected){}assertEquals(0,empty.size());File file=audio(c,files);store.clip(id,start+1000,file.getName(),.2,1000);OutputStream broken=new OutputStream(){int bytes;public void write(int value)throws IOException{if(++bytes>80)throw new IOException("Fixture destination full");}};try{NightAudioArchive.write(c,store,NightAudioArchive.prepare(c,store,id,record.updated),broken);fail("Destination failure must propagate");}catch(IOException expected){}assertTrue(file.exists());assertEquals(1,store.rows("SELECT id FROM clips").size());assertTrue(store.rows("SELECT clip FROM pending_clip_deletes").isEmpty());
        }finally{clean(c,database,files);}
    }
}
