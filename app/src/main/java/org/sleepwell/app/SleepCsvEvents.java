package org.sleepwell.app;

import java.util.*;
import java.util.regex.*;

/** Read-only interpretation of explicit vendor event fields, never inferred phone sleep stages. */
final class SleepCsvEvents {
    enum Lane {
        LIGHT("LIGHT", "LIGHT_START", "LIGHT_END", 0xffad89f4),
        DEEP("DEEP", "DEEP_START", "DEEP_END", 0xff6543aa),
        REM("REM", "REM_START", "REM_END", 0xffc9adf9),
        PAUSE("PAUSED", "TRACKING_PAUSED", "TRACKING_RESUMED", 0xffc89458),
        BROKEN("BROKEN", "BROKEN_START", "BROKEN_END", 0xff8f8986);
        final String label,start,end;final int color;
        Lane(String label,String start,String end,int color){this.label=label;this.start=start;this.end=end;this.color=color;}
    }
    static final class Event {
        final int index;final String raw,name,extra;final long time;final boolean inPeriod;
        Event(int index,String raw,String name,long time,String extra,boolean inPeriod){this.index=index;this.raw=raw;this.name=name;this.time=time;this.extra=extra;this.inPeriod=inPeriod;}
        boolean alarm(){return name.startsWith("ALARM_")&&extra.isEmpty();}
    }
    static final class Span {
        final Lane lane;final Event start,end;
        Span(Lane lane,Event start,Event end){this.lane=lane;this.start=start;this.end=end;}
    }
    final List<Event> events,alarms;final List<Span> spans;final int outside,unpaired,unknown,duplicates,unreadable;
    private SleepCsvEvents(List<Event> events,List<Event> alarms,List<Span> spans,int outside,int unpaired,int unknown,int duplicates,int unreadable){this.events=List.copyOf(events);this.alarms=List.copyOf(alarms);this.spans=List.copyOf(spans);this.outside=outside;this.unpaired=unpaired;this.unknown=unknown;this.duplicates=duplicates;this.unreadable=unreadable;}
    private static final Pattern FIELD=Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)-([0-9]{1,16})(?:-(.{1,1024}))?$");
    static SleepCsvEvents read(SleepCsvFormat.Entry source,List<String> raw){
        if(raw.size()>SleepCsvFormat.MAX_COLUMNS)throw new IllegalArgumentException("Too many original event fields.");
        List<Event> events=new ArrayList<>(),alarms=new ArrayList<>();int outside=0,unknown=0,unreadable=0;
        for(int i=0;i<raw.size();i++){
            Matcher match=FIELD.matcher(raw.get(i));if(!match.matches()){unreadable++;continue;}
            long time;try{time=Long.parseLong(match.group(2));}catch(NumberFormatException invalid){unreadable++;continue;}
            Event event=new Event(i,raw.get(i),match.group(1),time,match.group(3)==null?"":match.group(3),time>=source.start&&time<=source.end);
            events.add(event);if(!event.inPeriod)outside++;else if(event.alarm())alarms.add(event);
            boolean anchor=false;for(Lane lane:Lane.values())if(lane.start.equals(event.name)||lane.end.equals(event.name))anchor=true;
            if(!event.alarm()&&(!anchor||!event.extra.isEmpty()))unknown++;
        }
        List<Event> ordered=new ArrayList<>(events);ordered.sort(Comparator.comparingLong((Event event)->event.time).thenComparingInt(event->event.index));
        List<Span> spans=new ArrayList<>();int anchors=0,duplicates=0;Set<Integer> paired=new HashSet<>();
        for(Lane lane:Lane.values()){
            Event open=null;boolean ambiguous=false;Set<String> seen=new HashSet<>();
            for(Event event:ordered){
                if(!event.extra.isEmpty()||!lane.start.equals(event.name)&&!lane.end.equals(event.name))continue;
                String key=event.name+":"+event.time;if(!seen.add(key)){duplicates++;continue;}anchors++;
                if(lane.start.equals(event.name)){if(open==null){open=event;ambiguous=false;}else ambiguous=true;}
                else {if(open!=null&&!ambiguous&&event.time>open.time&&open.inPeriod&&event.inPeriod){spans.add(new Span(lane,open,event));paired.add(open.index);paired.add(event.index);}open=null;ambiguous=false;}
            }
        }
        spans.sort(Comparator.comparingLong((Span span)->span.start.time).thenComparingInt(span->span.lane.ordinal()));
        alarms.sort(Comparator.comparingLong((Event event)->event.time).thenComparingInt(event->event.index));
        return new SleepCsvEvents(events,alarms,spans,outside,anchors-paired.size(),unknown,duplicates,unreadable);
    }
    private SleepCsvEvents(){throw new AssertionError();}
}
