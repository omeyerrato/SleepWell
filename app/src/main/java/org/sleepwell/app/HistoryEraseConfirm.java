package org.sleepwell.app;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.widget.*;

/** SleepWell-owned confirmation: cancellation never invokes a cleanup action. */
final class HistoryEraseConfirm {
    static Dialog create(Context context,boolean cleanup,Runnable confirmed){
        Dialog dialog=new Dialog(context);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout root=Design.col(context);Design.pad(root,22,22);root.setBackground(Design.shape(context,Design.CREAM,24));
        Design.text(root,cleanup?"Finish audio cleanup?":"Delete recordings and nights?",22,600,Design.INK);Design.space(root,16);
        Design.text(root,cleanup?"Retry removing audio left by the earlier deletion. New sleep records and alarms remain.":"This removes local sleep sessions, graphs, notes and audio. Alarms remain. Backups and copies already shared with other apps stay where you saved them.",15,400,Design.MUTED);Design.space(root,18);
        Button positive=Design.button(root,cleanup?"Retry audio cleanup":"Delete history",true,()->{if(!dialog.isShowing())return;dialog.dismiss();confirmed.run();});positive.setContentDescription(cleanup?"Confirm audio cleanup":"Confirm history deletion");
        Design.button(root,"Keep",false,dialog::dismiss).setContentDescription("Keep history");
        ScrollView scroll=new ScrollView(context);scroll.setVerticalScrollBarEnabled(false);scroll.addView(root);dialog.setContentView(scroll);
        dialog.setOnShowListener(ignored->{Window window=dialog.getWindow();window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));int width=Math.min(context.getResources().getDisplayMetrics().widthPixels-Design.dp(context,32),Design.dp(context,420));window.setLayout(width,WindowManager.LayoutParams.WRAP_CONTENT);});
        return dialog;
    }
}
