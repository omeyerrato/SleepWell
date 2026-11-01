package org.sleepwell.app;

import android.app.*;
import android.os.Bundle;
import android.widget.*;
import java.time.*;
import java.util.concurrent.*;

/** Review only. A new night always goes through the ordinary tracking setup. */
public final class InterruptedRecordActivity extends Activity {
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private Records.Factory records;private String token,error="";private InterruptedRecords.Receipt receipt;private boolean resumed,keeping;private int generation;private long keepGeneration;private volatile boolean keepDone;private volatile String keepFailure="";
    @Override public void onCreate(Bundle state){super.onCreate(state);RecordUi.review(this);records=Records.factory;token=getIntent().getStringExtra(InterruptedRecords.EXTRA);if(android.os.Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::finish);render();}
    @Override protected void onResume(){super.onResume();resumed=true;if(keeping&&keepDone){applyKeep(keepGeneration);return;}load();}
    @Override protected void onPause(){resumed=false;generation++;super.onPause();}
    private void load(){if(keeping)return;int request=++generation;receipt=null;error="";render();worker.execute(()->{try(Store store=records.open(getApplicationContext())){if(Records.factory!=records)throw InterruptedRecords.unavailable();InterruptedRecords.Receipt found=InterruptedRecords.require(store,token,0);runOnUiThread(()->{if(isDestroyed()||isFinishing()||!resumed||request!=generation)return;if(Records.factory!=records){receipt=null;error=InterruptedRecords.unavailable().getMessage();render();return;}receipt=found;render();});}catch(RuntimeException unavailable){runOnUiThread(()->{if(isDestroyed()||isFinishing()||!resumed||request!=generation)return;error=unavailable.getMessage();receipt=null;render();});}});}
    private void render(){LinearLayout body=Design.page(this,false,null,n->{});RecordUi.header(body,"Interrupted record",this::finish);Design.space(body,16);
        if(receipt==null){Design.text(body,error.isEmpty()?"Checking retained record…":error,15,400,Design.MUTED);if(!error.isEmpty())Design.button(body,"Check again",false,this::load);return;}
        ZoneId zone;try{zone=ZoneId.of(receipt.raw.optString("zone"));}catch(DateTimeException unknown){zone=ZoneId.systemDefault();}
        Design.text(body,"Your saved portion is kept",24,600,Design.INK);Design.space(body,12);
        Design.text(body,"Record "+receipt.id+" · Interrupted",13,500,Design.MUTED);Design.space(body,12);
        PlanActivity.detail(body,"moon","Retained from",RecordUi.date(receipt.raw.optLong("start"),zone),RecordUi.preciseClock(this,receipt.raw.optLong("start"),zone),null);Design.line(body);
        PlanActivity.detail(body,"clock","Last retained time",RecordUi.date(receipt.raw.optLong("end"),zone),RecordUi.preciseClock(this,receipt.raw.optLong("end"),zone),null);Design.line(body);
        Design.space(body,12);Design.text(body,"Recovery detected · "+RecordUi.date(receipt.detected,zone)+" "+RecordUi.preciseClock(this,receipt.detected,zone)+" · "+zone,13,400,Design.MUTED);
        Design.space(body,12);Design.text(body,"The record did not close normally. The exact interruption time and cause were not saved. The last retained time is the final saved checkpoint; missing time has not been filled in.",15,400,Design.MUTED);
        Design.space(body,12);Design.text(body,"Reviewing does not start recording or audio. Starting a new night opens setup and keeps this record; recording begins only after your explicit Start.",14,400,Design.MUTED);
        if(!error.isEmpty()){Design.space(body,10);Design.text(body,error,13,500,NightDialTokens.NEGATIVE);}
        LinearLayout footer=RecordUi.footer(body);Design.button(footer,"Review retained record",true,()->open(0));Design.button(footer,"Start a new night",false,()->open(1));Design.button(footer,keeping?"Keeping…":"Keep retained record",false,this::keep);Design.button(footer,"Review discard",false,()->open(2));if(keeping){RecordUi.enabled(body,false);RecordUi.enabled(footer,false);}
    }
    private void open(int action){if(!resumed||!hasWindowFocus()||keeping||receipt==null)return;try(Store store=records.open(this)){if(Records.factory!=records)throw InterruptedRecords.unavailable();InterruptedRecords.Receipt current=InterruptedRecords.require(store,token,receipt.id);if(action==1){if(TrackingService.running)throw new IllegalStateException("A recording is already active. Return to Tonight to review it.");startActivity(RecordUi.intent(this,TrackingActivity.class).putExtra("record",current.id).putExtra(InterruptedRecords.EXTRA,token));}else startActivity(RecordUi.intent(this,action==0?RecordReportActivity.class:RecordEditorActivity.class).putExtra("record",current.id).putExtra(InterruptedRecords.EXTRA,token).putExtra("reviewRecoveryDiscard",action==2));}catch(RuntimeException unavailable){receipt=null;error=unavailable.getMessage();render();}}
    private void keep(){if(!resumed||!hasWindowFocus()||keeping||receipt==null)return;keeping=true;keepDone=false;keepFailure="";long operation=++keepGeneration;render();worker.execute(()->{try(Store store=records.open(getApplicationContext())){if(Records.factory!=records)throw InterruptedRecords.unavailable();InterruptedRecords.keep(store,token);}catch(RuntimeException unavailable){keepFailure=unavailable.getMessage()==null?"The retained-record choice could not be saved. Check again before retrying.":unavailable.getMessage();}finally{keepDone=true;runOnUiThread(()->applyKeep(operation));}});}
    private void applyKeep(long operation){if(isDestroyed()||isFinishing()||!resumed||operation!=keepGeneration||!keepDone)return;keeping=false;if(Records.factory!=records){receipt=null;error=InterruptedRecords.unavailable().getMessage();render();return;}if(keepFailure.isEmpty()){finish();return;}receipt=null;error=keepFailure;render();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
    boolean awaitReaders(long timeout)throws InterruptedException{return worker.awaitTermination(timeout,TimeUnit.MILLISECONDS);}
    @Override protected void onDestroy(){generation++;keepGeneration++;worker.shutdown();super.onDestroy();}
}
