package org.sleepwell.app;

/** A user-reviewed calendar period, never a measured sleep-stage or calendar-sync record. */
final class CalendarEventDraft {
    final long start,end,duration;final String zone,note;
    CalendarEventDraft(long start,long end,long duration,String zone,String note){
        if(start<0||end<=start||end>253402300799999L||duration<0||duration>end-start)throw new IllegalArgumentException("This period cannot be added to a calendar.");
        java.time.ZoneId.of(zone);this.start=start;this.end=end;this.duration=duration;this.zone=zone;this.note=note==null?"":note;
        if(this.note.length()>20000)throw new IllegalArgumentException("This note is too long to share.");
    }
    String description(boolean includeNote){long minutes=duration/60000;String text="SleepWell recorded period\nEffective recorded duration: "+(minutes/60)+" h "+(minutes%60)+" min\nTime zone: "+zone+"\nThe event covers the whole period, including pauses and awake intervals. It is not a measurement of time asleep.";return includeNote&&!note.isEmpty()?text+"\n\nNote:\n"+note:text;}
}
