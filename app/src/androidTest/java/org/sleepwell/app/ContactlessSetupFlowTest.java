package org.sleepwell.app;
import android.app.*;import android.content.*;import android.os.*;import android.view.*;import android.widget.*;import androidx.test.platform.app.InstrumentationRegistry;import androidx.test.uiautomator.*;import java.io.*;import java.util.*;import java.util.concurrent.*;import java.util.concurrent.atomic.*;import org.junit.Test;import static org.junit.Assert.*;
/** Isolated native configuration with fake duplex PCM. Never instantiates AudioTrack/AudioRecord. */
public final class ContactlessSetupFlowTest {
 private final Instrumentation i=InstrumentationRegistry.getInstrumentation();private final Context c=i.getTargetContext();private final UiDevice d=UiDevice.getInstance(i);private ContactlessSetupActivity activity;private final List<Activity> owned=new CopyOnWriteArrayList<>();private final List<SonarRuntime> runtimes=new CopyOnWriteArrayList<>();private final AtomicInteger opens=new AtomicInteger();private final AtomicReference<SonarCapabilities> capability=new AtomicReference<>(new SonarCapabilities(true,true,"","Injected hardware hints; no real audio"));private volatile boolean hold;private String step="fixture admission";
 @org.junit.Rule public final org.junit.rules.TestName testName=new org.junit.rules.TestName();
 private static final class Clock implements SonarPump.Clock {long n;public long millis(){return n;}public void idle()throws InterruptedException{if(Thread.interrupted())throw new InterruptedException();n+=10;}}
 private static final class Fake implements SonarPump.Io {long written,read,origin=-1;boolean closed;public void startMuted(){}public void check(){}public boolean routed(){return true;}public void confirmRoutes(){}public SonarPump.Timing timing(){return new SonarPump.Timing(0);}public void gain(float value){assertTrue(value<=.10f);origin=written;}public int write(short[] p,int offset,int count){int n=Math.min(480,count);written+=n;return n;}public int read(short[] p,int offset,int count){int n=(int)Math.min(Math.min(480,count),written-read);for(int k=0;k<n;k++){long t=read+k-origin;int frame=(int)(t/1920);double delay=frame>=60&&frame<400?240+5*Math.sin(frame*.3):240;p[offset+k]=(short)Math.round(32767*(origin<0?0:.5*SonarChirp.sample(t-8)+.16*SonarChirp.sample(t-delay)));}read+=n;return n;}public void close(){closed=true;}}
 private interface Checked {void run(SharedPreferences prefs)throws Exception;}
 private void fixture(Checked task)throws Exception {assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertEquals("1",d.executeShellCommand("getprop ro.kernel.qemu").trim());assertFalse(AlarmService.active);assertFalse(TrackingService.running);assertFalse(Prefs.flag(c,"ringing"));Map<String,?> before=new HashMap<>(Prefs.of(c).getAll());String name="sonar-fixture-"+UUID.randomUUID();SharedPreferences prefs=c.getSharedPreferences(name,0);SonarSettings.Factory oldSettings=SonarSettings.factory;SonarCapabilities.Provider oldCaps=SonarCapabilities.provider;ContactlessSetupActivity.Ports oldPorts=ContactlessSetupActivity.ports;Application app=(Application)c.getApplicationContext();Application.ActivityLifecycleCallbacks lifecycle=new Application.ActivityLifecycleCallbacks(){public void onActivityCreated(Activity a,Bundle b){if(a instanceof ContactlessSetupActivity)owned.add(a);}public void onActivityStarted(Activity a){}public void onActivityResumed(Activity a){}public void onActivityPaused(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,Bundle b){}public void onActivityDestroyed(Activity a){}};app.registerActivityLifecycleCallbacks(lifecycle);
  try{i.runOnMainSync(()->{SonarSettings.factory=ctx->prefs;SonarCapabilities.provider=ctx->capability.get();ContactlessSetupActivity.ports=(ctx,main,observer)->{SonarRuntime r=new SonarRuntime((config,cancelled)->{opens.incrementAndGet();if(hold){while(!cancelled.get())Thread.sleep(20);throw new InterruptedException();}return new Fake();},main,observer,new Clock());runtimes.add(r);return r;};});task.run(prefs);}catch(Exception|AssertionError failure){try{File f=folder();try(PrintWriter out=new PrintWriter(new File(f,"sonar-setup-"+testName.getMethodName()+"-failure.txt"),"UTF-8")){out.println("Last await/action: "+step);for(SonarRuntime runtime:runtimes){SonarRuntime.State state=runtime.state();out.println("Runtime phase="+state.phase+" busy="+state.busy+" ready="+runtime.ready()+" message="+state.message+" retainedFrames="+runtime.snapshot().size()+" lastQuality="+(state.frame==null?"none":state.frame.quality));}failure.printStackTrace(out);}d.takeScreenshot(new File(f,"sonar-setup-"+testName.getMethodName()+"-failure.png"));d.dumpWindowHierarchy(new File(f,"sonar-setup-"+testName.getMethodName()+"-failure.xml"));d.takeScreenshot(new File(f,"sonar-setup-failure.png"));d.dumpWindowHierarchy(new File(f,"sonar-setup-failure.xml"));}catch(Exception e){failure.addSuppressed(e);}throw failure;}finally{try{i.runOnMainSync(()->{for(Activity a:owned)if(!a.isDestroyed())a.finish();});for(SonarRuntime runtime:runtimes){runtime.cancel();assertTrue(runtime.awaitClosed(6000));}PlanOperations.drain(10000);waitFor(()->owned.stream().allMatch(Activity::isDestroyed));assertEquals(before,Prefs.of(c).getAll());assertFalse(AlarmService.active);assertFalse(TrackingService.running);}finally{app.unregisterActivityLifecycleCallbacks(lifecycle);i.runOnMainSync(()->{SonarSettings.factory=oldSettings;SonarCapabilities.provider=oldCaps;ContactlessSetupActivity.ports=oldPorts;});if(owned.stream().allMatch(Activity::isDestroyed))c.deleteSharedPreferences(name);}}}
 private void waitFor(java.util.function.BooleanSupplier check)throws Exception{long end=SystemClock.elapsedRealtime()+8000;while(!check.getAsBoolean()&&SystemClock.elapsedRealtime()<end)SystemClock.sleep(30);assertTrue(step+": expected asynchronous state",check.getAsBoolean());}
 /** Wait for the actual bounded fake-PCM worker, then independently assert its policy and rendered state. */
 private void qualified(String label)throws Exception{
  step=label+": full PCM worker completion";
  assertFalse("Test created an owned runtime",runtimes.isEmpty());
  SonarRuntime owner=runtimes.get(runtimes.size()-1);
  // CPU allowance on a shared emulator; the production 20s virtual signal and quality gates are unchanged.
  assertTrue(step,owner.awaitClosed(30000));
  SonarRuntime.State terminal=owner.state();
  assertEquals(label+": terminal phase / "+terminal.message,"DONE",terminal.phase);
  assertTrue(label+": unchanged echo policy / "+terminal.message,owner.ready());
  assertNotNull(label+": measured final frame",terminal.frame);
  assertEquals(label+": actual valid echo",SonarFrame.Quality.VALID,terminal.frame.quality);
  assertNotNull(label+": measured delta rather than missing evidence",terminal.frame.movement);
  step=label+": focused qualified UI";
  ready();
  waitFor(()->owner.ready()&&enabled("Use sonar"));
 }
 private void launch()throws Exception{ActivityMonitorAction(()->c.startActivity(new Intent(c,ContactlessSetupActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));ready();}
 private void recreate()throws Exception{ActivityMonitorAction(()->activity.recreate());ready();}
 private void ActivityMonitorAction(Runnable action)throws Exception{Instrumentation.ActivityMonitor monitor=i.addMonitor(ContactlessSetupActivity.class.getName(),null,false);try{i.runOnMainSync(action);activity=(ContactlessSetupActivity)monitor.waitForActivityWithTimeout(5000);assertNotNull(activity);}finally{i.removeMonitor(monitor);}}
 private void ready()throws Exception{waitFor(()->{boolean[] ready={false};i.runOnMainSync(()->ready[0]=activity.hasWindowFocus()&&!activity.isDestroyed()&&activity.getWindow().getDecorView().isLaidOut()&&(activity.getWindow().getInsetsController().getSystemBarsAppearance()&(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS))==0);return ready[0];});}
 private View find(View root,String text){if(root instanceof TextView&&text.contentEquals(((TextView)root).getText())||text.equals(root.getContentDescription()))return root;if(root instanceof ViewGroup)for(int n=0;n<((ViewGroup)root).getChildCount();n++){View v=find(((ViewGroup)root).getChildAt(n),text);if(v!=null)return v;}return null;}
 private void click(String title)throws Exception{step="click "+title;ready();i.runOnMainSync(()->{View v=find(activity.getWindow().getDecorView(),title);assertNotNull(title,v);assertTrue(title+" enabled",v.isEnabled());android.graphics.Rect bounds=new android.graphics.Rect();assertTrue(v.getGlobalVisibleRect(bounds));assertEquals(v.getHeight(),bounds.height());assertTrue(bounds.height()>=Design.dp(c,48));assertTrue(v.performClick());});}
 private boolean enabled(String title){boolean[] value={false};i.runOnMainSync(()->{View v=find(activity.getWindow().getDecorView(),title);value[0]=v!=null&&v.isEnabled();});return value[0];}
 private File folder(){File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());return folder;}
 private boolean brightRegion(android.graphics.Bitmap bitmap,int top,int bottom){int[] row=new int[bitmap.getWidth()];long bright=0,plum=0,total=(long)row.length*(bottom-top);int background=Design.PLUM;for(int y=top;y<bottom;y++){bitmap.getPixels(row,0,row.length,0,y,row.length,1);for(int rgb:row){if(android.graphics.Color.red(rgb)>180&&android.graphics.Color.green(rgb)>180&&android.graphics.Color.blue(rgb)>180)bright++;if(Math.abs(android.graphics.Color.red(rgb)-android.graphics.Color.red(background))<=12&&Math.abs(android.graphics.Color.green(rgb)-android.graphics.Color.green(background))<=12&&Math.abs(android.graphics.Color.blue(rgb)-android.graphics.Color.blue(background))<=12)plum++;}}return bright>=24&&plum*100>=total*60;}
 private boolean brightBars(android.graphics.Bitmap bitmap,int status,int navigation){return bitmap!=null&&status>0&&navigation>0&&status+navigation<bitmap.getHeight()&&brightRegion(bitmap,0,status)&&brightRegion(bitmap,bitmap.getHeight()-navigation,bitmap.getHeight());}
 private void capture(String name)throws Exception{
  step="capture "+name;
  ready();
  CountDownLatch frame=new CountDownLatch(1);
  int[] bars=new int[2];
  i.runOnMainSync(()->{
   View root=activity.getWindow().getDecorView();
   View use=find(root,"Use sonar");
   if(use!=null){android.graphics.Rect r=new android.graphics.Rect();assertTrue(use.getGlobalVisibleRect(r));assertEquals(use.getHeight(),r.height());}
   WindowInsets insets=root.getRootWindowInsets();
   assertNotNull("Current system bar insets",insets);
   bars[0]=insets.getInsets(WindowInsets.Type.statusBars()).top;
   bars[1]=insets.getInsets(WindowInsets.Type.navigationBars()).bottom;
   root.getViewTreeObserver().registerFrameCommitCallback(frame::countDown);
   root.invalidate();
  });
  assertTrue(frame.await(5,TimeUnit.SECONDS));
  // Check the compositor output, not only the requested appearance flags. No UI action is retried.
  assertTrue("Light status icons and navigation handle rendered on plum",d.wait(new Condition<UiDevice,Boolean>(){
   public Boolean apply(UiDevice ignored){android.graphics.Bitmap screen=i.getUiAutomation().takeScreenshot();if(screen==null)return false;android.graphics.Bitmap readable=screen;try{if(screen.getConfig()==android.graphics.Bitmap.Config.HARDWARE)readable=screen.copy(android.graphics.Bitmap.Config.ARGB_8888,false);return brightBars(readable,bars[0],bars[1]);}finally{if(readable!=null&&readable!=screen)readable.recycle();screen.recycle();}}
  },5000));
  File file=new File(folder(),"sonar-setup-"+name+".png");
  assertTrue(d.takeScreenshot(file));
  android.graphics.Bitmap actual=android.graphics.BitmapFactory.decodeFile(file.getAbsolutePath());
  try{assertTrue("Saved original contains light system bars",brightBars(actual,bars[0],bars[1]));}finally{if(actual!=null)actual.recycle();}
 }
 @Test public void explicitRealDspFakePcmQualificationSaveAndReopenNoAutostart()throws Exception{
  fixture(prefs->{
   launch();
   assertEquals(0,opens.get());
   assertFalse(enabled("Use sonar"));
   capture("initial");
   click("Test signal");
   qualified("initial probe");
   assertEquals(1,opens.get());
   capture("ready");
   recreate();
   assertFalse(enabled("Use sonar"));
   assertEquals(1,opens.get());
   click("Test signal");
   qualified("probe after recreation");
   step="change probe gain to 7 percent";
   i.runOnMainSync(()->{
    SeekBar slider=(SeekBar)find(activity.getWindow().getDecorView(),"Contactless probe gain");
    assertNotNull(slider);
    Bundle args=new Bundle();
    args.putFloat(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_PROGRESS_VALUE,6);
    assertTrue(slider.performAccessibilityAction(android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS.getId(),args));
   });
   assertFalse(enabled("Use sonar"));
   capture("gain-changed");
   click("Test signal");
   qualified("probe at changed gain");
   capture("retested");
   click("Use sonar");
   PlanOperations.drain(10000);
   step="saved activity closes";
   waitFor(()->activity.isDestroyed());
   assertEquals(.07f,SonarSettings.read(prefs).gain,.000001);
   assertEquals(Set.of("profile"),prefs.getAll().keySet());
   launch();
   assertEquals(3,opens.get());
   assertFalse(enabled("Use sonar"));
   capture("reopened");
   recreate();
   assertEquals(3,opens.get());
   assertFalse(enabled("Use sonar"));
   capture("recreated");
  });
 }
 @Test public void cancelRotationAndPrerequisiteErrorsNeverOpenNativeAudio()throws Exception{
  fixture(prefs->{
   hold=true;
   launch();
   click("Test signal");
   step="first held runtime opened";
   waitFor(()->opens.get()==1);
   capture("testing");
   click("Stop test");
   step="cancelled runtime closes";
   waitFor(()->!runtimes.get(0).state().busy);
   assertFalse(enabled("Use sonar"));
   capture("cancelled");
   click("Test signal");
   step="second held runtime opened";
   waitFor(()->opens.get()==2);
   recreate();
   assertTrue(runtimes.get(1).awaitClosed(6000));
   assertEquals(2,opens.get());
   assertFalse(enabled("Use sonar"));
   capture("cancel-recreated");
   capability.set(new SonarCapabilities(true,false,"Connect a charger before testing contactless sensing.","Unknown hardware"));
   recreate();
   click("Test signal");
   assertEquals(2,opens.get());
   capture("charging-error");
   capability.set(new SonarCapabilities(true,true,"This phone reports no near-ultrasound microphone or speaker support.","Unsupported"));
   recreate();
   click("Test signal");
   assertEquals(2,opens.get());
   capture("unsupported");
   capability.set(new SonarCapabilities(false,true,"","Unknown hardware"));
   recreate();
   assertFalse(enabled("Use sonar"));
   capture("permission");
   assertTrue(prefs.getAll().isEmpty());
  });
 }
 private SharedPreferences delayedFailure(SharedPreferences target,CountDownLatch entered,CountDownLatch release){return (SharedPreferences)java.lang.reflect.Proxy.newProxyInstance(SharedPreferences.class.getClassLoader(),new Class<?>[]{SharedPreferences.class},(proxy,method,args)->{try{Object result=method.invoke(target,args);if(!method.getName().equals("edit"))return result;SharedPreferences.Editor editor=(SharedPreferences.Editor)result;return java.lang.reflect.Proxy.newProxyInstance(SharedPreferences.Editor.class.getClassLoader(),new Class<?>[]{SharedPreferences.Editor.class},(owned,call,values)->{if(call.getName().equals("commit")){entered.countDown();if(!release.await(30,TimeUnit.SECONDS))throw new IllegalStateException("Controlled save fixture was not released.");return false;}try{Object value=call.invoke(editor,values);return value==editor?owned:value;}catch(java.lang.reflect.InvocationTargetException failure){throw failure.getCause();}});}catch(java.lang.reflect.InvocationTargetException failure){throw failure.getCause();}});}
 @Test public void retainedBusySaveAndStorageFailureNeverReplayCapture()throws Exception{
  fixture(prefs->{
   CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1);
   SharedPreferences blocked=delayedFailure(prefs,entered,release);
   i.runOnMainSync(()->SonarSettings.factory=ctx->blocked);
   try{
    launch();
    click("Test signal");
    qualified("probe before controlled save");
    click("Use sonar");
    step="controlled commit entered";
    assertTrue(entered.await(5,TimeUnit.SECONDS));
    assertFalse(enabled("Saving…"));
    capture("saving");
    recreate();
    assertFalse(enabled("Saving…"));
    assertEquals(1,opens.get());
    capture("saving-recreated");
    release.countDown();
    PlanOperations.drain(10000);
    step="failed commit shown";
    assertTrue(d.wait(Until.hasObject(By.text("Contactless settings could not be saved.")),5000));
    assertFalse(enabled("Use sonar"));
    assertTrue(prefs.getAll().isEmpty());
    capture("save-error");
    assertEquals(1,opens.get());
   }finally{release.countDown();}
  });
 }
}
