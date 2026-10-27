package org.sleepwell.app;
import android.content.*;import java.util.*;import java.util.concurrent.*;import java.util.concurrent.atomic.AtomicBoolean;
/** Retained worker owns provider/SQLite; no Activity references or UI-thread waits. */
final class HealthSleepJobs {
 interface Factory {HealthSleepPort create(Context c);}static Factory providers=HealthSleepAndroid::new;interface Preferences {SharedPreferences open(Context c);}static Preferences preferences=Prefs::of;
 static final class Conflict {final String id,origin,payload,fingerprint;final long session;Conflict(String id,String origin,String payload,String fingerprint,long session){this.id=id;this.origin=origin;this.payload=payload;this.fingerprint=fingerprint;this.session=session;}}
 static final class State {final boolean busy;final HealthSleepPort.Access access;final long last;final String message,error;final List<Conflict> conflicts;final int hidden;State(boolean busy,HealthSleepPort.Access access,long last,String message,String error,List<Conflict> conflicts,int hidden){this.busy=busy;this.access=access;this.last=last;this.message=message;this.error=error;this.conflicts=List.copyOf(conflicts);this.hidden=hidden;}}
 static final Map<Records.Factory,Job> jobs=new IdentityHashMap<>();
 static synchronized Job get(Context c,Records.Factory records){Job existing=jobs.get(records);if(existing!=null)return existing;Job created=new Job(c.getApplicationContext(),records,providers.create(c.getApplicationContext()),preferences.open(c));jobs.put(records,created);return created;}
 static void drain(Records.Factory records,long timeout)throws Exception{Job job;synchronized(HealthSleepJobs.class){job=jobs.get(records);}if(job==null)return;long until=System.nanoTime()+TimeUnit.MILLISECONDS.toNanos(timeout);do{long left=until-System.nanoTime();if(left<=0)throw new TimeoutException("Health Connect work remains active.");job.worker.submit(()->{}).get(left,TimeUnit.NANOSECONDS);synchronized(job){if(!job.state.busy&&!job.inspectPending)return;}}while(true);}
 static synchronized void forget(Records.Factory records){Job job=jobs.get(records);if(job==null)return;if(job.state.busy)throw new IllegalStateException("Health Connect work remains active.");jobs.remove(records);job.worker.shutdown();}
 interface Work {String run(HealthSleepStore local)throws Exception;}
 static final class Job {
  final Context app;final Records.Factory records;final HealthSleepPort provider;final SharedPreferences prefs;final AtomicBoolean cancelled=new AtomicBoolean();final ExecutorService worker=Executors.newSingleThreadExecutor(r->new Thread(r,"sleepwell-health-sync"));volatile State state=new State(false,new HealthSleepPort.Access(-1,Set.of()),0,"","",List.of(),0);
  private boolean inspectPending;
  Job(Context app,Records.Factory records,HealthSleepPort provider,SharedPreferences prefs){this.app=app;this.records=records;this.provider=provider;this.prefs=prefs;}
  synchronized void start(Work action,boolean change,boolean inspectAccess){if(state.busy)return;cancelled.set(false);State previous=state;state=new State(true,previous.access,previous.last,previous.message,"",previous.conflicts,previous.hidden);worker.execute(()->{HealthSleepPort.Access access=inspectAccess?new HealthSleepPort.Access(-1,Set.of()):previous.access;long last=previous.last;String message=previous.message,error="";List<Conflict> conflicts=previous.conflicts;int hidden=previous.hidden;
   try(Store store=records.open(app)){HealthSleepStore local=new HealthSleepStore(store);try{if(inspectAccess)access=provider.access();String result=action.run(local);if(change)local.notice(result,"");}catch(Exception failure){error=failure.getMessage()==null?"Health Connect could not complete this operation.":failure.getMessage();try{local.notice(null,error);}catch(Exception status){error+=" Sync status could not be saved.";}}
    message=local.state("last_result");String savedError=local.state("last_error");if(error.isEmpty())error=savedError;String saved=local.state("last_sync");last=saved.isEmpty()?0:Long.parseLong(saved);List<Conflict> rows=new ArrayList<>();for(org.json.JSONObject row:store.rows("SELECT * FROM health_imports WHERE status='conflict' ORDER BY modified DESC")){long id=row.optLong("session");if(local.exists(id))rows.add(new Conflict(row.optString("remote_id"),row.optString("origin"),row.optString("payload"),local.fingerprint(id),id));}conflicts=rows;hidden=store.rows("SELECT remote_id FROM health_imports WHERE status='hidden'").size();
   }catch(Exception failure){error=failure.getMessage()==null?"Health Connect storage is unavailable.":failure.getMessage();}synchronized(Job.this){state=new State(false,access,last,message,error,conflicts,hidden);if(inspectPending){inspectPending=false;inspect();}}});}
  synchronized void inspect(){if(state.busy){inspectPending=true;return;}start(local->null,false,true);}
  void sync(boolean read,boolean write){start(local->new HealthSleepSync(provider,local,app.getPackageName()).run(read,write,System.currentTimeMillis(),cancelled::get),true,true);}
  void resolve(Conflict conflict,boolean useProvider){start(local->{local.resolve(conflict.id,conflict.fingerprint,conflict.payload,useProvider,System.currentTimeMillis());return useProvider?"Provider change applied to the imported copy.":"Local copy kept. Later provider edits will not replace it.";},true,false);}
  void allowHidden(){start(local->{local.allowHidden();return "Hidden imports may return on the next manual sync.";},true,false);}
  void cancel(){cancelled.set(true);}
 }
 private HealthSleepJobs(){}
}
