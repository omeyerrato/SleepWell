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

/** Private date configuration only. All scheduler calls are injected; no alarm runtime may open. */
public class SpeakConfigurationFlowTest {
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
    private View dialog(String title)throws Exception{step="await owned "+title+" selector";View[] owner={null};await(()->{i.runOnMainSync(()->{View root=focusedWindow();if(root!=null&&root!=activity.getWindow().getDecorView()&&find(root,"Options for "+title,true)!=null)owner[0]=root;});return owner[0]!=null;});frame(owner[0]);return owner[0];}
    private void pick(String title,String value)throws Exception{clickDescription(title);choose(dialog(title),value,true);}
    private void choose(View owner,String value,boolean description)throws Exception{input(owner,value,description);step="await exact selector detachment after "+value;await(()->!owner.isAttachedToWindow());ready();frame(activity.getWindow().getDecorView());}
    private void launch(Alarm alarm)throws Exception{Instrumentation.ActivityMonitor monitor=i.addMonitor(AlarmEditorActivity.class.getName(),null,false);try{c.startActivity(new Intent(c,AlarmEditorActivity.class).putExtra("alarm",alarm.json().toString()).putExtra("route","challenge").putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));activity=(AlarmEditorActivity)monitor.waitForActivityWithTimeout(5000);assertNotNull(activity);}finally{i.removeMonitor(monitor);}ready();}
    private void recreate()throws Exception{Instrumentation.ActivityMonitor monitor=i.addMonitor(AlarmEditorActivity.class.getName(),null,false);try{i.runOnMainSync(activity::recreate);activity=(AlarmEditorActivity)monitor.waitForActivityWithTimeout(5000);assertNotNull(activity);}finally{i.removeMonitor(monitor);}ready();}
    private void capture(String name)throws Exception{step="capture speak-config-"+name;ready();frame(activity.getWindow().getDecorView());File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());assertTrue(device.takeScreenshot(new File(folder,"speak-config-"+name+".png")));}
    private boolean failureCaptured;
    private void failure(Throwable problem){if(failureCaptured)return;failureCaptured=true;try{File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());try(PrintWriter out=new PrintWriter(new File(folder,"speak-config-failure.txt"),"UTF-8")){out.println(step);problem.printStackTrace(out);}device.takeScreenshot(new File(folder,"speak-config-failure.png"));device.dumpWindowHierarchy(new File(folder,"speak-config-failure.xml"));}catch(Exception diagnostic){problem.addSuppressed(diagnostic);}}
    @Test public void explicitSpeakTaskConfigureRecreateSaveAndReopenPreservesOldTasks()throws Exception{
        assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertEquals("1",device.executeShellCommand("getprop ro.kernel.qemu").trim());assertEquals(0,Prefs.number(c,"checkPending",0));try(AlarmSkipFixture f=new AlarmSkipFixture()){try{Alarm initial=f.load();initial.enabled=false;initial.awakeCheck=1;try(Store store=f.open()){store.save(initial);}launch(initial);clickDescription("Configure Speak");require("Use Speak",false);capture("task");recreate();require("Use Speak",false);capture("recreated");assertEquals(1,f.load().challenges().tasks.size());click("Use Speak");require("Challenges",false);require("Edit task 2 Speak",true);capture("routine");click("Save routine");require("Edit alarm",false);click("Save alarm");await(activity::isDestroyed);Alarm saved=f.load();assertEquals(2,saved.challenges().tasks.size());assertEquals("Math",saved.challenges().tasks.get(0).type);assertEquals("Speak",saved.challenges().tasks.get(1).type);assertEquals(1,saved.challenges().tasks.get(1).rounds);assertEquals(1,f.schedules);launch(saved);require("Edit task 2 Speak",true);capture("reopened");}catch(Exception|AssertionError failure){failure(failure);throw failure;}finally{if(activity!=null)i.runOnMainSync(activity::finish);await(()->activity==null||activity.isDestroyed());}}
    }
}
