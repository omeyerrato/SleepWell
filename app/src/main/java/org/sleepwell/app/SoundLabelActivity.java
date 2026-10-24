package org.sleepwell.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.util.Objects;
import org.json.JSONObject;

/** Private selector: a draft is never a saved correction until Save is pressed. */
public final class SoundLabelActivity extends Activity implements SoundLabelJob.Observer {
    private static final String[] CHOICES={"snoring","speech","cough","baby_cry","laughter","noise"};
    private SoundLabelJob job;private SoundLabelJob.State operation;private String selected;
    static Intent intent(Activity activity,SoundEventRows.Row row,RecordEntry night){if(row.event==null)throw new IllegalArgumentException("Only estimates have labels");return RecordUi.intent(activity,SoundLabelActivity.class).putExtra("soundEvent",row.event.toString()).putExtra("recordUpdated",night.updated).putExtra("recordStart",night.edit.start).putExtra("recordEnd",night.edit.end);}
    @Override public void onCreate(Bundle saved){super.onCreate(saved);RecordUi.review(this);if(android.os.Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::cancel);Object retained=getLastNonConfigurationInstance();try{if(retained instanceof SoundLabelJob)job=(SoundLabelJob)retained;else{String encoded=getIntent().getStringExtra("soundEvent");if(encoded==null||encoded.length()>8192)throw new IllegalArgumentException();JSONObject event=new JSONObject(encoded);if(event.optLong("id")<=0||event.optLong("session")<=0)throw new IllegalArgumentException();job=new SoundLabelJob(getApplicationContext(),Records.factory,new SoundLabels.Snapshot(event,getIntent().getLongExtra("recordUpdated",-1),getIntent().getLongExtra("recordStart",-1),getIntent().getLongExtra("recordEnd",-1)));}selected=saved!=null?saved.getString("selected"):SoundLabels.manual(job.request.event)?SoundLabels.category(job.request.event):null;job.observe(this);if(!job.state().loaded&&!job.state().busy)job.load();}catch(Exception invalid){LinearLayout body=Design.page(this,false,null,n->{});RecordUi.header(body,"Correct label",this::cancel);Design.text(body,"This sound event is unavailable. Reopen the night.",16,400,Design.MUTED);Design.button(body,"Done",true,this::finish);}}
    @Override public Object onRetainNonConfigurationInstance(){return job;}
    @Override public void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putString("selected",selected);}
    public void changed(SoundLabelJob.State state){if(isDestroyed())return;operation=state;if(state.saved){setResult(RESULT_OK);finish();return;}render();}
    private void render(){boolean busy=operation!=null&&operation.busy,loaded=operation!=null&&operation.loaded;LinearLayout body=Design.page(this,false,null,n->{});RecordUi.label(this,body);RecordUi.header(body,"Correct label",this::cancel);Design.space(body,12);if(!loaded){Design.text(body,busy?"Loading sound event…":operation.error.isEmpty()?"Preparing sound event…":operation.error,15,400,Design.MUTED);if(!busy&&!operation.error.isEmpty())Design.button(body,"Try again",true,job::load);return;}
        JSONObject event=job.request.event;LinearLayout original=Design.col(this);Design.pad(original,16,14);original.setBackground(Design.shape(this,Design.SOFT,16));Design.text(original,"Original estimate",12,500,Design.MUTED);Design.space(original,5);Design.text(original,SoundCategories.title(event.optString("category")),19,600,Design.INK);Design.space(original,4);Design.text(original,Math.round(event.optDouble("score")*100)+"% model score · "+RecordUi.duration(event.optLong("duration_ms")),12,400,Design.MUTED);body.addView(original);Design.space(body,14);
        choice(body,null,"Use original estimate",!busy);Design.space(body,6);Design.text(body,"Your label",14,600,Design.INK);Design.space(body,4);for(String category:CHOICES)choice(body,category,SoundLabels.name(category),!busy);Design.space(body,10);Design.text(body,"Your label changes this event only.",12,400,Design.MUTED);if(!operation.error.isEmpty()){Design.space(body,10);Design.text(body,operation.error,14,500,Design.BROWN);}
        LinearLayout footer=RecordUi.footer(body);boolean changed=!Objects.equals(selected,SoundLabels.manual(event)?SoundLabels.category(event):null);View save=Design.button(footer,busy?"Saving…":"Save label",true,()->job.save(selected));RecordUi.enabled(save,!busy&&changed);View cancel=RecordUi.quietButton(footer,"Cancel",this::cancel);RecordUi.enabled(cancel,!busy);
    }
    private void choice(LinearLayout body,String category,String text,boolean enabled){boolean checked=Objects.equals(selected,category);LinearLayout row=Design.row(this);Design.pad(row,12,6);row.setMinimumHeight(Design.dp(this,54));row.setBackground(Design.touch(this,checked?Design.SOFT:Color.TRANSPARENT,12));FrameLayout circle=new FrameLayout(this);GradientDrawable outline=Design.shape(this,Design.CREAM,20);outline.setStroke(Design.dp(this,checked?2:1),checked?Design.BROWN:Design.MUTED);circle.setBackground(outline);if(checked){View dot=new View(this);dot.setBackground(Design.shape(this,Design.BROWN,20));circle.addView(dot,new FrameLayout.LayoutParams(Design.dp(this,12),Design.dp(this,12),Gravity.CENTER));}LinearLayout.LayoutParams radio=new LinearLayout.LayoutParams(Design.dp(this,24),Design.dp(this,24));radio.rightMargin=Design.dp(this,14);row.addView(circle,radio);row.addView(Design.text(this,text,15,checked?600:400,Design.INK),new LinearLayout.LayoutParams(0,-2,1));row.setSelected(checked);row.setFocusable(true);row.setContentDescription("Label: "+text);row.setOnClickListener(v->{selected=category;render();});RecordUi.enabled(row,enabled);body.addView(row);}
    private void cancel(){if(operation==null||!operation.busy)finish();}
    @android.annotation.SuppressLint("GestureBackNavigation") // API30–32 fallback; API33+ callback above.
    @Override public void onBackPressed(){cancel();}
    boolean awaitReaders(long timeout)throws InterruptedException{return job==null||job.awaitClosed(timeout);}
    @Override protected void onDestroy(){if(job!=null){job.detach(this);if(!isChangingConfigurations())job.close();}super.onDestroy();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
}
