package org.sleepwell.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/** Decorative art only. The containing View draws this across its padding/insets too. */
final class WakeDawnBackground extends Drawable {
    private final Bitmap image;
    private final Paint paint=new Paint(Paint.FILTER_BITMAP_FLAG|Paint.DITHER_FLAG);
    private final RectF destination=new RectF();

    WakeDawnBackground(Context context){
        // Resources owns/caches the immutable bitmap; state changes do not decode a new image.
        image=((BitmapDrawable)context.getDrawable(R.drawable.wake_dawn_landscape)).getBitmap();
    }

    @Override protected void onBoundsChange(Rect bounds){
        if(bounds.isEmpty()){destination.setEmpty();return;}
        float scale=Math.max((float)bounds.width()/image.getWidth(),(float)bounds.height()/image.getHeight());
        float width=image.getWidth()*scale,height=image.getHeight()*scale;
        float left=bounds.left+(bounds.width()-width)/2,top=bounds.top+(bounds.height()-height)/2;
        destination.set(left,top,left+width,top+height);
    }

    @Override public void draw(Canvas canvas){
        if(destination.isEmpty())return;
        int saved=canvas.save();canvas.clipRect(getBounds());canvas.drawBitmap(image,null,destination,paint);canvas.restoreToCount(saved);
    }
    @Override public void setAlpha(int alpha){paint.setAlpha(alpha);invalidateSelf();}
    @Override public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);invalidateSelf();}
    @Override public int getOpacity(){return paint.getAlpha()==255?PixelFormat.OPAQUE:PixelFormat.TRANSLUCENT;}
}
