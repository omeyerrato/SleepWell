package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Bedtime-to-alarm plan, not fabricated sensor or sleep-stage data. */
final class NightArc extends View {
    private final Paint p=new Paint(3);private final int bedtime,wake;private final boolean enabled;
    NightArc(Context c,int bedtime,int wake,boolean enabled){super(c);this.bedtime=bedtime;this.wake=wake;this.enabled=enabled;setContentDescription("Planned rest from "+clock(bedtime)+" to "+clock(wake));}
    private static String clock(int minute){return String.format(java.util.Locale.getDefault(),"%d:%02d %s",minute/60%12==0?12:minute/60%12,minute%60,minute<720?"AM":"PM");}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),s=w/360f;c.save();c.scale(s,s);
        Path arc=new Path();arc.moveTo(30,66);arc.quadTo(180,-33,330,66);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(20);p.setStrokeCap(Paint.Cap.ROUND);p.setColor(0xff494057);c.drawPath(arc,p);
        Path active=new Path();active.moveTo(30,66);active.quadTo(151,-15,270,38);if(enabled)p.setShader(new LinearGradient(30,0,270,0,new int[]{0xffc3a0fa,0xffa67be5},null,Shader.TileMode.CLAMP));else p.setColor(0xff756286);c.drawPath(active,p);p.setShader(null);
        p.setStyle(Paint.Style.FILL);p.setColor(Design.LILAC);for(int i=1;i<=9;i++){float x=42+i*28,y=69-24*(float)Math.sin(i*Math.PI/10);c.drawCircle(x,y,1.7f,p);}
        c.drawCircle(30,66,17,p);p.setColor(Design.PLUM);c.drawCircle(31,65,9,p);p.setColor(Design.LILAC);c.drawCircle(35,61,8,p);
        p.setColor(0xfff7ce83);c.drawCircle(330,66,17,p);p.setColor(Design.BROWN);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.6f);c.drawArc(324,62,336,74,180,180,false,p);c.drawLine(320,74,340,74,p);for(int i=0;i<5;i++){c.save();c.rotate(-90+i*45,330,68);c.drawLine(330,57,330,54,p);c.restore();}p.setStyle(Paint.Style.FILL);
        p.setTypeface(Design.font(getContext(),500));p.setTextSize(13);p.setColor(Design.CREAM);p.setTextAlign(Paint.Align.CENTER);int mins=(wake-bedtime+1440)%1440;String duration=mins/60+" h"+(mins%60>0?" "+mins%60+" min":"");c.drawText(duration+" planned",180,80,p);
        p.setTypeface(Design.font(getContext(),600));p.setTextSize(15);p.setColor(Design.LILAC);p.setTextAlign(Paint.Align.LEFT);c.drawText(clock(bedtime),0,105,p);p.setTextAlign(Paint.Align.RIGHT);c.drawText(clock(wake),360,105,p);
        p.setTypeface(Design.font(getContext(),400));p.setTextSize(12);p.setTextAlign(Paint.Align.LEFT);c.drawText("Bedtime",0,123,p);p.setTextAlign(Paint.Align.RIGHT);c.drawText("Wake-up",360,123,p);c.restore();
    }
}
