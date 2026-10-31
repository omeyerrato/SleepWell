package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.media.AudioManager;
import android.os.*;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.*;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.*;

/** Broad native rendering plus owned interactions. Disposable emulator only; no output actions. */
public final class NightDialBreadthTest {
    final Instrumentation i=InstrumentationRegistry.getInstrumentation();
    final Context c=i.getTargetContext();final UiDevice device=UiDevice.getInstance(i);
    final class Scene implements AutoCloseable {
        final String name="night-breadth-"+UUID.randomUUID();final Map<String,?> prefs=new HashMap<>(Prefs.of(c).getAll());
        final Records.Factory records=Records.factory;final DiaryStore.Factory diary=DiaryStore.factory;final SRIReviews.Factory sri=SRIReviews.factory;
        final AlarmScheduling.Port schedule=AlarmScheduling.port;final AtomicInteger scheduled=new AtomicInteger();
        final AudioManager audio=(AudioManager)c.getSystemService(Context.AUDIO_SERVICE);final int volume=audio.getStreamVolume(AudioManager.STREAM_ALARM);
        final List<Activity> owned=new CopyOnWriteArrayList<>();
        final Application app=(Application)c.getApplicationContext();
        final Application.ActivityLifecycleCallbacks life=new Application.ActivityLifecycleCallbacks(){public void onActivityCreated(Activity a,Bundle b){owned.add(a);}public void onActivityStarted(Activity a){}public void onActivityResumed(Activity a){}public void onActivityPaused(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,Bundle b){}public void onActivityDestroyed(Activity a){}};
        Scene()throws Exception{
            assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertEquals("1",device.executeShellCommand("getprop ro.kernel.qemu").trim());quiet();
            assertTrue(Prefs.of(c).edit().clear().putBoolean(NightDialWelcome.DONE,true).commit());Records.factory=context->new Store(context,name+".db");DiaryStore.factory=context->new DiaryStore(context,name+"-diary.db");SRIReviews.factory=context->context.getSharedPreferences(name+"-sri",Context.MODE_PRIVATE);
            AlarmScheduling.port=new AlarmScheduling.Port(){public boolean allowed(Context x){return true;}public void schedule(Context x,Alarm a){scheduled.incrementAndGet();}};app.registerActivityLifecycleCallbacks(life);
        }
        Activity launch(Class<? extends Activity> type)throws Exception{return launch(type,new Intent(c,type));}
        Activity launch(Class<? extends Activity> type,Intent intent)throws Exception{
            Instrumentation.ActivityMonitor monitor=i.addMonitor(type.getName(),null,false);
            try{c.startActivity(intent.putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));Activity activity=monitor.waitForActivityWithTimeout(6000);assertNotNull(type.getSimpleName(),activity);focused(activity);return activity;}finally{i.removeMonitor(monitor);}
        }
        Activity recreate(Activity before)throws Exception{Instrumentation.ActivityMonitor monitor=i.addMonitor(before.getClass().getName(),null,false);try{i.runOnMainSync(before::recreate);Activity activity=monitor.waitForActivityWithTimeout(6000);assertNotNull(activity);focused(activity);return activity;}finally{i.removeMonitor(monitor);}}
        void quiet(){assertFalse(AlarmService.active);assertFalse(TrackingService.running);assertNull(SleepSoundService.snapshot());assertFalse(MicrophoneLease.occupied());assertEquals(volume,audio.getStreamVolume(AudioManager.STREAM_ALARM));}
        void unchanged(){quiet();assertEquals(0,scheduled.get());try(Store store=new Store(c,name+".db")){assertTrue(store.alarms().isEmpty());assertTrue(store.rows("SELECT id FROM sessions").isEmpty());}}
        public void close()throws Exception{
            device.pressHome();i.runOnMainSync(()->owned.forEach(a->{if(!a.isDestroyed())a.finish();}));long until=SystemClock.elapsedRealtime()+6000;while(owned.stream().anyMatch(a->!a.isDestroyed())&&SystemClock.elapsedRealtime()<until)SystemClock.sleep(40);assertTrue(owned.stream().allMatch(Activity::isDestroyed));app.unregisterActivityLifecycleCallbacks(life);DiaryJobs.drain(DiaryStore.factory);DiaryJobs.forget(DiaryStore.factory);quiet();Records.factory=records;DiaryStore.factory=diary;SRIReviews.factory=sri;AlarmScheduling.port=schedule;
            SharedPreferences.Editor restore=Prefs.of(c).edit().clear();for(Map.Entry<String,?> e:prefs.entrySet()){Object v=e.getValue();if(v instanceof Boolean)restore.putBoolean(e.getKey(),(Boolean)v);else if(v instanceof Integer)restore.putInt(e.getKey(),(Integer)v);else if(v instanceof Long)restore.putLong(e.getKey(),(Long)v);else if(v instanceof Float)restore.putFloat(e.getKey(),(Float)v);else if(v instanceof String)restore.putString(e.getKey(),(String)v);else if(v instanceof Set<?>)restore.putStringSet(e.getKey(),new HashSet<>((Set<String>)v));}assertTrue(restore.commit());assertEquals(prefs,Prefs.of(c).getAll());c.deleteDatabase(name+".db");c.deleteDatabase(name+"-diary.db");c.deleteSharedPreferences(name+"-sri");
        }
    }
    void focused(Activity activity)throws Exception{long until=SystemClock.elapsedRealtime()+6000;boolean[] ready={false};while(!ready[0]&&SystemClock.elapsedRealtime()<until){i.runOnMainSync(()->ready[0]=!activity.isDestroyed()&&!activity.isFinishing()&&activity.getWindow().getDecorView().isLaidOut()&&android.view.inspector.WindowInspector.getGlobalWindowViews().stream().anyMatch(View::hasWindowFocus));if(!ready[0])SystemClock.sleep(32);}assertTrue("Native input owner "+activity.getClass().getSimpleName(),ready[0]);device.waitForIdle(750);}
    void capture(Activity activity,String name)throws Exception{
        device.waitForIdle(750);CountDownLatch frame=new CountDownLatch(1);i.runOnMainSync(()->{View decor=android.view.inspector.WindowInspector.getGlobalWindowViews().stream().filter(View::hasWindowFocus).findFirst().orElseThrow();decor.getViewTreeObserver().registerFrameCommitCallback(frame::countDown);decor.invalidate();});assertTrue("Native frame",frame.await(5,TimeUnit.SECONDS));
        File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());File picture=new File(folder,"night-dial-breadth-"+name+".png");boolean dark=false;long until=SystemClock.elapsedRealtime()+5000;while(!dark&&SystemClock.elapsedRealtime()<until){assertTrue(device.takeScreenshot(picture));android.graphics.BitmapFactory.Options options=new android.graphics.BitmapFactory.Options();options.inSampleSize=4;android.graphics.Bitmap bitmap=android.graphics.BitmapFactory.decodeFile(picture.getAbsolutePath(),options);assertNotNull(bitmap);try{int x=Math.max(1,Design.dp(c,8)/4);dark=true;for(float y:new float[]{.25f,.5f,.75f}){int color=bitmap.getPixel(x,Math.round(bitmap.getHeight()*y));dark&=android.graphics.Color.red(color)<40&&android.graphics.Color.green(color)<40&&android.graphics.Color.blue(color)<40;}}finally{bitmap.recycle();}if(!dark)SystemClock.sleep(64);}assertTrue("Final composed dark surface, not a launcher or transition overlay: "+name,dark);device.dumpWindowHierarchy(new File(folder,"night-dial-breadth-"+name+".xml"));
        i.runOnMainSync(()->assertTrue("Dark app chrome "+name,Design.darkSystemBarSurface(activity)));
    }
    UiObject2 text(String label){UiObject2 view=device.wait(Until.findObject(By.text(label)),5000);assertNotNull(label,view);return view;}
    void tap(String label)throws Exception{UiObject2 view=device.wait(Until.findObject(By.text(label)),1000);if(view==null){new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().text(label));view=text(label);}view.click();}
    @SuppressWarnings("unchecked") @Test public void secondaryDestinationsRenderWithDarkChromeAndQuietOwners()throws Exception{
        try(Scene scene=new Scene()){
            String[] destinations={"NapActivity","GuidedWindDownActivity","DiaryActivity","AppearanceActivity","GuardActivity","AutomaticTrackingActivity","RecurringTrackingActivity","SleepRhythmActivity","GoalBankActivity","TravelActivity","CalendarRulesActivity","SmartBedtimeActivity","SmartLightActivity","HueBridgeActivity","AutomationActivity","HealthConnectActivity","BackupActivity","CloudBackupActivity","SettingsProfileActivity","SearchActivity","SoundLibrarySettingsActivity","FavoriteSoundsActivity","SleepSoundsActivity","DreamCueActivity","SnoringCuesActivity","TagManagementActivity","HomeWidgetSettingsActivity","AlarmProtectionActivity","FeedbackSettingsActivity","AwakeSettingsActivity","SoundSensitivityActivity","TrackingDelayActivity","TrackingFlipActivity","TrackingDndActivity","ExternalMediaFadeActivity","BinauralSettingsActivity","SleepCsvActivity","ResearchExportActivity","RecordAccessActivity","AlarmAccessActivity","IncomingAutomationSettingsActivity","NarrationVoiceActivity","SRIActivity","NapProfilesActivity","HabitualTimingActivity"};
            for(String name:destinations){Class<? extends Activity> type=(Class<? extends Activity>)Class.forName("org.sleepwell.app."+name);Activity activity=scene.launch(type);if(type==HomeWidgetSettingsActivity.class)assertNotNull(device.wait(Until.findObject(By.desc("Widget preview ready")),5000));capture(activity,name.replace("Activity",""));scene.unchanged();}
        }
    }
    @Test public void napWheelCancellationExactAccessibilityAndRecreationStayDraftOnly()throws Exception{
        try(Scene scene=new Scene()){
            Activity activity=scene.launch(NapActivity.class);final Activity first=activity;final int[] original={0};
            i.runOnMainSync(()->{NightDialNapWheel wheel=find(first.getWindow().getDecorView(),NightDialNapWheel.class);assertNotNull(wheel);AccessibilityNodeInfo node=wheel.createAccessibilityNodeInfo();original[0]=(int)node.getRangeInfo().getCurrent();assertEquals(20,original[0]);float x=wheel.getWidth()/2f,y=wheel.getHeight()*.48f,r=Math.min(wheel.getWidth()*.42f,wheel.getHeight()*.45f);long now=SystemClock.uptimeMillis();android.view.MotionEvent down=android.view.MotionEvent.obtain(now,now,android.view.MotionEvent.ACTION_DOWN,x,y-r,0),move=android.view.MotionEvent.obtain(now,now+16,android.view.MotionEvent.ACTION_MOVE,x+r,y,0),cancel=android.view.MotionEvent.obtain(now,now+32,android.view.MotionEvent.ACTION_CANCEL,x+r,y,0);try{assertTrue(wheel.dispatchTouchEvent(down));assertTrue(wheel.dispatchTouchEvent(move));assertTrue(wheel.dispatchTouchEvent(cancel));}finally{down.recycle();move.recycle();cancel.recycle();}assertEquals(20,(int)wheel.createAccessibilityNodeInfo().getRangeInfo().getCurrent());});
            capture(activity,"NapCancelled");i.runOnMainSync(()->{NightDialNapWheel wheel=find(first.getWindow().getDecorView(),NightDialNapWheel.class);Bundle args=new Bundle();args.putFloat(AccessibilityNodeInfo.ACTION_ARGUMENT_PROGRESS_VALUE,35);assertTrue(wheel.performAccessibilityAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS.getId(),args));});
            activity=scene.recreate(activity);Activity restored=activity;i.runOnMainSync(()->assertEquals(35,(int)find(restored.getWindow().getDecorView(),NightDialNapWheel.class).createAccessibilityNodeInfo().getRangeInfo().getCurrent()));capture(activity,"NapRestored");scene.unchanged();
        }
    }
    @Test public void diaryKeepsDirtyDraftAcrossBackAndRecreationUntilExplicitDiscard()throws Exception{
        try(Scene scene=new Scene()){
            Activity activity=scene.launch(DiaryActivity.class);tap("Write diary entry");UiObject2 input=device.wait(Until.findObject(By.desc("Diary entry text")),5000);assertNotNull(input);input.setText("The room was quiet. I remember the sea.");device.pressBack();if(!device.hasObject(By.text("Keep this diary draft?")))device.pressBack();text("Keep this diary draft?");capture(activity,"DiaryDiscard");activity=scene.recreate(activity);text("Keep this diary draft?");tap("Keep writing");assertEquals("The room was quiet. I remember the sea.",device.findObject(By.desc("Diary entry text")).getText());capture(activity,"DiaryDraft");device.pressBack();text("Keep this diary draft?");tap("Discard changes");text("Write diary entry");try(DiaryStore store=new DiaryStore(c,scene.name+"-diary.db")){assertEquals(0,store.count());}scene.unchanged();capture(activity,"DiaryDiscarded");
        }
    }
    @Test public void allTaskConfigurationRoutesAreReachableWithoutStartingChecks()throws Exception{
        try(Scene scene=new Scene()){
            Alarm draft=new Alarm(1);draft.enabled=false;Activity activity=scene.launch(AlarmEditorActivity.class,new Intent(c,AlarmEditorActivity.class).putExtra("alarm",draft.json().toString()).putExtra("newAlarm",true));UiObject2 challenges=device.wait(Until.findObject(By.desc("Challenges")),5000);assertNotNull(challenges);challenges.click();text("Save routine");tap("Browse all tasks");
            for(String type:ChallengeRoutine.TYPES){UiObject2 search=device.wait(Until.findObject(By.desc("Search task types")),5000);assertNotNull(type,search);search.setText(type);UiObject2 configure=device.wait(Until.findObject(By.desc("Configure "+type)),5000);assertNotNull(type,configure);configure.click();device.waitForIdle(750);Activity[] selected={null};i.runOnMainSync(()->selected[0]=scene.owned.stream().filter(a->!a.isDestroyed()&&a.hasWindowFocus()).reduce((a,b)->b).orElseThrow());capture(selected[0],"Task-"+type.replace(' ','-'));UiObject2 back=device.wait(Until.findObject(By.desc("Back")),5000);assertNotNull(type+" Back",back);back.click();assertNotNull(device.wait(Until.findObject(By.desc("Search task types")),5000));scene.unchanged();}
        }
    }
    @Test public void cognitiveWorkspacesUseTheirRealNativeTasksWithoutAlarmOutput()throws Exception{
        try(Scene scene=new Scene()){
            for(String type:List.of("Math","Memory","Sequence","Retype","Counting","Flags","Mirror text","Dream journal","Visual search","Jumping","Type code")){
                ChallengeRoutine routine=new ChallengeRoutine(List.of(new ChallengeRoutine.Task(type,1)));
                Intent intent=new Intent(c,ChallengePracticeActivity.class).putExtra("request",UUID.randomUUID().toString()).putExtra("routine",ChallengeRoutineJson.encode(routine).toString());
                Activity activity=scene.launch(ChallengePracticeActivity.class,intent);capture(activity,"Workspace-"+type.replace(' ','-'));scene.unchanged();
            }
        }
    }
    @Test public void largeTextAndReducedMotionKeepKeyDestinationsReachable()throws Exception{
        try(Scene scene=new Scene()){
            assertTrue(Prefs.of(c).edit().putInt(Appearance.SIZE,2).putBoolean(Appearance.MOTION,true).commit());
            for(Class<? extends Activity> type:List.of(AppearanceActivity.class,NapActivity.class,GuidedWindDownActivity.class,BackupActivity.class,AutomaticTrackingActivity.class)){
                Activity activity=scene.launch(type);i.runOnMainSync(()->{assertFalse(InteractionFeedback.motion(activity));assertEquals(R.style.NightDialStaticAnimation,activity.getWindow().getAttributes().windowAnimations);});capture(activity,"Large-"+type.getSimpleName().replace("Activity",""));scene.unchanged();
            }
        }
    }
    static <T extends View>T find(View root,Class<T> type){if(type.isInstance(root))return type.cast(root);if(root instanceof ViewGroup group)for(int n=0;n<group.getChildCount();n++){T found=find(group.getChildAt(n),type);if(found!=null)return found;}return null;}
}
