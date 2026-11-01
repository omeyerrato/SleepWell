package org.sleepwell.app;

import android.app.Activity;
import android.content.*;
import android.database.sqlite.SQLiteDatabase;
import android.widget.LinearLayout;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import org.json.JSONObject;

/** Local UX receipts for actual recovery, deliberately excluded from portable record payloads. */
final class InterruptedRecords {
    static final String EXTRA="interruptedReceipt";
    static final class Receipt {
        final String token;final long id,detected;final JSONObject raw;
        Receipt(String token,long id,long detected,JSONObject raw){this.token=token;this.id=id;this.detected=detected;this.raw=raw;}
    }
    static void create(SQLiteDatabase db){
        db.execSQL("CREATE TABLE interrupted_reviews(session INTEGER PRIMARY KEY,token TEXT NOT NULL UNIQUE,scope TEXT NOT NULL,raw_hash TEXT NOT NULL,detected INTEGER NOT NULL)");
        db.execSQL("CREATE TRIGGER forget_interrupted_review AFTER DELETE ON sessions BEGIN DELETE FROM interrupted_reviews WHERE session=OLD.id; END");
    }
    private static String hash(String value){try{byte[] bytes=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));StringBuilder out=new StringBuilder();for(byte item:bytes)out.append(String.format(Locale.ROOT,"%02x",item&255));return out.toString();}catch(java.security.NoSuchAlgorithmException impossible){throw new IllegalStateException(impossible);}}
    static String fingerprint(JSONObject raw){List<String> keys=new ArrayList<>();raw.keys().forEachRemaining(keys::add);Collections.sort(keys);StringBuilder value=new StringBuilder();for(String key:keys){Object item=raw.opt(key);String text=String.valueOf(item),kind=item==null||item==JSONObject.NULL?"null":item instanceof String?"text":item instanceof Number?"number":"other";value.append(key.length()).append(':').append(key).append(kind).append(text.length()).append(':').append(text);}return hash(value.toString());}
    private static String scope(Store store){try{return hash(new File(store.getReadableDatabase().getPath()).getCanonicalPath());}catch(java.io.IOException invalid){throw new IllegalStateException("The retained record's storage could not be checked.",invalid);}}
    static void recovered(Store store,long id){
        List<JSONObject> rows=store.rows("SELECT * FROM sessions WHERE id=? AND end IS NOT NULL AND interrupted=1",""+id);if(rows.size()!=1)throw new IllegalStateException("The recovered record changed.");
        ContentValues receipt=new ContentValues();receipt.put("session",id);receipt.put("token",UUID.randomUUID().toString());receipt.put("scope",scope(store));receipt.put("raw_hash",fingerprint(rows.get(0)));receipt.put("detected",System.currentTimeMillis());if(store.getWritableDatabase().insertWithOnConflict("interrupted_reviews",null,receipt,SQLiteDatabase.CONFLICT_REPLACE)<0)throw new IllegalStateException("The recovery receipt could not be kept.");
    }
    static Receipt require(Store store,String token,long id){SQLiteDatabase db=store.getReadableDatabase();boolean own=!db.inTransaction();if(own)db.beginTransactionNonExclusive();try{
        if(!BackupRuntime.nonce(token))throw unavailable();
        List<JSONObject> receipts=store.rows("SELECT * FROM interrupted_reviews WHERE token=?",token);if(receipts.size()!=1)throw unavailable();JSONObject receipt=receipts.get(0);long selected=receipt.optLong("session");
        if(id>0&&selected!=id||!scope(store).equals(receipt.optString("scope")))throw unavailable();
        List<JSONObject> rows=store.rows("SELECT * FROM sessions WHERE id=? AND end IS NOT NULL AND interrupted=1",""+selected);if(rows.size()!=1||!fingerprint(rows.get(0)).equals(receipt.optString("raw_hash")))throw unavailable();
        if(own)db.setTransactionSuccessful();return new Receipt(token,selected,receipt.optLong("detected"),rows.get(0));
    }finally{if(own)db.endTransaction();}}
    static IllegalStateException unavailable(){return new IllegalStateException("This retained record changed or is no longer available. Return to Tonight; no replacement record was selected.");}
    static Receipt pending(Store store){
        // Only recovery receipts and their exact named rows are read, never all personal history.
        for(int count=0;count<16;count++){List<JSONObject> rows=store.rows("SELECT token FROM interrupted_reviews ORDER BY detected DESC,session DESC LIMIT 1");if(rows.isEmpty())return null;String token=rows.get(0).optString("token");try{return require(store,token,0);}catch(IllegalStateException stale){store.getWritableDatabase().delete("interrupted_reviews","token=?",new String[]{token});}}
        return null;
    }
    static void keep(Store store,String token){SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();try{require(store,token,0);if(db.delete("interrupted_reviews","token=?",new String[]{token})!=1)throw unavailable();db.setTransactionSuccessful();}finally{db.endTransaction();}}
    static void entry(Activity owner,LinearLayout body,Store store){
        Receipt receipt;try{receipt=pending(store);}catch(RuntimeException unavailable){Design.space(body,8);Design.text(body,"Retained-record review could not be read. Reopen Tonight to retry; saved records were kept.",13,400,Design.MUTED);return;}if(receipt==null)return;Design.space(body,8);PlanActivity.detail(body,"info","Interrupted record retained","Review the saved portion · missing time stays blank",null,()->owner.startActivity(RecordUi.intent(owner,InterruptedRecordActivity.class).putExtra(EXTRA,receipt.token)));Design.space(body,8);
    }
    private InterruptedRecords(){}
}
