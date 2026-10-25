package org.sleepwell.app;

import android.content.*;
import android.os.*;
import java.util.function.BooleanSupplier;

/** Movement-based playback attenuation estimate. No audio output, capture, stage or alarm authority. */
final class StillnessSoundFade {
 static final String KEY="soundStillnessFade";
 static final long QUIET=10*60000L,FADE=5*60000L,FRESH=90000L;
 static final double STILL=.04;
 static volatile Run current;
 static final class Policy {final boolean enabled,external;final String issue;Policy(boolean enabled,String issue){this(enabled,false,issue);}Policy(boolean enabled,boolean external,String issue){this.enabled=enabled;this.external=external;this.issue=issue;}boolean any(){return enabled||external;}}
 static Policy policy(Context c){return policy(c,false);}static Policy policy(Context c,boolean external){try{return new Policy(SoundPreferences.factory.open(c.getApplicationContext()).getBoolean(KEY,false),external,"");}catch(RuntimeException invalid){return new Policy(false,external,"Built-in stillness fade setting needs review");}}
 /** Obtained once after the sound service genuinely owns and starts this recording's playback. */
 static final class Admission {final long session;final boolean external;private final Run owner;private Admission(Run owner,boolean external){this.session=owner.session;this.owner=owner;this.external=external;}}
 static final class Evidence {
  final long from,to,first,last,gap;final int samples;final boolean valid;
  Evidence(long from,long to,long first,long last,long gap,int samples,boolean valid){this.from=from;this.to=to;this.first=first;this.last=last;this.gap=gap;this.samples=samples;this.valid=valid;}
  boolean complete(long now){long length=to-from;return valid&&length>=59000000000L&&length<=65000000000L&&samples>=60&&first>=from&&first-from<=2000000000L&&last>=first&&to-last<=2000000000L&&last-first>=55000000000L&&gap<=2000000000L&&now>=to/1000000L&&now-to/1000000L<=2000;}
 }
 /** Only admitted, actually aggregated accelerometer readings enter this minute. */
 static final class Minute {
  private long from,first,last,gap;private int samples;private boolean valid=true;
  void reset(long captured){from=captured;first=last=gap=0;samples=0;valid=true;}
  void sample(long captured,int accuracy){if(captured<from||captured<=last||accuracy<android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW||accuracy>android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH){valid=false;return;}if(first==0)first=captured;if(last>0)gap=Math.max(gap,captured-last);last=captured;if(samples<Integer.MAX_VALUE)samples++;else valid=false;}
  Evidence snapshot(long captured){return new Evidence(from,captured,first,last,gap,samples,valid);}
 }
 static void begin(Context c,long session,String run,Policy policy,BooleanSupplier live){Run old=current;if(old!=null)old.close();Run owner=new Run(c,session,run,policy,live);current=owner;owner.open();}
 static Admission claim(long session,long elapsedNow){Run owner=current;return owner==null?null:owner.claim(session,elapsedNow,false);}
 static Admission claimExternal(long session,long elapsedNow){Run owner=current;return owner==null?null:owner.claim(session,elapsedNow,true);}
 static boolean owns(Admission admission){return admission!=null&&admission.owner.owns(admission);}
 static void release(Admission admission){if(admission!=null)admission.owner.release(admission);}
 static double gain(Admission admission,long elapsedNow){return admission==null?1:admission.owner.gain(admission,elapsedNow);}
 static String status(Admission admission,long elapsedNow){return admission==null?"Stillness fade is not admitted for this playback":admission.owner.status(admission,elapsedNow);}
 static double gain(long session,long elapsedNow){Run owner=current;return owner==null||owner.session!=session||owner.ticket!=null&&owner.ticket.external?1:owner.gain(owner.ticket,elapsedNow);}
 static String status(long session,long elapsedNow){Run owner=current;return owner==null||owner.session!=session?"Stillness fade unavailable for this recording":owner.status(owner.ticket,elapsedNow);}
 static void minute(long session,String run,Evidence evidence,double inferred,boolean screenOff,boolean clockIssue){Run owner=current;if(owner!=null&&owner.session==session&&owner.run.equals(run))owner.minute(evidence,inferred,screenOff,clockIssue,SystemClock.elapsedRealtime());}
 static void pause(long session,String run){Run owner=current;if(owner!=null&&owner.session==session&&owner.run.equals(run))owner.pause();}
 static void resume(long session,String run){Run owner=current;if(owner!=null&&owner.session==session&&owner.run.equals(run))owner.resume();}
 static void end(long session,String run){Run owner=current;if(owner!=null&&owner.session==session&&owner.run.equals(run)){owner.close();if(current==owner)current=null;}}
 private static final class Run {
  final Context context;final long session;final String run;final Policy policy;final BooleanSupplier live;
  boolean active=true,closed,registered,screenKnown,screenOff;long changedAt,notBefore,lastEnd,quiet,fadeAt;Admission ticket;String reason="Waiting for tracking-owned playback";
  private final BroadcastReceiver screen=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent intent){synchronized(Run.this){if(closed)return;String action=intent.getAction();if(Intent.ACTION_SCREEN_ON.equals(action)||Intent.ACTION_SCREEN_OFF.equals(action)){screenKnown=true;screenOff=Intent.ACTION_SCREEN_OFF.equals(action);changedAt=SystemClock.elapsedRealtime();reset(screenOff?"Waiting for fresh stillness minutes":"Screen in use");}}}};
  Run(Context c,long session,String run,Policy policy,BooleanSupplier live){context=c.getApplicationContext();this.session=session;this.run=run;this.policy=policy;this.live=live;notBefore=SystemClock.elapsedRealtime();}
  synchronized void open(){if(!policy.any()||closed)return;try{IntentFilter filter=new IntentFilter(Intent.ACTION_SCREEN_ON);filter.addAction(Intent.ACTION_SCREEN_OFF);androidx.core.content.ContextCompat.registerReceiver(context,screen,filter,androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED);registered=true;readScreen();}catch(RuntimeException unavailable){screenKnown=false;reset("Screen state unavailable; stillness fade is inactive");}}
  private void readScreen(){try{PowerManager power=context.getSystemService(PowerManager.class);screenKnown=power!=null;screenOff=power!=null&&!power.isInteractive();changedAt=SystemClock.elapsedRealtime();}catch(RuntimeException unavailable){screenKnown=false;screenOff=false;}}
  synchronized boolean owns(Admission expected){return expected!=null&&ticket==expected&&expected.owner==this&&current==this&&!closed&&active&&(expected.external?policy.external:policy.enabled)&&live.getAsBoolean();}
  synchronized Admission claim(long expected,long now,boolean external){if(current!=this||closed||!active||!(external?policy.external:policy.enabled)||external&&ticket!=null||session!=expected||!live.getAsBoolean()||now<notBefore||Math.abs(now-SystemClock.elapsedRealtime())>1000)return null;ticket=new Admission(this,external);notBefore=now;reset("Waiting for 10 measured stillness minutes after playback began");return ticket;}
  synchronized void release(Admission expected){if(ticket!=expected)return;ticket=null;reset("Playback ownership released");}
  synchronized void minute(Evidence evidence,double inferred,boolean nowScreenOff,boolean clockIssue,long now){
   if(current!=this||closed||!active||ticket==null||!(ticket.external?policy.external:policy.enabled)||!live.getAsBoolean()){reset("Waiting for eligible tracking-owned playback");return;}
   long from=evidence.from/1000000L,to=evidence.to/1000000L;
   if(clockIssue){reset("Clock changed; stillness fade estimate unavailable");return;}
   if(!screenKnown||!screenOff||!nowScreenOff||changedAt>from){reset("Screen use or unknown screen state reset the estimate");return;}
   if(from<notBefore||!evidence.complete(now)){reset("Missing, stale or incomplete motion minute reset the estimate");return;}
   if(!Double.isFinite(inferred)||inferred<0||inferred>STILL){reset("Measured movement reset the estimate");return;}
   if(lastEnd>0&&(from<lastEnd||from-lastEnd>2000))reset("Motion minutes were not contiguous");
   quiet+=to-from;lastEnd=to;reason=fadeAt>0?"Fading after sustained measured stillness":"Measured stillness "+Math.min(10,quiet/60000L)+" / 10 min · estimate only";
   if(quiet>=QUIET&&fadeAt==0){fadeAt=now;reason="Fading over 5 min after measured stillness · estimate only";}
  }
  private void reset(String reason){quiet=lastEnd=fadeAt=0;this.reason=reason;}
  private boolean eligible(Admission expected,long now){if(expected==null||ticket!=expected||expected.owner!=this||current!=this||closed||!active||!(expected.external?policy.external:policy.enabled)||!live.getAsBoolean()||now<notBefore)return false;if(lastEnd>0&&(now<lastEnd||now-lastEnd>FRESH)){reset("Fresh motion evidence is missing; estimate reset");return false;}return screenKnown&&screenOff;}
  synchronized double gain(Admission expected,long now){if(!eligible(expected,now)||fadeAt==0||now<fadeAt)return 1;return Math.max(0,Math.min(1,1-(now-fadeAt)/(double)FADE));}
  synchronized String status(Admission expected,long now){if(!policy.issue.isEmpty()&&(expected==null||!expected.external))return policy.issue;if(!policy.any())return "Stillness fade off for this recording";if(!eligible(expected,now))return closed?"Stillness fade recording ended":!active?"Stillness fade paused":reason;return reason;}
  synchronized void pause(){active=false;reset("Tracking paused; estimate reset");}
  synchronized void resume(){if(closed)return;active=true;notBefore=SystemClock.elapsedRealtime();readScreen();reset("Waiting for 10 fresh stillness minutes after resume");}
  synchronized void close(){if(closed)return;closed=true;active=false;ticket=null;reset("Tracking ended");if(registered){registered=false;try{context.unregisterReceiver(screen);}catch(RuntimeException ignored){}}}
 }
 private StillnessSoundFade(){}
}
