package org.sleepwell.app;

import android.app.Activity;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.function.Supplier;

/** Read-only milestones from the pinned record source; never starts tracking. */
public final class RecordAchievementsActivity extends Activity {
    static Supplier<Instant> clock=Instant::now;
    private final ExecutorService worker=Executors.newSingleThreadExecutor(r->new Thread(r,"recorded-milestones"));
    private RecordAchievements data;
    private String error="";
    private boolean loading,renderedData;
    private int generation,milestone,drawnMilestone,overviewScroll,detailScroll;
    private LinearLayout body;

    @Override public void onCreate(Bundle state){
        super.onCreate(state);RecordUi.review(this);
        if(state!=null){int selected=state.getInt("milestone");milestone=selected==1||selected==7||selected==30?selected:0;overviewScroll=state.getInt("overviewScroll");detailScroll=state.getInt("detailScroll");}
        if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::back);
        render();
    }
    @Override protected void onResume(){super.onResume();load();}
    private void load(){
        int request=++generation;loading=true;error="";render();Records.Factory pinned=Records.factory;Supplier<Instant> time=clock;
        worker.execute(()->{try(Store store=pinned.open(getApplicationContext())){
            RecordAchievements result=RecordAchievementsReader.read(store,time.get().toEpochMilli());
            runOnUiThread(()->{if(isDestroyed()||request!=generation)return;data=result;loading=false;render();});
        }catch(Exception failure){runOnUiThread(()->{if(isDestroyed()||request!=generation)return;loading=false;error=failure instanceof IllegalStateException?failure.getMessage():"Recorded progress is unavailable. Your records are kept.";if(error==null||error.isBlank())error="Recorded progress is unavailable. Your records are kept.";render();});}});
    }
    private void render(){
        rememberScroll();renderedData=false;drawnMilestone=milestone;
        body=Design.page(this,false,null,n->{});RecordUi.label(this,body);RecordUi.header(body,milestone==0?"Achievements":title(milestone),this::back);Design.space(body,12);
        if(!error.isEmpty()){Design.text(body,error,16,500,Design.BROWN).setContentDescription("Achievements read error");Design.button(body,"Retry achievements",true,this::load);restoreScroll();return;}
        if(data==null){Design.text(body,"Reading saved evidence…",15,400,Design.MUTED);restoreScroll();return;}
        if(milestone==0)overview();else evidence();
        renderedData=true;restoreScroll();
    }
    private void overview(){
        Design.text(body,data.count+" qualifying "+(data.count==1?"night":"nights"),30,700,Design.INK).setContentDescription("Qualifying recorded nights "+data.count);
        Design.space(body,6);Design.text(body,RecordUi.duration(data.millis)+" of valid sensor coverage on these recorded days",13,400,Design.MUTED);
        if(loading)Design.text(body,"Refreshing…",12,400,Design.MUTED);Design.space(body,20);
        milestone(1);milestone(7);milestone(30);Design.space(body,12);
        PlanActivity.detail(body,"shield","How progress is counted","At least 3 h of valid saved signal per recorded day",null,()->SleepPicker.message(this,"What qualifies?","Completed phone-motion or contactless recordings with at least 3 hours of valid saved sensor coverage per noon-to-noon recorded day. Pauses, marked-awake time, trims and overlaps are excluded. Nap-tagged, manual/Health Connect/unknown records, interrupted periods and clock-changed periods do not qualify. This measures recording coverage, not confirmed sleep."));Design.line(body);
        Design.text(body,"Calculated from current saved records. Editing or deleting a period can change progress. No streak or sleep-quality score is inferred.",12,400,Design.MUTED);
        if(data.count==0){Design.space(body,16);Design.text(body,"No qualifying nights yet",19,600,Design.INK);Design.text(body,"Missing or incomplete nights do not become zero-sleep days.",12,400,Design.MUTED);}
        else{RecordUi.heading(body,"Recent qualifying days");int from=Math.max(0,data.days.size()-7);for(int n=data.days.size()-1;n>=from;n--){RecordAchievements.Day day=data.days.get(n);PlanActivity.detail(body,"moon",day.date.toString(),null,RecordUi.duration(day.millis),()->report(day.record));}}
        Design.space(body,12);Design.button(body,"Review saved records",true,()->startActivity(RecordUi.intent(this,HistoryActivity.class)));Design.button(body,"Refresh achievements",false,this::load).setEnabled(!loading);
    }
    private static String title(int target){return target==1?"First recorded night":target==7?"Seven recorded nights":"Thirty recorded nights";}
    private void milestone(int target){
        LinearLayout row=Design.row(this);row.setGravity(Gravity.TOP);row.setMinimumHeight(Design.dp(this,64));Design.pad(row,0,14);
        row.addView(new Design.Icon(this,data.reached(target)?"check":"moon",data.reached(target)?Design.BROWN:Design.MUTED),new LinearLayout.LayoutParams(Design.dp(this,26),Design.dp(this,26)));
        LinearLayout text=Design.col(this);text.setPadding(Design.dp(this,14),0,Design.dp(this,8),0);Design.text(text,title(target),17,600,Design.INK);Design.space(text,5);
        Design.text(text,data.reached(target)?"Reached · review contributing nights":data.progress(target)+" of "+target+" qualifying nights · review",13,400,Design.MUTED);row.addView(text,new LinearLayout.LayoutParams(0,-2,1));
        LinearLayout.LayoutParams arrow=new LinearLayout.LayoutParams(Design.dp(this,18),Design.dp(this,18));arrow.gravity=Gravity.CENTER_VERTICAL;row.addView(new Design.Icon(this,"chevron",Design.INK),arrow);
        row.setContentDescription(title(target)+": "+data.progress(target)+" of "+target);row.setFocusable(true);SoundEventUi.button(row);row.setBackground(Design.touch(this,android.graphics.Color.TRANSPARENT,10));
        row.setOnClickListener(v->{InteractionFeedback.action(v);rememberScroll();milestone=target;detailScroll=0;render();});body.addView(row);Design.line(body);
    }
    private void evidence(){
        int shown=data.progress(milestone);
        Design.text(body,shown+" of "+milestone+" qualifying nights",26,600,Design.INK).setContentDescription("Milestone basis "+shown+" of "+milestone);
        Design.space(body,8);Design.text(body,"The first "+shown+" qualifying recorded days in current saved evidence. Open each contributing period to review its original source and edits.",14,400,Design.MUTED);
        Design.space(body,8);Design.text(body,"Each day needs at least 3 h of valid signal. Pauses, awake edits, trims, overlaps and ineligible records keep the same exclusions. This is recording coverage, not confirmed sleep.",12,400,Design.MUTED);
        if(loading)Design.text(body,"Refreshing…",12,400,Design.MUTED);
        if(shown==0){Design.space(body,20);Design.text(body,"No qualifying days for this milestone",18,600,Design.INK);}
        for(int n=0;n<shown;n++){
            RecordAchievements.Day day=data.days.get(n);
            RecordUi.heading(body,day.date+" · "+RecordUi.duration(day.millis)+" valid signal");
            for(RecordAchievements.Contribution part:day.contributions){
                RecordEntry record=part.record;
                String interval=RecordUi.clock(this,record.edit.start,record.zone())+" – "+RecordUi.clock(this,record.edit.end,record.zone());
                LinearLayout row=PlanActivity.detail(body,"moon",interval,record.provenance()+" · "+RecordUi.duration(part.millis)+" contributing valid signal\n"+record.zone,null,()->report(record.id));
                row.setContentDescription("Open milestone record "+record.id);Design.line(body);
            }
        }
        Design.space(body,16);Design.button(body,"All milestones",true,this::back);Design.button(body,"Refresh milestone",false,this::load).setEnabled(!loading);
    }
    private void report(long id){startActivity(RecordUi.intent(this,RecordReportActivity.class).putExtra("record",id));}
    private void rememberScroll(){if(renderedData&&body!=null&&body.getParent() instanceof ScrollView scroll){if(drawnMilestone==0)overviewScroll=scroll.getScrollY();else detailScroll=scroll.getScrollY();}}
    private void restoreScroll(){LinearLayout page=body;if(page.getParent() instanceof ScrollView scroll){int position=milestone==0?overviewScroll:detailScroll;scroll.post(()->{if(body==page&&!isDestroyed())scroll.scrollTo(0,position);});}}
    private void back(){if(milestone==0)finish();else{rememberScroll();milestone=0;render();}}
    @android.annotation.SuppressLint("GestureBackNavigation") @Override public void onBackPressed(){back();}
    @Override public void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);rememberScroll();state.putInt("milestone",milestone);state.putInt("overviewScroll",overviewScroll);state.putInt("detailScroll",detailScroll);}
    void awaitReaders(long timeout)throws Exception{if(worker.isShutdown()){if(!worker.awaitTermination(timeout,TimeUnit.MILLISECONDS))throw new TimeoutException("Achievements reader busy");}else worker.submit(()->{}).get(timeout,TimeUnit.MILLISECONDS);}
    @Override protected void onDestroy(){generation++;worker.shutdownNow();super.onDestroy();}
    @Override public void onWindowFocusChanged(boolean focus){super.onWindowFocusChanged(focus);if(focus)RecordUi.lightBars(this);}
}
