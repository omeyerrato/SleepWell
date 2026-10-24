package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;
import java.time.ZoneId;
import java.util.*;

/** Native adjustable recording interval, with time-picker alternatives in the editor. */
@android.annotation.SuppressLint("ViewConstructor") // Mutable editor draft is required; never inflated from XML.
final class PeriodTimeline extends View {
    interface Change {void bounds(long start,long end);}
    private final RecordEntry record;private final RecordDraft draft;private final RecordRibbon.Signal signal;private final Change change;private final Paint p=new Paint(3);private final RectF coverageBounds=new RectF();private long axisStart,axisEnd;private int handle=-1;private final List<Long> axisTicks=new ArrayList<>();private final List<String> axisLabels=new ArrayList<>();
    PeriodTimeline(Context c,RecordEntry record,RecordDraft draft,Change change){this(c,record,draft,List.of(),change);}
    PeriodTimeline(Context c,RecordEntry record,RecordDraft draft,List<org.json.JSONObject> epochs,Change change){super(c);this.record=record;this.draft=draft;this.change=change;signal=new RecordRibbon.Signal(record,epochs,24,record==null?draft.start:record.rawStart,record==null?draft.end:record.rawEnd);long start=record==null||record.manual()?Math.min(record==null?draft.start:record.rawStart,draft.start):record.rawStart,end=record==null||record.manual()?Math.max(record==null?draft.end:record.rawEnd,draft.end):record.rawEnd;long margin=Math.min(3600000L,Math.max(1000,(end-start)/8));axisStart=Math.max(1,start-margin);axisEnd=end+margin;if(end-start>=4*3600000L){ZoneId zone=record==null?ZoneId.systemDefault():record.zone();java.time.ZonedDateTime first=java.time.Instant.ofEpochMilli(start).atZone(zone).withMinute(0).withSecond(0).withNano(0).minusHours(1),last=java.time.Instant.ofEpochMilli(end).atZone(zone);if(last.getMinute()!=0||last.getSecond()!=0||last.getNano()!=0)last=last.plusHours(1);axisStart=first.toInstant().toEpochMilli();axisEnd=last.withMinute(0).withSecond(0).withNano(0).toInstant().toEpochMilli();}setFocusable(true);describe();}
    private String clock(long time,ZoneId zone){return axisEnd-axisStart<120000?RecordUi.preciseClock(getContext(),time,zone):RecordUi.clock(getContext(),time,zone);}
    private float d(){return getResources().getDisplayMetrics().density;}
    private float x(long time){return 10*d()+(getWidth()-20*d())*(time-axisStart)/(axisEnd-axisStart);}
    private long time(float x){return axisStart+(long)((axisEnd-axisStart)*Math.max(0,Math.min(1,(x-10*d())/(getWidth()-20*d()))));}
    void changed(){if(draft.start<axisStart||draft.end>axisEnd){long margin=Math.min(3600000L,Math.max(1000,(draft.end-draft.start)/8));axisStart=Math.min(axisStart,Math.max(1,draft.start-margin));axisEnd=Math.max(axisEnd,draft.end+margin);}labels();describe();invalidate();}
    @Override protected void onSizeChanged(int width,int height,int oldWidth,int oldHeight){super.onSizeChanged(width,height,oldWidth,oldHeight);labels();}
    private void labels(){axisTicks.clear();axisLabels.clear();boolean overnight=axisEnd-axisStart>=4*3600000L;ZoneId zone=record==null?ZoneId.systemDefault():record.zone();if(overnight){long step=axisEnd-axisStart>16*3600000L?6*3600000L:2*3600000L;for(long t=axisStart;t<axisEnd;t+=step)axisTicks.add(t);if(axisTicks.size()>1&&x(axisEnd)-x(axisTicks.get(axisTicks.size()-1))<40*d())axisTicks.remove(axisTicks.size()-1);axisTicks.add(axisEnd);}else for(int n=0;n<3;n++)axisTicks.add(axisStart+(axisEnd-axisStart)*n/2);for(long time:axisTicks)axisLabels.add(overnight?java.time.Instant.ofEpochMilli(time).atZone(zone).format(java.time.format.DateTimeFormatter.ofPattern(android.text.format.DateFormat.is24HourFormat(getContext())?"HH:mm":"h a",Locale.getDefault())):clock(time,zone));}
    private void describe(){ZoneId zone=record==null?ZoneId.systemDefault():record.zone();setContentDescription("Editable period from "+clock(draft.start,zone)+" to "+clock(draft.end,zone)+". Drag either handle or use the Start and End time buttons. "+(record==null||record.manual()?"User-reported coverage; no movement measurements.":signal.measured()?"Shading is measured relative movement, not sleep stages; gaps are pauses or marked awake.":"Recorded coverage; movement measurements unavailable." ));}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float d=d(),mid=64*d;ZoneId zone=record==null?ZoneId.systemDefault():record.zone();p.setTypeface(Design.font(getContext(),400));p.setTextSize(Design.sp(getContext(),10*Appearance.scale(getContext())));p.setStrokeWidth(d);p.setColor(Design.LINE);for(int n=0;n<=6;n++){float x=10*d+(getWidth()-20*d)*n/6;c.drawLine(x,34*d,x,95*d,p);}
        p.setColor(Design.SOFT);c.drawRoundRect(10*d,mid-9*d,getWidth()-10*d,mid+9*d,3*d,3*d,p);
        RecordEntry projected=record==null?null:record.draft(draft);List<RecordIntervals.Span> spans=projected==null?RecordIntervals.subtract(List.of(new RecordIntervals.Span(draft.start,draft.end)),draft.awake):projected.coverage();
        if(projected!=null){coverageBounds.set(10*d,mid-10*d,getWidth()-10*d,mid+10*d);signal.draw(c,coverageBounds,projected,axisStart,axisEnd,p,d);}
        else{p.setColor(0xffd0baf6);for(RecordIntervals.Span s:spans)c.drawRoundRect(x(s.start),mid-10*d,x(s.end),mid+10*d,4*d,4*d,p);}
        // An awake edit remains a gap in coverage; its amber underline is a user annotation.
        p.setColor(0xffd7a372);for(RecordIntervals.Span s:RecordIntervals.intersect(draft.awake,draft.start,draft.end))c.drawRect(x(s.start),mid+12*d,x(s.end),mid+15*d,p);
        for(int n=0;n<2;n++){long time=n==0?draft.start:draft.end;float x=x(time);p.setColor(Design.BROWN);c.drawRoundRect(x-5*d,mid-16*d,x+5*d,mid+16*d,5*d,5*d,p);String label=clock(time,zone);float half=p.measureText(label)/2+8*d,center=Math.max(half,Math.min(getWidth()-half,x));c.drawRoundRect(center-half,4*d,center+half,27*d,11*d,11*d,p);Path pointer=new Path();pointer.moveTo(center-4*d,27*d);pointer.lineTo(x,34*d);pointer.lineTo(center+4*d,27*d);pointer.close();c.drawPath(pointer,p);p.setColor(Design.CREAM);p.setTextAlign(Paint.Align.CENTER);c.drawText(label,center,20*d,p);}
        p.setColor(Design.MUTED);for(int n=0;n<axisTicks.size();n++){p.setTextAlign(n==0?Paint.Align.LEFT:n==axisTicks.size()-1?Paint.Align.RIGHT:Paint.Align.CENTER);c.drawText(axisLabels.get(n),x(axisTicks.get(n)),getHeight()-8*d,p);}p.setTextAlign(Paint.Align.LEFT);
    }
    private void move(int target,long at){
        boolean manual=record==null||record.manual();long step=draft.end-draft.start<120000?1000:60000;at=Math.round(at/(double)step)*step;
        if(target==0){long low=manual?Math.max(axisStart,draft.end-36*3600000L):record.rawStart;draft.start=Math.max(low,Math.min(draft.end-1,at));}
        else{long high=manual?Math.min(Math.min(axisEnd,System.currentTimeMillis()),draft.start+36*3600000L):record.rawEnd;draft.end=Math.min(high,Math.max(draft.start+1,at));}
        change.bounds(draft.start,draft.end);describe();invalidate();
    }
    @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()==MotionEvent.ACTION_DOWN){handle=Math.abs(e.getX()-x(draft.start))<=Math.abs(e.getX()-x(draft.end))?0:1;getParent().requestDisallowInterceptTouchEvent(true);return true;}if(e.getAction()==MotionEvent.ACTION_MOVE||e.getAction()==MotionEvent.ACTION_UP){move(handle,time(e.getX()));if(e.getAction()==MotionEvent.ACTION_UP){getParent().requestDisallowInterceptTouchEvent(false);performClick();}return true;}if(e.getAction()==MotionEvent.ACTION_CANCEL)getParent().requestDisallowInterceptTouchEvent(false);return true;}
    @Override public boolean performClick(){super.performClick();return true;}
    @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(info);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);}
    @Override public boolean performAccessibilityAction(int action,android.os.Bundle args){int sign=action==AccessibilityNodeInfo.ACTION_SCROLL_FORWARD?1:action==AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD?-1:0;if(sign==0)return super.performAccessibilityAction(action,args);long step=draft.end-draft.start<120000?1000:60000;move(0,draft.start+sign*step);return true;}
}
