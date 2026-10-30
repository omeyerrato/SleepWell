package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.*;
import java.io.File;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.LongSupplier;
import org.junit.Test;
import static org.junit.Assert.*;

/** A narrow design review, not full feature acceptance. Disposable CI emulator only, silent. */
public final class NightDialReviewTest {
    private final Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
    private final Context context=instrumentation.getTargetContext();
    private final UiDevice device=UiDevice.getInstance(instrumentation);
    private MainActivity main;
    private void capture(String name)throws Exception {
        device.waitForIdle();CountDownLatch frame=new CountDownLatch(1);boolean[] owner={false};
        instrumentation.runOnMainSync(()->{for(View view:android.view.inspector.WindowInspector.getGlobalWindowViews())if(view.hasWindowFocus()&&view.isAttachedToWindow()&&view.isHardwareAccelerated()){owner[0]=true;view.getViewTreeObserver().registerFrameCommitCallback(frame::countDown);view.invalidate();break;}});
        assertTrue("A focused native window must own the capture",owner[0]);assertTrue("A real frame must commit",frame.await(5,TimeUnit.SECONDS));
        File folder=new File(context.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());assertTrue(device.takeScreenshot(new File(folder,"night-dial-"+name+".png")));
    }
    private void tab(String label){UiObject2 item=device.wait(Until.findObject(By.desc(label)),5000);assertNotNull(label,item);item.click();assertTrue(device.wait(Until.hasObject(By.desc(label).selected(true)),5000));}
    private void click(String label)throws Exception {UiObject2 item=device.findObject(By.text(label));if(item==null){new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().text(label));item=device.findObject(By.text(label));}assertNotNull(label,item);item.click();device.waitForIdle();}
    @Test public void rootsMixerAndOwnedPickerRemainSilentAndCancelable()throws Exception {
        assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertEquals("1",device.executeShellCommand("getprop ro.kernel.qemu").trim());
        assertFalse(AlarmService.active);assertFalse(TrackingService.running);assertNull(SleepSoundService.snapshot());
        String fixture="night-dial-"+UUID.randomUUID();String database=fixture+".db";SharedPreferences plan=context.getSharedPreferences(fixture,Context.MODE_PRIVATE);assertTrue(plan.edit().putInt("bedtime",1350).putInt("wakeTime",420).putInt("goal",510).commit());Map<String,?> savedPlan=new HashMap<>(plan.getAll());Map<String,?> original=new HashMap<>(Prefs.of(context).getAll());
        Records.Factory records=Records.factory;SleepPlans.Factory plans=SleepPlans.factory;LongSupplier clock=TonightScreen.clock;TimeZone zone=TimeZone.getDefault();List<Activity> activities=new CopyOnWriteArrayList<>();Application application=(Application)context.getApplicationContext();
        Application.ActivityLifecycleCallbacks lifecycle=new Application.ActivityLifecycleCallbacks(){public void onActivityCreated(Activity activity,Bundle state){activities.add(activity);}public void onActivityStarted(Activity activity){}public void onActivityResumed(Activity activity){}public void onActivityPaused(Activity activity){}public void onActivityStopped(Activity activity){}public void onActivitySaveInstanceState(Activity activity,Bundle state){}public void onActivityDestroyed(Activity activity){}};
        application.registerActivityLifecycleCallbacks(lifecycle);
        try {
            Records.factory=c->new Store(c,database);SleepPlans.factory=c->plan;TimeZone.setDefault(TimeZone.getTimeZone("Asia/Riyadh"));TonightScreen.clock=()->ZonedDateTime.of(2026,10,30,22,14,0,0,ZoneId.of("Asia/Riyadh")).toInstant().toEpochMilli();Appearance.save(context,false,0,0,false,false);
            try(Store store=new Store(context,database)){Alarm alarm=new Alarm(1);alarm.label="Daybreak";alarm.hour=7;alarm.minute=0;alarm.enabled=false;alarm.days.addAll(Set.of(1,2,3,4,5,6,7));store.save(alarm);}
            Instrumentation.ActivityMonitor monitor=instrumentation.addMonitor(MainActivity.class.getName(),null,false);try{context.startActivity(new Intent(context,MainActivity.class).putExtra("visualReview",true).putExtra("verificationLabel","Design review · synthetic saved plan").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));main=(MainActivity)monitor.waitForActivityWithTimeout(5000);assertNotNull(main);}finally{instrumentation.removeMonitor(monitor);}
            assertTrue(device.wait(Until.hasObject(By.desc("Begin night")),5000));assertTrue(device.hasObject(By.descStartsWith("Rest window from ")));capture("tonight");
            UiObject2 dial=device.findObject(By.descStartsWith("Rest window from "));assertTrue("Dial must be a substantial circle",Math.abs(dial.getVisibleBounds().width()-dial.getVisibleBounds().height())<Design.dp(context,30));dial.click();assertTrue(device.wait(Until.hasObject(By.text("Your rest window")),5000));capture("dial-help");device.findObject(By.desc("Close rest window explanation")).click();
            tab("Alarms");capture("alarms");tab("Settings");capture("settings");tab("Tonight");
            UiObject2 sounds=device.findObject(By.descStartsWith("Open sleep sounds,"));if(sounds==null){new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().descriptionStartsWith("Open sleep sounds,"));sounds=device.findObject(By.descStartsWith("Open sleep sounds,"));}assertNotNull(sounds);sounds.click();assertTrue(device.wait(Until.hasObject(By.text("Sleep sounds")),5000));capture("sounds");assertNull("Opening the mixer is silent",SleepSoundService.snapshot());device.pressBack();
            click("Bedtime goal");assertTrue(device.wait(Until.hasObject(By.text("Sleep goals")),5000));UiObject2 time=device.wait(Until.findObject(By.desc("Change planned bedtime")),5000);assertNotNull(time);time.click();assertTrue(device.wait(Until.hasObject(By.desc("Hour")),5000));capture("time");
            device.findObject(By.desc("Hour")).setText("99");click("Use time");assertTrue(device.wait(Until.hasObject(By.textStartsWith("Choose hour")),3000));capture("time-invalid");click("Cancel");assertEquals("Cancel must leave the saved plan untouched",savedPlan,plan.getAll());
            device.findObject(By.desc("Change planned bedtime")).click();device.findObject(By.desc("Hour")).setText("09");device.findObject(By.desc("Minute")).setText("15");
            PlanActivity editor=null;for(Activity activity:activities)if(activity instanceof PlanActivity&&!activity.isDestroyed())editor=(PlanActivity)activity;assertNotNull(editor);PlanActivity prior=editor;monitor=instrumentation.addMonitor(PlanActivity.class.getName(),null,false);try{instrumentation.runOnMainSync(prior::recreate);assertNotNull(monitor.waitForActivityWithTimeout(5000));}finally{instrumentation.removeMonitor(monitor);}assertTrue(device.wait(Until.hasObject(By.desc("Hour")),5000));assertEquals("09",device.findObject(By.desc("Hour")).getText());assertEquals("15",device.findObject(By.desc("Minute")).getText());capture("time-restored");click("Cancel");device.pressBack();
            assertEquals(savedPlan,plan.getAll());assertFalse(TrackingService.running);assertFalse(AlarmService.active);assertNull(SleepSoundService.snapshot());
            try(Store store=new Store(context,database)){assertFalse(store.alarm(1).enabled);assertEquals(0,store.backup().getJSONArray("sessions").length());}
        } catch(Exception|AssertionError failure){try{capture("failure");File folder=new File(context.getExternalFilesDir(null),"verification");device.dumpWindowHierarchy(new File(folder,"night-dial-failure.xml"));}catch(Exception ignored){}throw failure;}
        finally {
            instrumentation.runOnMainSync(()->activities.forEach(activity->{if(!activity.isDestroyed())activity.finish();}));instrumentation.waitForIdleSync();long end=SystemClock.elapsedRealtime()+5000;while(activities.stream().anyMatch(activity->!activity.isDestroyed())&&SystemClock.elapsedRealtime()<end)SystemClock.sleep(50);
            application.unregisterActivityLifecycleCallbacks(lifecycle);Records.factory=records;SleepPlans.factory=plans;TonightScreen.clock=clock;TimeZone.setDefault(zone);
            SharedPreferences.Editor restore=Prefs.of(context).edit();for(String key:new String[]{Appearance.AUTO,Appearance.THEME,Appearance.SIZE,Appearance.MOTION,Appearance.CONTRAST}){Object value=original.get(key);if(value instanceof Boolean)restore.putBoolean(key,(Boolean)value);else if(value instanceof Integer)restore.putInt(key,(Integer)value);else restore.remove(key);}assertTrue(restore.commit());assertEquals(original,Prefs.of(context).getAll());
            if(activities.stream().allMatch(Activity::isDestroyed)){context.deleteDatabase(database);context.deleteSharedPreferences(fixture);}
        }
    }
}
