package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import org.json.JSONObject;
import java.util.*;

/** A compact coverage ribbon; shading is aggregated measured movement, never inferred stages. */
@android.annotation.SuppressLint("ViewConstructor")
final class RecordRibbon extends View {
    private final RecordEntry record;private final Signal signal;private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private final RectF bounds=new RectF();
    RecordRibbon(Context context,RecordEntry record,List<JSONObject> epochs){super(context);this.record=record;signal=new Signal(record,epochs,16,record.edit.start,record.edit.end);setContentDescription(record.manual()?"User-reported interval; no measured sleep stages":signal.measured()?"Recorded coverage with measured relative movement; gaps are pauses or awake edits. This is not a sleep-stage graph.":"Recorded coverage; movement measurements unavailable. Gaps are pauses or awake edits.");setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);bounds.set(0,getHeight()*.19f,getWidth(),getHeight()*.81f);signal.draw(canvas,bounds,record,record.edit.start,record.edit.end,paint,getResources().getDisplayMetrics().density);}
    /** Shared immutable measured projection used by both history and the editable period. */
    static final class Signal {
        final List<ChartSeries.Bar> movement;final double high;private final Path clip=new Path();private final List<Shade> shades=new ArrayList<>();
        /** Visual color blending only: every stop comes from a measured bin; gaps start a new run. */
        private static final class Shade {
            final long from,to;final int[] colors;final float[] stops;
            Shade(List<ChartSeries.Bar> bars,int first,int last,double high){from=bars.get(first).from;to=bars.get(last).to;int count=last-first+1;colors=new int[count+2];stops=new float[count+2];
                for(int index=0;index<count;index++){ChartSeries.Bar bar=bars.get(first+index);colors[index+1]=((18+(int)(108*(bar.value/high)))<<24)|0x007542d2;stops[index+1]=(float)((bar.from+(bar.to-bar.from)/2-from)/(double)Math.max(1,to-from));}
                colors[0]=colors[1];colors[count+1]=colors[count];stops[count+1]=1;
            }
        }
        Signal(RecordEntry record,List<JSONObject> epochs,int columns,long start,long end){List<ChartSeries.Sample> samples=new ArrayList<>();if(record!=null&&!record.manual())for(JSONObject epoch:epochs)samples.add(new ChartSeries.Sample(epoch.isNull("start")?null:epoch.optLong("start"),epoch.optLong("time"),epoch.isNull("movement")?null:epoch.optDouble("movement")));movement=record==null||record.manual()?List.of():ChartSeries.aggregate(samples,start,end,columns);double maximum=.001;for(ChartSeries.Bar bar:movement)maximum=Math.max(maximum,bar.value);high=maximum;
            for(int first=0;first<movement.size();){int last=first;while(last+1<movement.size()&&movement.get(last+1).from==movement.get(last).to)last++;if(movement.get(last).to>movement.get(first).from)shades.add(new Shade(movement,first,last,high));first=last+1;}
        }
        boolean measured(){return !movement.isEmpty();}
        private float at(long time,RectF area,long start,long end){return area.left+area.width()*(time-start)/(float)Math.max(1,end-start);}
        void draw(Canvas canvas,RectF area,RecordEntry projected,long start,long end,Paint paint,float density){if(projected==null||end<=start)return;float radius=3*density;paint.setStyle(Paint.Style.FILL);for(RecordIntervals.Span span:projected.coverage()){
            float left=at(span.start,area,start,end),right=at(span.end,area,start,end);paint.setColor(projected.manual()?0xffd0baf6:0xffd1bcf7);canvas.drawRoundRect(left,area.top,right,area.bottom,radius,radius,paint);
            int save=canvas.save();clip.reset();clip.addRoundRect(left,area.top,right,area.bottom,radius,radius,Path.Direction.CW);canvas.clipPath(clip);
            for(Shade shade:shades){long from=Math.max(span.start,shade.from),to=Math.min(span.end,shade.to);if(to<=from)continue;paint.setShader(new LinearGradient(at(shade.from,area,start,end),0,at(shade.to,area,start,end),0,shade.colors,shade.stops,Shader.TileMode.CLAMP));canvas.drawRect(at(from,area,start,end),area.top,at(to,area,start,end),area.bottom,paint);paint.setShader(null);}canvas.restoreToCount(save);
        }}
    }
}
