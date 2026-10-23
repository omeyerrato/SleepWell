package org.sleepwell.app;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.*;
import static org.junit.Assert.*;

/** Real SQLite, WAV descriptors and ZIP bytes. No audio hardware or external provider. */
public class AudioArchiveConcurrencyTest {
    static final class Fixture implements AutoCloseable {
        final Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();final String database="sleepwell-verification-"+UUID.randomUUID()+".db";final Store store=new Store(context,database);final File file;final long id;final NightAudioArchive.Plan plan;
        Fixture()throws Exception{File directory=new File(context.getFilesDir(),"clips");assertTrue(directory.isDirectory()||directory.mkdirs());file=new File(directory,"verification-export-race-"+UUID.randomUUID()+".wav");byte[] data=new byte[32000];for(int n=0;n<data.length;n++)data[n]=(byte)(n%127);NoiseRecorder.writeWave(file,data);id=store.begin();long start=store.rows("SELECT start FROM sessions").get(0).getLong("start");store.clip(id,start+1000,file.getName(),.2,1000);store.end(id,false,3000,start+3000);plan=NightAudioArchive.prepare(context,store,id,Records.get(store,id).updated);}
        public void close(){store.close();context.deleteDatabase(database);if(file.exists())assertTrue(file.delete());}
    }
    static Map<String,byte[]> unzip(byte[] data)throws Exception{Map<String,byte[]> entries=new LinkedHashMap<>();try(ZipInputStream zip=new ZipInputStream(new ByteArrayInputStream(data))){for(ZipEntry entry;(entry=zip.getNextEntry())!=null;){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] bytes=new byte[4096];for(int n;(n=zip.read(bytes))!=-1;)out.write(bytes,0,n);entries.put(entry.getName(),out.toByteArray());}}return entries;}
    private void noSnapshots(Context context){File root=new File(context.getCacheDir(),"recording-exports");File[] files=root.listFiles();if(files!=null)for(File file:files)assertFalse("Snapshot must be cleaned: "+file.getName(),file.getName().startsWith("export-"));}
    @Test public void equalLengthReplacementRejectsBeforeDestinationAndCleansSnapshot()throws Exception{
        try(Fixture f=new Fixture()){long size=f.file.length();try(RandomAccessFile changed=new RandomAccessFile(f.file,"rw")){changed.seek(100);changed.writeByte(42);}assertEquals(size,f.file.length());assertEquals(1000,ClipWave.read(f.file).durationMs);ByteArrayOutputStream output=new ByteArrayOutputStream();try{NightAudioArchive.write(f.context,f.store,f.plan,output);fail("Equal-size replacement must reject");}catch(IOException expected){assertTrue(expected.getMessage().contains("changed"));}assertEquals(0,output.size());assertTrue(f.file.exists());assertEquals(1,f.store.rows("SELECT id FROM clips").size());noSnapshots(f.context);}
    }
    @Test public void legacyUnlinkWaitsForActualSourceCopyThenSnapshotSurvivesDeletion()throws Exception{
        ExecutorService workers=Executors.newFixedThreadPool(2);CountDownLatch copied=new CountDownLatch(1),release=new CountDownLatch(1),attempted=new CountDownLatch(1),deleted=new CountDownLatch(1);NightAudioArchive.Snapshot snapshot=null;
        try(Fixture f=new Fixture()){byte[] original=java.nio.file.Files.readAllBytes(f.file.toPath());Future<NightAudioArchive.Snapshot> staged=workers.submit(()->NightAudioArchive.stage(f.context,f.store,f.plan,()->true,()->{copied.countDown();if(!release.await(5,TimeUnit.SECONDS))throw new IOException("Fixture gate timeout");}));assertTrue(copied.await(3,TimeUnit.SECONDS));Future<?> unlink=workers.submit(()->{attempted.countDown();FileSafety.deleteClip(f.context,f.file.getName());deleted.countDown();});assertTrue(attempted.await(3,TimeUnit.SECONDS));assertFalse("Deletion must wait for actual descriptor copy",deleted.await(150,TimeUnit.MILLISECONDS));release.countDown();snapshot=staged.get(5,TimeUnit.SECONDS);unlink.get(3,TimeUnit.SECONDS);assertFalse(f.file.exists());ByteArrayOutputStream bytes=new ByteArrayOutputStream();snapshot.write(bytes,()->true);assertArrayEquals(original,unzip(bytes.toByteArray()).get("recordings/001.wav"));snapshot.close();snapshot=null;noSnapshots(f.context);
        }finally{release.countDown();if(snapshot!=null)snapshot.close();workers.shutdownNow();assertTrue(workers.awaitTermination(5,TimeUnit.SECONDS));}
    }
    @Test public void blockedDestinationDoesNotBlockCleanupAndOwnedCacheIsRecovered()throws Exception{
        ExecutorService worker=Executors.newSingleThreadExecutor();CountDownLatch providerEntered=new CountDownLatch(1),release=new CountDownLatch(1);NightAudioArchive.Snapshot snapshot=null;
        try(Fixture f=new Fixture()){byte[] original=java.nio.file.Files.readAllBytes(f.file.toPath());snapshot=NightAudioArchive.stage(f.context,f.store,f.plan,()->true);NightAudioArchive.Snapshot value=snapshot;ByteArrayOutputStream bytes=new ByteArrayOutputStream();OutputStream slow=new FilterOutputStream(bytes){boolean blocked;public void write(int item)throws IOException{if(!blocked){blocked=true;providerEntered.countDown();try{if(!release.await(5,TimeUnit.SECONDS))throw new IOException("Fixture provider timeout");}catch(InterruptedException e){Thread.currentThread().interrupt();throw new InterruptedIOException();}}out.write(item);}};
            Future<?> exported=worker.submit(()->{try{value.write(slow,()->true);}catch(Exception e){throw new RuntimeException(e);}});assertTrue(providerEntered.await(3,TimeUnit.SECONDS));try(Store deletion=new Store(f.context,f.database)){SoundClips.delete(f.context,deletion,f.plan.audio.get(0).row);}assertFalse(f.file.exists());assertTrue(f.store.rows("SELECT id FROM clips").isEmpty());assertTrue(value.workspace.directory.exists());try{AudioArchiveWorkspace.create(f.context);fail("Only one live snapshot allowed");}catch(IOException expected){}release.countDown();exported.get(5,TimeUnit.SECONDS);Map<String,byte[]> entries=unzip(bytes.toByteArray());assertArrayEquals(original,entries.get("recordings/001.wav"));assertNotNull(entries.get("manifest.json"));snapshot.close();snapshot=null;noSnapshots(f.context);
            File root=new File(f.context.getCacheDir(),"recording-exports"),stale=new File(root,"export-"+UUID.randomUUID()),unrelated=new File(root,"unrelated-"+UUID.randomUUID());assertTrue(stale.mkdir());assertTrue(new File(stale,"001.wav").createNewFile());assertTrue(unrelated.mkdir());try(AudioArchiveWorkspace recovered=AudioArchiveWorkspace.create(f.context)){assertFalse(stale.exists());assertTrue(unrelated.exists());}assertTrue(unrelated.delete());
            // Cancellation before provider access cleans the bounded cache as well.
            try{NightAudioArchive.stage(f.context,f.store,f.plan,()->false);fail("Canceled staging must reject");}catch(InterruptedIOException expected){}noSnapshots(f.context);
        }finally{release.countDown();if(snapshot!=null)snapshot.close();worker.shutdownNow();assertTrue(worker.awaitTermination(5,TimeUnit.SECONDS));}
    }
}
