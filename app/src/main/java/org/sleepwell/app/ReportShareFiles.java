package org.sleepwell.app;

import android.content.*;
import android.net.Uri;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;

/** Private, immutable previews. Admission starts only at explicit Share, separately from public GET_CONTENT. */
final class ReportShareFiles {
    static final int MAX_BYTES=128*1024,MAX_FILES=16;static final long AGE=10*60000L;
    private static final Object LOCK=new Object();private static final Map<String,Snapshot> ADMITTED=new ConcurrentHashMap<>();
    static final class Snapshot {
        final File file;final ReportShareData data;final String sha;final long epoch;volatile boolean shared;
        Snapshot(File file,ReportShareData data,String sha,long epoch){this.file=file;this.data=data;this.sha=sha;this.epoch=epoch;}
        Uri uri(Context app){return androidx.core.content.FileProvider.getUriForFile(app,app.getPackageName()+".report-share",file);}
        void verify(BooleanSupplier active)throws Exception{
            ReportShareData.check(()->active.getAsBoolean()&&SleepSummaryFiles.current(epoch));long age=System.currentTimeMillis()-file.lastModified();
            if(!file.isFile()||file.length()!=data.bytes.length||age<0||age>AGE)throw new IOException("This preview expired. Prepare it again.");
            MessageDigest digest=MessageDigest.getInstance("SHA-256");long count=0;
            try(InputStream input=new FileInputStream(file)){byte[] buffer=new byte[8192];for(int n;(n=input.read(buffer))!=-1;){ReportShareData.check(()->active.getAsBoolean()&&SleepSummaryFiles.current(epoch));count+=n;if(count>MAX_BYTES)throw new IOException("This preview changed.");digest.update(buffer,0,n);}}
            ReportShareData.check(()->active.getAsBoolean()&&SleepSummaryFiles.current(epoch));if(count!=data.bytes.length||!NightAudioArchive.hash(digest.digest()).equals(sha))throw new IOException("This preview changed. Prepare it again.");
        }
        void discard(){if(!shared||!SleepSummaryFiles.current(epoch))file.delete();}
    }
    static File directory(Context app,boolean create)throws IOException{
        File cache=app.getCacheDir().getCanonicalFile(),lexical=new File(cache,"private-report-previews").getAbsoluteFile(),root=lexical.getCanonicalFile();
        if(!root.equals(lexical)||!cache.equals(root.getParentFile())||root.exists()&&!root.isDirectory()||create&&!root.isDirectory()&&!root.mkdirs())throw new IOException("Private preview storage is unavailable.");return root;
    }
    static Snapshot stage(Context app,ReportShareData data,long epoch,BooleanSupplier active)throws Exception{
        synchronized(LOCK){if(data==null||data.bytes.length>MAX_BYTES)throw new IOException("This preview exceeds 128 KiB.");ReportShareData.check(()->active.getAsBoolean()&&SleepSummaryFiles.current(epoch));File root=directory(app,true);File[] files=root.listFiles();if(files==null||files.length>MAX_FILES*2)throw new IOException("Private preview storage needs attention.");int recent=0;
            for(File file:files){if(!owned(root,file))throw new IOException("Private preview storage contains an unknown entry.");long age=System.currentTimeMillis()-file.lastModified();if(age<0||age>AGE){revoke(app,file);if(!file.delete()&&file.exists())throw new IOException("An expired preview could not be removed.");}else recent++;}
            if(recent>=MAX_FILES)throw new IOException("16 recent previews are still available. They expire after ten minutes.");
            File pending=new File(root,"pending-"+UUID.randomUUID()+".txt"),target=new File(root,"report-"+UUID.randomUUID()+".txt");boolean saved=false;
            try{try(FileOutputStream output=new FileOutputStream(pending)){for(int at=0;at<data.bytes.length;at+=8192){ReportShareData.check(()->active.getAsBoolean()&&SleepSummaryFiles.current(epoch));output.write(data.bytes,at,Math.min(8192,data.bytes.length-at));}output.getFD().sync();}ReportShareData.check(()->active.getAsBoolean()&&SleepSummaryFiles.current(epoch));if(!pending.renameTo(target))throw new IOException("This preview could not be staged.");saved=true;return new Snapshot(target,data,ReportShareData.hash(data.bytes),epoch);}finally{pending.delete();if(!saved)target.delete();}
        }
    }
    private static boolean owned(File root,File file)throws IOException{return file.isFile()&&file.getCanonicalFile().equals(file.getAbsoluteFile())&&root.equals(file.getCanonicalFile().getParentFile())&&file.getName().matches("(report|pending)-[0-9a-f-]{36}\\.txt");}
    static Uri admit(Context app,Snapshot snapshot){synchronized(LOCK){if(snapshot==null||!SleepSummaryFiles.current(snapshot.epoch)||!snapshot.file.isFile()||snapshot.file.length()!=snapshot.data.bytes.length||System.currentTimeMillis()-snapshot.file.lastModified()<0||System.currentTimeMillis()-snapshot.file.lastModified()>AGE)throw new IllegalStateException("This preview changed or expired. Prepare it again.");Uri uri=snapshot.uri(app);ADMITTED.put(snapshot.file.getName(),snapshot);snapshot.shared=true;return uri;}}
    static Snapshot allowed(Context app,Uri uri)throws IOException{
        if(uri==null||!(app.getPackageName()+".report-share").equals(uri.getAuthority())||uri.getPathSegments().size()!=2||!uri.getPathSegments().get(0).equals("report_previews"))throw new FileNotFoundException("Unknown private report.");
        String name=uri.getLastPathSegment();Snapshot snapshot=ADMITTED.get(name);File root=directory(app,false);
        if(snapshot==null||!SleepSummaryFiles.current(snapshot.epoch)||!owned(root,snapshot.file)||!snapshot.file.getName().equals(name))throw new FileNotFoundException("This report has not been shared or is no longer available.");
        long age=System.currentTimeMillis()-snapshot.file.lastModified();if(age<0||age>AGE||snapshot.file.length()!=snapshot.data.bytes.length||snapshot.file.length()>MAX_BYTES)throw new FileNotFoundException("This shared report expired.");return snapshot;
    }
    static void revoke(Context app,Snapshot snapshot){synchronized(LOCK){snapshot.shared=false;revoke(app,snapshot.file);snapshot.file.delete();}}
    private static void revoke(Context app,File file){ADMITTED.remove(file.getName());if(file.getName().startsWith("report-"))app.revokeUriPermission(androidx.core.content.FileProvider.getUriForFile(app,app.getPackageName()+".report-share",file),Intent.FLAG_GRANT_READ_URI_PERMISSION);}
    /** Best effort result lets the existing history cleanup still attempt its other scopes. */
    static boolean historyChanging(Context app){synchronized(LOCK){ADMITTED.clear();try{File root=directory(app,false);if(!root.exists())return true;File[] files=root.listFiles();if(files==null||files.length>MAX_FILES*2)return false;boolean ok=true;for(File file:files){if(!owned(root,file)){ok=false;continue;}try{revoke(app,file);}catch(RuntimeException unavailable){ok=false;}if(!file.delete()&&file.exists())ok=false;}return ok;}catch(IOException|RuntimeException unavailable){return false;}}}
    private ReportShareFiles(){}
}
