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
    private final RecordEntry record;private final RecordDraft draft;private final RecordRibbon.Signal signal;private final Change change;private final Paint p=new Paint(3);private final RectF coverageBounds=new RectF();private long axisStart,axisEnd;private int handle=-1,activePointer=MotionEvent.INVALID_POINTER_ID,keyboardHandle;
    private float downX,downY,grabX;private boolean dragging;private long gestureStart,gestureEnd;
    private List<RecordIntervals.Span> gestureAwake=List.of();
    private static final int START_EARLIER=R.id.period_start_earlier,START_LATER=R.id.period_start_later,END_EARLIER=R.id.period_end_earlier,END_LATER=R.id.period_end_later;
    private final List<Long> axisTicks=new ArrayList<>();private final List<String> axisLabels=new ArrayList<>();
    PeriodTimeline(Context c,RecordEntry record,RecordDraft draft,Change change){this(c,record,draft,List.of(),change);}
    PeriodTimeline(Context c,RecordEntry record,RecordDraft draft,List<org.json.JSONObject> epochs,Change change){super(c);this.record=record;this.draft=draft;this.change=change;signal=new RecordRibbon.Signal(record,epochs,24,record==null?draft.start:record.rawStart,record==null?draft.end:record.rawEnd);long start=record==null||record.manual()?Math.min(record==null?draft.start:record.rawStart,draft.start):record.rawStart,end=record==null||record.manual()?Math.max(record==null?draft.end:record.rawEnd,draft.end):record.rawEnd;long margin=Math.min(3600000L,Math.max(1000,(end-start)/8));axisStart=Math.max(1,start-margin);axisEnd=end+margin;if(end-start>=4*3600000L){ZoneId zone=record==null?ZoneId.systemDefault():record.zone();java.time.ZonedDateTime first=java.time.Instant.ofEpochMilli(start).atZone(zone).withMinute(0).withSecond(0).withNano(0).minusHours(1),last=java.time.Instant.ofEpochMilli(end).atZone(zone);if(last.getMinute()!=0||last.getSecond()!=0||last.getNano()!=0)last=last.plusHours(1);axisStart=first.toInstant().toEpochMilli();axisEnd=last.withMinute(0).withSecond(0).withNano(0).toInstant().toEpochMilli();}setFocusable(true);setFocusableInTouchMode(true);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);describe();}
    private String clock(long time,ZoneId zone){return axisEnd-axisStart<120000?RecordUi.preciseClock(getContext(),time,zone):RecordUi.clock(getContext(),time,zone);}
    private float d(){return getResources().getDisplayMetrics().density;}
    private float x(long time){return 10*d()+(getWidth()-20*d())*(time-axisStart)/(axisEnd-axisStart);}
    private long time(float x){return axisStart+(long)((axisEnd-axisStart)*Math.max(0,Math.min(1,(x-10*d())/(getWidth()-20*d()))));}
    void changed(){if(draft.start<axisStart||draft.end>axisEnd){long margin=Math.min(3600000L,Math.max(1000,(draft.end-draft.start)/8));axisStart=Math.min(axisStart,Math.max(1,draft.start-margin));axisEnd=Math.max(axisEnd,draft.end+margin);}labels();describe();invalidate();}
    @Override protected void onSizeChanged(int width,int height,int oldWidth,int oldHeight){super.onSizeChanged(width,height,oldWidth,oldHeight);labels();}
    private void labels(){axisTicks.clear();axisLabels.clear();boolean overnight=axisEnd-axisStart>=4*3600000L;ZoneId zone=record==null?ZoneId.systemDefault():record.zone();if(overnight){long step=axisEnd-axisStart>16*3600000L?6*3600000L:2*3600000L;for(long t=axisStart;t<axisEnd;t+=step)axisTicks.add(t);if(axisTicks.size()>1&&x(axisEnd)-x(axisTicks.get(axisTicks.size()-1))<40*d())axisTicks.remove(axisTicks.size()-1);axisTicks.add(axisEnd);}else for(int n=0;n<3;n++)axisTicks.add(axisStart+(axisEnd-axisStart)*n/2);for(long time:axisTicks)axisLabels.add(overnight?java.time.Instant.ofEpochMilli(time).atZone(zone).format(java.time.format.DateTimeFormatter.ofPattern(android.text.format.DateFormat.is24HourFormat(getContext())?"HH:mm":"h a",Locale.getDefault())):clock(time,zone));}
    private void describe(){ZoneId zone=record==null?ZoneId.systemDefault():record.zone();setContentDescription("Editable period from "+clock(draft.start,zone)+" to "+clock(draft.end,zone)+". Drag either handle or use the Start and End time buttons. Keyboard: Up selects Start, Down selects End; Left and Right adjust the selected time. "+(record==null||record.manual()?"User-reported coverage; no movement measurements.":signal.measured()?"Shading is measured relative movement, not sleep stages; gaps are pauses or marked awake.":"Recorded coverage; movement measurements unavailable." ));setStateDescription((keyboardHandle==0?"Start":"End")+" handle selected");}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float d=d(),mid=64*d;ZoneId zone=record==null?ZoneId.systemDefault():record.zone();p.setTypeface(Design.font(getContext(),400));p.setTextSize(Design.sp(getContext(),10*Appearance.scale(getContext())));p.setStrokeWidth(d);p.setColor(Design.LINE);for(int n=0;n<=6;n++){float x=10*d+(getWidth()-20*d)*n/6;c.drawLine(x,34*d,x,95*d,p);}
        p.setColor(Design.SOFT);c.drawRoundRect(10*d,mid-9*d,getWidth()-10*d,mid+9*d,3*d,3*d,p);
        RecordEntry projected=record==null?null:record.draft(draft);List<RecordIntervals.Span> spans=projected==null?RecordIntervals.subtract(List.of(new RecordIntervals.Span(draft.start,draft.end)),draft.awake):projected.coverage();
        if(projected!=null){coverageBounds.set(10*d,mid-10*d,getWidth()-10*d,mid+10*d);signal.draw(c,coverageBounds,projected,axisStart,axisEnd,p,d);}
        else{p.setColor(0xffd0baf6);for(RecordIntervals.Span s:spans)c.drawRoundRect(x(s.start),mid-10*d,x(s.end),mid+10*d,4*d,4*d,p);}
        // An awake edit remains a gap in coverage; its amber underline is a user annotation.
        p.setColor(0xffd7a372);for(RecordIntervals.Span s:RecordIntervals.intersect(draft.awake,draft.start,draft.end))c.drawRect(x(s.start),mid+12*d,x(s.end),mid+15*d,p);
        for(int n=0;n<2;n++){long time=n==0?draft.start:draft.end;float x=x(time);p.setColor(Design.BROWN);c.drawRoundRect(x-5*d,mid-16*d,x+5*d,mid+16*d,5*d,5*d,p);if(hasFocus()&&!isInTouchMode()&&keyboardHandle==n){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2*d);c.drawRoundRect(x-8*d,mid-20*d,x+8*d,mid+20*d,8*d,8*d,p);p.setStyle(Paint.Style.FILL);p.setStrokeWidth(d);}String label=clock(time,zone);float half=p.measureText(label)/2+8*d,center=Math.max(half,Math.min(getWidth()-half,x));c.drawRoundRect(center-half,4*d,center+half,27*d,11*d,11*d,p);Path pointer=new Path();pointer.moveTo(center-4*d,27*d);pointer.lineTo(x,34*d);pointer.lineTo(center+4*d,27*d);pointer.close();c.drawPath(pointer,p);p.setColor(Design.CREAM);p.setTextAlign(Paint.Align.CENTER);c.drawText(label,center,20*d,p);}
        p.setColor(Design.MUTED);for(int n=0;n<axisTicks.size();n++){p.setTextAlign(n==0?Paint.Align.LEFT:n==axisTicks.size()-1?Paint.Align.RIGHT:Paint.Align.CENTER);c.drawText(axisLabels.get(n),x(axisTicks.get(n)),getHeight()-8*d,p);}p.setTextAlign(Paint.Align.LEFT);
    }
    private long step(){return draft.end-draft.start<120000?1000:60000;}
    private boolean move(int target,long at){
        if(target!=0&&target!=1)return false;
        boolean manual=record==null||record.manual();long step=step();at=Math.round(at/(double)step)*step;
        long beforeStart=draft.start,beforeEnd=draft.end;
        if(target==0){long low=manual?Math.max(axisStart,draft.end-36*3600000L):record.rawStart;draft.start=Math.max(low,Math.min(draft.end-1,at));}
        else{long high=manual?Math.min(Math.min(axisEnd,System.currentTimeMillis()),draft.start+36*3600000L):record.rawEnd;draft.end=Math.min(high,Math.max(draft.start+1,at));}
        if(beforeStart==draft.start&&beforeEnd==draft.end)return false;
        // Preview every position against the gesture baseline, not a previously clipped preview.
        List<RecordIntervals.Span> baseline=activePointer==MotionEvent.INVALID_POINTER_ID?new ArrayList<>(draft.awake):gestureAwake;
        List<RecordIntervals.Span> kept=activePointer!=MotionEvent.INVALID_POINTER_ID&&draft.start==gestureStart&&draft.end==gestureEnd?baseline:RecordIntervals.intersect(baseline,draft.start,draft.end);
        draft.awake.clear();draft.awake.addAll(kept);change.bounds(draft.start,draft.end);describe();invalidate();return true;
    }
    private boolean inputAllowed(){return isEnabled()&&isShown()&&isAttachedToWindow()&&hasWindowFocus();}
    private void intercept(boolean hold){if(getParent()!=null)getParent().requestDisallowInterceptTouchEvent(hold);}
    private int hit(float px,float py){
        float half=24*d(),top=64*d()-half,bottom=64*d()+half;
        if(py<top||py>bottom)return -1;
        float start=x(draft.start),end=x(draft.end);
        float a=Math.max(0,Math.min(getWidth()-2*half,start-half)),b=Math.max(0,Math.min(getWidth()-2*half,end-half));
        boolean first=px>=a&&px<=a+2*half,last=px>=b&&px<=b+2*half;
        return first&&last?(Math.abs(px-start)<=Math.abs(px-end)?0:1):first?0:last?1:-1;
    }
    private void clearGesture(){handle=-1;activePointer=MotionEvent.INVALID_POINTER_ID;dragging=false;gestureAwake=List.of();intercept(false);}
    /** Cancellation is transactional, including annotations clipped by a transient preview. */
    void cancelDrag(){
        if(activePointer==MotionEvent.INVALID_POINTER_ID){clearGesture();return;}
        long start=gestureStart,end=gestureEnd;List<RecordIntervals.Span> awake=new ArrayList<>(gestureAwake);
        boolean changed=draft.start!=start||draft.end!=end;clearGesture();
        draft.start=start;draft.end=end;draft.awake.clear();draft.awake.addAll(awake);
        if(changed)change.bounds(start,end);describe();invalidate();
    }
    @Override public boolean onTouchEvent(MotionEvent event){
        int action=event.getActionMasked();
        if(action==MotionEvent.ACTION_DOWN){
            cancelDrag();if(!inputAllowed()||event.getPointerCount()!=1)return false;
            int target=hit(event.getX(),event.getY());if(target<0)return false;
            handle=target;keyboardHandle=target;activePointer=event.getPointerId(0);downX=event.getX();downY=event.getY();grabX=x(target==0?draft.start:draft.end);
            gestureStart=draft.start;gestureEnd=draft.end;gestureAwake=new ArrayList<>(draft.awake);describe();invalidate();return true;
        }
        if(activePointer==MotionEvent.INVALID_POINTER_ID)return false;
        if(action==MotionEvent.ACTION_CANCEL||action==MotionEvent.ACTION_POINTER_DOWN||action==MotionEvent.ACTION_POINTER_UP||event.getPointerCount()!=1||!inputAllowed()){
            cancelDrag();return true;
        }
        int index=event.findPointerIndex(activePointer);if(index<0){cancelDrag();return true;}
        if(action==MotionEvent.ACTION_MOVE){
            float dx=event.getX(index)-downX,dy=event.getY(index)-downY;int slop=ViewConfiguration.get(getContext()).getScaledTouchSlop();
            if(!dragging){if(Math.abs(dy)>slop&&Math.abs(dy)>=Math.abs(dx)){cancelDrag();return true;}if(Math.abs(dx)<=slop)return true;dragging=true;intercept(true);}
            move(handle,time(grabX+dx));return true;
        }
        if(action==MotionEvent.ACTION_UP){
            if(dragging)move(handle,time(grabX+event.getX(index)-downX));
            boolean changed=draft.start!=gestureStart||draft.end!=gestureEnd;clearGesture();
            if(changed)InteractionFeedback.selection(this);performClick();return true;
        }
        return true;
    }
    @Override public boolean performClick(){super.performClick();return true;}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(!focused)cancelDrag();}
    @Override protected void onDetachedFromWindow(){cancelDrag();super.onDetachedFromWindow();}
    private boolean adjust(int target,int sign,boolean feedback){cancelDrag();keyboardHandle=target;boolean changed=move(target,(target==0?draft.start:draft.end)+sign*step());if(changed&&feedback)InteractionFeedback.selection(this);describe();invalidate();return changed;}
    @Override public boolean onKeyDown(int code,KeyEvent event){
        if(!inputAllowed())return super.onKeyDown(code,event);
        if(code==KeyEvent.KEYCODE_DPAD_UP||code==KeyEvent.KEYCODE_DPAD_DOWN){cancelDrag();keyboardHandle=code==KeyEvent.KEYCODE_DPAD_UP?0:1;describe();invalidate();return true;}
        if(code==KeyEvent.KEYCODE_DPAD_LEFT||code==KeyEvent.KEYCODE_DPAD_RIGHT){adjust(keyboardHandle,code==KeyEvent.KEYCODE_DPAD_RIGHT?1:-1,event.getRepeatCount()==0);return true;}
        return super.onKeyDown(code,event);
    }
    @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info){
        super.onInitializeAccessibilityNodeInfo(info);
        info.addAction(new AccessibilityNodeInfo.AccessibilityAction(START_EARLIER,"Move start earlier"));info.addAction(new AccessibilityNodeInfo.AccessibilityAction(START_LATER,"Move start later"));
        info.addAction(new AccessibilityNodeInfo.AccessibilityAction(END_EARLIER,"Move end earlier"));info.addAction(new AccessibilityNodeInfo.AccessibilityAction(END_LATER,"Move end later"));
        info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
    }
    @Override public boolean performAccessibilityAction(int action,android.os.Bundle args){
        int target=action==START_EARLIER||action==START_LATER?0:action==END_EARLIER||action==END_LATER?1:keyboardHandle;
        int sign=action==START_EARLIER||action==END_EARLIER||action==AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD?-1:action==START_LATER||action==END_LATER||action==AccessibilityNodeInfo.ACTION_SCROLL_FORWARD?1:0;
        if(sign==0)return super.performAccessibilityAction(action,args);if(!inputAllowed())return false;adjust(target,sign,true);return true;
    }
}
