package org.sleepwell.app;

import android.content.*;
import android.net.Uri;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.BooleanSupplier;

/** Private, immutable previews. Admission starts only at explicit Share, separately from public GET_CONTENT. */
final class ReportShareFiles {
    static final int MAX_BYTES=128*1024,MAX_FILES=16;static final long AGE=10*60000L;
    private static final Object LOCK=new Object();
    static final class Snapshot {
        final File file;final ReportShareData data;final String sha;final long epoch;volatile boolean shared;volatile String admittedGeneration="";
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
            for(File file:files){if(ReportShareAdmissions.metadata(file)){if(file.exists()&&!safe(root,file))throw new IOException("Private preview admission storage is unavailable.");continue;}if(!owned(root,file))throw new IOException("Private preview storage contains an unknown entry.");long age=System.currentTimeMillis()-file.lastModified();if(age<0||age>AGE){revoke(app,file);if(!file.delete()&&file.exists())throw new IOException("An expired preview could not be removed.");}else recent++;}
            if(recent>=MAX_FILES)throw new IOException("16 recent previews are still available. They expire after ten minutes.");
            File pending=new File(root,"pending-"+UUID.randomUUID()+".txt"),target=new File(root,"report-"+UUID.randomUUID()+".txt");boolean saved=false;
            try{try(FileOutputStream output=new FileOutputStream(pending)){for(int at=0;at<data.bytes.length;at+=8192){ReportShareData.check(()->active.getAsBoolean()&&SleepSummaryFiles.current(epoch));output.write(data.bytes,at,Math.min(8192,data.bytes.length-at));}output.getFD().sync();}ReportShareData.check(()->active.getAsBoolean()&&SleepSummaryFiles.current(epoch));if(!pending.renameTo(target))throw new IOException("This preview could not be staged.");saved=true;return new Snapshot(target,data,ReportShareData.hash(data.bytes),epoch);}finally{pending.delete();if(!saved)target.delete();}
        }
    }
    private static boolean safe(File root,File file)throws IOException{return file.isFile()&&file.getCanonicalFile().equals(file.getAbsoluteFile())&&root.equals(file.getCanonicalFile().getParentFile());}
    private static boolean owned(File root,File file)throws IOException{return safe(root,file)&&file.getName().matches("(report|pending)-[0-9a-f-]{36}\\.txt");}
    /** Background verification follows explicit Share; admission is durable before the visible chooser callback. */
    static void prepareAdmission(Context app,Snapshot snapshot,BooleanSupplier active)throws Exception{
        synchronized(LOCK){ReportShareData.check(()->active.getAsBoolean()&&SleepSummaryFiles.current(snapshot.epoch));snapshot.verify(active);
            String token=ReportShareAdmissions.generation(app,true);File root=directory(app,false);if(!owned(root,snapshot.file))throw new IOException("Private report is unavailable.");Map<String,ReportShareAdmissions.Entry> entries=ReportShareAdmissions.read(app,root);
            long now=System.currentTimeMillis();Iterator<ReportShareAdmissions.Entry> old=entries.values().iterator();while(old.hasNext()){ReportShareAdmissions.Entry entry=old.next();File file=new File(root,entry.file);long age=now-entry.modified;if(age<0||age>AGE||!owned(root,file)||file.length()!=entry.bytes||file.lastModified()!=entry.modified)old.remove();}
            if(!entries.containsKey(snapshot.file.getName())&&entries.size()>=MAX_FILES)throw new IOException("16 shared previews are still available. They expire after ten minutes.");
            ReportShareData.check(()->active.getAsBoolean()&&SleepSummaryFiles.current(snapshot.epoch));entries.put(snapshot.file.getName(),new ReportShareAdmissions.Entry(snapshot.file.getName(),snapshot.sha,snapshot.data.bytes.length,snapshot.file.lastModified()));ReportShareAdmissions.write(app,root,entries);
            ReportShareData.check(()->active.getAsBoolean()&&SleepSummaryFiles.current(snapshot.epoch));snapshot.admittedGeneration=token;
        }
    }
    /** Visible callback only: no metadata file or preference write on the UI dispatcher. */
    static Uri admit(Context app,Snapshot snapshot){synchronized(LOCK){if(snapshot==null||!SleepSummaryFiles.current(snapshot.epoch)||snapshot.admittedGeneration.isEmpty()||!ReportShareAdmissions.cached(snapshot.admittedGeneration))throw new IllegalStateException("This preview changed or expired. Prepare it again.");Uri uri=snapshot.uri(app);snapshot.shared=true;return uri;}}
    /** A cancelled first Share has no recipient grant; retain its private preview but retire admission. */
    static void cancelPrepared(Context app,Snapshot snapshot){synchronized(LOCK){if(snapshot==null||snapshot.shared)return;snapshot.admittedGeneration="";try{File root=directory(app,false);Map<String,ReportShareAdmissions.Entry> entries=ReportShareAdmissions.read(app,root);if(entries.remove(snapshot.file.getName())!=null)ReportShareAdmissions.write(app,root,entries);}catch(IOException|RuntimeException unavailable){/* No URI grant was issued; file expiry/history invalidation remain mandatory. */}}}
    static final class SharedFile {
        final File file;final long bytes,modified,epoch;final String sha,generation;
        SharedFile(File file,ReportShareAdmissions.Entry entry,String generation){this.file=file;bytes=entry.bytes;modified=entry.modified;sha=entry.sha;this.generation=generation;epoch=SleepSummaryFiles.epoch();}
        void verify(Context app)throws Exception{
            if(!SleepSummaryFiles.current(epoch)||!currentGeneration(app,generation))throw new IOException("This report is no longer shared.");long age=System.currentTimeMillis()-modified;if(age<0||age>AGE||!file.isFile()||file.length()!=bytes||file.lastModified()!=modified)throw new IOException("This report expired or changed.");
            MessageDigest digest=MessageDigest.getInstance("SHA-256");long count=0;try(InputStream input=new FileInputStream(file)){byte[] buffer=new byte[8192];for(int n;(n=input.read(buffer))!=-1;){if(!SleepSummaryFiles.current(epoch)||Thread.currentThread().isInterrupted())throw new IOException("Report read was invalidated.");count+=n;if(count>bytes||count>MAX_BYTES)throw new IOException("This report changed.");digest.update(buffer,0,n);}}
            if(!SleepSummaryFiles.current(epoch)||!currentGeneration(app,generation)||count!=bytes||!NightAudioArchive.hash(digest.digest()).equals(sha))throw new IOException("This report changed or is no longer shared.");
        }
    }
    private static boolean currentGeneration(Context app,String expected)throws IOException{synchronized(LOCK){return expected.equals(ReportShareAdmissions.generation(app,false));}}
    static SharedFile allowed(Context app,Uri uri)throws IOException{synchronized(LOCK){
        if(uri==null||!"content".equals(uri.getScheme())||!(app.getPackageName()+".report-share").equals(uri.getAuthority())||uri.getQuery()!=null||uri.getFragment()!=null||uri.getPathSegments().size()!=2||!uri.getPathSegments().get(0).equals("report_previews"))throw new FileNotFoundException("Unknown private report.");
        String name=uri.getLastPathSegment();File root=directory(app,false);String token=ReportShareAdmissions.generation(app,false);ReportShareAdmissions.Entry entry=ReportShareAdmissions.read(app,root).get(name);
        if(entry==null)throw new FileNotFoundException("This report has not been shared or is no longer available.");File file=new File(root,name);long age=System.currentTimeMillis()-entry.modified;
        if(!owned(root,file)||age<0||age>AGE||file.length()!=entry.bytes||file.lastModified()!=entry.modified)throw new FileNotFoundException("This shared report expired or changed.");return new SharedFile(file,entry,token);
    }}
    static void revoke(Context app,Snapshot snapshot){synchronized(LOCK){snapshot.shared=false;snapshot.admittedGeneration="";revoke(app,snapshot.file);snapshot.file.delete();}}
    private static void revoke(Context app,File file){try{File root=directory(app,false);Map<String,ReportShareAdmissions.Entry> entries=ReportShareAdmissions.read(app,root);if(entries.remove(file.getName())!=null)ReportShareAdmissions.write(app,root,entries);revokeUri(app,file);}catch(IOException failed){throw new IllegalStateException("Private report access could not be fully revoked.",failed);}}
    private static void revokeUri(Context app,File file){if(file.getName().startsWith("report-"))app.revokeUriPermission(androidx.core.content.FileProvider.getUriForFile(app,app.getPackageName()+".report-share",file),Intent.FLAG_GRANT_READ_URI_PERMISSION);}
    /** Rotate durable admission BEFORE file/grant cleanup; caller aborts history mutation on failure. */
    static boolean historyChanging(Context app){synchronized(LOCK){boolean ok=true;try{ReportShareAdmissions.rotate(app);}catch(IOException|RuntimeException unavailable){ok=false;}
        try{File root=directory(app,false);if(!root.exists())return ok;File[] files=root.listFiles();if(files==null||files.length>MAX_FILES*2)return false;
            for(File file:files){if(ReportShareAdmissions.metadata(file)){if(!file.exists())continue;if(!safe(root,file)){ok=false;continue;}}else{if(!owned(root,file)){ok=false;continue;}try{revokeUri(app,file);}catch(RuntimeException unavailable){ok=false;}}
                if(!file.delete()&&file.exists())ok=false;
            }return ok;
        }catch(IOException|RuntimeException unavailable){return false;}
    }}
    private ReportShareFiles(){}
}
