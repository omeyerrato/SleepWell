package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.util.*;

/** Every plotted point is one saved midpoint; the full 24-hour axis keeps midnight explicit. */
final class RhythmGroupsChart extends View {
    static final int[] COLORS={NightDialTokens.ACCENT,NightDialTokens.SECONDARY,NightDialTokens.META};
    private final RhythmGroups data;private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    RhythmGroupsChart(Context c,RhythmGroups data){super(c);this.data=data;setContentDescription(data.nights.size()+" recorded midpoints in "+data.groups.size()+" timing groups; horizontal clock axis midnight to midnight");}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float d=getResources().getDisplayMetrics().density,left=26*d,right=getWidth()-20*d,top=28*d,bottom=getHeight()-30*d;p.setTypeface(Design.font(getContext(),400));p.setTextSize(11*d);for(int hour=0;hour<=24;hour+=6){float x=left+(right-left)*hour/24f;p.setColor(Design.LINE);p.setStrokeWidth(d);c.drawLine(x,top,x,bottom,p);p.setColor(Design.MUTED);p.setTextAlign(hour==0?Paint.Align.LEFT:hour==24?Paint.Align.RIGHT:Paint.Align.CENTER);c.drawText(hour==0||hour==24?"12 AM":hour==12?"12 PM":hour<12?hour+" AM":(hour-12)+" PM",x,bottom+20*d,p);}
        for(int index=0;index<data.groups.size();index++){RhythmGroups.Group g=data.groups.get(index);float y=top+(bottom-top)*(index+.5f)/data.groups.size();p.setColor(Design.SOFT);c.drawRoundRect(left,y-17*d,right,y+17*d,12*d,12*d,p);p.setColor(COLORS[index%COLORS.length]);float center=left+(right-left)*(float)g.midpoint/1440f;p.setStrokeWidth(2*d);c.drawLine(center,y-22*d,center,y+22*d,p);for(int n=0;n<g.nights.size();n++){float x=left+(right-left)*(float)g.nights.get(n).midpoint/1440f;float jitter=((n%3)-1)*7*d;p.setAlpha(165);c.drawCircle(x,y+jitter,4*d,p);}p.setAlpha(255);}
    }
}
