package org.sleepwell.app;

import org.junit.Test;import static org.junit.Assert.*;

public final class TiltMotionTest {
    static long calibrate(TiltMotion motion,long start){for(int n=0;n<16;n++){long time=start+n*50_000_000L;double[] still=motion.sample(time,time,0,0,9.8,0);if(still!=null)assertArrayEquals(new double[]{0,0},still,0);}assertTrue(motion.calibrated());return start+16*50_000_000L;}
    @Test public void stillCalibrationMustPrecedeMovement(){TiltMotion motion=new TiltMotion(1);assertNull(motion.sample(1,1,3,0,9.8,0));assertFalse(motion.calibrated());long next=calibrate(new TiltMotion(1),1);assertTrue(next>0);}
    @Test public void staleFutureDuplicateNonfiniteAndViolentSamplesAreRejected(){TiltMotion motion=new TiltMotion(10);assertNull(motion.sample(9,10,0,0,9.8,0));assertNull(motion.sample(20,19,0,0,9.8,0));assertNull(motion.sample(10,300_000_011L,0,0,9.8,0));assertNull(motion.sample(11,11,Double.NaN,0,9.8,0));assertNull(motion.sample(12,12,0,0,30,0));assertEquals(0,motion.last());motion.sample(13,13,0,0,9.8,0);assertNull(motion.sample(13,13,0,0,9.8,0));}
    @Test public void calibratedTiltHasDeadzoneBoundedVelocityAndNoLargeGapCatchup(){TiltMotion motion=new TiltMotion(1);long time=calibrate(motion,1);double[] rest=motion.sample(time,time,0,0,9.8,0);assertEquals(0,rest[0],0);double sum=0;for(int n=1;n<30;n++){long now=time+n*50_000_000L;double[] d=motion.sample(now,now,-3,0,9.8,0);assertTrue(d[0]>=0&&d[0]<=.12);assertEquals(0,d[1],0);sum+=d[0];}assertTrue(sum>1);long gap=time+3_000_000_000L;assertNull(motion.sample(gap,gap,-3,0,9.8,0));}
    @Test public void displayRotationMapsBothAxesAndResetRequiresFreshCalibration(){assertArrayEquals(new double[]{2,3},TiltMotion.axes(2,3,0),0);assertArrayEquals(new double[]{-3,2},TiltMotion.axes(2,3,1),0);assertArrayEquals(new double[]{-2,-3},TiltMotion.axes(2,3,2),0);assertArrayEquals(new double[]{3,-2},TiltMotion.axes(2,3,3),0);TiltMotion motion=new TiltMotion(1);calibrate(motion,1);motion.reset(2_000_000_000L);assertFalse(motion.calibrated());assertNull(motion.sample(1,2_000_000_000L,-3,0,9.8,0));assertFalse(motion.calibrated());}
    @Test public void movingCalibrationNeverBecomesReady(){TiltMotion motion=new TiltMotion(1);for(int n=0;n<100;n++){long now=1+n*50_000_000L;motion.sample(now,now,n%2==0?2:-2,0,9.8,0);}assertFalse(motion.calibrated());}
}
