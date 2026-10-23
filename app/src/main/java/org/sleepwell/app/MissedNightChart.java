package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.time.ZoneId;

/** The two real event endpoints and reviewed interval; no synthetic sensor values or sleep stages. */
@android.annotation.SuppressLint("ViewConstructor")
final class MissedNightChart extends View {
    private final long begin,end;private final MissedNight.Interval evidence;private final RecordDraft draft;private final ZoneId zone;private final Paint paint=new Paint(3);
    MissedNightChart(Context context,long begin,long end,MissedNight.Interval evidence,RecordDraft draft,ZoneId zone){super(context);this.begin=begin;this.end=end;this.evidence=evidence;this.draft=draft;this.zone=zone;setContentDescription("Phone inactive from "+RecordUi.clock(context,evidence.start,zone)+" to "+RecordUi.clock(context,evidence.end,zone)+". Reviewed period "+RecordUi.clock(context,draft.start,zone)+" to "+RecordUi.clock(context,draft.end,zone)+". No sensor measurements.");}
    private float x(long time){return getPaddingLeft()+10*d()+(getWidth()-20*d())*(time-begin)/(float)(end-begin);}
    private float d(){return getResources().getDisplayMetrics().density;}
    @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);float d=d();paint.setTypeface(Design.font(getContext(),400));paint.setTextSize(Design.sp(getContext(),11*Appearance.scale(getContext())));paint.setColor(Design.MUTED);canvas.drawText("Phone inactive",0,15*d,paint);canvas.drawText("Reviewed period",0,72*d,paint);band(canvas,evidence.start,evidence.end,35*d,Design.LILAC);band(canvas,draft.start,draft.end,93*d,Design.BROWN);paint.setColor(Design.MUTED);for(int n=0;n<3;n++){long time=begin+(end-begin)*n/2;String text=RecordUi.clock(getContext(),time,zone);float width=paint.measureText(text),left=Math.max(0,Math.min(getWidth()-width,x(time)-width/2));canvas.drawText(text,left,126*d,paint);}}
    private void band(Canvas canvas,long start,long finish,float y,int color){float d=d();paint.setColor(Design.SOFT);canvas.drawRoundRect(10*d,y-7*d,getWidth()-10*d,y+7*d,4*d,4*d,paint);paint.setColor(color);canvas.drawRoundRect(x(start),y-7*d,x(finish),y+7*d,4*d,4*d,paint);for(long at:new long[]{start,finish})canvas.drawCircle(x(at),y,4*d,paint);}
}
