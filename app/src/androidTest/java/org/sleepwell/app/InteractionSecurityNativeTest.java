package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

/** Bounded emulator checks: private draft storage and an inert native consent button only. */
public final class InteractionSecurityNativeTest {
    private final Instrumentation i=InstrumentationRegistry.getInstrumentation();
    private final Context c=i.getTargetContext();
    private void ciOnly()throws Exception{assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertEquals("1",UiDevice.getInstance(i).executeShellCommand("getprop ro.kernel.qemu").trim());assertFalse(AlarmService.active);assertFalse(TrackingService.running);}
    private void await(java.util.function.BooleanSupplier ready)throws Exception{long end=SystemClock.elapsedRealtime()+5000;while(!ready.getAsBoolean()&&SystemClock.elapsedRealtime()<end)Thread.sleep(20);assertTrue("Native state ready",ready.getAsBoolean());}
    private HomeLaunchActivity launch(Intent input)throws Exception{Instrumentation.ActivityMonitor monitor=i.addMonitor(HomeLaunchActivity.class.getName(),null,false);try{c.startActivity(input.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));HomeLaunchActivity result=(HomeLaunchActivity)monitor.waitForActivityWithTimeout(5000);assertNotNull(result);await(result::hasWindowFocus);return result;}finally{i.removeMonitor(monitor);}}
    private ExecutorService worker(HomeLaunchActivity activity)throws Exception{Field field=HomeLaunchActivity.class.getDeclaredField("worker");field.setAccessible(true);return(ExecutorService)field.get(activity);}
    private void close(Activity activity)throws Exception{if(activity==null)return;if(!activity.isDestroyed())i.runOnMainSync(activity::finish);await(activity::isDestroyed);if(activity instanceof HomeLaunchActivity home)assertTrue(worker(home).awaitTermination(5000,TimeUnit.MILLISECONDS));}
    private Activity incomingFixture()throws Exception{Instrumentation.ActivityMonitor monitor=i.addMonitor(IncomingAutomationActivity.class.getName(),null,false);try{c.startActivity(new Intent(c,IncomingAutomationActivity.class).setAction("invalid-fixture-request").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));Activity result=monitor.waitForActivityWithTimeout(5000);assertNotNull(result);await(result::hasWindowFocus);return result;}finally{i.removeMonitor(monitor);}}
    private void barrier(CountDownLatch latch){try{if(!latch.await(5,TimeUnit.SECONDS))throw new IllegalStateException("Fixture release timed out");}catch(InterruptedException stopped){Thread.currentThread().interrupt();throw new IllegalStateException(stopped);}}
    @Test public void delayedShortcutDraftCannotRouteAfterCancelOrWhilePaused()throws Exception{
        ciOnly();String name="interaction-route-"+UUID.randomUUID();Records.Factory oldRecords=Records.factory;AlarmDefaultsSettings.Factory oldDefaults=AlarmDefaultsSettings.factory;SharedPreferences defaults=c.getSharedPreferences(name,0);CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1),pausedEntered=new CountDownLatch(1),pausedRelease=new CountDownLatch(1);HomeLaunchActivity cancelled=null,paused=null;Instrumentation.ActivityMonitor editor=i.addMonitor(AlarmEditorActivity.class.getName(),new Instrumentation.ActivityResult(Activity.RESULT_CANCELED,null),true);
        try{
            AlarmDefaultsSettings.factory=context->AlarmDefaultsSettings.of(defaults);Records.factory=context->{entered.countDown();barrier(release);return new Store(context,name+".db");};cancelled=launch(HomeLaunch.intent(c,HomeLaunch.Screen.ADD_ALARM,0));assertTrue(entered.await(5,TimeUnit.SECONDS));HomeLaunchActivity first=cancelled;i.runOnMainSync(first::onBackPressed);release.countDown();close(first);i.waitForIdleSync();assertEquals(0,editor.getHits());
            Records.factory=context->{pausedEntered.countDown();barrier(pausedRelease);return new Store(context,name+".db");};paused=launch(HomeLaunch.intent(c,HomeLaunch.Screen.ADD_ALARM,0));assertTrue(pausedEntered.await(5,TimeUnit.SECONDS));HomeLaunchActivity second=paused;i.runOnMainSync(()->i.callActivityOnPause(second));pausedRelease.countDown();worker(second).submit(()->{}).get(5,TimeUnit.SECONDS);i.waitForIdleSync();assertEquals(0,editor.getHits());i.runOnMainSync(()->i.callActivityOnResume(second));i.waitForIdleSync();assertEquals(1,editor.getHits());close(second);
            try(Store store=new Store(c,name+".db")){assertTrue(store.alarms().isEmpty());assertTrue(store.rows("SELECT * FROM sessions").isEmpty());}assertTrue(defaults.getAll().isEmpty());
        }finally{release.countDown();pausedRelease.countDown();try{try{close(cancelled);}finally{close(paused);}}finally{i.removeMonitor(editor);}Records.factory=oldRecords;AlarmDefaultsSettings.factory=oldDefaults;c.deleteDatabase(name+".db");c.deleteSharedPreferences(name);}assertFalse(AlarmService.active);assertFalse(TrackingService.running);
    }
    private void touch(SensitiveReviewTouches guard,Activity activity,Button button,int action,int flags){long now=SystemClock.uptimeMillis();MotionEvent.PointerProperties property=new MotionEvent.PointerProperties();property.id=0;property.toolType=MotionEvent.TOOL_TYPE_FINGER;MotionEvent.PointerCoords point=new MotionEvent.PointerCoords();point.x=button.getWidth()/2f;point.y=button.getHeight()/2f;point.pressure=1;point.size=1;MotionEvent event=MotionEvent.obtain(now,now,action,1,new MotionEvent.PointerProperties[]{property},new MotionEvent.PointerCoords[]{point},0,0,1,1,0,0,InputDevice.SOURCE_TOUCHSCREEN,flags);try{if(guard==null)activity.dispatchTouchEvent(event);else guard.dispatch(activity,event,button::dispatchTouchEvent);}finally{event.recycle();}}
    private void gestures(Activity host,boolean actualActivityDispatch)throws Exception{
        Button[] button={null};AtomicInteger clicks=new AtomicInteger(),cancels=new AtomicInteger();i.runOnMainSync(()->{button[0]=new Button(host){@Override public boolean dispatchTouchEvent(MotionEvent event){if(event.getActionMasked()==MotionEvent.ACTION_CANCEL)cancels.incrementAndGet();return super.dispatchTouchEvent(event);}};button[0].setText("Inert authorization fixture");button[0].setOnClickListener(view->clicks.incrementAndGet());host.setContentView(button[0]);});i.waitForIdleSync();
            for(int flag:new int[]{MotionEvent.FLAG_WINDOW_IS_OBSCURED,MotionEvent.FLAG_WINDOW_IS_PARTIALLY_OBSCURED}){
                SensitiveReviewTouches guard=actualActivityDispatch?null:new SensitiveReviewTouches();int before=clicks.get(),cancelBefore=cancels.get();i.runOnMainSync(()->{touch(guard,host,button[0],MotionEvent.ACTION_DOWN,flag);touch(guard,host,button[0],MotionEvent.ACTION_UP,0);});i.waitForIdleSync();assertEquals(before,clicks.get());int afterDown=cancels.get();if(!actualActivityDispatch)assertEquals(cancelBefore+1,afterDown);
                i.runOnMainSync(()->{touch(guard,host,button[0],MotionEvent.ACTION_DOWN,0);touch(guard,host,button[0],MotionEvent.ACTION_UP,flag);touch(guard,host,button[0],MotionEvent.ACTION_UP,0);});i.waitForIdleSync();assertEquals(before,clicks.get());assertEquals(afterDown+1,cancels.get());
                i.runOnMainSync(()->{touch(guard,host,button[0],MotionEvent.ACTION_DOWN,0);touch(guard,host,button[0],MotionEvent.ACTION_UP,0);});i.waitForIdleSync();assertEquals(before+1,clicks.get());
            }
    }
    @Test public void obscuredNativeGesturesCancelAndRequireAFreshCleanDown()throws Exception{
        ciOnly();Activity activity=null;try{activity=launch(new Intent(c,HomeLaunchActivity.class).setAction("invalid-fixture-route"));gestures(activity,false);}finally{close(activity);}
        // Invalid terminal request: no settings opt-in, journal receipt, grant or service dispatch.
        activity=null;try{activity=incomingFixture();gestures(activity,true);}finally{close(activity);}assertFalse(AlarmService.active);assertFalse(TrackingService.running);
    }
}
