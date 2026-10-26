package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import android.widget.*;

/** Shared, compact tracking choices. Chevrons always navigate; radio rows only select. */
final class TrackingOptionsUi {
    private TrackingOptionsUi(){}
    static void menu(LinearLayout body,String icon,String title,String detail,String value,Runnable open){
        Context c=body.getContext();LinearLayout row=Design.row(c);Design.pad(row,0,12);row.setMinimumHeight(Design.dp(c,72));
        Design.Icon glyph=new Design.Icon(c,icon,Design.INK);LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(Design.dp(c,25),Design.dp(c,25));ip.rightMargin=Design.dp(c,16);row.addView(glyph,ip);
        LinearLayout labels=Design.col(c);Design.text(labels,title,16,500,Design.INK);if(detail!=null&&!detail.isEmpty()){Design.space(labels,5);Design.text(labels,detail,12,400,Design.MUTED);}row.addView(labels,new LinearLayout.LayoutParams(0,-2,1));
        if(value!=null&&!value.isEmpty()){TextView current=Design.text(c,value,13,500,Design.MUTED);current.setMaxWidth(Design.dp(c,96));current.setGravity(Gravity.END);LinearLayout.LayoutParams vp=new LinearLayout.LayoutParams(-2,-2);vp.leftMargin=Design.dp(c,8);row.addView(current,vp);}
        if(open!=null){LinearLayout.LayoutParams arrow=new LinearLayout.LayoutParams(Design.dp(c,20),Design.dp(c,20));arrow.leftMargin=Design.dp(c,8);row.addView(new Design.Icon(c,"chevron",Design.INK),arrow);row.setFocusable(true);row.setBackground(Design.touch(c,Color.TRANSPARENT,12));row.setOnClickListener(v->{InteractionFeedback.action(v);open.run();});}
        body.addView(row,new LinearLayout.LayoutParams(-1,-2));Design.line(body);
    }
    static void choice(LinearLayout body,String title,String detail,String value,boolean selected,Runnable choose){
        Context c=body.getContext();LinearLayout row=Design.row(c);Design.pad(row,12,14);row.setMinimumHeight(Design.dp(c,78));row.setBackground(Design.touch(c,selected?Design.SOFT:Color.TRANSPARENT,17));
        row.addView(new Radio(c,selected),new LinearLayout.LayoutParams(Design.dp(c,36),Design.dp(c,36)));
        LinearLayout copy=Design.col(c);Design.text(copy,title,16,600,Design.INK);Design.space(copy,5);Design.text(copy,detail,12,400,Design.MUTED);LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-2,1);cp.leftMargin=Design.dp(c,10);row.addView(copy,cp);
        if(value!=null){TextView text=Design.text(c,value,14,600,Design.INK);LinearLayout.LayoutParams vp=new LinearLayout.LayoutParams(-2,-2);vp.leftMargin=Design.dp(c,8);row.addView(text,vp);}
        row.setFocusable(true);row.setContentDescription(title+". "+detail+(value==null?"":". "+value));SleepPicker.radio(row,selected);row.setOnClickListener(v->{if(!selected)InteractionFeedback.selection(v);choose.run();});body.addView(row,new LinearLayout.LayoutParams(-1,-2));
    }
    static void context(LinearLayout body,String parent){Design.text(body,parent+" / Settings",12,500,Design.MUTED);Design.space(body,16);}
    private static final class Radio extends View {
        final boolean checked;final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        Radio(Context c,boolean checked){super(c);this.checked=checked;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);float d=getResources().getDisplayMetrics().density,x=getWidth()/2f,y=getHeight()/2f;paint.setColor(Appearance.color(getContext(),checked?Design.BROWN:Design.MUTED));paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2*d);canvas.drawCircle(x,y,12*d,paint);if(checked){paint.setStyle(Paint.Style.FILL);canvas.drawCircle(x,y,7*d,paint);}}
    }
}
