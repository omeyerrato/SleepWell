package org.sleepwell.app;
import android.content.*;import java.util.*;import java.util.concurrent.*;
/** Small retained choice job; no Activity references or policy/sensor effects. */
final class TrackingFlipJobs {
 static final Map<TrackingFlipSettings.Factory,Job> jobs=new IdentityHashMap<>();static synchronized Job get(Context c){TrackingFlipSettings.Factory f=TrackingFlipSettings.factory;return jobs.computeIfAbsent(f,k->new Job(f.open(c.getApplicationContext())));}
 static final class State{final boolean busy,enabled;final String error;State(boolean busy,boolean enabled,String error){this.busy=busy;this.enabled=enabled;this.error=error;}}
 static final class Job{final SharedPreferences prefs;final ExecutorService worker=Executors.newSingleThreadExecutor();volatile State state=new State(false,false,"");int pending;Job(SharedPreferences p){prefs=p;}synchronized void operation(Boolean value){pending++;state=new State(true,state.enabled,state.error);worker.execute(()->{String error="";boolean enabled=state.enabled;try{if(value!=null&&!prefs.edit().putBoolean("enabled",value).commit())throw new IllegalStateException("Flip pause preference could not be saved.");enabled=prefs.getBoolean("enabled",false);}catch(RuntimeException failure){error="Flip pause settings need attention.";}synchronized(this){pending--;state=new State(pending>0,enabled,error);}});}void inspect(){operation(null);}void save(boolean enabled){operation(enabled);}}
 static void drain(TrackingFlipSettings.Factory f)throws Exception{Job j;synchronized(TrackingFlipJobs.class){j=jobs.get(f);}if(j!=null)j.worker.submit(()->{}).get(10,TimeUnit.SECONDS);}static synchronized void forget(TrackingFlipSettings.Factory f){Job j=jobs.get(f);if(j!=null){if(j.state.busy)throw new IllegalStateException("Flip settings are still saving");jobs.remove(f);j.worker.shutdown();}}
 private TrackingFlipJobs(){}
}
