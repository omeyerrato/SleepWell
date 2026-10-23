package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import org.json.JSONObject;
import java.util.*;

/** A compact record interval. Colour varies only where actual movement was measured. */
@android.annotation.SuppressLint("ViewConstructor") // Immutable record data is required; this View is never XML-inflated.
final class RecordRibbon extends View {
    private final RecordEntry record;
    private final List<ChartSeries.Bar> movement;
    private final List<RecordIntervals.Span> coverage;
    private final double high;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    RecordRibbon(Context context,RecordEntry record,List<JSONObject> epochs){
        super(context);this.record=record;
        List<ChartSeries.Sample> samples=new ArrayList<>();
        for(JSONObject epoch:epochs)samples.add(new ChartSeries.Sample(epoch.isNull("start")?null:epoch.optLong("start"),epoch.optLong("time"),epoch.isNull("movement")?null:epoch.optDouble("movement")));
        movement=record.manual()?List.of():ChartSeries.aggregate(samples,record.edit.start,record.edit.end,42);coverage=record.coverage();double maximum=.001;for(ChartSeries.Bar bar:movement)maximum=Math.max(maximum,bar.value);high=maximum;
        setContentDescription(record.manual()?"User-reported interval; no measured sleep stages":"Recorded coverage with measured relative movement; gaps are pauses or awake edits. This is not a sleep-stage graph.");
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }
    private float at(long time){return getWidth()*(time-record.edit.start)/(float)Math.max(1,record.edit.end-record.edit.start);}
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);float density=getResources().getDisplayMetrics().density,top=getHeight()*.19f,bottom=getHeight()*.81f,radius=3*density;
        paint.setColor(0xffeee5f5);canvas.drawRoundRect(0,top,getWidth(),bottom,radius,radius,paint);
        paint.setColor(record.manual()?0xffd0baf6:0xffc6a8f4);
        for(RecordIntervals.Span span:coverage)canvas.drawRoundRect(at(span.start),top,at(span.end),bottom,radius,radius,paint);
        for(ChartSeries.Bar bar:movement){
            int alpha=45+(int)(145*(bar.value/high));paint.setColor((alpha<<24)|0x007542d2);
            for(RecordIntervals.Span span:coverage){long start=Math.max(span.start,bar.from),end=Math.min(span.end,bar.to);if(end>start)canvas.drawRoundRect(at(start),top,at(end),bottom,radius,radius,paint);}
        }
    }
}
