package org.sleepwell.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Saved mix discovery with explicit Play. Opening Tonight never starts or changes playback. */
final class NightDialSoundCard extends FrameLayout {
    private static final ExecutorService ART=Executors.newSingleThreadExecutor(r->new Thread(r,"Night Dial artwork"));
    private static volatile Bitmap rain;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final ImageView image;
    private final Design.Icon transport;
    private final TextView detail;
    private final SoundMix mix;
    private final boolean artworkVisible;
    private final Runnable refresh=new Runnable(){public void run(){if(!isAttachedToWindow()||getWindowVisibility()!=VISIBLE)return;refreshTransport();handler.postDelayed(this,1000);}};
    NightDialSoundCard(Activity activity,Runnable open,boolean artworkVisible){
        super(activity);this.artworkVisible=artworkVisible;SoundMix saved;try{saved=SoundPreferences.load(activity);}catch(RuntimeException unavailable){saved=new SoundMix();}mix=saved;
        setBackground(Design.shape(activity,NightDialTokens.SURFACE,16));setForeground(Design.touch(activity,Color.TRANSPARENT,16));setClipToOutline(true);setMinimumHeight(Design.dp(activity,artworkVisible?110:76));
        image=new ImageView(activity);image.setScaleType(ImageView.ScaleType.CENTER_CROP);image.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);addView(image,new LayoutParams(-1,-1));
        image.setVisibility(artworkVisible?VISIBLE:GONE);if(artworkVisible){View shade=new View(activity);shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0x11000000,0xcc000000}));addView(shade,new LayoutParams(-1,-1));}
        LinearLayout copy=Design.col(activity);copy.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);Design.text(copy,mix.primary.equals("Rain")?"Rain on glass":mix.label(),16,600,NightDialTokens.TEXT);Design.space(copy,4);detail=Design.text(copy,"Sleep sounds · "+mix.timerMinutes+" min",12,400,NightDialTokens.TEXT);
        LayoutParams position=new LayoutParams(-1,-2,Gravity.BOTTOM);position.setMargins(Design.dp(activity,16),Design.dp(activity,12),Design.dp(activity,76),Design.dp(activity,16));addView(copy,position);
        transport=new Design.Icon(activity,"play",NightDialTokens.TEXT);transport.setPadding(Design.dp(activity,13),Design.dp(activity,13),Design.dp(activity,13),Design.dp(activity,13));transport.setBackground(Design.touch(activity,0xcc171717,100));transport.setFocusable(true);transport.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);SoundEventUi.button(transport);
        LayoutParams control=new LayoutParams(Design.dp(activity,48),Design.dp(activity,48),Gravity.BOTTOM|Gravity.END);control.setMargins(0,0,Design.dp(activity,12),Design.dp(activity,12));addView(transport,control);
        transport.setOnClickListener(view->{InteractionFeedback.action(view);SleepSoundService.Snapshot current=SleepSoundService.snapshot();boolean active=current!=null&&(current.state==SoundPlayback.State.PLAYING||current.state==SoundPlayback.State.LOADING);SleepSoundService.command(activity,active?SleepSoundService.PAUSE:SleepSoundService.PLAY);handler.postDelayed(this::refreshTransport,150);});
        setContentDescription("Open sleep sounds, "+mix.label());setFocusable(true);SoundEventUi.button(this);setOnClickListener(view->{InteractionFeedback.action(view);open.run();});
    }
    private void refreshTransport(){SleepSoundService.Snapshot current=SleepSoundService.snapshot();boolean active=current!=null&&(current.state==SoundPlayback.State.PLAYING||current.state==SoundPlayback.State.LOADING);transport.glyphName(active?"pause":"play");transport.setContentDescription((active?"Pause":"Play")+" sleep sounds");String error=SleepSoundService.error(getContext());detail.setText(!error.isEmpty()?"Playback needs attention · open mixer":current!=null&&current.state==SoundPlayback.State.LOADING?"Loading sounds…":active?"Playing · "+mix.timerMinutes+" min timer":"Sleep sounds · "+mix.timerMinutes+" min");}
    @Override protected void onAttachedToWindow(){super.onAttachedToWindow();handler.post(refresh);if(!artworkVisible)return;if(mix.primary.equals("Rain")&&rain!=null){image.setImageBitmap(rain);return;}Context app=getContext().getApplicationContext();String source=mix.primary;ART.execute(()->{Bitmap bitmap=null;try{BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=2;if(source.equals("Rain")){bitmap=rain;if(bitmap==null)rain=bitmap=BitmapFactory.decodeResource(app.getResources(),R.drawable.night_dial_rain,options);}else{String name=switch(source){case "Ocean"->"ocean";case "Wind"->"wind";case "Fireplace"->"fire";default->"imported";};try(java.io.InputStream stream=app.getAssets().open("sound-art/"+name+".png")){bitmap=BitmapFactory.decodeStream(stream,null,options);}}}catch(Exception ignored){}Bitmap result=bitmap;handler.post(()->{if(isAttachedToWindow()&&result!=null)image.setImageBitmap(result);});});}
    @Override protected void onDetachedFromWindow(){handler.removeCallbacksAndMessages(null);super.onDetachedFromWindow();}
    @Override protected void onWindowVisibilityChanged(int visibility){super.onWindowVisibilityChanged(visibility);if(handler==null)return;handler.removeCallbacks(refresh);if(visibility==VISIBLE&&isAttachedToWindow())handler.post(refresh);}
}
