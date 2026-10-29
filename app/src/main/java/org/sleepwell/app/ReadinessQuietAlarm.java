package org.sleepwell.app;

import android.Manifest;
import android.app.*;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.*;
import android.widget.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** An explicit real-alarm review. Opening readiness never schedules or plays anything. */
final class ReadinessQuietAlarm {
    private static final String REQUESTED="readinessQuietAlarmAt";

    static void entry(Activity activity,LinearLayout body){
        Button button=Design.button(body,"Test a quiet alarm",false,()->{if(live(activity)&&body.isAttachedToWindow()&&activity.hasWindowFocus())review(activity,body);});
        button.setText(RecordUi.withIcon(activity,"Test a quiet alarm","play",Design.BROWN));
        try{long last=Prefs.of(activity).getLong(REQUESTED,0);if(last>0){Design.space(body,4);Design.text(body,"Last requested for "+clock(activity,last)+". Delivery has not been checked here.",12,400,Design.MUTED);}}
        catch(RuntimeException unavailable){Design.text(body,"The last test request could not be read.",12,400,Design.MUTED);}
    }

    private static void review(Activity activity,LinearLayout owner){
        Dialog dialog=new Dialog(activity);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout body=Design.col(activity);Design.pad(body,22,22);body.setBackground(Design.shape(activity,Design.CREAM,24));
        Design.text(body,"Test a quiet alarm",23,600,Design.INK);Design.space(body,12);
        Design.text(body,"This schedules a real alarm in 10 seconds. Solve one Math question to stop it.",15,400,Design.INK);Design.space(body,16);
        PlanActivity.detail(body,"sound","Daybreak","Capped at 25%; uses the current phone volume","25%",null);Design.line(body);
        PlanActivity.detail(body,"math","One Math question","Your saved alarms stay unchanged",null,null);Design.line(body);
        Design.space(body,12);Design.text(body,"Vibration, flashlight, sunrise, boost, pinning and backup are off. This test has no automatic stop.",13,400,Design.MUTED);Design.space(body,12);
        TextView status=Design.text(body,"",13,500,Design.BROWN);status.setContentDescription("Quiet alarm request status");status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        String blocked=unavailable(activity);status.setText(blocked);status.setVisibility(blocked.isEmpty()?View.GONE:View.VISIBLE);
        boolean[] scheduled={false};Button[] confirm={null};Button[] close={null};
        confirm[0]=Design.button(body,"Schedule in 10 seconds",true,()->{
            if(scheduled[0]||!live(activity)||!owner.isAttachedToWindow()||!dialog.isShowing()||dialog.getWindow()==null||!dialog.getWindow().getDecorView().hasWindowFocus())return;
            String problem=unavailable(activity);if(!problem.isEmpty()){status.setText(problem);status.setVisibility(View.VISIBLE);confirm[0].setEnabled(false);confirm[0].setAlpha(.45f);return;}
            try{synchronized(Scheduler.class){
                problem=unavailable(activity);if(!problem.isEmpty())throw new IllegalStateException(problem);
                Alarm source=new Alarm(-1);source.label="Quiet alarm test";source.challenge="Math";source.difficulty=1;source.smartWindow=0;source.volume=25;source.snoozeLimit=0;source.tone="daybreak";
                long deadline=Scheduler.test(activity,source);scheduled[0]=true;
                confirm[0].setEnabled(false);confirm[0].setAlpha(.45f);close[0].setText("Done");
                boolean noted;try{noted=Prefs.of(activity).edit().putLong(REQUESTED,deadline).commit();}catch(RuntimeException unavailable){noted=false;}
                status.setText("Scheduled for "+clock(activity,deadline)+". Check that it rings and the Math screen opens."+(noted?"":" The local request note could not be saved."));status.setVisibility(View.VISIBLE);
            }}catch(RuntimeException failure){status.setText(scheduled[0]?"The quiet test was requested, but its status could not be shown.":"The test request could not be confirmed. "+(failure.getMessage()==null?"Check alarm access before trying again.":failure.getMessage()));status.setVisibility(View.VISIBLE);}
        });
        confirm[0].setEnabled(blocked.isEmpty());confirm[0].setAlpha(blocked.isEmpty()?1:.45f);close[0]=Design.button(body,"Cancel",false,dialog::dismiss);
        ScrollView scroll=new ScrollView(activity);scroll.setVerticalScrollBarEnabled(false);scroll.addView(body);dialog.setContentView(scroll);
        View.OnAttachStateChangeListener lifetime=new View.OnAttachStateChangeListener(){public void onViewAttachedToWindow(View view){}public void onViewDetachedFromWindow(View view){dialog.dismiss();}};
        owner.addOnAttachStateChangeListener(lifetime);dialog.setOnDismissListener(ignored->owner.removeOnAttachStateChangeListener(lifetime));SleepPicker.show(activity,dialog,600);
    }

    private static String unavailable(Context context){
        try{
            if(AlarmService.active||Prefs.flag(context,"ringing"))return "An alarm is active. Finish it before starting this test.";
            if(TrackingService.running||Prefs.flag(context,"tracking"))return "Finish sleep tracking before starting this test.";
            WakeCheckState check=WakeChecks.open(context).read();if(check!=null&&check.active())return "Finish the active wake-up check before testing.";
            long requested=Prefs.of(context).getLong(REQUESTED,0);if(requested>System.currentTimeMillis())return "A quiet test was already requested for "+clock(context,requested)+". Wait for that request first.";
            if(!Scheduler.allowed(context))return "Allow Exact alarms above, then reopen this review.";
            NotificationManager notifications=context.getSystemService(NotificationManager.class);
            if(!notifications.areNotificationsEnabled()||Build.VERSION.SDK_INT>=33&&context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return "Allow Notifications above, then reopen this review.";
            if(Build.VERSION.SDK_INT>=34&&!notifications.canUseFullScreenIntent())return "Allow Full-screen alarms above, then reopen this review.";
            NotificationChannel channel=notifications.getNotificationChannel(Notifications.ALARM);if(channel!=null&&channel.getImportance()<NotificationManager.IMPORTANCE_HIGH)return "Enable high-priority Wake-up alarms in Android notification settings, then reopen this review.";
            return "";
        }catch(RuntimeException unavailable){return "Quiet alarm settings could not be read. Reopen Alarm readiness before testing.";}
    }
    private static boolean live(Activity activity){return !activity.isFinishing()&&!activity.isDestroyed();}
    private static String clock(Context context,long at){return Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(android.text.format.DateFormat.is24HourFormat(context)?"HH:mm:ss":"h:mm:ss a",Locale.getDefault()));}
    private ReadinessQuietAlarm(){}
}
