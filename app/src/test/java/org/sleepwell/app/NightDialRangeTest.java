package org.sleepwell.app;

import org.junit.Test;
import static org.junit.Assert.*;

public final class NightDialRangeTest {
    @Test public void bedtimeMovesIndependentlyAcrossMidnight(){NightDialRange r=new NightDialRange(1350,420);r.begin(false,1350);assertTrue(r.move(10));assertEquals(10,r.bedtime());assertEquals(420,r.wake());assertEquals(410,r.duration());}
    @Test public void wakeMovesIndependentlyAcrossMidnight(){NightDialRange r=new NightDialRange(1080,1430);r.begin(true,1430);r.move(20);assertEquals(1080,r.bedtime());assertEquals(20,r.wake());assertEquals(380,r.duration());}
    @Test public void crossingOtherHandleClampsWithoutSwapping(){NightDialRange r=new NightDialRange(1350,420);r.begin(true,420);r.move(1340);assertEquals(1350,r.bedtime());assertEquals(1370,r.wake());assertEquals(20,r.duration());r.move(1390);assertEquals(1420,r.wake());}
    @Test public void maximumDoesNotWrapOrJump(){NightDialRange r=new NightDialRange(1350,420);r.begin(true,420);r.move(1000);assertEquals(810,r.duration());assertEquals(720,r.wake());r.move(995);assertEquals(805,r.duration());}
    @Test public void fiveMinuteSnapsDoNotAccumulateRoundingError(){NightDialRange r=new NightDialRange(1350,420);r.begin(true,420);assertFalse(r.move(422));assertTrue(r.move(423));assertEquals(425,r.wake());assertFalse(r.move(424));assertTrue(r.move(428));assertEquals(430,r.wake());}
    @Test public void clockGeometry(){assertEquals(0,NightDialRange.minute(0,-1),.001);assertEquals(360,NightDialRange.minute(1,0),.001);assertEquals(720,NightDialRange.minute(0,1),.001);assertEquals(1080,NightDialRange.minute(-1,0),.001);}
    @Test public void invalidWindowIsNotSilentlyCorrected(){for(int wake:new int[]{1350,1360,900})try{new NightDialRange(1350,wake);fail();}catch(IllegalArgumentException expected){}}
}
