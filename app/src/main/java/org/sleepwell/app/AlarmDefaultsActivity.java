package org.sleepwell.app;

import android.app.*;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

/** Behavior defaults only. The retained save job never owns an ordinary alarm or a schedule. */
public final class AlarmDefaultsActivity extends Activity implements AlarmDefaultsSave.Observer {
    private static final int EDIT_DEFAULTS=914;
    private AlarmDefaults selected;private AlarmDefaultsSave job;private boolean fromDraft,draftChanged;
    private String readError="",editError="",editRequest="",editSnapshot="";

    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);RecordUi.review(this);Object retained=getLastNonConfigurationInstance();
        try{job=retained instanceof AlarmDefaultsSave?(AlarmDefaultsSave)retained:new AlarmDefaultsSave(AlarmDefaultsSettings.factory.open(getApplicationContext()));}
        catch(RuntimeException unavailable){readError="Alarm defaults are unavailable. Reopen this page to try again.";}
        AlarmDefaultsSettings.Read current=null;
        if(job!=null)try{current=job.port.read();readError=current.error;}catch(RuntimeException unavailable){readError="Saved defaults could not be read. Factory settings are shown for review.";}
        fromDraft=saved==null?getIntent().hasExtra("template"):saved.getBoolean("fromDraft");
        try{selected=saved!=null?AlarmDefaultsJson.decode(saved.getString("template")):fromDraft?AlarmDefaultsJson.decode(getIntent().getStringExtra("template")):current!=null&&current.defaults!=null?current.defaults:new AlarmDefaults(new Alarm(1));}
        catch(IllegalArgumentException invalid){selected=new AlarmDefaults(new Alarm(1));readError="The defaults draft could not be read. Factory settings are shown for review.";}
        if(saved!=null){editRequest=saved.getString("editRequest","");editSnapshot=saved.getString("editSnapshot","");editError=saved.getString("editError","");draftChanged=saved.getBoolean("draftChanged");}
        if(job==null)changed();else job.observe(this);
    }
    @Override public Object onRetainNonConfigurationInstance(){return job;}
    @Override public void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putString("template",AlarmDefaultsJson.encode(selected));out.putBoolean("fromDraft",fromDraft);out.putBoolean("draftChanged",draftChanged);out.putString("editRequest",editRequest);out.putString("editSnapshot",editSnapshot);out.putString("editError",editError);}

    @Override public void changed(){
        if(isDestroyed()||selected==null)return;
        if(job!=null&&job.saved){readError="";draftChanged=false;if(job.reset){selected=new AlarmDefaults(new Alarm(1));fromDraft=false;}}
        boolean busy=job!=null&&job.busy;
        LinearLayout body=Design.page(this,false,null,null);Design.header(body,"New alarm defaults",this::finish);Design.space(body,12);
        Design.text(body,fromDraft?"Use this draft’s behavior for future alarms.":"Choose the starting settings for future alarms.",15,400,Design.MUTED);Design.space(body,16);
        Alarm value=selected.create(1);
        PlanActivity.detail(body,"tasks","Challenges",value.challenges().summary(),null,null);Design.line(body);
        PlanActivity.detail(body,"sound","Sound",value.soundMode.equals(AlarmSoundMode.SINGLE)?AlarmEditorActivity.toneName(value.tone):value.soundMode.title(),value.volume+"%",null);Design.line(body);
        PlanActivity.detail(body,"sun","Gentle increase",value.rampSeconds==0?"Off":value.rampSeconds+" sec",null,null);Design.line(body);
        PlanActivity.detail(body,"alarm","Snooze",value.snoozeMinutes+" min",value.snoozeLimit+" maximum",null);Design.space(body,12);
        View edit=PlanActivity.detail(body,"settings","Edit default settings","Challenges, sound, wake-up and snooze",null,this::editDefaults);RecordUi.enabled(edit,!busy&&job!=null);Design.space(body,12);
        Design.text(body,"New alarms start off at 7:00 with no repeat days. Existing alarms stay as saved.",13,400,Design.MUTED);Design.space(body,16);
        View reset=Design.button(body,"Restore factory defaults",false,()->{editError="";job.start(null);});RecordUi.enabled(reset,!busy&&job!=null);
        if(job==null)Design.button(body,"Retry reading defaults",false,this::recreate);
        LinearLayout footer=RecordUi.footer(body);
        String message=!editError.isEmpty()?editError:job!=null&&!job.error.isEmpty()?job.error:job!=null&&job.saved?(job.reset?"Factory defaults restored.":"Saved for new alarms."):!readError.isEmpty()?readError:draftChanged?"Review these changes, then save them for future alarms.":"";
        boolean problem=!editError.isEmpty()||job!=null&&!job.error.isEmpty()||!readError.isEmpty();
        TextView status=Design.text(footer,message,13,500,problem?NightDialTokens.NEGATIVE:Design.BROWN);status.setContentDescription("Alarm defaults status");status.setVisibility(message.isEmpty()?View.GONE:View.VISIBLE);status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        View save=Design.button(footer,busy?"Saving…":"Save new alarm defaults",true,()->{editError="";job.start(selected);});RecordUi.enabled(save,!busy&&job!=null);
    }
    private void editDefaults(){
        if(job==null||job.busy||!editRequest.isEmpty()||isFinishing()||isDestroyed()||!hasWindowFocus())return;
        editError="";editRequest=java.util.UUID.randomUUID().toString();editSnapshot=AlarmDefaultsJson.encode(selected);
        try{startActivityForResult(AlarmEditorActivity.defaultsDraft(this,selected,editRequest),EDIT_DEFAULTS);}
        catch(RuntimeException unavailable){editRequest="";editSnapshot="";editError="The defaults editor could not open. Your current defaults were kept.";changed();}
    }
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);if(request!=EDIT_DEFAULTS)return;
        String expected=editRequest,snapshot=editSnapshot;editRequest="";editSnapshot="";if(result!=RESULT_OK)return;
        try{if(expected.isEmpty()||data==null||!expected.equals(data.getStringExtra(AlarmEditorActivity.DEFAULTS_REQUEST))||!snapshot.equals(AlarmDefaultsJson.encode(selected))||job==null||job.busy)throw new IllegalArgumentException();selected=AlarmDefaultsJson.decode(data.getStringExtra(AlarmEditorActivity.DEFAULTS_TEMPLATE));job.saved=false;job.reset=false;job.error="";editError="";draftChanged=true;}
        catch(IllegalArgumentException unavailable){editError="The edited defaults could not be applied. Your previous draft was kept.";}
        changed();
    }
    boolean awaitWorkers(long millis)throws InterruptedException{return job==null||job.awaitClosed(millis);}
    @Override protected void onDestroy(){if(job!=null){job.detach();if(!isChangingConfigurations())job.close();}super.onDestroy();}
    @Override protected void onPostResume(){super.onPostResume();RecordUi.lightBars(this);}
    @Override public void onWindowFocusChanged(boolean focus){super.onWindowFocusChanged(focus);if(focus)RecordUi.lightBars(this);}
}
