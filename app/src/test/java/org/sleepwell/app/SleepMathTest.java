package org.sleepwell.app;

import org.junit.Test;
import java.time.*;
import java.util.Set;
import static org.junit.Assert.*;

public class SleepMathTest {
    private static long at(String iso){return Instant.parse(iso).toEpochMilli();}
    @Test public void oneShotRollsAcrossMidnight(){assertEquals(at("2026-10-23T03:00:00Z"),SleepMath.nextOccurrence(6,0,Set.of(),at("2026-10-22T23:30:00Z"),ZoneId.of("Asia/Riyadh")));}
    @Test public void exactMinuteSchedulesTomorrow(){assertEquals(at("2026-10-23T04:00:00Z"),SleepMath.nextOccurrence(7,0,Set.of(),at("2026-10-22T04:00:00Z"),ZoneId.of("Asia/Riyadh")));}
    @Test public void repeatSkipsWeekend(){assertEquals(at("2026-10-26T07:00:00Z"),SleepMath.nextOccurrence(7,0,Set.of(1,2,3,4,5),at("2026-10-23T08:00:00Z"),ZoneId.of("UTC")));}
    @Test public void daylightSavingGapMovesToValidTime(){assertEquals(at("2026-03-29T01:30:00Z"),SleepMath.nextOccurrence(2,30,Set.of(),at("2026-03-28T23:00:00Z"),ZoneId.of("Europe/Berlin")));}
    @Test public void bedtimeSpansMidnight(){assertTrue(SleepMath.inBedtime(23*60,22*60,7*60));assertTrue(SleepMath.inBedtime(6*60,22*60,7*60));assertFalse(SleepMath.inBedtime(12*60,22*60,7*60));assertFalse(SleepMath.inBedtime(7*60,22*60,7*60));}
    @Test public void daytimeRestAndDisabledWindow(){assertTrue(SleepMath.inBedtime(14*60,13*60,15*60));assertFalse(SleepMath.inBedtime(14*60,13*60,13*60));}
    @Test public void smartWakeRequiresWindowAndRepeatedActivity(){long end=at("2026-10-22T07:00:00Z");assertTrue(SleepMath.smartWake(end-60000,end,20,.15,2,300));assertFalse(SleepMath.smartWake(end-21*60000,end,20,.15,2,300));assertFalse(SleepMath.smartWake(end,end,20,.15,2,300));assertFalse(SleepMath.smartWake(end-60000,end,20,.15,1,300));assertFalse(SleepMath.smartWake(end-60000,end,20,.15,2,0));assertFalse(SleepMath.smartWake(end-60000,end,0,.15,2,300));assertFalse(SleepMath.smartWake(end-60000,end,20,.02,5,300));}
    @Test public void digitalSilenceIsZero(){assertEquals(0,SleepMath.rms(new short[]{0,0,0},3),0);assertEquals(0,SleepMath.rms(new short[0],0),0);}
    @Test public void digitalAmplitudeIsNormalized(){assertEquals(.5,SleepMath.rms(new short[]{16384,-16384},2),.00001);}
    @Test public void durationScoreDoesNotRewardOvertrackingBeyond100(){assertEquals(100,SleepMath.durationScore(600,480));assertEquals(50,SleepMath.durationScore(240,480));assertEquals(0,SleepMath.durationScore(-5,480));}
    @Test public void exportEscapesQuotesAndNewlines(){assertEquals("\"coffee, \"\"late\"\"\nnight\"",SleepMath.csv("coffee, \"late\"\nnight"));}
}
