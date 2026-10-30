package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.util.Locale;

/** Exact requested wake envelope, including delay; this never represents measured speaker output. */
@android.annotation.SuppressLint("ViewConstructor")
final class AlarmRampChart extends View {
    private final AlarmWakePolicy policy;
    private final boolean silent;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path curve=new Path(),area=new Path();
    AlarmRampChart(Context context,Alarm alarm){
        super(context);policy=new AlarmWakePolicy(alarm);silent=AlarmSoundMode.SILENT.equals(alarm.soundMode);
        setFocusable(true);setContentDescription((silent?"Silent output. ":"")+"Requested volume curve: "+policy.targetPercent+" percent after "+policy.rampMillis/1000+" seconds of gradual increase. Sound starts after "+policy.delayMillis/1000+" seconds. Time is measured after the alarm starts. This is configuration, not measured speaker output. Device volume steps may round the level.");
    }
    private void ink(int color){paint.setColor(color);paint.setStyle(Paint.Style.FILL);}
    private static String clock(long millis){long seconds=millis/1000;return seconds/60+String.format(Locale.getDefault(),":%02d",seconds%60);}
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);float d=getResources().getDisplayMetrics().density;
        paint.setTypeface(Design.font(getContext(),400));paint.setTextSize(Design.sp(getContext(),11*Appearance.scale(getContext())));
        Paint.FontMetrics fm=paint.getFontMetrics();float line=fm.descent-fm.ascent,left=Math.max(42*d,paint.measureText("100%")+10*d),right=getWidth()-14*d,top=line+18*d,bottom=getHeight()-line*2-20*d;
        if(right<=left||bottom<=top)return;
        ink(NightDialTokens.SECONDARY);paint.setTextAlign(Paint.Align.LEFT);canvas.drawText("Requested volume",left,-fm.ascent,paint);
        paint.setTextAlign(Paint.Align.RIGHT);canvas.drawText(silent?"Silent output":policy.targetPercent+"%",right,-fm.ascent,paint);
        paint.setStrokeWidth(d*.7f);
        for(int n=0;n<=2;n++){float y=bottom-(bottom-top)*n/2;ink(NightDialTokens.RULE);canvas.drawLine(left,y,right,y,paint);ink(NightDialTokens.META);canvas.drawText(n*50+"%",left-8*d,y-(fm.ascent+fm.descent)/2,paint);}
        long total=policy.delayMillis+policy.rampMillis;boolean immediate=total==0;long extent=Math.max(1000,total);
        if(!silent){
            curve.reset();for(int n=0;n<=120;n++){float x=left+(right-left)*n/120;float y=bottom-(bottom-top)*(float)(policy.gain(extent*n/120,false)*policy.targetPercent/100);if(n==0)curve.moveTo(x,y);else curve.lineTo(x,y);}
            area.set(curve);area.lineTo(right,bottom);area.lineTo(left,bottom);area.close();ink(0x1af2bf6d);canvas.drawPath(area,paint);ink(NightDialTokens.ACCENT);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2.2f*d);canvas.drawPath(curve,paint);paint.setStyle(Paint.Style.FILL);canvas.drawCircle(right,bottom-(bottom-top)*policy.targetPercent/100f,3.5f*d,paint);
            if(policy.delayMillis>0&&policy.rampMillis>0){float x=left+(right-left)*policy.delayMillis/extent;ink(NightDialTokens.META);paint.setStrokeWidth(d*.7f);canvas.drawLine(x,top,x,bottom,paint);}
        }else{ink(NightDialTokens.SECONDARY);paint.setTextAlign(Paint.Align.CENTER);canvas.drawText("No sound in silent mode",(left+right)/2,(top+bottom-fm.ascent-fm.descent)/2,paint);}
        ink(NightDialTokens.SECONDARY);paint.setTextAlign(Paint.Align.CENTER);
        if(immediate){canvas.drawText("Immediate · no delay or ramp",(left+right)/2,bottom+line+5*d,paint);}
        else{int ticks=paint.measureText(clock(extent))*3+24*d>right-left?1:2;for(int n=0;n<=ticks;n++){paint.setTextAlign(n==0?Paint.Align.LEFT:n==ticks?Paint.Align.RIGHT:Paint.Align.CENTER);canvas.drawText(clock(extent*n/ticks),left+(right-left)*n/ticks,bottom+line+5*d,paint);}paint.setTextAlign(Paint.Align.CENTER);canvas.drawText("Time after alarm starts",(left+right)/2,bottom+line*2+10*d,paint);}
    }
}
