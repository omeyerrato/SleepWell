package org.sleepwell.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import java.time.*;
import java.util.Locale;

/** Explicit Storage Access Framework destination, never an automatic upload. */
public final class SoundArchiveActivity extends Activity implements SoundArchiveJob.Observer {
    private static final int CREATE_DOCUMENT=401;private SoundArchiveJob job;
    @Override public void onCreate(Bundle saved){super.onCreate(saved);RecordUi.review(this);Object retained=getLastNonConfigurationInstance();job=retained instanceof SoundArchiveJob?(SoundArchiveJob)retained:new SoundArchiveJob(getApplicationContext(),Records.factory,getIntent().getLongExtra("record",0),getIntent().getLongExtra("updated",-1));job.observe(this);}
    @Override public Object onRetainNonConfigurationInstance(){return job;}
    @Override public void changed(SoundArchiveJob.State state){if(isDestroyed())return;LinearLayout body=Design.page(this,false,null,n->{});RecordUi.label(this,body);RecordUi.header(body,"Export recordings",this::finish);Design.space(body,20);if(state.plan!=null){Design.text(body,RecordUi.date(state.plan.record.edit.end,state.plan.record.zone()),18,600,Design.INK);Design.space(body,20);Design.item(body,"wave","Saved recordings",Integer.toString(state.plan.audio.size()),null);Design.item(body,"file","Sound estimates",Integer.toString(state.plan.events.size()),null);Design.item(body,"upload","Audio size",String.format(Locale.getDefault(),"%.1f MB",state.plan.bytes/1048576.0),null);Design.space(body,20);Design.text(body,"The ZIP includes complete recordings and sound estimates. Audio outside edited sleep times may be included.",14,400,Design.MUTED);}Design.space(body,18);if(!state.message.isEmpty())Design.text(body,state.message,15,500,Design.BROWN);boolean ready=state.plan!=null&&!state.plan.audio.isEmpty()&&(state.phase==SoundArchiveJob.Phase.READY||state.phase==SoundArchiveJob.Phase.DONE||state.phase==SoundArchiveJob.Phase.ERROR);View button=Design.button(body,state.phase==SoundArchiveJob.Phase.WRITING?"Exporting…":"Choose save location",true,this::choose);RecordUi.enabled(button,ready);if(state.phase==SoundArchiveJob.Phase.LOADING)Design.text(body,"Preparing recordings…",14,400,Design.MUTED);}
    private void choose(){SoundArchiveJob.State before=job.state();if(!job.choose())return;String day=Instant.ofEpochMilli(before.plan.record.edit.end).atZone(before.plan.record.zone()).toLocalDate().toString();try{startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("application/zip").putExtra(Intent.EXTRA_TITLE,"SleepWell-audio-"+day+".zip"),CREATE_DOCUMENT);}catch(android.content.ActivityNotFoundException missing){job.cancelChoice();SleepPicker.message(this,"No document picker","Install or enable Android Files to choose a save location.");}}
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request!=CREATE_DOCUMENT)return;if(result!=RESULT_OK||data==null||data.getData()==null)job.cancelChoice();else job.write(data.getData());}
    boolean awaitReaders(long timeout)throws InterruptedException{return job.awaitClosed(timeout);}
    @Override protected void onDestroy(){job.detach(this);if(!isChangingConfigurations())job.close();super.onDestroy();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
}
