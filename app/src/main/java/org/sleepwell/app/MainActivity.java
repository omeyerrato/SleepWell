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
    private Store store;private int tab=0;private Alarm pendingTone;private MediaPlayer preview;private NfcAdapter nfc;
    private static final int NOTIFY=21,TONE=22,EXPORT_JSON=23,EXPORT_CSV=24,IMPORT=25;
    private final Handler appearanceClock=new Handler(Looper.getMainLooper());
    private NightArc nightArc;private boolean clockReceiverRegistered;
    private final BroadcastReceiver clockChanged=new BroadcastReceiver(){public void onReceive(Context c,Intent intent){if(tab==0||tab==3)renderKeepingScroll();}};
    private final Runnable clockUpdate=new Runnable(){public void run(){if(tab==3)updateWindDown();refreshTonightClock();appearanceClock.postDelayed(this,30000);}};
    private int renderedPhase=-1;private TextView winddownValue,winddownCaption;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);store=Records.open(this);Notifications.channels(this);if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::back);
        if(getIntent().getBooleanExtra("visualReview",false))getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(state!=null){tab=state.getInt("tab");int[] collapsed=state.getIntArray("collapsedAlarms");if(collapsed!=null)for(int id:collapsed)collapsedAlarms.add(id);}
        if(state==null){if(getIntent().getBooleanExtra("showReport",false))tab=2;if(getIntent().hasExtra("destination")){int destination=getIntent().getIntExtra("destination",0);tab=destination==3?4:Math.max(0,Math.min(2,destination));}}
        if(!TrackingService.running && Prefs.flag(this,"tracking")) {
            store.recoverInterrupted();Prefs.put(this,"tracking",false);Prefs.put(this,"trackingPaused",false);Prefs.put(this,"session",0L);
        }
    }
    @Override protected void onResume() {super.onResume();if(!TrackingService.running)try{store.recoverInterrupted();}catch(Exception e){Prefs.put(this,"trackingError","An interrupted record could not be finalized. Free storage and reopen the app.");}render();appearanceClock.removeCallbacks(clockUpdate);appearanceClock.postDelayed(clockUpdate,30000);if(!clockReceiverRegistered){IntentFilter filter=new IntentFilter(Intent.ACTION_TIME_CHANGED);filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);androidx.core.content.ContextCompat.registerReceiver(this,clockChanged,filter,androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED);clockReceiverRegistered=true;}}
    @Override protected void onPause(){appearanceClock.removeCallbacks(clockUpdate);if(clockReceiverRegistered){unregisterReceiver(clockChanged);clockReceiverRegistered=false;}super.onPause();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused){Runnable apply=()->{if(!isDestroyed()&&hasWindowFocus())Design.systemBars(this,tab==3||tab==0&&Appearance.palette(this).dark);};getWindow().getDecorView().post(apply);getWindow().getDecorView().postDelayed(apply,200);}}
    @Override public void onSaveInstanceState(Bundle b) {super.onSaveInstanceState(b);b.putInt("tab",tab);b.putIntArray("collapsedAlarms",collapsedAlarms.stream().mapToInt(Integer::intValue).toArray());}
    private void back(){if(tab==0){finish();return;}tab=tab==11?1:tab==3?0:tab==5||tab==6||tab==9?4:0;render();}
    @android.annotation.SuppressLint("GestureBackNavigation") @Override public void onBackPressed(){back();}
    private void navigate(int destination){tab=destination==3?4:Math.max(0,Math.min(2,destination));render();}
    private void render() {
        nightArc=null;
        renderedPhase=Appearance.palette(this).phase;
        boolean root=tab==0||tab==1||tab==2||tab==4||tab==7;
        LinearLayout body=tab==3?Design.darkPage(this):Design.page(this,tab==0,root?(tab==2||tab==7?2:tab==4?3:tab==1?1:0):null,this::navigate);
        switch(tab){case 1:alarmList(body);break;case 2:report(body);break;case 3:bedtime(body);break;case 4:settingsHome(body);break;case 5:setup(body);break;case 9:soundAnalysis(body);break;case 11:alarmOptions(body);break;case 6:privacy(body);break;case 7:history(body);break;default:tonight(body);}
    }
    private ScrollView scroll(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup)for(int n=0;n<((ViewGroup)v).getChildCount();n++){ScrollView found=scroll(((ViewGroup)v).getChildAt(n));if(found!=null)return found;}return null;}
    private void renderKeepingScroll(){ScrollView old=scroll(getWindow().getDecorView());int y=old==null?0:old.getScrollY();boolean arcFocused=nightArc!=null&&nightArc.hasFocus();render();ScrollView current=scroll(getWindow().getDecorView());if(current!=null)current.post(()->{current.scrollTo(0,y);if(arcFocused&&nightArc!=null)nightArc.requestFocus();});}
    void refreshTonightClock(){if(tab!=0)return;long now=TonightScreen.clock.getAsLong();java.time.ZoneId zone=java.time.ZoneId.systemDefault();if((nightArc!=null&&nightArc.needsNewOccurrence(now,zone))||(Appearance.automatic(this)&&Appearance.palette(this).phase!=renderedPhase))renderKeepingScroll();else if(nightArc!=null)nightArc.update(now,zone);}
    private void settingsHome(LinearLayout body){Design.header(body,"Settings",null);
        settingsRow(body,"bell","Alarms","Alarm time, sound and behavior",()->{tab=1;render();});
        settingsRow(body,"moon","Sleep tracking","Phone movement and microphone",this::tracking);
        settingsRow(body,"sound","Sleep sounds","Mix relaxing audio with a timer",this::sleepSound);
        settingsRow(body,"wave","Sound analysis","Recording and sound sensitivity",()->{tab=9;render();});
        settingsRow(body,"shield","Bedtime guard","Reduce distractions at night",()->startActivity(RecordUi.intent(this,GuardActivity.class)));
        settingsRow(body,"palette","Appearance","Light, dark and theme options",()->{startActivity(new Intent(this,AppearanceActivity.class).putExtra("visualReview",getIntent().getBooleanExtra("visualReview",false)));if(Appearance.reduced(this))overridePendingTransition(0,0);});
        settingsRow(body,"settings","Reliability","Battery, permissions and tips",()->{tab=5;render();});
        settingsRow(body,"lock","Data & privacy","Your data stays on your device",()->{tab=6;render();});
        Design.space(body,12);LinearLayout source=PlanActivity.detail(body,"github","Free and open source","View source on GitHub",null,()->open(new Intent(Intent.ACTION_VIEW,Uri.parse("https://github.com/omeyerrato/SleepWell"))));source.setMinimumHeight(Ui.dp(this,56));source.removeViewAt(source.getChildCount()-1);source.addView(new Design.Icon(this,"external",Design.INK),new LinearLayout.LayoutParams(Ui.dp(this,18),Ui.dp(this,18)));
    }
    private void settingsRow(LinearLayout body,String icon,String title,String subtitle,Runnable action){
        LinearLayout row=Design.row(this);row.setMinimumHeight(Ui.dp(this,70));Design.Icon image=new Design.Icon(this,icon,Design.BROWN);image.setPadding(Ui.dp(this,8),Ui.dp(this,8),Ui.dp(this,8),Ui.dp(this,8));image.setBackground(Design.shape(this,Design.SOFT,15));LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48));ip.rightMargin=Ui.dp(this,18);row.addView(image,ip);
        LinearLayout labels=Design.col(this);Design.text(labels,title,17,600,Design.INK);Design.space(labels,5);Design.text(labels,subtitle,13,400,Design.MUTED);row.addView(labels,new LinearLayout.LayoutParams(0,-2,1));Design.Icon arrow=new Design.Icon(this,icon.equals("github")?"external":"chevron",Design.INK);row.addView(arrow,new LinearLayout.LayoutParams(Ui.dp(this,20),Ui.dp(this,20)));row.setBackground(Design.touch(this,android.graphics.Color.TRANSPARENT,8));row.setOnClickListener(v->action.run());body.addView(row);Design.line(body);
    }
    private void report(LinearLayout body){String failure=Prefs.text(this,"trackingError","");if(!failure.isEmpty()){Design.text(body,"Recording needs attention: "+failure,13,500,Design.BROWN);Design.space(body,12);}ReportScreen.render(this,body,store,new ReportScreen.Actions(){public void history(){startActivity(RecordUi.intent(MainActivity.this,HistoryActivity.class));}public void tonight(){tab=0;render();}public void detail(long id){showNight(id);}});}
    private void privacy(LinearLayout body){
        hero(body,"Data & privacy","Your sleep data stays on this phone.");Design.space(body,20);
        LinearLayout summary=Design.col(this);Design.pad(summary,18,18);summary.setBackground(Design.shape(this,Design.SOFT,20));
        LinearLayout heading=Design.row(this);Design.Icon icon=new Design.Icon(this,"report",Design.BROWN);LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(Ui.dp(this,30),Ui.dp(this,30));ip.rightMargin=Ui.dp(this,16);heading.addView(icon,ip);LinearLayout label=Design.col(this);Design.text(label,"On this phone",21,600,Design.INK);Design.space(label,5);Design.text(label,"Stored locally on your phone.",13,400,Design.MUTED);heading.addView(label,new LinearLayout.LayoutParams(0,-2,1));summary.addView(heading);Design.space(summary,22);
        long count=store.rows("SELECT COUNT(*) AS count FROM sessions").get(0).optLong("count"),bytes=0;for(JSONObject clip:store.rows("SELECT path FROM clips"))try{File file=FileSafety.clip(this,clip.optString("path"));if(file.isFile())bytes+=file.length();}catch(IOException ignored){}
        LinearLayout totals=Design.row(this),records=Design.col(this),audio=Design.col(this);Design.text(records,"Records",13,400,Design.MUTED);Design.space(records,5);Design.text(records,count+(count==1?" record":" records"),27,700,Design.INK);totals.addView(records,new LinearLayout.LayoutParams(0,-2,1));View divider=new View(this);divider.setBackgroundColor(Design.LINE);LinearLayout.LayoutParams dp=new LinearLayout.LayoutParams(Ui.dp(this,1),Ui.dp(this,50));dp.rightMargin=Ui.dp(this,20);totals.addView(divider,dp);Design.text(audio,"Audio",13,400,Design.MUTED);Design.space(audio,5);Design.text(audio,bytes==0?"0 MB":bytes<1024*1024?"<1 MB":(bytes/(1024*1024))+" MB",27,700,Design.INK);totals.addView(audio,new LinearLayout.LayoutParams(0,-2,1));summary.addView(totals);body.addView(summary);
        Design.space(body,8);PlanActivity.detail(body,"file","Audio retention","Pruned when a new clip-saving session starts.","7 days",null);Design.line(body);Design.space(body,14);
        PlanActivity.detail(body,"upload","Backup","Alarms, records, measured graphs and notes",null,null);Design.text(body,"Audio, app settings and goal commitments are excluded. Imported alarms start off.",12,400,Design.MUTED);
        LinearLayout exports=Design.row(this),exportColumn=Design.col(this),importColumn=Design.col(this);LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(0,-2,1);ep.rightMargin=Ui.dp(this,4);exports.addView(exportColumn,ep);LinearLayout.LayoutParams imp=new LinearLayout.LayoutParams(0,-2,1);imp.leftMargin=Ui.dp(this,4);exports.addView(importColumn,imp);body.addView(exports);
        Button export=Design.button(exportColumn,"Export backup",true,()->export(EXPORT_JSON));export.setTextSize(14*Appearance.scale(this));Button restore=Design.button(importColumn,"Import backup",false,()->startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("application/json").addCategory(Intent.CATEGORY_OPENABLE),IMPORT));restore.setTextSize(14*Appearance.scale(this));
        Design.space(body,12);Design.item(body,"file","Export records as CSV",null,()->export(EXPORT_CSV));Design.line(body);PlanActivity.detail(body,"cloud","Cloud backup","Off · no connected cloud service",null,null);Design.line(body);Design.space(body,16);
        LinearLayout remove=PlanActivity.detail(body,"trash","Delete sleep history","Permanently removes records, notes and audio.",null,()->new AlertDialog.Builder(this).setTitle("Delete recordings and nights?").setMessage("This removes local sleep sessions, graphs, notes and audio. Alarms remain.").setPositiveButton("Delete history",(d,w)->{if(TrackingService.running){Ui.toast(this,"End tracking before deleting history.");return;}stopPreview();for(JSONObject clip:store.rows("SELECT path FROM clips"))FileSafety.deleteClip(this,clip.optString("path"));store.eraseHistory();render();}).setNegativeButton("Keep",null).show());((Design.Icon)remove.getChildAt(0)).tint(0xffd22b20);LinearLayout copy=(LinearLayout)remove.getChildAt(1);((TextView)copy.getChildAt(0)).setTextColor(0xffd22b20);
    }
    private void hero(LinearLayout body,String title,String subtitle){body.addView(Design.iconButton(this,"back","Back",()->{tab=4;render();}),new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));Design.space(body,14);Design.text(body,title,30,700,Design.INK);Design.space(body,8);Design.text(body,subtitle,15,400,Design.MUTED);}
    private void tonight(LinearLayout body) {
        nightArc=TonightScreen.render(this,body,store,new TonightScreen.Actions(){
            public void alarms(){tab=1;render();}
            public void bedtime(){startActivity(RecordUi.intent(MainActivity.this,PlanActivity.class));}
            public void windDown(){tab=3;render();}
            public void settings(){tab=4;render();}
            public void start(){tracking();}
            public void activeAlarm(){startActivity(new Intent(MainActivity.this,AlarmActivity.class));}
            public void confirmCheck(){int id=Prefs.number(MainActivity.this,"checkPending",0);Scheduler.cancel(MainActivity.this,id,Scheduler.CHECK);Prefs.put(MainActivity.this,"checkPending",0);render();}
            public void update(Alarm a){try{Prefs.put(MainActivity.this,"lastOccurrence_"+a.id,0L);Scheduler.schedule(MainActivity.this,a);store.save(a);}catch(SecurityException e){Ui.toast(MainActivity.this,e.getMessage());}render();}
        });
    }
    private void tracking(){startActivityForResult(new Intent(this,TrackingActivity.class).putExtra("visualReview",getIntent().getBooleanExtra("visualReview",false)),30);}
    private void alarmList(LinearLayout body){
        LinearLayout head=RecordUi.header(body,"Alarms",null);head.addView(Design.iconButton(this,"more","Alarm options",()->{tab=11;render();}),new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));
        View add=Design.iconButton(this,"plus","Add alarm",()->editAlarm(newAlarm()));
        add.setBackground(Design.touch(this,Design.SOFT,28));head.addView(add,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));Design.space(body,16);
        List<Alarm> alarms=store.alarms();
        if(alarms.isEmpty()){
            Design.space(body,28);Design.text(body,"A morning worth waking up for",25,600,Design.INK);Design.space(body,12);
            Design.text(body,"Choose a time, a sound and a challenge for your first alarm.",15,400,Design.MUTED);Design.space(body,32);
        }
        for(Alarm a:alarms){
            LinearLayout top=Design.row(this);TextView time=Design.text(this,AlarmEditorActivity.displayTime(this,a),54,700,a.enabled?Design.INK:0xff8a7e78);
            time.setText(Design.timeText(AlarmEditorActivity.displayTime(this,a)));time.setMinimumHeight(Ui.dp(this,64));time.setGravity(Gravity.CENTER_VERTICAL);time.setContentDescription("Edit "+a.label);time.setOnClickListener(v->editAlarm(a));top.addView(time,new LinearLayout.LayoutParams(0,-2,1));
            Switch enabled=new Switch(this);enabled.setContentDescription("Enable "+a.label);Design.styleSwitch(this,enabled,Design.BROWN,0xffbcb5b0);enabled.setChecked(a.enabled);top.addView(enabled);body.addView(top);
            enabled.setOnCheckedChangeListener((v,on)->{if(on&&!Scheduler.allowed(this)){Ui.toast(this,"Allow exact alarms in Reliability first.");render();return;}a.enabled=on;try{if(on)Prefs.put(this,"lastOccurrence_"+a.id,0L);Scheduler.schedule(this,a);store.save(a);}catch(SecurityException e){Ui.toast(this,e.getMessage());}render();});
            boolean expanded=!collapsedAlarms.contains(a.id);LinearLayout identity=Design.row(this),copy=Design.col(this);Design.text(copy,a.label,17,600,Design.INK);Design.space(copy,4);Design.text(copy,days(a),13,400,Design.MUTED);identity.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
            View expand=Design.iconButton(this,expanded?"up":"down",(expanded?"Collapse ":"Expand ")+a.label,()->{if(expanded)collapsedAlarms.add(a.id);else collapsedAlarms.remove(a.id);render();});expand.setSelected(expanded);identity.addView(expand,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));body.addView(identity);Design.space(body,8);
            if(expanded){Design.line(body);alarmSetting(body,"sun","Smart wake-up",a.smartWindow==0?"Off":a.smartWindow+" min",()->editAlarm(a,"smart"));Design.line(body);
                alarmSetting(body,"tasks","Challenges",a.challenge+" · "+a.difficulty+(a.difficulty==1?" round":" rounds"),()->editAlarm(a,"challenge"));}
            Design.space(body,16);Design.line(body);Design.space(body,12);
        }
        body.addView(new View(this),new LinearLayout.LayoutParams(1,0,1));
        LinearLayout actions=Design.row(this),nap=Design.col(this),create=Design.col(this);LinearLayout.LayoutParams left=new LinearLayout.LayoutParams(0,-2,1);left.rightMargin=Ui.dp(this,4);actions.addView(nap,left);LinearLayout.LayoutParams right=new LinearLayout.LayoutParams(0,-2,1);right.leftMargin=Ui.dp(this,4);actions.addView(create,right);body.addView(actions);
        Button napButton=Design.button(nap,"Take a nap",false,this::nap);napButton.setText(RecordUi.withIcon(this,"Take a nap","bed",Design.INK));Button newButton=Design.button(create,"Add alarm",true,()->editAlarm(newAlarm()));newButton.setText(RecordUi.withIcon(this,"Add alarm","plus",Design.CREAM));
    }
    private void alarmOptions(LinearLayout body){RecordUi.header(body,"Alarm options",()->{tab=1;render();});Design.space(body,14);Design.toggle(body,"Keep active alarm on screen","The optional app guard returns to the challenge. Calls, Settings and Android controls stay available.",Prefs.flag(this,"alarmGuard"),on->{Prefs.put(this,"alarmGuard",on);if(on&&!guardEnabled())guardDisclosure();});Design.space(body,20);PlanActivity.detail(body,"phone","Bedside NFC tag",Prefs.text(this,"nfcTag","").isEmpty()?"No tag enrolled":"A tag is enrolled","Set up",this::enrollNfc);Design.line(body);Design.space(body,12);Design.text(body,"Place the tag across the room for an NFC challenge. Tag identifiers are a wake-up habit tool, not a security credential.",13,400,Design.MUTED);}
    private Alarm newAlarm(){return new Alarm(store.alarms().stream().mapToInt(a->a.id).max().orElse(0)+1);}
    private void alarmSetting(LinearLayout body,String icon,String title,String value,Runnable action){LinearLayout row=Design.item(body,icon,title,value,action);row.setMinimumHeight(Ui.dp(this,48));}
    private String days(Alarm a) {if(a.days.isEmpty())return "One time";if(a.days.equals(Set.of(1,2,3,4,5)))return "Mon – Fri";if(a.days.equals(Set.of(6,7)))return "Sat – Sun";if(a.days.size()==7)return "Every day";String[] d={"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};List<String> v=new ArrayList<>();for(int i=1;i<=7;i++)if(a.days.contains(i))v.add(d[i-1]);return String.join(" ",v);}
    private void editAlarm(Alarm source) {editAlarm(source,"edit");}
    private void editAlarm(Alarm source,String route) {startActivity(RecordUi.intent(this,AlarmEditorActivity.class).putExtra("alarm",source.json().toString()).putExtra("route",route));}
    private void nap() {
        LinearLayout body=Ui.column(this);body.setPadding(24,8,24,8);final int[] minutes={20};Ui.range(body,"Nap duration minutes",20,1,180,n->minutes[0]=n);
        new AlertDialog.Builder(this).setTitle("A little reset").setView(body).setPositiveButton("Set nap",(d,w)->{
            if(!Scheduler.allowed(this)){Ui.toast(this,"Allow exact alarms in Setup first.");return;}
            Alarm a=new Alarm(store.alarms().stream().mapToInt(x->x.id).max().orElse(0)+1);ZonedDateTime t=ZonedDateTime.now().plusMinutes(minutes[0]);a.hour=t.getHour();a.minute=t.getMinute();a.label=minutes[0]+" minute nap";a.smartWindow=0;a.enabled=true;store.save(a);Scheduler.schedule(this,a);render();
        }).setNegativeButton("Cancel",null).show();
    }
    private void history(LinearLayout body){startActivity(RecordUi.intent(this,HistoryActivity.class));tab=2;}
    private void showNight(long id){startActivity(RecordUi.intent(this,RecordEditorActivity.class).putExtra("record",id));}
    private void bedtime(LinearLayout body) {
        body.setClipToPadding(false);
        LinearLayout header=Design.row(this);View back=Design.iconButton(this,"back","Back",()->{tab=0;render();});((Design.Icon)back).tint(Design.LILAC);header.addView(back,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));header.addView(Design.text(this,"Wind-down",26,600,0xfff5eafa));body.addView(header);Design.space(body,20);
        SleepPlan plan=SleepPlans.load(this);winddownValue=Design.text(body,"",46,700,Design.LILAC);winddownValue.setGravity(Gravity.CENTER);Design.space(body,5);winddownCaption=Design.text(body,"",22,600,Design.LILAC);winddownCaption.setGravity(Gravity.CENTER);updateWindDown();Design.space(body,12);TextView bedtime=Design.text(body,"Target bedtime  "+Design.clock(this,plan.bedtime),16,500,Design.LILAC);bedtime.setGravity(Gravity.CENTER);bedtime.setOnClickListener(v->startActivity(RecordUi.intent(this,PlanActivity.class)));Design.space(body,16);
        FrameLayout scene=new FrameLayout(this);ImageView art=new ImageView(this);art.setImageResource(R.drawable.bedside_winddown);art.setScaleType(ImageView.ScaleType.CENTER_CROP);art.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);scene.addView(art,new FrameLayout.LayoutParams(-1,-1));View blend=new View(this);blend.setBackground(new android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,new int[]{Design.PLUM,0x00241e30}));scene.addView(blend,new FrameLayout.LayoutParams(-1,Ui.dp(this,40),Gravity.TOP));LinearLayout.LayoutParams sceneSize=new LinearLayout.LayoutParams(-1,Ui.dp(this,188));sceneSize.leftMargin=sceneSize.rightMargin=-Ui.dp(this,24);body.addView(scene,sceneSize);Design.space(body,12);
        LinearLayout panel=Design.col(this);Design.pad(panel,14,4);panel.setBackground(Design.shape(this,0xff342b40,20));body.addView(panel);
        Alarm next=null;long nearest=Long.MAX_VALUE;for(Alarm alarm:store.alarms())if(alarm.enabled){long when=alarm.next(System.currentTimeMillis());if(when<nearest){nearest=when;next=alarm;}}
        nightOption(panel,"alarm","Alarm",next==null?"No alarm enabled":"Set for "+AlarmEditorActivity.displayTime(this,next),next==null?"Off":AlarmEditorActivity.displayTime(this,next),()->{tab=1;render();});
        nightOption(panel,"shield","App guard","Block distracting apps",Prefs.flag(this,"bedGuard")?(guardEnabled()?"On":"Access needed"):"Off",()->startActivity(RecordUi.intent(this,GuardActivity.class)));
        try{SoundMix sounds=SoundPreferences.load(this);nightOption(panel,sounds.primary.equals("Rain")?"rain":"wave","Sleep sound",sounds.label(),sounds.timerMinutes+" min",this::sleepSound);}catch(RuntimeException invalid){nightOption(panel,"wave","Sleep sound","Choose your sounds","",this::sleepSound);}
        Design.space(body,6);Button track=Design.button(body,"Start sleep tracking",true,this::tracking);track.setTextColor(Design.PLUM);track.setBackground(Design.touch(this,Design.LILAC,18));windDownIcon(track,"report",Design.PLUM);
        Button pause=Design.button(body,"Pause wind-down (5 min)",false,()->{Prefs.put(this,"focusPauseUntil",System.currentTimeMillis()+5*60000L);Ui.toast(this,"App guard paused for 5 minutes.");});pause.setTextColor(Design.LILAC);pause.setBackground(Design.outline(this,0xff342b40,0xff62566d,18));windDownIcon(pause,"pause",Design.LILAC);pause.setEnabled(Prefs.flag(this,"bedGuard"));pause.setAlpha(pause.isEnabled()?1:.45f);
    }
    private void updateWindDown(){if(winddownValue==null)return;SleepPlan plan=SleepPlans.load(this);LocalTime now=LocalTime.now();int current=now.getHour()*60+now.getMinute(),remaining=Math.floorMod(plan.bedtime-current,1440);boolean due=remaining==0||plan.guardActive(current)&&remaining>plan.windDown;winddownValue.setText(due?"Time to rest":RecordUi.units(PlanActivity.duration(remaining)));winddownValue.setTextSize((due?38:remaining>=60?46:68)*Appearance.scale(this));winddownCaption.setText(due?"Your bedtime has passed":"until bed");}
    private void windDownIcon(Button button,String icon,int color){String title=button.getText().toString();android.text.SpannableString label=new android.text.SpannableString("\ufffc   "+title);label.setSpan(new android.text.style.ImageSpan(RecordUi.glyph(this,icon,color,20),android.text.style.ImageSpan.ALIGN_CENTER),0,1,0);button.setText(label);button.setContentDescription(title);}
    private void nightOption(LinearLayout parent,String icon,String title,String subtitle,String value,Runnable action){if(parent.getChildCount()>0){View line=new View(this);line.setBackgroundColor(0xff51465e);parent.addView(line,new LinearLayout.LayoutParams(-1,Ui.dp(this,1)));}LinearLayout row=Design.row(this);row.setMinimumHeight(Ui.dp(this,72));Design.Icon image=new Design.Icon(this,icon,Design.LILAC);LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(Ui.dp(this,26),Ui.dp(this,26));ip.rightMargin=Ui.dp(this,18);row.addView(image,ip);LinearLayout copy=Design.col(this);Design.text(copy,title,16,500,0xfff5eafa);Design.space(copy,5);Design.text(copy,subtitle,12,400,Design.LILAC);row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));TextView currentValue=Design.text(this,value,16,600,Design.LILAC);currentValue.setMaxWidth(Ui.dp(this,110));currentValue.setGravity(Gravity.END);row.addView(currentValue,new LinearLayout.LayoutParams(-2,-2));LinearLayout.LayoutParams arrow=new LinearLayout.LayoutParams(Ui.dp(this,18),Ui.dp(this,18));arrow.leftMargin=Ui.dp(this,10);row.addView(new Design.Icon(this,"chevron",Design.LILAC),arrow);row.setOnClickListener(v->action.run());row.setBackground(Design.touch(this,android.graphics.Color.TRANSPARENT,10));parent.addView(row);}
    private void sleepSound(){startActivity(RecordUi.intent(this,SleepSoundsActivity.class));}
    private void guardDisclosure() {new AlertDialog.Builder(this).setTitle("Optional app guard").setMessage(getString(R.string.guard_description)).setPositiveButton("Open accessibility settings",(d,w)->open(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))).setNegativeButton("Later",null).show();}
    private boolean guardEnabled() {String s=Settings.Secure.getString(getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);return s!=null&&s.contains(getPackageName()+"/");}
    private void enrollNfc() {
        nfc=NfcAdapter.getDefaultAdapter(this);if(nfc==null||!nfc.isEnabled()){Ui.toast(this,"Enable NFC on your phone first.");return;}
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Hold your phone over the tag").setMessage("This tag will be required for NFC alarms.").setNegativeButton("Cancel",null).create();
        dialog.setOnDismissListener(d->nfc.disableReaderMode(this));dialog.show();
        nfc.enableReaderMode(this,tag->runOnUiThread(()->{Prefs.put(this,"nfcTag",AlarmActivity.hex(tag.getId()));dialog.dismiss();Ui.toast(this,"Tag enrolled");render();}),NfcAdapter.FLAG_READER_NFC_A|NfcAdapter.FLAG_READER_NFC_B|NfcAdapter.FLAG_READER_NFC_F|NfcAdapter.FLAG_READER_NFC_V|NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,null);
    }
    private void setup(LinearLayout body) {
        hero(body,"Alarm readiness","Check your device settings for reliable alarms.");Design.space(body,18);
        readiness(body,"alarm","Exact alarms","Allows alarms to play on time.",Scheduler.allowed(this),false,()->open(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName()))));
        readiness(body,"bell","Notifications","Lets SleepWell show alerts.",getSystemService(NotificationManager.class).areNotificationsEnabled(),false,this::notificationAccess);
        if(Build.VERSION.SDK_INT>=34)readiness(body,"phone","Full-screen alarms","Shows the alarm when the screen is locked.",getSystemService(NotificationManager.class).canUseFullScreenIntent(),false,()->open(new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,Uri.parse("package:"+getPackageName()))));
        readiness(body,"battery","Background battery","Allow unrestricted battery use for reliable alarms.",getSystemService(PowerManager.class).isIgnoringBatteryOptimizations(getPackageName()),false,()->open(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)));
        readiness(body,"mic","Microphone","Used for optional sound recordings.",checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED,true,()->{if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},NOTIFY);else open(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName())));});
        Design.space(body,18);Design.button(body,"Open battery settings",true,()->open(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)));
        Design.space(body,14);Design.text(body,"On Samsung, add SleepWell to Never auto sleeping apps. Force stopping or powering off the phone prevents alarms.",12,400,Design.MUTED);

        Design.space(body,12);Design.toggle(body,"Wait for duration goal","Smart wake-up only: early waking waits for your recorded-duration goal. The fixed alarm still rings on time.",Prefs.flag(this,"smartNeedsGoal"),on->Prefs.put(this,"smartNeedsGoal",on));
        String error=Prefs.text(this,"lastError","");if(!error.isEmpty()){Design.space(body,18);Design.text(body,"Last alarm error",16,600,Design.INK);Design.space(body,6);Design.text(body,error,13,400,Design.MUTED);Design.button(body,"Clear diagnostic",false,()->{Prefs.put(this,"lastError","");render();});}
    }
    private void notificationAccess(){if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},NOTIFY);else open(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName()));}
    private void readiness(LinearLayout body,String icon,String title,String subtitle,boolean granted,boolean optional,Runnable action){
        LinearLayout row=Design.row(this);row.setMinimumHeight(Ui.dp(this,88));Design.Icon image=new Design.Icon(this,icon,Design.BROWN);Design.pad(image,10,10);image.setBackground(Design.shape(this,Design.SOFT,15));LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48));ip.rightMargin=Ui.dp(this,16);row.addView(image,ip);
        LinearLayout copy=Design.col(this);Design.text(copy,title,16,600,Design.INK);Design.space(copy,5);Design.text(copy,subtitle,12,400,Design.MUTED);row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));TextView state=Design.text(this,granted?"Allowed":optional?"Optional":"Needs attention",12,500,granted?0xff117b36:optional?Design.MUTED:0xffb56600);state.setGravity(Gravity.END);state.setMaxWidth(Ui.dp(this,76));LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-2,-2);sp.leftMargin=Ui.dp(this,12);row.addView(state,sp);Design.Icon marker=new Design.Icon(this,granted?"check":optional?"minus":"attention",Design.CREAM);Design.pad(marker,4,4);marker.setBackground(Design.shape(this,granted?0xff17813b:optional?0xff999999:0xffdf860b,20));LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(Ui.dp(this,22),Ui.dp(this,22));mp.leftMargin=Ui.dp(this,8);row.addView(marker,mp);row.setContentDescription(title+" · "+state.getText());row.setBackground(Design.touch(this,android.graphics.Color.TRANSPARENT,12));row.setOnClickListener(v->action.run());body.addView(row);Design.line(body);
    }
    private void soundAnalysis(LinearLayout body){
        RecordUi.header(body,"Sound analysis",()->{tab=4;render();});Design.space(body,8);Design.text(body,"Choose what your phone records while you sleep.",15,400,Design.MUTED);Design.space(body,24);
        Design.toggle(body,"Microphone analysis","Measures relative sound level during tracking.",Prefs.flag(this,"noiseEnabled"),on->{Prefs.put(this,"noiseEnabled",on);if(on&&checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},NOTIFY);render();});
        Design.space(body,14);Switch clips=Design.toggle(body,"Save sound clips",Prefs.flag(this,"noiseEnabled")?"Save up to 60 local, 10-second WAV clips per night.":"Turn on microphone analysis first.",Prefs.flag(this,"saveClips"),on->Prefs.put(this,"saveClips",on));clips.setEnabled(Prefs.flag(this,"noiseEnabled"));Design.space(body,22);
        Design.text(body,"Detection threshold",17,600,Design.INK);Design.space(body,6);Design.text(body,"Lower values capture quieter sounds.",13,400,Design.MUTED);Design.space(body,8);
        TextView value=Design.text(body,"Relative amplitude · "+Prefs.number(this,"noiseThreshold",20),14,500,Design.BROWN);SeekBar threshold=new SeekBar(this);threshold.setContentDescription("Sound detection threshold");threshold.setMin(5);threshold.setMax(100);threshold.setProgress(Prefs.number(this,"noiseThreshold",20));threshold.setProgressTintList(android.content.res.ColorStateList.valueOf(Design.BROWN));threshold.setThumbTintList(android.content.res.ColorStateList.valueOf(Design.BROWN));body.addView(threshold,new LinearLayout.LayoutParams(-1,Ui.dp(this,56)));threshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int n,boolean user){value.setText("Relative amplitude · "+n);if(user)Prefs.put(MainActivity.this,"noiseThreshold",n);}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});
        Design.space(body,18);Design.line(body);PlanActivity.detail(body,"lock","Private recordings","Kept in this phone's app storage",null,null);Design.line(body);PlanActivity.detail(body,"calendar","Audio retention","Old clips are pruned when clip saving next starts.","7 days",null);Design.line(body);Design.space(body,18);
        Design.text(body,"These are sound events, not snoring or speech classifications. Relative amplitude is not a calibrated decibel measurement. Microphone mode applies next time tracking starts. Threshold and clip-saving changes affect an active microphone session immediately.",13,400,Design.MUTED);
        String error=Prefs.text(this,"audioError","");if(!error.isEmpty()){Design.space(body,18);Design.text(body,"Microphone needs attention",17,600,Design.INK);Design.space(body,6);Design.text(body,error,13,400,Design.MUTED);}
        Design.button(body,"Done",true,()->{tab=4;render();});
    }
    private void open(Intent i) {try{startActivity(i);}catch(ActivityNotFoundException e){Ui.toast(this,"This setting is unavailable on this device.");}}
    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g) {super.onRequestPermissionsResult(r,p,g);render();}
    private void playClip(String filename) {
        stopPreview();try{preview=new MediaPlayer();preview.setDataSource(FileSafety.clip(this,filename).getAbsolutePath());preview.prepare();preview.setOnCompletionListener(p->stopPreview());preview.start();}catch(Exception e){stopPreview();Ui.toast(this,"Recording unavailable.");}
    }
    private void stopPreview() {if(preview!=null){preview.release();preview=null;}}
    private void export(int type) {Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(type==EXPORT_JSON?"application/json":"text/csv").putExtra(Intent.EXTRA_TITLE,"sleepwell-"+LocalDate.now()+(type==EXPORT_JSON?".json":".csv"));startActivityForResult(i,type);}
    @Override public void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);if(request==30){if(result==RESULT_OK)tab=2;render();return;}if(result!=RESULT_OK||data==null||data.getData()==null)return;Uri uri=data.getData();
        try {
            if(request==TONE) {if(pendingTone==null){Ui.toast(this,"Please choose the audio file again from Edit alarm.");return;}getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);pendingTone.tone=uri.toString();Prefs.put(this,"lastOccurrence_"+pendingTone.id,0L);store.save(pendingTone);try{Scheduler.schedule(this,pendingTone);}catch(SecurityException e){pendingTone.enabled=false;store.save(pendingTone);}Ui.toast(this,"Custom audio saved. Test it before relying on it.");}
            else if(request==EXPORT_JSON||request==EXPORT_CSV) {
                String content;
                if(request==EXPORT_JSON)content=store.backup().toString(2);
                else content=Records.csv(store);
                try(OutputStream out=getContentResolver().openOutputStream(uri,"wt")){if(out==null)throw new IOException("No output stream");out.write(content.getBytes(StandardCharsets.UTF_8));}Ui.toast(this,"Export saved. Backups contain graphs and notes, but exclude audio files and settings.");
            } else if(request==IMPORT) {
                if(TrackingService.running||Prefs.flag(this,"ringing")){Ui.toast(this,"End tracking and alarms before restoring.");return;}
                String raw;try(InputStream in=getContentResolver().openInputStream(uri)){if(in==null)throw new IOException("No input stream");ByteArrayOutputStream buffer=new ByteArrayOutputStream();byte[] chunk=new byte[8192];int n;while((n=in.read(chunk))!=-1){buffer.write(chunk,0,n);if(buffer.size()>8*1024*1024)throw new IOException("Backup exceeds 8 MB");}raw=buffer.toString(StandardCharsets.UTF_8.name());}
                JSONObject backup=new JSONObject(raw);Backup.validate(backup);
                new AlertDialog.Builder(this).setTitle("Replace local data from backup?").setMessage("This replaces alarms, sessions, graphs and notes. Existing audio is deleted. Imported alarms start disabled so you can review them.").setPositiveButton("Restore",(d,w)->{try{Backup.restore(this,store,backup);Ui.toast(this,"Data restored. Review and enable your alarms.");render();}catch(Exception e){Ui.toast(this,"Restore failed: "+e.getMessage());}}).setNegativeButton("Cancel",null).show();
            }
        }catch(Exception e){Ui.toast(this,"Could not complete this operation: "+e.getMessage());}render();
    }
    @Override protected void onDestroy(){stopPreview();if(nfc!=null)nfc.disableReaderMode(this);if(store!=null)store.close();super.onDestroy();}
}
