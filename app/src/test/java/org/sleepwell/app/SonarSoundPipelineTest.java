package org.sleepwell.app;

import org.junit.Test;
import java.util.*;
import java.util.concurrent.atomic.*;
import static org.junit.Assert.*;

public final class SonarSoundPipelineTest {
 static final long WALL=1700000000000L,ORIGIN=9000000000L;
 static final class Fake implements SoundAnalysisStream.Factory {
  int loaded,calls,closed;boolean speech,fail;Thread owner;List<Long> events=new ArrayList<>();List<Integer> windows=new ArrayList<>();
  public SoundAnalysisStream.Model load(){loaded++;owner=Thread.currentThread();return new SoundAnalysisStream.Model(){public float[] infer(float[] samples){assertSame(owner,Thread.currentThread());assertEquals(15600,samples.length);calls++;if(fail)throw new IllegalStateException("Model unavailable");float[] scores=new float[521];scores[speech?0:494]=.9f;return scores;}public void close(){assertSame(owner,Thread.currentThread());closed++;}};}
  SonarSoundPipeline pipeline(){return new SonarSoundPipeline(this,(at,count,category,score)->{assertEquals("speech",category);events.add(at);windows.add(count);},WALL,ORIGIN);}
 }
 void blocks(SonarSoundPipeline pipeline,int from,int count){for(int n=from;n<from+count;n++)pipeline.accept(new short[1920],1920,ORIGIN+n*40000000L,true);}
 @Test public void zeroEventsStillCountsActualSuccessfulInferences(){Fake f=new Fake();SonarSoundPipeline p=f.pipeline();blocks(p,0,50);assertEquals(2,p.frames());assertTrue(f.events.isEmpty());assertEquals("",p.issue());p.close();assertEquals(1,f.loaded);assertEquals(1,f.closed);assertEquals(2,f.calls);}
 @Test public void classifiedGroupsRetainOriginalCaptureClockAndActualWindows(){Fake f=new Fake();f.speech=true;SonarSoundPipeline p=f.pipeline();blocks(p,0,50);p.close();assertEquals(List.of(WALL+2),f.events);assertEquals(List.of(2),f.windows);assertEquals(2,p.frames());}
 @Test public void invalidInputCannotBridgePartialFramesOrCreatePaddedEnd(){Fake f=new Fake();SonarSoundPipeline p=f.pipeline();blocks(p,0,24);p.accept(new short[1920],1920,ORIGIN+24*40000000L,false);blocks(p,25,24);p.close();assertEquals(0,p.frames());assertEquals(0,f.calls);assertEquals(1,f.loaded);assertEquals(1,f.closed);}
 @Test public void omittedInputIsARealGapRatherThanRepeatedOrInventedSamples(){Fake f=new Fake();SonarSoundPipeline p=f.pipeline();blocks(p,0,24);blocks(p,25,24);p.close();assertEquals(0,p.frames());assertEquals(0,f.calls);}
 @Test public void separatedSingleWindowsCannotBecomeOneFalseTwoWindowEvent(){Fake f=new Fake();f.speech=true;SonarSoundPipeline p=f.pipeline();blocks(p,0,25);p.accept(new short[1920],1920,ORIGIN+25*40000000L,false);blocks(p,26,25);p.close();assertEquals(2,p.frames());assertTrue(f.events.isEmpty());assertEquals(1,f.loaded);assertEquals(1,f.closed);}
 @Test public void inferenceFailureIsExplicitAndCannotRetryOrInventSuccessfulCounts(){Fake f=new Fake();f.fail=true;SonarSoundPipeline p=f.pipeline();blocks(p,0,50);assertEquals("model",p.issue());assertEquals(0,p.frames());assertEquals(1,f.calls);p.close();p.close();assertEquals(1,f.closed);assertEquals(1,f.loaded);}
 @Test public void callerCannotMoveModelOwnershipBetweenThreads()throws Exception {Fake f=new Fake();SonarSoundPipeline p=f.pipeline();AtomicReference<Throwable> failure=new AtomicReference<>();Thread foreign=new Thread(()->{try{blocks(p,0,50);}catch(Throwable t){failure.set(t);}});foreign.start();foreign.join(1000);assertFalse(foreign.isAlive());assertTrue(failure.get() instanceof IllegalStateException);assertEquals(0,f.loaded);p.close();}
 @Test public void eventSinkFailureIsNotMisreportedAsAClassifierSuccess(){Fake f=new Fake();f.speech=true;SonarSoundPipeline p=new SonarSoundPipeline(f,(at,n,c,s)->{throw new IllegalStateException("Storage queue rejected");},WALL,ORIGIN);blocks(p,0,50);p.close();assertEquals("storage",p.issue());assertEquals(2,p.frames());assertEquals(1,f.closed);}
 @Test public void gapFlushFailureIsIsolatedAndReleasesTheSingleModel(){for(boolean invalid:new boolean[]{true,false}){Fake f=new Fake();f.speech=true;SonarSoundPipeline p=new SonarSoundPipeline(f,(at,n,c,s)->{throw new IllegalStateException("Storage queue rejected at gap");},WALL,ORIGIN);blocks(p,0,50);p.accept(new short[1920],1920,ORIGIN+(invalid?50:51)*40000000L,!invalid);assertEquals("storage",p.issue());assertEquals(2,p.frames());p.close();assertEquals(1,f.closed);}}
}
