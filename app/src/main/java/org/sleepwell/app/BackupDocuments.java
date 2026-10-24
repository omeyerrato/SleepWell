package org.sleepwell.app;

import android.content.*;
import android.net.Uri;
import android.os.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.BooleanSupplier;
import org.json.*;

/** Application-owned ports. Providers, databases and cleanup never run on the UI dispatcher. */
final class BackupDocuments implements BackupJob.Work {
    interface Transport {InputStream input(String uri)throws IOException;OutputStream output(String uri)throws IOException;void cancel();}
    interface Effects {void idle(long admission);void retired(List<Alarm> old,Store current);void recover(Store store);}
    interface Factory {BackupJob.Work create(Context app,Records.Factory records);}
    static Factory factory=BackupDocuments::new;
    private static final Executor CANCEL=new ThreadPoolExecutor(0,1,10,TimeUnit.SECONDS,new ArrayBlockingQueue<>(2),r->{Thread t=new Thread(r,"backup-document-cancel");t.setDaemon(true);return t;},new ThreadPoolExecutor.DiscardPolicy());
    private static final class NativeTransport implements Transport {
        final ContentResolver resolver;final AtomicReference<CancellationSignal> signal=new AtomicReference<>();final AtomicReference<ParcelFileDescriptor> descriptor=new AtomicReference<>();
        NativeTransport(Context app){resolver=app.getContentResolver();}
        private ParcelFileDescriptor open(String uri,String mode)throws IOException{CancellationSignal own=new CancellationSignal();signal.set(own);ParcelFileDescriptor file=resolver.openFileDescriptor(Uri.parse(uri),mode,own);if(file==null)throw new IOException("Document unavailable.");descriptor.set(file);return file;}
        public InputStream input(String uri)throws IOException{return new ParcelFileDescriptor.AutoCloseInputStream(open(uri,"r"));}
        public OutputStream output(String uri)throws IOException{return new ParcelFileDescriptor.AutoCloseOutputStream(open(uri,"wt"));}
        public void cancel(){CancellationSignal pending=signal.get();ParcelFileDescriptor file=descriptor.get();CANCEL.execute(()->{if(pending!=null)pending.cancel();if(file!=null)try{file.close();}catch(IOException ignored){}});}
    }
    private final Context app;private final Records.Factory records;private final Transport transport;private final Effects effects;
    BackupDocuments(Context app,Records.Factory records){this(app,records,new NativeTransport(app),new Effects(){
        private long expected;
        public void idle(long admission){BackupRuntime.requireIdle(app,admission);expected=admission;}
        public void retired(List<Alarm> previous,Store current){BackupRuntime.requireIdle(app,expected);for(Alarm old:previous){BackupRuntime.requireIdle(app,expected);Alarm fresh=current.alarm(old.id);if(fresh!=null&&fresh.enabled)continue;Naps.cancelJob(app,old);for(String action:new String[]{Scheduler.CHECK,Scheduler.FIRE,Scheduler.SNOOZE,Scheduler.BACKUP}){BackupRuntime.requireIdle(app,expected);Scheduler.cancel(app,old.id,action);}}BackupRuntime.requireIdle(app,expected);Scheduler.cancel(app,-1,Scheduler.TEST);}
        public void recover(Store store){SoundClips.recover(app,store);}
    });}
    BackupDocuments(Context app,Records.Factory records,Transport transport,Effects effects){this.app=app.getApplicationContext();this.records=records;this.transport=transport;this.effects=effects;}
    @Override public BackupJob.Review read(String uri,BooleanSupplier active)throws Exception {
        background();long admission=BackupRuntime.generation();effects.idle(admission);String document;try(InputStream input=transport.input(uri)){document=BackupIo.read(input,active);}BackupIo.active(active);BackupIo.jsonDepth(document);JSONObject root;try{root=new JSONObject(document);Backup.validate(root);}catch(JSONException|IllegalArgumentException invalid){throw new IOException("This is not a valid SleepWell backup.",invalid);}effects.idle(admission);
        String fingerprint;try(Store store=records.open(app)){android.database.sqlite.SQLiteDatabase db=store.getReadableDatabase();db.beginTransactionNonExclusive();try{fingerprint=BackupFingerprint.read(store);db.setTransactionSuccessful();}finally{db.endTransaction();}}
        effects.idle(admission);BackupIo.active(active);int format=root.getInt("format"),labels=0;if(format>=4){JSONArray rows=root.getJSONArray("sound_events");for(int at=0;at<rows.length();at++)if(!rows.getJSONObject(at).isNull("user_category"))labels++;}
        String summary=count(root.getJSONArray("alarms").length(),"alarm")+" · "+count(root.getJSONArray("sessions").length(),"sleep record")+(format>=4?"\n"+count(root.getJSONArray("sound_events").length(),"sound estimate")+" · "+count(labels,"user label"):"\nLegacy format "+format)+"\nImported alarms stay off. Audio and app settings are excluded.";
        return new BackupJob.Review(document,fingerprint,summary,admission);
    }
    @Override public void write(BackupJob.Kind kind,String uri,BooleanSupplier active)throws Exception {
        background();byte[] bytes;try(Store store=records.open(app)){BackupIo.active(active);if(kind==BackupJob.Kind.JSON)bytes=BackupIo.encode(store.backup().toString(2));else bytes=csv(store,active);}BackupIo.active(active);
        // Prepare and size-check completely before opening/truncating a destination.
        try(OutputStream output=transport.output(uri)){BackupIo.write(output,bytes,active);}
    }
    static byte[] csv(Store store,BooleanSupplier active)throws IOException {
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(Writer out=new OutputStreamWriter(BackupIo.limited(bytes),StandardCharsets.UTF_8)){
            out.write("start_iso,end_iso,duration_minutes,interrupted,rating,notes,source,origin,edited,zone,original_start_iso,original_end_iso,tags,awake_periods_utc,sensor,sensor_config,sound_analysis,health_connect\r\n");
            for(RecordEntry record:Records.all(store)){BackupIo.active(active);JSONArray awake=new JSONArray();for(RecordIntervals.Span span:record.edit.awake)awake.put(new JSONArray().put(java.time.Instant.ofEpochMilli(span.start).toString()).put(java.time.Instant.ofEpochMilli(span.end).toString()));
                out.write(java.time.Instant.ofEpochMilli(record.edit.start)+","+java.time.Instant.ofEpochMilli(record.edit.end)+","+record.duration()/60000+","+record.interrupted+","+(record.edit.rated?record.edit.rating:"")+","+SleepMath.csv(record.edit.note)+","+record.source+","+record.origin+","+record.edited+","+SleepMath.csv(record.zone)+","+java.time.Instant.ofEpochMilli(record.rawStart)+","+java.time.Instant.ofEpochMilli(record.rawEnd)+","+SleepMath.csv(new JSONArray(record.edit.tags).toString())+","+SleepMath.csv(awake.toString())+","+SleepMath.csv(record.sensor)+","+SleepMath.csv(record.sensorConfig)+","+SleepMath.csv(record.soundAnalysis)+","+SleepMath.csv(record.healthConnect)+"\r\n");
            }
            for(JSONObject row:store.rows("SELECT * FROM sessions WHERE end IS NULL ORDER BY start")){BackupIo.active(active);out.write(java.time.Instant.ofEpochMilli(row.optLong("start"))+",,,,,"+SleepMath.csv(row.optString("note",""))+","+row.optString("source","legacy")+","+row.optString("origin","legacy")+",false,"+SleepMath.csv(row.optString("zone",""))+","+java.time.Instant.ofEpochMilli(row.optLong("start"))+",,[],[],"+SleepMath.csv(row.optString("sensor","unspecified"))+","+SleepMath.csv(row.optString("sensor_config",""))+","+SleepMath.csv(row.optString("sound_analysis",""))+","+SleepMath.csv(row.optString("health_connect",""))+"\r\n");}
        }return bytes.toByteArray();
    }
    @Override public String restore(BackupJob.Review review)throws Exception {
        background();effects.idle(review.admission);JSONObject root=new JSONObject(review.document);Backup.validate(root);List<Alarm> old=new ArrayList<>();Store store=null;boolean committed=false;Exception failure=null;String message="";
        try{store=records.open(app);final Store owned=store;
            AtomicBoolean first=new AtomicBoolean(true);
            Backup.restoreData(store,root,()->{effects.idle(review.admission);if(first.getAndSet(false)){if(!review.fingerprint.equals(BackupFingerprint.read(owned)))throw new IllegalStateException("Local data changed. Choose the backup again to review the latest data.");old.addAll(owned.alarms());}effects.idle(review.admission);});committed=true;
            // Replacement committed. Cleanup failure must never be reported as an uncommitted restore.
            String warning="";try{effects.retired(old,store);}catch(RuntimeException cleanupFailure){warning=" Some old alarm schedules need review.";}
            try{effects.recover(store);}catch(RuntimeException cleanupFailure){warning+=" Audio cleanup needs attention.";}
            int pending=store.rows("SELECT clip FROM pending_clip_deletes").size();
            message="Data restored. Review and enable your alarms."+warning+(pending>0?" "+pending+" audio files still need cleanup in Data & privacy.":"");
        }catch(Exception error){failure=error;}finally{if(store!=null)try{store.close();}catch(Exception close){if(failure!=null)failure.addSuppressed(close);else failure=close;}}
        if(!committed){if(failure!=null)throw failure;throw new IllegalStateException("Replacement did not start.");}
        return failure==null?message:"Data restored. Cleanup or storage verification needs attention. Review alarms and Data & privacy before importing again.";
    }
    private static String count(int value,String noun){return value+" "+noun+(value==1?"":"s");}
    @Override public void cancel(){transport.cancel();}
    private static void background(){if(Looper.myLooper()==Looper.getMainLooper())throw new IllegalStateException("Document work must run in the background.");}
}
