package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;

/** The back of a game card, with a quiet embossed diamond; contains no puzzle evidence. */
final class NightDialMemoryBack extends Drawable {
    final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);final float d;final Path path=new Path();
    NightDialMemoryBack(Context c){d=c.getResources().getDisplayMetrics().density;}
    @Override public void draw(Canvas canvas){Rect b=getBounds();paint.setStyle(Paint.Style.FILL);paint.setColor(NightDialTokens.SURFACE);canvas.drawRoundRect(b.left,b.top,b.right,b.bottom,12*d,12*d,paint);float x=b.exactCenterX(),y=b.exactCenterY();paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(d);for(int n=0;n<3;n++){float r=(9+n*5)*d;paint.setColor(n==1?0xff8e7856:0xff4b4438);path.reset();path.moveTo(x,y-r);path.quadTo(x+r*.35f,y-r*.2f,x+r,y);path.quadTo(x+r*.2f,y+r*.35f,x,y+r);path.quadTo(x-r*.35f,y+r*.2f,x-r,y);path.quadTo(x-r*.2f,y-r*.35f,x,y-r);canvas.drawPath(path,paint);}paint.setStyle(Paint.Style.FILL);paint.setColor(NightDialTokens.META);canvas.drawCircle(x,y,1.5f*d,paint);}
    @Override public void setAlpha(int alpha){}@Override public void setColorFilter(ColorFilter filter){}@Override public int getOpacity(){return PixelFormat.OPAQUE;}
}
