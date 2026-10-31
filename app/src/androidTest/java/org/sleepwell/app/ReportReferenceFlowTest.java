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
    private String periodStep="before period interaction proof";
    private UiObject2 object(String name)throws Exception{for(int attempt=0;attempt<3;attempt++){instrumentation.waitForIdleSync();try{return currentObject(name);}catch(StaleObjectException stale){if(attempt==2)throw stale;}}throw new AssertionError("No current control: "+name);}
    private UiObject2 currentObject(String name)throws Exception{UiObject2 found=device.wait(Until.findObject(By.desc(name)),1800);if(found==null||found.getVisibleBounds().height()<Design.dp(context,32)){new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().description(name));found=device.wait(Until.findObject(By.desc(name)),3000);}assertNotNull(name,found);android.graphics.Rect bounds=found.getVisibleBounds();if(bounds.height()<Design.dp(context,32)){UiObject2 scroll=device.findObject(By.scrollable(true));assertNotNull("Scroll area for "+name,scroll);scroll.scroll(Direction.DOWN,.35f);found=device.wait(Until.findObject(By.desc(name)),3000);}assertNotNull(name,found);assertTrue("Visible control "+name,found.getVisibleBounds().height()>=Design.dp(context,24));return found;}
    private void click(String text)throws Exception{UiObject2 found=device.wait(Until.findObject(By.text(text)),1800);if(found==null)found=device.findObject(By.desc(text));if(found==null){new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().text(text));found=device.findObject(By.text(text));}assertNotNull(text,found);found.click();device.waitForIdle();}
    private void title(String value){assertTrue(value,device.wait(Until.hasObject(By.text(value)),5000));}
    private void launch(Class<?> activity,long id){launch(activity,id,"VISUAL TEST · seven isolated fixture nights");}
    private void launch(Class<?> activity,long id,String label){context.startActivity(new Intent(context,activity).putExtra("record",id).putExtra("visualReview",true).putExtra("verificationLabel",label).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));}
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
        try(java.io.PrintWriter writer=new java.io.PrintWriter(new File(folder,"report-reference-original-error.txt"))){original.printStackTrace(writer);StringBuilder states=new StringBuilder();instrumentation.runOnMainSync(()->{for(Stage stage:Stage.values())for(Activity activity:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(stage)){View decor=activity.getWindow().getDecorView();states.append(stage).append(' ').append(activity.getClass().getSimpleName()).append(" destroyed=").append(activity.isDestroyed()).append(" finishing=").append(activity.isFinishing()).append(" focused=").append(activity.hasWindowFocus()).append(" attached=").append(decor.isAttachedToWindow()).append(" layout=").append(decor.getWidth()).append('x').append(decor.getHeight()).append('\n');}});writer.println("Period phase: "+periodStep);writer.print(states);}catch(Exception|AssertionError diagnostic){original.addSuppressed(diagnostic);}
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
    private TrendsChart durationTrend(View view){if(view instanceof TrendsChart chart&&view.getContentDescription()!=null&&view.getContentDescription().toString().startsWith("Recorded duration."))return chart;if(view instanceof ViewGroup group)for(int n=0;n<group.getChildCount();n++){TrendsChart found=durationTrend(group.getChildAt(n));if(found!=null)return found;}return null;}
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
    private void audioRows(View root,List<ReportAudioRows> rows){if(root instanceof ReportAudioRows audio&&!rows.contains(audio))rows.add(audio);if(root instanceof ViewGroup group)for(int n=0;n<group.getChildCount();n++)audioRows(group.getChildAt(n),rows);}
    private void completeAction(Activity owner,String name){View action=named(owner.getWindow().getDecorView(),name);assertNotNull(name,action);android.graphics.Rect visible=new android.graphics.Rect();assertTrue(name,action.getGlobalVisibleRect(visible));assertEquals(name+" complete width",action.getWidth(),visible.width());assertEquals(name+" complete height",action.getHeight(),visible.height());assertTrue(name+" 48dp target",visible.width()>=Design.dp(context,48)&&visible.height()>=Design.dp(context,48));}
    private void reportVariants(String database,List<File> files,List<SoundLibraryUiTest.FakePort> ports)throws Exception{
        long local=3000,start,end;try(Store store=new Store(context,database)){RecordEntry original=Records.get(store,2000);start=original.rawStart;end=original.rawEnd;store.getWritableDatabase().execSQL("INSERT INTO sessions(id,start,end,active_ms,note,source,origin,zone) VALUES(?,?,?,?,?,'tracked','local',?)",new Object[]{local,start,end,end-start,"Synthetic WAV fixture · fake playback only",original.zone().getId()});store.getWritableDatabase().execSQL("INSERT INTO epochs(session,time,start,movement,noise,light,samples) SELECT ?,time,start,movement,noise,light,samples FROM epochs WHERE session=2000",new Object[]{local});
            event(store.getWritableDatabase(),local,start+3600000,9750,"snoring");event(store.getWritableDatabase(),local,start+7200000,9750,"snoring");event(store.getWritableDatabase(),local,start+9000000,9750,"snoring");event(store.getWritableDatabase(),local,start+10800000,3900,"speech");
            File directory=new File(context.getFilesDir(),"clips");assertTrue(directory.isDirectory()||directory.mkdirs());for(int clip=0;clip<2;clip++){File file=new File(directory,"report-reference-"+UUID.randomUUID()+".wav");files.add(file);byte[] pcm=new byte[320000];for(int n=0;n<160000;n++){short sample=(short)(Math.sin(n*(clip==0?.12:.19))*(1500+(n/2500%7)*1800));pcm[n*2]=(byte)sample;pcm[n*2+1]=(byte)(sample>>8);}NoiseRecorder.writeWave(file,pcm);assertEquals(10000,ClipWave.read(file).durationMs);store.clip(local,start+(clip==0?3600000:10800000)+10000,file.getName(),.8,10000);}
            ReportSounds sounds=ReportSounds.read(context,store,Records.get(store,local));assertEquals(3,sounds.snoring);assertEquals(1,sounds.speech);assertEquals(2,sounds.availableClips);assertEquals(2,sounds.analysisOnly);assertEquals(2,ReportAudioData.read(context,store,local,start,end,sounds).size());}
        launch(RecordReportActivity.class,local,"VISUAL TEST · synthetic WAV · fake playback");title("Sleep report");assertTrue(device.wait(Until.hasObject(By.desc("Play Snoring estimates audio preview").enabled(true)),5000));assertTrue(device.wait(Until.hasObject(By.desc("Play Speech estimates audio preview").enabled(true)),5000));reportActionsFit();RecordReportActivity audioOwner=(RecordReportActivity)focused(RecordReportActivity.class);instrumentation.runOnMainSync(()->{completeAction(audioOwner,"Play Snoring estimates audio preview");completeAction(audioOwner,"Play Speech estimates audio preview");for(String category:new String[]{"Snoring","Speech"}){View label=textView(audioOwner.getWindow().getDecorView(),category);assertTrue("Short visible audio category",label instanceof android.widget.TextView);assertEquals("Reference category title fits on one line",1,((android.widget.TextView)label).getLineCount());}assertNotNull("Estimate provenance remains visible beneath Snoring",textView(audioOwner.getWindow().getDecorView(),"3 estimates"));assertNotNull("Estimate provenance remains visible beneath Speech",textView(audioOwner.getWindow().getDecorView(),"1 estimate"));});
        object("Play Snoring estimates audio preview").click();assertTrue("Native inline action reaches only the injected player",device.wait(Until.hasObject(By.desc("Pause Snoring estimates audio preview")),5000));SoundLibraryUiTest.FakeSession playing=ports.stream().flatMap(port->port.sessions.stream()).filter(session->session.target.clip.optLong("session")==local&&session.plays>0).findFirst().orElseThrow();assertEquals(9750,playing.target.duration());instrumentation.runOnMainSync(()->playing.events.position(2750));assertTrue(device.wait(Until.hasObject(By.text("0:02 / 0:09 · one retained event")),5000));capture("inline-audio");object("Pause Snoring estimates audio preview").click();assertTrue(device.wait(Until.hasObject(By.desc("Play Snoring estimates audio preview")),5000));
        long provider;String encoded;try(Store store=new Store(context,database)){long sourceStart=start-86400000L,sourceEnd=sourceStart+480*60000L;int[] minutes={0,7,100,111,175,190,300,370,470,480},kinds={1,4,1,5,6,4,5,4,6};List<HealthSleep.Stage> stages=new ArrayList<>();for(int n=0;n<kinds.length;n++)stages.add(new HealthSleep.Stage(sourceStart+minutes[n]*60000L,sourceStart+minutes[n+1]*60000L,kinds[n]));HealthSleep source=new HealthSleep("report-stage-fixture","com.sleepwell.fixture",sourceStart,sourceEnd,1,0,0,"Synthetic imported provider night","Fixture source stages; never phone-estimated",stages);new HealthSleepStore(store).apply(List.of(source),List.of(),"private-report-stage-token",context.getPackageName(),System.currentTimeMillis());provider=store.rows("SELECT id FROM sessions WHERE health_connect<>''").get(0).optLong("id");RecordEntry imported=Records.get(store,provider);encoded=imported.healthConnect;assertEquals(HealthSleepCodec.encode(source),encoded);assertEquals(462*60000L,imported.duration());assertTrue(imported.manual());assertTrue(store.rows("SELECT id FROM epochs WHERE session=?",""+provider).isEmpty());assertTrue(store.rows("SELECT id FROM clips WHERE session=?",""+provider).isEmpty());}
        launch(RecordReportActivity.class,provider,"VISUAL TEST · imported provider fixture");title("Sleep report");assertTrue(device.wait(Until.hasObject(By.descStartsWith("Health Connect source-reported sleep stages.")),5000));launch(RecordReportActivity.class,provider,"VISUAL TEST · imported provider fixture");title("Sleep report");assertTrue(device.wait(Until.hasObject(By.descStartsWith("Health Connect source-reported sleep stages.")),5000));title("Reported by com.sleepwell.fixture");title("7 h 42 min");for(String stage:new String[]{"Awake","Light","Deep","REM"})title(stage);assertFalse(device.hasObject(By.descStartsWith("Phone movement chart.")));assertFalse(device.hasObject(By.text("Not estimated")));assertFalse(device.hasObject(By.descStartsWith("Play ")));RecordReportActivity providerOwner=(RecordReportActivity)focused(RecordReportActivity.class);instrumentation.runOnMainSync(()->{completeAction(providerOwner,"Edit sleep");completeAction(providerOwner,"Report options");completeAction(providerOwner,"Open original Health Connect sleep details");});capture("provider-stages");try(Store store=new Store(context,database)){assertEquals("Reopening never relabels imported source evidence",encoded,Records.get(store,provider).healthConnect);assertEquals(462*60000L,Records.get(store,provider).duration());}
    }
    private RecordDraft periodDraft(RecordEditorActivity owner){RecordDraft[] result={null};instrumentation.runOnMainSync(()->result[0]=((RecordDraft)field(owner,"draft")).copy());return result[0];}
    private PeriodTimeline periodView(RecordEditorActivity owner){
        PeriodTimeline[] result={null};assertTrue(periodStep+": same owned editor/timeline regains focus",device.wait(new Condition<UiDevice,Boolean>(){public Boolean apply(UiDevice ignored){instrumentation.runOnMainSync(()->{if(!ready(owner,owner.getWindow().getDecorView()))return;Object candidate=field(owner,"timeline");if(candidate instanceof PeriodTimeline view&&view.isAttachedToWindow()&&view.isLaidOut()&&view.isShown()&&view.getRootView()==owner.getWindow().getDecorView())result[0]=view;});return result[0]!=null;}},5000));
        instrumentation.runOnMainSync(()->{assertTrue(periodStep+": current editor still owns focus",ready(owner,owner.getWindow().getDecorView()));assertSame("Exact current timeline",result[0],field(owner,"timeline"));result[0].requestRectangleOnScreen(new android.graphics.Rect(0,0,result[0].getWidth(),result[0].getHeight()),true);});instrumentation.waitForIdleSync();return result[0];
    }
    private float[] periodPoint(RecordEditorActivity owner,long instant){PeriodTimeline view=periodView(owner);float[] point=new float[2];instrumentation.runOnMainSync(()->{int[] screen=new int[2];view.getLocationOnScreen(screen);float density=context.getResources().getDisplayMetrics().density;long first=(Long)field(view,"axisStart"),last=(Long)field(view,"axisEnd");point[0]=screen[0]+10*density+(view.getWidth()-20*density)*(instant-first)/(float)(last-first);point[1]=screen[1]+64*density;android.graphics.Rect visible=new android.graphics.Rect();assertTrue(view.getGlobalVisibleRect(visible));assertTrue("Whole actual timeline exposed",visible.height()==view.getHeight());});return point;}
    private void periodInput(long down,int action,float[]... points){MotionEvent.PointerProperties[] properties=new MotionEvent.PointerProperties[points.length];MotionEvent.PointerCoords[] coordinates=new MotionEvent.PointerCoords[points.length];for(int n=0;n<points.length;n++){properties[n]=new MotionEvent.PointerProperties();properties[n].id=7+n*4;properties[n].toolType=MotionEvent.TOOL_TYPE_FINGER;coordinates[n]=new MotionEvent.PointerCoords();coordinates[n].x=points[n][0];coordinates[n].y=points[n][1];coordinates[n].pressure=1;coordinates[n].size=1;}MotionEvent event=MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,points.length,properties,coordinates,0,0,1,1,0,0,android.view.InputDevice.SOURCE_TOUCHSCREEN,0);try{assertTrue(periodStep+": one native "+MotionEvent.actionToString(action)+" event dispatched",instrumentation.getUiAutomation().injectInputEvent(event,true));}finally{event.recycle();}instrumentation.waitForIdleSync();}
    private String awakeIntervals(RecordDraft draft){StringBuilder result=new StringBuilder();for(RecordIntervals.Span span:draft.awake)result.append(span.start).append(':').append(span.end).append(';');return result.toString();}
    private void samePeriod(String why,RecordDraft expected,RecordDraft actual){assertEquals(why+" start",expected.start,actual.start);assertEquals(why+" end",expected.end,actual.end);assertEquals(why+" exact awake annotations",awakeIntervals(expected),awakeIntervals(actual));}
    private android.widget.ScrollView periodScroll(PeriodTimeline view){ViewParent parent=view.getParent();while(!(parent instanceof android.widget.ScrollView)&&parent instanceof View)parent=parent.getParent();assertTrue("Actual editor ScrollView",parent instanceof android.widget.ScrollView);return (android.widget.ScrollView)parent;}
    private void periodVerticalScroll(RecordEditorActivity owner,boolean handle)throws Exception{periodStep=handle?"near-handle vertical scroll":"off-handle vertical scroll";RecordDraft before=periodDraft(owner);float[] point=periodPoint(owner,handle?before.start:before.start+(before.end-before.start)/2);PeriodTimeline view=periodView(owner);android.widget.ScrollView scroll=periodScroll(view);int[] old={0};boolean[] can={false};instrumentation.runOnMainSync(()->{old[0]=scroll.getScrollY();can[0]=scroll.canScrollVertically(1);});long down=SystemClock.uptimeMillis();periodInput(down,MotionEvent.ACTION_DOWN,point);for(int n=1;n<=8;n++)periodInput(down,MotionEvent.ACTION_MOVE,new float[]{point[0],point[1]-Design.dp(context,64)*n/8f});SystemClock.sleep(180);periodInput(down,MotionEvent.ACTION_MOVE,new float[]{point[0],point[1]-Design.dp(context,64)});periodInput(down,MotionEvent.ACTION_UP,new float[]{point[0],point[1]-Design.dp(context,64)});device.waitForIdle();samePeriod(handle?"Vertical handle gesture":"Off-handle page scroll",before,periodDraft(owner));if(can[0]){int[] after={0};instrumentation.runOnMainSync(()->after[0]=scroll.getScrollY());assertTrue("Page receives vertical scrolling instead of editing the period",after[0]>old[0]);}}
    private void periodDrag(RecordEditorActivity owner,int target,long requested)throws Exception{periodStep="deliberate "+(target==0?"Start":"End")+" drag";RecordDraft before=periodDraft(owner);float[] from=periodPoint(owner,target==0?before.start:before.end),to=periodPoint(owner,requested);long down=SystemClock.uptimeMillis();periodInput(down,MotionEvent.ACTION_DOWN,from);for(int n=1;n<=6;n++)periodInput(down,MotionEvent.ACTION_MOVE,new float[]{from[0]+(to[0]-from[0])*n/6,from[1]});periodInput(down,MotionEvent.ACTION_UP,to);RecordDraft after=periodDraft(owner);assertTrue("Deliberate selected handle changes",target==0?after.start!=before.start:after.end!=before.end);assertEquals("Other boundary stays fixed",target==0?before.end:before.start,target==0?after.end:after.start);}
    private android.view.accessibility.AccessibilityNodeInfo periodNode(android.view.accessibility.AccessibilityNodeInfo root){if(root==null)return null;CharSequence description=root.getContentDescription();if(description!=null&&description.toString().startsWith("Editable period from "))return root;for(int n=0;n<root.getChildCount();n++){android.view.accessibility.AccessibilityNodeInfo child=root.getChild(n);android.view.accessibility.AccessibilityNodeInfo found=periodNode(child);if(found!=null){if(found!=child)child.recycle();return found;}if(child!=null)child.recycle();}return null;}
    private void periodAccessibility(RecordEditorActivity owner,String label)throws Exception{periodStep="accessibility "+label;periodView(owner);android.view.accessibility.AccessibilityNodeInfo root=instrumentation.getUiAutomation().getRootInActiveWindow(),node=periodNode(root);assertNotNull("Actual native editable-period accessibility node",node);try{int action=0;for(android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction item:node.getActionList())if(label.contentEquals(item.getLabel()==null?"":item.getLabel()))action=item.getId();assertNotEquals("Named boundary action "+label,0,action);assertTrue("One semantic action dispatched: "+label,node.performAction(action));}finally{if(node!=root)node.recycle();if(root!=null)root.recycle();}instrumentation.waitForIdleSync();}
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
        start=periodPoint(owner,baseline.start);past=periodPoint(owner,baseline.start+30*60000L);down=SystemClock.uptimeMillis();periodInput(down,MotionEvent.ACTION_DOWN,start);periodInput(down,MotionEvent.ACTION_MOVE,past);owner=recreatePeriod(owner);samePeriod("Recreation saves the cancelled baseline",baseline,periodDraft(owner));periodInput(down,MotionEvent.ACTION_UP,past);samePeriod("Lifting the original pointer after recreation cannot commit its abandoned preview",baseline,periodDraft(owner));capture("period-cancelled");
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
        assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertEquals("1",device.executeShellCommand("getprop ro.kernel.qemu").trim());assertFalse(Prefs.flag(context,"ringing"));assertFalse(Prefs.flag(context,"tracking"));assertFalse(AlarmService.active);assertFalse(TrackingService.running);
        String database="sleepwell-report-reference-"+UUID.randomUUID()+".db";Map<String,?> preferences=new HashMap<>(Prefs.of(context).getAll());Records.Factory previous=Records.factory;ClipPlayback.Provider previousPlayback=ClipPlayback.provider;List<SoundLibraryUiTest.FakePort> ports=new CopyOnWriteArrayList<>();List<File> fixtureFiles=new ArrayList<>();List<ReportAudioRows> audioReaders=new CopyOnWriteArrayList<>();List<Activity> owned=new CopyOnWriteArrayList<>();Application application=(Application)context.getApplicationContext();
        Application.ActivityLifecycleCallbacks callbacks=new Application.ActivityLifecycleCallbacks(){public void onActivityCreated(Activity activity,Bundle state){owned.add(activity);assertTrue(activity instanceof HistoryActivity||activity instanceof RecordEditorActivity||activity instanceof RecordReportActivity||activity instanceof SoundEventsActivity||activity instanceof SleepRhythmActivity);}public void onActivityStarted(Activity activity){}public void onActivityResumed(Activity activity){}public void onActivityPaused(Activity activity){if(activity instanceof RecordReportActivity)audioRows(activity.getWindow().getDecorView(),audioReaders);}public void onActivityStopped(Activity activity){}public void onActivitySaveInstanceState(Activity activity,Bundle state){}public void onActivityDestroyed(Activity activity){}};application.registerActivityLifecycleCallbacks(callbacks);
        try{
            instrumentation.runOnMainSync(()->{Records.factory=c->new Store(c,database);ClipPlayback.provider=(app,source)->{SoundLibraryUiTest.FakePort port=new SoundLibraryUiTest.FakePort();ports.add(port);return port;};});String raw;try(Store store=new Store(context,database)){seed(store);verifySoundProjection(store);raw=original(store);assertEquals(7,Records.all(store).size());assertEquals(462*60000L,Records.get(store,2000).duration());}
            launch(HistoryActivity.class,0);title("Sleep history");assertTrue(device.wait(Until.hasObject(By.descStartsWith("Open record 2000,")),5000));capture("history");
            click("Trends");
            title("Trends");
            title("7 h 43 min");
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
            HistoryActivity yearlyOwner=(HistoryActivity)focused(HistoryActivity.class);
            instrumentation.runOnMainSync(()->{
                View root=yearlyOwner.getWindow().getDecorView();assertTrue(ready(yearlyOwner,root));
                TrendsChart chart=durationTrend(root);assertNotNull("Owned monthly duration chart",chart);
                android.graphics.Rect visible=new android.graphics.Rect();assertTrue(chart.getGlobalVisibleRect(visible));assertEquals(chart.getHeight(),visible.height());
                for(int month=0;month<latestDay.getMonthValue();month++)assertTrue("Navigate actual chart accessibility action",chart.performAccessibilityAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SCROLL_FORWARD,null));
                long total=0;int count=0;int[] minutes={462,455,485,430,475,450,483};
                for(int night=0;night<minutes.length;night++)if(latestDay.minusDays(night).getYear()==latestDay.getYear()&&latestDay.minusDays(night).getMonthValue()==latestDay.getMonthValue()){total+=minutes[night]*60000L;count++;}
                String expected=latestDay.withDayOfMonth(1)+": "+RecordUi.roundedDuration(total/count);
                assertEquals("Monthly tooltip and spoken selection use the same rounded mean",expected,chart.getContentDescription().toString());
                assertFalse("Selected monthly value must not cover the current goal caption",android.graphics.RectF.intersects(chart.selectedLabelBounds(),chart.goalLabelBounds()));
                assertTrue("Selected monthly value stays within the actual chart width",chart.selectedLabelBounds().left>=0&&chart.selectedLabelBounds().right<=chart.getWidth());
            });
            capture("year-selected");
            click("Week");
            openTrendOptionsAfterWeek();
            click("Sleep rhythm");
            title("Sleep rhythm");
            device.pressBack();
            title("Trends");
            awaitHistoryRender();
            object("Read details").click();
            title("Sleep report");
            assertTrue(device.wait(Until.hasObject(By.text("Snoring")),5000));
            assertTrue(device.hasObject(By.text("3 estimates")));
            assertTrue(device.hasObject(By.text("Speech")));
            assertTrue(device.hasObject(By.text("1 estimate")));
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
            click("Add tag");object("Tag name").setText("Late meal");click("Add tag");click("Add tag");object("Tag name").setText("Exercise");click("Add tag");object("Notes or dream").setText("Fixture note saved from the native editor");completeNotes();capture("tags-notes");
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
            reportVariants(database,fixtureFiles,ports);
            assertEquals(preferences,Prefs.of(context).getAll());assertFalse(AlarmService.active);assertFalse(TrackingService.running);assertFalse(Prefs.flag(context,"ringing"));assertFalse(Prefs.flag(context,"tracking"));
        }catch(Exception|AssertionError error){diagnose(error);throw error;}
        finally{boolean quiescent=false;try{instrumentation.runOnMainSync(()->{for(Activity activity:owned)if(!activity.isDestroyed())activity.finish();});instrumentation.waitForIdleSync();long until=SystemClock.elapsedRealtime()+6000;while(owned.stream().anyMatch(a->!a.isDestroyed())&&SystemClock.elapsedRealtime()<until)SystemClock.sleep(50);assertTrue("All isolated screens must be destroyed",owned.stream().allMatch(Activity::isDestroyed));for(Activity activity:owned){if(activity instanceof HistoryActivity)((HistoryActivity)activity).awaitReaders(5000);if(activity instanceof SleepRhythmActivity)((SleepRhythmActivity)activity).awaitReaders(5000);if(activity instanceof RecordReportActivity)((RecordReportActivity)activity).awaitReaders(5000);if(activity instanceof RecordEditorActivity)((RecordEditorActivity)activity).awaitReaders(5000);if(activity instanceof SoundEventsActivity)assertTrue("Sound event readers must finish before factory cleanup",((SoundEventsActivity)activity).awaitReaders(5000));}for(ReportAudioRows audio:audioReaders)assertTrue("Inline WAV readers must finish before fixture cleanup",((ExecutorService)field(audio,"reader")).awaitTermination(5000,TimeUnit.MILLISECONDS));for(SoundLibraryUiTest.FakePort port:ports){assertTrue("Only closed fake playback ports remain",port.closed);for(SoundLibraryUiTest.FakeSession session:port.sessions)assertTrue("Fake playback session closed",session.closes>0);}quiescent=true;assertEquals(preferences,Prefs.of(context).getAll());}
            finally{application.unregisterActivityLifecycleCallbacks(callbacks);instrumentation.runOnMainSync(()->{Records.factory=previous;ClipPlayback.provider=previousPlayback;});if(quiescent){context.deleteDatabase(database);for(File file:fixtureFiles)if(file.exists())assertTrue("Remove only this journey’s synthetic WAV",file.delete());}}}
    }
}
