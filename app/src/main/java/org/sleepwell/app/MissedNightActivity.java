package org.sleepwell.app;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.LongSupplier;

/** User-reviewed missing night. Nothing runs in the background or inserts on opening. */
public final class MissedNightActivity extends Activity {
    static LongSupplier clock=System::currentTimeMillis;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final Handler handler=new Handler(Looper.getMainLooper());
    private Records.Factory records;private MissedNightUsagePort usage;private ZoneId zone;private LocalDate date;
    private long begin,end,savedId;private List<MissedNight.Interval> choices=List.of();private MissedNight.Interval evidence;private RecordDraft draft;
    private String status="",error="",saveToken,createToken;private boolean loading,resumed;private int generation;
    private Dialog dialog;private String pickerField="",pickerStage="";private LocalDate pendingDate;private Bundle restorePicker;
    private final Runnable poll=this::observeSave;
    @Override public void onCreate(Bundle state){
        super.onCreate(state);RecordUi.review(this);records=Records.factory;usage=MissedNightUsage.factory.create(getApplicationContext());zone=ZoneId.systemDefault();date=Instant.ofEpochMilli(clock.getAsLong()).atZone(zone).toLocalDate();createToken=UUID.randomUUID().toString();
        if(state!=null){zone=ZoneId.of(state.getString("zone",zone.getId()));date=LocalDate.parse(state.getString("date",date.toString()));begin=state.getLong("begin");end=state.getLong("end");status=state.getString("status","");error=state.getString("error","");savedId=state.getLong("savedId");createToken=state.getString("createToken",createToken);saveToken=state.getString("saveToken");long[] pairs=state.getLongArray("pairs");List<MissedNight.Interval> restored=new ArrayList<>();if(pairs!=null)for(int n=0;n+1<pairs.length;n+=2)restored.add(new MissedNight.Interval(pairs[n],pairs[n+1]));choices=restored;if(state.containsKey("evidenceStart")){evidence=new MissedNight.Interval(state.getLong("evidenceStart"),state.getLong("evidenceEnd"));draft=new RecordDraft(state.getLong("draftStart"),state.getLong("draftEnd"));}pickerField=state.getString("pickerField","");pickerStage=state.getString("pickerStage","");if(state.containsKey("pendingDate"))pendingDate=LocalDate.parse(state.getString("pendingDate"));restorePicker=state.getBundle("picker");}
        if(android.os.Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::finish);
        if(status.isEmpty()&&saveToken==null)check();else render();
    }
    @Override protected void onResume(){super.onResume();resumed=true;if(saveToken!=null)observeSave();else if(!status.equals("saved"))try{if(!usage.allowed()&&!status.equals("denied")){status="denied";draft=null;choices=List.of();render();}else if(usage.allowed()&&status.equals("denied"))check();}catch(RuntimeException failure){failed(failure);}restoreDialog();}
    @Override protected void onPause(){resumed=false;handler.removeCallbacks(poll);super.onPause();}
    private void check(){
        if(loading||saveToken!=null)return;try{MissedNight.Window window=new MissedNight.Window(date,zone,clock.getAsLong());begin=window.start;end=window.end;}catch(RuntimeException failure){failed(failure);return;}
        int ticket=++generation;loading=true;error="";draft=null;evidence=null;choices=List.of();status="loading";render();long from=begin,to=end;Records.Factory pinned=records;MissedNightUsagePort source=usage;
        worker.execute(()->{String found="",problem="";List<MissedNight.Interval> available=List.of();try{if(!source.allowed())found="denied";else{List<MissedNight.Event> events=source.query(from,to);List<MissedNight.Interval> paired=MissedNight.pairs(events,from,to);try(Store store=pinned.open(getApplicationContext())){available=MissedNight.available(paired,MissedNightRecords.occupied(store));}found=events.isEmpty()?"empty":paired.isEmpty()?"incomplete":available.isEmpty()?"covered":"ready";}}catch(SecurityException denied){found="denied";problem=denied.getMessage();}catch(Exception failure){found="error";problem=message(failure);}String result=found,issue=problem;List<MissedNight.Interval> items=List.copyOf(available.subList(0,Math.min(24,available.size())));runOnUiThread(()->{if(isDestroyed()||ticket!=generation)return;loading=false;status=result;error=issue==null?"":issue;choices=items;if(!choices.isEmpty())select(choices.get(0));else render();});});
    }
    private void select(MissedNight.Interval interval){evidence=interval;draft=new RecordDraft(interval.start,interval.end);error="";render();}
    private void render(){
        LinearLayout body=Design.page(this,false,null,n->{});RecordUi.label(this,body);LinearLayout header=RecordUi.header(body,"Missing night",this::finish);RecordUi.headerAction(header,"info","About phone-idle estimates",()->dialog=SleepPicker.message(this,"Phone-idle estimate","An inactive phone does not prove you were asleep. Review the dates and times before saving. This creates a manual entry with no sleep stages, movement, sound analysis or audio. Android may keep only a few days of screen events."));
        Design.space(body,10);PlanActivity.detail(body,"calendar","Night ending",null,date.format(DateTimeFormatter.ofPattern("EEE d MMM",Locale.getDefault())),this::chooseNight);Design.line(body);Design.space(body,20);
        if(status.equals("ready")||status.equals("saved"))review(body);else{
            String title=switch(status){case "denied"->"Allow phone activity access";case "empty"->"No phone activity available";case "incomplete"->"No complete inactive period";case "covered"->"This night is already recorded";case "error"->"Phone activity unavailable";default->"Checking this night…";};
            String copy=switch(status){case "denied"->"Usage Access lets SleepWell read screen on and off times. Nothing is saved until you review it.";case "empty"->"Android did not return screen events for this night. A missing event is not a sleep estimate.";case "incomplete"->"Both a screen-off and a later screen-on event are needed. Missing endpoints are not guessed.";case "covered"->"The complete inactive periods overlap existing sleep records. Your history has not changed.";case "error"->"Check phone activity access and try again.";default->"Reading screen events on this phone.";};
            Design.text(body,title,23,600,Design.INK);Design.space(body,12);Design.text(body,copy,14,400,Design.MUTED);
        }
        LinearLayout footer=RecordUi.footer(body);TextView problem=Design.text(footer,error,12,500,0xffa93919);problem.setContentDescription("Missing night error");problem.setVisibility(error.isEmpty()?View.GONE:View.VISIBLE);problem.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        if(status.equals("denied"))Design.button(footer,"Open Usage Access",true,this::access);
        else if(status.equals("saved"))Design.button(footer,"Done",true,this::finish);
        else if(draft!=null){Design.button(footer,saveToken==null?"Save reviewed night":"Saving…",true,this::save);Design.button(footer,"Check again",false,this::check);}
        else if(!loading)Design.button(footer,"Check again",true,this::check);
        if(loading||saveToken!=null){RecordUi.enabled(body,false);RecordUi.enabled(footer,false);footer.setAlpha(.55f);}
    }
    private void review(LinearLayout body){
        Design.text(body,status.equals("saved")?"Saved to history":"Phone-idle estimate",22,600,Design.INK);Design.space(body,5);Design.text(body,status.equals("saved")?"Manual entry · no sensor measurements":"Review when you actually slept before saving.",13,400,Design.MUTED);Design.space(body,20);
        Design.text(body,"Reviewed duration",14,500,Design.INK);Design.space(body,5);Design.text(body,"",42,700,Design.INK).setText(RecordUi.units(RecordUi.duration(draft.end-draft.start)));Design.space(body,12);
        body.addView(new MissedNightChart(this,begin,end,evidence,draft,zone),new LinearLayout.LayoutParams(-1,Design.dp(this,130)));
        Design.space(body,12);PlanActivity.detail(body,"moon","Start",RecordUi.date(draft.start,zone),RecordUi.clock(this,draft.start,zone),status.equals("saved")?null:()->chooseDate("Start",null));Design.line(body);PlanActivity.detail(body,"sun","End",RecordUi.date(draft.end,zone),RecordUi.clock(this,draft.end,zone),status.equals("saved")?null:()->chooseDate("End",null));Design.line(body);
        if(status.equals("ready")&&choices.size()>1){PlanActivity.detail(body,"tasks","Longest inactive periods",null,""+choices.size(),()->{String[] labels=new String[choices.size()];int selected=0;for(int n=0;n<labels.length;n++){MissedNight.Interval p=choices.get(n);labels[n]=RecordUi.clock(this,p.start,zone)+" – "+RecordUi.clock(this,p.end,zone)+" · "+RecordUi.duration(p.end-p.start);if(p.same(evidence))selected=n;}dialog=SleepPicker.choices(this,"Inactive periods",labels,selected,n->select(choices.get(n)));});Design.line(body);}
        Design.space(body,14);LinearLayout tag=Design.row(this);tag.addView(new Design.Icon(this,"tag",Design.BROWN),new LinearLayout.LayoutParams(Design.dp(this,22),Design.dp(this,22)));TextView value=Design.text(this,"Phone-idle estimate",13,500,Design.INK);value.setPadding(Design.dp(this,10),0,0,0);tag.addView(value);body.addView(tag);Design.space(body,6);Design.text(body,"Saved as a manual record. No stages or recordings are added.",12,400,Design.MUTED);
    }
    private void chooseNight(){if(loading||saveToken!=null)return;LocalDate today=Instant.ofEpochMilli(clock.getAsLong()).atZone(zone).toLocalDate();String[] labels=new String[3];int selected=0;for(int n=0;n<3;n++){LocalDate day=today.minusDays(n);labels[n]=day.format(DateTimeFormatter.ofPattern("EEE d MMM uuuu",Locale.getDefault()));if(day.equals(date))selected=n;}dialog=SleepPicker.choices(this,"Night ending",labels,selected,n->{date=today.minusDays(n);savedId=0;createToken=UUID.randomUUID().toString();check();});}
    private void chooseDate(String field,Bundle restore){
        pickerField=field;pickerStage="date";LocalDate first=Instant.ofEpochMilli(begin).atZone(zone).toLocalDate(),last=Instant.ofEpochMilli(end-1).atZone(zone).toLocalDate();List<LocalDate> days=new ArrayList<>();for(LocalDate d=first;!d.isAfter(last);d=d.plusDays(1))days.add(d);String[] labels=days.stream().map(d->d.format(DateTimeFormatter.ofPattern("EEE d MMM uuuu",Locale.getDefault()))).toArray(String[]::new);LocalDate current=Instant.ofEpochMilli(field.equals("Start")?draft.start:draft.end).atZone(zone).toLocalDate();int selected=Math.max(0,days.indexOf(current));dialog=SleepPicker.choices(this,field+" date",labels,selected,n->{pendingDate=days.get(n);chooseTime(field,null);});if(restore!=null)dialog.onRestoreInstanceState(restore);
    }
    private void chooseTime(String field,Bundle restore){pickerField=field;pickerStage="time";ZonedDateTime original=Instant.ofEpochMilli(field.equals("Start")?draft.start:draft.end).atZone(zone);dialog=SleepPicker.time(this,field+" time",original.getHour()*60+original.getMinute(),minute->{LocalDateTime local=LocalDateTime.of(pendingDate,LocalTime.of(minute/60,minute%60));List<ZoneOffset> offsets=zone.getRules().getValidOffsets(local);if(offsets.size()>1){pickerStage="offset";String[] labels=offsets.stream().map(offset->"UTC"+offset).toArray(String[]::new);dialog=SleepPicker.choices(this,"Repeated time offset",labels,-1,n->applyTime(field,local,offsets.get(n)));}else applyTime(field,local,null);});if(restore!=null)dialog.onRestoreInstanceState(restore);}
    private void applyTime(String field,LocalDateTime local,ZoneOffset offset){try{long value=RecordDraft.instant(local,zone,offset),start=field.equals("Start")?value:draft.start,finish=field.equals("End")?value:draft.end;MissedNight.validateReview(start,finish,begin,end,clock.getAsLong());draft.start=start;draft.end=finish;error="";render();}catch(RuntimeException failure){failed(failure);}}
    private void restoreDialog(){if(restorePicker==null||draft==null)return;Bundle saved=restorePicker;restorePicker=null;if(pickerStage.equals("date"))chooseDate(pickerField,saved);else if(pickerStage.equals("time")&&pendingDate!=null)chooseTime(pickerField,saved);}
    private void access(){try{usage.settings(this);}catch(RuntimeException failure){failed(new IllegalStateException("Android Usage Access settings could not open. Open SleepWell in your phone settings and try again."));}}
    private void save(){if(saveToken!=null||draft==null||evidence==null)return;try{MissedNight.validateReview(draft.start,draft.end,begin,end,clock.getAsLong());saveToken=UUID.randomUUID().toString();error="";render();observeSave();}catch(RuntimeException failure){failed(failure);}}
    private void observeSave(){
        if(!resumed||saveToken==null||draft==null)return;Records.Factory pinned=records;MissedNightUsagePort source=usage;RecordDraft copy=draft.copy();copy.addTag("Phone-idle estimate");copy.note="Reviewed phone-idle estimate from screen off/on events. Phone inactivity does not prove sleep. No sensor measurements were recorded.";MissedNight.Interval pair=evidence;ZoneId savedZone=zone;long from=begin,to=end;String request=createToken;
        PlanOperations.Operation operation=PlanOperations.start(this,saveToken,app->{try(Store store=pinned.open(app)){return ""+MissedNightRecords.save(store,source,pair,copy,savedZone,from,to,clock.getAsLong(),request);}});
        if(!operation.done){handler.postDelayed(poll,75);return;}PlanOperations.forget(saveToken);saveToken=null;if(operation.error!=null){error=operation.error;render();return;}savedId=Long.parseLong(operation.notice);status="saved";setResult(RESULT_OK);render();
    }
    private static String message(Exception failure){return failure.getMessage()==null?"Phone activity could not be read.":failure.getMessage();}
    private void failed(Exception failure){error=message(failure);if(status.isEmpty()||status.equals("loading"))status="error";render();}
    @Override protected void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);state.putString("zone",zone.getId());state.putString("date",date.toString());state.putLong("begin",begin);state.putLong("end",end);state.putString("status",loading?"":status);state.putString("error",error);state.putLong("savedId",savedId);state.putString("createToken",createToken);state.putString("saveToken",saveToken);long[] pairs=new long[choices.size()*2];for(int n=0;n<choices.size();n++){pairs[n*2]=choices.get(n).start;pairs[n*2+1]=choices.get(n).end;}state.putLongArray("pairs",pairs);if(evidence!=null&&draft!=null){state.putLong("evidenceStart",evidence.start);state.putLong("evidenceEnd",evidence.end);state.putLong("draftStart",draft.start);state.putLong("draftEnd",draft.end);}if(dialog instanceof SleepPicker.StateDialog&&dialog.isShowing()){state.putString("pickerField",pickerField);state.putString("pickerStage",pickerStage);if(pendingDate!=null)state.putString("pendingDate",pendingDate.toString());state.putBundle("picker",dialog.onSaveInstanceState());}}
    @Override public void finish(){if(saveToken==null)super.finish();else Ui.toast(this,"Please wait for this save to finish.");}
    @android.annotation.SuppressLint("GestureBackNavigation") @Override public void onBackPressed(){finish();}
    @Override public void onWindowFocusChanged(boolean focus){super.onWindowFocusChanged(focus);if(focus)RecordUi.lightBars(this);}
    void awaitReaders(long timeoutMs)throws Exception{if(worker.isShutdown()){if(!worker.awaitTermination(timeoutMs,TimeUnit.MILLISECONDS))throw new TimeoutException("Phone activity reader is still active.");}else worker.submit(()->{}).get(timeoutMs,TimeUnit.MILLISECONDS);}
    @Override protected void onDestroy(){generation++;handler.removeCallbacksAndMessages(null);if(dialog!=null)dialog.dismiss();worker.shutdown();super.onDestroy();}
}
