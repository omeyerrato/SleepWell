package org.sleepwell.app;

import org.junit.Test;
import java.time.*;
import java.util.*;
import static org.junit.Assert.*;

public final class CalendarPolicyTest {
    final ZoneId zone=ZoneId.of("Asia/Riyadh");
    long at(String value){return ZonedDateTime.parse(value).toInstant().toEpochMilli();}
    long now=at("2026-10-24T22:00:00+03:00"),base=at("2026-10-25T07:00:00+03:00");
    CalendarPolicy.Rules rules=new CalendarPolicy.Rules(true,Set.of(1L),Set.of(2L),30,180,"one");
    CalendarPolicy.Snapshot snapshot(List<CalendarPolicy.Event> events){return new CalendarPolicy.Snapshot(at("2026-10-24T00:00:00+03:00"),at("2026-11-24T00:00:00+03:00"),now,zone,"one",events);}
    CalendarPolicy.Plan plan(CalendarPolicy.Snapshot snapshot){return CalendarPolicy.plan(rules,snapshot,now,base,zone,value->Instant.ofEpochMilli(value).atZone(zone).plusDays(1).toInstant().toEpochMilli());}
    CalendarPolicy.Event meeting(String start){long begin=at(start);return new CalendarPolicy.Event(2,begin,begin+3600000,false);}
    @Test public void offAndUnavailableNeverRemoveOrdinaryAlarm(){assertEquals(base,CalendarPolicy.plan(CalendarPolicy.Rules.off(),null,now,base,zone,x->x).deadline);assertEquals(base,plan(null).deadline);assertFalse(plan(null).reason.isEmpty());}
    @Test public void earlyMeetingUsesPreparationAndRetainsConsumedBase(){CalendarPolicy.Plan p=plan(snapshot(List.of(meeting("2026-10-25T06:30:00+03:00"))));assertEquals(at("2026-10-25T06:00:00+03:00"),p.deadline);assertEquals(base,p.base);assertEquals(base,p.originalBase);}
    @Test public void laterMeetingNeverPostponesAlarm(){assertEquals(base,plan(snapshot(List.of(meeting("2026-10-25T10:00:00+03:00")))).deadline);}
    @Test public void advanceBoundAndPreviousLocalDayAreExcluded(){assertEquals(base,plan(snapshot(List.of(meeting("2026-10-25T03:00:00+03:00")))).deadline);}
    @Test public void allDayUsesCalendarUtcDateRatherThanPhoneOffset(){CalendarPolicy.Event e=new CalendarPolicy.Event(1,at("2026-10-25T00:00:00Z"),at("2026-10-26T00:00:00Z"),true);CalendarPolicy.Plan p=plan(snapshot(List.of(e)));assertEquals(at("2026-10-26T07:00:00+03:00"),p.deadline);assertEquals(1,p.skipped);}
    @Test public void allDayEndIsExclusive(){CalendarPolicy.Event e=new CalendarPolicy.Event(1,at("2026-10-24T00:00:00Z"),at("2026-10-25T00:00:00Z"),true);assertEquals(base,plan(snapshot(List.of(e))).deadline);}
    @Test public void otherCalendarsAndTimedHolidayEventsCannotSkip(){CalendarPolicy.Event other=new CalendarPolicy.Event(9,now,base+3600000,true),timed=new CalendarPolicy.Event(1,now,base+3600000,false);assertEquals(base,plan(snapshot(List.of(other,timed))).deadline);}
    @Test public void staleRevisedAndChangedZoneSnapshotsKeepOrdinaryAlarm(){CalendarPolicy.Snapshot stale=new CalendarPolicy.Snapshot(now-86400000,base+86400000,now-1800001,zone,"one",List.of());assertEquals(base,plan(stale).deadline);CalendarPolicy.Snapshot changed=new CalendarPolicy.Snapshot(now-86400000,base+86400000,now,ZoneOffset.UTC,"one",List.of());assertEquals(base,plan(changed).deadline);CalendarPolicy.Snapshot revision=new CalendarPolicy.Snapshot(now-86400000,base+86400000,now,zone,"old",List.of());assertEquals(base,plan(revision).deadline);}
    @Test public void insufficientCoverageDoesNotSilentlySkipBeyondEvidence(){CalendarPolicy.Snapshot shortRange=new CalendarPolicy.Snapshot(now,base,now,zone,"one",List.of());assertEquals(base,plan(shortRange).deadline);}
    @Test public void firstMeetingWinsIndependentlyOfProviderOrder(){CalendarPolicy.Event early=meeting("2026-10-25T06:00:00+03:00"),later=meeting("2026-10-25T06:45:00+03:00");assertEquals(at("2026-10-25T05:30:00+03:00"),plan(snapshot(List.of(later,early))).deadline);assertEquals(plan(snapshot(List.of(later,early))).deadline,plan(snapshot(List.of(early,later))).deadline);}
    @Test public void pastShiftsCannotBackfillAnAlarm(){CalendarPolicy.Event old=meeting("2026-10-24T21:30:00+03:00");assertEquals(base,plan(snapshot(List.of(old))).deadline);}
    @Test public void badRulesAndEvidenceAreRejected(){assertThrows(IllegalArgumentException.class,()->new CalendarPolicy.Rules(true,Set.of(-1L),Set.of(),30,180,"a"));assertThrows(IllegalArgumentException.class,()->new CalendarPolicy.Event(1,2,1,false));assertThrows(IllegalArgumentException.class,()->new CalendarPolicy.Rules(true,Set.of(),Set.of(),0,180,"a"));}
}
