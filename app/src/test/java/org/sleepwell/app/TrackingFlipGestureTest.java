package org.sleepwell.app;
import org.junit.Test;import static org.junit.Assert.*;
public final class TrackingFlipGestureTest {
 static final long MS=1000000L;long at=1000*MS;TrackingFlipGesture g=new TrackingFlipGesture(at,1);
 boolean sample(double z){at+=100*MS;return g.sample(at,at,0,0,z,true);}boolean hold(double z,int count){boolean hit=false;for(int n=0;n<count;n++)hit|=sample(z);return hit;}
 @Test public void startingFaceDownNeverPauses(){assertFalse(hold(-9.8,100));}
 @Test public void stableUpThenDownTriggersOnce(){assertFalse(hold(9.8,12));assertTrue(hold(-9.8,14));assertFalse(hold(-9.8,100));}
 @Test public void initialUpOnlyDoesNotPause(){assertFalse(hold(9.8,100));}
 @Test public void transientFlipDoesNotPause(){hold(9.8,12);assertFalse(hold(-9.8,5));assertFalse(hold(9.8,5));}
 @Test public void largeMotionResetsTargetHold(){hold(9.8,12);hold(-9.8,6);at+=100*MS;assertFalse(g.sample(at,at,20,0,-9.8,true));assertFalse(hold(-9.8,7));}
 @Test public void staleSamplesCannotFormBaseline(){for(int n=0;n<20;n++){at+=100*MS;assertFalse(g.sample(at,at+251*MS,0,0,9.8,true));}assertFalse(hold(-9.8,15));}
 @Test public void futureSampleResetsBaseline(){hold(9.8,12);assertFalse(g.sample(at+100*MS,at,0,0,-9.8,true));assertFalse(hold(-9.8,15));}
 @Test public void sampleGapRequiresNewUpBaseline(){hold(9.8,12);at+=400*MS;assertFalse(hold(-9.8,15));}
 @Test public void preAdmissionSamplesCannotArm(){assertFalse(g.sample(999*MS,1000*MS,0,0,9.8,true));assertFalse(hold(-9.8,15));}
 @Test public void unreliableMotionRequiresFreshReliableSequence(){hold(9.8,12);at+=100*MS;assertFalse(g.sample(at,at,0,0,-9.8,false));assertFalse(hold(-9.8,15));hold(9.8,12);assertTrue(hold(-9.8,14));}
 @Test public void nonFiniteInputDoesNotPause(){hold(9.8,12);at+=100*MS;assertFalse(g.sample(at,at,Double.NaN,0,-9.8,true));assertFalse(hold(-9.8,15));}
 @Test public void invertedBaselineSupportsResumeOnlyAfterDown(){g=new TrackingFlipGesture(at,-1);assertFalse(hold(9.8,15));hold(-9.8,12);assertTrue(hold(9.8,14));}
 @Test public void sparseEndpointsCannotSatisfyStableDuration(){assertFalse(sample(9.8));at+=1000*MS;assertFalse(sample(9.8));assertFalse(hold(-9.8,14));}
 @Test public void oldBaselineExpiresDuringLongTurn(){hold(9.8,12);for(int n=0;n<110;n++){at+=100*MS;assertFalse(g.sample(at,at,5,5,5,true));}assertFalse(hold(-9.8,15));}
}
