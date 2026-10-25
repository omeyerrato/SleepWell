package org.sleepwell.app;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.os.*;
import android.view.View;
import android.widget.*;

/** Review and save only: this screen never changes volume, starts capture or plays audio. */
public final class ExternalMediaFadeActivity extends Activity {
 private boolean draft,accepted,resumed;private long revision,generation;private String error="";private SaveJob saving;
 private TextView live;private Button stop;private String shownToken="";private final Handler main=new Handler(Looper.getMainLooper());
 /** A retained result mailbox holds no Activity. Completion after destruction cannot redraw it. */
 private static final class SaveJob {final long generation;volatile boolean done;volatile String error="";SaveJob(long generation){this.generation=generation;}synchronized void complete(String issue){if(done)return;error=issue==null?"The save result was unavailable. Reload saved settings before trying again.":issue;done=true;}}
 @Override public void onCreate(Bundle state){super.onCreate(state);RecordUi.review(this);ExternalMediaFade.Selection selected=ExternalMediaFade.selection(this);draft=selected.enabled;revision=selected.revision;error=selected.issue;
  if(state!=null){draft=state.getBoolean("enabled");accepted=state.getBoolean("accepted");revision=state.getLong("revision",revision);error=state.getString("error",error);}
  Object retained=getLastNonConfigurationInstance();if(retained instanceof SaveJob job){saving=job;generation=job.generation;}else if(state!=null&&state.getBoolean("saving")){draft=selected.enabled;revision=selected.revision;accepted=false;error=selected.issue.isEmpty()?"The earlier save was interrupted or finished while this screen was closed. Saved settings have been reloaded; review before saving again.":selected.issue;}
  if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::finish);render();
 }
 private boolean busy(){return saving!=null&&!saving.done;}
 private void render(){boolean busy=busy();LinearLayout body=Design.page(this,false,null,n->{});RecordUi.label(this,body);RecordUi.header(body,"External-player media fade",this::finish);Design.space(body,16);
  Design.text(body,"Fade already-playing music",25,600,Design.INK);Design.space(body,8);Design.text(body,"Controls the device MEDIA stream, including any active player. It does not identify or stop a particular app.",14,400,Design.MUTED);Design.space(body,14);
  Switch enabled=Design.toggle(body,"Use with new recordings","Immediate manual phone-movement recording only",draft,on->{draft=on;if(!on)accepted=false;error="";render();});enabled.setEnabled(!busy&&Build.VERSION.SDK_INT>=33);
  Design.space(body,14);PlanActivity.detail(body,"clock","10 full stillness minutes","Fresh measurements after admission, then a 5 min fade with the screen off",null,null);
  Design.space(body,10);Design.text(body,"Before recording: start your external player, stop SleepWell’s mixer, and turn Play with tracking off. Delayed, automatic, recurring, contactless and batched recordings are excluded.",13,400,Design.MUTED);
  Design.space(body,10);Design.text(body,"Observed playback, route, call, user-volume or configuration changes cancel further reductions. Android does not reveal every audio-focus handover, so cancellation on an unobservable handover cannot be guaranteed.",13,400,Design.MUTED);
  Design.space(body,10);Design.text(body,"Volume is never restored automatically. The player may continue at minimum volume; raise it manually afterward. Stillness is a movement estimate, not proof of sleep or a sleep stage.",14,500,Design.INK);
  if(Build.VERSION.SDK_INT<33){Design.space(body,12);Design.text(body,"Requires Android 13 or newer. Saving Off remains available; no fade can run here.",14,500,Design.BROWN);}
  if(draft){Design.space(body,12);CheckBox consent=new CheckBox(this);consent.setTypeface(Design.font(this,400));consent.setTextSize(14*Appearance.scale(this));consent.setTextColor(Design.INK);consent.setButtonTintList(ColorStateList.valueOf(Design.BROWN));consent.setMinHeight(Design.dp(this,48));consent.setText("I allow lowering device media volume and will raise it manually afterward.");consent.setChecked(accepted);consent.setEnabled(!busy);consent.setOnCheckedChangeListener((button,on)->accepted=on);body.addView(consent);}
  Design.space(body,12);RecordUi.heading(body,"Current recording");live=Design.text(body,ExternalMediaFade.status(this),14,400,Design.MUTED);live.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);shownToken=ExternalMediaFade.token();stop=Design.button(body,"Stop fade for this recording",false,()->{if(!resumed||!hasWindowFocus())return;ExternalMediaFade.cancel(shownToken);refreshStatus();});stop.setVisibility(shownToken.isEmpty()?View.GONE:View.VISIBLE);Design.text(body,"Stop cancels further reductions only; it does not restore volume, stop playback or end tracking.",12,400,Design.MUTED);
  LinearLayout footer=RecordUi.footer(body);Design.text(footer,"Save applies to a new eligible recording. Saving Off or changing the saved revision cancels the current fade; saving never re-arms it.",12,400,Design.MUTED);if(!error.isEmpty()){TextView issue=Design.text(footer,error,13,500,Design.BROWN);issue.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);}if(busy)Design.text(footer,"Saving… Leaving this screen does not cancel the requested save.",13,400,Design.MUTED);
  Button save=Design.button(footer,"Save media fade",true,this::save);save.setEnabled(!busy&&revision>=0&&(Build.VERSION.SDK_INT>=33||!draft));Button reload=Design.button(footer,"Reload saved settings",false,this::reload);reload.setEnabled(!busy);Button cancel=Design.button(footer,"Cancel",false,this::finish);cancel.setEnabled(!busy);
 }
 private void save(){if(!resumed||!hasWindowFocus()||busy())return;if(draft&&!accepted){error="Confirm device media-volume control before saving On.";render();return;}error="";SaveJob job=new SaveJob(++generation);saving=job;try{ExternalMediaFade.save(getApplicationContext(),revision,draft,accepted,job::complete);}catch(RuntimeException unavailable){job.complete("Settings could not be saved. Reload saved settings and try again.");}render();}
 private void reload(){if(busy()||!resumed||!hasWindowFocus())return;ExternalMediaFade.Selection selected=ExternalMediaFade.selection(this);draft=selected.enabled;revision=selected.revision;accepted=false;error=selected.issue;render();}
 private void refreshStatus(){if(live!=null)live.setText(ExternalMediaFade.status(this));String token=ExternalMediaFade.token();if(!token.equals(shownToken)){shownToken=token;if(stop!=null)stop.setVisibility(token.isEmpty()?View.GONE:View.VISIBLE);}}
 private final Runnable poll=new Runnable(){public void run(){if(!resumed||isFinishing()||isDestroyed())return;SaveJob job=saving;if(job!=null&&job.done&&job.generation==generation){error=job.error;saving=null;if(error.isEmpty()){ExternalMediaFade.Selection selected=ExternalMediaFade.selection(ExternalMediaFadeActivity.this);revision=selected.revision;draft=selected.enabled;accepted=false;error=selected.issue.isEmpty()?"Saved for a new eligible recording. The current recording will not be re-armed.":selected.issue;}render();}else refreshStatus();main.postDelayed(this,1000);}};
 @Override protected void onResume(){super.onResume();resumed=true;main.removeCallbacks(poll);poll.run();}
 @Override protected void onPause(){resumed=false;main.removeCallbacks(poll);super.onPause();}
 @Override public Object onRetainNonConfigurationInstance(){return saving;}
 @Override public void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);state.putBoolean("enabled",draft);state.putBoolean("accepted",accepted);state.putLong("revision",revision);state.putString("error",error);state.putBoolean("saving",saving!=null);}
 @Override protected void onDestroy(){resumed=false;++generation;saving=null;main.removeCallbacksAndMessages(null);super.onDestroy();}
 @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
}
