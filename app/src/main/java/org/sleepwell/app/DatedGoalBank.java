package org.sleepwell.app;

import java.time.*;
import java.util.*;

/** A recorded-minute ledger against immutable commitments; never retrofits the current plan. */
final class DatedGoalBank {
    final Map<String,GoalProgress> progress;
    final List<GoalCommitment> goals;
    DatedGoalBank(List<GoalCommitment> goals,List<RecordEntry> records,Instant now){
        this.goals=List.copyOf(goals);Map<String,GoalProgress> calculated=new LinkedHashMap<>();List<GoalCommitment> ordered=new ArrayList<>(goals);
        ordered.sort(Comparator.comparing((GoalCommitment g)->g.start).thenComparing(g->g.id));
        List<RecordIntervals.Span> claimed=new ArrayList<>();
        for(GoalCommitment goal:ordered){
            if(!goal.metric.equals("duration")){calculated.put(goal.id,GoalProgress.calculate(goal,records,now));continue;}
            List<RecordEntry> projected=new ArrayList<>(),original=new ArrayList<>();
            for(RecordEntry r:records){
                if(r.edit.end<=r.edit.start)continue;
                RecordEntry local=new RecordEntry(r.id,r.rawStart,r.rawEnd,r.active,r.source,r.origin,goal.zone,r.interrupted,r.clockChanged,r.edit,r.edited,r.updated,r.pauses,r.sensor,r.sensorConfig,r.soundAnalysis);
                original.add(local);RecordDraft draft=local.edit.copy();draft.awake.addAll(claimed);projected.add(local.draft(draft));
            }
            RecordTrends.Range range=new RecordTrends.Range(goal.start,goal.through());
            SortedMap<LocalDate,RecordTrends.Day> actual=RecordTrends.days(projected,range,goal.cutoff),raw=RecordTrends.days(original,range,goal.cutoff);
            // Travel between adjacent frozen zones may put one interval in two periods.
            // Its already-credited minutes cannot become a second observed night or deficit.
            actual.entrySet().removeIf(e->e.getValue().duration()==0&&raw.get(e.getKey())!=null&&raw.get(e.getKey()).duration()>0);
            GoalProgress saved=GoalProgress.fromDays(goal,actual,now);calculated.put(goal.id,saved);
            for(GoalProgress.Day day:saved.days){RecordTrends.Day observation=actual.get(day.date);if(observation!=null)claimed.addAll(observation.coverage);}
            claimed=RecordIntervals.union(claimed);
        }
        progress=Collections.unmodifiableMap(calculated);
    }
    static final class Summary {
        final long balance;final int observed,unknown,commitments;final String selected;
        Summary(){this(0,0,0,0,"");}
        Summary(long balance,int observed,int unknown,int commitments,String selected){this.balance=balance;this.observed=observed;this.unknown=unknown;this.commitments=commitments;this.selected=selected;}
        boolean hasDates(){return commitments>0;}
    }
    Summary slice(String id,RecordTrends.Range range){
        GoalProgress p=progress.get(id);if(p==null)return new Summary();long balance=0;int observed=0,unknown=0;boolean included=false;
        for(GoalProgress.Day day:p.days){if(!range.contains(day.date))continue;included=true;if(day.balance==null)unknown++;else{observed++;balance=Math.addExact(balance,day.balance);}}return new Summary(balance,observed,unknown,included?1:0,included?id:"");
    }
    Summary summary(RecordTrends.Range range){
        long balance=0;int observed=0,unknown=0,commitments=0;String selected="";LocalDate latest=null;
        for(GoalCommitment goal:goals){if(!goal.metric.equals("duration"))continue;Summary part=slice(goal.id,range);balance=Math.addExact(balance,part.balance);observed+=part.observed;unknown+=part.unknown;
            if(part.hasDates()){commitments++;if(latest==null||goal.start.isAfter(latest)){latest=goal.start;selected=goal.id;}}
        }return new Summary(balance,observed,unknown,commitments,selected);
    }
    /** Current duration first, then the latest started period; future goals never mask history. */
    GoalCommitment preferred(Instant now){
        GoalCommitment active=null,history=null,future=null;
        for(GoalCommitment goal:goals){if(!goal.metric.equals("duration"))continue;LocalDate today=SleepDay.label(now.toEpochMilli(),ZoneId.of(goal.zone),goal.cutoff);
            if(today.isBefore(goal.start)){if(future==null||goal.start.isBefore(future.start))future=goal;continue;}
            if(history==null||goal.start.isAfter(history.start))history=goal;
            if(goal.stopped==null&&!today.isAfter(goal.through())&&(active==null||goal.start.isAfter(active.start)))active=goal;
        }return active!=null?active:history!=null?history:future;
    }
}
