package org.sleepwell.app;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/** At most one bounded current-track snapshot. No remote URL is accepted and no bytes are held in RAM. */
final class SoundPlaylistAudio implements AutoCloseable {
    private static final AtomicReference<SoundPlaylistAudio> OWNER=new AtomicReference<>();
    private static final ExecutorService CLOSER=new ThreadPoolExecutor(0,1,30,TimeUnit.SECONDS,new SynchronousQueue<>(),r->new Thread(r,"playlist-document-close"));
    private static final ExecutorService READER=new ThreadPoolExecutor(0,1,30,TimeUnit.SECONDS,new SynchronousQueue<>(),r->new Thread(r,"playlist-document-read"));
    private static boolean cleaned;
    private final Context app;private final CancellationSignal signal=new CancellationSignal();private volatile AssetFileDescriptor descriptor;private volatile boolean cancelled,reading,closeRequested;private File temporary;private long started;
    private SoundPlaylistAudio(Context context){app=context.getApplicationContext();}
    static SoundPlaylistAudio acquire(Context context)throws IOException{SoundPlaylistAudio result=new SoundPlaylistAudio(context);if(!OWNER.compareAndSet(null,result))throw new IOException("A previous playlist document is still closing.");result.cleanAbandoned();return result;}
    private void cleanAbandoned(){synchronized(SoundPlaylistAudio.class){if(cleaned)return;cleaned=true;File[] abandoned=app.getCacheDir().listFiles(f->f.isFile()&&f.getName().startsWith("sleepwell-playlist-")&&f.getName().endsWith(".tmp"));if(abandoned!=null)for(File file:abandoned)try{if(file.getCanonicalFile().getParentFile().equals(app.getCacheDir().getCanonicalFile()))file.delete();}catch(IOException ignored){}}}
    private void check()throws IOException{if(cancelled||Thread.currentThread().isInterrupted())throw new IOException("Playlist read was cancelled.");if(SystemClock.elapsedRealtime()-started>20000)throw new IOException("The document provider took too long.");}
    void readAsync(String source,java.util.function.Consumer<File> ready,java.util.function.Consumer<Exception> failed){reading=true;try{READER.execute(()->{File result=null;Exception error=null;try{result=read(source);}catch(Exception failure){error=failure;}finally{reading=false;if(error!=null||cancelled||closeRequested)try{finishClose();}catch(IOException cleanup){error=cleanup;}}if(error!=null)failed.accept(error);else if(!cancelled)ready.accept(result);});}catch(RejectedExecutionException occupied){reading=false;try{finishClose();}catch(IOException ignored){}failed.accept(new IOException("A previous playlist read is still closing."));}}
    File read(String source)throws IOException{Uri uri=Uri.parse(source);if(!"content".equals(uri.getScheme())||uri.getAuthority()==null)throw new IOException("Choose a local audio document.");started=SystemClock.elapsedRealtime();check();temporary=File.createTempFile("sleepwell-playlist-",".tmp",app.getCacheDir());descriptor=app.getContentResolver().openAssetFileDescriptor(uri,"r",signal);if(descriptor==null)throw new IOException("Audio document is unavailable.");
        try(AssetFileDescriptor fd=descriptor;InputStream in=fd.createInputStream();FileOutputStream out=new FileOutputStream(temporary)){AudioCopy.copy(in,out,SoundPlaylist.MAX_BYTES,this::check,n->{});out.getFD().sync();}finally{descriptor=null;}check();if(temporary.length()<1)throw new IOException("Audio document is empty.");try(android.media.MediaMetadataRetriever metadata=new android.media.MediaMetadataRetriever();FileInputStream input=new FileInputStream(temporary)){metadata.setDataSource(input.getFD());String duration=metadata.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);if(duration==null||Long.parseLong(duration)<=0||!"yes".equals(metadata.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)))throw new IOException("The document no longer contains readable audio.");}catch(IOException error){throw error;}catch(Exception invalid){throw new IOException("The audio document could not be decoded.",invalid);}check();return temporary;
    }
    void cancel(){cancelled=true;try{CLOSER.execute(()->{try{signal.cancel();}catch(RuntimeException ignored){}AssetFileDescriptor fd=descriptor;if(fd!=null)try{fd.close();}catch(IOException ignored){}});}catch(RejectedExecutionException busy){/* The process-wide reader remains owned until its actual worker exits. */}}
    @Override public void close()throws IOException{closeRequested=true;cancel();if(!reading)finishClose();}
    private synchronized void finishClose()throws IOException{AssetFileDescriptor fd=descriptor;if(fd!=null){fd.close();descriptor=null;}if(temporary!=null&&temporary.exists()&&!temporary.delete())throw new IOException("Playlist temporary-file cleanup needs attention.");temporary=null;OWNER.compareAndSet(this,null);}
}
