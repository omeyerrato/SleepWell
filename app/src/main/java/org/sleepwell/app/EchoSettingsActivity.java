package org.sleepwell.app;

import android.app.Activity;
import android.os.*;
import android.widget.LinearLayout;
import java.util.UUID;

/** Saving opts in only a subsequent explicitly started contactless night. */
public final class EchoSettingsActivity extends Activity {
    private boolean enabled,resumed;private long revision;private String token,error="";private final Handler main=new Handler(Looper.getMainLooper());private final Runnable observe=this::observe;
    @Override public void onCreate(Bundle state){super.onCreate(state);RecordUi.review(this);enabled=state==null?Prefs.flag(this,"sonarPeriodicity"):state.getBoolean("enabled");revision=state==null?Prefs.of(this).getLong("sonarPeriodicityRevision",0):state.getLong("revision");token=state==null?null:state.getString("token");render();}
    private void render(){LinearLayout body=Design.page(this,false,null,n->{});RecordUi.label(this,body);RecordUi.header(body,"Breathing experiment",this::finish);Design.space(body,16);Design.toggle(body,"Analyze periodic echoes","Next manually started contactless night",enabled,value->enabled=value);Design.space(body,20);Design.text(body,"Uses reflected sound already captured by contactless tracking. Raw sound and reflection samples are not saved by this experiment.",14,400,Design.MUTED);Design.space(body,14);Design.text(body,"An unvalidated 60-second estimate of repeating reflections. Other movement can look like breathing. Weak, interrupted or ambiguous signals stay blank.",14,400,Design.MUTED);Design.space(body,18);Design.item(body,"report","Recorded results","Available inside a night’s Recorded signals",null);Design.space(body,16);if(!error.isEmpty())Design.text(body,error,13,500,Design.BROWN);Design.button(RecordUi.footer(body),token==null?"Save settings":"Saving…",true,this::save);if(token!=null)RecordUi.enabled(body,false);}
    private void save(){if(token!=null)return;boolean draft=enabled;long expected=revision;token=UUID.randomUUID().toString();PlanOperations.start(this,token,app->{synchronized(EchoSettingsActivity.class){android.content.SharedPreferences p=Prefs.of(app);if(p.getLong("sonarPeriodicityRevision",0)!=expected||expected==Long.MAX_VALUE)throw new IllegalStateException("Settings changed. Reopen this page.");if(!p.edit().putBoolean("sonarPeriodicity",draft).putLong("sonarPeriodicityRevision",expected+1).commit())throw new IllegalStateException("Settings could not be saved. Try again.");}return null;});render();observe();}
    private void observe(){if(!resumed||token==null)return;PlanOperations.Operation op=PlanOperations.start(this,token,app->{throw new IllegalStateException("Save interrupted. Review your choice again.");});if(!op.done){main.postDelayed(observe,75);return;}PlanOperations.forget(token);token=null;if(op.error==null)finish();else{error=op.error;render();}}
    @Override protected void onResume(){super.onResume();resumed=true;observe();}
    @Override protected void onPause(){resumed=false;main.removeCallbacks(observe);super.onPause();}
    @Override public void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);state.putBoolean("enabled",enabled);state.putLong("revision",revision);state.putString("token",token);}
    @Override protected void onDestroy(){main.removeCallbacksAndMessages(null);super.onDestroy();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
}
