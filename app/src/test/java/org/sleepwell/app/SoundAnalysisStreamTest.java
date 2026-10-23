package org.sleepwell.app;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class SoundAnalysisStreamTest {
    static final class Model implements SoundAnalysisStream.Model {int calls,closes;boolean fail;float first,last;public float[] infer(float[] pcm){calls++;first=pcm[0];last=pcm[15599];if(fail)throw new IllegalStateException("Injected inference failure");float[] scores=new float[521];scores[38]=.9f;return scores;}public void close(){closes++;}}
    @Test public void framesEverySampleWithoutPaddingAndDiscardsIncompleteTail(){Model model=new Model();List<Long> starts=new ArrayList<>();try(SoundAnalysisStream stream=new SoundAnalysisStream(()->model,(at,count,category,score)->starts.add(at),()->fail())){short[] pcm=new short[15600];Arrays.fill(pcm,(short)16384);stream.accept(pcm,15600,1975);stream.accept(pcm,15600,2950);stream.accept(new short[100],100,2956);assertEquals(2,model.calls);assertEquals(.5,model.first,0);assertEquals(.5,model.last,0);}assertEquals(List.of(1000L),starts);assertEquals(1,model.closes);}
    @Test public void chunksPreserveSignedPcmAndNeverInferUntilAFullWindow(){Model model=new Model();SoundAnalysisStream stream=new SoundAnalysisStream(()->model,(at,count,category,score)->{},()->fail());short[] pcm=new short[7800];Arrays.fill(pcm,Short.MIN_VALUE);stream.accept(pcm,pcm.length,1487);assertEquals(0,model.calls);stream.accept(pcm,pcm.length,1975);assertEquals(1,model.calls);assertEquals(-1,model.first,0);assertEquals(-1,model.last,0);stream.close();stream.accept(new short[15600],15600,3000);assertEquals(1,model.calls);stream.close();assertEquals(1,model.closes);}
    @Test public void loadFailureDoesNotAllocateOrInventSoundEvents(){int[] failures={0},loads={0};SoundAnalysisStream stream=new SoundAnalysisStream(()->{loads[0]++;throw new IllegalStateException();},(at,count,category,score)->fail(),()->failures[0]++);stream.accept(new short[40000],40000,4000);stream.close();assertEquals(1,loads[0]);assertEquals(1,failures[0]);}
    @Test public void inferenceFailureReleasesModelOnceAndLaterPcmDoesNotRetry(){Model model=new Model();model.fail=true;int[] failures={0};SoundAnalysisStream stream=new SoundAnalysisStream(()->model,(at,count,category,score)->fail(),()->failures[0]++);stream.accept(new short[15600],15600,2000);stream.accept(new short[15600],15600,3000);stream.close();assertEquals(1,model.calls);assertEquals(1,model.closes);assertEquals(1,failures[0]);}
    @Test public void clipAndMetadataBudgetsRemainSharedAcrossRecorderRestarts(){ClipBudget budget=new ClipBudget();for(int n=0;n<3600;n++)assertTrue(budget.saveEvent());assertFalse(budget.saveEvent());assertTrue(budget.canStart(0));budget.saved(0);assertFalse(budget.canStart(29999));assertTrue(budget.canStart(30000));}
}
