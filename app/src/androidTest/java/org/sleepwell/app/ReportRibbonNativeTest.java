package org.sleepwell.app;

import android.content.*;
import android.graphics.*;
import android.os.Build;
import android.view.View;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import org.json.JSONObject;
import org.junit.Test;
import java.time.*;
import java.util.*;
import static org.junit.Assert.*;

/** Real private SQLite and native Canvas; no services, sensor, media or output effects. */
public class ReportRibbonNativeTest {
    private final android.app.Instrumentation i=InstrumentationRegistry.getInstrumentation();private final Context c=i.getTargetContext();
    private final long start=Instant.parse("2026-10-12T22:00:00Z").toEpochMilli(),end=start+4*3600000L;
    private void guard()throws Exception{assertEquals("true",InstrumentationRegistry.getArguments().getString("ciReview"));assertTrue(Build.HARDWARE.contains("ranchu")||Build.HARDWARE.contains("goldfish"));assertEquals("1",UiDevice.getInstance(i).executeShellCommand("getprop ro.kernel.qemu").trim());assertFalse(AlarmService.active);assertFalse(TrackingService.running);assertFalse(Prefs.flag(c,"ringing"));assertFalse(Prefs.flag(c,"tracking"));}
    private final class Fixture implements AutoCloseable {
        final String name="ribbon-"+UUID.randomUUID()+".db";final Store store=new Store(c,name);final Map<String,?> prefs=Prefs.of(c).getAll();final RecordEntry record;final List<JSONObject> epochs;final String original;
        Fixture(boolean manual){store.getWritableDatabase().execSQL("INSERT INTO sessions(id,start,end,active_ms,source,origin,zone) VALUES(1,?,?,?,?,?,'UTC')",new Object[]{start,end,manual?null:210*60000L,manual?"manual":"tracked","local"});if(!manual){store.pause(1,start+60*60000L);store.resume(1,start+90*60000L);}for(int minute=0;minute<240;minute++){if(!manual&&(minute>=30&&minute<45||minute>=60&&minute<90))continue;double motion=minute<120?.05:.8;store.epoch(1,start+minute*60000L,start+(minute+1)*60000L,motion,null,null,300);}RecordEntry initial=Records.get(store,1);RecordDraft draft=initial.edit.copy();draft.awake.add(new RecordIntervals.Span(start+120*60000L,start+150*60000L));Records.save(store,1,draft,initial.updated,System.currentTimeMillis());record=Records.get(store,1);epochs=store.rows("SELECT start,time,movement FROM epochs WHERE session=1 ORDER BY time");original=raw();}
        String raw(){return store.rows("SELECT * FROM sessions").toString()+store.rows("SELECT * FROM epochs ORDER BY time").toString()+store.rows("SELECT * FROM tracking_pauses ORDER BY id").toString()+store.rows("SELECT * FROM record_edits").toString();}
        public void close(){assertEquals("Rendering cannot modify evidence or edits",original,raw());store.close();c.deleteDatabase(name);assertEquals(prefs,Prefs.of(c).getAll());}
    }
    private Bitmap ribbon(Fixture f){Bitmap[] bitmap={null};i.runOnMainSync(()->{RecordRibbon view=new RecordRibbon(c,f.record,f.epochs);view.layout(0,0,800,32);bitmap[0]=Bitmap.createBitmap(800,32,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(bitmap[0]);canvas.drawColor(Design.CREAM);view.draw(canvas);});return bitmap[0];}
    private Bitmap timeline(Fixture f){Bitmap[] bitmap={null};i.runOnMainSync(()->{int height=Design.dp(c,116);PeriodTimeline view=new PeriodTimeline(c,f.record,f.record.edit.copy(),f.epochs,(a,b)->{throw new AssertionError("Drawing must not edit bounds");});assertTrue(view.getContentDescription().toString().contains(f.record.manual()?"User-reported coverage":"measured relative movement"));view.layout(0,0,800,height);bitmap[0]=Bitmap.createBitmap(800,height,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(bitmap[0]);canvas.drawColor(Design.CREAM);view.draw(canvas);});return bitmap[0];}
    private int historyX(int minute){return Math.round(800*minute/240f);}
    private int editorX(int minute){float inset=Design.dp(c,10);return Math.round(inset+(800-2*inset)*(minute+60)/300f);}
    @Test public void trackedHistoryAndEditorUseMeasuredVariationAndKeepPausedAwakeGaps()throws Exception{guard();try(Fixture f=new Fixture(false)){RecordRibbon.Signal compact=new RecordRibbon.Signal(f.record,f.epochs,16,start,end);assertTrue(compact.measured());assertTrue("Measured patches aggregate a dense night",compact.movement.size()<f.epochs.size()/4);Bitmap history=ribbon(f),editor=timeline(f);try{assertNotEquals(history.getPixel(historyX(15),16),history.getPixel(historyX(165),16));assertEquals("Missing signal must retain unshaded recorded coverage",0xffd1bcf7,history.getPixel(historyX(37),16));assertEquals("Paused history gap",Design.CREAM,history.getPixel(historyX(75),16));assertEquals("Awake history gap",Design.CREAM,history.getPixel(historyX(135),16));int y=Design.dp(c,64);assertEquals("Editor must not blend signal shading across a missing interval",0xffd1bcf7,editor.getPixel(editorX(37),y));assertNotEquals(editor.getPixel(editorX(15),y),editor.getPixel(editorX(165),y));assertEquals("Paused editor gap",Design.SOFT,editor.getPixel(editorX(75),y));assertEquals("Awake editor gap stays separate from annotation",Design.SOFT,editor.getPixel(editorX(135),y));}finally{history.recycle();editor.recycle();}}guard();}
    @Test public void manualCoverageNeverAdoptsAttachedMeasurementShading()throws Exception{guard();try(Fixture f=new Fixture(true)){assertFalse(new RecordRibbon.Signal(f.record,f.epochs,16,start,end).measured());Bitmap history=ribbon(f),editor=timeline(f);try{assertEquals(history.getPixel(historyX(15),16),history.getPixel(historyX(165),16));assertEquals(Design.CREAM,history.getPixel(historyX(135),16));int y=Design.dp(c,64);assertEquals(editor.getPixel(editorX(15),y),editor.getPixel(editorX(165),y));assertEquals(Design.SOFT,editor.getPixel(editorX(135),y));}finally{history.recycle();editor.recycle();}}guard();}
}
