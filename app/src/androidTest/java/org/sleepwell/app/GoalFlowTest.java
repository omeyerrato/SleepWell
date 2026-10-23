package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.*;
import androidx.test.uiautomator.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.time.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

/** Silent end-to-end configuration. Uses an isolated plan, records and a non-delivering scheduler. */
@RunWith(AndroidJUnit4.class)
public class GoalFlowTest {
    private final Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
    private final Context c=instrumentation.getTargetContext();
    private final UiDevice device=UiDevice.getInstance(instrumentation);
    private UiObject2 object(String description){UiObject2 v=device.wait(Until.findObject(By.desc(description)),2000);if(v==null){try{new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().description(description));}catch(UiObjectNotFoundException ignored){}v=device.wait(Until.findObject(By.desc(description)),1000);}assertNotNull(description,v);return v;}
    private void click(String label){UiObject2 v=device.findObject(By.desc(label));boolean description=v!=null;if(v==null)v=device.findObject(By.text(label));if(v==null)v=device.findObject(By.text(java.util.regex.Pattern.compile(java.util.regex.Pattern.quote(label),java.util.regex.Pattern.CASE_INSENSITIVE)));if(v==null||v.getVisibleBounds().height()<35){try{new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(description?new UiSelector().description(label):new UiSelector().text(label));}catch(UiObjectNotFoundException ignored){}v=device.wait(Until.findObject(description?By.desc(label):By.text(label)),2000);}assertNotNull(label,v);assertTrue("Control must be visibly tappable: "+label,v.getVisibleBounds().height()>=35);v.click();device.waitForIdle();}
    private void title(String text){assertTrue(text,device.wait(Until.hasObject(By.text(text)),5000));device.waitForIdle();}
    private Activity current(){Activity[] found={null};instrumentation.runOnMainSync(()->{for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))found[0]=a;});assertNotNull(found[0]);return found[0];}
    private void launch(Class<?> type){Instrumentation.ActivityMonitor monitor=instrumentation.addMonitor(type.getName(),null,false);try{c.startActivity(new Intent(c,type).putExtra("visualReview",true).putExtra("verificationLabel","VISUAL TEST · isolated goal records").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));assertNotNull(monitor.waitForActivityWithTimeout(5000));instrumentation.waitForIdleSync();}finally{instrumentation.removeMonitor(monitor);}}
    private void recreate(){Activity a=current();Instrumentation.ActivityMonitor monitor=instrumentation.addMonitor(a.getClass().getName(),null,false);try{instrumentation.runOnMainSync(a::recreate);assertNotNull(monitor.waitForActivityWithTimeout(5000));instrumentation.waitForIdleSync();}finally{instrumentation.removeMonitor(monitor);}}
    private void capture(String name){device.waitForIdle();SystemClock.sleep(650);instrumentation.runOnMainSync(()->{Activity a=currentOnMain();if(!name.equals("tonight"))assertTrue((a.getWindow().getInsetsController().getSystemBarsAppearance()&WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)!=0);});File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());assertTrue(device.takeScreenshot(new File(folder,"goal-"+name+".png")));}
    private Activity currentOnMain(){for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))return a;throw new AssertionError("No active native screen");}
    private void startDate(LocalDate date){click("Starts");GoalBankActivity activity=(GoalBankActivity)current();instrumentation.runOnMainSync(()->{assertNotNull(activity.startPicker);activity.startPicker.getDatePicker().updateDate(date.getYear(),date.getMonthValue()-1,date.getDayOfMonth());activity.startPicker.getButton(DialogInterface.BUTTON_POSITIVE).performClick();});device.waitForIdle();}
    private void seed(String database,LocalDate currentDay,ZoneId zone,int cutoff){
        try(Store store=new Store(c,database)){
            for(int n=1;n<=7;n++){
                LocalDate label=currentDay.minusDays(n),endDate=cutoff>390?label.plusDays(1):label;
                long end=endDate.atTime(6,30).atZone(zone).toInstant().toEpochMilli();
                if(!SleepDay.label(end,zone,cutoff).equals(label))end=label.plusDays(1).atTime(cutoff/60,cutoff%60).atZone(zone).toInstant().minusSeconds(60).toEpochMilli();
                assertEquals("Fixture must belong to its declared closed sleep day",label,SleepDay.label(end,zone,cutoff));
                RecordDraft draft=new RecordDraft(end-(n%3==0?450:n%3==1?480:510)*60000L,end);draft.note="Isolated manually entered goal test";
                Records.create(store,draft,zone,System.currentTimeMillis());
            }
        }
    }
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
    private Map<String,Object> personalPreferences(){
        Map<String,Object> copy=new HashMap<>();for(Map.Entry<String,?> entry:Prefs.of(c).getAll().entrySet()){
            Object value=entry.getValue();copy.put(entry.getKey(),value instanceof Set?new HashSet<>((Set<?>)value):value);
        }return copy;
    }
    private void stopIsolatedActivities(List<Activity> owned,Set<String> originalTokens) throws Exception {
        // A Save belongs to its operation, so allow it to finish before asking its Activity to close.
        PlanOperations.drain(10000);long deadline=SystemClock.elapsedRealtime()+10000;
        while(true){
            boolean[] destroyed={true};
            instrumentation.runOnMainSync(()->{for(Activity activity:owned){if(!activity.isDestroyed()){destroyed[0]=false;if(!activity.isFinishing())activity.finish();}}});
            instrumentation.waitForIdleSync();
            if(destroyed[0])break;
            if(SystemClock.elapsedRealtime()>=deadline)throw new AssertionError("Isolated Activities did not close; owned files were retained");
            SystemClock.sleep(100);
        }
        for(Activity activity:owned){
            if(activity instanceof PlanActivity)((PlanActivity)activity).awaitReaders(10000);
            else if(activity instanceof GoalBankActivity)((GoalBankActivity)activity).awaitReaders(10000);
            else if(activity instanceof HistoryActivity)((HistoryActivity)activity).awaitReaders(10000);
        }
        PlanOperations.drain(10000);instrumentation.waitForIdleSync();
        for(String token:PlanOperations.tokens())if(!originalTokens.contains(token))PlanOperations.forget(token);
    }
    @Test public void draftSaveCommitmentAndEffectiveProgressPersistWithoutDelivery() throws Exception {
        assertFalse(Prefs.flag(c,"ringing"));assertFalse(TrackingService.running);assertFalse("Do not recover or change a personal tracking state",Prefs.flag(c,"tracking"));
        assertFalse(Prefs.flag(c,"trackingPaused"));try(Store user=new Store(c)){for(Alarm a:user.alarms())assertFalse("Never disable an alarm to run this test",a.enabled);assertTrue("Do not finalize an unfinished personal recording",user.rows("SELECT id FROM sessions WHERE end IS NULL").isEmpty());}
        String personalBefore=personalDatabase();Map<String,Object> preferencesBefore=personalPreferences();
        ZoneId fixtureZone=ZoneId.systemDefault();int fixtureCutoff=Math.max(0,Math.min(1439,Prefs.number(c,"statsCutoff",0)));Instant fixtureNow=Instant.now();
        LocalDate currentDay=SleepDay.label(fixtureNow.toEpochMilli(),fixtureZone,fixtureCutoff),commitmentStart=currentDay.minusDays(7);
        String token=UUID.randomUUID().toString(),database="sleepwell-goal-verification-"+token+".db",preferences="sleepwell-goal-verification-"+token;SharedPreferences isolated=c.getSharedPreferences(preferences,Context.MODE_PRIVATE);
        assertTrue(isolated.edit().putInt("goal",480).putInt("bedtime",1350).putInt("wakeTime",390).putInt("windDownMinutes",30).putLong("planRevision",10).commit());
        SleepPlans.Factory previousPlans=SleepPlans.factory;SleepPlans.Reminder previousReminders=SleepPlans.reminders;Records.Factory previousRecords=Records.factory;AtomicInteger deliveries=new AtomicInteger();
        Set<String> originalTokens=PlanOperations.tokens();List<Activity> owned=new CopyOnWriteArrayList<>();Application application=(Application)c.getApplicationContext();
        Application.ActivityLifecycleCallbacks lifecycle=new Application.ActivityLifecycleCallbacks(){
            public void onActivityCreated(Activity activity,Bundle state){owned.add(activity);}
            public void onActivityStarted(Activity activity){}public void onActivityResumed(Activity activity){}public void onActivityPaused(Activity activity){}
            public void onActivityStopped(Activity activity){}public void onActivitySaveInstanceState(Activity activity,Bundle state){}public void onActivityDestroyed(Activity activity){}
        };
        application.registerActivityLifecycleCallbacks(lifecycle);
        SleepPlans.factory=context->isolated;SleepPlans.reminders=context->deliveries.incrementAndGet();Records.factory=context->new Store(context,database);
        try{
            seed(database,currentDay,fixtureZone,fixtureCutoff);launch(PlanActivity.class);title("Sleep goals");capture("plan");click("Increase sleep goal");click("Reminder");click("30 min before");click("Wind-down");click("60 min before");recreate();object("Sleep duration goal 8 h 15 min");click("Back");assertEquals(480,SleepPlans.load(c).goal);assertEquals(0,deliveries.get());
            launch(PlanActivity.class);title("Sleep goals");click("Increase sleep goal");click("Reminder");click("30 min before");click("Wind-down");click("60 min before");click("Save sleep plan");title("Tonight");SleepPlan saved=SleepPlans.load(c);assertEquals(495,saved.goal);assertEquals(1335,saved.bedtime);assertEquals(390,saved.wake);assertEquals(60,saved.windDown);assertTrue(saved.reminder);assertEquals(30,saved.reminderAdvance);assertEquals(1,deliveries.get());assertTrue(saved.goalMet(495*60000L));assertFalse(saved.goalMet(494*60000L));capture("tonight");
            launch(PlanActivity.class);title("Sleep goals");object("Sleep duration goal 8 h 15 min");capture("saved-plan");click("Goal bank");title("Goal bank");title("Build a sleep habit");capture("empty-bank");click("Set your first goal");title("Set a goal");click("30 days");object("Goal motivation").setText("Put the phone down before bed");recreate();title("Set a goal");assertEquals("Put the phone down before bed",object("Goal motivation").getText());click("Back");title("Goal bank");assertTrue(SleepPlans.goals(c).isEmpty());
            click("Set your first goal");title("Set a goal");startDate(commitmentStart);object("Goal motivation").setText("A rested morning");capture("create");click("Start goal");title("Goal bank");assertEquals(1,SleepPlans.goals(c).size());GoalCommitment goal=SleepPlans.goals(c).get(0);assertEquals(495,goal.target);assertEquals(fixtureCutoff,goal.cutoff);assertEquals(fixtureZone.getId(),goal.zone);try(Store store=new Store(c,database)){GoalProgress p=GoalProgress.calculate(goal,Records.all(store),fixtureNow);assertEquals(7,p.observed);assertEquals(0,p.unknown);assertTrue(p.balance<0);}capture("bank");
            launch(GoalBankActivity.class);title("Goal bank");title("Daily surplus & shortfall");capture("reopened-bank");new UiScrollable(new UiSelector().scrollable(true)).scrollToEnd(6);device.waitForIdle();capture("bank-details");click("Stop this goal");title("Stop this goal?");click("Stop goal");title("Stopped · progress kept");assertNotNull(SleepPlans.goals(c).get(0).stopped);capture("stopped");
            launch(HistoryActivity.class);title("Sleep history");click("Trends");title("Trends");assertTrue(device.wait(Until.hasObject(By.descStartsWith("Recorded duration.")),5000));capture("trends");assertEquals(1,deliveries.get());
        }finally{
            boolean quiescent=false;
            try{stopIsolatedActivities(owned,originalTokens);quiescent=true;}
            finally{
                application.unregisterActivityLifecycleCallbacks(lifecycle);
                SleepPlans.factory=previousPlans;SleepPlans.reminders=previousReminders;Records.factory=previousRecords;
                try{
                    c.startActivity(new Intent(c,MainActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));instrumentation.waitForIdleSync();device.waitForIdle();
                    assertTrue("Personal database rows changed during isolated goal verification",personalBefore.equals(personalDatabase()));
                    assertTrue("Personal preferences must not be changed",preferencesBefore.equals(personalPreferences()));
                }finally{
                    // A failed barrier retains private verification files instead of deleting under a writer.
                    if(quiescent){
                        c.deleteDatabase(database);c.deleteSharedPreferences(preferences);
                        assertFalse("Owned database must be removed",Arrays.asList(c.databaseList()).contains(database));
                        File directory=new File(c.getApplicationInfo().dataDir,"shared_prefs");
                        assertFalse("Owned preferences must be removed",new File(directory,preferences+".xml").exists());
                        assertFalse(new File(directory,preferences+".xml.bak").exists());
                    }
                }
            }
        }
    }
}
