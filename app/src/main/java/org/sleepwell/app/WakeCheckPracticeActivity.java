package org.sleepwell.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

/** Private visual Practice only. Never opens, claims, schedules or cancels a real check. */
public final class WakeCheckPracticeActivity extends Activity {
    private int delay;private boolean acknowledged,resumed;
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);RecordUi.review(this);
        Intent input=getIntent();delay=input.getIntExtra("draftDelayMinutes",-1);
        if(delay<0||delay>15||input.getData()!=null||input.getClipData()!=null||input.getSelector()!=null){finish();return;}
        acknowledged=saved!=null&&saved.getBoolean("practiceAcknowledged");render();
    }
    private void render(){
        LinearLayout body=Design.page(this,false,null,null);RecordUi.header(body,"Wake-check Practice",this::finish);
        Design.space(body,16);Design.text(body,"Practice only",13,600,Design.BROWN);
        Design.text(body,delay==0?"Wake-up check is Off in your draft":"Draft delay · After "+delay+" min",18,600,Design.INK);
        Design.space(body,10);Design.text(body,"This is a static example. No check is running, no alarm will restart, and your settings stay unchanged.",15,400,Design.MUTED);
        Design.space(body,24);TextView title=Design.text(body,acknowledged?"Practice acknowledgement received":"Still awake?",30,600,Design.INK);title.setGravity(Gravity.CENTER);title.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        Design.space(body,16);FrameLayout circle=new FrameLayout(this);WakeCheckRing ring=new WakeCheckRing(this);circle.addView(ring,new FrameLayout.LayoutParams(-1,-1));
        TextView example=Design.text(this,acknowledged?"✓":"2:00",42,700,Design.INK);example.setGravity(Gravity.CENTER);example.setContentDescription(acknowledged?"Practice acknowledged":"Static Practice example: two minutes");circle.addView(example,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout.LayoutParams size=new LinearLayout.LayoutParams(Design.dp(this,200),Design.dp(this,200));size.gravity=Gravity.CENTER_HORIZONTAL;body.addView(circle,size);Design.space(body,12);Design.text(body,"Example · no running countdown",13,400,Design.MUTED).setGravity(Gravity.CENTER);
        LinearLayout footer=RecordUi.footer(body);footer.setTag("wake-check-practice-footer");
        if(!acknowledged)Design.button(footer,"Practice acknowledgement",true,()->{if(resumed&&hasWindowFocus()&&!isFinishing()&&!isDestroyed()){acknowledged=true;render();}});
        Design.button(footer,"Return to wake-up settings",acknowledged,this::finish);
    }
    @Override protected void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);state.putBoolean("practiceAcknowledged",acknowledged);}
    @Override protected void onResume(){super.onResume();resumed=true;}
    @Override protected void onPause(){resumed=false;super.onPause();}
    @Override protected void onNewIntent(Intent input){super.onNewIntent(input);finish();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
}
