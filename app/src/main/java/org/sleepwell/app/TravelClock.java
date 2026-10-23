package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.time.*;

/** A live civil-time dial; its hands always use the selected zone's actual offset. */
@android.annotation.SuppressLint("ViewConstructor")
final class TravelClock extends View {
    private final Paint paint=new Paint(3);private final int face;private final String zone;private double hour,minute;
    TravelClock(Context c,String zone,int face){super(c);this.zone=zone;this.face=face;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    void update(long time){ZonedDateTime date=Instant.ofEpochMilli(time).atZone(ZoneId.of(zone));hour=Math.PI*(date.getHour()%12+date.getMinute()/60d)/6;minute=Math.PI*date.getMinute()/30;invalidate();}
    @Override protected void onDraw(Canvas canvas){float x=getWidth()/2f,y=getHeight()/2f,r=Math.min(getWidth(),getHeight())*.43f;paint.setColor(face);paint.setStyle(Paint.Style.FILL);canvas.drawCircle(x,y,r,paint);paint.setColor(Design.BROWN);paint.setStrokeWidth(Design.dp(getContext(),1));for(int n=0;n<12;n++){double angle=Math.PI*n/6;canvas.drawLine(x+(float)Math.sin(angle)*r*.82f,y-(float)Math.cos(angle)*r*.82f,x+(float)Math.sin(angle)*r*.91f,y-(float)Math.cos(angle)*r*.91f,paint);}paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeWidth(Design.dp(getContext(),3));canvas.drawLine(x,y,x+(float)Math.sin(hour)*r*.43f,y-(float)Math.cos(hour)*r*.43f,paint);paint.setStrokeWidth(Design.dp(getContext(),2));canvas.drawLine(x,y,x+(float)Math.sin(minute)*r*.68f,y-(float)Math.cos(minute)*r*.68f,paint);canvas.drawCircle(x,y,Design.dp(getContext(),3),paint);}
}
