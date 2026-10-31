package org.sleepwell.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;

/** Presentation only. The caller owns the real routine, clock, answers and occurrence. */
final class WakeChallengeUi {
    static final String SHELL="wake-screen-shell",CLOCK="wake-screen-clock",PROGRESS="wake-screen-progress",LANDSCAPE="wake-screen-landscape",SHEET="wake-screen-sheet";
    static LinearLayout card(Activity owner,String clock,String heading,String caption,int current,int total){return card(owner,clock,heading,caption,current,total,null);}
    static LinearLayout card(Activity owner,String clock,String heading,String caption,int current,int total,Runnable back){
        return card(owner,clock,heading,caption,current,total,back,false);
    }
    static LinearLayout card(Activity owner,String clock,String heading,String caption,int current,int total,Runnable back,boolean compact){
        LinearLayout body=Design.page(owner,false,null,null);body.setTag(SHELL);body.setLayoutParams(new ScrollView.LayoutParams(-1,-2));body.setPadding(Design.dp(owner,20),0,Design.dp(owner,20),Design.dp(owner,20));body.setBackgroundColor(Design.CREAM);
        owner.getWindow().setBackgroundDrawable(new ColorDrawable(Design.CREAM));
        Design.space(body,compact?12:24);FrameLayout clockRow=new FrameLayout(owner);clockRow.setTag(CLOCK);clockRow.setMinimumHeight(Design.dp(owner,compact?48:56));TextView time=Design.text(owner,clock,compact?32:40,700,Design.INK);time.setText(Design.timeText(clock));time.setGravity(Gravity.CENTER);clockRow.addView(time,new FrameLayout.LayoutParams(-1,-2,Gravity.CENTER));if(back!=null)clockRow.addView(Design.iconButton(owner,"back","Back",back),new FrameLayout.LayoutParams(Design.dp(owner,48),Design.dp(owner,48),Gravity.START|Gravity.CENTER_VERTICAL));body.addView(clockRow,new LinearLayout.LayoutParams(-1,-2));Design.space(body,4);Design.text(body,heading,compact?18:21,600,Design.INK).setGravity(Gravity.CENTER);Design.space(body,compact?8:16);Design.text(body,caption,compact?13:14,500,Design.INK).setGravity(Gravity.CENTER);
        LinearLayout dots=Design.row(owner);dots.setTag(PROGRESS);dots.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);dots.setContentDescription(total>0?"Challenge "+(current+1)+" of "+total:null);
        for(int n=0;n<total;n++){View dot=new View(owner);dot.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);dot.setBackground(Design.shape(owner,n<current?Design.BROWN:n==current?Design.INK:Design.LINE,2));LinearLayout.LayoutParams size=new LinearLayout.LayoutParams(0,Design.dp(owner,4),1);size.setMargins(Design.dp(owner,3),Design.dp(owner,12),Design.dp(owner,3),0);dots.addView(dot,size);}if(total==0)dots.setMinimumHeight(Design.dp(owner,12));body.addView(dots);
        View horizon=new Horizon(owner);horizon.setTag(LANDSCAPE);body.addView(horizon,new LinearLayout.LayoutParams(-1,Design.dp(owner,compact?32:48)));
        LinearLayout card=Design.col(owner);card.setTag(SHEET);Design.pad(card,0,compact?8:12);body.addView(card,new LinearLayout.LayoutParams(-1,-2));return card;
    }
    /** A quiet dawn mark: no automatic brightening or invented task progress. */
    private static final class Horizon extends View {
        final Paint paint=new Paint(3);Horizon(Context c){super(c);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onDraw(Canvas c){float d=getResources().getDisplayMetrics().density,x=getWidth()/2f,y=getHeight()*.82f;paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(d);paint.setColor(Design.LINE);c.drawLine(0,y,getWidth(),y,paint);paint.setColor(0xff6c5739);c.drawArc(x-16*d,y-16*d,x+16*d,y+16*d,180,180,false,paint);}
    }
    private WakeChallengeUi(){}
}
