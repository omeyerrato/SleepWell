package org.sleepwell.app;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.provider.Settings;
import android.widget.*;
import java.util.concurrent.*;

/** Explicit system activation/removal. Opening or restoring this screen cannot enable an admin. */
public final class UninstallProtectionActivity extends Activity {
    private static final int ACTIVATE=856;
    private final Handler main=new Handler(Looper.getMainLooper());
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private boolean resumed,busy;private int generation;private String notice="",shown="";
    private ComponentName component(){return new ComponentName(this,SleepWellAdminReceiver.class);}
    private int state(){try{DevicePolicyManager manager=getSystemService(DevicePolicyManager.class);if(manager==null)return -1;return manager.isAdminActive(component())?1:0;}catch(RuntimeException unavailable){return -1;}}
    private boolean visible(){return resumed&&hasWindowFocus()&&!isFinishing()&&!isDestroyed();}
    @Override public void onCreate(Bundle saved){super.onCreate(saved);RecordUi.review(this);if(saved!=null)notice=saved.getString("notice","");if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::finish);render();}
    private void render(){if(isFinishing()||isDestroyed())return;int active=state();shown=active+":"+busy+":"+notice;
        LinearLayout body=Design.page(this,false,null,n->{});RecordUi.label(this,body);RecordUi.header(body,"Uninstall protection",this::finish);Design.space(body,16);
        Design.text(body,"Add an extra uninstall step",25,600,Design.INK);Design.space(body,10);
        Design.text(body,"Android normally requires an active device administrator to be deactivated before uninstalling its app.",14,400,Design.MUTED);Design.space(body,18);
        PlanActivity.detail(body,"shield","Android administrator",active==1?"Active":active==0?"Off":"Status unavailable",null,null);Design.line(body);Design.space(body,16);
        Design.text(body,"Optional and removable",18,600,Design.INK);Design.space(body,8);
        Design.text(body,"Enable opens Android’s activation prompt. You can turn this off here or in Android security settings at any time. It does not prevent hardware shutdown, force-stop, suspension or every device-management action.",14,400,Design.MUTED);Design.space(body,12);
        Design.text(body,"SleepWell requests no password, screen-lock, wipe, camera or device-owner policy. It will not intercept deactivation or change another administrator.",13,400,Design.MUTED);Design.space(body,16);
        if(active<0)Design.text(body,"Android admin state could not be read. Use security settings to review it; no activation is assumed.",13,500,Design.BROWN);
        if(!notice.isEmpty()){TextView message=Design.text(body,notice,13,500,Design.BROWN);message.setAccessibilityLiveRegion(android.view.View.ACCESSIBILITY_LIVE_REGION_POLITE);Design.space(body,12);}
        if(busy)Design.text(body,"Removing this app’s administrator…",14,500,Design.BROWN);
        LinearLayout footer=RecordUi.footer(body);
        if(active==0){Button enable=Design.button(footer,"Enable extra uninstall step",true,this::activate);enable.setEnabled(!busy);}
        if(active==1){Button remove=Design.button(footer,"Remove uninstall protection",true,this::remove);remove.setEnabled(!busy);}
        Button settings=Design.button(footer,"Android security settings",false,this::security);settings.setEnabled(!busy);Design.button(footer,"Done",false,this::finish);
    }
    /** Only a real exported system Activity receives this locally constructed activation request. */
    private void system(Intent intent,int request){PackageManager packages=getPackageManager();ComponentName target=null;
        for(ResolveInfo candidate:packages.queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY|PackageManager.MATCH_SYSTEM_ONLY)){ActivityInfo info=candidate.activityInfo;if(info!=null&&info.exported&&(info.applicationInfo.flags&(ApplicationInfo.FLAG_SYSTEM|ApplicationInfo.FLAG_UPDATED_SYSTEM_APP))!=0){target=new ComponentName(info.packageName,info.name);break;}}
        if(target==null)throw new ActivityNotFoundException();intent.setComponent(target);if(request>0)startActivityForResult(intent,request);else startActivity(intent);
    }
    private void activate(){if(!visible()||busy||state()!=0)return;try{notice="";system(new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,component()).putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,getString(R.string.uninstall_admin_description)),ACTIVATE);}catch(RuntimeException blocked){notice="Android’s activation screen is unavailable or restricted on this device. No protection was enabled.";render();}}
    private void security(){if(!visible()||busy)return;try{system(new Intent(Settings.ACTION_SECURITY_SETTINGS),0);}catch(RuntimeException blocked){notice="Android security settings could not open. You can find device administrators in your phone’s Settings app.";render();}}
    private void remove(){if(!visible()||busy||state()!=1)return;busy=true;notice="";int expected=++generation;Context app=getApplicationContext();ComponentName own=component();render();
        worker.execute(()->{String issue="";try{DevicePolicyManager manager=app.getSystemService(DevicePolicyManager.class);if(manager==null)throw new IllegalStateException();if(manager.isAdminActive(own))manager.removeActiveAdmin(own);}catch(RuntimeException blocked){issue="Android did not accept removal. You can deactivate SleepWell in security settings.";}String result=issue;main.post(()->{if(expected!=generation||isFinishing()||isDestroyed())return;busy=false;notice=result.isEmpty()?state()==0?"Uninstall protection is off.":"Removal was requested. Android has not confirmed deactivation yet.":result;render();});});
    }
    private final Runnable poll=new Runnable(){public void run(){if(!resumed)return;String current=state()+":"+busy+":"+notice;if(!current.equals(shown))render();main.postDelayed(this,1000);}};
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==ACTIVATE){notice=state()==1?"Android administrator is active.":"Activation was not confirmed. The previous admin state is unchanged.";render();}}
    @Override protected void onResume(){super.onResume();resumed=true;main.removeCallbacks(poll);poll.run();}
    @Override protected void onPause(){resumed=false;main.removeCallbacks(poll);super.onPause();}
    @Override protected void onSaveInstanceState(Bundle saved){super.onSaveInstanceState(saved);saved.putString("notice",busy?"An administrator removal was requested. Android’s current state is shown.":notice);}
    @Override protected void onDestroy(){++generation;main.removeCallbacksAndMessages(null);worker.shutdown();super.onDestroy();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
}
