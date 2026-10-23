package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Bedtime-to-alarm plan, not fabricated sensor or sleep-stage data. */
final class NightArc extends View {
    private final Paint p=new Paint(3);private final int bedtime,wake;
    NightArc(Context c,int bedtime,int wake,boolean enabled){super(c);this.bedtime=bedtime;this.wake=wake;setContentDescription("Planned rest from "+clock(bedtime)+" to "+clock(wake));}
    private String clock(int minute){return Design.clock(getContext(),minute);}
    @Override protected void onDraw(Canvas c){super.onDraw(c);Appearance.Palette palette=Appearance.palette(getContext());float w=getWidth(),s=w/360f,scale=Design.sp(getContext(),13*Appearance.scale(getContext()))/(13*s);c.save();c.scale(s,s);c.translate(0,Math.max(0,(getHeight()/s-130)/2));
        // The plan is one continuous curve. Its accent is a trimmed portion of that
        // exact path, so stroke edges and tangents cannot separate at the join.
        Path arc=new Path();arc.moveTo(30,66);arc.quadTo(180,-24,330,66);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(20);p.setStrokeCap(Paint.Cap.ROUND);p.setColor(palette.line);c.drawPath(arc,p);
        PathMeasure measure=new PathMeasure(arc,false);Path active=new Path();measure.getSegment(0,measure.getLength()*.8f,active,true);p.setShader(new LinearGradient(30,0,330,0,new int[]{palette.dark?Design.VIOLET:palette.accent,palette.accent},null,Shader.TileMode.CLAMP));c.drawPath(active,p);p.setShader(null);
        p.setStyle(Paint.Style.FILL);p.setColor(palette.softInk);float[] position=new float[2];for(int i=1;i<=9;i++){measure.getPosTan(measure.getLength()*i/10f,position,null);c.drawCircle(position[0],position[1]+25,1.7f,p);}
        c.drawCircle(30,66,17,p);p.setColor(palette.bg);c.drawCircle(31,65,9,p);p.setColor(palette.softInk);c.drawCircle(35,61,8,p);
        p.setColor(0xfff7ce83);c.drawCircle(330,66,17,p);p.setColor(Design.BROWN);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.6f);c.drawArc(324,62,336,74,180,180,false,p);c.drawLine(320,74,340,74,p);for(int i=0;i<5;i++){c.save();c.rotate(-90+i*45,330,68);c.drawLine(330,57,330,54,p);c.restore();}p.setStyle(Paint.Style.FILL);
        p.setTypeface(Design.font(getContext(),500));p.setTextSize(13*scale);p.setColor(palette.ink);p.setTextAlign(Paint.Align.CENTER);int mins=(wake-bedtime+1440)%1440;String duration=mins/60+" h"+(mins%60>0?" "+mins%60+" min":"");c.drawText(duration+" planned",180,80,p);
        p.setTypeface(Design.font(getContext(),600));p.setTextSize(15*scale);p.setColor(palette.softInk);p.setTextAlign(Paint.Align.LEFT);c.drawText(clock(bedtime),0,105,p);p.setTextAlign(Paint.Align.RIGHT);c.drawText(clock(wake),360,105,p);
        p.setTypeface(Design.font(getContext(),400));p.setTextSize(12*scale);p.setTextAlign(Paint.Align.LEFT);c.drawText("Bedtime",0,123+(scale-1)*12,p);p.setTextAlign(Paint.Align.RIGHT);c.drawText("Wake-up",360,123+(scale-1)*12,p);c.restore();
    }
}
