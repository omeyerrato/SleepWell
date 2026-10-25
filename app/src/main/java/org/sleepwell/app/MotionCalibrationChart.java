package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.util.*;

/** Only measured half-second RMS bins from the indicated capture. Missing bins remain blank. */
final class MotionCalibrationChart extends View {
    private final Paint paint=new Paint(3);private List<Double> bins=List.of();
    MotionCalibrationChart(Context context){super(context);setContentDescription("Calibration graph: no measurements");}
    void data(List<Double> values){bins=new ArrayList<>(values);int measured=0;for(Double value:bins)if(value!=null)measured++;setContentDescription("Calibration graph: "+measured+" measured half-second bins of "+bins.size());invalidate();}
    private float d(float value){return value*getResources().getDisplayMetrics().density;}
    @Override protected void onDraw(Canvas canvas){float left=d(38),right=getWidth()-d(8),top=d(12),bottom=getHeight()-d(26);double peak=.12;for(Double v:bins)if(v!=null)peak=Math.max(peak,v);paint.setTypeface(Design.font(getContext(),400));paint.setTextSize(Design.sp(getContext(),10*Appearance.scale(getContext())));paint.setColor(Design.LINE);paint.setStrokeWidth(d(.7f));for(int n=0;n<4;n++){float y=top+(bottom-top)*n/3;canvas.drawLine(left,y,right,y,paint);}paint.setColor(Design.MUTED);paint.setTextAlign(Paint.Align.RIGHT);canvas.drawText(String.format(Locale.ROOT,"%.2f",peak),left-d(5),top+d(4),paint);canvas.drawText("0",left-d(5),bottom,paint);if(!bins.isEmpty()){float width=(right-left)/bins.size();paint.setColor(Design.VIOLET);for(int n=0;n<bins.size();n++){Double value=bins.get(n);if(value==null)continue;float bar=(float)((bottom-top)*value/peak);canvas.drawRoundRect(left+n*width+d(.6f),bottom-Math.max(d(1),bar),left+(n+1)*width-d(.6f),bottom,d(2),d(2),paint);}}paint.setColor(Design.MUTED);paint.setTextAlign(Paint.Align.LEFT);canvas.drawText("0 s",left,bottom+d(17),paint);paint.setTextAlign(Paint.Align.RIGHT);canvas.drawText(bins.isEmpty()?"No capture":String.format(Locale.ROOT,"%.0f s",bins.size()*.5),right,bottom+d(17),paint);}
}
