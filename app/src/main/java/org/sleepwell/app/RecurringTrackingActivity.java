package org.sleepwell.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;

/** Separate opt-in. Saving tonight-only settings never enables a repeating schedule. */
public final class RecurringTrackingActivity extends Activity {
    static final String PAGE_EXTRA="trackingOptionsPage";
    private String page="home";private Bundle positions=new Bundle();private ScrollView currentScroll;
    private RecurringTracking.Port owner;private long revision;private boolean enabled,stillness,charging;private int minute;private TextView error;private Dialog picker;private String errorCopy="";
    @Override public void onCreate(Bundle state){super.onCreate(state);if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::back);RecordUi.review(this);owner=RecurringTracking.port;RecurringTracking.Config config=RecurringTracking.load(owner.preferences(this),SleepPlans.load(this).bedtime);revision=state==null?config.revision:state.getLong("revision");enabled=state==null?config.enabled:state.getBoolean("enabled");stillness=state==null?config.stillness:state.getBoolean("stillness");charging=state==null?config.charging:state.getBoolean("charging");minute=state==null?config.minute:state.getInt("minute");errorCopy=state==null?"":state.getString("error","");String requested=state==null?getIntent().getStringExtra(PAGE_EXTRA):state.getString("page");if(java.util.Set.of("home","conditions","reliability","limits").contains(requested==null?"home":requested))page=requested==null?"home":requested;if(state!=null&&state.getBundle("positions")!=null)positions=state.getBundle("positions");render();if(state!=null&&state.getBundle("picker")!=null){openTime();picker.onRestoreInstanceState(state.getBundle("picker"));}}
    @Override protected void onResume(){super.onResume();try{RecurringTracking.recover(this,owner,false);}catch(RuntimeException failure){show(failure.getMessage());}refresh();}
    @Override protected void onSaveInstanceState(Bundle out){rememberPosition();super.onSaveInstanceState(out);out.putString("page",page);out.putBundle("positions",positions);out.putString("error",errorCopy);out.putLong("revision",revision);out.putBoolean("enabled",enabled);out.putBoolean("stillness",stillness);out.putBoolean("charging",charging);out.putInt("minute",minute);if(picker!=null&&picker.isShowing())out.putBundle("picker",picker.onSaveInstanceState());}
    @Override protected void onDestroy(){if(picker!=null)picker.dismiss();super.onDestroy();}
    private void rememberPosition(){if(currentScroll!=null&&currentScroll.isLaidOut())positions.putInt(page,currentScroll.getScrollY());}
    private void refresh(){rememberPosition();render();}
    private void openPage(String destination){rememberPosition();page=destination;render();}
    private void back(){if(!page.equals("home"))openPage("home");else finish();}
    @android.annotation.SuppressLint("GestureBackNavigation") @Override public void onBackPressed(){back();}
    private void render(){
        LinearLayout body=Design.page(this,false,null,n->{});currentScroll=(ScrollView)body.getParent();RecordUi.label(this,body);
        String title=switch(page){case "conditions"->"Nightly conditions";case "reliability"->"Start reliability";case "limits"->"Recording limits";default->"Repeat nightly";};
        RecordUi.header(body,title,this::back);Design.space(body,12);if(!page.equals("home"))TrackingOptionsUi.context(body,"Repeat nightly");
        switch(page){case "conditions"->conditionsPage(body);case "reliability"->reliabilityPage(body);case "limits"->limitsPage(body);default->home(body);}
        LinearLayout footer=RecordUi.footer(body);error=Design.text(footer,"",13,500,NightDialTokens.NEGATIVE);error.setVisibility(View.GONE);if(!errorCopy.isEmpty())show(errorCopy);
        if(!page.equals("home")){Design.text(footer,"Changes apply after Save nightly settings.",12,400,Design.MUTED);Design.button(footer,"Done",true,this::back);}
        else{
            Design.space(footer,8);LinearLayout actions=Design.row(this),cancel=Design.col(this),save=Design.col(this);LinearLayout.LayoutParams cancelSpace=new LinearLayout.LayoutParams(0,-2,1);cancelSpace.rightMargin=Design.dp(this,8);actions.addView(cancel,cancelSpace);actions.addView(save,new LinearLayout.LayoutParams(0,-2,2.6f));footer.addView(actions);Design.button(cancel,"Cancel",false,this::finish);Design.button(save,"Save nightly settings",true,this::save).setTextSize(14*Appearance.scale(this));
        }
        ScrollView owned=currentScroll;int position=positions.getInt(page);owned.post(()->{if(currentScroll==owned)owned.scrollTo(0,position);});
    }
    private void home(LinearLayout body){
        Design.text(body,"Movement tracking, every night.",15,400,Design.MUTED);Design.space(body,18);
        Design.toggle(body,"Nightly tracking","Start without arming each night.",enabled,value->{enabled=value;refresh();});Design.space(body,12);
        TrackingOptionsUi.menu(body,"clock","Start time","Every night · "+owner.zone().getId(),Design.clock(this,minute),this::openTime);
        RecordUi.heading(body,"Start method");
        TrackingOptionsUi.choice(body,"At the scheduled time","Begin recording phone movement.",null,!stillness,()->{stillness=false;refresh();});
        TrackingOptionsUi.choice(body,"After settling down","15 min still, screen off · an estimate.",null,stillness,()->{stillness=true;refresh();});
        RecordUi.heading(body,"Schedule setup");
        TrackingOptionsUi.menu(body,"plug","Start conditions","What happens when the phone is unplugged",charging?"Charging only":"Any power",()->openPage("conditions"));
        boolean ready=owner.notifications(this)&&owner.exact(this)&&owner.movement(this);
        TrackingOptionsUi.menu(body,"shield","Start reliability","Permissions and movement sensor",ready?"Ready":"Needs attention",()->openPage("reliability"));
        if(!ready){Design.space(body,8);Design.text(body,"Review start reliability before enabling this schedule.",13,500,Design.BROWN);}
        TrackingOptionsUi.menu(body,"info","Recording limits","Duration, microphone and missed starts",null,()->openPage("limits"));
        String schedule=owner.preferences(this).getString("scheduleStatus","");long next=owner.preferences(this).getLong("pendingAt",0);
        if(next>0&&owner.preferences(this).getBoolean("enabled",false)){RecordUi.heading(body,"Currently scheduled");Design.text(body,RecordUi.date(next,owner.zone())+" · "+RecordUi.clock(this,next,owner.zone()),16,600,Design.INK);}
        else if(!schedule.isEmpty()){Design.space(body,12);Design.text(body,schedule,13,400,Design.MUTED);}
        String status=RecurringTracking.status(owner,this);if(!status.isEmpty()){Design.space(body,10);Design.text(body,status,13,400,Design.MUTED);}
        Design.space(body,14);Design.text(body,"Your choices take effect after Save nightly settings.",12,400,Design.MUTED);
    }
    private void conditionsPage(LinearLayout body){
        Design.toggle(body,"Only when charging","If unplugged at the start time, skip that night.",charging,value->charging=value);Design.space(body,18);
        Design.text(body,"Charging is checked only at the saved start time. This nightly schedule skips an unplugged night rather than waiting for charging.",14,400,Design.MUTED);
        if(stillness){RecordUi.heading(body,"Settling down");TrackingOptionsUi.menu(body,"phone","Screen off","Required throughout the stillness wait","Required",null);Design.space(body,10);Design.text(body,"Stillness is a phone-idle estimate. It cannot prove sleep or a sleep stage.",13,400,Design.MUTED);}
    }
    private void reliabilityPage(LinearLayout body){
        boolean notificationsAllowed=owner.notifications(this),exactAllowed=owner.exact(this),movement=owner.movement(this);
        TrackingOptionsUi.menu(body,"bell","Notifications","Keep the ongoing Cancel control visible",notificationsAllowed?"Allowed":"Needs permission",this::notifications);
        TrackingOptionsUi.menu(body,"clock","Exact timing","Allow the scheduled start to run on time",exactAllowed?"Allowed":"Needs permission",Build.VERSION.SDK_INT>=31?this::exact:()->SleepPicker.message(this,"Exact timing","This Android version does not require a separate exact-alarm permission."));
        TrackingOptionsUi.menu(body,"phone","Movement sensor","Required for nightly tracking",movement?"Available":"Unavailable",null);
        Design.space(body,18);Design.text(body,"Force-stopping SleepWell or some battery restrictions can prevent starts. Reopen these settings after changing Android permissions.",14,400,Design.MUTED);
    }
    private void limitsPage(LinearLayout body){
        TrackingOptionsUi.menu(body,"clock","Stillness wait","Cancel tonight from the ongoing notification","Up to 4 h",null);
        TrackingOptionsUi.menu(body,"moon","Recording duration","Automatic stop after the limit","Up to 12 h",null);
        TrackingOptionsUi.menu(body,"mic","Sound recording","Microphone and sleep sounds never start here","Off",null);
        Design.space(body,18);Design.text(body,"Disabling this schedule and saving stops only its own active recording. Other recordings and tonight-only waits are separate.",14,400,Design.MUTED);
        Design.space(body,14);Design.text(body,"Awake-start rejection, if enabled in Automatic tracking → Sleep confirmation, also applies to repeating stillness starts.",13,400,Design.MUTED);
    }
    private void openTime(){picker=SleepPicker.time(this,"Nightly start time",minute,value->{minute=value;refresh();});}
    private void save(){try{RecurringTracking.Config saved=RecurringTracking.save(this,owner,new RecurringTracking.Config(revision,enabled,minute,stillness,charging),revision);revision=saved.revision;if(saved.enabled&&owner.preferences(this).getString("pending","").isEmpty()){refreshFailure("Settings were saved, but the next start could not be scheduled. "+owner.preferences(this).getString("scheduleStatus","Recheck exact timing permission."));return;}setResult(RESULT_OK);finish();}catch(RuntimeException failure){refreshFailure(failure.getMessage());}}
    private void refreshFailure(String message){errorCopy=message==null?"Nightly settings could not be saved.":message;render();}
    private void show(String message){
        errorCopy=message==null?"Nightly settings could not be saved.":message;if(error==null)return;TextView target=error;target.setText(errorCopy);target.setVisibility(View.VISIBLE);
        Runnable expose=()->{if(error==target&&!isDestroyed()&&target.isAttachedToWindow()&&target.isLaidOut())target.requestRectangleOnScreen(new android.graphics.Rect(0,0,target.getWidth(),target.getHeight()),true);};
        if(target.isLaidOut()&&!target.isLayoutRequested())target.post(expose);
        else target.addOnLayoutChangeListener(new View.OnLayoutChangeListener(){public void onLayoutChange(View view,int left,int top,int right,int bottom,int oldLeft,int oldTop,int oldRight,int oldBottom){if(right<=left||bottom<=top)return;view.removeOnLayoutChangeListener(this);expose.run();}});
    }
    private void exact(){try{if(Build.VERSION.SDK_INT>=31)startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName())));}catch(RuntimeException failure){show("Open Android app settings to allow exact alarms.");}}
    private void notifications(){if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=android.content.pm.PackageManager.PERMISSION_GRANTED){if(owner.preferences(this).getBoolean("notificationAsked",false)&&!shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)){startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName())));return;}owner.preferences(this).edit().putBoolean("notificationAsked",true).apply();requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},792);return;}try{startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName()));}catch(RuntimeException failure){show("Open Android app settings to allow notifications.");}}
    @Override public void onRequestPermissionsResult(int request,String[] names,int[] grants){super.onRequestPermissionsResult(request,names,grants);if(request==792)render();}
}
