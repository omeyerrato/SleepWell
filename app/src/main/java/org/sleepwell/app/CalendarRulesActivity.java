package org.sleepwell.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

/** Optional calendar selection. Only Save/Refresh changes future repeating schedules. */
public final class CalendarRulesActivity extends Activity {
    private CalendarPolicy.Rules draft;private List<CalendarAccess.Source> sources=List.of();
    private CalendarAccess.Port access;private String error="",status="";private boolean loading,busy;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();private final CancellationSignal cancel=new CancellationSignal();
    @Override public void onCreate(Bundle saved){super.onCreate(saved);RecordUi.review(this);access=CalendarAccess.factory.open(getApplicationContext());try{draft=saved==null?CalendarSettings.load(this):CalendarSettings.decode(saved.getString("draft"));}catch(RuntimeException invalid){draft=CalendarPolicy.Rules.off();error="Saved calendar rules need review. Choose your calendars again.";}load();}
    @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putString("draft",CalendarSettings.encode(draft));}
    private void load(){loading=true;render();worker.execute(()->{List<CalendarAccess.Source> result=List.of();String failure="";try{if(access.allowed())result=access.sources(cancel);}catch(RuntimeException unavailable){failure="Calendars could not be read. Check access and try again.";}List<CalendarAccess.Source> found=result;String message=failure;runOnUiThread(()->{if(isDestroyed())return;sources=found;loading=false;if(!message.isEmpty())error=message;render();});});}
    private void change(boolean enabled,Set<Long> holidays,Set<Long> meetings,int preparation,int advance){draft=new CalendarPolicy.Rules(enabled,holidays,meetings,preparation,advance,draft.revision);status="";}
    private void select(long id,boolean holiday,boolean checked){Set<Long> h=new HashSet<>(draft.holidays),m=new HashSet<>(draft.meetings),chosen=holiday?h:m;if(checked)chosen.add(id);else chosen.remove(id);change(draft.enabled,h,m,draft.preparation,draft.advance);}
    private void render(){LinearLayout body=Design.page(this,false,null,n->{});RecordUi.label(this,body);RecordUi.header(body,"Calendar alarms",this::finish);Design.space(body,12);Design.text(body,"Optional rules for repeating alarms.",15,400,Design.MUTED);Design.space(body,16);
        Design.toggle(body,"Use calendar rules","One-time alarms and naps keep their own times",draft.enabled,on->change(on,draft.holidays,draft.meetings,draft.preparation,draft.advance));Design.space(body,16);
        if(!access.allowed()){Design.text(body,"Calendar access is not allowed.",15,500,Design.INK);Design.button(body,"Allow calendar access",false,()->requestPermissions(new String[]{Manifest.permission.READ_CALENDAR},813));}
        else if(loading)Design.text(body,"Reading calendars…",14,400,Design.MUTED);
        else if(sources.isEmpty()){Design.text(body,"No visible calendars on this phone.",15,500,Design.INK);Design.text(body,"Add or show a calendar in your calendar app, then refresh.",13,400,Design.MUTED);}
        else for(CalendarAccess.Source source:sources){RecordUi.heading(body,source.name);Design.toggle(body,"Skip all-day dates",null,draft.holidays.contains(source.id),on->select(source.id,true,on));Design.space(body,6);Design.toggle(body,"Earlier for meetings",null,draft.meetings.contains(source.id),on->select(source.id,false,on));Design.space(body,10);}
        Design.space(body,10);Design.item(body,"clock","Preparation time",draft.preparation+" min",()->SleepPicker.choices(this,"Preparation time",new String[]{"15 min","30 min","45 min","60 min","90 min","120 min"},index(new int[]{15,30,45,60,90,120},draft.preparation),n->{change(draft.enabled,draft.holidays,draft.meetings,new int[]{15,30,45,60,90,120}[n],draft.advance);render();}));
        Design.item(body,"sun","Maximum advance",draft.advance+" min",()->SleepPicker.choices(this,"Maximum advance",new String[]{"30 min","60 min","120 min","180 min","360 min"},index(new int[]{30,60,120,180,360},draft.advance),n->{change(draft.enabled,draft.holidays,draft.meetings,draft.preparation,new int[]{30,60,120,180,360}[n]);render();}));Design.space(body,10);
        Design.text(body,"Meetings can move an alarm earlier on the same day. Missing calendar data keeps its ordinary time.",12,400,Design.MUTED);long refreshed=CalendarSettings.prefs(this).getLong("refreshed",0);Design.space(body,10);Design.text(body,refreshed==0?"Last refreshed: Never":"Last refreshed: "+RecordUi.clock(this,refreshed,ZoneId.systemDefault()),12,400,Design.MUTED);
        String syncError=CalendarSettings.prefs(this).getString("error","");if(!syncError.isEmpty()){Design.space(body,6);Design.text(body,syncError,13,500,Design.BROWN);}Design.button(body,"Reload calendar list",false,()->{error="";load();});
        Design.space(body,16);PlanActivity.detail(body,"calendar","Save future nights","Separate automatic calendar export",null,()->startActivity(RecordUi.intent(this,AutoCalendarActivity.class)));LinearLayout footer=RecordUi.footer(body);if(!error.isEmpty())Design.text(footer,error,13,500,0xffa93919);if(!status.isEmpty())Design.text(footer,status,13,500,Design.BROWN);Design.button(footer,busy?"Saving…":"Save calendar rules",true,this::save).setEnabled(!busy);RecordUi.enabled(body,!busy);
    }
    private int index(int[] values,int selected){for(int n=0;n<values.length;n++)if(values[n]==selected)return n;return -1;}
    private void save(){if(busy)return;if(draft.enabled&&(!access.allowed()||draft.holidays.isEmpty()&&draft.meetings.isEmpty())){error="Allow access and select at least one calendar.";render();return;}CalendarPolicy.Rules value=new CalendarPolicy.Rules(draft.enabled,draft.holidays,draft.meetings,draft.preparation,draft.advance,UUID.randomUUID().toString());busy=true;error="";render();worker.execute(()->{String result="";try{CalendarSettings.save(getApplicationContext(),value);CalendarSync.port.apply(getApplicationContext());}catch(RuntimeException failed){result="Rules could not be applied. Check access and try again.";}String failure=result;runOnUiThread(()->{if(isDestroyed())return;busy=false;draft=value;error=failure;status=failure.isEmpty()?"Calendar rules saved.":"";render();});});}
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] grants){super.onRequestPermissionsResult(request,permissions,grants);if(request==813)load();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)RecordUi.lightBars(this);}
    @Override protected void onDestroy(){cancel.cancel();worker.shutdown();super.onDestroy();}
    boolean awaitWorkers(long millis)throws InterruptedException{return worker.awaitTermination(millis,TimeUnit.MILLISECONDS);}
}
