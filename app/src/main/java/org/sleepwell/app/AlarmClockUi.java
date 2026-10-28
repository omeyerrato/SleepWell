package org.sleepwell.app;

import android.app.Activity;
import android.text.Layout;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;

/** Alarm clock presentation only. Steps propose a local minute; the editor owns its draft. */
final class AlarmClockUi {
    static void fit(TextView time,int preferredSp,int lines){
        time.setMaxLines(lines);time.setEllipsize(null);time.setHorizontallyScrolling(false);
        time.setAutoSizeTextTypeUniformWithConfiguration(20,Math.max(20,Math.round(preferredSp*Appearance.scale(time.getContext()))),1,TypedValue.COMPLEX_UNIT_SP);
    }
    static void steps(LinearLayout body,Button time,int minute,BooleanSupplier current,IntConsumer change){
        int index=body.indexOfChild(time);body.removeView(time);time.setMinHeight(Design.dp(body.getContext(),64));time.setMinimumHeight(Design.dp(body.getContext(),64));
        body.addView(new Clock((Activity)body.getContext(),time,minute,current,change),index,new LinearLayout.LayoutParams(-1,-2));
    }
    private static final class Clock extends ViewGroup {
        private final Activity owner;private final Button time;private final View hourUp,minuteUp,hourDown,minuteDown;private final int target;private int band;
        Clock(Activity owner,Button time,int minute,BooleanSupplier current,IntConsumer change){
            super(owner);this.owner=owner;this.time=time;target=Design.dp(owner,48);addView(time,new LayoutParams(-1,-2));
            hourUp=arrow("Increase alarm hour",-90,60,minute,current,change);minuteUp=arrow("Increase alarm minute",-90,1,minute,current,change);
            hourDown=arrow("Decrease alarm hour",90,-60,minute,current,change);minuteDown=arrow("Decrease alarm minute",90,-1,minute,current,change);
        }
        private View arrow(String label,int rotation,int delta,int minute,BooleanSupplier current,IntConsumer change){
            FrameLayout action=new FrameLayout(owner);Design.Icon glyph=new Design.Icon(owner,"chevron",Design.MUTED);glyph.setRotation(rotation);glyph.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);action.addView(glyph,new FrameLayout.LayoutParams(Design.dp(owner,20),Design.dp(owner,20),Gravity.CENTER));
            action.setContentDescription(label);action.setBackground(Design.touch(owner,android.graphics.Color.TRANSPARENT,24));action.setOnClickListener(view->{if(isAttachedToWindow()&&hasWindowFocus()&&!owner.isFinishing()&&!owner.isDestroyed()&&current.getAsBoolean()){InteractionFeedback.action(view);change.accept(Math.floorMod(minute+delta,1440));}});
            action.setFocusable(true);action.setTooltipText(label);action.setAccessibilityDelegate(new AccessibilityDelegate(){@Override public void onInitializeAccessibilityNodeInfo(View host,AccessibilityNodeInfo node){super.onInitializeAccessibilityNodeInfo(host,node);node.setClassName(Button.class.getName());}});addView(action,new LayoutParams(target,target));return action;
        }
        @Override protected void onMeasure(int widthSpec,int heightSpec){
            int width=MeasureSpec.getSize(widthSpec);band=width>=target*2?target:target*2;
            time.measure(MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED));
            for(View action:new View[]{hourUp,minuteUp,hourDown,minuteDown})action.measure(MeasureSpec.makeMeasureSpec(target,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(target,MeasureSpec.EXACTLY));
            setMeasuredDimension(width,time.getMeasuredHeight()+band*2);
        }
        @Override protected void onLayout(boolean changed,int left,int top,int right,int bottom){
            int width=right-left,height=time.getMeasuredHeight();time.layout(0,band,width,band+height);
            if(width<target*2){int x=(width-target)/2;hourUp.layout(x,0,x+target,target);minuteUp.layout(x,target,x+target,target*2);hourDown.layout(x,band+height,x+target,band+height+target);minuteDown.layout(x,band+height+target,x+target,band+height+target*2);return;}
            Layout glyphs=time.getLayout();if(glyphs==null)return;String label=time.getText().toString();int colon=label.indexOf(':'),end=label.indexOf(' ',colon+1);if(colon<1)return;if(end<0)end=label.length();
            // Use the actual fitted/spanned text layout; localized suffix widths are not guessed.
            float offset=time.getCompoundPaddingLeft(),hour=(glyphs.getPrimaryHorizontal(0)+glyphs.getPrimaryHorizontal(colon))/2+offset,minute=(glyphs.getPrimaryHorizontal(colon+1)+glyphs.getPrimaryHorizontal(end))/2+offset;
            float gap=Math.max(target,Math.abs(minute-hour)),center=(hour+minute)/2;gap=Math.min(gap,width-target);center=Math.max((target+gap)/2,Math.min(width-(target+gap)/2,center));
            int low=Math.round(center-gap/2-target/2f),high=Math.round(center+gap/2-target/2f),hx=hour>minute?high:low,mx=hour>minute?low:high;hourUp.layout(hx,0,hx+target,target);minuteUp.layout(mx,0,mx+target,target);hourDown.layout(hx,band+height,hx+target,band+height+target);minuteDown.layout(mx,band+height,mx+target,band+height+target);
        }
    }
    private AlarmClockUi(){}
}
