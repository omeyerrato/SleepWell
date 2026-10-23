package org.sleepwell.app;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

/** No Android audio classes: clocks, decoders and outputs are entirely in memory. */
public class SoundPlaybackTest {
    static final class Fake implements SoundPlayback.Clock,SoundPlayback.Sink {
        long now;final List<Layer> layers=new ArrayList<>();boolean refuse;
        public long now(){return now;}
        public SoundPlayback.Layer create(String source){if(refuse)throw new IllegalStateException();Layer layer=new Layer(source);layers.add(layer);return layer;}
        void ready(){for(Layer layer:new ArrayList<>(layers))if(!layer.closed&&layer.callback!=null)layer.callback.ready();}
    }
    static final class Layer implements SoundPlayback.Layer {
        final String source;boolean closed;int starts;float gain;SoundPlayback.Ready callback;
        Layer(String source){this.source=source;}
        public void prepare(SoundPlayback.Ready callback){this.callback=callback;}
        public void gain(float value){assertFalse(closed);gain=value;}
        public void start(){assertFalse(closed);starts++;}
        public void close(){closed=true;}
    }
    private final Fake sink=new Fake();private final SoundPlayback engine=new SoundPlayback(sink,sink);
    private SoundMix profile(){return new SoundMix();}
    private void play(){engine.play(profile());sink.ready();}
    @Test public void choicesAndConstructionNeverOpenAPlayer(){SoundMix m=profile();m.primary="Ocean";engine.update(m);assertTrue(sink.layers.isEmpty());assertEquals(SoundPlayback.State.STOPPED,engine.state);}
    @Test public void loadingTimeoutReleasesAndIgnoresLateReady(){engine.play(profile());sink.now=30000;engine.tick();assertEquals(SoundPlayback.State.ERROR,engine.state);for(Layer l:sink.layers){assertTrue(l.closed);l.callback.ready();assertEquals(0,l.starts);}assertEquals(SoundPlayback.State.ERROR,engine.state);}
    @Test public void pauseDuringFadeRestoresExactFadePositionOnResume(){play();sink.now=27*60000+30000;engine.pause();sink.now+=100000;engine.resume();sink.ready();assertEquals(.35,sink.layers.get(2).gain,.001);assertEquals(.1,sink.layers.get(3).gain,.001);}
    @Test public void loadingWaitsForBothLayersAndDoesNotConsumeTimer(){engine.play(profile());sink.now=30000;sink.layers.get(0).callback.ready();assertEquals(0,sink.layers.get(0).starts);assertEquals(0,engine.elapsed());sink.layers.get(1).callback.ready();assertEquals(SoundPlayback.State.PLAYING,engine.state);assertEquals(0,engine.elapsed());assertEquals(1,sink.layers.get(0).starts);}
    @Test public void actualGainsChangeWithoutRestarting(){play();assertEquals(.7,sink.layers.get(0).gain,.001);assertEquals(.2,sink.layers.get(1).gain,.001);SoundMix m=profile();m.primaryGain=25;m.brownGain=10;engine.update(m);assertEquals(.25,sink.layers.get(0).gain,.001);assertEquals(.1,sink.layers.get(1).gain,.001);assertEquals(2,sink.layers.size());assertEquals(1,sink.layers.get(0).starts);}
    @Test public void summedGainsAreBoundedEvenAtMaximum(){SoundMix m=profile();m.primaryGain=m.brownGain=100;engine.play(m);sink.ready();assertEquals(.5,sink.layers.get(0).gain,.001);assertEquals(1,sink.layers.get(0).gain+sink.layers.get(1).gain,.001);}
    @Test public void fadeMultipliesEverySourceAndDuck(){play();sink.now=27*60000+30000;engine.tick();assertEquals(.35,sink.layers.get(0).gain,.001);assertEquals(.1,sink.layers.get(1).gain,.001);engine.duck(true);assertEquals(.07,sink.layers.get(0).gain,.001);assertEquals(.02,sink.layers.get(1).gain,.001);engine.duck(false);assertEquals(.35,sink.layers.get(0).gain,.001);}
    @Test public void timerEndsReleasesAndTicksNeverRestart(){play();sink.now=30*60000;engine.tick();assertEquals(SoundPlayback.State.FINISHED,engine.state);assertTrue(sink.layers.stream().allMatch(l->l.closed));sink.now+=60000;engine.tick();engine.resume();assertEquals(2,sink.layers.size());assertEquals(30*60000,engine.elapsed());}
    @Test public void pauseFreezesElapsedAndExplicitResumeContinuesIt(){play();sink.now=8000;engine.pause();sink.now=80000;assertEquals(8000,engine.elapsed());assertTrue(sink.layers.stream().allMatch(l->l.closed));engine.resume();sink.ready();sink.now=82000;assertEquals(10000,engine.elapsed());}
    @Test public void pauseWhilePreparingInvalidatesReadyCallback(){engine.play(profile());Layer pending=sink.layers.get(0);engine.pause();pending.callback.ready();assertEquals(0,pending.starts);assertTrue(pending.closed);assertEquals(SoundPlayback.State.PAUSED,engine.state);}
    @Test public void stopWhilePreparingPreventsStaleStart(){engine.play(profile());List<Layer> previous=new ArrayList<>(sink.layers);engine.stop();for(Layer l:previous)l.callback.ready();assertEquals(SoundPlayback.State.STOPPED,engine.state);assertTrue(previous.stream().allMatch(l->l.closed&&l.starts==0));}
    @Test public void replacingSourceRetainsClockAndClosesOldDecoder(){play();sink.now=9000;SoundMix m=profile();m.primary="Ocean";engine.update(m);assertEquals(9000,engine.elapsed());assertTrue(sink.layers.get(0).closed);sink.ready();sink.now=11000;assertEquals(11000,engine.elapsed());assertEquals("Ocean",sink.layers.get(2).source);}
    @Test public void staleFailureCannotStopReplacement(){engine.play(profile());Layer old=sink.layers.get(0);SoundMix m=profile();m.primary="Wind";engine.update(m);sink.ready();old.callback.failed();assertEquals(SoundPlayback.State.PLAYING,engine.state);assertEquals("Wind",engine.mix().primary);}
    @Test public void sourceFailureReleasesWholeMix(){engine.play(profile());sink.layers.get(0).callback.failed();assertEquals(SoundPlayback.State.ERROR,engine.state);assertFalse(engine.error.isEmpty());assertTrue(sink.layers.stream().allMatch(l->l.closed));}
    @Test public void allocationFailureIsAnErrorAndCannotRestartItself(){sink.refuse=true;engine.play(profile());assertEquals(SoundPlayback.State.ERROR,engine.state);engine.resume();assertTrue(sink.layers.isEmpty());}
    @Test public void shorterTimerEndsAlreadyElapsedMix(){play();sink.now=20*60000;SoundMix m=profile();m.timerMinutes=15;engine.update(m);assertEquals(SoundPlayback.State.FINISHED,engine.state);assertTrue(sink.layers.stream().allMatch(l->l.closed));}
    @Test public void expiredTrackingOffsetDoesNotEvenAllocate(){engine.play(profile(),30*60000);assertEquals(SoundPlayback.State.FINISHED,engine.state);assertTrue(sink.layers.isEmpty());}
    @Test public void brownPrimaryDoesNotDuplicateBrownLayer(){SoundMix m=profile();m.primary="Brown noise";engine.play(m);sink.ready();assertEquals(1,sink.layers.size());assertEquals(.7,sink.layers.get(0).gain,.001);}
    @Test public void localDocumentReplacementUsesUriAndPreservesPause(){play();engine.pause();SoundMix m=profile();m.primary="My audio";m.uri="content://local/123";engine.update(m);assertEquals(2,sink.layers.size());engine.resume();sink.ready();assertEquals("content://local/123",sink.layers.get(2).source);}
    @Test public void oldSettingsKeepTheirOldLevelsAndNoFade(){for(String name:List.of("Rain","Brown noise")){SoundMix m=SoundMix.legacy(name,45);m.validate();assertEquals(name,m.primary);assertEquals(20,m.primaryGain);assertEquals(0,m.brownGain);assertEquals(45,m.timerMinutes);assertEquals(0,m.fadeMinutes);assertTrue(m.withTracking);}SoundMix off=SoundMix.legacy("Off",30);assertFalse(off.withTracking);assertEquals(70,off.primaryGain);}
    @Test public void invalidMixCannotAuthorizeExternalNetworkSource(){SoundMix m=profile();m.primary="My audio";m.uri="https://example.com/audio";try{m.validate();fail();}catch(IllegalArgumentException expected){}m.uri="";try{m.validate();fail();}catch(IllegalArgumentException expected){}}
    @Test public void trackingCannotStealManualSessionOrRetryAfterItEnds(){SoundOwnership o=new SoundOwnership();assertFalse(o.claim(12,true));assertFalse(o.claim(12,false));assertFalse(o.pause(12));assertFalse(o.stop(12));assertTrue(o.claim(13,false));}
    @Test public void onlyTrackingPauseMayAutoResumeOwnMix(){SoundOwnership o=new SoundOwnership();assertTrue(o.claim(12,false));assertTrue(o.pause(12));assertTrue(o.resume(12));assertFalse(o.resume(12));assertTrue(o.pause(12));o.manual();assertFalse(o.resume(12));assertFalse(o.stop(12));assertFalse(o.claim(12,false));}
    @Test public void sessionStopDoesNotStopAnotherSession(){SoundOwnership o=new SoundOwnership();assertTrue(o.claim(12,false));assertFalse(o.stop(13));assertTrue(o.stop(12));assertFalse(o.claim(12,false));}
}
