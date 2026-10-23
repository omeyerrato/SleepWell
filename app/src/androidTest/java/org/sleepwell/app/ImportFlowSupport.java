package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.media.AudioManager;
import android.os.*;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.*;
import androidx.test.uiautomator.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;
import static org.junit.Assert.*;

abstract class ImportFlowSupport {
    final Instrumentation instrument=InstrumentationRegistry.getInstrumentation();final Context c=instrument.getTargetContext();final UiDevice device=UiDevice.getInstance(instrument);
    final List<SleepSoundsActivity> owned=new CopyOnWriteArrayList<>();final Application app=(Application)c.getApplicationContext();
    final SoundPreferences.Factory previous=SoundPreferences.factory;SharedPreferences fixture;String fixtureName;Map<String,?> personal;int volume;
    final Application.ActivityLifecycleCallbacks lifecycle=new Application.ActivityLifecycleCallbacks(){public void onActivityCreated(Activity a,Bundle b){if(a instanceof SleepSoundsActivity)owned.add((SleepSoundsActivity)a);}public void onActivityStarted(Activity a){}public void onActivityResumed(Activity a){}public void onActivityPaused(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,Bundle b){}public void onActivityDestroyed(Activity a){}};
    void setup(String name)throws Exception{
        assertEquals("Explicit opt-in for silent CI emulator required","true",InstrumentationRegistry.getArguments().getString("ciAudioImports"));assertEquals("1",device.executeShellCommand("getprop ro.kernel.qemu").trim());assertTrue(Build.MODEL.startsWith("sdk_gphone64"));
        assertFalse(Prefs.flag(c,"ringing"));assertFalse(Prefs.flag(c,"tracking"));assertFalse(TrackingService.running);assertNull(SleepSoundService.snapshot());assertFalse(SoundImports.pending());try(Store store=new Store(c)){assertTrue(store.rows("SELECT id FROM sessions WHERE end IS NULL").isEmpty());}
        personal=new HashMap<>(Prefs.of(c).getAll());volume=((AudioManager)c.getSystemService(Context.AUDIO_SERVICE)).getStreamVolume(AudioManager.STREAM_MUSIC);fixtureName=name;fixture=c.getSharedPreferences(name,Context.MODE_PRIVATE);SoundPreferences.factory=context->fixture;app.registerActivityLifecycleCallbacks(lifecycle);
    }
    void until(String reason,BooleanSupplier predicate){long end=SystemClock.elapsedRealtime()+15000;while(!predicate.getAsBoolean()&&SystemClock.elapsedRealtime()<end)SystemClock.sleep(100);assertTrue(reason,predicate.getAsBoolean());}
    void text(String label){UiObject2 view=device.wait(Until.findObject(By.text(label)),7000);assertNotNull(label,view);view.click();device.waitForIdle();}
    void desc(String label)throws Exception{if(device.findObject(By.pkg(c.getPackageName()).desc(label))==null)new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().description(label));UiObject2 view=device.wait(Until.findObject(By.pkg(c.getPackageName()).desc(label)),5000);assertNotNull(label,view);view.click();device.waitForIdle();}
    SleepSoundsActivity launch(){Instrumentation.ActivityMonitor monitor=instrument.addMonitor(SleepSoundsActivity.class.getName(),null,false);try{c.startActivity(new Intent(c,SleepSoundsActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));Activity a=monitor.waitForActivityWithTimeout(5000);assertNotNull(a);assertTrue(device.wait(Until.hasObject(By.desc("Ocean sound")),5000));return (SleepSoundsActivity)a;}finally{instrument.removeMonitor(monitor);}}
    void closeScreens()throws Exception{until("Owned screens close",()->{boolean[] closed={true};instrument.runOnMainSync(()->{for(SleepSoundsActivity a:owned)if(!a.isDestroyed()){closed[0]=false;a.finish();}});return closed[0];});for(SleepSoundsActivity a:owned)a.awaitReaders(10000);SoundImports.awaitIdle(10000);}
    SleepSoundsActivity current(){SleepSoundsActivity[] result={null};instrument.runOnMainSync(()->{for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))if(a instanceof SleepSoundsActivity)result[0]=(SleepSoundsActivity)a;});return result[0];}
    SleepSoundsActivity recreate(SleepSoundsActivity old){instrument.runOnMainSync(old::recreate);until("Distinct resumed Activity after recreation",()->old.isDestroyed()&&current()!=null&&current()!=old);return current();}
    void picker()throws Exception{desc("My audio");if(!SoundPreferences.load(fixture).uri.isEmpty())text("Import another file");assertTrue("System DocumentsUI picker opens",device.wait(Until.hasObject(By.desc("Show roots")),7000));}
    void pick(String name)throws Exception{picker();device.findObject(By.desc("Show roots")).click();text(ImportDocuments.ROOT);UiObject2 file=device.wait(Until.findObject(By.text(name)),5000);if(file==null)new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().text(name));text(name);assertTrue("Return to the app after selection",device.wait(Until.hasObject(By.pkg(c.getPackageName()).text("Sleep sounds")),7000));}
    void settled(){until("Import reader retires",()->!SoundImports.pending());device.waitForIdle();}
    void capture(String name)throws Exception{new UiScrollable(new UiSelector().scrollable(true)).scrollToBeginning(8);until("Focused sound artwork ready",()->{SleepSoundsActivity a=current();if(a==null)return false;boolean[] ready={false};instrument.runOnMainSync(()->ready[0]=a.hasWindowFocus()&&a.artworkReady());return ready[0];});device.waitForIdle();File folder=folder();assertTrue(device.takeScreenshot(new File(folder,"sounds-import-"+name+".png")));}
    File folder(){File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());return folder;}
    File handoff(){return new File(folder(),"audio-import-fixture.txt");}
    void preserved(){assertEquals("Personal preferences must remain exact",personal,Prefs.of(c).getAll());assertEquals(volume,((AudioManager)c.getSystemService(Context.AUDIO_SERVICE)).getStreamVolume(AudioManager.STREAM_MUSIC));}
    void cleanup(boolean delete)throws Exception{boolean closed=false;try{SleepSoundService.command(c,SleepSoundService.STOP);until("Sound service stops",()->SleepSoundService.snapshot()==null);closeScreens();closed=true;}finally{app.unregisterActivityLifecycleCallbacks(lifecycle);SoundPreferences.factory=previous;if(closed&&delete)c.deleteSharedPreferences(fixtureName);}preserved();}
    void diagnostic(String stage,Throwable failure){try{device.takeScreenshot(new File(folder(),"sounds-import-failed-"+stage+".png"));device.dumpWindowHierarchy(new File(folder(),"sounds-import-failed-"+stage+".xml"));}catch(Exception error){failure.addSuppressed(error);}}
}
