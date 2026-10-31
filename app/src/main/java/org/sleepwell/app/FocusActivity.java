package org.sleepwell.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.time.LocalTime;
import java.time.ZoneId;

/** The guard's interruption screen. Navigation never starts sleep tracking. */
public class FocusActivity extends Activity {
    private TextView status;

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        LinearLayout body=Design.darkPage(this);Design.pad(body,24,16);
        LinearLayout header=Design.row(this);
        LinearLayout.LayoutParams shield=new LinearLayout.LayoutParams(Design.dp(this,25),Design.dp(this,25));shield.rightMargin=Design.dp(this,12);
        header.addView(new Design.Icon(this,"shield",Design.LILAC),shield);
        header.addView(Design.text(this,"Bedtime guard",20,600,NightDialTokens.TEXT));header.setMinimumHeight(Design.dp(this,48));body.addView(header);
        Design.space(body,24);Design.text(body,"It's time to rest",30,600,NightDialTokens.TEXT);Design.space(body,10);
        status=Design.text(body,"",15,400,Design.LILAC);Design.space(body,24);
        ImageView scene=new ImageView(this);scene.setImageResource(R.drawable.bedside_tonight);scene.setScaleType(ImageView.ScaleType.CENTER_CROP);
        scene.setImportantForAccessibility(ImageView.IMPORTANT_FOR_ACCESSIBILITY_NO);scene.setBackground(Design.shape(this,NightDialTokens.SURFACE,22));scene.setClipToOutline(true);android.graphics.ColorMatrix tonality=new android.graphics.ColorMatrix();tonality.setSaturation(0);scene.setColorFilter(new android.graphics.ColorMatrixColorFilter(tonality));
        body.addView(scene,new LinearLayout.LayoutParams(-1,Design.dp(this,174)));Design.space(body,18);
        LinearLayout settings=Design.row(this);settings.setMinimumHeight(Design.dp(this,56));
        LinearLayout.LayoutParams icon=new LinearLayout.LayoutParams(Design.dp(this,24),Design.dp(this,24));icon.rightMargin=Design.dp(this,14);
        settings.addView(new Design.Icon(this,"settings-outline",Design.LILAC),icon);
        settings.addView(Design.text(this,"Bedtime guard settings",15,500,NightDialTokens.TEXT),new LinearLayout.LayoutParams(0,-2,1));
        settings.addView(new Design.Icon(this,"chevron",Design.LILAC),new LinearLayout.LayoutParams(Design.dp(this,20),Design.dp(this,20)));
        settings.setContentDescription("Bedtime guard settings");settings.setFocusable(true);settings.setBackground(Design.touch(this,android.graphics.Color.TRANSPARENT,12));
        settings.setOnClickListener(view->{InteractionFeedback.action(view);startActivity(RecordUi.intent(this,GuardActivity.class));});body.addView(settings);
        LinearLayout footer=RecordUi.footer(body);
        nightButton(footer,"Open Wind-down",true,()->{startActivity(RecordUi.intent(this,MainActivity.class).putExtra("settingsPage","wind_down"));finish();});
        nightButton(footer,"Pause guard for 5 minutes",false,()->ReportDialogs.show(new AlertDialog.Builder(this)
                .setTitle("Pause bedtime guard?").setMessage("Your blocked apps will be available for five minutes. The guard then resumes on its schedule.")
                .setPositiveButton("Pause for 5 minutes",(dialog,which)->{Prefs.put(this,"focusPauseUntil",System.currentTimeMillis()+5*60000L);finish();})
                .setNegativeButton("Keep guard on",null)));
        Button home=nightButton(footer,"Go to home screen",false,()->{startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));finish();});
        home.setBackground(Design.touch(this,android.graphics.Color.TRANSPARENT,16));
    }

    private Button nightButton(LinearLayout parent,String title,boolean primary,Runnable action){
        Button button=Design.button(parent,title,false,action);button.setTextColor(primary?Design.PLUM:Design.LILAC);
        button.setBackground(primary?Design.touch(this,Design.LILAC,18):Design.outline(this,NightDialTokens.SURFACE,NightDialTokens.OUTLINE,18));return button;
    }

    @Override protected void onResume(){
        super.onResume();
        try{
            if(!Prefs.flag(this,"bedGuard")){status.setText("Bedtime guard is off. You can manage it in settings.");return;}
            long paused=Prefs.of(this).getLong("focusPauseUntil",0);
            if(paused>System.currentTimeMillis()){status.setText("Guard paused until "+RecordUi.clock(this,paused,ZoneId.systemDefault())+".");return;}
            if(!AlarmProtectionActivity.access(this)){status.setText("Bedtime guard needs accessibility access. Open settings to review it.");return;}
            SleepPlan plan=SleepPlans.load(this);LocalTime now=LocalTime.now();
            status.setText(plan.guardActive(now.getHour()*60+now.getMinute())?"Your selected apps are blocked until "+Design.clock(this,plan.wake)+".":"Your next wind-down starts at "+Design.clock(this,Math.floorMod(plan.bedtime-plan.windDown,1440))+".");
        }catch(RuntimeException unavailable){status.setText("Open bedtime guard settings to review your schedule.");}
    }

    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)Design.focusedSystemBars(this,true);}
}
