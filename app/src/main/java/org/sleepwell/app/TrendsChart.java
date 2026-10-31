package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Actual daily coverage or calendar-month means; absent dates remain blank. */
final class TrendsChart extends View {
    static final class Point {final LocalDate date;final Double value;Point(LocalDate date,Double value){this.date=date;this.value=value;}}
    private final List<Point> points=new ArrayList<>();private final boolean bedtime;private final int period;private final long goal;private final float axisInset;private final Paint p=new Paint(3),labelMeasure=new Paint(3);private final Path column=new Path();private int selected=-1;private double low,high;private java.util.function.Consumer<LocalDate> selection=date->{};
    TrendsChart(Context c,SortedMap<LocalDate,RecordTrends.Day> days,RecordTrends.Range range,int period,boolean bedtime,long goal){super(c);this.bedtime=bedtime;this.period=period;this.goal=goal;
        if(period==2){for(int month=1;month<=12;month++){List<Double> values=new ArrayList<>();for(RecordTrends.Day day:days.values())if(day.date.getMonthValue()==month){Double v=bedtime?day.bedtime():day.duration()/3600000d;if(v!=null)values.add(v);}double sum=0;for(double v:values)sum+=v;points.add(new Point(range.start.withMonth(month),values.isEmpty()?null:sum/values.size()));}}
        else for(LocalDate date=range.start;!date.isAfter(range.end);date=date.plusDays(1)){RecordTrends.Day day=days.get(date);points.add(new Point(date,day==null?null:bedtime?day.bedtime():day.duration()/3600000d));}
        low=bedtime?21*60:0;high=bedtime?24*60:Math.max(12,Math.ceil(goal/3600000d/4)*4);double min=Double.MAX_VALUE,max=-Double.MAX_VALUE;for(Point point:points)if(point.value!=null){min=Math.min(min,point.value);max=Math.max(max,point.value);}if(min!=Double.MAX_VALUE){if(bedtime){low=Math.min(low,Math.floor(min/60)*60);high=Math.max(high,Math.ceil(max/60)*60);}else{low=0;high=Math.max(high,Math.ceil(max/4)*4);}}if(high<=low)high=low+(bedtime?60:1);
        p.setTypeface(Design.font(c,400));p.setTextSize(Design.sp(c,10*Appearance.scale(c)));float widest=0;int ticks=tickCount();for(int n=0;n<=ticks;n++)widest=Math.max(widest,p.measureText(tickLabel(low+(high-low)*n/ticks)));axisInset=Math.max(32*d(),widest+6*d());
        setFocusable(true);StringBuilder summary=new StringBuilder(bedtime?"Bedtime consistency. ":"Recorded duration. ");for(Point point:points)summary.append(point.date).append(": ").append(point.value==null?"no record":bedtime?bedtimeLabel(point.value):RecordUi.roundedDuration((long)(point.value*3600000))).append(". ");setContentDescription(summary);
    }
    TrendsChart onSelected(java.util.function.Consumer<LocalDate> action){selection=action;return this;}
    void restoreSelection(LocalDate date){if(date==null)return;for(int n=0;n<points.size();n++)if(points.get(n).date.equals(date)){selected=n;describe();invalidate();return;}}
    private float d(){return getResources().getDisplayMetrics().density;}
    private String goalLabel(){return (goal%3600000==0?goal/3600000+" h":RecordUi.duration(goal))+" goal";}
    private Paint labelMetrics(){labelMeasure.setTypeface(Design.font(getContext(),400));labelMeasure.setTextSize(Design.sp(getContext(),10*Appearance.scale(getContext())));return labelMeasure;}
    private String selectedLabel(){Point point=points.get(selected);String value=point.value==null?"No record":bedtime?bedtimeLabel(point.value):RecordUi.roundedDuration((long)(point.value*3600000));return points.size()<=7?value:point.date.format(DateTimeFormatter.ofPattern(period==2?"MMM":"d MMM",Locale.getDefault()))+" · "+value;}
    RectF goalLabelBounds(){if(bedtime)return new RectF();Paint text=labelMetrics();return new RectF(right()-text.measureText(goalLabel()),0,right(),text.descent()-text.ascent());}
    RectF selectedLabelBounds(){
        if(selected<0)return new RectF();Paint text=labelMetrics();float density=d(),half=text.measureText(selectedLabel())/2+8*density;
        float limit=getWidth(),top=0,height=Math.max(20*density,text.descent()-text.ascent()+8*density);
        if(!bedtime){RectF caption=goalLabelBounds();float available=caption.left-6*density;if(available>=2*half)limit=available;else top=caption.bottom+6*density;}
        float center=Math.max(half,Math.min(limit-half,x(selected)));
        return new RectF(center-half,top,center+half,top+height);
    }
    private float left(){return axisInset;}
    private float right(){return getWidth()-6*d();}
    private float x(int index){return left()+(right()-left())*(index+.5f)/points.size();}
    private float y(double value,float top,float bottom){return (float)(bottom-(bottom-top)*(value-low)/(high-low));}
    private String bedtimeLabel(double minutes){int total=Math.floorMod((int)Math.round(minutes),1440);boolean clock24=android.text.format.DateFormat.is24HourFormat(getContext());return clock24?String.format(Locale.getDefault(),"%02d:%02d",total/60,total%60):(total/60%12==0?12:total/60%12)+(total%60==0?"":String.format(Locale.getDefault(),":%02d",total%60))+(total<720?" AM":" PM");}
    private int tickCount(){return bedtime&&high-low<=240?(int)Math.round((high-low)/60):3;}
    private String tickLabel(double value){return bedtime?bedtimeLabel(value):String.format(Locale.getDefault(),value%1==0?"%.0f h":"%.1f h",value);}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float d=d(),top=Math.max(20*d,selected<0?0:selectedLabelBounds().bottom),bottom=getHeight()-Math.max(38*d,Design.sp(getContext(),11*Appearance.scale(getContext()))*2.8f);p.setTypeface(Design.font(getContext(),400));p.setTextSize(Design.sp(getContext(),10*Appearance.scale(getContext())));p.setStrokeWidth(.7f*d);p.setStyle(Paint.Style.FILL);
        if(selected>=0){float halfSlot=(right()-left())/points.size()/2;p.setColor(NightDialTokens.SURFACE);c.drawRoundRect(x(selected)-halfSlot,top,x(selected)+halfSlot,bottom,4*d,4*d,p);}
        int ticks=tickCount();for(int n=0;n<=ticks;n++){double v=low+(high-low)*n/ticks;float yy=y(v,top,bottom);p.setColor(NightDialTokens.RULE);c.drawLine(left(),yy,right(),yy,p);p.setColor(Appearance.color(getContext(),Design.MUTED));p.setTextAlign(Paint.Align.RIGHT);c.drawText(tickLabel(v),left()-6*d,yy+3*d,p);}
        if(points.size()<=12){p.setColor(NightDialTokens.RULE);for(int index=0;index<points.size();index++)c.drawLine(x(index),top,x(index),bottom,p);}p.setColor(Design.LINE);p.setStrokeWidth(d);c.drawLine(left(),bottom,right(),bottom,p);
        if(!bedtime&&goal>0){p.setColor(Design.MUTED);p.setTextAlign(Paint.Align.RIGHT);c.drawText(goalLabel(),right(),-labelMetrics().ascent(),p);}

        Path line=new Path();boolean connected=false,any=false;float width=Math.max(d,Math.min(29*d,(right()-left())/points.size()*.66f));
        for(int i=0;i<points.size();i++){Point point=points.get(i);if(point.value==null){connected=false;continue;}any=true;float xx=x(i),yy=y(point.value,top,bottom);p.setColor(i==selected?NightDialTokens.TEXT:NightDialTokens.ACCENT);
            if(bedtime){if(!connected){line.moveTo(xx,yy);connected=true;}else line.lineTo(xx,yy);}
            else{p.setColor(i==selected?NightDialTokens.TEXT:NightDialTokens.ACCENT);if(point.value>0){column.reset();column.addRoundRect(new RectF(xx-width/2,yy,xx+width/2,bottom),new float[]{4*d,4*d,4*d,4*d,0,0,0,0},Path.Direction.CW);c.drawPath(column,p);}p.setShader(null);if(points.size()<=7&&i!=selected){p.setColor(Design.INK);p.setTypeface(Design.font(getContext(),500));p.setTextAlign(Paint.Align.CENTER);long minutes=Math.round(point.value*60);c.drawText(minutes/60+String.format(Locale.getDefault(),":%02d",minutes%60),xx,yy-6*d,p);p.setTypeface(Design.font(getContext(),400));}}
        }
        if(!bedtime&&goal>0){float goalY=y(goal/3600000d,top,bottom);p.setColor(Design.MUTED);p.setStrokeWidth(.8f*d);p.setPathEffect(new DashPathEffect(new float[]{4*d,4*d},0));c.drawLine(left(),goalY,right(),goalY,p);p.setPathEffect(null);}
        if(bedtime){int violet=NightDialTokens.ACCENT;p.setColor(violet);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.3f*d);c.drawPath(line,p);for(int i=0;i<points.size();i++){Point point=points.get(i);if(point.value==null)continue;float xx=x(i),yy=y(point.value,top,bottom);p.setStyle(Paint.Style.FILL);p.setColor(i==selected?NightDialTokens.TEXT:NightDialTokens.ACCENT);c.drawCircle(xx,yy,3*d,p);p.setStyle(Paint.Style.STROKE);p.setColor(i==selected?Design.BROWN:violet);c.drawCircle(xx,yy,3*d,p);}p.setStyle(Paint.Style.FILL);}
        p.setColor(Design.MUTED);p.setTextAlign(Paint.Align.CENTER);if(!any)c.drawText(bedtime?"No main sleep periods in this range":"No recorded days in this range",getWidth()/2f,(top+bottom)/2,p);
        int stride=points.size()<=7?1:points.size()==12?2:7;for(int i=0;i<points.size();i+=stride){LocalDate date=points.get(i).date;String label=points.size()<=7?date.format(DateTimeFormatter.ofPattern("EEE",Locale.getDefault())):points.size()==12?date.format(DateTimeFormatter.ofPattern("MMM",Locale.getDefault())):Integer.toString(date.getDayOfMonth());c.drawText(label,x(i),bottom+16*d,p);if(points.size()<=7)c.drawText(date.format(DateTimeFormatter.ofPattern("d MMM",Locale.getDefault())),x(i),bottom+29*d,p);}
        if(selected>=0){p.setColor(NightDialTokens.ACCENT);p.setStrokeWidth(.7f*d);c.drawLine(x(selected),top,x(selected),bottom,p);String value=selectedLabel();p.setTextSize(Design.sp(getContext(),10*Appearance.scale(getContext())));p.setTextAlign(Paint.Align.CENTER);RectF box=selectedLabelBounds();p.setColor(Design.BROWN);c.drawRoundRect(box,10*d,10*d,p);p.setColor(NightDialTokens.ACTION_INK);c.drawText(value,box.centerX(),box.centerY()-(p.ascent()+p.descent())/2,p);}
    }
    private void describe(){Point point=points.get(selected);setContentDescription(point.date+": "+(point.value==null?"no record":bedtime?bedtimeLabel(point.value):RecordUi.roundedDuration((long)(point.value*3600000))));}
    @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()==MotionEvent.ACTION_UP){selected=Math.max(0,Math.min(points.size()-1,(int)((e.getX()-left())/(right()-left())*points.size())));describe();selection.accept(points.get(selected).date);InteractionFeedback.selection(this);performClick();invalidate();}return true;}
    @Override public boolean performClick(){super.performClick();return true;}
    @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(info);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);}
    @Override public boolean performAccessibilityAction(int action,android.os.Bundle args){int step=action==AccessibilityNodeInfo.ACTION_SCROLL_FORWARD?1:action==AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD?-1:0;if(step==0)return super.performAccessibilityAction(action,args);int next=selected<0?(step>0?0:points.size()-1):selected+step;if(next<0||next>=points.size())return false;selected=next;describe();selection.accept(points.get(selected).date);InteractionFeedback.selection(this);invalidate();return true;}
}
