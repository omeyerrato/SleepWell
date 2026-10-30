package org.sleepwell.app;

import android.os.SystemClock;
import android.view.View;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import androidx.test.uiautomator.*;
import org.junit.Test;
import static org.junit.Assert.*;

/** Native active/paused/end states backed by the existing synthetic PCM + SQLite coordinator. */
public final class NightDialTrackingReviewTest {
    private void frame(SonarNightFlowTest journey,String name)throws Exception{NightDialCapture.frame(journey.i,journey.c,journey.d,name);}
    @Test public void measuredGraphPauseRecreationCancelEndAndExplicitSave()throws Exception{
        SonarNightFlowTest journey=new SonarNightFlowTest();journey.fixture(()->{
            journey.launch();journey.click("Start tracking  →");journey.waitFor(()->journey.night!=null&&journey.night.state().readings.stream().anyMatch(x->x.movement!=null));journey.renderedMeasurements();frame(journey,"tracking-active");
            journey.i.runOnMainSync(()->{View root=journey.activity.getWindow().getDecorView();View pause=journey.find(root,"Pause tracking"),end=journey.find(root,"End night"),chart=journey.signalChart(root);assertNotNull(pause);assertNotNull(end);assertNotNull(chart);android.graphics.Rect area=new android.graphics.Rect(),pauseArea=new android.graphics.Rect();assertTrue(chart.getGlobalVisibleRect(area));assertEquals("Entire actual graph fits above controls",chart.getHeight(),area.height());assertTrue(pause.getGlobalVisibleRect(pauseArea));assertTrue(area.bottom<=pauseArea.top);});
            journey.click("End night");assertTrue(journey.d.wait(Until.hasObject(By.text("End this night?")),5000));frame(journey,"tracking-end-confirmation");journey.d.findObject(By.text("Keep tracking")).click();assertTrue(journey.d.wait(Until.gone(By.text("End this night?")),5000));assertEquals("ACTIVE",journey.night.state().phase);assertFalse(journey.audio.get(0).closed);
            journey.click("Pause tracking");journey.waitFor(()->journey.night.state().phase.equals("PAUSED"));journey.settledPausedTimeline();long active=journey.night.state().active;frame(journey,"tracking-paused");assertTrue(journey.audio.get(0).closed);journey.recreate();journey.settledPausedTimeline();assertEquals("Recreation does not resume capture",active,journey.night.state().active);assertEquals(1,journey.audio.size());frame(journey,"tracking-paused-restored");
            journey.click("Resume tracking");journey.waitFor(()->journey.audio.size()==2&&journey.night.state().phase.equals("ACTIVE"));journey.renderedMeasurements();frame(journey,"tracking-resumed");journey.click("End night");assertTrue(journey.d.wait(Until.hasObject(By.text("End this night?")),5000));journey.d.findObject(By.text("End & review")).click();assertTrue(journey.night.awaitClosed(10000));journey.waitFor(()->journey.reviewed&&journey.activity.isDestroyed());try(Store store=new Store(journey.c,journey.database)){assertEquals(1,store.rows("SELECT * FROM sessions WHERE end IS NOT NULL").size());assertEquals(1,store.rows("SELECT * FROM tracking_pauses WHERE end IS NOT NULL").size());assertFalse(store.rows("SELECT * FROM epochs WHERE signal_quality='VALID'").isEmpty());assertTrue(store.rows("SELECT * FROM sound_events").isEmpty());Backup.validate(store.backup());}
        });
    }
}
