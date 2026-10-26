package org.sleepwell.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;

/** User feedback preference; opening or restoring this screen does not produce an effect. */
public final class FeedbackSettingsActivity extends Activity {
    private TextView status;
    @Override public void onCreate(Bundle state){super.onCreate(state);RecordUi.review(this);render();}
    private void render(){
        LinearLayout body=Design.page(this,false,null,n->{});
        RecordUi.header(body,"Touch feedback",this::finish);Design.text(body,"Settings · Appearance & feedback",12,400,Design.MUTED);Design.space(body,20);
        Switch touch=Design.toggle(body,"Touch feedback","Subtle haptics for navigation and controls",InteractionFeedback.enabled(this),enabled->{
            boolean saved=InteractionFeedback.setEnabled(this,enabled);
            if(status!=null)status.setText(saved?"Saved":"Could not save. Reopen this setting to check its value.");
        });touch.setContentDescription("Touch feedback");
        Design.space(body,12);Design.text(body,"Follows Android’s touch-feedback setting. Quiet during alarms, recording and accessibility touch exploration.",13,400,Design.MUTED);
        Design.space(body,14);status=Design.text(body,"",13,400,Design.MUTED);
        Design.space(body,20);Design.item(body,"palette","Theme & motion",null,()->startActivity(RecordUi.intent(this,AppearanceActivity.class)));
    }
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
}
