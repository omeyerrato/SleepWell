package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.graphics.Rect;
import android.os.*;
import android.view.*;
import android.view.inspector.WindowInspector;
import android.widget.*;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.*;
import org.junit.Test;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;
import static org.junit.Assert.*;

/** One CI-only native navigation/persistence flow. No alarm runtime, recording or output. */
public final class SettingsOrganizationFlowTest {
    private final Instrumentation i=InstrumentationRegistry.getInstrumentation();
    private final Context c=i.getTargetContext();
    private final UiDevice device=UiDevice.getInstance(i);
    private final List<Activity> owned=new CopyOnWriteArrayList<>();
    private Activity activity;
    private String step="CI admission";

    private void await(BooleanSupplier condition)throws Exception {
        long end=SystemClock.elapsedRealtime()+8000;
        while(!condition.getAsBoolean()&&SystemClock.elapsedRealtime()<end)Thread.sleep(20);
        assertTrue(step,condition.getAsBoolean());
    }
    private View find(View root,String value,boolean description){
        if(description?value.contentEquals(root.getContentDescription()==null?"":root.getContentDescription()):root instanceof TextView&&value.contentEquals(((TextView)root).getText()))return root;
        if(root instanceof ViewGroup group)for(int n=0;n<group.getChildCount();n++){View found=find(group.getChildAt(n),value,description);if(found!=null)return found;}
        return null;
    }
    private View focused(){View found=null;for(View root:WindowInspector.getGlobalWindowViews())if(root.getContext().getPackageName().equals(c.getPackageName())&&root.hasWindowFocus()&&root.isAttachedToWindow()&&root.isLaidOut()){assertNull("One focused application window",found);found=root;}return found;}
    private void ready()throws Exception{await(()->{boolean[] yes={false};i.runOnMainSync(()->yes[0]=activity!=null&&!activity.isDestroyed()&&!activity.isFinishing()&&activity.hasWindowFocus()&&focused()==activity.getWindow().getDecorView());return yes[0];});}
    private void require(String value)throws Exception{step="require "+value;ready();await(()->{boolean[] yes={false};i.runOnMainSync(()->yes[0]=find(activity.getWindow().getDecorView(),value,false)!=null);return yes[0];});}
    private void requireDescription(String value)throws Exception{step="require action "+value;ready();i.runOnMainSync(()->assertNotNull(value,find(activity.getWindow().getDecorView(),value,true)));}
    private void wholeRowWithoutScrolling(String title)throws Exception{step="whole initial row "+title;ready();i.runOnMainSync(()->{View target=find(activity.getWindow().getDecorView(),title,false);assertNotNull(title,target);while(!target.isClickable()&&target.getParent() instanceof View)target=(View)target.getParent();Rect visible=new Rect();assertTrue(title+" visible",target.getGlobalVisibleRect(visible));assertEquals(title+" whole initial width",target.getWidth(),visible.width());assertEquals(title+" whole initial height",target.getHeight(),visible.height());assertTrue(title+"48dp target",target.getHeight()>=Design.dp(c,48));});}
    private void frame(View root)throws Exception{CountDownLatch committed=new CountDownLatch(1);i.runOnMainSync(()->{assertSame("Owned focused frame",root,focused());assertTrue(root.isHardwareAccelerated());root.getViewTreeObserver().registerFrameCommitCallback(committed::countDown);root.invalidate();});assertTrue(step+" committed frame",committed.await(5,TimeUnit.SECONDS));i.runOnMainSync(()->assertSame(root,focused()));}
    private void input(String value,boolean description)throws Exception{
        step="single native tap "+value;ready();View root=activity.getWindow().getDecorView();
        input(value,description,root);
    }
    private void input(String value,boolean description,View root)throws Exception{
        step="single native tap "+value;
        await(()->{boolean[] yes={false};i.runOnMainSync(()->{if(focused()!=root)return;View target=find(root,value,description);if(target==null)return;while(!target.isClickable()&&target.getParent() instanceof View)target=(View)target.getParent();if(!target.isClickable()||!target.isEnabled())return;target.requestRectangleOnScreen(new Rect(0,0,target.getWidth(),target.getHeight()),true);Rect shown=new Rect();yes[0]=target.getGlobalVisibleRect(shown)&&shown.width()==target.getWidth()&&shown.height()==target.getHeight();});return yes[0];});
        frame(root);Rect bounds=new Rect();i.runOnMainSync(()->{assertSame(root,focused());View target=find(root,value,description);assertNotNull(value,target);while(!target.isClickable()&&target.getParent() instanceof View)target=(View)target.getParent();assertTrue(target.isClickable()&&target.isEnabled());assertSame(root,target.getRootView());Rect shown=new Rect();assertTrue(target.getGlobalVisibleRect(shown));assertEquals("Whole action width",target.getWidth(),shown.width());assertEquals("Whole action height",target.getHeight(),shown.height());assertTrue(value+"48dp width",shown.width()>=Design.dp(c,48));assertTrue(value+"48dp height",shown.height()>=Design.dp(c,48));int[] location=new int[2];target.getLocationOnScreen(location);bounds.set(location[0],location[1],location[0]+target.getWidth(),location[1]+target.getHeight());});
        // No repeated click after delivery: absent postconditions are failures.
        assertTrue(value+" dispatched once",device.click(bounds.centerX(),bounds.centerY()));
    }
    private View dialog(String title)throws Exception{
        step="owned dialog "+title;assertTrue(activity instanceof AlarmEditorActivity);
        java.lang.reflect.Field field=AlarmEditorActivity.class.getDeclaredField("settingsDialog");field.setAccessible(true);View[] window={null};
        await(()->{boolean[] yes={false};i.runOnMainSync(()->{try{Object owned=field.get(activity);if(!(owned instanceof SleepPicker.StateDialog current)||!current.title.equals(title)||!current.isShowing()||current.getWindow()==null)return;View root=current.getWindow().getDecorView();if(focused()!=root||!root.isAttachedToWindow()||!root.isLaidOut())return;assertNotNull("Actual owned dialog title",find(root,title,false));window[0]=root;yes[0]=true;}catch(IllegalAccessException inaccessible){throw new AssertionError(inaccessible);}});return yes[0];});
        return window[0];
    }
    private void click(String value)throws Exception{input(value,false);}
    private void clickDescription(String value)throws Exception{input(value,true);}
    private <T extends Activity>T openByTap(Class<T> type,String label,boolean description)throws Exception{
        Instrumentation.ActivityMonitor monitor=i.addMonitor(type.getName(),null,false);
        try{input(label,description);Activity next=monitor.waitForActivityWithTimeout(5000);assertNotNull("Opened "+type.getSimpleName(),next);activity=next;ready();return type.cast(next);}finally{i.removeMonitor(monitor);}
    }
    private void launchSettings()throws Exception{Instrumentation.ActivityMonitor monitor=i.addMonitor(MainActivity.class.getName(),null,false);try{c.startActivity(new Intent(c,MainActivity.class).putExtra("destination",3).putExtra("visualReview",true).putExtra("verificationLabel","CI private configuration · no real alarm").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));activity=monitor.waitForActivityWithTimeout(5000);assertNotNull(activity);}finally{i.removeMonitor(monitor);}require("Settings");}
    private void recreate()throws Exception{Activity previous=activity;Instrumentation.ActivityMonitor monitor=i.addMonitor(previous.getClass().getName(),null,false);try{i.runOnMainSync(previous::recreate);activity=monitor.waitForActivityWithTimeout(5000);assertNotNull("Recreated owned Activity",activity);}finally{i.removeMonitor(monitor);}await(previous::isDestroyed);ready();}
    private void switchChecked(String name,boolean expected)throws Exception{ready();i.runOnMainSync(()->{View view=find(activity.getWindow().getDecorView(),name,true);assertTrue(name+" is a real Switch",view instanceof Switch);assertEquals(name,expected,((Switch)view).isChecked());});}
    private void query(String value)throws Exception{ready();
        String[] field={null};i.runOnMainSync(()->{View root=activity.getWindow().getDecorView();field[0]=find(root,"Search alarm query",true)!=null?"Search alarm query":find(root,"Search settings query",true)!=null?"Search settings query":"Search query";View edit=find(root,field[0],true);if(edit!=null)edit.requestRectangleOnScreen(new Rect(0,0,edit.getWidth(),edit.getHeight()),true);});i.waitForIdleSync();
        UiObject2 edit=device.findObject(By.desc(field[0]));assertNotNull("Real search input",edit);edit.setText(value);
        i.waitForIdleSync();boolean[] ime={false};i.runOnMainSync(()->{WindowInsets insets=activity.getWindow().getDecorView().getRootWindowInsets();ime[0]=insets!=null&&insets.isVisible(WindowInsets.Type.ime());});if(ime[0]){assertTrue(device.pressBack());ready();}
    }
    private File folder(){File dir=new File(c.getExternalFilesDir(null),"verification");assertTrue(dir.isDirectory()||dir.mkdirs());return dir;}
    private void capture(String name)throws Exception{step="capture organization-"+name;ready();frame(activity.getWindow().getDecorView());i.runOnMainSync(()->{int appearance=activity.getWindow().getInsetsController().getSystemBarsAppearance();assertTrue(name+" dark status glyphs on cream",(appearance&WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)!=0);assertTrue(name+" dark navigation glyphs on cream",(appearance&WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS)!=0);});assertTrue(device.takeScreenshot(new File(folder(),"organization-"+name+".png")));}
    private void diagnose(Throwable failure){try{try(PrintWriter out=new PrintWriter(new File(folder(),"organization-failure.txt"),"UTF-8")){out.println(step);failure.printStackTrace(out);}device.takeScreenshot(new File(folder(),"organization-failure.png"));device.dumpWindowHierarchy(new File(folder(),"organization-failure.xml"));}catch(Exception diagnostic){failure.addSuppressed(diagnostic);}}
    private void unchanged(AlarmSkipFixture fixture,String original){assertEquals("Only Save alarm may write the draft",original,fixture.load().json().toString());assertEquals("No scheduling from navigation/draft controls",0,fixture.schedules);}
    private void closeAll()throws Exception{i.runOnMainSync(()->{List<Activity> reverse=new ArrayList<>(owned);Collections.reverse(reverse);for(Activity a:reverse)if(!a.isDestroyed())a.finish();});await(()->owned.stream().allMatch(Activity::isDestroyed));for(Activity a:owned)if(a instanceof SearchActivity)assertTrue(((SearchActivity)a).awaitReaders(8000));HomeSurfaces.drain(8000);}

    @Test public void groupedSettingsSearchAndAlarmDraftPersistWithoutOutputs()throws Exception {
        assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));
        assertEquals("1",device.executeShellCommand("getprop ro.kernel.qemu").trim());
        assertFalse(AlarmService.active);assertFalse(TrackingService.running);assertFalse(Prefs.flag(c,"ringing"));assertFalse(Prefs.flag(c,"tracking"));assertEquals(0,Prefs.number(c,"checkPending",0));
        // As in SoundSettingsFlowTest, initialize only absent inactive CI defaults before
        // the exact preservation baseline; delayed BOOT_COMPLETED may write these same values.
        SharedPreferences idle=Prefs.of(c);assertFalse(idle.getBoolean("trackingPaused",false));assertEquals(0L,idle.getLong("session",0));
        SharedPreferences.Editor initialize=idle.edit();for(String key:new String[]{"ringing","tracking","trackingPaused"})if(!idle.contains(key))initialize.putBoolean(key,false);if(!idle.contains("session"))initialize.putLong("session",0L);assertTrue(initialize.commit());
        Map<String,?> before=new HashMap<>(Prefs.of(c).getAll());assertTrue(!before.containsKey(InteractionFeedback.ENABLED)||before.get(InteractionFeedback.ENABLED) instanceof Boolean);
        Application app=(Application)c.getApplicationContext();Application.ActivityLifecycleCallbacks observer=new Application.ActivityLifecycleCallbacks(){public void onActivityCreated(Activity a,Bundle state){owned.add(a);assertTrue("Only inert settings/search/alarm editors may open",a instanceof MainActivity||a instanceof SearchActivity||a instanceof FeedbackSettingsActivity||a instanceof AlarmEditorActivity);assertTrue("Every fixture window suppresses haptic output",a.getIntent().getBooleanExtra("visualReview",false));}public void onActivityStarted(Activity a){}public void onActivityResumed(Activity a){}public void onActivityPaused(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,Bundle state){}public void onActivityDestroyed(Activity a){}};
        app.registerActivityLifecycleCallbacks(observer);
        try(AlarmSkipFixture fixture=new AlarmSkipFixture()){
            try{
                Alarm initial=fixture.load();initial.enabled=false;initial.label="CI organization alarm";initial.volume=43;initial.tone="chime";initial.snoozeMinutes=9;initial.snoozeTotalMinutes=30;initial.backupMinutes=7;initial.awakeCheck=2;initial.inputs=new AlarmInputs(true,AlarmInputs.Keys.IGNORE,true,false);initial.setRoutine(new ChallengeRoutine(List.of(new ChallengeRoutine.Task("Math",2),new ChallengeRoutine.Task("Memory",1))));try(Store store=fixture.open()){store.save(initial);}String original=fixture.load().json().toString();assertTrue(InteractionFeedback.setEnabled(c,true));
                launchSettings();for(String group:List.of("Alarms","Sleep & bedtime","Sound & recordings","Appearance & feedback","Reliability","Connected apps","Data & privacy","Home screen"))require(group);wholeRowWithoutScrolling("Home screen");capture("settings-root");
                click("Sleep & bedtime");require("Automatic tracking");require("Sleep plan");capture("settings-sleep");clickDescription("Back");requireDescription("Search settings");
                click("Sound & recordings");require("Snoring cues");require("Recordings & retention");clickDescription("Back");requireDescription("Search settings");
                click("Connected apps");require("Health Connect");require("Automations");clickDescription("Back");requireDescription("Search settings");
                // UIAutomator may report a false injection acknowledgement after API36 already closes the window.
                // Deliver Back once and require the actual owned Activity to be destroyed; never retry it.
                MainActivity settings=(MainActivity)activity;click("Appearance & feedback");require("Theme & text");openByTap(FeedbackSettingsActivity.class,"Touch feedback",false);switchChecked("Touch feedback",true);clickDescription("Touch feedback");require("Saved");switchChecked("Touch feedback",false);assertFalse(InteractionFeedback.enabled(c));recreate();switchChecked("Touch feedback",false);capture("feedback-off");Activity feedback=activity;device.pressBack();await(feedback::isDestroyed);activity=settings;require("Appearance & feedback");openByTap(FeedbackSettingsActivity.class,"Touch feedback",false);switchChecked("Touch feedback",false);assertFalse(InteractionFeedback.enabled(c));feedback=activity;device.pressBack();Activity closedFeedback=feedback;await(closedFeedback::isDestroyed);activity=settings;ready();clickDescription("Back");requireDescription("Search settings");
                openByTap(SearchActivity.class,"Search settings",true);require("Search settings");query("flashlight");require("Sunrise & flashlight wake-up");require("Settings › Alarms › Choose an alarm › Wake-up\nOpens Alarms; choose an alarm to set wake-up outputs");capture("search-flashlight");
                click("Search everything");require("Search SleepWell");query("CI organization alarm");require("1 result");require("CI organization alarm");click("Settings only");require("Search settings");query("flashlight");require("Sunrise & flashlight wake-up");clickDescription("Filter settings: Alarms");recreate();require("Sunrise & flashlight wake-up");i.runOnMainSync(()->{View category=find(activity.getWindow().getDecorView(),"Filter settings: Alarms",true);assertNotNull(category);assertTrue("Settings filter restored",category.isSelected());EditText field=(EditText)find(activity.getWindow().getDecorView(),"Search settings query",true);assertEquals("flashlight",field.getText().toString());});
                MainActivity alarms=openByTap(MainActivity.class,"Sunrise & flashlight wake-up",false);assertEquals("alarms",alarms.getIntent().getStringExtra("settingsPage"));require("Alarms");require("CI organization alarm");unchanged(fixture,original);
                openByTap(AlarmEditorActivity.class,"Alarm settings for CI organization alarm",true);require("Edit alarm");wholeRowWithoutScrolling("Schedule & dates");capture("alarm-editor");
                clickDescription("Search this alarm");require("Search alarm settings");query("snooze");require("Volume keys");
                int[] searchPosition={0};i.runOnMainSync(()->{View last=find(activity.getWindow().getDecorView(),"Open alarm setting Volume keys",true);assertNotNull(last);last.requestRectangleOnScreen(new Rect(0,0,last.getWidth(),last.getHeight()),true);});
                step="nonzero actual alarm search scroll";
                await(()->{boolean[] yes={false};i.runOnMainSync(()->{View field=find(activity.getWindow().getDecorView(),"Search alarm query",true);ScrollView pane=(ScrollView)((View)field.getParent()).getParent();searchPosition[0]=pane.getScrollY();yes[0]=searchPosition[0]>0;});return yes[0];});
                recreate();require("Search alarm settings");i.runOnMainSync(()->assertEquals("snooze",((EditText)find(activity.getWindow().getDecorView(),"Search alarm query",true)).getText().toString()));
                step="restored alarm search scroll and query";
                await(()->{boolean[] yes={false};i.runOnMainSync(()->{View field=find(activity.getWindow().getDecorView(),"Search alarm query",true);ScrollView pane=(ScrollView)((View)field.getParent()).getParent();yes[0]=pane.getScrollY()==searchPosition[0];});return yes[0];});unchanged(fixture,original);
                query("flashlight");require("Flashlight wake-up");require("Edit alarm › Sound › Gentle wake-up");capture("alarm-search");clickDescription("Open alarm setting Flashlight wake-up");require("Gentle wake-up");switchChecked("Flashlight wake-up",false);recreate();require("Gentle wake-up");switchChecked("Flashlight wake-up",false);clickDescription("Back");require("Search alarm settings");i.runOnMainSync(()->assertEquals("flashlight",((EditText)find(activity.getWindow().getDecorView(),"Search alarm query",true)).getText().toString()));unchanged(fixture,original);
                query("no matching alarm option");require("No matching alarm settings. Try another word.");click("Clear search");require("Choose a setting or search by name.");clickDescription("Back");require("Edit alarm");unchanged(fixture,original);
                boolean inheritedGoal=Prefs.flag(c,"smartNeedsGoal");click("Smart wake-up");require("Movement sensitivity");switchChecked("Respect sleep goal",inheritedGoal);
                clickDescription("Smart wake-up options");input("Cancel",false,dialog("Smart wake-up options"));require("Movement sensitivity");unchanged(fixture,original);
                clickDescription("Smart wake-up options");input("About smart wake-up",false,dialog("Smart wake-up options"));dialog("About smart wake-up");unchanged(fixture,original);
                Activity beforeAboutRotation=activity;Instrumentation.ActivityMonitor aboutMonitor=i.addMonitor(AlarmEditorActivity.class.getName(),null,false);
                try{i.runOnMainSync(beforeAboutRotation::recreate);activity=aboutMonitor.waitForActivityWithTimeout(5000);assertNotNull("Recreated editor with owned About dialog",activity);}finally{i.removeMonitor(aboutMonitor);}
                await(beforeAboutRotation::isDestroyed);View restoredAbout=dialog("About smart wake-up");i.runOnMainSync(()->assertNotNull("Restored About close action",find(restoredAbout,"Got it",false)));unchanged(fixture,original);
                input("Got it",false,restoredAbout);require("Movement sensitivity");switchChecked("Respect sleep goal",inheritedGoal);unchanged(fixture,original);
                i.runOnMainSync(()->{for(String level:List.of("Low","Medium","High")){View choice=find(activity.getWindow().getDecorView(),"Movement sensitivity "+level,true);assertNotNull(choice);assertFalse("Inherited sensitivity has no manual visual selection",choice.isSelected());android.view.accessibility.AccessibilityNodeInfo node=choice.createAccessibilityNodeInfo();try{assertTrue(node.isCheckable());assertFalse("Inherited sensitivity has no manual accessibility selection",node.isChecked());assertEquals(RadioButton.class.getName(),node.getClassName().toString());}finally{node.recycle();}}View goal=find(activity.getWindow().getDecorView(),"Respect sleep goal",true);goal.requestRectangleOnScreen(new Rect(0,0,goal.getWidth(),goal.getHeight()),true);});
                unchanged(fixture,original);capture("smart-following");clickDescription("Movement sensitivity High");
                // If inherited On, deliberately choose Off then On so the saved goal is an explicit override.
                if(inheritedGoal){clickDescription("Respect sleep goal");switchChecked("Respect sleep goal",false);}
                clickDescription("Respect sleep goal");switchChecked("Respect sleep goal",true);recreate();require("Movement sensitivity");switchChecked("Respect sleep goal",true);
                i.runOnMainSync(()->{for(String level:List.of("Low","Medium","High")){View choice=find(activity.getWindow().getDecorView(),"Movement sensitivity "+level,true);assertNotNull(choice);boolean selected=level.equals("High");assertEquals("Restored sensitivity visual selection",selected,choice.isSelected());android.view.accessibility.AccessibilityNodeInfo node=choice.createAccessibilityNodeInfo();try{assertTrue(node.isCheckable());assertEquals("Restored sensitivity accessibility selection",selected,node.isChecked());assertEquals(RadioButton.class.getName(),node.getClassName().toString());}finally{node.recycle();}}View goal=find(activity.getWindow().getDecorView(),"Respect sleep goal",true);goal.requestRectangleOnScreen(new Rect(0,0,goal.getWidth(),goal.getHeight()),true);});
                unchanged(fixture,original);capture("smart-configured");click("Save settings");require("Edit alarm");unchanged(fixture,original);
                click("Schedule & dates");require("Time basis");require("Bedtime reminder");capture("alarm-schedule");click("Done");require("Edit alarm");unchanged(fixture,original);
                click("More alarm settings");require("Buttons & orientation");require("New alarm defaults");capture("alarm-options");click("Buttons & orientation");require("Edit alarm · More settings");switchChecked("Lock alarm orientation",false);clickDescription("Lock alarm orientation");switchChecked("Lock alarm orientation",true);unchanged(fixture,original);click("Done");require("More alarm settings");click("Done");require("Edit alarm");
                click("Snooze");require("Snooze settings");click("Buttons & orientation");require("Edit alarm · Snooze");switchChecked("Lock alarm orientation",true);recreate();require("Edit alarm · Snooze");switchChecked("Lock alarm orientation",true);capture("controls-recreated");unchanged(fixture,original);click("Done");require("Snooze settings");click("Backup & wake-up check");require("Wake-up check");require("Stop ringing after");capture("alarm-backup");click("Done");require("Snooze settings");click("Done");require("Edit alarm");unchanged(fixture,original);
                AlarmEditorActivity editor=(AlarmEditorActivity)activity;click("Save alarm");await(editor::isDestroyed);Alarm saved=fixture.load();Alarm expected=Alarm.read(initial.json());expected.inputs=expected.inputs.lockOrientation(true);expected.setSmartWake(new AlarmSmartWake(2,1));assertEquals("Only requested orientation and smart wake-up fields changed; routine and other fields preserved",expected.json().toString(),saved.json().toString());assertEquals(1,fixture.schedules);assertFalse(saved.enabled);activity=alarms;require("Alarms");openByTap(AlarmEditorActivity.class,"Alarm settings for CI organization alarm",true);
                click("Smart wake-up");require("Movement sensitivity");switchChecked("Respect sleep goal",true);i.runOnMainSync(()->{for(String level:List.of("Low","Medium","High")){View choice=find(activity.getWindow().getDecorView(),"Movement sensitivity "+level,true);assertNotNull(choice);boolean selected=level.equals("High");assertEquals("Saved sensitivity visual selection",selected,choice.isSelected());android.view.accessibility.AccessibilityNodeInfo node=choice.createAccessibilityNodeInfo();try{assertTrue(node.isCheckable());assertEquals("Saved sensitivity accessibility selection",selected,node.isChecked());}finally{node.recycle();}}View goal=find(activity.getWindow().getDecorView(),"Respect sleep goal",true);goal.requestRectangleOnScreen(new Rect(0,0,goal.getWidth(),goal.getHeight()),true);});capture("smart-reopened");assertEquals(expected.json().toString(),fixture.load().json().toString());assertEquals(1,fixture.schedules);click("Save settings");require("Edit alarm");
                click("More alarm settings");click("Buttons & orientation");switchChecked("Lock alarm orientation",true);require("Edit alarm · More settings");capture("alarm-reopened");assertEquals(expected.json().toString(),fixture.load().json().toString());assertEquals(1,fixture.schedules);assertFalse(AlarmService.active);assertFalse(TrackingService.running);assertFalse(Prefs.flag(c,"ringing"));assertFalse(Prefs.flag(c,"tracking"));
            }catch(Exception|AssertionError failure){diagnose(failure);throw failure;}
            finally{try{closeAll();}finally{SharedPreferences.Editor restore=Prefs.of(c).edit();if(before.containsKey(InteractionFeedback.ENABLED))restore.putBoolean(InteractionFeedback.ENABLED,(Boolean)before.get(InteractionFeedback.ENABLED));else restore.remove(InteractionFeedback.ENABLED);assertTrue(restore.commit());assertEquals("All shared preferences restored",before,Prefs.of(c).getAll());}}
        }finally{app.unregisterActivityLifecycleCallbacks(observer);}
    }
}
