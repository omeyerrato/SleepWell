package org.sleepwell.app;

import android.content.Context;
import android.net.Uri;
import android.os.*;
import java.io.*;
import java.lang.ref.WeakReference;
import java.util.concurrent.*;

/** Retained export state; native provider access starts only after an explicit document result. */
final class SoundArchiveJob implements AutoCloseable {
    enum Phase {LOADING,READY,CHOOSING,WRITING,DONE,ERROR}
    static final class State {final Phase phase;final NightAudioArchive.Plan plan;final String message;State(Phase phase,NightAudioArchive.Plan plan,String message){this.phase=phase;this.plan=plan;this.message=message;}}
    interface Work {NightAudioArchive.Plan prepare()throws Exception;void write(NightAudioArchive.Plan plan,Uri destination)throws Exception;}
    interface Factory {Work create(Context context,Records.Factory records,long id,long updated);}
    interface Observer {void changed(State state);}
    static Factory factory=(context,records,id,updated)->{Context app=context.getApplicationContext();return new Work(){public NightAudioArchive.Plan prepare()throws Exception{try(Store store=records.open(app)){return NightAudioArchive.prepare(app,store,id,updated);}}public void write(NightAudioArchive.Plan plan,Uri uri)throws Exception{try(Store store=records.open(app);OutputStream output=app.getContentResolver().openOutputStream(uri,"wt")){if(output==null)throw new IOException("Destination unavailable");NightAudioArchive.write(app,store,plan,output);}}};};
    private final Work work;private final Handler main=new Handler(Looper.getMainLooper());private final ExecutorService worker=Executors.newSingleThreadExecutor();private WeakReference<Observer> observer=new WeakReference<>(null);private State state=new State(Phase.LOADING,null,"");private volatile boolean closed;
    SoundArchiveJob(Context context,Records.Factory records,long id,long updated){work=factory.create(context.getApplicationContext(),records,id,updated);load();}
    void observe(Observer value){observer=new WeakReference<>(value);value.changed(state);}void detach(Observer value){if(observer.get()==value)observer.clear();}
    private void publish(State value){if(closed)return;state=value;Observer target=observer.get();if(target!=null)target.changed(value);}
    private void load(){worker.execute(()->{try{NightAudioArchive.Plan plan=work.prepare();main.post(()->publish(new State(Phase.READY,plan,plan.audio.isEmpty()?"No saved recordings for this night.":"")));}catch(Exception failure){String message=failure instanceof IOException?failure.getMessage():"Recordings could not be prepared. Reopen sound events.";main.post(()->publish(new State(Phase.ERROR,null,message==null?"Recordings could not be prepared.":message)));}});}
    boolean choose(){if(closed||state.plan==null||state.plan.audio.isEmpty()||state.phase==Phase.WRITING||state.phase==Phase.CHOOSING)return false;publish(new State(Phase.CHOOSING,state.plan,"Choose a save location in Android."));return true;}
    void cancelChoice(){if(state.phase==Phase.CHOOSING)publish(new State(Phase.READY,state.plan,""));}
    void write(Uri destination){if(closed)return;if(state.phase!=Phase.CHOOSING||state.plan==null){publish(new State(Phase.ERROR,state.plan,"Reopen export and choose a save location again."));return;}if(destination==null||!"content".equals(destination.getScheme())){publish(new State(Phase.ERROR,state.plan,"Choose an Android document destination."));return;}NightAudioArchive.Plan plan=state.plan;publish(new State(Phase.WRITING,plan,"Exporting recordings…"));worker.execute(()->{try{work.write(plan,destination);main.post(()->publish(new State(Phase.DONE,plan,"Export complete.")));}catch(Exception failure){main.post(()->publish(new State(Phase.ERROR,plan,"Export failed. The destination may contain a partial file. Choose a location to retry.")));}});}
    State state(){return state;}boolean awaitClosed(long timeout)throws InterruptedException{return worker.awaitTermination(timeout,TimeUnit.MILLISECONDS);}
    @Override public void close(){closed=true;observer.clear();worker.shutdown();}
}
