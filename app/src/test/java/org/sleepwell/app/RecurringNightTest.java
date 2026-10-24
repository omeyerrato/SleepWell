package org.sleepwell.app;
import org.junit.Test;
import java.time.*;
import static org.junit.Assert.*;
public class RecurringNightTest {
    private long at(String text,ZoneId zone){return LocalDateTime.parse(text).atZone(zone).toInstant().toEpochMilli();}
    @Test public void nextUsesCalendarTimeAndNeverBackfills(){ZoneId z=ZoneId.of("Asia/Riyadh");assertEquals(at("2026-10-24T22:30",z),RecurringNight.next(at("2026-10-24T21:00",z),z,1350,"").at);assertEquals(at("2026-10-25T22:30",z),RecurringNight.next(at("2026-10-24T22:30",z),z,1350,"").at);}
    @Test public void dailyScheduleFollowsSpringDaylightSavingNotTwentyFourHours(){ZoneId z=ZoneId.of("America/New_York");long previous=at("2026-03-07T22:30",z);assertEquals(23*3600000L,RecurringNight.next(previous,z,1350,"2026-03-07").at-previous);}
    @Test public void fallTransitionHasTwentyFiveHourNightlyInterval(){ZoneId z=ZoneId.of("America/New_York");long previous=at("2026-10-31T22:30",z);assertEquals(25*3600000L,RecurringNight.next(previous,z,1350,"2026-10-31").at-previous);}
    @Test public void nonexistentLocalTimeMovesThroughGap(){ZoneId z=ZoneId.of("America/New_York");assertEquals(at("2026-03-08T03:30",z),RecurringNight.next(at("2026-03-08T00:00",z),z,150,"").at);}
    @Test public void repeatedClockHourCannotCreateSecondOccurrenceForDate(){ZoneId z=ZoneId.of("America/New_York");RecurringNight.Occurrence next=RecurringNight.next(at("2026-11-01T01:00",z),z,90,"2026-11-01");assertEquals("2026-11-02",next.date);}
    @Test public void clockRollbackCannotRepeatClaimedCalendarDay(){ZoneId z=ZoneId.of("Asia/Riyadh");assertEquals("2026-10-25",RecurringNight.next(at("2026-10-23T22:00",z),z,1350,"2026-10-24").date);}
    @Test public void deliveryHasBoundedGraceAndRejectsEarlyOrInvalidTimes(){assertFalse(RecurringNight.due(999,1000));assertTrue(RecurringNight.due(1000,1000));assertTrue(RecurringNight.due(301000,1000));assertFalse(RecurringNight.due(301001,1000));assertFalse(RecurringNight.due(0,0));}
    @Test public void cancelControlRequiresGlobalPermissionAndUnblockedTrackingChannel(){assertTrue(RecurringNight.notificationsVisible(true,true,null));assertTrue(RecurringNight.notificationsVisible(true,true,2));assertFalse(RecurringNight.notificationsVisible(true,true,0));assertFalse(RecurringNight.notificationsVisible(false,true,2));assertFalse(RecurringNight.notificationsVisible(true,false,2));}
    @Test public void invalidMinuteRejected(){for(int invalid:new int[]{-1,1440,Integer.MAX_VALUE})try{RecurringNight.next(0,ZoneId.of("UTC"),invalid,"");fail();}catch(IllegalArgumentException expected){}}
}
