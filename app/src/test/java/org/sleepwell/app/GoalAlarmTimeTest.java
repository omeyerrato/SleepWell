package org.sleepwell.app;
import java.time.*;import org.junit.Test;import static org.junit.Assert.*;
public final class GoalAlarmTimeTest {
 @Test public void overnightLocalClockGoalWrapsExactlyOnce(){GoalAlarmTime t=new GoalAlarmTime(22*60+30,480);assertEquals(6*60+30,t.minute);assertEquals(1,t.dayOffset);}
 @Test public void sameDayAndMinutePrecisionDoNotRoundToHour(){GoalAlarmTime t=new GoalAlarmTime(8*60+5,20);assertEquals(8*60+25,t.minute);assertEquals(0,t.dayOffset);assertEquals(9*60+29,new GoalAlarmTime(23*60+59,570).minute);}
 @Test public void fullSupportedGoalBoundsArePreserved(){assertEquals(0,new GoalAlarmTime(630,810).minute);assertEquals(20,new GoalAlarmTime(0,20).minute);}
 @Test public void corruptValuesRejected(){for(int[] pair:new int[][]{{-1,480},{1440,480},{1350,19},{1350,811}})try{new GoalAlarmTime(pair[0],pair[1]);fail();}catch(IllegalArgumentException expected){}}
 @Test public void existingWakeDateRemainsChosenDateAcrossMidnight(){GoalAlarmTime.Dated d=new GoalAlarmTime(1350,480).on(LocalDate.of(2026,10,26),ZoneId.of("Asia/Riyadh"));assertEquals(LocalDate.of(2026,10,25),d.bedDate);assertEquals(LocalDate.of(2026,10,26),d.wakeDate);assertEquals(480,d.elapsed);assertFalse(d.gap);assertFalse(d.overlap);}
 @Test public void springTransitionReportsSevenElapsedHoursForEightClockHours(){GoalAlarmTime.Dated d=new GoalAlarmTime(1350,480).on(LocalDate.of(2026,3,8),ZoneId.of("America/New_York"));assertEquals(420,d.elapsed);assertEquals(6,d.wake.getHour());assertEquals(30,d.wake.getMinute());}
 @Test public void autumnTransitionReportsNineElapsedHoursForEightClockHours(){GoalAlarmTime.Dated d=new GoalAlarmTime(1350,480).on(LocalDate.of(2026,11,1),ZoneId.of("America/New_York"));assertEquals(540,d.elapsed);}
 @Test public void skippedWakeClockMovesForwardUsingExistingAlarmResolution(){GoalAlarmTime t=new GoalAlarmTime(1350,240);GoalAlarmTime.Dated d=t.on(LocalDate.of(2026,3,8),ZoneId.of("America/New_York"));assertEquals(150,t.minute);assertTrue(d.gap);assertEquals(3,d.wake.getHour());assertEquals(30,d.wake.getMinute());assertEquals(d.wake.toInstant().toEpochMilli(),AlarmDate.at(d.wakeDate,2,30,ZoneId.of("America/New_York")));}
 @Test public void repeatedWakeClockUsesEarlierOffset(){GoalAlarmTime.Dated d=new GoalAlarmTime(1350,180).on(LocalDate.of(2026,11,1),ZoneId.of("America/New_York"));assertTrue(d.overlap);assertEquals(ZoneOffset.ofHours(-4),d.wake.getOffset());assertEquals(180,d.elapsed);}
 @Test public void bedGapCannotClaimPositiveElapsedGoal(){GoalAlarmTime.Dated d=new GoalAlarmTime(150,30).on(LocalDate.of(2026,3,8),ZoneId.of("America/New_York"));assertEquals(-30,d.elapsed);}
 @Test public void wallClockProposalIsZoneIndependentButDatedElapsedIsNot(){GoalAlarmTime t=new GoalAlarmTime(1350,480);LocalDate date=LocalDate.of(2026,3,8);assertEquals(480,t.on(date,ZoneId.of("Asia/Riyadh")).elapsed);assertEquals(420,t.on(date,ZoneId.of("America/New_York")).elapsed);assertEquals(390,t.minute);}
}
