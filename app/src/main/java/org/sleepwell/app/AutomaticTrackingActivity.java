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
        boolean movementAvailable(Activity activity);
        boolean notificationsAllowed(Activity activity);
        boolean microphoneAllowed(Activity activity);
    }
    private static final Port PLATFORM=new Port(){
        public SharedPreferences preferences(Activity a){return Prefs.of(a);}
        public long schedule(Activity a,AutomaticSleep.Mode mode){try(Store store=new Store(a)){return AutomaticStart.schedule(mode,System.currentTimeMillis(),ZoneId.systemDefault(),SleepPlans.load(a).bedtime,store.alarms());}}
        public void arm(Activity a,AutomaticSleep.Mode mode,long scheduled,boolean recordSounds,boolean keepClips){a.startForegroundService(new Intent(a,TrackingService.class).setAction(TrackingService.ARM).putExtra("mode",mode.name()).putExtra("scheduled",scheduled).putExtra("recordSounds",recordSounds).putExtra("keepClips",keepClips));a.startActivity(new Intent(a,TrackingActivity.class).putExtra("automaticRequest",true));a.finish();}
        public boolean microphoneAllowed(Activity a){return a.checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED;}
        public boolean notificationsAllowed(Activity a){return Build.VERSION.SDK_INT<33||a.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED;}
        public boolean movementAvailable(Activity a){return a.getSystemService(SensorManager.class).getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!=null;}
    };
    static Port port=PLATFORM;
    private AutomaticSleep.Mode mode;private TextView error;private boolean permissionPending,recordSounds,keepClips;private Port owner;private LinearLayout body;
    @Override public void onCreate(Bundle state){super.onCreate(state);owner=port;if(getIntent().getBooleanExtra("visualReview",false))getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);String selected=state==null?owner.preferences(this).getString("automaticMode","MANUAL"):state.getString("mode","MANUAL");try{mode=AutomaticSleep.Mode.valueOf(selected);}catch(Exception ignored){mode=AutomaticSleep.Mode.MANUAL;}recordSounds=state==null?owner.preferences(this).getBoolean("automaticRecordSounds",false):state.getBoolean("recordSounds");keepClips=state==null?owner.preferences(this).getBoolean("automaticKeepClips",false):state.getBoolean("keepClips");render();}
    @Override protected void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);state.putString("mode",mode.name());state.putBoolean("recordSounds",recordSounds);state.putBoolean("keepClips",keepClips);}
    private void render(){
        body=Design.page(this,false,null,n->{});Design.header(body,"Automatic tracking",this::finish);
        Design.text(body,"Choose when to start recording tonight.",16,400,Design.MUTED);Design.space(body,18);
        option(body,AutomaticSleep.Mode.MANUAL,"Manual only","Start a recording yourself.");
        option(body,AutomaticSleep.Mode.BEDTIME,"At bedtime","Use your saved bedtime.");
        option(body,AutomaticSleep.Mode.SMART_WINDOW,"Before smart window","Start 45 min before an alarm’s wake window.");
        option(body,AutomaticSleep.Mode.STILLNESS,"After settling down","15 min still, with the screen off. An estimate.");
        Design.space(body,16);Design.line(body);
        Design.item(body,"moon","Tonight only","Up to 4 h waiting",null);Design.line(body);
        if(mode!=AutomaticSleep.Mode.MANUAL){
            Design.toggle(body,"Record sounds","Microphone starts when tracking begins.",recordSounds,on->{recordSounds=on;if(!on)keepClips=false;render();});
            if(recordSounds){Design.space(body,8);Design.toggle(body,"Keep sound clips","Save short clips on this phone.",keepClips,on->keepClips=on);}
        }
        Design.space(body,8);
        error=Design.text(body,"",13,500,0xffa93919);error.setVisibility(View.GONE);
        String old=owner.preferences(this).getString("automaticResult","");if(!TrackingService.running&&owner.preferences(this).getBoolean("automaticArmed",false))old="The previous wait stopped. Arm again to resume automatic starts.";
        if(!old.isEmpty()){Design.space(body,12);Design.text(body,old,13,400,Design.MUTED);}
        Design.button(body,"Save settings",false,()->{if(save()){setResult(RESULT_OK);finish();}});
        if(mode!=AutomaticSleep.Mode.MANUAL)Design.fixedAction(body,"Arm tonight",this::arm);
    }
    private void option(LinearLayout body,AutomaticSleep.Mode choice,String title,String help){
        LinearLayout row=Design.row(this);Design.pad(row,12,15);row.setMinimumHeight(Design.dp(this,76));row.setBackground(Design.touch(this,mode==choice?Design.SOFT:android.graphics.Color.TRANSPARENT,17));
        RadioButton radio=new RadioButton(this);radio.setChecked(mode==choice);radio.setButtonTintList(android.content.res.ColorStateList.valueOf(Design.BROWN));radio.setClickable(false);radio.setFocusable(false);radio.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);row.addView(radio,new LinearLayout.LayoutParams(Design.dp(this,44),Design.dp(this,44)));
        LinearLayout copy=Design.col(this);Design.text(copy,title,16,600,Design.INK);Design.space(copy,5);Design.text(copy,help,12,400,Design.MUTED);row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));row.setContentDescription(title);row.setSelected(choice==mode);row.setOnClickListener(v->{mode=choice;render();});body.addView(row,new LinearLayout.LayoutParams(-1,-2));
    }
    private boolean save(){if(!owner.preferences(this).edit().putString("automaticMode",mode.name()).putBoolean("automaticRecordSounds",recordSounds).putBoolean("automaticKeepClips",recordSounds&&keepClips).commit()){show("Settings could not be saved. Try again.");return false;}return true;}
    private void arm(){
        if(permissionPending)return;
        if(TrackingService.running){show("A recording or automatic wait is already active. Open tracking to manage it.");return;}
        if(!owner.movementAvailable(this)){show("A working movement sensor is required.");return;}
        try{
            long scheduled=owner.schedule(this,mode);long wait=scheduled-System.currentTimeMillis();
            if(wait>AutomaticSleep.MAX_WAIT)throw new IllegalArgumentException("Arm within four hours of the start time.");
            if(Build.VERSION.SDK_INT>=33&&!owner.notificationsAllowed(this)){permissionPending=true;requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},91);return;}
            if(recordSounds&&!owner.microphoneAllowed(this)){permissionPending=true;requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},93);return;}
            if(save())owner.arm(this,mode,scheduled,recordSounds,keepClips);
        }catch(Exception failure){show(failure.getMessage()==null?"Automatic tracking could not start.":failure.getMessage());}
    }
    private void show(String copy){error.setText(copy);error.setVisibility(View.VISIBLE);error.post(()->error.requestRectangleOnScreen(new android.graphics.Rect(0,0,error.getWidth(),error.getHeight()),true));}
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] grants){
        super.onRequestPermissionsResult(request,permissions,grants);
        if((request==91||request==93)&&permissionPending){permissionPending=false;
            if(grants.length>0&&grants[0]==PackageManager.PERMISSION_GRANTED)arm();
            else{show(request==93?"Microphone was not allowed. Turn Record sounds off for movement only, or allow it in app settings.":"Notifications are needed to keep the cancel control visible.");Design.button(body,"Open app settings",false,()->startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,android.net.Uri.parse("package:"+getPackageName()))));}
        }
    }
}
