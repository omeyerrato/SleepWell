package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.View;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.*;
import androidx.test.uiautomator.*;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;

/** Real editor/storage flow, with no scheduler, playback, camera, brightness or alarm Service. */
public class AlarmWakeSettingsTest {
    private final Instrumentation i=InstrumentationRegistry.getInstrumentation();private final Context c=i.getTargetContext();private final UiDevice device=UiDevice.getInstance(i);
    private UiObject2 object(String description)throws Exception{UiObject2 value=device.findObject(By.desc(description));if(value==null){new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().description(description));value=device.wait(Until.findObject(By.desc(description)),5000);}assertNotNull(description,value);return value;}
    private void click(String text)throws Exception{UiObject2 value=device.wait(Until.findObject(By.text(text)),1000);if(value==null)value=device.wait(Until.findObject(By.desc(text)),2000);if(value==null){new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().text(text));value=device.wait(Until.findObject(By.text(text)),5000);}assertNotNull(text,value);value.click();device.waitForIdle();}
    private void capture(String name)throws Exception{CountDownLatch frame=new CountDownLatch(1);i.runOnMainSync(()->{for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)){View decor=a.getWindow().getDecorView();decor.getViewTreeObserver().registerFrameCommitCallback(frame::countDown);decor.invalidate();}});assertTrue(frame.await(5,TimeUnit.SECONDS));device.waitForIdle();File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());assertTrue(device.takeScreenshot(new File(folder,"wake-settings-"+name+".png")));}
    private void launch(Alarm alarm,String route){c.startActivity(new Intent(c,AlarmEditorActivity.class).putExtra("alarm",alarm.json().toString()).putExtra("route",route).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));assertTrue(device.wait(Until.hasObject(By.text(route.equals("gentle")?"Gentle wake-up":"Alarm sound")),5000));}
    @Test public void actualDraftRecreationSaveReopenAndCancelWithoutOutputs()throws Exception{
        assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertEquals("1",device.executeShellCommand("getprop ro.kernel.qemu").trim());assertFalse(Prefs.flag(c,"ringing"));assertFalse(AlarmService.active);assertFalse(TrackingService.running);
        String database="sleepwell-wake-settings-"+UUID.randomUUID()+".db";Map<String,?> personal=new HashMap<>(Prefs.of(c).getAll());Records.Factory priorRecords=Records.factory;AlarmScheduling.Port priorScheduling=AlarmScheduling.port;List<Activity> owned=new CopyOnWriteArrayList<>();int[] scheduled={0};
        Application app=(Application)c.getApplicationContext();Application.ActivityLifecycleCallbacks lifecycle=new Application.ActivityLifecycleCallbacks(){public void onActivityCreated(Activity a,Bundle state){owned.add(a);assertFalse(a instanceof AlarmActivity);}public void onActivityStarted(Activity a){}public void onActivityResumed(Activity a){}public void onActivityPaused(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,Bundle state){}public void onActivityDestroyed(Activity a){}};app.registerActivityLifecycleCallbacks(lifecycle);
        try{
            Records.factory=context->new Store(context,database);AlarmScheduling.port=new AlarmScheduling.Port(){public boolean allowed(Context context){return true;}public void schedule(Context context,Alarm alarm){assertFalse("Only disabled fixture may save",alarm.enabled);scheduled[0]++;}};
            Alarm fixture=new Alarm(1);fixture.volume=80;fixture.rampSeconds=120;fixture.label="Wake-up settings";fixture.vibrate=true;try(Store store=new Store(c,database)){store.save(fixture);}
            launch(fixture,"sound");capture("sound");assertFalse(object("Extra loud").isChecked());object("Extra loud").click();click("Gentle wake-up");capture("loud-curve");
            object("Sound delay").click();assertTrue("Duration choice must be visible before selecting",device.wait(Until.hasObject(By.text("3 min")),5000));capture("delay-options");click("3 min");object("Vibration").click();object("Screen sunrise").click();object("Flashlight wake-up").click();capture("lights");
            final Activity[] current={null};i.runOnMainSync(()->{for(Activity activity:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))current[0]=activity;});Instrumentation.ActivityMonitor monitor=i.addMonitor(AlarmEditorActivity.class.getName(),null,false);try{i.runOnMainSync(current[0]::recreate);assertNotNull(monitor.waitForActivityWithTimeout(5000));}finally{i.removeMonitor(monitor);}
            assertTrue(object("Screen sunrise").isChecked());assertTrue(object("Flashlight wake-up").isChecked());assertFalse(object("Vibration").isChecked());capture("recreated");click("Save changes");click("Use Daybreak");click("Save alarm");
            Alarm saved;try(Store store=new Store(c,database)){saved=store.alarm(1);}assertNotNull(saved);assertTrue(saved.extraLoud);assertTrue(saved.sunrise);assertTrue(saved.flashlight);assertFalse(saved.vibrate);assertEquals(180,saved.soundDelaySeconds);assertEquals(120,saved.rampSeconds);assertEquals(80,saved.volume);assertEquals(1,scheduled[0]);
            launch(saved,"gentle");capture("reopened");click("Save changes");assertTrue(object("Extra loud").isChecked());object("Extra loud").click();click("Gentle wake-up");capture("normal-curve");object("Volume ramp").click();click("1 min");capture("edited-curve");object("Volume ramp").click();click("Off");capture("immediate");device.pressBack();assertTrue(device.wait(Until.hasObject(By.text("Alarm sound")),5000));device.pressBack();assertTrue(device.wait(Until.hasObject(By.text("Edit alarm")),5000));device.pressBack();assertFalse(device.wait(Until.hasObject(By.text("Edit alarm")),1000));
            try(Store store=new Store(c,database)){assertEquals(saved.json().toString(),store.alarm(1).json().toString());}assertEquals(1,scheduled[0]);assertEquals(personal,Prefs.of(c).getAll());assertFalse(Prefs.flag(c,"ringing"));assertFalse(AlarmService.active);assertFalse(TrackingService.running);
        }catch(Exception|AssertionError failure){try{capture("failure");device.dumpWindowHierarchy(new File(c.getExternalFilesDir(null),"verification/wake-settings-failure.xml"));}catch(Exception diagnostic){failure.addSuppressed(diagnostic);}throw failure;}finally{
            try{i.runOnMainSync(()->{for(Activity a:owned)if(!a.isDestroyed())a.finish();});i.waitForIdleSync();long until=SystemClock.elapsedRealtime()+5000;while(owned.stream().anyMatch(a->!a.isDestroyed())&&SystemClock.elapsedRealtime()<until)SystemClock.sleep(50);assertTrue(owned.stream().allMatch(Activity::isDestroyed));}
            finally{app.unregisterActivityLifecycleCallbacks(lifecycle);Records.factory=priorRecords;AlarmScheduling.port=priorScheduling;if(owned.stream().allMatch(Activity::isDestroyed))c.deleteDatabase(database);}
        }
    }
}
