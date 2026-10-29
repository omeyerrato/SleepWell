package org.sleepwell.app;

import android.app.AlertDialog;
import android.app.Activity;
import android.app.Dialog;
import android.view.*;
import android.widget.*;

/** Uses the report's typography and warm surfaces in supporting edit dialogs. */
final class ReportDialogs {
    static AlertDialog show(AlertDialog.Builder builder){AlertDialog dialog=builder.show();style(dialog);return dialog;}
    static void message(Activity activity,String title,String value){Dialog dialog=new Dialog(activity);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);LinearLayout body=Design.col(activity);Design.pad(body,22,22);body.setBackground(Design.shape(activity,Design.CREAM,24));Design.text(body,title,22,600,Design.INK);Design.space(body,18);Design.text(body,value,15,400,Design.MUTED);Design.space(body,16);Design.button(body,"Got it",true,dialog::dismiss);ScrollView scroll=new ScrollView(activity);scroll.addView(body);dialog.setContentView(scroll);dialog.show();dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));dialog.getWindow().setLayout(activity.getResources().getDisplayMetrics().widthPixels-Design.dp(activity,32),-2);}
    static void style(AlertDialog dialog){dialog.getWindow().setBackgroundDrawable(Design.shape(dialog.getContext(),Design.CREAM,24));text(dialog.getWindow().getDecorView());for(int which:new int[]{AlertDialog.BUTTON_POSITIVE,AlertDialog.BUTTON_NEGATIVE,AlertDialog.BUTTON_NEUTRAL}){Button button=dialog.getButton(which);if(button==null)continue;button.setAllCaps(false);button.setTypeface(Design.font(dialog.getContext(),600));button.setTextColor(Design.BROWN);button.setMinHeight(Design.dp(dialog.getContext(),48));}}
    private static void text(View view){if(view instanceof TextView){TextView label=(TextView)view;label.setTypeface(Design.font(view.getContext(),view instanceof Button?600:400));label.setTextColor(Design.INK);if(label instanceof EditText)label.setHintTextColor(Design.MUTED);}if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int index=0;index<group.getChildCount();index++)text(group.getChildAt(index));}}
}
