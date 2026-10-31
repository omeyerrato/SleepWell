package org.sleepwell.app;

import android.content.Context;
import android.net.Uri;
import android.util.AtomicFile;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import org.json.*;
import static org.junit.Assert.*;

/** Called by the existing isolated native Share journey; no new test campaign or recipient app. */
final class ReportShareAdmissionChecks {
    private static void forgetMemory()throws Exception{Field field=ReportShareAdmissions.class.getDeclaredField("cachedGeneration");field.setAccessible(true);field.set(null,null);}
    private static void unreadable(Context context,Uri uri)throws Exception{try(InputStream input=context.getContentResolver().openInputStream(uri)){fail("An unadmitted, corrupt or retired report must fail closed");}catch(FileNotFoundException expected){}}
    private static void sameBytes(Context context,Uri uri,byte[] expected)throws Exception{try(InputStream input=context.getContentResolver().openInputStream(uri)){assertNotNull(input);assertArrayEquals(expected,input.readAllBytes());}}
    private static void atomic(File file,byte[] bytes)throws Exception{AtomicFile target=new AtomicFile(file);FileOutputStream output=target.startWrite();try{output.write(bytes);target.finishWrite(output);}catch(Exception failed){target.failWrite(output);throw failed;}}
    static void persistedAdmissionSurvivesMemoryLoss(Context context,ReportShareFiles.Snapshot snapshot)throws Exception{
        assertTrue("Only an explicitly shared fixture enters this check",snapshot.shared);Uri uri=snapshot.uri(context);File index=new File(ReportShareFiles.directory(context,false),ReportShareAdmissions.INDEX);byte[] encoded=Files.readAllBytes(index.toPath());assertTrue(encoded.length<=8192);
        JSONObject metadata=new JSONObject(new String(encoded,java.nio.charset.StandardCharsets.UTF_8));assertEquals(3,metadata.length());JSONArray entries=metadata.getJSONArray("admissions");assertTrue(entries.length()<=16);for(int n=0;n<entries.length();n++){JSONObject entry=entries.getJSONObject(n);assertEquals(4,entry.length());assertTrue(entry.has("file")&&entry.has("sha")&&entry.has("bytes")&&entry.has("modified"));}
        forgetMemory();sameBytes(context,uri,snapshot.data.bytes);assertEquals("text/plain",context.getContentResolver().getType(uri));
        // A malformed disk index cannot be bypassed by a cached in-process Snapshot.
        try{atomic(index,"{}".getBytes(java.nio.charset.StandardCharsets.UTF_8));unreadable(context,uri);}finally{atomic(index,encoded);forgetMemory();}
        sameBytes(context,uri,snapshot.data.bytes);unreadable(context,uri.buildUpon().appendQueryParameter("unexpected","1").build());
    }
    static void retiredGenerationRejectsReappearingCache(Context context,ReportShareFiles.Snapshot snapshot)throws Exception{
        assertTrue(snapshot.shared);Uri uri=snapshot.uri(context);File root=ReportShareFiles.directory(context,false),index=new File(root,ReportShareAdmissions.INDEX);byte[] oldIndex=Files.readAllBytes(index.toPath()),oldReport=Files.readAllBytes(snapshot.file.toPath());long modified=snapshot.file.lastModified();
        assertTrue("Private report history cleanup completes",ReportShareFiles.historyChanging(context));assertFalse(snapshot.file.exists());
        // Simulate old cache surviving/reappearing after cleanup, plus loss of process memory.
        try{Files.write(snapshot.file.toPath(),oldReport);assertTrue(snapshot.file.setLastModified(modified));atomic(index,oldIndex);forgetMemory();unreadable(context,uri);}finally{assertTrue(ReportShareFiles.historyChanging(context));}
        assertFalse(snapshot.file.exists());
    }
    private ReportShareAdmissionChecks(){}
}
