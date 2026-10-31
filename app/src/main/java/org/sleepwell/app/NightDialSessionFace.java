package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.time.*;
import java.util.Locale;

/** Quiet wall-clock plan for an owned recording. The arc is never labelled as measured sleep. */
final class NightDialSessionFace extends View {
    private static final int INK=0xffc9c6bc,META=0xffaaa79f,AMBER=0xffc5a367;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private final long start,end;private long now;private final ZoneId zone;
    NightDialSessionFace(Context context,long start,long end,long now,ZoneId zone){super(context);this.start=start;this.end=end;this.now=now;this.zone=zone;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    void now(long value){now=value;invalidate();}
    @Override protected void onMeasure(int widthSpec,int heightSpec){int width=resolveSize(Design.dp(getContext(),340),widthSpec);setMeasuredDimension(width,resolveSize(Math.round(width*.84f),heightSpec));}
    private float angle(long at){ZonedDateTime value=Instant.ofEpochMilli(at).atZone(zone);return(value.getHour()*60+value.getMinute())/4f-90;}
    @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);if(getWidth()==0)return;float s=getWidth()/360f;canvas.save();canvas.scale(s,s);RectF ring=new RectF(50,21,310,281);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(5);paint.setStrokeCap(Paint.Cap.ROUND);paint.setColor(NightDialTokens.RULE);canvas.drawOval(ring,paint);if(end>start){float sweep=(angle(end)-angle(start)+360)%360;if(sweep==0)sweep=360;paint.setColor(0xff66645f);paint.setStrokeWidth(4);canvas.drawArc(ring,angle(start),sweep,false,paint);if(now>start){float past=(float)Math.min(1,(double)(now-start)/(end-start));paint.setColor(AMBER);canvas.drawArc(ring,angle(start),sweep*past,false,paint);}}paint.setStrokeCap(Paint.Cap.BUTT);for(int n=0;n<48;n++){double a=Math.toRadians(n*7.5-90);float from=n%2==0?113:119;paint.setColor(n%2==0?0xff66645f:NightDialTokens.RULE);paint.setStrokeWidth(1);canvas.drawLine(180+from*(float)Math.cos(a),151+from*(float)Math.sin(a),180+123*(float)Math.cos(a),151+123*(float)Math.sin(a),paint);}paint.setStyle(Paint.Style.FILL);paint.setColor(META);paint.setTypeface(Design.font(getContext(),400));paint.setTextAlign(Paint.Align.CENTER);float type=Design.sp(getContext(),10*Appearance.scale(getContext()))/s;paint.setTextSize(type);for(int hour=0;hour<24;hour+=2){double a=Math.toRadians(hour*15-90);canvas.drawText(String.format(Locale.ROOT,"%02d",hour),180+99*(float)Math.cos(a),151+99*(float)Math.sin(a)-(paint.ascent()+paint.descent())/2,paint);}point(canvas,start,AMBER,6);if(end>start)point(canvas,end,0xff73736e,6);if(now>=start){point(canvas,now,INK,3);}canvas.restore();}
    private void point(Canvas canvas,long at,int color,float radius){double a=Math.toRadians(angle(at));paint.setStyle(Paint.Style.FILL);paint.setColor(NightDialTokens.GROUND);canvas.drawCircle(180+130*(float)Math.cos(a),151+130*(float)Math.sin(a),radius+2,paint);paint.setColor(color);canvas.drawCircle(180+130*(float)Math.cos(a),151+130*(float)Math.sin(a),radius,paint);}
}
