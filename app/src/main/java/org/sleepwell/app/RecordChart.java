package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;
import org.json.JSONObject;
import java.util.*;

/** Rounded measured-activity bars. Dense recordings use contiguous display averages, never interpolation. */
class RecordChart extends View {
    private final List<ChartSeries.Sample> samples=new ArrayList<>();
    private final java.time.ZoneId zone;private final String metric;private final long start;private long end;private final boolean night;private final Paint paint=new Paint(3);
    private List<ChartSeries.Bar> bars=Collections.emptyList();private int selected=-1;private double high=.001;private String measurementLabel;private int actualReadings;
    RecordChart(Context c,List<JSONObject> epochs,String metric,long start,long end){
        this(c,epochs,metric,start,end,false);
    }
    RecordChart(Context c,List<JSONObject> epochs,String metric,long start,long end,boolean night){
        this(c,epochs,metric,start,end,night,java.time.ZoneId.systemDefault());
    }
    RecordChart(Context c,List<JSONObject> epochs,String metric,long start,long end,boolean night,java.time.ZoneId zone){
        super(c);this.zone=zone;this.metric=metric;this.start=start;this.night=night;setFocusable(true);setDefaultFocusHighlightEnabled(false);updateData(epochs,end);
    }
    /** Refresh measured samples in place so keyboard/TalkBack focus and time selection survive live ticks. */
    void updateData(List<JSONObject> epochs,long end){
        this.end=Math.max(start+1,end);samples.clear();int measured=0;
        for(JSONObject epoch:epochs){Double value=epoch.isNull(metric)?null:epoch.optDouble(metric);long time=epoch.optLong("time");samples.add(new ChartSeries.Sample(epoch.isNull("start")?null:epoch.optLong("start"),time,value));if(value!=null&&Double.isFinite(value)&&value>=0&&time>=start&&time<=end)measured++;}
        actualReadings=measured;describeSeries();rebuild();invalidate();
    }
    RecordChart measurement(String label){measurementLabel=label;describeSeries();return this;}
    private String metricName(){return measurementLabel==null?metric:measurementLabel;}
    private void describeSeries(){setContentDescription(metricName()+" chart. "+actualReadings+" actual readings. Missing intervals are blank. Dense bars average contiguous readings.");}
    static String clock(long time){return java.time.Instant.ofEpochMilli(time).atZone(java.time.ZoneId.systemDefault()).format(java.time.format.DateTimeFormatter.ofPattern("h:mm a",Locale.getDefault()));}
    private boolean clock24(){return android.text.format.DateFormat.is24HourFormat(getContext());}
    private String label(long time){return ChartTicks.tooltip(time,start,end,zone,clock24(),Locale.getDefault());}
    private String axisTime(long time){return ChartTicks.tooltip(time,start,end,zone,clock24(),Locale.getDefault());}
    private float density(){return getResources().getDisplayMetrics().density;}
    private float textSize(){return Design.sp(getContext(),10*Appearance.scale(getContext()));}
    private int ink(){return night?Design.LILAC:Appearance.color(getContext(),Design.MUTED);}
    private float plotLeft(){return Math.max(28*density(),textSize()*2.8f);}
    private float plotRight(){return getWidth()-8*density();}
    private float x(long time){return plotLeft()+(plotRight()-plotLeft())*(time-start)/(end-start);}
    @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){
        super.onSizeChanged(w,h,oldw,oldh);rebuild();
    }
    @Override protected void onFocusChanged(boolean focused,int direction,Rect previous){super.onFocusChanged(focused,direction,previous);invalidate();}
    private void rebuild(){Long selection=selected>=0&&selected<bars.size()?bars.get(selected).from+(bars.get(selected).to-bars.get(selected).from)/2:null;bars=ChartSeries.aggregate(samples,start,end,Math.max(1,(int)((plotRight()-plotLeft())/(6*density()))));selected=-1;high=.001;for(int n=0;n<bars.size();n++){ChartSeries.Bar bar=bars.get(n);high=Math.max(high,bar.value);if(selection!=null&&selection>=bar.from&&selection<=bar.to)selected=n;}if(selected>=0)describeSelection();}
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);float d=density(),w=getWidth(),h=getHeight(),left=plotLeft(),right=plotRight();
        paint.setTypeface(Design.font(getContext(),400));paint.setTextSize(textSize());paint.setStyle(Paint.Style.FILL);paint.setTextAlign(Paint.Align.LEFT);
        List<ChartTicks.Tick> candidates=ChartTicks.candidates(start,end,zone,clock24(),Locale.getDefault(),5);List<ChartTicks.Placed> ticks=ChartTicks.layout(candidates,start,end,left,right,8*d,paint::measureText);int lines=1;for(ChartTicks.Placed tick:ticks)lines=Math.max(lines,tick.tick.label.split("\n").length);
        Paint.FontMetrics font=paint.getFontMetrics();float lineHeight=font.descent-font.ascent+2*d,capsule=Math.max(22*d,font.descent-font.ascent+8*d),top=capsule+4*d,bottom=h-Math.max(28*d,lineHeight*lines+10*d);
        paint.setColor(ink());canvas.drawText("High",0,top+5*d,paint);canvas.drawText("Low",0,bottom,paint);
        paint.setColor(night?0xff62566d:Appearance.contrast(getContext())?0xffa29185:Design.LINE);paint.setStrokeWidth(d*.7f);canvas.drawLine(left,bottom,right,bottom,paint);canvas.drawLine(left,top,left,bottom,paint);canvas.drawLine(right,top,right,bottom,paint);for(ChartTicks.Placed tick:ticks)if(tick.tick.endpoint==0){float grid=x(tick.tick.time);canvas.drawLine(grid,top,grid,bottom,paint);}
        for(int n=0;n<bars.size();n++){
            ChartSeries.Bar bar=bars.get(n);float center=x(bar.from+(bar.to-bar.from)/2),width=Math.max(d,Math.min(14*d,(x(bar.to)-x(bar.from))*.8f));center=Math.min(right-width/2,Math.max(left+width/2,center));
            float y=(float)(bottom-(bottom-top)*(bar.value/high));y=Math.min(y,bottom-2*d); // Divide before multiplying: even a finite extreme reading must stay drawable.
            int color=n==selected?0xffaa7352:Appearance.contrast(getContext())?(night?Design.LILAC:0xff6b3dc4):Design.VIOLET;
            if(!Appearance.contrast(getContext()))paint.setShader(new LinearGradient(0,y,0,bottom,color,n==selected?0xffd3ad94:0xffd6c2fa,Shader.TileMode.CLAMP));else paint.setColor(n==selected?Design.BROWN:color);
            canvas.drawRoundRect(center-width/2,y,center+width/2,bottom,3*d,3*d,paint);paint.setShader(null);
            if(n==selected){
                paint.setColor(Design.BROWN);canvas.drawCircle(center,y,3*d,paint);String label=bar.readings>1?axisTime(bar.from)+"–"+axisTime(bar.to)+" avg":axisTime(bar.to);float half=paint.measureText(label)/2+10*d,tx=Math.max(half,Math.min(w-half,center));
                canvas.drawRoundRect(tx-half,0,tx+half,capsule,capsule/2,capsule/2,paint);paint.setTextAlign(Paint.Align.CENTER);paint.setColor(Design.CREAM);canvas.drawText(label,tx,(capsule-font.descent-font.ascent)/2,paint);
            }
        }
        if(bars.isEmpty()){paint.setColor(ink());paint.setTextAlign(Paint.Align.CENTER);canvas.drawText("No readings for this signal",w/2,(top+bottom)/2,paint);}
        paint.setColor(ink());paint.setTextAlign(Paint.Align.CENTER);for(ChartTicks.Placed tick:ticks){String[] labels=tick.tick.label.split("\n");for(int n=0;n<labels.length;n++)canvas.drawText(labels[n],(float)tick.center,h-7*d-(labels.length-1-n)*lineHeight,paint);}
        if(hasFocus()){paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.5f*d);paint.setColor(night?Design.LILAC:0xff8952e4);canvas.drawRoundRect(left-2*d,top-2*d,right+2*d,bottom+2*d,5*d,5*d,paint);paint.setStyle(Paint.Style.FILL);}
    }
    @Override public boolean onTouchEvent(MotionEvent event){
        if(event.getAction()==MotionEvent.ACTION_UP){float ratio=Math.max(0,Math.min(1,(event.getX()-plotLeft())/(plotRight()-plotLeft())));long time=start+(long)((end-start)*ratio),distance=Long.MAX_VALUE;
            for(int n=0;n<bars.size();n++){ChartSeries.Bar bar=bars.get(n);long center=bar.from+(bar.to-bar.from)/2,delta=Math.abs(center-time);if(delta<distance){selected=n;distance=delta;}}performClick();invalidate();return true;}return true;
    }
    private void describeSelection(){ChartSeries.Bar bar=bars.get(selected);setContentDescription(metricName()+" at "+label(bar.to)+": "+String.format(Locale.getDefault(),"%.3f",bar.value)+(bar.readings>1?" average of "+bar.readings+" readings from "+label(bar.from):" measured reading"));}
    @Override public boolean performClick(){super.performClick();if(selected>=0)describeSelection();return true;}
    @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(info);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);}
    @Override public boolean performAccessibilityAction(int action,android.os.Bundle args){int step=action==AccessibilityNodeInfo.ACTION_SCROLL_FORWARD?1:action==AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD?-1:0;if(step==0)return super.performAccessibilityAction(action,args);int next=selected<0?(step>0?0:bars.size()-1):selected+step;if(next<0||next>=bars.size())return false;selected=next;performClick();invalidate();return true;}
}
