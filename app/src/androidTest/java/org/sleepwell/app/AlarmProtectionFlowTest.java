package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.*;
import androidx.test.uiautomator.*;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/** Isolated configuration only: never launches AlarmActivity/Service, changes access, or opens a power menu. */
public class AlarmProtectionFlowTest {
    private final Instrumentation i=InstrumentationRegistry.getInstrumentation();private final Context c=i.getTargetContext();private final UiDevice device=UiDevice.getInstance(i);
    private UiObject2 object(String description){UiObject2 value=device.wait(Until.findObject(By.desc(description)),5000);assertNotNull(description,value);return value;}
    private void click(String text)throws Exception{UiObject2 value=device.findObject(By.text(text));if(value==null){new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().text(text));value=device.wait(Until.findObject(By.text(text)),5000);}assertNotNull(text,value);value.click();device.waitForIdle();}
    private void title(String text){assertTrue(text,device.wait(Until.hasObject(By.text(text)),5000));}
    private void later(){UiObject2 button=device.wait(Until.findObject(By.text(Pattern.compile("later",Pattern.CASE_INSENSITIVE))),5000);assertNotNull(button);button.click();device.waitForIdle();}
    private void capture(String name)throws Exception{CountDownLatch frame=new CountDownLatch(1);i.runOnMainSync(()->{for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)){View decor=a.getWindow().getDecorView();decor.getViewTreeObserver().registerFrameCommitCallback(frame::countDown);decor.invalidate();}});assertTrue(frame.await(5,TimeUnit.SECONDS));device.waitForIdle();File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());assertTrue(device.takeScreenshot(new File(folder,"protection-"+name+".png")));}
    @Test public void compactListAndGlobalProtectionsPersistWithoutAlarmExecution()throws Exception{
        assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertEquals("1",device.executeShellCommand("getprop ro.kernel.qemu").trim());assertFalse(Prefs.flag(c,"ringing"));assertFalse(AlarmService.active);assertFalse(TrackingService.running);assertFalse(AlarmProtectionActivity.access(c));
        String suffix=UUID.randomUUID().toString(),database="sleepwell-protection-"+suffix+".db",preferences="sleepwell-protection-"+suffix;
        SharedPreferences fixture=c.getSharedPreferences(preferences,Context.MODE_PRIVATE);Map<String,?> personal=new HashMap<>(Prefs.of(c).getAll());Records.Factory priorRecords=Records.factory;AlarmProtections.Factory priorSettings=AlarmProtections.factory;
        List<Activity> owned=new CopyOnWriteArrayList<>();Application app=(Application)c.getApplicationContext();Application.ActivityLifecycleCallbacks lifecycle=new Application.ActivityLifecycleCallbacks(){public void onActivityCreated(Activity a,Bundle state){owned.add(a);assertFalse("An alarm must never be launched",a instanceof AlarmActivity);}public void onActivityStarted(Activity a){}public void onActivityResumed(Activity a){}public void onActivityPaused(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,Bundle state){}public void onActivityDestroyed(Activity a){}};
        app.registerActivityLifecycleCallbacks(lifecycle);
        try{
            Records.factory=context->new Store(context,database);AlarmProtections.factory=context->fixture;
            String saved;try(Store store=new Store(c,database)){Alarm first=new Alarm(1);first.hour=6;first.minute=30;first.label="Workdays";first.days.addAll(Set.of(1,2,3,4,5));first.enabled=true;store.save(first);Alarm second=new Alarm(2);second.hour=8;second.minute=30;second.label="Weekends";second.days.addAll(Set.of(6,7));second.enabled=true;store.save(second);saved=store.rows("SELECT * FROM alarms ORDER BY id").toString();}
            // Enabled rows are display-only fixtures. No scheduler method or toggle is invoked.
            c.startActivity(new Intent(c,MainActivity.class).putExtra("destination",1).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));object("Edit Workdays");assertTrue(object("Alarms").isSelected());assertFalse(device.hasObject(By.text("Smart wake-up")));assertFalse(device.hasObject(By.text("Challenges")));capture("alarms");
            object("Edit Workdays").click();title("Edit alarm");UiObject2 alarmOn=object("Alarm on");assertTrue(alarmOn.isChecked());assertTrue("Alarm on retains its 48dp target",alarmOn.getVisibleBounds().height()>=Math.round(48*c.getResources().getDisplayMetrics().density));assertTrue("Alarm on stays visible above Save",alarmOn.getVisibleBounds().bottom<=device.findObject(By.text("Save alarm")).getVisibleBounds().top);capture("editor");click("Alarm protection");title("Applies to all alarms");assertFalse(object("Prevent leaving app").isChecked());assertFalse(object("Prevent power off").isChecked());assertTrue(fixture.getAll().isEmpty());capture("off");
            object("Prevent leaving app").click();title("Enable the optional guard?");capture("disclosure");later();object("Prevent power off").click();title("Enable the optional guard?");later();assertTrue(object("Prevent power off").isChecked());assertTrue(object("Prevent leaving app").isChecked());capture("on");
            final Activity[] current={null};i.runOnMainSync(()->{for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))current[0]=a;});assertTrue(current[0] instanceof AlarmProtectionActivity);Instrumentation.ActivityMonitor monitor=i.addMonitor(AlarmProtectionActivity.class.getName(),null,false);try{i.runOnMainSync(current[0]::recreate);assertNotNull(monitor.waitForActivityWithTimeout(5000));}finally{i.removeMonitor(monitor);}title("Alarm protection");assertTrue(object("Prevent leaving app").isChecked());assertTrue(object("Prevent power off").isChecked());assertFalse(device.hasObject(By.text("Enable the optional guard?")));capture("recreated");
            click("Done");title("Save alarm");device.pressBack();object("Alarm options").click();title("Alarm options");click("Alarm protection");title("Alarm protection");assertTrue(object("Prevent power off").isChecked());capture("global");
            object("Prevent leaving app").click();assertFalse(object("Prevent leaving app").isChecked());assertTrue(object("Prevent power off").isChecked());capture("power-only");assertFalse(AlarmProtectionActivity.access(c));assertFalse(AlarmService.active);assertFalse(Prefs.flag(c,"ringing"));assertEquals(personal,Prefs.of(c).getAll());try(Store store=new Store(c,database)){assertEquals(saved,store.rows("SELECT * FROM alarms ORDER BY id").toString());}
        }finally{
            try{i.runOnMainSync(()->{for(Activity a:owned)if(!a.isDestroyed())a.finish();});i.waitForIdleSync();long until=SystemClock.elapsedRealtime()+5000;while(owned.stream().anyMatch(a->!a.isDestroyed())&&SystemClock.elapsedRealtime()<until)SystemClock.sleep(50);assertTrue("All owned Activities close before removing fixtures",owned.stream().allMatch(Activity::isDestroyed));}
            finally{app.unregisterActivityLifecycleCallbacks(lifecycle);Records.factory=priorRecords;AlarmProtections.factory=priorSettings;if(owned.stream().allMatch(Activity::isDestroyed)){c.deleteDatabase(database);c.deleteSharedPreferences(preferences);}}
        }
    }
}
