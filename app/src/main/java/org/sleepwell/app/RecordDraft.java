package org.sleepwell.app;

import java.time.*;
import java.util.*;

/** A user edit overlay, independent of the immutable recording and its measurements. */
final class RecordDraft {
    long start,end;String note="";int rating;boolean rated;
    final List<String> tags=new ArrayList<>();final List<RecordIntervals.Span> awake=new ArrayList<>();
    RecordDraft(long start,long end){this.start=start;this.end=end;}
    RecordDraft copy(){RecordDraft d=new RecordDraft(start,end);d.note=note;d.rating=rating;d.rated=rated;d.tags.addAll(tags);d.awake.addAll(awake);return d;}
    static String tag(String text){String t=text.trim().replaceFirst("^#+","").replaceAll("\\s+"," ");if(t.isEmpty()||t.length()>40)throw new IllegalArgumentException("Use a tag of 1–40 characters.");return t;}
    void addTag(String value){String clean=tag(value);for(String t:tags)if(t.equalsIgnoreCase(clean))return;if(tags.size()>=24)throw new IllegalArgumentException("Use at most 24 tags per record.");tags.add(clean);}
    void validate(long rawStart,long rawEnd,boolean manual,long now){
        if(start<=0||end<=start)throw new IllegalArgumentException("End must be after start.");
        if(end-start>36*3600000L)throw new IllegalArgumentException("A record can cover at most 36 hours.");
        if(manual&&end>now)throw new IllegalArgumentException("A completed manual record cannot end in the future.");
        if(!manual&&(start<rawStart||end>rawEnd))throw new IllegalArgumentException("Keep the edited period inside the original recording.");
        if(note.length()>20000)throw new IllegalArgumentException("Keep notes under 20,000 characters.");
        if(rating<0||rating>5)throw new IllegalArgumentException("Choose a rating from 0 to 5.");
        if(tags.size()>24)throw new IllegalArgumentException("Use at most 24 tags per record.");
        Set<String> unique=new HashSet<>();for(String value:tags)if(!tag(value).equals(value)||!unique.add(value.toLowerCase(Locale.ROOT)))throw new IllegalArgumentException("Tags must be unique.");
        if(awake.size()>100)throw new IllegalArgumentException("Use at most 100 awake periods.");
        for(RecordIntervals.Span s:awake)if(s.start<start||s.end>end||s.end<=s.start)throw new IllegalArgumentException("Awake periods must fit inside this record.");
    }
    /** Reject nonexistent local times; an overlap requires an explicit valid offset. */
    static long instant(LocalDateTime local,ZoneId zone,ZoneOffset chosen){
        List<ZoneOffset> offsets=zone.getRules().getValidOffsets(local);
        if(offsets.isEmpty())throw new IllegalArgumentException("This local time does not exist because the clock moved forward. Choose another time.");
        if(chosen==null&&offsets.size()>1)throw new IllegalArgumentException("Choose the earlier or later clock offset for this repeated time.");
        ZoneOffset offset=chosen==null?offsets.get(0):chosen;if(!offsets.contains(offset))throw new IllegalArgumentException("That clock offset is not valid for this time.");return local.toInstant(offset).toEpochMilli();
    }
}
