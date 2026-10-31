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
        LinearLayout body=Design.page(owner,false,null,null);body.setTag(SHELL);body.setLayoutParams(new ScrollView.LayoutParams(-1,-2));body.setPadding(0,0,0,0);body.setBackgroundColor(0xffffc677);
        owner.getWindow().setBackgroundDrawable(new ColorDrawable(0xffffd893));
        ((View)body.getParent().getParent()).setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xffffc677,Design.CREAM}));
        Design.space(body,compact?12:24);FrameLayout clockRow=new FrameLayout(owner);clockRow.setTag(CLOCK);clockRow.setMinimumHeight(Design.dp(owner,compact?48:56));TextView time=Design.text(owner,clock,compact?32:40,700,Design.INK);time.setText(Design.timeText(clock));time.setGravity(Gravity.CENTER);clockRow.addView(time,new FrameLayout.LayoutParams(-1,-2,Gravity.CENTER));if(back!=null)clockRow.addView(Design.iconButton(owner,"back","Back",back),new FrameLayout.LayoutParams(Design.dp(owner,48),Design.dp(owner,48),Gravity.START|Gravity.CENTER_VERTICAL));body.addView(clockRow,new LinearLayout.LayoutParams(-1,-2));Design.space(body,4);Design.text(body,heading,compact?18:21,600,Design.INK).setGravity(Gravity.CENTER);Design.space(body,compact?8:16);Design.text(body,caption,compact?13:14,500,Design.INK).setGravity(Gravity.CENTER);
        LinearLayout dots=Design.row(owner);dots.setTag(PROGRESS);dots.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);dots.setContentDescription(total>0?"Challenge "+(current+1)+" of "+total:null);
        for(int n=0;n<total;n++){View dot=new View(owner);dot.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);dot.setBackground(Design.shape(owner,n<=current?Design.BROWN:0xffe8ad65,8));LinearLayout.LayoutParams size=new LinearLayout.LayoutParams(Design.dp(owner,34),Design.dp(owner,compact?8:10));size.setMargins(Design.dp(owner,4),Design.dp(owner,compact?8:10),Design.dp(owner,4),0);dots.addView(dot,size);}if(total==0)dots.setMinimumHeight(Design.dp(owner,20));body.addView(dots);
        Landscape scene=new Landscape(owner);scene.setTag(LANDSCAPE);body.addView(scene,new LinearLayout.LayoutParams(-1,Design.dp(owner,compact?64:112)));
        LinearLayout card=Design.col(owner);card.setTag(SHEET);GradientDrawable sheet=Design.shape(owner,Design.CREAM,0);float radius=Design.dp(owner,32);sheet.setCornerRadii(new float[]{radius,radius,radius,radius,0,0,0,0});card.setBackground(sheet);Design.pad(card,compact?20:24,compact?20:24);body.addView(card,new LinearLayout.LayoutParams(-1,-2,1));return card;
    }
    /** Crop the existing decorative landscape without distorting its round sun or inventing progress. */
    private static final class Landscape extends View {
        private final Bitmap image;private final Paint paint=new Paint(3);private final Rect source=new Rect();private final RectF destination=new RectF();
        Landscape(Context context){super(context);image=((BitmapDrawable)context.getDrawable(R.drawable.wake_dawn_landscape)).getBitmap();setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){if(w<=0||h<=0)return;int height=Math.min(image.getHeight(),Math.round((float)image.getWidth()*h/w));int top=Math.max(0,Math.min(image.getHeight()-height,Math.round(image.getHeight()*.72f-height/2f)));source.set(0,top,image.getWidth(),top+height);destination.set(0,0,w,h);}
        @Override protected void onDraw(Canvas canvas){if(!destination.isEmpty())canvas.drawBitmap(image,source,destination,paint);}
    }
    private WakeChallengeUi(){}
}
