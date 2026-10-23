package org.sleepwell.app;

import org.junit.Test;
import static org.junit.Assert.*;
import java.time.*;

public class TravelPlanTest {
    private long at(String instant){return Instant.parse(instant).toEpochMilli();}
    private TravelPlan trip(String zone,LocalDate date,int minute){TravelPlan p=new TravelPlan("Asia/Riyadh",date);p.destination=zone;p.minute=minute;p.reminders=true;return p;}
    @Test public void londonOffsetChangesAcrossDepartureWeekend(){TravelPlan p=trip("Europe/London",LocalDate.of(2026,10,27),540);assertEquals(-7200,p.differenceSeconds(at("2026-10-23T12:00:00Z")));assertEquals(-10800,p.differenceSeconds(p.departureAnchor()));}
    @Test public void fractionalHourDifferencesRemainPrecise(){TravelPlan p=trip("Asia/Kathmandu",LocalDate.of(2026,10,27),540);assertEquals(9900,p.differenceSeconds(at("2026-10-23T12:00:00Z")));assertEquals("2 h 45 min ahead",TravelPlan.difference(9900));assertEquals("UTC+05:45",TravelPlan.offset(p.destination,at("2026-10-23T12:00:00Z")));}
    @Test public void nextReminderUsesDestinationCivilTime(){TravelPlan p=trip("Europe/London",LocalDate.of(2026,10,27),555);assertEquals(at("2026-10-27T09:15:00Z"),p.next(at("2026-10-23T12:00:00Z"),0));}
    @Test public void springGapMovesToAValidInstantWithoutLosingFollowingDates(){TravelPlan p=trip("Europe/London",LocalDate.of(2026,3,29),90);p.days=2;long first=p.next(at("2026-03-28T12:00:00Z"),0);assertEquals(at("2026-03-29T01:30:00Z"),first);assertEquals(at("2026-03-30T00:30:00Z"),p.next(first,first));}
    @Test public void repeatedFallHourDeliversOnlyTheEarlierOccurrence(){TravelPlan p=trip("Europe/London",LocalDate.of(2026,10,25),90);p.days=2;long first=p.next(at("2026-10-24T12:00:00Z"),0);assertEquals(at("2026-10-25T00:30:00Z"),first);assertEquals(at("2026-10-26T01:30:00Z"),p.next(at("2026-10-25T00:45:00Z"),first));}
    @Test public void periodIsFiniteAndNeverBackfillsMissedDays(){TravelPlan p=trip("UTC",LocalDate.of(2026,10,23),540);p.days=3;assertEquals(at("2026-10-25T09:00:00Z"),p.next(at("2026-10-24T20:00:00Z"),0));assertEquals(0,p.next(at("2026-10-25T10:00:00Z"),0));}
    @Test public void wallClockRollbackDoesNotRepeatConsumedCues(){TravelPlan p=trip("UTC",LocalDate.of(2026,10,23),540);assertEquals(at("2026-10-25T09:00:00Z"),p.next(at("2026-10-22T00:00:00Z"),at("2026-10-24T09:00:00Z")));}
    @Test public void disabledPlanHasNoScheduledBehavior(){TravelPlan p=trip("UTC",LocalDate.of(2026,10,23),540);p.reminders=false;assertEquals(0,p.next(0,0));}
    @Test public void zoneSearchUsesRealIanaIdentifiers(){assertTrue(TravelPlan.zones("new york").contains("America/New_York"));assertTrue(TravelPlan.zones("EUROPE/LONDON").contains("Europe/London"));assertTrue(TravelPlan.zones("not-a-zone").isEmpty());}
    @Test public void invalidZoneTimeAndPeriodAreRejected(){TravelPlan p=trip("UTC",LocalDate.of(2026,10,23),540);p.destination="UTC+03:00";invalid(p);p.destination="UTC";p.days=15;invalid(p);p.days=3;p.minute=1440;invalid(p);}
    private void invalid(TravelPlan p){try{p.validate();fail();}catch(IllegalArgumentException expected){}}
    @Test public void copiedDraftDoesNotMutateTheSavedObject(){TravelPlan p=trip("UTC",LocalDate.of(2026,10,23),540),copy=p.copy();copy.destination="Asia/Tokyo";copy.days=7;assertEquals("UTC",p.destination);assertEquals(3,p.days);}
    @Test public void calendarOffsetIsNotReducedToAShortestFlightDirection(){TravelPlan p=trip("Pacific/Auckland",LocalDate.of(2026,10,23),540);p.home="America/Los_Angeles";assertEquals(72000,p.differenceSeconds(at("2026-10-23T12:00:00Z")));assertEquals("20 h ahead",TravelPlan.difference(72000));}
}
