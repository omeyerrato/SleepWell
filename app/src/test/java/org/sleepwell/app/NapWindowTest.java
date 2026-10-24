package org.sleepwell.app;

import java.time.*;
import org.junit.Test;
import static org.junit.Assert.*;

public final class NapWindowTest {
 final NapEngineTest.Clock clock=new NapEngineTest.Clock();
 @Test public void scheduledNapUsesOnlyItsActualDurationNotThePriorBedtime(){NapPlan p=NapPlan.create(clock,20,49*3600000L,false);NightWindow w=NightWindow.nap(p,clock,ZoneId.of("UTC"));assertEquals(20,w.durationMinutes());assertEquals(49*60,w.remainingMinutes());assertFalse(w.active);assertEquals(0,w.fraction,0);assertEquals(p.startWall,w.start);assertEquals(p.deadlineWall(),w.end);}
 @Test public void startMidpointAndEndUseTheActualRelativeInterval(){NapPlan p=NapPlan.create(clock,20,0,false);assertEquals(0,NightWindow.nap(p,clock,ZoneId.of("UTC")).fraction,0);clock.advance(600000);NightWindow midpoint=NightWindow.nap(p,clock,ZoneId.of("UTC"));assertTrue(midpoint.active);assertEquals(.5,midpoint.fraction,0);assertEquals(10,midpoint.remainingMinutes());clock.advance(600000);assertEquals(1,NightWindow.nap(p,clock,ZoneId.of("UTC")).fraction,0);}
 @Test public void wallShiftMovesBothDisplayedEndpointsWithoutChangingDurationOrProgress(){NapPlan p=NapPlan.create(clock,20,0,false);clock.advance(600000);NightWindow before=NightWindow.nap(p,clock,ZoneId.of("UTC"));clock.wall+=10800000;NightWindow after=NightWindow.nap(p,clock,ZoneId.of("UTC"));assertEquals(before.start+10800000,after.start);assertEquals(before.end+10800000,after.end);assertEquals(before.durationMinutes(),after.durationMinutes());assertEquals(before.fraction,after.fraction,0);}
 @Test public void foldMayHaveEarlierLocalEndButIsStillExactlyTwentyActualMinutes(){clock.wall=Instant.parse("2026-10-25T00:50:00Z").toEpochMilli();NapPlan p=NapPlan.create(clock,20,0,false);ZoneId zone=ZoneId.of("Europe/Berlin");clock.advance(600000);NightWindow w=NightWindow.nap(p,clock,zone);assertEquals(20,w.durationMinutes());assertEquals(.5,w.fraction,0);assertTrue(Instant.ofEpochMilli(w.end).atZone(zone).toLocalTime().isBefore(Instant.ofEpochMilli(w.start).atZone(zone).toLocalTime()));}
 @Test public void expiryNeverBecomesAnotherNightOrAnotherFutureNap(){NapPlan p=NapPlan.create(clock,20,0,false);long end=p.deadlineWall();clock.advance(1200001);NightWindow w=NightWindow.nap(p,clock,ZoneId.of("UTC"));assertEquals(end,w.end);assertEquals(end,p.projectedDeadline(clock));assertFalse(w.active);assertEquals(1,w.fraction,0);assertEquals(0,w.remainingMinutes());assertEquals(20,w.durationMinutes());}
 @Test public void zoneChangeDoesNotChangeThePhysicalEndpointsOrFraction(){NapPlan p=NapPlan.create(clock,20,0,false);clock.advance(300000);NightWindow utc=NightWindow.nap(p,clock,ZoneId.of("UTC")),riyadh=NightWindow.nap(p,clock,ZoneId.of("Asia/Riyadh"));assertEquals(utc.start,riyadh.start);assertEquals(utc.end,riyadh.end);assertEquals(utc.fraction,riyadh.fraction,0);assertNotEquals(Instant.ofEpochMilli(utc.end).atZone(utc.zone).toLocalTime(),Instant.ofEpochMilli(riyadh.end).atZone(riyadh.zone).toLocalTime());}
}
