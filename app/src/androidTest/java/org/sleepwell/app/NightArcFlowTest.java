package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;
import static org.junit.Assert.*;

/** CI-only synthetic clock/plan, no OS clock change, scheduling, audio or tracking. */
@RunWith(AndroidJUnit4.class)
public class NightArcFlowTest {
    private final Instrumentation i=InstrumentationRegistry.getInstrumentation();
    private final Context c=i.getTargetContext();private final UiDevice device=UiDevice.getInstance(i);
    private MainActivity activity;
    private long local(int day,int hour,int minute){return LocalDate.of(2026,10,day).atTime(hour,minute).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();}
    private void launch(){Instrumentation.ActivityMonitor monitor=i.addMonitor(MainActivity.class.getName(),null,false);try{c.startActivity(new Intent(c,MainActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));activity=(MainActivity)monitor.waitForActivityWithTimeout(5000);assertNotNull(activity);i.waitForIdleSync();device.waitForIdle();}finally{i.removeMonitor(monitor);}}
    private NightArc arc(View view){if(view instanceof NightArc)return (NightArc)view;if(view instanceof ViewGroup)for(int n=0;n<((ViewGroup)view).getChildCount();n++){NightArc found=arc(((ViewGroup)view).getChildAt(n));if(found!=null)return found;}return null;}
    private View described(View root,String description){if(description.contentEquals(root.getContentDescription()==null?"":root.getContentDescription()))return root;if(root instanceof ViewGroup group)for(int n=0;n<group.getChildCount();n++){View found=described(group.getChildAt(n),description);if(found!=null)return found;}return null;}
    private void state(String part){assertTrue(part,device.wait(Until.hasObject(By.descContains(part)),5000));}
    private void capture(String name)throws Exception{CountDownLatch frame=new CountDownLatch(1);i.runOnMainSync(()->{View decor=activity.getWindow().getDecorView();assertTrue(decor.isHardwareAccelerated());decor.getViewTreeObserver().registerFrameCommitCallback(frame::countDown);decor.invalidate();});assertTrue(frame.await(5,TimeUnit.SECONDS));device.waitForIdle();File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());assertTrue(device.takeScreenshot(new File(folder,"arc-"+name+".png")));}
    @Test public void clockProgressAndExplanationWithoutAnyAlarmDelivery()throws Exception{
        assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertEquals("1",device.executeShellCommand("getprop ro.kernel.qemu").trim());
        assertFalse(Prefs.flag(c,"ringing"));assertFalse(TrackingService.running);assertFalse(Prefs.flag(c,"tracking"));assertNull(SleepSoundService.snapshot());
        Map<String,?> personal=new HashMap<>(Prefs.of(c).getAll());String suffix=UUID.randomUUID().toString(),database="sleepwell-arc-"+suffix+".db",preferences="sleepwell-arc-"+suffix;
        SharedPreferences fixture=c.getSharedPreferences(preferences,Context.MODE_PRIVATE);assertTrue(fixture.edit().putInt("bedtime",1350).putInt("wakeTime",390).putInt("goal",480).commit());Map<String,?> saved=new HashMap<>(fixture.getAll());
        Records.Factory priorRecords=Records.factory;SleepPlans.Factory priorPlans=SleepPlans.factory;LongSupplier priorClock=TonightScreen.clock;AtomicLong now=new AtomicLong(local(22,22,0));
        List<Activity> owned=new CopyOnWriteArrayList<>();Application app=(Application)c.getApplicationContext();Application.ActivityLifecycleCallbacks lifecycle=new Application.ActivityLifecycleCallbacks(){public void onActivityCreated(Activity a,Bundle b){owned.add(a);}public void onActivityStarted(Activity a){}public void onActivityResumed(Activity a){}public void onActivityPaused(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,Bundle b){}public void onActivityDestroyed(Activity a){}};app.registerActivityLifecycleCallbacks(lifecycle);
        try{
            Records.factory=context->new Store(context,database);SleepPlans.factory=context->fixture;TonightScreen.clock=now::get;Appearance.save(c,false,0,0,true,false);
            launch();state("Bedtime in 30 min");
            UiObject2 clockAction=device.findObject(By.desc("Alarms & challenges"));assertNotNull(clockAction);assertEquals(android.widget.Button.class.getName(),clockAction.getClassName());
            i.runOnMainSync(()->{NightArc rest=arc(activity.getWindow().getDecorView());assertNotNull(rest);android.view.accessibility.AccessibilityNodeInfo node=rest.createAccessibilityNodeInfo();try{assertEquals(android.widget.Button.class.getName(),node.getClassName().toString());assertTrue(node.isClickable());assertTrue(node.isFocusable());}finally{node.recycle();}View toggle=described(activity.getWindow().getDecorView(),"Tonight alarm enabled");assertTrue("Tonight switch uses gesture-only shared feedback",toggle instanceof InteractionFeedback.Toggle);});capture("upcoming");
            device.findObject(By.descStartsWith("Rest window from ")).click();assertTrue(device.wait(Until.hasObject(By.text("Your rest window")),5000));capture("help");UiObject2 close=device.wait(Until.findObject(By.desc("Close rest window explanation")),5000);assertNotNull(close);close.click();assertTrue(device.wait(Until.gone(By.text("Your rest window")),5000));device.waitForIdle();
            NightArc[] original={null};i.runOnMainSync(()->original[0]=arc(activity.getWindow().getDecorView()));assertNotNull(original[0]);now.set(local(22,22,30));
            // Observe the real foreground Handler tick, even with manual appearance.
            assertTrue("The existing arc must advance on the production 30-second tick",device.wait(Until.hasObject(By.descContains(". 0 percent of clock time elapsed.")),35000));i.runOnMainSync(()->assertSame(original[0],arc(activity.getWindow().getDecorView())));capture("start");
            now.set(local(23,2,30));i.runOnMainSync(activity::refreshTonightClock);state(". 50 percent of clock time elapsed.");capture("middle");
            now.set(local(23,6,30));i.runOnMainSync(activity::refreshTonightClock);state(". 100 percent of clock time elapsed.");capture("end");
            now.set(local(23,6,31));i.runOnMainSync(activity::refreshTonightClock);state("Bedtime in 15 h 59 min");assertFalse(device.hasObject(By.descContains("percent of clock time elapsed")));capture("next");
            Appearance.save(c,false,0,2,true,false);now.set(local(23,2,30));launch();state(". 50 percent of clock time elapsed.");capture("large");new UiScrollable(new UiSelector().scrollable(true)).scrollToEnd(6);device.waitForIdle();UiObject2 action=device.findObject(By.desc("Start sleep tracking")),dock=device.findObject(By.desc("Tonight").selected(true));assertNotNull(action);assertNotNull(dock);assertEquals(android.widget.Button.class.getName(),action.getClassName());assertTrue("Large text action must be fully reachable above navigation without activating it",action.getVisibleBounds().height()>=Design.dp(c,48)&&action.getVisibleBounds().bottom<=dock.getVisibleBounds().top);capture("large-action");
            Appearance.save(c,false,0,0,true,false);try(Store store=new Store(c,database)){Alarm alarm=new Alarm(1);alarm.hour=6;alarm.minute=30;alarm.enabled=true;alarm.days.add(7);store.save(alarm);}now.set(local(22,22,0));launch();state("Bedtime in 48 h 30 min");assertTrue(device.hasObject(By.textContains("SUN 25 OCT")));capture("weekly");
            assertEquals("Clock refresh must not save the isolated plan",saved,fixture.getAll());assertFalse(Prefs.flag(c,"ringing"));assertFalse(TrackingService.running);assertNull(SleepSoundService.snapshot());
        }finally{
            try{i.runOnMainSync(()->{for(Activity a:owned)if(!a.isDestroyed())a.finish();});i.waitForIdleSync();long deadline=SystemClock.elapsedRealtime()+5000;while(owned.stream().anyMatch(a->!a.isDestroyed())&&SystemClock.elapsedRealtime()<deadline)SystemClock.sleep(50);assertTrue("Owned Activities must close before restoring factories",owned.stream().allMatch(Activity::isDestroyed));}
            finally{app.unregisterActivityLifecycleCallbacks(lifecycle);TonightScreen.clock=priorClock;Records.factory=priorRecords;SleepPlans.factory=priorPlans;SharedPreferences.Editor edit=Prefs.of(c).edit();for(String key:new String[]{Appearance.AUTO,Appearance.THEME,Appearance.SIZE,Appearance.MOTION,Appearance.CONTRAST}){Object value=personal.get(key);if(value instanceof Boolean)edit.putBoolean(key,(Boolean)value);else if(value instanceof Integer)edit.putInt(key,(Integer)value);else edit.remove(key);}assertTrue(edit.commit());assertEquals("All personal preferences must be restored exactly",personal,Prefs.of(c).getAll());if(owned.stream().allMatch(Activity::isDestroyed)){c.deleteDatabase(database);c.deleteSharedPreferences(preferences);}}
        }
    }
}
