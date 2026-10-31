package org.sleepwell.app;

import android.app.AlertDialog;
import android.app.Activity;
import android.app.Dialog;
import android.view.*;
import android.widget.*;

/** Supporting confirmations retain native semantics and share Night Dial typography. */
final class ReportDialogs {
    static AlertDialog show(AlertDialog.Builder builder){AlertDialog dialog=builder.show();style(dialog);return dialog;}
    static void message(Activity activity,String title,String value){Dialog dialog=new Dialog(activity);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);LinearLayout body=Design.col(activity);Design.pad(body,22,22);body.setBackground(Design.shape(activity,Design.CREAM,24));Design.text(body,title,22,600,Design.INK);Design.space(body,18);Design.text(body,value,15,400,Design.MUTED);Design.space(body,16);Design.button(body,"Got it",true,dialog::dismiss);ScrollView scroll=new ScrollView(activity);scroll.addView(body);dialog.setContentView(scroll);dialog.show();dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));dialog.getWindow().setLayout(activity.getResources().getDisplayMetrics().widthPixels-Design.dp(activity,32),-2);}
    static void style(AlertDialog dialog){
        android.content.Context context=dialog.getContext();Window window=dialog.getWindow();NightDialMotion.configureSheet(dialog,context);window.setBackgroundDrawable(Design.shape(context,NightDialTokens.RAISED,24));window.setDimAmount(.6f);window.setGravity(Gravity.BOTTOM);WindowManager.LayoutParams placement=window.getAttributes();placement.y=Design.dp(context,12);window.setAttributes(placement);text(window.getDecorView());
        // Retain the actual native Buttons, IDs and AlertController callbacks. A horizontal
        // framework button bar cannot fit three descriptive consent actions on a phone.
        LinearLayout actions=Design.col(context);Design.pad(actions,20,16);ViewGroup oldBar=null;
        for(int which:new int[]{AlertDialog.BUTTON_POSITIVE,AlertDialog.BUTTON_NEGATIVE,AlertDialog.BUTTON_NEUTRAL}){
            Button button=dialog.getButton(which);if(button==null)continue;if(button.getParent() instanceof ViewGroup parent){if(oldBar==null)oldBar=parent;parent.removeView(button);}
            boolean primary=which==AlertDialog.BUTTON_POSITIVE,quiet=which==AlertDialog.BUTTON_NEUTRAL;
            button.setAllCaps(false);button.setTypeface(Design.font(context,600));button.setTextSize(16*Appearance.scale(context));button.setTextColor(primary?NightDialTokens.ACTION_INK:quiet?NightDialTokens.SECONDARY:NightDialTokens.TEXT);button.setSingleLine(false);button.setMaxLines(3);button.setEllipsize(null);button.setMinHeight(Design.dp(context,52));button.setMinimumHeight(Design.dp(context,52));button.setGravity(Gravity.CENTER);Design.pad(button,16,12);button.setStateListAnimator(null);button.setElevation(0);button.setBackground(Design.touch(context,primary?NightDialTokens.ACTION:quiet?android.graphics.Color.TRANSPARENT:NightDialTokens.SECONDARY_ACTION,100));
            LinearLayout.LayoutParams position=new LinearLayout.LayoutParams(-1,-2);if(actions.getChildCount()>0)position.topMargin=Design.dp(context,8);actions.addView(button,position);
        }
        if(oldBar!=null&&oldBar.getParent() instanceof ViewGroup parent){int index=parent.indexOfChild(oldBar);ViewGroup.LayoutParams position=oldBar.getLayoutParams();actions.setId(oldBar.getId());parent.removeView(oldBar);parent.addView(actions,index,position);}
        window.setLayout(Math.min(Design.dp(context,480),context.getResources().getDisplayMetrics().widthPixels-Design.dp(context,32)),-2);
    }
    private static void text(View view){if(view instanceof TextView){TextView label=(TextView)view;label.setTypeface(Design.font(view.getContext(),view instanceof Button?600:400));label.setTextColor(Design.INK);if(label instanceof EditText)label.setHintTextColor(Design.MUTED);}if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int index=0;index<group.getChildCount();index++)text(group.getChildAt(index));}}
}
