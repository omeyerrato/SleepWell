package org.sleepwell.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

/** User feedback preference; opening or restoring this screen does not produce an effect. */
public final class FeedbackSettingsActivity extends Activity {
    private TextView status;private LinearLayout strengths;private Button preview;
    @Override public void onCreate(Bundle state){super.onCreate(state);RecordUi.review(this);render();}
    private void render(){
        LinearLayout body=Design.page(this,false,null,n->{});
        RecordUi.header(body,"Touch feedback",this::finish);Design.space(body,16);
        Switch touch=Design.toggle(body,"Touch feedback","Feel taps on navigation and controls",InteractionFeedback.enabled(this),enabled->{
            boolean saved=InteractionFeedback.setEnabled(this,enabled);
            if(status!=null)status.setText(saved?"Saved":"Could not save. Reopen this setting to check its value.");
            if(preview!=null){boolean active=InteractionFeedback.enabled(this);preview.setEnabled(active);preview.setAlpha(active?1f:.45f);}
        });touch.setContentDescription("Touch feedback");
        Design.space(body,16);Design.line(body);RecordUi.heading(body,"Feedback strength");strengths=Design.row(this);strengths.setBackground(Design.shape(this,Design.SOFT,14));body.addView(strengths);renderStrengths();
        Design.space(body,10);Design.text(body,"Intensity also follows your phone’s touch settings.",12,400,Design.MUTED);Design.space(body,14);
        preview=new Button(this);preview.setText("Try feedback");preview.setAllCaps(false);preview.setTypeface(Design.font(this,600));preview.setTextSize(16*Appearance.scale(this));preview.setTextColor(Design.INK);preview.setStateListAnimator(null);preview.setElevation(0);preview.setMinimumHeight(Design.dp(this,52));preview.setMinHeight(Design.dp(this,52));preview.setBackground(Design.touch(this,Design.SOFT,16));preview.setOnClickListener(InteractionFeedback::action);preview.setEnabled(InteractionFeedback.enabled(this));preview.setAlpha(preview.isEnabled()?1f:.45f);body.addView(preview,new LinearLayout.LayoutParams(-1,-2));
        Design.space(body,12);Design.text(body,"Quiet during alarms, sleep recording and touch exploration.",12,400,Design.MUTED);
        Design.space(body,14);status=Design.text(body,"",13,400,Design.MUTED);status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        Design.space(body,16);Design.line(body);Design.item(body,"palette","Theme & motion",null,()->startActivity(RecordUi.intent(this,AppearanceActivity.class)));
    }
    private void renderStrengths(){
        strengths.removeAllViews();int selected=InteractionFeedback.strength(this);String[] names={"Light","Standard","Firm"};
        for(int n=0;n<names.length;n++){int strength=n;boolean checked=n==selected;FrameLayout hit=new FrameLayout(this);TextView label=Design.text(this,names[n],14,checked?600:400,checked?Design.INK:Design.MUTED);label.setGravity(Gravity.CENTER);if(checked)label.setBackground(Design.shape(this,Design.CREAM,12));FrameLayout.LayoutParams face=new FrameLayout.LayoutParams(-1,Design.dp(this,36),Gravity.CENTER);face.setMargins(Design.dp(this,3),0,Design.dp(this,3),0);hit.addView(label,face);hit.setFocusable(true);hit.setContentDescription("Feedback strength: "+names[n]);SleepPicker.radio(hit,checked);hit.setOnClickListener(view->{if(InteractionFeedback.strength(this)==strength)return;boolean saved=InteractionFeedback.setStrength(this,strength);if(status!=null)status.setText(saved?names[strength]+" saved"+(InteractionFeedback.enabled(this)?"":" · touch feedback is off"):"Could not save strength. Try again.");if(saved){InteractionFeedback.selection(view);renderStrengths();}});strengths.addView(hit,new LinearLayout.LayoutParams(0,Design.dp(this,48),1));}
    }
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
}
