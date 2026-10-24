package org.sleepwell.app;

import android.content.*;
import android.os.SystemClock;
import android.os.Build;
import java.security.MessageDigest;
import androidx.test.platform.app.InstrumentationRegistry;
import org.json.*;
import org.junit.Test;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;

/** Real bundled CPU model on public CC0 PCM replay. No microphone, speaker, service or sensor. */
public final class SonarSharedClassifierStorageTest {
 @Test public void wholeLicensedPcmThroughShared48kConversionPersistsActualPolicyEvents()throws Exception {
  assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertTrue(Build.HARDWARE.contains("ranchu")||Build.HARDWARE.contains("goldfish"));
  Context app=InstrumentationRegistry.getInstrumentation().getTargetContext(),tests=InstrumentationRegistry.getInstrumentation().getContext();Map<String,?> prefs=new HashMap<>(Prefs.of(app).getAll());JSONArray reports=new JSONArray();int snores=0,speech=0,speechSnores=0;
  assertFalse(TrackingService.running);assertFalse(AlarmService.active);assertFalse(Prefs.flag(app,"ringing"));
  for(String fixture:List.of("snoring-sorts","snoring-pencil","speech-countdown")){
   byte[] bytes;try(InputStream input=tests.getAssets().open("classifier-fixtures/"+fixture+".pcm");ByteArrayOutputStream output=new ByteArrayOutputStream()){byte[] block=new byte[8192];for(int n;(n=input.read(block))!=-1;){assertTrue("Bounded whole licensed fixture",output.size()+n<=3*1024*1024);output.write(block,0,n);}bytes=output.toByteArray();}
   assertEquals(0,bytes.length%2);assertTrue(bytes.length<=80*32000);String hash=sha256(bytes);assertEquals(Map.of("snoring-sorts","c7e95aed4687352e5dbd27148f521584e7224f7df7f1dddbb5875332dadce5a1","snoring-pencil","d53e9312fe9abd9ec6174d864b660ac38dc0edc792698087a99b774084ca68a1","speech-countdown","4a62ee7002290bce1555a3995c87fd6d0138343e54f767c418efd356e2765880").get(fixture),hash);
   int total=bytes.length/2*3;String name="sonar-shared-real-model-"+UUID.randomUUID()+".db";ConcurrentLinkedQueue<JSONObject> estimates=new ConcurrentLinkedQueue<>();SonarSoundWorker worker=null;CountDownLatch closed=new CountDownLatch(1);AtomicReference<SonarSoundWorker.Result> result=new AtomicReference<>();long origin=SystemClock.elapsedRealtimeNanos();
   try{try(Store store=new Store(app,name)){
    long id=store.begin(SensorProvenance.SONAR,SensorProvenance.sonar(new SonarConfig(.05f),"UNPROCESSED"),SoundAnalysisProvenance.encode(SoundAnalysisStatus.initial(true))),start=store.rows("SELECT start FROM sessions WHERE id=?",""+id).get(0).getLong("start");
    worker=new SonarSoundWorker(()->new YamnetClassifier(app),(at,n,category,score)->{try{estimates.add(new JSONObject().put("at",at).put("duration",n*975L).put("category",category).put("score",score));}catch(JSONException failure){throw new IllegalStateException(failure);}},start,origin,Runnable::run);
    for(int offset=0,index=0;offset<total;index++){
     int count=Math.min(1920,total-offset);short[] pcm=new short[count];for(int n=0;n<count;n++){int source=(offset+n)/3*2;pcm[n]=(short)((bytes[source]&255)|(bytes[source+1]<<8));}
     assertTrue("Bounded replay admission "+fixture,worker.offer(pcm,count,origin+Math.round(offset*(1e9/48000)),true));offset+=count;
     // Fixture producer may wait; the production capture callback never does. Avoid benchmarking a burst as realtime capture.
     if(index%24==23){long inferred=(offset/3-64)/15600;long until=SystemClock.elapsedRealtime()+20000;while(worker.frames()<inferred&&worker.issue().isEmpty()&&SystemClock.elapsedRealtime()<until)Thread.sleep(10);assertEquals("",worker.issue());assertTrue("Real model consumed prior captured fixture frames",worker.frames()>=inferred);}
    }
    worker.finish(origin+Math.round(total*(1e9/48000)),r->{result.set(r);closed.countDown();});assertTrue("Actual model cleanup acknowledged",closed.await(30,TimeUnit.SECONDS));assertEquals("",result.get().issue);assertEquals(((total-193)/3+1)/15600,result.get().frames);
    long active=total*1000L/48000;for(long from=0;from<active;){long to=Math.min(active,from+5000);store.sonarCheckpoint(id,start+from,start+to,null,"NO_ECHO",0,to);from=to;}
    for(JSONObject estimate:estimates){assertTrue(estimate.getLong("at")>=start);assertTrue(estimate.getLong("at")+estimate.getLong("duration")<=start+active);store.soundEvent(id,estimate.getLong("at"),estimate.getLong("duration"),estimate.getString("category"),estimate.getDouble("score"),null);}
    store.end(id,false,active,start+active,SoundAnalysisProvenance.encode(new SoundAnalysisStatus(true,"complete",result.get().frames,"")));
   }finally{if(worker!=null&&result.get()==null){try{worker.finish(origin+Math.round(total*(1e9/48000)),r->{result.set(r);closed.countDown();});}catch(IllegalStateException alreadyRequested){}assertTrue(closed.await(30,TimeUnit.SECONDS));}}
   try(Store reopened=new Store(app,name)){
    RecordEntry record=Records.all(reopened).get(0);SoundAnalysisStatus status=SoundAnalysisProvenance.read(record.soundAnalysis);assertEquals(result.get().frames,status.frames);assertEquals("complete",status.state);assertTrue(reopened.rows("SELECT * FROM clips").isEmpty());int snore=0,voice=0;for(JSONObject event:reopened.rows("SELECT * FROM sound_events")){assertTrue(event.isNull("clip"));assertEquals(YamnetClassifier.MODEL_ID,event.getString("model"));if(event.getString("category").equals("snoring"))snore++;if(event.getString("category").equals("speech"))voice++;}
    if(fixture.startsWith("snoring"))snores+=snore;else{speech+=voice;speechSnores+=snore;}Backup.validate(reopened.backup());reports.put(new JSONObject().put("fixture",fixture).put("pcm_sha256",hash).put("inferences",status.frames).put("snoring_events",snore).put("speech_events",voice).put("scope","whole public PCM repeated to48k, real causal FIR and bundled classifier; not microphone/bedroom accuracy"));
   }}finally{assertTrue(app.deleteDatabase(name));}
  }
  File directory=new File(app.getExternalFilesDir(null),"verification");assertTrue(directory.isDirectory()||directory.mkdirs());try(Writer output=new OutputStreamWriter(new FileOutputStream(new File(directory,"sonar-shared-classifier-replay.json")),java.nio.charset.StandardCharsets.UTF_8)){output.write(new JSONObject().put("model",YamnetClassifier.MODEL_ID).put("fixtures",reports).toString(2));}
  assertEquals("Solo speech must remain a snoring cross-negative",0,speechSnores);assertTrue("At least one existing whole snore preview must retain genuine snoring events",snores>0);assertTrue("Whole solo speech must retain genuine speech events",speech>0);assertEquals(prefs,Prefs.of(app).getAll());assertFalse(TrackingService.running);assertFalse(AlarmService.active);
 }
 private String sha256(byte[] bytes)throws Exception{StringBuilder out=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(bytes))out.append(String.format(Locale.ROOT,"%02x",b&255));return out.toString();}
}
