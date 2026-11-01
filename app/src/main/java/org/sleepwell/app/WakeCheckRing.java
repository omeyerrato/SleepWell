package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Pure shared graphic. It has no check state, clock, scheduler or alarm authority. */
final class WakeCheckRing extends View {private final Paint paint=new Paint(3);private final RectF bounds=new RectF();private float progress=1;WakeCheckRing(Context c){super(c);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);paint.setStyle(Paint.Style.STROKE);paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeWidth(12*getResources().getDisplayMetrics().density);}void progress(float value){if(progress!=value){progress=value;invalidate();}}@Override protected void onSizeChanged(int w,int h,int ow,int oh){float inset=paint.getStrokeWidth()/2+2;bounds.set(inset,inset,w-inset,h-inset);}@Override protected void onDraw(Canvas canvas){paint.setColor(NightDialTokens.RULE);canvas.drawOval(bounds,paint);paint.setColor(Design.BROWN);canvas.drawArc(bounds,-90,360*progress,false,paint);}}
