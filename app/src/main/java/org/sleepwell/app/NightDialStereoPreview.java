package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Mathematical preview of the configured source, before mix gain. No playback or capture. */
final class NightDialStereoPreview extends View {
    final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);final Path wave=new Path();int left,right;
    NightDialStereoPreview(Context c,int left,int right){super(c);setBackground(Design.shape(c,Design.SOFT,18));frequencies(left,right);}
    void frequencies(int left,int right){this.left=left;this.right=right;setContentDescription("Configured source before mix gain. Left "+left+" hertz; right "+right+" hertz. Each trace shows 40 milliseconds at 8 percent digital peak. No sound is playing from this preview.");invalidate();}
    @Override protected void onDraw(Canvas canvas){float d=getResources().getDisplayMetrics().density,start=48*d,end=getWidth()-16*d,top=32*d,bottom=getHeight()-28*d;if(end<=start||bottom<=top)return;paint.setTypeface(Design.font(getContext(),400));paint.setTextSize(Design.sp(getContext(),11*Appearance.scale(getContext())));paint.setStrokeWidth(d);float row=(bottom-top)/2,amplitude=row*.22f;for(int channel=0;channel<2;channel++){float y=top+row*(channel+.5f);paint.setStyle(Paint.Style.FILL);paint.setColor(Design.MUTED);paint.setTextAlign(Paint.Align.LEFT);canvas.drawText(channel==0?"Left":"Right",16*d,y-row*.34f,paint);paint.setTextAlign(Paint.Align.RIGHT);canvas.drawText("8%",start-8*d,y-amplitude+4*d,paint);paint.setColor(Design.LINE);canvas.drawLine(start,y,end,y,paint);wave.reset();int count=Math.max(2,(int)((end-start)/d));for(int n=0;n<=count;n++){double t=.040*n/count;float x=start+(end-start)*n/count,value=(float)Math.sin(2*Math.PI*(channel==0?left:right)*t);if(n==0)wave.moveTo(x,y-value*amplitude);else wave.lineTo(x,y-value*amplitude);}paint.setColor(channel==0?Design.INK:Design.BROWN);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.2f*d);canvas.drawPath(wave,paint);}paint.setStyle(Paint.Style.FILL);paint.setColor(Design.MUTED);paint.setTextAlign(Paint.Align.LEFT);canvas.drawText("0 ms",start,getHeight()-10*d,paint);paint.setTextAlign(Paint.Align.CENTER);canvas.drawText("20 ms",(start+end)/2,getHeight()-10*d,paint);paint.setTextAlign(Paint.Align.RIGHT);canvas.drawText("40 ms",end,getHeight()-10*d,paint);}
}
