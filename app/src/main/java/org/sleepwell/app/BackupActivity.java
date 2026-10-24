package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.time.LocalDate;

/** Board 12 supporting flow: explicit document selection and reviewed destructive replacement. */
public final class BackupActivity extends Activity {
    static final int DOCUMENT=620;
    interface Picker {void choose(BackupActivity activity,BackupJob.Kind kind);}
    static Picker picker=(activity,kind)->activity.startActivityForResult(new Intent(kind==BackupJob.Kind.IMPORT?Intent.ACTION_OPEN_DOCUMENT:Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(kind==BackupJob.Kind.CSV?"text/csv":"application/json").putExtra(Intent.EXTRA_TITLE,"sleepwell-"+LocalDate.now()+(kind==BackupJob.Kind.CSV?".csv":".json")),DOCUMENT);
    private Records.Factory records;private BackupJob job;private BackupJob.State drawn;private boolean resumed,pickerOutstanding;private BackupJob.Kind selected;private Dialog confirmation;
    private final Handler main=new Handler(Looper.getMainLooper());private final Runnable observe=new Runnable(){public void run(){if(!resumed)return;BackupJob.State value=job.state();if(value!=drawn)render();main.postDelayed(this,120);}};
    @Override public void onCreate(Bundle saved){super.onCreate(saved);RecordUi.review(this);records=Records.factory;job=BackupJobs.get(this,records);if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::back);
        if(saved!=null){pickerOutstanding=saved.getBoolean("picker");String kind=saved.getString("kind","");try{selected=BackupJob.Kind.valueOf(kind);}catch(IllegalArgumentException ignored){}if(!job.id.equals(saved.getString("job",""))){if(pickerOutstanding&&selected!=null)job.choose(selected);else try{job.interrupted(BackupJob.Phase.valueOf(saved.getString("phase","IDLE")));}catch(IllegalArgumentException ignored){}}}
        render();if(saved==null&&getIntent().hasExtra("action")&&!job.state().busy()){try{BackupJob.Kind kind=BackupJob.Kind.valueOf(getIntent().getStringExtra("action"));main.post(()->choose(kind));}catch(IllegalArgumentException ignored){}}
        if(saved==null&&getIntent().hasExtra("document")&&!job.state().busy()){try{selected=BackupJob.Kind.valueOf(getIntent().getStringExtra("kind"));if(job.choose(selected))job.document(getIntent().getStringExtra("document"));}catch(IllegalArgumentException ignored){}}
    }
    private void render(){drawn=job.state();LinearLayout body=Design.page(this,false,null,n->{});RecordUi.label(this,body);RecordUi.header(body,"Backup",this::back);Design.space(body,12);
        Design.text(body,"Keep a copy of your sleep data",21,600,Design.INK);Design.space(body,8);Design.text(body,"Choose a document on your phone or in a connected storage app.",14,400,Design.MUTED);Design.space(body,24);
        PlanActivity.detail(body,"file","JSON backup","Alarms, sleep records, notes and sound labels",null,null);Design.line(body);PlanActivity.detail(body,"wave","Audio & app settings","Excluded from this backup",null,null);Design.line(body);Design.space(body,22);
        if(drawn.phase==BackupJob.Phase.REVIEW&&drawn.review!=null){LinearLayout review=Design.col(this);Design.pad(review,18,18);review.setBackground(Design.shape(this,Design.SOFT,18));Design.text(review,"Review backup",19,600,Design.INK);Design.space(review,10);Design.text(review,drawn.review.summary,14,400,Design.MUTED);body.addView(review);Design.space(body,18);}
        if(drawn.occupied){ProgressBar busy=new ProgressBar(this);busy.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(Design.BROWN));busy.setContentDescription("Backup operation in progress");body.addView(busy,new LinearLayout.LayoutParams(Design.dp(this,32),Design.dp(this,32)));Design.space(body,10);}
        if(!drawn.message.isEmpty()){TextView status=Design.text(body,drawn.message,14,500,drawn.phase==BackupJob.Phase.ERROR?0xffa93920:Design.MUTED);status.setContentDescription("Backup status");status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);Design.space(body,18);}
        LinearLayout footer=RecordUi.footer(body);
        if(drawn.phase==BackupJob.Phase.REVIEW){Design.button(footer,"Review replacement",true,this::confirm);Design.button(footer,"Keep local data",false,()->{job.cancel();render();});}
        else if(drawn.occupied){Design.text(footer,drawn.phase==BackupJob.Phase.RESTORING?"Your data is being replaced. This step cannot be cancelled.":"You can leave this screen. Progress stays here while the app is running.",12,400,Design.MUTED);if(drawn.phase!=BackupJob.Phase.RESTORING)Design.button(footer,"Cancel operation",false,()->{job.cancel();render();});}
        else if(drawn.phase==BackupJob.Phase.CHOOSING){Design.button(footer,"Cancel selection",false,()->{pickerOutstanding=false;job.choiceCancelled();render();});}
        else {Design.button(footer,"Export backup",true,()->choose(BackupJob.Kind.JSON));Design.button(footer,"Import backup",false,()->choose(BackupJob.Kind.IMPORT));Design.button(footer,"Export records as CSV",false,()->choose(BackupJob.Kind.CSV));}RecordUi.lightBars(this);
    }
    private void choose(BackupJob.Kind kind){if(!job.choose(kind))return;selected=kind;pickerOutstanding=true;render();try{picker.choose(this,kind);}catch(RuntimeException unavailable){pickerOutstanding=false;job.choiceFailed();render();}}
    private void confirm(){BackupJob.State state=job.state();if(state.phase!=BackupJob.Phase.REVIEW||state.review==null||confirmation!=null)return;BackupJob.Review owned=state.review;
        Dialog dialog=new Dialog(this);confirmation=dialog;dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);LinearLayout box=Design.col(this);Design.pad(box,22,22);box.setBackground(Design.shape(this,Design.CREAM,24));Design.text(box,"Replace local data?",22,600,Design.INK);Design.space(box,14);Design.text(box,"This replaces alarms, sleep records, graphs, notes and any included sound labels. Existing audio is queued for deletion after replacement. Imported alarms stay off for review.",15,400,Design.MUTED);Design.space(box,18);
        Design.button(box,"Replace local data",true,()->{dialog.dismiss();if(job.state().phase==BackupJob.Phase.REVIEW&&job.state().review==owned)job.confirm();render();}).setContentDescription("Confirm backup replacement");Design.button(box,"Keep local data",false,dialog::dismiss).setContentDescription("Cancel backup replacement");ScrollView scroll=new ScrollView(this);scroll.addView(box);dialog.setContentView(scroll);dialog.setOnDismissListener(d->confirmation=null);dialog.setOnShowListener(d->{Window window=dialog.getWindow();window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));int light=WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;window.getInsetsController().setSystemBarsAppearance(light,light);window.setNavigationBarContrastEnforced(false);window.setLayout(Math.min(getResources().getDisplayMetrics().widthPixels-Design.dp(this,32),Design.dp(this,420)),-2);});dialog.show();
    }
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request!=DOCUMENT||!pickerOutstanding)return;pickerOutstanding=false;if(result!=RESULT_OK||data==null||data.getData()==null){job.choiceCancelled();render();return;}if(job.state().phase==BackupJob.Phase.IDLE&&selected!=null)job.choose(selected);job.document(data.getData().toString());render();}
    @Override protected void onResume(){super.onResume();resumed=true;main.post(observe);}
    @Override protected void onPause(){resumed=false;main.removeCallbacks(observe);super.onPause();}
    @Override public void onWindowFocusChanged(boolean focus){super.onWindowFocusChanged(focus);if(focus)RecordUi.lightBars(this);}
    private void back(){if(confirmation!=null){confirmation.dismiss();return;}if(job.state().phase==BackupJob.Phase.RESTORING)return;finish();}
    @android.annotation.SuppressLint("GestureBackNavigation") @Override public void onBackPressed(){back();}
    @Override protected void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);state.putString("job",job.id);state.putString("phase",job.state().phase.name());state.putBoolean("picker",pickerOutstanding);if(selected!=null)state.putString("kind",selected.name());}
    View captureView(){return confirmation!=null&&confirmation.isShowing()?confirmation.getWindow().getDecorView():getWindow().getDecorView();}
    boolean captureReady(){Window window=confirmation!=null&&confirmation.isShowing()?confirmation.getWindow():getWindow();int light=WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;return drawn==job.state()&&(window.getInsetsController().getSystemBarsAppearance()&light)==light;}
    @Override protected void onDestroy(){main.removeCallbacksAndMessages(null);if(confirmation!=null)confirmation.dismiss();super.onDestroy();}
}
