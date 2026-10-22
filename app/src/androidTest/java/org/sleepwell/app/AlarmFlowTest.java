package org.sleepwell.app;

import android.content.*;
import android.os.SystemClock;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import static org.junit.Assert.*;

/** Controlled tests: own alarm row only, no microphone, no loud escalation. */
@RunWith(AndroidJUnit4.class)
public class AlarmFlowTest {
    private final Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
    private final UiDevice device=UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    private void launch(Class<?> activity,Intent intent){intent.setClass(context,activity).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);context.startActivity(intent);device.waitForIdle();}
    private void click(String text)throws Exception{device.waitForIdle();if(!device.hasObject(By.text(text)))new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().text(text));assertTrue("Visible action: "+text,device.wait(Until.hasObject(By.text(text)),5000));device.findObject(By.text(text)).click();device.waitForIdle();}
    private void capture(String name){File folder=new File(context.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());assertTrue(device.takeScreenshot(new File(folder,name+".png")));}
    @Test public void editorPersistsAndScreenOffMathCompletes()throws Exception{
        assertTrue("Exact-alarm access is required",Scheduler.allowed(context));
        assertFalse("Do not interrupt an active user alarm",Prefs.flag(context,"ringing"));
        Alarm a=new Alarm(1999001);a.hour=6;a.minute=30;a.label="Quiet flow verification";a.enabled=false;a.difficulty=2;a.vibrate=false;a.backupMinutes=0;a.snoozeLimit=0;
        try(Store store=new Store(context)){
            assertNull("Reserved fixture ID must be unused",store.alarm(a.id));
            try{
                launch(AlarmEditorActivity.class,new Intent().putExtra("alarm",a.json().toString()));
                assertTrue(device.wait(Until.hasObject(By.text("Edit alarm")),5000));
                assertNotNull(device.findObject(By.desc("Monday")));device.findObject(By.desc("Monday")).click();
                assertTrue(device.wait(Until.hasObject(By.desc("Monday").selected(true)),5000));capture("05-edit-alarm");click("Save alarm");
                Alarm saved=store.alarm(a.id);assertNotNull(saved);assertTrue(saved.days.contains(1));assertEquals(6,saved.hour);assertEquals(30,saved.minute);assertFalse(saved.enabled);
                // A new activity reads the committed row, not the original draft object.
                launch(AlarmEditorActivity.class,new Intent().putExtra("alarm",store.alarm(a.id).json().toString()));
                assertTrue(device.wait(Until.hasObject(By.desc("Monday").selected(true)),5000));
                device.pressBack();launch(MainActivity.class,new Intent());
                device.findObject(By.desc("Alarms & challenges")).click();device.waitForIdle();capture("04-alarms");
                Scheduler.test(context,saved);
                device.sleep();
                assertTrue("Scheduled full-screen alarm must appear over keyguard",device.wait(Until.hasObject(By.text("Solve to dismiss")),18000));
                assertTrue(Prefs.flag(context,"ringing"));
                Alarm quiet=Alarm.read(new org.json.JSONObject(Prefs.text(context,"active_alarm","{}")));assertTrue(quiet.volume<=25);assertFalse(quiet.vibrate);assertEquals(0,quiet.backupMinutes);
                MathTask task=MathTask.decode(Prefs.text(context,"mathTask",""));
                int wrong=task.options.stream().filter(x->!task.accepts(x)).findFirst().get();
                capture("03-before-answer");device.findObject(By.desc("Answer "+wrong)).click();device.waitForIdle();capture("03-selected-wrong");click("Check answer");assertTrue(Prefs.flag(context,"ringing"));assertEquals(0,Prefs.number(context,"challengeCompleted",0));
                capture("03-wrong-answer");
                // Reopening preserves the current question and order.
                String before=Prefs.text(context,"mathTask","");launch(AlarmActivity.class,new Intent());assertEquals(before,Prefs.text(context,"mathTask",""));
                device.findObject(By.desc("Answer "+(task.left+task.right))).click();capture("03-wake-challenge");click("Check answer");
                assertEquals(1,Prefs.number(context,"challengeCompleted",0));assertTrue(Prefs.flag(context,"ringing"));
                MathTask second=MathTask.decode(Prefs.text(context,"mathTask",""));device.findObject(By.desc("Answer "+(second.left+second.right))).click();click("Check answer");
                long until=SystemClock.elapsedRealtime()+3000;while(Prefs.flag(context,"ringing")&&SystemClock.elapsedRealtime()<until)SystemClock.sleep(50);
                assertFalse("All rounds must stop the real alarm service",Prefs.flag(context,"ringing"));assertEquals(2,Prefs.number(context,"challengeCompleted",0));
                assertEquals("Stored alarm is unchanged by quiet test",2,store.alarm(a.id).difficulty);
            }finally{Scheduler.cancel(context,-1,Scheduler.TEST);Scheduler.cancel(context,-1,Scheduler.BACKUP);AlarmService.stop(context);store.deleteAlarm(a.id);}
        }
    }
}
