package org.sleepwell.app;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.graphics.Rect;
import android.view.inspector.WindowInspector;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.*;
import androidx.test.uiautomator.*;
import org.junit.Test;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.Assert.*;
/** Owned native settings on an explicit disposable emulator; scheduler and launcher are inert. */
public class RecurringTrackingFlowTest {
    final Instrumentation i=InstrumentationRegistry.getInstrumentation();final Context c=i.getTargetContext();final UiDevice device=UiDevice.getInstance(i);
    private String step="starting harness";
    private void require(BySelector selector){assertTrue(step+": "+selector,device.wait(Until.hasObject(selector),5000));}
    private View match(View root,String value,boolean description){if(description?value.contentEquals(root.getContentDescription()==null?"":root.getContentDescription()):root instanceof android.widget.TextView&&value.contentEquals(((android.widget.TextView)root).getText()))return root;if(root instanceof ViewGroup group)for(int n=0;n<group.getChildCount();n++){View found=match(group.getChildAt(n),value,description);if(found!=null)return found;}return null;}
    private View findFocusedRoot(){View found=null;for(View root:WindowInspector.getGlobalWindowViews())if(root.getContext().getPackageName().equals(c.getPackageName())&&root.hasWindowFocus()&&root.isAttachedToWindow()&&root.isLaidOut()&&root.getWidth()>0&&root.getHeight()>0){assertNull("Exactly one focused application window",found);found=root;}return found;}
    private View focusedRoot(){View root=findFocusedRoot();assertNotNull(step+": focused, attached and laid-out application window",root);return root;}
    private void frame()throws Exception{device.waitForIdle();CountDownLatch committed=new CountDownLatch(1);View[] owner={null};assertTrue(step+": focused laid-out window",device.wait(new Condition<UiDevice,Boolean>(){public Boolean apply(UiDevice ignored){i.runOnMainSync(()->{View root=findFocusedRoot();if(root==null)return;assertTrue("Native hardware frame",root.isHardwareAccelerated());owner[0]=root;root.getViewTreeObserver().registerFrameCommitCallback(committed::countDown);root.invalidate();});return owner[0]!=null;}},5000));assertTrue(step+": focused window frame",committed.await(5,TimeUnit.SECONDS));i.runOnMainSync(()->assertSame("Frame still belongs to the focused window",owner[0],focusedRoot()));}
    private void tap(String value,boolean description)throws Exception{
        step="tap "+value;frame();BySelector by=description?By.desc(value):By.text(value);UiSelector lazy=description?new UiSelector().description(value):new UiSelector().text(value);
        boolean[] complete={false};i.runOnMainSync(()->{View target=match(focusedRoot(),value,description);if(target==null)return;while(!target.isClickable()&&target.getParent() instanceof View)target=(View)target.getParent();Rect visible=new Rect();complete[0]=target.getGlobalVisibleRect(visible)&&visible.height()==target.getHeight();if(!complete[0])target.requestRectangleOnScreen(new Rect(0,0,target.getWidth(),target.getHeight()),true);});
        if(!complete[0])frame();
        if(!device.hasObject(by)){UiScrollable scroll=new UiScrollable(new UiSelector().scrollable(true));assertTrue(step+": scroll area",scroll.exists());assertTrue(step+": exposed control",scroll.scrollIntoView(lazy));}
        require(by);frame();
        i.runOnMainSync(()->{View target=match(focusedRoot(),value,description);assertNotNull(step,target);while(!target.isClickable()&&target.getParent() instanceof View)target=(View)target.getParent();assertTrue(step+": actual actionable control",target.isClickable()&&target.isEnabled());Rect bounds=new Rect();assertTrue(step,target.getGlobalVisibleRect(bounds));assertEquals(step+": complete touch target",target.getHeight(),bounds.height());assertTrue(step+": 48dp touch target",bounds.height()>=Design.dp(c,48));});
        // UiObject resolves its selector at dispatch; no retained accessibility node and no action retry.
        assertTrue(step+": dispatched once",device.findObject(lazy).click());device.waitForIdle();
    }
    private void click(String text)throws Exception{tap(text,false);}
    private void toggle(String description,boolean before,boolean after)throws Exception{require(By.desc(description).checked(before));tap(description,true);require(By.desc(description).checked(after));frame();}
    private void errorVisible(String message)throws Exception{require(By.text(message));frame();i.runOnMainSync(()->{View root=focusedRoot(),error=match(root,message,false),save=match(root,"Save nightly settings",false);assertNotNull(error);assertNotNull(save);Rect text=new Rect(),footer=new Rect();assertTrue(error.getGlobalVisibleRect(text));assertTrue(save.getGlobalVisibleRect(footer));assertEquals("Complete refreshed inline error",error.getHeight(),text.height());assertTrue("Error above Save footer",text.bottom<=footer.top);});}
    private void permissionRows(boolean notifications,boolean exact)throws Exception{frame();i.runOnMainSync(()->{View root=focusedRoot();View notification=match(root,"Notifications",false),timing=match(root,"Exact timing",false);assertNotNull(notification);assertNotNull(timing);ViewGroup n=(ViewGroup)notification.getParent(),t=(ViewGroup)timing.getParent();assertNotNull("Current notification availability",match(n,notifications?"Allowed":"Allow to keep Cancel visible",false));assertNotNull("Current exact-alarm availability",match(t,exact?"Allowed":"Allow exact alarms",false));assertNull("No stale notification summary",match(n,notifications?"Allow to keep Cancel visible":"Allowed",false));assertNull("No stale exact-alarm summary",match(t,exact?"Allow exact alarms":"Allowed",false));});}
    private void settling()throws Exception{require(By.desc("At this time").selected(true));tap("After settling",true);require(By.desc("After settling").selected(true));frame();}
    private Activity focused(Activity previous){Activity[] found={null};long until=SystemClock.elapsedRealtime()+5000;while(found[0]==null){i.runOnMainSync(()->{for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)){View root=a.getWindow().getDecorView();if(a instanceof RecurringTrackingActivity&&a!=previous&&!a.isFinishing()&&!a.isDestroyed()&&root.isAttachedToWindow()&&root.isLaidOut()&&root.getWidth()>0&&root.getHeight()>0)found[0]=a;}});assertTrue("Resumed, attached and laid-out recurring settings",found[0]!=null||SystemClock.elapsedRealtime()<until);if(found[0]==null)SystemClock.sleep(25);}return found[0];}
    final class Harness implements AutoCloseable {
        final RecurringTracking.Port old=RecurringTracking.port;final RecurringTrackingTest.Fake fake=new RecurringTrackingTest.Fake(c);final Map<String,?> prefs=Prefs.of(c).getAll();final List<Activity> owned=new CopyOnWriteArrayList<>();final Map<Activity,CountDownLatch> destroyed=new ConcurrentHashMap<>();final Application app=(Application)c.getApplicationContext();final File folder=new File(c.getExternalFilesDir(null),"verification");
        final Application.ActivityLifecycleCallbacks lifecycle=new Application.ActivityLifecycleCallbacks(){public void onActivityCreated(Activity a,Bundle b){owned.add(a);destroyed.put(a,new CountDownLatch(1));assertTrue("No external permissions, tracking or alarm screen allowed",a instanceof RecurringTrackingActivity);}public void onActivityStarted(Activity a){}public void onActivityResumed(Activity a){}public void onActivityPaused(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,Bundle b){}public void onActivityDestroyed(Activity a){destroyed.get(a).countDown();}};
        Harness()throws Exception{assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertTrue(Build.HARDWARE.contains("ranchu")||Build.HARDWARE.contains("goldfish"));assertEquals("1",device.executeShellCommand("getprop ro.kernel.qemu").trim());assertFalse(TrackingService.running);assertFalse(AlarmService.active);assertFalse(Prefs.flag(c,"ringing"));assertFalse(Prefs.flag(c,"tracking"));assertTrue(folder.isDirectory()||folder.mkdirs());app.registerActivityLifecycleCallbacks(lifecycle);i.runOnMainSync(()->RecurringTracking.port=fake);}
        void start()throws Exception{step="open recurring settings";Instrumentation.ActivityMonitor monitor=i.addMonitor(RecurringTrackingActivity.class.getName(),null,false);try{c.startActivity(new Intent(c,RecurringTrackingActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));Activity created=monitor.waitForActivityWithTimeout(5000);assertNotNull(created);assertSame(created,focused(null));require(By.text("Repeat nightly"));frame();}finally{i.removeMonitor(monitor);}}
        void finished(Activity activity)throws Exception{assertNotNull(destroyed.get(activity));assertTrue(step+": activity finished before replacement",destroyed.get(activity).await(5,TimeUnit.SECONDS));}
        void recreate()throws Exception{step="recreate recurring settings";Activity a=focused(null);i.runOnMainSync(a::recreate);focused(a);finished(a);frame();}
        void capture(String suffix)throws Exception{step="capture "+suffix;frame();assertTrue(device.takeScreenshot(new File(folder,"recurring-"+suffix+".png")));}
        void diagnostic(Throwable failure){try(java.io.PrintWriter writer=new java.io.PrintWriter(new File(folder,"recurring-original-error.txt"))){writer.println("Last action/await: "+step);failure.printStackTrace(writer);}catch(Exception error){failure.addSuppressed(error);}try{device.takeScreenshot(new File(folder,"recurring-failure.png"));device.dumpWindowHierarchy(new File(folder,"recurring-failure.xml"));}catch(Exception error){failure.addSuppressed(error);}}
        public void close()throws Exception{try{i.runOnMainSync(()->{for(Activity a:owned)if(!a.isDestroyed())a.finish();});long until=SystemClock.elapsedRealtime()+5000;while(owned.stream().anyMatch(a->!a.isDestroyed())&&SystemClock.elapsedRealtime()<until)SystemClock.sleep(25);assertTrue(owned.stream().allMatch(Activity::isDestroyed));assertEquals("No hardware launcher ran",0,fake.launches);assertEquals(prefs,Prefs.of(c).getAll());assertFalse(TrackingService.running);assertFalse(AlarmService.active);}finally{app.unregisterActivityLifecycleCallbacks(lifecycle);i.runOnMainSync(()->RecurringTracking.port=old);assertTrue(fake.p.edit().clear().commit());assertTrue(c.deleteSharedPreferences(fake.name));}}
    }
    @Test public void saveCancelRecreateReopenAndDisablePersistWithoutArmingTonight()throws Exception{
        try(Harness h=new Harness()){
            try{
                h.start();
                h.capture("off");
                toggle("Nightly tracking",false,true);
                settling();
                toggle("Only when charging",false,true);
                h.capture("draft");
                h.recreate();
                require(By.desc("After settling").selected(true));
                require(By.desc("Nightly tracking").checked(true));
                require(By.desc("Only when charging").checked(true));
                h.capture("draft-recreated");
                assertFalse(h.fake.p.getBoolean("enabled",false));
                Activity cancelled=focused(null);
                click("Cancel");
                h.finished(cancelled);
                h.start();
                assertFalse(h.fake.p.getBoolean("enabled",false));
                require(By.desc("At this time").selected(true));
                toggle("Nightly tracking",false,true);
                settling();
                toggle("Only when charging",false,true);
                click("Start time");
                require(By.text("Nightly start time"));
                require(By.desc("Minute"));
                frame();
                step="set minute to 15";
                assertTrue(device.findObject(new UiSelector().description("Minute")).setText("15"));
                require(By.desc("Minute").text("15"));
                h.recreate();
                require(By.text("Nightly start time"));
                require(By.desc("Minute").text("15"));
                h.capture("time-recreated");
                click("Use time");
                assertTrue("Time picker dismissed before Save",device.wait(Until.gone(By.text("Nightly start time")),5000));
                require(By.text("Repeat nightly"));
                require(By.desc("After settling").selected(true));
                Activity saved=focused(null);
                click("Save nightly settings");
                h.finished(saved);
                assertTrue(h.fake.p.getBoolean("enabled",false));
                assertTrue(h.fake.p.getBoolean("stillness",false));
                assertTrue(h.fake.p.getBoolean("charging",false));
                assertEquals(15,h.fake.p.getInt("minute",0)%60);
                assertTrue(h.fake.schedules>0);
                h.start();
                require(By.desc("After settling").selected(true));
                require(By.desc("Nightly tracking").checked(true));
                require(By.desc("Only when charging").checked(true));
                h.capture("saved");
                h.recreate();
                require(By.desc("After settling").selected(true));
                require(By.desc("Nightly tracking").checked(true));
                require(By.desc("Only when charging").checked(true));
                h.capture("saved-recreated");
                toggle("Nightly tracking",true,false);
                Activity disabled=focused(null);
                click("Save nightly settings");
                h.finished(disabled);
                assertFalse(h.fake.p.getBoolean("enabled",true));
                h.start();
                require(By.desc("Nightly tracking").checked(false));
                h.capture("disabled");
                assertEquals(0,h.fake.stops);
            }catch(Throwable failure){h.diagnostic(failure);throw failure;}
        }
    }
    @Test public void unavailablePermissionsAndSchedulingFailureRemainVisibleAfterRecreation()throws Exception{
        try(Harness h=new Harness()){
            try{
                h.fake.exact=false;
                h.start();
                permissionRows(true,false);
                h.capture("permission");
                toggle("Nightly tracking",false,true);
                click("Save nightly settings");
                errorVisible("Allow exact alarms to schedule nightly tracking.");
                permissionRows(true,false);
                h.capture("permission-error");
                h.recreate();
                errorVisible("Allow exact alarms to schedule nightly tracking.");
                permissionRows(true,false);
                h.capture("permission-recreated");
                h.fake.exact=true;
                h.fake.notifications=false;
                click("Save nightly settings");
                errorVisible("Allow notifications so the tracking cancel control stays visible.");
                permissionRows(false,true);
                h.capture("notifications-error");
                h.recreate();
                errorVisible("Allow notifications so the tracking cancel control stays visible.");
                permissionRows(false,true);
                h.fake.notifications=true;
                h.fake.failSchedule=true;
                click("Save nightly settings");
                assertTrue(h.fake.p.getBoolean("enabled",false));
                assertEquals("",h.fake.p.getString("pending",""));
                require(By.textStartsWith("Settings were saved, but the next start could not be scheduled."));
                permissionRows(true,true);
                errorVisible("Settings were saved, but the next start could not be scheduled. "+h.fake.p.getString("scheduleStatus",""));
                click("How this works");
                require(By.text("Nightly tracking"));
                require(By.text("Got it"));
                h.capture("limits");
                click("Got it");
                assertTrue("Explanation dismissed",device.wait(Until.gone(By.text("Got it")),5000));
                require(By.textStartsWith("Settings were saved, but the next start could not be scheduled."));
                h.capture("schedule-error");
                assertEquals(0,h.fake.launches);
            }catch(Throwable failure){h.diagnostic(failure);throw failure;}
        }
    }
}
