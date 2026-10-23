package org.sleepwell.app;

import android.content.*;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.*;
import android.provider.OpenableColumns;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/** Process-retained validation only. No Activity references, playback, or worker preference writes. */
final class SoundImports {
    static final long MAX_BYTES=100*1024*1024L,TIMEOUT_MS=20000;
    private static final Map<String,Job> jobs=new ConcurrentHashMap<>();
    private static boolean cacheCleaned;
    private static synchronized void cleanAbandoned(Context app){
        if(cacheCleaned)return;cacheCleaned=true;
        // A killed process cannot run finally. Only this importer's disposable private snapshots qualify.
        File[] files=app.getCacheDir().listFiles(f->f.isFile()&&f.getName().startsWith("sleepwell-audio-")&&f.getName().endsWith(".tmp"));
        if(files!=null)for(File file:files)if(!file.delete())file.deleteOnExit();
    }
    private static final ExecutorService workers=new ThreadPoolExecutor(0,2,30,TimeUnit.SECONDS,new SynchronousQueue<>(),r->{Thread t=new Thread(r,"audio-import");t.setDaemon(true);return t;});
    private static final ExecutorService closers=new ThreadPoolExecutor(0,2,30,TimeUnit.SECONDS,new SynchronousQueue<>(),r->{Thread t=new Thread(r,"audio-import-close");t.setDaemon(true);return t;});
    enum State { READING,READY,FAILED,CANCELLED,COMPLETED }
    static final class Job {
        final String id=UUID.randomUUID().toString();final Context app;final Uri uri;final int flags;
        final SharedPreferences preferences;final long revision,started=SystemClock.elapsedRealtime();final SoundMix previous;
        final CancellationSignal signal=new CancellationSignal();final CountDownLatch exited=new CountDownLatch(1);
        volatile State state=State.READING;volatile long bytes;volatile String title="My audio",error="";
        volatile AssetFileDescriptor descriptor;volatile boolean retired;
        Job(Context c,Uri u,int f){app=c.getApplicationContext();uri=u;flags=f;preferences=SoundPreferences.factory.open(app);synchronized(SoundPreferences.class){revision=SoundPreferences.revision(preferences);previous=SoundPreferences.load(preferences).copy();}}
        synchronized void fail(String message){if(state==State.READING||state==State.READY){error=message;state=State.FAILED;}}
        void check()throws IOException{if(state!=State.READING||Thread.currentThread().isInterrupted())throw new IOException("Cancelled");if(SystemClock.elapsedRealtime()-started>TIMEOUT_MS)throw new IOException("Timed out");}
        void read(){File temporary=null;try{
            cleanAbandoned(app);
            check();try(Cursor cursor=app.getContentResolver().query(uri,new String[]{OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE},null,signal)){
                if(cursor!=null&&cursor.moveToFirst()){
                    int size=cursor.getColumnIndex(OpenableColumns.SIZE),name=cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if(size>=0&&!cursor.isNull(size)&&(cursor.getLong(size)<=0||cursor.getLong(size)>MAX_BYTES))throw new IOException("Invalid audio size");
                    if(name>=0&&!cursor.isNull(name)){String clean=cursor.getString(name).replaceAll("[\\p{Cntrl}\\p{Cf}]", " ").trim();if(!clean.isEmpty())title=clean.substring(0,Math.min(clean.length(),80));}
                }
            }
            check();temporary=File.createTempFile("sleepwell-audio-",".tmp",app.getCacheDir());
            descriptor=app.getContentResolver().openAssetFileDescriptor(uri,"r",signal);if(descriptor==null)throw new IOException("No audio document");
            try(AssetFileDescriptor fd=descriptor;InputStream in=fd.createInputStream();OutputStream out=new FileOutputStream(temporary)){AudioCopy.copy(in,out,MAX_BYTES,this::check,n->bytes=n);}finally{descriptor=null;}
            check();try(MediaMetadataRetriever metadata=new MediaMetadataRetriever();FileInputStream input=new FileInputStream(temporary)){
                metadata.setDataSource(input.getFD());String duration=metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                if(duration==null||Long.parseLong(duration)<=0||!"yes".equals(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)))throw new IOException("No audio track");
            }
            check();synchronized(this){if(state==State.READING)state=State.READY;}
        }catch(Exception e){fail(SystemClock.elapsedRealtime()-started>=TIMEOUT_MS?"The audio provider took too long. Choose the file again.":"That file could not be read as audio. Choose a readable audio file up to 100 MiB.");}
        finally{if(temporary!=null&&!temporary.delete())temporary.deleteOnExit();exited.countDown();if(retired)jobs.remove(id,this);}}
        void cancel(){synchronized(this){if(state!=State.COMPLETED)state=State.CANCELLED;}closeOffMain();}
        void closeOffMain(){try{closers.execute(()->{signal.cancel();AssetFileDescriptor fd=descriptor;if(fd!=null)try{fd.close();}catch(IOException ignored){}});}catch(RejectedExecutionException ignored){/* A stuck provider never gets an unbounded number of cancellation threads. */}}
        void poll(){if(state==State.READING&&SystemClock.elapsedRealtime()-started>=TIMEOUT_MS){fail("The audio provider took too long. Choose the file again.");closeOffMain();}}
        /** Main thread only: no import can commit after another mix edit or after cancellation. */
        synchronized boolean commit(){
            if(state!=State.READY)return false;
            synchronized(SoundPreferences.class){
                if(SoundPreferences.revision(preferences)!=revision){fail("Your mix changed while the file was checked. Choose the file again to use it.");return false;}
                try{
                    int required=Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION;
                    if((flags&required)!=required)throw new SecurityException("Persistent read access not offered");
                    app.getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    SoundMix next=previous.copy();next.uri=uri.toString();next.title=title;next.primary="My audio";
                    if(!SoundPreferences.saveIfRevision(preferences,revision,next))throw new IllegalStateException("Mix changed");
                    preferences.edit().putString("soundError","").apply();state=State.COMPLETED;return true;
                }catch(RuntimeException e){fail("Access to that file could not be kept. Choose it again from a document provider.");return false;}
            }
        }
        void retire(){retired=true;if(exited.getCount()==0)jobs.remove(id,this);}
    }
    static Job start(Context c,Uri uri,int flags){
        if(!"content".equals(uri.getScheme())||uri.getAuthority()==null)throw new IllegalArgumentException("Choose an audio document");
        Job job=new Job(c,uri,flags);jobs.put(job.id,job);
        try{workers.execute(job::read);}catch(RejectedExecutionException e){job.fail("Another document is still closing. Try again shortly.");job.exited.countDown();}
        return job;
    }
    static Job find(String id){return id==null?null:jobs.get(id);}
    static boolean pending(){return !jobs.isEmpty();}
    static void awaitIdle(long timeout)throws InterruptedException,TimeoutException{long end=SystemClock.elapsedRealtime()+timeout;for(Job job:jobs.values()){long left=end-SystemClock.elapsedRealtime();if(left<=0||!job.exited.await(left,TimeUnit.MILLISECONDS))throw new TimeoutException("Audio provider still reading; retain isolated preferences");if(!job.retired)throw new TimeoutException("Audio import still owned by a screen");}}
}
