package org.sleepwell.app;

import android.content.Context;
import android.util.AtomicFile;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.json.*;

/** Bounded private admission metadata, never report text, notes or record/model identifiers. */
final class ReportShareAdmissions {
    static final String PREFS="private-report-share-admissions-v1",INDEX="admitted-v1.json";
    private static final int MAX_INDEX=8192;
    private static volatile String cachedGeneration;
    static final class Entry {
        final String file,sha;final long bytes,modified;
        Entry(String file,String sha,long bytes,long modified){this.file=file;this.sha=sha;this.bytes=bytes;this.modified=modified;}
    }
    // All callers hold ReportShareFiles' lock; no SleepSummaryFiles monitor is acquired here.
    static String generation(Context app,boolean create)throws IOException{
        if(cachedGeneration!=null)return cachedGeneration;
        try{
            android.content.SharedPreferences p=app.getSharedPreferences(PREFS,Context.MODE_PRIVATE);String token=p.getString("generation","");
            if(token.isEmpty()&&create){token=UUID.randomUUID().toString();if(!p.edit().putString("generation",token).commit())throw new IOException("Private report admission could not be saved.");}
            if(!BackupRuntime.nonce(token))throw new IOException("Private report admission is unavailable.");cachedGeneration=token;return token;
        }catch(ClassCastException invalid){throw new IOException("Private report admission is unreadable.",invalid);}
    }
    static boolean cached(String token){return token.equals(cachedGeneration);}
    static void rotate(Context app)throws IOException{
        String token=UUID.randomUUID().toString();
        if(!app.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putString("generation",token).commit())throw new IOException("Private report admission could not be invalidated.");
        cachedGeneration=token;
    }
    static boolean metadata(File file){String name=file.getName();return name.equals(INDEX)||name.equals(INDEX+".bak")||name.equals(INDEX+".new");}
    private static void paths(File root)throws IOException{for(String suffix:new String[]{"",".bak",".new"}){File file=new File(root,INDEX+suffix);if(!file.getCanonicalFile().equals(file.getAbsoluteFile())||!root.equals(file.getCanonicalFile().getParentFile())||file.exists()&&!file.isFile())throw new IOException("Private report admission storage is unavailable.");}}
    private static long whole(JSONObject row,String key,long maximum)throws JSONException{Object raw=row.get(key);if(!(raw instanceof Number))throw new JSONException("Invalid admission number");double value=((Number)raw).doubleValue();if(!Double.isFinite(value)||value!=Math.rint(value)||value<1||value>maximum)throw new JSONException("Invalid admission number");return((Number)raw).longValue();}
    static Map<String,Entry> read(Context app,File root)throws IOException{
        paths(root);File base=new File(root,INDEX);if(!base.exists()&&!new File(root,INDEX+".bak").exists())return new LinkedHashMap<>();
        byte[] bytes;try(InputStream input=new AtomicFile(base).openRead();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buffer=new byte[1024];int total=0;for(int n;(n=input.read(buffer))!=-1;){total+=n;if(total>MAX_INDEX)throw new IOException("Private report admission exceeds its limit.");out.write(buffer,0,n);}bytes=out.toByteArray();}
        try{
            String text=new String(bytes,StandardCharsets.UTF_8);try{BackupIo.jsonDepth(text);}catch(IOException invalid){throw new IOException("Private report admission is unreadable.",invalid);}JSONObject document=new JSONObject(text);if(document.length()!=3||whole(document,"version",1)!=1)throw new JSONException("Unsupported admission metadata");
            String token=document.getString("generation");if(!BackupRuntime.nonce(token))throw new JSONException("Invalid admission generation");JSONArray entries=document.getJSONArray("admissions");if(entries.length()>ReportShareFiles.MAX_FILES)throw new JSONException("Too many report admissions");Map<String,Entry> result=new LinkedHashMap<>();
            for(int n=0;n<entries.length();n++){JSONObject row=entries.getJSONObject(n);String name=row.getString("file"),sha=row.getString("sha");if(row.length()!=4||!name.matches("report-[0-9a-f-]{36}\\.txt")||!BackupRuntime.nonce(name.substring(7,name.length()-4))||!sha.matches("[0-9a-f]{64}")||result.containsKey(name))throw new JSONException("Invalid report admission");result.put(name,new Entry(name,sha,whole(row,"bytes",ReportShareFiles.MAX_BYTES),whole(row,"modified",Long.MAX_VALUE)));}
            return token.equals(generation(app,false))?result:new LinkedHashMap<>();
        }catch(JSONException|IllegalArgumentException invalid){throw new IOException("Private report admission is unreadable.",invalid);}
    }
    static void write(Context app,File root,Map<String,Entry> entries)throws IOException{
        if(entries.size()>ReportShareFiles.MAX_FILES)throw new IOException("Too many private report admissions.");paths(root);
        byte[] bytes;try{JSONArray rows=new JSONArray();for(Entry entry:entries.values())rows.put(new JSONObject().put("file",entry.file).put("sha",entry.sha).put("bytes",entry.bytes).put("modified",entry.modified));bytes=new JSONObject().put("version",1).put("generation",generation(app,false)).put("admissions",rows).toString().getBytes(StandardCharsets.UTF_8);}catch(JSONException invalid){throw new IOException("Private report admission could not be encoded.",invalid);}if(bytes.length>MAX_INDEX)throw new IOException("Private report admission exceeds its limit.");
        AtomicFile file=new AtomicFile(new File(root,INDEX));FileOutputStream output=file.startWrite();try{output.write(bytes);file.finishWrite(output);}catch(IOException|RuntimeException failed){file.failWrite(output);throw failed;}
    }
    private ReportShareAdmissions(){}
}
