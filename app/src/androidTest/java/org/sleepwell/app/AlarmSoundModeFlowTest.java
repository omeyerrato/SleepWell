package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.inspector.WindowInspector;
import android.graphics.Rect;
import android.widget.TextView;
import java.io.PrintWriter;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import org.json.*;
import org.junit.Test;
import static org.junit.Assert.*;

/** Private sound-mode configuration only. All scheduler calls are injected; no alarm runtime may open. */
public class AlarmSoundModeFlowTest {
    private final Instrumentation i=InstrumentationRegistry.getInstrumentation();private final Context c=i.getTargetContext();private final UiDevice device=UiDevice.getInstance(i);private AlarmEditorActivity activity;
    private String step="fixture admission";
    private void await(java.util.function.BooleanSupplier ready){assertTrue(step,device.wait(new Condition<UiDevice,Boolean>(){public Boolean apply(UiDevice ignored){return ready.getAsBoolean();}},5000));}
    private View find(View root,String value,boolean description){if(description?value.contentEquals(root.getContentDescription()==null?"":root.getContentDescription()):root instanceof TextView&&value.contentEquals(((TextView)root).getText()))return root;if(root instanceof ViewGroup group)for(int n=0;n<group.getChildCount();n++){View view=find(group.getChildAt(n),value,description);if(view!=null)return view;}return null;}
    private View focusedWindow(){View found=null;for(View root:WindowInspector.getGlobalWindowViews())if(root.getContext().getPackageName().equals(c.getPackageName())&&root.hasWindowFocus()&&root.isAttachedToWindow()&&root.isLaidOut()&&root.getWidth()>0&&root.getHeight()>0){assertNull("One focused application window",found);found=root;}return found;}
    private void ready(){await(()->{boolean[] yes={false};i.runOnMainSync(()->yes[0]=activity!=null&&!activity.isDestroyed()&&activity.hasWindowFocus()&&focusedWindow()==activity.getWindow().getDecorView());return yes[0];});}
    private void frame(View owner)throws Exception{CountDownLatch drawn=new CountDownLatch(1);i.runOnMainSync(()->{assertSame("Exact focused window before frame",owner,focusedWindow());owner.getViewTreeObserver().registerFrameCommitCallback(drawn::countDown);owner.invalidate();});assertTrue(step+": rendered owned frame",drawn.await(5,TimeUnit.SECONDS));i.runOnMainSync(()->assertSame("Frame retains its exact focused window",owner,focusedWindow()));}
    private void require(String value,boolean description){step="require page "+value;ready();await(()->{boolean[] yes={false};i.runOnMainSync(()->{View target=find(activity.getWindow().getDecorView(),value,description);if(target==null)return;target.requestRectangleOnScreen(new Rect(0,0,target.getWidth(),target.getHeight()),true);Rect visible=new Rect();yes[0]=target.isShown()&&target.getGlobalVisibleRect(visible)&&visible.height()==target.getHeight()&&visible.width()==target.getWidth();});return yes[0];});}
    private boolean hasText(String text){boolean[] found={false};i.runOnMainSync(()->found[0]=find(activity.getWindow().getDecorView(),text,false)!=null);return found[0];}
    private void input(View owner,String value,boolean description)throws Exception{
        step="single owned input "+value;
        await(()->{boolean[] ready={false};i.runOnMainSync(()->{if(focusedWindow()!=owner)return;View target=find(owner,value,description);if(target==null)return;while(!target.isClickable()&&target.getParent() instanceof View)target=(View)target.getParent();if(!target.isClickable()||!target.isEnabled())return;target.requestRectangleOnScreen(new Rect(0,0,target.getWidth(),target.getHeight()),true);Rect visible=new Rect();ready[0]=target.getGlobalVisibleRect(visible)&&visible.height()==target.getHeight()&&visible.width()==target.getWidth();});return ready[0];});
        frame(owner);Rect screen=new Rect();i.runOnMainSync(()->{assertSame(owner,focusedWindow());View target=find(owner,value,description);assertNotNull(step,target);while(!target.isClickable()&&target.getParent() instanceof View)target=(View)target.getParent();assertSame("Control belongs to exact decor",owner,target.getRootView());assertTrue(target.isClickable()&&target.isEnabled());Rect visible=new Rect();assertTrue(target.getGlobalVisibleRect(visible));assertEquals("Complete control width",target.getWidth(),visible.width());assertEquals("Complete control height",target.getHeight(),visible.height());assertTrue("48dp width",visible.width()>=Design.dp(c,48));assertTrue("48dp height",visible.height()>=Design.dp(c,48));int[] position=new int[2];target.getLocationOnScreen(position);screen.set(position[0],position[1],position[0]+target.getWidth(),position[1]+target.getHeight());});
        // Exactly one input. Never repeat a delivered action after an absent postcondition.
        assertTrue(step+": dispatched once",device.click(screen.centerX(),screen.centerY()));
    }
    private void click(String text)throws Exception{ready();input(activity.getWindow().getDecorView(),text,false);}
    private void clickDescription(String value)throws Exception{ready();input(activity.getWindow().getDecorView(),value,true);}
    private View dialog(String title)throws Exception{return dialog(title,false);}
    private View dialog(String title,boolean actions)throws Exception{step="await owned "+title+" selector";View[] owner={null};await(()->{i.runOnMainSync(()->{View root=focusedWindow();if(root!=null&&root!=activity.getWindow().getDecorView()&&find(root,(actions?"Actions for ":"Options for ")+title,true)!=null)owner[0]=root;});return owner[0]!=null;});frame(owner[0]);return owner[0];}
    private void pick(String title,String value)throws Exception{clickDescription(title);choose(dialog(title),value,true);}
    private void choose(View owner,String value,boolean description)throws Exception{input(owner,value,description);step="await exact selector detachment after "+value;await(()->!owner.isAttachedToWindow());ready();frame(activity.getWindow().getDecorView());}
    private void launch(Alarm alarm)throws Exception{Instrumentation.ActivityMonitor monitor=i.addMonitor(AlarmEditorActivity.class.getName(),null,false);try{c.startActivity(new Intent(c,AlarmEditorActivity.class).putExtra("alarm",alarm.json().toString()).putExtra("route","soundmodes").putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));activity=(AlarmEditorActivity)monitor.waitForActivityWithTimeout(5000);assertNotNull(activity);}finally{i.removeMonitor(monitor);}require("Sound playback",false);}
    private void recreate()throws Exception{Instrumentation.ActivityMonitor monitor=i.addMonitor(AlarmEditorActivity.class.getName(),null,false);try{i.runOnMainSync(activity::recreate);activity=(AlarmEditorActivity)monitor.waitForActivityWithTimeout(5000);assertNotNull(activity);}finally{i.removeMonitor(monitor);}require("Sound playback",false);}
    private void stableTonePage(String action)throws Exception{
        step="await loaded owned tone page before "+action;
        ready();await(()->{boolean[] stable={false};i.runOnMainSync(()->stable[0]=activity.toneAssetsReady()&&activity.toneCaptureView()==activity.getWindow().getDecorView()&&focusedWindow()==activity.getWindow().getDecorView()&&find(activity.getWindow().getDecorView(),"Alarm sound",false)!=null&&find(activity.getWindow().getDecorView(),action,false)!=null);return stable[0];});
        // loadWaves posts a full page replacement. Await its actual completion before measuring input.
        frame(activity.getWindow().getDecorView());require(action,false);
    }
    private void capture(String name)throws Exception{step="capture alarm-mode-"+name;ready();frame(activity.getWindow().getDecorView());File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());assertTrue(device.takeScreenshot(new File(folder,"alarm-mode-"+name+".png")));}
    private boolean failureCaptured;
    private void failure(Throwable problem){if(failureCaptured)return;failureCaptured=true;try{File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());try(PrintWriter out=new PrintWriter(new File(folder,"alarm-mode-failure.txt"),"UTF-8")){out.println(step);problem.printStackTrace(out);}device.takeScreenshot(new File(folder,"alarm-mode-failure.png"));device.dumpWindowHierarchy(new File(folder,"alarm-mode-failure.xml"));}catch(Exception diagnostic){problem.addSuppressed(diagnostic);}}
    @Test public void silentAndPlaylistChoicesCancelRecreateSaveReopenAndReturnSingleWithoutOutputs()throws Exception{
        assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertEquals("1",device.executeShellCommand("getprop ro.kernel.qemu").trim());assertEquals(0,Prefs.number(c,"checkPending",0));List<Activity> owned=new CopyOnWriteArrayList<>();Application app=(Application)c.getApplicationContext();Application.ActivityLifecycleCallbacks observer=new Application.ActivityLifecycleCallbacks(){public void onActivityCreated(Activity a,Bundle b){owned.add(a);assertTrue("Only inert configuration Activity",a instanceof AlarmEditorActivity);}public void onActivityStarted(Activity a){}public void onActivityResumed(Activity a){}public void onActivityPaused(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,Bundle b){}public void onActivityDestroyed(Activity a){}};app.registerActivityLifecycleCallbacks(observer);
        try(AlarmSkipFixture f=new AlarmSkipFixture()){try{Alarm initial=f.load();initial.enabled=false;initial.awakeCheck=1;try(Store store=f.open()){store.save(initial);}launch(initial);require("Single sound",false);capture("initial");pick("Playback mode","Silent");require("Silent",false);capture("silent");assertEquals(AlarmSoundMode.SINGLE,f.load().soundMode);
            pick("Playback mode","Playlist");require("1. Daybreak",false);capture("playlist");click("Add sound");View add=dialog("Add sound",true);choose(add,"Chime",true);clickDescription("Shuffle order");require("3. Chime",false);capture("configured");click("Playback mode");View cancelled=dialog("Playback mode");choose(cancelled,"Cancel",false);require("Shuffled playlist · 3",false);recreate();require("Shuffled playlist · 3",false);capture("recreated");click("Use playback settings");require("Alarm sound",false);stableTonePage("Use Shuffled playlist · 3");click("Use Shuffled playlist · 3");require("Edit alarm",false);click("Save alarm");await(activity::isDestroyed);Alarm saved=f.load();assertEquals(new AlarmSoundMode(AlarmSoundMode.Kind.PLAYLIST,List.of("daybreak","pulse","chime"),true),saved.soundMode);assertEquals(1,f.schedules);
            launch(saved);require("Shuffled playlist · 3",false);capture("reopened");pick("Playback mode","Single sound");require("Single sound",false);capture("single");click("Use playback settings");require("Alarm sound",false);stableTonePage("Use Daybreak");click("Use Daybreak");require("Edit alarm",false);click("Save alarm");await(activity::isDestroyed);assertEquals(AlarmSoundMode.SINGLE,f.load().soundMode);assertEquals(2,f.schedules);
        }catch(Exception|AssertionError problem){failure(problem);throw problem;}finally{i.runOnMainSync(()->{for(Activity a:owned)if(!a.isDestroyed())a.finish();});await(()->owned.stream().allMatch(Activity::isDestroyed));}}catch(Exception|AssertionError problem){failure(problem);throw problem;}finally{app.unregisterActivityLifecycleCallbacks(observer);}
    }
}
