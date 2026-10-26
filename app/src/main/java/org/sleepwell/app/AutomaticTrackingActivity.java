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
    private String page="home",errorCopy="";private Bundle positions=new Bundle();private ScrollView currentScroll;
    private ArmedWaitConditions conditions=ArmedWaitConditions.NONE;private String conditionsError="";private AutomaticSleep.Mode mode;private TextView error;private boolean permissionPending,recordSounds,keepClips,googleSleep,rejectFalseStarts;private Port owner;private LinearLayout body;
    @Override public void onCreate(Bundle state){super.onCreate(state);owner=port;GoogleSleepSignal.recover(this);if(getIntent().getBooleanExtra("visualReview",false))getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);String selected=state==null?owner.preferences(this).getString("automaticMode","MANUAL"):state.getString("mode","MANUAL");try{mode=AutomaticSleep.Mode.valueOf(selected);}catch(Exception ignored){mode=AutomaticSleep.Mode.MANUAL;}rejectFalseStarts=state==null?AutomaticFalseStart.requested(owner.preferences(this)):state.getBoolean("rejectFalseStarts");googleSleep=state==null?owner.preferences(this).getBoolean("automaticGoogleSleep",false):state.getBoolean("googleSleep");recordSounds=state==null?owner.preferences(this).getBoolean("automaticRecordSounds",false):state.getBoolean("recordSounds");keepClips=state==null?owner.preferences(this).getBoolean("automaticKeepClips",false):state.getBoolean("keepClips");try{conditions=state==null?ArmedWaitSettings.read(owner.preferences(this)):new ArmedWaitConditions(state.getBoolean("waitCharging"),state.getBoolean("waitScreenOff"));conditionsError=state==null?"":state.getString("conditionsError","");}catch(IllegalArgumentException invalid){conditionsError=invalid.getMessage();}String requested=state==null?getIntent().getStringExtra(PAGE_EXTRA):state.getString("page");if(java.util.Set.of("home","conditions","sounds","detection").contains(requested==null?"home":requested))page=requested==null?"home":requested;if(state!=null){positions=state.getBundle("positions")==null?new Bundle():state.getBundle("positions");errorCopy=state.getString("errorCopy","");}render();}
    @Override protected void onSaveInstanceState(Bundle state){rememberPosition();super.onSaveInstanceState(state);state.putString("page",page);state.putBundle("positions",positions);state.putString("errorCopy",errorCopy);state.putString("mode",mode.name());state.putBoolean("rejectFalseStarts",rejectFalseStarts);state.putBoolean("recordSounds",recordSounds);state.putBoolean("googleSleep",googleSleep);state.putBoolean("keepClips",keepClips);state.putBoolean("waitCharging",conditions.charging);state.putBoolean("waitScreenOff",conditions.screenOff);state.putString("conditionsError",conditionsError);}
    @Override protected void onResume(){super.onResume();refresh();}
    private void rememberPosition(){if(currentScroll!=null&&currentScroll.isLaidOut())positions.putInt(page,currentScroll.getScrollY());}
    private void refresh(){rememberPosition();render();}
    private void openPage(String destination){rememberPosition();page=destination;render();}
    private void back(){if(!page.equals("home"))openPage("home");else finish();}
    @android.annotation.SuppressLint("GestureBackNavigation") @Override public void onBackPressed(){back();}
    private void render(){
        body=Design.page(this,false,null,n->{});currentScroll=(ScrollView)body.getParent();RecordUi.label(this,body);
        String title=switch(page){case "conditions"->"Start conditions";case "sounds"->"Sound recording";case "detection"->"Sleep confirmation";default->"Automatic tracking";};
        RecordUi.header(body,title,this::back);Design.space(body,12);
        if(!page.equals("home"))TrackingOptionsUi.context(body,"Automatic tracking");
        switch(page){case "conditions"->conditionsPage();case "sounds"->soundsPage();case "detection"->detectionPage();default->home();}
        LinearLayout footer=RecordUi.footer(body);
        error=Design.text(footer,errorCopy.isEmpty()?conditionsError:errorCopy,13,500,0xffa93919);error.setVisibility(error.getText().length()==0?View.GONE:View.VISIBLE);
        if(page.equals("home")){
            Design.button(footer,"Save settings",true,()->{if(save()){setResult(RESULT_OK);finish();}});
            if(mode!=AutomaticSleep.Mode.MANUAL)Design.button(footer,"Arm tonight",false,this::arm);
        }else{Design.text(footer,"Changes apply when you save or arm tonight.",12,400,Design.MUTED);Design.button(footer,"Done",true,this::back);}
        ScrollView owned=currentScroll;int position=positions.getInt(page);owned.post(()->{if(currentScroll==owned)owned.scrollTo(0,position);});
    }
    private void home(){
        Design.text(body,"Choose when to start recording tonight.",15,400,Design.MUTED);Design.space(body,16);
        option(AutomaticSleep.Mode.MANUAL,"Manual only","Start a recording yourself.",null);
        option(AutomaticSleep.Mode.BEDTIME,"At bedtime","Use your saved bedtime.",Design.clock(this,SleepPlans.load(this).bedtime));
        option(AutomaticSleep.Mode.SMART_WINDOW,"Before smart window","45 min before an alarm’s wake window.",null);
        option(AutomaticSleep.Mode.STILLNESS,"After settling down","15 min still, with the screen off. An estimate.",null);
        if(mode!=AutomaticSleep.Mode.MANUAL){
            RecordUi.heading(body,"Tonight’s setup");
            String timing=mode==AutomaticSleep.Mode.BEDTIME?Design.clock(this,SleepPlans.load(this).bedtime):mode==AutomaticSleep.Mode.STILLNESS?"Wait begins when armed":"Next enabled alarm’s wake window";
            TrackingOptionsUi.menu(body,"calendar","Source schedule",timing,"",()->SleepPicker.message(this,"Source schedule",mode==AutomaticSleep.Mode.BEDTIME?"Uses your saved sleep-plan bedtime. Change the bedtime in Sleep goals before arming tonight.":mode==AutomaticSleep.Mode.SMART_WINDOW?"Uses the next enabled alarm and its smart-wake window, starting 45 minutes before that window. Change the alarm in Alarms before arming tonight.":"Arming begins a wait for 15 minutes of stillness with the screen off. Waiting alone does not prove sleep."));
            TrackingOptionsUi.menu(body,"plug","Start conditions","Charging and screen requirements",conditions.charging?"Charging required":conditions.screenOff||mode==AutomaticSleep.Mode.STILLNESS?"Screen off":"No extra wait",()->openPage("conditions"));
            TrackingOptionsUi.menu(body,"mic","Sound recording","For tonight’s automatic recording",recordSounds?(keepClips?"Analysis + clips":"Analysis only"):"Off",()->openPage("sounds"));
            if(mode==AutomaticSleep.Mode.STILLNESS)TrackingOptionsUi.menu(body,"moon","Sleep confirmation","Google estimate and awake-start rejection",googleSleep?"Google + stillness":"Phone stillness",()->openPage("detection"));
            Design.space(body,10);Design.text(body,"Save keeps your choices. Arm tonight starts one wait, lasting up to 4 hours.",12,400,Design.MUTED);
        }
        RecordUi.heading(body,"More start options");
        TrackingOptionsUi.menu(body,"calendar","Repeat nightly","A separate movement-only schedule",null,()->startActivity(RecordUi.intent(this,RecurringTrackingActivity.class)));
        TrackingOptionsUi.menu(body,"file","Missing-night estimate","Review phone activity before saving",null,()->startActivity(RecordUi.intent(this,MissedNightActivity.class)));
        String old=owner.preferences(this).getString("automaticResult","");if(!TrackingService.running&&owner.preferences(this).getBoolean("automaticArmed",false))old="The previous wait stopped. Arm again to resume automatic starts.";
        if(!old.isEmpty()){RecordUi.heading(body,"Last automatic start");Design.text(body,old,13,400,Design.MUTED);}
        if(googleSleep&&mode==AutomaticSleep.Mode.STILLNESS){String unavailable=GoogleSleepSignal.unavailable(this);if(!unavailable.isEmpty()){Design.space(body,10);Design.text(body,unavailable,13,500,Design.BROWN);}}
    }
    private void option(AutomaticSleep.Mode choice,String title,String help,String value){TrackingOptionsUi.choice(body,title,help,value,mode==choice,()->{mode=choice;refresh();});}
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
        if(permissionPending)return;if(!conditionsError.isEmpty()){show("Review the armed wait conditions and Save settings before arming.");return;}
        if(TrackingService.running){show("A recording or automatic wait is already active. Open tracking to manage it.");return;}
        if(!owner.movementAvailable(this)){show("A working movement sensor is required.");return;}
        try{
            long scheduled=owner.schedule(this,mode);long wait=scheduled-System.currentTimeMillis();
            if(wait>AutomaticSleep.MAX_WAIT)throw new IllegalArgumentException("Arm within four hours of the start time.");
            if(Build.VERSION.SDK_INT>=33&&!owner.notificationsAllowed(this)){permissionPending=true;requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},91);return;}
            if(googleSleep&&mode==AutomaticSleep.Mode.STILLNESS){if(checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)!=PackageManager.PERMISSION_GRANTED){permissionPending=true;requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION},94);return;}String unavailable=GoogleSleepSignal.unavailable(this);if(!unavailable.isEmpty())throw new IllegalStateException(unavailable);}
            if(recordSounds&&!owner.microphoneAllowed(this)){permissionPending=true;requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},93);return;}
            if(save())owner.arm(this,mode,scheduled,recordSounds,keepClips,conditions,googleSleep&&mode==AutomaticSleep.Mode.STILLNESS);
        }catch(Exception failure){show(failure.getMessage()==null?"Automatic tracking could not start.":failure.getMessage());}
    }
    private void show(String copy){errorCopy=copy;error.setText(copy);error.setVisibility(View.VISIBLE);error.post(()->error.requestRectangleOnScreen(new android.graphics.Rect(0,0,error.getWidth(),error.getHeight()),true));}
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] grants){
        super.onRequestPermissionsResult(request,permissions,grants);
        if((request==91||request==93||request==94)&&permissionPending){permissionPending=false;
            if(grants.length>0&&grants[0]==PackageManager.PERMISSION_GRANTED)arm();
            else{show(request==94?"Physical activity was not allowed. Turn Google sleep estimates off to use phone stillness only.":request==93?"Microphone was not allowed. Turn Record sounds off for movement only, or allow it in app settings.":"Notifications are needed to keep the cancel control visible.");Design.button(body,"Open app settings",false,()->startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,android.net.Uri.parse("package:"+getPackageName()))));}
        }
    }
}
