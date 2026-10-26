package org.sleepwell.app;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.BooleanSupplier;
import org.json.*;

/** Reviewed append-only import. Conflicts are kept, never overwritten or silently reidentified. */
final class SleepCsvImport {
    enum Kind {NEW,IDENTICAL,SOURCE_CONFLICT,PERIOD_CONFLICT,DELETED}
    static final class Item {final SleepCsvFormat.Entry entry;final Kind kind;final String reason;final long current;Item(SleepCsvFormat.Entry entry,Kind kind,String reason,long current){this.entry=entry;this.kind=kind;this.reason=reason;this.current=current;}boolean eligible(){return kind==Kind.NEW;}}
    static final class Review {final List<Item> items;final String fingerprint,sha256;final int repeated;final long admission;Review(List<Item> items,String fingerprint,String sha256,int repeated,long admission){this.items=List.copyOf(items);this.fingerprint=fingerprint;this.sha256=sha256;this.repeated=repeated;this.admission=admission;}int count(Kind kind){int count=0;for(Item item:items)if(item.kind==kind)count++;return count;}}
    interface Commit {void begin();void committed();}
    static String token(String id){return "sleep-as-android-csv:v1:"+id;}
    static Review prepare(Context context,Store store,byte[] bytes,BooleanSupplier active,long admission)throws Exception {
        BackupRuntime.requireIdle(context,admission);SleepCsvFormat.Document document=SleepCsvFormat.parse(bytes,System.currentTimeMillis(),active);SQLiteDatabase db=store.getReadableDatabase();db.beginTransactionNonExclusive();try{
            String fingerprint=fingerprint(store,active);Map<String,JSONObject> imported=new HashMap<>(),requests=new HashMap<>();for(JSONObject row:store.rows("SELECT session,source_id,csv FROM sleep_csv_sources"))imported.put(row.getString("source_id"),row);for(JSONObject row:store.rows("SELECT token,session FROM record_create_requests WHERE token LIKE 'sleep-as-android-csv:v1:%'"))requests.put(row.getString("token"),row);List<JSONObject> periods=store.rows("SELECT id,start,end FROM sessions WHERE end IS NOT NULL ORDER BY id");Map<String,Long> exact=new HashMap<>();for(JSONObject row:periods)exact.putIfAbsent(row.getLong("start")+":"+row.getLong("end"),row.getLong("id"));List<Item> items=new ArrayList<>();
            for(SleepCsvFormat.Entry entry:document.entries){NightAudioArchive.active(active);JSONObject previous=imported.get(entry.sourceId),request=requests.get(token(entry.sourceId));if(previous!=null){SleepCsvFormat.Entry saved=SleepCsvFormat.one(previous.getString("csv"));boolean identical=saved.contentKey.equals(entry.contentKey);items.add(new Item(entry,identical?Kind.IDENTICAL:Kind.SOURCE_CONFLICT,identical?"Already imported. Current edits are kept.":"Same vendor ID has different source contents. Current data is kept.",previous.getLong("session")));}
                else if(request!=null){long id=request.getLong("session");items.add(new Item(entry,id==0?Kind.DELETED:Kind.SOURCE_CONFLICT,id==0?"This source ID was previously deleted. It is not restored by append import.":"This ID was imported before but source evidence is unavailable. Current data is kept.",id));}
                else {Long id=exact.get(entry.start+":"+entry.end);items.add(id==null?new Item(entry,Kind.NEW,"New historical period",0):new Item(entry,Kind.PERIOD_CONFLICT,"An existing period has exactly these bounds. Kept to avoid duplicate history.",id));}}
            BackupRuntime.requireIdle(context,admission);NightAudioArchive.active(active);db.setTransactionSuccessful();return new Review(items,fingerprint,document.sha256,document.repeated,admission);
        }finally{db.endTransaction();}
    }
    static int append(Context context,Store store,Review review,Set<String> selected,BooleanSupplier active,Commit commit)throws Exception {
        Set<String> eligible=new HashSet<>();for(Item item:review.items)if(item.eligible())eligible.add(item.entry.sourceId);if(selected.isEmpty()||!eligible.containsAll(selected))throw new IllegalArgumentException("Select only reviewed new periods.");BackupRuntime.requireIdle(context,review.admission);SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();int added=0;
        try{if(!fingerprint(store,active).equals(review.fingerprint))throw new IllegalStateException("History changed after review. Choose the CSV again before importing.");long sourceBytes=SleepCsvEvidence.bytes(store);List<JSONObject> prior=store.rows("SELECT COUNT(*) AS amount FROM sleep_csv_sources");if(prior.get(0).optInt("amount")+selected.size()>SleepCsvFormat.MAX_RECORDS)throw new IllegalStateException("At most 2,000 imported CSV sources can be retained. Existing records are unchanged.");long now=System.currentTimeMillis();
            for(Item item:review.items)if(selected.contains(item.entry.sourceId)){NightAudioArchive.active(active);BackupRuntime.requireIdle(context,review.admission);SleepCsvFormat.Entry entry=item.entry;sourceBytes+=entry.raw.getBytes(StandardCharsets.UTF_8).length;if(sourceBytes>SleepCsvEvidence.MAX_BYTES)throw new IllegalStateException("The selected original CSV rows exceed the 3 MiB evidence limit. Select fewer records; nothing was imported.");entry.draft.validate(entry.start,entry.end,true,now);ContentValues session=new ContentValues();session.put("start",entry.start);session.put("end",entry.end);session.put("note",entry.note);session.put("rating",entry.draft.rated?entry.draft.rating:0);session.put("source","manual");session.put("origin","imported");session.put("sensor","none");session.put("sensor_config","");session.put("zone",entry.zone);long id=db.insertOrThrow("sessions",null,session);db.insertOrThrow("record_edits",null,Records.values(id,entry.draft,now));SleepCsvEvidence.add(store,id,entry);ContentValues request=new ContentValues();request.put("token",token(entry.sourceId));request.put("session",id);db.insertOrThrow("record_create_requests",null,request);added++;}
            SleepCsvEvidence.export(store);BackupRuntime.requireIdle(context,review.admission);NightAudioArchive.active(active);commit.begin();db.setTransactionSuccessful();
        }finally{db.endTransaction();}commit.committed();store.surfaceChanged();return added;
    }
    static String fingerprint(Store store,BooleanSupplier active)throws Exception {MessageDigest digest=MessageDigest.getInstance("SHA-256");int rows=0;long bytes=0;for(String table:List.of("sessions","record_edits","record_create_requests","sleep_csv_sources")){field(digest,table);try(Cursor c=store.getReadableDatabase().rawQuery("SELECT * FROM "+table+" ORDER BY rowid",null)){for(String column:c.getColumnNames())field(digest,column);while(c.moveToNext()){NightAudioArchive.active(active);if(++rows>100000)throw new IllegalStateException("History exceeds the bounded CSV review size. Existing data is unchanged.");for(int col=0;col<c.getColumnCount();col++){int type=c.getType(col);digest.update((byte)type);String value=c.isNull(col)?"":c.getString(col);bytes+=value.getBytes(StandardCharsets.UTF_8).length;if(bytes>64L*1024*1024)throw new IllegalStateException("History exceeds the 64 MiB CSV review scan limit.");field(digest,value);}}}}return NightAudioArchive.hash(digest.digest());}
    private static void field(MessageDigest digest,String value){byte[] bytes=value.getBytes(StandardCharsets.UTF_8);digest.update(ByteBuffer.allocate(4).putInt(bytes.length).array());digest.update(bytes);}
    private SleepCsvImport(){}
}
