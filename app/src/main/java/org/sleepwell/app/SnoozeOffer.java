package org.sleepwell.app;

/** An offered menu belongs to one occurrence and one snooze count. Zero always means cancel/reject. */
final class SnoozeOffer {
    private final String run;private final int used;private final int[] values;
    SnoozeOffer(String run,int used,int base,boolean shorten,int limit){if(run==null||run.isEmpty()||!AlarmInputs.canSnooze(true,used,limit))throw new IllegalArgumentException("No snooze is available");this.run=run;this.used=used;values=SnoozePolicy.choices(base,shorten,used);}
    int[] values(){return values.clone();}
    int select(int index,String currentRun,int currentUsed,boolean current,boolean resumed,int limit,int base,boolean shorten){if(!current||!resumed||!run.equals(currentRun)||used!=currentUsed||index<0||index>=values.length||!AlarmInputs.canSnooze(true,currentUsed,limit))return 0;int requested=values[index];return SnoozePolicy.accepts(requested,base,shorten,currentUsed)?requested:0;}
}
