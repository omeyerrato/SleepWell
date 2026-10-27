package org.sleepwell.app;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

/** Routine-only composition; all actions are supplied by the existing draft owner. */
final class ChallengeRoutineUi {
    static void header(Activity activity,LinearLayout body,Runnable back,Runnable more){
        LinearLayout navigation=Design.row(activity);
        navigation.addView(Design.iconButton(activity,"back","Back",back),new LinearLayout.LayoutParams(Design.dp(activity,48),Design.dp(activity,48)));
        navigation.addView(new View(activity),new LinearLayout.LayoutParams(0,1,1));
        navigation.addView(Design.iconButton(activity,"more","Challenge routine options",more),new LinearLayout.LayoutParams(Design.dp(activity,48),Design.dp(activity,48)));
        body.addView(navigation);Design.space(body,8);
        Design.text(body,"Challenges",30,700,Design.INK);Design.space(body,4);
        Design.text(body,"Your routine",16,500,Design.MUTED);Design.space(body,16);
    }
    static void task(LinearLayout body,String type,String detail,boolean allowed,Runnable action){
        android.content.Context context=body.getContext();LinearLayout row=Design.row(context);row.setMinimumHeight(Design.dp(context,64));
        FrameLayout well=new FrameLayout(context);well.setBackground(Design.shape(context,Design.SOFT,24));well.addView(new Design.Icon(context,ChallengeActionRows.icon(type),allowed?Design.BROWN:Design.MUTED),new FrameLayout.LayoutParams(Design.dp(context,28),Design.dp(context,28),Gravity.CENTER));
        LinearLayout.LayoutParams glyph=new LinearLayout.LayoutParams(Design.dp(context,48),Design.dp(context,48));glyph.rightMargin=Design.dp(context,16);row.addView(well,glyph);
        LinearLayout copy=Design.col(context);Design.text(copy,type,16,600,Design.INK);Design.space(copy,4);Design.text(copy,detail,12,400,Design.MUTED);LinearLayout.LayoutParams labels=new LinearLayout.LayoutParams(0,-2,1);labels.topMargin=Design.dp(context,8);labels.bottomMargin=Design.dp(context,8);labels.rightMargin=Design.dp(context,10);row.addView(copy,labels);
        row.addView(new Design.Icon(context,"chevron",allowed?Design.BROWN:Design.MUTED),new LinearLayout.LayoutParams(Design.dp(context,22),Design.dp(context,48)));
        row.setContentDescription("Configure "+type+(allowed?"":", routine full; remove a task first"));row.setFocusable(true);row.setEnabled(allowed);row.setScreenReaderFocusable(true);
        if(allowed){row.setBackground(Design.touch(context,android.graphics.Color.TRANSPARENT,12));row.setOnClickListener(v->{InteractionFeedback.action(v);action.run();});}
        body.addView(row);Design.line(body);
    }
    static void save(LinearLayout body,Runnable action){LinearLayout footer=RecordUi.footer(body);footer.setBackgroundColor(Appearance.color(body.getContext(),Design.CREAM));footer.setPadding(footer.getPaddingLeft(),Design.dp(body.getContext(),8),footer.getPaddingRight(),footer.getPaddingBottom());Design.button(footer,"Save routine",true,action);}
    private ChallengeRoutineUi(){}
}
