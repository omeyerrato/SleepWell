package org.sleepwell.app;

import android.content.Context;
import android.os.*;
import java.lang.ref.WeakReference;
import java.util.concurrent.*;

/** One retained operation, pinned database factory/application context, no Activity in work. */
final class SoundLabelJob implements AutoCloseable {
    interface Work {void load(SoundLabels.Snapshot request)throws Exception;void save(SoundLabels.Snapshot request,String category)throws Exception;}
    interface Factory {Work create(Context app,Records.Factory records);}
    static Factory factory=(app,records)->new Work(){public void load(SoundLabels.Snapshot request){try(Store store=records.open(app)){SoundLabels.check(store,request);}}public void save(SoundLabels.Snapshot request,String category){try(Store store=records.open(app)){SoundLabels.save(store,request,category);}}};
    static final class State {final boolean busy,loaded,saved;final String error;State(boolean busy,boolean loaded,boolean saved,String error){this.busy=busy;this.loaded=loaded;this.saved=saved;this.error=error;}}
    interface Observer {void changed(State state);}
    final SoundLabels.Snapshot request;private final Work work;private final ExecutorService worker=Executors.newSingleThreadExecutor();private final Handler main=new Handler(Looper.getMainLooper());private WeakReference<Observer> observer=new WeakReference<>(null);private State state=new State(false,false,false,"");private boolean closed;
    SoundLabelJob(Context context,Records.Factory records,SoundLabels.Snapshot request){this.request=request;work=factory.create(context.getApplicationContext(),records);}
    void observe(Observer value){observer=new WeakReference<>(value);value.changed(state);}
    void detach(Observer value){if(observer.get()==value)observer.clear();}
    void load(){start(false,null);}
    void save(String category){if(state.loaded)start(true,category);}
    private void start(boolean saving,String category){if(closed||state.busy||state.saved)return;state=new State(true,state.loaded,false,"");notifyObserver();worker.execute(()->{String error="";try{if(saving)work.save(request,category);else work.load(request);}catch(Exception failure){error=failure instanceof IllegalArgumentException&&failure.getMessage()!=null?failure.getMessage():saving?"Label could not be saved. Try again.":"Sound event could not be loaded. Try again.";}String message=error;main.post(()->{if(closed)return;state=new State(false,message.isEmpty()||saving,message.isEmpty()&&saving,message);notifyObserver();});});}
    State state(){return state;}
    private void notifyObserver(){Observer value=observer.get();if(value!=null)value.changed(state);}
    boolean awaitClosed(long timeout)throws InterruptedException{return worker.awaitTermination(timeout,TimeUnit.MILLISECONDS);}
    public void close(){closed=true;observer.clear();worker.shutdown();}
}
