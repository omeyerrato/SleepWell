package org.sleepwell.app;

import java.util.UUID;

/** Immutable single pending check. Clock anchors never change when retrying or reopening. */
final class WakeCheckState {
 static final long WINDOW=120000;
 enum Phase {WAITING,PROMPT,CONFIRMED,EXPIRED,CANCELLED,CLAIMED}
 interface Clock {long wall();long elapsed();long boot();}
 final String nonce,run,alarm,label,error;final int alarmId;final Phase phase;final boolean legacy,delivered;
 final long promptWall,promptElapsed,promptBoot,deadlineWall,deadlineElapsed,deadlineBoot;
 WakeCheckState(String nonce,String run,String alarm,String label,int alarmId,Phase phase,boolean legacy,boolean delivered,long promptWall,long promptElapsed,long promptBoot,long deadlineWall,long deadlineElapsed,long deadlineBoot,String error){
  if(nonce==null||!UUID.fromString(nonce).toString().equals(nonce)||run==null||(!legacy&&run.isEmpty())||alarm==null||alarm.isEmpty()||alarm.length()>65536||label==null||alarmId<=0||phase==null||promptWall<=0||promptElapsed<0||promptBoot<0||deadlineWall<0||deadlineElapsed<0||deadlineBoot<0||phase==Phase.PROMPT&&deadlineWall==0||error==null)throw new IllegalArgumentException("Invalid wake-up check state.");
  this.nonce=nonce;this.run=run;this.alarm=alarm;this.label=label;this.alarmId=alarmId;this.phase=phase;this.legacy=legacy;this.delivered=delivered;this.promptWall=promptWall;this.promptElapsed=promptElapsed;this.promptBoot=promptBoot;this.deadlineWall=deadlineWall;this.deadlineElapsed=deadlineElapsed;this.deadlineBoot=deadlineBoot;this.error=error;
 }
 static WakeCheckState waiting(String nonce,String run,String alarm,String label,int id,long delay,boolean legacy,Clock clock){if(delay<0||delay>24*60*60000L)throw new IllegalArgumentException("Invalid wake-up check delay.");return new WakeCheckState(nonce,run,alarm,label,id,Phase.WAITING,legacy,false,Math.addExact(clock.wall(),delay),Math.addExact(clock.elapsed(),delay),clock.boot(),0,0,0,"");}
 WakeCheckState prompt(Clock clock){return new WakeCheckState(nonce,run,alarm,label,alarmId,Phase.PROMPT,legacy,false,promptWall,promptElapsed,promptBoot,Math.addExact(clock.wall(),WINDOW),Math.addExact(clock.elapsed(),WINDOW),clock.boot(),"");}
 WakeCheckState update(Phase phase,boolean delivered,String error){return new WakeCheckState(nonce,run,alarm,label,alarmId,phase,legacy,delivered,promptWall,promptElapsed,promptBoot,deadlineWall,deadlineElapsed,deadlineBoot,error);}
 static boolean legacyEligible(int pendingId,int savedId,int activeId,boolean ringing){return pendingId>0&&pendingId==savedId&&pendingId==activeId&&!ringing;}
 boolean active(){return phase==Phase.WAITING||phase==Phase.PROMPT;}
 long remaining(Clock clock){return phase==Phase.WAITING?remaining(clock,promptWall,promptElapsed,promptBoot):remaining(clock,deadlineWall,deadlineElapsed,deadlineBoot);}
 private static long remaining(Clock clock,long wall,long elapsed,long boot){return Math.max(0,boot==clock.boot()?elapsed-clock.elapsed():wall-clock.wall());}
}
