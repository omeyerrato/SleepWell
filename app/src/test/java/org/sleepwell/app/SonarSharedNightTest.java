package org.sleepwell.app;

import org.junit.Test;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.Assert.*;

/** Real pump/DSP/48→16k/stream coordinator, injected PCM/model/storage only. */
public final class SonarSharedNightTest {
 static final class Sink extends SonarNightTest.Sink {
  final List<SoundAnalysisStatus> statuses=new CopyOnWriteArrayList<>();final List<long[]> events=new CopyOnWriteArrayList<>();volatile SoundAnalysisStatus analysis;
  Sink(SonarNightTest.Clock clock){super(clock);}
  public SonarNight.Session begin(SonarConfig config,String capture,boolean requested){analysis=SoundAnalysisStatus.initial(requested);return super.begin(config,capture);}
  public void soundEvent(long id,long at,long duration,String category,double score){thread();assertEquals(7,id);assertEquals("speech",category);events.add(new long[]{at,at+duration});}
  public void soundStatus(long id,SoundAnalysisStatus next){thread();next.follows(analysis);analysis=next;statuses.add(next);}
  public void end(long id,long at,long active,boolean interrupted,SoundAnalysisStatus result){result.follows(analysis);analysis=result;super.end(id,at,active,interrupted);}
 }
 static final class Model implements SoundAnalysisStream.Factory {
  final AtomicInteger loads=new AtomicInteger(),calls=new AtomicInteger(),closed=new AtomicInteger();volatile boolean failed,blocked;final CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1);
  public SoundAnalysisStream.Model load(){loads.incrementAndGet();Thread owner=Thread.currentThread();assertEquals("SleepWell-sonar-analysis",owner.getName());return new SoundAnalysisStream.Model(){public float[] infer(float[] samples){assertSame(owner,Thread.currentThread());calls.incrementAndGet();entered.countDown();if(blocked)try{assertTrue(release.await(8,TimeUnit.SECONDS));}catch(InterruptedException e){throw new AssertionError("Teardown must not interrupt model inference",e);}if(failed)throw new IllegalStateException("Injected model error");float[] scores=new float[521];scores[0]=.9f;return scores;}public void close(){assertSame(owner,Thread.currentThread());closed.incrementAndGet();}};}
 }
 SonarNight night(SonarNightTest.Clock clock,Sink sink,Model model,boolean requested,List<SonarNightTest.Audio> audio){return new SonarNight(SonarConfig.defaults(),(config,cancel)->{SonarNightTest.Audio io=new SonarNightTest.Audio(clock,sink);audio.add(io);return io;},sink,clock,Runnable::run,state->{},requested,model);}
 @Test public void oneCaptureFeedsBothRealSignalPathAndSoundFramingThroughPauseResume()throws Exception {
  SonarNightTest.Clock clock=new SonarNightTest.Clock();Sink sink=new Sink(clock);Model model=new Model();List<SonarNightTest.Audio> audio=new CopyOnWriteArrayList<>();SonarNight night=night(clock,sink,model,true,audio);
  try{night.start();SonarNightTest.await(()->night.state().analysis.frames>=2&&!sink.events.isEmpty());assertEquals(1,audio.size());assertTrue(night.state().readings.stream().anyMatch(f->f.movement!=null));night.pause();SonarNightTest.await(()->night.state().phase.equals("PAUSED"));assertTrue(audio.get(0).closed);assertEquals(1,model.closed.get());long frames=night.state().analysis.frames,active=night.state().active;clock.now.addAndGet(60000);assertEquals(frames,night.state().analysis.frames);assertEquals(active,night.state().active);assertTrue(night.resume());SonarNightTest.await(()->model.loads.get()==2&&night.state().analysis.frames>=frames+2);night.stop(false);assertTrue(night.awaitClosed(8000));assertEquals(2,audio.size());assertEquals(2,model.closed.get());assertEquals("complete",sink.analysis.state);assertTrue(sink.analysis.frames>=frames+2);assertEquals(7,sink.session.id);for(long[] event:sink.events){assertTrue(event[0]>=sink.session.wall);assertTrue(event[1]<=sink.ended);for(int n=0;n<sink.pauses.size();n++)assertFalse(event[0]<sink.resumes.get(n)&&event[1]>sink.pauses.get(n));}}
  finally{model.release.countDown();night.stop(true);assertTrue(night.awaitClosed(8000));}
 }
 @Test public void offNeverLoadsAnalysisOrChangesTheOneActualCaptureOwner()throws Exception {
  SonarNightTest.Clock clock=new SonarNightTest.Clock();Sink sink=new Sink(clock);Model model=new Model();List<SonarNightTest.Audio> audio=new CopyOnWriteArrayList<>();SonarNight night=night(clock,sink,model,false,audio);
  try{night.start();SonarNightTest.await(()->night.state().readings.stream().anyMatch(f->f.movement!=null));night.stop(false);assertTrue(night.awaitClosed(8000));assertEquals(1,audio.size());assertEquals(0,model.loads.get());assertEquals("off",sink.analysis.state);assertFalse(sink.analysis.requested);assertTrue(sink.events.isEmpty());}finally{night.stop(true);assertTrue(night.awaitClosed(8000));}
 }
 @Test public void pauseWaitsForAnalysisAcknowledgementWithoutBlockingCaptureClose()throws Exception {
  SonarNightTest.Clock clock=new SonarNightTest.Clock();Sink sink=new Sink(clock);Model model=new Model();model.blocked=true;List<SonarNightTest.Audio> audio=new CopyOnWriteArrayList<>();SonarNight night=night(clock,sink,model,true,audio);
  try{night.start();assertTrue(model.entered.await(8,TimeUnit.SECONDS));night.pause();SonarNightTest.await(()->audio.get(0).closed);assertEquals("PAUSING",night.state().phase);assertFalse(night.resume());assertEquals(0,model.closed.get());model.release.countDown();SonarNightTest.await(()->night.state().phase.equals("PAUSED"));assertEquals(1,model.closed.get());assertTrue(sink.paused);night.stop(false);assertTrue(night.awaitClosed(8000));}finally{model.release.countDown();night.stop(true);assertTrue(night.awaitClosed(8000));}
 }
 @Test public void modelFailureIsSavedAndCannotRetryOnResumeWhileSonarStillWorks()throws Exception {
  SonarNightTest.Clock clock=new SonarNightTest.Clock();Sink sink=new Sink(clock);Model model=new Model();model.failed=true;List<SonarNightTest.Audio> audio=new CopyOnWriteArrayList<>();SonarNight night=night(clock,sink,model,true,audio);
  try{night.start();SonarNightTest.await(()->night.state().analysis.state.equals("error"));assertEquals("model",night.state().analysis.issue);assertEquals("ACTIVE",night.state().phase);assertFalse(audio.get(0).closed);assertEquals(0,night.state().analysis.frames);night.pause();SonarNightTest.await(()->night.state().phase.equals("PAUSED"));assertTrue(night.resume());SonarNightTest.await(()->audio.size()==2&&night.state().phase.equals("ACTIVE"));night.stop(false);assertTrue(night.awaitClosed(8000));assertEquals(1,model.loads.get());assertEquals(1,model.closed.get());assertEquals("error",sink.analysis.state);assertEquals("model",sink.analysis.issue);assertTrue(sink.analysis.requested);assertEquals(0,sink.analysis.frames);}finally{night.stop(true);assertTrue(night.awaitClosed(8000));}
 }
 @Test public void lossOfCaptureValidityCannotReachTheSoundPathAsGoodInput(){for(SonarFrame.Quality q:SonarFrame.Quality.values())assertEquals(q.name(),Set.of(SonarFrame.Quality.VALID,SonarFrame.Quality.CALIBRATING,SonarFrame.Quality.NEEDS_MOVEMENT,SonarFrame.Quality.NO_ECHO).contains(q),SonarPump.soundPcmValid(q));}
}
