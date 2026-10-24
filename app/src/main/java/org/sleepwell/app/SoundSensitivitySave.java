package org.sleepwell.app;
import android.os.*;import java.lang.ref.WeakReference;import java.util.concurrent.*;
/** One retained immutable save; no Activity, runtime factory relookup, consent or device effect on the worker. */
final class SoundSensitivitySave implements AutoCloseable {
 static final class State {final boolean busy,saved;final SoundSensitivity policy;final String error;State(boolean busy,boolean saved,SoundSensitivity policy,String error){this.busy=busy;this.saved=saved;this.policy=policy;this.error=error;}}
 interface Observer {void changed(State state);}
 final SoundSensitivitySettings.Port port;private final ExecutorService worker=Executors.newSingleThreadExecutor();private final Handler main=new Handler(Looper.getMainLooper());private WeakReference<Observer> observer=new WeakReference<>(null);private State state=new State(false,false,null,"");private boolean closed;
 SoundSensitivitySave(SoundSensitivitySettings.Port port){if(port==null)throw new IllegalArgumentException("Missing sound settings");this.port=port;}
 void observe(Observer target){observer=new WeakReference<>(target);target.changed(state);}
 void detach(Observer target){if(observer.get()==target)observer.clear();}
 boolean start(SoundSensitivity policy){if(policy==null)throw new IllegalArgumentException("Missing sound grouping policy");if(closed||state.busy)return false;state=new State(true,false,policy,"");notifyObserver();worker.execute(()->{String error="";try{port.save(policy);}catch(RuntimeException failed){error="Sound sensitivity could not be saved. Try again.";}String result=error;main.post(()->{state=new State(false,result.isEmpty(),policy,result);notifyObserver();});});return true;}
 private void notifyObserver(){Observer target=observer.get();if(!closed&&target!=null)target.changed(state);}
 State state(){return state;}
 boolean awaitClosed(long millis)throws InterruptedException{return worker.awaitTermination(millis,TimeUnit.MILLISECONDS);}
 @Override public void close(){closed=true;observer.clear();worker.shutdown();}
}
