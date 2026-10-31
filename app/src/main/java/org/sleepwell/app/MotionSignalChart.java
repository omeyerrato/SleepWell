package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.util.*;

/** Actual half-second RMS bins. Blank spans represent missing samples, never invented zeroes. */
@android.annotation.SuppressLint("ViewConstructor")
final class MotionSignalChart extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<Double> data=List.of();
    private double scale=.24;
    MotionSignalChart(Context context){super(context);setContentDescription("Motion preview: no samples yet");}
    void data(List<Double> bins){
        data=new ArrayList<>(bins);double maximum=0;int measured=0;
        for(Double value:data)if(measured(value)){maximum=Math.max(maximum,value);measured++;}
        // Keep the established minimum range; larger observed values receive a little headroom.
        scale=.24;
        if(maximum>=scale){double step=Math.pow(10,Math.floor(Math.log10(maximum))-1);scale=Math.ceil(maximum*1.05/step)*step;}
        setContentDescription("Motion preview: "+measured+" measured half-second bins in the last 15 seconds. "+rangeLabel()+" relative units. Missing readings stay blank.");invalidate();
    }
    String rangeLabel(){return "Scale 0–"+String.format(Locale.getDefault(),scale<10?"%.2f":scale<100?"%.1f":"%.2g",scale);}
    private static boolean measured(Double value){return value!=null&&Double.isFinite(value)&&value>=0;}
    private float dp(float value){return value*getResources().getDisplayMetrics().density;}
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);paint.setTypeface(Design.font(getContext(),400));paint.setTextSize(Design.sp(getContext(),11*Appearance.scale(getContext())));
        Paint.FontMetrics metrics=paint.getFontMetrics();
        float left=Math.max(dp(27),paint.measureText("High")+dp(5)),right=getWidth()-dp(5);
        float top=Math.max(dp(7),-metrics.ascent/2),bottom=getHeight()-(metrics.descent-metrics.ascent)-dp(9);
        if(right<=left||bottom<=top)return;
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(.7f));paint.setColor(Design.LINE);paint.setPathEffect(new DashPathEffect(new float[]{dp(3),dp(3)},0));
        for(int n=0;n<=5;n++){float x=left+(right-left)*n/5;canvas.drawLine(x,top,x,bottom,paint);}
        canvas.drawLine(left,top,right,top,paint);paint.setPathEffect(null);canvas.drawLine(left,bottom,right,bottom,paint);
        paint.setStyle(Paint.Style.FILL);paint.setColor(Design.MUTED);paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("High",left-dp(5),top-metrics.ascent/2,paint);canvas.drawText("Low",left-dp(5),bottom,paint);
        float width=(right-left)/30,gap=Math.min(dp(.8f),width/5);paint.setColor(Appearance.contrast(getContext())?NightDialTokens.ACCENT:Design.VIOLET);
        for(int n=0;n<Math.min(30,data.size());n++){
            Double value=data.get(n);if(!measured(value)||value==0)continue;
            float x=left+n*width,height=(float)((bottom-top)*Math.min(1,value/scale));
            canvas.drawRoundRect(x+gap,bottom-height,x+width-gap,bottom,dp(2),dp(2),paint);
        }
        paint.setColor(Design.MUTED);paint.setTextAlign(Paint.Align.CENTER);
        float labelWidth=paint.measureText("12 s ago")+dp(8);int divisions=(right-left)/5>=labelWidth?5:(right-left)/3>=labelWidth?3:1;
        float baseline=getHeight()-metrics.descent-dp(1);
        for(int n=0;n<=divisions;n++){
            int seconds=15-15*n/divisions;String label=seconds==0?"Now":seconds+" s ago";
            float half=paint.measureText(label)/2,x=left+(right-left)*n/divisions;
            canvas.drawText(label,Math.max(half,Math.min(getWidth()-half,x)),baseline,paint);
        }
    }
}
