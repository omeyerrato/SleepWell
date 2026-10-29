package org.sleepwell.app;

import android.app.*;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.*;
import android.view.*;
import android.widget.*;

/** Confirmation observes a real asynchronous snapshot; detached or dismissed controls cannot submit it. */
final class AudioClearUi implements View.OnAttachStateChangeListener,ViewTreeObserver.OnWindowFocusChangeListener,Runnable {
    private final LinearLayout body,row;private final TextView status;private final AudioClearJob job;private final Runnable stopPreview,refresh;
    private final Handler main=new Handler(Looper.getMainLooper());private Dialog dialog;private TextView review;private Button confirm;private CheckBox favorites;private AudioClearJob.State shown,reviewed;private boolean attached,wasBusy;
    static void add(LinearLayout body,Runnable stopPreview,Runnable refresh){new AudioClearUi(body,stopPreview,refresh);}
    private AudioClearUi(LinearLayout body,Runnable stopPreview,Runnable refresh){this.body=body;this.stopPreview=stopPreview;this.refresh=refresh;job=AudioClearJob.get(body.getContext(),Records.factory);
        row=PlanActivity.detail(body,"trash","Delete recordings","Remove local audio. Keep nights and notes.",null,this::open);row.setContentDescription("Delete recordings");row.setMinimumHeight(Design.dp(body.getContext(),72));((Design.Icon)row.getChildAt(0)).tint(0xffd22b20);((TextView)((LinearLayout)row.getChildAt(1)).getChildAt(0)).setTextColor(0xffd22b20);
        View glyph=row.getChildAt(0);glyph.setPadding(Design.dp(body.getContext(),7),Design.dp(body.getContext(),7),Design.dp(body.getContext(),7),Design.dp(body.getContext(),7));glyph.setBackground(Design.shape(body.getContext(),Design.SOFT,12));LinearLayout.LayoutParams size=(LinearLayout.LayoutParams)glyph.getLayoutParams();size.width=size.height=Design.dp(body.getContext(),40);glyph.setLayoutParams(size);((TextView)((LinearLayout)row.getChildAt(1)).getChildAt(0)).setTextSize(16*Appearance.scale(body.getContext()));
        ((TextView)((LinearLayout)row.getChildAt(1)).getChildAt(2)).setTextSize(12*Appearance.scale(body.getContext()));
        status=Design.text(body,"",12,400,Design.MUTED);status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);status.setVisibility(View.GONE);row.addOnAttachStateChangeListener(this);if(row.isAttachedToWindow())onViewAttachedToWindow(row);
    }
    public void onViewAttachedToWindow(View view){attached=true;row.getViewTreeObserver().addOnWindowFocusChangeListener(this);run();}
    public void onViewDetachedFromWindow(View view){attached=false;main.removeCallbacks(this);if(row.getViewTreeObserver().isAlive())row.getViewTreeObserver().removeOnWindowFocusChangeListener(this);if(dialog!=null){dialog.setOnDismissListener(null);dialog.dismiss();dialog=null;}}
    public void onWindowFocusChanged(boolean focus){main.removeCallbacks(this);if(attached&&(focus||dialog!=null))run();}
    public void run(){main.removeCallbacks(this);if(!attached)return;AudioClearJob.State state=job.state();row.setEnabled(!state.busy);row.setAlpha(state.busy?.55f:1f);status.setText(state.message);status.setVisibility(state.message.isEmpty()?View.GONE:View.VISIBLE);
        if(dialog!=null&&dialog.isShowing()&&shown!=state){shown=state;reviewed=state;review.setText(state.message);confirm.setEnabled(!state.busy&&state.plan!=null&&!state.plan.clips.isEmpty());confirm.setText(state.plan!=null&&state.plan.retry?"Retry selected audio cleanup":"Delete selected recordings");favorites.setEnabled(!state.busy&&(state.plan==null||!state.plan.retry));favorites.setVisibility(state.plan!=null&&state.plan.retry?View.GONE:View.VISIBLE);}
        if(wasBusy&&!state.busy&&dialog==null){wasBusy=false;refresh.run();return;}wasBusy=state.busy;if(row.hasWindowFocus()||dialog!=null||state.busy)main.postDelayed(this,150);
    }
    private void open(){if(!attached||job.state().busy||dialog!=null)return;Dialog owned=new Dialog(body.getContext());dialog=owned;owned.requestWindowFeature(Window.FEATURE_NO_TITLE);LinearLayout content=Design.col(body.getContext());Design.pad(content,22,22);content.setBackground(Design.shape(body.getContext(),Design.CREAM,24));Design.text(content,"Delete local recordings?",22,600,Design.INK);Design.space(content,12);Design.text(content,"Only selected saved audio on this phone is removed. Sleep records, notes, alarms and sound estimates remain. Shared files remain while kept recordings reference them. Audio already exported or being exported, backups and shared copies remain where saved.",14,400,Design.MUTED);Design.space(content,12);
        favorites=new CheckBox(body.getContext());favorites.setText("Keep favorites");favorites.setTypeface(Design.font(body.getContext(),500));favorites.setTextSize(14*Appearance.scale(body.getContext()));favorites.setMinHeight(Design.dp(body.getContext(),48));favorites.setChecked(Prefs.of(body.getContext()).getBoolean("keepAudioFavorites",true));favorites.setTextColor(Appearance.color(body.getContext(),Design.INK));content.addView(favorites);review=Design.text(content,"Reading the actual local audio selection…",14,500,Design.BROWN);review.setContentDescription("Audio deletion selection");
        confirm=Design.button(content,"Delete selected recordings",true,()->{AudioClearJob.State selected=reviewed;if(!attached||dialog!=owned||!owned.isShowing()||selected==null||selected!=job.state()||selected.busy||selected.plan==null)return;stopPreview.run();wasBusy=true;job.confirm(selected);owned.dismiss();});confirm.setContentDescription("Confirm recording deletion");confirm.setEnabled(false);Design.button(content,"Keep recordings",false,owned::dismiss).setContentDescription("Keep recordings");
        favorites.setOnCheckedChangeListener((button,keep)->{reviewed=null;favorites.setEnabled(false);confirm.setEnabled(false);job.review(keep);shown=null;main.post(this);});ScrollView scroll=new ScrollView(body.getContext());scroll.addView(content);owned.setContentView(scroll);owned.setOnDismissListener(d->{if(dialog==owned)dialog=null;reviewed=null;shown=null;main.post(this);});owned.show();Window window=owned.getWindow();window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));window.setLayout(Math.min(body.getResources().getDisplayMetrics().widthPixels-Design.dp(body.getContext(),32),Design.dp(body.getContext(),420)),WindowManager.LayoutParams.WRAP_CONTENT);favorites.setEnabled(false);job.review(favorites.isChecked());shown=null;main.post(this);
    }
}
