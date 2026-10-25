package org.sleepwell.app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import org.json.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/** One ordered bounded writer per explicitly paired run. Never reads/writes SQLite on the UI callback. */
final class PairMotionWriter {
    private final ThreadPoolExecutor worker=new ThreadPoolExecutor(1,1,0,TimeUnit.MILLISECONDS,new ArrayBlockingQueue<>(256));
    private final Handler main=new Handler(Looper.getMainLooper());private final Consumer<String> error;private final Context application;private final String link;private Store store;private volatile boolean failed;private boolean closing;private int packets,windows;
    PairMotionWriter(Context context,JSONObject header,Consumer<String> error){this.application=context.getApplicationContext();this.error=error;this.link=header.optString("link");offer(()->{store=new Store(application);PairMotionTables.start(store,header);});}
    synchronized void packet(JSONObject row){if(closing||failed)return;offer(()->{if(++packets>10000)throw new IllegalStateException("Paired packet storage limit reached.");PairMotionTables.packet(store,link,row);});}
    synchronized void window(JSONObject row){if(closing||failed)return;offer(()->{if(++windows>10000)throw new IllegalStateException("Paired interval storage limit reached.");PairMotionTables.window(store,link,row);});}
    synchronized void close(long at,String reason){if(closing)return;closing=true;try{worker.execute(()->{try{if(store!=null)PairMotionTables.end(store,link,at,failed?"ERROR":reason);}catch(Exception failure){failure(failure);}finally{try{if(store!=null)store.close();}catch(RuntimeException failure){failure(failure);}store=null;}});}catch(RejectedExecutionException full){failed=true;main.post(()->error.accept("Paired storage is busy; the saved prefix remains. New paired estimates stopped."));/* Existing queued tasks own the handle until the terminal task below. */worker.getQueue().clear();try{worker.execute(()->{try{if(store!=null)store.close();}finally{store=null;}});}catch(RejectedExecutionException ignored){}}worker.shutdown();}
    private void offer(Write task){if(failed||closing)return;try{worker.execute(()->{if(failed)return;try{task.run();}catch(Exception problem){failure(problem);}});}catch(RejectedExecutionException full){failure(new IllegalStateException("Paired storage queue is full."));}}
    private void failure(Exception problem){if(failed)return;failed=true;main.post(()->error.accept("Paired readings could not be saved. The original phone recording continues; saved paired readings remain."));}
    private interface Write{void run()throws Exception;}
}
