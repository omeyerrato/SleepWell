package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.os.*;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import java.util.concurrent.*;

/** Bounded decorative image reader: black placeholder, worker decode, no retained Activity. */
final class NightDialArt extends ImageView {
    private static final ExecutorService READER=Executors.newSingleThreadExecutor(r->new Thread(r,"night-dial-art"));
    private static final Handler MAIN=new Handler(Looper.getMainLooper());
    private static final LruCache<Integer,Bitmap> CACHE=new LruCache<>(4*1024*1024){@Override protected int sizeOf(Integer key,Bitmap value){return value.getByteCount();}};
    private final int resource;private int generation;
    NightDialArt(Context c,int resource){super(c);this.resource=resource;setScaleType(ScaleType.CENTER_CROP);setBackground(Design.shape(c,Design.SOFT,20));setClipToOutline(true);setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);}
    @Override protected void onAttachedToWindow(){super.onAttachedToWindow();Bitmap cached=CACHE.get(resource);if(cached!=null){setImageBitmap(cached);return;}int request=++generation;android.content.res.Resources resources=getResources();READER.execute(()->{BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=2;Bitmap bitmap=BitmapFactory.decodeResource(resources,resource,options);if(bitmap!=null)CACHE.put(resource,bitmap);MAIN.post(()->{if(isAttachedToWindow()&&generation==request&&bitmap!=null)setImageBitmap(bitmap);});});}
    @Override protected void onDetachedFromWindow(){generation++;super.onDetachedFromWindow();}
}
