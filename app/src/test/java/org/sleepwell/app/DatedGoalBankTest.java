package org.sleepwell.app;

import org.junit.Test;
import java.time.*;
import java.util.*;
import static org.junit.Assert.*;

public final class DatedGoalBankTest {
 long id;
 GoalCommitment goal(int minutes,String start,String zone,int cutoff){return new GoalCommitment(UUID.randomUUID().toString(),"duration",minutes,LocalDate.parse(start),14,zone,cutoff,"",null);}
 RecordEntry night(String start,String end){RecordDraft d=new RecordDraft(Instant.parse(start).toEpochMilli(),Instant.parse(end).toEpochMilli());return new RecordEntry(++id,d.start,d.end,null,"manual","local","UTC",0,0,d,true,1,List.of());}
 DatedGoalBank ledger(List<GoalCommitment> goals,String now,RecordEntry... records){return new DatedGoalBank(goals,List.of(records),Instant.parse(now));}
 RecordTrends.Range range(String a,String b){return new RecordTrends.Range(LocalDate.parse(a),LocalDate.parse(b));}
 @Test public void adjacentTargetsAreFrozenAndAggregateOnlyTheirOwnRecordedDays(){
  GoalCommitment first=goal(480,"2026-10-01","UTC",0),next=goal(420,"2026-10-15","UTC",0);
  RecordEntry a=night("2026-10-01T23:00:00Z","2026-10-02T06:00:00Z"),b=night("2026-10-15T22:00:00Z","2026-10-16T06:00:00Z");
  DatedGoalBank bank=ledger(List.of(next,first),"2026-10-18T12:00:00Z",a,b);DatedGoalBank.Summary s=bank.summary(range("2026-10-01","2026-10-17"));
  assertEquals(0,s.balance);assertEquals(2,s.observed);assertEquals(15,s.unknown);assertEquals(2,s.commitments);assertEquals(next.id,s.selected);
  assertEquals(-3600000,bank.progress.get(first.id).balance);assertEquals(3600000,bank.progress.get(next.id).balance);
 }
 @Test public void aNewPlanCannotRetroactivelyChangeHistoricalTargets(){
  SleepPlan plan=new SleepPlan();GoalCommitment frozen=goal(plan.goal,"2026-10-01","UTC",0);plan.duration(600);
  DatedGoalBank bank=ledger(List.of(frozen),"2026-10-04T12:00:00Z",night("2026-10-01T23:00:00Z","2026-10-02T06:00:00Z"));
  assertEquals(-3600000,bank.summary(range("2026-10-01","2026-10-03")).balance);assertEquals(480,frozen.target);assertEquals(600,plan.goal);
 }
 @Test public void currentFutureAndMissingDaysNeverGenerateShortfall(){
  GoalCommitment g=goal(480,"2026-10-01","UTC",0);DatedGoalBank bank=ledger(List.of(g),"2026-10-03T12:00:00Z",night("2026-10-02T22:00:00Z","2026-10-03T06:00:00Z"));
  DatedGoalBank.Summary s=bank.summary(range("2026-10-01","2026-10-14"));assertEquals(0,s.observed);assertEquals(2,s.unknown);assertEquals(0,s.balance);
 }
 @Test public void futureAndBedtimeGoalsDoNotMaskCurrentDuration(){
  GoalCommitment current=goal(480,"2026-10-01","UTC",0),future=goal(600,"2026-11-01","UTC",0),bed=new GoalCommitment(UUID.randomUUID().toString(),"bedtime",1320,LocalDate.parse("2026-10-02"),14,"UTC",0,"",null);
  assertEquals(current.id,ledger(List.of(current,bed,future),"2026-10-05T12:00:00Z").preferred(Instant.parse("2026-10-05T12:00:00Z")).id);
  assertFalse(ledger(List.of(bed),"2026-10-05T12:00:00Z").summary(range("2026-10-01","2026-10-04")).hasDates());
 }
 @Test public void adjacentZonePeriodsCannotCreditOneActualIntervalTwice(){
  GoalCommitment first=goal(480,"2026-10-01","UTC",0),next=goal(480,"2026-10-15","Asia/Tokyo",0);
  RecordEntry overlap=night("2026-10-14T12:00:00Z","2026-10-14T20:00:00Z");DatedGoalBank bank=ledger(List.of(next,first),"2026-10-17T12:00:00Z",overlap);
  assertEquals(1,bank.summary(range("2026-10-01","2026-10-16")).observed);assertEquals(0,bank.progress.get(first.id).balance);assertEquals(0,bank.progress.get(next.id).observed);assertEquals(0,bank.progress.get(next.id).balance);assertTrue(overlap.edit.awake.isEmpty());
 }
 @Test public void editedAwakeAndOverlappingRecordsUseEffectiveCoverageOnce(){
  GoalCommitment g=goal(480,"2026-10-01","UTC",0);RecordEntry original=night("2026-10-01T22:00:00Z","2026-10-02T06:00:00Z");RecordDraft edit=original.edit.copy();edit.awake.add(new RecordIntervals.Span(Instant.parse("2026-10-02T01:00:00Z").toEpochMilli(),Instant.parse("2026-10-02T02:00:00Z").toEpochMilli()));
  RecordEntry duplicate=night("2026-10-01T23:00:00Z","2026-10-02T01:00:00Z");DatedGoalBank bank=ledger(List.of(g),"2026-10-03T12:00:00Z",original.draft(edit),duplicate);
  assertEquals(-3600000,bank.progress.get(g.id).balance);assertTrue(original.edit.awake.isEmpty());assertEquals(8*3600000L,original.duration());
 }
 @Test public void rangeSlicesUsePerDayAmountsRatherThanWholePeriodBalance(){
  GoalCommitment g=goal(480,"2026-10-01","UTC",0);DatedGoalBank bank=ledger(List.of(g),"2026-10-05T12:00:00Z",night("2026-10-01T23:00:00Z","2026-10-02T06:00:00Z"),night("2026-10-02T21:00:00Z","2026-10-03T06:00:00Z"));
  assertEquals(-3600000,bank.summary(range("2026-10-02","2026-10-02")).balance);assertEquals(3600000,bank.summary(range("2026-10-03","2026-10-03")).balance);assertEquals(0,bank.summary(range("2026-10-01","2026-10-04")).balance);
 }
 @Test public void closedDayBoundaryUsesFrozenZoneAcrossRepeatedDSTHour(){
  GoalCommitment g=goal(480,"2026-10-24","Europe/Berlin",150);RecordEntry r=night("2026-10-23T20:00:00Z","2026-10-24T04:00:00Z");
  DatedGoalBank a=ledger(List.of(g),"2026-10-25T00:40:00Z",r),b=ledger(List.of(g),"2026-10-25T01:20:00Z",r);assertEquals(a.progress.get(g.id).closedThrough,b.progress.get(g.id).closedThrough);assertEquals(a.progress.get(g.id).observed,b.progress.get(g.id).observed);
 }
 @Test public void stoppedAndZeroLengthRecoveryRowsStayHonest(){
  GoalCommitment g=goal(480,"2026-10-01","UTC",0).stop(LocalDate.parse("2026-10-02"));RecordEntry empty=night("2026-10-02T06:00:00Z","2026-10-02T06:00:00Z"),after=night("2026-10-02T22:00:00Z","2026-10-03T06:00:00Z");
  DatedGoalBank.Summary s=ledger(List.of(g),"2026-10-05T12:00:00Z",empty,after).summary(range("2026-10-01","2026-10-04"));assertEquals(2,s.unknown);assertEquals(0,s.observed);assertEquals(0,s.balance);
 }
}
