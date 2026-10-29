package org.sleepwell.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.*;
import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.*;
import org.json.JSONObject;

/** A read-only, pinned storage snapshot. Detached pages never receive a late result. */
final class PrivacyStorageSummary implements View.OnAttachStateChangeListener {
    private final LinearLayout card;
    private final TextView records,audio,status;
    private final View retry;
    private final Context app;
    private final Records.Factory factory;
    private final Handler main=new Handler(Looper.getMainLooper());
    private final ExecutorService worker=Executors.newSingleThreadExecutor(r->new Thread(r,"privacy-storage-summary"));
    private boolean attached,closed,loading;
    private int generation;

    PrivacyStorageSummary(LinearLayout body,Records.Factory factory){
        this.factory=factory;Context context=body.getContext();app=context.getApplicationContext();
        card=Design.col(context);Design.pad(card,16,14);card.setBackground(Design.shape(context,Design.SOFT,18));
        card.setContentDescription("Local storage summary");
        LinearLayout heading=Design.row(context);
        Design.Icon glyph=new Design.Icon(context,"report",Design.BROWN);glyph.setPadding(Design.dp(context,8),Design.dp(context,8),Design.dp(context,8),Design.dp(context,8));
        glyph.setBackground(Design.shape(context,0xffeee0d3,12));
        LinearLayout.LayoutParams icon=new LinearLayout.LayoutParams(Design.dp(context,40),Design.dp(context,40));icon.rightMargin=Design.dp(context,14);heading.addView(glyph,icon);
        LinearLayout copy=Design.col(context);Design.text(copy,"On this phone",19,600,Design.INK);Design.space(copy,3);Design.text(copy,"Stored locally on your phone.",12,400,Design.MUTED);heading.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
        retry=Design.iconButton(context,"undo","Retry storage summary",this::load);retry.setVisibility(View.GONE);heading.addView(retry,new LinearLayout.LayoutParams(Design.dp(context,48),Design.dp(context,48)));card.addView(heading);
        Design.space(card,16);LinearLayout totals=Design.row(context),left=Design.col(context),right=Design.col(context);
        Design.text(left,"Records",13,400,Design.MUTED);Design.space(left,4);records=Design.text(left,"Loading…",25,700,Design.INK);records.setContentDescription("Stored record count");
        totals.addView(left,new LinearLayout.LayoutParams(0,-2,1));View divider=new View(context);divider.setBackgroundColor(Design.LINE);LinearLayout.LayoutParams rule=new LinearLayout.LayoutParams(Design.dp(context,1),Design.dp(context,48));rule.setMargins(Design.dp(context,10),0,Design.dp(context,18),0);totals.addView(divider,rule);
        Design.text(right,"Audio",13,400,Design.MUTED);Design.space(right,4);audio=Design.text(right,"Loading…",25,700,Design.INK);audio.setContentDescription("Stored audio size");totals.addView(right,new LinearLayout.LayoutParams(0,-2,1));card.addView(totals);
        status=Design.text(card,"Reading local storage…",12,400,Design.MUTED);status.setContentDescription("Storage summary status");status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);status.setPadding(0,Design.dp(context,10),0,0);
        body.addView(card);card.addOnAttachStateChangeListener(this);if(card.isAttachedToWindow())onViewAttachedToWindow(card);
    }

    @Override public void onViewAttachedToWindow(View view){if(attached||closed)return;attached=true;load();}
    @Override public void onViewDetachedFromWindow(View view){attached=false;closed=true;generation++;main.removeCallbacksAndMessages(null);worker.shutdown();}
    private void load(){
        if(!attached||closed||loading)return;loading=true;int expected=++generation;retry.setVisibility(View.GONE);records.setText("Loading…");audio.setText("Loading…");status.setText("Reading local storage…");status.setVisibility(View.VISIBLE);
        Context context=app;Records.Factory pinned=factory;Handler dispatcher=main;WeakReference<PrivacyStorageSummary> owner=new WeakReference<>(this);
        worker.execute(()->{Snapshot snapshot;try{snapshot=read(context,pinned);}catch(Exception unavailable){snapshot=new Snapshot(-1,-1,0,"Storage totals unavailable. Try again.");}Snapshot result=snapshot;dispatcher.post(()->{PrivacyStorageSummary target=owner.get();if(target!=null&&target.attached&&!target.closed&&target.generation==expected)target.show(result);});});
    }
    private void show(Snapshot snapshot){
        loading=false;boolean failed=!snapshot.error.isEmpty();
        records.setText(failed?"Unavailable":snapshot.records+(snapshot.records==1?" record":" records"));
        audio.setText(failed?"Unavailable":android.text.format.Formatter.formatShortFileSize(card.getContext(),snapshot.bytes));
        String notice=failed?snapshot.error:snapshot.missing==0?"":snapshot.missing+(snapshot.missing==1?" recording unavailable.":" recordings unavailable.");
        status.setText(notice);status.setVisibility(notice.isEmpty()?View.GONE:View.VISIBLE);retry.setVisibility(failed?View.VISIBLE:View.GONE);
    }
    private static Snapshot read(Context app,Records.Factory factory)throws Exception{
        long records;List<JSONObject> clips;
        try(Store store=factory.open(app)){
            SQLiteDatabase database=store.getReadableDatabase();database.beginTransactionNonExclusive();
            try{records=store.rows("SELECT COUNT(*) AS count FROM sessions").get(0).getLong("count");clips=store.rows("SELECT DISTINCT path FROM clips");database.setTransactionSuccessful();}
            finally{database.endTransaction();}
        }
        long bytes=0;int missing=0;Set<String> paths=new HashSet<>();
        for(JSONObject clip:clips){
            File file;try{file=FileSafety.clip(app,clip.getString("path"));}catch(java.io.IOException invalid){missing++;continue;}
            if(!paths.add(file.getPath()))continue;
            try{BasicFileAttributes attributes=Files.readAttributes(file.toPath(),BasicFileAttributes.class);if(attributes.isRegularFile())bytes=Math.addExact(bytes,attributes.size());else missing++;}
            catch(NoSuchFileException absent){missing++;}
        }
        return new Snapshot(records,bytes,missing,"");
    }
    private static final class Snapshot{
        final long records,bytes;final int missing;final String error;
        Snapshot(long records,long bytes,int missing,String error){this.records=records;this.bytes=bytes;this.missing=missing;this.error=error;}
    }
}
