package org.sleepwell.app;

import android.database.Cursor;
import java.nio.charset.StandardCharsets;
import java.security.*;

/** Typed, ordered identity of every application table that replacement can affect. */
final class BackupFingerprint {
    static final String[] TABLES={"alarms","sessions","epochs","tracking_pauses","record_edits","record_create_requests","sound_events","clips","pending_clip_deletes","health_imports","health_exports","health_state","awake_runs","awake_estimates","echo_runs","echo_periods","pair_links","pair_packets","pair_windows","pir_runs","pir_samples","dream_cue_runs","dream_cue_requests"};
    static String read(Store store){try{
        MessageDigest digest=MessageDigest.getInstance("SHA-256");
        for(String table:TABLES){part(digest,table);try(Cursor rows=store.getReadableDatabase().rawQuery("SELECT * FROM "+table+" ORDER BY rowid",null)){
            number(digest,rows.getColumnCount());for(String column:rows.getColumnNames())part(digest,column);
            while(rows.moveToNext()){digest.update((byte)0x7e);for(int column=0;column<rows.getColumnCount();column++){int type=rows.getType(column);digest.update((byte)type);switch(type){case Cursor.FIELD_TYPE_NULL:break;case Cursor.FIELD_TYPE_INTEGER:number(digest,rows.getLong(column));break;case Cursor.FIELD_TYPE_FLOAT:number(digest,Double.doubleToLongBits(rows.getDouble(column)));break;case Cursor.FIELD_TYPE_BLOB:byte[] bytes=rows.getBlob(column);number(digest,bytes.length);digest.update(bytes);break;default:part(digest,rows.getString(column));}}}
            digest.update((byte)0x7f);
        }}return java.util.Base64.getEncoder().encodeToString(digest.digest());
    }catch(NoSuchAlgorithmException impossible){throw new AssertionError(impossible);}}
    private static void part(MessageDigest digest,String text){byte[] bytes=text.getBytes(StandardCharsets.UTF_8);number(digest,bytes.length);digest.update(bytes);}
    private static void number(MessageDigest digest,long value){for(int shift=56;shift>=0;shift-=8)digest.update((byte)(value>>>shift));}
    private BackupFingerprint(){}
}
