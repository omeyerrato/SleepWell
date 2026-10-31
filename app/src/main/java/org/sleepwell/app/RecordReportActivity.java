package org.sleepwell.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.view.*;
import java.util.concurrent.*;

/** A saved night's report; opening it never starts tracking or audio. */
public final class RecordReportActivity extends Activity {
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private long id;private int generation,scrollPosition;private boolean root;
    @Override public void onCreate(Bundle state){super.onCreate(state);RecordUi.review(this);id=getIntent().getLongExtra("record",0);root=getIntent().getBooleanExtra("reportRoot",false);if(state!=null)scrollPosition=Math.max(0,state.getInt("reportScroll"));}
    @Override protected void onResume(){super.onResume();load();}
    private void load(){
        int request=++generation;LinearLayout body=page();RecordUi.header(body,"Your night",root?null:this::finish);ReportNavigation.append(this,body,0,()->{},()->openHistory(false),()->openHistory(true));Design.text(body,"Opening your night…",15,400,Design.MUTED);
        Records.Factory factory=Records.factory;
        worker.execute(()->{try(Store store=factory.open(getApplicationContext())){RecordEntry entry=root?Records.latest(store):Records.get(store,id);ReportScreen.Snapshot snapshot=entry==null?null:ReportScreen.snapshot(getApplicationContext(),store,entry);runOnUiThread(()->{if(isDestroyed()||isFinishing()||request!=generation)return;render(snapshot);});}
            catch(Exception error){runOnUiThread(()->{if(isDestroyed()||isFinishing()||request!=generation)return;LinearLayout page=page();RecordUi.header(page,"Your night",root?null:this::finish);ReportNavigation.append(this,page,0,()->{},()->openHistory(false),()->openHistory(true));Design.space(page,24);Design.text(page,"This sleep record is unavailable.",18,600,Design.INK);Design.space(page,12);Design.button(page,"Try again",true,this::load);});}});
    }
    private LinearLayout page(){return Design.page(this,false,root?2:null,section->RecordUi.main(this,section));}
    private void render(ReportScreen.Snapshot snapshot){LinearLayout body=page();RecordUi.label(this,body);ReportScreen.render(this,body,snapshot,new ReportScreen.Actions(){
        public void history(){openHistory(false);}public void tonight(){RecordUi.main(RecordReportActivity.this,0);}public void detail(long record){startActivity(RecordUi.intent(RecordReportActivity.this,RecordSignalsActivity.class).putExtra("record",record));}
        public Runnable backAction(){return root?null:RecordReportActivity.this::finish;}
    });ScrollView scroll=(ScrollView)body.getParent();scroll.post(()->{if(!isDestroyed()&&scroll.isAttachedToWindow())scroll.scrollTo(0,scrollPosition);});}
    private void openHistory(boolean trends){ReportNavigation.open(this,trends?2:1);}
    private void rememberScroll(){View content=findViewById(android.R.id.content);ScrollView scroll=findScroll(content);if(scroll!=null)scrollPosition=scroll.getScrollY();}
    private ScrollView findScroll(View view){if(view instanceof ScrollView scroll)return scroll;if(view instanceof ViewGroup group)for(int n=0;n<group.getChildCount();n++){ScrollView found=findScroll(group.getChildAt(n));if(found!=null)return found;}return null;}
    @Override protected void onPause(){rememberScroll();generation++;super.onPause();}
    @Override public void onSaveInstanceState(Bundle state){rememberScroll();state.putInt("reportScroll",scrollPosition);super.onSaveInstanceState(state);}
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
