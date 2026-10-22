package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Original scalable landscape; UI text and controls are separate native views. */
final class DawnScene extends View {
    private final Paint p=new Paint(3);
    DawnScene(Context c){super(c);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    private void fill(Canvas c,int color,Path path){p.setColor(color);c.drawPath(path,p);}
    @Override protected void onDraw(Canvas c){super.onDraw(c);c.save();c.scale(getWidth()/400f,getHeight()/150f);
        p.setColor(0xffffefd1);c.drawCircle(254,115,39,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setStrokeCap(Paint.Cap.ROUND);p.setColor(0xfff79d20);
        for(int i=0;i<7;i++){c.save();c.rotate(-85+i*28,254,115);c.drawLine(254,63,254,54,p);c.restore();}p.setStyle(Paint.Style.FILL);
        Path hills=new Path();hills.moveTo(0,70);hills.cubicTo(100,70,180,150,400,85);hills.lineTo(400,150);hills.lineTo(0,150);hills.close();fill(c,0xffca955c,hills);
        Path front=new Path();front.moveTo(0,120);front.cubicTo(135,57,200,180,400,105);front.lineTo(400,150);front.lineTo(0,150);front.close();fill(c,0xffb47c48,front);
        p.setColor(0xffffe6bd);c.drawOval(-25,12,42,43,p);c.drawOval(12,28,68,51,p);c.drawRect(0,42,100,52,p);c.drawOval(355,18,435,64,p);c.drawOval(322,42,390,72,p);
        p.setColor(0xff555744);for(int side:new int[]{0,1}){c.save();if(side==1){c.translate(400,0);c.scale(-1,1);}for(int n=0;n<7;n++){c.save();c.rotate(-35+n*11,10,148);c.drawOval(3,71+n*4,17,151,p);c.restore();}c.restore();}c.restore();
    }
}
