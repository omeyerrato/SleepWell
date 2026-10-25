package org.sleepwell.app;
import android.content.*;import android.database.Cursor;import android.net.Uri;import android.os.*;import java.io.*;import java.util.*;
/** Only explicit-result summary files. No directory listing, database or writable surface. */
public final class SleepSummaryFileProvider extends androidx.core.content.FileProvider {
 static final String MIME="application/vnd.org.sleepwell.sleep-summary+json";static final long AGE=24*3600000L;
 private File allowed(Uri uri)throws IOException{if(getContext()==null||uri==null||!(getContext().getPackageName()+".sleep-summary").equals(uri.getAuthority()))throw new FileNotFoundException("Unknown summary document.");List<String> parts=uri.getPathSegments();if(parts.size()!=2||!parts.get(0).equals("summary_documents")||!parts.get(1).matches("summary-[0-9a-f-]{36}\\.json"))throw new FileNotFoundException("Unknown summary document.");File root=SleepSummaryFiles.directory(getContext()),file=new File(root,parts.get(1));long age=System.currentTimeMillis()-file.lastModified();if(!file.getCanonicalFile().equals(file.getAbsoluteFile())||!root.equals(file.getCanonicalFile().getParentFile())||!file.isFile()||file.length()>SleepSummaryFiles.MAX_BYTES||age<0||age>AGE)throw new FileNotFoundException("This summary document expired or is unavailable.");return file;}
 @Override public String getType(Uri uri){try{allowed(uri);return MIME;}catch(IOException unavailable){return null;}}
 @Override public ParcelFileDescriptor openFile(Uri uri,String mode)throws FileNotFoundException{if(!"r".equals(mode))throw new FileNotFoundException("Summary documents are read-only.");try{allowed(uri);}catch(IOException unavailable){throw new FileNotFoundException("Summary document unavailable.");}return super.openFile(uri,"r");}
 @Override public Cursor query(Uri uri,String[] projection,String selection,String[] args,String sort){try{allowed(uri);}catch(IOException unavailable){throw new IllegalArgumentException("Summary document unavailable.");}return super.query(uri,projection,null,null,null);}
 @Override public Uri insert(Uri uri,ContentValues values){throw new UnsupportedOperationException("Read-only summary documents.");}
 @Override public int update(Uri uri,ContentValues values,String selection,String[] args){throw new UnsupportedOperationException("Read-only summary documents.");}
 @Override public int delete(Uri uri,String selection,String[] args){throw new UnsupportedOperationException("Read-only summary documents.");}
 @Override public Bundle call(String method,String argument,Bundle extras){throw new UnsupportedOperationException("Read-only summary documents.");}
}
