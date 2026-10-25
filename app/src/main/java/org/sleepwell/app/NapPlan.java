package org.sleepwell.app;

import java.util.UUID;

/** A relative timer, not a recurring local-clock alarm. No sensor or sleep-stage inference. */
final class NapPlan {
 enum Phase {PENDING,CLAIMED,FINISHED,CANCELLED}
 interface Clock {long wall();long elapsed();long boot();String device();}
 final String nonce,device,error;final long startWall,startElapsed,boot;final int minutes,minimumRestMinutes;final boolean deleteAfter;final Phase phase;
 NapPlan(String nonce,String device,long startWall,long startElapsed,long boot,int minutes,boolean deleteAfter,Phase phase,String error){
  this(nonce,device,startWall,startElapsed,boot,minutes,deleteAfter,phase,error,0);
 }
 NapPlan(String nonce,String device,long startWall,long startElapsed,long boot,int minutes,boolean deleteAfter,Phase phase,String error,int minimumRestMinutes){
  if(minimumRestMinutes<0||minimumRestMinutes>0&&(minimumRestMinutes>=minutes||minutes-minimumRestMinutes>60))throw new IllegalArgumentException("Choose minimum rest and a wake interval of 1–60 minutes.");
  if(!UUID.fromString(nonce).toString().equals(nonce)||device.isEmpty()||device.length()>80||startWall<=0||startElapsed<0||boot<0||minutes<1||minutes>600||phase==null||error==null||error.length()>500)throw new IllegalArgumentException("Invalid nap timer");
  Math.addExact(startWall,minutes*60000L);Math.addExact(startElapsed,minutes*60000L);
  this.nonce=nonce;this.device=device;this.startWall=startWall;this.startElapsed=startElapsed;this.boot=boot;this.minutes=minutes;this.minimumRestMinutes=minimumRestMinutes;this.deleteAfter=deleteAfter;this.phase=phase;this.error=error;
 }
 static NapPlan create(Clock c,int minutes,long delay,boolean delete){if(delay<0||delay>366L*86400000)throw new IllegalArgumentException("Choose a nap start within the next year.");return new NapPlan(UUID.randomUUID().toString(),c.device(),Math.addExact(c.wall(),delay),Math.addExact(c.elapsed(),delay),c.boot(),minutes,delete,Phase.PENDING,"");}
 static NapPlan createPower(Clock c,int minimum,int window,long delay,boolean delete){if(minimum<1||window<1||window>60)throw new IllegalArgumentException("Choose minimum rest and a wake interval of 1–60 minutes.");NapPlan normal=create(c,Math.addExact(minimum,window),delay,delete);return new NapPlan(normal.nonce,normal.device,normal.startWall,normal.startElapsed,normal.boot,normal.minutes,delete,normal.phase,"",minimum);}
 boolean power(){return minimumRestMinutes>0;}
 int wakeInterval(){return power()?minutes-minimumRestMinutes:0;}
 long minimumRemaining(Clock c){return Math.addExact(startsIn(c),minimumRestMinutes*60000L);}
 boolean smartInterval(Clock c){return startsIn(c)<=0&&remaining(c)>0&&(!power()||minimumRemaining(c)<=0);}
 long deadlineWall(){return startWall+minutes*60000L;}
 boolean sameBoot(Clock c){return boot==c.boot()&&device.equals(c.device());}
 long remaining(Clock c){return (sameBoot(c)?startElapsed+minutes*60000L-c.elapsed():deadlineWall()-c.wall());}
 long startsIn(Clock c){return sameBoot(c)?startElapsed-c.elapsed():startWall-c.wall();}
 long projectedDeadline(Clock c){return Math.addExact(c.wall(),remaining(c));}
 NapPlan state(Phase p,String issue){return new NapPlan(nonce,device,startWall,startElapsed,boot,minutes,deleteAfter,p,issue,minimumRestMinutes);}
 boolean smartReady(Clock c,int window,double movement,int active,int samples){return phase==Phase.PENDING&&smartInterval(c)&&window>0&&remaining(c)>0&&remaining(c)<=Math.min(window,minutes)*60000L&&Double.isFinite(movement)&&movement>=.12&&active>=2&&samples>=20;}
}
