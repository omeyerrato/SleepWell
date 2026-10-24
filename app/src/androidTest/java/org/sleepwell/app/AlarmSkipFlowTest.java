package org.sleepwell.app;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.inspector.WindowInspector;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.Test;
import static org.junit.Assert.*;

/** Actual Alarms controls with private SQLite and a silent calendar-scheduling port. */
public final class AlarmSkipFlowTest {
 final Instrumentation i=InstrumentationRegistry.getInstrumentation();final Context c=i.getTargetContext();final UiDevice ui=UiDevice.getInstance(i);MainActivity activity;
 final List<Activity> owned=new CopyOnWriteArrayList<>();
 final Application.ActivityLifecycleCallbacks callbacks=new Application.ActivityLifecycleCallbacks(){public void onActivityCreated(Activity a,Bundle state){assertTrue("Only the private Main configuration screen may launch",a instanceof MainActivity);owned.add(a);}public void onActivityStarted(Activity a){}public void onActivityResumed(Activity a){activity=(MainActivity)a;}public void onActivityPaused(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,Bundle state){}public void onActivityDestroyed(Activity a){}};
 void launch()throws Exception{Instrumentation.ActivityMonitor monitor=i.addMonitor(MainActivity.class.getName(),null,false);try{c.startActivity(new Intent(c,MainActivity.class).putExtra("destination",1).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));activity=(MainActivity)monitor.waitForActivityWithTimeout(5000);assertNotNull(activity);}finally{i.removeMonitor(monitor);}text("Alarms");i.waitForIdleSync();}
 void recreate()throws Exception{Instrumentation.ActivityMonitor monitor=i.addMonitor(MainActivity.class.getName(),null,false);try{i.runOnMainSync(activity::recreate);activity=(MainActivity)monitor.waitForActivityWithTimeout(5000);assertNotNull(activity);}finally{i.removeMonitor(monitor);}text("Alarms");}
 void reopen()throws Exception{MainActivity old=activity;i.runOnMainSync(old::finish);long until=SystemClock.elapsedRealtime()+5000;while(!old.isDestroyed()&&SystemClock.elapsedRealtime()<until)SystemClock.sleep(40);assertTrue(old.isDestroyed());launch();}
 void text(String value){assertTrue(value,ui.wait(Until.hasObject(By.text(value)),5000));}
 void tap(String description)throws Exception{UiObject2 node=ui.wait(Until.findObject(By.desc(description)),1500);if(node==null){UiScrollable scroll=new UiScrollable(new UiSelector().scrollable(true));assertTrue(scroll.exists());scroll.scrollIntoView(new UiSelector().description(description));node=ui.wait(Until.findObject(By.desc(description)),3000);}assertNotNull(description,node);long until=SystemClock.elapsedRealtime()+5000;while(SystemClock.elapsedRealtime()<until){try{node=ui.findObject(By.desc(description));if(node!=null&&node.isEnabled()&&node.isClickable()&&node.getVisibleBounds().height()>=Design.dp(c,48)){node.click();i.waitForIdleSync();return;}}catch(StaleObjectException rebound){}SystemClock.sleep(40);}fail("Complete enabled actual hit target: "+description);}
 void keep(){UiObject2 node=ui.wait(Until.findObject(By.text("Keep alarm")),5000);assertNotNull(node);node.click();assertTrue(ui.wait(Until.gone(By.text("Skip this occurrence?")),5000));}
 void capture(String name)throws Exception{CountDownLatch committed=new CountDownLatch(1);boolean[] armed={false};long until=SystemClock.elapsedRealtime()+5000;while(!armed[0]&&SystemClock.elapsedRealtime()<until){i.runOnMainSync(()->{for(View decor:WindowInspector.getGlobalWindowViews())if(decor.hasWindowFocus()&&decor.isAttachedToWindow()&&decor.isHardwareAccelerated()){decor.getViewTreeObserver().registerFrameCommitCallback(committed::countDown);decor.invalidate();armed[0]=true;break;}});if(!armed[0])SystemClock.sleep(40);}assertTrue("A focused actual Main/dialog frame",armed[0]);assertTrue(committed.await(5,TimeUnit.SECONDS));if(name.equals("expired"))i.runOnMainSync(()->{ArrayList<View> matches=new ArrayList<>();activity.getWindow().getDecorView().findViewsWithText(matches,"Skipped occurrence for One time",View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);assertEquals("One expired status row",1,matches.size());View row=matches.get(0);android.graphics.Rect bounds=new android.graphics.Rect();assertTrue("Expired status is visible",row.getGlobalVisibleRect(bounds));assertEquals("Whole expired status fits",row.getHeight(),bounds.height());assertTrue("Expired status stays compact rather than consuming the list",row.getHeight()<=Design.dp(c,100));ArrayList<View> actions=new ArrayList<>();activity.getWindow().getDecorView().findViewsWithText(actions,"Take a nap",View.FIND_VIEWS_WITH_TEXT);assertEquals(1,actions.size());View action=actions.get(0);assertTrue("Remaining bottom action stays visible",action.getGlobalVisibleRect(bounds));assertEquals("Complete remaining action",action.getHeight(),bounds.height());});File dir=new File(c.getExternalFilesDir(null),"verification");assertTrue(dir.isDirectory()||dir.mkdirs());assertTrue(ui.takeScreenshot(new File(dir,"alarm-skip-"+name+".png")));}
 void saved(AlarmSkipFixture f,boolean hasSkip,boolean enabled){long until=SystemClock.elapsedRealtime()+5000;Alarm a;do{a=f.load();if((a.skip!=null)==hasSkip&&a.enabled==enabled)return;SystemClock.sleep(40);}while(SystemClock.elapsedRealtime()<until);fail("Saved skip/enable state did not settle: "+a.json());}
 void error(AlarmSkipFixture f,boolean present){long until=SystemClock.elapsedRealtime()+5000;do{if(!f.load().scheduleError.isEmpty()==present)return;SystemClock.sleep(40);}while(SystemClock.elapsedRealtime()<until);fail("Saved scheduling status did not settle");}
 void finishOwned(){i.runOnMainSync(()->owned.forEach(a->{if(!a.isDestroyed())a.finish();}));i.waitForIdleSync();long until=SystemClock.elapsedRealtime()+5000;while(owned.stream().anyMatch(a->!a.isDestroyed())&&SystemClock.elapsedRealtime()<until)SystemClock.sleep(40);assertTrue(owned.stream().allMatch(Activity::isDestroyed));}
 @Test public void recurringSkipUndoAndFailedSchedulingPersistAcrossRealMainRecreation()throws Exception{
  assertEquals("1",ui.executeShellCommand("getprop ro.kernel.qemu").trim());Application app=(Application)c.getApplicationContext();app.registerActivityLifecycleCallbacks(callbacks);
  try(AlarmSkipFixture f=new AlarmSkipFixture()) {try{
   launch();capture("default");tap("Skip next alarm for Workdays");text("Skip this occurrence?");capture("confirmation");i.runOnMainSync(activity::refreshAlarmListClock);text("Skip this occurrence?");keep();assertNull(f.load().skip);assertEquals(0,f.schedules);
   tap("Skip next alarm for Workdays");tap("Confirm skip for Workdays");saved(f,true,true);String nonce=f.load().skip.nonce;capture("skipped");
   recreate();assertEquals(nonce,f.load().skip.nonce);tap("Undo skip for Workdays");saved(f,false,true);capture("undo");
   tap("Skip next alarm for Workdays");tap("Confirm skip for Workdays");recreate();saved(f,true,true);capture("recreated");reopen();saved(f,true,true);capture("reopened");tap("Undo skip for Workdays");
   f.fail=true;tap("Skip next alarm for Workdays");tap("Confirm skip for Workdays");error(f,true);tap("Retry scheduling for Workdays");error(f,true);capture("schedule-error");reopen();error(f,true);capture("error-reopened");
   f.fail=false;tap("Retry scheduling for Workdays");error(f,false);saved(f,true,true);capture("retry");
   f.fail=true;tap("Undo skip for Workdays");saved(f,false,true);error(f,true);capture("undo-error");reopen();error(f,true);capture("undo-error-reopened");f.fail=false;tap("Retry scheduling for Workdays");saved(f,false,true);error(f,false);capture("undo-retry");
  }finally{finishOwned();}}finally{app.unregisterActivityLifecycleCallbacks(callbacks);}
 }
 @Test public void oneTimeSkipStaysDisabledAfterItsSelectedTimePasses()throws Exception{
  assertEquals("1",ui.executeShellCommand("getprop ro.kernel.qemu").trim());Application app=(Application)c.getApplicationContext();app.registerActivityLifecycleCallbacks(callbacks);
  try(AlarmSkipFixture f=new AlarmSkipFixture()){try{try(Store store=f.open()){Alarm a=f.load();a.days.clear();a.label="One time";store.save(a);}launch();tap("Skip next alarm for One time");text("This one-time alarm will be turned off. Undo is available before this time.");capture("one-time-confirmation");tap("Confirm skip for One time");saved(f,true,false);long selected=f.load().skip.at(f.zone);recreate();saved(f,true,false);capture("one-time-skipped");f.now=selected+1;recreate();saved(f,true,false);assertFalse(ui.hasObject(By.desc("Undo skip for One time")));text("Time passed");assertEquals(1,f.schedules);capture("expired");}finally{finishOwned();}}finally{app.unregisterActivityLifecycleCallbacks(callbacks);}
 }
}
