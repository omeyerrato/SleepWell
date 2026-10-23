package org.sleepwell.app;

import org.junit.Test;
import java.time.*;
import java.util.*;
import static org.junit.Assert.*;

/** Pure plan/bank decisions. Every observation is an explicitly constructed in-memory fixture. */
public class GoalModelTest {
    private long nextId;
    private long time(String iso){return Instant.parse(iso).toEpochMilli();}
    private GoalCommitment goal(String metric,int target,String start,int days,String zone,int cutoff){
        return new GoalCommitment(UUID.randomUUID().toString(),metric,target,LocalDate.parse(start),days,zone,cutoff,"",null);
    }
    private RecordEntry record(String start,String end,String zone,boolean nap){
        RecordDraft draft=new RecordDraft(time(start),time(end));if(nap)draft.addTag("Nap");
        return new RecordEntry(++nextId,draft.start,draft.end,null,"manual","local",zone,0,0,draft,true,1,List.of());
    }
    private GoalProgress progress(GoalCommitment goal,String now,RecordEntry... records){return GoalProgress.calculate(goal,List.of(records),Instant.parse(now));}
    private GoalProgress.Day day(GoalProgress progress,String date){return progress.days.stream().filter(d->d.date.equals(LocalDate.parse(date))).findFirst().orElseThrow(()->new AssertionError("Missing day "+date));}

    @Test public void legacyMismatchIsPreservedUntilAnExplicitLinkedAdjustment(){
        SleepPlan plan=new SleepPlan();assertEquals(480,plan.goal);assertEquals(1350,plan.bedtime);assertEquals(420,plan.wake);
        SleepPlan copy=plan.copy();copy.validate();assertEquals(plan.bedtime,copy.bedtime);assertEquals(plan.wake,copy.wake);
        copy.duration(450);assertEquals(420,copy.wake);assertEquals(1410,copy.bedtime);
        copy.bedtime(1430);assertEquals(440,copy.wake);assertEquals(450,copy.goal);
        copy.wake(15);assertEquals(1005,copy.bedtime);assertEquals(450,copy.goal);
        assertEquals("Editing a draft must not mutate the original",1350,plan.bedtime);
    }

    @Test public void durationGoalChangesTheActualThresholdAndRejectsInvalidSettings(){
        SleepPlan plan=new SleepPlan();plan.duration(450);
        assertFalse(plan.goalMet(450*60000L-1));assertTrue(plan.goalMet(450*60000L));
        for(int invalid:new int[]{0,19,815}){SleepPlan draft=plan.copy();draft.goal=invalid;try{draft.validate();fail("Invalid duration "+invalid);}catch(IllegalArgumentException expected){}}
    }

    @Test public void validLegacyMinutePrecisionSurvivesUnrelatedDraftChanges(){
        SleepPlan legacy=new SleepPlan();legacy.goal=487;legacy.windDown=45;legacy.validate();
        assertEquals(487,legacy.goal);assertEquals(1350,legacy.bedtime);assertEquals(420,legacy.wake);
    }

    @Test public void guardStartsAtWindDownAndEndsExactlyAtPlannedWakeAcrossMidnight(){
        SleepPlan plan=new SleepPlan();plan.bedtime=1350;plan.wake=390;plan.windDown=30;
        assertFalse(plan.guardActive(1319));assertTrue(plan.guardActive(1320));assertTrue(plan.guardActive(1439));
        assertTrue(plan.guardActive(0));assertTrue(plan.guardActive(389));assertFalse(plan.guardActive(390));
        plan.windDown=0;assertFalse(plan.guardActive(1349));assertTrue(plan.guardActive(1350));
        plan.bedtime=480;plan.wake=960;plan.windDown=30;
        assertFalse(plan.guardActive(449));assertTrue(plan.guardActive(450));assertTrue(plan.guardActive(959));assertFalse(plan.guardActive(960));
    }

    @Test public void reminderAdvanceWrapsMidnightAndSkipsAlreadyPassedOccurrences(){
        SleepPlan plan=new SleepPlan();plan.bedtime=15;plan.reminderAdvance=30;
        assertEquals(time("2026-10-22T23:45:00Z"),plan.nextReminder(time("2026-10-22T23:40:00Z"),ZoneId.of("UTC")));
        assertEquals(time("2026-10-23T23:45:00Z"),plan.nextReminder(time("2026-10-22T23:50:00Z"),ZoneId.of("UTC")));
    }

    @Test public void springReminderIsThirtyElapsedMinutesBeforeTheResolvedBedtime(){
        SleepPlan plan=new SleepPlan();plan.bedtime=180;plan.reminderAdvance=30;
        assertEquals(time("2026-03-29T00:30:00Z"),plan.nextReminder(time("2026-03-28T23:00:00Z"),ZoneId.of("Europe/Berlin")));
    }

    @Test public void autumnReminderUsesTheCorrectRepeatedClockOffset(){
        SleepPlan plan=new SleepPlan();plan.bedtime=180;plan.reminderAdvance=30;
        assertEquals(time("2026-10-25T01:30:00Z"),plan.nextReminder(time("2026-10-24T22:00:00Z"),ZoneId.of("Europe/Berlin")));
    }

    @Test public void noonCutoffDoesNotCloseThePreviousLabelUntilNoon(){
        GoalCommitment goal=goal("duration",480,"2026-10-21",14,"UTC",720);
        RecordEntry first=record("2026-10-21T23:00:00Z","2026-10-22T07:00:00Z","UTC",false),second=record("2026-10-22T23:00:00Z","2026-10-23T07:00:00Z","UTC",false);
        GoalProgress before=progress(goal,"2026-10-23T08:00:00Z",first,second);
        assertEquals(LocalDate.parse("2026-10-21"),before.closedThrough);assertEquals(1,before.days.size());assertEquals(1,before.observed);
        GoalProgress after=progress(goal,"2026-10-23T12:00:00Z",first,second);
        assertEquals(LocalDate.parse("2026-10-22"),after.closedThrough);assertEquals(2,after.observed);assertEquals(0,after.balance);
    }

    @Test public void repeatedHourCannotReopenAnAlreadyClosedSleepDay(){
        GoalCommitment goal=goal("duration",480,"2026-10-24",14,"Europe/Berlin",150);
        GoalProgress first=progress(goal,"2026-10-25T00:40:00Z"),second=progress(goal,"2026-10-25T01:20:00Z");
        assertEquals(LocalDate.parse("2026-10-24"),first.closedThrough);assertEquals(first.closedThrough,second.closedThrough);
        assertEquals(1,first.days.size());assertEquals(1,second.days.size());
        assertEquals(LocalDate.parse("2026-10-25"),SleepDay.label(time("2026-10-25T01:20:00Z"),ZoneId.of("Europe/Berlin"),150));
    }

    @Test public void missingCutoffHourUsesOneForwardResolvedBoundary(){
        GoalCommitment goal=goal("duration",480,"2026-03-27",14,"Europe/Berlin",150);
        assertEquals(LocalDate.parse("2026-03-27"),progress(goal,"2026-03-29T01:20:00Z").closedThrough);
        assertEquals(LocalDate.parse("2026-03-28"),progress(goal,"2026-03-29T01:40:00Z").closedThrough);
    }

    @Test public void frozenZoneControlsDayAssignmentAndBedtimeAfterTravel(){
        RecordEntry travel=record("2026-10-22T16:30:00Z","2026-10-23T00:30:00Z","Asia/Tokyo",false);
        GoalCommitment duration=goal("duration",480,"2026-10-22",14,"Pacific/Honolulu",0);
        GoalProgress d=progress(duration,"2026-10-24T12:00:00Z",travel);
        assertEquals(Long.valueOf(8*3600000L),day(d,"2026-10-22").duration);assertNull(day(d,"2026-10-23").duration);
        GoalCommitment bedtime=goal("bedtime",390,"2026-10-22",14,"Pacific/Honolulu",0);
        assertEquals(0,progress(bedtime,"2026-10-24T12:00:00Z",travel).balance);
        assertEquals("The captured record zone must remain immutable","Asia/Tokyo",travel.zone);
    }

    @Test public void crossDayOverlapIsNotCreditedAgainInsideACommitment(){
        RecordEntry earlier=record("2026-10-20T22:00:00Z","2026-10-21T07:00:00Z","UTC",false),later=record("2026-10-21T06:00:00Z","2026-10-22T07:00:00Z","UTC",false);
        GoalCommitment goal=goal("duration",480,"2026-10-22",14,"UTC",0);
        GoalProgress p=progress(goal,"2026-10-23T12:00:00Z",earlier,later);
        assertEquals(Long.valueOf(24*3600000L),day(p,"2026-10-22").duration);assertEquals(16*3600000L,p.balance);
    }

    @Test public void missingAndCurrentDaysNeverBecomeInventedDeficits(){
        GoalCommitment goal=goal("duration",480,"2026-10-20",14,"UTC",0);
        RecordEntry first=record("2026-10-19T22:00:00Z","2026-10-20T06:00:00Z","UTC",false),third=record("2026-10-21T21:00:00Z","2026-10-22T06:00:00Z","UTC",false),current=record("2026-10-22T20:00:00Z","2026-10-23T06:00:00Z","UTC",false);
        GoalProgress p=progress(goal,"2026-10-23T12:00:00Z",first,third,current);
        assertEquals(3,p.days.size());assertEquals(2,p.observed);assertEquals(1,p.unknown);assertNull(day(p,"2026-10-21").balance);
        assertEquals(3600000,p.balance);assertFalse(p.complete);assertFalse(p.ended);
    }

    @Test public void positiveAndNegativeObservedDurationBalancesRetainTheirSign(){
        GoalCommitment goal=goal("duration",480,"2026-10-20",14,"UTC",0);
        RecordEntry shortNight=record("2026-10-19T23:00:00Z","2026-10-20T06:00:00Z","UTC",false),longNight=record("2026-10-20T21:00:00Z","2026-10-21T06:00:00Z","UTC",false);
        GoalProgress p=progress(goal,"2026-10-22T12:00:00Z",shortNight,longNight);
        assertEquals(Long.valueOf(-3600000),day(p,"2026-10-20").balance);assertEquals(Long.valueOf(3600000),day(p,"2026-10-21").balance);assertEquals(0,p.balance);
        assertEquals("−1 h",GoalProgress.signed(-3600000));assertEquals("+20 min",GoalProgress.signed(1200000));
    }

    @Test public void napsCountTowardDurationButCannotStandInForBedtime(){
        RecordEntry night=record("2026-10-20T22:00:00Z","2026-10-21T06:00:00Z","UTC",false),nap=record("2026-10-21T14:00:00Z","2026-10-21T14:20:00Z","UTC",true),longNap=record("2026-10-22T12:00:00Z","2026-10-22T16:00:00Z","UTC",true);
        GoalProgress duration=progress(goal("duration",480,"2026-10-21",14,"UTC",0),"2026-10-23T12:00:00Z",night,nap,longNap);
        assertEquals(Long.valueOf(1200000),day(duration,"2026-10-21").balance);
        GoalProgress bedtime=progress(goal("bedtime",1320,"2026-10-21",14,"UTC",0),"2026-10-23T12:00:00Z",night,nap,longNap);
        assertEquals(Long.valueOf(0),day(bedtime,"2026-10-21").balance);assertNull(day(bedtime,"2026-10-22").balance);assertEquals(1,bedtime.unknown);
    }

    @Test public void bedtimeBalanceHandlesBothMidnightAndNoonWithoutADayOfFalseCredit(){
        RecordEntry beforeMidnight=record("2026-10-20T23:50:00Z","2026-10-21T07:50:00Z","UTC",false);
        assertEquals(20*60000L,progress(goal("bedtime",10,"2026-10-21",14,"UTC",0),"2026-10-22T12:00:00Z",beforeMidnight).balance);
        RecordEntry afterNoon=record("2026-10-21T12:10:00Z","2026-10-21T16:10:00Z","UTC",false);
        assertEquals(-20*60000L,progress(goal("bedtime",710,"2026-10-21",14,"UTC",0),"2026-10-22T12:00:00Z",afterNoon).balance);
    }

    @Test public void laterPlanChangesCannotRewriteAFrozenCommitmentTarget(){
        SleepPlan plan=new SleepPlan();GoalCommitment frozen=goal("duration",plan.goal,"2026-10-21",14,"UTC",0);
        plan.duration(600);RecordEntry night=record("2026-10-20T22:00:00Z","2026-10-21T06:00:00Z","UTC",false);
        assertEquals(480,frozen.target);assertEquals(0,progress(frozen,"2026-10-22T12:00:00Z",night).balance);
        assertEquals(-2*3600000L,progress(goal("duration",plan.goal,"2026-10-21",14,"UTC",0),"2026-10-22T12:00:00Z",night).balance);
    }

    @Test public void recordEditsRecomputeTheBankWithoutChangingOriginalData(){
        RecordEntry original=record("2026-10-20T22:00:00Z","2026-10-21T06:00:00Z","UTC",false);
        GoalCommitment goal=goal("duration",480,"2026-10-21",14,"UTC",0);
        RecordDraft edit=original.edit.copy();edit.awake.add(new RecordIntervals.Span(time("2026-10-21T01:00:00Z"),time("2026-10-21T02:00:00Z")));
        assertEquals(0,progress(goal,"2026-10-22T12:00:00Z",original).balance);
        assertEquals(-3600000,progress(goal,"2026-10-22T12:00:00Z",original.draft(edit)).balance);
        assertTrue(original.edit.awake.isEmpty());assertEquals(8*3600000L,original.duration());
    }

    @Test public void completeRequiresEveryCommittedDayToBeClosedAndObserved(){
        GoalCommitment goal=goal("duration",480,"2028-02-20",14,"UTC",0);List<RecordEntry> records=new ArrayList<>();
        assertEquals(LocalDate.parse("2028-03-04"),goal.end());
        for(int n=0;n<14;n++){LocalDate date=goal.start.plusDays(n);records.add(record(date.minusDays(1)+"T22:00:00Z",date+"T06:00:00Z","UTC",false));}
        GoalProgress full=GoalProgress.calculate(goal,records,Instant.parse("2028-03-05T00:00:00Z"));assertTrue(full.ended);assertTrue(full.complete);assertEquals(14,full.observed);assertEquals(0,full.balance);
        records.remove(5);GoalProgress missing=GoalProgress.calculate(goal,records,Instant.parse("2028-03-05T00:00:00Z"));assertTrue(missing.ended);assertFalse(missing.complete);assertEquals(1,missing.unknown);
    }

    @Test public void thirtyDayCommitmentUsesCalendarDaysAcrossDST(){
        GoalCommitment goal=goal("duration",480,"2026-03-20",30,"Europe/Berlin",0);
        assertEquals(LocalDate.parse("2026-04-18"),goal.end());
        GoalProgress p=progress(goal,"2026-04-19T00:30:00Z");assertEquals(30,p.days.size());assertEquals(30,p.unknown);assertTrue(p.ended);assertFalse(p.complete);
    }

    @Test public void stoppedCommitmentCountsOnlyItsInclusiveFinalDay(){
        GoalCommitment original=goal("duration",480,"2026-05-01",14,"UTC",0),stopped=original.stop(LocalDate.parse("2026-05-03"));List<RecordEntry> records=new ArrayList<>();
        for(int n=1;n<=5;n++){LocalDate date=LocalDate.of(2026,5,n);records.add(record(date.minusDays(1)+"T22:00:00Z",date+"T06:00:00Z","UTC",false));}
        GoalProgress p=GoalProgress.calculate(stopped,records,Instant.parse("2026-05-06T12:00:00Z"));assertEquals(3,p.days.size());assertEquals(3,p.observed);assertTrue(p.ended);assertFalse(p.complete);assertNull(original.stopped);
    }

    @Test public void cancellingBeforeStartNeverCountsAFutureStartDay(){
        GoalCommitment cancelled=goal("duration",480,"2026-05-10",14,"UTC",0).stop(LocalDate.parse("2026-05-01"));
        RecordEntry later=record("2026-05-09T22:00:00Z","2026-05-10T06:00:00Z","UTC",false);
        GoalProgress p=progress(cancelled,"2026-05-20T12:00:00Z",later);assertTrue(p.days.isEmpty());assertEquals(0,p.observed);assertEquals(0,p.balance);assertTrue(p.ended);assertFalse(p.complete);
    }
}
