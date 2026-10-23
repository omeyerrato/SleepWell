package org.sleepwell.app;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.json.JSONObject;
import org.json.JSONException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import static org.junit.Assert.*;

/** Isolated storage and unattached native Views only; no activities or hardware actions. */
@RunWith(AndroidJUnit4.class)
public class RecordReplayTest {
    private Context context(){return InstrumentationRegistry.getInstrumentation().getTargetContext();}
    private String database(){return "sleepwell-replay-verification-"+UUID.randomUUID()+".db";}
    private long time(String value){return Instant.parse(value).toEpochMilli();}
    private RecordDraft draft(){
        RecordDraft d=new RecordDraft(time("2020-10-20T22:00:00Z"),time("2020-10-21T06:00:00Z"));
        d.note="First committed manual draft";d.addTag("Replay verification");return d;
    }

    @Test public void manualCreateReplaySurvivesReopenWithoutDuplicatingOrOverwriting() throws Exception {
        String name=database(),token=UUID.randomUUID().toString();long id;String original;
        try{
            try(Store store=new Store(context(),name)){
                assertTrue("The replay table must be migrated before this test",store.getReadableDatabase().getVersion()>=4);
                id=Records.create(store,draft(),ZoneId.of("UTC"),System.currentTimeMillis(),token);
                original=store.backup().toString();
            }
            try(Store reopened=new Store(context(),name)){
                assertEquals(id,Records.create(reopened,draft(),ZoneId.of("UTC"),System.currentTimeMillis(),token));
                RecordDraft changed=draft();changed.note="A recreated editor must not overwrite the committed request";
                changed.start+=3600000;
                assertEquals(id,Records.create(reopened,changed,ZoneId.of("Europe/Berlin"),System.currentTimeMillis(),token));
                assertEquals("A replay must leave committed rows unchanged",original,reopened.backup().toString());
                assertEquals(1,reopened.rows("SELECT id FROM sessions").size());
                assertEquals(1,reopened.rows("SELECT session FROM record_edits").size());
                assertEquals("First committed manual draft",Records.get(reopened,id).edit.note);
                assertTrue(reopened.rows("SELECT id FROM epochs").isEmpty());
                assertTrue(reopened.rows("SELECT id FROM tracking_pauses").isEmpty());
            }
        }finally{context().deleteDatabase(name);}
    }

    @Test public void failedCreateRollsBackRequestAndCanRetryExactlyOnce() throws Exception {
        String name=database(),token=UUID.randomUUID().toString();
        try(Store store=new Store(context(),name)){
            store.getWritableDatabase().execSQL("CREATE TRIGGER verification_reject_create BEFORE INSERT ON record_edits BEGIN SELECT RAISE(ABORT,'Injected create failure'); END");
            try{Records.create(store,draft(),ZoneId.of("UTC"),System.currentTimeMillis(),token);fail("Injected failure must abort creation");}
            catch(SQLiteException expected){}
            assertTrue("A failed request must leave no partial session",store.rows("SELECT id FROM sessions").isEmpty());
            assertTrue(store.rows("SELECT session FROM record_edits").isEmpty());
            store.getWritableDatabase().execSQL("DROP TRIGGER verification_reject_create");
            long id=Records.create(store,draft(),ZoneId.of("UTC"),System.currentTimeMillis(),token);
            assertEquals(id,Records.create(store,draft(),ZoneId.of("UTC"),System.currentTimeMillis(),token));
            assertEquals(1,store.rows("SELECT id FROM sessions").size());
            assertEquals(1,store.rows("SELECT session FROM record_edits").size());
        }finally{context().deleteDatabase(name);}
    }

    @Test public void deletedCreateRequestCannotResurrectOrResolveToAReusedId() throws Exception {
        for(boolean eraseAll:new boolean[]{false,true}){
            String name=database(),token=UUID.randomUUID().toString();long deletedId;
            try{
                try(Store store=new Store(context(),name)){
                    deletedId=Records.create(store,draft(),ZoneId.of("UTC"),System.currentTimeMillis(),token);
                    if(eraseAll)store.eraseHistory();else Records.delete(store,deletedId,Records.get(store,deletedId).updated);
                    rejectsReplay(store,token);
                    RecordDraft replacement=draft();replacement.note="Unrelated replacement record";
                    long replacementId=Records.create(store,replacement,ZoneId.of("UTC"),System.currentTimeMillis(),UUID.randomUUID().toString());
                    assertEquals("Exercise SQLite's deleted maximum ID reuse",deletedId,replacementId);
                }
                try(Store reopened=new Store(context(),name)){
                    String before=reopened.backup().toString();
                    rejectsReplay(reopened,token);
                    assertEquals(before,reopened.backup().toString());
                    assertEquals(1,reopened.rows("SELECT id FROM sessions").size());
                    assertEquals("Unrelated replacement record",Records.get(reopened,deletedId).edit.note);
                }
            }finally{context().deleteDatabase(name);}
        }
    }

    private void rejectsReplay(Store store,String token){
        try{Records.create(store,draft(),ZoneId.of("UTC"),System.currentTimeMillis(),token);fail("A deleted creation must not be replayed");}
        catch(IllegalArgumentException expected){}
    }

    @Test public void manualRevertedOrZeroLengthOriginalRejectsBeforeReplacingAnyData() throws Exception {
        String from=database(),to=database();
        try(Store source=new Store(context(),from);Store target=new Store(context(),to)){
            Records.create(source,draft(),ZoneId.of("UTC"),System.currentTimeMillis());
            RecordDraft protectedDraft=draft();protectedDraft.note="Existing target must survive rejected restore";
            Records.create(target,protectedDraft,ZoneId.of("UTC"),System.currentTimeMillis());
            JSONObject valid=source.backup();Backup.validate(valid);
            String original=target.backup().toString(),requests=target.rows("SELECT * FROM record_create_requests ORDER BY token").toString();
            for(boolean reverted:new boolean[]{false,true}){
                JSONObject bad=new JSONObject(valid.toString());
                if(reverted)bad.getJSONArray("edits").getJSONObject(0).put("reverted",1);
                else{JSONObject raw=bad.getJSONArray("sessions").getJSONObject(0);raw.put("end",raw.getLong("start"));}
                try{Backup.restoreData(target,bad);fail("Invalid manual original/reversion must reject before replacement");}
                catch(JSONException expected){}
                assertEquals(original,target.backup().toString());
                assertEquals("Rejected restore must not clear saved creation identities",requests,target.rows("SELECT * FROM record_create_requests ORDER BY token").toString());
            }
        }finally{context().deleteDatabase(from);context().deleteDatabase(to);}
    }

    @Test public void manualTimelineCanDragAfterBothBoundsMoveBeyondOriginalCapture(){
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            RecordDraft original=draft();
            RecordEntry record=new RecordEntry(1,original.start,original.end,null,"manual","local","UTC",0,0,original,true,1,List.of());
            RecordDraft expanded=original.copy();
            expanded.start=time("2020-10-21T10:00:00Z");expanded.end=time("2020-10-21T12:00:00Z");
            FrameLayout parent=new FrameLayout(context());
            PeriodTimeline timeline=new PeriodTimeline(context(),record,expanded,(start,end)->{});
            int width=Design.dp(context(),400),height=Design.dp(context(),122);
            parent.addView(timeline,new FrameLayout.LayoutParams(width,height));
            parent.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));
            parent.layout(0,0,width,height);
            long now=SystemClock.uptimeMillis();
            touch(timeline,now,MotionEvent.ACTION_DOWN,width*.95f,height*.5f);
            touch(timeline,now+20,MotionEvent.ACTION_MOVE,width*.90f,height*.5f);
            touch(timeline,now+40,MotionEvent.ACTION_UP,width*.90f,height*.5f);
            assertEquals("Dragging End must preserve the edited Start",time("2020-10-21T10:00:00Z"),expanded.start);
            assertNotEquals("The manual handle must respond rather than silently ignoring the drag",time("2020-10-21T12:00:00Z"),expanded.end);
            assertTrue("Manual End must not clamp back into the immutable original",expanded.end>record.rawEnd);
            assertTrue("Dragging must keep a positive interval",expanded.end>expanded.start);
            expanded.validate(record.rawStart,record.rawEnd,true,System.currentTimeMillis());
        });
    }

    private void touch(PeriodTimeline view,long at,int action,float x,float y){
        MotionEvent event=MotionEvent.obtain(at,at,action,x,y,0);
        try{assertTrue(view.onTouchEvent(event));}finally{event.recycle();}
    }
}
