package org.sleepwell.app;
import android.content.*;import android.os.*;import java.util.*;import java.util.concurrent.*;import java.util.function.*;
/** Ordered journal operations; callbacks return to the main thread without blocking capture/UI. */
final class TrackingDelayRuntime {
 static final Map<TrackingDelaySettings.Factory,Job> jobs=new IdentityHashMap<>();static synchronized Job get(Context c){TrackingDelaySettings.Factory f=TrackingDelaySettings.factory;return jobs.computeIfAbsent(f,k->new Job(new TrackingDelaySettings(f.open(c.getApplicationContext()))));}
 interface Ready{void ready(TrackingDelay plan);}interface Action{TrackingDelay run();}
 static final class State{final boolean busy;final int minutes;final TrackingDelay plan;final String error;State(boolean busy,int minutes,TrackingDelay plan,String error){this.busy=busy;this.minutes=minutes;this.plan=plan;this.error=error;}}
 static final class Job{final TrackingDelaySettings settings;final ExecutorService worker=Executors.newSingleThreadExecutor();final Handler main=new Handler(Looper.getMainLooper());volatile State state=new State(false,0,null,"");int pending;Job(TrackingDelaySettings settings){this.settings=settings;}
 synchronized void submit(Action action,Ready ready,Consumer<String> failure){pending++;State old=state;state=new State(true,old.minutes,old.plan,old.error);worker.execute(()->{TrackingDelay result=null;String error="";int minutes=state.minutes;TrackingDelay saved=state.plan;try{result=action.run();minutes=settings.minutes();saved=settings.read();}catch(RuntimeException e){error=e.getMessage()==null?"Delayed start needs attention.":e.getMessage();}synchronized(this){pending--;state=new State(pending>0,minutes,saved,error);}TrackingDelay value=result;String problem=error;if(ready!=null||failure!=null)main.post(()->{if(problem.isEmpty()){if(ready!=null)ready.ready(value);}else if(failure!=null)failure.accept(problem);});});}
 void inspect(Predicate<String> live,int boot){submit(()->{TrackingDelay p=settings.read();if(p!=null&&p.pending()&&(p.boot!=boot||!live.test(p.token))){p=p.state(TrackingDelay.Phase.INTERRUPTED,0,"The waiting service ended. Start again explicitly.");settings.save(p);}return p;},null,null);}
 void preference(int minutes){submit(()->{settings.minutes(minutes);return settings.read();},null,null);}
 void arm(TrackingDelay p,Ready ready,Consumer<String> failure){submit(()->{settings.save(p);return p;},ready,failure);}
 void claim(String token,long now,int boot,boolean force,Ready ready,Consumer<String> failure){submit(()->{TrackingDelay p=settings.read();if(p==null||!p.token.equals(token)||p.phase!=TrackingDelay.Phase.WAITING)return null;TrackingDelay next=p.claim(now,boot,force);if(next!=p)settings.save(next);return next;},ready,failure);}
 void finish(String token,TrackingDelay.Phase phase,long id,String issue){submit(()->{TrackingDelay p=settings.read();if(p==null||!p.token.equals(token)||!p.pending())return p;TrackingDelay next=p.state(phase,id,issue);settings.save(next);return next;},null,null);}
 }
 static void drain(TrackingDelaySettings.Factory f)throws Exception{Job job;synchronized(TrackingDelayRuntime.class){job=jobs.get(f);}if(job!=null)job.worker.submit(()->{}).get(10,TimeUnit.SECONDS);}static synchronized void forget(TrackingDelaySettings.Factory f){Job j=jobs.remove(f);if(j!=null){if(j.state.busy)throw new IllegalStateException("Delay work remains");j.worker.shutdown();}}
 private TrackingDelayRuntime(){}
}
