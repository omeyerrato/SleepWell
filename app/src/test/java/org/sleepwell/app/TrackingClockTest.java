package org.sleepwell.app;

import org.junit.Test;
import static org.junit.Assert.*;

public class TrackingClockTest {
    @Test public void pauseFreezesActiveTimeButNeverExtendsLifetime(){TrackingClock c=new TrackingClock(100);assertEquals(1000,c.active(1100));c.pause(1100);assertEquals(1000,c.active(10000));assertEquals(9900,c.lifetime(10000));c.resume(10000);assertEquals(1500,c.active(10500));}
    @Test public void repeatedCommandsAreIdempotentAcrossSeveralPauses(){TrackingClock c=new TrackingClock(0);c.pause(100);c.pause(900);c.resume(1000);c.resume(2000);c.pause(3000);assertEquals(2100,c.active(9000));assertTrue(c.paused());c.resume(10000);assertEquals(2200,c.active(10100));}
    @Test public void twelveHourLimitUsesTotalMonotonicLifetime(){TrackingClock c=new TrackingClock(1000);c.pause(2000);c.resume(100000);assertEquals(43200000,c.lifetime(43201000));assertTrue(c.active(43201000)<43200000);}
    @Test public void timelineRetainsItsOriginalWallAnchorWhenThePhoneClockChanges(){TrackingClock c=new TrackingClock(1000);long anchor=100000;assertEquals(101000,c.timestamp(anchor,2000));c.pause(2000);c.resume(5000);assertEquals(105000,c.timestamp(anchor,6000));assertEquals(2000,c.active(6000));}
    @Test public void alarmCalendarWindowUsesWallTimeRatherThanTheRecordingAxis(){long deadline=7200000;TrackingClock c=new TrackingClock(0);long axis=c.timestamp(0,1000),wallAfterClockChange=deadline-60000;assertTrue(SleepMath.smartWake(wallAfterClockChange,deadline,20,.15,2,300));assertFalse(SleepMath.smartWake(axis,deadline,20,.15,2,300));}
}
