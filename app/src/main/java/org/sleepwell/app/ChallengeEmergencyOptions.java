package org.sleepwell.app;

import android.app.*;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.util.function.BooleanSupplier;

/** An owned options sheet; none of its actions completes, stops or snoozes an alarm. */
final class ChallengeEmergencyOptions {
    static Dialog show(Activity owner,boolean practice,String run,int unit,BooleanSupplier current,Runnable configuredMath,Bundle restore){
        SleepPicker.StateDialog dialog=new SleepPicker.StateDialog(owner,"challenge-emergency","Emergency options");dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout panel=new ReviewPanel(owner);Design.pad(panel,20,22);panel.setBackground(Design.shape(owner,NightDialTokens.RAISED,24));panel.setFilterTouchesWhenObscured(true);
        Design.text(panel,"Emergency options",23,600,Design.INK);Design.space(panel,14);LinearLayout content=Design.col(owner);ScrollView scroll=new ScrollView(owner);scroll.addView(content);panel.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));LinearLayout actions=Design.col(owner);panel.addView(actions,new LinearLayout.LayoutParams(-1,-2));Design.text(content,practice?"Practice only. These options do not start or stop an alarm.":"Your alarm keeps running. Opening these options does not finish or skip the current task.",14,500,Design.MUTED);Design.space(content,14);
        ComponentName dialer=ChallengeDialer.target(owner);Design.text(content,"Open your default dialer. SleepWell supplies no number and does not place a call.",13,400,Design.MUTED);TextView error=Design.text(content,dialer==null?"No default system dialer is available.":"",13,500,NightDialTokens.NEGATIVE);error.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        BooleanSupplier admitted=()->dialog.isShowing()&&dialog.getWindow()!=null&&dialog.getWindow().getDecorView().hasWindowFocus()&&!owner.isFinishing()&&!owner.isDestroyed()&&current.getAsBoolean();
        Design.button(actions,"Open system dialer",false,()->{if(!admitted.getAsBoolean())return;try{ChallengeDialer.open(owner,dialer,run,unit,!practice);dialog.dismiss();}catch(IllegalStateException unavailable){error.setText(unavailable.getMessage());}}).setEnabled(dialer!=null);
        Design.space(content,12);Design.text(content,configuredMath==null?"This task has no separately configured Math fallback. Its own supported alternatives remain available in the task.":"Use this task’s saved Math fallback settings. Choosing the alternative does not complete a round.",13,400,Design.MUTED);
        if(configuredMath!=null)Design.button(actions,"Use configured Math fallback",false,()->{if(!admitted.getAsBoolean())return;configuredMath.run();dialog.dismiss();});
        Design.button(actions,"Return to current task",true,()->{if(dialog.isShowing()&&dialog.getWindow().getDecorView().hasWindowFocus())dialog.dismiss();});dialog.scroll=scroll;dialog.setContentView(panel);SleepPicker.show(owner,dialog,590);if(restore!=null)dialog.onRestoreInstanceState(restore);return dialog;
    }
    private static final class ReviewPanel extends LinearLayout {private final SensitiveReviewTouches touches=new SensitiveReviewTouches();ReviewPanel(Activity owner){super(owner);setOrientation(VERTICAL);}@Override public boolean dispatchTouchEvent(MotionEvent event){return touches.dispatch(getContext(),event,super::dispatchTouchEvent);}}
    private ChallengeEmergencyOptions(){}
}
