package org.sleepwell.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/** Flat stateful surface with stable geometry, visible focus, and a cancelable press tint. */
final class NightDialSurface extends Drawable {
    private final Context context;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bounds=new RectF();
    private final int base;
    private final float radius;
    private boolean focused,pressed,enabled=true;
    private int alpha=255;
    private float emphasis;
    private ValueAnimator animator;
    NightDialSurface(Context context,int base,float radiusDp){this.context=context;this.base=base;radius=Design.dp(context,radiusDp);}
    @Override public boolean isStateful(){return true;}
    @Override protected boolean onStateChange(int[] states){
        boolean nextFocus=false,nextPress=false,nextEnabled=false;
        for(int state:states){if(state==android.R.attr.state_focused)nextFocus=true;if(state==android.R.attr.state_pressed)nextPress=true;if(state==android.R.attr.state_enabled)nextEnabled=true;}
        boolean changed=nextFocus!=focused||nextPress!=pressed||nextEnabled!=enabled;
        focused=nextFocus;pressed=nextPress;enabled=nextEnabled;
        if(changed){animateTo(enabled&&pressed?1f:0f);invalidateSelf();}return changed;
    }
    private void animateTo(float target){
        if(animator!=null){animator.cancel();animator=null;}
        if(!NightDialMotion.enabled(context)||getCallback()==null||!isVisible()){emphasis=target;return;}
        animator=ValueAnimator.ofFloat(emphasis,target);animator.setDuration(NightDialMotion.PRESS);
        animator.addUpdateListener(value->{emphasis=(float)value.getAnimatedValue();invalidateSelf();});animator.start();
    }
    @Override public void draw(Canvas canvas){
        bounds.set(getBounds());paint.setStyle(Paint.Style.FILL);
        int color=base;
        if(!enabled&&Color.alpha(base)>0)color=NightDialTokens.RAISED;
        paint.setColor(color);paint.setAlpha(Math.round(Color.alpha(color)*alpha/255f));canvas.drawRoundRect(bounds,radius,radius,paint);
        if(emphasis>0){paint.setColor(NightDialTokens.foreground(base)==NightDialTokens.ACTION_INK?Color.BLACK:Color.WHITE);paint.setAlpha(Math.round(24*emphasis*alpha/255f));canvas.drawRoundRect(bounds,radius,radius,paint);}
        if(focused&&enabled){float stroke=Design.dp(context,2);paint.setStyle(Paint.Style.STROKE);paint.setColor(NightDialTokens.foreground(base)==NightDialTokens.ACTION_INK?NightDialTokens.ACTION_INK:NightDialTokens.ACCENT);paint.setAlpha(alpha);paint.setStrokeWidth(stroke);bounds.inset(stroke/2,stroke/2);canvas.drawRoundRect(bounds,radius,radius,paint);}
    }
    @Override public void jumpToCurrentState(){if(animator!=null){animator.cancel();animator=null;}emphasis=enabled&&pressed?1:0;invalidateSelf();}
    @Override public boolean setVisible(boolean visible,boolean restart){boolean changed=super.setVisible(visible,restart);if(!visible)jumpToCurrentState();return changed;}
    @Override public void setAlpha(int alpha){this.alpha=alpha;invalidateSelf();}
    @Override public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);invalidateSelf();}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
