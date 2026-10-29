package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.util.Locale;

/** Room-only graphs of observed lux and digital RMS. No illustrative samples or gap filling. */
final class RoomConditionsUi {
    static String number(double value){return String.format(Locale.getDefault(),value<10?"%.1f":"%.0f",value);}
    static final class LightChart extends View {
        private final Paint paint=new Paint(3);private final RoomLightHistory history;private long now;
        LightChart(Context context,RoomLightHistory history){super(context);this.history=history;setContentDescription("Ambient light: no readings yet");}
        void time(long value){now=value;history.prune(now);int count=0;for(RoomLightHistory.Sample sample:history.samples)if(Float.isFinite(sample.lux))count++;setContentDescription("Ambient light: "+count+" actual observations in this screen's last 15 minutes. Bars average observations in each time bin; unmeasured bins stay blank");invalidate();}
        private float dp(float value){return Design.dp(getContext(),value);}
        @Override protected void onDraw(Canvas canvas){
            float left=dp(44),right=getWidth()-dp(5),top=dp(16),bottom=getHeight()-dp(25);if(right<=left||bottom<=top)return;
            float peak=0;int count=0;for(RoomLightHistory.Sample sample:history.samples)if(Float.isFinite(sample.lux)){peak=Math.max(peak,sample.lux);count++;}
            double high=peak<=1?1:peak<=20?Math.ceil(peak/5)*5:Math.ceil(peak/20)*20;
            paint.setTypeface(Design.font(getContext(),400));paint.setTextSize(Design.sp(getContext(),11*Appearance.scale(getContext())));paint.setStrokeWidth(dp(.7f));
            for(int n=0;n<3;n++){float y=bottom-(bottom-top)*n/2;paint.setColor(0xff62566d);canvas.drawLine(left,y,right,y,paint);paint.setColor(Design.LILAC);paint.setTextAlign(Paint.Align.RIGHT);canvas.drawText(number(high*n/2)+" lx",left-dp(6),y+dp(3),paint);}
            long start=history.start(now),end=Math.max(start+1,now);
            int columns=Math.max(1,Math.min(32,(int)((right-left)/dp(11))));double[] means=new double[columns];int[] readings=new int[columns];
            for(RoomLightHistory.Sample sample:history.samples){if(!Float.isFinite(sample.lux)||sample.time<start||sample.time>end)continue;int bin=Math.min(columns-1,(int)((sample.time-start)/(double)(end-start)*columns));readings[bin]++;means[bin]+=(sample.lux-means[bin])/readings[bin];}
            float slot=(right-left)/columns,width=Math.min(dp(10),slot*.76f);
            for(int bin=0;bin<columns;bin++){if(readings[bin]==0)continue;float x=left+slot*(bin+.5f),y=bottom-(float)((bottom-top)*means[bin]/high);y=Math.min(bottom-dp(1),y);paint.setShader(new LinearGradient(0,y,0,bottom,0xffe4ae6c,0xffa67545,Shader.TileMode.CLAMP));canvas.drawRoundRect(x-width/2,y,x+width/2,bottom,dp(3),dp(3),paint);paint.setShader(null);}
            paint.setColor(Design.LILAC);paint.setTextAlign(Paint.Align.LEFT);long seconds=Math.max(0,(end-start)/1000);String began=seconds<60?seconds+" s ago":seconds/60+" min ago";canvas.drawText(began,left,bottom+dp(19),paint);paint.setTextAlign(Paint.Align.RIGHT);canvas.drawText("Now",right,bottom+dp(19),paint);
            if(count==0){paint.setTextAlign(Paint.Align.CENTER);canvas.drawText("No light readings yet",(left+right)/2,(top+bottom)/2,paint);}
        }
    }
    static final class SoundMeter extends View {
        private final Paint paint=new Paint(3);private Double level;
        SoundMeter(Context context){super(context);setContentDescription("Sound level: not measuring");}
        void level(Double value){level=value;setContentDescription(value==null?"Sound level: not measuring":"Actual microphone RMS: "+number(value*100)+" percent of digital full scale; not sound pressure in decibels");invalidate();}
        @Override protected void onDraw(Canvas canvas){float gap=Design.dp(getContext(),4),width=(getWidth()-gap*6)/7f,height=getHeight(),radius=Design.dp(getContext(),5);float amount=level==null?0:(float)Math.max(0,Math.min(1,level));for(int n=0;n<7;n++){float left=n*(width+gap);paint.setColor(0xff51465e);canvas.drawRoundRect(left,0,left+width,height,radius,radius,paint);float filled=Math.max(0,Math.min(1,amount*7-n));if(filled>0){paint.setColor(Design.LILAC);canvas.drawRoundRect(left,0,left+width*filled,height,radius,radius,paint);}}}
    }
    private RoomConditionsUi(){}
}
