package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Small appearance sample of the saved plan; no miniature labels competing with the clock face. */
final class NightDialPlanMark extends View {
    final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);final int bedtime,wake;
    NightDialPlanMark(Context c,int bedtime,int wake){super(c);this.bedtime=bedtime;this.wake=wake;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    @Override protected void onDraw(Canvas canvas){float d=getResources().getDisplayMetrics().density,x=getWidth()/2f,y=getHeight()/2f,r=Math.min(getWidth(),getHeight())*.40f;paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(3*d);paint.setColor(Design.LINE);canvas.drawCircle(x,y,r,paint);paint.setStrokeCap(Paint.Cap.ROUND);float start=bedtime/4f-90,sweep=Math.floorMod(wake-bedtime,1440)/4f;paint.setColor(Design.BROWN);canvas.drawArc(x-r,y-r,x+r,y+r,start,sweep,false,paint);paint.setStrokeWidth(d);for(int n=0;n<24;n++){double angle=Math.PI*n/12;paint.setColor(n%6==0?Design.MUTED:Design.LINE);canvas.drawLine(x+(float)Math.cos(angle)*r*.8f,y+(float)Math.sin(angle)*r*.8f,x+(float)Math.cos(angle)*r*.88f,y+(float)Math.sin(angle)*r*.88f,paint);}paint.setStyle(Paint.Style.FILL);for(float angle:new float[]{start,start+sweep}){double rad=Math.toRadians(angle);paint.setColor(Design.BROWN);canvas.drawCircle(x+(float)Math.cos(rad)*r,y+(float)Math.sin(rad)*r,4*d,paint);}paint.setColor(Design.INK);canvas.drawCircle(x,y,3*d,paint);}
}
