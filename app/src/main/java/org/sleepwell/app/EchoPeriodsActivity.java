package org.sleepwell.app;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;

import android.os.Bundle;
import android.widget.*;
import org.json.*;
import java.util.*;
import java.util.concurrent.*;

/** Reviews real echo measurements and quality, without reconstructing a breathing waveform. */
public final class EchoPeriodsActivity extends ComponentActivity {
    private static final int PAGE_SIZE=20;
    private final ExecutorService reader=Executors.newSingleThreadExecutor();
    private Records.Factory factory;
    private RecordEntry record;
    private JSONObject run;
    private List<JSONObject> windows=List.of();
    private String error="";
    private boolean original;
    private int generation,filter,page,chartMode,overviewScroll,detailScroll;
    private long selectedStart;
    private ScrollView scroll;

    @Override public void onCreate(Bundle state){
        super.onCreate(state);RecordUi.review(this);factory=Records.factory;
        getOnBackPressedDispatcher().addCallback(this,new OnBackPressedCallback(true){@Override public void handleOnBackPressed(){back();}});
        if(state!=null){original=state.getBoolean("original");filter=Math.max(0,Math.min(2,state.getInt("filter")));page=Math.max(0,state.getInt("page"));chartMode=state.getInt("chartMode")==1?1:0;selectedStart=state.getLong("selectedStart");overviewScroll=state.getInt("overviewScroll");detailScroll=state.getInt("detailScroll");}
    }
    @Override protected void onResume(){super.onResume();load();}
    private void load(){
        rememberScroll();int version=++generation;long id=getIntent().getLongExtra("record",0);
        LinearLayout body=Design.page(this,false,null,n->{});RecordUi.header(body,"Breathing rhythm",this::back);scroll=null;Design.text(body,"Loading recorded intervals…",14,400,Design.MUTED);
        reader.execute(()->{
            try(Store store=factory.open(getApplicationContext())){
                android.database.sqlite.SQLiteDatabase db=store.getReadableDatabase();db.beginTransactionNonExclusive();
                RecordEntry entry;JSONObject policy;List<JSONObject> samples;
                try{entry=Records.get(store,id);List<JSONObject> runs=store.rows("SELECT * FROM echo_runs WHERE session=?",Long.toString(id));policy=runs.isEmpty()?null:runs.get(0);samples=store.rows("SELECT start,end AS time,rate,periodicity,quality FROM echo_periods WHERE session=? ORDER BY start",Long.toString(id));if(samples.size()>2160)throw new IllegalStateException("Too many analysis windows.");db.setTransactionSuccessful();}
                finally{db.endTransaction();}
                runOnUiThread(()->{if(isDestroyed()||version!=generation)return;record=entry;run=policy;windows=samples;error="";render();});
            }catch(Exception failure){runOnUiThread(()->{if(isDestroyed()||version!=generation)return;error="Recorded analysis could not be opened. Your night is kept.";render();});}
        });
    }
    private void rememberScroll(){if(scroll!=null){if(selectedStart==0)overviewScroll=scroll.getScrollY();else detailScroll=scroll.getScrollY();}}
    private void render(){
        LinearLayout body=Design.page(this,false,null,n->{});scroll=(ScrollView)body.getParent();RecordUi.label(this,body);
        LinearLayout header=RecordUi.header(body,selectedStart==0?"Breathing rhythm":"Recorded minute",this::back);
        RecordUi.headerAction(header,"info","How echo rhythm is calculated",this::method);
        Design.space(body,12);
        if(!error.isEmpty()){Design.text(body,error,15,500,Design.BROWN);Design.button(body,"Retry",true,this::load);return;}
        if(record==null)return;
        Design.text(body,RecordUi.date(record.edit.end,record.zone()),15,500,Design.MUTED);Design.space(body,16);
        if(run==null||run.optInt("enabled")==0){
            Design.text(body,run==null?"No rhythm analysis recorded":"Rhythm analysis was off",22,600,Design.INK);Design.space(body,10);
            Design.text(body,"Enable it for your next manually started contactless night.",14,400,Design.MUTED);
            Design.button(body,"Rhythm settings",true,()->startActivity(RecordUi.intent(this,EchoSettingsActivity.class)));return;
        }
        if(!record.sonar()||!EchoPeriodicity.ALGORITHM.equals(run.optString("algorithm"))){
            Design.text(body,"This analysis format is unavailable",21,600,Design.INK);Design.space(body,10);Design.text(body,"The original recorded analysis is kept. This screen supports contactless echo periodicity v1.",14,400,Design.MUTED);return;
        }
        long start=original?record.rawStart:record.edit.start,end=original?record.rawEnd:record.edit.end;
        List<JSONObject> eligible=new ArrayList<>();
        for(JSONObject row:windows){long from=row.optLong("start"),to=row.optLong("time");if(from<start||to>end)continue;if(!original&&RecordIntervals.length(RecordIntervals.intersect(record.coverage(),from,to))!=to-from)continue;eligible.add(row);}
        EchoRhythm rhythm;
        try{rhythm=new EchoRhythm(eligible);}catch(IllegalArgumentException invalid){Design.text(body,"Some recorded intervals could not be read",20,600,Design.INK);Design.text(body,"Your night and its original analysis are kept.",14,400,Design.MUTED);Design.button(body,"Reload analysis",true,this::load);return;}
        if(selectedStart!=0){EchoRhythm.Minute selected=rhythm.at(selectedStart);if(selected!=null){detail(body,rhythm,selected);restoreScroll(detailScroll);return;}selectedStart=0;detailScroll=0;Design.text(body,"That minute is no longer in the reviewed period.",13,500,Design.BROWN);}
        if(record.edited||!record.awake.events.isEmpty())RecordUi.segments(body,new String[]{"Reviewed period","Original capture"},original?1:0,value->{original=value==1;page=0;overviewScroll=0;render();});
        Design.space(body,18);Design.text(body,"Typical periodic rate",16,500,Design.INK);
        Design.text(body,rhythm.median==null?"No stable estimate":String.format(Locale.getDefault(),"%.1f",rhythm.median),rhythm.median==null?28:48,700,Design.INK);
        Design.text(body,rhythm.median==null?"A complete, repeating echo is needed.":"cycles/min · median of qualified minutes",13,400,Design.MUTED);
        Design.space(body,16);
        LinearLayout metrics=Design.row(this);
        metric(metrics,"Recorded range",rhythm.low==null?"—":String.format(Locale.getDefault(),"%.1f–%.1f",rhythm.low,rhythm.high),"cycles/min");
        metric(metrics,"Adjacent variation",rhythm.variability==null?"—":String.format(Locale.getDefault(),"%.1f",rhythm.variability),rhythm.adjacentPairs+" consecutive pairs");body.addView(metrics);
        Design.space(body,18);RecordUi.segments(body,new String[]{"Rate","Minute changes"},chartMode,value->{rememberScroll();chartMode=value;render();});
        Design.space(body,12);
        if(end>start)body.addView(new EchoRhythmChart(this,rhythm.minutes,start,end,record.zone(),chartMode==1,this::openMinute),new LinearLayout.LayoutParams(-1,Design.dp(this,210)));
        Design.text(body,chartMode==0?"Cycles/min · gaps stay blank":"Change in cycles/min · only consecutive qualified minutes",12,400,Design.MUTED);
        Design.space(body,12);Design.text(body,"Experimental respiratory rhythm estimate. A repeating reflection can also come from other movement.",13,500,Design.BROWN);
        RecordUi.heading(body,"Signal coverage");
        long eligibleDuration=original?Math.max(0,end-start):RecordIntervals.length(record.coverage());
        Design.text(body,rhythm.qualified+" min qualified · "+rhythm.minutes.size()+" min analyzed",16,600,Design.INK);
        Design.text(body,RecordUi.duration(eligibleDuration)+" in this "+(original?"capture":"reviewed period")+" · "+runState(run.optString("state")),13,400,Design.MUTED);
        Design.space(body,8);Design.item(body,"info","Signal details","",()->quality(rhythm,eligibleDuration));
        RecordUi.heading(body,"Recorded intervals");
        RecordUi.segments(body,new String[]{"All","Qualified","Unresolved"},filter,value->{filter=value;page=0;overviewScroll=scroll.getScrollY();render();});
        List<EchoRhythm.Minute> shown=rhythm.filtered(filter);int pages=Math.max(1,(shown.size()+PAGE_SIZE-1)/PAGE_SIZE);page=Math.min(page,pages-1);
        if(shown.isEmpty()){Design.space(body,16);Design.text(body,rhythm.minutes.isEmpty()?"No complete analysis minutes in this period.":"No intervals match this filter.",14,400,Design.MUTED);}
        for(int n=page*PAGE_SIZE;n<Math.min(shown.size(),(page+1)*PAGE_SIZE);n++){
            EchoRhythm.Minute minute=shown.get(n);Design.item(body,"wave",RecordUi.clock(this,minute.start,record.zone()),minute.rate==null?minute.reason():String.format(Locale.getDefault(),"%.1f cycles/min",minute.rate),()->openMinute(minute.start));
        }
        if(pages>1){Design.space(body,12);Design.text(body,"Page "+(page+1)+" of "+pages,13,500,Design.MUTED);LinearLayout controls=Design.row(this);if(page>0)pageButton(controls,"Previous",()->{rememberScroll();page--;render();});if(page+1<pages)pageButton(controls,"Next",()->{rememberScroll();page++;render();});body.addView(controls);}
        Design.space(body,14);Design.button(body,"Refresh recorded intervals",false,this::load);restoreScroll(overviewScroll);
    }
    private void metric(LinearLayout row,String label,String value,String unit){LinearLayout block=Design.col(this);Design.text(block,label,12,500,Design.MUTED);Design.space(block,5);Design.text(block,value,23,600,Design.INK);Design.text(block,unit,11,400,Design.MUTED);row.addView(block,new LinearLayout.LayoutParams(0,-2,1));}
    private void pageButton(LinearLayout row,String title,Runnable action){Button button=Design.button(row,title,false,action);LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,-2,1);params.setMargins(Design.dp(this,3),Design.dp(this,6),Design.dp(this,3),0);button.setLayoutParams(params);}
    private void openMinute(long start){rememberScroll();selectedStart=start;detailScroll=0;render();}
    private void detail(LinearLayout body,EchoRhythm rhythm,EchoRhythm.Minute minute){
        Design.text(body,(original?"Original capture":"Reviewed period")+" / Recorded minute",12,500,Design.MUTED);Design.space(body,14);
        Design.text(body,RecordUi.preciseClock(this,minute.start,record.zone())+" – "+RecordUi.preciseClock(this,minute.end,record.zone()),21,600,Design.INK);
        Design.text(body,RecordUi.date(minute.start,record.zone())+" · "+record.zone().getId(),12,400,Design.MUTED);Design.text(body,"60 seconds · "+record.provenance(),12,400,Design.MUTED);Design.space(body,24);
        Design.text(body,minute.rate==null?minute.reason():String.format(Locale.getDefault(),"%.1f cycles/min",minute.rate),30,700,Design.INK);Design.space(body,8);
        Design.text(body,switch(minute.quality){case "PERIODIC"->"A consistent repeating reflection passed the signal checks.";case "AMBIGUOUS"->"Different reflection bins suggest conflicting periods. No rate is assigned.";case "NO_STABLE_PERIOD"->"No repeatable period passed the checks. No rate is assigned.";default->"The full minute did not contain enough continuous, valid echo samples. No rate is assigned.";},14,400,Design.MUTED);
        RecordUi.heading(body,"Compared with the previous minute");
        Design.text(body,minute.change==null?"No consecutive comparison":String.format(Locale.getDefault(),"%+.1f cycles/min",minute.change),23,600,Design.INK);
        Design.text(body,minute.change==null?"Both minutes must be qualified and touch in time. Missing or excluded intervals are never bridged.":"Measured difference between two touching, qualified one-minute windows.",13,400,Design.MUTED);
        RecordUi.heading(body,"Signal evidence");Design.item(body,"wave","Periodicity score",String.format(Locale.getDefault(),"%.3f",minute.correlation),null);
        Design.text(body,"Autocorrelation of the reflected signal; this is not a confidence probability or a breathing diagnosis.",13,400,Design.MUTED);
        Design.item(body,"phone","Source","Phone contactless echoes",null);Design.item(body,"info","Method","Echo periodicity v1",this::method);
        int index=rhythm.minutes.indexOf(minute);Design.space(body,18);
        if(index>0)Design.button(body,"Previous recorded minute",false,()->{selectedStart=rhythm.minutes.get(index-1).start;detailScroll=0;render();});
        if(index+1<rhythm.minutes.size())Design.button(body,"Next recorded minute",false,()->{selectedStart=rhythm.minutes.get(index+1).start;detailScroll=0;render();});
        Design.button(body,"Back to rhythm",true,this::back);
    }
    private void quality(EchoRhythm rhythm,long eligible){
        long missing=Math.max(0,eligible-rhythm.minutes.size()*EchoPeriodicity.WINDOW);
        SleepPicker.message(this,"Signal coverage",rhythm.qualified+" qualified minutes\n"+rhythm.insufficient+" with insufficient signal\n"+rhythm.unstable+" with no stable period\n"+rhythm.ambiguous+" with conflicting periods\n"+RecordUi.duration(missing)+" without a complete analyzed window\n\nUnresolved or missing signal is not evidence that breathing stopped. Reviewed results omit trimmed, paused and marked-awake intervals.");
    }
    private void method(){SleepPicker.message(this,"About rhythm estimates","The phone analyzes a full minute of reflected sound at a time, looking for consistent periods between 5 and 40 cycles/min. Only signals passing continuity, repeatability and agreement checks receive a rate.\n\nTypical rate is the median. Range is the lowest and highest qualified rate. Adjacent variation is the root mean square of rate differences between touching qualified minutes; gaps are never connected.\n\nThis experimental respiratory estimate can include other movement, fans or another person. It does not detect apnea, count breaths, measure clinical breathing variability or identify sleep stages. Raw sound and echo waveforms are not saved by this experiment.");}
    private static String runState(String state){return switch(state){case "active"->"recording in progress";case "complete"->"capture complete";case "interrupted"->"capture interrupted";default->"capture state unavailable";};}
    private void restoreScroll(int position){ScrollView owned=scroll;owned.post(()->{if(scroll==owned)owned.scrollTo(0,Math.max(0,position));});}
    private void back(){if(selectedStart!=0){selectedStart=0;detailScroll=0;render();}else finish();}
    @Override public void onSaveInstanceState(Bundle state){rememberScroll();super.onSaveInstanceState(state);state.putBoolean("original",original);state.putInt("filter",filter);state.putInt("page",page);state.putInt("chartMode",chartMode);state.putLong("selectedStart",selectedStart);state.putInt("overviewScroll",overviewScroll);state.putInt("detailScroll",detailScroll);}
    @Override protected void onDestroy(){generation++;reader.shutdownNow();super.onDestroy();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
}
