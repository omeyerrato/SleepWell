package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.*;
import androidx.test.uiautomator.*;
import org.json.JSONObject;
import org.junit.Test;
import java.io.File;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.Assert.*;

/** Disposable, host-muted CI emulator only. Every graph is an explicitly labelled DB fixture. */
public final class ReportReferenceFlowTest {
    @org.junit.Rule public final SoundBrowseTestScope browseScope=new SoundBrowseTestScope();
    private final Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
    private final Context context=instrumentation.getTargetContext();
    private final UiDevice device=UiDevice.getInstance(instrumentation);
    private UiObject2 object(String name)throws Exception{for(int attempt=0;attempt<3;attempt++){instrumentation.waitForIdleSync();try{return currentObject(name);}catch(StaleObjectException stale){if(attempt==2)throw stale;}}throw new AssertionError("No current control: "+name);}
    private UiObject2 currentObject(String name)throws Exception{UiObject2 found=device.wait(Until.findObject(By.desc(name)),1800);if(found==null||found.getVisibleBounds().height()<Design.dp(context,32)){new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().description(name));found=device.wait(Until.findObject(By.desc(name)),3000);}assertNotNull(name,found);android.graphics.Rect bounds=found.getVisibleBounds();if(bounds.height()<Design.dp(context,32)){UiObject2 scroll=device.findObject(By.scrollable(true));assertNotNull("Scroll area for "+name,scroll);scroll.scroll(Direction.DOWN,.35f);found=device.wait(Until.findObject(By.desc(name)),3000);}assertNotNull(name,found);assertTrue("Visible control "+name,found.getVisibleBounds().height()>=Design.dp(context,24));return found;}
    private void click(String text)throws Exception{UiObject2 found=device.wait(Until.findObject(By.text(text)),1800);if(found==null)found=device.findObject(By.desc(text));if(found==null){new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().text(text));found=device.findObject(By.text(text));}assertNotNull(text,found);found.click();device.waitForIdle();}
    private void title(String value){assertTrue(value,device.wait(Until.hasObject(By.text(value)),5000));}
    private void launch(Class<?> activity,long id){context.startActivity(new Intent(context,activity).putExtra("record",id).putExtra("visualReview",true).putExtra("verificationLabel","VISUAL TEST · seven isolated fixture nights").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));}
    private boolean ready(Activity activity,View decor){return !activity.isFinishing()&&!activity.isDestroyed()&&ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).contains(activity)&&activity.getWindow().getDecorView()==decor&&activity.hasWindowFocus()&&decor.hasWindowFocus()&&decor.isAttachedToWindow()&&decor.isLaidOut()&&decor.getWidth()>0&&decor.getHeight()>0;}
    private Activity focused(Class<? extends Activity> type)throws Exception{long until=SystemClock.elapsedRealtime()+8000;Activity[] found={null};while(found[0]==null&&SystemClock.elapsedRealtime()<until){instrumentation.runOnMainSync(()->{for(Activity activity:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))if(type.isInstance(activity)&&ready(activity,activity.getWindow().getDecorView())){found[0]=activity;break;}});if(found[0]==null)SystemClock.sleep(50);}assertNotNull("Focused resumed "+type.getSimpleName(),found[0]);return found[0];}
    private void awaitHistoryRender()throws Exception{HistoryActivity owner=(HistoryActivity)focused(HistoryActivity.class);owner.awaitReaders(5000);instrumentation.waitForIdleSync();}
    private android.graphics.Rect trendActionBounds(HistoryActivity owner,View decor,View target){
        assertTrue("Current History window must own focus",ready(owner,decor));assertTrue("Native History frame",decor.isHardwareAccelerated());
        View week=named(decor,"Week");assertNotNull("Current Week selector",week);assertTrue("Week selection must render before options",week.isSelected());
        assertSame("Use the current rendered Trend options",target,named(decor,"Trend options"));
        assertTrue("Trend options must be an attached visible enabled action",target.isAttachedToWindow()&&target.isLaidOut()&&target.isShown()&&target.isEnabled()&&target.isClickable());
        android.graphics.Rect visible=new android.graphics.Rect();assertTrue("Trend options must be visible",target.getGlobalVisibleRect(visible));
        assertEquals("Complete Trend options width",target.getWidth(),visible.width());assertEquals("Complete Trend options height",target.getHeight(),visible.height());
        assertTrue("Trend options retains a 48dp target",visible.width()>=Design.dp(context,48)&&visible.height()>=Design.dp(context,48));
        int[] location=new int[2];target.getLocationOnScreen(location);return new android.graphics.Rect(location[0],location[1],location[0]+target.getWidth(),location[1]+target.getHeight());
    }
    private void openTrendOptionsAfterWeek()throws Exception{
        HistoryActivity owner=(HistoryActivity)focused(HistoryActivity.class);owner.awaitReaders(5000);instrumentation.waitForIdleSync();
        CountDownLatch frame=new CountDownLatch(1);View[] decor={null},target={null};android.graphics.Rect[] bounds={null};
        instrumentation.runOnMainSync(()->{
            decor[0]=owner.getWindow().getDecorView();target[0]=named(decor[0],"Trend options");assertNotNull("Native Trend options",target[0]);bounds[0]=trendActionBounds(owner,decor[0],target[0]);
            decor[0].getViewTreeObserver().registerFrameCommitCallback(frame::countDown);decor[0].invalidate();
        });
        assertTrue("Current Week and options must commit their native frame",frame.await(8,TimeUnit.SECONDS));device.waitForIdle();
        instrumentation.runOnMainSync(()->assertEquals("The same current action must retain its full bounds",bounds[0],trendActionBounds(owner,decor[0],target[0])));
        assertTrue("Physically tap current Trend options",device.click(bounds[0].centerX(),bounds[0].centerY()));device.waitForIdle();title("Trend options");
    }
    private void destroyed(Activity activity){long until=SystemClock.elapsedRealtime()+8000;while(!activity.isDestroyed()&&SystemClock.elapsedRealtime()<until)SystemClock.sleep(50);assertTrue("Saved editor must finish before opening a replacement",activity.isDestroyed());}
    private void diagnose(Throwable original){
        File folder=new File(context.getExternalFilesDir(null),"verification");folder.mkdirs();
        try(java.io.PrintWriter writer=new java.io.PrintWriter(new File(folder,"report-reference-original-error.txt"))){original.printStackTrace(writer);StringBuilder states=new StringBuilder();instrumentation.runOnMainSync(()->{for(Stage stage:Stage.values())for(Activity activity:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(stage)){View decor=activity.getWindow().getDecorView();states.append(stage).append(' ').append(activity.getClass().getSimpleName()).append(" destroyed=").append(activity.isDestroyed()).append(" finishing=").append(activity.isFinishing()).append(" focused=").append(activity.hasWindowFocus()).append(" attached=").append(decor.isAttachedToWindow()).append(" layout=").append(decor.getWidth()).append('x').append(decor.getHeight()).append('\n');}});writer.print(states);}catch(Exception|AssertionError diagnostic){original.addSuppressed(diagnostic);}
        // Failure diagnostics are deliberately independent of the acceptance gate:
        // a modal error or absent resumed window must not hide the original failure.
        try{device.dumpWindowHierarchy(new File(folder,"report-reference-failure.xml"));}catch(Exception|AssertionError diagnostic){original.addSuppressed(diagnostic);}
        try{device.takeScreenshot(new File(folder,"report-reference-failure.png"));}catch(Exception|AssertionError diagnostic){original.addSuppressed(diagnostic);}
    }
    private void capture(String name)throws Exception{
        long until=SystemClock.elapsedRealtime()+8000;boolean committed=false;
        while(!committed&&SystemClock.elapsedRealtime()<until){
            CountDownLatch frame=new CountDownLatch(1);Activity[] owner={null};View[] decor={null};
            instrumentation.runOnMainSync(()->{for(Activity activity:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)){
                View root=activity.getWindow().getDecorView();if(!ready(activity,root))continue;
                assertTrue("Screenshots require a native hardware frame",root.isHardwareAccelerated());owner[0]=activity;decor[0]=root;
                root.getViewTreeObserver().registerFrameCommitCallback(frame::countDown);root.invalidate();break;
            }});
            if(owner[0]==null){SystemClock.sleep(50);continue;}
            if(!frame.await(Math.min(1000,Math.max(1,until-SystemClock.elapsedRealtime())),TimeUnit.MILLISECONDS))continue;
            device.waitForIdle();boolean[] stillReady={false};instrumentation.runOnMainSync(()->stillReady[0]=ready(owner[0],decor[0]));committed=stillReady[0];
        }
        assertTrue("A resumed, attached, laid-out, focused report window must commit its own frame: "+name,committed);
        if(name.equals("week")||name.equals("month")||name.equals("year")){
            UiObject2 action=device.findObject(By.desc("Read details")),dock=device.findObject(By.desc("Report").selected(true));assertNotNull("Read details on first trend frame",action);assertNotNull("Selected Report dock",dock);assertTrue("Read details must retain its complete touch target above the dock without scrolling",action.getVisibleBounds().height()>=Design.dp(context,48)&&action.getVisibleBounds().bottom<=dock.getVisibleBounds().top);
        }
        File folder=new File(context.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());assertTrue(device.takeScreenshot(new File(folder,"report-reference-"+name+".png")));
    }
    private Object field(Object owner,String name){try{java.lang.reflect.Field value=owner.getClass().getDeclaredField(name);value.setAccessible(true);return value.get(owner);}catch(ReflectiveOperationException error){throw new AssertionError("Owned calendar field "+name,error);}}
    private boolean calendarReady(HistoryActivity owner,RecordCalendar calendar,Dialog dialog,View decor,LocalDate selected){
        if(owner.isFinishing()||owner.isDestroyed()||!ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).contains(owner)
                ||field(owner,"calendar")!=calendar||field(calendar,"activity")!=owner||field(calendar,"dialog")!=dialog
                ||!calendar.showing()||!dialog.isShowing()||dialog.getWindow()==null||dialog.getWindow().getDecorView()!=decor
                ||!decor.hasWindowFocus()||!decor.isAttachedToWindow()||!decor.isLaidOut()||!decor.isShown()||decor.getWidth()<=0||decor.getHeight()<=0||!decor.isHardwareAccelerated())return false;
        View title=textView(decor,"Trend date"),pending=named(decor,"Pending date "+selected),choice=named(decor,"Choose date "+selected);
        return title!=null&&title.isShown()&&pending!=null&&pending.isShown()&&choice!=null&&choice.isShown()&&choice.isSelected();
    }
    private void captureCalendar(LocalDate selected)throws Exception{
        CountDownLatch frame=new CountDownLatch(1);HistoryActivity[] owner={null};RecordCalendar[] calendar={null};Dialog[] dialog={null};View[] decor={null};
        instrumentation.runOnMainSync(()->{
            int count=0;for(Activity activity:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))if(activity instanceof HistoryActivity){owner[0]=(HistoryActivity)activity;count++;}
            assertEquals("Exactly one resumed History owns the calendar",1,count);
            Object current=field(owner[0],"calendar");assertTrue("Current owned calendar",current instanceof RecordCalendar);calendar[0]=(RecordCalendar)current;
            Object window=field(calendar[0],"dialog");assertTrue("Actual calendar Dialog",window instanceof Dialog);dialog[0]=(Dialog)window;assertNotNull(dialog[0].getWindow());decor[0]=dialog[0].getWindow().getDecorView();
            assertTrue("Owned focused calendar must show the requested pending date",calendarReady(owner[0],calendar[0],dialog[0],decor[0],selected));
            decor[0].getViewTreeObserver().registerFrameCommitCallback(frame::countDown);decor[0].invalidate();
        });
        assertTrue("The actual focused calendar window must commit its hardware frame",frame.await(8,TimeUnit.SECONDS));device.waitForIdle();
        instrumentation.runOnMainSync(()->assertTrue("The same owned calendar/date must retain focus after its frame",calendarReady(owner[0],calendar[0],dialog[0],decor[0],selected)));
        File folder=new File(context.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());assertTrue(device.takeScreenshot(new File(folder,"report-reference-week-calendar.png")));
    }
    private View named(View view,String description){if(android.text.TextUtils.equals(description,view.getContentDescription()))return view;if(view instanceof ViewGroup group)for(int n=0;n<group.getChildCount();n++){View found=named(group.getChildAt(n),description);if(found!=null)return found;}return null;}
    private View textView(View view,String value){if(view instanceof android.widget.TextView&&value.contentEquals(((android.widget.TextView)view).getText()))return view;if(view instanceof ViewGroup group)for(int n=0;n<group.getChildCount();n++){View found=textView(group.getChildAt(n),value);if(found!=null)return found;}return null;}
    private void completeNotes()throws Exception{
        UiScrollable page=new UiScrollable(new UiSelector().scrollable(true));if(page.exists())assertTrue("Scroll exposes Notes",page.scrollIntoView(new UiSelector().description("Notes or dream")));
        instrumentation.runOnMainSync(()->{int matched=0;for(Activity activity:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))if(activity instanceof RecordEditorActivity){matched++;View note=named(activity.getWindow().getDecorView(),"Notes or dream");assertNotNull(note);note.requestRectangleOnScreen(new android.graphics.Rect(0,0,note.getWidth(),note.getHeight()),true);}assertEquals("Exactly one editor owns the note geometry",1,matched);});device.waitForIdle();
        instrumentation.runOnMainSync(()->{int matched=0;for(Activity activity:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))if(activity instanceof RecordEditorActivity){matched++;View root=activity.getWindow().getDecorView(),note=named(root,"Notes or dream"),save=textView(root,"Save changes");assertNotNull(note);assertNotNull(save);android.graphics.Rect field=new android.graphics.Rect(),footer=new android.graphics.Rect();assertTrue(note.getGlobalVisibleRect(field));assertTrue(save.getGlobalVisibleRect(footer));assertEquals("The complete note field can be exposed",note.getHeight(),field.height());assertEquals(note.getWidth(),field.width());assertTrue("Notes must stay fully above the fixed Save footer",field.bottom<=footer.top);}assertEquals("Exactly one editor owns the note geometry",1,matched);});
    }
    private void reportActionsFit()throws Exception{assertTrue("Loaded report actions",device.wait(Until.hasObject(By.desc("Report options")),5000));instrumentation.runOnMainSync(()->{int matched=0;for(Activity activity:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))if(activity instanceof RecordReportActivity){matched++;View root=activity.getWindow().getDecorView();for(String name:new String[]{"Edit sleep","Report options","View sound events"}){View action=named(root,name);assertNotNull(name,action);android.graphics.Rect visible=new android.graphics.Rect();assertTrue(name,action.getGlobalVisibleRect(visible));assertEquals(name+" complete touch target",action.getHeight(),visible.height());assertTrue(name+" 48dp target",visible.height()>=Design.dp(context,48));}}assertEquals("Exactly one report owns the action geometry",1,matched);});}
    private void seed(Store store){ZoneId zone=ZoneId.systemDefault();LocalDate latest=LocalDate.now(zone).minusDays(1);int[] durations={462,455,485,430,475,450,483};android.database.sqlite.SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();try{for(int night=0;night<7;night++){
        long end=latest.minusDays(night).atTime(6,30).atZone(zone).toInstant().toEpochMilli(),start=end-(durations[night]+(night==3?10:0))*60000L;long id=2000+night;
        db.execSQL("INSERT INTO sessions(id,start,end,active_ms,note,source,origin,zone) VALUES(?,?,?,?,?,'tracked','local',?)",new Object[]{id,start,end,durations[night]*60000L,"Synthetic CI sensor fixture, never a personal recording",zone.getId()});
        if(night==3){store.pause(id,start+120*60000L);store.resume(id,start+130*60000L);}
        for(int minute=0;minute<(end-start)/60000;minute++){if(night==3&&minute>=120&&minute<130)continue;double movement=.014+.004*((minute*37+night*11)%13)/12;int[] bursts={19,61,146,214,293,359,426},widths={3,13,6,20,4,9,5};for(int burst=0;burst<bursts.length;burst++){int offset=minute-bursts[burst]-night*3;if(offset>=0&&offset<widths[burst])movement+=(.08+.025*(burst%4))*(1-Math.abs((offset+.5)/widths[burst]*2-1));}store.epoch(id,start+minute*60000L,start+(minute+1)*60000L,movement,null,null,300);}
    }long first=Records.get(store,2000).edit.start,last=Records.get(store,2000).edit.end;
        event(db,2000,first-5000,10000,"snoring");event(db,2000,first+3600000,9750,"snoring");event(db,2000,first+7200000,9750,"snoring");event(db,2000,first+10800000,3900,"speech");
        event(db,2000,first-20000,20000,"noise");event(db,2000,last,10000,"noise");event(db,2001,first+3600000,10000,"speech");
        db.setTransactionSuccessful();}finally{db.endTransaction();}}
    private void event(android.database.sqlite.SQLiteDatabase db,long session,long start,long duration,String category){db.execSQL("INSERT INTO sound_events(session,time,duration_ms,category,score,model) VALUES(?,?,?,?,?,?)",new Object[]{session,start,duration,category,.8,"synthetic-report-fixture"});}
    private void verifySoundProjection(Store store){RecordEntry record=Records.get(store,2000);ReportSounds summary=ReportScreen.snapshot(context,store,record).sounds;assertEquals(3,summary.snoring);assertEquals(1,summary.speech);assertEquals(0,summary.other);assertEquals(4,summary.total);assertEquals(4,summary.analysisOnly);assertEquals(0,summary.availableClips);
        RecordDraft trimmed=record.edit.copy();trimmed.start+=30*60000L;trimmed.end=record.edit.start+90*60000L;ReportSounds narrow=ReportSounds.read(context,store,record.draft(trimmed));assertEquals(1,narrow.snoring);assertEquals(0,narrow.speech);assertEquals(1,narrow.total);
        String missing="report-fixture-missing-"+UUID.randomUUID()+".wav";store.clip(record.id,record.edit.start+3600000+9750,missing,.8,9750);ReportSounds unavailable=ReportSounds.read(context,store,record);assertEquals(0,unavailable.availableClips);assertEquals(3,unavailable.analysisOnly);store.getWritableDatabase().delete("clips","path=?",new String[]{missing});assertEquals(4,ReportSounds.read(context,store,record).analysisOnly);
    }
    private String original(Store store){return store.rows("SELECT * FROM sessions ORDER BY id").toString()+store.rows("SELECT * FROM epochs ORDER BY session,time").toString()+store.rows("SELECT * FROM tracking_pauses ORDER BY id").toString()+store.rows("SELECT * FROM sound_events ORDER BY id").toString()+store.rows("SELECT * FROM clips ORDER BY id").toString();}
    private RecordDraft periodDraft(RecordEditorActivity owner){RecordDraft[] result={null};instrumentation.runOnMainSync(()->result[0]=((RecordDraft)field(owner,"draft")).copy());return result[0];}
    private PeriodTimeline periodView(RecordEditorActivity owner){PeriodTimeline[] result={null};instrumentation.runOnMainSync(()->{assertTrue(ready(owner,owner.getWindow().getDecorView()));result[0]=(PeriodTimeline)field(owner,"timeline");assertTrue(result[0].isAttachedToWindow()&&result[0].isShown());result[0].requestRectangleOnScreen(new android.graphics.Rect(0,0,result[0].getWidth(),result[0].getHeight()),true);});instrumentation.waitForIdleSync();return result[0];}
    private float[] periodPoint(RecordEditorActivity owner,long instant){PeriodTimeline view=periodView(owner);float[] point=new float[2];instrumentation.runOnMainSync(()->{int[] screen=new int[2];view.getLocationOnScreen(screen);float density=context.getResources().getDisplayMetrics().density;long first=(Long)field(view,"axisStart"),last=(Long)field(view,"axisEnd");point[0]=screen[0]+10*density+(view.getWidth()-20*density)*(instant-first)/(float)(last-first);point[1]=screen[1]+64*density;android.graphics.Rect visible=new android.graphics.Rect();assertTrue(view.getGlobalVisibleRect(visible));assertTrue("Whole actual timeline exposed",visible.height()==view.getHeight());});return point;}
    private void periodInput(long down,int action,float[]... points){MotionEvent.PointerProperties[] properties=new MotionEvent.PointerProperties[points.length];MotionEvent.PointerCoords[] coordinates=new MotionEvent.PointerCoords[points.length];for(int n=0;n<points.length;n++){properties[n]=new MotionEvent.PointerProperties();properties[n].id=7+n*4;properties[n].toolType=MotionEvent.TOOL_TYPE_FINGER;coordinates[n]=new MotionEvent.PointerCoords();coordinates[n].x=points[n][0];coordinates[n].y=points[n][1];coordinates[n].pressure=1;coordinates[n].size=1;}MotionEvent event=MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,points.length,properties,coordinates,0,0,1,1,0,0,android.view.InputDevice.SOURCE_TOUCHSCREEN,0);try{assertTrue("One native pointer event dispatched",instrumentation.getUiAutomation().injectInputEvent(event,true));}finally{event.recycle();}instrumentation.waitForIdleSync();}
    private String awakeIntervals(RecordDraft draft){StringBuilder result=new StringBuilder();for(RecordIntervals.Span span:draft.awake)result.append(span.start).append(':').append(span.end).append(';');return result.toString();}
    private void samePeriod(String why,RecordDraft expected,RecordDraft actual){assertEquals(why+" start",expected.start,actual.start);assertEquals(why+" end",expected.end,actual.end);assertEquals(why+" exact awake annotations",awakeIntervals(expected),awakeIntervals(actual));}
    private android.widget.ScrollView periodScroll(PeriodTimeline view){ViewParent parent=view.getParent();while(!(parent instanceof android.widget.ScrollView)&&parent instanceof View)parent=parent.getParent();assertTrue("Actual editor ScrollView",parent instanceof android.widget.ScrollView);return (android.widget.ScrollView)parent;}
    private void periodVerticalScroll(RecordEditorActivity owner,boolean handle)throws Exception{RecordDraft before=periodDraft(owner);float[] point=periodPoint(owner,handle?before.start:before.start+(before.end-before.start)/2);PeriodTimeline view=periodView(owner);android.widget.ScrollView scroll=periodScroll(view);int[] old={0};boolean[] can={false};instrumentation.runOnMainSync(()->{old[0]=scroll.getScrollY();can[0]=scroll.canScrollVertically(1);});long down=SystemClock.uptimeMillis();periodInput(down,MotionEvent.ACTION_DOWN,point);for(int n=1;n<=8;n++)periodInput(down,MotionEvent.ACTION_MOVE,new float[]{point[0],point[1]-Design.dp(context,64)*n/8f});SystemClock.sleep(180);periodInput(down,MotionEvent.ACTION_MOVE,new float[]{point[0],point[1]-Design.dp(context,64)});periodInput(down,MotionEvent.ACTION_UP,new float[]{point[0],point[1]-Design.dp(context,64)});device.waitForIdle();samePeriod(handle?"Vertical handle gesture":"Off-handle page scroll",before,periodDraft(owner));if(can[0]){int[] after={0};instrumentation.runOnMainSync(()->after[0]=scroll.getScrollY());assertTrue("Page receives vertical scrolling instead of editing the period",after[0]>old[0]);}}
    private void periodDrag(RecordEditorActivity owner,int target,long requested)throws Exception{RecordDraft before=periodDraft(owner);float[] from=periodPoint(owner,target==0?before.start:before.end),to=periodPoint(owner,requested);long down=SystemClock.uptimeMillis();periodInput(down,MotionEvent.ACTION_DOWN,from);for(int n=1;n<=6;n++)periodInput(down,MotionEvent.ACTION_MOVE,new float[]{from[0]+(to[0]-from[0])*n/6,from[1]});periodInput(down,MotionEvent.ACTION_UP,to);RecordDraft after=periodDraft(owner);assertTrue("Deliberate selected handle changes",target==0?after.start!=before.start:after.end!=before.end);assertEquals("Other boundary stays fixed",target==0?before.end:before.start,target==0?after.end:after.start);}
    private android.view.accessibility.AccessibilityNodeInfo periodNode(android.view.accessibility.AccessibilityNodeInfo root){if(root==null)return null;CharSequence description=root.getContentDescription();if(description!=null&&description.toString().startsWith("Editable period from "))return root;for(int n=0;n<root.getChildCount();n++){android.view.accessibility.AccessibilityNodeInfo child=root.getChild(n);android.view.accessibility.AccessibilityNodeInfo found=periodNode(child);if(found!=null){if(found!=child)child.recycle();return found;}if(child!=null)child.recycle();}return null;}
    private void periodAccessibility(RecordEditorActivity owner,String label)throws Exception{periodView(owner);android.view.accessibility.AccessibilityNodeInfo root=instrumentation.getUiAutomation().getRootInActiveWindow(),node=periodNode(root);assertNotNull("Actual native editable-period accessibility node",node);try{int action=0;for(android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction item:node.getActionList())if(label.contentEquals(item.getLabel()==null?"":item.getLabel()))action=item.getId();assertNotEquals("Named boundary action "+label,0,action);assertTrue("One semantic action dispatched: "+label,node.performAction(action));}finally{if(node!=root)node.recycle();if(root!=null)root.recycle();}instrumentation.waitForIdleSync();}
    private RecordEditorActivity recreatePeriod(RecordEditorActivity previous)throws Exception{Instrumentation.ActivityMonitor monitor=instrumentation.addMonitor(RecordEditorActivity.class.getName(),null,false);try{instrumentation.runOnMainSync(previous::recreate);assertNotNull("Recreated actual editor",monitor.waitForActivityWithTimeout(8000));}finally{instrumentation.removeMonitor(monitor);}destroyed(previous);RecordEditorActivity owner=(RecordEditorActivity)focused(RecordEditorActivity.class);assertTrue("Recreated current editor has loaded its own attached timeline",device.wait(new Condition<UiDevice,Boolean>(){public Boolean apply(UiDevice ignored){boolean[] loaded={false};instrumentation.runOnMainSync(()->{Object view=field(owner,"timeline");loaded[0]=view instanceof PeriodTimeline&&field(owner,"draft")!=null&&ready(owner,owner.getWindow().getDecorView())&&((PeriodTimeline)view).isAttachedToWindow()&&((PeriodTimeline)view).getRootView()==owner.getWindow().getDecorView();});return loaded[0];}},5000));return owner;}
    private long[] periodInteractionProof(String database,String raw)throws Exception{
        RecordEditorActivity owner=(RecordEditorActivity)focused(RecordEditorActivity.class);
        // Use the existing actual awake controls so preview clipping has a real draft annotation.
        object("Add awake period").click();title("Awake starts");click("Use time");title("Awake ends");click("Use time");
        RecordDraft baseline=periodDraft(owner);assertEquals(1,baseline.awake.size());
        periodVerticalScroll(owner,false);periodVerticalScroll(owner,true);
        float[] start=periodPoint(owner,baseline.start),past=periodPoint(owner,baseline.start+30*60000L);long down=SystemClock.uptimeMillis();
        periodInput(down,MotionEvent.ACTION_DOWN,start);periodInput(down,MotionEvent.ACTION_UP,new float[]{start[0]+Design.dp(context,2),start[1]});samePeriod("A handle tap never jumps to the finger",baseline,periodDraft(owner));
        down=SystemClock.uptimeMillis();periodInput(down,MotionEvent.ACTION_DOWN,start);periodInput(down,MotionEvent.ACTION_MOVE,past);assertTrue("Preview crosses the existing awake annotation",periodDraft(owner).awake.isEmpty());periodInput(down,MotionEvent.ACTION_MOVE,start);periodInput(down,MotionEvent.ACTION_UP,start);samePeriod("Dragging past an annotation and back retains it",baseline,periodDraft(owner));
        down=SystemClock.uptimeMillis();periodInput(down,MotionEvent.ACTION_DOWN,start);periodInput(down,MotionEvent.ACTION_MOVE,past);assertTrue(periodDraft(owner).start>baseline.start);periodInput(down,MotionEvent.ACTION_CANCEL,past);samePeriod("Cancel rolls back bounds and annotations",baseline,periodDraft(owner));
        down=SystemClock.uptimeMillis();periodInput(down,MotionEvent.ACTION_DOWN,start);periodInput(down,MotionEvent.ACTION_MOVE,past);float[] other=periodPoint(owner,baseline.end);periodInput(down,MotionEvent.ACTION_POINTER_DOWN|(1<<MotionEvent.ACTION_POINTER_INDEX_SHIFT),past,other);samePeriod("Second pointer cancels instead of changing owner",baseline,periodDraft(owner));periodInput(down,MotionEvent.ACTION_POINTER_UP|(1<<MotionEvent.ACTION_POINTER_INDEX_SHIFT),past,other);periodInput(down,MotionEvent.ACTION_UP,past);samePeriod("Remaining pointer cannot commit cancelled preview",baseline,periodDraft(owner));
        down=SystemClock.uptimeMillis();periodInput(down,MotionEvent.ACTION_DOWN,start);periodInput(down,MotionEvent.ACTION_MOVE,past);RecordEditorActivity dialogOwner=owner;instrumentation.runOnMainSync(()->assertTrue(named(dialogOwner.getWindow().getDecorView(),"Change end time").performClick()));assertTrue("Actual owned End dialog has window focus",device.wait(new Condition<UiDevice,Boolean>(){public Boolean apply(UiDevice ignored){boolean[] ready={false};instrumentation.runOnMainSync(()->{Object editor=field(dialogOwner,"timeEditor");if(editor==null)return;Dialog dialog=(Dialog)field(editor,"dialog");ready[0]=dialog.isShowing()&&dialog.getWindow()!=null&&dialog.getWindow().getDecorView().hasWindowFocus()&&!dialogOwner.hasWindowFocus();});return ready[0];}},5000));samePeriod("Actual dialog focus loss cancels preview",baseline,periodDraft(owner));periodInput(down,MotionEvent.ACTION_CANCEL,past);click("Cancel");owner=(RecordEditorActivity)focused(RecordEditorActivity.class);
        start=periodPoint(owner,baseline.start);past=periodPoint(owner,baseline.start+30*60000L);down=SystemClock.uptimeMillis();periodInput(down,MotionEvent.ACTION_DOWN,start);periodInput(down,MotionEvent.ACTION_MOVE,past);owner=recreatePeriod(owner);samePeriod("Recreation saves the cancelled baseline",baseline,periodDraft(owner));capture("period-cancelled");
        periodAccessibility(owner,"Move start earlier");periodAccessibility(owner,"Move end later");samePeriod("Recorded bounds stay inside immutable raw recording",baseline,periodDraft(owner));
        periodDrag(owner,0,baseline.start+30*60000L);RecordDraft trimmed=periodDraft(owner);assertTrue(trimmed.start>=baseline.start+20*60000L&&trimmed.start<=baseline.start+40*60000L);assertTrue("Only intentional completed trim drops outside annotation",trimmed.awake.isEmpty());capture("period-drag-start");
        periodDrag(owner,1,baseline.end-30*60000L);trimmed=periodDraft(owner);assertTrue(trimmed.end<=baseline.end-20*60000L&&trimmed.end>=baseline.end-40*60000L);capture("period-drag-end");
        RecordDraft beforeActions=periodDraft(owner);periodAccessibility(owner,"Move start later");assertEquals(beforeActions.start+60000,periodDraft(owner).start);periodAccessibility(owner,"Move start earlier");periodAccessibility(owner,"Move end earlier");assertEquals(beforeActions.end-60000,periodDraft(owner).end);periodAccessibility(owner,"Move end later");samePeriod("Both named boundary actions are reversible",beforeActions,periodDraft(owner));
        PeriodTimeline keyboard=periodView(owner);instrumentation.runOnMainSync(()->assertTrue(keyboard.requestFocus()));instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT);assertEquals(beforeActions.end-60000,periodDraft(owner).end);instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);assertEquals(beforeActions.start+60000,periodDraft(owner).start);instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT);samePeriod("Keyboard chooses and adjusts each handle",beforeActions,periodDraft(owner));capture("period-accessibility");
        owner=recreatePeriod(owner);samePeriod("Completed handle changes survive unsaved recreation",beforeActions,periodDraft(owner));capture("period-draft-recreated");
        try(Store store=new Store(context,database)){assertEquals("No period edit saved before explicit Save",baseline.start,Records.get(store,2000).edit.start);assertEquals(baseline.end,Records.get(store,2000).edit.end);assertEquals(raw,original(store));}
        return new long[]{beforeActions.start,beforeActions.end};
    }
    @Test public void referenceHistoryTrendsEditAndNightReportPersistWithoutEffects()throws Exception{
        assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertEquals("1",device.executeShellCommand("getprop ro.kernel.qemu").trim());assertFalse(Prefs.flag(context,"ringing"));assertFalse(AlarmService.active);assertFalse(TrackingService.running);
        String database="sleepwell-report-reference-"+UUID.randomUUID()+".db";Map<String,?> preferences=new HashMap<>(Prefs.of(context).getAll());Records.Factory previous=Records.factory;List<Activity> owned=new CopyOnWriteArrayList<>();Application application=(Application)context.getApplicationContext();
        Application.ActivityLifecycleCallbacks callbacks=new Application.ActivityLifecycleCallbacks(){public void onActivityCreated(Activity activity,Bundle state){owned.add(activity);assertTrue(activity instanceof HistoryActivity||activity instanceof RecordEditorActivity||activity instanceof RecordReportActivity||activity instanceof SoundEventsActivity||activity instanceof SleepRhythmActivity);}public void onActivityStarted(Activity activity){}public void onActivityResumed(Activity activity){}public void onActivityPaused(Activity activity){}public void onActivityStopped(Activity activity){}public void onActivitySaveInstanceState(Activity activity,Bundle state){}public void onActivityDestroyed(Activity activity){}};application.registerActivityLifecycleCallbacks(callbacks);
        try{
            Records.factory=c->new Store(c,database);String raw;try(Store store=new Store(context,database)){seed(store);verifySoundProjection(store);raw=original(store);assertEquals(7,Records.all(store).size());assertEquals(462*60000L,Records.get(store,2000).duration());}
            launch(HistoryActivity.class,0);title("Sleep history");assertTrue(device.wait(Until.hasObject(By.descStartsWith("Open record 2000,")),5000));capture("history");
            click("Trends");
            title("Trends");
            capture("week");
            LocalDate latestDay=LocalDate.now(ZoneId.systemDefault()).minusDays(1),chosenDay=latestDay.getDayOfMonth()>1?latestDay.minusDays(1):latestDay.plusDays(1);
            object("Choose trend date").click();
            title("Trend date");
            object("Choose date "+chosenDay).click();
            assertTrue("Calendar must render the actual pending date",device.wait(Until.hasObject(By.desc("Pending date "+chosenDay)),5000));
            captureCalendar(chosenDay);
            click("Use date");
            title("Trends");
            String missingDate=(chosenDay.isBefore(latestDay)?chosenDay.minusDays(6):chosenDay)+": no record.";
            assertTrue("Changing the calendar changes the actual plotted range and preserves unknown dates",device.wait(Until.hasObject(By.descContains(missingDate)),5000));
            awaitHistoryRender();
            object("Choose trend date").click();
            title("Trend date");
            assertTrue("Calendar must render the actual pending date",device.wait(Until.hasObject(By.desc("Pending date "+chosenDay)),5000));
            object("Choose date "+latestDay).click();
            assertTrue("Restored date must render before Use",device.wait(Until.hasObject(By.desc("Pending date "+latestDay)),5000));
            click("Use date");
            title("Trends");
            assertTrue(device.wait(Until.hasObject(By.descStartsWith("Recorded duration. "+latestDay.minusDays(6)+": ")),5000));
            awaitHistoryRender();
            UiObject2 durationChart=device.wait(Until.findObject(By.descStartsWith("Recorded duration.")),5000);assertNotNull("Actual weekly duration chart",durationChart);android.graphics.Rect durationBounds=durationChart.getVisibleBounds();assertTrue("Inspect a recorded day",device.click(durationBounds.centerX(),durationBounds.centerY()));String selectedDay=LocalDate.now(ZoneId.systemDefault()).minusDays(4)+": 7 h 10 min";assertTrue("The selected fourth day reports its real paused-duration total",device.wait(Until.hasObject(By.desc(selectedDay)),5000));capture("week-selected");
            click("Month");
            assertTrue("Month selection must render",device.wait(Until.hasObject(By.desc("Month").selected(true)),5000));
            capture("month");
            click("Year");
            assertTrue("Year selection must render",device.wait(Until.hasObject(By.desc("Year").selected(true)),5000));
            capture("year");
            click("Week");
            openTrendOptionsAfterWeek();
            click("Sleep rhythm");
            title("Sleep rhythm");
            device.pressBack();
            title("Trends");
            awaitHistoryRender();
            object("Read details").click();
            title("Sleep report");
            assertTrue(device.wait(Until.hasObject(By.text("Snoring estimates")),5000));
            assertTrue(device.hasObject(By.text("3 events")));
            assertTrue(device.hasObject(By.text("Speech estimates")));
            assertTrue(device.hasObject(By.text("1 event")));
            assertTrue(device.hasObject(By.text("Analysis only · no recordings saved.")));
            assertFalse(device.hasObject(By.textStartsWith("Play")));
            UiObject2 measuredChart=device.wait(Until.findObject(By.descStartsWith("Phone movement chart.")),5000);assertNotNull("Actual movement chart",measuredChart);android.graphics.Rect measuredBounds=measuredChart.getVisibleBounds();assertTrue("Inspect a measured interval",device.click(measuredBounds.centerX(),measuredBounds.centerY()));assertTrue("Selected measured value is available",device.wait(Until.hasObject(By.desc(java.util.regex.Pattern.compile("(?s)Phone movement at .* relative.*"))),5000));
            reportActionsFit();capture("night");
            launch(RecordReportActivity.class,2003);title("Sleep report");UiObject2 gapChart=device.wait(Until.findObject(By.descStartsWith("Phone movement chart.")),5000);assertNotNull("Gapped measured chart",gapChart);android.graphics.Rect gapBounds=gapChart.getVisibleBounds();
            // This fixture has440 elapsed minutes and a real blank120–130min pause.
            // Tap its midpoint on the visible time axis, rather than invoking a chart callback.
            int gapX=gapBounds.left+Design.dp(context,28)+(int)((gapBounds.width()-Design.dp(context,36))*125d/440d);assertTrue("Inspect the recorded pause",device.click(gapX,gapBounds.centerY()));assertTrue("A blank interval must not select a distant measurement",device.wait(Until.hasObject(By.desc(java.util.regex.Pattern.compile("(?s)Phone movement at .*no readings for this interval.*"))),5000));capture("chart-gap");
            launch(RecordReportActivity.class,2000);title("Sleep report");object("Edit sleep");reportActionsFit();
            object("Report options").click();click("About this recording");title("About this recording");click("Got it");title("Sleep report");
            object("Report options").click();click("View trends");title("Trends");device.pressBack();title("Sleep report");reportActionsFit();
            object("View sound events").click();
            title("Sound events");
            assertTrue(device.wait(Until.hasObject(By.desc("All (4)")),5000));
            assertTrue(device.hasObject(By.desc("Snoring (3)")));
            assertTrue(device.hasObject(By.desc("Speech (1)")));
            assertFalse(device.hasObject(By.textStartsWith("Play")));
            object("Sound event options").click();
            click("Other sounds");
            assertTrue(device.wait(Until.hasObject(By.text("No events in this category")),5000));
            object("Sound event options").click();
            click("All events");
            assertTrue(device.wait(Until.hasObject(By.desc("All (4)").selected(true)),5000));
            device.pressBack();
            title("Sleep report");
            object("Edit sleep").click();
            title("Edit sleep");
            object("Notes or dream");completeNotes();
            capture("editor");
            long[] editedPeriod=periodInteractionProof(database,raw);
            click("Add tag");object("Tag name").setText("Late meal");click("Add");click("Add tag");object("Tag name").setText("Exercise");click("Add");object("Notes or dream").setText("Fixture note saved from the native editor");completeNotes();capture("tags-notes");
            object("Add awake period").click();
            title("Awake starts");
            click("Use time");
            title("Awake ends");
            click("Use time");
            Activity savedEditor=focused(RecordEditorActivity.class);
            click("Save changes");
            destroyed(savedEditor);
            focused(RecordReportActivity.class);
            title("Sleep report");
            title(RecordUi.duration(editedPeriod[1]-editedPeriod[0]-10*60000L));
            try(Store store=new Store(context,database)){RecordEntry entry=Records.get(store,2000);assertEquals(editedPeriod[1]-editedPeriod[0]-10*60000L,entry.duration());assertEquals(editedPeriod[0],entry.edit.start);assertEquals(editedPeriod[1],entry.edit.end);assertEquals(1,entry.edit.awake.size());assertEquals("Fixture note saved from the native editor",entry.edit.note);assertTrue(entry.edit.tags.containsAll(List.of("Late meal","Exercise")));assertEquals(raw,original(store));}
            object("Edit sleep").click();
            focused(RecordEditorActivity.class);
            title("Edit sleep");
            assertEquals("Fixture note saved from the native editor",object("Notes or dream").getText());
            completeNotes();capture("reopened");
            RecordEditorActivity reopened=(RecordEditorActivity)focused(RecordEditorActivity.class);assertEquals(editedPeriod[0],periodDraft(reopened).start);assertEquals(editedPeriod[1],periodDraft(reopened).end);periodView(reopened);capture("period-persisted");periodAccessibility(reopened,"Move end earlier");assertEquals(editedPeriod[1]-60000,periodDraft(reopened).end);
            object("Notes or dream").setText("This change must be discarded");
            device.pressBack();
            focused(RecordReportActivity.class);
            title("Sleep report");
            try(Store store=new Store(context,database)){assertEquals("Fixture note saved from the native editor",Records.get(store,2000).edit.note);assertEquals("Back discards the unsaved boundary action",editedPeriod[1],Records.get(store,2000).edit.end);assertEquals(editedPeriod[0],Records.get(store,2000).edit.start);assertEquals(raw,original(store));}
            assertEquals(preferences,Prefs.of(context).getAll());assertFalse(AlarmService.active);assertFalse(TrackingService.running);assertFalse(Prefs.flag(context,"ringing"));
        }catch(Exception|AssertionError error){diagnose(error);throw error;}
        finally{boolean quiescent=false;try{instrumentation.runOnMainSync(()->{for(Activity activity:owned)if(!activity.isDestroyed())activity.finish();});instrumentation.waitForIdleSync();long until=SystemClock.elapsedRealtime()+6000;while(owned.stream().anyMatch(a->!a.isDestroyed())&&SystemClock.elapsedRealtime()<until)SystemClock.sleep(50);assertTrue("All isolated screens must be destroyed",owned.stream().allMatch(Activity::isDestroyed));for(Activity activity:owned){if(activity instanceof HistoryActivity)((HistoryActivity)activity).awaitReaders(5000);if(activity instanceof SleepRhythmActivity)((SleepRhythmActivity)activity).awaitReaders(5000);if(activity instanceof RecordReportActivity)((RecordReportActivity)activity).awaitReaders(5000);if(activity instanceof RecordEditorActivity)((RecordEditorActivity)activity).awaitReaders(5000);if(activity instanceof SoundEventsActivity)assertTrue("Sound event readers must finish before factory cleanup",((SoundEventsActivity)activity).awaitReaders(5000));}quiescent=true;assertEquals(preferences,Prefs.of(context).getAll());}
            finally{application.unregisterActivityLifecycleCallbacks(callbacks);Records.factory=previous;if(quiescent)context.deleteDatabase(database);}}
    }
}
