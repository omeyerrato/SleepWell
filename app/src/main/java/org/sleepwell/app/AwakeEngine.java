package org.sleepwell.app;
/** Single-worker monotonic evidence. Gaps never become light evidence. */
final class AwakeEngine {
 interface Sink {void span(long from,long to,String reason);}
 static final long FRESH=5000,LIGHT_CONFIRM=10000,INTERACTION_TAIL=30000,MAX=36*3600000L;
 final AwakePolicy policy;final Sink sink;final long start;long last,user=-1,userUntil=-1,bright=-1,lastLight=-1,snooze=-1,snoozeUntil=-1;boolean paused,closed;
 AwakeEngine(AwakePolicy p,long start,Sink sink){this.policy=p;this.start=this.last=start;this.sink=sink;}
 private boolean at(long now){if(closed||now<last||now<start||now-start>MAX)return false;last=now;return policy.enabled&&!paused;}
 void interaction(long now){if(!at(now)||!policy.interaction)return;expire(now);if(user<0)user=now;userUntil=Math.min(start+MAX,now+INTERACTION_TAIL);}
 void screenOff(long now){if(!at(now))return;endUser(now);}
 void light(long now,Double lux){if(!at(now)||!policy.light)return;expire(now);if(lux==null||!Double.isFinite(lux)||lux<60){endLight();return;}if(bright<0)bright=now;lastLight=now;}
 void snooze(long now,long until){if(!at(now)||!policy.snooze||until<=now||until-now>60*60000L)return;expire(now);if(snooze<0)snooze=now;snoozeUntil=Math.min(start+MAX,Math.max(snoozeUntil,until));}
 void tick(long now){if(at(now))expire(now);}
 private void expire(long now){if(user>=0&&now>=userUntil)endUser(userUntil);if(bright>=0&&now-lastLight>FRESH)endLight();if(snooze>=0&&now>=snoozeUntil)endSnooze(snoozeUntil);}
 private void emit(long a,long b,String why){if(a>=start&&b>a)sink.span(a,b,why);}
 private void endUser(long now){if(user>=0)emit(user,Math.min(now,userUntil),"phone_use");user=userUntil=-1;}
 private void endLight(){if(bright>=0&&lastLight-bright>=LIGHT_CONFIRM)emit(bright,lastLight,"bright_light");bright=lastLight=-1;}
 private void endSnooze(long now){if(snooze>=0)emit(snooze,Math.min(now,snoozeUntil),"snooze");snooze=snoozeUntil=-1;}
 void pause(long now){if(closed||now<last)return;last=now;endUser(now);endLight();endSnooze(now);paused=true;}
 void resume(long now){if(!closed&&now>=last){last=now;paused=false;}}
 void finish(long now){if(closed)return;pause(Math.max(last,now));closed=true;}
}
