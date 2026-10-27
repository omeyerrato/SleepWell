package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.graphics.Rect;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.view.inspector.WindowInspector;
import androidx.activity.result.ActivityResultLauncher;
import androidx.test.uiautomator.UiDevice;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.BooleanSupplier;
import static org.junit.Assert.*;

/** Part of the existing MotionConfigurationFlowTest method; no hardware or Android permission grant. */
final class RoomConditionsFixture {
    private final Instrumentation i;private final Context c;private final UiDevice device;
    private RoomConditionsActivity activity;private final List<Activity> owned=new CopyOnWriteArrayList<>();
    private final List<Light> lightPorts=new CopyOnWriteArrayList<>();private final AtomicInteger opens=new AtomicInteger(),closes=new AtomicInteger();
    private final AtomicBoolean unavailable=new AtomicBoolean(),failOpen=new AtomicBoolean(),failClose=new AtomicBoolean();
    private final AtomicBoolean rationaleNeeded=new AtomicBoolean();private final AtomicInteger permissionRequests=new AtomicInteger();
    private String step="Room fixture admission";
    private static final class Light implements RoomLightPort {
        final boolean available;volatile Sink sink;Light(boolean available){this.available=available;}
        public boolean available(){return available;}public boolean begin(Sink target){sink=target;return available;}public void close(){sink=null;}
        void emit(float lux){assertNotNull(sink);sink.sample(SystemClock.elapsedRealtime(),lux,true);}
    }
    private final class Capture implements RoomSoundLevel.Capture {
        private final BooleanSupplier current;private final MicrophoneLease lease;private boolean closed;
        Capture(BooleanSupplier current){this.current=current;lease=MicrophoneLease.acquire("room-native-fixture");assertNotNull("Owned fake microphone lease",lease);}
        public void start(){assertTrue(current.getAsBoolean());}
        public int read(short[] values){if(!current.getAsBoolean())return 0;Arrays.fill(values,(short)8192);return values.length;}
        public void close(){if(failClose.get())throw new IllegalStateException("Injected cleanup failure");if(!closed){closed=true;lease.released();closes.incrementAndGet();}}
    }
    private RoomConditionsFixture(Instrumentation i,Context c,UiDevice device){this.i=i;this.c=c;this.device=device;}
    static void verify(Instrumentation i,Context c,UiDevice device)throws Exception{new RoomConditionsFixture(i,c,device).run();}
    private void await(BooleanSupplier condition)throws Exception{long until=SystemClock.elapsedRealtime()+10000;while(!condition.getAsBoolean()&&SystemClock.elapsedRealtime()<until)Thread.sleep(30);assertTrue("Room fixture reached its actual owned state",condition.getAsBoolean());}
    private void ready()throws Exception{await(()->{boolean[] value={false};i.runOnMainSync(()->value[0]=activity!=null&&!activity.isDestroyed()&&activity.hasWindowFocus()&&activity.getWindow().getDecorView().isLaidOut());return value[0];});}
    private void launch()throws Exception{step="Launch owned Room screen";Instrumentation.ActivityMonitor monitor=i.addMonitor(RoomConditionsActivity.class.getName(),null,false);try{i.runOnMainSync(()->c.startActivity(new Intent(c,RoomConditionsActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));activity=(RoomConditionsActivity)monitor.waitForActivityWithTimeout(5000);assertNotNull(activity);}finally{i.removeMonitor(monitor);}ready();}
    private void recreate()throws Exception{step="Recreate Room without replaying microphone";RoomConditionsActivity old=activity;Instrumentation.ActivityMonitor monitor=i.addMonitor(RoomConditionsActivity.class.getName(),null,false);try{i.runOnMainSync(old::recreate);activity=(RoomConditionsActivity)monitor.waitForActivityWithTimeout(5000);assertNotNull(activity);}finally{i.removeMonitor(monitor);}ready();await(old::isDestroyed);await(()->RoomSoundLevel.current()==null);}
    private void finish()throws Exception{RoomConditionsActivity old=activity;i.runOnMainSync(old::finish);await(old::isDestroyed);await(()->RoomSoundLevel.current()==null);assertFalse(MicrophoneLease.occupied());}
    private View control(View root,String label){if(root.isClickable()&&(label.equals(root.getContentDescription())||root instanceof TextView&&label.contentEquals(((TextView)root).getText())))return root;if(root instanceof ViewGroup group)for(int n=0;n<group.getChildCount();n++){View found=control(group.getChildAt(n),label);if(found!=null)return found;}return null;}
    private boolean text(View root,String value){if(root instanceof TextView&&value.contentEquals(((TextView)root).getText()))return true;if(root instanceof ViewGroup group)for(int n=0;n<group.getChildCount();n++)if(text(group.getChildAt(n),value))return true;return false;}
    private boolean shown(String value){boolean[] found={false};i.runOnMainSync(()->found[0]=text(activity.getWindow().getDecorView(),value));return found[0];}
    private Rect bounds(String label,boolean scroll){Rect[] result={null};i.runOnMainSync(()->{View target=control(activity.getWindow().getDecorView(),label);assertNotNull(label,target);Rect visible=new Rect();if(target.isEnabled()&&target.getGlobalVisibleRect(visible)&&visible.width()==target.getWidth()&&visible.height()==target.getHeight()){assertTrue(label+" full 48dp width",visible.width()>=Design.dp(c,48));assertTrue(label+" full 48dp height",visible.height()>=Design.dp(c,48));int[] screen=new int[2];target.getLocationOnScreen(screen);result[0]=new Rect(screen[0],screen[1],screen[0]+target.getWidth(),screen[1]+target.getHeight());}else if(scroll)target.requestRectangleOnScreen(new Rect(0,0,target.getWidth(),target.getHeight()),true);});return result[0];}
    private void commitFrame()throws Exception{CountDownLatch frame=new CountDownLatch(1);i.runOnMainSync(()->{View root=activity.getWindow().getDecorView();assertTrue(root.hasWindowFocus());root.getViewTreeObserver().registerFrameCommitCallback(frame::countDown);root.invalidate();});assertTrue(frame.await(5,TimeUnit.SECONDS));}
    private void click(String label)throws Exception{step="Single focused Room click: "+label;ready();await(()->bounds(label,true)!=null);commitFrame();Rect target=bounds(label,false);assertNotNull(target);assertTrue("One real focused input",device.click(target.centerX(),target.centerY()));}
    private void capture(String literal)throws Exception{step="Capture "+literal;ready();assertNotNull("Fixed footer remains fully reachable",bounds("Use bedtime dimming",false));commitFrame();File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());assertTrue(device.takeScreenshot(new File(folder,literal+".png")));}
    private View focused(){View found=null;for(View root:WindowInspector.getGlobalWindowViews())if(root.isAttachedToWindow()&&root.hasWindowFocus()&&root.getContext().getPackageName().equals(c.getPackageName())){assertNull("One owned focused window",found);found=root;}return found;}
    private void awaitDialog()throws Exception{await(()->{boolean[] found={false};i.runOnMainSync(()->{View root=focused();found[0]=root!=null&&text(root,"Microphone for room sound");});return found[0];});}
    private void dialogClick(String label)throws Exception{
        step="Single focused Room dialog input: "+label;
        View[] owner={null},button={null};Rect[] visible={null},target={null};int[] origin=new int[2];
        await(()->{boolean[] found={false};i.runOnMainSync(()->{View root=focused();if(root==null)return;View value=control(root,label);if(value==null)return;Rect area=new Rect();if(value.isEnabled()&&value.getGlobalVisibleRect(area)&&area.width()==value.getWidth()&&area.height()==value.getHeight()){assertTrue(area.width()>=Design.dp(c,48));assertTrue(area.height()>=Design.dp(c,48));owner[0]=root;button[0]=value;found[0]=true;}});return found[0];});
        CountDownLatch frame=new CountDownLatch(1);i.runOnMainSync(()->{assertSame(owner[0],focused());owner[0].getViewTreeObserver().registerFrameCommitCallback(frame::countDown);owner[0].invalidate();});assertTrue(frame.await(5,TimeUnit.SECONDS));
        i.runOnMainSync(()->{assertSame("Committed dialog still owns focus",owner[0],focused());View value=button[0];assertSame(value,control(owner[0],label));assertTrue(value.isEnabled()&&value.isAttachedToWindow());Rect area=new Rect();assertTrue(value.getGlobalVisibleRect(area));assertEquals(value.getWidth(),area.width());assertEquals(value.getHeight(),area.height());assertTrue(area.width()>=Design.dp(c,48));assertTrue(area.height()>=Design.dp(c,48));visible[0]=area;
            // Global-visible coordinates belong to the floating window's root, not the screen.
            int[] screen=new int[2];value.getLocationOnScreen(screen);owner[0].getLocationOnScreen(origin);target[0]=new Rect(screen[0],screen[1],screen[0]+value.getWidth(),screen[1]+value.getHeight());});
        assertTrue("One screen-coordinate input to the measured owned dialog button",device.click(target[0].centerX(),target[0].centerY()));
        File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());try(PrintWriter out=new PrintWriter(new FileOutputStream(new File(folder,"room-conditions-dialog-input.txt"),true))){out.println(label+" root-visible="+visible[0]+" root-screen-origin="+origin[0]+","+origin[1]+" screen-target="+target[0]);}
    }
    private void captureDialog(String literal)throws Exception{awaitDialog();View[] owner={null};i.runOnMainSync(()->{owner[0]=focused();assertNotNull(owner[0]);assertTrue(text(owner[0],"Microphone for room sound"));});CountDownLatch frame=new CountDownLatch(1);i.runOnMainSync(()->{owner[0].getViewTreeObserver().registerFrameCommitCallback(frame::countDown);owner[0].invalidate();});assertTrue(frame.await(5,TimeUnit.SECONDS));File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());assertTrue(device.takeScreenshot(new File(folder,literal+".png")));}
    private boolean checked(String label){boolean[] value={false};i.runOnMainSync(()->value[0]=((Switch)control(activity.getWindow().getDecorView(),label)).isChecked());return value[0];}
    private void run()throws Exception{
        assertFalse(AlarmService.active);assertFalse(TrackingService.running);assertFalse(MicrophoneLease.occupied());
        Map<String,?> original=new HashMap<>(Prefs.of(c).getAll());boolean permission=c.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)==android.content.pm.PackageManager.PERMISSION_GRANTED;
        RoomLightPort.Factory oldLights=RoomConditionsActivity.lights;RoomSoundLevel.Factory oldAudio=RoomConditionsActivity.microphones;RoomConditionsActivity.Access oldAccess=RoomConditionsActivity.access;
        Application app=(Application)c.getApplicationContext();Application.ActivityLifecycleCallbacks lifecycle=new Application.ActivityLifecycleCallbacks(){public void onActivityCreated(Activity a,Bundle state){if(a instanceof RoomConditionsActivity){owned.add(a);assertTrue(a.getIntent().getBooleanExtra("visualReview",false));}}public void onActivityStarted(Activity a){}public void onActivityResumed(Activity a){}public void onActivityPaused(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,Bundle state){}public void onActivityDestroyed(Activity a){}};app.registerActivityLifecycleCallbacks(lifecycle);
        try{
            assertTrue(Prefs.of(c).edit().putBoolean(RoomDimming.ENABLED,false).commit());
            i.runOnMainSync(()->{RoomConditionsActivity.lights=context->{Light value=new Light(!unavailable.get());lightPorts.add(value);return value;};RoomConditionsActivity.access=new RoomConditionsActivity.Access(){public boolean microphone(Context context){return !rationaleNeeded.get();}public boolean rationale(Activity owner){return rationaleNeeded.get();}public void request(Activity owner,ActivityResultLauncher<String> launcher){assertTrue("Request only after the rationale has dismissed and Activity focus returned",owner.hasWindowFocus());assertFalse(owner.isFinishing());permissionRequests.incrementAndGet();}};RoomConditionsActivity.microphones=(context,current)->{opens.incrementAndGet();if(failOpen.get())throw new IllegalStateException("Injected microphone unavailable");return new Capture(current);};});
            launch();await(()->lightPorts.get(lightPorts.size()-1).sink!=null);i.runOnMainSync(()->lightPorts.get(lightPorts.size()-1).emit(0));await(()->shown("0.0 lx"));Thread.sleep(1100);i.runOnMainSync(()->lightPorts.get(lightPorts.size()-1).emit(2));await(()->shown("2.0 lx"));assertFalse(checked("Microphone analysis"));assertEquals(0,opens.get());assertNotNull(bounds("Microphone analysis",false));assertNotNull(bounds("Screen dimming",false));capture("room-conditions-default");
            rationaleNeeded.set(true);click("Microphone analysis");dialogClick("Not now");ready();assertEquals(0,permissionRequests.get());assertEquals(0,opens.get());click("Microphone analysis");awaitDialog();device.pressBack();ready();assertEquals(0,permissionRequests.get());click("Microphone analysis");awaitDialog();recreate();assertEquals(0,permissionRequests.get());assertEquals(0,opens.get());click("Microphone analysis");captureDialog("room-conditions-microphone-access");dialogClick("Continue");await(()->permissionRequests.get()==1);ready();assertFalse(checked("Microphone analysis"));assertEquals(0,opens.get());assertEquals(permission,c.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)==android.content.pm.PackageManager.PERMISSION_GRANTED);finish();rationaleNeeded.set(false);launch();
            click("Microphone analysis");await(()->shown("25%"));assertTrue(checked("Microphone analysis"));assertEquals(1,opens.get());assertTrue(MicrophoneLease.occupied());capture("room-conditions-measuring");
            recreate();assertEquals(1,closes.get());assertEquals(1,opens.get());assertFalse(checked("Microphone analysis"));assertTrue(shown("Not measuring"));capture("room-conditions-recreated");
            click("Screen dimming");assertFalse(RoomDimming.enabled(c));recreate();assertTrue(checked("Screen dimming"));assertFalse(RoomDimming.enabled(c));float[] originalBrightness={0};RoomConditionsActivity dimmed=activity;i.runOnMainSync(()->originalBrightness[0]=dimmed.getWindow().getAttributes().screenBrightness);click("Use bedtime dimming");await(()->RoomDimming.enabled(c));i.runOnMainSync(()->assertTrue(activity.getWindow().getAttributes().screenBrightness<=.05f));capture("room-conditions-dimming-saved");finish();i.runOnMainSync(()->assertEquals("Exact window brightness restored on leaving",originalBrightness[0],dimmed.getWindow().getAttributes().screenBrightness,0));
            launch();assertTrue(checked("Screen dimming"));assertFalse(checked("Microphone analysis"));assertEquals(1,opens.get());capture("room-conditions-reopened");
            failOpen.set(true);click("Microphone analysis");await(()->shown("Microphone unavailable or interrupted. Tap to try again."));assertFalse(checked("Microphone analysis"));assertFalse(MicrophoneLease.occupied());capture("room-conditions-microphone-error");finish();failOpen.set(false);
            unavailable.set(true);launch();await(()->shown("Light sensor unavailable on this device."));assertTrue(shown("Not measuring"));capture("room-conditions-unavailable");
            failClose.set(true);click("Microphone analysis");await(()->shown("25%"));click("Microphone analysis");await(()->RoomSoundLevel.current()!=null&&RoomSoundLevel.current().state==RoomSoundLevel.State.BLOCKED);assertTrue(MicrophoneLease.occupied());capture("room-conditions-cleanup-blocked");failClose.set(false);click("Retry microphone cleanup");await(()->RoomSoundLevel.current()==null);assertFalse(MicrophoneLease.occupied());assertFalse(checked("Microphone analysis"));capture("room-conditions-cleaned");finish();
            assertEquals(permission,c.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)==android.content.pm.PackageManager.PERMISSION_GRANTED);
        }catch(Exception|AssertionError failure){try{File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());device.takeScreenshot(new File(folder,"room-conditions-failure.png"));device.dumpWindowHierarchy(new File(folder,"room-conditions-failure.xml"));try(PrintWriter out=new PrintWriter(new File(folder,"room-conditions-failure.txt"),"UTF-8")){out.println(step);RoomSoundLevel pending=RoomSoundLevel.current();out.println("Room microphone: "+(pending==null?"closed":pending.state)+" / fake permission requests "+permissionRequests.get()+" / fake opens "+opens.get()+" / fake closes "+closes.get()+" / owned lease "+MicrophoneLease.occupied());failure.printStackTrace(out);}}catch(Exception capture){failure.addSuppressed(capture);}throw failure;
        }finally{
            try{failClose.set(false);i.runOnMainSync(()->{for(Activity a:owned)if(!a.isDestroyed())a.finish();});RoomSoundLevel pending=RoomSoundLevel.current();if(pending!=null)pending.cancel();await(()->owned.stream().allMatch(Activity::isDestroyed));await(()->{RoomSoundLevel remaining=RoomSoundLevel.current();if(remaining!=null&&remaining.state==RoomSoundLevel.State.BLOCKED)remaining.retryCleanup();return remaining==null;});assertFalse(MicrophoneLease.occupied());}
            finally{app.unregisterActivityLifecycleCallbacks(lifecycle);i.runOnMainSync(()->{RoomConditionsActivity.lights=oldLights;RoomConditionsActivity.microphones=oldAudio;RoomConditionsActivity.access=oldAccess;});SharedPreferences.Editor restore=Prefs.of(c).edit();for(String key:new String[]{RoomDimming.ENABLED,"roomMicrophoneAsked"}){if(original.containsKey(key))restore.putBoolean(key,(Boolean)original.get(key));else restore.remove(key);}assertTrue(restore.commit());assertEquals(original,Prefs.of(c).getAll());}
        }
    }
}
