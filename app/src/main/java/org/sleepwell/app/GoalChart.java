package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Real bedtime columns or signed daily/cumulative commitment progress. */
final class GoalChart extends View {
    /** The target key shares the heading until actual text widths require a second line. */
    static final class BedtimeHeading extends LinearLayout {
        private final TextView title,label;private final LinearLayout key;
        BedtimeHeading(Context context,int target){
            super(context);setGravity(Gravity.CENTER_VERTICAL);
            title=Design.text(context,"Your bedtime (last 7 nights)",14,600,Design.INK);addView(title);
            key=Design.row(context);View dash=new View(context){private final Paint ink=new Paint(3);@Override protected void onDraw(Canvas canvas){ink.setColor(Appearance.contrast(context)?Design.BROWN:0xffe47825);ink.setStrokeWidth(Design.dp(context,2));ink.setPathEffect(new DashPathEffect(new float[]{Design.dp(context,4),Design.dp(context,3)},0));canvas.drawLine(0,getHeight()/2f,getWidth(),getHeight()/2f,ink);}};
            key.addView(dash,new LinearLayout.LayoutParams(Design.dp(context,18),Design.dp(context,12)));
            label=Design.text(context,"Target "+Design.clock(context,target),11,400,Design.MUTED);LinearLayout.LayoutParams caption=new LinearLayout.LayoutParams(-2,-2);caption.leftMargin=Design.dp(context,5);key.addView(label,caption);addView(key);
        }
        @Override protected void onMeasure(int widthSpec,int heightSpec){
            int available=MeasureSpec.getSize(widthSpec)-getPaddingLeft()-getPaddingRight(),gap=Design.dp(getContext(),10);
            float natural=title.getPaint().measureText(title.getText().toString())+label.getPaint().measureText(label.getText().toString())+Design.dp(getContext(),23)+gap;
            boolean stack=natural>available;setOrientation(stack?VERTICAL:HORIZONTAL);
            LinearLayout.LayoutParams heading=(LinearLayout.LayoutParams)title.getLayoutParams();if(heading.width!=(stack?-1:0)||heading.weight!=(stack?0:1))title.setLayoutParams(new LinearLayout.LayoutParams(stack?-1:0,-2,stack?0:1));LinearLayout.LayoutParams slot=(LinearLayout.LayoutParams)key.getLayoutParams();int left=stack?0:gap,top=stack?Design.dp(getContext(),6):0,gravity=stack?Gravity.END:Gravity.CENTER_VERTICAL;if(slot.leftMargin!=left||slot.topMargin!=top||slot.gravity!=gravity){slot.leftMargin=left;slot.topMargin=top;slot.gravity=gravity;key.setLayoutParams(slot);}super.onMeasure(widthSpec,heightSpec);
        }
    }
    private final Paint p=new Paint(3),axis=new Paint(3);
    private final List<LocalDate> dates=new ArrayList<>();
    private final List<Double> values=new ArrayList<>();
    private final boolean bedtime,cumulative;
    private final double target;
    private double low,high;
    private int selected=-1;
    GoalChart(Context c,SortedMap<LocalDate,RecordTrends.Day> days,LocalDate end,int bedtimeTarget){
        super(c);bedtime=true;cumulative=false;target=RecordTrends.nightMinute(bedtimeTarget);
        for(int n=6;n>=0;n--){LocalDate date=end.minusDays(n);dates.add(date);RecordTrends.Day day=days.get(date);values.add(day==null?null:day.bedtime());}
        low=Math.floor(target/60)*60;high=low+60;for(Double v:values)if(v!=null){low=Math.min(low,Math.floor(v/60)*60);high=Math.max(high,Math.ceil(v/60)*60);}prepare();
    }
    GoalChart(Context c,GoalCommitment goal,GoalProgress progress,boolean cumulative){
        super(c);bedtime=false;this.cumulative=cumulative;target=0;double sum=0;
        for(int n=0;n<goal.days;n++){LocalDate date=goal.start.plusDays(n);dates.add(date);GoalProgress.Day day=n<progress.days.size()?progress.days.get(n):null;Double value=null;if(day!=null&&day.balance!=null){sum+=day.balance/60000d;value=cumulative?sum:day.balance/60000d;}values.add(value);}
        low=-30;high=30;for(Double v:values)if(v!=null){low=Math.min(low,Math.floor(v/30)*30);high=Math.max(high,Math.ceil(v/30)*30);}prepare();
    }
    private void prepare(){setFocusable(true);setContentDescription(bedtime?"Recorded bedtimes. Missing main periods are blank.":cumulative?"Cumulative observed goal balance. Missing days are blank.":"Daily observed goal surplus or deficit. Missing days are blank.");}
    private float d(){return getResources().getDisplayMetrics().density;}
    private float left(){if(!bedtime)return Math.max(50*d(),Design.sp(getContext(),10*Appearance.scale(getContext()))*5.2f);axis.setTypeface(Design.font(getContext(),400));axis.setTextSize(Design.sp(getContext(),10*Appearance.scale(getContext())));return Math.max(40*d(),Math.max(axis.measureText(value(low)),Math.max(axis.measureText(value(high)),axis.measureText(value((low+high)/2))))+8*d());}
    private float right(){return getWidth()-8*d();}
    private float x(int n){return left()+(right()-left())*(n+.5f)/dates.size();}
    private float y(double value,float top,float bottom){return (float)(bottom-(bottom-top)*(value-low)/Math.max(1,high-low));}
    private String clock(double minutes){return Design.clock(getContext(),Math.floorMod((int)Math.round(minutes),1440));}
    private String value(double amount){return bedtime?clock(amount):GoalProgress.signed((long)(amount*60000));}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float d=d(),top=(bedtime?12:26)*d,bottom=getHeight()-32*d;
        p.setTypeface(Design.font(getContext(),400));p.setTextSize(Design.sp(getContext(),(bedtime?10:9)*Appearance.scale(getContext())));p.setStrokeWidth(.7f*d);p.setTextAlign(Paint.Align.RIGHT);
        if(bedtime){p.setColor(Design.LINE);p.setAlpha(100);for(int n=0;n<dates.size();n++)c.drawLine(x(n),top,x(n),bottom,p);p.setAlpha(255);p.setColor(Design.LINE);c.drawLine(left(),top,left(),bottom,p);c.drawLine(left(),bottom,right(),bottom,p);}
        for(int n=0;n<3;n++){double v=low+(high-low)*n/2;float yy=y(v,top,bottom);if(!bedtime){p.setColor(Design.LINE);c.drawLine(left(),yy,right(),yy,p);}p.setColor(Design.MUTED);c.drawText(value(v),left()-6*d,yy+3*d,p);}
        if(!bedtime){p.setColor(0xffe47825);p.setPathEffect(new DashPathEffect(new float[]{4*d,4*d},0));c.drawLine(left(),y(target,top,bottom),right(),y(target,top,bottom),p);p.setPathEffect(null);p.setTextAlign(Paint.Align.RIGHT);p.setColor(Design.MUTED);c.drawText("0 · on target",right(),12*d,p);}
        float width=Math.max(d,Math.min(22*d,(right()-left())/dates.size()*.65f));boolean any=false,connected=false;Path line=new Path();
        for(int n=0;n<values.size();n++){Double v=values.get(n);if(v==null){connected=false;continue;}any=true;float xx=x(n),yy=y(v,top,bottom);int color=selected==n?Design.BROWN:Design.VIOLET;
            if(cumulative){if(!connected){line.moveTo(xx,yy);connected=true;}else line.lineTo(xx,yy);p.setColor(color);c.drawCircle(xx,yy,2.4f*d,p);}
            else{float base=bedtime?bottom:y(0,top,bottom),from=Math.min(base,yy),to=Math.max(base,yy);if(to-from<2*d){from=base-d;to=base+d;}if(Appearance.contrast(getContext()))p.setColor(color);else p.setShader(new LinearGradient(0,from,0,to,color,0xffdac6fa,Shader.TileMode.CLAMP));c.drawRoundRect(xx-width/2,from,xx+width/2,to,3*d,3*d,p);p.setShader(null);if(bedtime){p.setColor(Design.CREAM);c.drawCircle(xx,yy+Math.min(4*d,(bottom-yy)/2),2.2f*d,p);}}
        }
        if(cumulative){p.setColor(Design.VIOLET);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f*d);c.drawPath(line,p);p.setStyle(Paint.Style.FILL);}
        if(bedtime){p.setStrokeWidth(1.1f*d);p.setColor(Appearance.contrast(getContext())?Design.BROWN:0xffe47825);p.setPathEffect(new DashPathEffect(new float[]{4*d,4*d},0));c.drawLine(left(),y(target,top,bottom),right(),y(target,top,bottom),p);p.setPathEffect(null);}
        p.setColor(Design.MUTED);p.setTextAlign(Paint.Align.CENTER);if(!any)c.drawText(bedtime?"No main sleep periods yet":"No closed recorded days yet",getWidth()/2f,(top+bottom)/2,p);
        int stride=bedtime?1:Math.max(1,dates.size()/5);for(int n=0;n<dates.size();n+=stride)c.drawText(dates.get(n).format(DateTimeFormatter.ofPattern(bedtime?"EEE":"d MMM",Locale.getDefault())),x(n),bottom+18*d,p);
        if(selected>=0){String text=values.get(selected)==null?"No closed record":value(values.get(selected));float half=p.measureText(text)/2+8*d,center=Math.max(half,Math.min(getWidth()-half,x(selected)));p.setColor(Design.BROWN);c.drawRoundRect(center-half,0,center+half,20*d,10*d,10*d,p);p.setColor(Design.CREAM);c.drawText(text,center,14*d,p);}
    }
    private void select(int n){selected=n;setContentDescription(dates.get(n)+": "+(values.get(n)==null?"no closed record":value(values.get(n))));invalidate();}
    @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()==MotionEvent.ACTION_UP){select(Math.max(0,Math.min(dates.size()-1,(int)((e.getX()-left())/(right()-left())*dates.size()))));performClick();}return true;}
    @Override public boolean performClick(){super.performClick();return true;}
    @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(info);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);}
    @Override public boolean performAccessibilityAction(int action,android.os.Bundle args){int step=action==AccessibilityNodeInfo.ACTION_SCROLL_FORWARD?1:action==AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD?-1:0;if(step==0)return super.performAccessibilityAction(action,args);int next=selected<0?(step>0?0:dates.size()-1):selected+step;if(next<0||next>=dates.size())return false;select(next);performClick();return true;}
}
