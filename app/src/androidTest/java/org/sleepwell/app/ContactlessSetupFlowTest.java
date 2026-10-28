package org.sleepwell.app;
import android.app.*;import android.content.*;import android.os.*;import android.view.*;import android.widget.*;import androidx.test.platform.app.InstrumentationRegistry;import androidx.test.uiautomator.*;import java.io.*;import java.util.*;import java.util.concurrent.*;import java.util.concurrent.atomic.*;import org.junit.Test;import static org.junit.Assert.*;
/** Isolated native configuration with fake duplex PCM. Never instantiates AudioTrack/AudioRecord. */
public final class ContactlessSetupFlowTest {
 private final Instrumentation i=InstrumentationRegistry.getInstrumentation();private final Context c=i.getTargetContext();private final UiDevice d=UiDevice.getInstance(i);private ContactlessSetupActivity activity;private final List<Activity> owned=new CopyOnWriteArrayList<>();private final List<SonarRuntime> runtimes=new CopyOnWriteArrayList<>();private final AtomicInteger opens=new AtomicInteger();private final AtomicReference<SonarCapabilities> capability=new AtomicReference<>(new SonarCapabilities(true,true,"","Injected hardware hints; no real audio",false));private final AtomicInteger permissionRequests=new AtomicInteger(),settingsVisits=new AtomicInteger(),microphoneSettings=new AtomicInteger(),soundSettings=new AtomicInteger();private final List<RouteWatch> routeWatches=new CopyOnWriteArrayList<>();
 private static final class RouteWatch implements ContactlessSetupAccess.Watch {final Runnable changed;volatile boolean closed;RouteWatch(Runnable changed){this.changed=changed;}public void close(){closed=true;}}
 private final ContactlessSetupAccess fakeAccess=new ContactlessSetupAccess(){public boolean rationale(Activity owner){return false;}public void request(Activity owner){permissionRequests.incrementAndGet();}public void settings(Activity owner,boolean microphone){assertTrue(owner instanceof ContactlessSetupActivity);settingsVisits.incrementAndGet();(microphone?microphoneSettings:soundSettings).incrementAndGet();owner.startActivity(new Intent(owner,ContactlessSetupActivity.class).putExtra("visualReview",true));}public ContactlessSetupAccess.Watch watch(Context context,Handler main,Runnable changed){RouteWatch watch=new RouteWatch(changed);routeWatches.add(watch);return watch;}};
 private volatile boolean hold;private String step="fixture admission";
 @org.junit.Rule public final org.junit.rules.TestName testName=new org.junit.rules.TestName();
 private static final class Clock implements SonarPump.Clock {long n;public long millis(){return n;}public void idle()throws InterruptedException{if(Thread.interrupted())throw new InterruptedException();n+=10;}}
 private static final class Fake implements SonarPump.Io {long written,read,origin=-1;boolean closed;public void startMuted(){}public void check(){}public boolean routed(){return true;}public void confirmRoutes(){}public SonarPump.Timing timing(){return new SonarPump.Timing(0);}public void gain(float value){assertTrue(value<=.10f);origin=written;}public int write(short[] p,int offset,int count){int n=Math.min(480,count);written+=n;return n;}public int read(short[] p,int offset,int count){int n=(int)Math.min(Math.min(480,count),written-read);for(int k=0;k<n;k++){long t=read+k-origin;int frame=(int)(t/1920);double delay=frame>=60&&frame<400?240+5*Math.sin(frame*.3):240;p[offset+k]=(short)Math.round(32767*(origin<0?0:.5*SonarChirp.sample(t-8)+.16*SonarChirp.sample(t-delay)));}read+=n;return n;}public void close(){closed=true;}}
 private interface Checked {void run(SharedPreferences prefs)throws Exception;}
 private void fixture(Checked task)throws Exception {assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertEquals("1",d.executeShellCommand("getprop ro.kernel.qemu").trim());assertFalse(AlarmService.active);assertFalse(TrackingService.running);assertFalse(Prefs.flag(c,"ringing"));Map<String,?> before=new HashMap<>(Prefs.of(c).getAll());String name="sonar-fixture-"+UUID.randomUUID();SharedPreferences prefs=c.getSharedPreferences(name,0);SonarSettings.Factory oldSettings=SonarSettings.factory;SonarCapabilities.Provider oldCaps=SonarCapabilities.provider;ContactlessSetupActivity.Ports oldPorts=ContactlessSetupActivity.ports;ContactlessSetupAccess oldAccess=ContactlessSetupActivity.access;Application app=(Application)c.getApplicationContext();Application.ActivityLifecycleCallbacks lifecycle=new Application.ActivityLifecycleCallbacks(){public void onActivityCreated(Activity a,Bundle b){if(a instanceof ContactlessSetupActivity)owned.add(a);}public void onActivityStarted(Activity a){}public void onActivityResumed(Activity a){}public void onActivityPaused(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,Bundle b){}public void onActivityDestroyed(Activity a){}};app.registerActivityLifecycleCallbacks(lifecycle);
  try{i.runOnMainSync(()->{ContactlessSetupActivity.access=fakeAccess;SonarSettings.factory=ctx->prefs;SonarCapabilities.provider=ctx->capability.get();ContactlessSetupActivity.ports=(ctx,main,observer)->{SonarRuntime r=new SonarRuntime((config,cancelled)->{opens.incrementAndGet();if(hold){while(!cancelled.get())Thread.sleep(20);throw new InterruptedException();}return new Fake();},main,observer,new Clock());runtimes.add(r);return r;};});task.run(prefs);}catch(Exception|AssertionError failure){try{File f=folder();try(PrintWriter out=new PrintWriter(new File(f,"sonar-setup-"+testName.getMethodName()+"-failure.txt"),"UTF-8")){out.println("Last await/action: "+step);for(SonarRuntime runtime:runtimes){SonarRuntime.State state=runtime.state();out.println("Runtime phase="+state.phase+" busy="+state.busy+" ready="+runtime.ready()+" message="+state.message+" retainedFrames="+runtime.snapshot().size()+" lastQuality="+(state.frame==null?"none":state.frame.quality));}failure.printStackTrace(out);}d.takeScreenshot(new File(f,"sonar-setup-"+testName.getMethodName()+"-failure.png"));d.dumpWindowHierarchy(new File(f,"sonar-setup-"+testName.getMethodName()+"-failure.xml"));d.takeScreenshot(new File(f,"sonar-setup-failure.png"));d.dumpWindowHierarchy(new File(f,"sonar-setup-failure.xml"));}catch(Exception e){failure.addSuppressed(e);}throw failure;}finally{try{i.runOnMainSync(()->{for(Activity a:owned)if(!a.isDestroyed())a.finish();});for(SonarRuntime runtime:runtimes){runtime.cancel();assertTrue(runtime.awaitClosed(6000));}PlanOperations.drain(10000);waitFor(()->owned.stream().allMatch(Activity::isDestroyed));assertTrue("Every private route watcher closed before restoring factories",routeWatches.stream().allMatch(w->w.closed));assertEquals(before,Prefs.of(c).getAll());assertFalse(AlarmService.active);assertFalse(TrackingService.running);}finally{app.unregisterActivityLifecycleCallbacks(lifecycle);i.runOnMainSync(()->{SonarSettings.factory=oldSettings;SonarCapabilities.provider=oldCaps;ContactlessSetupActivity.ports=oldPorts;ContactlessSetupActivity.access=oldAccess;});if(owned.stream().allMatch(Activity::isDestroyed))c.deleteSharedPreferences(name);}}}
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
 private void route(SonarCapabilities next){i.runOnMainSync(()->{capability.set(next);List<RouteWatch> live=routeWatches.stream().filter(w->!w.closed).toList();assertEquals("Exactly one resumed setup device watcher",1,live.size());live.get(0).changed.run();});}
 private void status(String title,String state){i.runOnMainSync(()->{View row=find(activity.getWindow().getDecorView(),title+" · "+state);assertNotNull(title+" truthful status",row);android.graphics.Rect area=new android.graphics.Rect();assertTrue(title+" full row",row.getGlobalVisibleRect(area));assertEquals(row.getWidth(),area.width());assertEquals(row.getHeight(),area.height());assertTrue(area.width()>=Design.dp(c,48));assertTrue(area.height()>=Design.dp(c,48));assertTrue(title+" keyboard focus",row.isFocusable());assertEquals(android.widget.Button.class.getName(),row.createAccessibilityNodeInfo().getClassName());});}
 private void settingsReturn(String row,SonarCapabilities after)throws Exception{ContactlessSetupActivity parent=activity;Instrumentation.ActivityMonitor monitor=i.addMonitor(ContactlessSetupActivity.class.getName(),null,false);ContactlessSetupActivity child;int before=opens.get();try{click(row);child=(ContactlessSetupActivity)monitor.waitForActivityWithTimeout(8000);assertNotNull("Owned inert settings surrogate",child);assertNotSame(parent,child);}finally{i.removeMonitor(monitor);}step="actual parent paused for inert settings surrogate";waitFor(()->{boolean[] paused={false};i.runOnMainSync(()->paused[0]=child.hasWindowFocus()&&!parent.hasWindowFocus()&&routeWatches.stream().filter(w->!w.closed).count()==1);return paused[0];});assertEquals(before,opens.get());i.runOnMainSync(()->{capability.set(after);child.finish();});waitFor(child::isDestroyed);ready();assertSame(parent,activity);assertFalse(enabled("Use sonar"));assertEquals(before,opens.get());}
 private File folder(){File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());return folder;}
 private boolean brightRegion(android.graphics.Bitmap bitmap,int top,int bottom){int[] row=new int[bitmap.getWidth()];long bright=0,plum=0,total=(long)row.length*(bottom-top);int background=Design.PLUM;for(int y=top;y<bottom;y++){bitmap.getPixels(row,0,row.length,0,y,row.length,1);for(int rgb:row){if(android.graphics.Color.red(rgb)>180&&android.graphics.Color.green(rgb)>180&&android.graphics.Color.blue(rgb)>180)bright++;if(Math.abs(android.graphics.Color.red(rgb)-android.graphics.Color.red(background))<=12&&Math.abs(android.graphics.Color.green(rgb)-android.graphics.Color.green(background))<=12&&Math.abs(android.graphics.Color.blue(rgb)-android.graphics.Color.blue(background))<=12)plum++;}}return bright>=24&&plum*100>=total*60;}
 private boolean brightBars(android.graphics.Bitmap bitmap,int status,int navigation){return bitmap!=null&&status>0&&navigation>0&&status+navigation<bitmap.getHeight()&&brightRegion(bitmap,0,status)&&brightRegion(bitmap,bitmap.getHeight()-navigation,bitmap.getHeight());}
 private static final class GlyphRegion {final String name;final android.graphics.Rect bounds;GlyphRegion(String name,android.graphics.Rect bounds){this.name=name;this.bounds=new android.graphics.Rect(bounds);}}
 private List<GlyphRegion> statusGlyphs(int status){List<GlyphRegion> result=new ArrayList<>();for(String id:List.of("clock","wifi_signal","mobile_signal","battery")){List<UiObject2> values=d.findObjects(By.res("com.android.systemui",id));if(values.isEmpty())return List.of();for(int n=0;n<values.size();n++){android.graphics.Rect area=values.get(n).getVisibleBounds();if(area.isEmpty()||area.top<0||area.bottom>status)return List.of();result.add(new GlyphRegion(id+"["+n+"]",area));}}UiObject2 notifications=d.findObject(By.res("com.android.systemui","notificationIcons"));if(notifications!=null)for(UiObject2 icon:notifications.findObjects(By.clazz("android.widget.ImageView"))){android.graphics.Rect area=icon.getVisibleBounds();if(!area.isEmpty()&&area.top>=0&&area.bottom<=status)result.add(new GlyphRegion("notification["+result.size()+"]",area));}return result;}
 private static final class GlyphSample {final List<GlyphRegion> regions;final String stats;final boolean light;GlyphSample(android.graphics.Bitmap bitmap,List<GlyphRegion> regions){this.regions=List.copyOf(regions);StringBuilder report=new StringBuilder();boolean passed=!regions.isEmpty();for(GlyphRegion region:regions){android.graphics.Rect area=region.bounds;int bright=0,black=0;if(area.left<0||area.top<0||area.right>bitmap.getWidth()||area.bottom>bitmap.getHeight()){passed=false;continue;}int[] pixels=new int[area.width()*area.height()];bitmap.getPixels(pixels,0,area.width(),area.left,area.top,area.width(),area.height());for(int rgb:pixels){int r=android.graphics.Color.red(rgb),g=android.graphics.Color.green(rgb),b=android.graphics.Color.blue(rgb);if(r>180&&g>180&&b>180)bright++;if(r<=20&&g<=20&&b<=20)black++;}passed&=bright>=24&&bright>2*black;report.append(region.name).append(area.toShortString()).append(" light=").append(bright).append(" black=").append(black).append("; ");}light=passed;stats=report.toString();}}
 private boolean focusedOwner(Activity expected){boolean[] focused={false};i.runOnMainSync(()->focused[0]=activity==expected&&!expected.isFinishing()&&!expected.isDestroyed()&&expected.hasWindowFocus()&&expected.getWindow().getDecorView().isAttachedToWindow()&&expected.getWindow().getDecorView().isLaidOut()&&(expected.getWindow().getInsetsController().getSystemBarsAppearance()&(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS))==0);return focused[0];}
 private String windowState(){String[] value={"no owner"};i.runOnMainSync(()->{if(activity!=null)value[0]="owner="+System.identityHashCode(activity)+" focused="+activity.hasWindowFocus()+" destroyed="+activity.isDestroyed()+" finishing="+activity.isFinishing()+" appearance="+activity.getWindow().getInsetsController().getSystemBarsAppearance()+" legacyUi="+activity.getWindow().getDecorView().getSystemUiVisibility();});return value[0];}
 private void glyphReport(String message)throws IOException{try(Writer out=new OutputStreamWriter(new FileOutputStream(new File(folder(),"sonar-setup-glyphs-"+testName.getMethodName()+".txt"),true),java.nio.charset.StandardCharsets.UTF_8)){out.write(message+"\n");}}
 private void fullBounds(View view,String label,boolean target){assertNotNull(label,view);android.graphics.Rect area=new android.graphics.Rect();assertTrue(label+" fully visible",view.getGlobalVisibleRect(area));assertEquals(label+" width",view.getWidth(),area.width());assertEquals(label+" height",view.getHeight(),area.height());if(target){assertTrue(label+" 48dp width",area.width()>=Design.dp(c,48));assertTrue(label+" 48dp height",area.height()>=Design.dp(c,48));}}
 private void capture(String name)throws Exception{
  step="capture "+name;
  ready();
  CountDownLatch frame=new CountDownLatch(1);
  int[] bars=new int[2];
  i.runOnMainSync(()->{
   View root=activity.getWindow().getDecorView();
   View use=find(root,"Use sonar");if(use==null)use=find(root,"Saving…");fullBounds(use,"Full Use/Save footer",true);
   View test=find(root,"Test signal");if(test==null)test=find(root,"Stop test");if(test==null)test=find(root,"Microphone settings");fullBounds(test,"Full Test/Stop/access footer",true);
   for(String title:List.of("Headphones","Microphone")){View label=find(root,title);assertNotNull(title,label);fullBounds((View)label.getParent(),title+" status action",true);}
   fullBounds(find(root,"Back"),"Back",true);fullBounds(find(root,"Contactless options"),"Options",true);
   WindowInsets insets=root.getRootWindowInsets();
   assertNotNull("Current system bar insets",insets);
   bars[0]=insets.getInsets(WindowInsets.Type.statusBars()).top;
   bars[1]=insets.getInsets(WindowInsets.Type.navigationBars()).bottom;
   root.getViewTreeObserver().registerFrameCommitCallback(frame::countDown);
   root.invalidate();
  });
  assertTrue(frame.await(5,TimeUnit.SECONDS));
  // SystemUI updates its glyphs separately. Require each real glyph region on two compositor samples;
  // a white Wi-Fi icon cannot qualify a dark clock/mobile/battery. No action, flag or bad frame is changed.
  Activity expected=activity;AtomicReference<GlyphSample> last=new AtomicReference<>();int[] stable={0},samples={0};long began=SystemClock.elapsedRealtime();
  boolean settled=d.wait(new Condition<UiDevice,Boolean>(){
   public Boolean apply(UiDevice ignored){samples[0]++;if(!focusedOwner(expected)){stable[0]=0;return false;}List<GlyphRegion> regions;try{regions=statusGlyphs(bars[0]);}catch(StaleObjectException changed){stable[0]=0;return false;}android.graphics.Bitmap screen=i.getUiAutomation().takeScreenshot();if(screen==null){stable[0]=0;return false;}android.graphics.Bitmap readable=screen;try{if(screen.getConfig()==android.graphics.Bitmap.Config.HARDWARE)readable=screen.copy(android.graphics.Bitmap.Config.ARGB_8888,false);if(readable==null){stable[0]=0;return false;}GlyphSample sample=new GlyphSample(readable,regions);last.set(sample);boolean light=sample.light&&brightBars(readable,bars[0],bars[1])&&focusedOwner(expected);stable[0]=light?stable[0]+1:0;return stable[0]>=2;}finally{if(readable!=null&&readable!=screen)readable.recycle();screen.recycle();}}
  },8000);
  GlyphSample observed=last.get();glyphReport(name+" settled="+settled+" samples="+samples[0]+" elapsedMs="+(SystemClock.elapsedRealtime()-began)+" "+windowState()+" "+(observed==null?"no compositor glyph sample":observed.stats));
  assertTrue("Every clock/Wi-Fi/mobile/battery/visible-notification glyph must settle light on plum",settled);
  assertTrue("The same owner retains focused light-glyph flags",focusedOwner(expected));
  File file=new File(folder(),"sonar-setup-"+name+".png");assertTrue(d.takeScreenshot(file));
  android.graphics.Bitmap actual=android.graphics.BitmapFactory.decodeFile(file.getAbsolutePath());
  try{assertNotNull("Saved original decodes",actual);GlyphSample saved=new GlyphSample(actual,observed.regions);glyphReport(name+" saved "+saved.stats);assertTrue("Saved original contains each light status glyph and navigation handle: "+saved.stats,saved.light&&brightBars(actual,bars[0],bars[1]));}finally{if(actual!=null)actual.recycle();}

 }
 @Test public void explicitRealDspFakePcmQualificationSaveAndReopenNoAutostart()throws Exception{
  fixture(prefs->{
   launch();
   assertEquals(0,opens.get());
   assertFalse(enabled("Use sonar"));
   status("Headphones","Not connected");status("Microphone","Allowed");capture("initial");
   SonarCapabilities denied=new SonarCapabilities(false,true,"","Injected permission denied",false),allowed=new SonarCapabilities(true,true,"","Injected permission allowed",false);
   route(denied);status("Microphone","Not allowed");capture("access-denied");
   click("Microphone · Not allowed");assertEquals(1,permissionRequests.get());assertEquals(0,opens.get());assertFalse(enabled("Test signal"));status("Microphone","Requesting access…");capture("access-requesting");
   i.runOnMainSync(()->{View duplicate=find(activity.getWindow().getDecorView(),"Microphone · Requesting access…");assertNotNull(duplicate);duplicate.performClick();});assertEquals("Pending status input cannot repeat a request",1,permissionRequests.get());
   ContactlessSetupActivity oldRequest=activity;recreate();assertTrue(oldRequest.isDestroyed());assertEquals(1,permissionRequests.get());status("Microphone","Requesting access…");assertEquals(0,opens.get());i.runOnMainSync(()->{oldRequest.onRequestPermissionsResult(701,new String[]{android.Manifest.permission.RECORD_AUDIO},new int[]{android.content.pm.PackageManager.PERMISSION_GRANTED});activity.onRequestPermissionsResult(701,new String[]{android.Manifest.permission.RECORD_AUDIO},new int[]{android.content.pm.PackageManager.PERMISSION_DENIED});});status("Microphone","Not allowed");assertEquals(0,opens.get());
   settingsReturn("Microphone · Not allowed",allowed);status("Microphone","Allowed");status("Headphones","Not connected");assertEquals(1,settingsVisits.get());assertEquals(0,opens.get());capture("access-returned");
   settingsReturn("Microphone · Allowed",allowed);settingsReturn("Headphones · Not connected",allowed);assertEquals(3,settingsVisits.get());assertEquals(2,microphoneSettings.get());assertEquals(1,soundSettings.get());assertEquals(1,permissionRequests.get());assertEquals(0,opens.get());assertTrue(prefs.getAll().isEmpty());route(new SonarCapabilities(true,true,"","Injected route unavailable"));status("Headphones","Not checked");assertFalse(enabled("Use sonar"));assertEquals(0,opens.get());route(allowed);
   click("Test signal");
   qualified("initial probe");
   assertEquals(1,opens.get());
   status("Headphones","Not connected");status("Microphone","Allowed");capture("ready");
   route(new SonarCapabilities(true,true,"Disconnect headphones and external audio devices before testing.","Injected external output",true));status("Headphones","External audio connected");assertFalse(enabled("Use sonar"));capture("route-blocked");click("Test signal");assertEquals(1,opens.get());
   route(new SonarCapabilities(true,true,"","Injected built-in inventory",false));status("Headphones","Not connected");assertFalse(enabled("Use sonar"));assertEquals(1,opens.get());capture("route-returned");Runnable staleWatch=routeWatches.get(routeWatches.size()-1).changed;
   recreate();i.runOnMainSync(staleWatch);
   assertFalse(enabled("Use sonar"));
   assertEquals(1,opens.get());
   click("Test signal");
   qualified("probe after recreation");
   step="open actual contactless options before changing probe gain";
   click("Contactless options");
   ActivityMonitorAction(()->activity.recreate());
   step="restored owned options have focus and actual gain control";
   waitFor(()->{boolean[] present={false};i.runOnMainSync(()->{int count=0;for(View root:android.view.inspector.WindowInspector.getGlobalWindowViews())if(root.hasWindowFocus()&&root.isAttachedToWindow()&&root.isLaidOut()&&root.getContext().getPackageName().equals(c.getPackageName())){if(root!=activity.getWindow().getDecorView()&&find(root,"Contactless probe gain") instanceof SeekBar)count++;}assertTrue("Only one owned gain dialog",count<=1);present[0]=count==1;});return present[0];});
   step="change probe gain to 7 percent";
   i.runOnMainSync(()->{
    SeekBar slider=null;for(View root:android.view.inspector.WindowInspector.getGlobalWindowViews())if(root.hasWindowFocus()&&root.getContext().getPackageName().equals(c.getPackageName())){View value=find(root,"Contactless probe gain");if(value instanceof SeekBar)slider=(SeekBar)value;}
    assertNotNull(slider);
    Bundle args=new Bundle();
    args.putFloat(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_PROGRESS_VALUE,6);
    assertTrue(slider.performAccessibilityAction(android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS.getId(),args));
   });
   i.runOnMainSync(()->{View done=null;for(View root:android.view.inspector.WindowInspector.getGlobalWindowViews())if(root.hasWindowFocus()&&root.getContext().getPackageName().equals(c.getPackageName())){View target=find(root,"Done");if(target!=null){assertNull("One options Done",done);done=target;}}assertNotNull(done);android.graphics.Rect bounds=new android.graphics.Rect();assertTrue(done.getGlobalVisibleRect(bounds));assertEquals(done.getWidth(),bounds.width());assertEquals(done.getHeight(),bounds.height());assertTrue(bounds.width()>=Design.dp(c,48));assertTrue(bounds.height()>=Design.dp(c,48));assertTrue(done.performClick());});
   ready();
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
