package org.sleepwell.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import org.json.JSONObject;
import java.util.*;
import java.util.concurrent.*;

/** Real classified intervals; analysis-only events do not pretend to contain a recording. */
public final class SoundEventsActivity extends Activity {
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private List<JSONObject> events=List.of();private RecordEntry record;private String category="all",error="";private boolean loaded;
    @Override public void onCreate(Bundle saved){super.onCreate(saved);RecordUi.review(this);if(saved!=null)category=saved.getString("category","all");render();long id=getIntent().getLongExtra("record",0);Records.Factory source=Records.factory;worker.execute(()->{try(Store store=source.open(getApplicationContext())){RecordEntry value=Records.get(store,id);List<JSONObject> sounds=store.rows("SELECT * FROM sound_events WHERE session=? AND time+duration_ms>CAST(? AS INTEGER) AND time<CAST(? AS INTEGER) ORDER BY time",""+id,""+value.edit.start,""+value.edit.end);runOnUiThread(()->{if(isDestroyed())return;record=value;events=sounds;loaded=true;render();});}catch(Exception failure){runOnUiThread(()->{if(isDestroyed())return;error="Sound events could not be loaded.";loaded=true;render();});}});}
    @Override public void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putString("category",category);}
    private void render(){
        LinearLayout body=Design.page(this,false,null,n->{});RecordUi.label(this,body);RecordUi.headerAction(RecordUi.header(body,"Sound events",this::finish),"settings","Snoring cues",()->startActivity(RecordUi.intent(this,SnoringCuesActivity.class)));
        if(!loaded){Design.text(body,"Loading…",15,400,Design.MUTED);return;}if(!error.isEmpty()){Design.text(body,error,15,400,Design.MUTED);return;}
        Design.text(body,RecordUi.date(record.edit.end,record.zone()),14,400,Design.MUTED);Design.space(body,18);
        String[] keys={"all","snoring","speech","noise"},titles={"All","Snoring","Speech","Other"};LinearLayout tabs=Design.row(this);
        for(int n=0;n<keys.length;n++){String key=keys[n];int count=0;for(JSONObject event:events)if(key.equals("all")||event.optString("category").equals(key))count++;tabs.addView(RecordUi.chip(this,titles[n]+" ("+count+")",category.equals(key),()->{category=key;render();}),new LinearLayout.LayoutParams(0,-2,1));}body.addView(tabs);Design.space(body,10);
        if(events.isEmpty()){Design.space(body,28);Design.text(body,"No sound events",23,600,Design.INK);Design.space(body,8);Design.text(body,"Enable microphone analysis for your next night.",14,400,Design.MUTED);return;}
        Design.text(body,"On-device estimates",12,400,Design.MUTED);Design.space(body,8);int shown=0;
        for(JSONObject event:events){String type=event.optString("category");if(!category.equals("all")&&!category.equals(type))continue;shown++;LinearLayout row=Design.row(this);Design.pad(row,12,16);row.setBackground(Design.shape(this,Design.SOFT,16));
            TextView time=Design.text(this,RecordUi.clock(this,Math.max(record.edit.start,event.optLong("time")),record.zone()),13,400,Design.MUTED);row.addView(time,new LinearLayout.LayoutParams(Design.dp(this,80),-2));LinearLayout copy=Design.col(this);String title=type.equals("snoring")?"Snoring estimate":type.equals("speech")?"Speech estimate":"Other sound";Design.text(copy,title,17,600,Design.INK);Design.space(copy,6);Design.text(copy,RecordUi.duration(Math.max(0,Math.min(record.edit.end,event.optLong("time")+event.optLong("duration_ms"))-Math.max(record.edit.start,event.optLong("time")))),13,400,Design.MUTED);row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));body.addView(row);Design.space(body,10);
        }
        if(shown==0)Design.text(body,"No events in this category.",14,400,Design.MUTED);
        Design.space(body,12);Design.item(body,"wave","Saved recordings & signals",null,()->{startActivity(RecordUi.intent(this,RecordSignalsActivity.class).putExtra("record",record.id));finish();});
    }
    boolean awaitReaders(long timeoutMs) throws InterruptedException{return worker.awaitTermination(timeoutMs,TimeUnit.MILLISECONDS);}
    @Override protected void onDestroy(){worker.shutdownNow();super.onDestroy();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
}
