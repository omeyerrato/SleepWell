package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.graphics.Rect;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.test.uiautomator.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import static org.junit.Assert.*;

/** Continuation of the existing routine method: actual editor/Practice owners, no alarm admission. */
final class RoutinePracticeFixture {
    private final Instrumentation i;private final Context c;private final UiDevice device;
    private final List<Activity> owned=new CopyOnWriteArrayList<>();private final Set<Activity> resumed=Collections.newSetFromMap(new ConcurrentHashMap<>());
    private volatile Activity top;private String step="Routine practice admission";private AlarmEditorActivity editor;private ChallengePracticeActivity practice;
    private final Clock clock=new Clock();private String database,stored;
    private final java.util.concurrent.atomic.AtomicInteger recordOpens=new java.util.concurrent.atomic.AtomicInteger();private int allowedRecordOpens;
    private RoutinePracticeFixture(Instrumentation i,Context c,UiDevice device){this.i=i;this.c=c;this.device=device;}
    static void verify(Instrumentation i,Context c,UiDevice device)throws Exception{new RoutinePracticeFixture(i,c,device).run();}
    private static final class Clock implements JumpTimeline.Port {long now=1000;Runnable frame;public long now(){return now;}public void request(Runnable value){frame=value;}public void cancel(){frame=null;}void tick(){now+=16;Runnable next=frame;frame=null;if(next!=null)next.run();}}
    private <T>T main(Supplier<T> read){if(Looper.myLooper()==Looper.getMainLooper())return read.get();java.util.concurrent.atomic.AtomicReference<T> value=new java.util.concurrent.atomic.AtomicReference<>();i.runOnMainSync(()->value.set(read.get()));return value.get();}
    @SuppressWarnings("unchecked") private static <T>T field(Object object,String name){try{java.lang.reflect.Field value=object.getClass().getDeclaredField(name);value.setAccessible(true);return (T)value.get(object);}catch(ReflectiveOperationException missing){throw new AssertionError(name,missing);}}
    private void await(BooleanSupplier ready)throws Exception{long end=SystemClock.elapsedRealtime()+8000;while(!ready.getAsBoolean()&&SystemClock.elapsedRealtime()<end)Thread.sleep(30);assertTrue(step,ready.getAsBoolean());}
    private boolean ready(Activity owner){return main(()->owner!=null&&!owner.isDestroyed()&&!owner.isFinishing()&&resumed.contains(owner)&&owner.hasWindowFocus()&&owner.getWindow().getDecorView().isAttachedToWindow()&&owner.getWindow().getDecorView().isHardwareAccelerated()&&owner.getWindow().getDecorView().isLaidOut()&&!owner.getWindow().getDecorView().isLayoutRequested());}
    private void ready()throws Exception{await(()->ready(top));}
    private ChallengePracticeSession session(){return main(()->field(practice,"session"));}
    private ChallengePracticeScreen screen(){return main(()->field(practice,"screen"));}
    private int completed(){return main(()->session().completed());}
    private View find(View root,String name){if(name.contentEquals(root.getContentDescription()==null?"":root.getContentDescription())||root instanceof TextView text&&name.contentEquals(text.getText()))return root;if(root instanceof ViewGroup group)for(int n=0;n<group.getChildCount();n++){View found=find(group.getChildAt(n),name);if(found!=null)return found;}return null;}
    private View find(String name){return main(()->find(top.getWindow().getDecorView(),name));}
    private void tap(View view)throws Exception{ready();Rect bounds=main(()->{Rect area=new Rect();assertNotNull(view);assertTrue(view.isAttachedToWindow());assertTrue(view.getGlobalVisibleRect(area));assertEquals(view.getWidth(),area.width());assertEquals(view.getHeight(),area.height());assertTrue(area.width()>=Design.dp(c,48)&&area.height()>=Design.dp(c,48));return area;});assertTrue(device.click(bounds.centerX(),bounds.centerY()));device.waitForIdle();}
    private void click(String name)throws Exception{step="Actual click "+name;ready();await(()->find(name)!=null);View view=find(name);view=main(()->{View target=find(name);while(target!=null&&!target.isClickable())target=target.getParent() instanceof View?(View)target.getParent():null;return target;});tap(view);}
    private void text(String value)throws Exception{step="Visible "+value;await(()->find(value)!=null);}
    private void enter(EditText input,String value)throws Exception{tap(input);i.runOnMainSync(()->input.setText(value));boolean visible=main(()->top.getWindow().getDecorView().getRootWindowInsets().isVisible(WindowInsets.Type.ime()));if(visible){Activity owner=top;device.pressBack();await(()->top==owner&&ready(owner)&&main(()->!owner.getWindow().getDecorView().getRootWindowInsets().isVisible(WindowInsets.Type.ime())));} }
    private void unchanged()throws Exception{assertEquals("Practice never opens Records; only its editor owns the private store",allowedRecordOpens,recordOpens.get());try(Store store=new Store(c,database)){assertEquals("Practice never saves the alarm draft",stored,store.backup().toString());}}
    private ChallengeRoutine.Task task(String type,int rounds){return new ChallengeRoutine.Task(type,rounds);}
    private void launch(ChallengeRoutine routine)throws Exception{
        if(editor!=null&&!editor.isDestroyed()){i.runOnMainSync(editor::finish);await(editor::isDestroyed);}
        Alarm draft=new Alarm(1);draft.enabled=false;draft.label="Private routine practice";draft.setRoutine(routine);
        i.runOnMainSync(()->c.startActivity(new Intent(c,AlarmEditorActivity.class).putExtra("alarm",draft.json().toString()).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
        await(()->top instanceof AlarmEditorActivity&&ready(top));editor=(AlarmEditorActivity)top;click("Challenges");allowedRecordOpens=recordOpens.get();click("Preview routine");await(()->top instanceof ChallengePracticeActivity&&ready(top));practice=(ChallengePracticeActivity)top;
    }
    private void recreate()throws Exception{Activity old=practice;i.runOnMainSync(old::recreate);await(()->top instanceof ChallengePracticeActivity&&top!=old&&old.isDestroyed()&&ready(top));practice=(ChallengePracticeActivity)top;}
    private void fullBounds(View view,String label,boolean action){
        assertNotNull(label,view);Rect visible=new Rect();assertTrue(label,view.isAttachedToWindow()&&view.isShown()&&view.getGlobalVisibleRect(visible));
        assertEquals(label+" width",view.getWidth(),visible.width());assertEquals(label+" height",view.getHeight(),visible.height());
        if(action)assertTrue(label+" touch target",visible.width()>=Design.dp(c,48)&&visible.height()>=Design.dp(c,48));
    }
    private boolean assertCountingBoard(View root){CharSequence description=root.getContentDescription();if(description!=null&&description.toString().startsWith("Counting board. ")){fullBounds(root,"Full Counting board",false);assertEquals(root.getWidth(),root.getHeight());return true;}if(root instanceof ViewGroup group)for(int n=0;n<group.getChildCount();n++)if(assertCountingBoard(group.getChildAt(n)))return true;return false;}
    private void fullProgress(View root){
        CharSequence description=root.getContentDescription();
        if(description!=null&&description.toString().startsWith("Challenge ")&&root instanceof LinearLayout dots)for(int n=0;n<dots.getChildCount();n++)fullBounds(dots.getChildAt(n),"Progress dot "+n,false);
        if(root instanceof ViewGroup group)for(int n=0;n<group.getChildCount();n++)fullProgress(group.getChildAt(n));
    }
    private void capture(String suffix)throws Exception{
        step="Capture routine-preview-"+suffix;ready();CountDownLatch frame=new CountDownLatch(1);Activity expected=top;
        i.runOnMainSync(()->{View root=expected.getWindow().getDecorView();View action=find(root,suffix.equals("unsupported")?"Return to routine":suffix.equals("completed")||suffix.equals("second-completed")?"Done":"Cancel preview");assertNotNull("Actual exit action",action);while(!action.isClickable()&&action.getParent() instanceof View)action=(View)action.getParent();Rect area=new Rect();assertTrue(action.getGlobalVisibleRect(area));assertEquals("Full exit width",action.getWidth(),area.width());assertEquals("Full exit height",action.getHeight(),area.height());assertTrue(area.width()>=Design.dp(c,48)&&area.height()>=Design.dp(c,48));fullProgress(root);
            if(suffix.equals("counting")){fullBounds(find(root,"Counting answer"),"Counting answer",true);fullBounds(find(root,"Check count"),"Check count",true);assertTrue("Actual Counting board",assertCountingBoard(root));}
            if(suffix.equals("flags")){fullBounds(find(root,"Flag to identify"),"Full flag artwork",false);ChallengeRoutine.Position at=session().position();FlagPuzzle puzzle=FlagPuzzle.create(session().run,session().completed(),at.round);for(int option:puzzle.choices)fullBounds(find(root,FlagPuzzle.COUNTRIES.get(option)),"Flag choice",true);}
            root.getViewTreeObserver().registerFrameCommitCallback(frame::countDown);root.invalidate();});
        assertTrue(frame.await(5,TimeUnit.SECONDS));assertTrue(ready(expected));File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());assertTrue(device.takeScreenshot(new File(folder,"routine-preview-"+suffix+".png")));
    }
    private void checkMath()throws Exception{MathTask question=main(()->field(screen(),"math"));if(question.settings.typed)enter(main(()->field(screen(),"mathInput")),Integer.toString(question.answer()));else click("Answer "+question.answer());click("Check practice answer");}
    private void mathFocusGuard()throws Exception{
        ChallengePracticeScreen old=screen();ChallengePracticeOwner owner=main(()->field(old,"owner"));assertFalse("Off-main guard never reads Views",owner.current());
        MathTask question=main(()->field(old,"math"));Integer choice=main(()->field(old,"choice"));View answer=find("Answer "+question.options.get(0)),check=find("Check practice answer"),feedback=find("Select an answer");String copy=main(()->((TextView)feedback).getText().toString());Dialog[] dialog={null};
        i.runOnMainSync(()->dialog[0]=SleepPicker.message(practice,"Practice focus ownership","Only the actual focused task may accept input."));await(()->main(()->!practice.hasWindowFocus()&&dialog[0].getWindow().getDecorView().hasWindowFocus()));
        i.runOnMainSync(()->{assertFalse(owner.current());assertTrue(answer.performClick());assertTrue(check.performClick());assertEquals("Rejected selection cannot mutate old controller state",choice,(Integer)field(old,"choice"));assertEquals("Rejected check cannot publish stale feedback",copy,((TextView)feedback).getText().toString());});assertEquals(1,completed());i.runOnMainSync(dialog[0]::dismiss);ready();assertEquals(question.encode(),main(()->((MathTask)field(screen(),"math")).encode()));
    }
    private WakeCognitiveTask cognitive(){return main(()->field(screen(),"cognitive"));}
    private void memoryPairs(int limit)throws Exception{WakeCognitiveTask.Memory memory=(WakeCognitiveTask.Memory)cognitive();int done=0;for(int symbol=0;symbol<3&&done<limit;symbol++){List<Integer> pair=new ArrayList<>();for(int n=0;n<6;n++)if(memory.order[n]==symbol)pair.add(n);if((memory.matched&(1<<pair.get(0)))!=0)continue;tap(memory.buttons[pair.get(0)]);tap(memory.buttons[pair.get(1)]);done++;}}
    private void sequenceRestoration()throws Exception{
        capture("sequence");WakeCognitiveTask.Sequence sequence=(WakeCognitiveTask.Sequence)cognitive();String expected=main(()->sequence.expected);click(Integer.toString(expected.charAt(0)=='1'?2:1));assertEquals("Wrong sequence cannot advance",3,completed());
        expected=main(()->sequence.expected);for(char digit:expected.toCharArray())click(Character.toString(digit));assertEquals(4,completed());
        WakeCognitiveTask.Sequence round=(WakeCognitiveTask.Sequence)cognitive();String next=main(()->round.expected);assertEquals(4,next.length());click(next.substring(0,1));View stale=find(next.substring(1,2));recreate();WakeCognitiveTask.Sequence restored=(WakeCognitiveTask.Sequence)cognitive();assertEquals(next,main(()->restored.expected));assertEquals(next.substring(0,1),main(()->restored.entered));
        i.runOnMainSync(()->{String before=round.entered;assertTrue(stale.performClick());assertEquals("Destroyed sequence cannot mutate its state",before,round.entered);});assertEquals(4,completed());capture("sequence-recreated");for(int n=1;n<next.length();n++)click(next.substring(n,n+1));assertEquals(5,completed());
    }
    private void retypeCancelResume()throws Exception{
        WakeCognitiveTask.Retype retype=(WakeCognitiveTask.Retype)cognitive();enter(retype.input,"wrong");click("Check answer");assertEquals(5,completed());enter(retype.input,retype.phrase.substring(0,6));capture("retype");String run=session().run;click("Cancel preview");await(()->top==editor&&ready(editor));unchanged();click("Resume routine preview");await(()->top instanceof ChallengePracticeActivity&&ready(top));practice=(ChallengePracticeActivity)top;
        assertEquals(run,session().run);assertEquals(5,completed());WakeCognitiveTask.Retype restored=(WakeCognitiveTask.Retype)cognitive();assertEquals(retype.phrase,restored.phrase);assertEquals(retype.phrase.substring(0,6),main(()->restored.input.getText().toString()));enter(restored.input,restored.phrase);click("Check answer");assertEquals(6,completed());WakeCognitiveTask.Retype second=(WakeCognitiveTask.Retype)cognitive();enter(second.input,second.phrase);click("Check answer");assertEquals(7,completed());
    }
    private void counting()throws Exception{ChallengePracticeSession current=session();ChallengeRoutine.Position at=main(current::position);CountingPuzzle puzzle=CountingPuzzle.create(current.run,completed(),at.round);enter(main(()->field(screen(),"counting")),Integer.toString(puzzle.answer()));click("Check count");}
    private void flags()throws Exception{ChallengePracticeSession current=session();ChallengeRoutine.Position at=main(current::position);FlagPuzzle puzzle=FlagPuzzle.create(current.run,completed(),at.round);click(FlagPuzzle.COUNTRIES.get(puzzle.target));}
    private void mirror()throws Exception{TypedWakeTasks typed=main(()->field(screen(),"typed"));enter(typed.input,MirrorText.phrase(session().run,completed()));click("Check mirrored phrase");}
    private void journal()throws Exception{TypedWakeTasks typed=main(()->field(screen(),"typed"));enter(typed.input,"short");click("Check practice entry");assertEquals(10,completed());capture("journal-invalid");String answer="Today I remember walking outside with quiet friends and watching the bright morning sky together.";enter(typed.input,answer);recreate();TypedWakeTasks restored=main(()->field(screen(),"typed"));assertEquals(answer,main(()->restored.input.getText().toString()));capture("journal-recreated");click("Check practice entry");assertTrue(main(()->session().finished()));}
    private void jump()throws Exception{
        capture("jumping");click("Start or pause jump game");
        for(int obstacle=0;obstacle<3;obstacle++){
            JumpTask task=main(()->field(screen(),"jump"));int before=obstacle;
            i.runOnMainSync(()->{for(int n=0;n<1000&&task.game.obstacleX>250&&!task.game.crashed;n++)clock.tick();});assertFalse(task.game.crashed);click("Jump");
            i.runOnMainSync(()->{for(int n=0;n<1000&&task.game.cleared==before&&!task.game.crashed;n++)clock.tick();});assertFalse("Real game physics must clear the obstacle",task.game.crashed);assertEquals(before+1,task.game.cleared);
        }assertTrue(main(()->session().finished()));assertNull("Real completion cancels its frame owner",clock.frame);
    }
    private void firstRun()throws Exception{
        ChallengeRoutine routine=new ChallengeRoutine(List.of(task("Math",2),task("Memory",1),task("Sequence",2),task("Retype",2),task("Counting",1),task("Flags",1),task("Mirror text",1),task("Dream journal",1)));
        launch(routine);capture("math");View staleAnswer=find("Answer "+main(()->((MathTask)field(screen(),"math")).options.get(0))),staleCheck=find("Check practice answer");checkMath();assertEquals(1,completed());i.runOnMainSync(()->{assertTrue(staleAnswer.performClick());assertTrue(staleCheck.performClick());});assertEquals("Old unit cannot advance its successor",1,completed());mathFocusGuard();capture("math-round2");checkMath();assertEquals(2,completed());capture("memory");memoryPairs(1);
        WakeCognitiveTask.Memory old=(WakeCognitiveTask.Memory)cognitive();int unmatched=0;while((old.matched&(1<<unmatched))!=0)unmatched++;View stale=old.buttons[unmatched];String oldText=main(()->((Button)stale).getText().toString());int matched=old.matched;recreate();WakeCognitiveTask.Memory restored=(WakeCognitiveTask.Memory)cognitive();assertArrayEquals(old.order,restored.order);assertEquals(matched,restored.matched);i.runOnMainSync(()->{assertTrue("Stale enabled card still has its listener",stale.performClick());assertEquals(oldText,((Button)stale).getText().toString());});assertEquals(2,completed());capture("memory-recreated");memoryPairs(3);assertEquals(3,completed());
        sequenceRestoration();retypeCancelResume();capture("counting");counting();assertEquals(8,completed());capture("flags");flags();assertEquals(9,completed());capture("mirror");mirror();assertEquals(10,completed());journal();capture("completed");click("Done");await(()->top==editor&&ready(editor));unchanged();
    }
    private void secondRun()throws Exception{
        launch(new ChallengeRoutine(List.of(task("Visual search",1),task("Type code",1),task("Jumping",1))));capture("search");UiObject2 moon=device.findObject(By.descStartsWith("Sleeping moon, row"));assertNotNull(moon);moon.click();device.waitForIdle();assertEquals(1,completed());
        capture("type-code");TypeCodeTask code=main(()->field(screen(),"code"));enter(code.input,"WRONG");click("Check code");assertEquals(1,completed());enter(code.input,code.puzzle.code.toLowerCase(Locale.ROOT));click("Check code");assertEquals(2,completed());jump();capture("second-completed");click("Done");await(()->top==editor&&ready(editor));unchanged();
    }
    private void randomAndUnsupported()throws Exception{
        launch(ChallengeRoutine.random(List.of(task("Math",1),task("Counting",1),task("Retype",1)),2));String selected=ChallengeRoutineJson.encode(session().routine).toString(),run=session().run;capture("random");recreate();assertEquals(selected,ChallengeRoutineJson.encode(session().routine).toString());assertEquals(run,session().run);capture("random-recreated");
        for(int n=0;n<2;n++){String type=main(()->session().position().task.type);if(type.equals("Math"))checkMath();else if(type.equals("Counting"))counting();else{WakeCognitiveTask.Retype task=(WakeCognitiveTask.Retype)cognitive();enter(task.input,task.phrase);click("Check answer");}}assertTrue(main(()->session().finished()));click("Done");await(()->top==editor&&ready(editor));unchanged();
        launch(new ChallengeRoutine(List.of(task("NFC",1))));assertNull(session());text("Return to routine");assertTrue(device.hasObject(By.textContains("NFC")));capture("unsupported");click("Return to routine");await(()->top==editor&&ready(editor));unchanged();
    }
    private void run()throws Exception{
        assertFalse(AlarmService.active);assertFalse(TrackingService.running);assertFalse(Prefs.flag(c,"ringing"));Map<String,?> originalPrefs=new HashMap<>(Prefs.of(c).getAll());Map<String,JournalWrites.Operation> journalOperations=new HashMap<>(JournalWrites.operations);
        database="practice-"+UUID.randomUUID()+".db";try(Store store=new Store(c,database)){Alarm alarm=new Alarm(1);alarm.enabled=false;alarm.label="Preserved saved alarm";store.save(alarm);stored=store.backup().toString();}
        Records.Factory records=Records.factory;DiaryStore.Factory diary=DiaryStore.factory;AlarmScheduling.Port scheduling=AlarmScheduling.port;JumpTimeline.Factory timeline=JumpTimeline.factory;java.util.concurrent.atomic.AtomicInteger diaryOpens=new java.util.concurrent.atomic.AtomicInteger(),schedules=new java.util.concurrent.atomic.AtomicInteger();boolean[] drained={false};
        Application app=(Application)c.getApplicationContext();Application.ActivityLifecycleCallbacks lifecycle=new Application.ActivityLifecycleCallbacks(){public void onActivityCreated(Activity activity,Bundle saved){owned.add(activity);assertTrue("Only editor and isolated Practice, never AlarmActivity or hardware child",activity instanceof AlarmEditorActivity||activity instanceof ChallengePracticeActivity);assertTrue(activity.getIntent().getBooleanExtra("visualReview",false));}public void onActivityStarted(Activity activity){}public void onActivityResumed(Activity activity){resumed.add(activity);top=activity;}public void onActivityPaused(Activity activity){resumed.remove(activity);}public void onActivityStopped(Activity activity){}public void onActivitySaveInstanceState(Activity activity,Bundle saved){}public void onActivityDestroyed(Activity activity){resumed.remove(activity);}};app.registerActivityLifecycleCallbacks(lifecycle);
        try{
            i.runOnMainSync(()->{Records.factory=context->{recordOpens.incrementAndGet();return new Store(context,database);};DiaryStore.factory=context->{diaryOpens.incrementAndGet();throw new IllegalStateException("Practice may not open the diary");};AlarmScheduling.port=new AlarmScheduling.Port(){public boolean allowed(Context context){return false;}public void schedule(Context context,Alarm alarm){schedules.incrementAndGet();throw new AssertionError("Practice cannot schedule");}};JumpTimeline.factory=()->clock;});
            firstRun();secondRun();randomAndUnsupported();assertEquals(0,diaryOpens.get());assertEquals(0,schedules.get());assertEquals(journalOperations,JournalWrites.operations);assertEquals(originalPrefs,Prefs.of(c).getAll());assertFalse(AlarmService.active);assertFalse(TrackingService.running);assertFalse(Prefs.flag(c,"ringing"));
        }catch(Exception|AssertionError failure){try{File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());device.takeScreenshot(new File(folder,"routine-preview-failure.png"));device.dumpWindowHierarchy(new File(folder,"routine-preview-failure.xml"));}catch(Exception capture){failure.addSuppressed(capture);}throw failure;
        }finally{try{i.runOnMainSync(()->{for(Activity activity:owned)if(!activity.isDestroyed())activity.finish();});await(()->owned.stream().allMatch(Activity::isDestroyed));JournalWrites.drain();drained[0]=true;assertNull(clock.frame);}finally{app.unregisterActivityLifecycleCallbacks(lifecycle);if(owned.stream().allMatch(Activity::isDestroyed)&&drained[0]){i.runOnMainSync(()->{Records.factory=records;DiaryStore.factory=diary;AlarmScheduling.port=scheduling;JumpTimeline.factory=timeline;});assertTrue(c.deleteDatabase(database));}}}
    }
}
