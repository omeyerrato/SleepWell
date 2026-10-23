package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.database.sqlite.SQLiteDiskIOException;
import android.os.SystemClock;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.*;
import androidx.test.uiautomator.*;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.Assert.*;

/** Controlled movement-only tests. No alarm changes, microphone, media, vibration or screen-power actions. */
@RunWith(AndroidJUnit4.class)
public class TrackingFlowTest {
    private final Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
    private final Context context=instrumentation.getTargetContext();private final UiDevice device=UiDevice.getInstance(instrumentation);
    private final String[] keys={"noiseEnabled","saveClips","lullaby","mixWithTracking","recordLight","tracking","trackingPaused","session","epochCount","movement","audioError","trackingError","lastError"};
    private void preconditions(){assertNull("Do not disturb sleep-sound playback",SleepSoundService.snapshot());assertFalse("Never disturb a running alarm",Prefs.flag(context,"ringing"));assertFalse("Never replace another recording",TrackingService.running);assertEquals(0,Prefs.number(context,"checkPending",0));try(Store store=new Store(context)){for(Alarm alarm:store.alarms())assertFalse("Recording test requires alarms already off; never disable them for this test",alarm.enabled);}}
    private void quiet(){Prefs.of(context).edit().putBoolean("noiseEnabled",false).putBoolean("saveClips",false).putString("lullaby","Off").putBoolean("mixWithTracking",false).putBoolean("recordLight",false).apply();assertFalse(Prefs.flag(context,"noiseEnabled"));assertFalse(Prefs.flag(context,"saveClips"));assertEquals("Off",Prefs.text(context,"lullaby",""));}
    private void restore(Map<String,?> original){SharedPreferences.Editor e=Prefs.of(context).edit();for(String key:keys){Object v=original.get(key);if(v instanceof Boolean)e.putBoolean(key,(Boolean)v);else if(v instanceof Long)e.putLong(key,(Long)v);else if(v instanceof Integer)e.putInt(key,(Integer)v);else if(v instanceof String)e.putString(key,(String)v);else e.remove(key);}assertTrue(e.commit());}
    private void launch(){Instrumentation.ActivityMonitor monitor=instrumentation.addMonitor(MainActivity.class.getName(),null,false);try{context.startActivity(new Intent(context,MainActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));assertNotNull(monitor.waitForActivityWithTimeout(5000));instrumentation.waitForIdleSync();device.waitForIdle();}finally{instrumentation.removeMonitor(monitor);}}
    private void click(String title){UiObject2 button=device.findObject(By.desc(title));if(button==null)button=device.wait(Until.findObject(By.text(title)),5000);assertNotNull(title,button);button.click();device.waitForIdle();}
    private void capture(String name){File folder=new File(context.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());device.waitForIdle();assertTrue(device.takeScreenshot(new File(folder,"tracking-"+name+".png")));}
    private RecordChart chart(android.view.View view){if(view instanceof RecordChart)return (RecordChart)view;if(view instanceof android.view.ViewGroup){android.view.ViewGroup group=(android.view.ViewGroup)view;for(int i=0;i<group.getChildCount();i++){RecordChart found=chart(group.getChildAt(i));if(found!=null)return found;}}return null;}
    private TrackingService.Snapshot state(){final TrackingService.Snapshot[] value={null};instrumentation.runOnMainSync(()->value[0]=TrackingService.snapshot());return value[0];}
    private TrackingService.Snapshot await(String phase,int samples){long end=SystemClock.elapsedRealtime()+7000;while(SystemClock.elapsedRealtime()<end){TrackingService.Snapshot s=state();if(s!=null&&s.phase.equals(phase)&&s.samples>=samples)return s;SystemClock.sleep(100);}fail("Tracking phase not reached: "+phase);return null;}
    private void stopFinally(){if(TrackingService.running)context.startService(new Intent(context,TrackingService.class).setAction(TrackingService.STOP));long until=SystemClock.elapsedRealtime()+7000;while(TrackingService.running&&SystemClock.elapsedRealtime()<until)SystemClock.sleep(100);assertFalse("Service must be closed before restoring test preferences",TrackingService.running);}
    private Map<String,String> clipHashes() throws Exception {Map<String,String> result=new TreeMap<>();File[] clips=new File(context.getFilesDir(),"clips").listFiles();if(clips!=null)for(File file:clips){MessageDigest digest=MessageDigest.getInstance("SHA-256");try(InputStream input=new FileInputStream(file)){byte[] buffer=new byte[8192];int read;while((read=input.read(buffer))!=-1)digest.update(buffer,0,read);}result.put(file.getName(),Base64.getEncoder().encodeToString(digest.digest()));}return result;}
    @Test public void setupPauseRecreateResumeStopAndReopenReport() throws Exception {
        preconditions();Map<String,?> preferences=Prefs.of(context).getAll();Map<String,String> clipsBefore=clipHashes();TrackingService.StoreFactory originalFactory=TrackingService.storeFactory;AtomicLong created=new AtomicLong();long maximum;
        try(Store store=new Store(context)){maximum=store.rows("SELECT COALESCE(MAX(id),0) AS id FROM sessions").get(0).optLong("id");}
        TrackingService.storeFactory=c->new Store(c){@Override public long begin(){long id=super.begin();created.set(id);return id;}};
        try{
            quiet();launch();UiObject2 start=device.wait(Until.findObject(By.desc("Start sleep tracking")),5000);assertNotNull(start);start.click();assertTrue(device.wait(Until.hasObject(By.text("Tracking setup")),5000));assertFalse(device.findObject(By.desc("Record sounds")).isChecked());
            // Toggle actual light preference and start with it off; no light registration may occur.
            UiObject2 light=device.findObject(By.desc("Record light levels"));if(light!=null){light.click();light.click();assertFalse(light.isChecked());}capture("setup");click("Start tracking  →");
            TrackingService.Snapshot s=await("ACTIVE",0);long id=s.id;assertEquals(created.get(),id);assertTrue(id>maximum);assertFalse(s.mic);assertFalse(s.light);assertFalse(Prefs.flag(context,"recordLight"));assertTrue(device.wait(Until.hasObject(By.text("Tracking sleep")),5000));capture("collecting");
            s=await("ACTIVE",12);SystemClock.sleep(10000);assertTrue(s.motion);assertNotNull(s.movement);capture("live");final RecordChart[] selected={null};instrumentation.runOnMainSync(()->{for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))if(a instanceof TrackingActivity)selected[0]=chart(a.getWindow().getDecorView());assertNotNull(selected[0]);selected[0].setFocusableInTouchMode(true);assertTrue(selected[0].requestFocus());assertTrue(selected[0].performAccessibilityAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SCROLL_FORWARD,null));});SystemClock.sleep(1500);instrumentation.runOnMainSync(()->{assertTrue(selected[0].hasFocus());assertTrue(selected[0].getContentDescription().toString().startsWith("movement at "));assertNotNull(selected[0].getParent());});click("Pause tracking");s=await("PAUSED",0);assertTrue(device.wait(Until.hasObject(By.text("Tracking paused")),5000));long frozen=s.active;assertFalse(s.motion);assertFalse(s.light);assertFalse(s.mic);capture("paused");SystemClock.sleep(1500);assertEquals(frozen,state().active);
            final Activity[] active={null};instrumentation.runOnMainSync(()->{for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))active[0]=a;});assertTrue(active[0] instanceof TrackingActivity);Instrumentation.ActivityMonitor monitor=instrumentation.addMonitor(TrackingActivity.class.getName(),null,false);try{instrumentation.runOnMainSync(active[0]::recreate);assertNotNull(monitor.waitForActivityWithTimeout(5000));instrumentation.waitForIdleSync();}finally{instrumentation.removeMonitor(monitor);}assertTrue(device.wait(Until.hasObject(By.text("Tracking paused")),5000));assertEquals(id,state().id);assertTrue(Prefs.flag(context,"trackingPaused"));
            click("Resume tracking");s=await("ACTIVE",12);assertEquals(id,s.id);assertTrue(s.active>frozen);assertFalse(s.mic);assertFalse(s.light);capture("resumed");click("Pause tracking");await("PAUSED",0);assertTrue(device.wait(Until.hasObject(By.text("Tracking paused")),5000));click("Stop tracking");UiObject2 end=device.wait(Until.findObject(By.text(java.util.regex.Pattern.compile("End & review",java.util.regex.Pattern.CASE_INSENSITIVE))),5000);assertNotNull("End confirmation",end);end.click();long until=SystemClock.elapsedRealtime()+7000;while(TrackingService.running&&SystemClock.elapsedRealtime()<until)SystemClock.sleep(100);assertFalse(TrackingService.running);assertTrue(device.wait(Until.hasObject(By.text("Sleep report")),5000));capture("report");
            try(Store store=new Store(context)){
                JSONObject session=store.rows("SELECT * FROM sessions WHERE id=?",""+id).get(0);assertFalse(session.isNull("end"));assertEquals(0,session.optInt("interrupted"));List<JSONObject> pauses=store.rows("SELECT * FROM tracking_pauses WHERE session=? ORDER BY start",""+id),epochs=store.rows("SELECT * FROM epochs WHERE session=? ORDER BY time",""+id);assertEquals(2,pauses.size());assertEquals(2,epochs.size());assertTrue(epochs.get(0).getInt("samples")>=12);assertTrue(epochs.get(1).getInt("samples")>=12);assertTrue(epochs.get(1).getLong("start")>=pauses.get(0).getLong("end"));for(JSONObject epoch:epochs){assertTrue(epoch.isNull("noise"));assertTrue(epoch.isNull("light"));assertFalse(epoch.isNull("movement"));}assertTrue(session.getLong("active_ms")>0);assertTrue(session.getLong("end")-session.getLong("start")-session.getLong("active_ms")>=1000);Backup.validate(store.backup());
            }
            launch();for(UiObject2 tab:device.findObjects(By.desc("Report")))if(tab.getVisibleBounds().centerY()>device.getDisplayHeight()*.7f){tab.click();break;}assertTrue(device.wait(Until.hasObject(By.text("Sleep report")),5000));assertTrue(device.hasObject(By.text("Paused time is excluded from recorded duration.")));capture("reopened");assertEquals(clipsBefore,clipHashes());
        }finally{
            stopFinally();TrackingService.storeFactory=originalFactory;long id=created.get();if(id>maximum)try(Store store=new Store(context)){assertTrue(store.rows("SELECT id FROM clips WHERE session=?",""+id).isEmpty());store.getWritableDatabase().delete("epochs","session=?",new String[]{""+id});store.getWritableDatabase().delete("tracking_pauses","session=?",new String[]{""+id});store.getWritableDatabase().delete("sessions","id=?",new String[]{""+id});}restore(preferences);launch();
        }
    }
    @Test public void injectedPersistenceFailureStopsCleanly() throws Exception {
        preconditions();Map<String,?> preferences=Prefs.of(context).getAll();TrackingService.StoreFactory previous=TrackingService.storeFactory;String name="sleepwell-verification-failure-"+UUID.randomUUID()+".db";
        try{
            quiet();launch();TrackingService.storeFactory=c->new Store(c,name){@Override void epoch(long session,Long start,long time,Double movement,Double noise,Double light,int count){throw new SQLiteDiskIOException("Injected verification write failure");}};
            context.startForegroundService(new Intent(context,TrackingService.class));await("ACTIVE",4);context.startService(new Intent(context,TrackingService.class).setAction(TrackingService.PAUSE));long until=SystemClock.elapsedRealtime()+7000;while(TrackingService.running&&SystemClock.elapsedRealtime()<until)SystemClock.sleep(100);assertFalse(TrackingService.running);assertNull(state());assertFalse(Prefs.flag(context,"tracking"));assertTrue(Prefs.text(context,"trackingError","").contains("Injected verification write failure"));
            try(Store store=new Store(context,name)){assertEquals(1,store.rows("SELECT interrupted FROM sessions").get(0).getInt("interrupted"));assertTrue(store.rows("SELECT * FROM epochs").isEmpty());}
        }finally{stopFinally();TrackingService.storeFactory=previous;context.deleteDatabase(name);restore(preferences);launch();}
    }
}
