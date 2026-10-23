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
            for(String name:List.of("snoring-sorts","snoring-pencil","speech-crowd")){
                byte[] pcm;try(InputStream in=tests.getAssets().open("classifier-fixtures/"+name+".pcm")){ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] chunk=new byte[8192];for(int n;(n=in.read(chunk))!=-1;)bytes.write(chunk,0,n);pcm=bytes.toByteArray();}
                assertTrue(pcm.length>15600*2);float[] frame=new float[15600];JSONArray windows=new JSONArray(),events=new JSONArray();SoundAnalysis.Group group=new SoundAnalysis.Group((start,count,category,score)->{try{events.put(new JSONObject().put("start_ms",start-1).put("duration_ms",count*975).put("category",category).put("score",score));}catch(JSONException e){throw new IllegalStateException(e);}});
                double snoreMax=0,speechMax=0;int frames=pcm.length/(15600*2);
                for(int window=0;window<frames;window++){
                    for(int n=0;n<frame.length;n++){int index=(window*15600+n)*2;frame[n]=(short)((pcm[index]&255)|(pcm[index+1]<<8))/32768f;}
                    float[] scores=model.infer(frame);SoundAnalysis.Result selected=SoundAnalysis.classify(scores);int top=0;for(int n=1;n<scores.length;n++)if(scores[n]>scores[top])top=n;
                    snoreMax=Math.max(snoreMax,scores[38]);speechMax=Math.max(speechMax,scores[0]);windows.put(new JSONObject().put("start_ms",window*975).put("top_index",top).put("top_score",scores[top]).put("snoring_score",scores[38]).put("speech_score",scores[0]).put("silence_score",scores[494]).put("selected",selected==null?"none":selected.category));group.add(1+window*975,selected);
                }
                group.flush();reports.put(new JSONObject().put("fixture",name).put("frames",frames).put("snoring_max",snoreMax).put("speech_max",speechMax).put("windows",windows).put("policy_events",events));
            }
        }
        File folder=new File(target.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());try(Writer out=new OutputStreamWriter(new FileOutputStream(new File(folder,"analysis-human-fixture-evaluation.json")),java.nio.charset.StandardCharsets.UTF_8)){out.write(new JSONObject().put("model",YamnetClassifier.MODEL_ID).put("scope","Three CC0 public previews, observational compatibility evidence; not calibrated bedroom or clinical accuracy").put("fixtures",reports).toString(2));}
        // Positive-class compatibility, separate from the stricter event policy whose output is reported above.
        assertTrue("A real snoring fixture must elicit a nontrivial snoring score",reports.getJSONObject(0).getDouble("snoring_max")>.2);
        assertTrue("A second independent snoring fixture must elicit a nontrivial snoring score",reports.getJSONObject(1).getDouble("snoring_max")>.2);
        assertTrue("Speech fixture must elicit a nontrivial speech score",reports.getJSONObject(2).getDouble("speech_max")>.2);
        assertTrue("Speech fixture must not cross the snoring threshold",reports.getJSONObject(2).getDouble("snoring_max")<.6);
    }
}
