package org.sleepwell.app;
import java.time.*;import java.util.*;
/** Provider-reported sleep, never inferred movement/audio. */
final class HealthSleep {
 static final int MAX_STAGES=4096;final String id,origin,title,notes;final long start,end,modified;final Integer startOffset,endOffset;final List<Stage> stages;
 static final class Stage {final long start,end;final int kind;Stage(long start,long end,int kind){this.start=start;this.end=end;this.kind=kind;}boolean awake(){return kind==1||kind==3||kind==7;}}
 HealthSleep(String id,String origin,long start,long end,long modified,Integer startOffset,Integer endOffset,String title,String notes,List<Stage> stages){
  if(id==null||id.isBlank()||id.length()>512||origin==null||!origin.matches("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)*")||origin.length()>255||start<=0||end<=start||end-start>36*3600000L||modified<0||title==null||notes==null||title.length()>1000||notes.length()>20000||stages==null||stages.size()>MAX_STAGES)throw new IllegalArgumentException("Health Connect sleep details are invalid or too large.");
  if(startOffset!=null)ZoneOffset.ofTotalSeconds(startOffset);if(endOffset!=null)ZoneOffset.ofTotalSeconds(endOffset);
  List<Stage> sorted=new ArrayList<>(stages);sorted.sort(Comparator.comparingLong(s->s.start));long previous=start;for(Stage s:sorted){if(s.start<previous||s.end<=s.start||s.end>end||s.kind<0||s.kind>7)throw new IllegalArgumentException("Health Connect sleep stages overlap or exceed their session.");previous=s.end;}
  this.id=id;this.origin=origin;this.start=start;this.end=end;this.modified=modified;this.startOffset=startOffset;this.endOffset=endOffset;this.title=title;this.notes=notes;this.stages=List.copyOf(sorted);
 }
 String zone(){return endOffset==null?"":ZoneOffset.ofTotalSeconds(endOffset).getId();}
 List<RecordIntervals.Span> awake(){List<RecordIntervals.Span> out=new ArrayList<>();for(Stage s:stages)if(s.awake())out.add(new RecordIntervals.Span(s.start,s.end));return RecordIntervals.union(out);}
 RecordDraft draft(){RecordDraft d=new RecordDraft(start,end);String text=(title.isEmpty()?"":title+"\n")+notes;d.note=text.length()>20000?text.substring(0,20000):text;return d;}
 long duration(){return end-start-RecordIntervals.length(awake());}
}
