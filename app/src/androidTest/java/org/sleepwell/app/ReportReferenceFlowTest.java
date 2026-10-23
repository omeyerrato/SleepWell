package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.*;
import androidx.test.uiautomator.*;
import org.json.JSONObject;
import org.junit.Test;
import java.io.File;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.Assert.*;

/** Disposable, host-muted CI emulator only. Every graph is an explicitly labelled DB fixture. */
public final class ReportReferenceFlowTest {
    private final Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
    private final Context context=instrumentation.getTargetContext();
    private final UiDevice device=UiDevice.getInstance(instrumentation);
    private UiObject2 object(String name)throws Exception{UiObject2 found=device.wait(Until.findObject(By.desc(name)),1800);if(found==null||found.getVisibleBounds().height()<Design.dp(context,32)){new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().description(name));found=device.wait(Until.findObject(By.desc(name)),3000);}assertNotNull(name,found);android.graphics.Rect bounds=found.getVisibleBounds();if(bounds.height()<Design.dp(context,32)){UiObject2 scroll=device.findObject(By.scrollable(true));assertNotNull("Scroll area for "+name,scroll);scroll.scroll(Direction.DOWN,.35f);found=device.wait(Until.findObject(By.desc(name)),3000);}assertNotNull(name,found);assertTrue("Visible control "+name,found.getVisibleBounds().height()>=Design.dp(context,24));return found;}
    private void click(String text)throws Exception{UiObject2 found=device.wait(Until.findObject(By.text(text)),1800);if(found==null)found=device.findObject(By.desc(text));if(found==null){new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().text(text));found=device.findObject(By.text(text));}assertNotNull(text,found);found.click();device.waitForIdle();}
    private void title(String value){assertTrue(value,device.wait(Until.hasObject(By.text(value)),5000));}
    private void launch(Class<?> activity,long id){context.startActivity(new Intent(context,activity).putExtra("record",id).putExtra("visualReview",true).putExtra("verificationLabel","VISUAL TEST · seven isolated fixture nights").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));}
    private boolean ready(Activity activity,View decor){return !activity.isFinishing()&&!activity.isDestroyed()&&ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).contains(activity)&&activity.getWindow().getDecorView()==decor&&activity.hasWindowFocus()&&decor.hasWindowFocus()&&decor.isAttachedToWindow()&&decor.isLaidOut()&&decor.getWidth()>0&&decor.getHeight()>0;}
    private void capture(String name)throws Exception{
        long until=SystemClock.elapsedRealtime()+8000;boolean committed=false;
        while(!committed&&SystemClock.elapsedRealtime()<until){
            CountDownLatch frame=new CountDownLatch(1);Activity[] owner={null};View[] decor={null};
            instrumentation.runOnMainSync(()->{for(Activity activity:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)){
                View root=activity.getWindow().getDecorView();if(!ready(activity,root))continue;
                assertTrue("Screenshots require a native hardware frame",root.isHardwareAccelerated());owner[0]=activity;decor[0]=root;
                root.getViewTreeObserver().registerFrameCommitCallback(frame::countDown);root.invalidate();break;
            }});
            if(owner[0]==null){SystemClock.sleep(50);continue;}
            if(!frame.await(Math.min(1000,Math.max(1,until-SystemClock.elapsedRealtime())),TimeUnit.MILLISECONDS))continue;
            device.waitForIdle();boolean[] stillReady={false};instrumentation.runOnMainSync(()->stillReady[0]=ready(owner[0],decor[0]));committed=stillReady[0];
        }
        assertTrue("A resumed, attached, laid-out, focused report window must commit its own frame: "+name,committed);
        if(name.equals("week")||name.equals("month")||name.equals("year")){
            UiObject2 action=device.findObject(By.desc("Read details")),dock=device.findObject(By.desc("Report").selected(true));assertNotNull("Read details on first trend frame",action);assertNotNull("Selected Report dock",dock);assertTrue("Read details must retain its complete touch target above the dock without scrolling",action.getVisibleBounds().height()>=Design.dp(context,48)&&action.getVisibleBounds().bottom<=dock.getVisibleBounds().top);
        }
        File folder=new File(context.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());assertTrue(device.takeScreenshot(new File(folder,"report-reference-"+name+".png")));
    }
    private void seed(Store store){ZoneId zone=ZoneId.systemDefault();LocalDate latest=LocalDate.now(zone).minusDays(1);int[] durations={462,455,485,430,475,450,483};android.database.sqlite.SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();try{for(int night=0;night<7;night++){
        long end=latest.minusDays(night).atTime(6,30).atZone(zone).toInstant().toEpochMilli(),start=end-(durations[night]+(night==3?10:0))*60000L;long id=2000+night;
        db.execSQL("INSERT INTO sessions(id,start,end,active_ms,note,source,origin,zone) VALUES(?,?,?,?,?,'tracked','local',?)",new Object[]{id,start,end,durations[night]*60000L,"Synthetic CI sensor fixture, never a personal recording",zone.getId()});
        if(night==3){store.pause(id,start+120*60000L);store.resume(id,start+130*60000L);}
        for(int minute=0;minute<(end-start)/60000;minute++){if(night==3&&minute>=120&&minute<130)continue;double movement=.015+.05*(1+Math.sin(minute*.11+night))+.12*(minute%91<5?1:0);store.epoch(id,start+minute*60000L,start+(minute+1)*60000L,movement,null,null,300);}
    }db.setTransactionSuccessful();}finally{db.endTransaction();}}
    private String original(Store store){return store.rows("SELECT * FROM sessions ORDER BY id").toString()+store.rows("SELECT * FROM epochs ORDER BY session,time").toString()+store.rows("SELECT * FROM tracking_pauses ORDER BY id").toString();}
    @Test public void referenceHistoryTrendsEditAndNightReportPersistWithoutEffects()throws Exception{
        assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertEquals("1",device.executeShellCommand("getprop ro.kernel.qemu").trim());assertFalse(Prefs.flag(context,"ringing"));assertFalse(AlarmService.active);assertFalse(TrackingService.running);
        String database="sleepwell-report-reference-"+UUID.randomUUID()+".db";Map<String,?> preferences=new HashMap<>(Prefs.of(context).getAll());Records.Factory previous=Records.factory;List<Activity> owned=new CopyOnWriteArrayList<>();Application application=(Application)context.getApplicationContext();
        Application.ActivityLifecycleCallbacks callbacks=new Application.ActivityLifecycleCallbacks(){public void onActivityCreated(Activity activity,Bundle state){owned.add(activity);assertTrue(activity instanceof HistoryActivity||activity instanceof RecordEditorActivity||activity instanceof RecordReportActivity);}public void onActivityStarted(Activity activity){}public void onActivityResumed(Activity activity){}public void onActivityPaused(Activity activity){}public void onActivityStopped(Activity activity){}public void onActivitySaveInstanceState(Activity activity,Bundle state){}public void onActivityDestroyed(Activity activity){}};application.registerActivityLifecycleCallbacks(callbacks);
        try{
            Records.factory=c->new Store(c,database);String raw;try(Store store=new Store(context,database)){seed(store);raw=original(store);assertEquals(7,Records.all(store).size());assertEquals(462*60000L,Records.get(store,2000).duration());}
            launch(HistoryActivity.class,0);title("Sleep history");assertTrue(device.wait(Until.hasObject(By.descStartsWith("Open record 2000,")),5000));capture("history");
            click("Trends");title("Trends");capture("week");click("Month");assertTrue(object("Month").isSelected());capture("month");click("Year");assertTrue(object("Year").isSelected());capture("year");click("Week");object("Read details").click();title("Sleep report");assertTrue(device.wait(Until.hasObject(By.desc("Edit sleep")),5000));capture("night");object("Edit sleep").click();title("Edit sleep");object("Notes or dream");capture("editor");
            click("Add tag");object("Tag name").setText("Late meal");click("Add");click("Add tag");object("Tag name").setText("Exercise");click("Add");object("Notes or dream").setText("Fixture note saved from the native editor");capture("tags-notes");
            object("Add awake period").click();title("Awake starts");click("Use time");title("Awake ends");click("Use time");click("Save changes");title("Sleep report");
            try(Store store=new Store(context,database)){RecordEntry entry=Records.get(store,2000);assertEquals(452*60000L,entry.duration());assertEquals(1,entry.edit.awake.size());assertEquals("Fixture note saved from the native editor",entry.edit.note);assertTrue(entry.edit.tags.containsAll(List.of("Late meal","Exercise")));assertEquals(raw,original(store));}
            object("Edit sleep").click();title("Edit sleep");assertEquals("Fixture note saved from the native editor",object("Notes or dream").getText());capture("reopened");object("Notes or dream").setText("This change must be discarded");device.pressBack();title("Sleep report");
            try(Store store=new Store(context,database)){assertEquals("Fixture note saved from the native editor",Records.get(store,2000).edit.note);assertEquals(raw,original(store));}
            assertEquals(preferences,Prefs.of(context).getAll());assertFalse(AlarmService.active);assertFalse(TrackingService.running);assertFalse(Prefs.flag(context,"ringing"));
        }catch(Exception|AssertionError error){try{capture("failure");device.dumpWindowHierarchy(new File(context.getExternalFilesDir(null),"verification/report-reference-failure.xml"));}catch(Exception diagnostic){error.addSuppressed(diagnostic);}throw error;}
        finally{boolean quiescent=false;try{instrumentation.runOnMainSync(()->{for(Activity activity:owned)if(!activity.isDestroyed())activity.finish();});instrumentation.waitForIdleSync();long until=SystemClock.elapsedRealtime()+6000;while(owned.stream().anyMatch(a->!a.isDestroyed())&&SystemClock.elapsedRealtime()<until)SystemClock.sleep(50);assertTrue("All isolated screens must be destroyed",owned.stream().allMatch(Activity::isDestroyed));for(Activity activity:owned){if(activity instanceof HistoryActivity)((HistoryActivity)activity).awaitReaders(5000);if(activity instanceof RecordReportActivity)((RecordReportActivity)activity).awaitReaders(5000);if(activity instanceof RecordEditorActivity)((RecordEditorActivity)activity).awaitReaders(5000);}quiescent=true;assertEquals(preferences,Prefs.of(context).getAll());}
            finally{application.unregisterActivityLifecycleCallbacks(callbacks);Records.factory=previous;if(quiescent)context.deleteDatabase(database);}}
    }
}
