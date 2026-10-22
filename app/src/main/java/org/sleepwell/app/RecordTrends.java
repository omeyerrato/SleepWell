package org.sleepwell.app;

import java.time.*;
import java.util.*;

/** Daily observed coverage, never an estimate of missing nights or clinical sleep debt. */
final class RecordTrends {
    static final class Day {
        final LocalDate date;final List<RecordIntervals.Span> coverage=new ArrayList<>();final List<RecordEntry> entries=new ArrayList<>();
        Day(LocalDate date){this.date=date;}
        long duration(){return RecordIntervals.length(coverage);}
        Double bedtime(){RecordEntry longest=null;for(RecordEntry r:entries){boolean nap=r.edit.tags.stream().anyMatch(t->t.equalsIgnoreCase("nap"));if(!nap&&r.duration()>=3*3600000L&&(longest==null||r.duration()>longest.duration()))longest=r;}if(longest==null)return null;ZonedDateTime t=Instant.ofEpochMilli(longest.edit.start).atZone(longest.zone());return (double)nightMinute(t.getHour()*60+t.getMinute());}
    }
    static final class Range {final LocalDate start,end;Range(LocalDate start,LocalDate end){this.start=start;this.end=end;}boolean contains(LocalDate day){return !day.isBefore(start)&&!day.isAfter(end);}}
    static Range range(LocalDate anchor,int selection){return selection==1?new Range(anchor.withDayOfMonth(1),anchor.withDayOfMonth(anchor.lengthOfMonth())):selection==2?new Range(anchor.withDayOfYear(1),anchor.withDayOfYear(anchor.lengthOfYear())):new Range(anchor.minusDays(6),anchor);}
    static SortedMap<LocalDate,Day> days(List<RecordEntry> records,Range range,int cutoff){
        SortedMap<LocalDate,Day> all=new TreeMap<>();for(RecordEntry r:records){Day d=all.computeIfAbsent(r.day(cutoff),Day::new);d.coverage.addAll(r.coverage());d.entries.add(r);}
        List<RecordIntervals.Span> claimed=new ArrayList<>();SortedMap<LocalDate,Day> result=new TreeMap<>();
        for(Day day:all.values()){List<RecordIntervals.Span> unique=RecordIntervals.subtract(day.coverage,claimed);claimed.addAll(unique);claimed=RecordIntervals.union(claimed);day.coverage.clear();day.coverage.addAll(unique);if(range.contains(day.date))result.put(day.date,day);}return result;
    }
    static long average(Collection<Day> days){if(days.isEmpty())return 0;long total=0;for(Day d:days)total=Math.addExact(total,d.duration());return total/days.size();}
    static long goalGap(Collection<Day> days,long goal){long total=0;for(Day d:days)total=Math.addExact(total,goal-d.duration());return total;}
    static int nightMinute(int minute){return minute<720?minute+1440:minute;}
    static boolean late(RecordEntry r,int bedtime){ZonedDateTime t=Instant.ofEpochMilli(r.edit.start).atZone(r.zone());int delta=Math.floorMod(t.getHour()*60+t.getMinute()-bedtime+720,1440)-720;return delta>30;}
}
