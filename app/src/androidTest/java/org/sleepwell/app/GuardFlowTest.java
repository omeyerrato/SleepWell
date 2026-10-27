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
import org.json.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import static org.junit.Assert.*;

/** Isolated preference draft/save/pause only. Does not grant access or launch blocked apps. */
@RunWith(AndroidJUnit4.class)
public class GuardFlowTest {
    private final Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
    private final Context c=instrumentation.getTargetContext();private final UiDevice device=UiDevice.getInstance(instrumentation);
    private void click(String text){UiObject2 view=device.findObject(By.desc(text));if(view==null)view=device.wait(Until.findObject(By.text(text)),4000);assertNotNull(text,view);view.click();device.waitForIdle();}
    private GuardActivity launch(){Instrumentation.ActivityMonitor monitor=instrumentation.addMonitor(GuardActivity.class.getName(),null,false);try{c.startActivity(new Intent(c,GuardActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));Activity activity=monitor.waitForActivityWithTimeout(5000);assertNotNull(activity);assertTrue(device.wait(Until.hasObject(By.desc("Choose other apps").enabled(true)),5000));device.waitForIdle();return (GuardActivity)activity;}finally{instrumentation.removeMonitor(monitor);}}
    private void capture(String name){device.waitForIdle();SystemClock.sleep(500);File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());assertTrue(device.takeScreenshot(new File(folder,"guard-"+name+".png")));}
    private String personalDatabase() throws Exception {
        try(Store store=new Store(c)){
            JSONObject snapshot=new JSONObject();
            for(String table:new String[]{"alarms","sessions","epochs","clips","tracking_pauses","record_edits","record_create_requests"}){
                String order=table.equals("record_edits")?"session":table.equals("record_create_requests")?"token":"id";
                snapshot.put(table,new JSONArray(store.rows("SELECT * FROM "+table+" ORDER BY "+order)));
            }
            return snapshot.toString();
        }
    }
    @Test public void blockedAppDraftCancelsAndSavedChoicesReopen()throws Exception{
        assertFalse(Prefs.flag(c,"ringing"));assertFalse(TrackingService.running);assertFalse(Prefs.flag(c,"tracking"));assertFalse(Prefs.flag(c,"trackingPaused"));try(Store original=new Store(c)){assertTrue("Do not finalize an unfinished personal recording",original.rows("SELECT id FROM sessions WHERE end IS NULL").isEmpty());}String personalRows=personalDatabase();Map<String,?> personal=new HashMap<>(Prefs.of(c).getAll());String name="sleepwell-guard-verification-"+UUID.randomUUID();SharedPreferences isolated=c.getSharedPreferences(name,Context.MODE_PRIVATE);assertTrue(isolated.edit().putBoolean("bedGuard",true).commit());GuardActivity.Preferences previous=GuardActivity.preferences;GuardActivity.preferences=context->isolated;
        List<GuardActivity> owned=new CopyOnWriteArrayList<>();Application application=(Application)c.getApplicationContext();Application.ActivityLifecycleCallbacks lifecycle=new Application.ActivityLifecycleCallbacks(){public void onActivityCreated(Activity a,Bundle b){if(a instanceof GuardActivity)owned.add((GuardActivity)a);}public void onActivityStarted(Activity a){}public void onActivityResumed(Activity a){}public void onActivityPaused(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,Bundle b){}public void onActivityDestroyed(Activity a){}};application.registerActivityLifecycleCallbacks(lifecycle);Set<String> tokens=PlanOperations.tokens();
        try{
            GuardActivity first=launch();UiObject2 app=device.findObject(By.descStartsWith("Block "));assertNotNull("At least one actual launchable app",app);String chosen=app.getContentDescription();app.click();UiObject2 schedule=device.findObject(By.desc("Bedtime schedule"));assertNotNull(schedule);schedule.click();instrumentation.runOnMainSync(first::recreate);assertTrue(device.wait(Until.hasObject(By.desc("Choose other apps").enabled(true)),5000));assertTrue(device.findObject(By.desc(chosen)).isChecked());assertFalse(device.findObject(By.desc("Bedtime schedule")).isChecked());capture("draft");device.findObject(By.desc("Back")).click();device.waitForIdle();assertTrue(isolated.getBoolean("bedGuard",false));assertTrue(isolated.getStringSet("blocked",Set.of()).isEmpty());
            launch();UiObject2 selected=device.findObject(By.desc(chosen));assertNotNull(selected);assertFalse(selected.isChecked());selected.click();click("Save blocked apps");assertTrue(device.wait(Until.gone(By.text("Save blocked apps")),5000));assertEquals(1,isolated.getStringSet("blocked",Set.of()).size());assertTrue(isolated.getBoolean("bedGuard",false));
            GuardActivity reopened=launch();assertTrue(device.findObject(By.desc(chosen)).isChecked());assertTrue(device.findObject(By.desc("Bedtime schedule")).isChecked());capture("saved");
            UiObject2 choose=device.findObject(By.desc("Choose other apps"));assertNotNull(choose);choose.click();assertTrue(device.wait(Until.hasObject(By.text("Choose apps to block")),5000));UiObject2 draftChoice=device.findObject(By.desc(chosen));assertNotNull(draftChoice);assertTrue(draftChoice.isChecked());draftChoice.click();assertFalse(device.findObject(By.desc(chosen)).isChecked());assertEquals("Picker draft is not saved",1,isolated.getStringSet("blocked",Set.of()).size());
            Instrumentation.ActivityMonitor pickerMonitor=instrumentation.addMonitor(GuardActivity.class.getName(),null,false);try{instrumentation.runOnMainSync(reopened::recreate);assertNotNull(pickerMonitor.waitForActivityWithTimeout(5000));}finally{instrumentation.removeMonitor(pickerMonitor);}assertTrue(device.wait(Until.hasObject(By.text("Choose apps to block")),5000));assertTrue(device.wait(Until.hasObject(By.desc(chosen).checked(false)),5000));capture("options-recreated");click("Cancel");assertTrue(device.wait(Until.gone(By.text("Choose apps to block")),5000));assertTrue(device.findObject(By.desc(chosen)).isChecked());assertEquals("Cancel retains saved blocked apps",1,isolated.getStringSet("blocked",Set.of()).size());assertTrue(isolated.getBoolean("bedGuard",false));
            long before=System.currentTimeMillis();click("Pause for 5 min");long until=isolated.getLong("focusPauseUntil",0);assertTrue(until>=before+299000&&until<=System.currentTimeMillis()+301000);capture("paused");assertTrue("Personal preferences changed",personal.equals(Prefs.of(c).getAll()));assertTrue("Personal rows changed",personalRows.equals(personalDatabase()));
        }finally{
            boolean closed=false;try{PlanOperations.drain(10000);long deadline=SystemClock.elapsedRealtime()+10000;while(true){boolean[] done={true};instrumentation.runOnMainSync(()->{for(GuardActivity a:owned)if(!a.isDestroyed()){done[0]=false;a.finish();}});instrumentation.waitForIdleSync();if(done[0])break;if(SystemClock.elapsedRealtime()>deadline)throw new AssertionError("Guard Activity did not close; retaining owned preferences");SystemClock.sleep(100);}for(GuardActivity a:owned)a.awaitReaders(10000);PlanOperations.drain(10000);closed=true;}finally{application.unregisterActivityLifecycleCallbacks(lifecycle);GuardActivity.preferences=previous;if(closed){for(String token:PlanOperations.tokens())if(!tokens.contains(token))PlanOperations.forget(token);c.deleteSharedPreferences(name);}c.startActivity(new Intent(c,MainActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}assertTrue("Personal preferences changed",personal.equals(Prefs.of(c).getAll()));assertTrue("Personal rows changed",personalRows.equals(personalDatabase()));
        }
    }
}
