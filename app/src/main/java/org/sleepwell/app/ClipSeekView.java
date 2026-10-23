package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.function.LongConsumer;

/** Reference-sized track inside a full 48dp accessible hit area; no platform-theme slider. */
@android.annotation.SuppressLint("ViewConstructor")
final class ClipSeekView extends View {
    private final Paint paint=new Paint(3);private final long duration;private final LongConsumer seek;private long position;private boolean dragging;
    ClipSeekView(Context context,long duration,LongConsumer seek){super(context);this.duration=duration;this.seek=seek;setFocusable(true);setContentDescription("Recording position");setMinimumHeight(Design.dp(context,48));}
    void position(long value){if(!dragging){position=Math.max(0,Math.min(duration,value));invalidate();}}
    private float left(){return Design.dp(getContext(),10);}private float right(){return getWidth()-left();}
    private void choose(float x){position=Math.max(0,Math.min(duration,Math.round((x-left())/Math.max(1,right()-left())*duration)));invalidate();}
    @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);float y=getHeight()/2f,x=left()+(right()-left())*position/Math.max(1f,duration);paint.setStrokeWidth(Design.dp(getContext(),4));paint.setStrokeCap(Paint.Cap.ROUND);paint.setColor(Design.LINE);canvas.drawLine(left(),y,right(),y,paint);paint.setColor(Design.BROWN);canvas.drawLine(left(),y,x,y,paint);canvas.drawCircle(x,y,Design.dp(getContext(),5),paint);if(isFocused()){paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Design.dp(getContext(),1));canvas.drawCircle(x,y,Design.dp(getContext(),9),paint);paint.setStyle(Paint.Style.FILL);}}
    @Override public boolean onTouchEvent(MotionEvent event){if(!isEnabled())return false;switch(event.getActionMasked()){case MotionEvent.ACTION_DOWN:dragging=true;getParent().requestDisallowInterceptTouchEvent(true);choose(event.getX());return true;case MotionEvent.ACTION_MOVE:choose(event.getX());return true;case MotionEvent.ACTION_UP:choose(event.getX());dragging=false;getParent().requestDisallowInterceptTouchEvent(false);seek.accept(position);performClick();return true;case MotionEvent.ACTION_CANCEL:dragging=false;getParent().requestDisallowInterceptTouchEvent(false);return true;default:return false;}}
    @Override public boolean performClick(){super.performClick();return true;}
    @Override public boolean onKeyDown(int key,android.view.KeyEvent event){if(key==KeyEvent.KEYCODE_DPAD_LEFT||key==KeyEvent.KEYCODE_DPAD_RIGHT){position=Math.max(0,Math.min(duration,position+(key==KeyEvent.KEYCODE_DPAD_LEFT?-1:1)*1000));seek.accept(position);invalidate();return true;}return super.onKeyDown(key,event);}
    @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(info);info.setClassName("android.widget.SeekBar");info.setRangeInfo(AccessibilityNodeInfo.RangeInfo.obtain(AccessibilityNodeInfo.RangeInfo.RANGE_TYPE_FLOAT,0,duration,position));info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);}
    @Override public boolean performAccessibilityAction(int action,Bundle args){long next=position;if(action==AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS.getId()&&args!=null)next=(long)args.getFloat(AccessibilityNodeInfo.ACTION_ARGUMENT_PROGRESS_VALUE);else if(action==AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)next+=1000;else if(action==AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)next-=1000;else return super.performAccessibilityAction(action,args);position=Math.max(0,Math.min(duration,next));seek.accept(position);invalidate();sendAccessibilityEvent(android.view.accessibility.AccessibilityEvent.TYPE_VIEW_SELECTED);return true;}
}
