package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.provider.Settings;
import android.view.*;
import android.widget.*;

/** Global, optional protection controls, reached from each alarm or Alarm options. */
public final class AlarmProtectionActivity extends Activity {
    private Dialog disclosure;
    static boolean access(Context context){String services=Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);ComponentName own=new ComponentName(context,BedtimeGuard.class);if(services!=null)for(String service:services.split(":"))if(own.equals(ComponentName.unflattenFromString(service)))return true;return false;}
    @Override public void onCreate(Bundle saved){super.onCreate(saved);RecordUi.review(this);}
    @Override protected void onResume(){super.onResume();render();}
    private void render(){LinearLayout body=Design.page(this,false,null,n->{});RecordUi.label(this,body);RecordUi.header(body,"Alarm protection",this::finish);Design.space(body,10);Design.text(body,"Applies to all alarms",20,600,Design.INK);Design.space(body,8);Design.text(body,"Optional controls that stay active while a wake-up challenge is ringing.",14,400,Design.MUTED);Design.space(body,24);
        try{protection(body,AlarmProtections.LEAVING,"Prevent leaving app","Return to the challenge when another app opens. Calls, Settings and emergency screens stay available.");Design.space(body,12);protection(body,AlarmProtections.POWER,"Prevent power off","Dismiss recognised power menus while the alarm rings.");}
        catch(RuntimeException e){Design.text(body,"Protection settings could not be read. Try reopening this page.",14,500,Design.BROWN);return;}
        Design.space(body,24);boolean enabled=access(this);PlanActivity.detail(body,"shield","Accessibility access",enabled?"SleepWell guard is enabled":"Required for either protection",enabled?"Enabled":"Set up",this::disclosure);Design.line(body);Design.space(body,20);Design.text(body,"Power-menu protection is best effort. It cannot stop a forced hardware restart, work while the phone is off, or cover every manufacturer's menu.",13,400,Design.MUTED);Design.space(body,12);Design.text(body,"The guard uses window package and class names. It does not read screen contents or typed text.",12,400,Design.MUTED);Design.space(body,24);Design.button(body,"Done",true,this::finish);
    }
    private void protection(LinearLayout body,String key,String title,String description){Design.toggle(body,title,description,AlarmProtections.enabled(this,key),value->{AlarmProtections.set(this,key,value);if(value&&!access(this))disclosure();});}
    private void disclosure(){
        if(isFinishing()||isDestroyed()||disclosure!=null&&disclosure.isShowing())return;
        Dialog dialog=new Dialog(this);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ScrollView scroll=new ScrollView(this);LinearLayout body=Design.col(this);Design.pad(body,24,24);body.setBackground(Design.shape(this,Design.CREAM,24));scroll.addView(body);dialog.setContentView(scroll);
        Design.text(body,"Enable the optional guard?",23,600,Design.INK);Design.space(body,16);Design.text(body,getString(R.string.guard_description),14,400,Design.MUTED);Design.space(body,20);
        Design.button(body,"Open settings",true,()->{dialog.dismiss();if(!isFinishing()&&!isDestroyed())startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));});Design.space(body,8);Design.button(body,"Later",false,dialog::dismiss);
        disclosure=dialog;dialog.setOnDismissListener(ignored->{if(disclosure==dialog)disclosure=null;});dialog.show();Window window=dialog.getWindow();window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));window.setLayout(getResources().getDisplayMetrics().widthPixels-Design.dp(this,32),ViewGroup.LayoutParams.WRAP_CONTENT);window.setNavigationBarContrastEnforced(false);androidx.core.view.WindowInsetsControllerCompat bars=androidx.core.view.WindowCompat.getInsetsController(window,window.getDecorView());bars.setAppearanceLightStatusBars(true);bars.setAppearanceLightNavigationBars(true);
    }
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
    @Override protected void onDestroy(){if(disclosure!=null)disclosure.dismiss();super.onDestroy();}
}
