package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.telecom.TelecomManager;

/** Only a reviewed blank keypad handoff. No call, number, grants, alarm stop or protection setting. */
final class ChallengeDialer {
    interface Port {String defaultPackage(Context context);ComponentName resolve(Context context,String pkg);void open(Activity owner,Intent intent);}
    static final Port PLATFORM=new Port(){
        public String defaultPackage(Context c){TelecomManager manager=c.getSystemService(TelecomManager.class);return manager==null?null:manager.getDefaultDialerPackage();}
        public ComponentName resolve(Context c,String pkg){ResolveInfo found=c.getPackageManager().resolveActivity(new Intent(Intent.ACTION_DIAL).setPackage(pkg),PackageManager.MATCH_DEFAULT_ONLY);ActivityInfo activity=found==null?null:found.activityInfo;return activity!=null&&activity.exported&&activity.enabled&&activity.applicationInfo.enabled&&pkg.equals(activity.packageName)?new ComponentName(activity.packageName,activity.name):null;}
        public void open(Activity owner,Intent intent){owner.startActivity(intent);}
    };
    static Port port=PLATFORM;
    static ComponentName target(Context context){try{String pkg=port.defaultPackage(context);if(pkg==null||pkg.isEmpty()||pkg.length()>255)return null;ComponentName target=port.resolve(context,pkg);return target!=null&&pkg.equals(target.getPackageName())?target:null;}catch(RuntimeException unavailable){return null;}}
    static void open(Activity owner,ComponentName reviewed,String run,int unit,boolean real){
        if(reviewed==null||!reviewed.equals(target(owner)))throw new IllegalStateException("The default dialer changed or is unavailable. Reopen Emergency options.");
        ActivityManager manager=owner.getSystemService(ActivityManager.class);if(manager!=null&&manager.getLockTaskModeState()!=ActivityManager.LOCK_TASK_MODE_NONE)throw new IllegalStateException("Android screen pinning is active. Use Android’s unpin controls to open the dialer. The alarm keeps running.");
        Intent blank=new Intent(Intent.ACTION_DIAL).setComponent(reviewed);
        if(real)AlarmDialerHandoff.begin(owner,run,unit,reviewed.getPackageName());
        try{port.open(owner,blank);}catch(RuntimeException unavailable){if(real)AlarmDialerHandoff.clear(run);throw new IllegalStateException("The system dialer could not open. The alarm and current task remain active.");}
    }
    private ChallengeDialer(){}
}
