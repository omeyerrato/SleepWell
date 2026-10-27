package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Planned output curve from the exact policy used by AlarmPlayback, never sensor data. */
@android.annotation.SuppressLint("ViewConstructor") // Constructed with an immutable alarm policy; never inflated from XML.
final class AlarmRampChart extends View {
    private final AlarmWakePolicy policy;private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private final Path curve=new Path(),fill=new Path();private Shader shade;
    AlarmRampChart(Context context,Alarm alarm){super(context);policy=new AlarmWakePolicy(alarm);setContentDescription("Planned volume curve: "+policy.targetPercent+" percent after "+alarm.rampSeconds+" seconds of gradual increase. Sound starts after "+alarm.soundDelaySeconds+" seconds. Device volume steps may round the final level.");setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);}
    @Override protected void onSizeChanged(int width,int height,int oldWidth,int oldHeight){super.onSizeChanged(width,height,oldWidth,oldHeight);float density=getResources().getDisplayMetrics().density;shade=new LinearGradient(0,20*density,0,Math.max(21*density,height-50*density),0x44ab784e,0x0aab784e,Shader.TileMode.CLAMP);}
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);float density=getResources().getDisplayMetrics().density,scale=Design.fontScale(getContext());paint.setTypeface(Design.font(getContext(),400));paint.setTextSize(11*density*scale);
        Paint.FontMetrics metrics=paint.getFontMetrics();long duration=Math.max(1,policy.rampMillis),finalSeconds=duration/1000;String longest=finalSeconds/60+":"+String.format(java.util.Locale.ROOT,"%02d",finalSeconds%60);float left=Math.max(38*density,paint.measureText("100%")+8*density),right=getWidth()-Math.max(14*density,paint.measureText(longest)/2+2*density),top=Math.max(20*density,-metrics.ascent+10*density),bottom=getHeight()-50*density*scale;
        if(right<=left||bottom<=top)return;float middle=-((metrics.ascent+metrics.descent)/2);
        paint.setStrokeWidth(density);paint.setStyle(Paint.Style.STROKE);paint.setColor(Design.LINE);
        for(int n=0;n<=5;n++){float y=bottom-(bottom-top)*n/5;canvas.drawLine(left,y,right,y,paint);}if(policy.rampMillis>0)for(int n=0;n<=4;n++){float x=left+(right-left)*n/4;canvas.drawLine(x,top,x,bottom,paint);}
        paint.setColor(Design.MUTED);canvas.drawLine(left,top,left,bottom,paint);canvas.drawLine(left,bottom,right,bottom,paint);
        paint.setStyle(Paint.Style.FILL);paint.setTextAlign(Paint.Align.RIGHT);for(int n=0;n<=5;n++)canvas.drawText(n*20+"%",left-7*density,bottom-(bottom-top)*n/5+middle,paint);
        paint.setTextAlign(Paint.Align.CENTER);if(policy.rampMillis==0){String caption="Immediate \u00b7 no volume ramp";if(paint.measureText(caption)<=getWidth()){canvas.drawText(caption,getWidth()/2f,bottom+24*density*scale,paint);}else{canvas.drawText("Immediate",getWidth()/2f,bottom+20*density*scale,paint);canvas.drawText("No volume ramp",getWidth()/2f,bottom+40*density*scale,paint);}}else{
            int ticks=(right-left)/4>=paint.measureText(longest)+8*density?4:(right-left)/2>=paint.measureText(longest)+8*density?2:1;
            for(int n=0;n<=ticks;n++){long seconds=duration*n/(ticks*1000);canvas.drawText(seconds/60+":"+String.format(java.util.Locale.ROOT,"%02d",seconds%60),left+(right-left)*n/ticks,bottom+20*density*scale,paint);}canvas.drawText("Time (minutes)",(left+right)/2,bottom+40*density*scale,paint);
        }
        curve.reset();float firstY=bottom,endY=bottom;for(int n=0;n<=100;n++){float x=left+(right-left)*n/100;long at=policy.delayMillis+duration*n/100;float y=bottom-(bottom-top)*(float)(policy.gain(at,false)*policy.targetPercent/100);if(n==0){curve.moveTo(x,y);firstY=y;}else curve.lineTo(x,y);endY=y;}
        fill.set(curve);fill.lineTo(right,bottom);fill.lineTo(left,bottom);fill.close();paint.setShader(shade);canvas.drawPath(fill,paint);paint.setShader(null);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2.5f*density);paint.setColor(Design.BROWN);canvas.drawPath(curve,paint);paint.setStyle(Paint.Style.FILL);canvas.drawCircle(left,firstY,3.5f*density,paint);canvas.drawCircle(right,endY,4*density,paint);paint.setTextAlign(Paint.Align.RIGHT);paint.setTypeface(Design.font(getContext(),600));paint.setColor(Design.INK);canvas.drawText(policy.targetPercent+"%",right,endY-9*density,paint);
    }
}
