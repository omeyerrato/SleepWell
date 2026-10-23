package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.View;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.*;
import androidx.test.uiautomator.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import org.junit.Test;
import static org.junit.Assert.*;

/** Emulator-only real settings storage plus deterministic idle evidence; no Service/sensors/outputs. */
public class AutomaticTrackingFlowTest {
    private final Instrumentation i=InstrumentationRegistry.getInstrumentation();
    private final Context c=i.getTargetContext();private final UiDevice device=UiDevice.getInstance(i);
    private AutomaticTrackingActivity activity;
    private UiObject2 object(String description)throws Exception{UiObject2 value=device.findObject(By.desc(description));if(value==null){new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().description(description));value=device.wait(Until.findObject(By.desc(description)),5000);}assertNotNull(description,value);return value;}
    private void click(String text)throws Exception{if(text.equals("Save settings"))new UiScrollable(new UiSelector().scrollable(true)).scrollToEnd(5);UiObject2 object=device.findObject(By.text(text));if(object==null){new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().text(text));object=device.wait(Until.findObject(By.text(text)),5000);}assertNotNull(text,object);assertTrue(text+" must have a visible 48 dp touch target",object.getVisibleBounds().height()>=Design.dp(c,48));object.click();device.waitForIdle();}
    private void capture(String name)throws Exception{CountDownLatch frame=new CountDownLatch(1);i.runOnMainSync(()->{View decor=activity.getWindow().getDecorView();decor.getViewTreeObserver().registerFrameCommitCallback(frame::countDown);decor.invalidate();});assertTrue(frame.await(5,TimeUnit.SECONDS));device.waitForIdle();File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());assertTrue(device.takeScreenshot(new File(folder,"automatic-"+name+".png")));}
    private void launch()throws Exception{AutomaticTrackingActivity prior=activity;Instrumentation.ActivityMonitor monitor=i.addMonitor(AutomaticTrackingActivity.class.getName(),null,false);try{c.startActivity(new Intent(c,AutomaticTrackingActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));Activity created=monitor.waitForActivityWithTimeout(5000);assertNotNull("A new automatic-settings Activity must be created; an old task is not a reopen",created);assertNotSame(prior,created);activity=(AutomaticTrackingActivity)created;}finally{i.removeMonitor(monitor);}long deadline=SystemClock.elapsedRealtime()+5000;boolean[] ready={false};while(!ready[0]&&SystemClock.elapsedRealtime()<deadline){i.runOnMainSync(()->ready[0]=!activity.isDestroyed()&&activity.hasWindowFocus()&&activity.getWindow().getDecorView().isAttachedToWindow());if(!ready[0])SystemClock.sleep(50);}assertTrue("The reopened settings window must gain focus",ready[0]);assertTrue(device.wait(Until.hasObject(By.text("Automatic tracking")),5000));}
    private void destroyed(Activity target){long deadline=SystemClock.elapsedRealtime()+5000;boolean[] closed={false};while(!closed[0]&&SystemClock.elapsedRealtime()<deadline){i.runOnMainSync(()->closed[0]=target.isDestroyed());if(!closed[0])SystemClock.sleep(50);}assertTrue("Saving must destroy the first settings Activity before reopening",closed[0]);}
    @Test public void choicesPersistAndArmingUsesFakeOutputOnly()throws Exception{
        assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertEquals("1",device.executeShellCommand("getprop ro.kernel.qemu").trim());assertFalse(TrackingService.running);assertFalse(AlarmService.active);
        Map<String,?> personal=new HashMap<>(Prefs.of(c).getAll());String name="automatic-test-"+UUID.randomUUID();SharedPreferences prefs=c.getSharedPreferences(name,0);AutomaticTrackingActivity.Port prior=AutomaticTrackingActivity.port;int[] armed={0};List<Activity> owned=new CopyOnWriteArrayList<>();Application app=(Application)c.getApplicationContext();Application.ActivityLifecycleCallbacks lifecycle=new Application.ActivityLifecycleCallbacks(){public void onActivityCreated(Activity a,Bundle b){if(a instanceof AutomaticTrackingActivity)owned.add(a);}public void onActivityStarted(Activity a){}public void onActivityResumed(Activity a){}public void onActivityPaused(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,Bundle b){}public void onActivityDestroyed(Activity a){}};app.registerActivityLifecycleCallbacks(lifecycle);
        try{
            AutomaticTrackingActivity.port=new AutomaticTrackingActivity.Port(){
                public SharedPreferences preferences(Activity a){return prefs;}
                public long schedule(Activity a,AutomaticSleep.Mode mode){if(mode!=AutomaticSleep.Mode.STILLNESS)throw new IllegalArgumentException("Enable an alarm with a smart wake-up window first.");return System.currentTimeMillis();}
                public boolean movementAvailable(Activity a){return true;}
                public boolean notificationsAllowed(Activity a){return true;}
                public boolean microphoneAllowed(Activity a){return true;}
                public void arm(Activity a,AutomaticSleep.Mode mode,long scheduled,boolean recordSounds,boolean keepClips){assertTrue(recordSounds);assertTrue(keepClips);armed[0]++;assertEquals(AutomaticSleep.Mode.STILLNESS,mode);}
            };
            launch();capture("manual");object("After settling down").click();device.waitForIdle();capture("stillness");object("Record sounds").click();device.waitForIdle();object("Keep sound clips").click();capture("sounds");Activity first=activity;click("Save settings");assertEquals("Save must not invoke the Arm action",0,armed[0]);assertEquals("STILLNESS",prefs.getString("automaticMode",""));assertTrue(prefs.getBoolean("automaticRecordSounds",false));assertTrue(prefs.getBoolean("automaticKeepClips",false));destroyed(first);
            launch();assertTrue(object("After settling down").isSelected());assertTrue(object("Record sounds").isChecked());assertTrue(object("Keep sound clips").isChecked());capture("reopened");click("Arm tonight");assertEquals(1,armed[0]);
            object("Before smart window").click();device.waitForIdle();click("Arm tonight");assertTrue(device.wait(Until.hasObject(By.text("Enable an alarm with a smart wake-up window first.")),5000));assertTrue(device.findObject(By.text("Enable an alarm with a smart wake-up window first.")).getVisibleBounds().height()>0);capture("error");assertEquals(1,armed[0]);assertEquals("STILLNESS",prefs.getString("automaticMode",""));
            // The same production decision model gates a fake recording effect on actual sampled evidence.
            int[] recordings={0};AutomaticTransition transition=new AutomaticTransition(new AutomaticSleep(AutomaticSleep.Mode.STILLNESS,0,0),()->recordings[0]++);
            for(long elapsed=0;elapsed<=AutomaticSleep.QUIET_REQUIRED;elapsed+=200)transition.sample(elapsed,.01,false);
            assertEquals(0,recordings[0]);transition.tick(AutomaticSleep.QUIET_REQUIRED,false);transition.tick(AutomaticSleep.QUIET_REQUIRED+1000,false);assertEquals(1,recordings[0]);
            assertEquals(personal,Prefs.of(c).getAll());assertFalse(TrackingService.running);assertFalse(AlarmService.active);
        }catch(Exception|AssertionError failure){try{File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());device.takeScreenshot(new File(folder,"automatic-failure.png"));device.dumpWindowHierarchy(new File(folder,"automatic-failure.xml"));}catch(Exception diagnostic){failure.addSuppressed(diagnostic);}throw failure;}finally{try{i.runOnMainSync(()->{for(Activity a:owned)if(!a.isDestroyed())a.finish();});i.waitForIdleSync();for(Activity a:owned)destroyed(a);}finally{app.unregisterActivityLifecycleCallbacks(lifecycle);AutomaticTrackingActivity.port=prior;if(owned.stream().allMatch(Activity::isDestroyed))c.deleteSharedPreferences(name);}}
    }
}
