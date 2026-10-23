package org.sleepwell.app;

import java.time.*;
import java.util.*;

/** Screen-event evidence only: an idle phone is not evidence that its owner slept. */
final class MissedNight {
    enum Kind { OFF, ON, RESET }
    static final class Event {
        final long at;final Kind kind;
        Event(long at,Kind kind){this.at=at;this.kind=Objects.requireNonNull(kind);}
    }
    static final class Interval {
        final long start,end;
        Interval(long start,long end){if(start<=0||end<=start)throw new IllegalArgumentException("Invalid phone-idle interval.");this.start=start;this.end=end;}
        boolean same(Interval other){return other!=null&&start==other.start&&end==other.end;}
    }
    static final class Window {
        final long start,end;final LocalDate date;
        Window(LocalDate date,ZoneId zone,long now){
            LocalDate today=Instant.ofEpochMilli(now).atZone(zone).toLocalDate();
            if(date.isAfter(today)||date.isBefore(today.minusDays(2)))throw new IllegalArgumentException("Choose one of the last three nights.");
            this.date=date;start=date.minusDays(1).atTime(12,0).atZone(zone).toInstant().toEpochMilli();end=Math.min(now,date.atTime(12,0).atZone(zone).toInstant().toEpochMilli());
            if(end<=start)throw new IllegalArgumentException("This night is not available yet.");
        }
    }
    static List<Interval> pairs(List<Event> events,long begin,long end){
        if(begin<=0||end<=begin)throw new IllegalArgumentException("Invalid review window.");
        List<Interval> result=new ArrayList<>();long pending=0,last=Long.MIN_VALUE;
        for(Event event:events){
            if(event.at<begin||event.at>=end)continue;
            if(event.at<last)throw new IllegalArgumentException("Phone activity is out of order. Check again.");
            last=event.at;
            if(event.kind==Kind.RESET){pending=0;continue;}
            if(event.kind==Kind.OFF){pending=event.at;continue;}
            if(pending>0&&event.at>pending)result.add(new Interval(pending,event.at));
            pending=0;
        }
        return result;
    }
    static boolean overlaps(long start,long end,List<Interval> recorded){for(Interval r:recorded)if(start<r.end&&end>r.start)return true;return false;}
    static List<Interval> available(List<Interval> paired,List<Interval> recorded){List<Interval> out=new ArrayList<>();for(Interval pair:paired)if(!overlaps(pair.start,pair.end,recorded))out.add(pair);out.sort(Comparator.comparingLong((Interval p)->p.end-p.start).reversed().thenComparingLong(p->p.start));return out;}
    static void validateReview(long start,long end,long begin,long finish,long now){
        if(start<=0||end<=start)throw new IllegalArgumentException("End must be after start.");
        if(start<begin||end>finish)throw new IllegalArgumentException("Keep this estimate inside the selected night.");
        if(end>now)throw new IllegalArgumentException("A reviewed night cannot end in the future.");
    }
    private MissedNight(){}
}
