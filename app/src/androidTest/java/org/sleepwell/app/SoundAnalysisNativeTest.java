package org.sleepwell.app;

import android.content.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.*;
import static org.junit.Assert.*;

/** In-memory synthetic inputs only: no AudioRecord, MediaPlayer, outputs or production database. */
@RunWith(AndroidJUnit4.class)
public class SoundAnalysisNativeTest {
    private Context context(){return InstrumentationRegistry.getInstrumentation().getTargetContext();}
    private static boolean topTen(float[] scores,int label){int larger=0;for(float v:scores)if(v>scores[label])larger++;return larger<10;}
    @Test public void bundledCpuModelSeparatesSilenceSineAndWhiteNoiseWithoutSpeechOrSnoring(){
        // AndroidJUnit invokes test methods off the app's UI thread; classifier owns this thread.
        assertNotEquals(android.os.Looper.getMainLooper(),android.os.Looper.myLooper());
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{try{new YamnetClassifier(context());fail("UI-thread model creation must reject before native allocation");}catch(IllegalStateException expected){assertEquals("Sound model must load on a worker",expected.getMessage());}});
        try(YamnetClassifier classifier=new YamnetClassifier(context())){
            float[] silence=classifier.infer(new float[15600]);assertTrue(topTen(silence,494));SoundAnalysis.Result silent=SoundAnalysis.classify(silence);assertTrue(silent==null||silent.category.equals("noise"));
            float[] samples=new float[15600];for(int n=0;n<samples.length;n++)samples[n]=(float)Math.sin(2*Math.PI*440*n/16000);float[] sine=classifier.infer(samples);SoundAnalysis.Result tone=SoundAnalysis.classify(sine);assertTrue(tone==null||tone.category.equals("noise"));
            Random random=new Random(51773);for(int n=0;n<samples.length;n++)samples[n]=random.nextFloat()*2-1;float[] noise=classifier.infer(samples);SoundAnalysis.Result white=SoundAnalysis.classify(noise);assertTrue(white==null||white.category.equals("noise"));
            for(float[] output:List.of(silence,sine,noise)){assertEquals(521,output.length);for(float score:output)assertTrue(Float.isFinite(score)&&score>=0&&score<=1);}
        }
    }
    @Test public void estimatesPersistWithoutSavingAudioAndDeleteWithTheirNight() throws Exception {
        Context c=context();String name="sleepwell-verification-"+UUID.randomUUID()+".db";try{
            long id;
            try(Store store=new Store(c,name)){id=store.begin();long start=store.rows("SELECT start FROM sessions WHERE id=?",""+id).get(0).getLong("start");store.soundEvent(id,start,1950,"snoring",.8,null);store.soundEvent(id,start+3000,1950,"speech",.9,null);store.end(id,false,6000,start+6000);assertTrue(store.rows("SELECT * FROM clips").isEmpty());}
            try(Store store=new Store(c,name)){List<JSONObject> values=store.rows("SELECT * FROM sound_events WHERE session=? ORDER BY time",""+id);assertEquals(2,values.size());long first=values.get(0).getLong("time");assertEquals("RawQuery string bounds need explicit numeric affinity for arithmetic",2,store.rows("SELECT id FROM sound_events WHERE session=? AND time+duration_ms>CAST(? AS INTEGER) AND time<CAST(? AS INTEGER)",""+id,""+(first+1000),""+(first+6000)).size());assertTrue(store.rows("SELECT id FROM sound_events WHERE session=? AND time+duration_ms>CAST(? AS INTEGER)",""+id,""+(first+6000)).isEmpty());assertEquals("snoring",values.get(0).getString("category"));assertEquals(.8,values.get(0).getDouble("score"),0);assertTrue(values.get(0).isNull("clip"));Records.delete(store,id,0);assertTrue(store.rows("SELECT * FROM sound_events").isEmpty());}
        }finally{c.deleteDatabase(name);}
    }
    @Test public void savedRecordingsLinkOnlyFullyContainedIntervalsBeforeOrAfterTheEvent() throws Exception {
        Context c=context();String name="sleepwell-verification-"+UUID.randomUUID()+".db";try(Store store=new Store(c,name)){
            long id=store.begin(),start=store.rows("SELECT start FROM sessions WHERE id=?",""+id).get(0).getLong("start");
            store.soundEvent(id,start+1000,1950,"snoring",.8,null);store.soundEvent(id,start+9000,1950,"speech",.8,null);
            store.clip(id,start+10000,"synthetic-unplayed.wav",.1,10000);
            List<JSONObject> first=store.rows("SELECT clip FROM sound_events ORDER BY time");assertFalse(first.get(0).isNull("clip"));assertTrue(first.get(1).isNull("clip"));
            store.soundEvent(id,start+3000,1950,"snoring",.8,null);assertFalse(store.rows("SELECT clip FROM sound_events ORDER BY id DESC LIMIT 1").get(0).isNull("clip"));
            store.soundEvent(id,start-100,1950,"noise",.8,null);assertTrue(store.rows("SELECT clip FROM sound_events ORDER BY id DESC LIMIT 1").get(0).isNull("clip"));
            store.getWritableDatabase().delete("clips",null,null);assertTrue(store.rows("SELECT id FROM sound_events WHERE clip IS NOT NULL").isEmpty());
            store.clip(id,start+20000,"different-unplayed.wav",.1,1000);assertTrue(store.rows("SELECT id FROM sound_events WHERE clip IS NOT NULL").isEmpty());
        }finally{c.deleteDatabase(name);}
    }
}
