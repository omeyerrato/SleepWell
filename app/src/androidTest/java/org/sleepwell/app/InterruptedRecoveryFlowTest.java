package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.graphics.Rect;
import android.os.*;
import android.view.*;
import android.view.inspector.WindowInspector;
import android.widget.*;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import org.junit.Test;
import java.io.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.Assert.*;

/** Actual interrupted recovery/review with private stores; no service, capture or alarm output. */
public final class InterruptedRecoveryFlowTest {
    private final Instrumentation i=InstrumentationRegistry.getInstrumentation();
    private final Context c=i.getTargetContext();
    private final UiDevice device=UiDevice.getInstance(i);
    private final List<Activity> owned=new CopyOnWriteArrayList<>();
    private String step="setup";

    private void await(java.util.function.BooleanSupplier condition)throws Exception{
        long until=SystemClock.elapsedRealtime()+8000;
        while(!condition.getAsBoolean()&&SystemClock.elapsedRealtime()<until)SystemClock.sleep(25);
        assertTrue(step,condition.getAsBoolean());
    }
    private View focused(){
        for(View root:WindowInspector.getGlobalWindowViews())
            if(root.isAttachedToWindow()&&root.hasWindowFocus()&&root.getContext().getPackageName().equals(c.getPackageName()))return root;
        return null;
    }
    private View find(View root,String label){
        if(root==null)return null;
        if(label.contentEquals(root.getContentDescription()==null?"":root.getContentDescription())||root instanceof TextView text&&label.contentEquals(text.getText()))return root;
        if(root instanceof ViewGroup group)for(int n=0;n<group.getChildCount();n++){View found=find(group.getChildAt(n),label);if(found!=null)return found;}
        return null;
    }
    private boolean shown(String label){boolean[] yes={false};i.runOnMainSync(()->yes[0]=find(focused(),label)!=null);return yes[0];}
    private <T extends Activity>T live(Class<T> type){for(int n=owned.size()-1;n>=0;n--){Activity owner=owned.get(n);if(type.isInstance(owner)&&!owner.isDestroyed()&&!owner.isFinishing())return type.cast(owner);}return null;}
    private View reveal(String label)throws Exception{
        step="visible "+label;await(()->shown(label));View[] root={null};
        await(()->{boolean[] yes={false};i.runOnMainSync(()->{root[0]=focused();View target=find(root[0],label);if(target==null)return;target.requestRectangleOnScreen(new Rect(0,0,target.getWidth(),target.getHeight()),true);Rect visible=new Rect();yes[0]=target.getGlobalVisibleRect(visible)&&visible.width()==target.getWidth()&&visible.height()==target.getHeight();});return yes[0];});
        frame(root[0]);return root[0];
    }
    private void frame(View root)throws Exception{CountDownLatch committed=new CountDownLatch(1);i.runOnMainSync(()->{assertSame(step,root,focused());assertTrue(root.isHardwareAccelerated());root.getViewTreeObserver().registerFrameCommitCallback(committed::countDown);root.invalidate();});assertTrue(step,committed.await(5,TimeUnit.SECONDS));}
    private void click(String label)throws Exception{
        View root=reveal(label);Rect[] bounds={null};i.runOnMainSync(()->{assertSame(root,focused());View target=find(root,label);while(target!=null&&!target.isClickable())target=target.getParent() instanceof View parent?parent:null;assertNotNull(label,target);Rect visible=new Rect();assertTrue(target.getGlobalVisibleRect(visible));assertEquals(target.getWidth(),visible.width());assertEquals(target.getHeight(),visible.height());assertTrue(label,target.isEnabled());assertTrue(label,target.getHeight()>=Design.dp(c,48));int[] position=new int[2];target.getLocationOnScreen(position);bounds[0]=new Rect(position[0],position[1],position[0]+target.getWidth(),position[1]+target.getHeight());step="tap "+label+" at native screen "+bounds[0].toShortString();});
        assertTrue(step,device.click(bounds[0].centerX(),bounds[0].centerY()));
    }
    private File folder(){File dir=new File(c.getExternalFilesDir(null),"verification");assertTrue(dir.isDirectory()||dir.mkdirs());return dir;}
    private void capture(String name,String label)throws Exception{View root=reveal(label);frame(root);assertTrue(device.takeScreenshot(new File(folder(),"interrupted-recovery-"+name+".png")));}
    private <T extends Activity>T recreate(Class<T> type,String label)throws Exception{
        T previous=live(type);assertNotNull(previous);Instrumentation.ActivityMonitor monitor=i.addMonitor(type.getName(),null,false);
        try{i.runOnMainSync(previous::recreate);T next=type.cast(monitor.waitForActivityWithTimeout(8000));assertNotNull(next);await(previous::isDestroyed);await(()->shown(label));return next;}finally{i.removeMonitor(monitor);}
    }
    private <T extends Activity>T launch(Class<T> type,Intent intent,String label)throws Exception{
        Instrumentation.ActivityMonitor monitor=i.addMonitor(type.getName(),null,false);
        try{c.startActivity(intent.putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));T next=type.cast(monitor.waitForActivityWithTimeout(8000));assertNotNull(next);await(()->shown(label));return next;}finally{i.removeMonitor(monitor);}
    }
    private long unfinished(Store store,long start){long id=store.begin();ContentValues values=new ContentValues();values.put("start",start);values.put("active_ms",60000L);store.getWritableDatabase().update("sessions",values,"id=?",new String[]{""+id});store.epoch(id,start,start+60000,.25,null,null,60);return id;}
    private void cloneRaw(Store store,org.json.JSONObject raw){ContentValues values=new ContentValues();raw.keys().forEachRemaining(key->{Object value=raw.opt(key);if(value==org.json.JSONObject.NULL)values.putNull(key);else if(value instanceof Number number)values.put(key,number.doubleValue());else values.put(key,String.valueOf(value));});store.getWritableDatabase().insertOrThrow("sessions",null,values);}
    private void backTo(String label)throws Exception{assertTrue(device.pressBack());await(()->shown(label));}

    @Test public void unfinishedRecoveryReviewKeepSetupAndNamedDiscardRejectReusedIdentity()throws Exception{
        assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertEquals("1",device.executeShellCommand("getprop ro.kernel.qemu").trim());
        assertFalse(TrackingService.running);assertFalse(AlarmService.active);assertFalse(Prefs.flag(c,"tracking"));assertFalse(Prefs.flag(c,"ringing"));assertEquals(0,Prefs.number(c,"checkPending",0));assertNull(SleepSoundService.snapshot());assertFalse(MicrophoneLease.occupied());
        assertFalse("Existing recurring tracking must be idle",RecurringTracking.port.preferences(c).getBoolean("enabled",false));
        String name="interrupted-flow-"+UUID.randomUUID(),database=name+".db";SharedPreferences plan=c.getSharedPreferences(name+"-plan",0),delay=c.getSharedPreferences(name+"-delay",0);
        Records.Factory priorRecords=Records.factory;SleepPlans.Factory priorPlan=SleepPlans.factory;TrackingDelaySettings.Factory priorDelay=TrackingDelaySettings.factory;TrackingActivity.Commands priorCommands=TrackingActivity.commands;TrackingService.StoreFactory priorStores=TrackingActivity.stores;WakeChecks.Factory priorWake=WakeChecks.factory;AlarmScheduling.Port priorScheduling=AlarmScheduling.port;
        Map<String,?> personal=new HashMap<>(Prefs.of(c).getAll());java.util.concurrent.atomic.AtomicInteger starts=new java.util.concurrent.atomic.AtomicInteger();Application app=(Application)c.getApplicationContext();
        Application.ActivityLifecycleCallbacks life=new Application.ActivityLifecycleCallbacks(){public void onActivityCreated(Activity a,Bundle b){owned.add(a);assertTrue("Only actual local recovery owners",a instanceof MainActivity||a instanceof InterruptedRecordActivity||a instanceof RecordReportActivity||a instanceof RecordEditorActivity||a instanceof TrackingActivity);}public void onActivityStarted(Activity a){}public void onActivityResumed(Activity a){}public void onActivityPaused(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,Bundle b){}public void onActivityDestroyed(Activity a){}};
        app.registerActivityLifecycleCallbacks(life);
        try{
            Records.factory=context->new Store(context,database);SleepPlans.factory=context->plan;TrackingDelaySettings.factory=context->delay;TrackingActivity.stores=context->new Store(context,database);WakeChecks.factory=new AlarmToneFlowTest.InertWakeChecks();
            TrackingActivity.commands=new TrackingActivity.Commands(){public TrackingService.Snapshot snapshot(){return null;}public void start(TrackingActivity owner,Intent request){starts.incrementAndGet();throw new AssertionError("No recording is authorized by a recovery handoff");}public void command(TrackingActivity owner,Intent request){throw new AssertionError("No recording command");}public void review(TrackingActivity owner){throw new AssertionError("No service-owned report handoff");}};
            AlarmScheduling.port=new AlarmScheduling.Port(){public boolean allowed(Context context){return true;}public void schedule(Context context,Alarm alarm){throw new AssertionError("Recovery cannot schedule an alarm");}};
            long start=System.currentTimeMillis()-600000,id;String beforeUpgrade,epochsBefore,alarmsBefore;
            try(Store original=new Store(c,database)){id=unfinished(original,start);Alarm alarm=new Alarm(740001);alarm.label="Unchanged recovery fixture alarm";original.save(alarm);beforeUpgrade=original.rows("SELECT * FROM sessions").toString();epochsBefore=original.rows("SELECT * FROM epochs").toString();alarmsBefore=original.rows("SELECT * FROM alarms").toString();}
            // Exact v17 schema fixture: v18 adds only these two local UX objects, no record columns.
            try(android.database.sqlite.SQLiteDatabase old=android.database.sqlite.SQLiteDatabase.openDatabase(c.getDatabasePath(database).getPath(),null,android.database.sqlite.SQLiteDatabase.OPEN_READWRITE)){old.execSQL("DROP TRIGGER forget_interrupted_review");old.execSQL("DROP TABLE interrupted_reviews");old.setVersion(17);}
            try(Store upgraded=new Store(c,database)){assertEquals(18,upgraded.getReadableDatabase().getVersion());assertEquals("17→18 does not rewrite existing records",beforeUpgrade,upgraded.rows("SELECT * FROM sessions").toString());assertEquals(epochsBefore,upgraded.rows("SELECT * FROM epochs").toString());assertEquals(alarmsBefore,upgraded.rows("SELECT * FROM alarms").toString());assertTrue(upgraded.rows("SELECT * FROM interrupted_reviews").isEmpty());assertEquals("Portable backup schema remains separate",14,Backup.FORMAT);}
            launch(MainActivity.class,new Intent(c,MainActivity.class),"Interrupted record retained");capture("tonight","Interrupted record retained");
            String token,retained;try(Store store=new Store(c,database)){InterruptedRecords.Receipt receipt=InterruptedRecords.pending(store);assertNotNull(receipt);assertEquals(id,receipt.id);assertEquals(start+60000,receipt.raw.optLong("end"));assertEquals(60000,receipt.raw.optLong("active_ms"));assertTrue("Missing tail is not filled",receipt.raw.optLong("end")<System.currentTimeMillis()-300000);assertEquals(1,receipt.raw.optInt("interrupted"));token=receipt.token;retained=InterruptedRecords.fingerprint(receipt.raw);assertEquals(epochsBefore,store.rows("SELECT * FROM epochs").toString());}
            click("Interrupted record retained");capture("review","Last retained time");recreate(InterruptedRecordActivity.class,"Your saved portion is kept");capture("review-restored","Last retained time");
            click("Review retained record");await(()->shown("Recorded movement"));i.runOnMainSync(()->{assertNull("Recovery report is read-only",find(focused(),"Edit sleep"));assertNull("No record-menu escape",find(focused(),"Report options"));assertNull("No unguarded signals escape",find(focused(),"Explore recorded signals"));assertNull("No audio-playback route",find(focused(),"View sound events"));});capture("retained-report","Recorded movement");recreate(RecordReportActivity.class,"Recorded movement");capture("retained-report-restored","Recorded movement");backTo("Your saved portion is kept");
            click("Start a new night");await(()->shown("Settle in"));capture("manual-setup","Start tracking  →");assertEquals(0,starts.get());assertFalse(TrackingService.running);backTo("Your saved portion is kept");capture("manual-returned","Review retained record");
            InterruptedRecordActivity owner=live(InterruptedRecordActivity.class);i.runOnMainSync(owner::finish);await(owner::isDestroyed);await(()->shown("Interrupted record retained"));click("Interrupted record retained");capture("reopened","Your saved portion is kept");click("Keep retained record");await(()->live(InterruptedRecordActivity.class)==null&&shown("Tonight"));
            try(Store store=new Store(c,database)){assertEquals(retained,InterruptedRecords.fingerprint(store.rows("SELECT * FROM sessions WHERE id=?",""+id).get(0)));assertNull(InterruptedRecords.pending(store));assertEquals(epochsBefore,store.rows("SELECT * FROM epochs").toString());}
            launch(InterruptedRecordActivity.class,new Intent(c,InterruptedRecordActivity.class).putExtra(InterruptedRecords.EXTRA,token),InterruptedRecords.unavailable().getMessage());capture("kept-token-retired",InterruptedRecords.unavailable().getMessage());backTo("Tonight");
            long second;try(Store store=new Store(c,database)){second=unfinished(store,start+120000);}recreate(MainActivity.class,"Interrupted record retained");click("Interrupted record retained");await(()->shown("Your saved portion is kept"));click("Review discard");String named="Delete retained record "+second+"?";await(()->shown(named));capture("discard-review",named);
            View[] retiredDelete={null};i.runOnMainSync(()->retiredDelete[0]=find(focused(),"Delete record"));assertNotNull(retiredDelete[0]);recreate(RecordEditorActivity.class,named);i.runOnMainSync(()->retiredDelete[0].performClick());capture("discard-restored",named);try(Store store=new Store(c,database)){assertNotNull(InterruptedRecords.pending(store));assertEquals(2,store.rows("SELECT id FROM sessions").size());}click("Keep record");await(()->live(RecordEditorActivity.class)==null&&shown("Your saved portion is kept"));
            click("Review discard");await(()->shown(named));org.json.JSONObject replacement;try(Store store=new Store(c,database)){replacement=store.rows("SELECT * FROM sessions WHERE id=?",""+second).get(0);store.getWritableDatabase().delete("sessions","id=?",new String[]{""+second});cloneRaw(store,replacement);assertTrue("Session delete retires token even for an identical raw reused ID",store.rows("SELECT * FROM interrupted_reviews WHERE session=?",""+second).isEmpty());}
            click("Delete record");String stale="Could not delete. "+InterruptedRecords.unavailable().getMessage();await(()->shown(stale));capture("stale-discard-rejected",stale);try(Store store=new Store(c,database)){assertEquals(InterruptedRecords.fingerprint(replacement),InterruptedRecords.fingerprint(store.rows("SELECT * FROM sessions WHERE id=?",""+second).get(0)));assertEquals(retained,InterruptedRecords.fingerprint(store.rows("SELECT * FROM sessions WHERE id=?",""+id).get(0)));}
            backTo(InterruptedRecords.unavailable().getMessage());capture("stale-review",InterruptedRecords.unavailable().getMessage());backTo("Tonight");
            long third;try(Store store=new Store(c,database)){third=unfinished(store,start+240000);}recreate(MainActivity.class,"Interrupted record retained");click("Interrupted record retained");await(()->shown("Your saved portion is kept"));click("Review discard");String thirdName="Delete retained record "+third+"?";await(()->shown(thirdName));capture("discard-final",thirdName);click("Delete record");await(()->live(RecordEditorActivity.class)==null&&shown(InterruptedRecords.unavailable().getMessage()));capture("discarded",InterruptedRecords.unavailable().getMessage());
            try(Store store=new Store(c,database)){assertTrue(store.rows("SELECT id FROM sessions WHERE id=?",""+third).isEmpty());assertTrue(store.rows("SELECT id FROM epochs WHERE session=?",""+third).isEmpty());assertEquals(2,store.rows("SELECT id FROM sessions").size());assertEquals(alarmsBefore,store.rows("SELECT * FROM alarms").toString());unfinished(store,start+360000);store.recoverInterrupted();assertNotNull(InterruptedRecords.pending(store));store.eraseHistory();assertTrue("Existing all-history erase invalidates every receipt",store.rows("SELECT * FROM interrupted_reviews").isEmpty());assertTrue(store.rows("SELECT id FROM sessions").isEmpty());assertEquals(alarmsBefore,store.rows("SELECT * FROM alarms").toString());}
            assertEquals(0,starts.get());assertFalse(TrackingService.running);assertFalse(AlarmService.active);assertNull(SleepSoundService.snapshot());assertFalse(MicrophoneLease.occupied());
        }catch(Exception|AssertionError failure){try{device.takeScreenshot(new File(folder(),"interrupted-recovery-failure.png"));device.dumpWindowHierarchy(new File(folder(),"interrupted-recovery-failure.xml"));try(Writer writer=new FileWriter(new File(folder(),"interrupted-recovery-failure.txt"))){writer.write(step+"\n"+failure);}}catch(Exception diagnostic){failure.addSuppressed(diagnostic);}throw failure;}
        finally{try{i.runOnMainSync(()->{for(Activity owner:owned)if(!owner.isDestroyed())owner.finish();});await(()->owned.stream().allMatch(Activity::isDestroyed));for(Activity owner:owned){if(owner instanceof InterruptedRecordActivity recovery)assertTrue(recovery.awaitReaders(10000));else if(owner instanceof RecordReportActivity report)report.awaitReaders(10000);else if(owner instanceof RecordEditorActivity editor)editor.awaitReaders(10000);}TrackingDelayRuntime.drain(TrackingDelaySettings.factory);TrackingDelayRuntime.forget(TrackingDelaySettings.factory);HomeSurfaces.drain(10000);
            // Existing Tracking setup owns only these idle bookkeeping flags. Restore their prior presence.
            Map<String,Object> actual=new HashMap<>(Prefs.of(c).getAll()),expected=new HashMap<>(personal);SharedPreferences.Editor flags=Prefs.of(c).edit();for(String key:List.of("tracking","trackingPaused")){assertFalse(Prefs.of(c).getBoolean(key,false));actual.remove(key);expected.remove(key);Object value=personal.get(key);if(value==null)flags.remove(key);else{assertTrue(value instanceof Boolean);flags.putBoolean(key,(Boolean)value);}}assertEquals("No unrelated preference changes",expected,actual);assertTrue(flags.commit());assertEquals(personal,Prefs.of(c).getAll());assertFalse(TrackingService.running);assertFalse(AlarmService.active);
        }finally{app.unregisterActivityLifecycleCallbacks(life);Records.factory=priorRecords;SleepPlans.factory=priorPlan;TrackingDelaySettings.factory=priorDelay;TrackingActivity.commands=priorCommands;TrackingActivity.stores=priorStores;WakeChecks.factory=priorWake;AlarmScheduling.port=priorScheduling;if(owned.stream().allMatch(Activity::isDestroyed)){assertTrue(c.deleteDatabase(database));assertTrue(c.deleteSharedPreferences(name+"-plan"));assertTrue(c.deleteSharedPreferences(name+"-delay"));}}}
    }
}
