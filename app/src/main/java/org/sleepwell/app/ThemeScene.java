package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Original vector color studies. No text or touch targets are baked into the artwork. */
final class ThemeScene extends View {
    private final int phase;private final Paint paint=new Paint(3);
    ThemeScene(Context c,int phase){super(c);this.phase=phase;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    private void hill(Canvas c,int color,float a,float b,float d){
        paint.setColor(color);Path p=new Path();p.moveTo(0,a);p.cubicTo(35,b,65,d,100,a+8);p.lineTo(100,100);p.lineTo(0,100);p.close();c.drawPath(p,paint);
    }
    @Override protected void onDraw(Canvas c){super.onDraw(c);c.save();float width=getWidth()/100f,height=getHeight()/100f;if(width<=0||height<=0){c.restore();return;}c.scale(width,height);
        int top=phase==0?0xff211b38:phase==1?0xff8d61df:0xffffb86b;
        int bottom=phase==0?0xff514269:phase==1?0xffd2a9fb:0xffffe1a7;
        paint.setShader(new LinearGradient(0,0,0,100,top,bottom,Shader.TileMode.CLAMP));c.drawRect(0,0,100,100,paint);paint.setShader(null);
        // The landscape fills the actual rectangle. Sky objects use the height scale on both axes,
        // keeping their shape and their whole silhouette visible in the adaptive 64–100dp previews.
        if(phase==0){c.save();c.translate(48,38);c.scale(height/width,1);paint.setColor(0xffdfc7ff);Path moon=new Path(),cutout=new Path();moon.addCircle(0,0,14,Path.Direction.CW);cutout.addCircle(7,-6,13,Path.Direction.CW);moon.op(cutout,Path.Op.DIFFERENCE);c.drawPath(moon,paint);c.restore();
            paint.setColor(0xffdac5f5);for(float[] star:new float[][]{{15,17},{79,21},{27,48},{88,47},{66,62}}){float radius=.8f*height/width;c.drawOval(star[0]-radius,star[1]-.8f,star[0]+radius,star[1]+.8f,paint);}
        }else if(phase==1){paint.setColor(0xffe6cbfa);c.drawOval(-10,42,32,57,paint);c.drawOval(9,36,39,57,paint);c.drawOval(58,51,112,65,paint);
        }else{c.save();c.translate(58,68);c.scale(height/width,1);paint.setColor(0xfffff3c9);c.drawCircle(0,0,17,paint);paint.setColor(0xfff6a338);paint.setStrokeWidth(1.6f);
            for(int n=0;n<7;n++){c.save();c.rotate(-85+n*28);c.drawLine(0,-25,0,-30,paint);c.restore();}c.restore();
            paint.setColor(0xffffe9c0);c.drawOval(-8,36,22,48,paint);c.drawOval(8,41,37,51,paint);}
        hill(c,phase==2?0xffd9aa71:phase==1?0xffaa7cdb:0xff655080,74,48,95);
        hill(c,phase==2?0xffc28b51:phase==1?0xff8357b3:0xff403054,87,60,100);
        hill(c,phase==2?0xffa8723e:phase==1?0xff614384:0xff2a233e,96,82,87);
        paint.setColor(phase==2?0xff62604b:0xff30283f);for(int side=0;side<2;side++){c.save();if(side==1){c.translate(100,0);c.scale(-1,1);}for(int n=0;n<4;n++){c.save();c.rotate(-28+n*18,8,101);c.drawOval(6,78+n*2,12,105,paint);c.restore();}c.restore();}c.restore();
    }
}
