package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.SeekBar;
import java.util.function.IntConsumer;

/** A duration draft, not a running timer. Cancelled gestures restore the original duration. */
final class NightDialNapWheel extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arc=new RectF();
    private final IntConsumer changed,settled;
    private final int maximum;
    private int minutes,began,pointer=-1,lastTick;
    private boolean dragging;
    NightDialNapWheel(Context c,int minutes,int maximum,IntConsumer changed,IntConsumer settled){
        super(c);this.minutes=minutes;this.maximum=Math.max(maximum,minutes);this.changed=changed;this.settled=settled;
        setFocusable(true);setClickable(true);setContentDescription("Nap duration");updateDescription();
    }
    private float radius(){return Math.min(getWidth()*.42f,getHeight()*.45f);}
    private float cx(){return getWidth()/2f;}
    private float cy(){return getHeight()*.48f;}
    private void updateDescription(){setStateDescription(minutes+" minutes");}
    private void choose(int value,boolean commit){
        value=Math.max(1,Math.min(maximum,value));
        if(value!=minutes){minutes=value;updateDescription();changed.accept(minutes);invalidate();}
        if(commit)settled.accept(minutes);
    }
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);float x=cx(),y=cy(),r=radius(),d=getResources().getDisplayMetrics().density;
        arc.set(x-r,y-r,x+r,y+r);paint.setStyle(Paint.Style.STROKE);paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeWidth(5*d);
        paint.setColor(NightDialTokens.RULE);canvas.drawArc(arc,135,270,false,paint);
        float sweep=270f*(minutes-1)/Math.max(1,maximum-1);paint.setColor(isEnabled()?NightDialTokens.ACCENT:NightDialTokens.SECONDARY);canvas.drawArc(arc,135,sweep,false,paint);
        paint.setStrokeWidth(d);for(int tick=0;tick<=24;tick++){double angle=Math.toRadians(135+tick*270f/24);float inner=r-(tick%6==0?16:11)*d;
            paint.setColor(tick*maximum/24f<=minutes?0xff897044:NightDialTokens.RULE);
            canvas.drawLine(x+(float)Math.cos(angle)*inner,y+(float)Math.sin(angle)*inner,x+(float)Math.cos(angle)*(r-7*d),y+(float)Math.sin(angle)*(r-7*d),paint);
        }
        double angle=Math.toRadians(135+sweep);paint.setStyle(Paint.Style.FILL);paint.setColor(NightDialTokens.GROUND);
        canvas.drawCircle(x+(float)Math.cos(angle)*r,y+(float)Math.sin(angle)*r,10*d,paint);paint.setColor(isEnabled()?NightDialTokens.ACCENT:NightDialTokens.SECONDARY);
        canvas.drawCircle(x+(float)Math.cos(angle)*r,y+(float)Math.sin(angle)*r,7*d,paint);
        paint.setTypeface(Design.font(getContext(),500));paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(Design.sp(getContext(),48*Appearance.scale(getContext())));paint.setColor(Design.INK);
        canvas.drawText(Integer.toString(minutes),x,y+6*d,paint);paint.setTextSize(Design.sp(getContext(),14*Appearance.scale(getContext())));paint.setColor(Design.MUTED);canvas.drawText("minutes",x,y+32*d,paint);
        paint.setTextSize(Design.sp(getContext(),12*Appearance.scale(getContext())));canvas.drawText(isEnabled()?"Turn to set your rest":"Saved duration",x,y+r*.91f,paint);
        if(isFocused()){paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2*d);paint.setColor(NightDialTokens.OUTLINE);canvas.drawRoundRect(d,d,getWidth()-d,getHeight()-d,20*d,20*d,paint);}
    }
    private void move(float x,float y){
        double angle=Math.toDegrees(Math.atan2(y-cy(),x-cx()));double relative=(angle-135+360)%360;
        if(relative>270)relative=relative>315?0:270;
        int value=1+(int)Math.round(relative*(maximum-1)/270);
        choose(value,false);int tick=minutes/5;if(tick!=lastTick){lastTick=tick;InteractionFeedback.tick(this);}
    }
    @Override public boolean onTouchEvent(android.view.MotionEvent event){
        if(!isEnabled())return false;
        switch(event.getActionMasked()){
            case android.view.MotionEvent.ACTION_DOWN->{float distance=(float)Math.hypot(event.getX()-cx(),event.getY()-cy());if(Math.abs(distance-radius())>Design.dp(getContext(),28))return false;began=minutes;lastTick=minutes/5;pointer=event.getPointerId(0);dragging=true;getParent().requestDisallowInterceptTouchEvent(true);move(event.getX(),event.getY());return true;}
            case android.view.MotionEvent.ACTION_MOVE->{if(!dragging)return false;int index=event.findPointerIndex(pointer);if(index<0){cancel();return true;}move(event.getX(index),event.getY(index));return true;}
            case android.view.MotionEvent.ACTION_UP->{if(!dragging)return false;dragging=false;pointer=-1;getParent().requestDisallowInterceptTouchEvent(false);performClick();settled.accept(minutes);return true;}
            case android.view.MotionEvent.ACTION_CANCEL->{cancel();return true;}
            case android.view.MotionEvent.ACTION_POINTER_UP->{if(event.getPointerId(event.getActionIndex())==pointer)cancel();return true;}
            default->{return dragging;}
        }
    }
    private void cancel(){if(dragging){dragging=false;pointer=-1;getParent().requestDisallowInterceptTouchEvent(false);choose(began,false);}}
    @Override public boolean performClick(){super.performClick();return true;}
    @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(info);info.setClassName(SeekBar.class.getName());info.setRangeInfo(AccessibilityNodeInfo.RangeInfo.obtain(AccessibilityNodeInfo.RangeInfo.RANGE_TYPE_INT,1,maximum,minutes));if(isEnabled()){info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);}}
    @Override public boolean performAccessibilityAction(int action,Bundle args){if(isEnabled()){if(action==AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS.getId()&&args!=null){float value=args.getFloat(AccessibilityNodeInfo.ACTION_ARGUMENT_PROGRESS_VALUE,Float.NaN);if(!Float.isFinite(value))return false;choose(Math.round(value),true);return true;}if(action==AccessibilityNodeInfo.ACTION_SCROLL_FORWARD||action==AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD){choose(minutes+(action==AccessibilityNodeInfo.ACTION_SCROLL_FORWARD?1:-1),true);return true;}}return super.performAccessibilityAction(action,args);}
    @Override public boolean onKeyDown(int key,KeyEvent event){if(isEnabled()&&(key==KeyEvent.KEYCODE_DPAD_RIGHT||key==KeyEvent.KEYCODE_DPAD_UP||key==KeyEvent.KEYCODE_PLUS)){choose(minutes+1,true);return true;}if(isEnabled()&&(key==KeyEvent.KEYCODE_DPAD_LEFT||key==KeyEvent.KEYCODE_DPAD_DOWN||key==KeyEvent.KEYCODE_MINUS)){choose(minutes-1,true);return true;}return super.onKeyDown(key,event);}
}
