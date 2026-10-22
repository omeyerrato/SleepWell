package org.sleepwell.app;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import androidx.test.uiautomator.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.Assert.*;

/** Delays isolated save ownership; never starts tracking, alarms or audio. */
@RunWith(AndroidJUnit4.class)
public class RecordRecreationTest {
    private final Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
    private final Context context=instrumentation.getTargetContext();
    private final UiDevice device=UiDevice.getInstance(instrumentation);

    @Test public void pendingNewAndExistingSavesReattachAfterActivityRecreation() throws Exception {
        assertFalse("Do not interrupt an alarm",Prefs.flag(context,"ringing"));
        assertFalse("Do not interrupt recording",TrackingService.running);
        Records.Factory previous=Records.factory;
        try{
            for(boolean existing:new boolean[]{false,true}){
                String name="sleepwell-recreation-verification-"+UUID.randomUUID()+".db";
                CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1),saveClosed=new CountDownLatch(1);
                AtomicBoolean delayNextSave=new AtomicBoolean();
                long id=0;
                if(existing)try(Store store=new Store(context,name)){
                    long end=System.currentTimeMillis()-3600000;
                    RecordDraft original=new RecordDraft(end-8*3600000,end);original.note="Before pending edit";
                    id=Records.create(store,original,ZoneId.systemDefault(),System.currentTimeMillis());
                }
                Records.factory=c->{
                    boolean ownedSave=delayNextSave.compareAndSet(true,false);
                    if(ownedSave){
                        entered.countDown();
                        try{if(!release.await(10,TimeUnit.SECONDS))throw new IllegalStateException("Verification save was not released");}
                        catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException(e);}
                    }
                    return new Store(c,name){
                        @Override public synchronized void close(){try{super.close();}finally{if(ownedSave)saveClosed.countDown();}}
                    };
                };
                try{
                    launch(id);String title=existing?"Edit sleep":"Add sleep";
                    assertTrue(device.wait(Until.hasObject(By.text(title)),5000));
                    UiObject2 notes=device.wait(Until.findObject(By.desc("Notes or dream")),5000);assertNotNull(notes);
                    String expected=existing?"One committed edit after recreation":"One committed creation after recreation";
                    notes.setText(expected);delayNextSave.set(true);
                    UiObject2 save=device.wait(Until.findObject(By.text("Save changes")),5000);assertNotNull(save);save.click();
                    assertTrue("Save worker must enter the controlled delay",entered.await(5,TimeUnit.SECONDS));
                    recreate();
                    UiObject2 saving=device.wait(Until.findObject(By.text("Saving…")),5000);assertNotNull("Recreated editor must retain the pending save",saving);
                    assertFalse("A pending operation must not offer a second Save",saving.isEnabled());
                    device.pressBack();assertTrue("Back must keep the submitted save visible",device.wait(Until.hasObject(By.text("Saving…")),2000));
                    release.countDown();
                    assertTrue("The recreated editor must observe completion and finish",device.wait(Until.hasObject(By.text("Sleep history")),5000));
                    assertTrue(device.wait(Until.gone(By.text("Loading records…")),5000));
                    try(Store store=new Store(context,name)){
                        assertEquals("Recreation must neither duplicate nor lose the period",1,store.rows("SELECT id FROM sessions").size());
                        assertEquals(expected,Records.all(store).get(0).edit.note);
                        assertTrue(store.rows("SELECT id FROM epochs").isEmpty());
                        assertTrue(store.rows("SELECT id FROM tracking_pauses").isEmpty());
                    }
                }finally{
                    release.countDown();
                    boolean closed=entered.getCount()!=0||saveClosed.await(5,TimeUnit.SECONDS);
                    // Close the isolated task before deleting its database; keep the restored app awake.
                    Records.factory=previous;
                    context.startActivity(new Intent(context,MainActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    instrumentation.waitForIdleSync();device.waitForIdle();
                    if(closed)context.deleteDatabase(name);
                    assertTrue("The isolated save must close before its database is removed",closed);
                }
            }
        }finally{Records.factory=previous;}
    }

    private void launch(long id){
        Instrumentation.ActivityMonitor monitor=instrumentation.addMonitor(RecordEditorActivity.class.getName(),null,false);
        try{
            context.startActivity(new Intent(context,RecordEditorActivity.class).putExtra("record",id).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            assertNotNull(monitor.waitForActivityWithTimeout(5000));instrumentation.waitForIdleSync();
        }finally{instrumentation.removeMonitor(monitor);}
    }
    private void recreate(){
        Activity[] current={null};instrumentation.runOnMainSync(()->{for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))if(a instanceof RecordEditorActivity)current[0]=a;});
        assertNotNull(current[0]);Instrumentation.ActivityMonitor monitor=instrumentation.addMonitor(RecordEditorActivity.class.getName(),null,false);
        try{instrumentation.runOnMainSync(current[0]::recreate);assertNotNull(monitor.waitForActivityWithTimeout(5000));instrumentation.waitForIdleSync();}
        finally{instrumentation.removeMonitor(monitor);}
    }
}
