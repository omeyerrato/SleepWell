package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.graphics.Rect;
import android.os.*;
import android.view.*;
import android.view.inspector.WindowInspector;
import android.widget.*;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.*;
import org.junit.Test;
import java.io.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

/** Private records/plans only, real owned controls, no reminder or runtime delivery. */
public final class DatedGoalBankFlowTest {
 final Instrumentation i=InstrumentationRegistry.getInstrumentation();final Context c=i.getTargetContext();final UiDevice d=UiDevice.getInstance(i);
 final List<Activity> owned=new CopyOnWriteArrayList<>();Activity current;String db,prefs;LocalDate today;ZoneId zone;int cutoff;final AtomicInteger deliveries=new AtomicInteger();
 interface Task {void run()throws Exception;}
 void fixture(Task task)throws Exception{
  assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertEquals("1",d.executeShellCommand("getprop ro.kernel.qemu").trim());assertFalse(AlarmService.active);assertFalse(TrackingService.running);assertFalse(Prefs.flag(c,"ringing"));
  String original;try(Store store=new Store(c)){original=BackupFingerprint.read(store);}Map<String,?> originalPrefs=new HashMap<>(Prefs.of(c).getAll());
  db="dated-goal-"+UUID.randomUUID()+".db";prefs="dated-goal-"+UUID.randomUUID();SharedPreferences privatePrefs=c.getSharedPreferences(prefs,0);assertTrue(privatePrefs.edit().putInt("goal",600).commit());
  SleepPlans.Factory savedPlans=SleepPlans.factory;Records.Factory savedRecords=Records.factory;SleepPlans.Reminder savedReminder=SleepPlans.reminders;
  SleepPlans.factory=context->privatePrefs;Records.factory=context->new Store(context,db);SleepPlans.reminders=context->deliveries.incrementAndGet();
  zone=ZoneId.systemDefault();cutoff=Math.max(0,Math.min(1439,Prefs.number(c,"statsCutoff",0)));today=SleepDay.label(System.currentTimeMillis(),zone,cutoff);
  Application application=(Application)c.getApplicationContext();Application.ActivityLifecycleCallbacks lifecycle=new Application.ActivityLifecycleCallbacks(){public void onActivityCreated(Activity a,Bundle b){owned.add(a);}public void onActivityResumed(Activity a){current=a;}public void onActivityStarted(Activity a){}public void onActivityPaused(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,Bundle b){}public void onActivityDestroyed(Activity a){}};application.registerActivityLifecycleCallbacks(lifecycle);
  Set<String> tokens=PlanOperations.tokens();boolean quiet=false;
  try{task.run();}catch(Exception|AssertionError failure){try{d.takeScreenshot(new File(folder(),"dated-goal-failure.png"));d.dumpWindowHierarchy(new File(folder(),"dated-goal-failure.xml"));}catch(Exception e){failure.addSuppressed(e);}throw failure;}
  finally{try{PlanOperations.drain(10000);waitFor(()->{i.runOnMainSync(()->{for(Activity a:owned)if(!a.isDestroyed()&&!a.isFinishing())a.finish();});return owned.stream().allMatch(Activity::isDestroyed);});for(Activity a:owned){if(a instanceof PlanActivity)((PlanActivity)a).awaitReaders(10000);if(a instanceof GoalBankActivity)((GoalBankActivity)a).awaitReaders(10000);if(a instanceof HistoryActivity)((HistoryActivity)a).awaitReaders(10000);}PlanOperations.drain(10000);quiet=true;}
   finally{application.unregisterActivityLifecycleCallbacks(lifecycle);SleepPlans.factory=savedPlans;Records.factory=savedRecords;SleepPlans.reminders=savedReminder;for(String t:PlanOperations.tokens())if(!tokens.contains(t))PlanOperations.forget(t);assertEquals(0,deliveries.get());try(Store store=new Store(c)){assertEquals("Every original table and value must remain unchanged",original,BackupFingerprint.read(store));}assertEquals(originalPrefs,Prefs.of(c).getAll());if(quiet){c.deleteDatabase(db);c.deleteSharedPreferences(prefs);}}}
 }
 interface Check {boolean ready()throws Exception;}
 void waitFor(Check check)throws Exception{long until=SystemClock.elapsedRealtime()+10000;while(!check.ready()){if(SystemClock.elapsedRealtime()>until)throw new AssertionError("Owned goal state did not become ready");Thread.sleep(30);}}
 void launch(Class<?> type,boolean trends)throws Exception{Intent intent=new Intent(c,type).putExtra("visualReview",true).putExtra("verificationLabel","TEST FIXTURE · dated goal records").putExtra("trends",trends).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);Activity a=i.startActivitySync(intent);current=a;waitFor(()->a.hasWindowFocus()&&a.getWindow().getDecorView().isLaidOut());}
 View find(View root,String label){if(label.contentEquals(root.getContentDescription()==null?"":root.getContentDescription())||root instanceof TextView&&label.contentEquals(((TextView)root).getText()))return root;if(root instanceof ViewGroup){ViewGroup g=(ViewGroup)root;for(int n=0;n<g.getChildCount();n++){View v=find(g.getChildAt(n),label);if(v!=null)return v;}}return null;}
 View focused(){for(View root:WindowInspector.getGlobalWindowViews())if(root.hasWindowFocus()&&root.isAttachedToWindow()&&root.getContext().getPackageName().equals(c.getPackageName()))return root;return null;}
 Rect ready(String label){Rect[] result={null};i.runOnMainSync(()->{View root=focused();if(root==null)return;View v=find(root,label);if(v==null)return;while(v!=null&&!v.isClickable())v=v.getParent() instanceof View?(View)v.getParent():null;if(v==null||!v.isEnabled())return;Rect bounds=new Rect();if(v.getGlobalVisibleRect(bounds)&&bounds.height()==v.getHeight()&&bounds.width()==v.getWidth()&&bounds.height()>=Design.dp(c,48)){int[] screen=new int[2];v.getLocationOnScreen(screen);result[0]=new Rect(screen[0],screen[1],screen[0]+v.getWidth(),screen[1]+v.getHeight());}});return result[0];}
 void click(String label)throws Exception{if(ready(label)==null){try{new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().description(label));}catch(UiObjectNotFoundException ignored){try{new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().text(label));}catch(UiObjectNotFoundException ignoredToo){}}}waitFor(()->ready(label)!=null);View[] owner={null};CountDownLatch commit=new CountDownLatch(1);i.runOnMainSync(()->{owner[0]=focused();assertNotNull(owner[0]);owner[0].getViewTreeObserver().registerFrameCommitCallback(commit::countDown);owner[0].invalidate();});assertTrue(commit.await(5,TimeUnit.SECONDS));Rect r=ready(label);assertNotNull(label,r);i.runOnMainSync(()->assertSame("Exact focused committed input owner",owner[0],focused()));assertTrue(d.click(r.centerX(),r.centerY()));}
 void text(String label)throws Exception{waitFor(()->{boolean[] present={false};i.runOnMainSync(()->{View root=focused();present[0]=root!=null&&find(root,label)!=null;});return present[0];});}
 boolean keyboardVisible(){boolean[] visible={false};i.runOnMainSync(()->{View root=focused();android.view.WindowInsets insets=root==null?null:root.getRootWindowInsets();visible[0]=insets!=null&&insets.isVisible(android.view.WindowInsets.Type.ime());});return visible[0];}
 void finishNumericEntry(String title)throws Exception{text(title);if(keyboardVisible()){assertTrue("Close only the numeric keyboard",d.pressBack());waitFor(()->!keyboardVisible());}text(title);waitFor(()->ready("Cancel")!=null&&ready("Use duration")!=null);}
 boolean belongsTo(View view,Activity owner){if(view==null)return false;Context context=view.getContext();while(context!=null){if(context==owner)return true;if(!(context instanceof ContextWrapper))break;Context base=((ContextWrapper)context).getBaseContext();if(base==context)break;context=base;}if(view instanceof ViewGroup group)for(int n=0;n<group.getChildCount();n++)if(belongsTo(group.getChildAt(n),owner))return true;return false;}
 // Activity relaunch may preserve its DecorView. New Activity identity and newly owned content,
 // rather than a different window object, prove that the replacement owns the current frame.
 void recreate()throws Exception{Activity old=current;i.runOnMainSync(old::recreate);waitFor(()->{boolean[] ready={false};i.runOnMainSync(()->{View owner=focused();Activity replacement=current;ready[0]=replacement!=old&&old.isDestroyed()&&!replacement.isDestroyed()&&owner!=null&&belongsTo(owner,replacement)&&owner.isLaidOut();});return ready[0];});}
 File folder(){File f=new File(c.getExternalFilesDir(null),"verification");assertTrue(f.isDirectory()||f.mkdirs());return f;}
 void capture(String name,String control)throws Exception{text(control);if(!control.equals("Choose goal target"))waitFor(()->ready(control)!=null);CountDownLatch commit=new CountDownLatch(1);i.runOnMainSync(()->{View root=focused();assertNotNull(root);root.getViewTreeObserver().registerFrameCommitCallback(commit::countDown);root.invalidate();});assertTrue(commit.await(5,TimeUnit.SECONDS));assertTrue(d.takeScreenshot(new File(folder(),"dated-goal-"+name+".png")));}
 GoalCommitment goal(String metric,int target,LocalDate start){return new GoalCommitment(UUID.randomUUID().toString(),metric,target,start,14,zone.getId(),cutoff,"",null);}
 void chooseDate(LocalDate date)throws Exception{click("Starts");text("Goal starts");UiObject2 selected=d.wait(Until.findObject(By.descStartsWith("Pending date ")),3000);assertNotNull(selected);LocalDate pending=LocalDate.parse(selected.getContentDescription().substring("Pending date ".length()));int months=(date.getYear()-pending.getYear())*12+date.getMonthValue()-pending.getMonthValue();for(int n=0;n<Math.abs(months);n++)click(months<0?"Previous month":"Next month");click("Choose date "+date);}
 String gap(){String[] found={""};i.runOnMainSync(()->{View root=focused();found[0]=gapIn(root);});return found[0];}
 String gapIn(View root){if(root==null)return "";CharSequence desc=root.getContentDescription();if(desc!=null&&desc.toString().startsWith("Open dated goal bank."))return desc.toString();if(root instanceof ViewGroup){ViewGroup g=(ViewGroup)root;for(int n=0;n<g.getChildCount();n++){String found=gapIn(g.getChildAt(n));if(!found.isEmpty())return found;}}return "";}
 @Test public void historicalTargetAndIdentityDrivePlanAndTrendLinks()throws Exception{fixture(()->{
  GoalCommitment target=goal("duration",480,today.minusDays(7));SleepPlans.add(c,target);SleepPlans.add(c,goal("bedtime",1320,today.minusDays(6)));SleepPlans.add(c,goal("duration",600,today.plusDays(20)));
  long end=today.minusDays(1).atTime(6,30).atZone(zone).toInstant().toEpochMilli();if(!SleepDay.label(end,zone,cutoff).equals(today.minusDays(1)))end=today.atTime(cutoff/60,cutoff%60).atZone(zone).toInstant().minusSeconds(60).toEpochMilli();try(Store store=new Store(c,db)){RecordDraft record=new RecordDraft(end-7*3600000L,end);record.note="Private dated-goal fixture";Records.create(store,record,zone,System.currentTimeMillis());}
  String history;try(Store store=new Store(c,db)){history=BackupFingerprint.read(store);}launch(PlanActivity.class,false);text("−1 h");capture("plan","Goal bank");click("Goal bank");text("Duration goal");capture("history","New goal");assertEquals(target.id,current.getIntent().getStringExtra("goal"));
  launch(HistoryActivity.class,true);waitFor(()->gap().contains("−1 h"));assertTrue("Today's 10 h target must not replace the saved 8 h commitment",gap().contains("1 recorded"));capture("trends",gap());click(gap());text("Recorded goal balance");assertEquals(target.id,current.getIntent().getStringExtra("goal"));capture("linked","New goal");click("8 h duration goal");text("Duration goal");try(Store store=new Store(c,db)){assertEquals(history,BackupFingerprint.read(store));}assertEquals(600,SleepPlans.load(c).goal);
 });}
 @Test public void datedTargetDraftCancelSaveReopenAndErrorPersist()throws Exception{fixture(()->{
  launch(GoalBankActivity.class,false);capture("empty","Set your first goal");click("Set your first goal");click("Choose goal target");text("Goal duration");UiObject2 hours=d.wait(Until.findObject(By.desc("Duration hours")),3000);assertNotNull(hours);hours.setText("7");UiObject2 minutes=d.findObject(By.desc("Duration minutes"));assertNotNull(minutes);minutes.setText("30");finishNumericEntry("Goal duration");capture("target","Use duration");
  click("Cancel");
  text("Set a goal");
  text("10 h");assertTrue(SleepPlans.goals(c).isEmpty());click("Choose goal target");d.wait(Until.findObject(By.desc("Duration hours")),3000).setText("7");d.findObject(By.desc("Duration minutes")).setText("30");finishNumericEntry("Goal duration");click("Use duration");
  chooseDate(today.minusDays(1));capture("calendar","Use date");recreate();text("Goal starts");text("Pending date "+today.minusDays(1));click("Use date");click("Start goal");text("Goal bank");waitFor(()->SleepPlans.goals(c).size()==1);GoalCommitment saved=SleepPlans.goals(c).get(0);assertEquals(450,saved.target);assertEquals(today.minusDays(1),saved.start);capture("saved","New goal");
  Map<String,?> before=new HashMap<>(c.getSharedPreferences(prefs,0).getAll());click("New goal");click("Start goal");text("There is already a goal for this metric during these dates.");capture("error","Start goal");assertEquals(before,c.getSharedPreferences(prefs,0).getAll());recreate();text("There is already a goal for this metric during these dates.");capture("error-recreated","Start goal");assertEquals(before,c.getSharedPreferences(prefs,0).getAll());
 });}
}
