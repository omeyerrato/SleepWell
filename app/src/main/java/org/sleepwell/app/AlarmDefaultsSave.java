package org.sleepwell.app;
import android.os.*;import java.lang.ref.WeakReference;import java.util.concurrent.*;

/** Immutable single-flight settings save, retained across configuration changes; no scheduler/device owners. */
final class AlarmDefaultsSave implements AutoCloseable {
    interface Observer {void changed();}
    final AlarmDefaultsSettings.Port port;private final ExecutorService worker=Executors.newSingleThreadExecutor();private final Handler main=new Handler(Looper.getMainLooper());private WeakReference<Observer> observer=new WeakReference<>(null);boolean busy,saved,reset;String error="";private boolean closed;
    AlarmDefaultsSave(AlarmDefaultsSettings.Port port){this.port=port;}
    void observe(Observer target){observer=new WeakReference<>(target);target.changed();}void detach(){observer.clear();}
    void start(AlarmDefaults value){if(closed||busy)return;busy=true;saved=false;reset=value==null;error="";notifyObserver();worker.execute(()->{String failure="";try{port.save(value);}catch(RuntimeException unavailable){failure="Defaults could not be saved. Try again.";}String message=failure;main.post(()->{busy=false;saved=message.isEmpty();error=message;notifyObserver();});});}
    private void notifyObserver(){Observer target=observer.get();if(!closed&&target!=null)target.changed();}
    boolean awaitClosed(long millis)throws InterruptedException{return worker.awaitTermination(millis,TimeUnit.MILLISECONDS);}
    @Override public void close(){closed=true;observer.clear();worker.shutdown();}
}
