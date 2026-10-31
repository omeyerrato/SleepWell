package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;

/** Complete source pairs on independent lanes. Cross-lane overlaps are retained, never flattened. */
final class SleepCsvEventChart extends View {
    private final SleepCsvEvents data;private final long from,to;private final ZoneId zone;private final Consumer<SleepCsvEvents.Span> inspect;private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private float downX,downY;
    SleepCsvEventChart(Context context,SleepCsvEvents data,long from,long to,ZoneId zone,Consumer<SleepCsvEvents.Span> inspect){super(context);this.data=data;this.from=from;this.to=to;this.zone=zone;this.inspect=inspect;setMinimumHeight(Design.dp(context,250));setContentDescription("Imported event annotations. Separate LIGHT, DEEP, REM, PAUSED and BROKEN lanes contain only complete source event pairs. Blank gaps are unknown. Use Inspect paired intervals for exact timestamps.");}
    private float dp(float value){return value*getResources().getDisplayMetrics().density;}
    private float left(){return dp(60);}private float right(){return getWidth()-dp(8);}private float row(){return dp(30);}private float top(){return dp(17);}
    private float x(long time){return left()+(float)((double)(time-from)/(to-from))*(right()-left());}
    @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);if(to<=from||right()<=left())return;paint.setTypeface(Design.font(getContext(),400));paint.setTextSize(dp(10));paint.setStrokeWidth(dp(1));
        float bottom=top()+row()*6;
        for(int n=0;n<=4;n++){float at=left()+(right()-left())*n/4;paint.setColor(NightDialTokens.RULE);canvas.drawLine(at,top(),at,bottom,paint);long time=from+(long)((to-from)*(n/4d));paint.setColor(Design.MUTED);paint.setTextAlign(n==0?Paint.Align.LEFT:n==4?Paint.Align.RIGHT:Paint.Align.CENTER);canvas.drawText(clock(time),at,bottom+dp(18),paint);}
        for(SleepCsvEvents.Lane lane:SleepCsvEvents.Lane.values()){float y=top()+row()*(lane.ordinal()+.5f);paint.setTextAlign(Paint.Align.RIGHT);paint.setColor(Design.MUTED);canvas.drawText(lane.label,left()-dp(8),y+dp(3),paint);paint.setColor(NightDialTokens.RULE);canvas.drawRoundRect(left(),y-dp(7),right(),y+dp(7),dp(5),dp(5),paint);}
        paint.setTextAlign(Paint.Align.RIGHT);paint.setColor(Design.MUTED);canvas.drawText("ALARM",left()-dp(8),top()+row()*5.5f+dp(3),paint);
        for(SleepCsvEvents.Span span:data.spans){if(span.end.time<=from||span.start.time>=to)continue;float y=top()+row()*(span.lane.ordinal()+.5f);paint.setColor(span.lane.color);float a=x(Math.max(from,span.start.time)),b=x(Math.min(to,span.end.time));canvas.drawRoundRect(a,y-dp(7),Math.max(a+dp(.5f),b),y+dp(7),dp(4),dp(4),paint);}
        for(SleepCsvEvents.Event event:data.alarms){if(event.time<from||event.time>to)continue;float at=x(event.time),y=top()+row()*5.5f;paint.setColor(Design.BROWN);canvas.drawCircle(at,y,dp(2.5f),paint);}
        paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(dp(9));paint.setColor(Design.MUTED);canvas.drawText("Source event time · "+zone.getId(),(left()+right())/2,getHeight()-dp(6),paint);
    }
    private String clock(long time){return Instant.ofEpochMilli(time).atZone(zone).format(DateTimeFormatter.ofPattern("HH:mm",Locale.getDefault()));}
    @Override public boolean onTouchEvent(MotionEvent event){if(to<=from||right()<=left())return false;switch(event.getActionMasked()){case MotionEvent.ACTION_DOWN:downX=event.getX();downY=event.getY();return true;case MotionEvent.ACTION_UP:float slop=ViewConfiguration.get(getContext()).getScaledTouchSlop();if(Math.abs(event.getX()-downX)>slop||Math.abs(event.getY()-downY)>slop)return true;if(event.getX()<left()||event.getX()>right())return true;int lane=(int)((event.getY()-top())/row());if(event.getY()<top()||lane<0||lane>=SleepCsvEvents.Lane.values().length)return true;long time=from+(long)((event.getX()-left())/(right()-left())*(to-from));for(SleepCsvEvents.Span span:data.spans)if(span.lane.ordinal()==lane&&time>=span.start.time&&time<=span.end.time){performClick();inspect.accept(span);break;}return true;case MotionEvent.ACTION_CANCEL:return true;default:return true;}}
    @Override public boolean performClick(){super.performClick();return true;}
}
