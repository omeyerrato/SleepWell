package org.sleepwell.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Static preview of actual bundled loop samples. Never opens a microphone or a document URI. */
final class NightDialWaveform extends View {
    private static final ExecutorService READER=Executors.newSingleThreadExecutor(r->new Thread(r,"Sound preview samples"));
    private static final Map<String,float[]> CACHE=new ConcurrentHashMap<>();
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private final String asset;private float[] peaks;private boolean unavailable,requested;private float gain=1;private android.animation.ValueAnimator gainAnimation;
    void gain(int value,boolean animate){float next=value/100f;if(gainAnimation!=null)gainAnimation.cancel();if(!animate||!NightDialMotion.enabled(getContext())){gain=next;invalidate();return;}gainAnimation=android.animation.ValueAnimator.ofFloat(gain,next);gainAnimation.setDuration(100);gainAnimation.addUpdateListener(a->{gain=(float)a.getAnimatedValue();invalidate();});gainAnimation.start();}
    @Override protected void onDetachedFromWindow(){if(gainAnimation!=null)gainAnimation.cancel();super.onDetachedFromWindow();}
    NightDialWaveform(Context context,String asset){super(context);this.asset=asset;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    @Override protected void onAttachedToWindow(){super.onAttachedToWindow();if(asset==null){unavailable=true;invalidate();return;}peaks=CACHE.get(asset);if(peaks!=null||requested)return;requested=true;Context app=getContext().getApplicationContext();READER.execute(()->{float[] decoded=null;try(java.io.InputStream input=app.getAssets().open(asset)){decoded=WavPreview.read(input,128);CACHE.put(asset,decoded);}catch(Exception ignored){}float[] result=decoded;post(()->{peaks=result;unavailable=result==null;if(isAttachedToWindow())invalidate();});});}
    @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);float middle=getHeight()/2f;paint.setStrokeWidth(Math.max(1,Design.dp(getContext(),.7f)));paint.setColor(NightDialTokens.META);if(peaks==null){paint.setStrokeWidth(Design.dp(getContext(),1));canvas.drawLine(0,middle,getWidth(),middle,paint);paint.setTypeface(Design.font(getContext(),400));paint.setTextSize(Design.sp(getContext(),12*Appearance.scale(getContext())));paint.setTextAlign(Paint.Align.CENTER);String label=unavailable?"Preview unavailable":"Loading sound preview…";paint.setColor(NightDialTokens.SECONDARY);canvas.drawText(label,getWidth()/2f,middle-Design.dp(getContext(),6),paint);return;}float maximum=0;for(float value:peaks)maximum=Math.max(maximum,value);float step=getWidth()/(float)peaks.length;paint.setColor(NightDialTokens.TEXT);for(int n=0;n<peaks.length;n++){float height=maximum==0?0:peaks[n]/maximum*(getHeight()*.43f)*gain;float x=(n+.5f)*step;canvas.drawLine(x,middle-Math.max(.5f,height),x,middle+Math.max(.5f,height),paint);}}
}
