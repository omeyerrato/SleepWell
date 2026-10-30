package org.sleepwell.app;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.hardware.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.time.ZoneId;

/** Choose a mode, then explicitly arm one bounded foreground wait. No boot receiver is involved. */
public final class AutomaticTrackingActivity extends Activity {
    interface Port {
        SharedPreferences preferences(Activity activity);
        long schedule(Activity activity,AutomaticSleep.Mode mode);
        void arm(Activity activity,AutomaticSleep.Mode mode,long scheduled,boolean recordSounds,boolean keepClips);
        default void arm(Activity activity,AutomaticSleep.Mode mode,long scheduled,boolean recordSounds,boolean keepClips,ArmedWaitConditions conditions){arm(activity,mode,scheduled,recordSounds,keepClips);}
        default void arm(Activity activity,AutomaticSleep.Mode mode,long scheduled,boolean recordSounds,boolean keepClips,ArmedWaitConditions conditions,boolean googleSleep){arm(activity,mode,scheduled,recordSounds,keepClips,conditions);}
        boolean movementAvailable(Activity activity);
        boolean notificationsAllowed(Activity activity);
        boolean microphoneAllowed(Activity activity);
        default String currentPhase(Activity activity){TrackingService.Snapshot snapshot=TrackingService.snapshot();return snapshot==null||snapshot.phase.equals("CLOSED")?"":snapshot.phase;}
    }
    private static final Port PLATFORM=new Port(){
        public SharedPreferences preferences(Activity a){return Prefs.of(a);}
        public long schedule(Activity a,AutomaticSleep.Mode mode){try(Store store=new Store(a)){return AutomaticStart.schedule(mode,System.currentTimeMillis(),ZoneId.systemDefault(),SleepPlans.load(a).bedtime,store.alarms(),(alarm,now,zone)->AlarmSkips.next(a,alarm,now,zone));}}
        public void arm(Activity a,AutomaticSleep.Mode mode,long scheduled,boolean recordSounds,boolean keepClips){arm(a,mode,scheduled,recordSounds,keepClips,ArmedWaitConditions.NONE);}
        public void arm(Activity a,AutomaticSleep.Mode mode,long scheduled,boolean recordSounds,boolean keepClips,ArmedWaitConditions conditions){arm(a,mode,scheduled,recordSounds,keepClips,conditions,false);}
        public void arm(Activity a,AutomaticSleep.Mode mode,long scheduled,boolean recordSounds,boolean keepClips,ArmedWaitConditions conditions,boolean googleSleep){a.startForegroundService(ArmedWaitSettings.extras(new Intent(a,TrackingService.class).setAction(TrackingService.ARM).putExtra("mode",mode.name()).putExtra("scheduled",scheduled).putExtra("recordSounds",recordSounds).putExtra("keepClips",keepClips).putExtra(GoogleSleepSignal.EXTRA,googleSleep),conditions));a.startActivity(new Intent(a,TrackingActivity.class).putExtra("automaticRequest",true));a.finish();}
        public boolean microphoneAllowed(Activity a){return a.checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED;}
        public boolean notificationsAllowed(Activity a){return Build.VERSION.SDK_INT<33||a.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED;}
        public boolean movementAvailable(Activity a){return a.getSystemService(SensorManager.class).getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!=null;}
    };
    static Port port=PLATFORM;
    static final String PAGE_EXTRA="trackingOptionsPage";
    // Denial recovery belongs to the draft, not a callback's soon-to-be-replaced view.
    private static final java.util.Set<String> PAGES=java.util.Set.of("home","conditions","sounds","detection","options","source","access");
    private final java.util.ArrayDeque<String> history=new java.util.ArrayDeque<>();
    private final Handler stateHandler=new Handler(Looper.getMainLooper());private boolean resumed;private String runtimePhase="";
    private final Runnable stateTick=new Runnable(){public void run(){if(!resumed)return;if(!currentPhase().equals(runtimePhase))refresh();stateHandler.postDelayed(this,1000);}};
    private String page="home",errorCopy="";private int permissionRecovery,permissionRequest;private Bundle positions=new Bundle();private ScrollView currentScroll;
    private ArmedWaitConditions conditions=ArmedWaitConditions.NONE;private String conditionsError="";private AutomaticSleep.Mode mode;private TextView error;private boolean permissionPending,recordSounds,keepClips,googleSleep,rejectFalseStarts;private Port owner;private LinearLayout body;
    @Override public void onCreate(Bundle state){super.onCreate(state);if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::back);owner=port;GoogleSleepSignal.recover(this);if(getIntent().getBooleanExtra("visualReview",false))getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);String selected=state==null?owner.preferences(this).getString("automaticMode","MANUAL"):state.getString("mode","MANUAL");try{mode=AutomaticSleep.Mode.valueOf(selected);}catch(Exception ignored){mode=AutomaticSleep.Mode.MANUAL;}rejectFalseStarts=state==null?AutomaticFalseStart.requested(owner.preferences(this)):state.getBoolean("rejectFalseStarts");googleSleep=state==null?owner.preferences(this).getBoolean("automaticGoogleSleep",false):state.getBoolean("googleSleep");recordSounds=state==null?owner.preferences(this).getBoolean("automaticRecordSounds",false):state.getBoolean("recordSounds");keepClips=state==null?owner.preferences(this).getBoolean("automaticKeepClips",false):state.getBoolean("keepClips");try{conditions=state==null?ArmedWaitSettings.read(owner.preferences(this)):new ArmedWaitConditions(state.getBoolean("waitCharging"),state.getBoolean("waitScreenOff"));conditionsError=state==null?"":state.getString("conditionsError","");}catch(IllegalArgumentException invalid){conditionsError=invalid.getMessage();}String requested=state==null?getIntent().getStringExtra(PAGE_EXTRA):state.getString("page");if(PAGES.contains(requested==null?"home":requested))page=requested==null?"home":requested;if(state!=null){positions=state.getBundle("positions")==null?new Bundle():state.getBundle("positions");errorCopy=state.getString("errorCopy","");int recovery=state.getInt("permissionRecovery");if(recovery==91||recovery==93||recovery==94)permissionRecovery=recovery;int request=state.getInt("permissionRequest");if(state.getBoolean("permissionPending")&&(request==91||request==93||request==94)){permissionPending=true;permissionRequest=request;}java.util.ArrayList<String> restored=state.getStringArrayList("pageHistory");if(restored!=null)for(String entry:restored)if(PAGES.contains(entry))history.addLast(entry);}render();}
    @Override protected void onSaveInstanceState(Bundle state){rememberPosition();super.onSaveInstanceState(state);state.putString("page",page);state.putBundle("positions",positions);state.putString("errorCopy",errorCopy);state.putInt("permissionRecovery",permissionRecovery);state.putBoolean("permissionPending",permissionPending);state.putInt("permissionRequest",permissionRequest);state.putStringArrayList("pageHistory",new java.util.ArrayList<>(history));state.putString("mode",mode.name());state.putBoolean("rejectFalseStarts",rejectFalseStarts);state.putBoolean("recordSounds",recordSounds);state.putBoolean("googleSleep",googleSleep);state.putBoolean("keepClips",keepClips);state.putBoolean("waitCharging",conditions.charging);state.putBoolean("waitScreenOff",conditions.screenOff);state.putString("conditionsError",conditionsError);}
    @Override protected void onResume(){super.onResume();resumed=true;stateHandler.removeCallbacks(stateTick);stateHandler.post(stateTick);if(permissionRecovery!=0&&!permissionMissing(permissionRecovery)){permissionRecovery=0;errorCopy="Access is available. Review your choices before arming tonight.";}refresh();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
    @Override protected void onPause(){resumed=false;stateHandler.removeCallbacks(stateTick);super.onPause();}
    @Override protected void onDestroy(){stateHandler.removeCallbacks(stateTick);super.onDestroy();}
    private String currentPhase(){try{String phase=owner.currentPhase(this);return phase==null?"UNAVAILABLE":phase;}catch(RuntimeException unavailable){return "UNAVAILABLE";}}
    private void rememberPosition(){if(currentScroll!=null&&currentScroll.isLaidOut())positions.putInt(page,currentScroll.getScrollY());}
    private void refresh(){rememberPosition();render();}
    private void openPage(String destination){if(!PAGES.contains(destination)||page.equals(destination))return;rememberPosition();history.push(page);page=destination;render();}
    private void back(){if(page.equals("home")){finish();return;}rememberPosition();page=history.isEmpty()?"home":history.pop();render();}
    @android.annotation.SuppressLint("GestureBackNavigation") @Override public void onBackPressed(){back();}
    private void render(){
        runtimePhase=currentPhase();body=Design.page(this,false,null,n->{});body.setPadding(body.getPaddingLeft(),Design.dp(this,8),body.getPaddingRight(),body.getPaddingBottom());currentScroll=(ScrollView)body.getParent();RecordUi.label(this,body);
        String title=switch(page){case "conditions"->"Start conditions";case "sounds"->"Sound recording";case "detection"->"Sleep confirmation";case "options"->"Automatic tracking options";case "source"->"Source schedule";case "access"->"Activity access";default->"Automatic tracking";};
        LinearLayout header=RecordUi.header(body,title,this::back);header.getLayoutParams().height=-2;header.setMinimumHeight(Design.dp(this,52));
        Design.space(body,8);
        if(!page.equals("home"))TrackingOptionsUi.context(body,"Automatic tracking");
        switch(page){case "conditions"->conditionsPage();case "sounds"->soundsPage();case "detection"->detectionPage();case "options"->optionsPage();case "source"->sourcePage();case "access"->accessPage();default->home();}
        LinearLayout footer=RecordUi.footer(body);
        error=Design.text(footer,errorCopy.isEmpty()?conditionsError:errorCopy,13,500,0xffa93919);error.setVisibility(error.getText().length()==0?View.GONE:View.VISIBLE);
        if(permissionRecovery!=0&&permissionMissing(permissionRecovery))Design.button(footer,"Open app settings",false,this::openPermissionSettings);
        if(page.equals("home")){
            Design.text(footer,runtimePhase.equals("ARMED")?"Tonight is already armed. Save applies next time.":!runtimePhase.isEmpty()?"Manage your current session in Tracking.":"Save for later, or arm these settings for tonight.",12,400,Design.MUTED);
            if(mode==AutomaticSleep.Mode.MANUAL)Design.button(footer,"Save settings",true,this::saveAndClose);
            else{LinearLayout actions=Design.row(this),save=Design.col(this),arm=Design.col(this);LinearLayout.LayoutParams left=new LinearLayout.LayoutParams(0,-2,1);left.rightMargin=Design.dp(this,5);actions.addView(save,left);LinearLayout.LayoutParams right=new LinearLayout.LayoutParams(0,-2,1);right.leftMargin=Design.dp(this,5);actions.addView(arm,right);footer.addView(actions);Design.button(save,"Save settings",true,this::saveAndClose);Button start=Design.button(arm,"Arm tonight",false,this::arm);RecordUi.enabled(start,runtimePhase.isEmpty()&&!permissionPending);}
        }else{Design.text(footer,"Save or Arm tonight on the previous screen to apply.",12,400,Design.MUTED);Design.button(footer,"Done",true,this::back);}
        ScrollView owned=currentScroll;int position=positions.getInt(page);owned.post(()->{if(currentScroll==owned)owned.scrollTo(0,position);});
    }
    private void saveAndClose(){if(save()){setResult(RESULT_OK);finish();}}
    private void home(){
        LinearLayout introduction=Design.row(this);introduction.addView(Design.text(this,"Choose tonight’s start method.",14,400,Design.MUTED),new LinearLayout.LayoutParams(0,-2,1));RecordUi.menuAction(introduction,"Automatic tracking options",()->openPage("options"),false);body.addView(introduction);Design.space(body,8);
        if(!runtimePhase.isEmpty()){String status=switch(runtimePhase){case "ARMED"->"Automatic wait active";case "DELAYED"->"Delayed start active";case "STARTING"->"Tracking is starting";case "ACTIVE"->"Recording is active";case "PAUSED"->"Recording is paused";case "PAUSING"->"Recording is pausing";case "STOPPING"->"Recording is finishing";case "BLOCKED"->"Tracking needs attention";default->"Tracking status unavailable";};menu("info",status,"Review the current session without changing this draft",null,()->startActivity(RecordUi.intent(this,TrackingActivity.class)));}
        option(AutomaticSleep.Mode.MANUAL,"Manual only","Start a recording yourself.",null);
        option(AutomaticSleep.Mode.BEDTIME,"At bedtime","Use your saved bedtime.",Design.clock(this,SleepPlans.load(this).bedtime));
        option(AutomaticSleep.Mode.SMART_WINDOW,"Before smart window","45 min before an alarm’s wake window.",null);
        option(AutomaticSleep.Mode.STILLNESS,"After settling down","15 min still, with the screen off. An estimate.",null);
        Design.space(body,6);Design.line(body);
        menu("calendar","Source schedule",mode==AutomaticSleep.Mode.MANUAL?"Manual start":mode==AutomaticSleep.Mode.BEDTIME?"Sleep-plan bedtime":mode==AutomaticSleep.Mode.SMART_WINDOW?"Next enabled alarm’s wake window":"Stillness and screen off",mode==AutomaticSleep.Mode.BEDTIME?Design.clock(this,SleepPlans.load(this).bedtime):null,()->openPage("source"));
        menu("plug","Start conditions","Charging and screen-off requirements",!conditionsError.isEmpty()?"Review settings":conditions.charging?"Charging":conditions.screenOff||mode==AutomaticSleep.Mode.STILLNESS?"Screen off":"None",()->openPage("conditions"));
        menu("shield","Activity access","For optional Google sleep estimates",!googleSleep||mode!=AutomaticSleep.Mode.STILLNESS?"Not required":activityAllowed()?"Allowed":"Needed",()->openPage("access"));
    }
    private void option(AutomaticSleep.Mode choice,String title,String help,String value){int index=body.getChildCount();TrackingOptionsUi.choice(body,title,help,value,mode==choice,()->{mode=choice;refresh();});LinearLayout row=(LinearLayout)body.getChildAt(index);Design.pad(row,12,8);row.setMinimumHeight(Design.dp(this,64));}
    private void menu(String icon,String title,String detail,String value,Runnable open){int index=body.getChildCount();TrackingOptionsUi.menu(body,icon,title,detail,value,open);LinearLayout row=(LinearLayout)body.getChildAt(index);Design.pad(row,0,8);row.setMinimumHeight(Design.dp(this,56));}
    private void sourcePage(){
        String explanation=switch(mode){case MANUAL->"No wait starts automatically. Start a recording from Tracking when you are ready.";case BEDTIME->"Uses your saved sleep-plan bedtime. Review that bedtime before arming tonight.";case SMART_WINDOW->"Uses the next enabled alarm and begins 45 minutes before its smart wake-up window. Review the alarm before arming tonight.";case STILLNESS->"Arming begins a wait for 15 minutes of stillness with the screen off. Waiting alone does not prove sleep.";};
        Design.text(body,explanation,15,400,Design.INK);Design.space(body,16);
        if(mode==AutomaticSleep.Mode.BEDTIME){Design.text(body,"Saved bedtime · "+Design.clock(this,SleepPlans.load(this).bedtime),20,600,Design.INK);menu("bed","Review sleep plan","Opens the saved plan; this draft stays here",null,()->startActivity(RecordUi.intent(this,PlanActivity.class)));}
        if(mode==AutomaticSleep.Mode.SMART_WINDOW)menu("alarm","Open alarms","Choose an alarm to review its wake window",null,()->startActivity(RecordUi.intent(this,MainActivity.class).putExtra("settingsPage","alarms")));
        Design.space(body,16);Design.text(body,"Arm tonight creates one wait of up to four hours. Save settings alone starts no wait. Repeating schedules are separate.",13,400,Design.MUTED);
    }
    private void optionsPage(){
        menu("mic","Sound recording","For the recording after Arm tonight",recordSounds?(keepClips?"Analysis + clips":"Analysis only"):"Off",()->openPage("sounds"));
        menu("moon","Sleep confirmation","For After settling down only",googleSleep?"Google + stillness":"Phone stillness",()->openPage("detection"));
        menu("calendar","Repeat nightly","A separate movement-only schedule",null,()->startActivity(RecordUi.intent(this,RecurringTrackingActivity.class)));
        menu("file","Missing-night estimate","Review phone activity before saving",null,()->startActivity(RecordUi.intent(this,MissedNightActivity.class)));
        String old=owner.preferences(this).getString("automaticResult","");if(runtimePhase.isEmpty()&&owner.preferences(this).getBoolean("automaticArmed",false))old="The previous wait stopped. Arm again to resume automatic starts.";
        RecordUi.heading(body,"Last automatic start");Design.text(body,old.isEmpty()?"No previous automatic-start result is saved.":old,13,400,Design.MUTED);
        Design.space(body,12);menu("info","About Save and Arm tonight",null,null,()->SleepPicker.message(this,"Save and Arm tonight","Save stores these choices for later. Arm tonight saves them and waits for your selected start condition for up to four hours. Repeating schedules are separate. An already active wait keeps its original settings; open Tracking to review or cancel it."));
    }
    private boolean activityAllowed(){return checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)==PackageManager.PERMISSION_GRANTED;}
    private void accessPage(){
        Design.text(body,"Physical activity access",20,600,Design.INK);TextView state=Design.text(body,activityAllowed()?"Allowed":"Not allowed",15,600,Design.BROWN);state.setContentDescription("Physical activity access state");Design.space(body,14);
        Design.text(body,"Phone movement and settling-down waits do not need this permission. Only optional Google sleep estimates require Physical activity access and Google Play services. Permission alone starts no wait.",14,400,Design.MUTED);Design.space(body,14);
        menu("moon","Google estimate settings","Review the optional extra condition",googleSleep?"On":"Off",()->openPage("detection"));
        if(permissionRecovery==0||!permissionMissing(permissionRecovery))Design.button(body,"Manage activity access",false,this::openPermissionSettings);
    }
    private void conditionsPage(){
        Design.toggle(body,"Wait for charging","Keep tonight armed until the phone is charging.",conditions.charging,on->conditions=new ArmedWaitConditions(on,conditions.screenOff));Design.space(body,14);
        if(mode!=AutomaticSleep.Mode.STILLNESS)Design.toggle(body,"Wait for screen off","Wait after the scheduled time while you use the phone.",conditions.screenOff,on->conditions=new ArmedWaitConditions(conditions.charging,on));
        else TrackingOptionsUi.menu(body,"phone","Screen off","Already required when settling down","Required",null);
        Design.space(body,18);Design.text(body,"Tonight only · the four-hour wait limit stays the same.",14,400,Design.MUTED);
    }
    private void soundsPage(){
        Design.toggle(body,"Record sounds","Microphone starts when tracking begins.",recordSounds,on->{recordSounds=on;if(!on)keepClips=false;refresh();});Design.space(body,14);
        if(recordSounds)Design.toggle(body,"Keep sound clips","Save short clips on this phone.",keepClips,on->keepClips=on);
        else Design.text(body,"Movement tracking works with the microphone off.",14,400,Design.MUTED);
        Design.space(body,18);Design.text(body,"Applies only when you arm tonight. Repeat nightly always uses movement without microphone recording.",13,400,Design.MUTED);
    }
    private void detectionPage(){
        Design.text(body,"Used with the After settling down start method.",14,400,Design.MUTED);Design.space(body,16);
        Design.toggle(body,"Require Google sleep estimate","Require a fresh estimate as well as phone stillness.",googleSleep,on->{googleSleep=on;refresh();});Design.space(body,12);
        TrackingOptionsUi.menu(body,"info","Google estimate details","Permission, confidence and update timing",null,()->SleepPicker.message(this,"Google sleep estimate","Requires Physical activity permission and Google Play services, plus fresh confidence of at least 80/100 and phone stillness. Updates may take 10 minutes or longer. This extra condition applies only to tonight’s armed wait, not recurring starts. It does not prove sleep or infer sleep stages."));
        if(googleSleep){String unavailable=GoogleSleepSignal.unavailable(this);if(!unavailable.isEmpty())Design.text(body,unavailable,13,500,Design.BROWN);String status=Prefs.text(this,"googleSleepStatus","");if(!status.isEmpty()&&!TrackingService.running)Design.text(body,status,12,400,Design.MUTED);}
        RecordUi.heading(body,"Awake-start protection");
        Design.toggle(body,"Reject awake starts","Discard an untouched start if you keep using the phone.",rejectFalseStarts,on->rejectFalseStarts=on);Design.space(body,12);
        TrackingOptionsUi.menu(body,"info","Rejection details","Also applies to repeating stillness starts",null,()->SleepPicker.message(this,"Reject awake starts","During the first 45 minutes, two minutes of unlocked phone use plus sustained movement or an upright phone can stop and discard an untouched automatic stillness recording. Changed records stay for review. This saved choice also applies to repeating stillness starts; it does not prove that you are awake or asleep."));
    }
    private boolean save(){if(!ArmedWaitSettings.put(owner.preferences(this).edit().putBoolean(AutomaticFalseStart.SETTING,rejectFalseStarts).putString("automaticMode",mode.name()).putBoolean("automaticRecordSounds",recordSounds).putBoolean("automaticGoogleSleep",googleSleep).putBoolean("automaticKeepClips",recordSounds&&keepClips),conditions).commit()){show("Settings could not be saved. Try again.");return false;}conditionsError="";return true;}
    private void arm(){
        if(permissionPending)return;if(permissionRecovery!=0&&!permissionMissing(permissionRecovery))permissionRecovery=0;if(!conditionsError.isEmpty()){show("Review the armed wait conditions and Save settings before arming.");return;}
        if(!currentPhase().isEmpty()||TrackingService.running){show("A recording or automatic wait is already active. Open tracking to manage it.");return;}
        if(!owner.movementAvailable(this)){show("A working movement sensor is required.");return;}
        try{
            long scheduled=owner.schedule(this,mode);long wait=scheduled-System.currentTimeMillis();
            if(wait>AutomaticSleep.MAX_WAIT)throw new IllegalArgumentException("Arm within four hours of the start time.");
            if(Build.VERSION.SDK_INT>=33&&!owner.notificationsAllowed(this)){requestPermission(91,Manifest.permission.POST_NOTIFICATIONS);return;}
            if(googleSleep&&mode==AutomaticSleep.Mode.STILLNESS){if(checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)!=PackageManager.PERMISSION_GRANTED){requestPermission(94,Manifest.permission.ACTIVITY_RECOGNITION);return;}String unavailable=GoogleSleepSignal.unavailable(this);if(!unavailable.isEmpty())throw new IllegalStateException(unavailable);}
            if(recordSounds&&!owner.microphoneAllowed(this)){requestPermission(93,Manifest.permission.RECORD_AUDIO);return;}
            if(save())owner.arm(this,mode,scheduled,recordSounds,keepClips,conditions,googleSleep&&mode==AutomaticSleep.Mode.STILLNESS);
        }catch(Exception failure){show(failure.getMessage()==null?"Automatic tracking could not start.":failure.getMessage());}
    }
    private void requestPermission(int request,String permission){permissionPending=true;permissionRequest=request;try{requestPermissions(new String[]{permission},request);}catch(RuntimeException unavailable){permissionPending=false;permissionRequest=0;throw unavailable;}}
    private void show(String copy){errorCopy=copy;error.setText(copy);error.setVisibility(View.VISIBLE);error.post(()->error.requestRectangleOnScreen(new android.graphics.Rect(0,0,error.getWidth(),error.getHeight()),true));}
    private boolean permissionMissing(int request){return switch(request){case 91->Build.VERSION.SDK_INT>=33&&!owner.notificationsAllowed(this);case 93->!owner.microphoneAllowed(this);case 94->checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)!=PackageManager.PERMISSION_GRANTED;default->false;};}
    private void openPermissionSettings(){if(isFinishing()||isDestroyed()||!hasWindowFocus())return;try{startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,android.net.Uri.parse("package:"+getPackageName())));}catch(RuntimeException unavailable){show("Android could not open app settings. Open SleepWell's permissions in Android Settings, then return here.");}}
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] grants){
        super.onRequestPermissionsResult(request,permissions,grants);
        if(isDestroyed()||isFinishing())return;String expected=switch(request){case 91->Manifest.permission.POST_NOTIFICATIONS;case 93->Manifest.permission.RECORD_AUDIO;case 94->Manifest.permission.ACTIVITY_RECOGNITION;default->"";};
        if(permissionPending&&request==permissionRequest&&!expected.isEmpty()){if(permissions.length>0&&(permissions.length!=1||!expected.equals(permissions[0])))return;permissionPending=false;permissionRequest=0;
            if(permissions.length==1&&grants.length==1&&grants[0]==PackageManager.PERMISSION_GRANTED&&!permissionMissing(request))arm();
            else{permissionRecovery=request;errorCopy=request==94?"Physical activity was not allowed. Turn Google sleep estimates off to use phone stillness only.":request==93?"Microphone was not allowed. Turn Record sounds off for movement only, or allow it in app settings.":"Notifications are needed to keep the cancel control visible.";refresh();show(errorCopy);}
        }
    }
}
