package org.sleepwell.app;

import android.app.Activity;
import android.content.Context;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.concurrent.*;

/** Same saved projection and RemoteViews as the launcher. Preview actions are inert. */
final class NightDialWidgetPreview extends LinearLayout implements AutoCloseable {
    private final Activity owner;private final Records.Factory records;private final HomeSurfaces.Reader reader;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();private final Handler main=new Handler(Looper.getMainLooper());
    private boolean closed,started;
    NightDialWidgetPreview(Activity owner){super(owner);this.owner=owner;records=Records.factory;reader=HomeSurfaces.reader;setOrientation(VERTICAL);Design.text(this,"Widget preview",13,500,Design.MUTED);Design.space(this,10);Design.text(this,"Reading your saved summary…",14,400,Design.MUTED);}
    @Override protected void onAttachedToWindow(){super.onAttachedToWindow();if(started||closed)return;started=true;Context app=owner.getApplicationContext();worker.execute(()->{HomeWidgetSummary result;try{result=reader.load(app,records);}catch(RuntimeException unavailable){result=HomeWidgetSummary.error();}HomeWidgetSummary data=result;main.post(()->{if(closed||owner.isDestroyed()||!isAttachedToWindow())return;removeAllViews();Design.text(this,"Widget preview · saved data",13,500,Design.MUTED);Design.space(this,10);try{View widget=HomeWidgetViews.render(owner,data).apply(owner,this);inert(widget);addView(widget,new LayoutParams(-1,Design.dp(owner,Math.round(300*Math.max(1,Design.fontScale(owner))))));setContentDescription("Widget preview ready");}catch(RuntimeException unavailable){Design.text(this,"Preview unavailable. You can still add the widget from your launcher.",14,400,Design.MUTED);}});});}
    private void inert(View view){view.setOnClickListener(null);view.setClickable(false);view.setLongClickable(false);view.setFocusable(false);if(view instanceof ViewGroup group)for(int n=0;n<group.getChildCount();n++)inert(group.getChildAt(n));}
    @Override public void close(){closed=true;main.removeCallbacksAndMessages(null);worker.shutdownNow();}
}
