package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.graphics.Rect;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import org.junit.Test;
import java.io.File;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

/** Real settings/save/reopen with private preferences and fake provider/application. */
public final class CalendarRulesFlowTest {
    final Instrumentation i=InstrumentationRegistry.getInstrumentation();final Context c=i.getTargetContext();final UiDevice device=UiDevice.getInstance(i);CalendarRulesActivity activity;
    void await(java.util.function.BooleanSupplier yes)throws Exception{long end=SystemClock.elapsedRealtime()+8000;while(!yes.getAsBoolean()&&SystemClock.elapsedRealtime()<end)Thread.sleep(20);assertTrue("Calendar state ready",yes.getAsBoolean());}
    View find(View root,String text){if(root instanceof Switch&&text.contentEquals(root.getContentDescription()))return root;if(root instanceof TextView&&text.contentEquals(((TextView)root).getText()))return root;if(root instanceof ViewGroup group)for(int n=0;n<group.getChildCount();n++){View value=find(group.getChildAt(n),text);if(value!=null)return value;}return null;}
    View toggle(View root,String text){if(root instanceof Switch&&text.contentEquals(root.getContentDescription()))return root;if(root instanceof ViewGroup group)for(int n=0;n<group.getChildCount();n++){View value=toggle(group.getChildAt(n),text);if(value!=null)return value;}return null;}
    boolean shown(String text){boolean[] ready={false};i.runOnMainSync(()->ready[0]=activity!=null&&activity.hasWindowFocus()&&find(activity.getWindow().getDecorView(),text)!=null);return ready[0];}
    void launch()throws Exception{Instrumentation.ActivityMonitor monitor=i.addMonitor(CalendarRulesActivity.class.getName(),null,false);try{c.startActivity(new Intent(c,CalendarRulesActivity.class).putExtra("visualReview",true).putExtra("verificationLabel","Private calendar fixture · no alarms").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));activity=(CalendarRulesActivity)monitor.waitForActivityWithTimeout(5000);assertNotNull(activity);}finally{i.removeMonitor(monitor);}await(()->shown("Earlier for meetings"));}
    void click(String text)throws Exception{await(()->shown(text));Rect[] bounds={null};i.runOnMainSync(()->{View root=activity.getWindow().getDecorView(),view=toggle(root,text);if(view==null)view=find(root,text);assertTrue(view.isEnabled());while(!view.isClickable()&&view.getParent() instanceof View)view=(View)view.getParent();assertTrue(view.isClickable());view.requestRectangleOnScreen(new Rect(0,0,view.getWidth(),view.getHeight()),true);Rect r=new Rect();assertTrue(view.getGlobalVisibleRect(r));assertEquals(view.getHeight(),r.height());bounds[0]=r;});assertTrue(device.click(bounds[0].centerX(),bounds[0].centerY()));}
    void capture(String name)throws Exception{await(()->shown("Save calendar rules"));i.waitForIdleSync();File dir=new File(c.getExternalFilesDir(null),"verification");assertTrue(dir.isDirectory()||dir.mkdirs());assertTrue(device.takeScreenshot(new File(dir,"calendar-rules-"+name+".png")));}
    void close()throws Exception{CalendarRulesActivity old=activity;i.runOnMainSync(old::finish);await(old::isDestroyed);assertTrue(old.awaitWorkers(8000));}
    @Test public void calendarChoicesSaveAndReopenWithoutProviderOrAlarmEffects()throws Exception{
        assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertEquals("1",device.executeShellCommand("getprop ro.kernel.qemu").trim());assertFalse(AlarmService.active);assertFalse(TrackingService.running);Map<String,?> before=new HashMap<>(Prefs.of(c).getAll());String name="calendar-ui-"+UUID.randomUUID();SharedPreferences privatePrefs=c.getSharedPreferences(name,0);java.util.function.Function<Context,SharedPreferences> oldPrefs=CalendarSettings.factory;CalendarAccess.Factory oldAccess=CalendarAccess.factory;CalendarSync.Port oldApply=CalendarSync.port;AtomicInteger applied=new AtomicInteger();
        try{
            CalendarSettings.factory=context->privatePrefs;CalendarAccess.factory=context->new CalendarAccess.Port(){public boolean allowed(){return true;}public List<CalendarAccess.Source> sources(CancellationSignal signal){assertNotEquals(Looper.getMainLooper(),Looper.myLooper());return List.of(new CalendarAccess.Source(7,"Private calendar"));}public CalendarPolicy.Snapshot snapshot(CalendarPolicy.Rules rules,long now,ZoneId zone,CancellationSignal cancel){throw new AssertionError("Fixture never queries real events");}};
            CalendarSync.port=context->{assertNotEquals(Looper.getMainLooper(),Looper.myLooper());CalendarPolicy.Rules saved=CalendarSettings.load(context);assertTrue(saved.enabled);assertEquals(Set.of(7L),saved.holidays);assertEquals(Set.of(7L),saved.meetings);applied.incrementAndGet();};
            launch();capture("initial");click("Use calendar rules");click("Skip all-day dates");click("Earlier for meetings");assertTrue(privatePrefs.getAll().isEmpty());capture("draft");click("Save calendar rules");await(()->shown("Calendar rules saved."));assertEquals(1,applied.get());capture("saved");close();launch();assertTrue(CalendarSettings.load(c).enabled);assertEquals(Set.of(7L),CalendarSettings.load(c).meetings);capture("reopened");
        }finally{if(activity!=null&&!activity.isDestroyed())close();CalendarSettings.factory=oldPrefs;CalendarAccess.factory=oldAccess;CalendarSync.port=oldApply;assertTrue(c.deleteSharedPreferences(name));assertEquals(before,Prefs.of(c).getAll());assertFalse(AlarmService.active);assertFalse(TrackingService.running);}
    }
}
