package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.View;
import androidx.test.platform.app.InstrumentationRegistry;
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
    private void click(String text)throws Exception{UiObject2 object=device.findObject(By.text(text));if(object==null){new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().text(text));object=device.wait(Until.findObject(By.text(text)),5000);}assertNotNull(text,object);object.click();device.waitForIdle();}
    private void capture(String name)throws Exception{CountDownLatch frame=new CountDownLatch(1);i.runOnMainSync(()->{View decor=activity.getWindow().getDecorView();decor.getViewTreeObserver().registerFrameCommitCallback(frame::countDown);decor.invalidate();});assertTrue(frame.await(5,TimeUnit.SECONDS));device.waitForIdle();File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());assertTrue(device.takeScreenshot(new File(folder,"automatic-"+name+".png")));}
    private void launch(){activity=(AutomaticTrackingActivity)i.startActivitySync(new Intent(c,AutomaticTrackingActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));assertTrue(device.wait(Until.hasObject(By.text("Automatic tracking")),5000));}
    @Test public void choicesPersistAndArmingUsesFakeOutputOnly()throws Exception{
        assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertEquals("1",device.executeShellCommand("getprop ro.kernel.qemu").trim());assertFalse(TrackingService.running);assertFalse(AlarmService.active);
        Map<String,?> personal=new HashMap<>(Prefs.of(c).getAll());String name="automatic-test-"+UUID.randomUUID();SharedPreferences prefs=c.getSharedPreferences(name,0);AutomaticTrackingActivity.Port prior=AutomaticTrackingActivity.port;int[] armed={0};
        try{
            AutomaticTrackingActivity.port=new AutomaticTrackingActivity.Port(){
                public SharedPreferences preferences(Activity a){return prefs;}
                public long schedule(Activity a,AutomaticSleep.Mode mode){assertEquals(AutomaticSleep.Mode.STILLNESS,mode);return System.currentTimeMillis();}
                public boolean movementAvailable(Activity a){return true;}
                public boolean notificationsAllowed(Activity a){return true;}
                public boolean microphoneAllowed(Activity a){return true;}
                public void arm(Activity a,AutomaticSleep.Mode mode,long scheduled,boolean recordSounds,boolean keepClips){assertTrue(recordSounds);assertTrue(keepClips);armed[0]++;assertEquals(AutomaticSleep.Mode.STILLNESS,mode);}
            };
            launch();capture("manual");object("After settling down").click();device.waitForIdle();capture("stillness");object("Record sounds").click();device.waitForIdle();object("Keep sound clips").click();capture("sounds");click("Save settings");assertEquals("STILLNESS",prefs.getString("automaticMode",""));
            launch();assertTrue(object("After settling down").isSelected());assertTrue(object("Record sounds").isChecked());assertTrue(object("Keep sound clips").isChecked());capture("reopened");click("Arm tonight");assertEquals(1,armed[0]);
            // The same production decision model gates a fake recording effect on actual sampled evidence.
            int[] recordings={0};AutomaticTransition transition=new AutomaticTransition(new AutomaticSleep(AutomaticSleep.Mode.STILLNESS,0,0),()->recordings[0]++);
            for(long elapsed=0;elapsed<=AutomaticSleep.QUIET_REQUIRED;elapsed+=200)transition.sample(elapsed,.01,false);
            assertEquals(0,recordings[0]);transition.tick(AutomaticSleep.QUIET_REQUIRED,false);transition.tick(AutomaticSleep.QUIET_REQUIRED+1000,false);assertEquals(1,recordings[0]);
            assertEquals(personal,Prefs.of(c).getAll());assertFalse(TrackingService.running);assertFalse(AlarmService.active);
        }finally{if(activity!=null)i.runOnMainSync(activity::finish);i.waitForIdleSync();AutomaticTrackingActivity.port=prior;c.deleteSharedPreferences(name);}
    }
}
