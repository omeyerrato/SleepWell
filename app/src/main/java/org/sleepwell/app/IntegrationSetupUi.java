package org.sleepwell.app;

import android.app.Activity;
import android.content.*;
import android.widget.*;

/** Readable setup information and explicit local copy actions; no connection or delivery probes. */
final class IntegrationSetupUi {
    static LinearLayout section(LinearLayout parent,String title){
        Design.space(parent,12);LinearLayout card=Design.col(parent.getContext());Design.pad(card,16,14);card.setBackground(Design.shape(parent.getContext(),Design.SOFT,18));Design.text(card,title,17,600,Design.INK);Design.space(card,8);parent.addView(card,new LinearLayout.LayoutParams(-1,-2));return card;
    }
    static void detail(LinearLayout parent,String text){Design.text(parent,text,13,400,Design.MUTED);Design.space(parent,8);}
    static void step(LinearLayout parent,int number,String text){Design.text(parent,number+". "+text,14,400,Design.INK);Design.space(parent,10);}
    static void copy(Activity owner,LinearLayout parent,String label,String value){
        TextView text=Design.text(parent,value,13,500,Design.BROWN);text.setTextIsSelectable(true);Design.button(parent,label,false,()->{try{ClipboardManager clipboard=(ClipboardManager)owner.getSystemService(Context.CLIPBOARD_SERVICE);if(clipboard==null)throw new IllegalStateException();clipboard.setPrimaryClip(ClipData.newPlainText(label,value));Ui.toast(owner,"Copied");}catch(RuntimeException unavailable){Ui.toast(owner,"Clipboard unavailable. You can select the text above.");}});
    }
    static Button pairedAction(LinearLayout parent,String title,boolean enabled,Runnable action){LinearLayout column=Design.col(parent.getContext());LinearLayout.LayoutParams width=new LinearLayout.LayoutParams(0,-2,1);if(parent.getChildCount()>0)width.leftMargin=Design.dp(parent.getContext(),8);parent.addView(column,width);Button button=Design.button(column,title,false,action);button.setTextSize(13*Appearance.scale(parent.getContext()));button.setEnabled(enabled);return button;}
    private IntegrationSetupUi(){}
}
