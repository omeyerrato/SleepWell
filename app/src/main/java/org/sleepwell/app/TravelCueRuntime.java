package org.sleepwell.app;
import android.content.*;
import android.os.*;
import java.time.*;
import java.util.*;
import java.util.function.BooleanSupplier;

/** Owned by one admitted manual recording. No constructor starts tracking or a foreground service. */
final class TravelCueRuntime {
 final String nonce=UUID.randomUUID().toString();final long revision;private final Context context;private final TravelPlans.Repository trip;private final TravelCueStore store;private final BooleanSupplier alive;private final Runnable changed;private final HandlerThread thread=new HandlerThread("SleepWell experimental cues");private final Handler worker;private TravelCueSession session;private volatile TravelCueSession.State state;private volatile boolean closed;
 TravelCueRuntime(Context c,long revision,BooleanSupplier alive,Runnable changed){context=c.getApplicationContext();this.revision=revision;this.alive=alive;this.changed=changed;trip=TravelPlans.repository(context);store=new TravelCueStore(trip.prefs);thread.start();worker=new Handler(thread.getLooper());worker.post(this::start);}
 TravelCueSession.State state(){return state;}
 boolean busy(){TravelCueSession.State value=state;return !closed&&(value==null||value.busy());}
 private boolean current(){if(closed||!alive.getAsBoolean()||Prefs.flag(context,"ringing")||AlarmService.active)return false;try{TravelPlan plan=trip.load(System.currentTimeMillis());TravelCuePlan cues=store.load(plan,System.currentTimeMillis());return cues.enabled&&cues.revision==revision&&cues.tripRevision==plan.revision;}catch(RuntimeException failure){return false;}}
 private void start(){try{store.begin(nonce,revision);if(!current())throw new IllegalStateException("Use the current active manual recording and saved cue window.");long now=System.currentTimeMillis();TravelPlan plan=trip.load(now);TravelCuePlan cues=store.load(plan,now);cues.validateTrip(plan);TravelCuePlan.Window window=cues.next(now);if(window==null)throw new IllegalStateException("These cue windows have ended. Choose another window.");TravelCueSession.Clock clock=new TravelCueSession.Clock(){public long wall(){return System.currentTimeMillis();}public long elapsed(){return SystemClock.elapsedRealtime();}public String zone(){return ZoneId.systemDefault().getId();}};
  TravelCueSession.Scheduler timer=new TravelCueSession.Scheduler(){public void at(long elapsed,Runnable callback){if(!worker.postDelayed(()->safe(callback),Math.max(0,elapsed-SystemClock.elapsedRealtime())))throw new IllegalStateException("The cue timer stopped.");}public void cancel(){worker.removeCallbacksAndMessages(null);}};
  session=new TravelCueSession(nonce,window,clock,timer,events->{PhoneTorches.Request request=PhoneTorches.provider.acquire(context,PhoneTorchCoordinator.Kind.TRAVEL,this::current,new PhoneTorches.Events(){public void ready(PhoneTorches.Handle value){dispatch(()->events.ready(new TravelCueSession.Handle(){public void set(boolean on){value.set(on);}public void close(){value.close();}}),value::close);}public void lost(String reason){dispatch(()->events.lost(reason),()->releaseError(reason));}});return request::close;},value->{store.save(value,revision);state=value;changed.run();if(!value.busy())closeThread();},this::current);session.start();
 }catch(RuntimeException failure){error(failure.getMessage()==null?"Light cues could not start.":failure.getMessage());}}
 private void safe(Runnable callback){try{callback.run();}catch(RuntimeException failure){error("Light cues stopped because their state could not be saved.");}}
 private void dispatch(Runnable action,Runnable rejected){if(!worker.post(()->safe(action)))rejected.run();}
 private void releaseError(String reason){if(!reason.equals("The flashlight could not be released."))return;TravelCueSession.State old=state;if(old==null)return;TravelCueSession.State value=new TravelCueSession.State(nonce,TravelCueSession.Phase.ERROR,old.start,old.end,old.requests,old.skipped,reason);try{store.save(value,revision);state=value;changed.run();}catch(RuntimeException ignored){/* A newer journal owner is preserved. */}}
 private void error(String reason){if(session!=null)try{session.stop(reason);}catch(RuntimeException ignored){}TravelCueSession.State old=state;state=new TravelCueSession.State(nonce,TravelCueSession.Phase.ERROR,old==null?0:old.start,old==null?0:old.end,old==null?0:old.requests,old==null?0:old.skipped,reason.length()>240?"Light cues need attention.":reason);try{store.save(state,revision);}catch(RuntimeException ignored){}changed.run();closeThread();}
 void stop(String reason){closed=true;worker.post(()->{if(session!=null)try{session.stop(reason);}catch(RuntimeException ignored){}changed.run();closeThread();});}
 private void closeThread(){closed=true;worker.postDelayed(thread::quitSafely,3500);}
}
