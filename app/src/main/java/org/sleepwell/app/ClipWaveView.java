package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Actual PCM peaks. Silence remains the centre line; colours show playback progress only. */
@android.annotation.SuppressLint("ViewConstructor") // Programmatic view requires actual decoded peaks.
final class ClipWaveView extends View {
    private final float[] peaks;private final Paint paint=new Paint(3);private final int active,inactive;private float progress;
    ClipWaveView(Context context,float[] peaks,int active,int inactive){super(context);this.peaks=peaks.clone();this.active=active;this.inactive=inactive;float maximum=0;for(float value:peaks)maximum=Math.max(maximum,value);if(maximum>0)for(int n=0;n<this.peaks.length;n++)this.peaks[n]/=maximum;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    void progress(float value){progress=Math.max(0,Math.min(1,value));invalidate();}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float density=getResources().getDisplayMetrics().density,mid=getHeight()/2f,step=getWidth()/(float)peaks.length;for(int n=0;n<peaks.length;n++){float height=peaks[n]*(getHeight()-4*density)/2;paint.setColor((n+.5f)/peaks.length<=progress?active:inactive);float x=step*(n+.5f),width=Math.min(3*density,step*.65f);if(height==0){paint.setStrokeWidth(density*.6f);c.drawLine(x-width/2,mid,x+width/2,mid,paint);}else c.drawRoundRect(x-width/2,mid-height,x+width/2,mid+height,width/2,width/2,paint);}}
}
