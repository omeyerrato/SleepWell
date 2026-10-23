package org.sleepwell.app;

import java.time.*;
import java.util.*;

/** Signed observed surplus. Missing/current days never become invented deficits. */
final class GoalProgress {
    static final class Day {
        final LocalDate date;final Long balance;final Long duration;final Double bedtime;
        Day(LocalDate date,Long balance,Long duration,Double bedtime){this.date=date;this.balance=balance;this.duration=duration;this.bedtime=bedtime;}
    }
    final List<Day> days=new ArrayList<>();long balance;int observed,unknown;
    boolean ended,complete;LocalDate closedThrough;
    static GoalProgress calculate(GoalCommitment goal,List<RecordEntry> records,Instant now){
        ZoneId zone=ZoneId.of(goal.zone);LocalDate today=SleepDay.label(now.toEpochMilli(),zone,goal.cutoff);
        LocalDate last=today.minusDays(1);if(last.isAfter(goal.through()))last=goal.through();
        List<RecordEntry> inZone=new ArrayList<>();for(RecordEntry r:records)inZone.add(new RecordEntry(r.id,r.rawStart,r.rawEnd,r.active,r.source,r.origin,goal.zone,r.interrupted,r.clockChanged,r.edit,r.edited,r.updated,r.pauses));
        SortedMap<LocalDate,RecordTrends.Day> actual=RecordTrends.days(inZone,new RecordTrends.Range(goal.start,goal.through()),goal.cutoff);
        GoalProgress progress=new GoalProgress();progress.closedThrough=last;progress.ended=today.isAfter(goal.through());
        for(LocalDate day=goal.start;!day.isAfter(last);day=day.plusDays(1)){
            RecordTrends.Day saved=actual.get(day);Long duration=saved==null?null:saved.duration();Double bedtime=saved==null?null:saved.bedtime();Long amount=null;
            if(goal.metric.equals("duration")&&duration!=null)amount=duration-goal.target*60000L;
            if(goal.metric.equals("bedtime")&&bedtime!=null){int delta=Math.floorMod((int)Math.round(bedtime)-goal.target+720,1440)-720;amount=-delta*60000L;}
            progress.days.add(new Day(day,amount,duration,bedtime));if(amount==null)progress.unknown++;else{progress.observed++;progress.balance=Math.addExact(progress.balance,amount);}
        }
        progress.complete=progress.ended&&progress.unknown==0&&progress.observed==goal.days&&goal.stopped==null;return progress;
    }
    static String signed(long millis){return (millis<0?"−":millis>0?"+":"")+RecordUiSafe.duration(Math.abs(millis));}
    /** No Android dependency in the calculation layer. */
    private static final class RecordUiSafe {static String duration(long ms){long min=ms/60000;return min>=60?min/60+" h"+(min%60==0?"":" "+min%60+" min"):min+" min";}}
}
