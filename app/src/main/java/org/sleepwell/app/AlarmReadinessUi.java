package org.sleepwell.app;

import android.Manifest;
import android.app.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;

/** Readiness-only permission recovery; checking this page never requests access or starts an alarm. */
final class AlarmReadinessUi {
    private static final String MIC_ASKED="readinessMicrophoneAsked",NOTIFICATIONS_ASKED="readinessNotificationsAsked";
    static void rows(Activity activity,LinearLayout body,int permissionRequest,Runnable refresh){
        NotificationManager notifications=activity.getSystemService(NotificationManager.class);boolean[] requesting={false};
        row(activity,body,"alarm","Exact alarms","Allows alarms to play on time.",Scheduler.allowed(activity),false,
                ()->open(activity,Build.VERSION.SDK_INT>=31?new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,packageUri(activity)):appSettings(activity)));
        row(activity,body,"bell","Notifications",Build.VERSION.SDK_INT>=33?hint(activity,Manifest.permission.POST_NOTIFICATIONS,NOTIFICATIONS_ASKED,"Lets SleepWell show alerts."):"Lets SleepWell show alerts.",notifications.areNotificationsEnabled(),false,
                ()->{if(Build.VERSION.SDK_INT>=33)permission(activity,body,requesting,Manifest.permission.POST_NOTIFICATIONS,NOTIFICATIONS_ASKED,"Allow notifications?","SleepWell uses notifications to show alarm alerts and recording status.",permissionRequest,refresh,()->open(activity,new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,activity.getPackageName())));else open(activity,new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,activity.getPackageName()));});
        if(Build.VERSION.SDK_INT>=34)row(activity,body,"phone","Full-screen alarms","Shows the alarm when the screen is locked.",notifications.canUseFullScreenIntent(),false,
                ()->open(activity,new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,packageUri(activity))));
        row(activity,body,"battery","Background battery","Allow unrestricted battery use for reliable alarms.",activity.getSystemService(PowerManager.class).isIgnoringBatteryOptimizations(activity.getPackageName()),false,()->battery(activity));
        row(activity,body,"mic","Microphone",hint(activity,Manifest.permission.RECORD_AUDIO,MIC_ASKED,"Used for sound events. Optional."),activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED,true,
                ()->permission(activity,body,requesting,Manifest.permission.RECORD_AUDIO,MIC_ASKED,"Allow microphone access?","SleepWell uses the microphone for optional sound events and recordings. Allow access to use those features.",permissionRequest,refresh,()->open(activity,appSettings(activity))));
    }
    private static String hint(Activity activity,String permission,String asked,String description){
        if(activity.checkSelfPermission(permission)!=PackageManager.PERMISSION_GRANTED&&Prefs.flag(activity,asked)&&!activity.shouldShowRequestPermissionRationale(permission))return description+" Change access in app settings.";
        return description;
    }
    private static boolean live(Activity activity){return !activity.isFinishing()&&!activity.isDestroyed();}
    private static Uri packageUri(Activity activity){return Uri.parse("package:"+activity.getPackageName());}
    private static Intent appSettings(Activity activity){return new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,packageUri(activity));}
    static void battery(Activity activity){open(activity,new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));}
    private static void open(Activity activity,Intent intent){
        if(!live(activity)||!activity.hasWindowFocus())return;
        try{activity.startActivity(intent);}catch(RuntimeException unavailable){
            try{activity.startActivity(appSettings(activity));}catch(RuntimeException blocked){Ui.toast(activity,"Android could not open this setting. Open SleepWell in Android Settings.");}
        }
    }
    private static void permission(Activity activity,View owner,boolean[] requesting,String permission,String asked,String title,String explanation,int request,Runnable refresh,Runnable granted){
        if(requesting[0]||!live(activity)||!owner.isAttachedToWindow()||!activity.hasWindowFocus())return;
        if(activity.checkSelfPermission(permission)==PackageManager.PERMISSION_GRANTED){granted.run();return;}
        if(activity.shouldShowRequestPermissionRationale(permission)){
            Dialog dialog=new Dialog(activity);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            LinearLayout content=Design.col(activity);Design.pad(content,22,22);content.setBackground(Design.shape(activity,Design.CREAM,24));
            Design.text(content,title,21,600,Design.INK);Design.space(content,14);Design.text(content,explanation,14,400,Design.MUTED);Design.space(content,14);
            Design.button(content,"Continue",true,()->{if(!live(activity)||!owner.isAttachedToWindow()||!dialog.isShowing()||dialog.getWindow()==null||!dialog.getWindow().getDecorView().hasWindowFocus())return;dialog.dismiss();request(activity,requesting,permission,asked,request,refresh);});
            Design.button(content,"Not now",false,dialog::dismiss);dialog.setContentView(content);
            View.OnAttachStateChangeListener lifetime=new View.OnAttachStateChangeListener(){public void onViewAttachedToWindow(View view){}public void onViewDetachedFromWindow(View view){dialog.dismiss();}};
            owner.addOnAttachStateChangeListener(lifetime);dialog.setOnDismissListener(ignored->owner.removeOnAttachStateChangeListener(lifetime));
            dialog.show();Window window=dialog.getWindow();window.setBackgroundDrawableResource(android.R.color.transparent);window.setLayout(Math.min(Design.dp(activity,420),activity.getResources().getDisplayMetrics().widthPixels-Design.dp(activity,32)),-2);
        }else if(Prefs.flag(activity,asked))open(activity,appSettings(activity));
        else request(activity,requesting,permission,asked,request,refresh);
    }
    private static void request(Activity activity,boolean[] requesting,String permission,String asked,int request,Runnable refresh){
        if(requesting[0]||!live(activity))return;if(activity.checkSelfPermission(permission)==PackageManager.PERMISSION_GRANTED){refresh.run();return;}
        boolean previous=Prefs.flag(activity,asked);Prefs.put(activity,asked,true);requesting[0]=true;
        try{activity.requestPermissions(new String[]{permission},request);}catch(RuntimeException unavailable){requesting[0]=false;Prefs.put(activity,asked,previous);Ui.toast(activity,"Android could not open this permission request. Open SleepWell in Android Settings.");refresh.run();}
    }
    private static void row(Activity activity,LinearLayout body,String icon,String title,String subtitle,boolean allowed,boolean optional,Runnable action){
        int tint=allowed?0xff117b36:optional?Design.MUTED:0xffb56600;
        LinearLayout row=Design.row(activity);row.setMinimumHeight(Design.dp(activity,84));
        Design.Icon image=new Design.Icon(activity,icon,!allowed&&!optional?0xffb56600:Design.BROWN);Design.pad(image,10,10);image.setBackground(Design.shape(activity,!allowed&&!optional?0xffffecc0:Design.SOFT,15));
        LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(Design.dp(activity,48),Design.dp(activity,48));ip.rightMargin=Design.dp(activity,16);row.addView(image,ip);
        LinearLayout copy=Design.col(activity);Design.text(copy,title,16,600,Design.INK);Design.space(copy,5);Design.text(copy,subtitle,12,400,Design.MUTED);row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
        TextView state=Design.text(activity,allowed?"Allowed":optional?"Optional":"Needs attention",12,500,tint);state.setGravity(Gravity.END);state.setMaxWidth(Design.dp(activity,76));LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-2,-2);sp.leftMargin=Design.dp(activity,12);row.addView(state,sp);
        Design.Icon marker=new Design.Icon(activity,allowed?"check":optional?"minus":"attention",Design.CREAM);Design.pad(marker,4,4);marker.setBackground(Design.shape(activity,allowed?0xff17813b:optional?0xff999999:0xffdf860b,20));LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(Design.dp(activity,22),Design.dp(activity,22));mp.leftMargin=Design.dp(activity,8);row.addView(marker,mp);
        row.setContentDescription(title+" · "+state.getText());row.setBackground(Design.touch(activity,android.graphics.Color.TRANSPARENT,12));row.setOnClickListener(view->{if(row.isAttachedToWindow()&&live(activity)&&activity.hasWindowFocus())action.run();});body.addView(row);Design.line(body);
    }
    private AlarmReadinessUi(){}
}
