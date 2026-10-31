package org.sleepwell.app;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.os.Bundle;
import android.widget.LinearLayout;

/** Requests launcher pinning only after an explicit tap; opening never installs a widget. */
public final class HomeWidgetSettingsActivity extends Activity {
    private String message="";private NightDialWidgetPreview preview;
    @Override public void onCreate(Bundle state){super.onCreate(state);RecordUi.review(this);render();}
    private void render(){if(preview!=null)preview.close();LinearLayout body=Design.page(this,false,null,n->{});Design.header(body,"Home screen",this::finish);Design.text(body,"SleepWell widget",21,600,Design.INK);Design.text(body,"Shows your next saved alarm and last sleep record. Taps open screens; they do not start alarms, sounds or tracking.",14,400,Design.MUTED);Design.space(body,16);preview=new NightDialWidgetPreview(this);body.addView(preview,new LinearLayout.LayoutParams(-1,-2));Design.space(body,12);Design.button(body,"Add SleepWell widget",true,()->{try{AppWidgetManager manager=AppWidgetManager.getInstance(this);boolean accepted=manager.isRequestPinAppWidgetSupported()&&manager.requestPinAppWidget(new ComponentName(this,HomeWidgetProvider.class),null,null);message=accepted?"Pin request sent. Confirm it in your launcher.":"Your launcher does not support widget pin requests. Add SleepWell from its widget list.";}catch(RuntimeException failure){message="Widget pinning unavailable. Use your launcher's widget list.";}render();});Design.button(body,"Refresh home widget",false,()->{HomeSurfaces.changed(this);message="Widget refresh requested.";render();});Design.space(body,18);Design.text(body,"App shortcuts",20,600,Design.INK);Design.text(body,"Long-press SleepWell in your launcher, or pin a screen below.",13,400,Design.MUTED);for(HomeLaunch.Screen screen:HomeShortcuts.SCREENS)Design.button(body,"Pin "+HomeShortcuts.label(screen),false,()->{try{message=HomeShortcuts.pin(this,screen)?"Pin request sent. Confirm it in your launcher.":"This launcher does not support pinned shortcuts.";}catch(RuntimeException failure){message="Shortcut pinning is unavailable. Try your launcher's app menu.";}render();});if(!message.isEmpty()){Design.space(body,14);Design.text(body,message,13,500,Design.BROWN);}Design.space(body,14);Design.text(body,"Widget contents are visible on your home screen. Refresh after changing alarms in another process. Launcher and Android update delivery can be delayed.",12,400,Design.MUTED);}
    @Override protected void onDestroy(){if(preview!=null)preview.close();super.onDestroy();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
}
