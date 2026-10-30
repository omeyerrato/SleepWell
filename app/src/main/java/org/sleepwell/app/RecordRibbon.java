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
        final List<ChartSeries.Bar> movement;final double high;private final Path clip=new Path();
        Signal(RecordEntry record,List<JSONObject> epochs,int columns,long start,long end){List<ChartSeries.Sample> samples=new ArrayList<>();if(record!=null&&!record.manual())for(JSONObject epoch:epochs)samples.add(new ChartSeries.Sample(epoch.isNull("start")?null:epoch.optLong("start"),epoch.optLong("time"),epoch.isNull("movement")?null:epoch.optDouble("movement")));movement=record==null||record.manual()?List.of():ChartSeries.aggregate(samples,start,end,columns);double maximum=.001;for(ChartSeries.Bar bar:movement)maximum=Math.max(maximum,bar.value);high=maximum;}
        boolean measured(){return !movement.isEmpty();}
        private float at(long time,RectF area,long start,long end){return area.left+area.width()*(time-start)/(float)Math.max(1,end-start);}
        void draw(Canvas canvas,RectF area,RecordEntry projected,long start,long end,Paint paint,float density){if(projected==null||end<=start)return;float radius=3*density;paint.setStyle(Paint.Style.FILL);for(RecordIntervals.Span span:projected.coverage()){
            float left=at(span.start,area,start,end),right=at(span.end,area,start,end);paint.setColor(projected.manual()?0xffd0baf6:0xffd1bcf7);canvas.drawRoundRect(left,area.top,right,area.bottom,radius,radius,paint);
            int save=canvas.save();clip.reset();clip.addRoundRect(left,area.top,right,area.bottom,radius,radius,Path.Direction.CW);canvas.clipPath(clip);
            // Discrete measured bins make the coverage ribbon readable at a glance. The
            // colors describe movement only; they are not fabricated sleep-stage bands.
            for(ChartSeries.Bar measured:movement){long from=Math.max(span.start,measured.from),to=Math.min(span.end,measured.to);if(to<=from)continue;float a=at(from,area,start,end),b=at(to,area,start,end);paint.setColor(((18+(int)(108*(measured.value/high)))<<24)|0x007542d2);canvas.drawRect(a,area.top,b,area.bottom,paint);}
            canvas.restoreToCount(save);
        }}
    }
}
