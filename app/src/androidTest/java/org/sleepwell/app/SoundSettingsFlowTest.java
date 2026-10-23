package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.os.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.*;
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
    private void choose(String title,String text){assertTrue(device.wait(Until.hasObject(By.text(title)),5000));UiObject2 option=device.wait(Until.findObject(By.res("android:id/text1").text(text)),5000);assertNotNull("Dialog choice is available",option);option.click();assertTrue("Dialog selection must finish before the next action",device.wait(Until.gone(By.text(title)),5000));device.waitForIdle();}
    private void clickDesc(String text){UiObject2 view=device.wait(Until.findObject(By.pkg(c.getPackageName()).desc(text)),4000);assertNotNull(text,view);view.click();device.waitForIdle();}
    private void initializeCiIdleDefaults()throws Exception{
        if(!"true".equals(InstrumentationRegistry.getArguments().getString("ciReview")))return;
        assertEquals("CI fixtures must never initialize a physical phone","1",device.executeShellCommand("getprop ro.kernel.qemu").trim());
        SharedPreferences p=Prefs.of(c);assertFalse(p.getBoolean("ringing",false));assertFalse(p.getBoolean("tracking",false));assertFalse(p.getBoolean("trackingPaused",false));assertEquals(0L,p.getLong("session",0));
        // A freshly installed app may receive delayed BOOT_COMPLETED during the test.
        // Define absent inactive defaults before the baseline; never replace existing values.
        // Every key/value, including these four, is still compared exactly in body and cleanup.
        SharedPreferences.Editor edit=p.edit();for(String key:new String[]{"ringing","tracking","trackingPaused"})if(!p.contains(key))edit.putBoolean(key,false);if(!p.contains("session"))edit.putLong("session",0L);assertTrue(edit.commit());
    }
    private void preserved(Map<String,?> before,String stage)throws Exception{Map<String,?> after=Prefs.of(c).getAll();boolean same=before.equals(after);if(!same&&"true".equals(InstrumentationRegistry.getArguments().getString("ciReview"))){Set<String> keys=new TreeSet<>(before.keySet());keys.addAll(after.keySet());List<String> changed=new ArrayList<>();for(String key:keys)if(!Objects.equals(before.get(key),after.get(key)))changed.add(key);File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());try(java.io.FileWriter report=new java.io.FileWriter(new File(folder,"sounds-settings-changed-keys-"+stage+".txt"))){report.write(String.join("\n",changed));}}assertTrue("Personal preferences changed",same);}
    private SleepSoundsActivity launch(){Instrumentation.ActivityMonitor monitor=instrumentation.addMonitor(SleepSoundsActivity.class.getName(),null,false);try{c.startActivity(new Intent(c,SleepSoundsActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));Activity a=monitor.waitForActivityWithTimeout(5000);assertNotNull(a);assertTrue(device.wait(Until.hasObject(By.desc("Ocean sound")),5000));device.waitForIdle();return (SleepSoundsActivity)a;}finally{instrumentation.removeMonitor(monitor);}}
    private SleepSoundsActivity awaitReplacement(SleepSoundsActivity old){SleepSoundsActivity[] replacement={null};long deadline=SystemClock.elapsedRealtime()+5000;while(replacement[0]==null&&SystemClock.elapsedRealtime()<deadline){instrumentation.runOnMainSync(()->{for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))if(a instanceof SleepSoundsActivity&&a!=old&&old.isDestroyed())replacement[0]=(SleepSoundsActivity)a;});if(replacement[0]==null)SystemClock.sleep(100);}assertNotNull("Recreation must destroy the old Activity and resume a different instance",replacement[0]);return replacement[0];}
    private void awaitDestroyed(SleepSoundsActivity screen){boolean[] gone={false};long deadline=SystemClock.elapsedRealtime()+5000;while(!gone[0]&&SystemClock.elapsedRealtime()<deadline){instrumentation.runOnMainSync(()->gone[0]=screen.isDestroyed());if(!gone[0])SystemClock.sleep(100);}assertTrue("Back must finish the sound screen before reopening",gone[0]);}
    private void capture(String name)throws Exception{
        boolean[] ready={false};int[] bars={0,0};android.view.View[] target={null};int stable=0;long end=SystemClock.elapsedRealtime()+10000;
        while(stable<4&&SystemClock.elapsedRealtime()<end){
            ready[0]=false;instrumentation.runOnMainSync(()->{for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))if(a instanceof SleepSoundsActivity&&a.hasWindowFocus()){
                android.view.View decor=a.getWindow().getDecorView();android.graphics.drawable.Drawable background=decor.getBackground();android.view.WindowInsets insets=decor.getRootWindowInsets();int mask=android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
                ready[0]=((SleepSoundsActivity)a).artworkReady()&&(a.getWindow().getInsetsController().getSystemBarsAppearance()&mask)==0&&background instanceof android.graphics.drawable.ColorDrawable&&((android.graphics.drawable.ColorDrawable)background).getColor()==Design.PLUM&&insets!=null;
                if(ready[0]){target[0]=decor;bars[0]=insets.getInsets(android.view.WindowInsets.Type.statusBars()).top;bars[1]=insets.getInsets(android.view.WindowInsets.Type.navigationBars()).bottom;}
            }});stable=ready[0]?stable+1:0;if(stable<4)SystemClock.sleep(100);
        }
        assertEquals("Focused artwork and light bars on a plum Window must settle before capture",4,stable);
        java.util.concurrent.CountDownLatch frame=new java.util.concurrent.CountDownLatch(1);instrumentation.runOnMainSync(()->{assertTrue(target[0].isHardwareAccelerated());target[0].getViewTreeObserver().registerFrameCommitCallback(frame::countDown);target[0].invalidate();});assertTrue(frame.await(5,java.util.concurrent.TimeUnit.SECONDS));device.waitForIdle();
        File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());File file=new File(folder,"sounds-settings-"+name+".png");assertTrue(device.takeScreenshot(file));
        // Read the actual system-bar pixels. Requested app flags alone can miss a
        // stale platform appearance. Never set bar flags or edit the PNG in test.
        android.graphics.Bitmap pixels=android.graphics.BitmapFactory.decodeFile(file.getAbsolutePath());assertNotNull(pixels);try{assertTrue("Status glyphs must actually be light on plum",bars[0]>0&&lightOnPlum(pixels,0,bars[0]));assertTrue("Navigation glyphs must actually be light on plum",bars[1]>0&&lightOnPlum(pixels,pixels.getHeight()-bars[1],pixels.getHeight()));}finally{pixels.recycle();}
    }
    private boolean lightOnPlum(android.graphics.Bitmap image,int from,int to){int light=0,plum=0,total=0;for(int y=Math.max(0,from);y<Math.min(image.getHeight(),to);y++)for(int x=0;x<image.getWidth();x++){int color=image.getPixel(x,y),r=android.graphics.Color.red(color),g=android.graphics.Color.green(color),b=android.graphics.Color.blue(color);total++;if(r>=210&&g>=210&&b>=210)light++;if(Math.abs(r-36)<=12&&Math.abs(g-30)<=12&&Math.abs(b-48)<=12)plum++;}return total>0&&light>20&&plum>=total*.6;}
    @Test public void silentChoicesRecreateAndReopenWithoutPlayback()throws Exception{
        assertFalse("Do not replace preferences while an audio import is active",SoundImports.pending());assertFalse(Prefs.flag(c,"ringing"));assertFalse(TrackingService.running);assertFalse(Prefs.flag(c,"tracking"));assertNull("Do not disturb personal music",SleepSoundService.snapshot());
        try(Store store=new Store(c)){assertTrue("Do not finalize a personal unfinished night",store.rows("SELECT id FROM sessions WHERE end IS NULL").isEmpty());}
        initializeCiIdleDefaults();
        Map<String,?> personal=new HashMap<>(Prefs.of(c).getAll());String name="sleepwell-sound-settings-"+UUID.randomUUID();SharedPreferences fixture=c.getSharedPreferences(name,Context.MODE_PRIVATE);SoundPreferences.Factory previous=SoundPreferences.factory;SoundPreferences.factory=context->fixture;
        List<SleepSoundsActivity> owned=new CopyOnWriteArrayList<>();Application app=(Application)c.getApplicationContext();Application.ActivityLifecycleCallbacks lifecycle=new Application.ActivityLifecycleCallbacks(){public void onActivityCreated(Activity a,Bundle b){if(a instanceof SleepSoundsActivity)owned.add((SleepSoundsActivity)a);}public void onActivityStarted(Activity a){}public void onActivityResumed(Activity a){}public void onActivityPaused(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,Bundle b){}public void onActivityDestroyed(Activity a){}};app.registerActivityLifecycleCallbacks(lifecycle);
        try{
            Map<String,?> unopened=new HashMap<>(fixture.getAll());SleepSoundsActivity first=launch();capture("rain");assertNull(SleepSoundService.snapshot());assertEquals("Opening a legacy/default mix must not persist defaults or errors",unopened,fixture.getAll());
            clickDesc("Ocean sound");assertTrue("Ocean selection must finish before checking storage",device.wait(Until.hasObject(By.desc("Ocean sound").selected(true)),5000));assertEquals("Ocean",SoundPreferences.load(fixture).primary);
            clickDesc("Timer");choose("Stop after playing for","45 min");assertEquals(45,SoundPreferences.load(fixture).timerMinutes);clickDesc("Fade out");choose("Fade at the end of the timer","10 min");assertEquals(10,SoundPreferences.load(fixture).fadeMinutes);capture("ocean");
            instrumentation.runOnMainSync(first::recreate);SleepSoundsActivity recreated=awaitReplacement(first);assertTrue(device.wait(Until.hasObject(By.desc("Ocean sound").selected(true)),5000));assertNotNull(device.findObject(By.text("45 min")));assertNotNull(device.findObject(By.text("10 min")));capture("recreated");
            clickDesc("Back");awaitDestroyed(recreated);launch();assertTrue(device.wait(Until.hasObject(By.desc("Ocean sound").selected(true)),5000));capture("reopened");assertNull("Configuration must never start audio",SleepSoundService.snapshot());preserved(personal,"body");
        }catch(AssertionError|Exception failure){if("true".equals(InstrumentationRegistry.getArguments().getString("ciReview")))try{File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());device.takeScreenshot(new File(folder,"sounds-settings-failed.png"));device.dumpWindowHierarchy(new File(folder,"sounds-settings-failed.xml"));}catch(Exception diagnostic){failure.addSuppressed(diagnostic);}throw failure;
        }finally{
            boolean closed=false;try{long deadline=SystemClock.elapsedRealtime()+10000;while(true){boolean[] done={true};instrumentation.runOnMainSync(()->{for(SleepSoundsActivity a:owned)if(!a.isDestroyed()){done[0]=false;a.finish();}});instrumentation.waitForIdleSync();if(done[0])break;if(SystemClock.elapsedRealtime()>deadline)throw new AssertionError("Sound screen did not close; retaining isolated preferences");SystemClock.sleep(100);}for(SleepSoundsActivity a:owned)a.awaitReaders(10000);closed=true;}
            finally{app.unregisterActivityLifecycleCallbacks(lifecycle);SoundPreferences.factory=previous;if(closed)c.deleteSharedPreferences(name);c.startActivity(new Intent(c,MainActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}
            assertNull("No playback was authorized",SleepSoundService.snapshot());preserved(personal,"cleanup");
        }
    }
}
