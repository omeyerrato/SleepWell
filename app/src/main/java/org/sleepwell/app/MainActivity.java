package org.sleepwell.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.media.*;
import android.net.Uri;
import android.nfc.*;
import android.os.*;
import android.provider.Settings;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

public class MainActivity extends Activity {
    private final Set<Integer> collapsedAlarms=new HashSet<>();
    private String settingsGroup="";private final Map<String,Integer> settingsScroll=new HashMap<>();private final Deque<String> settingsBack=new ArrayDeque<>();private boolean settingsRoute;
    private Store store;private int tab=0;private Alarm pendingTone;private MediaPlayer preview;private NfcEnrollment nfcEnrollment;
    private static final int NOTIFY=21,TONE=22,EXPORT_JSON=23,EXPORT_CSV=24,IMPORT=25;
    private final Handler appearanceClock=new Handler(Looper.getMainLooper());
    private NightArc nightArc;private boolean clockReceiverRegistered,restoreRestWindow;
    private final BroadcastReceiver clockChanged=new BroadcastReceiver(){public void onReceive(Context c,Intent intent){if(tab==0||tab==1||tab==3)renderKeepingScroll();}};
    private final Runnable clockUpdate=new Runnable(){public void run(){if(tab==3)updateWindDown();refreshAlarmListClock();refreshTonightClock();appearanceClock.postDelayed(this,30000);}};
    private int renderedPhase=-1;private TextView winddownValue,winddownCaption,windGuardState;private Dialog windOptions;private Bundle restoreWindOptions;private RoomDimming windDimming;private boolean windResumed;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);store=Records.open(this);Notifications.channels(this);if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::back);
        if(getIntent().getBooleanExtra("visualReview",false))getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(state!=null){tab=state.getInt("tab");restoreWindOptions=state.getBundle("windOptions");restoreRestWindow=tab==0&&state.getBoolean("restWindowExplanation");int[] collapsed=state.getIntArray("collapsedAlarms");if(collapsed!=null)for(int id:collapsed)collapsedAlarms.add(id);}
        if(state!=null){settingsGroup=validSettingsGroup(state.getString("settingsGroup",""));settingsRoute=state.getBoolean("settingsRoute");ArrayList<String> history=state.getStringArrayList("settingsBack");if(history!=null)settingsBack.addAll(history);Bundle positions=state.getBundle("settingsScroll");if(positions!=null)for(String key:positions.keySet())settingsScroll.put(key,Math.max(0,positions.getInt(key)));}
        if(state==null){if("reliability".equals(getIntent().getStringExtra("homeSurfacePage")))tab=5;if(getIntent().getBooleanExtra("showReport",false))tab=2;if(getIntent().hasExtra("destination")){int destination=getIntent().getIntExtra("destination",0);tab=destination==3?4:Math.max(0,Math.min(2,destination));}}
        if(state==null)applySettingsRoute();
        if(!TrackingService.running && Prefs.flag(this,"tracking")) {
            store.recoverInterrupted();Prefs.put(this,"tracking",false);Prefs.put(this,"trackingPaused",false);Prefs.put(this,"session",0L);
        }
    }
    @Override protected void onResume() {super.onResume();windResumed=true;HomeSurfaces.changed(this);HomeShortcuts.publish(this);recoverNightlySchedule();if(!TrackingService.running)try{store.recoverInterrupted();}catch(Exception e){Prefs.put(this,"trackingError","An interrupted record could not be finalized. Free storage and reopen the app.");}render();appearanceClock.removeCallbacks(clockUpdate);appearanceClock.postDelayed(clockUpdate,30000);if(!clockReceiverRegistered){IntentFilter filter=new IntentFilter(Intent.ACTION_TIME_CHANGED);filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);androidx.core.content.ContextCompat.registerReceiver(this,clockChanged,filter,androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED);clockReceiverRegistered=true;}}
    private void recoverNightlySchedule(){try{RecurringTracking.Port owner=RecurringTracking.port;if(owner.preferences(this).getBoolean("enabled",false))RecurringTracking.recover(this,owner,false);}catch(RuntimeException failure){android.util.Log.e("SleepWell","Nightly schedule could not be reconciled on app reopening",failure);}}
    @Override protected void onPause(){windResumed=false;if(windDimming!=null)windDimming.restore();if(nfcEnrollment!=null)nfcEnrollment.close();rememberSettingsScroll();appearanceClock.removeCallbacks(clockUpdate);if(clockReceiverRegistered){unregisterReceiver(clockChanged);clockReceiverRegistered=false;}super.onPause();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused){Runnable apply=()->{if(!isDestroyed()&&hasWindowFocus())Design.systemBars(this,tab==3||tab==0&&Appearance.palette(this).dark);};getWindow().getDecorView().post(apply);getWindow().getDecorView().postDelayed(apply,200);if(tab==3&&restoreWindOptions!=null){Bundle saved=restoreWindOptions;restoreWindOptions=null;windDownOptions(saved);}}syncWindDimming();}
    @Override public void onSaveInstanceState(Bundle b) {super.onSaveInstanceState(b);b.putBoolean("restWindowExplanation",tab==0&&(restoreRestWindow||nightArc!=null&&nightArc.explanationShowing()));rememberSettingsScroll();b.putBoolean("settingsRoute",settingsRoute);b.putStringArrayList("settingsBack",new ArrayList<>(settingsBack));b.putString("settingsGroup",settingsGroup);Bundle positions=new Bundle();for(Map.Entry<String,Integer> entry:settingsScroll.entrySet())positions.putInt(entry.getKey(),entry.getValue());b.putBundle("settingsScroll",positions);if(tab==3&&windOptions!=null&&windOptions.isShowing())b.putBundle("windOptions",windOptions.onSaveInstanceState());b.putInt("tab",tab);b.putIntArray("collapsedAlarms",collapsedAlarms.stream().mapToInt(Integer::intValue).toArray());}
    private void back(){if(!settingsBack.isEmpty()){rememberSettingsScroll();String[] prior=settingsBack.pop().split(":",2);tab=Integer.parseInt(prior[0]);settingsGroup=prior.length>1?validSettingsGroup(prior[1]):"";render();return;}if(settingsRoute){finish();return;}if(tab==4&&!settingsGroup.isEmpty()){settingsGroup="";render();return;}if(tab==0){finish();return;}tab=tab==11?1:tab==3?0:tab==5||tab==6||tab==9?4:0;render();}
    @android.annotation.SuppressLint("GestureBackNavigation") @Override public void onBackPressed(){back();}
    private void navigate(int destination){rememberSettingsScroll();settingsBack.clear();if(destination==3)settingsGroup="";if(destination==2){startActivity(RecordUi.intent(this,HistoryActivity.class));return;}tab=destination==3?4:Math.max(0,Math.min(2,destination));render();}
    private void render() {
        nightArc=null;if(tab!=3&&windOptions!=null)windOptions.dismiss();
        renderedPhase=Appearance.palette(this).phase;
        boolean root=tab==0||tab==1||tab==2||tab==4||tab==7;
        LinearLayout body=tab==3?Design.darkPage(this):Design.page(this,tab==0,root?(tab==2||tab==7?2:tab==4?3:tab==1?1:0):null,this::navigate);
        switch(tab){case 1:alarmList(body);break;case 2:report(body);break;case 3:bedtime(body);break;case 4:settingsHome(body);break;case 5:setup(body);break;case 9:soundAnalysis(body);break;case 11:alarmOptions(body);break;case 12:recordingOptions(body);break;case 6:privacy(body);break;case 7:history(body);break;default:tonight(body);}
        if(tab==4){ScrollView current=scroll(getWindow().getDecorView());if(current!=null){int position=settingsScroll.getOrDefault(settingsGroup,0);current.post(()->current.scrollTo(0,position));}}
        if(restoreRestWindow){restoreRestWindow=false;NightArc owner=nightArc;if(tab==0&&owner!=null)owner.post(()->{if(!isDestroyed()&&!isFinishing()&&tab==0&&nightArc==owner)owner.restoreExplanation();});}
        syncWindDimming();
    }
    private void syncWindDimming(){boolean active=tab==3&&windResumed&&hasWindowFocus();if(active&&windDimming==null)windDimming=new RoomDimming(this);if(windDimming!=null)windDimming.apply(active);}
    private ScrollView scroll(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup)for(int n=0;n<((ViewGroup)v).getChildCount();n++){ScrollView found=scroll(((ViewGroup)v).getChildAt(n));if(found!=null)return found;}return null;}
    private void renderKeepingScroll(){ScrollView old=scroll(getWindow().getDecorView());int y=old==null?0:old.getScrollY();boolean arcFocused=nightArc!=null&&nightArc.hasFocus();render();ScrollView current=scroll(getWindow().getDecorView());if(current!=null)current.post(()->{current.scrollTo(0,y);if(arcFocused&&nightArc!=null)nightArc.requestFocus();});}
    void refreshAlarmListClock(){if(tab==1&&hasWindowFocus())renderKeepingScroll();}
    void refreshTonightClock(){if(tab!=0)return;long now=TonightScreen.clock.getAsLong();java.time.ZoneId zone=java.time.ZoneId.systemDefault();if((nightArc!=null&&nightArc.needsNewOccurrence(now,zone))||(Appearance.automatic(this)&&Appearance.palette(this).phase!=renderedPhase))renderKeepingScroll();else if(nightArc!=null)nightArc.update(now,zone);}
    private static String validSettingsGroup(String group){return group!=null&&Set.of("alarm","sleep","sound","appearance","connections").contains(group)?group:"";}
    private void applySettingsRoute(){
        String page=getIntent().getStringExtra("settingsPage");settingsRoute=page!=null||getIntent().hasExtra("settingsGroup");if(page!=null)tab=switch(page){case "alarms"->1;case "sound_analysis"->9;case "privacy"->6;case "reliability"->5;case "wind_down"->3;case "alarm_options"->11;default->4;};
        String group=validSettingsGroup(getIntent().getStringExtra("settingsGroup"));if(!group.isEmpty()){settingsGroup=group;tab=4;}
    }
    private void rememberSettingsScroll(){if(tab==4){ScrollView current=scroll(getWindow().getDecorView());if(current!=null)settingsScroll.put(settingsGroup,current.getScrollY());}}
    private void openSettingsGroup(String group){rememberSettingsScroll();settingsBack.push(tab+":"+settingsGroup);settingsGroup=validSettingsGroup(group);tab=4;render();}
    private void openSettingsPage(int page){rememberSettingsScroll();settingsBack.push(tab+":"+settingsGroup);tab=page;render();}
    private void settingsSearch(){startActivity(RecordUi.intent(this,SearchActivity.class).putExtra("searchMode","settings"));}
    private void settingsHome(LinearLayout body){
        if(!settingsGroup.isEmpty()){settingsSection(body);return;}
        Design.header(body,"Settings",null);
        RecordUi.headerAction((LinearLayout)body.getChildAt(0),"search","Search settings",this::settingsSearch);
        settingsRow(body,"bell","Alarms","Alarm time, sound and wake-up",()->openSettingsGroup("alarm"));
        settingsRow(body,"moon","Sleep tracking","Sensors, bedtime and sleep goals",()->openSettingsGroup("sleep"));
        settingsRow(body,"wave","Sound analysis","Snoring and sleep recordings",()->openSettingsPage(9));
        settingsRow(body,"shield","Bedtime guard","Reduce distractions at night",()->startActivity(RecordUi.intent(this,GuardActivity.class)));
        settingsRow(body,"palette","Appearance","Theme, text and touch feedback",()->openSettingsGroup("appearance"));
        settingsRow(body,"settings","Reliability","Battery, permissions and alarm readiness",()->openSettingsPage(5));
        settingsRow(body,"plug","Integrations","Health Connect, lights and automations",()->openSettingsGroup("connections"));
        settingsRow(body,"lock","Data & privacy","Recordings, storage and backup",()->openSettingsPage(6));
        Design.space(body,12);LinearLayout source=PlanActivity.detail(body,"github","Free and open source","View source on GitHub",null,()->open(new Intent(Intent.ACTION_VIEW,Uri.parse("https://github.com/omeyerrato/SleepWell"))));source.setMinimumHeight(Ui.dp(this,56));source.removeViewAt(source.getChildCount()-1);source.addView(new Design.Icon(this,"external",Design.INK),new LinearLayout.LayoutParams(Ui.dp(this,18),Ui.dp(this,18)));
    }
    private void settingsSection(LinearLayout body){
        String title=switch(settingsGroup){case "alarm"->"Alarms";case "sleep"->"Sleep tracking";case "sound"->"Sound & recordings";case "appearance"->"Appearance";default->"Integrations";};
        RecordUi.header(body,title,this::back);
        switch(settingsGroup){
            case "alarm":
                settingsHeading(body,"Your alarms");
                settingsRow(body,"bell","Manage alarms","Choose an alarm to edit its settings",()->openSettingsPage(1));
                settingsRow(body,"settings","Alarm defaults","Starting settings for new alarms",()->startActivity(RecordUi.intent(this,AlarmDefaultsActivity.class)));
                settingsRow(body,"shield","Alarm protection","Prevent leaving and power-menu handling",()->startActivity(RecordUi.intent(this,AlarmProtectionActivity.class)));
                settingsHeading(body,"Schedules & challenges");
                settingsRow(body,"calendar","Calendar rules","Skipped dates and meeting-aware alarms",()->startActivity(RecordUi.intent(this,CalendarRulesActivity.class)));
                settingsRow(body,"clock","Naps & timers","Short rests, Power nap and schedules",()->startActivity(RecordUi.intent(this,NapActivity.class)));
                settingsRow(body,"phone","Bedside NFC tag","Enroll a tag for wake-up challenges",()->openSettingsPage(11));
                settingsHeading(body,"Smart wake-up default");
                Design.toggle(body,"Wait for duration goal","Early waking waits for your duration goal. The fixed alarm still rings on time.",Prefs.flag(this,"smartNeedsGoal"),on->Prefs.put(this,"smartNeedsGoal",on));break;
            case "sleep":
                settingsHeading(body,"Tracking");
                settingsRow(body,"moon","Sleep tracking","Sensor setup and current recording",this::tracking);
                settingsRow(body,"clock","Automatic tracking","When tracking starts each night",()->startActivity(RecordUi.intent(this,AutomaticTrackingActivity.class)));
                settingsRow(body,"sun","Room conditions","Light, sound and screen dimming",()->startActivity(RecordUi.intent(this,RoomConditionsActivity.class)));
                settingsRow(body,"sun","Awake estimates","Phone-use, light and snooze clues",()->startActivity(RecordUi.intent(this,AwakeSettingsActivity.class)));
                settingsHeading(body,"Bedtime");
                settingsRow(body,"bed","Sleep plan","Bedtime, sleep goal and reminders",()->startActivity(RecordUi.intent(this,PlanActivity.class)));
                settingsRow(body,"shield","Bedtime guard","Choose distracting apps to block",()->startActivity(RecordUi.intent(this,GuardActivity.class)));
                settingsHeading(body,"Rhythm & diary");
                settingsRow(body,"report","Sleep rhythm","Recorded timing and consistency",()->startActivity(RecordUi.intent(this,SleepRhythmActivity.class)));
                settingsRow(body,"moon","Dream diary","Private notes and dreams",()->startActivity(RecordUi.intent(this,DiaryActivity.class)));
                settingsRow(body,"sun","Travel mode","Time zones and daylight reminders",()->startActivity(RecordUi.intent(this,TravelActivity.class)));break;
            case "sound":
                settingsHeading(body,"Analysis & recordings");
                settingsRow(body,"wave","Sound analysis","Choose recorded sound categories",()->openSettingsPage(9));
                settingsRow(body,"mic","Recording threshold","Choose which sounds save a clip",()->openSettingsPage(12));
                settingsRow(body,"wave","Snoring cues","Optional sound and vibration responses",()->startActivity(RecordUi.intent(this,SnoringCuesActivity.class)));
                settingsRow(body,"file","Recordings & retention","Storage limits and saved recordings",()->startActivity(RecordUi.intent(this,SoundLibrarySettingsActivity.class)));
                settingsRow(body,"heart","Favorite sounds","Review starred recordings",()->startActivity(RecordUi.intent(this,FavoriteSoundsActivity.class)));
                settingsHeading(body,"Bedtime audio");
                settingsRow(body,"music","Sleep sounds","Mixer, playlists and bedtime audio",()->startActivity(RecordUi.intent(this,SleepSoundsActivity.class)));
                settingsHeading(body,"Experimental");
                settingsRow(body,"wave","Laughter & instruments","Analyze a saved recording",()->startActivity(RecordUi.intent(this,RecordedRecognitionActivity.class)));break;
            case "appearance":
                settingsRow(body,"palette","Theme & text","Changing light, text size and contrast",()->{startActivity(RecordUi.intent(this,AppearanceActivity.class));if(Appearance.reduced(this))overridePendingTransition(0,0);});
                settingsRow(body,"phone","Touch feedback","Subtle acknowledgements for actions",()->startActivity(RecordUi.intent(this,FeedbackSettingsActivity.class)));
                settingsHeading(body,"Home screen");
                settingsRow(body,"home","Tonight layout","Order, visibility and illustration",()->startActivity(RecordUi.intent(this,TonightLayoutActivity.class)));
                settingsRow(body,"home","Widgets & shortcuts","Add SleepWell to your home screen",()->startActivity(RecordUi.intent(this,HomeWidgetSettingsActivity.class)));break;
            default:
                settingsHeading(body,"Health & routines");
                settingsRow(body,"plug","Health Connect","Import and export sleep records",()->startActivity(RecordUi.intent(this,HealthConnectActivity.class)));
                settingsRow(body,"plug","Automations","Tasker, webhooks and MQTT",()->startActivity(RecordUi.intent(this,AutomationActivity.class)));
                settingsHeading(body,"Bedroom devices");
                settingsRow(body,"sun","Bedroom light","Paired lights and sunrise controls",()->startActivity(RecordUi.intent(this,SmartLightActivity.class)));
                settingsRow(body,"phone","Paired movement","Connect a second phone",()->startActivity(RecordUi.intent(this,PairMotionActivity.class)));
                settingsRow(body,"home","External PIR input","Optional Home Assistant sensor",()->startActivity(RecordUi.intent(this,PirSettingsActivity.class)));
                settingsHeading(body,"App access");
                settingsRow(body,"moon","Tracking requests","Review requests from other apps",()->startActivity(RecordUi.intent(this,IncomingAutomationSettingsActivity.class)));
                settingsRow(body,"lock","Saved record access","Apps allowed to read sleep summaries",()->startActivity(RecordUi.intent(this,RecordAccessActivity.class)));
                settingsRow(body,"alarm","Alarm access","Apps allowed to manage ordinary alarms",()->startActivity(RecordUi.intent(this,AlarmAccessActivity.class)));break;
        }
    }
    private void settingsHeading(LinearLayout body,String title){Design.space(body,16);Design.text(body,title,14,600,Design.MUTED);Design.space(body,6);}
    private void settingsRow(LinearLayout body,String icon,String title,String subtitle,Runnable action){
        LinearLayout row=Design.row(this);row.setMinimumHeight(Ui.dp(this,70));Design.Icon image=new Design.Icon(this,icon,Design.BROWN);image.setPadding(Ui.dp(this,8),Ui.dp(this,8),Ui.dp(this,8),Ui.dp(this,8));image.setBackground(Design.shape(this,Design.SOFT,15));LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48));ip.rightMargin=Ui.dp(this,18);row.addView(image,ip);
        LinearLayout labels=Design.col(this);Design.text(labels,title,17,600,Design.INK);Design.space(labels,5);Design.text(labels,subtitle,13,400,Design.MUTED);row.addView(labels,new LinearLayout.LayoutParams(0,-2,1));row.addView(new Design.Icon(this,"chevron",Design.INK),new LinearLayout.LayoutParams(Ui.dp(this,20),Ui.dp(this,20)));row.setContentDescription(title+". "+subtitle);row.setBackground(Design.touch(this,android.graphics.Color.TRANSPARENT,8));row.setOnClickListener(v->{InteractionFeedback.action(v);action.run();});body.addView(row);Design.line(body);
    }
    private void report(LinearLayout body){String failure=Prefs.text(this,"trackingError","");if(!failure.isEmpty()){Design.text(body,"Recording needs attention: "+failure,13,500,Design.BROWN);Design.space(body,12);}ReportScreen.render(this,body,store,new ReportScreen.Actions(){public void history(){startActivity(RecordUi.intent(MainActivity.this,HistoryActivity.class));}public void tonight(){tab=0;render();}public void detail(long id){showNight(id);}});}
    private void privacy(LinearLayout body){
        PrivacyScreen.render(this,body,new PrivacyScreen.Actions(){
            public void back(){MainActivity.this.back();}
            public void retention(){startActivity(RecordUi.intent(MainActivity.this,SoundLibrarySettingsActivity.class));}
            public void backup(){startActivity(RecordUi.intent(MainActivity.this,BackupActivity.class));}
            public void exportBackup(){export(EXPORT_JSON);}
            public void importBackup(){MainActivity.this.backup(BackupJob.Kind.IMPORT);}
            public void csv(){export(EXPORT_CSV);}
            public void cloud(){startActivity(RecordUi.intent(MainActivity.this,CloudBackupActivity.class));}
            public void exportAudio(){startActivity(RecordUi.intent(MainActivity.this,LibraryAudioExportActivity.class));}
            public void restoreAudio(){startActivity(RecordUi.intent(MainActivity.this,LibraryAudioImportActivity.class));}
            public void research(){startActivity(RecordUi.intent(MainActivity.this,ResearchExportActivity.class));}
            public void connections(){openSettingsGroup("connections");}
            public void stopPreview(){MainActivity.this.stopPreview();}
            public void refresh(){render();}
        });
    }
    private void hero(LinearLayout body,String title,String subtitle){body.addView(Design.iconButton(this,"back","Back",this::back),new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));Design.space(body,14);Design.text(body,title,30,700,Design.INK);Design.space(body,8);Design.text(body,subtitle,15,400,Design.MUTED);}
    private void tonight(LinearLayout body) {
        nightArc=TonightScreen.render(this,body,store,new TonightScreen.Actions(){
            public void alarms(){tab=1;render();}
            public void bedtime(){startActivity(RecordUi.intent(MainActivity.this,PlanActivity.class));}
            public void windDown(){tab=3;render();}
            public void settings(){tab=4;render();}
            public void start(){tracking();}
            public void activeAlarm(){startActivity(new Intent(MainActivity.this,AlarmActivity.class));}
            public void confirmCheck(){WakeChecks.review(MainActivity.this);}
            public void edit(Alarm a){editAlarm(a);}
            public void update(Alarm a){if(a.nap!=null||a.napInvalid){if(a.enabled)editAlarm(a);else try{Naps.cancel(MainActivity.this,a);}catch(RuntimeException failure){Ui.toast(MainActivity.this,failure.getMessage());}render();return;}try{Alarm expected=store.alarm(a.id);if(expected==null)throw new IllegalStateException("This alarm was deleted.");AlarmSkips.toggle(MainActivity.this,store,expected,a.enabled);}catch(RuntimeException e){Ui.toast(MainActivity.this,e.getMessage());}render();}
            @Override public void update(Alarm changed,Alarm original){if(changed.nap!=null||changed.napInvalid){update(changed);return;}try{AlarmSkips.edit(MainActivity.this,store,changed,original);}catch(RuntimeException failure){Ui.toast(MainActivity.this,failure.getMessage());}render();}
        });
    }
    private void tracking(){startActivityForResult(new Intent(this,TrackingActivity.class).putExtra("visualReview",getIntent().getBooleanExtra("visualReview",false)),30);}
    private void alarmList(LinearLayout body){
        LinearLayout head=RecordUi.header(body,"Alarms",null);RecordUi.menuAction(head,"Alarm options",()->{tab=11;render();},false);
        View add=Design.iconButton(this,"plus","Add alarm",()->editAlarm(newAlarm()));
        add.setBackground(Design.touch(this,Design.SOFT,28));head.addView(add,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));Design.space(body,16);
        List<Alarm> alarms=store.alarms();
        if(alarms.isEmpty()){
            Design.space(body,28);Design.text(body,"A morning worth waking up for",25,600,Design.INK);Design.space(body,12);
            Design.text(body,"Choose a time, a sound and a challenge for your first alarm.",15,400,Design.MUTED);Design.space(body,32);
        }
        for(Alarm a:alarms){
            LinearLayout top=Design.row(this);TextView time=Design.text(this,Naps.display(this,a),58,700,a.enabled?Design.INK:0xff8a7e78);
            time.setText(Design.timeText(Naps.display(this,a)));time.setMinimumHeight(Ui.dp(this,68));time.setGravity(Gravity.CENTER_VERTICAL);time.setContentDescription("Edit "+a.label);time.setStateDescription(Naps.display(this,a));AlarmClockUi.fit(time,58,2);time.setOnClickListener(v->{InteractionFeedback.action(v);editAlarm(a);});top.addView(time,new LinearLayout.LayoutParams(0,-2,1));
            InteractionFeedback.Toggle enabled=new InteractionFeedback.Toggle(this);enabled.setContentDescription("Enable "+a.label);Design.styleSwitch(this,enabled,Design.BROWN,0xffbcb5b0);enabled.setChecked(a.enabled&&!a.napInvalid);enabled.setEnabled(!a.napInvalid);top.addView(enabled);body.addView(top);
            enabled.setOnCheckedChangeListener((v,on)->{enabled.userChanged();if(a.nap!=null||a.napInvalid){if(on)editAlarm(a);else try{Naps.cancel(this,a);}catch(RuntimeException failure){Ui.toast(this,failure.getMessage());}render();return;}if(on&&!Scheduler.allowed(this)){Ui.toast(this,"Allow exact alarms in Reliability first.");render();return;}try{AlarmSkips.toggle(this,store,a,on);}catch(RuntimeException e){Ui.toast(this,e.getMessage());}render();});
            LinearLayout identity=Design.row(this),copy=Design.col(this);TextView name=Design.text(copy,a.label,17,600,Design.INK);name.setMaxLines(1);name.setEllipsize(android.text.TextUtils.TruncateAt.END);Design.space(copy,4);String schedule=days(a)+(a.nextOverride!=null||a.nextOverrideInvalid?" · Next: "+NextAlarmOverrides.summary(this,a):"");TextView details=Design.text(copy,schedule,13,400,Design.MUTED);details.setMaxLines(2);details.setEllipsize(android.text.TextUtils.TruncateAt.END);copy.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);identity.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
            View edit=new Design.Icon(this,"chevron",Design.INK);edit.setPadding(Ui.dp(this,15),Ui.dp(this,15),Ui.dp(this,15),Ui.dp(this,15));edit.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);identity.addView(edit,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));identity.setMinimumHeight(Ui.dp(this,48));identity.setContentDescription("Alarm settings for "+a.label);identity.setStateDescription(schedule);identity.setTooltipText(a.label+"\n"+schedule);identity.setFocusable(true);identity.setBackground(Design.touch(this,android.graphics.Color.TRANSPARENT,12));identity.setAccessibilityDelegate(new View.AccessibilityDelegate(){@Override public void onInitializeAccessibilityNodeInfo(View host,android.view.accessibility.AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(host,info);info.setClassName(Button.class.getName());}});identity.setOnClickListener(v->{InteractionFeedback.action(v);editAlarm(a);});body.addView(identity);
            int skipStart=body.getChildCount();AlarmSkipUi.add(body,a,this::render);
            // Keep the ordinary one-line skip action compact; status/error rows retain their natural height.
            for(int index=skipStart;index<body.getChildCount();index++){View action=body.getChildAt(index);if(("Skip next alarm for "+a.label).equals(action.getContentDescription()))action.setMinimumHeight(Ui.dp(this,48));}
            Design.space(body,16);Design.line(body);Design.space(body,12);
        }
        body.addView(new View(this),new LinearLayout.LayoutParams(1,0,1));
        LinearLayout actions=Design.row(this),nap=Design.col(this),create=Design.col(this);LinearLayout.LayoutParams left=new LinearLayout.LayoutParams(0,-2,1);left.rightMargin=Ui.dp(this,4);actions.addView(nap,left);LinearLayout.LayoutParams right=new LinearLayout.LayoutParams(0,-2,1);right.leftMargin=Ui.dp(this,4);actions.addView(create,right);body.addView(actions);
        Button napButton=Design.button(nap,"Take a nap",false,this::nap);napButton.setText(RecordUi.withIcon(this,"Take a nap","bed",Design.INK));Button newButton=Design.button(create,"Add alarm",true,()->editAlarm(newAlarm()));newButton.setText(RecordUi.withIcon(this,"Add alarm","plus",Design.CREAM));
    }
    private void alarmOptions(LinearLayout body){RecordUi.header(body,"Alarm options",this::back);Design.space(body,14);Design.item(body,"shield","Alarm protection","All alarms",()->startActivity(RecordUi.intent(this,AlarmProtectionActivity.class)));Design.space(body,20);PlanActivity.detail(body,"phone","Bedside NFC tag",Prefs.text(this,"nfcTag","").isEmpty()?"No tag enrolled":"A tag is enrolled","Set up",this::enrollNfc);Design.line(body);Design.space(body,12);Design.text(body,"Place the tag across the room for an NFC challenge. Tag identifiers are a wake-up habit tool, not a security credential.",13,400,Design.MUTED);}
    private Alarm newAlarm(){AlarmDefaultsSettings.Read saved=AlarmDefaultsSettings.load(this);if(!saved.error.isEmpty())Ui.toast(this,saved.error);return saved.create(store.alarms().stream().mapToInt(a->a.id).max().orElse(0)+1);}
    private void alarmSetting(LinearLayout body,String icon,String title,String value,Runnable action){LinearLayout row=Design.item(body,icon,title,value,action);row.setMinimumHeight(Ui.dp(this,48));}
    private String days(Alarm a) {if(a.nap!=null||a.napInvalid)return Naps.summary(this,a);if(a.repeatInvalid)return "Review repeat rule";if(a.repeatRule.custom())return a.repeatRule.label();if(a.dateInvalid)return "Review date";if(a.date!=null)return a.date.format(java.time.format.DateTimeFormatter.ofPattern("EEE d MMM uuuu",Locale.getDefault()));if(a.days.isEmpty())return "One time";if(a.days.equals(Set.of(1,2,3,4,5)))return "Mon – Fri";if(a.days.equals(Set.of(6,7)))return "Sat – Sun";if(a.days.size()==7)return "Every day";String[] d={"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};List<String> v=new ArrayList<>();for(int i=1;i<=7;i++)if(a.days.contains(i))v.add(d[i-1]);return String.join(" ",v);}
    private void editAlarm(Alarm source) {editAlarm(source,"edit");}
    private void editAlarm(Alarm source,String route) {if(source.nap!=null||source.napInvalid){startActivity(RecordUi.intent(this,NapActivity.class).putExtra("id",source.id));return;}startActivity(RecordUi.intent(this,AlarmEditorActivity.class).putExtra("alarm",source.json().toString()).putExtra("route",route));}
    private void nap() {startActivity(RecordUi.intent(this,NapActivity.class));}
    private void history(LinearLayout body){startActivity(RecordUi.intent(this,HistoryActivity.class));tab=2;}
    private void showNight(long id){startActivity(RecordUi.intent(this,RecordEditorActivity.class).putExtra("record",id));}
    private void bedtime(LinearLayout body) {
        body.setClipToPadding(false);Design.pad(body,24,8);
        LinearLayout header=Design.row(this);View back=Design.iconButton(this,"back","Back",()->{tab=0;render();});((Design.Icon)back).tint(Design.CREAM);header.addView(back,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));header.addView(Design.text(this,"Wind-down",25,600,Design.CREAM),new LinearLayout.LayoutParams(0,-2,1));RecordUi.menuAction(header,"Wind-down options",()->windDownOptions(null),true);body.addView(header,new LinearLayout.LayoutParams(-1,-2));Design.space(body,8);
        SleepPlan plan=SleepPlans.load(this);winddownValue=Design.text(body,"",46,700,Design.LILAC);winddownValue.setGravity(Gravity.CENTER);winddownCaption=Design.text(body,"",20,600,Design.LILAC);winddownCaption.setGravity(Gravity.CENTER);updateWindDown();Design.space(body,8);String target="Target bedtime  "+Design.clock(this,plan.bedtime);android.text.SpannableString targetLabel=new android.text.SpannableString(target+"  \ufffc");targetLabel.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),16,target.length(),0);targetLabel.setSpan(new android.text.style.ImageSpan(RecordUi.glyph(this,"chevron",Design.LILAC,16),android.text.style.ImageSpan.ALIGN_CENTER),targetLabel.length()-1,targetLabel.length(),0);TextView bedtime=Design.text(body,"",16,400,Design.LILAC);bedtime.setText(targetLabel);bedtime.setGravity(Gravity.CENTER);bedtime.setMinimumHeight(Ui.dp(this,48));bedtime.setFocusable(true);bedtime.setContentDescription(target);bedtime.setBackground(Design.touch(this,android.graphics.Color.TRANSPARENT,12));bedtime.setOnClickListener(v->{InteractionFeedback.action(v);startActivity(RecordUi.intent(this,PlanActivity.class));});Design.space(body,4);
        FrameLayout scene=new FrameLayout(this);ImageView art=new ImageView(this);art.setImageResource(R.drawable.bedside_winddown);art.setScaleType(ImageView.ScaleType.CENTER_CROP);art.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);scene.addView(art,new FrameLayout.LayoutParams(-1,-1));View blend=new View(this);blend.setBackground(new android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,new int[]{Design.PLUM,0x00241e30}));scene.addView(blend,new FrameLayout.LayoutParams(-1,Ui.dp(this,40),Gravity.TOP));LinearLayout.LayoutParams sceneSize=new LinearLayout.LayoutParams(-1,Ui.dp(this,144));sceneSize.leftMargin=sceneSize.rightMargin=-Ui.dp(this,24);body.addView(scene,sceneSize);Design.space(body,10);
        LinearLayout panel=Design.col(this);Design.pad(panel,14,4);panel.setBackground(Design.shape(this,0xff342b40,20));body.addView(panel,new LinearLayout.LayoutParams(-1,-2));
        Naps.Upcoming upcoming=Naps.next(this,store.alarms(),TonightScreen.clock.getAsLong(),ZoneId.systemDefault());Alarm next=upcoming.alarm;
        nightOption(panel,"alarm",next!=null&&next.nap!=null?"Nap":"Alarm",next==null?"No alarm enabled":next.nap!=null?Naps.summary(this,next):"Set for "+AlarmEditorActivity.displayTime(this,next),next==null?"Off":Naps.display(this,next),()->{if(next!=null&&next.nap!=null)editAlarm(next);else{tab=1;render();}});
        windGuardState=nightOption(panel,"shield","App guard","Block distracting apps",Prefs.flag(this,"bedGuard")?(Prefs.of(this).getLong("focusPauseUntil",0)>System.currentTimeMillis()?"Paused":guardEnabled()?"On":"Access needed"):"Off",()->startActivity(RecordUi.intent(this,GuardActivity.class)));
        try{SoundMix sounds=SoundPreferences.load(this);nightOption(panel,sounds.primary.equals("Rain")?"rain":"wave",sounds.label(),sounds.timerMinutes+" min timer",sounds.timerMinutes+" min",this::sleepSound);}catch(RuntimeException invalid){nightOption(panel,"wave","Sleep sound","Choose your sounds","",this::sleepSound);}
        LinearLayout footer=RecordUi.footer(body);Button track=Design.button(footer,"Start sleep tracking",true,this::tracking);track.setTextColor(Design.PLUM);track.setBackground(Design.touch(this,Design.LILAC,18));windDownIcon(track,"report",Design.PLUM);
        Button pause=Design.button(footer,"Pause wind-down (5 min)",false,()->{Prefs.put(this,"focusPauseUntil",System.currentTimeMillis()+5*60000L);updateWindDown();Ui.toast(this,"App guard paused for 5 minutes.");});pause.setTextColor(Design.LILAC);pause.setBackground(Design.outline(this,0xff342b40,0xff62566d,18));windDownIcon(pause,"pause",Design.LILAC);pause.setEnabled(Prefs.flag(this,"bedGuard"));pause.setAlpha(pause.isEnabled()?1:.45f);
    }
    private void windDownOptions(Bundle state){if(windOptions!=null&&windOptions.isShowing())return;SleepPicker.StateDialog dialog=new SleepPicker.StateDialog(this,"wind-down-options","Wind-down options");dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);LinearLayout content=Design.col(this);Design.pad(content,20,20);content.setBackground(Design.outline(this,0xff342b40,0xff62566d,20));Design.text(content,"Wind-down options",22,600,Design.CREAM);Design.space(content,14);nightOption(content,"moon","Guided wind-down","Read, breathe or listen","",()->{dialog.dismiss();startActivity(RecordUi.intent(this,GuidedWindDownActivity.class));});nightOption(content,"bed","Sleep plan","Bedtime, wake-up and reminders","",()->{dialog.dismiss();startActivity(RecordUi.intent(this,PlanActivity.class));});Button done=Design.button(content,"Done",false,dialog::dismiss);done.setTextColor(Design.PLUM);done.setBackground(Design.touch(this,Design.LILAC,16));ScrollView scroll=new ScrollView(this);scroll.addView(content);dialog.scroll=scroll;dialog.setContentView(scroll);dialog.setOnDismissListener(ignored->{if(windOptions==dialog)windOptions=null;});windOptions=dialog;SleepPicker.show(this,dialog,400);if(state!=null)dialog.onRestoreInstanceState(state);}
    private void updateWindDown(){if(winddownValue==null)return;SleepPlan plan=SleepPlans.load(this);LocalTime now=LocalTime.now();int current=now.getHour()*60+now.getMinute(),remaining=Math.floorMod(plan.bedtime-current,1440);boolean due=remaining==0||plan.guardActive(current)&&remaining>plan.windDown;winddownValue.setText(due?"Time to rest":RecordUi.units(PlanActivity.duration(remaining)));winddownValue.setTextSize((due?38:remaining>=60?46:68)*Appearance.scale(this));winddownCaption.setText(due?"Your bedtime has passed":"until bed");if(windGuardState!=null)windGuardState.setText(Prefs.flag(this,"bedGuard")?(Prefs.of(this).getLong("focusPauseUntil",0)>System.currentTimeMillis()?"Paused":guardEnabled()?"On":"Access needed"):"Off");}
    private void windDownIcon(Button button,String icon,int color){String title=button.getText().toString();android.text.SpannableString label=new android.text.SpannableString("\ufffc   "+title);label.setSpan(new android.text.style.ImageSpan(RecordUi.glyph(this,icon,color,20),android.text.style.ImageSpan.ALIGN_CENTER),0,1,0);button.setText(label);button.setContentDescription(title);}
    // Tooltip child views consume touch events; keep full text on the clickable row.
    private TextView nightOption(LinearLayout parent,String icon,String title,String subtitle,String value,Runnable action){if(parent.getChildCount()>0){View line=new View(this);line.setBackgroundColor(0xff51465e);parent.addView(line,new LinearLayout.LayoutParams(-1,Ui.dp(this,1)));}LinearLayout row=Design.row(this);row.setMinimumHeight(Ui.dp(this,64));Design.Icon image=new Design.Icon(this,icon,Design.LILAC);LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(Ui.dp(this,26),Ui.dp(this,26));ip.rightMargin=Ui.dp(this,18);row.addView(image,ip);LinearLayout copy=Design.col(this);TextView label=Design.text(copy,title,16,500,Design.CREAM);label.setMaxLines(2);label.setEllipsize(android.text.TextUtils.TruncateAt.END);Design.space(copy,5);TextView help=Design.text(copy,subtitle,12,400,Design.LILAC);help.setMaxLines(2);help.setEllipsize(android.text.TextUtils.TruncateAt.END);row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));TextView currentValue=Design.text(this,value,16,600,Design.LILAC);currentValue.setMaxWidth(Ui.dp(this,110));currentValue.setGravity(Gravity.END);row.addView(currentValue,new LinearLayout.LayoutParams(-2,-2));LinearLayout.LayoutParams arrow=new LinearLayout.LayoutParams(Ui.dp(this,18),Ui.dp(this,18));arrow.leftMargin=Ui.dp(this,10);row.addView(new Design.Icon(this,"chevron",Design.LILAC),arrow);row.setTooltipText(title+"\n"+subtitle);row.setOnClickListener(v->{InteractionFeedback.action(v);action.run();});row.setBackground(Design.touch(this,android.graphics.Color.TRANSPARENT,10));row.setFocusable(true);parent.addView(row,new LinearLayout.LayoutParams(-1,-2));return currentValue;}
    private void sleepSound(){startActivity(RecordUi.intent(this,SleepSoundsActivity.class));}
    private void guardDisclosure() {ReportDialogs.show(new AlertDialog.Builder(this).setTitle("Optional app guard").setMessage(getString(R.string.guard_description)).setPositiveButton("Open accessibility settings",(d,w)->open(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))).setNegativeButton("Later",null));}
    private boolean guardEnabled() {String s=Settings.Secure.getString(getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);return s!=null&&s.contains(getPackageName()+"/");}
    private void enrollNfc() {
        if(nfcEnrollment!=null)nfcEnrollment.close();try{nfcEnrollment=NfcEnrollment.platform(this,id->{Prefs.put(this,"nfcTag",id);render();},message->Ui.toast(this,message));nfcEnrollment.open();}catch(RuntimeException unavailable){Ui.toast(this,"NFC reader could not start. Your enrolled tag was kept.");}
    }
    private void setup(LinearLayout body) {
        hero(body,"Alarm readiness","Check your device settings for reliable alarms.");Design.space(body,18);
        AlarmReadinessUi.rows(this,body,NOTIFY,this::render);
        Design.space(body,18);Design.button(body,"Open battery settings",true,()->{if(body.isAttachedToWindow())AlarmReadinessUi.battery(this);});
        Design.space(body,10);ReadinessQuietAlarm.entry(this,body);
        Design.space(body,12);Design.item(body,"info","Alarm reliability tips",null,()->SleepPicker.message(this,"Alarm reliability","On Samsung, add SleepWell to Never auto sleeping apps. Allow unrestricted battery use and keep alarm permissions enabled. Force stopping or powering off the phone prevents alarms."));
        String error=Prefs.text(this,"lastError","");if(!error.isEmpty()){Design.space(body,18);Design.text(body,"Last alarm error",16,600,Design.INK);Design.space(body,6);Design.text(body,error,13,400,Design.MUTED);Design.button(body,"Dismiss message",false,()->{Prefs.put(this,"lastError","");render();});}
    }
    private void notificationAccess(){if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},NOTIFY);else open(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName()));}
    private void readiness(LinearLayout body,String icon,String title,String subtitle,boolean granted,boolean optional,Runnable action){
        LinearLayout row=Design.row(this);row.setMinimumHeight(Ui.dp(this,88));Design.Icon image=new Design.Icon(this,icon,Design.BROWN);Design.pad(image,10,10);image.setBackground(Design.shape(this,Design.SOFT,15));LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48));ip.rightMargin=Ui.dp(this,16);row.addView(image,ip);
        LinearLayout copy=Design.col(this);Design.text(copy,title,16,600,Design.INK);Design.space(copy,5);Design.text(copy,subtitle,12,400,Design.MUTED);row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));TextView state=Design.text(this,granted?"Allowed":optional?"Optional":"Needs attention",12,500,granted?0xff117b36:optional?Design.MUTED:0xffb56600);state.setGravity(Gravity.END);state.setMaxWidth(Ui.dp(this,76));LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-2,-2);sp.leftMargin=Ui.dp(this,12);row.addView(state,sp);Design.Icon marker=new Design.Icon(this,granted?"check":optional?"minus":"attention",Design.CREAM);Design.pad(marker,4,4);marker.setBackground(Design.shape(this,granted?0xff17813b:optional?0xff999999:0xffdf860b,20));LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(Ui.dp(this,22),Ui.dp(this,22));mp.leftMargin=Ui.dp(this,8);row.addView(marker,mp);row.setContentDescription(title+" · "+state.getText());row.setBackground(Design.touch(this,android.graphics.Color.TRANSPARENT,12));row.setOnClickListener(v->action.run());body.addView(row);Design.line(body);
    }
    private String audioRetentionLabel(){int days=Prefs.number(this,"audioRetentionDays",7);return days==0?"Keep all":days+(days==1?" day":" days");}
    private void soundAnalysis(LinearLayout body){
        RecordUi.header(body,"Sound analysis",this::back);Design.space(body,8);
        Design.toggle(body,"Microphone analysis","Estimate snoring and speech in your next recording.",Prefs.flag(this,"noiseEnabled"),on->{Prefs.put(this,"noiseEnabled",on);if(on&&checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},NOTIFY);render();});
        Design.space(body,12);Switch clips=Design.toggle(body,"Save sound clips",Prefs.flag(this,"noiseEnabled")?"Keep short clips from your next recording.":"Turn on microphone analysis first.",Prefs.flag(this,"saveClips"),on->Prefs.put(this,"saveClips",on));clips.setEnabled(Prefs.flag(this,"noiseEnabled"));
        if(Prefs.flag(this,"noiseEnabled")&&checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
            Design.space(body,12);LinearLayout access=PlanActivity.detail(body,"mic","Microphone access","Allow access to analyze and record sounds.","Open settings",()->open(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName()))));
            Design.pad(access,12,10);access.setBackground(Design.touch(this,Design.SOFT,14));access.setContentDescription("Microphone permission needed. Open Android settings");
        }
        settingsHeading(body,"Detection");
        settingsRow(body,"wave","Sound sensitivity","Choose which sound categories to detect",()->startActivity(RecordUi.intent(this,SoundSensitivityActivity.class)));
        settingsRow(body,"wave","Snoring cues","Sound or vibration when snoring is estimated",()->startActivity(RecordUi.intent(this,SnoringCuesActivity.class)));
        settingsHeading(body,"Your recordings");
        RecordEntry latest=Records.latest(store);PlanActivity.detail(body,"wave","Sound events",latest==null?"Record a night first":"Review your latest night",null,latest==null?null:()->startActivity(RecordUi.intent(this,SoundEventsActivity.class).putExtra("record",latest.id)));Design.line(body);
        PlanActivity.detail(body,"calendar","Audio retention","Automatically remove older recordings",audioRetentionLabel(),()->startActivity(RecordUi.intent(this,SoundLibrarySettingsActivity.class)));Design.line(body);
        settingsRow(body,"more","More sound settings","Threshold, favorites and bedtime audio",()->openSettingsGroup("sound"));
        Design.space(body,8);Design.item(body,"info","About sound analysis",null,()->SleepPicker.message(this,"Sound analysis","A bundled model estimates snoring and speech locally. Labels can be wrong and do not identify who made a sound. Microphone changes apply next recording; clip threshold affects saving, not class sensitivity. Relative sound levels are not calibrated decibels."));
        String analysisError=Prefs.text(this,"analysisError","");if(!analysisError.isEmpty())Design.text(body,analysisError,13,400,Design.MUTED);
        String error=Prefs.text(this,"audioError","");if(!error.isEmpty()){Design.space(body,18);Design.text(body,"Microphone needs attention",17,600,Design.INK);Design.space(body,6);Design.text(body,error,13,400,Design.MUTED);}
        Design.button(body,"Done",true,this::back);
    }
    private void recordingOptions(LinearLayout body){
        RecordUi.header(body,"Recording threshold",this::back);
        Design.space(body,10);Design.text(body,"Lower values save quieter sounds.",15,400,Design.MUTED);Design.space(body,24);
        TextView value=Design.text(body,"Relative amplitude · "+Prefs.number(this,"noiseThreshold",20),17,600,Design.INK);
        SeekBar threshold=new SeekBar(this);threshold.setContentDescription("Sound detection threshold");threshold.setMin(5);threshold.setMax(100);threshold.setProgress(Prefs.number(this,"noiseThreshold",20));AlarmSliders.style(threshold);body.addView(threshold,new LinearLayout.LayoutParams(-1,Ui.dp(this,56)));
        threshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int n,boolean user){value.setText("Relative amplitude · "+n);if(user)Prefs.put(MainActivity.this,"noiseThreshold",n);}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});
        Design.space(body,20);PlanActivity.detail(body,"lock","Private recordings","Kept in this phone's app storage",null,null);Design.line(body);
        PlanActivity.detail(body,"wave","Detection sensitivity","Sound categories have separate sensitivity controls",null,()->startActivity(RecordUi.intent(this,SoundSensitivityActivity.class)));
        Design.fixedAction(body,"Done",this::back);
    }
    private void open(Intent i) {try{startActivity(i);}catch(ActivityNotFoundException e){Ui.toast(this,"This setting is unavailable on this device.");}}
    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g) {super.onRequestPermissionsResult(r,p,g);render();}
    private void playClip(String filename) {
        stopPreview();try{preview=new MediaPlayer();preview.setDataSource(FileSafety.clip(this,filename).getAbsolutePath());preview.prepare();preview.setOnCompletionListener(p->stopPreview());preview.start();}catch(Exception e){stopPreview();Ui.toast(this,"Recording unavailable.");}
    }
    private void stopPreview() {if(preview!=null){preview.release();preview=null;}}
    private void backup(BackupJob.Kind kind){startActivity(RecordUi.intent(this,BackupActivity.class).putExtra("action",kind.name()));}
    private void export(int type) {backup(type==EXPORT_JSON?BackupJob.Kind.JSON:BackupJob.Kind.CSV);}
    @Override public void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);if(request==30){if(result==RESULT_OK)tab=2;render();return;}if(result!=RESULT_OK||data==null||data.getData()==null)return;Uri uri=data.getData();
        try {
            if(request==TONE) {if(pendingTone==null){Ui.toast(this,"Please choose the audio file again from Edit alarm.");return;}getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);pendingTone.tone=uri.toString();Prefs.put(this,"lastOccurrence_"+pendingTone.id,0L);store.save(pendingTone);try{Scheduler.schedule(this,pendingTone);}catch(SecurityException e){pendingTone.enabled=false;store.save(pendingTone);}Ui.toast(this,"Custom audio saved. Test it before relying on it.");}
            else if(request==EXPORT_JSON||request==EXPORT_CSV||request==IMPORT) {
                // An already-open legacy picker may return after an app update. The
                // private flow owns the URI and asynchronous work; this Store stays UI-owned.
                startActivity(RecordUi.intent(this,BackupActivity.class).putExtra("document",uri.toString()).putExtra("kind",request==IMPORT?BackupJob.Kind.IMPORT.name():request==EXPORT_JSON?BackupJob.Kind.JSON.name():BackupJob.Kind.CSV.name()));
            }
        }catch(Exception e){Ui.toast(this,"Could not complete this operation: "+e.getMessage());}render();
    }
    @Override protected void onDestroy(){if(windDimming!=null)windDimming.restore();if(windOptions!=null)windOptions.dismiss();stopPreview();if(nfcEnrollment!=null)nfcEnrollment.close();if(store!=null)store.close();super.onDestroy();}
}
