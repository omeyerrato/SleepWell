package org.sleepwell.app;
/** Immutable elapsed-time waiting plan. Waiting is not a recording or sleep estimate. */
final class TrackingDelay {
 enum Phase{WAITING,CLAIMED,STARTED,CANCELLED,INTERRUPTED,FAILED}
 static final long LATE=30000;static final int[] CHOICES={0,1,5,10,15,30};
 final String token;final long elapsed,wall,deadline,session;final int boot,minutes;final Phase phase;final boolean sonar,sounds,clips,light;final String issue;
 static boolean allowed(int minutes){for(int x:CHOICES)if(x==minutes)return true;return false;}
 TrackingDelay(String token,long elapsed,long wall,int boot,int minutes,boolean sonar,boolean sounds,boolean clips,boolean light,Phase phase,long session,String issue){if(token==null||token.isBlank()||elapsed<0||wall<0||boot<0||minutes<=0||!allowed(minutes)||phase==null||session<0||phase==Phase.STARTED&&session==0||phase!=Phase.STARTED&&session!=0||clips&&!sounds||sonar&&light||issue==null||issue.length()>1000)throw new IllegalArgumentException("Invalid delayed tracking plan");this.token=token;this.elapsed=elapsed;this.wall=wall;this.boot=boot;this.minutes=minutes;deadline=Math.addExact(elapsed,minutes*60000L);this.sonar=sonar;this.sounds=sounds;this.clips=clips;this.light=light;this.phase=phase;this.session=session;this.issue=issue;}
 long remaining(long now){return Math.max(0,deadline-now);}boolean pending(){return phase==Phase.WAITING||phase==Phase.CLAIMED;}
 TrackingDelay state(Phase next,long id,String error){return new TrackingDelay(token,elapsed,wall,boot,minutes,sonar,sounds,clips,light,next,id,error);}
 TrackingDelay claim(long now,int currentBoot,boolean startNow){if(phase!=Phase.WAITING)return this;if(currentBoot!=boot||now<elapsed||now>deadline&&now-deadline>LATE)return state(Phase.INTERRUPTED,0,"The waiting window was interrupted. Start again explicitly.");if(!startNow&&now<deadline)return this;return state(Phase.CLAIMED,0,"");}
 String status(){return switch(phase){case WAITING->"Waiting to start";case CLAIMED->"Preparing recording";case STARTED->"Recording began after the wait";case CANCELLED->"Delay cancelled; no recording created";case INTERRUPTED->"Delay interrupted; start again explicitly";case FAILED->"Delayed start needs attention";};}
}
