package org.sleepwell.app;

import java.time.*;
import java.util.*;
import java.util.function.LongUnaryOperator;

/** Read-only calendar planning. Missing coverage keeps the ordinary alarm. */
final class CalendarPolicy {
    static final class Rules {
        final boolean enabled;
        final Set<Long> holidays,meetings;
        final int preparation,advance;
        final String revision;
        Rules(boolean enabled,Set<Long> holidays,Set<Long> meetings,int preparation,int advance,String revision){
            if(holidays==null||meetings==null||holidays.size()>64||meetings.size()>64||preparation<5||preparation>180||advance<15||advance>360||revision==null||revision.isEmpty()||revision.length()>80)throw new IllegalArgumentException("Invalid calendar rules");
            for(long id:holidays)if(id<0)throw new IllegalArgumentException("Invalid calendar");
            for(long id:meetings)if(id<0)throw new IllegalArgumentException("Invalid calendar");
            this.enabled=enabled;this.holidays=Set.copyOf(holidays);this.meetings=Set.copyOf(meetings);this.preparation=preparation;this.advance=advance;this.revision=revision;
        }
        static Rules off(){return new Rules(false,Set.of(),Set.of(),30,180,"initial");}
    }
    static final class Event {
        final long calendar,begin,end;final boolean allDay;
        Event(long calendar,long begin,long end,boolean allDay){if(calendar<0||begin<0||end<=begin)throw new IllegalArgumentException("Invalid calendar interval");this.calendar=calendar;this.begin=begin;this.end=end;this.allDay=allDay;}
        boolean on(LocalDate day){return allDay&&!day.isBefore(Instant.ofEpochMilli(begin).atZone(ZoneOffset.UTC).toLocalDate())&&day.isBefore(Instant.ofEpochMilli(end).atZone(ZoneOffset.UTC).toLocalDate());}
    }
    static final class Snapshot {
        final long from,through,readAt;final ZoneId zone;final String revision;final List<Event> events;
        Snapshot(long from,long through,long readAt,ZoneId zone,String revision,List<Event> events){if(from<0||through<=from||readAt<0||zone==null||revision==null||events==null||events.size()>4096)throw new IllegalArgumentException("Invalid calendar snapshot");this.from=from;this.through=through;this.readAt=readAt;this.zone=zone;this.revision=revision;this.events=List.copyOf(events);}
        boolean fresh(long now,ZoneId current,Rules rules){return rules.revision.equals(revision)&&zone.equals(current)&&now>=readAt&&now-readAt<=30*60000L;}
        boolean covers(LocalDate date){return from<=date.atStartOfDay(zone).toInstant().toEpochMilli()&&through>=date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli();}
    }
    static final class Plan {
        final long originalBase,base,deadline;final int skipped;final String reason,revision;
        Plan(long originalBase,long base,long deadline,int skipped,String reason){this(originalBase,base,deadline,skipped,reason,"");}
        Plan(long originalBase,long base,long deadline,int skipped,String reason,String revision){this.originalBase=originalBase;this.base=base;this.deadline=deadline;this.skipped=skipped;this.reason=reason;this.revision=revision;}
        boolean changed(){return deadline!=originalBase;}
    }
    static Plan ordinary(long base,String reason){return new Plan(base,base,base,0,reason);}
    static Plan plan(Rules rules,Snapshot snapshot,long now,long originalBase,ZoneId zone,LongUnaryOperator following){
        if(!rules.enabled)return ordinary(originalBase,"");
        if(originalBase<=now||snapshot==null||!snapshot.fresh(now,zone,rules))return ordinary(originalBase,"Calendar unavailable; ordinary alarm kept");
        long candidate=originalBase;int skipped=0;
        for(int n=0;n<32;n++){
            LocalDate date=Instant.ofEpochMilli(candidate).atZone(zone).toLocalDate();
            if(!snapshot.covers(date))return ordinary(originalBase,"Calendar range unavailable; ordinary alarm kept");
            boolean holiday=false;for(Event event:snapshot.events)if(rules.holidays.contains(event.calendar)&&event.on(date)){holiday=true;break;}
            if(holiday){long next=following.applyAsLong(candidate);if(next<=candidate)return ordinary(originalBase,"Next occurrence unavailable; ordinary alarm kept");candidate=next;skipped++;continue;}
            long deadline=candidate;
            for(Event event:snapshot.events){
                if(event.allDay||!rules.meetings.contains(event.calendar))continue;
                long proposed=event.begin-rules.preparation*60000L;
                // A shift stays on the alarm's local day and can never postpone its deadline.
                if(proposed<=now||proposed<candidate-rules.advance*60000L||proposed>=deadline||!Instant.ofEpochMilli(proposed).atZone(zone).toLocalDate().equals(date))continue;
                deadline=proposed;
            }
            return new Plan(originalBase,candidate,deadline,skipped,deadline<candidate?"Earlier for a calendar meeting":skipped>0?"Skipped all-day calendar dates":"",rules.revision);
        }
        return ordinary(originalBase,"Too many calendar exclusions; ordinary alarm kept");
    }
    private CalendarPolicy(){}
}
