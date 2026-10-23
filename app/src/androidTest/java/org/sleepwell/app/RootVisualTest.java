package org.sleepwell.app;

import android.content.*;
import android.app.Activity;
import android.os.SystemClock;
import android.view.WindowInsetsController;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.*;
import androidx.test.uiautomator.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

/** No alarm, microphone, tracking, power-button or stored-data mutations. */
@RunWith(AndroidJUnit4.class)
public class RootVisualTest {
    private void capture(UiDevice device,File target)throws Exception{
        CountDownLatch frame=new CountDownLatch(1);boolean[] registered={false};long until=SystemClock.elapsedRealtime()+5000;
        while(!registered[0]&&SystemClock.elapsedRealtime()<until){
            InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))if(a.hasWindowFocus()){android.view.View decor=a.getWindow().getDecorView();assertTrue("Capture requires a hardware-rendered frame",decor.isHardwareAccelerated());decor.getViewTreeObserver().registerFrameCommitCallback(frame::countDown);decor.invalidate();registered[0]=true;}});
            if(!registered[0])SystemClock.sleep(50);
        }
        assertTrue("Capture requires the focused app window",registered[0]);assertTrue("The destination frame must commit before capture",frame.await(5,TimeUnit.SECONDS));device.waitForIdle();assertTrue(device.takeScreenshot(target));
    }
    private void stableDarkBars(){long end=SystemClock.elapsedRealtime()+5000;int stable=0;while(stable<4){boolean[] dark={false};InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))if(a instanceof MainActivity&&a.hasWindowFocus()){int mask=WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;android.graphics.drawable.Drawable background=a.getWindow().getDecorView().getBackground();dark[0]=(a.getWindow().getInsetsController().getSystemBarsAppearance()&mask)==0&&background instanceof android.graphics.drawable.ColorDrawable&&((android.graphics.drawable.ColorDrawable)background).getColor()==Design.PLUM;}});stable=dark[0]?stable+1:0;if(SystemClock.elapsedRealtime()>end)fail("Wind-down needs both a plum Window background and light system-bar appearance for gesture and three-button navigation");SystemClock.sleep(100);}}
    @Test public void rootNavigationAndActualScreenshots()throws Exception{
        Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();UiDevice device=UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        assertFalse(Prefs.flag(c,"ringing"));assertFalse(TrackingService.running);assertFalse(Prefs.flag(c,"tracking"));assertFalse(Prefs.flag(c,"trackingPaused"));try(Store original=new Store(c)){assertTrue("Do not finalize an unfinished personal recording",original.rows("SELECT id FROM sessions WHERE end IS NULL").isEmpty());}
        c.startActivity(new Intent(c,MainActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));device.waitForIdle();
        assertFalse(Prefs.flag(c,"ringing"));assertFalse(TrackingService.running);
        File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());
        for(String tab:new String[]{"Tonight","Alarms","Report","Settings"}){
            UiObject2 destination=device.findObject(By.desc(tab).selected(true));
            if(destination==null){for(UiObject2 candidate:device.findObjects(By.desc(tab)))if(candidate.getVisibleBounds().centerY()>device.getDisplayHeight()*.7f){candidate.click();break;}}
            assertTrue(device.wait(Until.hasObject(By.desc(tab).selected(true)),5000));device.waitForIdle();capture(device,new File(folder,"root-"+tab.toLowerCase(java.util.Locale.ROOT)+".png"));
            if(tab.equals("Report")){
                UiObject2 chart=device.findObject(By.descStartsWith("movement chart."));
                if(chart!=null){chart.click();device.waitForIdle();android.os.SystemClock.sleep(150);assertTrue(device.hasObject(By.descStartsWith("movement at ")));capture(device,new File(folder,"root-report-selected.png"));}
            }
            UiObject2 chosen=device.findObject(By.desc(tab).selected(true));
            assertNotNull(chosen);assertTrue(chosen.getVisibleBounds().width()>=44);assertTrue(chosen.getVisibleBounds().height()>=44);
            assertFalse(device.hasObject(By.text("Emergency dismiss")));assertFalse(device.hasObject(By.text("Test a quiet alarm")));
            assertFalse(device.hasObject(By.textContains("feature matrix")));assertFalse(device.hasObject(By.text("Export session CSV")));
        }
        for(String[] detail:new String[][]{{"Reliability","Alarm readiness","readiness"},{"Sound analysis","Sound analysis","sound-analysis"},{"Bedtime guard","Bedtime guard","bedtime-guard"},{"Travel mode","Travel mode","travel"},{"Data & privacy","Data & privacy","privacy"}}){
            UiObject2 link=device.findObject(By.text(detail[0]));if(link==null){new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().text(detail[0]));link=device.findObject(By.text(detail[0]));}assertNotNull(link);link.click();device.waitForIdle();assertTrue(device.wait(Until.hasObject(By.text(detail[1])),5000));
            assertTrue("The Settings dock must leave the destination",device.wait(Until.gone(By.pkg(c.getPackageName()).desc("Settings").selected(true)),5000));
            if(detail[2].equals("privacy")){assertTrue(device.wait(Until.hasObject(By.text("Your sleep data stays on this phone.")),5000));assertTrue(device.wait(Until.hasObject(By.text("On this phone")),5000));}
            if(detail[2].equals("sound-analysis"))assertTrue(device.wait(Until.hasObject(By.text("Microphone analysis")),5000));
            if(detail[2].equals("bedtime-guard"))assertTrue(device.wait(Until.hasObject(By.desc("Choose other apps").enabled(true)),5000));
            capture(device,new File(folder,"root-"+detail[2]+".png"));
            UiObject2 back=device.findObject(By.pkg("org.sleepwell.app").desc("Back"));assertNotNull(back);if(detail[2].equals("readiness"))device.pressBack();else back.click();device.waitForIdle();assertTrue(device.wait(Until.hasObject(By.desc("Settings").selected(true)),5000));
        }
        for(UiObject2 candidate:device.findObjects(By.desc("Tonight")))if(candidate.getVisibleBounds().centerY()>device.getDisplayHeight()*.7f){candidate.click();break;}
        for(int transition=0;transition<3;transition++){UiObject2 wind=device.findObject(By.text("Wind-down at"));assertNotNull(wind);wind.click();device.waitForIdle();assertTrue(device.wait(Until.hasObject(By.text("Wind-down")),5000));stableDarkBars();if(transition==2)capture(device,new File(folder,"root-wind-down.png"));device.findObject(By.pkg("org.sleepwell.app").desc("Back")).click();device.waitForIdle();assertTrue(device.wait(Until.hasObject(By.desc("Tonight").selected(true)),5000));}
        assertFalse(Prefs.flag(c,"ringing"));
    }
}
