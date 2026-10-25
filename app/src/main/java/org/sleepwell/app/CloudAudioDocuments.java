package org.sleepwell.app;

import android.content.Context;
import java.io.*;
import java.security.MessageDigest;
import java.util.function.BooleanSupplier;

/** Bounded private staging and full-byte verification before the existing audio import review. */
final class CloudAudioDocuments implements AutoCloseable {
    private static final java.util.Set<String> live=new java.util.HashSet<>();
    final File file;private CloudAudioDocuments(File file){this.file=file;}
    static synchronized File fresh(Context app,String prefix)throws IOException{File cache=app.getCacheDir().getCanonicalFile();File[] files=cache.listFiles();if(files==null)throw new IOException("Private ZIP cache is unavailable.");for(File old:files)if(old.getName().matches(prefix+"[0-9]+[.]zip")&&!live.contains(old.getAbsolutePath())){if(!old.getCanonicalFile().equals(old.getAbsoluteFile())||!cache.equals(old.getCanonicalFile().getParentFile())||!old.isFile()||!old.delete())throw new IOException("An interrupted private ZIP still needs cleanup.");}File created=File.createTempFile(prefix,".zip",cache);live.add(created.getAbsolutePath());return created;}
    static synchronized void remove(File file)throws IOException{try{if(file.exists()&&!file.delete())throw new IOException("Private ZIP staging still needs cleanup.");}finally{live.remove(file.getAbsolutePath());}}
    static CloudAudioDocuments verify(Context app,InputStream source,String expected,BooleanSupplier active)throws Exception{if(expected==null||!expected.matches("[a-f0-9]{64}"))throw new IOException("This audio version has no valid verification receipt.");File file=fresh(app,"cloud-audio-review-");boolean kept=false;try{String sha;try(OutputStream out=new FileOutputStream(file)){sha=copy(source,out,LibraryAudioImport.MAX_ZIP,active);}if(!sha.equals(expected))throw new IOException("This folder version changed or its bytes no longer match the saved receipt. Local data was kept.");kept=true;return new CloudAudioDocuments(file);}finally{if(!kept)remove(file);}}
    static String copy(InputStream source,OutputStream destination,long limit,BooleanSupplier active)throws Exception{MessageDigest digest=MessageDigest.getInstance("SHA-256");byte[] buffer=new byte[65536];long total=0;int empty=0;for(;;){NightAudioArchive.active(active);int n=source.read(buffer);if(n<0)break;if(n==0){if(++empty>16)throw new IOException("The document provider returned no data.");continue;}empty=0;total=Math.addExact(total,n);if(total>limit)throw new IOException("The audio ZIP exceeds its supported size; nothing was silently omitted.");digest.update(buffer,0,n);if(destination!=null)destination.write(buffer,0,n);}return NightAudioArchive.hash(digest.digest());}
    public void close()throws IOException{remove(file);}
}
