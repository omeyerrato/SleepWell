package org.sleepwell.app;

import android.app.Instrumentation;
import android.content.Context;
import android.os.SystemClock;
import android.view.View;
import androidx.test.uiautomator.UiDevice;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

/** The focused Activity or Dialog must have attached, laid out and committed a hardware frame. */
final class NightDialCapture {
    static void frame(Instrumentation i,Context c,UiDevice d,String name)throws Exception{
        d.waitForIdle();CountDownLatch committed=new CountDownLatch(1);boolean[] owner={false};long until=SystemClock.elapsedRealtime()+5000;
        while(!owner[0]&&SystemClock.elapsedRealtime()<until){i.runOnMainSync(()->{for(View root:android.view.inspector.WindowInspector.getGlobalWindowViews())if(root.hasWindowFocus()&&root.isAttachedToWindow()&&root.isLaidOut()&&root.isHardwareAccelerated()){owner[0]=true;root.getViewTreeObserver().registerFrameCommitCallback(committed::countDown);root.invalidate();return;}});if(!owner[0])SystemClock.sleep(50);}
        assertTrue(name+": focused native capture owner",owner[0]);assertTrue(name+": committed hardware frame",committed.await(5,TimeUnit.SECONDS));File dir=new File(c.getExternalFilesDir(null),"verification");assertTrue(dir.isDirectory()||dir.mkdirs());assertTrue(d.takeScreenshot(new File(dir,"night-dial-"+name+".png")));
    }
}
