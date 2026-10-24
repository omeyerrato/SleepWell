package org.sleepwell.app;

import android.app.Activity;
import android.os.*;
import android.view.View;
import android.widget.*;
import java.time.*;

/** Explicit user admission only. Opening, resuming and recreating this screen never arm output. */
public final class TravelCueReviewActivity extends Activity {
 private final Handler handler=new Handler(Looper.getMainLooper());private final Runnable tick=this::refresh;
 private TravelCuePlan plan;private TravelCues.Manual manual;private TravelCueSession.State state;private String error="",rendered="";private boolean resumed,unavailable;
 @Override public void onCreate(Bundle saved){super.onCreate(saved);RecordUi.review(this);if(saved!=null)error=saved.getString("error","");refresh();}
 @Override protected void onResume(){super.onResume();resumed=true;refresh();}
 @Override protected void onPause(){resumed=false;handler.removeCallbacks(tick);super.onPause();}
 private void refresh(){handler.removeCallbacks(tick);try{long now=TravelReminders.clock.getAsLong();TravelPlans.Repository repository=TravelPlans.repository(this);TravelPlan trip=repository.load(now);plan=new TravelCueStore(repository.prefs).load(trip,now);plan.validateTrip(trip);manual=TravelCues.control.manual();state=TravelCues.control.status(this);unavailable=false;TravelCuePlan.Window window=plan.next(now);String windowKey=window==null?"ended":window.start+"/"+window.end+"/"+(window.start-now<=TravelCueSession.MAX_WAIT);String key=windowKey+"/"+plan.revision+"/"+(manual==null?"none":manual.session+manual.token)+"/"+(state==null?"none":state.nonce+state.phase+state.requests+state.skipped+state.message)+"/"+error;if(!key.equals(rendered)){rendered=key;render();}}catch(Exception failure){error=failure.getMessage()==null?"Light-cue status could not be read.":failure.getMessage();String key="unavailable/"+error;plan=null;unavailable=true;if(!key.equals(rendered)){rendered=key;render();}}if(resumed)handler.postDelayed(tick,500);}
 private void render(){LinearLayout body=Design.page(this,false,null,n->{});RecordUi.header(body,"Review light cues",this::finish);Design.space(body,16);
  Design.Icon light=new Design.Icon(this,"flashlight",Design.BROWN);light.setPadding(Design.dp(this,14),Design.dp(this,14),Design.dp(this,14),Design.dp(this,14));light.setBackground(Design.shape(this,Design.SOFT,20));body.addView(light,new LinearLayout.LayoutParams(Design.dp(this,64),Design.dp(this,64)));Design.space(body,16);
  Design.text(body,unavailable?"Status unavailable":state==null?"Not armed":title(state.phase),28,600,Design.INK).setContentDescription("Cue session status");Design.space(body,8);
  long now=TravelReminders.clock.getAsLong();TravelCuePlan.Window next=plan==null?null:plan.next(now);long start=state!=null&&state.busy()?state.start:next==null?0:next.start,end=state!=null&&state.busy()?state.end:next==null?0:next.end;
  if(plan!=null){Design.text(body,"Home time · "+plan.home,13,400,Design.MUTED);Design.space(body,12);if(start>0){LinearLayout window=Design.col(this);Design.pad(window,18,18);window.setBackground(Design.shape(this,Design.SOFT,18));Design.text(window,RecordUi.date(start,ZoneId.of(plan.home)),14,500,Design.INK);Design.space(window,8);Design.text(window,RecordUi.clock(this,start,ZoneId.of(plan.home))+" – "+RecordUi.clock(this,end,ZoneId.of(plan.home)),24,600,Design.INK);Design.space(window,7);Design.text(window,"Destination: "+RecordUi.clock(this,start,ZoneId.of(plan.destination))+" · "+plan.destination,12,400,Design.MUTED);body.addView(window);}else Design.text(body,plan.enabled?"These cue windows have ended.":"Experimental cues are off in Travel mode.",15,500,Design.MUTED);}
  Design.space(body,20);PlanActivity.detail(body,"flashlight","Requested light pulses",null,"2 ms / 15 s",null);Design.line(body);
  if(state!=null){PlanActivity.detail(body,"report","Requests / skipped",null,state.requests+" / "+state.skipped,null);Design.line(body);if(state.phase!=TravelCueSession.Phase.ERROR){Design.space(body,10);Design.text(body,state.message,13,400,Design.MUTED);}}
  Design.space(body,12);Design.text(body,"Actual pulse duration and light dose are not measured. This is experimental, not a proven jet-lag treatment.",13,400,Design.MUTED);
  boolean busy=state!=null&&state.busy();boolean soon=next!=null&&next.start-now<=TravelCueSession.MAX_WAIT;
  LinearLayout footer=RecordUi.footer(body);String visibleError=!error.isEmpty()?error:state!=null&&state.phase==TravelCueSession.Phase.ERROR?state.message:"";TextView issue=Design.text(footer,visibleError,12,500,0xffa93919);issue.setContentDescription("Cue session error");issue.setVisibility(visibleError.isEmpty()?View.GONE:View.VISIBLE);
  if(busy){String displayedNonce=state.nonce;Design.button(footer,"Stop light cues",true,()->{try{TravelCues.control.stop(displayedNonce);error="";rendered="";refresh();}catch(Exception failure){fail(failure);}});}else{
   String reason=manual==null?"A current manual recording is required.":!soon&&next!=null?"Arm within 12 hours of the window.":"Only the remaining window is armed. Missed requests are skipped.";Design.text(footer,reason,12,400,Design.MUTED);
   TravelCues.Manual displayedManual=manual;long displayedRevision=plan==null?-1:plan.revision;Button arm=Design.button(footer,"Arm remaining window",true,()->{try{TravelCues.control.arm(getApplicationContext(),displayedManual,displayedRevision);error="";rendered="";refresh();}catch(Exception failure){fail(failure);}});arm.setEnabled(plan!=null&&plan.enabled&&manual!=null&&soon);
  }Design.button(footer,"Refresh status",false,()->{error="";rendered="";refresh();});
 }
 private void fail(Exception failure){error=failure.getMessage()==null?"Light cues need attention.":failure.getMessage();rendered="";refresh();}
 private static String title(TravelCueSession.Phase phase){switch(phase){case ARMED:return "Window armed";case OPENING:return "Opening flashlight…";case RUNNING:return "Light cues active";case FINISHED:return "Window finished";case STOPPED:return "Light cues stopped";case INTERRUPTED:return "Session interrupted";default:return "Light cues need attention";}}
 @Override protected void onSaveInstanceState(Bundle saved){super.onSaveInstanceState(saved);saved.putString("error",error);}
 @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);super.onDestroy();}
 @Override public void onWindowFocusChanged(boolean focus){super.onWindowFocusChanged(focus);if(focus)RecordUi.lightBars(this);}
}
