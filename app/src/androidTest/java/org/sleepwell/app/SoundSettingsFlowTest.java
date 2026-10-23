package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.os.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import static org.junit.Assert.*;

/** Configuration only. Never taps Play, imports a document, starts tracking or creates an audio sink. */
@RunWith(AndroidJUnit4.class)
public class SoundSettingsFlowTest {
    private final Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
    private final Context c=instrumentation.getTargetContext();private final UiDevice device=UiDevice.getInstance(instrumentation);
    private void clickText(String text){UiObject2 view=device.wait(Until.findObject(By.text(text)),4000);assertNotNull(text,view);view.click();device.waitForIdle();}
    private void clickDesc(String text){UiObject2 view=device.wait(Until.findObject(By.pkg(c.getPackageName()).desc(text)),4000);assertNotNull(text,view);view.click();device.waitForIdle();}
    private SleepSoundsActivity launch(){Instrumentation.ActivityMonitor monitor=instrumentation.addMonitor(SleepSoundsActivity.class.getName(),null,false);try{c.startActivity(new Intent(c,SleepSoundsActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));Activity a=monitor.waitForActivityWithTimeout(5000);assertNotNull(a);assertTrue(device.wait(Until.hasObject(By.desc("Ocean sound")),5000));device.waitForIdle();return (SleepSoundsActivity)a;}finally{instrumentation.removeMonitor(monitor);}}
    private void capture(String name){device.waitForIdle();SystemClock.sleep(500);File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());assertTrue(device.takeScreenshot(new File(folder,"sounds-settings-"+name+".png")));}
    @Test public void silentChoicesRecreateAndReopenWithoutPlayback()throws Exception{
        assertFalse(Prefs.flag(c,"ringing"));assertFalse(TrackingService.running);assertFalse(Prefs.flag(c,"tracking"));assertNull("Do not disturb personal music",SleepSoundService.snapshot());
        try(Store store=new Store(c)){assertTrue("Do not finalize a personal unfinished night",store.rows("SELECT id FROM sessions WHERE end IS NULL").isEmpty());}
        Map<String,?> personal=new HashMap<>(Prefs.of(c).getAll());String name="sleepwell-sound-settings-"+UUID.randomUUID();SharedPreferences fixture=c.getSharedPreferences(name,Context.MODE_PRIVATE);SoundPreferences.Factory previous=SoundPreferences.factory;SoundPreferences.factory=context->fixture;
        List<SleepSoundsActivity> owned=new CopyOnWriteArrayList<>();Application app=(Application)c.getApplicationContext();Application.ActivityLifecycleCallbacks lifecycle=new Application.ActivityLifecycleCallbacks(){public void onActivityCreated(Activity a,Bundle b){if(a instanceof SleepSoundsActivity)owned.add((SleepSoundsActivity)a);}public void onActivityStarted(Activity a){}public void onActivityResumed(Activity a){}public void onActivityPaused(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,Bundle b){}public void onActivityDestroyed(Activity a){}};app.registerActivityLifecycleCallbacks(lifecycle);
        try{
            SoundPreferences.save(fixture,new SoundMix());SleepSoundsActivity first=launch();capture("rain");assertNull(SleepSoundService.snapshot());
            clickDesc("Ocean sound");assertEquals("Ocean",SoundPreferences.load(fixture).primary);assertTrue(device.findObject(By.desc("Ocean sound")).isSelected());
            clickDesc("Timer");clickText("45 min");clickDesc("Fade out");clickText("10 min");assertEquals(45,SoundPreferences.load(fixture).timerMinutes);assertEquals(10,SoundPreferences.load(fixture).fadeMinutes);capture("ocean");
            instrumentation.runOnMainSync(first::recreate);assertTrue(device.wait(Until.hasObject(By.desc("Ocean sound")),5000));assertTrue(device.findObject(By.desc("Ocean sound")).isSelected());assertNotNull(device.findObject(By.text("45 min")));assertNotNull(device.findObject(By.text("10 min")));capture("recreated");
            clickDesc("Back");launch();assertTrue(device.findObject(By.desc("Ocean sound")).isSelected());capture("reopened");assertNull("Configuration must never start audio",SleepSoundService.snapshot());assertTrue("Personal preferences changed",personal.equals(Prefs.of(c).getAll()));
        }finally{
            boolean closed=false;try{long deadline=SystemClock.elapsedRealtime()+10000;while(true){boolean[] done={true};instrumentation.runOnMainSync(()->{for(SleepSoundsActivity a:owned)if(!a.isDestroyed()){done[0]=false;a.finish();}});instrumentation.waitForIdleSync();if(done[0])break;if(SystemClock.elapsedRealtime()>deadline)throw new AssertionError("Sound screen did not close; retaining isolated preferences");SystemClock.sleep(100);}for(SleepSoundsActivity a:owned)a.awaitReaders(10000);closed=true;}
            finally{app.unregisterActivityLifecycleCallbacks(lifecycle);SoundPreferences.factory=previous;if(closed)c.deleteSharedPreferences(name);c.startActivity(new Intent(c,MainActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}
            assertNull("No playback was authorized",SleepSoundService.snapshot());assertTrue("Personal preferences changed",personal.equals(Prefs.of(c).getAll()));
        }
    }
}
