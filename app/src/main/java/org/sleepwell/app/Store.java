package org.sleepwell.app;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import org.json.*;
import java.util.*;

public class Store extends SQLiteOpenHelper {
    private final Context surfaceContext;private boolean surfaceDirty;
    void surfaceChanged(){surfaceDirty=true;try{if(!getReadableDatabase().inTransaction()){surfaceDirty=false;HomeSurfaces.changed(surfaceContext);}}catch(RuntimeException ignored){}}
    @Override public synchronized void close(){try{super.close();}finally{if(surfaceDirty){surfaceDirty=false;HomeSurfaces.changed(surfaceContext);}}}
    public Store(Context c) { this(c,"sleepwell.db"); }
    Store(Context c,String name) { super(c,name,null,17);surfaceContext=c.getApplicationContext(); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE alarms(id INTEGER PRIMARY KEY, data TEXT NOT NULL)");
        db.execSQL("CREATE TABLE sessions(id INTEGER PRIMARY KEY, start INTEGER NOT NULL, end INTEGER, note TEXT DEFAULT '', rating INTEGER DEFAULT 0, interrupted INTEGER DEFAULT 0, active_ms INTEGER, clock_changed INTEGER DEFAULT 0)");
        db.execSQL("CREATE TABLE epochs(id INTEGER PRIMARY KEY, session INTEGER NOT NULL, time INTEGER NOT NULL, movement REAL, noise REAL, light REAL, samples INTEGER, start INTEGER)");
        db.execSQL("CREATE INDEX epochs_session ON epochs(session,time)");
        db.execSQL("CREATE TABLE clips(id INTEGER PRIMARY KEY, session INTEGER NOT NULL, time INTEGER NOT NULL, path TEXT NOT NULL, level REAL)");
        createPauses(db);
        createRecords(db);db.execSQL("ALTER TABLE sessions ADD COLUMN source TEXT NOT NULL DEFAULT 'legacy'");db.execSQL("ALTER TABLE sessions ADD COLUMN origin TEXT NOT NULL DEFAULT 'legacy'");db.execSQL("ALTER TABLE sessions ADD COLUMN zone TEXT NOT NULL DEFAULT ''");
        createRequests(db);createSoundEvents(db);createSoundLibrary(db);createSoundLabels(db);createSensorProvenance(db);createSoundAnalysis(db);HealthSleepTables.create(db);AwakeStore.create(db);EchoPeriods.create(db);PairMotionTables.create(db);PirStore.create(db);DreamCueTables.create(db);SoundEventBackup.createClipFavorites(db);SleepCsvEvidence.createTables(db);
    }
    private static void createSoundEvents(SQLiteDatabase db){
        db.execSQL("ALTER TABLE clips ADD COLUMN category TEXT NOT NULL DEFAULT 'noise'");
        db.execSQL("ALTER TABLE clips ADD COLUMN score REAL NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE clips ADD COLUMN duration_ms INTEGER NOT NULL DEFAULT 10000");
        db.execSQL("CREATE TABLE sound_events(id INTEGER PRIMARY KEY,session INTEGER NOT NULL,time INTEGER NOT NULL,duration_ms INTEGER NOT NULL,category TEXT NOT NULL,score REAL NOT NULL,model TEXT NOT NULL,clip INTEGER)");
        db.execSQL("CREATE INDEX sound_events_session ON sound_events(session,time)");
        db.execSQL("CREATE TRIGGER unlink_sound_clip AFTER DELETE ON clips BEGIN UPDATE sound_events SET clip=NULL WHERE clip=OLD.id; END");
    }
    private static void createSoundLibrary(SQLiteDatabase db){
        db.execSQL("ALTER TABLE clips ADD COLUMN favorite INTEGER NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE sound_events ADD COLUMN favorite INTEGER NOT NULL DEFAULT 0");
        db.execSQL("CREATE INDEX sound_events_clip_favorite ON sound_events(clip,favorite)");
        db.execSQL("CREATE TABLE pending_clip_deletes(clip INTEGER PRIMARY KEY,session INTEGER NOT NULL,path TEXT NOT NULL,time INTEGER NOT NULL)");
    }
    private static void createSoundLabels(SQLiteDatabase db){db.execSQL("ALTER TABLE sound_events ADD COLUMN user_category TEXT");db.execSQL("ALTER TABLE sound_events ADD COLUMN label_revision INTEGER NOT NULL DEFAULT 0");}
    private static void createSensorProvenance(SQLiteDatabase db){db.execSQL("ALTER TABLE sessions ADD COLUMN sensor TEXT NOT NULL DEFAULT 'unspecified'");db.execSQL("ALTER TABLE sessions ADD COLUMN sensor_config TEXT NOT NULL DEFAULT ''");db.execSQL("ALTER TABLE epochs ADD COLUMN signal_quality TEXT");}
    private static void createSoundAnalysis(SQLiteDatabase db){db.execSQL("ALTER TABLE sessions ADD COLUMN sound_analysis TEXT NOT NULL DEFAULT ''");}
    long soundEvent(long session,long start,long duration,String category,double score,Long clip){
        if(session<=0||start<=0||duration<=0||!SoundCategories.valid(category)||!Double.isFinite(score)||score<0||score>1)throw new IllegalArgumentException("Invalid sound event");
        if(clip==null){List<JSONObject> saved=rows("SELECT id FROM clips WHERE session=? AND time-duration_ms<=CAST(? AS INTEGER) AND time>=CAST(? AS INTEGER) ORDER BY time LIMIT 1",""+session,""+start,""+(start+duration));if(!saved.isEmpty())clip=saved.get(0).optLong("id");}
        ContentValues v=new ContentValues();v.put("session",session);v.put("time",start);v.put("duration_ms",duration);v.put("category",category);v.put("score",score);v.put("model",YamnetClassifier.MODEL_ID);if(clip!=null)v.put("clip",clip);return getWritableDatabase().insertOrThrow("sound_events",null,v);
    }
    private static void createPauses(SQLiteDatabase db){db.execSQL("CREATE TABLE tracking_pauses(id INTEGER PRIMARY KEY, session INTEGER NOT NULL, start INTEGER NOT NULL, end INTEGER)");db.execSQL("CREATE INDEX pauses_session ON tracking_pauses(session,start)");}
    private static void createRequests(SQLiteDatabase db){db.execSQL("CREATE TABLE record_create_requests(token TEXT PRIMARY KEY,session INTEGER NOT NULL)");}
    private static void createRecords(SQLiteDatabase db){db.execSQL("CREATE TABLE record_edits(session INTEGER PRIMARY KEY,start INTEGER NOT NULL,end INTEGER NOT NULL,note TEXT NOT NULL DEFAULT '',tags TEXT NOT NULL DEFAULT '[]',awake TEXT NOT NULL DEFAULT '[]',rating INTEGER NOT NULL DEFAULT 0,rated INTEGER NOT NULL DEFAULT 0,updated INTEGER NOT NULL,reverted INTEGER NOT NULL DEFAULT 0)");}
    @Override public void onUpgrade(SQLiteDatabase db,int old,int next) {if(old<2){db.execSQL("ALTER TABLE sessions ADD COLUMN active_ms INTEGER");db.execSQL("ALTER TABLE sessions ADD COLUMN clock_changed INTEGER DEFAULT 0");db.execSQL("ALTER TABLE epochs ADD COLUMN start INTEGER");createPauses(db);}if(old<3){createRecords(db);db.execSQL("ALTER TABLE sessions ADD COLUMN source TEXT NOT NULL DEFAULT 'legacy'");db.execSQL("ALTER TABLE sessions ADD COLUMN origin TEXT NOT NULL DEFAULT 'legacy'");db.execSQL("ALTER TABLE sessions ADD COLUMN zone TEXT NOT NULL DEFAULT ''");}if(old<4)createRequests(db);if(old<5)createSoundEvents(db);if(old<6)createSoundLibrary(db);if(old<7)createSoundLabels(db);if(old<8)createSensorProvenance(db);if(old<9)createSoundAnalysis(db);if(old<10)HealthSleepTables.create(db);if(old<11)AwakeStore.create(db);if(old<12)EchoPeriods.create(db);if(old<13)PairMotionTables.create(db);if(old<14)PirStore.create(db);if(old<15)DreamCueTables.create(db);if(old<16)SoundEventBackup.createClipFavorites(db);if(old<17)SleepCsvEvidence.createTables(db);if(next>17)throw new IllegalStateException("Unknown database version");}
    public synchronized List<Alarm> alarms() {
        List<Alarm> result=new ArrayList<>();
        try(Cursor c=getReadableDatabase().rawQuery("SELECT data FROM alarms ORDER BY id",null)) {
            while(c.moveToNext()) { try { result.add(Alarm.read(new JSONObject(c.getString(0)))); } catch(JSONException ignored) {} }
        } return result;
    }
    public Alarm alarm(int id) { for(Alarm a:alarms()) if(a.id==id) return a; return null; }
    public void save(Alarm a) { ContentValues v=new ContentValues(); v.put("id",a.id); v.put("data",a.json().toString()); getWritableDatabase().insertWithOnConflict("alarms",null,v,SQLiteDatabase.CONFLICT_REPLACE);surfaceChanged(); }
    public void deleteAlarm(int id) { getWritableDatabase().delete("alarms","id=?",new String[]{""+id});surfaceChanged(); }
    public long begin() {return begin(SensorProvenance.MOTION,"");}
    public long begin(String sensor,String sensorConfig) {return begin(sensor,sensorConfig,SensorProvenance.SONAR.equals(sensor)?SoundAnalysisProvenance.encode(SoundAnalysisStatus.initial(false)):"");}
    long begin(String sensor,String sensorConfig,String soundAnalysis) {
        SensorProvenance.validate(sensor,sensorConfig);if(!Set.of(SensorProvenance.MOTION,SensorProvenance.SONAR).contains(sensor))throw new IllegalArgumentException("A tracked recording needs an explicit sensor.");
        SoundAnalysisStatus analysis=SoundAnalysisProvenance.read(soundAnalysis);if(SensorProvenance.SONAR.equals(sensor)&&analysis==null||analysis!=null&&(analysis.frames!=0||!Set.of("off","pending").contains(analysis.state)))throw new IllegalArgumentException("Invalid initial sound analysis evidence");
        SQLiteDatabase db=getWritableDatabase();long now=System.currentTimeMillis();
        recoverInterrupted();ContentValues v=new ContentValues();v.put("start",now);v.put("active_ms",0);v.put("source","tracked");v.put("origin","local");v.put("zone",java.time.ZoneId.systemDefault().getId());v.put("sensor",sensor);v.put("sensor_config",sensorConfig);v.put("sound_analysis",soundAnalysis);return db.insertOrThrow("sessions",null,v);
    }
    public void end(long id, boolean interrupted) { AwakeStore.recover(getWritableDatabase(),id);EchoPeriods.recover(getWritableDatabase(),id);PirStore.finish(getWritableDatabase(),id,interrupted);ContentValues v=new ContentValues(); v.put("end",System.currentTimeMillis()); v.put("interrupted",interrupted?1:0); getWritableDatabase().update("sessions",v,"id=?",new String[]{""+id});surfaceChanged(); }
    void end(long id,boolean interrupted,long active,long now){AwakeStore.recover(getWritableDatabase(),id);EchoPeriods.recover(getWritableDatabase(),id);PirStore.finish(getWritableDatabase(),id,interrupted);resume(id,now);ContentValues v=new ContentValues();v.put("end",now);v.put("active_ms",active);v.put("interrupted",interrupted?1:0);getWritableDatabase().update("sessions",v,"id=?",new String[]{""+id});surfaceChanged();}
    /** Ordered worker only. Counts cannot outrun the last durable capture checkpoint. */
    void soundAnalysisCheckpoint(long id,String encoded){SQLiteDatabase db=getWritableDatabase();db.beginTransaction();try{soundAnalysisValues(id,encoded,null,null);ContentValues values=new ContentValues();values.put("sound_analysis",encoded);if(db.update("sessions",values,"id=? AND end IS NULL",new String[]{""+id})!=1)throw new IllegalStateException("Sound analysis recording changed");db.setTransactionSuccessful();}finally{db.endTransaction();}}
    /** Final analysis evidence and the actual session end commit or roll back together. */
    void end(long id,boolean interrupted,long active,long now,String encoded){SQLiteDatabase db=getWritableDatabase();db.beginTransaction();try{soundAnalysisValues(id,encoded,active,now);end(id,interrupted,active,now);ContentValues values=new ContentValues();values.put("sound_analysis",encoded);if(db.update("sessions",values,"id=?",new String[]{""+id})!=1)throw new IllegalStateException("Sound analysis recording changed");db.setTransactionSuccessful();}finally{db.endTransaction();}}
    private void soundAnalysisValues(long id,String encoded,Long active,Long end){
        List<JSONObject> found=rows("SELECT * FROM sessions WHERE id=?",""+id);if(found.size()!=1)throw new IllegalArgumentException("Sound analysis recording is missing");JSONObject row=found.get(0);SoundAnalysisStatus next=SoundAnalysisProvenance.read(encoded),previous=SoundAnalysisProvenance.read(row.optString("sound_analysis",""));
        if(next==null||previous==null||!row.isNull("end")||!Set.of(SensorProvenance.SONAR,SensorProvenance.MOTION).contains(row.optString("sensor"))||!"tracked".equals(row.optString("source")))throw new IllegalArgumentException("Sound analysis recording changed");
        next.follows(previous);long captured=active==null?row.optLong("active_ms"):active;
        if(captured<row.optLong("active_ms")||captured<0||captured>SensorProvenance.MAX_SESSION||next.frames*975>captured||end==null&&Set.of("complete","insufficient").contains(next.state)||end!=null&&(end<row.optLong("start")||captured>end-row.optLong("start")||!next.terminal()))throw new IllegalArgumentException("Sound analysis exceeds captured time or remains unfinished");
    }
    void clockChanged(long id){ContentValues v=new ContentValues();v.put("clock_changed",1);getWritableDatabase().update("sessions",v,"id=?",new String[]{""+id});}
    void active(long id,long elapsed){ContentValues v=new ContentValues();v.put("active_ms",Math.max(0,elapsed));getWritableDatabase().update("sessions",v,"id=?",new String[]{""+id});}
    void pause(long session,long now){if(!rows("SELECT id FROM tracking_pauses WHERE session=? AND end IS NULL",""+session).isEmpty())return;ContentValues v=new ContentValues();v.put("session",session);v.put("start",now);getWritableDatabase().insertOrThrow("tracking_pauses",null,v);}
    void resume(long session,long now){getWritableDatabase().execSQL("UPDATE tracking_pauses SET end=MAX(start,?) WHERE session=? AND end IS NULL",new Object[]{now,session});}
    void recoverInterrupted(){for(JSONObject candidate:rows("SELECT id FROM sessions WHERE end IS NULL"))recoverInterrupted(candidate.optLong("id"));}
    private void recoverInterrupted(long id){SQLiteDatabase db=getWritableDatabase();db.beginTransaction();try{
        List<JSONObject> found=rows("SELECT * FROM sessions WHERE id=? AND end IS NULL",""+id);if(found.isEmpty()){db.setTransactionSuccessful();return;}JSONObject s=found.get(0);long last=s.optLong("start");
        // Validate historical evidence before changing any pause or session field.
        String analysis=SoundAnalysisProvenance.recovered(s.optString("sound_analysis",""));
        for(JSONObject e:rows("SELECT MAX(time) AS time FROM epochs WHERE session=?",""+id))last=Math.max(last,e.optLong("time"));
        for(JSONObject p:rows("SELECT MAX(COALESCE(end,start)) AS time FROM tracking_pauses WHERE session=?",""+id))last=Math.max(last,p.optLong("time"));
        long start=s.optLong("start"),limit=start>Long.MAX_VALUE-36*3600000L?Long.MAX_VALUE:start+36*3600000L;
        for(JSONObject e:rows("SELECT MAX(time+duration_ms) AS time FROM sound_events WHERE session=? AND time>=? AND duration_ms>0 AND duration_ms<=129600000 AND time+duration_ms<=CAST(? AS INTEGER) AND category IN ('snoring','speech','cough','baby_cry','laughter','noise') AND score>=0 AND score<=1 AND LENGTH(TRIM(model))>0 AND LENGTH(model)<=128",""+id,""+start,""+limit))last=Math.max(last,e.optLong("time"));
        for(JSONObject awake:rows("SELECT MAX(end) AS time FROM awake_estimates WHERE session=?",""+id))last=Math.max(last,awake.optLong("time"));
        for(JSONObject echo:rows("SELECT MAX(end) AS time FROM echo_periods WHERE session=?",""+id))last=Math.max(last,echo.optLong("time"));
        for(JSONObject pir:rows("SELECT MAX(time) AS time FROM pir_samples WHERE session=? AND time>=CAST(? AS INTEGER) AND time<=CAST(? AS INTEGER)",""+id,""+start,""+limit))last=Math.max(last,pir.optLong("time"));
        AwakeStore.recover(db,id);EchoPeriods.recover(db,id);PirStore.recover(db,id);DreamCueTables.recover(this,DreamCues.liveNonces());resume(id,last);ContentValues values=new ContentValues();values.put("end",last);values.put("interrupted",1);values.put("sound_analysis",analysis);if(db.update("sessions",values,"id=? AND end IS NULL",new String[]{""+id})!=1)throw new IllegalStateException("Recording changed during recovery");db.setTransactionSuccessful();
    }finally{db.endTransaction();surfaceChanged();}}

    long recordedMillis(JSONObject s){List<long[]> pauses=new ArrayList<>();long end=s.isNull("end")?System.currentTimeMillis():s.optLong("end");for(JSONObject p:rows("SELECT start,end FROM tracking_pauses WHERE session=?",""+s.optLong("id")))pauses.add(new long[]{p.optLong("start"),p.isNull("end")?end:p.optLong("end")});return SessionTiming.recorded(s.optLong("start"),end,s.isNull("active_ms")?null:s.optLong("active_ms"),pauses);}
    public void annotate(long id,String note,int rating) { ContentValues v=new ContentValues(); v.put("note",note); v.put("rating",rating); getWritableDatabase().update("sessions",v,"id=?",new String[]{""+id}); }
    public void epoch(long session,long time,Double movement,Double noise,Double light,int samples) {
        epoch(session,null,time,movement,noise,light,samples);
    }
    void epoch(long session,Long start,long time,Double movement,Double noise,Double light,int samples){ContentValues v=new ContentValues();v.put("session",session);v.put("start",start);v.put("time",time);v.put("movement",movement);v.put("noise",noise);v.put("light",light);v.put("samples",samples);getWritableDatabase().insertOrThrow("epochs",null,v);}
    void checkpoint(long session,Long start,long time,Double movement,Double noise,Double light,int samples,long elapsed,boolean measured){SQLiteDatabase db=getWritableDatabase();db.beginTransaction();try{if(measured)epoch(session,start,time,movement,noise,light,samples);active(session,elapsed);db.setTransactionSuccessful();}finally{db.endTransaction();}}
    /** Ordered storage-worker boundary: one real homogeneous <=5s interval plus active checkpoint. */
    void sonarCheckpoint(long session,long from,long to,Double movement,String quality,int samples,long active) {
        SensorProvenance.evidence(movement,quality,samples);if(session<=0||from<=0||to<=from||to-from>5000||active<0||active>SensorProvenance.MAX_SESSION)throw new IllegalArgumentException("Invalid contactless checkpoint");
        SQLiteDatabase db=getWritableDatabase();db.beginTransaction();try{
            List<JSONObject> rows=rows("SELECT * FROM sessions WHERE id=?",""+session);if(rows.size()!=1)throw new IllegalArgumentException("The contactless recording no longer exists.");JSONObject saved=rows.get(0);long start=saved.optLong("start"),previous=saved.isNull("active_ms")?0:saved.optLong("active_ms");
            if(!saved.isNull("end")||!SensorProvenance.SONAR.equals(saved.optString("sensor"))||!saved.optString("source").equals("tracked")||from<start||to-start>SensorProvenance.MAX_SESSION||active<previous||active>to-start)throw new IllegalArgumentException("The contactless recording changed.");SensorProvenance.validate(saved.optString("sensor"),saved.optString("sensor_config"));
            long prior=start;for(JSONObject epoch:rows("SELECT MAX(time) AS time FROM epochs WHERE session=?",""+session))if(!epoch.isNull("time"))prior=Math.max(prior,epoch.optLong("time"));if(from<prior)throw new IllegalArgumentException("Contactless intervals overlap.");
            List<JSONObject> pauses=rows("SELECT start,end FROM tracking_pauses WHERE session=? AND start<CAST(? AS INTEGER) AND (end IS NULL OR end>CAST(? AS INTEGER)) ORDER BY start",""+session,""+to,""+prior);long covered=prior;for(JSONObject pause:pauses){long a=pause.optLong("start"),b=pause.isNull("end")?Long.MAX_VALUE:pause.optLong("end");if(a<to&&b>from)throw new IllegalArgumentException("Contactless data cannot cross a pause.");if(a<=covered&&b>covered)covered=b;}if(from>prior&&covered<from)throw new IllegalArgumentException("Persist the missing contactless signal interval first.");
            ContentValues v=new ContentValues();v.put("session",session);v.put("start",from);v.put("time",to);v.put("movement",movement);v.putNull("noise");v.putNull("light");v.put("samples",samples);v.put("signal_quality",quality);db.insertOrThrow("epochs",null,v);active(session,active);db.setTransactionSuccessful();
        }finally{db.endTransaction();}
    }
    public void clip(long session,long time,String path,double level) {clip(session,time,path,level,10000);}
    void clip(long session,long end,String path,double level,long duration){
        if(duration<=0)throw new IllegalArgumentException("Invalid recording duration");SQLiteDatabase db=getWritableDatabase();db.beginTransaction();try{
            ContentValues v=new ContentValues();v.put("session",session);v.put("time",end);v.put("path",path);v.put("level",level);v.put("duration_ms",duration);long id=db.insertOrThrow("clips",null,v);
            db.execSQL("UPDATE sound_events SET clip=? WHERE session=? AND clip IS NULL AND time>=? AND time+duration_ms<=?",new Object[]{id,session,end-duration,end});db.setTransactionSuccessful();
        }finally{db.endTransaction();}
    }
    public List<JSONObject> rows(String sql,String...args) {
        List<JSONObject> rows=new ArrayList<>();
        try(Cursor c=getReadableDatabase().rawQuery(sql,args)) {
            while(c.moveToNext()) { JSONObject j=new JSONObject(); for(int i=0;i<c.getColumnCount();i++) try { j.put(c.getColumnName(i), c.isNull(i)?JSONObject.NULL:(c.getType(i)==Cursor.FIELD_TYPE_STRING?c.getString(i):c.getDouble(i))); } catch(JSONException ignored) {} rows.add(j); }
        } return rows;
    }
    public JSONObject backup() {
        SQLiteDatabase db=getReadableDatabase();db.beginTransactionNonExclusive();try{JSONObject root=new JSONObject(); JSONArray savedAlarms=new JSONArray();for(Alarm a:alarms()){JSONObject portable=a.json();portable.remove("weeklyNapOwner");savedAlarms.put(portable);}try { root.put("format",13).put("alarms",savedAlarms)
            .put("sessions",new JSONArray(rows("SELECT * FROM sessions"))).put("epochs",new JSONArray(rows("SELECT * FROM epochs"))).put("pauses",new JSONArray(rows("SELECT * FROM tracking_pauses"))).put("edits",new JSONArray(rows("SELECT * FROM record_edits"))).put("sound_events",SoundEventBackup.export(this)).put("clip_favorites",SoundEventBackup.exportClipFavorites(this));AwakeBackup.export(this,root);EchoPeriods.export(this,root);root.put("paired_motion",PairMotionBackup.export(this));PirStore.export(this,root);root.put("dream_cues",DreamCueBackup.export(this));root.put("sleep_csv",SleepCsvEvidence.export(this));SoundEventBackup.checkPortableSize(root);SensorBackup.validateExport(root);SoundAnalysisProvenance.validateExport(root);HealthSleepCodec.validate(root); } catch(JSONException e) { throw new IllegalStateException(e); } db.setTransactionSuccessful();return root;}finally{db.endTransaction();}
    }
    void historyReplacing(){if("sleepwell.db".equals(getDatabaseName())&&Records.factory==Records.PRODUCTION){AutoCalendar.historyChanging(surfaceContext);RecordAccessGrants.historyChanging(surfaceContext);AlarmAccessGrants.historyChanging(surfaceContext);}}
    public void eraseHistory() { historyReplacing();SQLiteDatabase db=getWritableDatabase(); db.beginTransaction(); try { AwakeStore.erase(db);HealthSleepTables.erased(db);db.delete("portable_clip_favorites",null,null);SoundClips.stageAll(this,null);db.execSQL("UPDATE record_create_requests SET session=0");db.delete("sound_events",null,null);db.delete("clips",null,null); db.delete("epochs",null,null);db.delete("tracking_pauses",null,null);db.delete("record_edits",null,null); db.delete("sessions",null,null); db.setTransactionSuccessful(); } finally { db.endTransaction();surfaceChanged(); } }
}
