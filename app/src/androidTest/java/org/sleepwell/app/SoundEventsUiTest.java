package org.sleepwell.app;

import android.app.Activity;
import android.content.*;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.*;
import androidx.test.uiautomator.*;
import org.junit.Test;
import java.io.File;
import java.util.*;
import static org.junit.Assert.*;

/** Category/navigation state only, no microphone or playback. Synthetic data labeled in every capture. */
public class SoundEventsUiTest {
    @Test public void filtersPersistThroughRecreationWithoutLaunchingAnyOutputs() throws Exception {
        android.app.Instrumentation i=InstrumentationRegistry.getInstrumentation();Context c=i.getTargetContext();
        org.junit.Assume.assumeTrue(android.os.Build.HARDWARE.contains("ranchu")||android.os.Build.HARDWARE.contains("goldfish"));
        org.junit.Assume.assumeTrue("Explicit CI renderer only",InstrumentationRegistry.getArguments().getString("ciReview","").equals("true"));
        UiDevice device=UiDevice.getInstance(i);String database="sleepwell-verification-"+UUID.randomUUID()+".db";Records.Factory before=Records.factory;List<Activity> owned=new ArrayList<>();
        try{
            long id;try(Store store=new Store(c,database)){id=store.begin();long start=store.rows("SELECT start FROM sessions WHERE id=?",""+id).get(0).getLong("start");for(int n=0;n<4;n++)store.soundEvent(id,start+n*20000,1950,n==2?"speech":"snoring",.8,null);store.end(id,false,90000,start+90000);}
            i.runOnMainSync(()->Records.factory=context->new Store(context,database));
            long session=id;i.runOnMainSync(()->{c.startActivity(new Intent(c,SoundEventsActivity.class).putExtra("record",session).putExtra("visualReview",true).putExtra("verificationLabel","Synthetic classifier UI fixture").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));});
            assertTrue(device.wait(Until.hasObject(By.text("Snoring estimate")),5000));
            File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());device.waitForIdle();assertTrue(device.takeScreenshot(new File(folder,"analysis-events-all.png")));
            device.findObject(By.desc("Speech (1)")).click();assertTrue(device.wait(Until.gone(By.text("Snoring estimate")),3000));assertTrue(device.hasObject(By.text("Speech estimate")));device.waitForIdle();assertTrue(device.takeScreenshot(new File(folder,"analysis-events-speech.png")));
            i.runOnMainSync(()->{for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))if(a instanceof SoundEventsActivity){owned.add(a);a.recreate();}});assertTrue(device.wait(Until.hasObject(By.desc("Speech (1)").selected(true)),5000));assertFalse(device.hasObject(By.text("Snoring estimate")));device.waitForIdle();assertTrue(device.takeScreenshot(new File(folder,"analysis-events-recreated.png")));
        }catch(Throwable failure){
            try{File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());device.takeScreenshot(new File(folder,"analysis-events-failure.png"));device.dumpWindowHierarchy(new File(folder,"analysis-events-failure.xml"));try(Store store=new Store(c,database)){java.nio.file.Files.write(new File(folder,"analysis-events-fixture.json").toPath(),new org.json.JSONObject().put("sessions",new org.json.JSONArray(store.rows("SELECT * FROM sessions"))).put("events",new org.json.JSONArray(store.rows("SELECT * FROM sound_events"))).toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));}}catch(Throwable diagnostic){failure.addSuppressed(diagnostic);}throw failure;
        }finally{i.runOnMainSync(()->{for(Stage stage:Stage.values())if(stage!=Stage.DESTROYED)for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(stage))if(a instanceof SoundEventsActivity&&!owned.contains(a))owned.add(a);for(Activity a:owned)a.finish();});i.waitForIdleSync();boolean drained=false;try{for(Activity a:owned)assertTrue("Finish sound readers before restoring fixture scope",((SoundEventsActivity)a).awaitReaders(5000));drained=true;}finally{i.runOnMainSync(()->Records.factory=before);if(drained)c.deleteDatabase(database);}}
    }
}
