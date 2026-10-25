package org.sleepwell.app;

import android.content.Context;
import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;

/** One bounded, private immutable export snapshot. No provider can expose this cache. */
final class AudioArchiveWorkspace implements AutoCloseable {
    private static final Semaphore ACTIVE=new Semaphore(1);
    private static final int MAX_FILES=999;final File directory;private final int capacity;private boolean closed;
    private AudioArchiveWorkspace(File directory,int capacity){this.directory=directory;this.capacity=capacity;}
    static AudioArchiveWorkspace create(Context context)throws IOException{return create(context,NightAudioArchive.MAX_CLIPS);}
    static AudioArchiveWorkspace create(Context context,int capacity)throws IOException{
        if(capacity<1||capacity>MAX_FILES)throw new IOException("Unsupported audio export size.");
        if(!ACTIVE.tryAcquire())throw new IOException("Another audio export is still finishing.");
        try{
            File cache=context.getCacheDir().getCanonicalFile(),root=new File(cache,"recording-exports").getCanonicalFile();
            if(!cache.equals(root.getParentFile()))throw new IOException("Invalid private export cache.");
            if(!root.isDirectory()&&!root.mkdirs())throw new IOException("Private export cache unavailable.");
            File[] stale=root.listFiles();if(stale==null||stale.length>256)throw new IOException("Private export cache could not be inspected.");
            // The permit is held before stale cleanup, so no live snapshot can be removed.
            for(File entry:stale)if(entry.getName().matches("export-[0-9a-f-]{36}"))remove(root,entry);
            File directory=new File(root,"export-"+UUID.randomUUID());if(!directory.mkdir())throw new IOException("Private export cache unavailable.");
            return new AudioArchiveWorkspace(directory,capacity);
        }catch(IOException|RuntimeException failure){ACTIVE.release();throw failure;}
    }
    File file(int index)throws IOException{if(closed||index<0||index>=capacity)throw new IOException("Export snapshot unavailable.");return new File(directory,String.format(Locale.ROOT,"%03d.wav",index+1));}
    private static void remove(File root,File directory)throws IOException{
        if(!directory.getCanonicalFile().equals(directory.getAbsoluteFile())||!directory.getCanonicalFile().getParentFile().equals(root)||!directory.isDirectory())throw new IOException("Invalid private export cache.");
        File[] files=directory.listFiles();if(files==null||files.length>MAX_FILES)throw new IOException("Private export cache could not be cleaned.");
        for(File file:files){if(!file.getName().matches("[0-9]{3}\\.wav")||!file.getCanonicalFile().equals(file.getAbsoluteFile())||!file.getCanonicalFile().getParentFile().equals(directory.getCanonicalFile())||!file.isFile()||!file.delete())throw new IOException("Private export cache could not be cleaned.");}
        if(!directory.delete())throw new IOException("Private export cache could not be cleaned.");
    }
    @Override public void close()throws IOException{if(closed)return;closed=true;try{remove(directory.getParentFile(),directory);}finally{ACTIVE.release();}}
}
