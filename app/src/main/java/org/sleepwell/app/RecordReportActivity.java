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
    private boolean recoveryResumed;private String recoveryToken="";private Records.Factory recoveryScope;private long id,lastLoadedAt;private int generation,scrollPosition;private boolean root,hasSnapshot;private ReportScreen.Snapshot lastGood;private String refreshState="";
    @Override public void onCreate(Bundle state){super.onCreate(state);RecordUi.review(this);recoveryScope=Records.factory;recoveryToken=getIntent().getStringExtra(InterruptedRecords.EXTRA);if(recoveryToken==null)recoveryToken="";id=getIntent().getLongExtra("record",0);root=getIntent().getBooleanExtra("reportRoot",false);if(state!=null)scrollPosition=Math.max(0,state.getInt("reportScroll"));if(getLastNonConfigurationInstance() instanceof Retained prior){lastGood=prior.snapshot;hasSnapshot=prior.loaded;lastLoadedAt=prior.checked;}}
    @Override protected void onResume(){super.onResume();recoveryResumed=true;load();}
    private void load(){
        int request=++generation;refreshState="Updating record…";if(hasSnapshot&&recoveryToken.isEmpty())render(lastGood);else{LinearLayout body=page();RecordUi.header(body,"Your night",root?null:this::finish);ReportNavigation.append(this,body,0,()->{},()->openHistory(false),()->openHistory(true));Design.text(body,"Opening your night…",15,400,Design.MUTED);}
        Records.Factory factory=Records.factory;
        worker.execute(()->{try(Store store=factory.open(getApplicationContext())){ReportScreen.Snapshot snapshot;android.database.sqlite.SQLiteDatabase receiptDb=recoveryToken.isEmpty()?null:store.getReadableDatabase();if(receiptDb!=null)receiptDb.beginTransactionNonExclusive();try{if(!recoveryToken.isEmpty()){if(factory!=recoveryScope||Records.factory!=recoveryScope||root)throw new MissingRecord();try{InterruptedRecords.require(store,recoveryToken,id);}catch(IllegalStateException retired){throw new MissingRecord();}}RecordEntry entry;try{entry=root?Records.latest(store):Records.get(store,id);}catch(IllegalArgumentException unavailable){if(!root&&store.rows("SELECT id FROM sessions WHERE id=?",""+id).isEmpty())throw new MissingRecord();throw unavailable;}snapshot=entry==null?null:ReportScreen.snapshot(getApplicationContext(),store,entry);if(snapshot!=null&&recoveryToken.isEmpty()){try{snapshot.additional=AdditionalSoundStore.read(store,entry.id);}catch(RuntimeException unavailable){snapshot.additionalUnavailable=true;}}if(receiptDb!=null)receiptDb.setTransactionSuccessful();}finally{if(receiptDb!=null)receiptDb.endTransaction();}ReportScreen.Snapshot checkedSnapshot=snapshot;runOnUiThread(()->{if(isDestroyed()||isFinishing()||request!=generation||!recoveryToken.isEmpty()&&!recoveryResumed)return;if(!recoveryToken.isEmpty()&&Records.factory!=recoveryScope){load();return;}lastGood=checkedSnapshot;hasSnapshot=true;lastLoadedAt=System.currentTimeMillis();refreshState="";render(checkedSnapshot);});}
            catch(Exception error){runOnUiThread(()->{if(isDestroyed()||isFinishing()||request!=generation||!recoveryToken.isEmpty()&&!recoveryResumed)return;if(error instanceof MissingRecord||!recoveryToken.isEmpty()){lastGood=null;hasSnapshot=false;}refreshState="Could not refresh this record. Last saved data is shown; checked "+RecordUi.date(lastLoadedAt,java.time.ZoneId.systemDefault())+" "+RecordUi.preciseClock(this,lastLoadedAt,java.time.ZoneId.systemDefault())+".";if(hasSnapshot){render(lastGood);return;}LinearLayout page=page();RecordUi.header(page,"Your night",root?null:this::finish);ReportNavigation.append(this,page,0,()->{},()->openHistory(false),()->openHistory(true));Design.space(page,24);Design.text(page,!recoveryToken.isEmpty()?InterruptedRecords.unavailable().getMessage():error instanceof MissingRecord?"This record no longer exists.":"This sleep record is unavailable.",18,600,Design.INK);Design.space(page,12);Design.button(page,"Try again",true,this::load);});}});
    }
    private static final class MissingRecord extends Exception {}
    private LinearLayout page(){return Design.page(this,false,root?2:null,section->RecordUi.main(this,section));}
    private void render(ReportScreen.Snapshot snapshot){LinearLayout body=page();RecordUi.label(this,body);if(!refreshState.isEmpty()){Design.text(body,refreshState,13,500,refreshState.startsWith("Could")?NightDialTokens.NEGATIVE:Design.MUTED);if(refreshState.startsWith("Could"))RecordUi.quietButton(body,"Retry record",this::load);Design.space(body,12);}ReportScreen.render(this,body,snapshot,new ReportScreen.Actions(){
        public void history(){openHistory(false);}public void tonight(){RecordUi.main(RecordReportActivity.this,0);}public void detail(long record){startActivity(RecordUi.intent(RecordReportActivity.this,RecordSignalsActivity.class).putExtra("record",record));}
        public Runnable backAction(){return root?null:RecordReportActivity.this::finish;}
        public boolean readOnly(){return !recoveryToken.isEmpty();}
    });ScrollView scroll=(ScrollView)body.getParent();scroll.post(()->{if(!isDestroyed()&&scroll.isAttachedToWindow())scroll.scrollTo(0,scrollPosition);});}
    private static final class Retained {final ReportScreen.Snapshot snapshot;final boolean loaded;final long checked;Retained(RecordReportActivity owner){snapshot=owner.lastGood;loaded=owner.hasSnapshot;checked=owner.lastLoadedAt;}}
    @Override public Object onRetainNonConfigurationInstance(){return new Retained(this);}
    private void openHistory(boolean trends){ReportNavigation.open(this,trends?2:1);}
    private void rememberScroll(){View content=findViewById(android.R.id.content);ScrollView scroll=findScroll(content);if(scroll!=null)scrollPosition=scroll.getScrollY();}
    private ScrollView findScroll(View view){if(view instanceof ScrollView scroll)return scroll;if(view instanceof ViewGroup group)for(int n=0;n<group.getChildCount();n++){ScrollView found=findScroll(group.getChildAt(n));if(found!=null)return found;}return null;}
    @Override protected void onPause(){rememberScroll();recoveryResumed=false;generation++;super.onPause();}
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
