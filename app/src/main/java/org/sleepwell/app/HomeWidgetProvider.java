package org.sleepwell.app;

import android.appwidget.*;
import android.content.*;
import android.os.*;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public final class HomeWidgetProvider extends AppWidgetProvider {
    static final String REFRESH="org.sleepwell.app.REFRESH_HOME_WIDGET";
    private static final Set<String> EVENTS=Set.of(REFRESH,AppWidgetManager.ACTION_APPWIDGET_UPDATE,AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED,AppWidgetManager.ACTION_APPWIDGET_ENABLED,Intent.ACTION_BOOT_COMPLETED,Intent.ACTION_TIME_CHANGED,Intent.ACTION_TIMEZONE_CHANGED,Intent.ACTION_DATE_CHANGED,Intent.ACTION_MY_PACKAGE_REPLACED);
    @Override public void onReceive(Context context,Intent intent){if(intent==null||intent.getAction()==null||!EVENTS.contains(intent.getAction()))return;PendingResult pending=goAsync();AtomicBoolean finished=new AtomicBoolean();Runnable finish=()->{if(finished.compareAndSet(false,true))pending.finish();};new Handler(Looper.getMainLooper()).postDelayed(finish,8000);HomeSurfaces.request(context,finish);}
}
