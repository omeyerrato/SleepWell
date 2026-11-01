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

/** Real Trip -> occurrence review -> alarm draft -> SQLite Save. Only scheduling is injected. */
public final class TripOccurrenceFlowTest {
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
        await(()->{boolean[] yes={false};i.runOnMainSync(()->{root[0]=focused();View target=find(root[0],label);if(target==null)return;target.requestRectangleOnScreen(new Rect(0,0,target.getWidth(),target.getHeight()),true);Rect visible=new Rect();yes[0]=target.isEnabled()&&target.getGlobalVisibleRect(visible)&&visible.width()==target.getWidth()&&visible.height()==target.getHeight();});return yes[0];});
        frame(root[0]);return root[0];
    }
    private void frame(View root)throws Exception{CountDownLatch committed=new CountDownLatch(1);i.runOnMainSync(()->{assertSame(step,root,focused());assertTrue(root.isHardwareAccelerated());root.getViewTreeObserver().registerFrameCommitCallback(committed::countDown);root.invalidate();});assertTrue(step,committed.await(5,TimeUnit.SECONDS));}
    private void click(String label)throws Exception{
        View root=reveal(label);Rect[] bounds={null};i.runOnMainSync(()->{assertSame(root,focused());View target=find(root,label);while(target!=null&&!target.isClickable())target=target.getParent() instanceof View parent?parent:null;assertNotNull(label,target);Rect visible=new Rect();assertTrue(target.getGlobalVisibleRect(visible));assertEquals(target.getWidth(),visible.width());assertEquals(target.getHeight(),visible.height());assertTrue(label,target.isEnabled());assertTrue(label,target.getHeight()>=Design.dp(c,48));int[] origin=new int[2];target.getLocationOnScreen(origin);bounds[0]=new Rect(origin[0],origin[1],origin[0]+target.getWidth(),origin[1]+target.getHeight());});
        assertTrue(step,device.click(bounds[0].centerX(),bounds[0].centerY()));
    }
    private File folder(){File dir=new File(c.getExternalFilesDir(null),"verification");assertTrue(dir.isDirectory()||dir.mkdirs());return dir;}
    private void capture(String name,String label)throws Exception{View root=reveal(label);frame(root);assertTrue(device.takeScreenshot(new File(folder(),"trip-occurrence-"+name+".png")));}
    private <T extends Activity>T recreate(Class<T> type,String label)throws Exception{
        T previous=live(type);assertNotNull(previous);Instrumentation.ActivityMonitor monitor=i.addMonitor(type.getName(),null,false);
        try{i.runOnMainSync(previous::recreate);T next=type.cast(monitor.waitForActivityWithTimeout(8000));assertNotNull(next);await(previous::isDestroyed);await(()->shown(label));return next;}finally{i.removeMonitor(monitor);}
    }
    @SuppressWarnings("unchecked") private <T>T field(Object owner,String name){try{java.lang.reflect.Field value=owner.getClass().getDeclaredField(name);value.setAccessible(true);return (T)value.get(owner);}catch(ReflectiveOperationException missing){throw new AssertionError(name,missing);}}
    private String reviewKey(NextAlarmOverrideActivity owner){NextAlarmOverride value=field(owner,"reviewed");assertNotNull(value);return value.key();}
    private void currentReview(NextAlarmOverrideActivity expected,View use,String key,String parentDraft){
        assertSame("Retired callback must not finish or replace the current review",expected,live(NextAlarmOverrideActivity.class));assertFalse(expected.isDestroyed());assertFalse(expected.isFinishing());assertNotNull("Current reviewed positive",use);assertTrue("The current review owns focus",expected.hasWindowFocus());assertSame(expected.getWindow().getDecorView(),use.getRootView());assertTrue("The current reviewed positive stays attached",use.isAttachedToWindow());assertTrue(use.isEnabled());assertEquals("Displayed immutable proposal remains reviewed",key,reviewKey(expected));Alarm parent=field(live(AlarmEditorActivity.class),"alarm");assertEquals("Retired callback cannot transfer a result into the parent draft",parentDraft,parent.json().toString());
    }
    private void enterReview(String alarmLabel)throws Exception{click("Apply once to an existing alarm");click(alarmLabel);await(()->shown("Then returns to recurrence"));}
    private void useReviewed()throws Exception{click("Review next time");await(()->shown("Reviewed change"));click("Use next time");await(()->shown("Save alarm"));}

    /** Existing Records.Factory seam gates the real final SQLite write; no product flags or outputs. */
    private final class PlanWriteRace {
        final String database;final java.util.concurrent.atomic.AtomicBoolean armed=new java.util.concurrent.atomic.AtomicBoolean();
        final CountDownLatch attempted=new CountDownLatch(1),acquired=new CountDownLatch(1),done=new CountDownLatch(1);
        final java.util.concurrent.atomic.AtomicReference<Throwable> failure=new java.util.concurrent.atomic.AtomicReference<>();
        Thread writer;long alarmId,target;TripSleepPlan replacement;TravelPlan trip;Map<String,?> before;boolean blockedAtCommit;
        PlanWriteRace(String database){this.database=database;}
        void arm(Alarm original,long target,TripSleepPlan saved,TravelPlan trip,Map<String,?> before){this.alarmId=original.id;this.target=target;this.replacement=saved.copy();replacement.step=saved.step==15?30:15;this.trip=trip.copy();this.before=before;armed.set(true);}
        void beforeSave(Alarm candidate){
            if(candidate.id!=alarmId||candidate.nextOverride==null||!armed.compareAndSet(true,false))return;
            assertTrue("Final alarm mutation owns Scheduler first",Thread.holdsLock(Scheduler.class));
            assertTrue("Final alarm mutation retains the shared trip lock",Thread.holdsLock(TravelPlans.LOCK));
            writer=new Thread(()->{try{attempted.countDown();synchronized(TravelPlans.LOCK){acquired.countDown();try(Store committed=new Store(c,database)){Alarm actual=committed.alarm((int)alarmId);assertNotNull(actual);assertNotNull("Plan writer can enter only after the actual override committed",actual.nextOverride);assertEquals(target,actual.nextOverride.at(ZoneId.systemDefault()));}assertEquals("Original reviewed plan stays intact through alarm commit",before,new TripSleepStore(c).prefs.getAll());new TripSleepStore(c).save(replacement,trip);}}catch(Throwable error){failure.set(error);}finally{done.countDown();}},"private-trip-plan-writer");
            writer.start();try{assertTrue("Competing plan writer reaches the lock",attempted.await(3,TimeUnit.SECONDS));long until=SystemClock.elapsedRealtime()+3000;while(writer.getState()!=Thread.State.BLOCKED&&done.getCount()!=0&&SystemClock.elapsedRealtime()<until)SystemClock.sleep(5);assertEquals("Trip writer is blocked after final admission and before SQLite save",Thread.State.BLOCKED,writer.getState());assertEquals("Writer has not entered the trip owner",1L,acquired.getCount());blockedAtCommit=true;}catch(InterruptedException interrupted){Thread.currentThread().interrupt();throw new AssertionError(interrupted);}
        }
        void complete()throws Exception{assertTrue("Bounded plan writer finishes after commit",done.await(8,TimeUnit.SECONDS));if(failure.get()!=null)throw new AssertionError("Competing plan write failed",failure.get());assertTrue(blockedAtCommit);assertEquals(0L,acquired.getCount());}
        void close()throws Exception{if(writer!=null){writer.join(8000);assertFalse("Private writer is drained before fixture cleanup",writer.isAlive());}}
    }
    private void linkedRejected(TripAlarmShift prior,TripSleepPlan plan,Alarm original,Alarm linked,long target,Store store){
        assertFalse(TripAlarmShift.ordinary(linked));
        try{TripAlarmShift.prepare(c,plan,linked,target,store);fail("Linked or invalid weekly profile cannot prepare a travel shift");}catch(IllegalArgumentException expected){assertEquals(TripAlarmShift.LINKED_MESSAGE,expected.getMessage());}
        try{prior.check(c,linked,store);fail("Retained proposal cannot admit a linked weekly alarm");}catch(IllegalArgumentException expected){assertEquals(TripAlarmShift.LINKED_MESSAGE,expected.getMessage());}
        try{prior.checkDraft(c,original,linked,store);fail("Travel draft cannot introduce linked profile ownership");}catch(IllegalArgumentException expected){assertEquals(TripAlarmShift.LINKED_MESSAGE,expected.getMessage());}
    }
    @Test public void savedTripOccurrenceReviewKeepStaleAndSavePreserveRecurringAlarm()throws Exception{
        assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));
        assertEquals("1",device.executeShellCommand("getprop ro.kernel.qemu").trim());
        assertFalse(AlarmService.active);assertFalse(TrackingService.running);assertFalse(Prefs.flag(c,"ringing"));assertFalse(Prefs.flag(c,"tracking"));assertEquals("No pending legacy wake check",0,Prefs.number(c,"checkPending",0));
        String name="trip-occurrence-"+UUID.randomUUID(),database=name+".db";
        SharedPreferences travel=c.getSharedPreferences(name+"-travel",0),sleep=c.getSharedPreferences(name+"-sleep",0),calendar=c.getSharedPreferences(name+"-calendar",0);
        Records.Factory oldRecords=Records.factory;TravelPlans.Factory oldTravel=TravelPlans.factory;
        java.util.function.Function<Context,SharedPreferences> oldSleep=TripSleepStore.factory,oldCalendar=CalendarSettings.factory;
        AlarmScheduling.Port oldSchedule=AlarmScheduling.port;WakeChecks.Factory oldWake=WakeChecks.factory;
        Map<String,?> personal=new HashMap<>(Prefs.of(c).getAll());List<Alarm> scheduled=new CopyOnWriteArrayList<>();PlanWriteRace race=new PlanWriteRace(database);
        Application app=(Application)c.getApplicationContext();
        Application.ActivityLifecycleCallbacks life=new Application.ActivityLifecycleCallbacks(){public void onActivityCreated(Activity a,Bundle b){owned.add(a);assertTrue("Only local trip and its actual alarm review/editor",a instanceof TripSleepActivity||a instanceof NextAlarmOverrideActivity||a instanceof AlarmEditorActivity);}public void onActivityStarted(Activity a){}public void onActivityResumed(Activity a){}public void onActivityPaused(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,Bundle b){}public void onActivityDestroyed(Activity a){}};
        app.registerActivityLifecycleCallbacks(life);
        try(Store store=new Store(c,database)){
            Records.factory=context->new Store(context,database){@Override public void save(Alarm request){race.beforeSave(request);super.save(request);}};TravelPlans.factory=context->travel;TripSleepStore.factory=context->sleep;CalendarSettings.factory=context->calendar;
            WakeChecks.factory=new AlarmToneFlowTest.InertWakeChecks();
            AlarmScheduling.port=new AlarmScheduling.Port(){public boolean allowed(Context context){return true;}public void schedule(Context context,Alarm request){try(Store read=new Store(context,database)){assertNotNull("Override committed before scheduling port",read.alarm(request.id).nextOverride);assertEquals(read.alarm(request.id).json().toString(),request.json().toString());scheduled.add(Alarm.read(request.json()));}}};
            long pinnedNow=System.currentTimeMillis();ZoneId zone=ZoneId.systemDefault();LocalDate tomorrow=Instant.ofEpochMilli(pinnedNow).atZone(zone).toLocalDate().plusDays(1);
            TravelPlan trip=new TravelPlan(zone.getId(),tomorrow);trip.destination=zone.getId().equals("Europe/London")?"Asia/Riyadh":"Europe/London";TravelPlans.repository(c).save(trip,pinnedNow);
            TripSleepPlan plan=TripSleepPlan.initial(trip,new SleepPlan());plan.days=1;plan.bed=22*60;plan.wake=7*60;plan.earlier=true;plan.reminders=false;new TripSleepStore(c).save(plan,trip);
            Alarm original=new Alarm(740001);while(personal.containsKey("lastOccurrence_"+original.id)||personal.containsKey("deadline_"+original.id))original.id++;
            original.label="Travel recurring fixture";original.enabled=true;original.hour=7;original.minute=0;original.days.add(tomorrow.getDayOfWeek().getValue());store.save(original);
            Alarm linked=Alarm.read(original.json());linked.id=original.id+1;while(personal.containsKey("lastOccurrence_"+linked.id)||personal.containsKey("deadline_"+linked.id))linked.id++;linked.label="Linked weekly Nap fixture";linked.weeklyNapOwner=UUID.randomUUID().toString();store.save(linked);
            Alarm invalidLinked=Alarm.read(original.json());invalidLinked.id=linked.id+1;while(personal.containsKey("lastOccurrence_"+invalidLinked.id)||personal.containsKey("deadline_"+invalidLinked.id))invalidLinked.id++;invalidLinked.label="Invalid weekly Nap fixture";invalidLinked=Alarm.read(invalidLinked.json().put("weeklyNapOwner",7));store.save(invalidLinked);String linkedBefore=linked.json().toString(),invalidLinkedBefore=invalidLinked.json().toString();
            TripAlarmShift prior=TripAlarmShift.prepare(c,plan,original,plan.nights().get(0).wake,store);linkedRejected(prior,plan,original,linked,prior.target,store);linkedRejected(prior,plan,original,invalidLinked,prior.target,store);
            String baseline=original.json().toString();Map<String,?> tripBefore=new HashMap<>(travel.getAll()),sleepBefore=new HashMap<>(sleep.getAll());
            assertEquals(0,Prefs.of(c).getLong("lastOccurrence_"+original.id,0));
            long source=NextAlarmOverrides.source(c,original,pinnedNow,zone),following=original.nextOrdinary(source,zone);
            assertTrue(source>pinnedNow+60000);assertTrue(plan.nights().get(0).wake>pinnedNow+60000);
            Instrumentation.ActivityMonitor monitor=i.addMonitor(TripSleepActivity.class.getName(),null,false);
            try{c.startActivity(new Intent(c,TripSleepActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));assertNotNull(monitor.waitForActivityWithTimeout(8000));}finally{i.removeMonitor(monitor);}
            capture("plan","Create wake-up alarm draft");click("Apply once to an existing alarm");capture("choices",original.label);assertFalse("Linked weekly Nap alarm is not offered",shown(linked.label));assertFalse("Invalid weekly Nap ownership is not offered",shown(invalidLinked.label));capture("linked-excluded",original.label);
            recreate(TripSleepActivity.class,original.label);capture("choices-restored",original.label);click("Keep current alarms");await(()->shown("Apply once to an existing alarm"));
            assertEquals(baseline,store.alarm(original.id).json().toString());assertEquals(sleepBefore,sleep.getAll());assertTrue(scheduled.isEmpty());capture("kept","Apply once to an existing alarm");
            enterReview(original.label);capture("proposal","Review next time");View[] detachedReview={null};i.runOnMainSync(()->detachedReview[0]=find(focused(),"Review next time"));assertNotNull(detachedReview[0]);click("Review next time");capture("reviewed","Use next time");
            NextAlarmOverrideActivity sameOwner=live(NextAlarmOverrideActivity.class);View[] detachedUse={null};String[] displayedKey={null};i.runOnMainSync(()->{detachedUse[0]=find(focused(),"Use next time");displayedKey[0]=reviewKey(sameOwner);assertFalse(detachedReview[0].isAttachedToWindow());assertTrue("Invoke the detached Review callback itself",detachedReview[0].performClick());currentReview(sameOwner,detachedUse[0],displayedKey[0],baseline);});
            click("Review next time");View[] currentUse={null};i.runOnMainSync(()->{currentUse[0]=find(focused(),"Use next time");assertFalse(detachedUse[0].isAttachedToWindow());assertTrue("Invoke the detached positive callback itself",detachedUse[0].performClick());currentReview(sameOwner,currentUse[0],displayedKey[0],baseline);});
            View[] retiredUse={null};i.runOnMainSync(()->retiredUse[0]=find(focused(),"Use next time"));assertNotNull(retiredUse[0]);
            NextAlarmOverrideActivity restoredOwner=recreate(NextAlarmOverrideActivity.class,"Reviewed change");i.runOnMainSync(()->retiredUse[0].performClick());i.runOnMainSync(()->{assertTrue("Original owner is retired",sameOwner.isDestroyed());assertFalse(retiredUse[0].isAttachedToWindow());currentReview(restoredOwner,find(focused(),"Use next time"),displayedKey[0],baseline);});capture("reviewed-restored","Use next time");
            assertEquals(baseline,store.alarm(original.id).json().toString());assertTrue(scheduled.isEmpty());click("Keep current alarm");await(()->live(AlarmEditorActivity.class)==null&&shown("Apply once to an existing alarm"));capture("cancelled","Apply once to an existing alarm");
            enterReview(original.label);useReviewed();capture("draft","Save alarm");assertEquals(baseline,store.alarm(original.id).json().toString());assertTrue(scheduled.isEmpty());
            recreate(AlarmEditorActivity.class,"Save alarm");capture("draft-restored","Save alarm");assertEquals(baseline,store.alarm(original.id).json().toString());
            TripSleepPlan changed=new TripSleepStore(c).load();changed.step=15;new TripSleepStore(c).save(changed,trip);
            click("Save alarm");String stale="The saved trip or sleep plan changed. Review it before applying a shift.";await(()->shown(stale));capture("stale-plan",stale);
            assertEquals(baseline,store.alarm(original.id).json().toString());assertTrue(scheduled.isEmpty());assertTrue(device.pressBack());await(()->shown("Save alarm changes?"));click("Discard");await(()->live(AlarmEditorActivity.class)==null&&shown("Apply once to an existing alarm"));
            TripSleepActivity old=live(TripSleepActivity.class);i.runOnMainSync(old::finish);await(old::isDestroyed);
            Instrumentation.ActivityMonitor fresh=i.addMonitor(TripSleepActivity.class.getName(),null,false);try{c.startActivity(new Intent(c,TripSleepActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));assertNotNull(fresh.waitForActivityWithTimeout(8000));}finally{i.removeMonitor(fresh);}
            click("Apply once to an existing alarm");await(()->shown(original.label));Alarm external=Alarm.read(original.json());external.label="Changed outside travel";store.save(external);click(original.label);
            String alarmChanged="The saved alarm changed. Reopen its travel occurrence.";await(()->shown(alarmChanged));capture("stale-alarm",alarmChanged);assertNull(live(AlarmEditorActivity.class));assertTrue(scheduled.isEmpty());assertEquals(external.json().toString(),store.alarm(original.id).json().toString());store.save(original);
            Map<String,?> reviewedPlan=new HashMap<>(sleep.getAll());long target=new TripSleepStore(c).load().nights().get(0).wake;
            enterReview(original.label);useReviewed();capture("ready-to-save","Save alarm");race.arm(original,target,new TripSleepStore(c).load(),trip,reviewedPlan);click("Save alarm");await(()->live(AlarmEditorActivity.class)==null&&scheduled.size()==1&&shown("Apply once to an existing alarm"));
            race.complete();
            Alarm saved=store.alarm(original.id);assertNotNull(saved.nextOverride);assertEquals(target,saved.nextOverride.at(zone));assertEquals(source,saved.nextOverride.sourceAt(saved,zone));assertEquals(NextAlarmOverride.baseKey(original),NextAlarmOverride.baseKey(saved));
            assertEquals(following,saved.next(target,zone));assertFalse(saved.occurs(source,zone));assertTrue(saved.occurs(following,zone));Alarm recurrence=Alarm.read(saved.json());recurrence.setNextOverride(null);assertEquals("All ordinary alarm configuration stays saved",baseline,recurrence.json().toString());
            assertEquals(target,scheduled.get(0).nextOverride.at(zone));assertTrue("A deliberate later plan write gets its own new revision",new TripSleepStore(c).load().revision>plan.revision);assertEquals(race.replacement.json().toString(),new TripSleepStore(c).load().json().toString());assertEquals(tripBefore,travel.getAll());assertEquals("Linked profile alarm retains its exact ownership and recurrence",linkedBefore,store.alarm(linked.id).json().toString());assertEquals("Invalid profile metadata is retained, not detached",invalidLinkedBefore,store.alarm(invalidLinked.id).json().toString());assertTrue(store.rows("SELECT id FROM sessions").isEmpty());capture("saved","Apply once to an existing alarm");
            click("Apply once to an existing alarm");String changedAfterCommit="The trip, saved sleep schedule, clock or phone zone changed. Review and save the plan before choosing an occurrence.";await(()->shown(changedAfterCommit));capture("post-commit-plan-changed",changedAfterCommit);assertEquals("Later plan edit never reschedules the saved override",1,scheduled.size());assertEquals(saved.json().toString(),store.alarm(original.id).json().toString());
            TripSleepActivity staleOwner=live(TripSleepActivity.class);click("Back");await(staleOwner::isDestroyed);Instrumentation.ActivityMonitor reopen=i.addMonitor(TripSleepActivity.class.getName(),null,false);TripSleepActivity refreshedPlan;try{c.startActivity(new Intent(c,TripSleepActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));refreshedPlan=(TripSleepActivity)reopen.waitForActivityWithTimeout(8000);assertNotNull(refreshedPlan);}finally{i.removeMonitor(reopen);}await(()->shown("Apply once to an existing alarm"));i.runOnMainSync(()->{TripSleepPlan displayed=field(refreshedPlan,"draft");try{assertEquals("A fresh real trip owner reloads the later saved plan",race.replacement.json().toString(),displayed.json().toString());}catch(org.json.JSONException invalid){throw new AssertionError(invalid);}});
            click("Apply once to an existing alarm");String noEligible="No eligible ordinary recurring occurrence can take this wake time. Use an enabled saved ordinary alarm with no skip or next-time override; the target must be before its following occurrence. Linked Nap profile alarms stay managed in Nap profiles. You can still create a dated alarm draft.";await(()->shown(noEligible));capture("no-eligible",noEligible);assertTrue(shown("Create wake-up alarm draft"));assertEquals(1,scheduled.size());assertEquals(personal,Prefs.of(c).getAll());try(Writer receipt=new OutputStreamWriter(new FileOutputStream(new File(folder(),"trip-occurrence-admission-proof.json")),java.nio.charset.StandardCharsets.UTF_8)){receipt.write(new org.json.JSONObject().put("retired_activity_positive_cannot_finish_recreated_review",true).put("detached_current_render_review_and_use_rejected",true).put("retired_callbacks_never_transfer_parent_result",true).put("owned_dialog_targets_use_actual_screen_coordinates",true).put("linked_valid_and_invalid_excluded",true).put("linked_profile_json_unchanged",true).put("explicit_stale_plan_rejected_before_save",true).put("scheduler_and_trip_locks_held_at_sqlite_write",true).put("competing_plan_writer_blocked_before_alarm_commit",race.blockedAtCommit).put("competing_plan_writer_entered_after_committed_override",true).put("post_commit_stale_plan_rejected_before_listing",true).put("fresh_owner_reloaded_saved_plan_before_no_eligible_review",true).put("ordinary_recurrence_preserved",true).put("scheduling_port_calls",scheduled.size()).put("physical_outputs",false).toString(2));}
        }catch(Exception|AssertionError failure){try{device.takeScreenshot(new File(folder(),"trip-occurrence-failure.png"));device.dumpWindowHierarchy(new File(folder(),"trip-occurrence-failure.xml"));try(Writer writer=new FileWriter(new File(folder(),"trip-occurrence-failure.txt"))){writer.write(step+"\n"+failure);}}catch(Exception diagnostic){failure.addSuppressed(diagnostic);}throw failure;}
        finally{try{race.close();i.runOnMainSync(()->{for(Activity owner:owned)if(!owner.isDestroyed())owner.finish();});await(()->owned.stream().allMatch(Activity::isDestroyed));for(Activity owner:owned)if(owner instanceof TripSleepActivity tripOwner)assertTrue(tripOwner.awaitReaders(8000));assertEquals(personal,Prefs.of(c).getAll());assertFalse(AlarmService.active);assertFalse(TrackingService.running);}finally{app.unregisterActivityLifecycleCallbacks(life);Records.factory=oldRecords;TravelPlans.factory=oldTravel;TripSleepStore.factory=oldSleep;CalendarSettings.factory=oldCalendar;AlarmScheduling.port=oldSchedule;WakeChecks.factory=oldWake;if(owned.stream().allMatch(Activity::isDestroyed)){assertTrue(c.deleteDatabase(database));for(String suffix:new String[]{"-travel","-sleep","-calendar"})assertTrue(c.deleteSharedPreferences(name+suffix));}}}
    }
}
