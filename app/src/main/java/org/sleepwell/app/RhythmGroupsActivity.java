package org.sleepwell.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/** Recomputed from saved effective periods; no new sensor collection or inferred awake days. */
public final class RhythmGroupsActivity extends Activity {
    private final ExecutorService reader=Executors.newSingleThreadExecutor();private Records.Factory factory;private RhythmGroups data;private boolean busy;private String error="";private int generation;
    @Override public void onCreate(Bundle state){super.onCreate(state);RecordUi.review(this);factory=Records.factory;}
    @Override protected void onResume(){super.onResume();load();}
    private void load(){int version=++generation;busy=true;error="";render();int cutoff=Math.max(0,Math.min(1439,Prefs.number(this,"statsCutoff",0)));long now=SleepRhythmActivity.clock.getAsLong();ZoneId zone=ZoneId.systemDefault();reader.execute(()->{try(Store store=factory.open(getApplicationContext())){if(store.rows("SELECT COUNT(*) AS n FROM sessions").get(0).optLong("n")>5000)throw new IllegalStateException("This history is too large to group here. Your records are kept.");RhythmStats periods=new RhythmStats(Records.all(store),cutoff,31,now);RhythmGroups result=new RhythmGroups(periods.nights,now,zone);runOnUiThread(()->{if(isDestroyed()||version!=generation)return;data=result;busy=false;render();});}catch(Exception failure){runOnUiThread(()->{if(isDestroyed()||version!=generation)return;busy=false;error="Timing groups could not be loaded. Your records are kept.";render();});}});}
    private void render(){LinearLayout body=Design.page(this,false,null,n->{});RecordUi.label(this,body);RecordUi.header(body,"Sleep timing groups",this::finish);Design.space(body,14);if(busy){Design.text(body,"Reading saved periods…",15,400,Design.MUTED);return;}if(!error.isEmpty()){Design.text(body,error,15,500,Design.BROWN);Design.button(body,"Retry",true,this::load);return;}if(data==null)return;
        Design.text(body,data.nights.size()+" recorded main nights",27,700,Design.INK);Design.space(body,6);Design.text(body,"Latest 30 eligible nights in 90 days · "+data.zone.getId(),12,400,Design.MUTED);Design.space(body,18);
        if(data.nights.isEmpty()){Design.text(body,"Record a main night to see its timing",19,600,Design.INK);Design.space(body,8);Design.text(body,"Periods need at least 3 hours and a known, unchanged clock and time zone.",14,400,Design.MUTED);Design.button(body,"Open sleep history",true,()->startActivity(RecordUi.intent(this,HistoryActivity.class)));return;}
        body.addView(new RhythmGroupsChart(this,data),new LinearLayout.LayoutParams(-1,Design.dp(this,Math.max(160,90*data.groups.size()))));Design.space(body,12);
        for(int n=0;n<data.groups.size();n++){RhythmGroups.Group g=data.groups.get(n);Design.text(body,"Group "+(n+1)+" · "+g.nights.size()+" nights",18,600,RhythmGroupsChart.COLORS[n%3]);Design.space(body,5);Design.text(body,"Average midpoint "+RhythmChart.label(this,g.midpoint),16,600,Design.INK);Design.text(body,"Mean distance "+Math.round(g.spread)+" min · average recorded duration "+RecordUi.duration(g.duration),12,400,Design.MUTED);Design.space(body,14);for(RhythmStats.Night night:g.nights){String day=night.day.format(DateTimeFormatter.ofPattern("EEE d MMM",Locale.getDefault()));PlanActivity.detail(body,"moon",day,night.record.manual()?"User-reported period":night.record.provenance(),RhythmChart.label(this,night.midpoint),()->startActivity(RecordUi.intent(this,RecordReportActivity.class).putExtra("record",night.record.id)));}Design.line(body);Design.space(body,16);}
        Design.text(body,"Groups compare recorded period midpoints around the 24-hour clock. They do not estimate sleep stages, chronotype or body-clock phase.",13,400,Design.MUTED);Design.space(body,8);Design.text(body,"Up to 3 groups; each extra group needs 3 nights and reduces average timing distance by at least 60 minutes. Sparse data can remain one broad group.",12,400,Design.MUTED);Design.space(body,8);Design.text(body,data.excluded+" periods excluded for interruption, clock change or another time zone. Missing dates add no points. "+data.manual+" user-reported nights included.",12,400,Design.MUTED);Design.space(body,12);Design.button(body,"Refresh groups",false,this::load);
    }
    @Override protected void onDestroy(){generation++;reader.shutdownNow();super.onDestroy();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
}
