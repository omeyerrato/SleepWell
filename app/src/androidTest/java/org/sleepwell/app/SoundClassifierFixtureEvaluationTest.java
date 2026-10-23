package org.sleepwell.app;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import org.json.*;
import org.junit.Test;
import java.io.*;
import java.util.*;
import static org.junit.Assert.*;

/** Public CC0 fixture evaluation, in-memory CPU only. Never records or plays audio. */
public class SoundClassifierFixtureEvaluationTest {
    @Test public void evaluatePublicSnoringAndSpeechFixturesWithoutHardware() throws Exception {
        Context target=InstrumentationRegistry.getInstrumentation().getTargetContext(),tests=InstrumentationRegistry.getInstrumentation().getContext();JSONArray reports=new JSONArray();
        try(YamnetClassifier model=new YamnetClassifier(target)){
            for(String name:List.of("snoring-sorts","snoring-pencil","speech-crowd","speech-countdown")){
                byte[] pcm;try(InputStream in=tests.getAssets().open("classifier-fixtures/"+name+".pcm")){ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] chunk=new byte[8192];for(int n;(n=in.read(chunk))!=-1;)bytes.write(chunk,0,n);pcm=bytes.toByteArray();}
                assertTrue(pcm.length>15600*2);float[] frame=new float[15600];JSONArray windows=new JSONArray(),events=new JSONArray();SoundAnalysis.Group group=new SoundAnalysis.Group((start,count,category,score)->{try{events.put(new JSONObject().put("start_ms",start-1).put("duration_ms",count*975).put("category",category).put("score",score));}catch(JSONException e){throw new IllegalStateException(e);}});
                double snoreMax=0,speechMax=0;int frames=pcm.length/(15600*2);
                for(int window=0;window<frames;window++){
                    for(int n=0;n<frame.length;n++){int index=(window*15600+n)*2;frame[n]=(short)((pcm[index]&255)|(pcm[index+1]<<8))/32768f;}
                    float[] scores=model.infer(frame);SoundAnalysis.Result selected=SoundAnalysis.classify(scores);int top=0;for(int n=1;n<scores.length;n++)if(scores[n]>scores[top])top=n;
                    snoreMax=Math.max(snoreMax,scores[38]);speechMax=Math.max(speechMax,scores[0]);float competitor=0;for(int n=0;n<scores.length;n++)if(n!=38&&n!=36)competitor=Math.max(competitor,scores[n]);windows.put(new JSONObject().put("start_ms",window*975).put("top_index",top).put("top_score",scores[top]).put("snoring_score",scores[38]).put("breathing_score",scores[36]).put("snoring_competitor_score",competitor).put("speech_score",scores[0]).put("silence_score",scores[494]).put("selected",selected==null?"none":selected.category));group.add(1+window*975,selected);
                }
                group.flush();JSONObject report=new JSONObject().put("fixture",name).put("expected_broad_class",name.startsWith("snoring-")?"snoring":"speech").put("frames",frames).put("snoring_max",snoreMax).put("speech_max",speechMax).put("windows",windows).put("policy_events",events);
                String database="sleepwell-verification-"+UUID.randomUUID()+".db";long session;
                try{
                    try(Store store=new Store(target,database)){
                        session=store.begin();long start=store.rows("SELECT start FROM sessions WHERE id=?",""+session).get(0).getLong("start");
                        SoundAnalysisStream.Model sharedModel=new SoundAnalysisStream.Model(){public float[] infer(float[] input){return model.infer(input);}public void close(){/* Outer scope owns the reused native model. */}};
                        try(SoundAnalysisStream stream=new SoundAnalysisStream(()->sharedModel,(at,count,category,score)->store.soundEvent(session,at,count*975L,category,score,null),()->fail("Fixture inference unexpectedly failed"))){
                            short[] chunk=new short[1024];int total=pcm.length/2;
                            for(int offset=0;offset<total;offset+=chunk.length){int count=Math.min(chunk.length,total-offset);for(int n=0;n<count;n++){int index=(offset+n)*2;chunk[n]=(short)((pcm[index]&255)|(pcm[index+1]<<8));}stream.accept(chunk,count,start+(offset+count)*1000L/16000);}
                        }
                        store.end(session,false,pcm.length*1000L/32000,start+pcm.length*1000L/32000);assertTrue("Analysis-only fixtures must not create audio clips",store.rows("SELECT id FROM clips").isEmpty());
                    }
                    try(Store reopened=new Store(target,database)){
                        List<JSONObject> stored=reopened.rows("SELECT * FROM sound_events WHERE session=? ORDER BY time",""+session);int snoring=0,speech=0,other=0;
                        for(JSONObject event:stored){String category=event.getString("category");if(category.equals("snoring"))snoring++;else if(category.equals("speech"))speech++;else other++;}
                        report.put("persisted_events",new JSONArray(stored)).put("persisted_snoring_count",snoring).put("persisted_speech_count",speech).put("persisted_other_count",other);
                    }
                }finally{target.deleteDatabase(database);}
                reports.put(report);
            }
        }
        File folder=new File(target.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());try(Writer out=new OutputStreamWriter(new FileOutputStream(new File(folder,"analysis-human-fixture-evaluation.json")),java.nio.charset.StandardCharsets.UTF_8)){out.write(new JSONObject().put("model",YamnetClassifier.MODEL_ID).put("scope","Four CC0 public previews, observational compatibility evidence; not calibrated bedroom or clinical accuracy").put("fixtures",reports).toString(2));}
        // Positive-class compatibility, separate from the stricter event policy whose output is reported above.
        assertTrue("A real snoring fixture must elicit a nontrivial snoring score",reports.getJSONObject(0).getDouble("snoring_max")>.2);
        assertTrue("A second independent snoring fixture must elicit a nontrivial snoring score",reports.getJSONObject(1).getDouble("snoring_max")>.2);
        assertTrue("Speech fixture must elicit a nontrivial speech score",reports.getJSONObject(2).getDouble("speech_max")>.2);
        assertTrue("Speech fixture must not cross the snoring threshold",reports.getJSONObject(2).getDouble("snoring_max")<.6);
        assertEquals("Crowd distractor must not persist snoring",0,reports.getJSONObject(2).getInt("persisted_snoring_count"));
        assertTrue("Actual PCM through production framing, score policy, grouping and reopened storage must produce snoring",reports.getJSONObject(0).getInt("persisted_snoring_count")+reports.getJSONObject(1).getInt("persisted_snoring_count")>0);
        assertTrue("Actual solo speech PCM through production pipeline must persist speech",reports.getJSONObject(3).getInt("persisted_speech_count")>0);
        assertEquals("Solo speech must not persist snoring",0,reports.getJSONObject(3).getInt("persisted_snoring_count"));
    }
}
