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
    private final java.time.ZoneId zone;private final String metric;private final long start;private long end;private final boolean night;private final android.text.TextPaint paint=new android.text.TextPaint(3);
    private List<ChartSeries.Bar> bars=Collections.emptyList();private int selected=-1;private Long inspectedAt;private double high=.001;private String measurementLabel;private int actualReadings;private final Path column=new Path();
    private boolean compactLabels;
    private Runnable liveReload,liveRetry;private String liveMessage;private PhoneMotionChartReader.Data liveData;
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
    /** LIVE loading uses this same view; Report charts keep their existing synchronous in-memory API. */
    void liveSource(Runnable reload){liveReload=reload;liveMessage("Opening movement history\u2026",null);}
    int liveColumns(){return Math.max(1,Math.min(500,(int)((plotRight()-plotLeft())/(6*density()))));}
    PhoneMotionChartReader.Data renderedLiveData(){return liveData;}
    Long inspectedTime(){return inspectedAt;}
    boolean startsAt(long time){return start==time;}
    void liveData(PhoneMotionChartReader.Data data){if(data.start!=start)return;liveData=data;liveMessage=null;liveRetry=null;end=data.end;bars=data.bars;high=data.high;actualReadings=data.readings;describeSeries();selectInspectedTime();if(inspectedAt!=null)describeSelection();invalidate();}
    void liveMessage(String message,Runnable retry){liveData=null;liveMessage=message;liveRetry=retry;bars=Collections.emptyList();selected=-1;setContentDescription(message);invalidate();}
    RecordChart measurement(String label){measurementLabel=label;describeSeries();return this;}
    RecordChart compactLabels(){compactLabels=true;return this;}
    private String metricName(){return measurementLabel==null?metric:measurementLabel;}
    private void describeSeries(){setContentDescription(metricName()+" chart. "+actualReadings+" actual readings. Missing intervals are blank. Dense bars average contiguous readings. Tap a time or use the forward and backward actions to inspect a value.");}
    static String clock(long time){return java.time.Instant.ofEpochMilli(time).atZone(java.time.ZoneId.systemDefault()).format(java.time.format.DateTimeFormatter.ofPattern("h:mm a",Locale.getDefault()));}
    private boolean clock24(){return android.text.format.DateFormat.is24HourFormat(getContext());}
    private String label(long time){return ChartTicks.tooltip(time,start,end,zone,clock24(),Locale.getDefault());}
    private String axisTime(long time){return ChartTicks.tooltip(time,start,end,zone,clock24(),Locale.getDefault());}
    private float density(){return getResources().getDisplayMetrics().density;}
    private float textSize(){return Design.sp(getContext(),10*Appearance.scale(getContext()));}
    private int ink(){return night?NightDialTokens.SECONDARY:Appearance.color(getContext(),Design.MUTED);}
    private float plotLeft(){return Math.max(28*density(),textSize()*2.8f);}
    private float plotRight(){return getWidth()-8*density();}
    private float x(long time){return plotLeft()+(plotRight()-plotLeft())*(time-start)/(end-start);}
    @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){
        super.onSizeChanged(w,h,oldw,oldh);if(liveReload!=null)liveReload.run();else rebuild();
    }
    @Override protected void onFocusChanged(boolean focused,int direction,Rect previous){super.onFocusChanged(focused,direction,previous);invalidate();}
    private void rebuild(){bars=ChartSeries.aggregate(samples,start,end,Math.max(1,(int)((plotRight()-plotLeft())/(6*density()))));high=.001;for(ChartSeries.Bar bar:bars)high=Math.max(high,bar.value);selectInspectedTime();if(inspectedAt!=null)describeSelection();}
    private void selectInspectedTime(){selected=-1;if(inspectedAt==null)return;for(int n=0;n<bars.size();n++){ChartSeries.Bar bar=bars.get(n);if(inspectedAt>=bar.from&&inspectedAt<=bar.to){selected=n;return;}}}
    private String valueLabel(ChartSeries.Bar bar){return String.format(Locale.getDefault(),"%.3g",bar.value)+(metric.equals("light")?" lx":metric.equals("movement")||metric.equals("noise")?" relative":"");}
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);float d=density(),w=getWidth(),h=getHeight(),left=plotLeft(),right=plotRight();
        paint.setTypeface(Design.font(getContext(),400));paint.setTextSize(textSize());paint.setStyle(Paint.Style.FILL);paint.setTextAlign(Paint.Align.LEFT);
        List<ChartTicks.Tick> candidates=ChartTicks.candidates(start,end,zone,clock24(),Locale.getDefault(),5);List<ChartTicks.Placed> ticks=ChartTicks.layout(candidates,start,end,left,right,8*d,paint::measureText);int lines=1;for(ChartTicks.Placed tick:ticks)lines=Math.max(lines,tick.tick.label.split("\n").length);
        Paint.FontMetrics font=paint.getFontMetrics();float lineHeight=font.descent-font.ascent+2*d,capsule=compactLabels?Math.max(24*d,lineHeight+8*d):Math.max(36*d,2*lineHeight+8*d),top=capsule+4*d,bottom=h-Math.max(28*d,lineHeight*lines+10*d);
        paint.setColor(ink());canvas.drawText("High",0,top+5*d,paint);canvas.drawText("Low",0,bottom,paint);
        paint.setColor(night?NightDialTokens.RULE:Appearance.contrast(getContext())?0xffa29185:0xffeee6dc);paint.setStrokeWidth(d*.7f);canvas.drawLine(left,bottom,right,bottom,paint);for(ChartTicks.Placed tick:ticks)if(tick.tick.endpoint==0){float grid=x(tick.tick.time);canvas.drawLine(grid,top,grid,bottom,paint);}
        for(int n=0;n<bars.size();n++){
            ChartSeries.Bar bar=bars.get(n);float center=x(bar.from+(bar.to-bar.from)/2),width=Math.max(d,Math.min(14*d,(x(bar.to)-x(bar.from))*.8f));center=Math.min(right-width/2,Math.max(left+width/2,center));
            float y=(float)(bottom-(bottom-top)*(bar.value/high));y=Math.min(y,bottom-2*d); // Divide before multiplying: even a finite extreme reading must stay drawable.
            int color=n==selected?0xffaa7352:Appearance.contrast(getContext())?(night?Design.LILAC:0xff6b3dc4):Design.VIOLET;
            if(night)paint.setColor(n==selected?NightDialTokens.TEXT:0xffc5a367);else if(!Appearance.contrast(getContext()))paint.setShader(new LinearGradient(0,y,0,bottom,color,n==selected?0xffd3ad94:0xffd6c2fa,Shader.TileMode.CLAMP));else paint.setColor(n==selected?Design.BROWN:color);
            column.reset();column.addRoundRect(new RectF(center-width/2,y,center+width/2,bottom),new float[]{3*d,3*d,3*d,3*d,0,0,0,0},Path.Direction.CW);canvas.drawPath(column,paint);paint.setShader(null);
        }
        if(inspectedAt!=null&&liveMessage==null){
            float center=x(inspectedAt);String timeLabel=selected>=0?(bars.get(selected).readings>1?axisTime(bars.get(selected).from)+"–"+axisTime(bars.get(selected).to):axisTime(bars.get(selected).to)):axisTime(inspectedAt);
            String value=selected>=0?valueLabel(bars.get(selected))+(bars.get(selected).readings>1?" avg":""):"No readings";timeLabel=timeLabel.replace('\n',' ');
            boolean valueOnLeft=compactLabels&&center>w/2;float captionWidth=compactLabels?Math.min(w*.36f,paint.measureText(value)):0;
            float labelLeft=valueOnLeft?captionWidth+12*d:0,labelRight=compactLabels&&!valueOnLeft?w-captionWidth-12*d:w;
            float available=Math.max(1,labelRight-labelLeft-24*d);timeLabel=android.text.TextUtils.ellipsize(timeLabel,paint,available,android.text.TextUtils.TruncateAt.END).toString();value=android.text.TextUtils.ellipsize(value,paint,compactLabels?Math.max(1,captionWidth):Math.max(1,w-24*d),android.text.TextUtils.TruncateAt.END).toString();
            float half=Math.min((labelRight-labelLeft)/2,(compactLabels?paint.measureText(timeLabel):Math.max(paint.measureText(timeLabel),paint.measureText(value)))/2+10*d),tx=Math.max(labelLeft+half,Math.min(labelRight-half,center));
            paint.setColor(Design.BROWN);paint.setStrokeWidth(d*.7f);
            if(selected>=0){ChartSeries.Bar bar=bars.get(selected);float point=x(bar.from+(bar.to-bar.from)/2),y=(float)(bottom-(bottom-top)*(bar.value/high));y=Math.min(y,bottom-2*d);canvas.drawLine(point,capsule,point,y,paint);canvas.drawCircle(point,y,3*d,paint);}
            canvas.drawRoundRect(tx-half,0,tx+half,capsule,8*d,8*d,paint);paint.setTextAlign(Paint.Align.CENTER);paint.setColor(Design.CREAM);float first=compactLabels?capsule/2-(font.ascent+font.descent)/2:(capsule-2*lineHeight)/2-font.ascent;canvas.drawText(timeLabel,tx,first,paint);if(!compactLabels)canvas.drawText(value,tx,first+lineHeight,paint);else{paint.setColor(Design.BROWN);paint.setTextAlign(valueOnLeft?Paint.Align.LEFT:Paint.Align.RIGHT);canvas.drawText(value,valueOnLeft?0:w,first,paint);}
        }
        if(bars.isEmpty()){paint.setColor(ink());paint.setTextAlign(Paint.Align.CENTER);if(liveMessage==null)canvas.drawText("No readings for this signal",w/2,(top+bottom)/2,paint);else{String[] words=liveMessage.split(" ");String line="";List<String> linesText=new ArrayList<>();for(String word:words){String next=line.isEmpty()?word:line+" "+word;if(!line.isEmpty()&&paint.measureText(next)>Math.max(1,right-left)){linesText.add(line);line=word;}else line=next;}if(!line.isEmpty())linesText.add(line);for(int n=0;n<linesText.size();n++)canvas.drawText(linesText.get(n),(left+right)/2,(top+bottom)/2+(n-(linesText.size()-1)/2f)*lineHeight,paint);}}
        paint.setColor(ink());paint.setTextAlign(Paint.Align.CENTER);for(ChartTicks.Placed tick:ticks){String[] labels=tick.tick.label.split("\n");for(int n=0;n<labels.length;n++)canvas.drawText(labels[n],(float)tick.center,h-7*d-(labels.length-1-n)*lineHeight,paint);}
        if(hasFocus()){paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.5f*d);paint.setColor(night?Design.LILAC:0xff8952e4);canvas.drawRoundRect(left-2*d,top-2*d,right+2*d,bottom+2*d,5*d,5*d,paint);paint.setStyle(Paint.Style.FILL);}
    }
    @Override public boolean onTouchEvent(MotionEvent event){
        if(liveMessage!=null){if(event.getAction()==MotionEvent.ACTION_UP)performClick();return true;}
        if(event.getAction()==MotionEvent.ACTION_UP){float ratio=Math.max(0,Math.min(1,(event.getX()-plotLeft())/Math.max(1,plotRight()-plotLeft())));inspectedAt=start+(long)((end-start)*ratio);selectInspectedTime();InteractionFeedback.selection(this);performClick();invalidate();return true;}return true;
    }
    private void describeSelection(){if(inspectedAt==null)return;if(selected<0){setContentDescription(metricName()+" at "+label(inspectedAt)+": no readings for this interval.");return;}ChartSeries.Bar bar=bars.get(selected);setContentDescription(metricName()+" at "+label(bar.to)+": "+valueLabel(bar)+(bar.readings>1?" average of "+bar.readings+" readings from "+label(bar.from):" measured reading"));}
    @Override public boolean performClick(){super.performClick();if(liveMessage!=null){if(liveRetry!=null)liveRetry.run();return true;}if(inspectedAt==null&&!bars.isEmpty()){selected=0;inspectedAt=bars.get(0).from+(bars.get(0).to-bars.get(0).from)/2;}describeSelection();return true;}
    @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(info);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);}
    private boolean step(int direction){int next=selected<0?(direction>0?0:bars.size()-1):selected+direction;if(selected<0&&inspectedAt!=null){while(next>=0&&next<bars.size()&&(direction>0?bars.get(next).to<inspectedAt:bars.get(next).from>inspectedAt))next+=direction;}if(next<0||next>=bars.size())return false;selected=next;ChartSeries.Bar bar=bars.get(next);inspectedAt=bar.from+(bar.to-bar.from)/2;InteractionFeedback.selection(this);performClick();invalidate();return true;}
    @Override public boolean performAccessibilityAction(int action,android.os.Bundle args){int direction=action==AccessibilityNodeInfo.ACTION_SCROLL_FORWARD?1:action==AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD?-1:0;return direction==0?super.performAccessibilityAction(action,args):step(direction);}
    @Override public boolean onKeyDown(int key,KeyEvent event){if(liveRetry!=null&&(key==KeyEvent.KEYCODE_ENTER||key==KeyEvent.KEYCODE_DPAD_CENTER))return performClick();if(key==KeyEvent.KEYCODE_DPAD_RIGHT||key==KeyEvent.KEYCODE_DPAD_LEFT)return step(key==KeyEvent.KEYCODE_DPAD_RIGHT?1:-1);return super.onKeyDown(key,event);}
}
