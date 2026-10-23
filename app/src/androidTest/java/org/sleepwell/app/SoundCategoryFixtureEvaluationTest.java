package org.sleepwell.app;

import android.content.Context;
import android.os.Build;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import org.json.*;
import org.junit.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.Assert.*;

/** Licensed public PCM only: real CPU/framing/policy/group/SQLite; never captures or outputs sound. */
public class SoundCategoryFixtureEvaluationTest {
    @Before public void ciOnly()throws Exception{Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertTrue(Build.HARDWARE.contains("ranchu")||Build.HARDWARE.contains("goldfish"));assertEquals("1",UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).executeShellCommand("getprop ro.kernel.qemu").trim());assertFalse(AlarmService.active);assertFalse(TrackingService.running);assertFalse(Prefs.flag(c,"ringing"));}
    @Test public void realPublicPcmMustPersistNewCategoryPositivesAndRejectCrossClassNegatives()throws Exception{
        Context target=InstrumentationRegistry.getInstrumentation().getTargetContext(),tests=InstrumentationRegistry.getInstrumentation().getContext();JSONArray reports=new JSONArray();Map<String,JSONObject> byName=new HashMap<>();
        try(YamnetClassifier model=new YamnetClassifier(target)){
            for(String name:List.of("cough-fabrizio","cough-crowd","babycry-bonehead","laughter-crowd","snoring-sorts","snoring-pencil","speech-countdown","speech-crowd")){
                byte[] pcm;try(InputStream in=tests.getAssets().open("classifier-fixtures/"+name+".pcm")){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];for(int count;(count=in.read(buffer))!=-1;){assertTrue("Bounded PCM fixture",out.size()+count<=2*1024*1024);out.write(buffer,0,count);}pcm=out.toByteArray();}assertEquals(0,pcm.length%2);
                String expected=name.startsWith("cough-")?"cough":name.startsWith("babycry-")?"baby_cry":name.startsWith("laughter-")?"laughter":name.startsWith("snoring-")?"snoring":"speech";JSONArray windows=new JSONArray();JSONObject report=new JSONObject().put("fixture",name).put("expected_broad_class",expected);String database="sleepwell-verification-"+UUID.randomUUID()+".db";
                try{
                    long session;try(Store store=new Store(target,database)){
                        session=store.begin();long start=store.rows("SELECT start FROM sessions WHERE id=?",""+session).get(0).getLong("start");SoundAnalysisStream.Model observing=new SoundAnalysisStream.Model(){public float[] infer(float[] input){float[] scores=model.infer(input);SoundAnalysis.Result result=SoundAnalysis.classify(scores);int top=0;for(int n=1;n<scores.length;n++)if(scores[n]>scores[top])top=n;try{JSONArray ranking=new JSONArray();List<Integer> order=new ArrayList<>();for(int n=0;n<scores.length;n++)order.add(n);order.sort((a,b)->Float.compare(scores[b],scores[a]));for(int n=0;n<8;n++)ranking.put(new JSONObject().put("index",order.get(n)).put("score",scores[order.get(n)]));windows.put(new JSONObject().put("start_ms",windows.length()*975).put("top_index",top).put("top_score",scores[top]).put("laughter_family_score",Math.max(Math.max(Math.max(scores[13],scores[14]),Math.max(scores[15],scores[16])),Math.max(scores[17],scores[18]))).put("crying_parent_score",scores[19]).put("baby_cry_score",scores[20]).put("cough_score",scores[42]).put("throat_clearing_score",scores[43]).put("snoring_score",scores[38]).put("speech_score",scores[0]).put("selected",result==null?"none":result.category).put("top8",ranking));}catch(JSONException error){throw new IllegalStateException(error);}return scores;}public void close(){/* Outer owner reuses the real CPU model. */}};
                        try(SoundAnalysisStream stream=new SoundAnalysisStream(()->observing,(at,count,category,score)->store.soundEvent(session,at,count*975L,category,score,null),()->fail("Unexpected fixture inference failure"))){short[] chunk=new short[1024];int samples=pcm.length/2;for(int offset=0;offset<samples;offset+=chunk.length){int count=Math.min(chunk.length,samples-offset);for(int n=0;n<count;n++){int index=(offset+n)*2;chunk[n]=(short)((pcm[index]&255)|(pcm[index+1]<<8));}stream.accept(chunk,count,start+(offset+count)*1000L/16000);}}
                        store.end(session,false,pcm.length*1000L/32000,start+pcm.length*1000L/32000);assertTrue("Analysis-only creates no WAV rows",store.rows("SELECT id FROM clips").isEmpty());
                    }
                    try(Store reopened=new Store(target,database)){List<JSONObject> events=reopened.rows("SELECT * FROM sound_events WHERE session=? ORDER BY time",""+session);JSONObject counts=new JSONObject();for(String category:SoundCategories.VALUES)counts.put(category,0);for(JSONObject event:events){String category=event.getString("category");counts.put(category,counts.getInt(category)+1);assertTrue(event.isNull("clip"));assertEquals(YamnetClassifier.MODEL_ID,event.getString("model"));assertTrue(event.getLong("duration_ms")>=1950);}report.put("persisted_events",new JSONArray(events)).put("counts",counts).put("windows",windows).put("frames",windows.length());}
                }finally{target.deleteDatabase(database);}reports.put(report);byName.put(name,report);
            }
        }
        File folder=new File(target.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());try(Writer out=new OutputStreamWriter(new FileOutputStream(new File(folder,"analysis-category-fixture-evaluation.json")),StandardCharsets.UTF_8)){out.write(new JSONObject().put("model",YamnetClassifier.MODEL_ID).put("scope","CC0 public compatibility fixtures; uncalibrated scores, not bedroom/clinical accuracy").put("fixtures",reports).toString(2));}
        assertTrue("Actual cough PCM must persist a production grouped cough event",count(byName,"cough-fabrizio","cough")+count(byName,"cough-crowd","cough")>0);assertTrue("Actual infant PCM must persist production baby cry",count(byName,"babycry-bonehead","baby_cry")>0);assertTrue("Actual laughter PCM must persist production laughter",count(byName,"laughter-crowd","laughter")>0);
        for(String name:List.of("snoring-sorts","snoring-pencil","speech-countdown","speech-crowd"))for(String category:List.of("cough","baby_cry","laughter"))assertEquals(name+" must not persist "+category,0,count(byName,name,category));
        for(String name:List.of("cough-fabrizio","cough-crowd","babycry-bonehead","laughter-crowd"))assertEquals(name+" must not persist snoring",0,count(byName,name,"snoring"));
        assertTrue("Existing snore evidence remains",count(byName,"snoring-sorts","snoring")+count(byName,"snoring-pencil","snoring")>0);assertTrue("Existing speech evidence remains",count(byName,"speech-countdown","speech")>0);
    }
    private int count(Map<String,JSONObject> reports,String name,String category)throws JSONException{return reports.get(name).getJSONObject("counts").getInt(category);}
}
