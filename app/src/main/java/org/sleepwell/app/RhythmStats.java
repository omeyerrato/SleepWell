package org.sleepwell.app;

import java.time.*;
import java.util.*;

/** Descriptive recorded timing; neither sleep-onset detection nor a clinical chronotype estimator. */
final class RhythmStats {
    static final long MAIN_MINIMUM=3*3600000L;
    static final class Night {
        final RecordEntry record;final LocalDate day,wakeDay;final double bedtime,midpoint;final long start,end;
        Night(RecordEntry record,LocalDate day,List<RecordIntervals.Span> coverage){this.record=record;this.day=day;start=coverage.get(0).start;end=coverage.get(coverage.size()-1).end;wakeDay=Instant.ofEpochMilli(end).atZone(record.zone()).toLocalDate();bedtime=minute(start,record.zone());midpoint=minute(start+(end-start)/2,record.zone());}
    }
    final List<Night> nights,recent;final int recorded,workCount,freeCount;final Double workMidpoint,freeMidpoint,jetlag,range;
    RhythmStats(List<RecordEntry> input,int cutoff,int workdays,long now){
        if(cutoff<0||cutoff>=1440||workdays<0||workdays>127)throw new IllegalArgumentException("Invalid rhythm settings");
        List<RecordEntry> eligible=new ArrayList<>();for(RecordEntry r:input)if(r.rawEnd>0&&r.edit.end<=now&&r.edit.end>r.edit.start&&r.duration()>=MAIN_MINIMUM&&r.edit.tags.stream().noneMatch(t->t.equalsIgnoreCase("nap")))eligible.add(r);
        List<Night> chosen=new ArrayList<>();if(!eligible.isEmpty()){
            LocalDate first=eligible.stream().map(r->r.day(cutoff)).min(LocalDate::compareTo).get(),last=eligible.stream().map(r->r.day(cutoff)).max(LocalDate::compareTo).get();
            for(RecordTrends.Day day:RecordTrends.days(eligible,new RecordTrends.Range(first,last),cutoff).values()){
                if(day.duration()<MAIN_MINIMUM)continue;
                RecordEntry longest=day.entries.stream().max(Comparator.comparingLong(RecordEntry::duration).thenComparingLong(r->r.updated).thenComparingLong(r->r.id)).get();
                chosen.add(new Night(longest,day.date,longest.coverage()));
            }
        }
        nights=List.copyOf(chosen);recent=List.copyOf(chosen.subList(Math.max(0,chosen.size()-7),chosen.size()));recorded=(int)chosen.stream().filter(n->n.record.source.equals("tracked")).count();
        List<Double> beds=new ArrayList<>(),work=new ArrayList<>(),free=new ArrayList<>();for(Night n:recent){beds.add(n.bedtime);(working(n.wakeDay,workdays)?work:free).add(n.midpoint);}workCount=work.size();freeCount=free.size();workMidpoint=mean(work);freeMidpoint=mean(free);jetlag=workMidpoint==null||freeMidpoint==null?null:distance(workMidpoint,freeMidpoint);range=beds.size()<2?null:arc(beds)[1];
    }
    static boolean working(LocalDate wakeDay,int mask){return (mask&(1<<(wakeDay.getDayOfWeek().getValue()-1)))!=0;}
    static double minute(long time,ZoneId zone){ZonedDateTime local=Instant.ofEpochMilli(time).atZone(zone);return local.getHour()*60+local.getMinute()+local.getSecond()/60d;}
    static Double mean(List<Double> minutes){if(minutes.isEmpty())return null;double x=0,y=0;for(double value:minutes){double angle=value*Math.PI/720;x+=Math.cos(angle);y+=Math.sin(angle);}if(Math.hypot(x,y)<1e-9*minutes.size())return null;return (Math.atan2(y,x)*720/Math.PI+1440)%1440;}
    static double distance(double first,double second){double diff=Math.abs(first-second)%1440;return Math.min(diff,1440-diff);}
    /** UI clocks and their difference use the same nearest-minute precision. Raw analytics stay unrounded. */
    static int displayMinute(double minute){return Math.floorMod((int)Math.round(minute),1440);}
    static long displayGapMillis(double first,double second){return Math.round(distance(displayMinute(first),displayMinute(second)))*60000L;}
    static long displayDurationMillis(double minutes){return Math.round(minutes)*60000L;}
    /** Smallest containing circular arc, expressed as start clock minute and clockwise length. */
    static double[] arc(List<Double> values){if(values.isEmpty())return new double[]{0,0};List<Double> sorted=new ArrayList<>(values);Collections.sort(sorted);double gap=-1,start=sorted.get(0);for(int n=0;n<sorted.size();n++){double next=n+1<sorted.size()?sorted.get(n+1):sorted.get(0)+1440,space=next-sorted.get(n);if(space>gap){gap=space;start=next%1440;}}return new double[]{start,1440-gap};}
}
