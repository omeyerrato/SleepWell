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
        c.startActivity(new Intent(c,MainActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));device.waitForIdle();
        File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());
        for(String tab:new String[]{"Tonight","Report","Settings"}){
            UiObject2 destination=device.findObject(By.desc(tab).selected(true));
            if(destination==null){for(UiObject2 candidate:device.findObjects(By.desc(tab)))if(candidate.getVisibleBounds().centerY()>device.getDisplayHeight()*.7f){candidate.click();break;}}
            assertTrue(device.wait(Until.hasObject(By.desc(tab).selected(true)),5000));device.waitForIdle();assertTrue(device.takeScreenshot(new File(folder,"root-"+tab.toLowerCase(java.util.Locale.ROOT)+".png")));
            if(tab.equals("Report")){
                UiObject2 chart=device.findObject(By.descStartsWith("movement chart."));
                if(chart!=null){chart.click();device.waitForIdle();assertTrue(device.hasObject(By.descStartsWith("movement at ")));assertTrue(device.takeScreenshot(new File(folder,"root-report-selected.png")));}
            }
            assertFalse(device.hasObject(By.textContains("feature matrix")));assertFalse(device.hasObject(By.text("Export session CSV")));
        }
        for(UiObject2 candidate:device.findObjects(By.desc("Tonight")))if(candidate.getVisibleBounds().centerY()>device.getDisplayHeight()*.7f){candidate.click();break;}
        assertFalse(Prefs.flag(c,"ringing"));
    }
}
