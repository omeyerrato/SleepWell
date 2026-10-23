package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.provider.Settings;
import android.view.*;
import android.widget.*;

/** Global, optional protection controls, reached from each alarm or Alarm options. */
public final class AlarmProtectionActivity extends Activity {
    static boolean access(Context context){String services=Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);ComponentName own=new ComponentName(context,BedtimeGuard.class);if(services!=null)for(String service:services.split(":"))if(own.equals(ComponentName.unflattenFromString(service)))return true;return false;}
    @Override public void onCreate(Bundle saved){super.onCreate(saved);RecordUi.review(this);}
    @Override protected void onResume(){super.onResume();render();}
    private void render(){LinearLayout body=Design.page(this,false,null,n->{});RecordUi.label(this,body);RecordUi.header(body,"Alarm protection",this::finish);Design.space(body,10);Design.text(body,"Applies to all alarms",20,600,Design.INK);Design.space(body,8);Design.text(body,"Optional controls that stay active while a wake-up challenge is ringing.",14,400,Design.MUTED);Design.space(body,24);
        try{protection(body,AlarmProtections.LEAVING,"Prevent leaving app","Return to the challenge when another app opens. Calls, Settings and emergency screens stay available.");Design.space(body,12);protection(body,AlarmProtections.POWER,"Prevent power off","Dismiss recognised power menus while the alarm rings.");}
        catch(RuntimeException e){Design.text(body,"Protection settings could not be read. Try reopening this page.",14,500,Design.BROWN);return;}
        Design.space(body,24);boolean enabled=access(this);PlanActivity.detail(body,"shield","Accessibility access",enabled?"SleepWell guard is enabled":"Required for either protection",enabled?"Enabled":"Set up",this::disclosure);Design.line(body);Design.space(body,20);Design.text(body,"Power-menu protection is best effort. It cannot stop a forced hardware restart, work while the phone is off, or cover every manufacturer's menu.",13,400,Design.MUTED);Design.space(body,12);Design.text(body,"The guard uses window package and class names. It does not read screen contents or typed text.",12,400,Design.MUTED);Design.space(body,24);Design.button(body,"Done",true,this::finish);
    }
    private void protection(LinearLayout body,String key,String title,String description){Design.toggle(body,title,description,AlarmProtections.enabled(this,key),value->{AlarmProtections.set(this,key,value);if(value&&!access(this))disclosure();});}
    private void disclosure(){new AlertDialog.Builder(this).setTitle("Enable the optional guard?").setMessage(getString(R.string.guard_description)).setPositiveButton("Open accessibility settings",(dialog,which)->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))).setNegativeButton("Later",null).show();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
}
