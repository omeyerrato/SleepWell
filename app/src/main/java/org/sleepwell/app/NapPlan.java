package org.sleepwell.app;

import java.util.UUID;

/** A relative timer, not a recurring local-clock alarm. No sensor or sleep-stage inference. */
final class NapPlan {
 enum Phase {PENDING,CLAIMED,FINISHED,CANCELLED}
 interface Clock {long wall();long elapsed();long boot();String device();}
 final String nonce,device,error;final long startWall,startElapsed,boot;final int minutes,minimumRestMinutes,exactSeconds;final boolean deleteAfter;final Phase phase;
 NapPlan(String nonce,String device,long startWall,long startElapsed,long boot,int minutes,boolean deleteAfter,Phase phase,String error){
  this(nonce,device,startWall,startElapsed,boot,minutes,deleteAfter,phase,error,0);
 }
 NapPlan(String nonce,String device,long startWall,long startElapsed,long boot,int minutes,boolean deleteAfter,Phase phase,String error,int minimumRestMinutes){this(nonce,device,startWall,startElapsed,boot,minutes,deleteAfter,phase,error,minimumRestMinutes,0);}
 NapPlan(String nonce,String device,long startWall,long startElapsed,long boot,int minutes,boolean deleteAfter,Phase phase,String error,int minimumRestMinutes,int exactSeconds){
  if(exactSeconds<0||exactSeconds>86400||exactSeconds>0&&(minimumRestMinutes!=0||minutes!=(exactSeconds+59)/60))throw new IllegalArgumentException("Choose an exact timer of 1 second to 24 hours.");
  if(minimumRestMinutes<0||minimumRestMinutes>0&&(minimumRestMinutes>=minutes||minutes-minimumRestMinutes>60))throw new IllegalArgumentException("Choose minimum rest and a wake interval of 1–60 minutes.");
  if(!UUID.fromString(nonce).toString().equals(nonce)||device.isEmpty()||device.length()>80||startWall<=0||startElapsed<0||boot<0||minutes<1||minutes>(exactSeconds>0?1440:600)||phase==null||error==null||error.length()>500)throw new IllegalArgumentException("Invalid nap timer");
  long duration=exactSeconds>0?exactSeconds*1000L:minutes*60000L;Math.addExact(startWall,duration);Math.addExact(startElapsed,duration);
  this.nonce=nonce;this.device=device;this.startWall=startWall;this.startElapsed=startElapsed;this.boot=boot;this.minutes=minutes;this.minimumRestMinutes=minimumRestMinutes;this.exactSeconds=exactSeconds;this.deleteAfter=deleteAfter;this.phase=phase;this.error=error;
 }
 static NapPlan create(Clock c,int minutes,long delay,boolean delete){if(delay<0||delay>366L*86400000)throw new IllegalArgumentException("Choose a nap start within the next year.");return new NapPlan(UUID.randomUUID().toString(),c.device(),Math.addExact(c.wall(),delay),Math.addExact(c.elapsed(),delay),c.boot(),minutes,delete,Phase.PENDING,"");}
 static NapPlan createPower(Clock c,int minimum,int window,long delay,boolean delete){if(minimum<1||window<1||window>60)throw new IllegalArgumentException("Choose minimum rest and a wake interval of 1–60 minutes.");NapPlan normal=create(c,Math.addExact(minimum,window),delay,delete);return new NapPlan(normal.nonce,normal.device,normal.startWall,normal.startElapsed,normal.boot,normal.minutes,delete,normal.phase,"",minimum);}
 static NapPlan createSeconds(Clock c,int seconds,long delay,boolean delete){if(seconds<1||seconds>86400||delay<0||delay>366L*86400000)throw new IllegalArgumentException("Choose 1 second to 24 hours and a start within the next year.");return new NapPlan(UUID.randomUUID().toString(),c.device(),Math.addExact(c.wall(),delay),Math.addExact(c.elapsed(),delay),c.boot(),(seconds+59)/60,delete,Phase.PENDING,"",0,seconds);}
 long durationMillis(){return exactSeconds>0?exactSeconds*1000L:minutes*60000L;}
 String durationLabel(){return exactSeconds>0?TimerRequest.duration(exactSeconds):minutes+" min";}
 boolean power(){return minimumRestMinutes>0;}
 int wakeInterval(){return power()?minutes-minimumRestMinutes:0;}
 long minimumRemaining(Clock c){return Math.addExact(startsIn(c),minimumRestMinutes*60000L);}
 boolean smartInterval(Clock c){return exactSeconds==0&&startsIn(c)<=0&&remaining(c)>0&&(!power()||minimumRemaining(c)<=0);}
 long deadlineWall(){return startWall+durationMillis();}
 boolean sameBoot(Clock c){return boot==c.boot()&&device.equals(c.device());}
 long remaining(Clock c){return (sameBoot(c)?startElapsed+durationMillis()-c.elapsed():deadlineWall()-c.wall());}
 long startsIn(Clock c){return sameBoot(c)?startElapsed-c.elapsed():startWall-c.wall();}
 long projectedDeadline(Clock c){return Math.addExact(c.wall(),remaining(c));}
 NapPlan state(Phase p,String issue){return new NapPlan(nonce,device,startWall,startElapsed,boot,minutes,deleteAfter,p,issue,minimumRestMinutes,exactSeconds);}
 boolean smartReady(Clock c,int window,double movement,int active,int samples){return exactSeconds==0&&phase==Phase.PENDING&&smartInterval(c)&&window>0&&remaining(c)>0&&remaining(c)<=Math.min(window,minutes)*60000L&&Double.isFinite(movement)&&movement>=.12&&active>=2&&samples>=20;}
}
