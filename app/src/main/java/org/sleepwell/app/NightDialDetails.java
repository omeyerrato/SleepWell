package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.view.*;
import android.widget.*;

/** Small, task-specific presentation objects. They hold no state or feature authority. */
final class NightDialDetails {
    static LinearLayout fold(LinearLayout parent,String title,boolean expanded,java.util.function.Consumer<Boolean> changed){
        Context c=parent.getContext();LinearLayout group=Design.col(c),content=Design.col(c);content.setVisibility(expanded?View.VISIBLE:View.GONE);LinearLayout header=Design.row(c);header.setMinimumHeight(Design.dp(c,56));header.addView(Design.text(c,title,15,500,Design.INK),new LinearLayout.LayoutParams(0,-2,1));Design.Icon arrow=new Design.Icon(c,"down",Design.MUTED);arrow.setRotation(expanded?180:0);header.addView(arrow,new LinearLayout.LayoutParams(Design.dp(c,20),Design.dp(c,20)));header.setBackground(Design.touch(c,Color.TRANSPARENT,12));header.setFocusable(true);header.setContentDescription(title);header.setStateDescription(expanded?"Expanded":"Collapsed");header.setOnClickListener(v->{boolean open=content.getVisibility()!=View.VISIBLE;InteractionFeedback.selection(v);content.setVisibility(open?View.VISIBLE:View.GONE);arrow.setRotation(open?180:0);header.setStateDescription(open?"Expanded":"Collapsed");changed.accept(open);});group.addView(header);group.addView(content,new LinearLayout.LayoutParams(-1,-2));parent.addView(group,new LinearLayout.LayoutParams(-1,-2));return content;
    }
    /** Disclosure leaves the important copy visible, with long supporting explanations on demand. */
    static void disclosure(LinearLayout parent,String title,String value){
        Context c=parent.getContext();LinearLayout panel=Design.col(c);TextView detail=Design.text(c,value,14,400,Design.MUTED);detail.setVisibility(View.GONE);
        LinearLayout row=Design.row(c);row.setMinimumHeight(Design.dp(c,52));TextView label=Design.text(c,title,14,500,Design.INK);row.addView(label,new LinearLayout.LayoutParams(0,-2,1));Design.Icon arrow=new Design.Icon(c,"down",Design.MUTED);row.addView(arrow,new LinearLayout.LayoutParams(Design.dp(c,20),Design.dp(c,20)));row.setFocusable(true);row.setContentDescription(title);row.setStateDescription("Collapsed");row.setBackground(Design.touch(c,Color.TRANSPARENT,12));row.setOnClickListener(v->{boolean open=detail.getVisibility()!=View.VISIBLE;InteractionFeedback.selection(v);detail.setVisibility(open?View.VISIBLE:View.GONE);arrow.setRotation(open?180:0);row.setStateDescription(open?"Expanded":"Collapsed");});panel.addView(row);panel.addView(detail,new LinearLayout.LayoutParams(-1,-2));parent.addView(panel,new LinearLayout.LayoutParams(-1,-2));
    }
    static LinearLayout note(LinearLayout parent,String title,String detail){Context c=parent.getContext();LinearLayout note=Design.col(c);Design.pad(note,20,20);note.setBackground(new Paper(c));Design.text(note,title,22,500,Design.INK);Design.space(note,10);Design.text(note,detail,14,400,Design.MUTED);parent.addView(note,new LinearLayout.LayoutParams(-1,-2));return note;}
    static void journey(LinearLayout parent,String[] titles,String[] details,int current){
        Context c=parent.getContext();for(int n=0;n<titles.length;n++){LinearLayout row=Design.row(c);row.setGravity(Gravity.TOP);TextView number=Design.text(c,Integer.toString(n+1),14,500,n==current?Design.BROWN:Design.MUTED);number.setGravity(Gravity.CENTER);number.setBackground(Design.outline(c,Design.SOFT,n==current?Design.BROWN:Design.LINE,20));LinearLayout.LayoutParams marker=new LinearLayout.LayoutParams(Design.dp(c,36),Design.dp(c,36));marker.rightMargin=Design.dp(c,14);row.addView(number,marker);LinearLayout copy=Design.col(c);Design.text(copy,titles[n],16,500,Design.INK);Design.space(copy,5);Design.text(copy,details[n],14,400,Design.MUTED);row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));parent.addView(row,new LinearLayout.LayoutParams(-1,-2));if(n+1<titles.length)Design.space(parent,20);}
    }
    static void diaryRow(LinearLayout parent,String title,String date,String excerpt,Runnable action){
        Context c=parent.getContext();LinearLayout row=Design.col(c);Design.pad(row,18,18);row.setBackground(new android.graphics.drawable.LayerDrawable(new Drawable[]{new Paper(c),Design.touch(c,Color.TRANSPARENT,18)}));Design.text(row,date,12,400,Design.MUTED);Design.space(row,10);Design.text(row,title,20,500,Design.INK);if(!excerpt.isBlank()){Design.space(row,8);TextView preview=Design.text(row,excerpt.replaceAll("\\s+"," "),14,400,Design.MUTED);preview.setMaxLines(2);preview.setEllipsize(android.text.TextUtils.TruncateAt.END);}Design.space(row,12);Design.text(row,"Read entry  ›",13,500,Design.BROWN);row.setFocusable(true);row.setContentDescription(title+". "+date+". Open diary entry");row.setScreenReaderFocusable(true);row.setOnClickListener(v->{InteractionFeedback.action(v);action.run();});LinearLayout.LayoutParams size=new LinearLayout.LayoutParams(-1,-2);size.bottomMargin=Design.dp(c,12);parent.addView(row,size);
    }
    static final class Paper extends Drawable {
        final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);final float density;Paper(Context c){density=c.getResources().getDisplayMetrics().density;}
        @Override public void draw(Canvas canvas){Rect b=getBounds();float r=18*density;paint.setColor(0xff191918);canvas.drawRoundRect(b.left,b.top,b.right,b.bottom,r,r,paint);canvas.save();Path clip=new Path();clip.addRoundRect(new RectF(b),r,r,Path.Direction.CW);canvas.clipPath(clip);paint.setStrokeWidth(density*.5f);paint.setColor(0xff242422);float gap=8*density;for(float x=b.left;x<b.right;x+=gap)canvas.drawLine(x,b.top,x,b.bottom,paint);paint.setColor(0xff1f1f1d);for(float y=b.top;y<b.bottom;y+=gap)canvas.drawLine(b.left,y,b.right,y,paint);canvas.restore();}
        @Override public void setAlpha(int value){}@Override public void setColorFilter(ColorFilter filter){}@Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }
    private NightDialDetails(){}
}
