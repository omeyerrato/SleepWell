package org.sleepwell.app;

import android.content.*;
import android.os.*;
import android.widget.Switch;
import org.junit.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

/** Real configuration/ticket/night/storage, injected duplex and model. No services or hardware. */
public final class SonarSharedSoundFlowTest {
 void commands(SonarNightFlowTest h,boolean fail,AtomicInteger loads){
  TrackingActivity.Commands delegate=TrackingActivity.commands;
  TrackingActivity.commands=new TrackingActivity.Commands(){
   public TrackingService.Snapshot snapshot(){return delegate.snapshot();}
   public void review(TrackingActivity owner){delegate.review(owner);}
   public void start(TrackingActivity owner,Intent intent){assertEquals(TrackingService.SONAR,intent.getAction());SonarEntry.Ticket ticket=SonarEntry.consume(intent.getStringExtra("sonarAdmission"),SystemClock.elapsedRealtime(),0);assertNotNull(ticket);assertTrue("Explicit visible this-night opt-in reached the immutable ticket",ticket.analysisRequested);
    h.night=new SonarNight(ticket.config,(config,cancelled)->{SonarNightStorageTest.Echo io=new SonarNightStorageTest.Echo(h.clock);h.audio.add(io);return io;},new SonarNightStorageTest.PairedStore(h.c,ctx->new Store(ctx,h.database),h.clock),h.clock,new Handler(Looper.getMainLooper())::post,state->{},ticket.analysisRequested,()->{loads.incrementAndGet();if(fail)throw new IllegalStateException("Injected model load error");Thread worker=Thread.currentThread();assertEquals("SleepWell-sonar-analysis",worker.getName());return new SoundAnalysisStream.Model(){public float[] infer(float[] pcm){assertSame(worker,Thread.currentThread());float[] scores=new float[521];scores[494]=.9f;return scores;}public void close(){assertSame(worker,Thread.currentThread());}};});h.night.start();
   }
   public void command(TrackingActivity owner,Intent intent){if(TrackingService.RESUME.equals(intent.getAction())){SonarEntry.Ticket ticket=SonarEntry.consume(intent.getStringExtra("sonarAdmission"),SystemClock.elapsedRealtime(),h.night.state().id);assertTrue(SonarEntry.sameRun(ticket,h.night.state().id,new SonarConfig(h.night.state().gain),h.night.state().analysis.requested));assertTrue(h.night.resume());}else delegate.command(owner,intent);}
  };
 }
 void checked(SonarNightFlowTest h,boolean expected){h.i.runOnMainSync(()->{android.view.View control=h.find(h.activity.getWindow().getDecorView(),"Analyze sleep sounds");assertTrue(control instanceof Switch);assertEquals(expected,((Switch)control).isChecked());});}
 @Test public void optInRecreateStartPauseResumeAndSavedZeroEventEvidence()throws Exception {
  SonarNightFlowTest h=new SonarNightFlowTest();h.evidencePrefix="sonar-shared-opt-in-";AtomicInteger loads=new AtomicInteger();h.fixture(()->{
   h.i.runOnMainSync(()->commands(h,false,loads));h.launch();checked(h,false);h.capture("default-off","Analyze sleep sounds");assertTrue(h.audio.isEmpty());assertEquals(0,loads.get());h.click("Analyze sleep sounds");checked(h,true);h.recreate();checked(h,true);h.capture("opt-in-recreated","Analyze sleep sounds");
   h.click("Start tracking  →");h.waitFor(()->h.night!=null&&h.night.state().analysis.frames>=2);h.capture("analysis-live","Analysis only");assertEquals(1,h.audio.size());h.click("Pause tracking");h.waitFor(()->h.night.state().phase.equals("PAUSED"));h.capture("analysis-paused","Resume tracking");assertTrue(h.audio.get(0).closed);long first=h.night.state().analysis.frames;
   h.click("Resume tracking");h.waitFor(()->h.audio.size()==2&&h.night.state().analysis.frames>first);h.click("Stop tracking");h.confirmEnd();assertTrue(h.night.awaitClosed(10000));h.waitFor(()->h.reviewed&&h.activity.isDestroyed());try(Store reopened=new Store(h.c,h.database)){RecordEntry record=Records.all(reopened).get(0);SoundAnalysisStatus status=SoundAnalysisProvenance.read(record.soundAnalysis);assertTrue(status.requested);assertEquals("complete",status.state);assertTrue(status.frames>first);assertTrue(reopened.rows("SELECT * FROM sound_events").isEmpty());assertTrue(reopened.rows("SELECT * FROM clips").isEmpty());assertEquals(1,reopened.rows("SELECT * FROM tracking_pauses WHERE end IS NOT NULL").size());Backup.validate(reopened.backup());}assertEquals(2,loads.get());h.launch();checked(h,false);assertEquals("Reopening setup cannot replay capture",2,h.audio.size());assertEquals(2,loads.get());
  });
 }
 @Test public void failedAnalysisRemainsVisibleAndCannotEscalateOrReplayOnResume()throws Exception {
  SonarNightFlowTest h=new SonarNightFlowTest();h.evidencePrefix="sonar-shared-error-";AtomicInteger loads=new AtomicInteger();h.fixture(()->{
   h.i.runOnMainSync(()->commands(h,true,loads));h.launch();h.click("Analyze sleep sounds");h.click("Start tracking  →");h.waitFor(()->h.night!=null&&h.night.state().analysis.state.equals("error"));h.capture("model-error","Sound analysis stopped · model unavailable");assertEquals("ACTIVE",h.night.state().phase);assertFalse(h.audio.get(0).closed);
   h.click("Pause tracking");h.waitFor(()->h.night.state().phase.equals("PAUSED"));h.recreate();h.capture("model-error-recreated","Sound analysis stopped · model unavailable");h.click("Resume tracking");h.waitFor(()->h.audio.size()==2&&h.night.state().phase.equals("ACTIVE"));h.capture("model-error-resumed","Sound analysis stopped · model unavailable");assertEquals(1,loads.get());h.click("Stop tracking");h.confirmEnd();assertTrue(h.night.awaitClosed(10000));h.waitFor(()->h.reviewed&&h.activity.isDestroyed());try(Store reopened=new Store(h.c,h.database)){SoundAnalysisStatus status=SoundAnalysisProvenance.read(Records.all(reopened).get(0).soundAnalysis);assertEquals("error",status.state);assertEquals("model",status.issue);assertEquals(0,status.frames);assertTrue(status.requested);assertTrue(reopened.rows("SELECT * FROM clips").isEmpty());Backup.validate(reopened.backup());}
  });
 }
}
