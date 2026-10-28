package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;
import static org.junit.Assert.*;

/** CI-only synthetic clock/plan, no OS clock change, scheduling, audio or tracking. */
@RunWith(AndroidJUnit4.class)
public class NightArcFlowTest {
    private final Instrumentation i=InstrumentationRegistry.getInstrumentation();
    private final Context c=i.getTargetContext();private final UiDevice device=UiDevice.getInstance(i);
    private MainActivity activity;
    private AlertDialog assetDialog;

    /** Diagnostic fixture only: Android-loaded resources in real windows, never app navigation. */
    private void nativeRasterGallery()throws Exception{
        List<java.lang.reflect.Field> resources=new ArrayList<>();
        for(java.lang.reflect.Field field:R.drawable.class.getFields())if(field.getName().startsWith("ic_phosphor_"))resources.add(field);
        resources.sort(Comparator.comparing(java.lang.reflect.Field::getName));
        assertEquals("All semantic resources, without duplicate aliases",73,resources.size());
        List<String> metrics=new ArrayList<>();metrics.add("resource\tclass\tintrinsic_width\tintrinsic_height\tbitmap_width\tbitmap_height\tbitmap_density\talpha_min\talpha_max\ttransparent_pixels\ttint\tactual_window_bounds");
        for(int page=0;page<3;page++){
            int begin=page*25,end=Math.min(begin+25,resources.size()),number=page+1;
            List<ImageButton> controls=new ArrayList<>();List<Integer> colors=new ArrayList<>();List<String> decoded=new ArrayList<>();
            i.runOnMainSync(()->{
                LinearLayout body=diagnosticBody("Raster glyphs "+number+" / 3",(end-begin)+" real Android drawables · espresso / teal tint check");
                for(int row=begin;row<end;row+=5){
                    LinearLayout line=new LinearLayout(activity);line.setGravity(Gravity.CENTER);
                    for(int index=row;index<Math.min(row+5,end);index++){
                        String name=resources.get(index).getName();int id;
                        try{id=resources.get(index).getInt(null);}catch(IllegalAccessException failure){throw new AssertionError(failure);}
                        Drawable loaded=activity.getDrawable(id);assertTrue(name+" must load as BitmapDrawable, never VectorDrawable",loaded instanceof BitmapDrawable);
                        BitmapDrawable glyph=(BitmapDrawable)loaded.mutate();Bitmap bitmap=glyph.getBitmap();
                        int expected=Design.dp(c,24);assertTrue(name+" intrinsic 24dp width",Math.abs(expected-glyph.getIntrinsicWidth())<=1);assertTrue(name+" intrinsic 24dp height",Math.abs(expected-glyph.getIntrinsicHeight())<=1);
                        assertTrue(name+" decoded alpha channel",bitmap.hasAlpha());int min=255,max=0,transparent=0;
                        for(int y=0;y<bitmap.getHeight();y++)for(int x=0;x<bitmap.getWidth();x++){int alpha=Color.alpha(bitmap.getPixel(x,y));min=Math.min(min,alpha);max=Math.max(max,alpha);if(alpha==0)transparent++;}
                        assertEquals(name+" background genuinely transparent",0,min);assertTrue(name+" nonempty visible glyph",max>0);assertTrue(name+" transparent outside",transparent>0);
                        for(int x:new int[]{0,bitmap.getWidth()-1})for(int y:new int[]{0,bitmap.getHeight()-1})assertEquals(name+" no opaque corner/canvas",0,Color.alpha(bitmap.getPixel(x,y)));
                        // Two successive real Drawable tint changes; PixelCopy below verifies the final native-rendered color.
                        int color=index%2==0?0xff4a2414:0xff315f73;glyph.setTint(0xffb34c35);glyph.setTint(color);
                        LinearLayout cell=new LinearLayout(activity);cell.setOrientation(LinearLayout.VERTICAL);cell.setGravity(Gravity.CENTER);
                        ImageButton button=new ImageButton(activity);button.setImageDrawable(glyph);button.setScaleType(ImageView.ScaleType.CENTER);button.setPadding(Design.dp(c,12),Design.dp(c,12),Design.dp(c,12),Design.dp(c,12));button.setBackgroundColor(Color.TRANSPARENT);button.setSoundEffectsEnabled(false);button.setHapticFeedbackEnabled(false);button.setContentDescription("Raster glyph "+name);cell.addView(button,new LinearLayout.LayoutParams(Design.dp(c,48),Design.dp(c,48)));
                        TextView label=new TextView(activity);label.setText((index+1)+" "+name.replace("ic_phosphor_","").replace("_regular","").replace('_',' '));label.setTextSize(9);label.setTextColor(0xff4a2414);label.setGravity(Gravity.CENTER);label.setMaxLines(2);label.setEllipsize(android.text.TextUtils.TruncateAt.END);cell.addView(label,new LinearLayout.LayoutParams(-1,Design.dp(c,28)));
                        line.addView(cell,new LinearLayout.LayoutParams(0,Design.dp(c,76),1));controls.add(button);colors.add(color);decoded.add(name+"\t"+glyph.getClass().getSimpleName()+"\t"+glyph.getIntrinsicWidth()+"\t"+glyph.getIntrinsicHeight()+"\t"+bitmap.getWidth()+"\t"+bitmap.getHeight()+"\t"+bitmap.getDensity()+"\t"+min+"\t"+max+"\t"+transparent+"\t"+Integer.toHexString(color));
                    }
                    body.addView(line,new LinearLayout.LayoutParams(-1,-2));
                }
                showDiagnostic(body);
            });
            assertEquals("Exact page control count",page<2?25:23,controls.size());
            captureDiagnostic("arc-icon-page-"+number+".png",controls);
            Bitmap frame=copyDiagnosticWindow();
            try{for(int index=0;index<controls.size();index++){ImageButton control=controls.get(index);int[] xy=new int[2];i.runOnMainSync(()->control.getLocationInWindow(xy));assertNativeTint(frame,xy[0],xy[1],control.getWidth(),control.getHeight(),colors.get(index),control.getContentDescription().toString());metrics.add(decoded.get(index)+"\t"+xy[0]+","+xy[1]+","+control.getWidth()+","+control.getHeight());}}finally{frame.recycle();}
            dismissDiagnostic();
        }
        launcherDiagnostic(metrics);
        File folder=new File(c.getExternalFilesDir(null),"verification");try(java.io.Writer output=new java.io.OutputStreamWriter(new java.io.FileOutputStream(new File(folder,"arc-icon-metrics.tsv")),java.nio.charset.StandardCharsets.UTF_8)){for(String row:metrics)output.write(row+"\n");}
    }
    private LinearLayout diagnosticBody(String title,String subtitle){
        LinearLayout body=new LinearLayout(activity);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(Design.dp(c,12),Design.dp(c,12),Design.dp(c,12),Design.dp(c,12));body.setBackgroundColor(0xfffff4e4);
        TextView heading=new TextView(activity);heading.setText(title);heading.setTextSize(20);heading.setTextColor(0xff4a2414);body.addView(heading);
        TextView detail=new TextView(activity);detail.setText("Diagnostic fixture · "+subtitle);detail.setTextSize(11);detail.setTextColor(0xff4a2414);detail.setPadding(0,0,0,Design.dp(c,12));body.addView(detail);return body;
    }
    private void showDiagnostic(LinearLayout body){assetDialog=new AlertDialog.Builder(activity).setView(body).setPositiveButton("Close diagnostic",(dialog,which)->{}).create();assetDialog.setCanceledOnTouchOutside(false);assetDialog.show();assetDialog.getWindow().setLayout(activity.getResources().getDisplayMetrics().widthPixels-Design.dp(c,24),ViewGroup.LayoutParams.WRAP_CONTENT);}
    private void captureDiagnostic(String name,List<? extends View> controls)throws Exception{
        AlertDialog owned=assetDialog;View window=owned.getWindow().getDecorView();
        waitFor(()->{View active=focused();boolean[] ready={false};i.runOnMainSync(()->ready[0]=assetDialog==owned&&owned.isShowing()&&window.isLaidOut()&&window.hasWindowFocus()&&active==window);return ready[0];});
        CountDownLatch committed=new CountDownLatch(1);i.runOnMainSync(()->{assertTrue(window.isHardwareAccelerated());for(View control:controls)full(control,"Diagnostic "+control.getContentDescription());window.getViewTreeObserver().registerFrameCommitCallback(committed::countDown);window.invalidate();});assertTrue("Actual diagnostic frame committed",committed.await(5,TimeUnit.SECONDS));
        File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());assertTrue(device.takeScreenshot(new File(folder,name)));
    }
    private Bitmap copyDiagnosticWindow()throws Exception{
        // PixelCopy samples the actual committed dialog surface, not an offscreen Canvas rendering of an icon.
        Window window=assetDialog.getWindow();View decor=window.getDecorView();Bitmap copied=Bitmap.createBitmap(decor.getWidth(),decor.getHeight(),Bitmap.Config.ARGB_8888);CountDownLatch done=new CountDownLatch(1);int[] result={-1};i.runOnMainSync(()->PixelCopy.request(window,copied,value->{result[0]=value;done.countDown();},new Handler(Looper.getMainLooper())));assertTrue(done.await(5,TimeUnit.SECONDS));assertEquals("Actual dialog surface copied",PixelCopy.SUCCESS,result[0]);return copied;
    }
    private void assertNativeTint(Bitmap frame,int left,int top,int width,int height,int color,String label){boolean found=false;for(int y=Math.max(0,top);y<Math.min(frame.getHeight(),top+height)&&!found;y++)for(int x=Math.max(0,left);x<Math.min(frame.getWidth(),left+width);x++){int actual=frame.getPixel(x,y);if(Math.abs(Color.red(actual)-Color.red(color))<=8&&Math.abs(Color.green(actual)-Color.green(color))<=8&&Math.abs(Color.blue(actual)-Color.blue(color))<=8){found=true;break;}}assertTrue(label+" retains real rendered tint",found);}
    private void dismissDiagnostic()throws Exception{AlertDialog owned=assetDialog;View window=owned.getWindow().getDecorView();i.runOnMainSync(owned::dismiss);waitFor(()->{boolean[] closed={false};i.runOnMainSync(()->closed[0]=!window.isAttachedToWindow()&&activity.hasWindowFocus());return closed[0];});assetDialog=null;}
    private void launcherDiagnostic(List<String> metrics)throws Exception{
        assertTrue("The themed preview scope requires CI API33+",Build.VERSION.SDK_INT>=33);List<ImageView> previews=new ArrayList<>();List<AdaptiveIconDrawable> icons=new ArrayList<>();
        i.runOnMainSync(()->{
            Drawable applicationIcon;try{applicationIcon=activity.getPackageManager().getApplicationIcon(c.getPackageName());}catch(android.content.pm.PackageManager.NameNotFoundException failure){throw new AssertionError(failure);}
            assertTrue("PackageManager loads the actual adaptive launcher",applicationIcon instanceof AdaptiveIconDrawable);AdaptiveIconDrawable adaptive=(AdaptiveIconDrawable)applicationIcon;assertNotNull("Actual API33 monochrome resource",adaptive.getMonochrome());
            LinearLayout body=diagnosticBody("Launcher color and themed icons","PackageManager resource + framework adaptive mask; large and48dp");
            for(int size:new int[]{120,48}){
                LinearLayout line=new LinearLayout(activity);line.setGravity(Gravity.CENTER);line.setPadding(0,Design.dp(c,12),0,Design.dp(c,12));
                for(boolean themed:new boolean[]{false,true}){
                    Drawable loaded=adaptive.getConstantState().newDrawable(activity.getResources()).mutate();AdaptiveIconDrawable shown;
                    if(themed){Drawable monochrome=((AdaptiveIconDrawable)loaded).getMonochrome().mutate();monochrome.setTint(0xff4a2414);shown=new AdaptiveIconDrawable(new ColorDrawable(0xffe9d6b8),monochrome);}else shown=(AdaptiveIconDrawable)loaded;
                    ImageView preview=new ImageView(activity);preview.setImageDrawable(shown);preview.setScaleType(ImageView.ScaleType.FIT_CENTER);preview.setContentDescription((themed?"Themed":"Color")+" actual adaptive launcher "+size+"dp");LinearLayout.LayoutParams slot=new LinearLayout.LayoutParams(Design.dp(c,size),Design.dp(c,size));slot.setMargins(Design.dp(c,12),0,Design.dp(c,12),0);line.addView(preview,slot);previews.add(preview);icons.add(shown);
                }
                body.addView(line,new LinearLayout.LayoutParams(-1,-2));
            }
            showDiagnostic(body);
        });
        captureDiagnostic("arc-launcher-icons.png",previews);
        i.runOnMainSync(()->{for(int index=0;index<previews.size();index++){AdaptiveIconDrawable icon=icons.get(index);Rect bounds=icon.getBounds();RectF mask=new RectF();icon.getIconMask().computeBounds(mask,true);assertTrue("Real framework adaptive mask is nonempty",!mask.isEmpty());assertTrue("Mask fits drawable bounds",mask.left>=bounds.left-1&&mask.top>=bounds.top-1&&mask.right<=bounds.right+1&&mask.bottom<=bounds.bottom+1);assertEquals("Preview fully accommodates native drawable width",previews.get(index).getWidth(),bounds.width());assertEquals("Preview fully accommodates native drawable height",previews.get(index).getHeight(),bounds.height());metrics.add("launcher\t"+previews.get(index).getContentDescription()+"\t"+icon.getClass().getSimpleName()+"\tmask="+mask+"\tbounds="+bounds);}});
        dismissDiagnostic();
    }
    private long local(int day,int hour,int minute){return LocalDate.of(2026,10,day).atTime(hour,minute).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();}
    private void launch(){Instrumentation.ActivityMonitor monitor=i.addMonitor(MainActivity.class.getName(),null,false);try{c.startActivity(new Intent(c,MainActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));activity=(MainActivity)monitor.waitForActivityWithTimeout(5000);assertNotNull(activity);i.waitForIdleSync();device.waitForIdle();}finally{i.removeMonitor(monitor);}}
    private NightArc arc(View view){if(view instanceof NightArc)return (NightArc)view;if(view instanceof ViewGroup)for(int n=0;n<((ViewGroup)view).getChildCount();n++){NightArc found=arc(((ViewGroup)view).getChildAt(n));if(found!=null)return found;}return null;}
    private View described(View root,String description){if(description.contentEquals(root.getContentDescription()==null?"":root.getContentDescription()))return root;if(root instanceof ViewGroup group)for(int n=0;n<group.getChildCount();n++){View found=described(group.getChildAt(n),description);if(found!=null)return found;}return null;}
    private void state(String part){assertTrue(part,device.wait(Until.hasObject(By.descContains(part)),5000));}
    private void waitFor(java.util.function.BooleanSupplier condition)throws Exception{long end=SystemClock.elapsedRealtime()+8000;while(!condition.getAsBoolean()&&SystemClock.elapsedRealtime()<end)SystemClock.sleep(30);assertTrue("Actual owned Tonight state",condition.getAsBoolean());}
    private void recreate()throws Exception{MainActivity old=activity;Instrumentation.ActivityMonitor monitor=i.addMonitor(MainActivity.class.getName(),null,false);try{i.runOnMainSync(old::recreate);activity=(MainActivity)monitor.waitForActivityWithTimeout(5000);assertNotNull(activity);}finally{i.removeMonitor(monitor);}waitFor(old::isDestroyed);waitFor(()->{boolean[] ready={false};i.runOnMainSync(()->ready[0]=activity.hasWindowFocus()&&!activity.isDestroyed()&&activity.getWindow().getDecorView().isLaidOut());return ready[0];});}
    private void full(View view,String label){assertNotNull(label,view);android.graphics.Rect area=new android.graphics.Rect();assertTrue(label+" visible",view.getGlobalVisibleRect(area));assertEquals(label+" full width",view.getWidth(),area.width());assertEquals(label+" full height",view.getHeight(),area.height());assertTrue(label+" 48dp height",area.height()>=Design.dp(c,48));assertTrue(label+" 48dp width",area.width()>=Design.dp(c,48));}
    private View focused(){View[] value={null};i.runOnMainSync(()->{for(View root:android.view.inspector.WindowInspector.getGlobalWindowViews())if(root.isAttachedToWindow()&&root.isLaidOut()&&root.hasWindowFocus()&&root.getContext().getPackageName().equals(c.getPackageName())){if(value[0]!=null){value[0]=null;return;}value[0]=root;}});return value[0];}
    private void mainActionBounds(){View root=activity.getWindow().getDecorView();View action=described(root,"Start sleep tracking");full(action,"Fixed Start action");ViewGroup footer=(ViewGroup)action.getParent(),page=(ViewGroup)footer.getParent();assertTrue("Content remains independently scrollable",page.getChildAt(0) instanceof ScrollView);assertEquals("Action footer is immediately before the existing dock",page.getChildCount()-2,page.indexOfChild(footer));assertTrue(footer.getBackground() instanceof android.graphics.drawable.ColorDrawable);assertEquals("Footer inherits the current phase ground",Appearance.palette(c).bg,((android.graphics.drawable.ColorDrawable)footer.getBackground()).getColor());View dock=page.getChildAt(page.getChildCount()-1);for(String name:List.of("Tonight","Alarms","Report","Settings"))full(described(dock,name),"Existing dock "+name);android.graphics.Rect start=new android.graphics.Rect(),navigation=new android.graphics.Rect();assertTrue(action.getGlobalVisibleRect(start));assertTrue(dock.getGlobalVisibleRect(navigation));assertTrue("Start never enters the dock",start.bottom<=navigation.top);assertEquals(android.widget.Button.class.getName(),action.createAccessibilityNodeInfo().getClassName());}
    private void capture(String name)throws Exception{waitFor(()->{View candidate=focused();if(candidate==null)return false;boolean[] proper={false};i.runOnMainSync(()->proper[0]=name.equals("help")?described(candidate,"Close rest window explanation")!=null:candidate==activity.getWindow().getDecorView());return proper[0];});View window=focused();assertNotNull(window);CountDownLatch frame=new CountDownLatch(1);i.runOnMainSync(()->{assertTrue(window.hasWindowFocus());assertTrue(window.isHardwareAccelerated());if(name.equals("help"))full(described(window,"Close rest window explanation"),"Real help close");else if(name.equals("empty-action"))full(described(window,"Add alarm"),"Actual Add alarm destination");else mainActionBounds();window.getViewTreeObserver().registerFrameCommitCallback(frame::countDown);window.invalidate();});assertTrue(frame.await(5,TimeUnit.SECONDS));File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());assertTrue(device.takeScreenshot(new File(folder,"arc-"+name+".png")));}
    private void click(String description)throws Exception{UiObject2 target=device.wait(Until.findObject(By.desc(description)),5000);assertNotNull(description,target);android.graphics.Rect area=target.getVisibleBounds();assertTrue(description+" full 48dp target",area.width()>=Design.dp(c,48)&&area.height()>=Design.dp(c,48));assertTrue(device.click(area.centerX(),area.centerY()));}
    @Test public void clockProgressAndExplanationWithoutAnyAlarmDelivery()throws Exception{
        assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertEquals("1",device.executeShellCommand("getprop ro.kernel.qemu").trim());
        assertFalse(Prefs.flag(c,"ringing"));assertFalse(TrackingService.running);assertFalse(Prefs.flag(c,"tracking"));assertNull(SleepSoundService.snapshot());
        Map<String,?> personal=new HashMap<>(Prefs.of(c).getAll());String suffix=UUID.randomUUID().toString(),database="sleepwell-arc-"+suffix+".db",preferences="sleepwell-arc-"+suffix;
        SharedPreferences fixture=c.getSharedPreferences(preferences,Context.MODE_PRIVATE);assertTrue(fixture.edit().putInt("bedtime",1350).putInt("wakeTime",390).putInt("goal",480).commit());Map<String,?> saved=new HashMap<>(fixture.getAll());
        Records.Factory priorRecords=Records.factory;SleepPlans.Factory priorPlans=SleepPlans.factory;LongSupplier priorClock=TonightScreen.clock,priorSkipsClock=AlarmSkips.clock;AlarmScheduling.Port priorScheduling=AlarmScheduling.port;int[] scheduled={0};AtomicLong now=new AtomicLong(local(22,22,0));
        List<Activity> owned=new CopyOnWriteArrayList<>();Application app=(Application)c.getApplicationContext();Application.ActivityLifecycleCallbacks lifecycle=new Application.ActivityLifecycleCallbacks(){public void onActivityCreated(Activity a,Bundle b){owned.add(a);assertTrue("Main configuration only; never an Alarm/Tracking owner",a instanceof MainActivity);assertTrue(a.getIntent().getBooleanExtra("visualReview",false));}public void onActivityStarted(Activity a){}public void onActivityResumed(Activity a){}public void onActivityPaused(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,Bundle b){}public void onActivityDestroyed(Activity a){}};app.registerActivityLifecycleCallbacks(lifecycle);
        try{
            Records.factory=context->new Store(context,database);SleepPlans.factory=context->fixture;TonightScreen.clock=now::get;AlarmSkips.clock=now::get;AlarmScheduling.port=new AlarmScheduling.Port(){public boolean allowed(Context context){return true;}public void schedule(Context context,Alarm alarm){scheduled[0]++;assertNull(alarm.nap);}};try(Store store=new Store(c,database)){Alarm alarm=new Alarm(1);alarm.hour=6;alarm.minute=30;alarm.enabled=false;alarm.days.addAll(Set.of(1,2,3,4,5,6,7));store.save(alarm);}Appearance.save(c,false,0,0,true,false);
            launch();state("Bedtime in 30 min");
            UiObject2 clockAction=device.findObject(By.desc("Alarms & challenges"));assertNotNull(clockAction);assertEquals(android.widget.Button.class.getName(),clockAction.getClassName());
            i.runOnMainSync(()->{NightArc rest=arc(activity.getWindow().getDecorView());assertNotNull(rest);android.view.accessibility.AccessibilityNodeInfo node=rest.createAccessibilityNodeInfo();try{assertEquals(android.widget.Button.class.getName(),node.getClassName().toString());assertTrue(node.isClickable());assertTrue(node.isFocusable());}finally{node.recycle();}View toggle=described(activity.getWindow().getDecorView(),"Tonight alarm enabled");assertTrue("Tonight switch uses gesture-only shared feedback",toggle instanceof InteractionFeedback.Toggle);});capture("upcoming");
            device.findObject(By.descStartsWith("Rest window from ")).click();assertTrue(device.wait(Until.hasObject(By.text("Your rest window")),5000));MainActivity beforeHelpRotation=activity;Instrumentation.ActivityMonitor helpMonitor=i.addMonitor(MainActivity.class.getName(),null,false);try{i.runOnMainSync(beforeHelpRotation::recreate);activity=(MainActivity)helpMonitor.waitForActivityWithTimeout(5000);assertNotNull("Recreated Tonight",activity);}finally{i.removeMonitor(helpMonitor);}i.waitForIdleSync();assertTrue(device.wait(Until.hasObject(By.text("Your rest window")),5000));i.runOnMainSync(()->{assertTrue(beforeHelpRotation.isDestroyed());NightArc restored=arc(activity.getWindow().getDecorView());assertNotNull(restored);assertTrue("Owned rest-window explanation restored without another tap",restored.explanationShowing());});capture("help");UiObject2 close=device.wait(Until.findObject(By.desc("Close rest window explanation")),5000);assertNotNull(close);close.click();assertTrue(device.wait(Until.gone(By.text("Your rest window")),5000));device.waitForIdle();
            NightArc[] original={null};i.runOnMainSync(()->original[0]=arc(activity.getWindow().getDecorView()));assertNotNull(original[0]);now.set(local(22,22,30));
            // Observe the real foreground Handler tick, even with manual appearance.
            assertTrue("The existing arc must advance on the production 30-second tick",device.wait(Until.hasObject(By.descContains(". 0 percent of clock time elapsed.")),35000));i.runOnMainSync(()->assertSame(original[0],arc(activity.getWindow().getDecorView())));capture("start");
            now.set(local(23,2,30));i.runOnMainSync(activity::refreshTonightClock);state(". 50 percent of clock time elapsed.");capture("middle");
            now.set(local(23,6,30));i.runOnMainSync(activity::refreshTonightClock);state(". 100 percent of clock time elapsed.");capture("end");
            now.set(local(23,6,31));i.runOnMainSync(activity::refreshTonightClock);state("Bedtime in 15 h 59 min");assertFalse(device.hasObject(By.descContains("percent of clock time elapsed")));capture("next");
            Appearance.save(c,false,0,2,true,false);now.set(local(23,2,30));launch();state(". 50 percent of clock time elapsed.");capture("large");new UiScrollable(new UiSelector().scrollable(true)).scrollToEnd(6);device.waitForIdle();UiObject2 action=device.findObject(By.desc("Start sleep tracking")),dock=device.findObject(By.desc("Tonight").selected(true));assertNotNull(action);assertNotNull(dock);assertEquals(android.widget.Button.class.getName(),action.getClassName());assertTrue("Large text action must be fully reachable above navigation without activating it",action.getVisibleBounds().height()>=Design.dp(c,48)&&action.getVisibleBounds().bottom<=dock.getVisibleBounds().top);capture("large-action");
            Appearance.save(c,false,0,0,true,false);try(Store store=new Store(c,database)){Alarm alarm=new Alarm(1);alarm.hour=6;alarm.minute=30;alarm.enabled=true;alarm.days.add(7);store.save(alarm);}now.set(local(22,22,0));launch();state("Bedtime in 48 h 30 min");assertTrue(device.hasObject(By.textContains("SUN 25 OCT")));capture("weekly");
            click("Tonight alarm enabled");assertTrue(device.wait(Until.hasObject(By.text("Alarm off")),5000));try(Store store=new Store(c,database)){assertFalse("Real switch persists Off",store.alarm(1).enabled);}assertEquals(1,scheduled[0]);capture("off");recreate();state("Bedtime in 30 min");assertFalse(device.findObject(By.desc("Tonight alarm enabled")).isChecked());assertEquals(1,device.findObjects(By.text("Alarm off")).size());capture("off-recreated");
            click("Tonight alarm enabled");state("Bedtime in 48 h 30 min");assertEquals(2,scheduled[0]);click("Monday");assertTrue(device.wait(Until.hasObject(By.desc("Monday").selected(true)),5000));try(Store store=new Store(c,database)){assertTrue(store.alarm(1).enabled);assertEquals(Set.of(1,7),store.alarm(1).days);}assertEquals(3,scheduled[0]);recreate();assertTrue(device.findObject(By.desc("Monday")).isSelected());assertTrue(device.findObject(By.desc("Sunday")).isSelected());capture("weekday-recreated");
            try(Store store=new Store(c,database)){store.deleteAlarm(1);}now.set(local(22,22,0));launch();state("Bedtime in 30 min");assertFalse("No switch without an existing alarm",device.hasObject(By.desc("Tonight alarm enabled")));UiObject2 first=device.wait(Until.findObject(By.desc("Set first alarm")),5000);assertNotNull(first);assertFalse(device.hasObject(By.text("Alarm off")));capture("empty");UiObject2 firstAction=device.findObject(By.desc("Set first alarm"));assertNotNull(firstAction);assertEquals(android.widget.Button.class.getName(),firstAction.getClassName());click("Set first alarm");assertTrue(device.wait(Until.hasObject(By.desc("Add alarm")),5000));capture("empty-action");device.pressBack();state("Bedtime in 30 min");assertNotNull(device.findObject(By.desc("Set first alarm")));assertEquals(3,scheduled[0]);try(Store store=new Store(c,database)){assertTrue(store.alarms().isEmpty());}
            assertEquals("Clock refresh must not save the isolated plan",saved,fixture.getAll());assertFalse(Prefs.flag(c,"ringing"));assertFalse(TrackingService.running);assertNull(SleepSoundService.snapshot());
            nativeRasterGallery();
        }catch(Exception|AssertionError failure){try{File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());device.takeScreenshot(new File(folder,"arc-failure.png"));device.dumpWindowHierarchy(new File(folder,"arc-failure.xml"));}catch(Exception diagnostic){failure.addSuppressed(diagnostic);}throw failure;}finally{
            try{i.runOnMainSync(()->{if(assetDialog!=null){assetDialog.dismiss();assetDialog=null;}for(Activity a:owned)if(!a.isDestroyed())a.finish();});i.waitForIdleSync();long deadline=SystemClock.elapsedRealtime()+5000;while(owned.stream().anyMatch(a->!a.isDestroyed())&&SystemClock.elapsedRealtime()<deadline)SystemClock.sleep(50);assertTrue("Owned Activities must close before restoring factories",owned.stream().allMatch(Activity::isDestroyed));}
            finally{app.unregisterActivityLifecycleCallbacks(lifecycle);TonightScreen.clock=priorClock;AlarmSkips.clock=priorSkipsClock;AlarmScheduling.port=priorScheduling;Records.factory=priorRecords;SleepPlans.factory=priorPlans;SharedPreferences.Editor edit=Prefs.of(c).edit();for(String key:new String[]{Appearance.AUTO,Appearance.THEME,Appearance.SIZE,Appearance.MOTION,Appearance.CONTRAST}){Object value=personal.get(key);if(value instanceof Boolean)edit.putBoolean(key,(Boolean)value);else if(value instanceof Integer)edit.putInt(key,(Integer)value);else edit.remove(key);}assertTrue(edit.commit());assertEquals("All personal preferences must be restored exactly",personal,Prefs.of(c).getAll());if(owned.stream().allMatch(Activity::isDestroyed)){c.deleteDatabase(database);c.deleteSharedPreferences(preferences);}}
        }
    }
}
