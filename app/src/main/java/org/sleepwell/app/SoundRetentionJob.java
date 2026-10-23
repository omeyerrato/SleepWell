package org.sleepwell.app;

import android.content.Context;
import android.os.*;
import java.lang.ref.WeakReference;
import java.util.concurrent.*;

/** A retained immutable operation. Worker ownership includes no Activity or mutable global factory. */
final class SoundRetentionJob implements AutoCloseable {
    static final class Policy {final int days;final boolean favorites;Policy(int days,boolean favorites){this.days=days;this.favorites=favorites;}}
    static final class Result {final boolean policySaved,applied;final String message;Result(boolean saved,boolean applied,String message){policySaved=saved;this.applied=applied;this.message=message;}}
    static final class State {final boolean busy;final Policy policy;final Result result;State(boolean busy,Policy policy,Result result){this.busy=busy;this.policy=policy;this.result=result;}}
    interface Observer {void changed(State state);}
    interface Work {Result apply(Policy policy)throws Exception;}
    interface Factory {Work create(Context context,Records.Factory records);}
    static Factory factory=(context,records)->{Context app=context.getApplicationContext();return policy->applyNative(app,records,policy);};
    private final Work work;private final ExecutorService worker=Executors.newSingleThreadExecutor();private final Handler main=new Handler(Looper.getMainLooper());private WeakReference<Observer> observer=new WeakReference<>(null);private State state=new State(false,null,null);private boolean closed;
    SoundRetentionJob(Context context,Records.Factory records){work=factory.create(context.getApplicationContext(),records);}
    void observe(Observer value){observer=new WeakReference<>(value);value.changed(state);}
    void detach(Observer value){if(observer.get()==value)observer.clear();}
    boolean start(Policy policy){if(closed||state.busy)return false;state=new State(true,policy,null);notifyObserver();worker.execute(()->{Result result;try{result=work.apply(policy);}catch(Exception failure){result=new Result(false,false,"Retention could not be saved. Try again.");}Result done=result;main.post(()->{state=new State(false,policy,done);notifyObserver();});});return true;}
    private void notifyObserver(){Observer target=observer.get();if(target!=null)target.changed(state);}
    static Result applyNative(Context context,Records.Factory records,Policy policy){boolean saved=false;try(Store store=records.open(context)){
        if(!Prefs.of(context).edit().putInt("audioRetentionDays",policy.days).putBoolean("keepAudioFavorites",policy.favorites).putString("audioRetentionError","").commit())return new Result(false,false,"Retention could not be saved. Try again.");saved=true;SoundClips.prune(context,store);
        if(!store.rows("SELECT clip FROM pending_clip_deletes").isEmpty())return new Result(true,false,"Retention saved. Some recordings could not be deleted; apply again to retry.");return new Result(true,true,"Retention saved.");
    }catch(Exception failure){return new Result(saved,false,saved?"Retention saved, but cleanup failed. Apply again to retry.":"Retention could not be saved. Try again.");}}
    State state(){return state;}
    boolean awaitClosed(long timeout)throws InterruptedException{return worker.awaitTermination(timeout,TimeUnit.MILLISECONDS);}
    @Override public void close(){closed=true;observer.clear();worker.shutdown();}
}
