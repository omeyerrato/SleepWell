package org.sleepwell.app;
import android.content.Context;import android.graphics.*;import android.view.View;import java.util.*;
/** Real 40ms echo-change intervals. Missing/failed intervals remain blank; never decorative bars. */
@android.annotation.SuppressLint("ViewConstructor") final class SonarSignalChart extends View {
 private List<SonarFrame> frames=List.of();private final Paint p=new Paint(3);
 SonarSignalChart(Context c){super(c);data(List.of());}
 void data(List<SonarFrame> values){frames=List.copyOf(values);int count=0;for(SonarFrame f:frames)if(f.movement!=null)count++;setContentDescription("Contactless signal: "+count+" measured echo-change intervals; missing signal stays blank");invalidate();}
 private float d(float n){return Design.dp(getContext(),n);}
 @Override protected void onDraw(Canvas c){float l=d(27),r=getWidth()-d(6),t=d(8),b=getHeight()-d(23);p.setTypeface(Design.font(getContext(),400));p.setTextSize(Design.sp(getContext(),10));p.setColor(Design.LILAC);p.setTextAlign(Paint.Align.RIGHT);c.drawText("High",l-d(4),t+d(5),p);c.drawText("Low",l-d(4),b,p);p.setColor(0x445e526e);p.setStrokeWidth(d(1));for(int n=0;n<=4;n++){float x=l+(r-l)*n/4;c.drawLine(x,t,x,b,p);}c.drawLine(l,b,r,b,p);long end=frames.isEmpty()?15000:frames.get(frames.size()-1).to,start=end-15000;for(SonarFrame f:frames){if(f.movement==null||f.to<=start||f.from>=end)continue;float x=l+(r-l)*Math.max(0,f.from-start)/15000f,x2=l+(r-l)*Math.min(15000,f.to-start)/15000f;float h=(float)((b-t)*Math.min(1,f.movement/2));p.setColor(Design.VIOLET);c.drawRect(x,b-Math.max(d(1),h),Math.max(x+.25f,x2),b,p);}p.setColor(Design.LILAC);p.setTextAlign(Paint.Align.LEFT);c.drawText("15 s",l,b+d(17),p);p.setTextAlign(Paint.Align.RIGHT);c.drawText("Now",r,b+d(17),p);}
}
