package org.sleepwell.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.time.*;

/** Separate opt-in. Saving tonight-only settings never enables a repeating schedule. */
public final class RecurringTrackingActivity extends Activity {
    private RecurringTracking.Port owner;private long revision;private boolean enabled,stillness,charging;private int minute;private TextView error;private Dialog picker;private String errorCopy="";
    @Override public void onCreate(Bundle state){super.onCreate(state);RecordUi.review(this);owner=RecurringTracking.port;RecurringTracking.Config config=RecurringTracking.load(owner.preferences(this),SleepPlans.load(this).bedtime);revision=state==null?config.revision:state.getLong("revision");enabled=state==null?config.enabled:state.getBoolean("enabled");stillness=state==null?config.stillness:state.getBoolean("stillness");charging=state==null?config.charging:state.getBoolean("charging");minute=state==null?config.minute:state.getInt("minute");errorCopy=state==null?"":state.getString("error","");render();if(state!=null&&state.getBundle("picker")!=null){openTime();picker.onRestoreInstanceState(state.getBundle("picker"));}}
    @Override protected void onResume(){super.onResume();try{RecurringTracking.recover(this,owner,false);}catch(RuntimeException failure){show(failure.getMessage());}render();}
    @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putString("error",errorCopy);out.putLong("revision",revision);out.putBoolean("enabled",enabled);out.putBoolean("stillness",stillness);out.putBoolean("charging",charging);out.putInt("minute",minute);if(picker!=null&&picker.isShowing())out.putBundle("picker",picker.onSaveInstanceState());}
    @Override protected void onDestroy(){if(picker!=null)picker.dismiss();super.onDestroy();}
    private void render(){LinearLayout body=Design.page(this,false,null,n->{});Design.header(body,"Repeat nightly",this::finish);Design.text(body,"Movement tracking, every night.",16,400,Design.MUTED);Design.space(body,18);
        Design.toggle(body,"Nightly tracking","Start without arming each night.",enabled,value->{enabled=value;render();});Design.space(body,12);Design.line(body);
        long time=LocalDate.now(owner.zone()).atTime(minute/60,minute%60).atZone(owner.zone()).toInstant().toEpochMilli();
        Design.item(body,"clock","Start time",RecordUi.clock(this,time,owner.zone()),this::openTime);Design.line(body);
        Design.space(body,14);RecordUi.segments(body,new String[]{"At this time","After settling"},stillness?1:0,index->{stillness=index==1;render();});Design.space(body,10);
        Design.text(body,stillness?"At your start time, wait for 15 min still with the screen off. This is a phone-idle estimate.":"Begin recording phone movement at the saved time.",14,400,Design.MUTED);Design.space(body,16);
        Design.toggle(body,"Only when charging","If unplugged at the start time, skip that night.",charging,value->charging=value);Design.space(body,16);Design.line(body);
        Design.item(body,"mic","Sound recording","Off for recurring starts",null);Design.line(body);
        Design.item(body,"bell","Notifications",owner.notifications(this)?"Allowed":"Allow to keep Cancel visible",owner.notifications(this)?null:this::notifications);Design.line(body);
        Design.item(body,"clock","Exact timing",owner.exact(this)?"Allowed":"Allow exact alarms",owner.exact(this)?null:this::exact);Design.line(body);
        String schedule=owner.preferences(this).getString("scheduleStatus","");long next=owner.preferences(this).getLong("pendingAt",0);if(next>0&&owner.preferences(this).getBoolean("enabled",false)){Design.space(body,14);Design.text(body,"Next start",13,500,Design.MUTED);Design.text(body,RecordUi.date(next,owner.zone())+" · "+RecordUi.clock(this,next,owner.zone()),16,600,Design.INK);}else if(!schedule.isEmpty()){Design.space(body,12);Design.text(body,schedule,13,400,Design.MUTED);}
        String status=RecurringTracking.status(owner,this);if(!status.isEmpty()){Design.space(body,10);Design.text(body,status,13,400,Design.MUTED);}error=Design.text(body,"",13,500,0xffa93919);error.setVisibility(View.GONE);if(!errorCopy.isEmpty())show(errorCopy);Design.space(body,12);
        Design.item(body,"info","How this works","Limits and missed starts",()->SleepPicker.message(this,"Nightly tracking","Waits up to 4 hours for stillness, then records for at most 12 hours. Use the ongoing notification to cancel tonight. Disabling stops only this schedule’s active recording.\n\nMicrophone and sleep sounds never start here. Stillness cannot prove sleep or a sleep stage.\n\nForce-stopping SleepWell or some battery restrictions can prevent starts. Reopen these settings after changes. Charging is checked only at the start time."));
        LinearLayout footer=RecordUi.footer(body);Design.button(footer,"Save nightly settings",true,this::save);Design.button(footer,"Cancel",false,this::finish);
    }
    private void openTime(){picker=SleepPicker.time(this,"Nightly start time",minute,value->{minute=value;render();});}
    private void save(){try{RecurringTracking.Config saved=RecurringTracking.save(this,owner,new RecurringTracking.Config(revision,enabled,minute,stillness,charging),revision);revision=saved.revision;if(saved.enabled&&owner.preferences(this).getString("pending","").isEmpty()){show("Settings were saved, but the next start could not be scheduled. "+owner.preferences(this).getString("scheduleStatus","Recheck exact timing permission."));return;}setResult(RESULT_OK);finish();}catch(RuntimeException failure){show(failure.getMessage());}}
    private void show(String message){errorCopy=message==null?"Nightly settings could not be saved.":message;if(error==null)return;error.setText(errorCopy);error.setVisibility(View.VISIBLE);error.post(()->error.requestRectangleOnScreen(new android.graphics.Rect(0,0,error.getWidth(),error.getHeight()),true));}
    private void exact(){try{if(Build.VERSION.SDK_INT>=31)startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName())));}catch(RuntimeException failure){show("Open Android app settings to allow exact alarms.");}}
    private void notifications(){if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=android.content.pm.PackageManager.PERMISSION_GRANTED){if(owner.preferences(this).getBoolean("notificationAsked",false)&&!shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)){startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName())));return;}owner.preferences(this).edit().putBoolean("notificationAsked",true).apply();requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},792);return;}try{startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName()));}catch(RuntimeException failure){show("Open Android app settings to allow notifications.");}}
    @Override public void onRequestPermissionsResult(int request,String[] names,int[] grants){super.onRequestPermissionsResult(request,names,grants);if(request==792)render();}
}
