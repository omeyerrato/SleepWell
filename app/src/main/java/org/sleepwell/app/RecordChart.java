package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;
import org.json.JSONObject;
import java.util.*;

/** Each bar is an actual saved epoch; missing minutes remain blank. */
class RecordChart extends View {
    private final List<JSONObject> epochs;private final String metric;private final long start,end;
    private final Paint p=new Paint(3);private int selected=-1;private final double max,min;
    RecordChart(Context c,List<JSONObject> epochs,String metric,long start,long end){super(c);this.epochs=epochs;this.metric=metric;this.start=start;this.end=Math.max(start+1,end);double high=0,low=Double.MAX_VALUE;int n=0;for(JSONObject e:epochs)if(!e.isNull(metric)){double value=e.optDouble(metric);high=Math.max(high,value);low=Math.min(low,value);n++;}max=Math.max(.001,high);min=n==0?0:low;setFocusable(true);setContentDescription(metric+" chart. "+n+" actual readings. Missing intervals are blank.");}
    static String clock(long time){return java.time.Instant.ofEpochMilli(time).atZone(java.time.ZoneId.systemDefault()).format(java.time.format.DateTimeFormatter.ofPattern("h:mm a",Locale.getDefault()));}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float d=getResources().getDisplayMetrics().density,w=getWidth(),h=getHeight(),left=28*d,right=w-8*d,top=26*d,bottom=h-28*d;
        p.setTypeface(Design.font(getContext(),400));p.setTextSize(10*Appearance.scale(getContext())*getResources().getDisplayMetrics().scaledDensity);p.setColor(Appearance.color(getContext(),Design.MUTED));p.setStyle(Paint.Style.FILL);p.setTextAlign(Paint.Align.LEFT);c.drawText("High",0,top+5*d,p);c.drawText("Low",0,bottom,p);
        p.setColor(Design.LINE);p.setStrokeWidth(d*.7f);c.drawLine(left,bottom,right,bottom,p);for(int n=0;n<=4;n++){float x=left+(right-left)*n/4;c.drawLine(x,top,x,bottom,p);}
        double minuteWidth=(right-left)*60000.0/(end-start);float bar=Math.max(d,Math.min(14*d,(float)minuteWidth*.8f));int readings=0;
        for(int n=0;n<epochs.size();n++){JSONObject epoch=epochs.get(n);if(epoch.isNull(metric))continue;readings++;long time=epoch.optLong("time");float x=(float)(left+(right-left)*(time-start)/(double)(end-start));x=Math.min(right-bar/2,Math.max(left+bar/2,x));float y=(float)(bottom-(bottom-top)*epoch.optDouble(metric)/max);
            y=Math.min(y,bottom-2*d); // A true zero remains distinguishable from missing data.
            int color=n==selected?0xffaa7352:Design.VIOLET;p.setShader(new LinearGradient(0,y,0,bottom,color,n==selected?0xffd3ad94:0xffd6c2fa,Shader.TileMode.CLAMP));c.drawRoundRect(x-bar/2,y,x+bar/2,bottom,3*d,3*d,p);p.setShader(null);
            if(n==selected){p.setColor(Design.BROWN);c.drawCircle(x,y,3*d,p);p.setTextAlign(Paint.Align.CENTER);String value=clock(time);float tx=Math.max(40*d,Math.min(w-40*d,x));float half=p.measureText(value)/2+10*d;c.drawRoundRect(tx-half,0,tx+half,22*d,11*d,11*d,p);p.setColor(Design.CREAM);c.drawText(value,tx,15*d,p);}
        }
        if(readings==0){p.setColor(Appearance.color(getContext(),Design.MUTED));p.setTextAlign(Paint.Align.CENTER);c.drawText("No readings for this signal",w/2,(top+bottom)/2,p);}
        p.setColor(Appearance.color(getContext(),Design.MUTED));p.setTextAlign(Paint.Align.LEFT);c.drawText(clock(start),left,h-7*d,p);p.setTextAlign(Paint.Align.RIGHT);c.drawText(clock(end),right,h-7*d,p);
        if(end-start>=30*60000L){p.setTextAlign(Paint.Align.CENTER);for(int n=1;n<4;n++)c.drawText(clock(start+(end-start)*n/4),left+(right-left)*n/4,h-7*d,p);}
    }
    @Override public boolean onTouchEvent(android.view.MotionEvent e){if(e.getAction()==MotionEvent.ACTION_UP){float d=getResources().getDisplayMetrics().density;float ratio=Math.max(0,Math.min(1,(e.getX()-28*d)/(getWidth()-36*d)));long time=start+(long)((end-start)*ratio);long distance=Long.MAX_VALUE;for(int n=0;n<epochs.size();n++){JSONObject row=epochs.get(n);if(!row.isNull(metric)&&Math.abs(row.optLong("time")-time)<distance){distance=Math.abs(row.optLong("time")-time);selected=n;}}performClick();invalidate();return true;}return true;}
    @Override public boolean performClick(){super.performClick();if(selected>=0){JSONObject e=epochs.get(selected);setContentDescription(metric+" at "+clock(e.optLong("time"))+": "+e.optDouble(metric));}return true;}
    @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(info);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);}
    @Override public boolean performAccessibilityAction(int action,android.os.Bundle args){int step=action==AccessibilityNodeInfo.ACTION_SCROLL_FORWARD?1:action==AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD?-1:0;if(step==0)return super.performAccessibilityAction(action,args);int n=selected;while((n+=step)>=0&&n<epochs.size())if(!epochs.get(n).isNull(metric)){selected=n;performClick();invalidate();return true;}return false;}
}
