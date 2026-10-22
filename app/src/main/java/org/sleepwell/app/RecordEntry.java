package org.sleepwell.app;

import java.time.*;
import java.util.*;

/** Shared effective projection used by history, reports, CSV, editor and trends. */
final class RecordEntry {
    final long id,rawStart,rawEnd,updated;final Long active;final String source,origin,zone;
    final boolean edited;final int interrupted,clockChanged;final RecordDraft edit;final List<RecordIntervals.Span> pauses;
    RecordEntry(long id,long rawStart,long rawEnd,Long active,String source,String origin,String zone,int interrupted,int clockChanged,RecordDraft edit,boolean edited,long updated,List<RecordIntervals.Span> pauses){this.id=id;this.rawStart=rawStart;this.rawEnd=rawEnd;this.active=active;this.source=source;this.origin=origin;this.zone=zone;this.interrupted=interrupted;this.clockChanged=clockChanged;this.edit=edit;this.edited=edited;this.updated=updated;this.pauses=pauses;}
    boolean manual(){return source.equals("manual");}
    ZoneId zone(){try{return zone.isEmpty()?ZoneId.systemDefault():ZoneId.of(zone);}catch(DateTimeException e){return ZoneId.systemDefault();}}
    List<RecordIntervals.Span> coverage(){
        List<RecordIntervals.Span> original=RecordIntervals.subtract(List.of(new RecordIntervals.Span(manual()?edit.start:rawStart,manual()?edit.end:rawEnd)),manual()?List.of():pauses);
        original=RecordIntervals.limit(original,manual()?null:active);return RecordIntervals.subtract(RecordIntervals.intersect(original,edit.start,edit.end),edit.awake);
    }
    long duration(){return RecordIntervals.length(coverage());}
    boolean durationLimited(){return !manual()&&active!=null&&active>1000+RecordIntervals.length(RecordIntervals.subtract(List.of(new RecordIntervals.Span(rawStart,rawEnd)),pauses));}
    String provenance(){return (manual()?"User-reported":source.equals("legacy")?"Legacy recording":"Phone recording")+(edited&&!manual()?" · edited":"")+(origin.equals("imported")?" · imported":"");}
    RecordEntry draft(RecordDraft d){return new RecordEntry(id,rawStart,rawEnd,active,source,origin,zone,interrupted,clockChanged,d,true,updated,pauses);}
    LocalDate day(int cutoff){ZonedDateTime end=Instant.ofEpochMilli(edit.end).atZone(zone());LocalDate day=end.toLocalDate();return end.getHour()*60+end.getMinute()<cutoff?day.minusDays(1):day;}
}
