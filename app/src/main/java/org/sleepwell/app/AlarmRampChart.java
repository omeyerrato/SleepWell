package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Planned output curve from the exact policy used by AlarmPlayback, never sensor data. */
@android.annotation.SuppressLint("ViewConstructor") // Constructed with an immutable alarm policy; never inflated from XML.
final class AlarmRampChart extends View {
    private final AlarmWakePolicy policy;private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private final Path curve=new Path(),fill=new Path();private Shader shade;
    AlarmRampChart(Context context,Alarm alarm){super(context);policy=new AlarmWakePolicy(alarm);setContentDescription("Planned volume curve: "+policy.targetPercent+" percent after "+alarm.rampSeconds+" seconds of gradual increase. Sound starts after "+alarm.soundDelaySeconds+" seconds. Device volume steps may round the final level.");setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);}
    @Override protected void onSizeChanged(int width,int height,int oldWidth,int oldHeight){super.onSizeChanged(width,height,oldWidth,oldHeight);float density=getResources().getDisplayMetrics().density;shade=new LinearGradient(0,20*density,0,Math.max(21*density,height-48*density),0x44ab784e,0x0aab784e,Shader.TileMode.CLAMP);}
    @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);float density=getResources().getDisplayMetrics().density,scale=Design.fontScale(getContext()),left=38*density,right=getWidth()-18*density,top=20*density,bottom=getHeight()-48*density;
        paint.setTypeface(Design.font(getContext(),400));paint.setTextSize(10*density*scale);paint.setStrokeWidth(density);paint.setStyle(Paint.Style.STROKE);paint.setColor(Design.LINE);
        for(int n=0;n<=5;n++){float y=bottom-(bottom-top)*n/5;canvas.drawLine(left,y,right,y,paint);}if(policy.rampMillis>0)for(int n=0;n<=4;n++){float x=left+(right-left)*n/4;canvas.drawLine(x,top,x,bottom,paint);}
        paint.setStyle(Paint.Style.FILL);paint.setColor(Design.MUTED);paint.setTextAlign(Paint.Align.RIGHT);for(int n=0;n<=5;n++)canvas.drawText(n*20+"%",left-7*density,bottom-(bottom-top)*n/5+3*density,paint);
        long duration=Math.max(1,policy.rampMillis);paint.setTextAlign(Paint.Align.CENTER);if(policy.rampMillis==0){canvas.drawText("Immediate · no volume ramp",(left+right)/2,bottom+24*density,paint);}else{for(int n=0;n<=4;n++){long seconds=duration*n/4000;canvas.drawText(seconds/60+":"+String.format(java.util.Locale.ROOT,"%02d",seconds%60),left+(right-left)*n/4,bottom+20*density,paint);}canvas.drawText("Planned time after sound delay",(left+right)/2,bottom+40*density,paint);}
        curve.reset();float endY=bottom;for(int n=0;n<=100;n++){float x=left+(right-left)*n/100;long at=policy.delayMillis+duration*n/100;float y=bottom-(bottom-top)*(float)(policy.gain(at,false)*policy.targetPercent/100);if(n==0)curve.moveTo(x,y);else curve.lineTo(x,y);endY=y;}
        fill.set(curve);fill.lineTo(right,bottom);fill.lineTo(left,bottom);fill.close();paint.setShader(shade);canvas.drawPath(fill,paint);paint.setShader(null);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2.5f*density);paint.setColor(Design.BROWN);canvas.drawPath(curve,paint);paint.setStyle(Paint.Style.FILL);canvas.drawCircle(right,endY,4*density,paint);paint.setTextAlign(Paint.Align.RIGHT);paint.setTypeface(Design.font(getContext(),600));paint.setColor(Design.INK);canvas.drawText(policy.targetPercent+"%",right,endY-10*density,paint);
    }
}
