package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.LinearLayout;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.*;
import androidx.test.uiautomator.UiDevice;
import org.json.JSONObject;
import org.junit.Test;
import java.io.File;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.Assert.*;

/** In-memory, labelled chart fixtures. Disposable CI emulator only; no output services. */
public final class ChartClockFlowTest {
    private final Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
    private final Context context=instrumentation.getTargetContext();
    private final UiDevice device=UiDevice.getInstance(instrumentation);
    private boolean ready(Activity a,View decor){return !a.isDestroyed()&&!a.isFinishing()&&ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).contains(a)&&a.getWindow().getDecorView()==decor&&a.hasWindowFocus()&&decor.hasWindowFocus()&&decor.isAttachedToWindow()&&decor.isLaidOut()&&decor.getWidth()>0&&decor.getHeight()>0;}
    private void focusChart(Activity owner,RecordChart chart){long until=SystemClock.elapsedRealtime()+8000;boolean[] valid={false};while(!valid[0]&&SystemClock.elapsedRealtime()<until){instrumentation.runOnMainSync(()->{View decor=owner.getWindow().getDecorView();if(ready(owner,decor)&&chart.getRootView()==decor&&chart.isAttachedToWindow()&&chart.isLaidOut()&&chart.getWidth()>0&&chart.getHeight()>0){
            // A new content tree cannot take keyboard focus before attachment/layout.
            // Use the platform focus request to leave touch mode, not a test-only
            // focusable flag or a removed retention assertion.
            valid[0]=chart.requestFocusFromTouch()&&chart.hasFocus();
        }});if(!valid[0])SystemClock.sleep(50);}assertTrue("The focused resumed owner must give its actual laid-out chart keyboard focus",valid[0]);}
    private void capture(Activity a,String name)throws Exception{
        long until=SystemClock.elapsedRealtime()+8000;boolean committed=false;
        while(!committed&&SystemClock.elapsedRealtime()<until){CountDownLatch frame=new CountDownLatch(1);View[] root={null};instrumentation.runOnMainSync(()->{View decor=a.getWindow().getDecorView();if(ready(a,decor)){assertTrue(decor.isHardwareAccelerated());root[0]=decor;decor.getViewTreeObserver().registerFrameCommitCallback(frame::countDown);decor.invalidate();}});if(root[0]==null){SystemClock.sleep(50);continue;}if(!frame.await(1,TimeUnit.SECONDS))continue;device.waitForIdle();boolean[] valid={false};instrumentation.runOnMainSync(()->valid[0]=ready(a,root[0]));committed=valid[0];}
        assertTrue("Focused fixture frame must commit: "+name,committed);File folder=new File(context.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());assertTrue(device.takeScreenshot(new File(folder,"chart-clock-"+name+".png")));
    }
    private List<JSONObject> samples(long start,long end,long cadence)throws Exception{List<JSONObject> values=new ArrayList<>();int count=(int)((end-start)/cadence);for(int n=1;n<=count;n++){JSONObject value=new JSONObject().put("start",start+(n-1)*cadence).put("time",start+n*cadence);if(n<count/3||n>count/3+Math.max(1,count/20))value.put("movement",n%19==0?0:.02+.09*(1+Math.sin(n*.19))+.1*(n%47<3?1:0));values.add(value);}return values;}
    private RecordChart render(Activity a,String title,List<JSONObject> values,long start,long end,ZoneId zone,boolean night){RecordChart[] chart={null};instrumentation.runOnMainSync(()->{LinearLayout body=Design.page(a,false,null,null);Design.header(body,title,null);Design.text(body,"VISUAL TEST · synthetic measurements in memory",11,600,Design.BROWN);Design.space(body,20);LinearLayout panel=Design.col(a);Design.pad(panel,night?12:0,night?12:0);if(night)panel.setBackground(Design.shape(a,Design.PLUM,20));Design.text(panel,"Recorded movement",16,600,night?Design.LILAC:Design.INK);Design.space(panel,12);chart[0]=new RecordChart(a,values,"movement",start,end,night,zone);panel.addView(chart[0],new LinearLayout.LayoutParams(-1,Design.dp(a,230)));body.addView(panel);Design.space(body,16);Design.text(body,"Missing intervals stay blank. Tap a column to inspect its measured interval.",13,400,Design.MUTED);});focusChart(a,chart[0]);return chart[0];}
    @Test public void alignedClocksElapsedSecondsAndFoldSelectionRenderWithoutEffects()throws Exception{
        assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertEquals("1",device.executeShellCommand("getprop ro.kernel.qemu").trim());assertFalse(Prefs.flag(context,"ringing"));assertFalse(AlarmService.active);assertFalse(TrackingService.running);assertFalse(Prefs.flag(context,"tracking"));assertFalse(Prefs.flag(context,"trackingPaused"));
        Map<String,?> before=new HashMap<>(Prefs.of(context).getAll());String database="sleepwell-chart-clock-"+UUID.randomUUID()+".db";Records.Factory previous=Records.factory;Activity[] owner={null};
        try{
            Records.factory=c->new Store(c,database);try(Store store=new Store(context,database)){store.getWritableDatabase().execSQL("INSERT INTO sessions(id,start,end,active_ms,note,source,origin,zone) VALUES(1,1000,2000,1000,'Isolated chart owner','tracked','local','UTC')");}
            Instrumentation.ActivityMonitor monitor=instrumentation.addMonitor(RecordReportActivity.class.getName(),null,false);try{context.startActivity(new Intent(context,RecordReportActivity.class).putExtra("record",1L).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));owner[0]=monitor.waitForActivityWithTimeout(8000);assertNotNull(owner[0]);}finally{instrumentation.removeMonitor(monitor);}
            ((RecordReportActivity)owner[0]).awaitReaders(8000);instrumentation.waitForIdleSync();
            ZoneId utc=ZoneOffset.UTC;long start=Instant.parse("2026-10-22T22:48:00Z").toEpochMilli(),end=Instant.parse("2026-10-23T06:30:00Z").toEpochMilli();List<JSONObject> overnight=samples(start,end,60000);RecordChart chart=render(owner[0],"Overnight clock axis",overnight,start,end,utc,false);
            instrumentation.runOnMainSync(()->{assertTrue(chart.performAccessibilityAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD,null));String selected=chart.getContentDescription().toString();assertTrue(selected.contains("movement at"));chart.updateData(overnight,end);assertEquals(selected,chart.getContentDescription().toString());assertTrue(chart.hasFocus());assertTrue(chart.isAttachedToWindow());});capture(owner[0],"overnight");
            render(owner[0],"Live signal clock axis",overnight,start,end,utc,true);capture(owner[0],"night");
            long shortEnd=start+21321;render(owner[0],"Short measured signal",samples(start,shortEnd,1000),start,shortEnd,utc,false);capture(owner[0],"short");
            ZoneId berlin=ZoneId.of("Europe/Berlin");long foldStart=ZonedDateTime.parse("2026-10-25T00:30:00+02:00[Europe/Berlin]").toInstant().toEpochMilli(),foldEnd=ZonedDateTime.parse("2026-10-25T03:30:00+01:00[Europe/Berlin]").toInstant().toEpochMilli();RecordChart fold=render(owner[0],"Repeated-hour clock axis",samples(foldStart,foldEnd,60000),foldStart,foldEnd,berlin,false);
            instrumentation.runOnMainSync(()->{boolean first=false,second=false;while(fold.performAccessibilityAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD,null)){String label=fold.getContentDescription().toString();first|=label.contains("+02:00");second|=label.contains("+01:00");if(first&&second)break;}assertTrue("First occurrence must expose its UTC offset",first);assertTrue("Repeated occurrence must expose its distinct UTC offset",second);});capture(owner[0],"fold");assertEquals("Chart fixture must not write personal preferences",before,Prefs.of(context).getAll());
        }finally{
            try{if(owner[0]!=null){instrumentation.runOnMainSync(owner[0]::finish);long until=SystemClock.elapsedRealtime()+8000;while(!owner[0].isDestroyed()&&SystemClock.elapsedRealtime()<until)SystemClock.sleep(50);assertTrue(owner[0].isDestroyed());((RecordReportActivity)owner[0]).awaitReaders(8000);}}finally{Records.factory=previous;context.deleteDatabase(database);assertEquals("Chart cleanup must not write personal preferences",before,Prefs.of(context).getAll());}
        }
    }
}
