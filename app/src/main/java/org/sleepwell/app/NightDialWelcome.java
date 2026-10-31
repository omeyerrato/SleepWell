package org.sleepwell.app;

import android.app.Activity;
import android.content.Intent;
import android.widget.LinearLayout;
import java.time.ZoneId;

/** Optional first-night presentation. Never creates a plan, alarm, permission request or recording. */
final class NightDialWelcome {
 static final String DONE="nightDialFirstNightDone";
 static boolean offer(Activity activity,Store store){
  Intent launch=activity.getIntent();
  if(!Intent.ACTION_MAIN.equals(launch.getAction())||!launch.hasCategory(Intent.CATEGORY_LAUNCHER)||launch.hasExtra("destination")||launch.hasExtra("homeSurfacePage")||launch.hasExtra("showReport")||launch.hasExtra("settingsPage")||launch.hasExtra("settingsGroup")||AlarmService.active||TrackingService.running)return false;
  // Saved data/preferences and explicit system/deep-link destinations keep their normal route.
  return Prefs.of(activity).getAll().isEmpty()&&store.alarms().isEmpty()&&store.rows("SELECT id FROM sessions LIMIT 1").isEmpty();
 }
 static boolean complete(Activity activity){return Prefs.of(activity).edit().putBoolean(DONE,true).commit();}
 static void render(Activity activity,LinearLayout body,Runnable setup,Runnable explore){
  Design.text(body,"SleepWell",30,600,NightDialTokens.TEXT);Design.space(body,8);
  Design.text(body,"Plan your night. Make room for rest.",16,400,NightDialTokens.SECONDARY);Design.space(body,18);
  Design.text(body,"Example night",12,500,NightDialTokens.META);
  long now=System.currentTimeMillis();ZoneId zone=ZoneId.systemDefault();NightWindow example=NightWindow.at(22*60+30,7*60,0,now,zone);
  NightArc preview=new NightArc(activity,22*60+30,7*60,0,now,zone,ignored->{});
  preview.exampleFace();
  preview.setOnClickListener(null);preview.setClickable(false);preview.setFocusable(false);preview.setContentDescription("Example plan: bedtime "+Design.clock(activity,22*60+30)+", wake-up "+Design.clock(activity,7*60)+". "+example.durationMinutes()/60+" hours "+example.durationMinutes()%60+" minutes planned.");
  body.addView(preview,new LinearLayout.LayoutParams(-1,-2));Design.space(body,8);
  Design.text(body,"Your night, on your device",20,500,NightDialTokens.TEXT);Design.space(body,8);
  Design.text(body,"Plan your sleep, settle in with sound and review your recorded nights. Start without an account.",15,400,NightDialTokens.SECONDARY);
  LinearLayout footer=RecordUi.footer(body);Design.button(footer,"Set up my night",true,setup);Design.button(footer,"Explore first",false,explore);
 }
 private NightDialWelcome(){}
}
