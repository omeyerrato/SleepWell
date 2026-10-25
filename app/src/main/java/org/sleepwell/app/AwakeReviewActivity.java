package org.sleepwell.app;
import android.os.*;import android.view.*;import android.widget.*;
/** Ended phone-night review. Ignoring evidence never edits the original event or manual awake times. */
public final class AwakeReviewActivity extends androidx.activity.ComponentActivity implements AwakeReviewJob.Observer {
 private AwakeReviewJob job;private int page;
 @Override public void onCreate(Bundle state){super.onCreate(state);RecordUi.review(this);Object old=getLastCustomNonConfigurationInstance();job=old instanceof AwakeReviewJob?(AwakeReviewJob)old:new AwakeReviewJob(getApplicationContext(),getIntent().getLongExtra("record",0));if(state!=null)page=state.getInt("page");job.observe(this);}
 @Override public Object onRetainCustomNonConfigurationInstance(){return job;}
 @Override public void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putInt("page",page);}
 public void changed(){if(isDestroyed())return;LinearLayout body=Design.page(this,false,null,n->{});RecordUi.label(this,body);RecordUi.header(body,"Awake estimates",this::finish);Design.space(body,12);if(job.record==null){Design.text(body,job.busy?"Loading…":"This record is unavailable.",16,500,Design.INK);}else{RecordEntry r=job.record;Design.text(body,RecordUi.duration(r.duration()),38,700,Design.INK).setContentDescription("Reviewed duration");Design.text(body,"After awake estimates · "+RecordUi.duration(r.draft(r.edit).awake(AwakeData.UNKNOWN).duration())+" before estimates",13,400,Design.MUTED);Design.space(body,12);Design.text(body,r.awake.summary(),14,400,Design.MUTED);Design.space(body,10);Design.text(body,"Estimates may be wrong. Manual awake edits and tracking pauses stay separate.",12,400,Design.MUTED);Design.space(body,14);
 int size=r.awake.events.size(),from=Math.min(Math.max(0,page)*30,size),to=Math.min(size,from+30);for(int n=from;n<to;n++){AwakeData.Event e=r.awake.events.get(n);LinearLayout row=Design.item(body,"sun",AwakeData.reason(e.reason),RecordUi.clock(this,e.start,r.zone())+" – "+RecordUi.clock(this,e.end,r.zone())+" · "+(e.ignored?"Ignored":"Estimated awake"),null);View control=Design.iconButton(this,e.ignored?"undo":"close",(e.ignored?"Use estimate ":"Ignore estimate ")+e.id,()->job.review(e.id,!e.ignored));RecordUi.enabled(control,!job.busy);row.addView(control,new LinearLayout.LayoutParams(Design.dp(this,48),Design.dp(this,48)));Design.line(body);}
 if(size>30){if(page>0)Design.button(body,"Previous estimates",false,()->{page--;changed();});if(to<size)Design.button(body,"More estimates",false,()->{page++;changed();});}
 if(size>0){View ignore=Design.button(body,"Ignore all estimates",false,()->job.review(0,true));RecordUi.enabled(ignore,!job.busy);View restore=Design.button(body,"Use all estimates",false,()->job.review(0,false));RecordUi.enabled(restore,!job.busy);}
 }LinearLayout footer=RecordUi.footer(body);if(!job.error.isEmpty())Design.text(footer,job.error,13,500,0xffa93919);if(job.busy)Design.text(footer,"Saving / loading…",13,500,Design.MUTED);View reload=Design.button(footer,job.busy?"Please wait…":"Refresh review",true,job::load);RecordUi.enabled(reload,!job.busy);}
 boolean awaitWorkers(long timeout)throws InterruptedException{return job.await(timeout);}
 @Override protected void onDestroy(){job.detach(this);if(!isChangingConfigurations())job.close();super.onDestroy();}
 @Override protected void onPostResume(){super.onPostResume();RecordUi.lightBars(this);}
 @Override public void onWindowFocusChanged(boolean focus){super.onWindowFocusChanged(focus);if(focus)RecordUi.lightBars(this);}
}
