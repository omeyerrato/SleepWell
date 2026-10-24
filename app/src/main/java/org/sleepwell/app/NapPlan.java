package org.sleepwell.app;

import java.util.UUID;

/** A relative timer, not a recurring local-clock alarm. No sensor or sleep-stage inference. */
final class NapPlan {
 enum Phase {PENDING,CLAIMED,FINISHED,CANCELLED}
 interface Clock {long wall();long elapsed();long boot();String device();}
 final String nonce,device,error;final long startWall,startElapsed,boot;final int minutes;final boolean deleteAfter;final Phase phase;
 NapPlan(String nonce,String device,long startWall,long startElapsed,long boot,int minutes,boolean deleteAfter,Phase phase,String error){
  if(!UUID.fromString(nonce).toString().equals(nonce)||device.isEmpty()||device.length()>80||startWall<=0||startElapsed<0||boot<0||minutes<1||minutes>600||phase==null||error==null||error.length()>500)throw new IllegalArgumentException("Invalid nap timer");
  Math.addExact(startWall,minutes*60000L);Math.addExact(startElapsed,minutes*60000L);
  this.nonce=nonce;this.device=device;this.startWall=startWall;this.startElapsed=startElapsed;this.boot=boot;this.minutes=minutes;this.deleteAfter=deleteAfter;this.phase=phase;this.error=error;
 }
 static NapPlan create(Clock c,int minutes,long delay,boolean delete){if(delay<0||delay>366L*86400000)throw new IllegalArgumentException("Choose a nap start within the next year.");return new NapPlan(UUID.randomUUID().toString(),c.device(),Math.addExact(c.wall(),delay),Math.addExact(c.elapsed(),delay),c.boot(),minutes,delete,Phase.PENDING,"");}
 long deadlineWall(){return startWall+minutes*60000L;}
 boolean sameBoot(Clock c){return boot==c.boot()&&device.equals(c.device());}
 long remaining(Clock c){return (sameBoot(c)?startElapsed+minutes*60000L-c.elapsed():deadlineWall()-c.wall());}
 long startsIn(Clock c){return sameBoot(c)?startElapsed-c.elapsed():startWall-c.wall();}
 long projectedDeadline(Clock c){return Math.addExact(c.wall(),Math.max(0,remaining(c)));}
 NapPlan state(Phase p,String issue){return new NapPlan(nonce,device,startWall,startElapsed,boot,minutes,deleteAfter,p,issue);}
 boolean smartReady(Clock c,int window,double movement,int active,int samples){return phase==Phase.PENDING&&startsIn(c)<=0&&window>0&&remaining(c)>0&&remaining(c)<=Math.min(window,minutes)*60000L&&Double.isFinite(movement)&&movement>=.12&&active>=2&&samples>=20;}
}
