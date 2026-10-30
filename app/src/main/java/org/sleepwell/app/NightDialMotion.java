package org.sleepwell.app;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.PathInterpolator;

/** Native motion under the hand. Never animates an entrance for page content or starts output. */
final class NightDialMotion {
    static final int PRESS=100, SELECTION=120, SWITCH=160, MENU_IN=160,
            MENU_OUT=120, SHEET_IN=240, SHEET_OUT=180, DIAL_SETTLE=180;
    static final PathInterpolator EASE_OUT=new PathInterpolator(.22f,1f,.36f,1f);
    static boolean enabled(Context context){return InteractionFeedback.motion(context);}
    static void configureSheet(Dialog dialog, Context context){
        Window window=dialog.getWindow();if(window==null)return;
        window.setWindowAnimations(enabled(context)?R.style.NightDialSheetAnimation:0);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }
    /** Selection is announced by the control; this only settles newly focused visual emphasis. */
    static void cancel(View view){if(view!=null)view.animate().cancel();}
    private NightDialMotion(){}
}
