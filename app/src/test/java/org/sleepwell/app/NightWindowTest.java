package org.sleepwell.app;

import org.junit.Test;
import java.time.*;
import static org.junit.Assert.*;

public class NightWindowTest {
    private long time(String value){return Instant.parse(value).toEpochMilli();}
    private NightWindow at(String now){return NightWindow.at(22*60+30,6*60+30,0,time(now),ZoneId.of("UTC"));}
    @Test public void upcomingPlanHasNoInventedProgress(){NightWindow n=at("2026-10-22T22:00:00Z");assertFalse(n.active);assertEquals(0,n.fraction,0);assertEquals(30,n.remainingMinutes());assertEquals(480,n.durationMinutes());}
    @Test public void startMidpointAndEndUseElapsedClockTime(){NightWindow start=at("2026-10-22T22:30:00Z"),middle=at("2026-10-23T02:30:00Z"),end=at("2026-10-23T06:30:00Z");assertTrue(start.active);assertEquals(0,start.fraction,0);assertEquals(.5,middle.fraction,.00001);assertEquals(240,middle.remainingMinutes());assertTrue(end.active);assertEquals(1,end.fraction,0);assertEquals(0,end.remainingMinutes());}
    @Test public void afterWakeShowsNextPlanRatherThanStaleFullBar(){NightWindow n=at("2026-10-23T06:30:01Z");assertFalse(n.active);assertEquals(0,n.fraction,0);assertEquals(time("2026-10-23T22:30:00Z"),n.start);}
    @Test public void daytimeRestWindowWorks(){NightWindow n=NightWindow.at(480,960,0,time("2026-10-23T12:00:00Z"),ZoneId.of("UTC"));assertTrue(n.active);assertEquals(.5,n.fraction,0);}
    @Test public void weeklyAlarmAnchorsOneNightImmediatelyBeforeItsOccurrence(){NightWindow n=NightWindow.at(1350,390,time("2026-10-25T06:30:00Z"),time("2026-10-22T23:00:00Z"),ZoneId.of("UTC"));assertFalse(n.active);assertEquals(time("2026-10-24T22:30:00Z"),n.start);assertEquals(480,n.durationMinutes());}
    @Test public void springGapUsesSevenActualHoursAndResolvedWake(){ZoneId zone=ZoneId.of("Europe/Berlin");NightWindow n=NightWindow.at(1350,390,0,time("2026-03-29T01:00:00Z"),zone);assertEquals(420,n.durationMinutes());assertEquals(.5,n.fraction,.00001);NightWindow gap=NightWindow.at(1350,150,0,time("2026-03-29T01:00:00Z"),zone);assertEquals(LocalTime.of(3,30),Instant.ofEpochMilli(gap.end).atZone(zone).toLocalTime());}
    @Test public void autumnFoldUsesNineActualHoursWithoutMovingBackward(){ZoneId zone=ZoneId.of("Europe/Berlin");NightWindow first=NightWindow.at(1350,390,0,time("2026-10-25T00:30:00Z"),zone),second=NightWindow.at(1350,390,0,time("2026-10-25T01:30:00Z"),zone);assertEquals(540,first.durationMinutes());assertTrue(second.fraction>first.fraction);assertEquals(60,first.remainingMinutes()-second.remainingMinutes());}
    @Test public void invalidAndExpiredExplicitWindowsAreNeverRenderedAsProgress(){assertFalse(NightWindow.at(390,390,0,time("2026-10-23T04:00:00Z"),ZoneId.of("UTC")).valid);assertFalse(NightWindow.at(1350,390,time("2026-10-22T06:30:00Z"),time("2026-10-23T04:00:00Z"),ZoneId.of("UTC")).valid);}
    @Test public void springGapCannotTurnAnImpossibleShortWindowIntoAnotherDay(){for(int wake:new int[]{180,210}){NightWindow n=NightWindow.at(150,wake,0,time("2026-03-29T00:00:00Z"),ZoneId.of("Europe/Berlin"));assertFalse(n.valid);assertEquals(0,n.fraction,0);}}
    @Test public void initialAlarmRenderAndExistingViewAgreeAtTheExactWakeBoundary(){ZoneId zone=ZoneId.of("UTC");long now=time("2026-10-23T06:30:00Z"),end=NightWindow.nextAlarm(6,30,java.util.Set.of(),now,zone);assertEquals(now,end);assertEquals(1,NightWindow.at(1350,390,end,now,zone).fraction,0);assertEquals(time("2026-10-24T06:30:00Z"),NightWindow.nextAlarm(6,30,java.util.Set.of(),now+1,zone));}
}
