package org.sleepwell.app;

/** Presentation handoff only: no store, scheduling, preferences or occurrence authority. */
final class AlarmDuplicateDraft {
 static Alarm create(Alarm source,int id){
  if(source==null||source.nap!=null||source.napInvalid||id<=0||id==source.id)throw new IllegalArgumentException("Choose an ordinary alarm and an unused alarm identity.");
  Alarm draft=Alarm.read(source.json());draft.id=id;draft.enabled=false;int end=Math.min(75,source.label.length());if(end>0&&Character.isHighSurrogate(source.label.charAt(end-1)))end--;draft.label=source.label.substring(0,end)+" copy";
  // A duplicate can reuse its base schedule, never a skipped/moved/claimed occurrence.
  AlarmSkips.clear(draft);return draft;
 }
 private AlarmDuplicateDraft(){}
}
