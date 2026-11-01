package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.graphics.Rect;
import android.os.*;
import android.view.*;
import android.view.inspector.WindowInspector;
import android.widget.TextView;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.Test;
import static org.junit.Assert.*;

/** One real editor/result journey. Private disabled rows; no runtime alarm or output. */
public class RandomChallengeDraftFlowTest {
    private final Instrumentation i=InstrumentationRegistry.getInstrumentation();
    private final Context c=i.getTargetContext();
    private final UiDevice device=UiDevice.getInstance(i);
    private final List<Activity> owned=new CopyOnWriteArrayList<>();
    private volatile Activity top;
    private AlarmEditorActivity parent;
    private RandomChallengeActivity random;
    private String step="private fixture admission";
    private <T>T main(Callable<T> read){Object[] value={null};i.runOnMainSync(()->{try{value[0]=read.call();}catch(Exception failure){throw new AssertionError(failure);}});@SuppressWarnings("unchecked") T result=(T)value[0];return result;}
    private <T>T field(Object owner,String name)throws Exception{@SuppressWarnings("unchecked") T value=(T)readField(owner,name);return value;}
    private Object readField(Object owner,String name)throws Exception{java.lang.reflect.Field field=owner.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(owner);}
    private void await(java.util.function.BooleanSupplier ready){assertTrue(step,device.wait(new Condition<UiDevice,Boolean>(){public Boolean apply(UiDevice ignored){return ready.getAsBoolean();}},5000));}
    private View find(View root,String value,boolean description){if(description?value.contentEquals(root.getContentDescription()==null?"":root.getContentDescription()):root instanceof TextView text&&value.contentEquals(text.getText()))return root;if(root instanceof ViewGroup group)for(int n=0;n<group.getChildCount();n++){View found=find(group.getChildAt(n),value,description);if(found!=null)return found;}return null;}
    private View focused(){View found=null;for(View root:WindowInspector.getGlobalWindowViews())if(c.getPackageName().equals(root.getContext().getPackageName())&&root.hasWindowFocus()&&root.isAttachedToWindow()&&root.isLaidOut()&&root.getWidth()>0&&root.getHeight()>0){assertNull("One owned focused application window",found);found=root;}return found;}
    private void ready(Activity owner){await(()->main(()->top==owner&&!owner.isDestroyed()&&owner.hasWindowFocus()&&focused()==owner.getWindow().getDecorView()));}
    private void frame(View owner)throws Exception{CountDownLatch drawn=new CountDownLatch(1);i.runOnMainSync(()->{assertSame(owner,focused());owner.getViewTreeObserver().registerFrameCommitCallback(drawn::countDown);owner.invalidate();});assertTrue(step+": committed owned frame",drawn.await(5,TimeUnit.SECONDS));i.runOnMainSync(()->assertSame(owner,focused()));}
    private void input(View owner,String value,boolean description)throws Exception{
        step="single owned input "+value;
        await(()->main(()->{if(focused()!=owner)return false;View target=find(owner,value,description);if(target==null)return false;while(!target.isClickable()&&target.getParent() instanceof View)target=(View)target.getParent();if(!target.isClickable()||!target.isEnabled())return false;target.requestRectangleOnScreen(new Rect(0,0,target.getWidth(),target.getHeight()),true);Rect visible=new Rect();return target.getGlobalVisibleRect(visible)&&visible.width()==target.getWidth()&&visible.height()==target.getHeight();}));
        frame(owner);Rect hit=main(()->{assertSame(owner,focused());View target=find(owner,value,description);assertNotNull(value,target);while(!target.isClickable()&&target.getParent() instanceof View)target=(View)target.getParent();assertSame(owner,target.getRootView());assertTrue(target.isEnabled()&&target.isClickable());Rect visible=new Rect();assertTrue(target.getGlobalVisibleRect(visible));assertEquals(target.getWidth(),visible.width());assertEquals(target.getHeight(),visible.height());assertTrue("48dp width "+value,visible.width()>=Design.dp(c,48));assertTrue("48dp height "+value,visible.height()>=Design.dp(c,48));return visible;});
        assertTrue("Dispatched once: "+value,device.click(hit.centerX(),hit.centerY()));
    }
    private void click(Activity owner,String text)throws Exception{ready(owner);input(owner.getWindow().getDecorView(),text,false);}
    private void describe(Activity owner,String value)throws Exception{ready(owner);input(owner.getWindow().getDecorView(),value,true);}
    private View modal(Activity owner,String value,boolean description)throws Exception{step="owned modal "+value;View[] found={null};await(()->main(()->{View root=focused();if(top!=owner||root==null||root==owner.getWindow().getDecorView()||find(root,value,description)==null)return false;found[0]=root;return true;}));frame(found[0]);return found[0];}
    private View exitReview()throws Exception{return modal(random,"Discard random task changes?",false);}
    private void choose(View owner,String value,boolean description)throws Exception{input(owner,value,description);await(()->!owner.isAttachedToWindow());}
    private void count(int value)throws Exception{describe(random,"Tasks per occurrence");View picker=modal(random,"Options for Tasks per occurrence",true);choose(picker,value+(value==1?" task":" tasks"),true);ready(random);}
    private void child()throws Exception{step="current Random tasks child";await(()->top instanceof RandomChallengeActivity&&!top.isDestroyed());random=(RandomChallengeActivity)top;ready(random);}
    private ChallengeRoutine randomDraft(){return main(()->ChallengeRoutine.random(this.<List<ChallengeRoutine.Task>>field(random,"pool"),this.<Integer>field(random,"count")));}
    private String parentDraft(){return main(()->ChallengeRoutineJson.encode(this.<ChallengeRoutine>field(parent,"routineDraft")).toString());}
    private void open()throws Exception{click(parent,"Configure random pool");child();}
    private void capture(String suffix,View owner)throws Exception{frame(owner);File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());assertTrue(device.takeScreenshot(new File(folder,"random-pool-"+suffix+".png")));}
    private void unchanged(AlarmSkipFixture fixture,String stored)throws Exception{try(Store store=fixture.open()){assertEquals("Child edits never write the library",stored,store.backup().toString());}assertEquals(0,fixture.schedules);assertEquals(fixture.preferences,Prefs.of(c).getAll());assertFalse(AlarmService.active);assertFalse(TrackingService.running);assertFalse(Prefs.flag(c,"ringing"));assertNull(SleepSoundService.snapshot());}
    private void failure(Throwable failure){try{File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());try(PrintWriter out=new PrintWriter(new File(folder,"random-pool-failure.txt"),"UTF-8")){out.println(step);failure.printStackTrace(out);}device.takeScreenshot(new File(folder,"random-pool-failure.png"));device.dumpWindowHierarchy(new File(folder,"random-pool-failure.xml"));}catch(Exception diagnostic){failure.addSuppressed(diagnostic);}}

    @Test public void dirtyBackKeepDiscardAndUseReturnOnlyParentDraftWithoutOutputs()throws Exception{
        assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertEquals("1",device.executeShellCommand("getprop ro.kernel.qemu").trim());assertFalse(AlarmService.active);assertFalse(TrackingService.running);assertFalse(Prefs.flag(c,"ringing"));assertEquals(0,Prefs.number(c,"checkPending",0));
        Application app=(Application)c.getApplicationContext();Application.ActivityLifecycleCallbacks observer=new Application.ActivityLifecycleCallbacks(){public void onActivityCreated(Activity activity,Bundle state){owned.add(activity);assertTrue("Only private editor/configuration owners",activity instanceof AlarmEditorActivity||activity instanceof RandomChallengeActivity);assertTrue("Configuration interaction feedback is quiet",activity.getIntent().getBooleanExtra("visualReview",false));}public void onActivityResumed(Activity activity){top=activity;}public void onActivityStarted(Activity activity){}public void onActivityPaused(Activity activity){}public void onActivityStopped(Activity activity){}public void onActivitySaveInstanceState(Activity activity,Bundle state){}public void onActivityDestroyed(Activity activity){}};
        app.registerActivityLifecycleCallbacks(observer);
        try(AlarmSkipFixture fixture=new AlarmSkipFixture()){try{
            ChallengeRoutine source=ChallengeRoutine.random(List.of(new ChallengeRoutine.Task("Math",1,0,new MathSettings(MathSettings.Level.EASY,MathSettings.Operator.ADD.bit,false)),new ChallengeRoutine.Task("Memory",1),new ChallengeRoutine.Task("Counting",1)),2);
            Alarm initial;String stored;try(Store store=fixture.open()){for(Alarm alarm:store.alarms()){alarm.enabled=false;if(alarm.id==1)alarm.setRoutine(source);store.save(alarm);}fixture.sentinel=store.alarm(2).json().toString();initial=store.alarm(1);stored=store.backup().toString();}
            Instrumentation.ActivityMonitor launch=i.addMonitor(AlarmEditorActivity.class.getName(),null,false);try{c.startActivity(new Intent(c,AlarmEditorActivity.class).putExtra("alarm",initial.json().toString()).putExtra("route","challenge").putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));parent=(AlarmEditorActivity)launch.waitForActivityWithTimeout(5000);assertNotNull(parent);}finally{i.removeMonitor(launch);}ready(parent);String original=parentDraft();
            open();describe(random,"1. Math");click(random,"Hard");ready(random);assertTrue(device.pressBack());ready(random);assertEquals("Nested Back retains edited pool",MathSettings.Level.HARD,randomDraft().tasks.get(0).math.level);assertEquals(2,randomDraft().randomCount);capture("edited",random.getWindow().getDecorView());unchanged(fixture,stored);
            assertTrue(device.pressBack());View oldReview=exitReview();View retiredDiscard=main(()->find(oldReview,"Discard changes",false));assertNotNull(retiredDiscard);capture("dirty-back",oldReview);
            RandomChallengeActivity retired=random;Instrumentation.ActivityMonitor recreation=i.addMonitor(RandomChallengeActivity.class.getName(),null,false);try{i.runOnMainSync(retired::recreate);random=(RandomChallengeActivity)recreation.waitForActivityWithTimeout(5000);assertNotNull(random);}finally{i.removeMonitor(recreation);}await(retired::isDestroyed);View restoredReview=exitReview();assertEquals(MathSettings.Level.HARD,randomDraft().tasks.get(0).math.level);i.runOnMainSync(retiredDiscard::performClick);assertFalse("Retired discard cannot finish recreated owner",random.isFinishing());assertTrue(restoredReview.isAttachedToWindow());capture("review-recreated",restoredReview);choose(restoredReview,"Keep editing",false);ready(random);assertEquals(MathSettings.Level.HARD,randomDraft().tasks.get(0).math.level);capture("kept",random.getWindow().getDecorView());unchanged(fixture,stored);
            count(1);click(random,"Cancel");View discard=exitReview();capture("discard",discard);choose(discard,"Discard changes",false);await(()->top==parent);ready(parent);assertEquals("Discard returns unchanged parent draft",original,parentDraft());unchanged(fixture,stored);capture("discarded-parent",parent.getWindow().getDecorView());
            open();assertEquals(MathSettings.Level.EASY,randomDraft().tasks.get(0).math.level);assertEquals(2,randomDraft().randomCount);assertTrue(device.pressBack());await(()->top==parent);ready(parent);assertEquals("Clean Back exits without a review",original,parentDraft());
            open();count(1);capture("configured",random.getWindow().getDecorView());click(random,"Use random routine");await(()->top==parent);ready(parent);ChallengeRoutine returned=RandomChallenges.decode(new org.json.JSONArray(parentDraft()));assertEquals(1,returned.randomCount);assertEquals(MathSettings.Level.EASY,returned.tasks.get(0).math.level);unchanged(fixture,stored);capture("returned-parent",parent.getWindow().getDecorView());String applied=parentDraft();
            Instrumentation.ActivityMonitor parentRecreation=i.addMonitor(AlarmEditorActivity.class.getName(),null,false);AlarmEditorActivity oldParent=parent;try{i.runOnMainSync(oldParent::recreate);parent=(AlarmEditorActivity)parentRecreation.waitForActivityWithTimeout(5000);assertNotNull(parent);}finally{i.removeMonitor(parentRecreation);}ready(parent);assertEquals("Returned parent draft survives its recreation",applied,parentDraft());unchanged(fixture,stored);capture("parent-recreated",parent.getWindow().getDecorView());
            org.json.JSONObject proof=new org.json.JSONObject().put("scope","08D actual private parent editor and Random tasks child").put("dirty_nested_back_keeps_pool",true).put("dirty_outer_back_review",true).put("review_and_draft_survive_recreation",true).put("retired_discard_cannot_finish_current_owner",true).put("keep_editing_retains_draft",true).put("discard_keeps_parent_unchanged",true).put("clean_back_has_no_review",true).put("use_returns_parent_draft_only",true).put("parent_draft_survives_recreation",true).put("library_unchanged",true).put("scheduling_calls",fixture.schedules).put("actual_alarm_started",false).put("outputs_started",false);try(PrintWriter out=new PrintWriter(new File(c.getExternalFilesDir(null),"verification/random-pool-draft-proof.json"),"UTF-8")){out.println(proof.toString(2));}
        }catch(Exception|AssertionError problem){failure(problem);throw problem;}finally{i.runOnMainSync(()->{for(Activity activity:owned)if(!activity.isDestroyed())activity.finish();});step="all private owners destroyed before fixture cleanup";await(()->owned.stream().allMatch(Activity::isDestroyed));}}
        finally{app.unregisterActivityLifecycleCallbacks(observer);}
    }
}
