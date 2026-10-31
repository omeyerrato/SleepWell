package org.sleepwell.app;

import android.app.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Compact board02 occurrence actions; no controls are added to relative naps. */
final class AlarmSkipUi {
 static void add(LinearLayout body,Alarm source,Runnable refresh,Controller controller){
  if(source.nap!=null||source.napInvalid)return;Activity activity=(Activity)body.getContext();Alarm expected=Alarm.read(source.json());
  if(expected.skipInvalid){Design.item(body,"attention","Skipped time needs review","Open alarm settings",()->activity.startActivity(RecordUi.intent(activity,AlarmEditorActivity.class).putExtra("alarm",expected.json().toString())));return;}
  boolean future=expected.skip!=null&&expected.skip.at(AlarmSkips.zone.get())>AlarmSkips.clock.getAsLong();
  if(expected.skip!=null){Design.space(body,6);LinearLayout row=Design.row(activity);Design.pad(row,12,8);row.setBackground(Design.shape(activity,Design.SOFT,12));row.setContentDescription("Skipped occurrence for "+expected.label);Design.Icon icon=new Design.Icon(activity,"skip",Design.MUTED);row.addView(icon,new LinearLayout.LayoutParams(Design.dp(activity,24),Design.dp(activity,24)));LinearLayout copy=Design.col(activity);copy.setPadding(Design.dp(activity,12),0,Design.dp(activity,8),0);Design.text(copy,"Skipped",14,600,Design.INK);Design.space(copy,3);Design.text(copy,when(activity,expected.skip.at(AlarmSkips.zone.get())),12,400,Design.MUTED);row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));if(future){LinearLayout action=Design.col(activity);Button undo=Design.button(action,"Undo",false,()->controller.change(()->AlarmSkips.undo(activity,expected)));undo.setText(RecordUi.withIcon(activity,"Undo","undo",Design.INK));undo.setTextSize(14);undo.setMinHeight(Design.dp(activity,48));undo.setMinimumHeight(Design.dp(activity,48));undo.setContentDescription("Undo skip for "+expected.label);LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)undo.getLayoutParams();lp.topMargin=0;row.addView(action);}else{TextView passed=Design.text(activity,"Time passed",12,400,Design.MUTED);row.addView(passed,new LinearLayout.LayoutParams(-2,-2));}body.addView(row,new LinearLayout.LayoutParams(-1,-2));}
  if(expected.enabled&&(expected.skip==null||!future)){Design.space(body,6);LinearLayout row=Design.item(body,"skip","Skip next alarm",null,()->controller.confirm(expected));row.setContentDescription("Skip next alarm for "+expected.label);row.setBackground(Design.touch(activity,Design.SOFT,12));Design.pad(row,12,0);}
  if(!expected.scheduleError.isEmpty()){Design.space(body,8);android.widget.TextView error=Design.text(body,expected.scheduleError,13,500,NightDialTokens.NEGATIVE);error.setContentDescription("Alarm scheduling error for "+expected.label);Design.button(body,"Retry alarm scheduling",false,()->controller.change(()->AlarmSkips.retry(activity,expected))).setContentDescription("Retry scheduling for "+expected.label);}
 }
 interface Change{Alarm apply();}
 static String when(Activity activity,long at){ZoneId zone=AlarmSkips.zone.get();return Instant.ofEpochMilli(at).atZone(zone).format(DateTimeFormatter.ofPattern("EEE d MMM",Locale.getDefault()))+" · "+RecordUi.clock(activity,at,zone);}
 /** One owned review survives root re-render/recreation. Only the existing skip owner commits it. */
 static final class Controller {
  private final Activity activity;private final Runnable refresh;private SleepPicker.StateDialog dialog;private Alarm expected;private long chosen;private Bundle pending;private String problem="";
  Controller(Activity activity,Runnable refresh,Bundle restored){this.activity=activity;this.refresh=refresh;pending=restored;}
  void confirm(Alarm source){if(dialog!=null&&dialog.isShowing()||activity.isFinishing())return;long next=AlarmSkips.next(activity,source);if(next<=0){notice("Review this alarm's schedule before skipping.");return;}expected=Alarm.read(source.json());chosen=next;show(null);}
  Bundle save(){if(dialog==null||!dialog.isShowing())return pending;Bundle state=new Bundle();if(!problem.isEmpty())state.putString("problem",problem);else{if(expected==null)return pending;state.putString("expected",expected.json().toString());state.putLong("chosen",chosen);}state.putBundle("sheet",dialog.onSaveInstanceState());return state;}
  void restore(){if(pending==null||dialog!=null||activity.isFinishing()||activity.isDestroyed())return;Bundle state=pending;pending=null;if(state.containsKey("problem")){notice(state.getString("problem"));return;}try{expected=Alarm.read(new org.json.JSONObject(state.getString("expected")));chosen=state.getLong("chosen");if(chosen<=0)throw new IllegalArgumentException();show(state.getBundle("sheet"));}catch(Exception invalid){expected=null;notice("Reopen Skip next alarm to review its current time.");}}
  private void show(Bundle state){
   Alarm reviewed=expected;long deadline=chosen;SleepPicker.StateDialog owned=new SleepPicker.StateDialog(activity,"alarm-skip","Skip next alarm");dialog=owned;owned.requestWindowFeature(Window.FEATURE_NO_TITLE);LinearLayout root=Design.col(activity);Design.pad(root,22,24);root.setBackground(Design.shape(activity,NightDialTokens.RAISED,24));Design.text(root,"Skip "+reviewed.label+"?",23,600,NightDialTokens.TEXT);Design.space(root,16);Design.text(root,when(activity,deadline),21,500,NightDialTokens.TEXT);Design.space(root,12);Design.text(root,reviewed.days.isEmpty()?"This one-time alarm will be turned off. Undo is available before this time.":"Only this occurrence is skipped. Your repeating schedule stays on. You can undo before this time.",15,400,NightDialTokens.SECONDARY);Design.space(root,18);
   Design.button(root,"Skip alarm",true,()->{owned.dismiss();change(()->AlarmSkips.skip(activity,reviewed,deadline));}).setContentDescription("Confirm skip for "+reviewed.label);Design.button(root,"Keep alarm",false,owned::dismiss);
   ScrollView scroll=new ScrollView(activity);scroll.addView(root);owned.scroll=scroll;owned.setContentView(scroll);owned.setOnDismissListener(ignored->{if(dialog==owned){dialog=null;expected=null;}});SleepPicker.show(activity,owned,380);if(state!=null)owned.onRestoreInstanceState(state);
  }
  void change(Change action){try{action.apply();refresh.run();}catch(RuntimeException failure){refresh.run();notice(failure instanceof AlarmSkips.Changed?failure.getMessage():"Alarm settings need attention. Reopen this alarm before retrying.");}}
  private void notice(String message){
   problem=message==null?"Review this alarm before retrying.":message;SleepPicker.StateDialog owned=new SleepPicker.StateDialog(activity,"alarm-skip-error","Alarm needs attention");dialog=owned;owned.requestWindowFeature(Window.FEATURE_NO_TITLE);LinearLayout root=Design.col(activity);Design.pad(root,22,24);root.setBackground(Design.shape(activity,NightDialTokens.RAISED,24));Design.text(root,"Alarm needs attention",23,600,NightDialTokens.TEXT);Design.space(root,16);Design.text(root,problem,15,400,NightDialTokens.SECONDARY);Design.space(root,18);Design.button(root,"Got it",true,owned::dismiss);ScrollView scroll=new ScrollView(activity);scroll.addView(root);owned.scroll=scroll;owned.setContentView(scroll);owned.setOnDismissListener(ignored->{if(dialog==owned){dialog=null;problem="";expected=null;}});SleepPicker.show(activity,owned,340);
  }
  void close(){pending=null;if(dialog!=null)dialog.dismiss();}
 }
 private AlarmSkipUi(){}
}
