package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.os.Build;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.*;
import androidx.test.uiautomator.*;
import org.junit.Test;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

/** Delayed real cleanup through an injected gate; no device outputs. */
public class SoundRetentionLifecycleTest {
    @Test public void rotationRetainsBusyPolicyPinnedFactoryFailureAndExplicitRetry()throws Exception{
        Instrumentation i=InstrumentationRegistry.getInstrumentation();Context c=i.getTargetContext();org.junit.Assume.assumeTrue(Build.HARDWARE.contains("ranchu")||Build.HARDWARE.contains("goldfish"));org.junit.Assume.assumeTrue(InstrumentationRegistry.getArguments().getString("ciReview","").equals("true"));
        UiDevice device=UiDevice.getInstance(i);String database="sleepwell-verification-"+UUID.randomUUID()+".db";Records.Factory prior=Records.factory;SoundRetentionJob.Factory factory=SoundRetentionJob.factory;Map<String,?> prefs=Prefs.of(c).getAll();String[] keys={"audioRetentionDays","keepAudioFavorites","audioRetentionError"};CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1);AtomicInteger calls=new AtomicInteger();List<Activity> owned=new ArrayList<>();File dir=new File(c.getFilesDir(),"clips");assertTrue(dir.isDirectory()||dir.mkdirs());File blocked=new File(dir,"verification-retention-"+UUID.randomUUID()+".wav");assertTrue(blocked.mkdir());File child=new File(blocked,"blocked");assertTrue(child.createNewFile());File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());
        try{
            try(Store store=new Store(c,database)){long start=System.currentTimeMillis()-2*86400000L;store.getWritableDatabase().execSQL("INSERT INTO sessions(id,start,end,source,zone) VALUES(1,?,?,'tracked','UTC')",new Object[]{start,start+30000});store.clip(1,start+10000,blocked.getName(),.2,10000);}
            i.runOnMainSync(()->{Records.factory=context->new Store(context,database);SoundRetentionJob.factory=(context,records)->policy->{calls.incrementAndGet();entered.countDown();if(!release.await(30,TimeUnit.SECONDS))throw new IOException("Fixture gate timeout");return SoundRetentionJob.applyNative(context,records,policy);};Prefs.put(c,"audioRetentionDays",7);Prefs.put(c,"keepAudioFavorites",true);c.startActivity(new Intent(c,SoundLibrarySettingsActivity.class).putExtra("visualReview",true).putExtra("verificationLabel","Synthetic delayed retention fixture").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));});
            assertTrue(device.wait(Until.hasObject(By.text("Keep recordings")),5000));device.findObject(By.text("Keep recordings")).click();assertTrue(device.wait(Until.hasObject(By.desc("1 day")),3000));device.findObject(By.desc("1 day")).click();device.findObject(By.text("Save & apply retention")).click();assertTrue(entered.await(5,TimeUnit.SECONDS));assertTrue(device.wait(Until.hasObject(By.text("Applying…").enabled(false)),3000));
            i.runOnMainSync(()->{Records.factory=context->{throw new AssertionError("Retained operation rebound to global factory");};for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))if(a instanceof SoundLibrarySettingsActivity){owned.add(a);a.recreate();}});
            assertTrue(device.wait(Until.hasObject(By.text("Applying…").enabled(false)),5000));assertTrue(device.hasObject(By.text("Keep recordings").enabled(false)));assertTrue(device.hasObject(By.desc("Keep favorites").enabled(false)));assertEquals(1,calls.get());device.waitForIdle();assertTrue(device.takeScreenshot(new File(folder,"library-retention-busy-recreated.png")));
            release.countDown();String error="Retention saved. Some recordings could not be deleted; apply again to retry.";assertTrue(device.wait(Until.hasObject(By.text(error)),5000));assertEquals(1,Prefs.number(c,"audioRetentionDays",0));assertTrue(child.exists());try(Store store=new Store(c,database)){assertEquals(1,store.rows("SELECT clip FROM pending_clip_deletes").size());}
            i.runOnMainSync(()->{for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))if(a instanceof SoundLibrarySettingsActivity){owned.add(a);a.recreate();}});assertTrue(device.wait(Until.hasObject(By.text(error)),5000));assertEquals(1,calls.get());device.waitForIdle();assertTrue(device.takeScreenshot(new File(folder,"library-retention-error-recreated.png")));
            assertTrue(child.delete());assertTrue(blocked.delete());device.findObject(By.text("Save & apply retention")).click();assertTrue(device.wait(Until.hasObject(By.text("Retention saved.")),5000));assertEquals(2,calls.get());try(Store store=new Store(c,database)){assertTrue(store.rows("SELECT clip FROM pending_clip_deletes").isEmpty());assertTrue(store.rows("SELECT id FROM clips").isEmpty());}device.waitForIdle();assertTrue(device.takeScreenshot(new File(folder,"library-retention-retry.png")));
        }catch(Throwable failure){try{device.takeScreenshot(new File(folder,"library-retention-failure.png"));device.dumpWindowHierarchy(new File(folder,"library-retention-failure.xml"));}catch(Throwable diagnostic){failure.addSuppressed(diagnostic);}throw failure;}
        finally{release.countDown();i.runOnMainSync(()->{for(Stage stage:Stage.values())if(stage!=Stage.DESTROYED)for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(stage))if(a instanceof SoundLibrarySettingsActivity&&!owned.contains(a))owned.add(a);for(Activity a:owned)a.finish();});i.waitForIdleSync();boolean drained=false;try{for(Activity a:owned)assertTrue(((SoundLibrarySettingsActivity)a).awaitReaders(5000));drained=true;}finally{i.runOnMainSync(()->{Records.factory=prior;SoundRetentionJob.factory=factory;SharedPreferences.Editor edit=Prefs.of(c).edit();for(String key:keys){edit.remove(key);Object value=prefs.get(key);if(value instanceof Integer)edit.putInt(key,(Integer)value);else if(value instanceof Boolean)edit.putBoolean(key,(Boolean)value);else if(value instanceof String)edit.putString(key,(String)value);}assertTrue(edit.commit());});if(drained){c.deleteDatabase(database);if(child.exists())assertTrue(child.delete());if(blocked.exists())assertTrue(blocked.delete());}}}
    }
}
