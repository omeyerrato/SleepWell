package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Real saved rules. Choosing and reviewing a destination never queues an event. */
public final class AutomationActivity extends Activity {
    interface Port {
        AutomationStore open(Context app) throws Exception;
        AutomationStore.Vault vault();
        String status(Context app);
        void schedule(Context app,AutomationStore store) throws Exception;
        void recover(Context app);
    }
    private static final Port PLATFORM=new Port(){
        public AutomationStore open(Context app){return new AutomationStore(app);}
        public AutomationStore.Vault vault(){return new AutomationSecrets();}
        public String status(Context app){return app.getSharedPreferences("automation_status",0).getString("error","");}
        public void schedule(Context app,AutomationStore store){Automations.schedule(app,store);}
        public void recover(Context app){Automations.recover(app);}
    };
    static Port port=PLATFORM;
    private static final Set<String> PAGES=Set.of("home","options","delivery","destination","editor","event","transport","rule-options","event-details");
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final ArrayDeque<String> history=new ArrayDeque<>();
    private Port owner;private AutomationRule draft;
    private List<AutomationStore.Row> rows=List.of();private boolean rowsLoaded,loading,resumed,secretRestorationFailed;
    private long readGeneration,navigation;private String page="home",failure="",loadFailure="",status="",request,operationKind="",confirmation="",confirmationId="",testOccurrence="";private long confirmationRevision;
    private AutomationRule.Transport destinationType=AutomationRule.Transport.INTENT;
    private Bundle positions=new Bundle();private ScrollView currentScroll;private Dialog ownedDialog;
    private EditText name,destination,topic,username,secret;private Switch enabled;

    @Override public void onCreate(Bundle state){
        super.onCreate(state);RecordUi.review(this);owner=port;
        if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::back);
        if(state!=null){
            page=state.getString("page","home");if(!PAGES.contains(page))page="home";
            positions=state.getBundle("positions")==null?new Bundle():state.getBundle("positions");
            ArrayList<String> saved=state.getStringArrayList("history");if(saved!=null)for(String prior:saved)if(PAGES.contains(prior))history.addLast(prior);
            failure=state.getString("failure","");request=state.getString("request");operationKind=state.getString("operationKind","");
            confirmation=state.getString("confirmation","");confirmationId=state.getString("confirmationId","");confirmationRevision=state.getLong("confirmationRevision");testOccurrence=state.getString("testOccurrence","");
            try{destinationType=AutomationRule.Transport.valueOf(state.getString("destinationType","INTENT"));}catch(IllegalArgumentException ignored){}
            if(state.containsKey("id"))try{
                String credential="",cipher=state.getString("secret","");secretRestorationFailed=state.getBoolean("secretRestorationFailed");
                try{if(!cipher.isEmpty())credential=owner.vault().open(cipher);}catch(Exception unavailable){secretRestorationFailed=true;}
                draft=new AutomationRule(state.getString("id"),state.getString("name",""),AutomationEvent.Kind.valueOf(state.getString("event")),AutomationRule.Transport.valueOf(state.getString("transport")),state.getBoolean("enabled"),state.getLong("revision"),state.getString("destination",""),state.getString("topic",""),state.getString("username",""),credential);
                if(secretRestorationFailed)failure="Re-enter or explicitly clear the credential before saving this draft.";
            }catch(Exception invalid){draft=null;page="home";failure="The interrupted draft could not be restored. Saved rules are unchanged.";}
        }
        if(draft==null&&editorPage(page))page="home";
        render();try{owner.recover(getApplicationContext());}catch(RuntimeException unavailable){failure="Saved rules remain. Delivery scheduling needs attention.";render();}
    }
    private static boolean editorPage(String page){return Set.of("editor","event","transport","rule-options","event-details").contains(page);}
    private boolean available(){return resumed&&!isFinishing()&&!isDestroyed()&&hasWindowFocus()&&request==null;}
    private void remember(){if(currentScroll!=null&&currentScroll.isLaidOut())positions.putInt(page,currentScroll.getScrollY());}
    private void fields(){
        if(draft==null||name==null)return;
        draft=new AutomationRule(draft.id,name.getText().toString(),draft.event,draft.transport,enabled!=null&&enabled.isChecked(),draft.revision,destination.getText().toString().trim(),topic==null?draft.topic:topic.getText().toString(),username==null?draft.username:username.getText().toString(),secret==null?draft.secret:secret.getText().toString());
    }
    private void open(String next){if(!available())return;fields();remember();navigation++;loading=false;history.push(page);page=next;render();}
    private void back(){
        if(ownedDialog!=null&&ownedDialog.isShowing()){ownedDialog.cancel();return;}
        if(request!=null){Ui.toast(this,"Please wait for this operation to finish.");return;}
        navigation++;loading=false;remember();
        if(page.equals("home")){finish();return;}
        if(page.equals("editor")){draft=null;clearFields();history.clear();page="home";failure="";refreshRows();return;}
        fields();page=history.isEmpty()?(draft==null?"home":"editor"):history.pop();render();if(page.equals("home"))refreshRows();
    }
    @android.annotation.SuppressLint("GestureBackNavigation") @Override public void onBackPressed(){back();}
    private void clearFields(){name=null;destination=null;topic=null;username=null;secret=null;enabled=null;}
    private void render(){
        if(isFinishing()||isDestroyed())return;
        LinearLayout body=Design.page(this,false,null,n->{});currentScroll=(ScrollView)body.getParent();RecordUi.label(this,body);
        String title=switch(page){case "options"->"Automation options";case "delivery"->"Delivery & privacy";case "destination"->transportLabel(destinationType)+" destination";case "editor"->draft.revision==0?"Add rule":"Edit rule";case "event"->"Sleep event";case "transport"->"Destination type";case "rule-options"->"Rule options";case "event-details"->"Event details";default->"Automations";};
        LinearLayout header=RecordUi.header(body,title,this::back);header.getLayoutParams().height=-2;header.setMinimumHeight(Design.dp(this,52));
        if(page.equals("home"))RecordUi.headerAction(header,"more","Automation options",()->open("options"));
        if(page.equals("editor"))RecordUi.headerAction(header,"more","Rule options",()->open("rule-options"));
        Design.space(body,12);
        switch(page){case "options"->options(body);case "delivery"->delivery(body);case "destination"->destinationHelp(body);case "editor"->editor(body);case "event"->events(body);case "transport"->transports(body);case "rule-options"->ruleOptions(body);case "event-details"->{Design.text(body,eventLabel(draft.event),18,600,Design.INK);Design.space(body,12);Design.text(body,eventHelp(draft.event),14,400,Design.MUTED);}default->home(body);}
        LinearLayout footer=RecordUi.footer(body);
        if(!failure.isEmpty())Design.text(footer,failure,13,500,Design.BROWN);
        if(request!=null)Design.text(footer,switch(operationKind){case "save"->"Saving rule…";case "delete"->"Deleting rule…";case "enable"->"Updating rule…";default->"Queueing the reviewed test…";},13,400,Design.MUTED);
        if(page.equals("home"))Design.button(footer,"Add rule",true,()->newDraft(AutomationRule.Transport.INTENT)).setEnabled(request==null&&!loading);
        else if(page.equals("editor")){Design.text(footer,"Only Save rule applies this draft. Saving does not send a test.",12,400,Design.MUTED);Design.button(footer,"Save rule",true,this::save).setEnabled(request==null);}
        else Design.button(footer,"Done",true,this::back).setEnabled(request==null);
        if(request!=null)RecordUi.enabled(body,false);
        ScrollView captured=currentScroll;int position=positions.getInt(page);captured.post(()->{if(currentScroll==captured)captured.scrollTo(0,position);});
    }
    private void menu(LinearLayout body,String icon,String title,String detail,String value,Runnable action){PlanActivity.detail(body,icon,title,detail,value,action);Design.line(body);}
    private void home(LinearLayout body){
        clearFields();Design.text(body,"Trigger actions from sleep events.",14,400,Design.MUTED);Design.space(body,16);RecordUi.heading(body,"Your rules");
        if(loading)Design.text(body,"Loading rules…",13,400,Design.MUTED);
        if(!loadFailure.isEmpty()){Design.text(body,loadFailure,13,500,Design.BROWN);Design.button(body,"Refresh rules",false,this::refreshRows).setEnabled(request==null);}
        if(!status.isEmpty())Design.text(body,status,13,500,Design.BROWN);
        if(rowsLoaded&&rows.isEmpty())Design.text(body,"No rules yet. Add a destination and choose a sleep event.",14,400,Design.MUTED);
        for(AutomationStore.Row row:rows){
            LinearLayout card=Design.col(this);Design.pad(card,14,8);card.setBackground(Design.shape(this,Design.SOFT,16));
            menu(card,row.transport.equals("INTENT")?"settings":"link",row.name,eventLabel(AutomationEvent.Kind.valueOf(row.event))+" → "+transportLabel(AutomationRule.Transport.valueOf(row.transport)),null,()->load(row));card.getChildAt(0).setEnabled(request==null&&!loading);
            Switch choice=Design.toggle(card,"Enabled",row.status,row.enabled,on->setEnabled(row,on));choice.setContentDescription("Rule enabled: "+row.name);choice.setEnabled(request==null&&!loading);
            body.addView(card);Design.space(body,10);
        }
        Design.space(body,10);RecordUi.heading(body,"Connection destinations");
        destinationRoute(body,AutomationRule.Transport.INTENT,"Tasker / receiving app","Target a receiving app package");
        destinationRoute(body,AutomationRule.Transport.WEBHOOK,"HTTPS webhook","Send a reviewed event to your URL");
        destinationRoute(body,AutomationRule.Transport.MQTT,"MQTT over TLS","Publish to your broker and topic");
        Design.space(body,8);Design.text(body,"New rules are Off. Enable only destinations you trust.",12,400,Design.MUTED);
    }
    private void destinationRoute(LinearLayout body,AutomationRule.Transport type,String title,String detail){menu(body,type==AutomationRule.Transport.INTENT?"settings":"link",title,detail,null,()->{if(!available())return;destinationType=type;open("destination");});}
    private void options(LinearLayout body){
        menu(body,"link","Incoming tracking requests","Separate default-off access; confirmation each time",null,()->{if(available())startActivity(RecordUi.intent(this,IncomingAutomationSettingsActivity.class));});
        menu(body,"info","Delivery & privacy","Shared fields, retries and destination requirements",null,()->open("delivery"));
        menu(body,"reset","Refresh saved rules","Read current rule and delivery status",null,()->{if(!available())return;page="home";history.clear();refreshRows();});
    }
    private void delivery(LinearLayout body){
        Design.text(body,"Destinations act on your events",20,600,Design.INK);Design.space(body,12);
        Design.text(body,"Rules share event type, occurrence time and a hash ID. Sound recognition also shares the estimated sound class. Your destination can trigger its own actions.",14,400,Design.MUTED);Design.space(body,14);
        Design.text(body,"Tasker / receiving app uses a targeted broadcast. Webhooks require HTTPS and reject redirects. MQTT uses verified TLS and non-retained QoS 1 publishing.",14,400,Design.MUTED);Design.space(body,14);
        Design.text(body,"Up to 6 attempts in 24 hours. Android may delay delivery. Receivers must deduplicate event_id. Editing or disabling cancels queued attempts; an in-flight request cannot be recalled.",14,400,Design.MUTED);Design.space(body,14);
        Design.text(body,"Playback events follow player acknowledgements. Recognition remains an estimate and sends at most one event per 30 seconds across all classes; other estimates remain saved locally.",14,400,Design.MUTED);
    }
    private void destinationHelp(LinearLayout body){
        String detail=switch(destinationType){case INTENT->"Choose the receiving app package. In Tasker use Event → System → Intent Received, then the action shown in your rule. The receiving profile must be active.";case WEBHOOK->"Enter your HTTPS destination and optional bearer token. Events use HTTPS POST; redirects are rejected. A saved rule alone does not prove that the destination accepts requests.";case MQTT->"Enter ssl://host:8883, a publish topic and optional username/password. TLS certificates are verified; publishes use non-retained QoS 1. Connection status is not inferred from saving.";};
        Design.text(body,detail,15,400,Design.INK);Design.space(body,18);Design.text(body,"Add rule opens a private draft with Enabled Off. Nothing is sent while you choose or review it.",14,400,Design.MUTED);Design.space(body,18);
        Design.button(body,"Add "+transportLabel(destinationType)+" rule",false,()->newDraft(destinationType));
    }
    private void newDraft(AutomationRule.Transport type){if(!available()||loading)return;remember();navigation++;draft=new AutomationRule(UUID.randomUUID().toString(),"",AutomationEvent.Kind.TRACKING_STARTED,type,false,0,type==AutomationRule.Transport.INTENT?"net.dinglisch.android.taskerm":"","","","");secretRestorationFailed=false;failure="";history.clear();page="editor";clearFields();render();}
    private void editor(LinearLayout body){
        name=field(body,"Rule name",draft.name,false);menu(body,"moon","When",eventLabel(draft.event),null,()->open("event"));menu(body,"link","Destination type",transportLabel(draft.transport),null,()->open("transport"));
        destination=field(body,draft.transport==AutomationRule.Transport.INTENT?"Receiving app package":draft.transport==AutomationRule.Transport.WEBHOOK?"HTTPS URL":"Broker: ssl://host:8883",draft.destination,false);topic=null;username=null;secret=null;
        if(draft.transport==AutomationRule.Transport.MQTT){topic=field(body,"Publish topic",draft.topic,false);username=field(body,"Username (optional)",draft.username,false);}
        if(draft.transport!=AutomationRule.Transport.INTENT){secret=field(body,draft.transport==AutomationRule.Transport.WEBHOOK?"Bearer token (optional)":"Password (optional)",draft.secret,true);secret.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){secretRestorationFailed=false;}public void afterTextChanged(Editable e){}});}
        enabled=Design.toggle(body,"Enabled","New events only; editing cancels queued attempts.",draft.enabled,on->{});
    }
    private EditText field(LinearLayout body,String title,String value,boolean password){Design.space(body,10);Design.text(body,title,13,500,Design.INK);EditText field=new EditText(this);field.setSingleLine(true);field.setSaveEnabled(false);field.setTextSize(15);field.setTypeface(Design.font(this,400));field.setTextColor(Design.INK);field.setBackground(Design.shape(this,Design.SOFT,12));field.setPadding(Design.dp(this,12),Design.dp(this,10),Design.dp(this,12),Design.dp(this,10));field.setMinHeight(Design.dp(this,48));field.setContentDescription(title);field.setInputType(InputType.TYPE_CLASS_TEXT|(password?InputType.TYPE_TEXT_VARIATION_PASSWORD:InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS));field.setText(value);body.addView(field,new LinearLayout.LayoutParams(-1,-2));return field;}
    private void events(LinearLayout body){for(AutomationEvent.Kind kind:AutomationEvent.Kind.values()){String label=eventLabel(kind);choice(body,label,draft.event==kind,()->{draft=new AutomationRule(draft.id,draft.name,kind,draft.transport,draft.enabled,draft.revision,draft.destination,draft.topic,draft.username,draft.secret);render();});}}
    private void transports(LinearLayout body){for(AutomationRule.Transport type:AutomationRule.Transport.values())choice(body,transportLabel(type),draft.transport==type,()->{if(draft.transport!=type){draft=new AutomationRule(draft.id,draft.name,draft.event,type,draft.enabled,draft.revision,type==AutomationRule.Transport.INTENT?"net.dinglisch.android.taskerm":"","","","");secretRestorationFailed=false;clearFields();}render();});Design.space(body,14);Design.text(body,"Changing the type clears this draft's destination-specific fields. Choosing the current type keeps them.",13,400,Design.MUTED);}
    private void choice(LinearLayout body,String label,boolean selected,Runnable action){LinearLayout row=Design.row(this);Design.pad(row,12,10);row.setMinimumHeight(Design.dp(this,56));row.setBackground(Design.touch(this,selected?Design.SOFT:android.graphics.Color.TRANSPARENT,12));row.addView(new Design.Icon(this,selected?"check":"minus",selected?Design.BROWN:Design.MUTED),new LinearLayout.LayoutParams(Design.dp(this,24),Design.dp(this,24)));TextView text=Design.text(this,label,15,selected?600:400,Design.INK);LinearLayout.LayoutParams copy=new LinearLayout.LayoutParams(0,-2,1);copy.leftMargin=Design.dp(this,12);row.addView(text,copy);SleepPicker.radio(row,selected);row.setContentDescription(label);row.setOnClickListener(v->{if(available()){if(!selected)InteractionFeedback.selection(v);action.run();}});body.addView(row);Design.space(body,3);}
    private void ruleOptions(LinearLayout body){
        menu(body,"info","Event details",eventLabel(draft.event),null,()->open("event-details"));
        if(draft.transport==AutomationRule.Transport.INTENT){RecordUi.heading(body,"Receiving app setup");Design.text(body,"Tasker profile: Event → System → Intent Received",13,400,Design.MUTED);TextView action=Design.text(body,"org.sleepwell.app.event."+draft.event.name(),13,500,Design.BROWN);action.setTextIsSelectable(true);Design.text(body,"Extras: event_id, event, occurred_at, test, schema; sound_class on actual recognition events. The receiving profile must be active.",12,400,Design.MUTED);}
        menu(body,"link","Delivery & privacy","Retries, shared fields and transport limits",null,()->open("delivery"));
        if(draft.revision>0){Design.button(body,"Send saved rule’s test event",false,()->confirm("test",draft.id,draft.revision));Design.text(body,"Requires confirmation. Uses the saved destination, not this draft.",12,400,Design.MUTED);Design.space(body,12);Design.button(body,"Delete rule",false,()->confirm("delete",draft.id,draft.revision));}
        else Design.text(body,"Save this rule before reviewing a test event or deleting it.",13,400,Design.MUTED);
    }
    private void refreshRows(){
        if(request!=null||isFinishing()||isDestroyed())return;long ticket=++readGeneration;loading=true;loadFailure="";render();Port pinned=owner;Context app=getApplicationContext();
        Automations.io.execute(()->{List<AutomationStore.Row> found=List.of();String issue="",delivery="";boolean success=false;try(AutomationStore store=pinned.open(app)){found=store.rows();delivery=pinned.status(app);success=true;}catch(Exception unavailable){issue="Rules could not be loaded. Saved rules have not been removed.";}List<AutomationStore.Row> result=found;String error=issue,deliveryStatus=delivery;boolean loaded=success;handler.post(()->{if(isDestroyed()||isFinishing()||ticket!=readGeneration)return;loading=false;loadFailure=error;if(loaded){rows=result;rowsLoaded=true;status=deliveryStatus;}if(page.equals("home"))render();});});
    }
    private void load(AutomationStore.Row row){
        if(!available()||loading)return;long ticket=++navigation;loading=true;render();Port pinned=owner;Context app=getApplicationContext();
        Automations.io.execute(()->{AutomationRule found=null;try(AutomationStore store=pinned.open(app)){found=store.rule(row.id);}catch(Exception ignored){}AutomationRule result=found;handler.post(()->{if(isDestroyed()||isFinishing()||ticket!=navigation||!page.equals("home"))return;loading=false;if(result==null){render();confirm("unavailable",row.id,row.revision);return;}draft=result;secretRestorationFailed=false;failure="";history.clear();page="editor";clearFields();render();});});
    }
    private void save(){
        if(!available())return;fields();if(secretRestorationFailed){failure="Re-enter or explicitly clear the credential before saving this draft.";render();return;}
        try{draft.validate();}catch(Exception invalid){failure=invalid.getMessage();render();return;}
        AutomationRule copy=draft;Port pinned=owner;begin("save",app->{try(AutomationStore store=pinned.open(app)){store.save(copy);return scheduled(pinned,app,store,"Rule saved.");}});
    }
    private void setEnabled(AutomationStore.Row row,boolean value){if(!available()||loading)return;Port pinned=owner;begin("enable",app->{try(AutomationStore store=pinned.open(app)){store.enabled(row.id,row.revision,value);return scheduled(pinned,app,store,"Rule updated.");}});}
    private static String scheduled(Port owner,Context app,AutomationStore store,String committed){try{owner.schedule(app,store);return committed;}catch(Exception unavailable){return committed+" Delivery scheduling needs attention. Reopen Automations to retry scheduling.";}}
    private void begin(String kind,PlanOperations.Task task){if(request!=null||isFinishing()||isDestroyed())return;request=UUID.randomUUID().toString();operationKind=kind;failure="";PlanOperations.start(getApplicationContext(),request,task);render();observe();}
    private void observe(){
        if(!resumed||request==null||isFinishing()||isDestroyed())return;String token=request;
        if(!PlanOperations.tokens().contains(token)){request=null;operationKind="";failure="The previous operation finished or was interrupted. Review saved rules before trying again.";render();if(page.equals("home"))refreshRows();return;}
        PlanOperations.Operation operation=PlanOperations.start(getApplicationContext(),token,app->{throw new IllegalStateException("Operation result unavailable. Review saved rules.");});
        if(!operation.done){handler.removeCallbacks(poll);handler.postDelayed(poll,100);return;}
        PlanOperations.forget(token);request=null;String kind=operationKind;operationKind="";
        if(operation.error!=null){failure=operation.error;render();return;}
        failure=operation.notice==null?"":operation.notice;
        if(kind.equals("save")||kind.equals("delete")||kind.equals("enable")){draft=null;clearFields();history.clear();page="home";navigation++;refreshRows();}else render();
    }
    private final Runnable poll=this::observe;
    private void confirm(String kind,String id,long revision){if(!available())return;fields();confirmation=kind;confirmationId=id;confirmationRevision=revision;showConfirmation();}
    private void showConfirmation(){
        if(confirmation.isEmpty()||ownedDialog!=null||!available())return;
        String kind=confirmation,id=confirmationId;long revision=confirmationRevision;String title=kind.equals("test")?"Send saved rule’s test event?":kind.equals("unavailable")?"Destination unavailable":"Delete rule?";
        String message=kind.equals("test")?"This sends a real event to the saved destination and can trigger its actions. Unsaved changes are not used.":kind.equals("unavailable")?"The saved destination could not be opened. Your sleep records are unaffected. Keep this rule or delete it to enter the destination again.":"Queued attempts will be removed. An in-flight request may already have been sent.";
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle(title).setMessage(message).setNegativeButton(kind.equals("unavailable")?"Keep rule":"Cancel",null).setPositiveButton(kind.equals("test")?"Send test":"Delete rule",null).create();ownedDialog=dialog;
        dialog.setOnDismissListener(ignored->{if(ownedDialog==dialog){ownedDialog=null;confirmation="";confirmationId="";}});dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            if(ownedDialog!=dialog||!resumed||isFinishing()||isDestroyed()||request!=null||!dialog.getWindow().getDecorView().hasWindowFocus()||!confirmation.equals(kind)||!confirmationId.equals(id)||confirmationRevision!=revision)return;
            if(!kind.equals("unavailable")&&(draft==null||!draft.id.equals(id)||draft.revision!=revision))return;
            Port pinned=owner;testOccurrence=kind.equals("test")?UUID.randomUUID().toString():"";String occurrence=testOccurrence;
            dialog.dismiss();
            if(kind.equals("test"))begin("test",app->{try(AutomationStore store=pinned.open(app)){AutomationRule saved=store.rule(id);if(saved==null||!saved.enabled)return "Enable and save this rule before sending a test.";if(saved.revision!=revision)throw new IllegalStateException("Rule changed. Reopen it before sending a test.");store.enqueue(AutomationEvent.occurrence(saved.event,occurrence,System.currentTimeMillis(),true),id);return scheduled(pinned,app,store,"Test event queued.");}});
            else begin("delete",app->{try(AutomationStore store=pinned.open(app)){store.delete(id);return scheduled(pinned,app,store,"Rule deleted.");}});
        });
    }
    @Override protected void onResume(){super.onResume();resumed=true;observe();if(page.equals("home")&&request==null)refreshRows();showConfirmation();}
    @Override protected void onPause(){resumed=false;handler.removeCallbacks(poll);super.onPause();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused){RecordUi.lightBars(this);showConfirmation();}}
    @Override public void onSaveInstanceState(Bundle state){
        fields();remember();super.onSaveInstanceState(state);state.putString("page",page);state.putBundle("positions",positions);state.putStringArrayList("history",new ArrayList<>(history));state.putString("failure",failure);state.putString("request",request);state.putString("operationKind",operationKind);state.putString("destinationType",destinationType.name());state.putString("confirmation",confirmation);state.putString("confirmationId",confirmationId);state.putLong("confirmationRevision",confirmationRevision);state.putString("testOccurrence",testOccurrence);
        if(draft!=null){state.putString("id",draft.id);state.putString("name",draft.name);state.putString("event",draft.event.name());state.putString("transport",draft.transport.name());state.putBoolean("enabled",draft.enabled);state.putLong("revision",draft.revision);state.putString("destination",draft.destination);state.putString("topic",draft.topic);state.putString("username",draft.username);state.putBoolean("secretRestorationFailed",secretRestorationFailed);try{if(!draft.secret.isEmpty())state.putString("secret",owner.vault().seal(draft.secret));}catch(Exception unavailable){state.putBoolean("secretRestorationFailed",true);}}
    }
    @Override protected void onDestroy(){readGeneration++;navigation++;handler.removeCallbacksAndMessages(null);if(ownedDialog!=null){ownedDialog.setOnDismissListener(null);ownedDialog.dismiss();ownedDialog=null;}super.onDestroy();}
    static String eventLabel(AutomationEvent.Kind kind){return switch(kind){case TRACKING_STARTED->"When tracking starts";case TRACKING_STOPPED->"When tracking ends";case ALARM_DISMISSED->"When alarm dismissed";case TRACKING_PAUSED->"When recording capture is paused";case TRACKING_RESUMED->"When recording capture resumes";case ALARM_STARTED->"When an alarm session starts";case ALARM_SNOOZED->"When snooze is accepted and alarm stops";case WAKE_CHECK_CONFIRMED->"When wake-up check is confirmed";case WAKE_CHECK_MISSED->"When a delivered wake-up check expires";case WIND_DOWN_REMINDER_POSTED->"When a wind-down notification is posted";case SLEEP_SOUND_STARTED->"When sleep sounds start playing";case SLEEP_SOUND_STOPPED->"When sleep sounds stop playing";case ANTI_SNORE_SOUND_STARTED->"When an anti-snore sound is accepted";case SOUND_RECOGNIZED->"When a sound estimate is saved";};}
    static String eventHelp(AutomationEvent.Kind kind){return switch(kind){case TRACKING_STARTED->"A saved recording entered capture; waiting or arming alone does not emit this event.";case TRACKING_STOPPED->"The recording owner ended capture; this does not certify a complete or uninterrupted night.";case TRACKING_PAUSED,TRACKING_RESUMED->"Emitted after the recording’s pause/resume transition is saved, including contactless capture. The same saved transition keeps the same event ID.";case ALARM_STARTED->"An owned real alarm entered its foreground session. Silent alarms qualify; audible output is not proven.";case ALARM_SNOOZED->"Android accepted the snooze deadline, its receipt was saved, and that owned alarm was stopped. Failed or unconfirmed snoozes do not emit it.";case ALARM_DISMISSED->"The wake routine completed and its owned alarm was dismissed.";case WAKE_CHECK_CONFIRMED->"An owned wake-up check was explicitly confirmed and its confirmed state saved.";case WAKE_CHECK_MISSED->"A delivered check’s confirmation window expired and the missed state was saved. This does not promise that restarting the alarm succeeded.";case WIND_DOWN_REMINDER_POSTED->"The saved fixed or smart reminder was submitted to Android notifications. Repeats are separate events; seeing or hearing the notification is not proven.";case SLEEP_SOUND_STARTED->"A real sleep-sound player acknowledged playing. Each playback or resume emits once; loading, selection and previews do not. Muted media can qualify; hearing it is not proven.";case SLEEP_SOUND_STOPPED->"All acknowledged players in that playback segment paused or released after pause, end or failure. Unconfirmed cleanup or process death may omit this event.";case ANTI_SNORE_SOUND_STARTED->"A production cue’s tone request was accepted, or its first headphone pulse drained on a verified route. Vibration-only and previews do not qualify. Hearing the cue is not proven.";case SOUND_RECOGNIZED->"After an actual tracking sound estimate is saved: snoring, speech, laughter, baby cry or cough/throat clearing. Includes original class and timestamp. At most one event per 30 seconds globally; other estimates remain saved but are not sent. No clinical accuracy claim.";};}
    static String transportLabel(AutomationRule.Transport kind){return switch(kind){case INTENT->"Tasker / app";case WEBHOOK->"Webhook";case MQTT->"MQTT";};}
}
