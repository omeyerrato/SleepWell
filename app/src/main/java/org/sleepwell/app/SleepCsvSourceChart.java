package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import java.math.BigDecimal;
import java.util.function.IntConsumer;

/** One bar per actual source column. Pages, rather than aggregation, retain every gap and raw value. */
final class SleepCsvSourceChart extends View {
    static final int PAGE=64;private final SleepCsvFormat.Signals signal;private final int first,last,color;private final IntConsumer inspect;private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private int selected=-1;private float downY;
    SleepCsvSourceChart(Context context,SleepCsvFormat.Signals signal,int page,int color,IntConsumer inspect){super(context);this.signal=signal;first=Math.max(0,Math.min(page*PAGE,Math.max(0,signal.samples.size()-1)));last=Math.min(signal.samples.size(),first+PAGE);this.color=color;this.inspect=inspect;setMinimumHeight(Design.dp(context,190));setFocusable(true);setContentDescription("Original vendor scale chart. Source positions "+(first+1)+" to "+last+". Missing values are blank; samples are not sleep stages or elapsed-time measurements.");}
    void selected(int index){selected=index;invalidate();}
    private float dp(float value){return value*getResources().getDisplayMetrics().density;}
    @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);float left=dp(48),right=getWidth()-dp(8),top=dp(16),bottom=getHeight()-dp(44);if(right<=left||bottom<=top)return;double maximum=signal.maximum.signum()>0?signal.maximum.doubleValue():1;paint.setTypeface(Design.font(getContext(),400));paint.setTextSize(dp(10));paint.setStrokeWidth(dp(1));
        for(int line=0;line<=2;line++){float y=bottom-(bottom-top)*line/2;paint.setColor(0xffe6d9ce);canvas.drawLine(left,y,right,y,paint);paint.setColor(Design.MUTED);paint.setTextAlign(Paint.Align.RIGHT);canvas.drawText(number(maximum*line/2),left-dp(7),y+dp(3),paint);}
        if(last<=first){paint.setColor(Design.MUTED);paint.setTextAlign(Paint.Align.CENTER);canvas.drawText("No numeric source samples",(left+right)/2,(top+bottom)/2,paint);return;}
        float step=(right-left)/(last-first),width=Math.max(dp(.5f),step*.68f);for(int n=first;n<last;n++){float x=left+(n-first+.5f)*step;SleepCsvFormat.Sample sample=signal.samples.get(n);if(n==selected){paint.setColor(0xffeadfd5);canvas.drawRect(x-step/2,top,x+step/2,bottom,paint);}if(!sample.available())continue;paint.setColor(color);float height=(float)Math.max(0,Math.min(1,sample.value.doubleValue()/maximum))*(bottom-top);if(sample.value.signum()==0)canvas.drawCircle(x,bottom,Math.min(dp(2),width/2),paint);else canvas.drawRoundRect(x-width/2,bottom-height,x+width/2,bottom,Math.min(dp(2),width/2),Math.min(dp(2),width/2),paint);}
        paint.setTextAlign(Paint.Align.CENTER);paint.setColor(Design.MUTED);int[] labels={first,first+(last-first-1)/2,last-1};for(int i=0;i<labels.length;i++){int n=labels[i];if(n<first||n>=last||i>0&&n==labels[i-1])continue;SleepCsvFormat.Sample sample=signal.samples.get(n);float x=left+(n-first+.5f)*step;x=Math.max(left+dp(13),Math.min(right-dp(13),x));canvas.drawText(sample.clock.isEmpty()?"#"+sample.column:sample.clock,x,bottom+dp(17),paint);}paint.setTextSize(dp(9));canvas.drawText(signal.clockColumns?"CSV column order · original clock labels":"CSV source order · column numbers",(left+right)/2,getHeight()-dp(5),paint);
    }
    private static String number(double value){if(value==0)return "0";if(value>=1e6)return String.format(java.util.Locale.ROOT,"%.1fM",value/1e6);if(value>=1000)return String.format(java.util.Locale.ROOT,"%.1fk",value/1000);return BigDecimal.valueOf(value).round(new java.math.MathContext(3)).stripTrailingZeros().toPlainString();}
    @Override public boolean onTouchEvent(MotionEvent event){if(last<=first)return false;switch(event.getActionMasked()){case MotionEvent.ACTION_DOWN:downY=event.getY();return true;case MotionEvent.ACTION_UP:if(Math.abs(event.getY()-downY)>ViewConfiguration.get(getContext()).getScaledTouchSlop())return true;float left=dp(48),right=getWidth()-dp(8);if(event.getX()<left||event.getX()>right)return true;int index=first+Math.min(last-first-1,(int)((event.getX()-left)/(right-left)*(last-first)));selected(index);performClick();inspect.accept(index);return true;case MotionEvent.ACTION_CANCEL:return true;default:return true;}}
    @Override public boolean performClick(){super.performClick();return true;}
}
