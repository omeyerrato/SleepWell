package org.sleepwell.app;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.function.BooleanSupplier;
import org.json.JSONArray;
import org.json.JSONObject;

/** One explicitly reviewed period, never a database/backup or the public summary-picker schema. */
final class ReportShareData {
    final long record;final boolean notes;final String text,fingerprint;final byte[] bytes;
    private ReportShareData(long record,boolean notes,String text,String fingerprint){this.record=record;this.notes=notes;this.text=text;this.fingerprint=fingerprint;bytes=text.getBytes(StandardCharsets.UTF_8);}
    static String hash(byte[] bytes)throws Exception{return NightAudioArchive.hash(MessageDigest.getInstance("SHA-256").digest(bytes));}
    static ReportShareData read(Store store,long id,boolean notes,BooleanSupplier active)throws Exception{
        android.database.sqlite.SQLiteDatabase db=store.getReadableDatabase();db.beginTransactionNonExclusive();
        try{
            check(active);if(id<=0||store.rows("SELECT id FROM sessions WHERE id=? AND end IS NOT NULL",""+id).isEmpty())throw new IOException("This completed record is unavailable. Return to Report.");
            if(store.rows("SELECT COUNT(*) AS n FROM tracking_pauses WHERE session=?",""+id).get(0).optLong("n")>5000)throw new IOException("This period has too many intervals for a bounded share preview.");
            RecordEntry r=Records.get(store,id);check(active);long now=System.currentTimeMillis(),duration=r.duration();
            if(r.edit.start<=0||r.edit.end<=r.edit.start||r.edit.end>now||r.edit.end-r.edit.start>SensorProvenance.MAX_SESSION||duration<0||duration>r.edit.end-r.edit.start)throw new IOException("This saved period cannot be represented as a completed summary.");
            String knownZone="Unknown (bounds below are UTC)";try{if(!r.zone.isEmpty())knownZone=ZoneId.of(r.zone).getId();}catch(DateTimeException unknown){}
            StringBuilder text=new StringBuilder("SleepWell — reviewed period\n\n")
                .append("Start (UTC): ").append(Instant.ofEpochMilli(r.edit.start)).append('\n')
                .append("End (UTC): ").append(Instant.ofEpochMilli(r.edit.end)).append('\n')
                .append("Recorded time zone: ").append(knownZone).append('\n')
                .append("Reviewed duration: ").append(RecordUi.duration(duration)).append('\n')
                .append("Source: ").append(r.provenance()).append('\n')
                .append("Recording interrupted: ").append(r.interrupted!=0?"Yes":"No").append('\n')
                .append("Clock changed: ").append(r.clockChanged!=0?"Yes":"No").append("\n\n")
                .append(r.manual()?"User-reported period; no sensor measurements.":!r.healthConnect.isEmpty()?"Source-provided period with reviewed local edits; source stages are not included.":"Reviewed recorded coverage excludes saved pauses and awake edits/accepted awake estimates. Unmarked awake time may remain.")
                .append("\nNot a validated sleep-stage or health measurement.\n");
            if(notes){if(r.edit.note.length()>20000)throw new IOException("These notes exceed the supported share size. Turn off Include notes.");text.append("\nNotes (included by your choice):\n").append(r.edit.note.isEmpty()?"No note saved.":r.edit.note).append('\n');}
            JSONArray coverage=new JSONArray();for(RecordIntervals.Span span:r.coverage()){check(active);if(coverage.length()>=5000)throw new IOException("This period has too many intervals for a bounded share preview.");coverage.put(new JSONArray().put(span.start).put(span.end));}
            JSONObject identity=new JSONObject().put("id",r.id).put("updated",r.updated).put("rawStart",r.rawStart).put("rawEnd",r.rawEnd).put("coverage",coverage).put("notes",notes).put("preview",text.toString());
            ReportShareData result=new ReportShareData(id,notes,text.toString(),hash(identity.toString().getBytes(StandardCharsets.UTF_8)));
            if(result.bytes.length>ReportShareFiles.MAX_BYTES)throw new IOException("This summary exceeds 128 KiB. Turn off Include notes.");
            check(active);db.setTransactionSuccessful();return result;
        }finally{db.endTransaction();}
    }
    static void check(BooleanSupplier active)throws IOException{if(!active.getAsBoolean()||Thread.currentThread().isInterrupted())throw new IOException("Share preparation cancelled or expired.");}
}
