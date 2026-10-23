package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.view.*;
import android.widget.LinearLayout;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.*;
import androidx.test.uiautomator.*;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.util.*;
import static org.junit.Assert.*;

/** Labeled synthetic rendering fixtures in memory only: no sessions/epochs/preferences are written. */
@RunWith(AndroidJUnit4.class)
public class ChartVisualTest {
    @Test public void denseNightGapsSelectionAndEmptySignal() throws Exception {
        Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();Context c=instrumentation.getTargetContext();UiDevice device=UiDevice.getInstance(instrumentation);
        assertFalse(Prefs.flag(c,"ringing"));assertFalse(TrackingService.running);
        Instrumentation.ActivityMonitor monitor=instrumentation.addMonitor(MainActivity.class.getName(),null,false);Activity activity;
        try{c.startActivity(new Intent(c,MainActivity.class).putExtra("visualReview",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));activity=monitor.waitForActivityWithTimeout(5000);assertNotNull(activity);instrumentation.waitForIdleSync();}finally{instrumentation.removeMonitor(monitor);}
        File folder=new File(c.getExternalFilesDir(null),"verification");assertTrue(folder.isDirectory()||folder.mkdirs());
        long start=java.time.LocalDate.of(2026,10,20).atTime(22,30).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),end=start+8*3600000L;
        List<JSONObject> epochs=new ArrayList<>();for(int n=1;n<=480;n++){JSONObject epoch=new JSONObject().put("time",start+n*60000L);
            // A deliberate 24-minute outage plus true zero readings; no continuity is invented.
            if(n<200||n>224)epoch.put("movement",n>=80&&n<=88?0:.03+.13*Math.pow(Math.sin(n*.022),2)+.07*Math.pow(Math.sin(n*.091),6));epochs.add(epoch);}
        JSONObject session=new JSONObject().put("id",-1).put("start",start).put("end",end).put("interrupted",1);
        try{
            instrumentation.runOnMainSync(()->{LinearLayout body=Design.page(activity,false,2,index->activity.recreate());Design.text(body,"VISUAL TEST · synthetic measurements",11,600,Design.BROWN);Design.space(body,10);ReportScreen.render(activity,body,new ReportScreen.Snapshot(session,epochs,0),new ReportScreen.Actions(){public void history(){throw new AssertionError("Fixture navigation is not a product flow");}public void tonight(){activity.recreate();}public void detail(long id){throw new AssertionError("Fixture has no stored record");}});});
            device.waitForIdle();assertTrue(device.takeScreenshot(new File(folder,"chart-dense.png")));
            UiObject2 chart=device.findObject(By.descStartsWith("movement chart."));assertNotNull(chart);chart.click();device.waitForIdle();assertTrue(device.hasObject(By.descContains("average of")));assertTrue(device.takeScreenshot(new File(folder,"chart-selected.png")));
            final RecordChart[] empty={null};instrumentation.runOnMainSync(()->{LinearLayout body=Design.page(activity,false,null,index->{});Design.header(body,"Missing signal",null);Design.text(body,"VISUAL TEST · no measured values",12,600,Design.BROWN);empty[0]=new RecordChart(activity,Collections.emptyList(),"movement",start,end);body.addView(empty[0],new LinearLayout.LayoutParams(-1,Design.dp(activity,200)));assertFalse(empty[0].performAccessibilityAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SCROLL_FORWARD,null));});
            device.waitForIdle();assertTrue(device.hasObject(By.descContains("0 actual readings")));assertTrue(device.takeScreenshot(new File(folder,"chart-empty.png")));
            // Imported finite extremes must draw valid columns rather than overflow to NaN/Infinity.
            instrumentation.runOnMainSync(()->{try{
                List<JSONObject> extreme=Arrays.asList(new JSONObject().put("time",60000).put("movement",Double.MAX_VALUE),new JSONObject().put("time",120000).put("movement",Double.MAX_VALUE));
                RecordChart chartView=new RecordChart(activity,extreme,"movement",0,120000);chartView.layout(0,0,800,400);android.graphics.Bitmap bitmap=android.graphics.Bitmap.createBitmap(800,400,android.graphics.Bitmap.Config.ARGB_8888);chartView.draw(new android.graphics.Canvas(bitmap));int colored=0;
                for(int x=0;x<800;x++)for(int y=0;y<400;y++){int pixel=bitmap.getPixel(x,y);if(android.graphics.Color.alpha(pixel)>100&&android.graphics.Color.blue(pixel)-android.graphics.Color.red(pixel)>30)colored++;}assertTrue("Finite extremes must produce visible violet bars",colored>1000);bitmap.recycle();
                List<JSONObject> scoped=Arrays.asList(new JSONObject().put("time",-1).put("movement",20),new JSONObject().put("time",120000).put("movement",1),new JSONObject().put("time",180000).put("movement",2),new JSONObject().put("time",600001).put("movement",20));
                GraphView detail=new GraphView(activity,scoped,"movement",0,600000);detail.layout(0,0,800,400);assertTrue(detail.getContentDescription().toString().contains("2 actual readings"));assertTrue(detail.performAccessibilityAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SCROLL_FORWARD,null));assertTrue(detail.getContentDescription().toString().contains(RecordChart.clock(120000)));
            }catch(org.json.JSONException e){throw new AssertionError(e);}});
        }finally{instrumentation.runOnMainSync(activity::recreate);instrumentation.waitForIdleSync();}
    }
}
