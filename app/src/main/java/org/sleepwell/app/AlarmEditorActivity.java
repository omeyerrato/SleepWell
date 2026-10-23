package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.*;
import org.json.*;
import java.util.*;

/** A draft survives settings navigation and recreation; only Save alarm writes user data. */
public class AlarmEditorActivity extends Activity {
    private Alarm alarm;private String route="edit";private Store store;private Dialog settingsDialog;
    @Override public void onCreate(Bundle state){super.onCreate(state);store=Records.open(this);RecordUi.review(this);
        try{alarm=Alarm.read(new JSONObject(state!=null?state.getString("draft"):getIntent().getStringExtra("alarm")));}
        catch(Exception e){finish();return;}
        route=state!=null?state.getString("route","edit"):getIntent().getStringExtra("route");if(route==null)route="edit";
        if(android.os.Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::back);
        render();
    }
    @Override public void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putString("draft",alarm.json().toString());out.putString("route",route);}
    private void back(){if(!route.equals("edit")){route=route.equals("gentle")?"sound":"edit";render();}else finish();}
    @android.annotation.SuppressLint("GestureBackNavigation") @Override public void onBackPressed(){back();}
    private void go(String next){route=next;render();}
    private void render(){LinearLayout body=Design.page(this,false,null,null);
        switch(route){case "smart":smart(body);break;case "sound":sound(body);break;case "gentle":gentle(body);break;case "snooze":snooze(body);break;case "challenge":challenges(body);break;default:editor(body);}
    }
    private void editor(LinearLayout body){Design.header(body,"Edit alarm",this::back);Design.space(body,28);
        Button time=Design.button(body,displayTime(this,alarm),false,()->new TimePickerDialog(this,(v,h,m)->{alarm.hour=h;alarm.minute=m;render();},alarm.hour,alarm.minute,android.text.format.DateFormat.is24HourFormat(this)).show());
        time.setText(Design.timeText(displayTime(this,alarm)));time.setTextSize(60*Appearance.scale(this));time.setContentDescription("Alarm time "+displayTime(this,alarm));time.setBackground(Design.touch(this,android.graphics.Color.TRANSPARENT,12));time.setMinHeight(Design.dp(this,88));
        Design.space(body,24);LinearLayout days=Design.row(this);String[] labels={"M","T","W","T","F","S","S"},names={"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
        for(int n=1;n<=7;n++){final int day=n;Button b=new Button(this);b.setStateListAnimator(null);b.setElevation(0);b.setText(labels[n-1]);b.setAllCaps(false);b.setTextSize(16*Appearance.scale(this));b.setTypeface(Design.font(this,600));b.setPadding(0,0,0,0);b.setMinWidth(0);b.setMinimumWidth(0);b.setTextColor(alarm.days.contains(n)?Design.CREAM:Design.BROWN);b.setSelected(alarm.days.contains(n));b.setContentDescription(names[n-1]);b.setBackground(Design.touch(this,alarm.days.contains(n)?Design.BROWN:Design.SOFT,28));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,Design.dp(this,48),1);lp.setMargins(Design.dp(this,2),0,Design.dp(this,2),0);days.addView(b,lp);b.setOnClickListener(v->{if(alarm.days.contains(day))alarm.days.remove(day);else alarm.days.add(day);render();});}
        body.addView(days);Design.space(body,24);
        LinearLayout name=Design.item(body,null,"Name",alarm.label,()->{EditText edit=new EditText(this);edit.setText(alarm.label);edit.setSingleLine(true);new AlertDialog.Builder(this).setTitle("Alarm name").setView(edit).setPositiveButton("Done",(d,w)->{alarm.label=edit.getText().toString().trim();if(alarm.label.isEmpty())alarm.label="A fresh start";render();}).setNegativeButton("Cancel",null).show();});Design.pad(name,14,0);name.setBackground(Design.shape(this,Design.SOFT,14));
        Design.space(body,14);Design.line(body);
        Design.item(body,"sun","Smart wake-up",alarm.smartWindow==0?"Off":alarm.smartWindow+" min",()->go("smart"));Design.line(body);
        Design.item(body,"tasks","Challenges",alarm.challenge+" × "+alarm.difficulty,()->go("challenge"));Design.line(body);
        Design.item(body,"sound","Sound",toneName(alarm.tone),()->go("sound"));Design.line(body);
        Design.item(body,"alarm","Snooze",alarm.snoozeMinutes+" min",()->go("snooze"));Design.line(body);
        Design.item(body,"shield","Alarm protection","All alarms",()->startActivity(RecordUi.intent(this,AlarmProtectionActivity.class)));Design.line(body);
        if(store.alarm(alarm.id)!=null){Design.button(body,"Delete alarm",false,()->new AlertDialog.Builder(this).setTitle("Delete this alarm?").setPositiveButton("Delete",(d,w)->{for(String action:new String[]{Scheduler.FIRE,Scheduler.SNOOZE,Scheduler.CHECK,Scheduler.BACKUP})Scheduler.cancel(this,alarm.id,action);store.deleteAlarm(alarm.id);finish();}).setNegativeButton("Keep",null).show());}
        LinearLayout footer=RecordUi.footer(body);Design.toggle(footer,"Alarm on","Rings at your chosen local time",alarm.enabled,on->alarm.enabled=on);Design.space(footer,12);Design.button(footer,"Save alarm",true,this::save);
    }
    private void smart(LinearLayout body){Design.header(body,"Smart wake-up",this::back);
        Design.toggle(body,"Smart wake-up","Uses recorded movement; fixed deadline stays scheduled",alarm.smartWindow>0,on->{alarm.smartWindow=on?30:0;render();});Design.space(body,24);
        Design.text(body,"Wake window",16,600,Design.INK);Design.space(body,8);Design.text(body,alarm.smartWindow+" min before "+displayTime(this,alarm),26,700,Design.INK);Design.space(body,24);
        range(body,"Window length (minutes)",alarm.smartWindow,0,60,n->alarm.smartWindow=n);
        Design.space(body,24);Design.text(body,"Requires active sleep tracking. Two consecutive minutes of sustained movement can trigger an earlier alarm. This is experimental, not a measurement of sleep stage.",15,400,Design.MUTED);
        Design.space(body,24);Design.text(body,"The duration-goal setting is available in Settings → Reliability. Movement sensitivity is currently fixed.",14,400,Design.MUTED);
        Design.button(body,"Use settings",true,()->go("edit"));
    }
    private void challenges(LinearLayout body){Design.header(body,"Challenges",this::back);Design.text(body,"Your task",18,600,Design.INK);Design.space(body,16);
        String[] options={"Math","Memory","Sequence","Retype","Shake","NFC"};
        for(String s:options){LinearLayout r=Design.item(body,"tasks",s,alarm.challenge.equals(s)?"Selected":"",()->{alarm.challenge=s;render();});r.setSelected(alarm.challenge.equals(s));Design.line(body);}
        Design.space(body,16);range(body,"Rounds",alarm.difficulty,1,6,n->alarm.difficulty=n);
        Design.text(body,"Choose a task for this alarm. NFC requires a tag enrolled in Alarms → Alarm options. Unavailable phone sensors fall back to a math challenge.",14,400,Design.MUTED);
        Design.toggle(body,"Offer screen pinning","Uses Android's voluntary pinning controls",alarm.pin,on->alarm.pin=on);
        Design.button(body,"Use challenge",true,()->go("edit"));
    }
    private void sound(LinearLayout body){Design.header(body,"Alarm sound",this::back);
        String[] tones={"daybreak","pulse","system"};for(String t:tones){Design.item(body,"sound",toneName(t),alarm.tone.equals(t)?"Selected":"",()->{alarm.tone=t;render();});Design.line(body);}
        Design.item(body,"sound","Import audio file",alarm.tone.startsWith("content:")?"Selected":"",()->startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("audio/*").addCategory(Intent.CATEGORY_OPENABLE),1));
        Design.space(body,16);range(body,"Alarm volume %",alarm.volume,10,100,n->alarm.volume=n);
        Design.toggle(body,"Extra loud","Use maximum device alarm volume. Keeps your normal volume choice.",alarm.extraLoud,on->alarm.extraLoud=on);Design.space(body,12);
        Design.item(body,"sun","Gentle wake-up",duration(alarm.rampSeconds),()->go("gentle"));Design.line(body);
        Design.space(body,16);Design.text(body,"Quiet test rings in 10 seconds, capped at 25%, without vibration or loud backup. Your draft is not saved by the test.",14,400,Design.MUTED);
        Design.button(body,"Test a quiet alarm",false,()->{try{Scheduler.test(this,alarm);Ui.toast(this,"Quiet test scheduled in 10 seconds");}catch(SecurityException e){Ui.toast(this,e.getMessage());}});
        Design.button(body,"Use "+toneName(alarm.tone),true,()->go("edit"));
    }
    private static String duration(int seconds){return seconds==0?"Off":seconds<60?seconds+" sec":seconds%60==0?seconds/60+" min":seconds/60+" min "+seconds%60+" sec";}
    private void choice(LinearLayout body,String icon,String title,String description,int value,int[] choices,java.util.function.IntConsumer change){PlanActivity.detail(body,icon,title,description,duration(value),()->{if(settingsDialog!=null&&settingsDialog.isShowing()||isFinishing())return;Dialog dialog=new Dialog(this);dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);ScrollView scroll=new ScrollView(this);LinearLayout options=Design.col(this);Design.pad(options,20,20);options.setBackground(Design.shape(this,Design.CREAM,24));scroll.addView(options);dialog.setContentView(scroll);Design.text(options,title,22,600,Design.INK);for(int seconds:choices)Design.button(options,duration(seconds),seconds==value,()->{change.accept(seconds);dialog.dismiss();render();});Design.button(options,"Cancel",false,dialog::dismiss);settingsDialog=dialog;dialog.setOnDismissListener(ignored->{if(settingsDialog==dialog)settingsDialog=null;});dialog.show();dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels-Design.dp(this,32),android.view.ViewGroup.LayoutParams.WRAP_CONTENT);});}
    private void gentle(LinearLayout body){Design.header(body,"Gentle wake-up",()->go("sound"));Design.text(body,"Gradually increase the alarm volume to wake you up more comfortably.",15,400,Design.MUTED);Design.space(body,18);
        choice(body,"sound","Volume ramp","Ramp duration",alarm.rampSeconds,new int[]{0,30,60,120,300,600,1440},n->alarm.rampSeconds=n);
        body.addView(new AlarmRampChart(this,alarm),new LinearLayout.LayoutParams(-1,Design.dp(this,225*Math.max(1,Design.fontScale(this)))));Design.space(body,16);Design.line(body);
        choice(body,"clock","Sound delay","Wait before starting the alarm sound",alarm.soundDelaySeconds,new int[]{0,30,60,120,180,300,600,1440},n->alarm.soundDelaySeconds=n);Design.line(body);Design.space(body,12);
        wakeToggle(body,"vibrate","Vibration","Vibrate from the start of the alarm",alarm.vibrate,on->alarm.vibrate=on);
        wakeToggle(body,"sun","Screen sunrise","Gradually brighten the alarm screen",alarm.sunrise,on->alarm.sunrise=on);
        wakeToggle(body,"flashlight","Flashlight wake-up","Keep the rear light on while the alarm rings",alarm.flashlight,on->alarm.flashlight=on);Design.space(body,12);
        Design.text(body,"Sunrise follows the alarm's start time. The volume ramp follows the sound delay; late playback joins the curve already in progress. Add a delay for a quiet light lead-in. Flashlight availability depends on your phone.",12,400,Design.MUTED);Design.space(body,16);
        Design.fixedAction(body,"Save changes",()->go("sound"));
    }
    private void wakeToggle(LinearLayout body,String icon,String title,String description,boolean value,java.util.function.Consumer<Boolean> change){LinearLayout row=Design.row(this);Design.pad(row,0,12);row.setMinimumHeight(Design.dp(this,70));Design.Icon glyph=new Design.Icon(this,icon,icon.equals("sun")?0xffff8a00:Design.BROWN);LinearLayout.LayoutParams iconLayout=new LinearLayout.LayoutParams(Design.dp(this,26),Design.dp(this,26));iconLayout.rightMargin=Design.dp(this,16);row.addView(glyph,iconLayout);LinearLayout copy=Design.col(this);Design.text(copy,title,15,600,Design.INK);Design.space(copy,5);Design.text(copy,description,12,400,Design.MUTED);row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));Switch toggle=new Switch(this);toggle.setContentDescription(title);Design.styleSwitch(this,toggle,Design.BROWN,0xffbcb5b0);toggle.setChecked(value);toggle.setOnCheckedChangeListener((view,checked)->change.accept(checked));row.addView(toggle);body.addView(row,new LinearLayout.LayoutParams(-1,-2));Design.line(body);}
    private void snooze(LinearLayout body){Design.header(body,"Snooze & backup",this::back);Design.text(body,"Snooze settings",21,600,Design.INK);Design.space(body,16);
        range(body,"Snooze duration (minutes)",alarm.snoozeMinutes,1,15,n->alarm.snoozeMinutes=n);range(body,"Maximum snoozes",alarm.snoozeLimit,0,5,n->alarm.snoozeLimit=n);
        Design.space(body,20);Design.text(body,"Backup alarm",21,600,Design.INK);range(body,"Escalate after (minutes, 0 = off)",alarm.backupMinutes,0,15,n->alarm.backupMinutes=n);
        Design.text(body,"If left ringing, a separate scheduled backup changes the sound to Pulse at 100% of device alarm volume. Quiet tests never escalate.",14,400,Design.MUTED);
        Design.space(body,20);range(body,"Wake-up check (minutes, 0 = off)",alarm.awakeCheck,0,15,n->alarm.awakeCheck=n);
        Design.text(body,"After dismissal, confirm on Tonight before this delay expires or the challenge rings again.",14,400,Design.MUTED);Design.button(body,"Use settings",true,()->go("edit"));
    }
    private void range(LinearLayout p,String title,int value,int min,int max,java.util.function.IntConsumer change){TextView label=Design.text(p,title+" · "+value,15,500,Design.INK);SeekBar seek=new SeekBar(this);seek.setContentDescription(title);seek.setProgressTintList(android.content.res.ColorStateList.valueOf(Design.BROWN));seek.setThumbTintList(android.content.res.ColorStateList.valueOf(Design.BROWN));seek.setMax(max-min);seek.setProgress(value-min);seek.setMinimumHeight(Design.dp(this,48));p.addView(seek,new LinearLayout.LayoutParams(-1,Design.dp(this,56)));seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int n,boolean user){label.setText(title+" · "+(n+min));if(user)change.accept(n+min);}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});}
    private void save(){if(alarm.enabled&&!AlarmScheduling.port.allowed(this)){new AlertDialog.Builder(this).setTitle("Allow exact alarms").setMessage("Android requires Alarms & reminders access to schedule this alarm. Your draft is preserved.").setPositiveButton("Open settings",(d,w)->startActivity(new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName())))).setNegativeButton("Keep editing",null).show();return;}
        if(alarm.challenge.equals("NFC")&&Prefs.text(this,"nfcTag","").isEmpty()){Ui.toast(this,"Enroll an NFC tag in Alarms → Alarm options before using this challenge.");return;}
        // Schedule first so a denied OS request cannot silently enable an unscheduled alarm in storage.
        try{AlarmScheduling.port.schedule(this,alarm);store.save(alarm);finish();}catch(SecurityException e){Ui.toast(this,e.getMessage());}
    }
    static String displayTime(android.content.Context c,Alarm a){return Design.clock(c,a.hour*60+a.minute);}
    static String toneName(String tone){return tone.startsWith("content:")?"My audio":tone.equals("pulse")?"Pulse":tone.equals("system")?"Device alarm":"Daybreak";}
    @Override protected void onActivityResult(int r,int result,Intent data){super.onActivityResult(r,result,data);if(r==1&&result==RESULT_OK&&data!=null&&data.getData()!=null){try{getContentResolver().takePersistableUriPermission(data.getData(),Intent.FLAG_GRANT_READ_URI_PERMISSION);alarm.tone=data.getData().toString();render();}catch(SecurityException e){Ui.toast(this,"Could not retain access to this audio. Choose another file.");}}}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
    @Override protected void onDestroy(){if(settingsDialog!=null)settingsDialog.dismiss();if(store!=null)store.close();super.onDestroy();}
}
