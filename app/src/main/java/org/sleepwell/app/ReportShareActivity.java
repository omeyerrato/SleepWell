package org.sleepwell.app;

import android.app.Activity;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

/** Private Report → preview → explicit Android chooser. Public GET_CONTENT authentication stays separate. */
public final class ReportShareActivity extends Activity implements ReportShareJob.Observer {
    private final SensitiveReviewTouches touches=new SensitiveReviewTouches();private ReportShareJob job;private boolean resumed,chooserLaunched;
    private LinearLayout body;private ScrollView scroll;private int position,revision;
    @Override public boolean dispatchTouchEvent(android.view.MotionEvent event){return touches.dispatch(this,event,super::dispatchTouchEvent);}
    @Override public void onCreate(Bundle state){super.onCreate(state);RecordUi.review(this);long record=0;try{Intent input=getIntent();if(input!=null&&input.getData()==null&&input.getClipData()==null&&input.getSelector()==null)record=input.getLongExtra("record",0);}catch(RuntimeException invalid){}if(state!=null)position=state.getInt("shareScroll");Object retained=getLastNonConfigurationInstance();if(retained instanceof ReportShareJob kept&&kept.record==record)job=kept;else{if(retained instanceof ReportShareJob abandoned)abandoned.close();job=new ReportShareJob(getApplicationContext(),record,state!=null&&state.getBoolean("includeNotes"),state!=null);}job.observe(this);}
    @Override protected void onNewIntent(Intent input){super.onNewIntent(input);setIntent(input);if(job!=null){job.cancelShare();job.detach(this);job.close();}finish();}
    @Override public Object onRetainNonConfigurationInstance(){return job;}
    private void remember(){if(scroll!=null)position=scroll.getScrollY();}
    @Override public void changed(ReportShareJob.State state){
        if(isDestroyed())return;
        if(state.phase==ReportShareJob.Phase.VERIFIED){
            if(!resumed||!hasWindowFocus()||isFinishing()||chooserLaunched){job.cancelShare();return;}
            try{Uri uri=job.admit(state);Intent send=new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_STREAM,uri).putExtra(Intent.EXTRA_TITLE,"SleepWell reviewed period").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);send.setClipData(ClipData.newRawUri("Reviewed SleepWell period",uri));Intent chooser=Intent.createChooser(send,"Share this night").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);chooser.setClipData(send.getClipData());chooserLaunched=true;startActivity(chooser);job.chooserOpened(state);return;}catch(RuntimeException unavailable){chooserLaunched=false;job.chooserFailed(state);return;}
        }
        remember();int rendered=++revision;if(body==null){body=Design.page(this,false,null,n->{});scroll=(ScrollView)body.getParent();}else body.removeAllViews();RecordUi.label(this,body);RecordUi.header(body,"Share this night",this::finish);Design.space(body,12);
        Design.text(body,"One saved period · private preview",14,600,Design.INK);Design.space(body,8);Design.text(body,"Review the actual fields below. Share opens Android’s chooser; nothing is sent by opening this page.",14,400,Design.MUTED);Design.space(body,12);
        CheckBox notes=new CheckBox(this);notes.setText("Include notes");notes.setContentDescription("Include notes in shared report");notes.setTextColor(Design.INK);notes.setTextSize(14*Appearance.scale(this));notes.setTypeface(Design.font(this,400));notes.setButtonTintList(android.content.res.ColorStateList.valueOf(NightDialTokens.ACCENT));notes.setMinimumHeight(Design.dp(this,48));notes.setChecked(state.notes);notes.setEnabled(!state.busy());notes.setOnCheckedChangeListener((button,on)->job.prepare(on));body.addView(notes);
        Design.text(body,"Dates, reviewed duration and source are included. Notes start off. Audio, tags, ratings, raw signals, credentials and device/provider IDs are never included.",12,400,Design.MUTED);Design.space(body,14);
        if(state.snapshot!=null){RecordUi.heading(body,"Preview to be shared");TextView preview=Design.text(body,state.snapshot.data.text,14,400,Design.INK);preview.setTextIsSelectable(true);preview.setContentDescription("Actual shared report preview");Design.space(body,12);Design.text(body,"Text document · "+state.snapshot.data.bytes.length+" bytes",12,400,Design.MUTED);}
        if(!state.message.isEmpty()){Design.space(body,8);Design.text(body,state.message,13,500,state.phase==ReportShareJob.Phase.ERROR?NightDialTokens.NEGATIVE:Design.MUTED);}
        Design.space(body,12);Design.text(body,"Temporary read access expires after ten minutes. A chosen app can keep the copy it receives.",12,400,Design.MUTED);LinearLayout footer=RecordUi.footer(body);
        Design.button(footer,state.phase==ReportShareJob.Phase.VERIFYING?"Checking preview…":"Share",true,()->{if(resumed&&hasWindowFocus()&&!chooserLaunched)job.verifyShare();}).setEnabled(state.phase==ReportShareJob.Phase.READY&&state.snapshot!=null&&!chooserLaunched);
        if(state.phase==ReportShareJob.Phase.ERROR)Design.button(footer,"Prepare preview again",false,()->job.prepare(state.notes));Design.button(footer,"Cancel",false,this::finish);
        int top=position;scroll.post(()->{if(!isDestroyed()&&revision==rendered)scroll.scrollTo(0,top);});
    }
    @Override protected void onResume(){super.onResume();resumed=true;chooserLaunched=false;job.checkHistory();changed(job.state());}
    @Override protected void onPause(){remember();resumed=false;job.cancelShare();super.onPause();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused){RecordUi.lightBars(this);if(job!=null)job.checkHistory();}else if(job!=null)job.cancelShare();}
    @Override public void onSaveInstanceState(Bundle state){remember();super.onSaveInstanceState(state);state.putInt("shareScroll",position);state.putBoolean("includeNotes",job.state().notes);}
    @Override protected void onDestroy(){job.detach(this);if(!isChangingConfigurations())job.close();super.onDestroy();}
    void awaitReaders(long millis)throws InterruptedException{job.awaitClosed(millis);}
}
