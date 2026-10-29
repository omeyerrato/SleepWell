package org.sleepwell.app;

import android.app.*;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.*;
import java.util.*;
import java.util.function.IntConsumer;

/** Action affordances never borrow the chevron used for task settings navigation. */
final class ChallengeActionRows {
    static View add(LinearLayout parent,String icon,String title,String detail,boolean selected,boolean allowed,String reason,Runnable action){android.content.Context c=parent.getContext();LinearLayout row=base(parent,icon,title,detail+(reason.isEmpty()?"":"\n"+reason));row.setSelected(selected);FrameLayout status=new FrameLayout(c);status.setBackground(Design.shape(c,Design.SOFT,24));status.addView(new Design.Icon(c,selected?"check":"plus",allowed||selected?Design.BROWN:Design.MUTED),new FrameLayout.LayoutParams(Design.dp(c,24),Design.dp(c,24),Gravity.CENTER));row.addView(status,new LinearLayout.LayoutParams(Design.dp(c,48),Design.dp(c,48)));boolean enabled=allowed&&!selected;row.setEnabled(enabled);row.setFocusable(true);row.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);row.setContentDescription((selected?"Already in pool ":enabled?"Add to pool ":"Cannot add ")+title+(reason.isEmpty()?"":", "+reason));if(enabled){row.setBackground(Design.touch(c,android.graphics.Color.TRANSPARENT,12));row.setOnClickListener(v->{InteractionFeedback.action(v);action.run();});row.setAccessibilityDelegate(new View.AccessibilityDelegate(){@Override public void onInitializeAccessibilityNodeInfo(View host,AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(host,info);info.setClassName(Button.class.getName());}});}return row;}
    static View configure(LinearLayout parent,String icon,String title,String detail,boolean allowed,Runnable action){android.content.Context c=parent.getContext();LinearLayout row=base(parent,icon,title,detail);row.addView(new Design.Icon(c,"chevron",allowed?Design.BROWN:Design.MUTED),new LinearLayout.LayoutParams(Design.dp(c,22),Design.dp(c,48)));row.setContentDescription("Configure "+title+(allowed?"":", routine full; remove a task first"));row.setFocusable(true);row.setEnabled(allowed);if(allowed){row.setBackground(Design.touch(c,android.graphics.Color.TRANSPARENT,12));row.setOnClickListener(v->{InteractionFeedback.action(v);action.run();});}return row;}
    private static LinearLayout base(LinearLayout parent,String icon,String title,String detail){android.content.Context c=parent.getContext();LinearLayout row=Design.row(c);row.setMinimumHeight(Design.dp(c,68));LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(Design.dp(c,28),Design.dp(c,28));gp.rightMargin=Design.dp(c,16);row.addView(new Design.Icon(c,icon,Design.BROWN),gp);LinearLayout copy=Design.col(c);Design.text(copy,title,16,600,Design.INK);if(!detail.isEmpty()){Design.space(copy,4);Design.text(copy,detail,12,400,Design.MUTED);}LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-2,1);cp.topMargin=Design.dp(c,8);cp.bottomMargin=Design.dp(c,8);cp.rightMargin=Design.dp(c,10);row.addView(copy,cp);parent.addView(row);return row;}
    static Dialog actions(Activity activity,String title,List<String> labels,IntConsumer choose){
        String[] icons=labels.stream().map(label->label.equals("Move up")?"up":label.equals("Move down")?"down":"trash").toArray(String[]::new);
        return SleepPicker.actions(activity,title,labels.toArray(String[]::new),icons,choose);
    }
    static String icon(String task){return switch(task){case "Shake"->"phone";case "Spin around","Walk"->"walk";case "Camera light","Light"->"sun";case "Type code"->"keyboard";case "Pose"->"walk";case "Smile"->"moon";case "Spoken phrase","Speak","Laughter","Loud sound","Musical note"->"mic";case "Object recognition"->"camera";case "Picture match","QR code"->"qr";case "Visual search","Jumping"->"moon";case "Counting"->"math";case "Tilt maze"->"phone";case "Flags"->"tasks";case "Memory"->"memory";case "Sequence"->"sequence";case "Mirror text","Retype"->"keyboard";case "Dream journal"->"memory";case "NFC"->"sonar";default->"math";};}
    private ChallengeActionRows(){}
}
