package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;
import java.time.ZoneId;
import java.util.*;
import java.util.function.LongConsumer;

/** One bar per actual minute, with missing time left blank and a numerical cycles/minute axis. */
final class EchoRhythmChart extends View {
    private final List<EchoRhythm.Minute> minutes;
    private final long start,end;
    private final ZoneId zone;
    private final boolean changes;
    private final LongConsumer open;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private int selected=-1;
    private float downX,downY;
    private double extent=1;
    EchoRhythmChart(Context context,List<EchoRhythm.Minute> minutes,long start,long end,ZoneId zone,boolean changes,LongConsumer open){
        super(context);this.minutes=minutes;this.start=start;this.end=Math.max(start+1,end);this.zone=zone;this.changes=changes;this.open=open;
        for(EchoRhythm.Minute minute:minutes)if(minute.change!=null)extent=Math.max(extent,Math.abs(minute.change));
        extent=Math.ceil(extent);setFocusable(true);setClickable(true);
        setContentDescription((changes?"Consecutive-minute echo rate changes":"Periodic echo rate")+" chart, cycles per minute. Gaps are blank. Select an interval to inspect.");
    }
    private float dp(float value){return value*getResources().getDisplayMetrics().density;}
    private float left(){return dp(35);}
    private float right(){return getWidth()-dp(8);}
    private float x(long time){return left()+(right()-left())*(time-start)/(end-start);}
    private Double value(EchoRhythm.Minute minute){return changes?minute.change:minute.rate;}
    private String number(double value){return String.format(Locale.getDefault(),"%.0f",value);}
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);float top=dp(18),bottom=getHeight()-dp(45),left=left(),right=right();
        double minimum=changes?-extent:5,maximum=changes?extent:40;
        paint.setTypeface(Design.font(getContext(),400));paint.setTextSize(Design.sp(getContext(),10*Appearance.scale(getContext())));
        for(int n=0;n<=2;n++){
            float y=top+(bottom-top)*n/2;double label=maximum-(maximum-minimum)*n/2;
            paint.setColor(Appearance.color(getContext(),Design.LINE));paint.setStrokeWidth(dp(.7f));canvas.drawLine(left,y,right,y,paint);
            paint.setColor(Appearance.color(getContext(),Design.MUTED));paint.setTextAlign(Paint.Align.RIGHT);canvas.drawText(number(label),left-dp(8),y+dp(3),paint);
        }
        float zero=changes?(top+bottom)/2:bottom;int readings=0;
        for(int n=0;n<minutes.size();n++){
            EchoRhythm.Minute minute=minutes.get(n);Double value=value(minute);if(value==null)continue;readings++;
            float y=(float)(bottom-(bottom-top)*(value-minimum)/(maximum-minimum));
            float a=x(minute.start),b=x(minute.end),width=Math.max(dp(.6f),Math.min(dp(12),(b-a)*.72f)),center=(a+b)/2;
            paint.setColor(n==selected?Design.BROWN:Appearance.contrast(getContext())?0xff6b3dc4:Design.VIOLET);
            float y1=Math.min(y,zero),y2=Math.max(y,zero);
            if(changes&&value==0)canvas.drawCircle(center,zero,dp(1.3f),paint);
            else{if(y2-y1<dp(2))y1=y2-dp(2);canvas.drawRoundRect(center-width/2,y1,center+width/2,y2,dp(2),dp(2),paint);}
        }
        paint.setColor(Appearance.color(getContext(),Design.MUTED));paint.setTextAlign(Paint.Align.CENTER);
        boolean fullDay=android.text.format.DateFormat.is24HourFormat(getContext());
        List<ChartTicks.Placed> ticks=ChartTicks.layout(ChartTicks.candidates(start,end,zone,fullDay,Locale.getDefault(),4),start,end,left,right,dp(8),paint::measureText);
        for(ChartTicks.Placed tick:ticks){String[] lines=tick.tick.label.split("\n");for(int n=0;n<lines.length;n++)canvas.drawText(lines[n],(float)tick.center,bottom+dp(18+13*n),paint);}
        if(readings==0)canvas.drawText(changes?"No consecutive estimates":"No qualified estimates",(left+right)/2,(top+bottom)/2,paint);
        if(hasFocus()){paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(1.5f));paint.setColor(Design.VIOLET);canvas.drawRoundRect(left,top,right,bottom,dp(4),dp(4),paint);paint.setStyle(Paint.Style.FILL);}
    }
    @Override public boolean onTouchEvent(MotionEvent event){
        if(event.getAction()==MotionEvent.ACTION_DOWN){downX=event.getX();downY=event.getY();return true;}
        if(event.getAction()==MotionEvent.ACTION_UP){
            int slop=ViewConfiguration.get(getContext()).getScaledTouchSlop();if(Math.hypot(event.getX()-downX,event.getY()-downY)>slop)return true;
            if(event.getX()<left()||event.getX()>right())return true;
            long time=start+(long)((event.getX()-left())/(right()-left())*(end-start));
            // A tap in a genuine gap must not select some unrelated nearest reading.
            for(int n=0;n<minutes.size();n++)if(time>=minutes.get(n).start&&time<minutes.get(n).end){selected=n;performClick();break;}return true;
        }
        return true;
    }
    @Override public boolean performClick(){super.performClick();if(selected>=0&&selected<minutes.size())open.accept(minutes.get(selected).start);return true;}
    @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(info);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);}
    @Override public boolean performAccessibilityAction(int action,android.os.Bundle args){
        int step=action==AccessibilityNodeInfo.ACTION_SCROLL_FORWARD?1:action==AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD?-1:0;
        if(step==0)return super.performAccessibilityAction(action,args);
        int next=selected<0?(step>0?0:minutes.size()-1):selected+step;if(next<0||next>=minutes.size())return false;
        selected=next;EchoRhythm.Minute minute=minutes.get(selected);Double value=value(minute);
        String description=RecordUi.preciseClock(getContext(),minute.start,zone)+", "+(value==null?minute.reason():String.format(Locale.getDefault(),"%.1f cycles per minute",value));
        setContentDescription(description+". Activate to inspect interval.");announceForAccessibility(description);invalidate();return true;
    }
}
