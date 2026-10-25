package org.sleepwell.app;
/** A menu is frozen to one occurrence/count and one remaining total budget. */
final class SnoozeOffer {
 private final String run;private final int used;private final int[] values;private final long remaining;
 SnoozeOffer(String run,int used,int base,boolean shorten,int limit){this(run,used,base,shorten,limit,Long.MAX_VALUE);}
 SnoozeOffer(String run,int used,int base,boolean shorten,int limit,long remaining){if(run==null||run.isEmpty()||remaining<=0||!AlarmInputs.canSnooze(true,used,limit))throw new IllegalArgumentException("No snooze is available");this.run=run;this.used=used;this.remaining=remaining;values=SnoozePolicy.choices(base,shorten,used,remaining);}
 int[] values(){return values.clone();}
 boolean current(String currentRun,int currentUsed,boolean active,boolean resumed,int limit,long currentRemaining){return active&&resumed&&run.equals(currentRun)&&used==currentUsed&&remaining==currentRemaining&&currentRemaining>0&&AlarmInputs.canSnooze(true,currentUsed,limit);}
 int select(int index,String currentRun,int currentUsed,boolean active,boolean resumed,int limit,int base,boolean shorten){return select(index,currentRun,currentUsed,active,resumed,limit,base,shorten,Long.MAX_VALUE);}
 int select(int index,String currentRun,int currentUsed,boolean active,boolean resumed,int limit,int base,boolean shorten,long remaining){if(index<0||index>=values.length||!current(currentRun,currentUsed,active,resumed,limit,remaining))return 0;int requested=values[index];return SnoozePolicy.accepts(requested,base,shorten,currentUsed)&&requested*60000L<=remaining?requested:0;}
}
