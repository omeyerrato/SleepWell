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
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MainActivity extends Activity {
    private Store store;private int tab=0;private Alarm pendingTone;private MediaPlayer preview;private NfcAdapter nfc;
    private static final String[] TABS={"Tonight","Alarms","History","Bedtime","Setup"};
    private static final int MIC=20,NOTIFY=21,TONE=22,EXPORT_JSON=23,EXPORT_CSV=24,IMPORT=25;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);store=new Store(this);Notifications.channels(this);
        if(getIntent().getBooleanExtra("visualReview",false))getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(state!=null)tab=state.getInt("tab");
        if(store.alarms().isEmpty())store.save(new Alarm(1));
        if(!TrackingService.running && Prefs.flag(this,"tracking")) {
            for(JSONObject s:store.rows("SELECT id,start FROM sessions WHERE end IS NULL")) {
                long id=s.optLong("id");List<JSONObject> last=store.rows("SELECT MAX(time) AS last FROM epochs WHERE session=?",""+id);
                long end=last.isEmpty()?s.optLong("start"):last.get(0).optLong("last",s.optLong("start"));
                android.content.ContentValues v=new android.content.ContentValues();v.put("end",end);v.put("interrupted",1);store.getWritableDatabase().update("sessions",v,"id=?",new String[]{""+id});
            }
            Prefs.put(this,"tracking",false);Prefs.put(this,"session",0L);
        }
    }
    @Override protected void onResume() {super.onResume();render();}
    @Override public void onSaveInstanceState(Bundle b) {super.onSaveInstanceState(b);b.putInt("tab",tab);}
    private void navigate(int destination){tab=destination==0?0:destination==1?2:4;render();}
    private void render() {
        boolean root=tab==0||tab==1||tab==2||tab==4||tab==7;
        LinearLayout body=Design.page(this,tab==0,root?(tab==2||tab==7?1:tab==4?2:0):null,this::navigate);
        switch(tab){case 1:alarmList(body);break;case 2:report(body);break;case 3:Design.header(body,"Wind-down",()->{tab=0;render();});bedtime(body);break;case 4:settingsHome(body);break;case 5:Design.header(body,"Reliability & recording",()->{tab=4;render();});setup(body);break;case 6:privacy(body);break;case 7:history(body);break;default:tonight(body);}
    }
    private void settingsHome(LinearLayout body){Design.header(body,"Settings",null);
        settingsRow(body,"bell","Alarms","Alarm time, sound and behavior",()->{tab=1;render();});
        settingsRow(body,"moon","Sleep tracking","Phone movement and microphone",()->{tab=5;render();});
        settingsRow(body,"wave","Sound analysis","Recording and sound sensitivity",()->{tab=5;render();});
        settingsRow(body,"shield","Bedtime guard","Reduce distractions at night",()->{tab=3;render();});
        settingsRow(body,"settings","Reliability","Battery, permissions and tips",()->{tab=5;render();});
        settingsRow(body,"lock","Data & privacy","Your data stays on your device",()->{tab=6;render();});
        settingsRow(body,"github","Free and open source","View source on GitHub",()->open(new Intent(Intent.ACTION_VIEW,Uri.parse("https://github.com/omeyerrato/SleepWell"))));
    }
    private void settingsRow(LinearLayout body,String icon,String title,String subtitle,Runnable action){
        LinearLayout row=Design.row(this);row.setMinimumHeight(Ui.dp(this,74));Design.Icon image=new Design.Icon(this,icon,Design.BROWN);image.setPadding(Ui.dp(this,8),Ui.dp(this,8),Ui.dp(this,8),Ui.dp(this,8));image.setBackground(Design.shape(this,Design.SOFT,15));LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48));ip.rightMargin=Ui.dp(this,20);row.addView(image,ip);
        LinearLayout labels=Design.col(this);Design.text(labels,title,17,600,Design.INK);Design.space(labels,5);Design.text(labels,subtitle,13,400,Design.MUTED);row.addView(labels,new LinearLayout.LayoutParams(0,-2,1));Design.Icon arrow=new Design.Icon(this,icon.equals("github")?"external":"chevron",Design.INK);row.addView(arrow,new LinearLayout.LayoutParams(Ui.dp(this,20),Ui.dp(this,20)));row.setBackground(Design.touch(this,android.graphics.Color.TRANSPARENT,8));row.setOnClickListener(v->action.run());body.addView(row);Design.line(body);
    }
    private void report(LinearLayout body){ReportScreen.render(this,body,store,new ReportScreen.Actions(){public void history(){tab=7;render();}public void tonight(){tab=0;render();}public void detail(long id){showNight(id);}});}
    private void privacy(LinearLayout body){Design.header(body,"Data & privacy",()->{tab=4;render();});Design.text(body,"Your sleep data stays on this phone.",16,400,Design.MUTED);Design.space(body,24);
        LinearLayout summary=Design.col(this);Design.pad(summary,20,20);summary.setBackground(Design.shape(this,Design.SOFT,18));Design.text(summary,"On this phone",22,600,Design.INK);Design.space(summary,14);long count=store.rows("SELECT COUNT(*) AS count FROM sessions").get(0).optLong("count");Design.text(summary,count+(count==1?" recorded session":" recorded sessions"),25,600,Design.INK);body.addView(summary);Design.space(body,24);
        Design.text(body,"Backup & export",20,600,Design.INK);Design.space(body,8);Design.text(body,"Backups include alarms, records, graphs and notes. Audio files and app preferences are excluded. Restored alarms start switched off.",14,400,Design.MUTED);
        LinearLayout exports=Design.row(this),exportColumn=Design.col(this),importColumn=Design.col(this);LinearLayout.LayoutParams exportParams=new LinearLayout.LayoutParams(0,-2,1);exportParams.rightMargin=Ui.dp(this,6);exports.addView(exportColumn,exportParams);LinearLayout.LayoutParams importParams=new LinearLayout.LayoutParams(0,-2,1);importParams.leftMargin=Ui.dp(this,6);exports.addView(importColumn,importParams);body.addView(exports);Design.button(exportColumn,"Export backup",true,()->export(EXPORT_JSON));Design.button(importColumn,"Import backup",false,()->startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("application/json").addCategory(Intent.CATEGORY_OPENABLE),IMPORT));Design.item(body,"file","Export records as CSV","",()->export(EXPORT_CSV));Design.line(body);Design.space(body,20);
        Design.text(body,"Recordings",20,600,Design.INK);Design.space(body,8);Design.text(body,"Sound clips stay in private storage. Clips older than 7 days are removed when a new recording starts. Microphone use is optional.",14,400,Design.MUTED);Design.space(body,20);
        Design.button(body,"Delete all sleep history",false,()->new AlertDialog.Builder(this).setTitle("Delete recordings and nights?").setMessage("This removes local sleep sessions, graphs, notes and audio. Alarms remain.").setPositiveButton("Delete history",(d,w)->{if(TrackingService.running){Ui.toast(this,"End tracking before deleting history.");return;}stopPreview();for(JSONObject clip:store.rows("SELECT path FROM clips"))FileSafety.deleteClip(this,clip.optString("path"));store.eraseHistory();render();}).setNegativeButton("Keep",null).show());
    }
    private void tonight(LinearLayout body) {
        TonightScreen.render(this,body,store,new TonightScreen.Actions(){
            public void alarms(){tab=1;render();}
            public void bedtime(){pickTime("bedtime",1350);}
            public void windDown(){tab=3;render();}
            public void settings(){tab=4;render();}
            public void start(){if(TrackingService.running){stopTracking();tab=2;new Handler(Looper.getMainLooper()).postDelayed(MainActivity.this::render,400);}else startTracking();}
            public void activeAlarm(){startActivity(new Intent(MainActivity.this,AlarmActivity.class));}
            public void confirmCheck(){int id=Prefs.number(MainActivity.this,"checkPending",0);Scheduler.cancel(MainActivity.this,id,Scheduler.CHECK);Prefs.put(MainActivity.this,"checkPending",0);render();}
            public void update(Alarm a){try{Prefs.put(MainActivity.this,"lastOccurrence_"+a.id,0L);Scheduler.schedule(MainActivity.this,a);store.save(a);}catch(SecurityException e){Ui.toast(MainActivity.this,e.getMessage());}render();}
        });
    }
    private void alarmList(LinearLayout body){
        LinearLayout head=Design.row(this);TextView title=Design.text(this,"Alarms",30,700,Design.INK);head.addView(title,new LinearLayout.LayoutParams(0,-2,1));head.addView(Design.iconButton(this,"plus","Add alarm",()->editAlarm(new Alarm(store.alarms().stream().mapToInt(a->a.id).max().orElse(0)+1))),new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));body.addView(head);Design.space(body,8);
        for(Alarm a:store.alarms()){
            LinearLayout top=Design.row(this);TextView time=Design.text(this,AlarmEditorActivity.displayTime(a),42,700,a.enabled?Design.INK:Design.MUTED);time.setMinimumHeight(Ui.dp(this,58));time.setContentDescription("Edit "+a.label);time.setOnClickListener(v->editAlarm(a));top.addView(time,new LinearLayout.LayoutParams(0,-2,1));
            Switch enabled=new Switch(this);enabled.setContentDescription("Enable "+a.label);enabled.setChecked(a.enabled);enabled.setMinHeight(Ui.dp(this,48));enabled.setThumbTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));enabled.setTrackTintList(new android.content.res.ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{Design.BROWN,0xffbfb7b1}));top.addView(enabled);body.addView(top);
            enabled.setOnCheckedChangeListener((v,on)->{if(on&&!Scheduler.allowed(this)){Ui.toast(this,"Allow exact alarms in Reliability first.");render();return;}a.enabled=on;try{if(on)Prefs.put(this,"lastOccurrence_"+a.id,0L);Scheduler.schedule(this,a);store.save(a);}catch(SecurityException e){Ui.toast(this,e.getMessage());}render();});
            Design.text(body,a.label,18,600,Design.INK);Design.space(body,4);Design.text(body,days(a),14,400,Design.MUTED);Design.space(body,12);Design.line(body);
            Design.item(body,"sun","Smart wake-up",a.smartWindow==0?"Off":a.smartWindow+" min",()->editAlarm(a));Design.line(body);
            Design.item(body,"tasks","Challenges",a.challenge+" × "+a.difficulty,()->editAlarm(a));Design.line(body);
            Design.item(body,"alarm","Test a quiet alarm","10 sec",()->{try{Scheduler.test(this,a);Ui.toast(this,"Quiet alarm in 10 seconds · capped at 25%");}catch(SecurityException e){Ui.toast(this,e.getMessage());}});Design.space(body,20);Design.line(body);Design.space(body,14);
        }
        Design.button(body,"Take a nap",false,this::nap);Design.button(body,"Add alarm",true,()->editAlarm(new Alarm(store.alarms().stream().mapToInt(a->a.id).max().orElse(0)+1)));
    }
    private String days(Alarm a) {if(a.days.isEmpty())return "Next occurrence only";String[] d={"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};List<String> v=new ArrayList<>();for(int i=1;i<=7;i++)if(a.days.contains(i))v.add(d[i-1]);return String.join(" ",v);}
    private void editAlarm(Alarm source) {startActivity(new Intent(this,AlarmEditorActivity.class).putExtra("alarm",source.json().toString()));}
    private void nap() {
        LinearLayout body=Ui.column(this);body.setPadding(24,8,24,8);final int[] minutes={20};Ui.range(body,"Nap duration minutes",20,1,180,n->minutes[0]=n);
        new AlertDialog.Builder(this).setTitle("A little reset").setView(body).setPositiveButton("Set nap",(d,w)->{
            if(!Scheduler.allowed(this)){Ui.toast(this,"Allow exact alarms in Setup first.");return;}
            Alarm a=new Alarm(store.alarms().stream().mapToInt(x->x.id).max().orElse(0)+1);ZonedDateTime t=ZonedDateTime.now().plusMinutes(minutes[0]);a.hour=t.getHour();a.minute=t.getMinute();a.label=minutes[0]+" minute nap";a.smartWindow=0;a.enabled=true;store.save(a);Scheduler.schedule(this,a);render();
        }).setNegativeButton("Cancel",null).show();
    }
    private void history(LinearLayout body) {
        Design.header(body,"Sleep history",()->{tab=2;render();});Ui.text(body,"Recorded time is time tracked, not a clinical measure of time asleep.");
        List<JSONObject> rows=store.rows("SELECT s.*, (SELECT COUNT(*) FROM epochs e WHERE e.session=s.id) AS epochs FROM sessions s ORDER BY start DESC LIMIT 60");
        List<JSONObject> week=store.rows("SELECT start,end FROM sessions WHERE end IS NOT NULL AND interrupted=0 AND start>=?",""+(System.currentTimeMillis()-7*86400000L));
        if(!week.isEmpty()) {long total=0;for(JSONObject s:week)total+=s.optLong("end")-s.optLong("start");LinearLayout stats=Ui.card(body);Ui.label(stats,"LAST 7 DAYS",11,Ui.MINT);Ui.heading(stats,SleepMath.duration(total/week.size()),32);Ui.text(stats,"Average recorded session · "+week.size()+" complete sessions");}
        if(rows.isEmpty()){LinearLayout empty=Ui.card(body);Ui.heading(empty,"Your story starts tonight.",24);Ui.text(empty,"Start a night to record movement, light and optional room noise. Your real data will appear here.");}
        for(JSONObject s:rows) {
            LinearLayout card=Ui.card(body);long id=s.optLong("id"),start=s.optLong("start"),end=s.optLong("end",System.currentTimeMillis());
            Ui.label(card,Instant.ofEpochMilli(start).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("EEE, dd MMM · HH:mm")),12,Ui.MINT);
            Ui.heading(card,SleepMath.duration(end-start),32);Ui.text(card,s.isNull("end")?"Tracking in progress":s.optInt("interrupted")==1?"Interrupted session · sensor coverage may be incomplete":"Recorded time · "+s.optInt("epochs")+" samples");
            if(!s.optString("note","").isEmpty())Ui.text(card,s.optString("note"));
            Ui.button(card,"Explore night",()->showNight(id));
        }

    }
    private void showNight(long id) {
        List<JSONObject> sessions=store.rows("SELECT * FROM sessions WHERE id=?",""+id);if(sessions.isEmpty())return;JSONObject s=sessions.get(0);
        List<JSONObject> epochs=store.rows("SELECT * FROM epochs WHERE session=? ORDER BY time",""+id);
        LinearLayout content=Ui.column(this);content.setPadding(24,12,24,24);ScrollView scroll=new ScrollView(this);scroll.addView(content);
        Ui.heading(content,"Movement",22);Ui.text(content,"Measured acceleration in one-minute buckets. Higher peaks mean more phone movement.");content.addView(new GraphView(this,epochs,"movement"),new LinearLayout.LayoutParams(-1,Ui.dp(this,160)));
        Ui.heading(content,"Room noise",22);Ui.text(content,"Sample-weighted microphone amplitude, not calibrated decibels. Missing readings mean the microphone was off or unavailable.");content.addView(new GraphView(this,epochs,"noise"),new LinearLayout.LayoutParams(-1,Ui.dp(this,130)));
        Ui.heading(content,"Room light",22);content.addView(new GraphView(this,epochs,"light"),new LinearLayout.LayoutParams(-1,Ui.dp(this,110)));
        int goal=Prefs.number(this,"goal",480);long duration=s.isNull("end")?0:s.optLong("end")-s.optLong("start");Ui.text(content,"Recorded duration / goal: "+SleepMath.duration(duration)+" / "+goal/60.0+"h. This is not a sleep-quality score.");
        for(JSONObject clip:store.rows("SELECT * FROM clips WHERE session=? ORDER BY time",""+id)) {
            String title=Instant.ofEpochMilli(clip.optLong("time")).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm"));
            Ui.button(content,"▶ Loud sound · "+title+" · 10 sec",()->playClip(clip.optString("path")));
        }
        EditText note=Ui.input(content,"Notes & #tags",s.optString("note",""),InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);final int[] rating={s.optInt("rating",0)};Ui.range(content,"How rested did you feel? (0 = unrated)",rating[0],0,5,n->rating[0]=n);
        new AlertDialog.Builder(this).setTitle("Your recorded night").setView(scroll).setPositiveButton("Save notes",(d,w)->{store.annotate(id,note.getText().toString(),rating[0]);stopPreview();render();}).setNegativeButton("Close",(d,w)->stopPreview()).setOnCancelListener(d->stopPreview()).show();
    }
    private void bedtime(LinearLayout body) {
        Ui.heading(body,"Protect your evening.",30);Ui.text(body,"Less scrolling tonight. More energy for the morning you want.");
        LinearLayout routine=Ui.card(body);Ui.label(routine,"YOUR WIND-DOWN",11,Ui.MINT);
        Ui.button(routine,"Bedtime · "+clock(Prefs.number(this,"bedtime",1350)),()->pickTime("bedtime",1350));Ui.button(routine,"Guard ends · "+clock(Prefs.number(this,"wakeTime",420)),()->pickTime("wakeTime",420));
        Ui.range(routine,"Recorded duration goal (minutes)",Prefs.number(this,"goal",480),300,600,n->Prefs.put(this,"goal",n));
        Ui.toggle(routine,"Bedtime reminder","Sends a daily notification at your chosen bedtime.",Prefs.flag(this,"bedReminder"),on->{Prefs.put(this,"bedReminder",on);if(on&&!Scheduler.allowed(this))Ui.toast(this,"Allow exact alarms in Setup to deliver this on time.");Scheduler.bedtime(this);});
        Ui.toggle(routine,"Bedtime app guard","Requires the optional accessibility service. Redirects only apps you select.",Prefs.flag(this,"bedGuard"),on->{Prefs.put(this,"bedGuard",on);if(on)guardDisclosure();});
        Ui.button(routine,"Choose distracting apps",this::chooseApps);
        Ui.toggle(routine,"Keep active alarm on screen","The optional accessibility service returns you to the challenge. Calls, Settings and emergency access remain available.",Prefs.flag(this,"alarmGuard"),on->{Prefs.put(this,"alarmGuard",on);if(on)guardDisclosure();});
        LinearLayout sound=Ui.card(body);Ui.heading(sound,"A softer landing",23);
        Ui.choice(sound,"Sleep sound",Prefs.text(this,"lullaby","Off"),new String[]{"Off","Rain","Brown noise"},s->Prefs.put(this,"lullaby",s));
        Ui.range(sound,"Sleep sound timer minutes",Prefs.number(this,"lullabyMinutes",30),5,90,n->Prefs.put(this,"lullabyMinutes",n));Ui.text(sound,"Changes apply next time tracking starts. Audio automatically stops at the timer.");
        Ui.button(body,"Enroll a bedside NFC tag",this::enrollNfc);
        Ui.text(body,Prefs.text(this,"nfcTag","").isEmpty()?"No NFC tag enrolled. Put a tag across the room to make getting up part of your alarm challenge.":"An NFC tag is enrolled. Tag IDs are a wake-up habit tool, not a security credential.");
    }
    private void pickTime(String key,int defaultValue) {int t=Prefs.number(this,key,defaultValue);new TimePickerDialog(this,(v,h,m)->{Prefs.put(this,key,h*60+m);Scheduler.bedtime(this);render();},t/60,t%60,true).show();}
    private static String clock(int minutes){return String.format(Locale.getDefault(),"%02d:%02d",minutes/60,minutes%60);}
    private void guardDisclosure() {new AlertDialog.Builder(this).setTitle("Optional app guard").setMessage(getString(R.string.guard_description)).setPositiveButton("Open accessibility settings",(d,w)->open(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))).setNegativeButton("Later",null).show();}
    private boolean guardEnabled() {String s=Settings.Secure.getString(getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);return s!=null&&s.contains(getPackageName()+"/");}
    private void chooseApps() {
        List<ResolveInfo> apps=getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),PackageManager.MATCH_ALL);
        apps.removeIf(a->a.activityInfo.packageName.equals(getPackageName())||a.activityInfo.packageName.equals("com.android.settings")||a.activityInfo.packageName.contains("dialer")||a.activityInfo.packageName.contains("incallui")||a.activityInfo.packageName.contains("emergency"));
        apps.sort(Comparator.comparing(a->a.loadLabel(getPackageManager()).toString().toLowerCase(Locale.ROOT)));
        List<ResolveInfo> unique=new ArrayList<>();Set<String> seen=new HashSet<>();for(ResolveInfo a:apps)if(seen.add(a.activityInfo.packageName))unique.add(a);
        Set<String> selected=new HashSet<>(Prefs.of(this).getStringSet("blocked",Set.of()));String[] names=new String[unique.size()];boolean[] checked=new boolean[unique.size()];
        for(int i=0;i<names.length;i++){names[i]=unique.get(i).loadLabel(getPackageManager()).toString();checked[i]=selected.contains(unique.get(i).activityInfo.packageName);}
        new AlertDialog.Builder(this).setTitle("Apps to keep for morning").setMultiChoiceItems(names,checked,(d,i,on)->{String p=unique.get(i).activityInfo.packageName;if(on)selected.add(p);else selected.remove(p);}).setPositiveButton("Save",(d,w)->Prefs.of(this).edit().putStringSet("blocked",selected).apply()).setNegativeButton("Cancel",null).show();
    }
    private void enrollNfc() {
        nfc=NfcAdapter.getDefaultAdapter(this);if(nfc==null||!nfc.isEnabled()){Ui.toast(this,"Enable NFC on your phone first.");return;}
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Hold your phone over the tag").setMessage("This tag will be required for NFC alarms.").setNegativeButton("Cancel",null).create();
        dialog.setOnDismissListener(d->nfc.disableReaderMode(this));dialog.show();
        nfc.enableReaderMode(this,tag->runOnUiThread(()->{Prefs.put(this,"nfcTag",AlarmActivity.hex(tag.getId()));dialog.dismiss();Ui.toast(this,"Tag enrolled");render();}),NfcAdapter.FLAG_READER_NFC_A|NfcAdapter.FLAG_READER_NFC_B|NfcAdapter.FLAG_READER_NFC_F|NfcAdapter.FLAG_READER_NFC_V|NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,null);
    }
    private void setup(LinearLayout body) {
        Ui.heading(body,"Set up for a good night.",30);Ui.text(body,"Android permissions are yours to control. Review each capability here.");
        LinearLayout reliable=Ui.card(body);Ui.label(reliable,"ALARM READINESS",11,Ui.MINT);
        status(reliable,"Exact alarms",Scheduler.allowed(this),()->open(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName()))));
        status(reliable,"Notifications",getSystemService(NotificationManager.class).areNotificationsEnabled(),()->{if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},NOTIFY);else open(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName()));});
        if(Build.VERSION.SDK_INT>=34)status(reliable,"Full-screen alarm",getSystemService(NotificationManager.class).canUseFullScreenIntent(),()->open(new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,Uri.parse("package:"+getPackageName()))));
        status(reliable,"Battery unrestricted",getSystemService(PowerManager.class).isIgnoringBatteryOptimizations(getPackageName()),()->open(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)));
        status(reliable,"Optional app guard",guardEnabled(),this::guardDisclosure);
        Ui.text(reliable,"On Samsung, also add SleepWell to Never auto sleeping apps. Force stopping or powering off the phone prevents alarms. Keep an independent backup while testing this early release.");
        LinearLayout recording=Ui.card(body);Ui.heading(recording,"Choose what gets recorded",23);
        Ui.toggle(recording,"Night noise analysis","Microphone records amplitude while tracking. Sound labels are not classified in this release.",Prefs.flag(this,"noiseEnabled"),on->{Prefs.put(this,"noiseEnabled",on);if(on&&checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},NOTIFY);});
        Ui.toggle(recording,"Save loud sound clips","Optional 10-second local WAV clips. Maximum 60 per night; deleted automatically after 7 days.",Prefs.flag(this,"saveClips"),on->Prefs.put(this,"saveClips",on));
        Ui.range(recording,"Noise trigger sensitivity threshold",Prefs.number(this,"noiseThreshold",20),5,100,n->Prefs.put(this,"noiseThreshold",n));
        Ui.text(recording,"Lower values record quieter sounds. Threshold units are relative amplitude, not dB. Changes to recording mode apply next night.");
        Ui.toggle(recording,"Smart wake waits for duration goal","Early triggering is disabled until the current session reaches your goal. The fixed alarm still rings on time.",Prefs.flag(this,"smartNeedsGoal"),on->Prefs.put(this,"smartNeedsGoal",on));
        Ui.button(body,"View source on GitHub",()->open(new Intent(Intent.ACTION_VIEW,Uri.parse("https://github.com/omeyerrato/SleepWell"))));
        Ui.text(body,"No accounts. No ads. No subscriptions. No network permission. Audio, movement and notes stay in private app storage unless you export them. Android cloud backup is disabled.");
        String error=Prefs.text(this,"lastError","");String audioError=Prefs.text(this,"audioError","");if(!error.isEmpty()||!audioError.isEmpty()){LinearLayout errors=Ui.card(body);Ui.heading(errors,"Diagnostics",20);Ui.text(errors,error+"\n"+audioError);Ui.button(errors,"Clear diagnostics",()->{Prefs.put(this,"lastError","");Prefs.put(this,"audioError","");render();});}
        Ui.button(body,"Delete all sleep history",()->new AlertDialog.Builder(this).setTitle("Delete recordings and nights?").setMessage("This removes local sleep sessions, graphs, notes and audio. Alarms remain.").setPositiveButton("Delete history",(d,w)->{
            if(TrackingService.running){Ui.toast(this,"End tracking before deleting history.");return;}
            stopPreview();for(JSONObject clip:store.rows("SELECT path FROM clips"))FileSafety.deleteClip(this,clip.optString("path"));store.eraseHistory();render();
        }).setNegativeButton("Keep",null).show());
    }
    private void status(LinearLayout p,String title,boolean granted,Runnable request) {Ui.button(p,(granted?"✓  ":"○  ")+title+(granted?" · ready":" · review"),request);}
    private void open(Intent i) {try{startActivity(i);}catch(ActivityNotFoundException e){Ui.toast(this,"This setting is unavailable on this device.");}}
    private void startTracking() {
        if(TrackingService.running)return;
        if(Prefs.flag(this,"noiseEnabled")&&checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED) {requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},MIC);return;}
        try{startForegroundService(new Intent(this,TrackingService.class));new Handler(Looper.getMainLooper()).postDelayed(this::render,500);}catch(Exception e){Ui.toast(this,"Could not start tracking: "+e.getMessage());}
    }
    private void stopTracking() {startService(new Intent(this,TrackingService.class).setAction("STOP"));}
    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g) {super.onRequestPermissionsResult(r,p,g);if(r==MIC){if(g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED)startTracking();else{Ui.toast(this,"Microphone denied. Turn noise analysis off to use movement-only tracking.");}}render();}
    private void playClip(String filename) {
        stopPreview();try{preview=new MediaPlayer();preview.setDataSource(FileSafety.clip(this,filename).getAbsolutePath());preview.prepare();preview.setOnCompletionListener(p->stopPreview());preview.start();}catch(Exception e){stopPreview();Ui.toast(this,"Recording unavailable.");}
    }
    private void stopPreview() {if(preview!=null){preview.release();preview=null;}}
    private void export(int type) {Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(type==EXPORT_JSON?"application/json":"text/csv").putExtra(Intent.EXTRA_TITLE,"sleepwell-"+LocalDate.now()+(type==EXPORT_JSON?".json":".csv"));startActivityForResult(i,type);}
    @Override public void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);if(result!=RESULT_OK||data==null||data.getData()==null)return;Uri uri=data.getData();
        try {
            if(request==TONE) {if(pendingTone==null){Ui.toast(this,"Please choose the audio file again from Edit alarm.");return;}getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);pendingTone.tone=uri.toString();Prefs.put(this,"lastOccurrence_"+pendingTone.id,0L);store.save(pendingTone);try{Scheduler.schedule(this,pendingTone);}catch(SecurityException e){pendingTone.enabled=false;store.save(pendingTone);}Ui.toast(this,"Custom audio saved. Test it before relying on it.");}
            else if(request==EXPORT_JSON||request==EXPORT_CSV) {
                String content;
                if(request==EXPORT_JSON)content=store.backup().toString(2);
                else {StringBuilder b=new StringBuilder("start_iso,end_iso,recorded_minutes,interrupted,rating,notes\r\n");for(JSONObject s:store.rows("SELECT * FROM sessions ORDER BY start")) {long start=s.optLong("start");boolean ended=!s.isNull("end");b.append(Instant.ofEpochMilli(start)).append(',').append(ended?Instant.ofEpochMilli(s.optLong("end")):"").append(',').append(ended?(s.optLong("end")-start)/60000:"").append(',').append(s.optInt("interrupted")).append(',').append(s.optInt("rating")).append(',').append(SleepMath.csv(s.optString("note"))).append("\r\n");}content=b.toString();}
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
