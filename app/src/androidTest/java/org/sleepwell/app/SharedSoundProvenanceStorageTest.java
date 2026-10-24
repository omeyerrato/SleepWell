package org.sleepwell.app;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import androidx.test.platform.app.InstrumentationRegistry;
import org.json.*;
import org.junit.Test;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

/** Private databases only. No capture, playback, tracking services or scheduling effects. */
public final class SharedSoundProvenanceStorageTest {
 final Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();
 final class Fixture implements AutoCloseable {
  final String name="shared-sound-evidence-"+UUID.randomUUID()+".db";final Store store;
  Fixture(){this(false);}Fixture(boolean fail){store=new Store(c,name){@Override void end(long id,boolean interrupted,long active,long now){super.end(id,interrupted,active,now);if(fail)throw new SQLiteDiskIOException("Injected final transaction failure");}};}
  long begin(boolean requested){return store.begin(SensorProvenance.SONAR,config(),encoded(requested,requested?"pending":"off",0,""));}
  long start(long id){return row(store,id).optLong("start");}
  void measured(long id,long start){store.sonarCheckpoint(id,start,start+5000,0d,"VALID",125,5000);}
  public void close(){store.close();assertTrue(c.deleteDatabase(name));}
 }
 String config(){return SensorProvenance.sonar(new SonarConfig(.05f),"UNPROCESSED");}
 String encoded(boolean requested,String state,long frames,String issue){return SoundAnalysisProvenance.encode(new SoundAnalysisStatus(requested,state,frames,issue));}
 JSONObject row(Store store,long id){return store.rows("SELECT * FROM sessions WHERE id=?",""+id).get(0);}
 SoundAnalysisStatus status(Store store,long id){return SoundAnalysisProvenance.read(row(store,id).optString("sound_analysis"));}
 JSONObject closed(Fixture f,String state,long frames,String issue)throws Exception {long id=f.begin(true),s=f.start(id);f.measured(id,s);f.store.end(id,false,5000,s+5000,encoded(true,state,frames,issue));return f.store.backup();}

 @Test public void schemaEightToNinePreservesAllExistingTablesColumnsAndSqliteTypes()throws Exception {
  String name="shared-sound-migration-"+UUID.randomUUID()+".db";
  try(SoundLabelFixture seedFixture=new SoundLabelFixture(c);Store seed=seedFixture.open()){
   seed.save(new Alarm(91));seed.pause(1,seedFixture.start+3000);seed.resume(1,seedFixture.start+3500);RecordEntry r=Records.get(seed,1);RecordDraft edit=r.edit.copy();edit.note="Original note حلم";Records.save(seed,1,edit,r.updated,System.currentTimeMillis());seed.getWritableDatabase().execSQL("INSERT INTO record_create_requests VALUES('keep-request',1)");seed.getWritableDatabase().execSQL("INSERT INTO pending_clip_deletes VALUES(99,1,'fixture-owned-pending.wav',?)",new Object[]{seedFixture.start});
   List<JSONObject> schema=seed.rows("SELECT type,name,sql FROM sqlite_master WHERE sql IS NOT NULL AND name NOT LIKE 'sqlite_%' AND name!='android_metadata' ORDER BY CASE type WHEN 'table' THEN 0 WHEN 'index' THEN 1 ELSE 2 END,name");Map<String,String[]> columns=new LinkedHashMap<>();Map<String,String> original=new LinkedHashMap<>();
   for(String table:BackupFingerprint.TABLES){if(table.startsWith("health_"))continue;List<String> names=new ArrayList<>();for(JSONObject col:seed.rows("PRAGMA table_info("+table+")"))if(!col.getString("name").equals("sound_analysis")&&!col.getString("name").equals("health_connect"))names.add(col.getString("name"));columns.put(table,names.toArray(new String[0]));original.put(table,typed(seed.getReadableDatabase(),table,columns.get(table)));}
   try(SQLiteDatabase old=SQLiteDatabase.openOrCreateDatabase(c.getDatabasePath(name),null)){for(JSONObject object:schema)if(!object.getString("name").startsWith("health_"))old.execSQL(object.getString("sql").replaceAll(",\\s*health_connect TEXT NOT NULL DEFAULT ''","").replaceAll(",\\s*sound_analysis TEXT NOT NULL DEFAULT ''",""));old.execSQL("ATTACH DATABASE ? AS original",new Object[]{c.getDatabasePath(seedFixture.database).getAbsolutePath()});for(String table:columns.keySet()){String selected=String.join(",",columns.get(table));old.execSQL("INSERT INTO "+table+"("+selected+") SELECT "+selected+" FROM original."+table);}old.execSQL("DETACH DATABASE original");old.setVersion(8);}
   try(Store upgraded=new Store(c,name)){assertEquals(10,upgraded.getReadableDatabase().getVersion());for(String table:columns.keySet())assertEquals(table,original.get(table),typed(upgraded.getReadableDatabase(),table,columns.get(table)));assertEquals("",row(upgraded,1).getString("sound_analysis"));assertEquals("ok",upgraded.rows("PRAGMA integrity_check").get(0).getString("integrity_check"));assertTrue(seedFixture.file.exists());}
  }finally{c.deleteDatabase(name);}
 }
 @Test public void newManualSonarOffIsExplicitButMotionLegacyAndManualEntriesStayUnknown()throws Exception {try(Fixture f=new Fixture()){
  long motion=f.store.begin();assertEquals("",row(f.store,motion).getString("sound_analysis"));f.store.end(motion,false);long sonar=f.store.begin(SensorProvenance.SONAR,config());assertFalse(status(f.store,sonar).requested);assertEquals("off",status(f.store,sonar).state);f.store.end(sonar,false);long now=System.currentTimeMillis();long manual=Records.create(f.store,new RecordDraft(now-60000,now-1000),java.time.ZoneId.of("UTC"),now);assertEquals("",Records.get(f.store,manual).soundAnalysis);
 }}
 @Test public void completedZeroEventAndErrorEvidenceRoundTripWithoutInventingSounds()throws Exception {for(String variant:List.of("complete","insufficient","error","limit"))try(Fixture source=new Fixture();Fixture target=new Fixture()){
  String state=variant.equals("limit")?"error":variant;long frames=state.equals("insufficient")?0:3;String issue=state.equals("error")?variant.equals("limit")?"limit":"model":"";JSONObject incoming=closed(source,state,frames,issue);assertEquals(7,incoming.getInt("format"));assertEquals(0,incoming.getJSONArray("sound_events").length());Alarm a=new Alarm(1);a.enabled=true;source.store.save(a);incoming=source.store.backup();Backup.restoreData(target.store,incoming);RecordEntry r=Records.all(target.store).get(0);SoundAnalysisStatus result=SoundAnalysisProvenance.read(r.soundAnalysis);assertEquals(state,result.state);assertEquals(issue,result.issue);assertEquals(frames,result.frames);assertEquals(r.soundAnalysis,r.draft(r.edit.copy()).soundAnalysis);assertEquals(r.soundAnalysis,Records.display(r).getString("sound_analysis"));assertFalse(target.store.alarm(1).enabled);assertTrue(target.store.rows("SELECT * FROM clips").isEmpty());Backup.validate(target.store.backup());
 }}
 @Test public void oldFormatsOneThroughFiveIgnoreInjectedAnalysisMetadata()throws Exception {try(Fixture source=new Fixture();Fixture target=new Fixture()){
  JSONObject latest=closed(source,"complete",3,"");for(int format=1;format<=5;format++){JSONObject old=new JSONObject(latest.toString()).put("format",format);old.getJSONArray("sessions").getJSONObject(0).put("sound_analysis","untrusted-legacy-field");Backup.restoreData(target.store,old);assertEquals("",Records.all(target.store).get(0).soundAnalysis);Backup.validate(target.store.backup());}
 }}
 @Test public void malformedTypedMetadataRejectsBeforeAnyReplacement()throws Exception {try(Fixture source=new Fixture();Fixture target=new Fixture()){
  JSONObject valid=closed(source,"complete",3,"");long kept=target.store.begin();target.store.end(kept,false);String before=BackupFingerprint.read(target.store);
  for(String key:List.of("version","requested","frames","state","issue","extra")){JSONObject bad=new JSONObject(valid.toString());JSONObject session=bad.getJSONArray("sessions").getJSONObject(0),evidence=new JSONObject(session.getString("sound_analysis"));switch(key){case "version":evidence.put(key,"1");break;case "requested":evidence.put(key,"true");break;case "frames":evidence.put(key,1.5);break;case "state":evidence.put(key,"medical-diagnosis");break;case "issue":evidence.put(key,"private-path");break;default:evidence.put(key,true);}session.put("sound_analysis",evidence.toString());assertThrows(JSONException.class,()->Backup.restoreData(target.store,bad));assertEquals(before,BackupFingerprint.read(target.store));}
  for(String variant:List.of("missing","null","wrongSensor","unfinishedClosed","tooManyFrames")){JSONObject bad=new JSONObject(valid.toString()),session=bad.getJSONArray("sessions").getJSONObject(0);switch(variant){case "missing":session.remove("sound_analysis");break;case "null":session.put("sound_analysis",JSONObject.NULL);break;case "wrongSensor":session.put("sensor",SensorProvenance.MOTION).put("sensor_config","");break;case "unfinishedClosed":session.put("sound_analysis",encoded(true,"active",3,""));break;default:session.put("sound_analysis",encoded(true,"complete",6,""));}assertThrows(JSONException.class,()->Backup.restoreData(target.store,bad));assertEquals(before,BackupFingerprint.read(target.store));}
 }}
 @Test public void durableCountsCannotMoveBackwardsEscalateOrExceedCapture()throws Exception {try(Fixture f=new Fixture()){
  long id=f.begin(true),s=f.start(id);f.measured(id,s);f.store.soundAnalysisCheckpoint(id,encoded(true,"active",3,""));String before=BackupFingerprint.read(f.store);
  for(String bad:List.of(encoded(true,"active",2,""),encoded(true,"active",6,""),encoded(false,"off",0,""),encoded(true,"pending",0,""))){assertThrows(IllegalArgumentException.class,()->f.store.soundAnalysisCheckpoint(id,bad));assertEquals(before,BackupFingerprint.read(f.store));}
  f.store.end(id,false,5000,s+5000,encoded(true,"complete",3,""));before=BackupFingerprint.read(f.store);assertThrows(IllegalArgumentException.class,()->f.store.soundAnalysisCheckpoint(id,encoded(true,"active",4,"")));assertEquals(before,BackupFingerprint.read(f.store));
 }}
 @Test public void finalStatusAndEndRollBackTogetherOnDatabaseFailure()throws Exception {try(Fixture f=new Fixture(true)){
  long id=f.begin(true),s=f.start(id);f.measured(id,s);f.store.soundAnalysisCheckpoint(id,encoded(true,"active",3,""));f.store.pause(id,s+5000);String before=BackupFingerprint.read(f.store);assertThrows(SQLiteDiskIOException.class,()->f.store.end(id,false,5000,s+5000,encoded(true,"complete",3,"")));assertEquals(before,BackupFingerprint.read(f.store));assertTrue(row(f.store,id).isNull("end"));assertTrue(f.store.rows("SELECT end FROM tracking_pauses").get(0).isNull("end"));
 }}
 @Test public void crashRecoveryAndUnfinishedPortableRestoreKeepActualCounts()throws Exception {for(boolean active:List.of(false,true))try(Fixture f=new Fixture();Fixture target=new Fixture()){
  long id=f.begin(true),s=f.start(id);f.measured(id,s);if(active)f.store.soundAnalysisCheckpoint(id,encoded(true,"active",3,""));JSONObject pending=f.store.backup();Backup.restoreData(target.store,pending);assertEquals("interrupted",status(target.store,id).state);assertEquals(active?3:0,status(target.store,id).frames);assertEquals(s+5000,row(target.store,id).getLong("end"));f.store.recoverInterrupted();assertEquals("interrupted",status(f.store,id).state);assertEquals(active?3:0,status(f.store,id).frames);assertEquals(1,row(f.store,id).getInt("interrupted"));Backup.validate(target.store.backup());Backup.validate(f.store.backup());
 }}
 @Test public void interruptedRecoveryRollsBackAnAlreadyClosedPauseOnStorageFailure()throws Exception {
  String name="shared-sound-recovery-failure-"+UUID.randomUUID()+".db";
  try(Store store=new Store(c,name){@Override void resume(long id,long now){super.resume(id,now);throw new SQLiteDiskIOException("Injected failure after pause closure");}}){
   long id=store.begin(SensorProvenance.SONAR,config(),encoded(true,"pending",0,"")),s=row(store,id).getLong("start");store.sonarCheckpoint(id,s,s+5000,null,"NO_ECHO",0,5000);store.pause(id,s+5000);String before=BackupFingerprint.read(store);
   assertThrows(SQLiteDiskIOException.class,store::recoverInterrupted);assertEquals(before,BackupFingerprint.read(store));assertTrue(row(store,id).isNull("end"));assertTrue(store.rows("SELECT end FROM tracking_pauses").get(0).isNull("end"));
   store.getWritableDatabase().execSQL("UPDATE sessions SET sound_analysis='invalid' WHERE id=?",new Object[]{id});before=BackupFingerprint.read(store);assertThrows(IllegalArgumentException.class,store::recoverInterrupted);assertEquals(before,BackupFingerprint.read(store));
  }finally{c.deleteDatabase(name);}
 }
 @Test public void guardedFormatSixRestoreRollbackPreservesOriginalEvidence()throws Exception {try(Fixture source=new Fixture();Fixture target=new Fixture()){
  JSONObject incoming=closed(source,"complete",3,"");closed(target,"error",2,"capture");String before=BackupFingerprint.read(target.store);AtomicInteger calls=new AtomicInteger();assertThrows(IllegalStateException.class,()->Backup.restoreData(target.store,incoming,()->{if(calls.incrementAndGet()==4)throw new IllegalStateException("Injected concurrent admission");}));assertEquals(before,BackupFingerprint.read(target.store));
 }}
 @Test public void csvStreamingAndSharedProjectionCarryClosedAndOpenAnalysisEvidence()throws Exception {try(Fixture f=new Fixture()){
  closed(f,"complete",3,"");long open=f.begin(true);assertEquals("pending",status(f.store,open).state);String csv=Records.csv(f.store);assertEquals(csv,new String(BackupDocuments.csv(f.store,()->true),StandardCharsets.UTF_8));assertTrue(csv.startsWith("start_iso,end_iso,"));assertTrue(csv.contains("sensor,sensor_config,sound_analysis\r\n"));for(JSONObject session:f.store.rows("SELECT sound_analysis FROM sessions"))assertTrue(csv.contains(SleepMath.csv(session.getString("sound_analysis"))));
 }}
 @Test public void invalidInitialEvidenceCannotRecoverOrCloseAnotherRecording()throws Exception {try(Fixture f=new Fixture()){
  long id=f.store.begin();String before=BackupFingerprint.read(f.store);for(Runnable invalid:List.<Runnable>of(()->f.store.begin(SensorProvenance.MOTION,"",encoded(true,"pending",0,"")),()->f.store.begin(SensorProvenance.SONAR,config(),encoded(true,"complete",3,"")),()->f.store.begin(SensorProvenance.SONAR,config(),"bad"))){assertThrows(IllegalArgumentException.class,invalid::run);assertEquals(before,BackupFingerprint.read(f.store));}assertTrue(row(f.store,id).isNull("end"));
 }}
 String typed(SQLiteDatabase db,String table,String[] columns)throws Exception {JSONArray rows=new JSONArray();try(Cursor query=db.rawQuery("SELECT "+String.join(",",columns)+" FROM "+table+" ORDER BY rowid",null)){while(query.moveToNext()){JSONArray row=new JSONArray();for(int n=0;n<query.getColumnCount();n++){int type=query.getType(n);row.put(new JSONArray().put(type).put(type==Cursor.FIELD_TYPE_NULL?JSONObject.NULL:type==Cursor.FIELD_TYPE_INTEGER?query.getLong(n):type==Cursor.FIELD_TYPE_FLOAT?query.getDouble(n):query.getString(n)));}rows.put(row);}}return rows.toString();}
}
