package org.sleepwell.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import java.util.concurrent.*;

/** A saved night's report; opening it never starts tracking or audio. */
public final class RecordReportActivity extends Activity {
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private long id;private int generation;
    @Override public void onCreate(Bundle state){super.onCreate(state);RecordUi.review(this);id=getIntent().getLongExtra("record",0);}
    @Override protected void onResume(){super.onResume();load();}
    private void load(){
        int request=++generation;LinearLayout body=Design.page(this,false,null,null);RecordUi.header(body,"Sleep report",this::finish);Design.text(body,"Opening your night…",15,400,Design.MUTED);
        Records.Factory factory=Records.factory;
        worker.execute(()->{try(Store store=factory.open(getApplicationContext())){RecordEntry entry=Records.get(store,id);ReportScreen.Snapshot snapshot=ReportScreen.snapshot(getApplicationContext(),store,entry);runOnUiThread(()->{if(isDestroyed()||request!=generation)return;render(snapshot);});}
            catch(Exception error){runOnUiThread(()->{if(isDestroyed()||request!=generation)return;LinearLayout page=Design.page(this,false,null,null);RecordUi.header(page,"Sleep report",this::finish);Design.space(page,24);Design.text(page,"This sleep record is unavailable.",18,600,Design.INK);Design.space(page,12);Design.button(page,"Try again",true,this::load);});}});
    }
    private void render(ReportScreen.Snapshot snapshot){LinearLayout body=Design.page(this,false,null,null);RecordUi.label(this,body);ReportScreen.render(this,body,snapshot,new ReportScreen.Actions(){
        public void history(){openHistory(false);}public void tonight(){finish();}public void detail(long record){startActivity(RecordUi.intent(RecordReportActivity.this,RecordSignalsActivity.class).putExtra("record",record));}
        public Runnable backAction(){return RecordReportActivity.this::finish;}
    });}
    private void openHistory(boolean trends){startActivity(RecordUi.intent(this,HistoryActivity.class).putExtra("trends",trends));}
    void awaitReaders(long timeoutMillis)throws Exception{
        long started=System.nanoTime(),budget=TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        try{
            // Android can mark the Activity destroyed before onDestroy shuts down its reader.
            if(!isDestroyed()&&!worker.isShutdown()){
                worker.submit(()->{}).get(Math.max(0,budget-(System.nanoTime()-started)),TimeUnit.NANOSECONDS);
                if(!isDestroyed()&&!worker.isShutdown())return;
            }
        }catch(RejectedExecutionException rejected){
            // Destruction may race the admission check; rejection alone never proves quiescence.
            if(!worker.isShutdown())throw rejected;
        }catch(TimeoutException timedOut){
            // shutdownNow may remove a queued barrier. Require actual termination below.
            if(!worker.isShutdown())throw timedOut;
        }
        long remaining=Math.max(0,budget-(System.nanoTime()-started));
        if(!worker.awaitTermination(remaining,TimeUnit.NANOSECONDS))throw new TimeoutException("Report reader still active");
    }
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
    @Override protected void onDestroy(){generation++;worker.shutdownNow();super.onDestroy();}
}
