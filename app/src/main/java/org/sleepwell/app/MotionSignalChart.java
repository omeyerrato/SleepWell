package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.util.*;

/** Actual half-second RMS bins. Blank spans represent missing samples. */
@android.annotation.SuppressLint("ViewConstructor")
final class MotionSignalChart extends View {
    private final Paint p=new Paint(3);private List<Double> data=List.of();
    MotionSignalChart(Context c){super(c);setContentDescription("Motion preview: no samples yet");}
    void data(List<Double> bins){data=new ArrayList<>(bins);int samples=0;for(Double value:bins)if(value!=null)samples++;setContentDescription("Motion preview: "+samples+" measured half-second bins in the last 15 seconds");invalidate();}
    private float d(float value){return value*getResources().getDisplayMetrics().density;}
    @Override protected void onDraw(Canvas canvas){float left=d(26),right=getWidth()-d(6),top=d(8),bottom=getHeight()-d(26);p.setTypeface(Design.font(getContext(),400));p.setTextSize(Design.sp(getContext(),10*Appearance.scale(getContext())));p.setColor(Design.MUTED);p.setTextAlign(Paint.Align.RIGHT);canvas.drawText("High",left-d(4),top+d(5),p);canvas.drawText("Low",left-d(4),bottom,p);p.setStrokeWidth(d(.7f));p.setColor(Design.LINE);for(int n=0;n<6;n++){float x=left+(right-left)*n/5;canvas.drawLine(x,top,x,bottom,p);}canvas.drawLine(left,bottom,right,bottom,p);double peak=.24;for(Double value:data)if(value!=null)peak=Math.max(peak,value);float width=(right-left)/30;for(int n=0;n<data.size();n++){Double value=data.get(n);if(value==null)continue;float x=left+n*width,bar=(float)((bottom-top)*Math.min(1,value/peak));p.setColor(Design.VIOLET);canvas.drawRoundRect(x+d(.6f),bottom-Math.max(d(1),bar),x+width-d(.6f),bottom,d(2),d(2),p);}p.setColor(Design.MUTED);p.setTextAlign(Paint.Align.CENTER);canvas.drawText("15 s ago",left+d(18),bottom+d(17),p);canvas.drawText("Now",right-d(10),bottom+d(17),p);}
}
