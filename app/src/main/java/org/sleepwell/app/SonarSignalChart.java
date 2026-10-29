package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.util.*;

/** Display averages of contiguous real echo intervals; invalid evidence stays blank. */
@android.annotation.SuppressLint("ViewConstructor") final class SonarSignalChart extends View {
    private List<SonarFrame> frames=List.of();
    private final Paint paint=new Paint(3);
    SonarSignalChart(Context context){super(context);data(List.of());}
    void data(List<SonarFrame> values){
        frames=List.copyOf(values);int count=0;
        for(SonarFrame frame:frames)if(frame.movement!=null)count++;
        setContentDescription("Contactless signal: "+count+" measured echo-change intervals. Bars average contiguous readings; missing signal stays blank.");
        invalidate();
    }
    private float dp(float value){return Design.dp(getContext(),value);}
    @Override protected void onDraw(Canvas canvas){
        float left=dp(30),right=getWidth()-dp(6),top=dp(12),bottom=getHeight()-dp(25);
        paint.setTypeface(Design.font(getContext(),400));paint.setTextSize(Design.sp(getContext(),10*Appearance.scale(getContext())));
        paint.setColor(Design.LILAC);paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("High",left-dp(5),top+dp(5),paint);canvas.drawText("Low",left-dp(5),bottom,paint);
        paint.setColor(0xff51445e);paint.setStrokeWidth(dp(.7f));
        for(int n=0;n<=3;n++){float x=left+(right-left)*n/3;canvas.drawLine(x,top,x,bottom,paint);}
        canvas.drawLine(left,bottom,right,bottom,paint);
        paint.setTextAlign(Paint.Align.RIGHT);paint.setColor(Design.LILAC);canvas.drawText("Scale 0–2 relative",right,dp(10),paint);
        long end=frames.isEmpty()?15000:frames.get(frames.size()-1).to,start=end-15000;
        List<ChartSeries.Sample> samples=new ArrayList<>();
        for(SonarFrame frame:frames)samples.add(new ChartSeries.Sample(frame.from,frame.to,frame.movement));
        List<ChartSeries.Bar> bars=ChartSeries.aggregate(samples,start,end,30);
        for(ChartSeries.Bar bar:bars){
            float x=left+(right-left)*(bar.from-start)/15000f,x2=left+(right-left)*(bar.to-start)/15000f;
            // A short real interval still needs a visible marker at its actual time.
            float width=Math.max(dp(2),Math.min(dp(12),x2-x-dp(2))),center=(x+x2)/2;
            float height=(float)((bottom-top)*Math.min(1,bar.value/2));
            float y=bottom-Math.max(dp(1),height);paint.setShader(new LinearGradient(0,y,0,bottom,Design.VIOLET,0xffc9a8f6,Shader.TileMode.CLAMP));
            canvas.drawRoundRect(center-width/2,y,center+width/2,bottom,dp(3),dp(3),paint);paint.setShader(null);
        }
        paint.setColor(Design.LILAC);paint.setTextAlign(Paint.Align.LEFT);canvas.drawText("15 s ago",left,bottom+dp(18),paint);
        paint.setTextAlign(Paint.Align.RIGHT);canvas.drawText("Now",right,bottom+dp(18),paint);
        if(right-left>dp(220)){paint.setTextAlign(Paint.Align.CENTER);canvas.drawText("10 s ago",left+(right-left)/3,bottom+dp(18),paint);canvas.drawText("5 s ago",left+2*(right-left)/3,bottom+dp(18),paint);}
        if(bars.isEmpty()){paint.setTextAlign(Paint.Align.CENTER);canvas.drawText("No signal yet",(left+right)/2,(top+bottom)/2,paint);}
    }
}
