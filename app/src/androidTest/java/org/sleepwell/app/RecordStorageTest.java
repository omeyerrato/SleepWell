package org.sleepwell.app;

import android.content.Context;
import android.database.sqlite.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.json.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.time.*;
import java.util.*;
import static org.junit.Assert.*;

/** Isolated UUID databases only. No personal rows, preferences, alarms or hardware actions. */
@RunWith(AndroidJUnit4.class)
public class RecordStorageTest {
    private Context c(){return InstrumentationRegistry.getInstrumentation().getTargetContext();}
    private String name(){return "sleepwell-record-verification-"+UUID.randomUUID()+".db";}
    private long start(){return Instant.parse("2020-10-20T22:00:00Z").toEpochMilli();}
    private long tracked(Store s){long start=start(),end=start+8*3600000L;s.getWritableDatabase().execSQL("INSERT INTO sessions(id,start,end,active_ms,note,rating,source,origin,zone,clock_changed) VALUES(1,?,?,?,'original #coffee',3,'tracked','local','UTC',1)",new Object[]{start,end,450*60000L});s.pause(1,start+4*3600000L);s.resume(1,start+270*60000L);s.epoch(1,start,start+60000,.1,null,null,300);s.epoch(1,start+60000,start+120000,.2,null,null,300);return 1;}
    private String raw(Store s){JSONObject out=new JSONObject();for(String table:new String[]{"sessions","epochs","tracking_pauses","clips","alarms"})try{out.put(table,new JSONArray(s.rows("SELECT * FROM "+table+" ORDER BY id")));}catch(JSONException e){throw new IllegalStateException(e);}return out.toString();}
    @Test public void editsPreserveRawRowsAndDriveDisplayCsvAndTrends() throws Exception {
        String name=name();try(Store s=new Store(c(),name)){tracked(s);String original=raw(s);RecordEntry before=Records.get(s,1);RecordDraft d=before.edit.copy();d.start+=3600000;d.awake.add(new RecordIntervals.Span(start()+2*3600000,start()+150*60000));d.note="edited, \"dream\"\nsecond line";d.addTag("Late meal");d.rated=true;d.rating=0;Records.save(s,1,d,before.updated,System.currentTimeMillis());RecordEntry saved=Records.get(s,1);assertEquals(360*60000L,saved.duration());assertTrue(saved.edit.rated);assertEquals(0,saved.edit.rating);assertEquals(original,raw(s));JSONObject display=Records.display(saved);assertEquals(1,display.getInt("clock_changed"));assertTrue(display.getBoolean("edited"));assertEquals(d.start,display.getLong("start"));String csv=Records.csv(s);assertTrue(csv.contains(",360,0,0,"));assertTrue(csv.contains("Late meal"));SortedMap<LocalDate,RecordTrends.Day> days=RecordTrends.days(Records.all(s),RecordTrends.range(LocalDate.parse("2020-10-21"),0),0);assertEquals(saved.duration(),days.get(LocalDate.parse("2020-10-21")).duration());}
        finally{c().deleteDatabase(name);}
    }
    @Test public void resetKeepsRevisionAndRejectsStaleSaveAndDelete() throws Exception {
        String name=name();try(Store s=new Store(c(),name)){tracked(s);RecordEntry first=Records.get(s,1);RecordDraft edited=first.edit.copy();edited.note="changed";Records.save(s,1,edited,first.updated,System.currentTimeMillis());RecordEntry saved=Records.get(s,1);Records.reset(s,1,saved.updated);RecordEntry reset=Records.get(s,1);assertFalse(reset.edited);assertTrue(reset.updated>saved.updated);assertEquals("original #coffee",reset.edit.note);for(boolean delete:new boolean[]{false,true})try{if(delete)Records.delete(s,1,first.updated);else Records.save(s,1,edited,first.updated,System.currentTimeMillis());fail("Stale revision must reject after reset");}catch(IllegalArgumentException expected){}assertEquals(1,s.rows("SELECT * FROM sessions").size());Backup.validate(s.backup());}
        finally{c().deleteDatabase(name);}
    }
    @Test public void backupThreeRoundTripAndMalformedEditRejectsBeforeReplacement() throws Exception {
        String from=name(),to=name();try(Store a=new Store(c(),from);Store b=new Store(c(),to)){tracked(a);RecordEntry r=Records.get(a,1);RecordDraft d=r.edit.copy();d.note="round trip";d.addTag("Exercise");d.awake.add(new RecordIntervals.Span(start()+3600000,start()+4200000));Records.save(a,1,d,r.updated,System.currentTimeMillis());JSONObject backup=a.backup();Backup.restoreData(b,backup);RecordEntry restored=Records.get(b,1);assertEquals("imported",restored.origin);assertEquals("tracked",restored.source);assertEquals("UTC",restored.zone);assertEquals(Records.get(a,1).duration(),restored.duration());assertEquals(d.tags,restored.edit.tags);assertEquals(1,restored.edit.awake.size());assertEquals(1,restored.clockChanged);String before=b.backup().toString();JSONObject bad=new JSONObject(backup.toString());bad.getJSONArray("edits").getJSONObject(0).put("end",start()+9*3600000L);try{Backup.restoreData(b,bad);fail();}catch(JSONException expected){}assertEquals(before,b.backup().toString());Records.reset(a,1,Records.get(a,1).updated);Backup.restoreData(b,a.backup());assertFalse(Records.get(b,1).edited);assertTrue(Records.get(b,1).updated>0);}
        finally{c().deleteDatabase(from);c().deleteDatabase(to);}
    }
    @Test public void manualImportsCannotAcquireMeasuredMetadata() throws Exception {
        String source=name(),target=name();try(Store a=new Store(c(),source);Store b=new Store(c(),target)){RecordDraft d=new RecordDraft(start(),start()+8*3600000);long id=Records.create(a,d,ZoneId.of("UTC"),System.currentTimeMillis());JSONObject valid=a.backup();Backup.restoreData(b,valid);assertTrue(Records.get(b,id).manual());assertEquals(8*3600000,Records.get(b,id).duration());String before=b.backup().toString();
            for(int mode=0;mode<6;mode++){JSONObject bad=new JSONObject(valid.toString());if(mode==0)bad.getJSONArray("sessions").getJSONObject(0).put("active_ms",1);if(mode==1)bad.getJSONArray("epochs").put(new JSONObject().put("session",id).put("time",start()+1000).put("movement",.1));if(mode==2)bad.getJSONArray("pauses").put(new JSONObject().put("id",1).put("session",id).put("start",start()).put("end",start()+1000));if(mode==3)bad.put("edits",new JSONArray());if(mode==4)bad.getJSONArray("sessions").getJSONObject(0).put("zone","Unknown/Not_A_Zone");if(mode==5)bad.getJSONArray("edits").getJSONObject(0).put("session",999);try{Backup.restoreData(b,bad);fail("Bad manual metadata must reject: "+mode);}catch(JSONException expected){}assertEquals(before,b.backup().toString());}}
        finally{c().deleteDatabase(source);c().deleteDatabase(target);}
    }
    @Test public void activeRecordsCannotBeEditedDeletedOrReset() throws Exception {
        String name=name();try(Store s=new Store(c(),name)){long id=s.begin();for(int action=0;action<3;action++)try{if(action==0)Records.save(s,id,new RecordDraft(start(),start()+1000),0,System.currentTimeMillis());if(action==1)Records.delete(s,id,0);if(action==2)Records.reset(s,id,0);fail();}catch(IllegalArgumentException expected){}assertEquals(1,s.rows("SELECT * FROM sessions").size());assertTrue(s.rows("SELECT * FROM record_edits").isEmpty());}
        finally{c().deleteDatabase(name);}
    }
    @Test public void failedOverlayInsertLeavesPriorEditAndRawDataUntouched() throws Exception {
        String name=name();try(Store s=new Store(c(),name)){tracked(s);RecordEntry first=Records.get(s,1);RecordDraft one=first.edit.copy();one.note="first";Records.save(s,1,one,first.updated,System.currentTimeMillis());String before=s.backup().toString();s.getWritableDatabase().execSQL("CREATE TRIGGER verification_reject_edit BEFORE INSERT ON record_edits BEGIN SELECT RAISE(ABORT,'Injected edit failure'); END");RecordEntry saved=Records.get(s,1);RecordDraft two=saved.edit.copy();two.note="should not save";try{Records.save(s,1,two,saved.updated,System.currentTimeMillis());fail();}catch(SQLiteException expected){}assertEquals(before,s.backup().toString());}
        finally{c().deleteDatabase(name);}
    }
    @Test public void legacyFormatTwoKeepsUnknownOriginZoneAndExistingDurations() throws Exception {
        String source=name(),target=name();try(Store a=new Store(c(),source);Store b=new Store(c(),target)){tracked(a);JSONObject old=a.backup();old.put("format",2);old.remove("edits");for(int i=0;i<old.getJSONArray("sessions").length();i++){JSONObject s=old.getJSONArray("sessions").getJSONObject(i);s.remove("source");s.remove("origin");s.remove("zone");}Backup.restoreData(b,old);RecordEntry r=Records.get(b,1);assertEquals("legacy",r.source);assertEquals("imported",r.origin);assertEquals("",r.zone);assertEquals(450*60000L,r.duration());assertEquals(List.of("coffee"),r.edit.tags);}
        finally{c().deleteDatabase(source);c().deleteDatabase(target);}
    }
}
