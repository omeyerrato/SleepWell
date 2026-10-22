package org.sleepwell.app;

import android.content.*;
import android.graphics.*;
import android.view.*;
import org.json.*;
import java.util.*;

/** Plots actual sampled values. An empty graph stays empty. */
class GraphView extends View {
    private final List<JSONObject> points; private final String metric; private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private final Path path=new Path();
    public GraphView(Context c){this(c,List.of(),"movement");}
    GraphView(Context c,List<JSONObject> data,String metric) { super(c);points=data;this.metric=metric;setContentDescription(metric+" graph with "+data.size()+" recorded samples");setMinimumHeight(Ui.dp(c,150)); }
    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);float w=getWidth(),h=getHeight(),pad=Ui.dp(getContext(),12);paint.setStrokeWidth(Ui.dp(getContext(),1));
        paint.setColor(Color.rgb(53,69,59));for(int i=1;i<4;i++)c.drawLine(pad,h*i/4,w-pad,h*i/4,paint);
        if(points.isEmpty()) { paint.setColor(Ui.MUTED);paint.setTextSize(Ui.dp(getContext(),14));c.drawText("No sensor samples yet",pad,h/2,paint);return; }
        double max=.01;for(JSONObject p:points) max=Math.max(max,p.optDouble(metric,0));
        path.reset();double first=points.get(0).optDouble("time"),last=points.get(points.size()-1).optDouble("time");
        for(int i=0;i<points.size();i++) { JSONObject p=points.get(i);float x=(float)(pad+(w-2*pad)*(last>first?(p.optDouble("time")-first)/(last-first):.5));float y=(float)(h-pad-(h-2*pad)*p.optDouble(metric,0)/max);if(i==0)path.moveTo(x,y);else path.lineTo(x,y);if(points.size()==1) {paint.setColor(Ui.MINT);c.drawCircle(x,y,Ui.dp(getContext(),3),paint);} }
        paint.setColor(metric.equals("noise")?Ui.GOLD:Ui.MINT);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Ui.dp(getContext(),2));c.drawPath(path,paint);paint.setStyle(Paint.Style.FILL);
    }
}
