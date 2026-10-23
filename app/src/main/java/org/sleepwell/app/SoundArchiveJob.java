package org.sleepwell.app;

import android.content.Context;
import android.net.Uri;
import android.os.*;
import java.io.*;
import java.lang.ref.WeakReference;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

/** Retained export state; native provider access starts only after an explicit document result. */
final class SoundArchiveJob implements AutoCloseable {
    enum Phase {LOADING,READY,CHOOSING,WRITING,DONE,ERROR}
    static final class State {final Phase phase;final NightAudioArchive.Plan plan;final String message;State(Phase phase,NightAudioArchive.Plan plan,String message){this.phase=phase;this.plan=plan;this.message=message;}}
    interface Work {NightAudioArchive.Plan prepare()throws Exception;void write(NightAudioArchive.Plan plan,Uri destination)throws Exception;default void write(NightAudioArchive.Plan plan,Uri destination,BooleanSupplier active)throws Exception{NightAudioArchive.active(active);write(plan,destination);}default void cancel(){}}
    interface Factory {Work create(Context context,Records.Factory records,long id,long updated);}
    interface Observer {void changed(State state);}
    // Provider cancellation can block in Binder or close(), so it must never run on the UI.
    private static final Executor CANCEL=new ThreadPoolExecutor(0,1,10,TimeUnit.SECONDS,new ArrayBlockingQueue<>(1),r->{Thread t=new Thread(r,"audio-export-cancel");t.setDaemon(true);return t;},new ThreadPoolExecutor.DiscardPolicy());
    private static final class NativeWork implements Work {
        final Context app;final Records.Factory records;final long id,updated;final AtomicReference<CancellationSignal> signal=new AtomicReference<>();final AtomicReference<ParcelFileDescriptor> descriptor=new AtomicReference<>();
        NativeWork(Context context,Records.Factory records,long id,long updated){app=context.getApplicationContext();this.records=records;this.id=id;this.updated=updated;}
        public NightAudioArchive.Plan prepare()throws Exception{try(Store store=records.open(app)){return NightAudioArchive.prepare(app,store,id,updated);}}
        public void write(NightAudioArchive.Plan plan,Uri uri)throws Exception{write(plan,uri,()->true);}
        public void write(NightAudioArchive.Plan plan,Uri uri,BooleanSupplier active)throws Exception{
            NightAudioArchive.Snapshot staged;try(Store store=records.open(app)){staged=NightAudioArchive.stage(app,store,plan,active);}
            try(NightAudioArchive.Snapshot snapshot=staged){CancellationSignal cancellation=new CancellationSignal();signal.set(cancellation);try{
                NightAudioArchive.active(active);ParcelFileDescriptor file=app.getContentResolver().openFileDescriptor(uri,"wt",cancellation);if(file==null)throw new IOException("Destination unavailable");descriptor.set(file);
                try(OutputStream output=new ParcelFileDescriptor.AutoCloseOutputStream(file)){NightAudioArchive.active(active);snapshot.write(output,active);}finally{descriptor.compareAndSet(file,null);}
            }finally{signal.compareAndSet(cancellation,null);}}
        }
        public void cancel(){ParcelFileDescriptor file=descriptor.get();CancellationSignal cancellation=signal.get();CANCEL.execute(()->{if(file!=null)try{file.close();}catch(IOException ignored){}if(cancellation!=null)cancellation.cancel();});}
    }
    static Factory factory=NativeWork::new;
    private final Work work;private final Handler main=new Handler(Looper.getMainLooper());private final ExecutorService worker=Executors.newSingleThreadExecutor();private WeakReference<Observer> observer=new WeakReference<>(null);private volatile State state=new State(Phase.LOADING,null,"");private volatile boolean closed;
    SoundArchiveJob(Context context,Records.Factory records,long id,long updated){work=factory.create(context.getApplicationContext(),records,id,updated);load();}
    void observe(Observer value){if(closed)return;observer=new WeakReference<>(value);value.changed(state);}void detach(Observer value){if(observer.get()==value)observer.clear();}
    private void publish(State value){if(closed)return;state=value;Observer target=observer.get();if(target!=null)target.changed(value);}
    private void load(){worker.execute(()->{if(closed)return;try{NightAudioArchive.Plan plan=work.prepare();main.post(()->publish(new State(Phase.READY,plan,plan.audio.isEmpty()?"No saved recordings for this night.":"")));}catch(Exception failure){main.post(()->publish(new State(Phase.ERROR,null,"Recordings could not be prepared. Reopen sound events.")));}});}
    boolean choose(){if(closed||state.plan==null||state.plan.audio.isEmpty()||state.phase!=Phase.READY&&state.phase!=Phase.DONE&&state.phase!=Phase.ERROR)return false;publish(new State(Phase.CHOOSING,state.plan,"Choose a save location in Android."));return true;}
    void cancelChoice(){if(state.phase==Phase.CHOOSING)publish(new State(Phase.READY,state.plan,""));}
    void write(Uri destination){if(closed||state.phase!=Phase.CHOOSING||state.plan==null)return;if(destination==null||!"content".equals(destination.getScheme())){publish(new State(Phase.ERROR,state.plan,"Choose an Android document destination."));return;}NightAudioArchive.Plan plan=state.plan;publish(new State(Phase.WRITING,plan,"Exporting recordings…"));worker.execute(()->{if(closed)return;try{work.write(plan,destination,()->!closed);main.post(()->publish(new State(Phase.DONE,plan,"Export complete.")));}catch(Exception failure){main.post(()->publish(new State(Phase.ERROR,plan,"Export failed. The destination may contain a partial file. Choose a location to retry.")));}});}
    State state(){return state;}boolean awaitClosed(long timeout)throws InterruptedException{return worker.awaitTermination(timeout,TimeUnit.MILLISECONDS);}
    @Override public void close(){if(closed)return;closed=true;observer.clear();try{work.cancel();}finally{worker.shutdownNow();}}
}
