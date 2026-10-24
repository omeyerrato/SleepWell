package org.sleepwell.app;

import android.app.*;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.widget.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Compact board02 occurrence actions; no controls are added to relative naps. */
final class AlarmSkipUi {
 static void add(LinearLayout body,Alarm source,Runnable refresh){
  if(source.nap!=null||source.napInvalid)return;Activity activity=(Activity)body.getContext();Alarm expected=Alarm.read(source.json());
  if(expected.skipInvalid){Design.item(body,"attention","Skipped time needs review","Open alarm settings",()->activity.startActivity(RecordUi.intent(activity,AlarmEditorActivity.class).putExtra("alarm",expected.json().toString())));return;}
  boolean future=expected.skip!=null&&expected.skip.at(AlarmSkips.zone.get())>AlarmSkips.clock.getAsLong();
  if(expected.skip!=null){Design.space(body,6);LinearLayout row=Design.row(activity);Design.pad(row,12,8);row.setBackground(Design.shape(activity,Design.SOFT,12));row.setContentDescription("Skipped occurrence for "+expected.label);Design.Icon icon=new Design.Icon(activity,"skip",Design.MUTED);row.addView(icon,new LinearLayout.LayoutParams(Design.dp(activity,24),Design.dp(activity,24)));LinearLayout copy=Design.col(activity);copy.setPadding(Design.dp(activity,12),0,Design.dp(activity,8),0);Design.text(copy,"Skipped",14,600,Design.INK);Design.space(copy,3);Design.text(copy,when(activity,expected.skip.at(AlarmSkips.zone.get())),12,400,Design.MUTED);row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));if(future){LinearLayout action=Design.col(activity);Button undo=Design.button(action,"Undo",false,()->run(activity,()->AlarmSkips.undo(activity,expected),refresh));undo.setText(RecordUi.withIcon(activity,"Undo","undo",Design.INK));undo.setTextSize(14);undo.setMinHeight(Design.dp(activity,48));undo.setMinimumHeight(Design.dp(activity,48));undo.setContentDescription("Undo skip for "+expected.label);LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)undo.getLayoutParams();lp.topMargin=0;row.addView(action);}else{TextView passed=Design.text(activity,"Time passed",12,400,Design.MUTED);row.addView(passed,new LinearLayout.LayoutParams(-2,-2));}body.addView(row,new LinearLayout.LayoutParams(-1,-2));}
  if(expected.enabled&&(expected.skip==null||!future)){Design.space(body,6);LinearLayout row=Design.item(body,"skip","Skip next alarm",null,()->confirm(activity,body,expected,refresh));row.setContentDescription("Skip next alarm for "+expected.label);row.setBackground(Design.touch(activity,Design.SOFT,12));Design.pad(row,12,0);}
  if(!expected.scheduleError.isEmpty()){Design.space(body,8);android.widget.TextView error=Design.text(body,expected.scheduleError,13,500,0xffa93919);error.setContentDescription("Alarm scheduling error for "+expected.label);Design.button(body,"Retry alarm scheduling",false,()->run(activity,()->AlarmSkips.retry(activity,expected),refresh)).setContentDescription("Retry scheduling for "+expected.label);}
 }
 interface Change{Alarm apply();}
 private static void run(Activity activity,Change change,Runnable refresh){try{change.apply();refresh.run();}catch(RuntimeException failure){SleepPicker.message(activity,"Alarm needs attention",failure instanceof AlarmSkips.Changed?failure.getMessage():"Alarm settings need attention. Reopen this alarm before retrying.");refresh.run();}}
 static String when(Activity activity,long at){ZoneId zone=AlarmSkips.zone.get();return Instant.ofEpochMilli(at).atZone(zone).format(DateTimeFormatter.ofPattern("EEE d MMM",Locale.getDefault()))+" · "+RecordUi.clock(activity,at,zone);}
 private static void confirm(Activity activity,LinearLayout owner,Alarm expected,Runnable refresh){
  long chosen=AlarmSkips.next(activity,expected);if(chosen<=0)return;
  Dialog dialog=new Dialog(activity);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);LinearLayout root=Design.col(activity);Design.pad(root,22,22);root.setBackground(Design.shape(activity,Design.CREAM,24));Design.text(root,"Skip this occurrence?",22,600,Design.INK);Design.space(root,16);Design.text(root,when(activity,chosen),21,600,Design.INK);Design.space(root,12);Design.text(root,expected.days.isEmpty()?"This one-time alarm will be turned off. Undo is available before this time.":"Only this local date is skipped. Your weekday schedule stays on.",15,400,Design.MUTED);Design.space(root,18);
  Design.button(root,"Skip alarm",true,()->{dialog.dismiss();run(activity,()->AlarmSkips.skip(activity,expected,chosen),refresh);}).setContentDescription("Confirm skip for "+expected.label);Design.button(root,"Keep alarm",false,dialog::dismiss);
  ScrollView scroll=new ScrollView(activity);scroll.addView(root);dialog.setContentView(scroll);View.OnAttachStateChangeListener listener=new View.OnAttachStateChangeListener(){public void onViewAttachedToWindow(View v){}public void onViewDetachedFromWindow(View v){dialog.dismiss();}};owner.addOnAttachStateChangeListener(listener);dialog.setOnDismissListener(ignored->owner.removeOnAttachStateChangeListener(listener));dialog.show();dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));dialog.getWindow().setLayout(Math.min(activity.getResources().getDisplayMetrics().widthPixels-Design.dp(activity,32),Design.dp(activity,420)),WindowManager.LayoutParams.WRAP_CONTENT);
 }
 private AlarmSkipUi(){}
}
