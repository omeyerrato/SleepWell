package org.sleepwell.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.*;

/** Reads actual Android readiness. Requests remain explicit and separate from optional recording access. */
final class AlarmReadinessUi {
 static final String MIC_ASKED="readinessMicrophoneAsked",NOTIFICATIONS_ASKED="readinessNotificationsAsked";
 static boolean notificationsDenied(Activity activity){return Prefs.flag(activity,NOTIFICATIONS_ASKED)&&!activity.getSystemService(NotificationManager.class).areNotificationsEnabled();}
 static void rows(Activity activity,LinearLayout body,Controller owner,boolean firstNight){
  NotificationManager notifications=activity.getSystemService(NotificationManager.class);
  Runnable notification=()->{if(firstNight&&notificationsDenied(activity))open(activity,notificationSettings(activity));else owner.permission(Manifest.permission.POST_NOTIFICATIONS,firstNight);};
  if(firstNight)row(activity,body,"bell","Notifications","Alarm alerts, bedtime reminders and recording status",notifications.areNotificationsEnabled(),false,firstNight,notification);
  row(activity,body,"alarm","Exact alarms","Allows alarms to play on time.",Scheduler.allowed(activity),false,firstNight,()->open(activity,Build.VERSION.SDK_INT>=31?new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,packageUri(activity)):appSettings(activity)));
  if(!firstNight)row(activity,body,"bell","Notifications",Build.VERSION.SDK_INT>=33?hint(activity,Manifest.permission.POST_NOTIFICATIONS,NOTIFICATIONS_ASKED,"Lets SleepWell show alerts."):"Lets SleepWell show alerts.",notifications.areNotificationsEnabled(),false,false,notification);
  if(Build.VERSION.SDK_INT>=34)row(activity,body,"phone","Full-screen alarms","Shows the alarm when the screen is locked.",notifications.canUseFullScreenIntent(),false,firstNight,()->open(activity,new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,packageUri(activity))));
  row(activity,body,"battery","Background battery","Review unrestricted battery use for reliable alarms.",activity.getSystemService(PowerManager.class).isIgnoringBatteryOptimizations(activity.getPackageName()),false,firstNight,()->battery(activity));
  if(!firstNight)row(activity,body,"mic","Microphone",hint(activity,Manifest.permission.RECORD_AUDIO,MIC_ASKED,"Used for sound events. Optional."),activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED,true,false,()->owner.permission(Manifest.permission.RECORD_AUDIO,false));
 }
 private static String hint(Activity activity,String permission,String asked,String description){
  if(activity.checkSelfPermission(permission)!=PackageManager.PERMISSION_GRANTED&&Prefs.flag(activity,asked)&&!activity.shouldShowRequestPermissionRationale(permission))return description+" Change access in app settings.";
  return description;
 }
 private static boolean live(Activity activity){return !activity.isFinishing()&&!activity.isDestroyed();}
 private static Uri packageUri(Activity activity){return Uri.parse("package:"+activity.getPackageName());}
 private static Intent appSettings(Activity activity){return new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,packageUri(activity));}
 private static Intent notificationSettings(Activity activity){return new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,activity.getPackageName());}
 static void battery(Activity activity){open(activity,new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));}
 private static void open(Activity activity,Intent intent){
  if(!live(activity)||!activity.hasWindowFocus())return;
  launchSettings(activity,intent);
 }
 private static void launchSettings(Activity activity,Intent intent){
  try{activity.startActivity(intent);}catch(RuntimeException unavailable){try{activity.startActivity(appSettings(activity));}catch(RuntimeException blocked){Ui.toast(activity,"Android could not open this setting. Open SleepWell in Android Settings.");}}
 }
 /** Owns explanation windows across root re-renders and recreation. Restoration never requests access. */
 static final class Controller {
  private final Activity activity;private final int request;private final Runnable refresh;private final boolean[] requesting={false};
  private SleepPicker.StateDialog dialog;private Bundle pending;private String selected="";private boolean first;
  Controller(Activity activity,int request,Runnable refresh,Bundle state){this.activity=activity;this.request=request;this.refresh=refresh;pending=state;}
  void returned(){requesting[0]=false;}
  void permission(String permission,boolean explainFirst){
   if(requesting[0]||dialog!=null||!live(activity)||!activity.hasWindowFocus())return;
   boolean notifications=permission.equals(Manifest.permission.POST_NOTIFICATIONS);
   if(notifications&&Build.VERSION.SDK_INT<33){open(activity,notificationSettings(activity));return;}
   if(activity.checkSelfPermission(permission)==PackageManager.PERMISSION_GRANTED){open(activity,notifications?notificationSettings(activity):appSettings(activity));return;}
   String asked=notifications?NOTIFICATIONS_ASKED:MIC_ASKED;
   if(explainFirst||activity.shouldShowRequestPermissionRationale(permission)){selected=permission;first=explainFirst;show(null);}
   else if(Prefs.flag(activity,asked))open(activity,appSettings(activity));
   else request(activity,requesting,permission,asked,request,refresh);
  }
  void information(){if(dialog!=null||!live(activity))return;selected="information";first=false;show(null);}
  Bundle save(){if(dialog==null||!dialog.isShowing())return pending;Bundle state=new Bundle();state.putString("selected",selected);state.putBoolean("first",first);state.putBundle("sheet",dialog.onSaveInstanceState());return state;}
  void restore(){if(pending==null||dialog!=null||!live(activity))return;Bundle state=pending;pending=null;String value=state.getString("selected","");if(!value.equals("information")&&!value.equals(Manifest.permission.POST_NOTIFICATIONS)&&!value.equals(Manifest.permission.RECORD_AUDIO))return;selected=value;first=state.getBoolean("first");show(state.getBundle("sheet"));}
  private void show(Bundle restored){
   String permission=selected;boolean info=permission.equals("information"),notifications=permission.equals(Manifest.permission.POST_NOTIFICATIONS);
   String asked=notifications?NOTIFICATIONS_ASKED:MIC_ASKED;
   boolean settings=!info&&(first&&Prefs.flag(activity,asked)||Prefs.flag(activity,asked)&&!activity.shouldShowRequestPermissionRationale(permission));
   String title=info?"Alarm reliability":first?"Notifications":notifications?"Allow notifications?":"Allow microphone access?";
   SleepPicker.StateDialog owned=new SleepPicker.StateDialog(activity,"alarm-readiness",title);dialog=owned;owned.requestWindowFeature(Window.FEATURE_NO_TITLE);
   LinearLayout content=Design.col(activity);Design.pad(content,22,24);content.setBackground(Design.shape(activity,NightDialTokens.RAISED,24));Design.text(content,title,23,600,NightDialTokens.TEXT);Design.space(content,16);
   if(info){Design.text(content,"Review notifications, exact alarms and background access in Android Settings. On Samsung, add SleepWell to Never auto sleeping apps. Force stopping or powering off the phone prevents alarms. A quiet test cannot prove overnight reliability.",15,400,NightDialTokens.SECONDARY);}
   else if(first){Design.text(content,"SleepWell uses notifications for:",15,400,NightDialTokens.SECONDARY);Design.space(content,14);explanation(content,"moon","Bedtime reminders","A reminder when it is time to wind down.");explanation(content,"alarm","Wake-up alerts","The notification for your morning alarm.");explanation(content,"report","Recording status","Updates while you record your night.");Design.space(content,8);Design.text(content,"You can continue using the app with notifications off. Microphone access is a separate, optional choice.",13,400,NightDialTokens.SECONDARY);}
   else Design.text(content,notifications?"SleepWell uses notifications to show alarm alerts and recording status.":"SleepWell uses the microphone for optional sound events and recordings. Allow access to use those features.",15,400,NightDialTokens.SECONDARY);
   Design.space(content,18);
   if(info)Design.button(content,"Got it",true,owned::dismiss);
   else{
    Design.button(content,settings?"Open Android settings":first?"Continue to Android":"Continue",true,()->{
     if(!live(activity)||!owned.isShowing()||owned.getWindow()==null||!owned.getWindow().getDecorView().hasWindowFocus())return;
     // Start the destination before dismissing its focused explanation owner.
     if(settings)launchSettings(activity,notifications?notificationSettings(activity):appSettings(activity));
     else request(activity,requesting,permission,asked,request,refresh);
     owned.dismiss();
    });
    Design.button(content,"Not now",false,owned::dismiss);
   }
   ScrollView scroll=new ScrollView(activity);scroll.addView(content);owned.scroll=scroll;owned.setContentView(scroll);owned.setOnDismissListener(ignored->{if(dialog==owned){dialog=null;selected="";}});SleepPicker.show(activity,owned,first?570:info?390:350);if(restored!=null)owned.onRestoreInstanceState(restored);
  }
  void close(){pending=null;if(dialog!=null)dialog.dismiss();}
 }
 private static void explanation(LinearLayout parent,String icon,String title,String detail){
  Activity activity=(Activity)parent.getContext();LinearLayout row=Design.row(activity);row.setMinimumHeight(Design.dp(activity,58));Design.Icon glyph=new Design.Icon(activity,icon,NightDialTokens.SECONDARY);LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(Design.dp(activity,24),Design.dp(activity,24));ip.rightMargin=Design.dp(activity,14);row.addView(glyph,ip);LinearLayout copy=Design.col(activity);Design.text(copy,title,15,500,NightDialTokens.TEXT);Design.space(copy,4);Design.text(copy,detail,13,400,NightDialTokens.SECONDARY);row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));parent.addView(row);
 }
 private static void request(Activity activity,boolean[] requesting,String permission,String asked,int request,Runnable refresh){
  if(requesting[0]||!live(activity))return;if(activity.checkSelfPermission(permission)==PackageManager.PERMISSION_GRANTED){refresh.run();return;}
  boolean previous=Prefs.flag(activity,asked);Prefs.put(activity,asked,true);requesting[0]=true;
  try{activity.requestPermissions(new String[]{permission},request);}catch(RuntimeException unavailable){requesting[0]=false;Prefs.put(activity,asked,previous);Ui.toast(activity,"Android could not open this permission request. Open SleepWell in Android Settings.");refresh.run();}
 }
 private static void row(Activity activity,LinearLayout body,String icon,String title,String subtitle,boolean allowed,boolean optional,boolean first,Runnable action){
  String state=allowed?"Allowed":optional?"Optional":first?"Not allowed":"Needs attention";
  LinearLayout card=Design.col(activity);Design.pad(card,16,12);card.setBackground(Design.touch(activity,NightDialTokens.SURFACE,16));LinearLayout head=Design.row(activity);Design.Icon glyph=new Design.Icon(activity,icon,NightDialTokens.SECONDARY);LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(Design.dp(activity,24),Design.dp(activity,24));ip.rightMargin=Design.dp(activity,12);head.addView(glyph,ip);head.addView(Design.text(activity,title,16,500,NightDialTokens.TEXT),new LinearLayout.LayoutParams(0,-2,1));TextView status=Design.text(activity,state,12,500,allowed?NightDialTokens.POSITIVE:optional?NightDialTokens.SECONDARY:NightDialTokens.NEGATIVE);status.setMaxWidth(Design.dp(activity,100));status.setGravity(Gravity.END);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-2,-2);sp.leftMargin=Design.dp(activity,12);head.addView(status,sp);card.addView(head);
  LinearLayout detail=Design.row(activity);TextView copy=Design.text(activity,subtitle,13,400,NightDialTokens.SECONDARY);LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-2,1);cp.rightMargin=Design.dp(activity,12);detail.addView(copy,cp);TextView review=Design.text(activity,first&&title.equals("Notifications")&&notificationsDenied(activity)?"Open settings":"Review",13,500,NightDialTokens.TEXT);review.setMinHeight(Design.dp(activity,48));review.setGravity(Gravity.CENTER);Design.pad(review,16,0);review.setBackground(Design.shape(activity,NightDialTokens.SECONDARY_ACTION,24));detail.addView(review,new LinearLayout.LayoutParams(-2,-2));Design.space(card,4);card.addView(detail);
  card.setContentDescription(title+" · "+state);card.setStateDescription(subtitle+". Review Android access.");card.setFocusable(true);card.setAccessibilityDelegate(new View.AccessibilityDelegate(){@Override public void onInitializeAccessibilityNodeInfo(View host,AccessibilityNodeInfo node){super.onInitializeAccessibilityNodeInfo(host,node);node.setClassName(Button.class.getName());}});card.setOnClickListener(view->{if(card.isAttachedToWindow()&&live(activity)&&activity.hasWindowFocus()){InteractionFeedback.action(view);action.run();}});body.addView(card,new LinearLayout.LayoutParams(-1,-2));Design.space(body,10);
 }
 private AlarmReadinessUi(){}
}
