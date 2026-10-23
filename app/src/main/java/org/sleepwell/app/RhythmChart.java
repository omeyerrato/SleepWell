package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Dated bedtime markers only. Capsules are marker glyphs, never invented confidence intervals. */
@android.annotation.SuppressLint("ViewConstructor")
final class RhythmChart extends View {
    private final List<RhythmStats.Night> nights;private final Paint paint=new Paint(3);private final double goal,low,high;private final double[] values;private final String[] days;private final String[] axis=new String[3];private final DashPathEffect goalDash;private final String goalLabel;private int selected=-1;
    RhythmChart(Context context,List<RhythmStats.Night> nights,int bedtime){super(context);this.nights=nights;List<Double> times=new ArrayList<>();for(RhythmStats.Night n:nights)times.add(n.bedtime);times.add((double)bedtime);double[] span=RhythmStats.arc(times);low=Math.floor((span[0]-15)/60)*60;high=Math.max(low+120,Math.ceil((span[0]+span[1]+15)/60)*60);goal=project(bedtime,span[0]);goalDash=new DashPathEffect(new float[]{dp(4),dp(3)},0);goalLabel="Goal "+label(context,goal);values=new double[nights.size()];days=new String[nights.size()];StringBuilder description=new StringBuilder("Recorded bedtime chart. ");for(int n=0;n<nights.size();n++){RhythmStats.Night night=nights.get(n);values[n]=project(night.bedtime,span[0]);days[n]=night.day.format(DateTimeFormatter.ofPattern("EEE d",Locale.getDefault()));description.append(night.day).append(": ").append(label(context,night.bedtime)).append(", ").append(night.record.provenance()).append(". ");}for(int n=0;n<3;n++)axis[n]=label(context,low+(high-low)*n/2);setContentDescription(description);setFocusable(true);}
    private double project(double value,double start){return start+((value-start+1440)%1440);}
    static String label(Context c,double minutes){return Design.clock(c,RhythmStats.displayMinute(minutes));}
    private float dp(float value){return value*getResources().getDisplayMetrics().density;}
    private float left(){return dp(48);}private float right(){return getWidth()-dp(8);}private float x(double value){return (float)(left()+(right()-left())*(value-low)/(high-low));}
    @Override protected void onDraw(Canvas canvas){float top=dp(37),bottom=getHeight()-dp(32),height=(bottom-top)/Math.max(1,nights.size());paint.setTypeface(Design.font(getContext(),400));paint.setTextSize(Design.sp(getContext(),10*Appearance.scale(getContext())));paint.setStyle(Paint.Style.FILL);paint.setTextAlign(Paint.Align.LEFT);paint.setColor(Design.MUTED);canvas.drawText(goalLabel,left()+dp(8),dp(17),paint);paint.setColor(Design.VIOLET);canvas.drawRect(left(),dp(7),left()+dp(3),dp(19),paint);paint.setColor(Design.MUTED);paint.setTextAlign(Paint.Align.RIGHT);canvas.drawText("Your bedtime",right(),dp(17),paint);
        for(int n=0;n<nights.size();n++){float y=top+height*(n+.5f);paint.setColor(Design.SOFT);canvas.drawRect(left(),y-dp(7),right(),y+dp(7),paint);paint.setColor(Design.MUTED);paint.setTextAlign(Paint.Align.RIGHT);canvas.drawText(days[n],left()-dp(7),y+dp(4),paint);float xx=x(values[n]);paint.setColor(0xffdcccfa);canvas.drawRoundRect(xx-dp(15),y-dp(5),xx+dp(15),y+dp(5),dp(5),dp(5),paint);paint.setColor(n==selected?Design.BROWN:0xff7540e8);canvas.drawCircle(xx,y,dp(4.5f),paint);}
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(1));paint.setColor(Design.LINE);for(int n=0;n<3;n++){float xx=x(low+(high-low)*n/2);canvas.drawLine(xx,top-dp(6),xx,bottom,paint);}paint.setColor(Design.VIOLET);paint.setPathEffect(goalDash);canvas.drawLine(x(goal),top-dp(7),x(goal),bottom,paint);paint.setPathEffect(null);paint.setStyle(Paint.Style.FILL);paint.setColor(Design.MUTED);paint.setTextAlign(Paint.Align.CENTER);for(int n=0;n<3;n++){float half=paint.measureText(axis[n])/2;float center=Math.max(dp(2)+half,Math.min(getWidth()-dp(2)-half,x(low+(high-low)*n/2)));canvas.drawText(axis[n],center,bottom+dp(19),paint);}
    }
    @Override public boolean onTouchEvent(MotionEvent event){if(event.getAction()==MotionEvent.ACTION_UP&&!nights.isEmpty()){float top=dp(37),bottom=getHeight()-dp(32);selected=Math.max(0,Math.min(nights.size()-1,(int)((event.getY()-top)/(bottom-top)*nights.size())));RhythmStats.Night night=nights.get(selected);setContentDescription(night.day+", "+label(getContext(),night.bedtime)+", "+night.record.provenance());announceForAccessibility(getContentDescription());performClick();invalidate();}return true;}
    @Override public boolean performClick(){super.performClick();return true;}
}
