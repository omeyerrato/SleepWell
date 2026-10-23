package org.sleepwell.app;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class SoundAnalysisTest {
    private float[] scores(int label,float score){float[] s=new float[521];s[label]=score;return s;}
    @Test public void amplitudeDoesNotClassifyAndWeakOrAmbiguousPredictionsStayOther(){assertNull(SoundAnalysis.classify(new float[521]));assertEquals("noise",SoundAnalysis.classify(scores(38,.59f)).category);float[] ambiguous=scores(38,.8f);ambiguous[0]=.7f;assertEquals("noise",SoundAnalysis.classify(ambiguous).category);float[] competing=scores(38,.85f);competing[200]=.9f;assertEquals("noise",SoundAnalysis.classify(competing).category);}
    @Test public void snoringAndSpeechMustWinAllLabelsWithAMargin(){assertEquals("snoring",SoundAnalysis.classify(scores(38,.8f)).category);assertEquals("speech",SoundAnalysis.classify(scores(0,.8f)).category);assertEquals("noise",SoundAnalysis.classify(scores(0,.6f)).category);assertNull(SoundAnalysis.classify(scores(494,.9f)));}
    @Test public void invalidScoresRejectBeforePersistence(){for(float v:new float[]{Float.NaN,Float.POSITIVE_INFINITY,-.1f,1.1f})try{SoundAnalysis.classify(scores(38,v));fail();}catch(IllegalArgumentException expected){}try{SoundAnalysis.classify(new float[520]);fail();}catch(IllegalArgumentException expected){}}
    @Test public void isolatedFalsePositiveNeverBecomesAnEvent(){List<String> out=new ArrayList<>();SoundAnalysis.Group group=new SoundAnalysis.Group((start,count,category,score)->out.add(category));group.add(1000,new SoundAnalysis.Result("snoring",.9));group.add(1975,null);group.flush();assertTrue(out.isEmpty());}
    @Test public void groupedEventRetainsStartDurationMeanAndFlushIsIdempotent(){List<String> out=new ArrayList<>();SoundAnalysis.Group group=new SoundAnalysis.Group((start,count,category,score)->out.add(start+":"+count+":"+category+":"+score));group.add(1000,new SoundAnalysis.Result("snoring",.8));group.add(1975,new SoundAnalysis.Result("snoring",.9));group.add(2950,new SoundAnalysis.Result("speech",.9));group.flush();group.flush();assertEquals(List.of("1000:2:snoring:0.8500000000000001"),out);}
    @Test public void continuousSoundHasBoundedEventLength(){List<Integer> counts=new ArrayList<>();SoundAnalysis.Group group=new SoundAnalysis.Group((start,count,category,score)->counts.add(count));for(int n=0;n<25;n++)group.add(1000+n*975,new SoundAnalysis.Result("noise",.7));group.flush();assertEquals(List.of(10,10,5),counts);}
}
