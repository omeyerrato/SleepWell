package org.sleepwell.app;

import android.content.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import static org.junit.Assert.*;

/** No alarm, microphone, tracking, power-button or stored-data mutations. */
@RunWith(AndroidJUnit4.class)
public class RootVisualTest {
    @Test public void rootNavigationAndActualScreenshots(){
        Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();UiDevice device=UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        assertFalse(Prefs.flag(c,"ringing"));assertFalse(TrackingService.running);assertFalse(Prefs.flag(c,"tracking"));assertFalse(Prefs.flag(c,"trackingPaused"));try(Store original=new Store(c)){assertTrue("Do not finalize an unfinished personal recording",original.rows("SELECT id FROM sessions WHERE end IS NULL").isEmpty());}
        c.startActivity(new Intent(c,MainActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));device.waitForIdle();
        assertFalse(Prefs.flag(c,"ringing"));assertFalse(TrackingService.running);
        File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());
        for(String tab:new String[]{"Tonight","Alarms","Report","Settings"}){
            UiObject2 destination=device.findObject(By.desc(tab).selected(true));
            if(destination==null){for(UiObject2 candidate:device.findObjects(By.desc(tab)))if(candidate.getVisibleBounds().centerY()>device.getDisplayHeight()*.7f){candidate.click();break;}}
            assertTrue(device.wait(Until.hasObject(By.desc(tab).selected(true)),5000));device.waitForIdle();assertTrue(device.takeScreenshot(new File(folder,"root-"+tab.toLowerCase(java.util.Locale.ROOT)+".png")));
            if(tab.equals("Report")){
                UiObject2 chart=device.findObject(By.descStartsWith("movement chart."));
                if(chart!=null){chart.click();device.waitForIdle();android.os.SystemClock.sleep(150);assertTrue(device.hasObject(By.descStartsWith("movement at ")));assertTrue(device.takeScreenshot(new File(folder,"root-report-selected.png")));}
            }
            UiObject2 chosen=device.findObject(By.desc(tab).selected(true));
            assertNotNull(chosen);assertTrue(chosen.getVisibleBounds().width()>=44);assertTrue(chosen.getVisibleBounds().height()>=44);
            assertFalse(device.hasObject(By.text("Emergency dismiss")));assertFalse(device.hasObject(By.text("Test a quiet alarm")));
            assertFalse(device.hasObject(By.textContains("feature matrix")));assertFalse(device.hasObject(By.text("Export session CSV")));
        }
        for(String[] detail:new String[][]{{"Reliability","Alarm readiness","readiness"},{"Sound analysis","Sound analysis","sound-analysis"},{"Bedtime guard","Bedtime guard","bedtime-guard"},{"Data & privacy","Data & privacy","privacy"}}){
            UiObject2 link=device.findObject(By.text(detail[0]));assertNotNull(link);link.click();device.waitForIdle();assertTrue(device.wait(Until.hasObject(By.text(detail[1])),5000));
            if(detail[2].equals("bedtime-guard"))assertTrue(device.wait(Until.hasObject(By.desc("Choose other apps").enabled(true)),5000));
            assertTrue(device.takeScreenshot(new File(folder,"root-"+detail[2]+".png")));
            UiObject2 back=device.findObject(By.pkg("org.sleepwell.app").desc("Back"));assertNotNull(back);if(detail[2].equals("readiness"))device.pressBack();else back.click();device.waitForIdle();assertTrue(device.wait(Until.hasObject(By.desc("Settings").selected(true)),5000));
        }
        for(UiObject2 candidate:device.findObjects(By.desc("Tonight")))if(candidate.getVisibleBounds().centerY()>device.getDisplayHeight()*.7f){candidate.click();break;}
        UiObject2 wind=device.findObject(By.text("Wind-down at"));assertNotNull(wind);wind.click();device.waitForIdle();assertTrue(device.wait(Until.hasObject(By.text("Wind-down")),5000));assertTrue(device.takeScreenshot(new File(folder,"root-wind-down.png")));device.findObject(By.pkg("org.sleepwell.app").desc("Back")).click();device.waitForIdle();
        assertFalse(Prefs.flag(c,"ringing"));
    }
}
