package org.sleepwell.app;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

/** Private explicit export screen. No encoder/provider allocation until an explicit SAF result. */
public final class AacExportActivity extends Activity implements AacExportJob.Observer {
    private static final int DESTINATION=463;private AacExportJob job;
    static Intent night(Activity owner,long record){return new Intent(owner,AacExportActivity.class).putExtra("record",record);}
    static Intent favorites(Activity owner){return new Intent(owner,AacExportActivity.class).putExtra("favorites",true);}
    @Override public void onCreate(Bundle saved){super.onCreate(saved);RecordUi.review(this);Object retained=getLastNonConfigurationInstance();job=retained instanceof AacExportJob?(AacExportJob)retained:new AacExportJob(getApplicationContext(),Records.factory,getIntent().getLongExtra("record",0),getIntent().getBooleanExtra("favorites",false));job.observe(this);}
    @Override public Object onRetainNonConfigurationInstance(){return job;}
    @Override public void changed(){if(isDestroyed())return;LinearLayout body=Design.page(this,false,null,n->{});RecordUi.label(this,body);RecordUi.header(body,"Export M4A audio",this::finish);Design.space(body,14);Design.text(body,job.favorites?"Favorite recording intervals":"This night’s recording intervals",18,600,Design.INK);Design.space(body,8);Design.text(body,"AAC-LC in M4A. One selected part saves as M4A; multiple parts save as a ZIP with their interval and label metadata. Original WAV files stay unchanged.",14,400,Design.MUTED);Design.space(body,12);
        if(job.plan!=null){long duration=0;for(int n:job.selection)duration+=job.plan.parts.get(n).duration();Design.item(body,"wave","Selected audio",job.selection.size()+" parts · "+RecordUi.duration(duration),null);Design.text(body,"Only effective recorded periods are included. Pauses, missing capture and reviewed awake exclusions remain separate. AAC is lossy and can add brief codec padding.",13,400,Design.MUTED);if(job.plan.withoutAudio>0)Design.text(body,job.plan.withoutAudio+" favorite estimates have no saved audio.",13,400,Design.MUTED);if(job.plan.outside>0)Design.text(body,job.plan.outside+" recordings have no interval in the effective period.",13,400,Design.MUTED);Design.space(body,8);
            if(job.editable()&&!job.plan.parts.isEmpty()){Design.button(body,job.selection.size()==job.plan.parts.size()?"Clear selection":"Select all",false,()->job.selectAll(job.selection.size()!=job.plan.parts.size()));}
            for(int i=0;i<job.plan.parts.size();i++){int index=i;AacArchive.Part part=job.plan.parts.get(i);boolean selected=job.selection.contains(i);LinearLayout row=Design.row(this);row.setMinimumHeight(Design.dp(this,76));Design.pad(row,10,10);row.setBackground(Design.touch(this,selected?Design.SOFT:android.graphics.Color.TRANSPARENT,12));row.addView(new Design.Icon(this,selected?"check":"wave",Design.BROWN),new LinearLayout.LayoutParams(Design.dp(this,24),Design.dp(this,24)));LinearLayout copy=Design.col(this);Design.text(copy,(i+1)+" · "+part.title,15,600,Design.INK);Design.text(copy,RecordUi.date(part.startWall,part.night.zone())+" · "+RecordUi.preciseClock(this,part.startWall,part.night.zone())+" · "+RecordUi.duration(part.duration()),12,400,Design.MUTED);LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-2,1);cp.leftMargin=Design.dp(this,14);row.addView(copy,cp);row.setSelected(selected);row.setFocusable(true);row.setEnabled(job.editable());row.setContentDescription((selected?"Selected ":"Select ")+"audio part "+(i+1));if(job.editable())row.setOnClickListener(v->job.toggle(index));body.addView(row);}
        }
        Design.space(body,14);if(!job.message.isEmpty())Design.text(body,job.message,14,500,Design.BROWN);Design.space(body,8);
        if(job.phase==AacExportJob.Phase.WRITING||job.phase==AacExportJob.Phase.CANCELLING){View cancel=Design.button(body,job.phase==AacExportJob.Phase.CANCELLING?"Closing export…":"Cancel export",false,job::cancelExport);RecordUi.enabled(cancel,job.phase==AacExportJob.Phase.WRITING);}
        else if(job.phase!=AacExportJob.Phase.LOADING&&job.phase!=AacExportJob.Phase.CHOOSING){View save=Design.button(body,job.selection.size()==1?"Choose M4A save location":"Choose ZIP save location",true,this::choose);RecordUi.enabled(save,job.editable()&&!job.selection.isEmpty());Design.button(body,"Refresh recordings",false,job::refresh);}
    }
    private void choose(){if(!hasWindowFocus()||!job.choose())return;try{startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(job.bundle?"application/zip":"audio/mp4").putExtra(Intent.EXTRA_TITLE,job.bundle?"SleepWell-M4A-recordings.zip":"SleepWell-recording.m4a"),DESTINATION);}catch(ActivityNotFoundException|SecurityException error){job.pickerError();}}
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request!=DESTINATION)return;if(result!=RESULT_OK||data==null){job.cancelChoice();return;}try{if(data.getClipData()!=null&&data.getClipData().getItemCount()>1){job.cancelChoice();return;}job.write(data.getData());}catch(RuntimeException invalid){job.cancelChoice();}}
    @Override protected void onDestroy(){job.detach(this);if(!isChangingConfigurations())job.close();super.onDestroy();}
    @Override public void onWindowFocusChanged(boolean focus){super.onWindowFocusChanged(focus);if(focus)RecordUi.lightBars(this);}
}
