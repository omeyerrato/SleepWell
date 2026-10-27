package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Dated bedtime markers only. Capsules are marker glyphs, never invented confidence intervals. */
@android.annotation.SuppressLint("ViewConstructor")
final class RhythmChart extends View {
    private final List<RhythmStats.Night> nights;private final Paint paint=new Paint(3);private final Paint.FontMetrics metrics=new Paint.FontMetrics();private final float[] centers=new float[3];private final double goal,low,high;private final double[] values;private final String[] days;private final String[] axis=new String[3];private final DashPathEffect goalDash;private final String goalLabel;private int selected=-1;
    RhythmChart(Context context,List<RhythmStats.Night> nights,int bedtime){super(context);this.nights=nights;List<Double> times=new ArrayList<>();for(RhythmStats.Night n:nights)times.add(n.bedtime);times.add((double)bedtime);double[] span=RhythmStats.arc(times);low=Math.floor((span[0]-15)/60)*60;high=Math.max(low+120,Math.ceil((span[0]+span[1]+15)/60)*60);goal=project(bedtime,span[0]);goalDash=new DashPathEffect(new float[]{dp(4),dp(3)},0);goalLabel="Goal "+label(context,goal);values=new double[nights.size()];days=new String[nights.size()];StringBuilder description=new StringBuilder("Recorded bedtime chart. ");for(int n=0;n<nights.size();n++){RhythmStats.Night night=nights.get(n);values[n]=project(night.bedtime,span[0]);days[n]=night.day.format(DateTimeFormatter.ofPattern("EEE d",Locale.getDefault()));description.append(night.day).append(": ").append(label(context,night.bedtime)).append(", ").append(night.record.provenance()).append(". ");}for(int n=0;n<3;n++)axis[n]=label(context,low+(high-low)*n/2);setContentDescription(description);setFocusable(true);}
    private double project(double value,double start){return start+((value-start+1440)%1440);}
    static String label(Context c,double minutes){return Design.clock(c,RhythmStats.displayMinute(minutes));}
    private float dp(float value){return value*getResources().getDisplayMetrics().density;}
    private void textPaint(){paint.setTypeface(Design.font(getContext(),400));paint.setTextSize(Design.sp(getContext(),11*Appearance.scale(getContext())));paint.getFontMetrics(metrics);paint.setStyle(Paint.Style.FILL);paint.setPathEffect(null);}
    private float textHeight(){return metrics.descent-metrics.ascent;}
    private float left(){float width=dp(30);for(String day:days)width=Math.max(width,paint.measureText(day));return width+dp(8);}
    private float right(){return getWidth()-dp(8);}
    private boolean stackedLegend(float width){return paint.measureText(goalLabel)+paint.measureText("Your bedtime")+dp(62)>width-left()-dp(8);}
    private float top(){return dp(11)+(textHeight()+dp(12))*(stackedLegend(getWidth())?2:1);}
    private float bottom(){return getHeight()-textHeight()-dp(17);}
    private float x(double value){return (float)(left()+(right()-left())*(value-low)/(high-low));}
    @Override protected void onMeasure(int widthSpec,int heightSpec){textPaint();int width=MeasureSpec.getSize(widthSpec);float scale=Math.max(1,Design.fontScale(getContext()));int height=Math.round(dp(220*scale)+(stackedLegend(width)?textHeight()+dp(12):0));setMeasuredDimension(resolveSize(width,widthSpec),resolveSize(height,heightSpec));}
    @Override protected void onDraw(Canvas canvas){
        textPaint();float left=left(),right=right(),top=top(),bottom=bottom(),height=(bottom-top)/Math.max(1,nights.size());
        float legendBaseline=dp(5)-metrics.ascent,legendCenter=legendBaseline+(metrics.ascent+metrics.descent)/2;
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(2));paint.setPathEffect(goalDash);paint.setColor(Design.VIOLET);canvas.drawLine(left+dp(2),legendCenter-dp(6),left+dp(2),legendCenter+dp(6),paint);paint.setPathEffect(null);paint.setStyle(Paint.Style.FILL);paint.setColor(Design.MUTED);paint.setTextAlign(Paint.Align.LEFT);canvas.drawText(goalLabel,left+dp(10),legendBaseline,paint);
        float bedtimeX=right-paint.measureText("Your bedtime"),bedtimeBaseline=legendBaseline;if(stackedLegend(getWidth())){bedtimeX=left+dp(38);bedtimeBaseline+=textHeight()+dp(12);}float bedtimeCenter=bedtimeBaseline+(metrics.ascent+metrics.descent)/2;paint.setColor(0xffdcccfa);canvas.drawRoundRect(bedtimeX-dp(37),bedtimeCenter-dp(5),bedtimeX-dp(7),bedtimeCenter+dp(5),dp(5),dp(5),paint);paint.setColor(Design.MUTED);canvas.drawText("Your bedtime",bedtimeX,bedtimeBaseline,paint);
        for(int n=0;n<nights.size();n++){float y=top+height*(n+.5f);paint.setColor(Design.SOFT);canvas.drawRect(left,y-dp(7),right,y+dp(7),paint);paint.setColor(Design.MUTED);paint.setTextAlign(Paint.Align.RIGHT);canvas.drawText(days[n],left-dp(8),y-(metrics.ascent+metrics.descent)/2,paint);}
        // Draw guides first so they do not cut through the actual bedtime markers.
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(1));paint.setColor(Design.LINE);for(int n=0;n<3;n++){float xx=x(low+(high-low)*n/2);canvas.drawLine(xx,top-dp(3),xx,bottom,paint);}paint.setColor(Design.VIOLET);paint.setPathEffect(goalDash);canvas.drawLine(x(goal),top-dp(3),x(goal),bottom,paint);paint.setPathEffect(null);paint.setStyle(Paint.Style.FILL);
        canvas.save();canvas.clipRect(left,top,right,bottom);for(int n=0;n<nights.size();n++){float y=top+height*(n+.5f),xx=x(values[n]);paint.setColor(0xffdcccfa);canvas.drawRoundRect(xx-dp(15),y-dp(5),xx+dp(15),y+dp(5),dp(5),dp(5),paint);paint.setColor(n==selected?Design.BROWN:0xff7540e8);canvas.drawCircle(xx,y,dp(4.5f),paint);}canvas.restore();
        paint.setColor(Design.MUTED);paint.setTextAlign(Paint.Align.CENTER);for(int n=0;n<3;n++){float half=paint.measureText(axis[n])/2;centers[n]=Math.max(dp(2)+half,Math.min(getWidth()-dp(2)-half,x(low+(high-low)*n/2)));}boolean middleFits=centers[1]-centers[0]>(paint.measureText(axis[0])+paint.measureText(axis[1]))/2+dp(6)&&centers[2]-centers[1]>(paint.measureText(axis[1])+paint.measureText(axis[2]))/2+dp(6);for(int n=0;n<3;n++)if(n!=1||middleFits)canvas.drawText(axis[n],centers[n],bottom+dp(9)-metrics.ascent,paint);
    }
    @Override public boolean onTouchEvent(MotionEvent event){if(event.getAction()==MotionEvent.ACTION_UP&&!nights.isEmpty()){textPaint();float top=top(),bottom=bottom();selected=Math.max(0,Math.min(nights.size()-1,(int)((event.getY()-top)/(bottom-top)*nights.size())));RhythmStats.Night night=nights.get(selected);setContentDescription(night.day+", "+label(getContext(),night.bedtime)+", "+night.record.provenance());announceForAccessibility(getContentDescription());performClick();invalidate();}return true;}
    @Override public boolean performClick(){super.performClick();return true;}
}
