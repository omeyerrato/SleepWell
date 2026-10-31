package org.sleepwell.app;

import android.app.Activity;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;
import android.view.*;
import android.widget.*;
import java.io.File;
import java.util.UUID;
import java.util.function.*;
import org.json.JSONObject;

/** Short-lived deletion receipt, never an alarm run or permission to overwrite another row. */
final class AlarmDeletionUndo {
    static final long LIFETIME=30_000;
    static LongSupplier clock=SystemClock::elapsedRealtime;
    private static long revision;
    private static Receipt latest;
    enum State { AVAILABLE,RESTORED,UNAVAILABLE }
    static final class Receipt {
        final String nonce=UUID.randomUUID().toString(),database,document,label;
        final Records.Factory records;final int id;final long mutation,admission,created,expires;
        State state=State.AVAILABLE;String issue="",restoredDocument="";long restoredMutation=-1;boolean wasRestored;
        Receipt(String database,String document,Alarm alarm,Records.Factory records,long mutation,long admission){
            this.database=database;this.document=document;this.records=records;id=alarm.id;label=alarm.label;
            this.mutation=mutation;this.admission=admission;created=clock.getAsLong();expires=Math.addExact(created,LIFETIME);
        }
    }
    /** Called inside the same write transaction, before each actual alarm-table mutation. */
    static synchronized void changing(){
        revision=revision==Long.MAX_VALUE?Long.MAX_VALUE:revision+1;
        if(latest!=null&&(latest.state==State.AVAILABLE||latest.state==State.RESTORED)){boolean restored=latest.state==State.RESTORED;latest.state=State.UNAVAILABLE;latest.issue=restored?"The restored alarm changed. Open it from the list.":"Alarms changed. This deletion can no longer be undone.";}
    }
    private static String database(Store store){try{return new File(store.getWritableDatabase().getPath()).getCanonicalPath();}catch(java.io.IOException invalid){throw new IllegalStateException("Alarm storage identity is unavailable.");}}
    private static String raw(SQLiteDatabase db,int id){try(Cursor cursor=db.rawQuery("SELECT data FROM alarms WHERE id=?",new String[]{Integer.toString(id)})){return cursor.moveToFirst()?cursor.getString(0):null;}}
    /** Existing editor cancellation happens first. The final deletion is exact-row CAS. */
    static void delete(Context context,Store store,Records.Factory records,Alarm reviewed){
        SQLiteDatabase db=store.getWritableDatabase();String database=database(store);Receipt prepared;
        db.beginTransaction();try{
            String document=raw(db,reviewed.id);Alarm current=document==null?null:Alarm.read(new JSONObject(document));
            if(current==null||!current.json().toString().equals(reviewed.json().toString()))throw new IllegalStateException("This alarm changed. Reopen it before deleting.");
            synchronized(AlarmDeletionUndo.class){changing();if(revision==Long.MAX_VALUE)throw new IllegalStateException("Alarm deletion recovery is unavailable.");
                if(db.delete("alarms","id=? AND data=?",new String[]{Integer.toString(reviewed.id),document})!=1)throw new IllegalStateException("This alarm changed. Reopen it before deleting.");
                prepared=new Receipt(database,document,current,records,revision,BackupRuntime.generation());
            }
            db.setTransactionSuccessful();
        }catch(org.json.JSONException invalid){throw new IllegalStateException("This alarm could not be reviewed for deletion.",invalid);}finally{db.endTransaction();}
        synchronized(AlarmDeletionUndo.class){if(revision==prepared.mutation)latest=prepared;}
        store.surfaceChanged();
    }
    private static boolean admission(Context context,Receipt receipt){
        return receipt.admission==BackupRuntime.generation()&&!AlarmService.active&&!TrackingService.running&&!Prefs.flag(context,"ringing")&&!Prefs.flag(context,"tracking");
    }
    private static synchronized boolean available(Context context,Receipt receipt){
        if(receipt==null||latest!=receipt||receipt.state!=State.AVAILABLE)return false;
        long now=clock.getAsLong();
        if(now<receipt.created||now>=receipt.expires){receipt.state=State.UNAVAILABLE;receipt.issue="Undo expired. The alarm remains deleted.";}
        else if(receipt.mutation!=revision||Records.factory!=receipt.records){receipt.state=State.UNAVAILABLE;receipt.issue="Alarms or storage changed. This deletion can no longer be undone.";}
        else if(!admission(context,receipt)){receipt.state=State.UNAVAILABLE;receipt.issue="Undo is unavailable during an alarm or recording. Review alarms before adding one again.";}
        return receipt.state==State.AVAILABLE;
    }
    static Alarm restore(Context context,Receipt receipt){synchronized(Scheduler.class){
        if(!available(context,receipt))throw new IllegalStateException(receipt!=null&&!receipt.issue.isEmpty()?receipt.issue:"This Undo is no longer available.");
        try(Store store=receipt.records.open(context)){
            SQLiteDatabase db=store.getWritableDatabase();if(!database(store).equals(receipt.database)){synchronized(AlarmDeletionUndo.class){receipt.state=State.UNAVAILABLE;receipt.issue="Alarm storage changed. This Undo is no longer available.";}throw new IllegalStateException(receipt.issue);}
            Alarm restored;long insertedRevision;String insertedDocument;db.beginTransaction();try{synchronized(AlarmDeletionUndo.class){
                if(!available(context,receipt))throw new IllegalStateException("This Undo is no longer available.");
                if(raw(db,receipt.id)!=null){receipt.state=State.UNAVAILABLE;receipt.issue="This alarm identity is in use. Nothing was overwritten.";throw new IllegalStateException(receipt.issue);}
                // Consume before mutation. A stale view cannot replay it, even if the write fails.
                receipt.state=State.UNAVAILABLE;receipt.issue="Restoration could not complete. Add the alarm again if needed.";
                JSONObject settings=new JSONObject(receipt.document);for(String key:new String[]{"nap","weeklyNapOwner","skip","nextOverride","scheduleError"})settings.remove(key);settings.put("enabled",false);
                restored=Alarm.read(settings);insertedDocument=restored.json().toString();ContentValues row=new ContentValues();row.put("id",restored.id);row.put("data",insertedDocument);changing();insertedRevision=revision;db.insertOrThrow("alarms",null,row);db.setTransactionSuccessful();
            }}finally{db.endTransaction();}
            synchronized(AlarmDeletionUndo.class){receipt.wasRestored=true;long now=clock.getAsLong();if(latest==receipt&&revision==insertedRevision&&Records.factory==receipt.records&&admission(context,receipt)&&now>=receipt.created&&now<receipt.expires){receipt.restoredMutation=insertedRevision;receipt.restoredDocument=insertedDocument;receipt.state=State.RESTORED;receipt.issue="Restored off; review before enabling.";}else{receipt.state=State.UNAVAILABLE;receipt.issue="The restored alarm changed. Open it from the list.";}}
            store.surfaceChanged();return restored;
        }catch(org.json.JSONException invalid){throw new IllegalStateException("The deleted alarm settings could not be restored.",invalid);}
    }}
    /** Resolve only the exact restored row. Never turn a retained Review view into an ID lookup. */
    private static Alarm reviewed(Context context,Store store,Receipt receipt){
        String identity=database(store);SQLiteDatabase db=store.getReadableDatabase();
        db.beginTransactionNonExclusive();try{
            String document=raw(db,receipt.id);long now=clock.getAsLong();
            synchronized(AlarmDeletionUndo.class){
                if(latest!=receipt||receipt.state!=State.RESTORED)return null;
                if(Records.factory!=receipt.records||!receipt.database.equals(identity)||receipt.restoredMutation!=revision||!receipt.restoredDocument.equals(document)||!admission(context,receipt)){
                    receipt.state=State.UNAVAILABLE;receipt.issue="The restored alarm changed. Open it from the list.";return null;
                }
                if(now<receipt.created||now>=receipt.expires){receipt.state=State.UNAVAILABLE;receipt.issue="This restoration receipt expired. Open the alarm from the list.";return null;}
                try{return Alarm.read(new JSONObject(document));}catch(org.json.JSONException invalid){receipt.state=State.UNAVAILABLE;receipt.issue="The restored alarm could not be reviewed. Open alarms again.";return null;}
            }
        }finally{db.endTransaction();}
    }
    static synchronized void forget(Records.Factory records){if(latest!=null&&latest.records==records)latest=null;}
    static void add(LinearLayout body,Store store,Runnable refresh,Consumer<Alarm> edit){
        Activity activity=(Activity)body.getContext();Receipt receipt;
        synchronized(AlarmDeletionUndo.class){receipt=latest;}
        if(receipt==null||receipt.records!=Records.factory||!receipt.database.equals(database(store)))return;
        available(activity,receipt);if(receipt.state==State.RESTORED)reviewed(activity,store,receipt);
        LinearLayout card=Design.col(activity);Design.pad(card,16,14);card.setBackground(Design.shape(activity,NightDialTokens.RAISED,16));
        Design.text(card,receipt.state==State.RESTORED?"Restored "+receipt.label:receipt.wasRestored?"Restoration changed: "+receipt.label:"Deleted "+receipt.label,17,600,NightDialTokens.TEXT);
        Design.space(card,6);Design.text(card,receipt.state==State.AVAILABLE?"Undo restores this alarm off for review. Active timers and moved or skipped occurrences are not resumed.":receipt.issue,13,400,NightDialTokens.SECONDARY);
        LinearLayout actions=Design.row(activity);card.addView(actions);LinearLayout primary=Design.col(activity);actions.addView(primary,new LinearLayout.LayoutParams(0,-2,1));
        if(receipt.state==State.AVAILABLE)Design.button(primary,"Undo",true,()->{if(!card.isAttachedToWindow()||!card.hasWindowFocus()||activity.isFinishing()||activity.isDestroyed())return;try{restore(activity,receipt);}catch(RuntimeException unavailable){Ui.toast(activity,unavailable.getMessage()==null?"This Undo is no longer available.":unavailable.getMessage());}refresh.run();}).setContentDescription("Undo delete for "+receipt.label);
        else if(receipt.state==State.RESTORED)Design.button(primary,"Review alarm",true,()->{if(!card.isAttachedToWindow()||!card.hasWindowFocus()||activity.isFinishing()||activity.isDestroyed())return;Alarm current=reviewed(activity,store,receipt);if(current!=null)edit.accept(current);else refresh.run();}).setContentDescription("Review restored "+receipt.label);
        LinearLayout secondary=Design.col(activity);actions.addView(secondary,new LinearLayout.LayoutParams(0,-2,1));Design.button(secondary,"Dismiss",false,()->{if(!card.isAttachedToWindow()||!card.hasWindowFocus()||activity.isFinishing()||activity.isDestroyed())return;synchronized(AlarmDeletionUndo.class){if(latest==receipt)latest=null;}refresh.run();}).setContentDescription("Dismiss deletion for "+receipt.label);
        body.addView(card);Design.space(body,16);
        if(receipt.state==State.AVAILABLE){Runnable expired=()->{synchronized(AlarmDeletionUndo.class){if(latest!=receipt)return;}refresh.run();};card.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener(){public void onViewAttachedToWindow(View view){view.postDelayed(expired,Math.max(1,receipt.expires-clock.getAsLong()+1));}public void onViewDetachedFromWindow(View view){view.removeCallbacks(expired);}});if(card.isAttachedToWindow())card.postDelayed(expired,Math.max(1,receipt.expires-clock.getAsLong()+1));}
    }
    private AlarmDeletionUndo(){}
}
