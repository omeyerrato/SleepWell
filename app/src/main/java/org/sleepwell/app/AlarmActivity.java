package org.sleepwell.app;

import android.app.*;
import android.content.*;
import android.hardware.*;
import android.nfc.*;
import android.os.*;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;

public class AlarmActivity extends Activity implements SensorEventListener {
    private Alarm alarm;private ChallengeRoutine routine;private ChallengeRoutine.Position position;private WalkSession walk;private int sensorExpected;private String sensorRun="";private long sensorArmedNanos; private final Random random=new Random();private final Handler handler=new Handler(Looper.getMainLooper());
    private int completed=0,target=0,shakes=0;private String answer="";private long lastShake=0;
    private TypedWakeTasks typedWake;private Bundle typedState;
    private QrProof qrProof;private MathTask mathTask;private Integer selected;private TextView feedback;private EditText mathInput;private String mathEntry="",mathEntryKey="";
    private Dialog snoozeDialog;private AlarmFlipSensor flipSensor;private String flipOwner="";private long inputArmedUptime;
    private SensorManager sensors;private TextView progress;private NfcAdapter nfc;
    private final Handler lightHandler=new Handler(Looper.getMainLooper());private AlarmSunrise sunrise;private String occurrence="";private long lightStarted;private boolean resumed;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);setShowWhenLocked(true);setTurnScreenOn(true);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,()->Ui.toast(this,"Complete the challenge or snooze."));
        if(!Prefs.flag(this,"ringing")) { finish();return; }
        try { alarm=Alarm.read(new JSONObject(Prefs.text(this,"active_alarm","{}"))); } catch(JSONException e) { finish();return; }
        routine=alarm.challenges();occurrence=Prefs.text(this,"alarmRun","");
        if(state!=null)typedState=state.getBundle("typedWake");
        if(state!=null) { completed=state.getInt("completed");shakes=state.getInt("shakes"); }
        completed=Math.max(0,Prefs.number(this,"challengeCompleted",completed));
        if(state!=null && state.containsKey("selected"))selected=state.getInt("selected");if(state!=null){mathEntry=state.getString("mathEntry","");mathEntryKey=state.getString("mathEntryKey","");}
        if(state!=null&&state.containsKey("qrNonce"))try{qrProof=new QrProof(state.getString("qrRun"),state.getInt("qrUnit"),state.getString("qrNonce"),state.getString("qrHash"));}catch(IllegalArgumentException ignored){}restoreShakes();build();configureSunrise();
        if(alarm.pin) new AlertDialog.Builder(this).setTitle("Keep the challenge on screen?").setMessage("Android screen pinning asks you to confirm. You can unpin using Android's navigation controls.").setPositiveButton("Pin",(d,w)->startLockTask()).setNegativeButton("Later",null).show();
    }
    @Override public void onNewIntent(Intent i) {super.onNewIntent(i);setIntent(i);if(!Prefs.flag(this,"ringing")){finish();return;}if(!occurrence.equals(Prefs.text(this,"alarmRun",""))){inputArmedUptime=SystemClock.uptimeMillis();if(snoozeDialog!=null)snoozeDialog.dismiss();if(nfc!=null){nfc.disableReaderMode(this);nfc=null;}try{alarm=Alarm.read(new JSONObject(Prefs.text(this,"active_alarm","{}")));}catch(JSONException error){finish();return;}if(typedWake!=null){typedWake.close();typedWake=null;}typedState=null;qrProof=null;routine=alarm.challenges();completed=Math.max(0,Prefs.number(this,"challengeCompleted",0));occurrence=Prefs.text(this,"alarmRun","");restoreShakes();selected=null;build();configureSunrise();if(resumed){syncFlip();lightTick.run();}}}
    private void configureSunrise(){lightHandler.removeCallbacksAndMessages(null);closeSunrise();occurrence=Prefs.text(this,"alarmRun","");lightStarted=Prefs.of(this).getLong("alarmStartedElapsed",SystemClock.elapsedRealtime());final float original=getWindow().getAttributes().screenBrightness;sunrise=new AlarmSunrise(new AlarmWakePolicy(alarm),new AlarmSunrise.Port(){public void brightness(float value){WindowManager.LayoutParams attrs=getWindow().getAttributes();attrs.screenBrightness=value;getWindow().setAttributes(attrs);}public void restore(){WindowManager.LayoutParams attrs=getWindow().getAttributes();attrs.screenBrightness=original;getWindow().setAttributes(attrs);}});}
    private void closeSunrise(){if(sunrise!=null)try{sunrise.close();}catch(RuntimeException error){Prefs.put(this,"lastError","The alarm window brightness could not be restored.");}}
    private long lightElapsed(){return Math.max(0,SystemClock.elapsedRealtime()-lightStarted);}
    private final Runnable lightTick=new Runnable(){public void run(){if(!resumed||sunrise==null)return;try{if(!AlarmService.current(AlarmActivity.this,occurrence)){closeWalk();sunrise.pause();finish();return;}sunrise.resume(lightElapsed());lightHandler.postDelayed(this,250);}catch(RuntimeException error){closeSunrise();Prefs.put(AlarmActivity.this,"lastError","Screen sunrise is unavailable. Sound and the challenge continue.");}}};
    @Override protected void onResume(){super.onResume();resumed=true;inputArmedUptime=SystemClock.uptimeMillis();if(!AlarmService.current(this,occurrence)){closeWalk();finish();return;}if(position!=null&&(position.task.type.equals("Shake")||position.task.type.equals("NFC")||position.task.type.equals("Walk")||position.task.type.equals("QR code")||position.task.type.equals("Mirror text")||position.task.type.equals("Dream journal")))build();syncFlip();lightTick.run();}
    @Override protected void onPause(){if(typedWake!=null){typedState=typedWake.state();typedWake.close();typedWake=null;}resumed=false;if(snoozeDialog!=null)snoozeDialog.dismiss();if(flipSensor!=null)flipSensor.pause();closeWalk();if(sensors!=null)sensors.unregisterListener(this);if(nfc!=null)nfc.disableReaderMode(this);lightHandler.removeCallbacksAndMessages(null);if(sunrise!=null)try{sunrise.pause();}catch(RuntimeException error){closeSunrise();}super.onPause();}
    @Override public void onSaveInstanceState(Bundle b) { super.onSaveInstanceState(b);if(typedWake!=null)typedState=typedWake.state();if(typedState!=null)b.putBundle("typedWake",typedState);b.putInt("completed",completed);b.putInt("shakes",shakes);if(selected!=null)b.putInt("selected",selected);if(mathInput!=null)mathEntry=mathInput.getText().toString();b.putString("mathEntry",mathEntry);b.putString("mathEntryKey",mathEntryKey);if(qrProof!=null){b.putString("qrRun",qrProof.run);b.putInt("qrUnit",qrProof.unit);b.putString("qrNonce",qrProof.nonce);b.putString("qrHash",qrProof.hash);} }
    private void build() {
        if(typedWake!=null){typedState=typedWake.state();typedWake.close();typedWake=null;}
        if(mathInput!=null)mathEntry=mathInput.getText().toString();mathInput=null;closeWalk();handler.removeCallbacksAndMessages(null);if(sensors!=null)sensors.unregisterListener(this);if(nfc!=null){nfc.disableReaderMode(this);nfc=null;}position=routine.at(completed);if(position==null){dismiss();return;}final int expected=completed;final String expectedRun=occurrence;sensorExpected=expected;sensorRun=expectedRun;sensorArmedNanos=SystemClock.elapsedRealtimeNanos();
        LinearLayout body=Design.page(this,false,null,null);body.setPadding(0,0,0,0);
        ((View)body.getParent().getParent()).setBackground(new android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xffffc677,0xffffd893}));
        Design.space(body,32);TextView time=Design.text(body,java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("h:mm a",Locale.getDefault())),38,700,Design.INK);time.setGravity(Gravity.CENTER);
        Design.space(body,4);Design.text(body,"Time to wake up",21,600,Design.INK).setGravity(Gravity.CENTER);
        Design.space(body,20);Design.text(body,"Challenge "+(position.index+1)+" of "+routine.tasks.size()+(position.task.units()>1?" · Round "+(position.round+1)+" of "+position.task.units():""),14,500,Design.INK).setGravity(Gravity.CENTER);
        LinearLayout dots=Design.row(this);dots.setGravity(Gravity.CENTER);for(int n=0;n<routine.tasks.size();n++){View dot=new View(this);dot.setBackground(Design.shape(this,n<=position.index?Design.BROWN:0xffe8ad65,8));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(Ui.dp(this,34),Ui.dp(this,12));lp.setMargins(Ui.dp(this,4),Ui.dp(this,10),Ui.dp(this,4),0);dots.addView(dot,lp);}body.addView(dots);
        body.addView(new DawnScene(this),new LinearLayout.LayoutParams(-1,Ui.dp(this,112)));
        LinearLayout card=Design.col(this);card.setBackground(Design.shape(this,Design.CREAM,32));Design.pad(card,24,28);body.addView(card,new LinearLayout.LayoutParams(-1,-2));
        progress=Design.text(card,"",14,500,Design.MUTED);progress.setVisibility(View.GONE);
        if(alarm.routineInvalid)Design.text(card,"Routine settings could not be read. Complete the saved fallback task.",13,500,Design.MUTED);
        switch(position.task.type) {
            case "Mirror text":case "Dream journal":android.content.Context ownedApp=getApplicationContext();typedWake=new TypedWakeTasks(card,position.task.type,expectedRun,expected,position.task.journalMinimum,typedState,()->AlarmService.current(ownedApp,expectedRun)&&Prefs.number(ownedApp,"challengeCompleted",0)==expected,()->advance(expectedRun,expected));break;
            case "Visual search": visualSearch(card,expectedRun,expected);break;
            case "Walk": walk(card,expectedRun,expected);break;
            case "QR code": morningCode(card,expectedRun,expected);break;
            case "Shake":
                target=position.task.rounds*5;Ui.heading(card,"Move into your morning",24);progress.setText(shakes+" / "+target+" deliberate shakes");progress.setVisibility(View.VISIBLE);
                Ui.text(card,"Hold the phone securely. A gentle shake counts at most once every half second.");
                sensors=getSystemService(SensorManager.class);Sensor accel=sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                if(accel==null || resumed&&!registerShake(accel)) {Design.text(card,"Motion sensor unavailable. Solve this instead.",14,400,Design.MUTED);math(card,expectedRun,expected);}break;
            case "Memory": memory(card,expectedRun,expected);break;
            case "Sequence": sequence(card,expectedRun,expected);break;
            case "NFC":
                Ui.heading(card,"Get up. Tap your tag.",24);Ui.text(card,"Use the NFC tag you enrolled in Alarm options. Keep it across the room.");
                nfc=NfcAdapter.getDefaultAdapter(this);
                if(nfc==null || !nfc.isEnabled() || Prefs.text(this,"nfcTag","").isEmpty()) {Design.text(card,"NFC unavailable. Solve this instead.",14,400,Design.MUTED);math(card,expectedRun,expected);}
                else if(resumed)try{nfc.enableReaderMode(this,tag->{String id=hex(tag.getId());if(id.equals(Prefs.text(this,"nfcTag",""))) runOnUiThread(()->advance(expectedRun,expected));else runOnUiThread(()->Ui.toast(this,"That is not your enrolled tag."));},NfcAdapter.FLAG_READER_NFC_A|NfcAdapter.FLAG_READER_NFC_B|NfcAdapter.FLAG_READER_NFC_F|NfcAdapter.FLAG_READER_NFC_V|NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,null);}catch(RuntimeException unavailable){Design.text(card,"NFC reader could not start. Solve this instead.",14,400,Design.MUTED);math(card,expectedRun,expected);}break;
            default:
                if(!position.task.type.equals("Retype")){math(card,expectedRun,expected);break;}
                Design.text(card,"Retype to dismiss",22,600,Design.INK).setGravity(Gravity.CENTER);
                answer=Prefs.text(this,"retypeTask","");if(answer.isEmpty()){answer="MORNING "+(1000+random.nextInt(9000));Prefs.put(this,"retypeTask",answer);}
                Design.space(card,16);Design.text(card,answer,26,700,Design.INK).setGravity(Gravity.CENTER);
                EditText input=Ui.input(card,"Your answer","",InputType.TYPE_CLASS_TEXT);Ui.button(card,"Check answer",()->{if(input.getText().toString().trim().equals(answer))advance(expectedRun,expected);else input.setError("Try again");},true);
        }
        Design.space(card,12);Design.text(card,"Alarm stays on until you finish.",13,400,Design.MUTED).setGravity(Gravity.CENTER);
        int used=Prefs.number(this,"snoozes_"+alarm.id,0);
        if(used<alarm.snoozeLimit)Design.button(card,(alarm.inputs.chooseSnooze?"Snooze…":"Snooze "+SnoozePolicy.minutes(alarm.snoozeMinutes,alarm.shortenSnoozes,used)+" min")+" · "+(alarm.snoozeLimit-used)+" left",false,this::chooseSnooze);
        if((getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE)!=0)Design.button(card,"Developer stop · debug build",false,()->new AlertDialog.Builder(this).setTitle("Stop this development alarm?")
            .setMessage("Developer control for this debug build. It is hidden in release builds.").setPositiveButton("Stop alarm",(d,w)->{alarm.awakeCheck=0;dismiss();}).setNegativeButton("Keep going",null).show());
    }
    private void syncFlip(){boolean wanted=resumed&&alarm!=null&&!alarm.inputsInvalid&&alarm.inputs.flip&&AlarmInputs.canSnooze(AlarmService.current(this,occurrence),Prefs.number(this,"snoozes_"+alarm.id,0),alarm.snoozeLimit);if(flipSensor!=null&&(!wanted||flipSensor.closed()||!flipOwner.equals(occurrence))){flipSensor.close();if(!flipSensor.released())return;flipSensor=null;}if(!wanted)return;if(flipSensor==null){String run=occurrence;flipOwner=run;flipSensor=new AlarmFlipSensor(this,()->resumed&&run.equals(occurrence)&&AlarmInputs.canSnooze(AlarmService.current(this,run),Prefs.number(this,"snoozes_"+alarm.id,0),alarm.snoozeLimit),this::snooze);}if(!flipSensor.start()){Prefs.put(this,"lastError","Flip snooze is unavailable. Use the on-screen Snooze action.");Ui.toast(this,"Flip snooze unavailable");}}
    @Override public boolean onKeyDown(int key,KeyEvent event){if(alarm!=null&&(key==KeyEvent.KEYCODE_VOLUME_UP||key==KeyEvent.KEYCODE_VOLUME_DOWN)&&!alarm.inputsInvalid&&alarm.inputs.interceptKeys()){if(resumed&&event.getDownTime()>=inputArmedUptime&&alarm.inputs.keySnooze(true,event.getRepeatCount(),AlarmService.current(this,occurrence),Prefs.number(this,"snoozes_"+alarm.id,0),alarm.snoozeLimit))snooze();return true;}return super.onKeyDown(key,event);}
    @Override public boolean onKeyUp(int key,KeyEvent event){if(alarm!=null&&!alarm.inputsInvalid&&alarm.inputs.interceptKeys()&&(key==KeyEvent.KEYCODE_VOLUME_UP||key==KeyEvent.KEYCODE_VOLUME_DOWN))return true;return super.onKeyUp(key,event);}
    private void math(LinearLayout card,String expectedRun,int expected){math(card,expectedRun,expected,position.task.type.equals("Math")?position.task.math:MathSettings.LEGACY);}
    private void visualSearch(LinearLayout card,String expectedRun,int expected){
        boolean fallback=expectedRun.equals(Prefs.text(this,"moonFallbackRun",""))&&Prefs.number(this,"moonFallbackUnit",-1)==expected;
        if(fallback){Design.text(card,"Math fallback",16,600,Design.INK);math(card,expectedRun,expected);return;}
        try{MoonSearch board=MoonSearchState.open(Prefs.of(this),expectedRun,expected,position.round,random);MoonSearchView.render(card,board,()->expectedRun.equals(occurrence)&&expected==completed&&AlarmService.current(this,expectedRun),()->advance(expectedRun,expected));
            Design.button(card,"Use Math instead",false,()->{if(!expectedRun.equals(occurrence)||expected!=completed||!AlarmService.current(this,expectedRun))return;if(Prefs.of(this).edit().putString("moonFallbackRun",expectedRun).putInt("moonFallbackUnit",expected).commit())build();else Ui.toast(this,"Fallback could not be saved. Try again.");});
        }catch(RuntimeException invalid){Design.text(card,"Search board unavailable · Math fallback",14,500,Design.MUTED);math(card,expectedRun,expected);}
    }
    private void math(LinearLayout card,String expectedRun,int expected,MathSettings configuration){
        try{mathTask=MathTask.decode(Prefs.text(this,"mathTask",""));if(!mathTask.matches(configuration))throw new IllegalArgumentException("Arithmetic settings changed");}catch(RuntimeException e){mathTask=MathTask.create(random,configuration);selected=null;Prefs.put(this,"mathTask",mathTask.encode());}
        Design.text(card,"Solve to dismiss",22,600,Design.INK).setGravity(Gravity.CENTER);Design.space(card,12);
        TextView question=Design.text(card,mathTask.question(),48,700,Design.INK);question.setGravity(Gravity.CENTER);question.setContentDescription(mathTask.spoken());Design.space(card,20);
        if(configuration.typed){String key=expectedRun+":"+expected+":"+mathTask.encode();if(!key.equals(mathEntryKey)){mathEntry="";mathEntryKey=key;}mathInput=new EditText(this);mathInput.setContentDescription("Math answer");mathInput.setHint("Your answer");mathInput.setSingleLine(true);mathInput.setInputType(InputType.TYPE_CLASS_NUMBER);mathInput.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(5)});mathInput.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_DONE);mathInput.setGravity(Gravity.CENTER);mathInput.setTextSize(34*Appearance.scale(this));mathInput.setTypeface(Design.font(this,600));mathInput.setTextColor(Design.INK);mathInput.setHintTextColor(Design.MUTED);mathInput.setBackground(Design.outline(this,Design.SOFT,Design.LINE,20));Design.pad(mathInput,16,18);mathInput.setText(mathEntry);card.addView(mathInput,new LinearLayout.LayoutParams(-1,-2));}
        else for(int row=0;row<2;row++){LinearLayout r=Design.row(this);for(int col=0;col<2;col++){int option=mathTask.options.get(row*2+col);Button b=new Button(this);b.setStateListAnimator(null);b.setElevation(0);b.setText(""+option);b.setTextSize(38*Appearance.scale(this));b.setTypeface(Design.font(this,600));b.setTextColor(selected!=null&&selected==option?Design.CREAM:Design.INK);b.setSelected(selected!=null&&selected==option);b.setContentDescription("Answer "+option);b.setBackground(Design.touch(this,selected!=null&&selected==option?Design.BROWN:Design.SOFT,22));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,Ui.dp(this,78),1);lp.setMargins(Ui.dp(this,col==0?0:4),Ui.dp(this,4),Ui.dp(this,col==0?4:0),Ui.dp(this,4));r.addView(b,lp);b.setOnClickListener(v->{selected=option;build();});}card.addView(r);}
        feedback=Design.text(card,configuration.typed?"Type a whole-number answer":"Select an answer",13,400,Design.MUTED);feedback.setGravity(Gravity.CENTER);Design.space(card,6);
        Runnable check=()->{if(configuration.typed){if(mathInput==null||mathInput.getText().toString().trim().isEmpty()){feedback.setText("Enter an answer first");return;}if(mathTask.acceptsTyped(mathInput.getText().toString())){advance(expectedRun,expected);return;}}else{if(selected==null){feedback.setText("Choose an answer first");return;}if(mathTask.accepts(selected)){advance(expectedRun,expected);return;}}feedback.setText("Try again · alarm is still ringing");feedback.setTextColor(0xffb33120);feedback.announceForAccessibility("Try again");};Design.button(card,"Check answer",true,check);if(mathInput!=null)mathInput.setOnEditorActionListener((view,action,event)->{if(action==android.view.inputmethod.EditorInfo.IME_ACTION_DONE){check.run();return true;}return false;});
    }
    private void morningCode(LinearLayout card,String expectedRun,int expected){MorningCode code=position.task.code;if(code==null){Design.text(card,"Morning code unavailable. Solve Math instead.",14,500,Design.MUTED);math(card,expectedRun,expected);return;}boolean chosen=expectedRun.equals(Prefs.text(this,"qrFallbackRun",""))&&Prefs.number(this,"qrFallbackUnit",-1)==expected;QrCameraAccess.Status permission=QrCameraAccess.port.status(this);if(chosen||permission!=QrCameraAccess.Status.READY){if(!chosen&&expectedRun.equals(occurrence)&&expected==completed&&AlarmService.current(this,expectedRun))Prefs.of(this).edit().putString("qrFallbackRun",expectedRun).putInt("qrFallbackUnit",expected).apply();Design.text(card,chosen?"Math fallback":"Camera unavailable · Math fallback",16,600,Design.INK);Design.space(card,12);math(card,expectedRun,expected,code.fallback);return;}Design.text(card,"Find your morning code",22,600,Design.INK);Design.space(card,10);Design.text(card,code.label,18,500,Design.BROWN);Design.space(card,16);Design.button(card,"Scan morning code",true,()->{if(!expectedRun.equals(occurrence)||expected!=completed||!AlarmService.current(this,expectedRun)){finish();return;}qrProof=new QrProof(expectedRun,expected,UUID.randomUUID().toString(),code.hash);try{startActivityForResult(QrCodeActivity.scan(this,code,expectedRun,expected,qrProof.nonce),344);}catch(RuntimeException unavailable){qrProof=null;qrFallback(expectedRun,expected);}});Design.button(card,"Use Math fallback",false,()->qrFallback(expectedRun,expected));}
    private void qrFallback(String expectedRun,int expected){if(!expectedRun.equals(occurrence)||expected!=completed||!AlarmService.current(this,expectedRun))return;qrProof=null;Prefs.of(this).edit().putString("qrFallbackRun",expectedRun).putInt("qrFallbackUnit",expected).apply();build();}
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request!=344||qrProof==null)return;QrProof proof=qrProof;qrProof=null;if(data==null||result!=RESULT_OK&&result!=QrCodeActivity.FALLBACK||!proof.run.equals(data.getStringExtra("run"))||proof.unit!=data.getIntExtra("unit",-1))return;ChallengeRoutine.Position current=routine.at(completed);String hash=current!=null&&current.task.code!=null?current.task.code.hash:"";boolean fallback=result==QrCodeActivity.FALLBACK;if(!proof.consume(data.getStringExtra("request"),occurrence,completed,hash,data.getStringExtra("hash"),fallback,()->AlarmService.current(this,proof.run)))return;if(fallback)qrFallback(proof.run,proof.unit);else advance(proof.run,proof.unit);}
    private void closeWalk(){if(walk!=null&&walk.stopForFallback())walk=null;}
    private void walk(LinearLayout card,String expectedRun,int expected){
        if(WalkFallbackSaved.chosen(Prefs.of(this),expectedRun,expected)){Design.text(card,"Math fallback",16,600,Design.INK);Design.space(card,12);math(card,expectedRun,expected);return;}
        Design.text(card,"Walk into your morning",22,600,Design.INK);Design.space(card,12);int target=position.task.targetSteps;
        int earned=expectedRun.equals(Prefs.text(this,"walkRun",""))&&Prefs.number(this,"walkUnit",-1)==expected?Prefs.number(this,"walkEarned",0):0;
        TextView count=Design.text(card,Math.max(0,Math.min(target,earned))+" / "+target+" steps",30,700,Design.INK);
        Design.text(card,"Carry your phone. Walk in a safe, clear space.",13,400,Design.MUTED);if(!resumed)return;
        // A failed unregister retains the old handle. Never allocate a replacement over uncertain cleanup.
        if(walk!=null){Design.text(card,"Step counting could not stop. Use Math fallback to retry.",14,500,Design.MUTED);walkFallback(card,walk,expectedRun,expected);return;}
        WalkSession.Port source;try{source=WalkPlatform.factory.create(this);}catch(RuntimeException unavailable){Design.text(card,"Step counting is unavailable. Solve math instead.",14,500,Design.MUTED);math(card,expectedRun,expected);return;}
        WalkSession[] owner={null};
        walk=new WalkSession(source,new WalkSession.Effects(){
            public boolean current(String run,int unit){return resumed&&run.equals(occurrence)&&unit==completed&&AlarmService.current(AlarmActivity.this,run);}
            public void progress(String run,int unit,int value){if(!current(run,unit))return;Prefs.of(AlarmActivity.this).edit().putString("walkRun",run).putInt("walkUnit",unit).putInt("walkEarned",value).apply();count.setText(value+" / "+target+" steps");}
            public void solved(String run,int unit){advance(run,unit);}
            public void unavailable(String run,int unit,WalkSession.State state){handler.post(()->{if(walk==owner[0]&&current(run,unit))build();});}
        },expectedRun,expected,target,earned);
        WalkSession session=walk;owner[0]=session;session.resume();if(walk!=session)return;
        String unavailable=switch(session.state()){case PERMISSION->"Physical activity access is off. Solve math instead.";case UNSUPPORTED->"This phone has no supported step sensor. Solve math instead.";case ERROR->"Step counting could not start. Solve math instead.";default->"";};
        if(!unavailable.isEmpty()){Design.space(card,12);Design.text(card,unavailable,14,500,Design.MUTED);math(card,expectedRun,expected);return;}
        walkFallback(card,session,expectedRun,expected);
    }
    private void walkFallback(LinearLayout card,WalkSession session,String expectedRun,int expected){
        TextView error=Design.text(card,"",13,500,0xffa93919);error.setVisibility(View.GONE);error.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        Design.button(card,"Use Math fallback",false,()->{
            WalkFallback.Result result=WalkFallback.choose(expectedRun,expected,new WalkFallback.Effects(){
                public boolean current(String run,int unit){return resumed&&walk==session&&run.equals(occurrence)&&unit==completed&&AlarmService.current(AlarmActivity.this,run);}
                public boolean stop(){return session.stopForFallback();}
                public boolean save(String run,int unit){return WalkFallbackSaved.save(Prefs.of(AlarmActivity.this),run,unit);}
            });
            if(result==WalkFallback.Result.CHOSEN){walk=null;selected=null;build();}
            else if(result!=WalkFallback.Result.STALE){error.setText(result==WalkFallback.Result.STOP_FAILED?"Step counting could not stop. Try again.":"Math fallback could not be saved. Try again.");error.setVisibility(View.VISIBLE);}
        });
    }
    private boolean registerShake(Sensor accelerometer){try{return sensors.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_GAME);}catch(RuntimeException unavailable){return false;}}
    private void restoreShakes(){shakes=occurrence.equals(Prefs.text(this,"challengeShakeRun",""))&&completed==Prefs.number(this,"challengeShakeUnit",-1)?Prefs.number(this,"challengeShakeCount",0):0;}
    private void advance(String expectedRun,int expected) {synchronized(Scheduler.class){if(!expectedRun.equals(occurrence))return;if(!AlarmService.current(this,expectedRun)){finish();return;}int next=routine.advance(completed,expected);if(next==completed)return;completed=next;Prefs.of(this).edit().putInt("challengeCompleted",completed).putString("mathTask","").putString("retypeTask","").putInt("challengeShakeCount",0).putInt("challengeShakeUnit",completed).putString("challengeShakeRun",occurrence).apply();shakes=0;selected=null;if(routine.at(completed)==null)dismiss();else build();}}
    private void memory(LinearLayout card,String expectedRun,int expected) {
        Ui.heading(card,"Find the matching pairs",24);List<String> values=new ArrayList<>(List.of("☀","☀","☽","☽","✿","✿"));Collections.shuffle(values,random);
        GridLayout grid=new GridLayout(this);grid.setColumnCount(3);card.addView(grid);final Button[] first={null};final String[] firstValue={null};final int[] pairs={0};final boolean[] waiting={false};
        for(String value:values) {
            Button b=new Button(this);b.setStateListAnimator(null);b.setElevation(0);b.setText("?");b.setContentDescription("Hidden memory card");b.setTextSize(25*Appearance.scale(this));b.setTextColor(Ui.BG);
            GridLayout.LayoutParams lp=new GridLayout.LayoutParams();lp.width=Ui.dp(this,76);lp.height=Ui.dp(this,68);lp.setMargins(3,3,3,3);grid.addView(b,lp);
            b.setOnClickListener(v->{if(waiting[0] || first[0]==b || !b.isEnabled())return;b.setText(value);b.setContentDescription(value);
                if(first[0]==null) { first[0]=b;firstValue[0]=value; }
                else if(value.equals(firstValue[0])) { first[0].setEnabled(false);b.setEnabled(false);first[0]=null;if(++pairs[0]==3)advance(expectedRun,expected); }
                else { waiting[0]=true;Button previous=first[0];first[0]=null;handler.postDelayed(()->{previous.setText("?");b.setText("?");previous.setContentDescription("Hidden memory card");b.setContentDescription("Hidden memory card");waiting[0]=false;},700); }
            });
        }
    }
    private void sequence(LinearLayout card,String expectedRun,int expectedUnit) {
        Ui.heading(card,"Watch. Remember. Repeat.",22);StringBuilder seq=new StringBuilder();for(int i=0;i<3+Math.min(3,position.round);i++)seq.append(1+random.nextInt(4));String expected=seq.toString();
        TextView prompt=Ui.heading(card,expected,32);handler.postDelayed(()->prompt.setText("Now repeat the sequence"),2500);
        StringBuilder entered=new StringBuilder();LinearLayout row=new LinearLayout(this);card.addView(row);
        for(int i=1;i<=4;i++) {int n=i;Button b=new Button(this);b.setStateListAnimator(null);b.setElevation(0);b.setText(""+i);b.setTextColor(Ui.BG);row.addView(b,new LinearLayout.LayoutParams(0,Ui.dp(this,56),1));
            b.setOnClickListener(v->{entered.append(n);prompt.setText(entered.toString());if(!expected.startsWith(entered.toString())){Ui.toast(this,"Watch once more");build();}else if(entered.toString().equals(expected))advance(expectedRun,expectedUnit);});}
        Ui.button(card,"Show sequence again",()->{entered.setLength(0);prompt.setText(expected);handler.postDelayed(()->prompt.setText("Now repeat the sequence"),2500);});
    }
    static String hex(byte[] value) {StringBuilder b=new StringBuilder();for(byte x:value)b.append(String.format(Locale.ROOT,"%02X",x));return b.toString();}
    private void chooseSnooze(){if(!alarm.inputs.chooseSnooze||alarm.inputsInvalid){snooze();return;}if(snoozeDialog!=null&&snoozeDialog.isShowing())return;String run=occurrence;int used=Prefs.number(this,"snoozes_"+alarm.id,0);if(!AlarmInputs.canSnooze(AlarmService.current(this,run),used,alarm.snoozeLimit))return;SnoozeOffer offer=new SnoozeOffer(run,used,alarm.snoozeMinutes,alarm.shortenSnoozes,alarm.snoozeLimit);int[] choices=offer.values();String[] labels=java.util.Arrays.stream(choices).mapToObj(n->n+" min").toArray(String[]::new);snoozeDialog=SleepPicker.choices(this,"Snooze for",labels,choices.length-1,n->{synchronized(Scheduler.class){int minutes=offer.select(n,occurrence,Prefs.number(this,"snoozes_"+alarm.id,0),AlarmService.current(this,occurrence),resumed,alarm.snoozeLimit,alarm.snoozeMinutes,alarm.shortenSnoozes);if(minutes>0)snooze(minutes);}});}
    private void snooze(){snooze(SnoozePolicy.minutes(alarm.snoozeMinutes,alarm.shortenSnoozes,Prefs.number(this,"snoozes_"+alarm.id,0)));}
    private void snooze(int minutes) {
        synchronized(Scheduler.class){if(!AlarmService.current(this,occurrence)){finish();return;}
        int used=Prefs.number(this,"snoozes_"+alarm.id,0);if(!AlarmInputs.canSnooze(true,used,alarm.snoozeLimit)||!SnoozePolicy.accepts(minutes,alarm.snoozeMinutes,alarm.shortenSnoozes,used))return;
        try { Prefs.put(this,"snoozed_"+alarm.id,alarm.json().toString());Scheduler.at(this,alarm.id,Scheduler.SNOOZE,System.currentTimeMillis()+minutes*60000L);
            Prefs.put(this,"snoozes_"+alarm.id,Prefs.number(this,"snoozes_"+alarm.id,0)+1);AlarmService.stop(this,occurrence);finish(); }
        catch(SecurityException e) {Ui.toast(this,e.getMessage());}
        }
    }
    private void dismiss() {
        synchronized(Scheduler.class){if(!AlarmService.current(this,occurrence)){finish();return;}
        if(alarm.awakeCheck>0 && alarm.id>0) {
            if(!WakeChecks.arm(this,alarm,occurrence))Ui.toast(this,"Wake-up check needs attention. Open the check from Tonight.");
        }
        boolean completedDismissal=routine!=null&&routine.at(completed)==null;
        if(AlarmService.stop(this,occurrence)&&completedDismissal&&alarm.id>0)Automations.emit(this,AutomationEvent.Kind.ALARM_DISMISSED,occurrence);
        Naps.dismissed(this,alarm,alarm.awakeCheck>0);
        try { stopLockTask(); } catch(IllegalStateException ignored) {}
        startActivity(new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));finish();
        }
    }
    @Override public void onSensorChanged(SensorEvent e) {
        if(!resumed||position==null||!position.task.type.equals("Shake")||sensorExpected!=completed||!sensorRun.equals(occurrence)||e.timestamp<sensorArmedNanos)return;
        double mag=Math.sqrt(e.values[0]*e.values[0]+e.values[1]*e.values[1]+e.values[2]*e.values[2]);long now=SystemClock.elapsedRealtime();
        if(mag>15 && now-lastShake>500) { lastShake=now;shakes++;progress.setVisibility(View.VISIBLE);progress.setText(shakes+" / "+target+" deliberate shakes");Prefs.of(this).edit().putString("challengeShakeRun",occurrence).putInt("challengeShakeUnit",completed).putInt("challengeShakeCount",shakes).apply();if(shakes>=target)advance(sensorRun,sensorExpected); }
    }
    @Override public void onAccuracyChanged(Sensor sensor,int accuracy){}
    @Override protected void onDestroy() {if(typedWake!=null)typedWake.close();if(snoozeDialog!=null)snoozeDialog.dismiss();if(flipSensor!=null)flipSensor.close();lightHandler.removeCallbacksAndMessages(null);closeWalk();closeSunrise();handler.removeCallbacksAndMessages(null);if(sensors!=null)sensors.unregisterListener(this);if(nfc!=null)nfc.disableReaderMode(this);super.onDestroy();}
    // Android 11/12 fallback only. Newer versions use the registered OnBackInvokedCallback.
    @android.annotation.SuppressLint("GestureBackNavigation")
    @Override public void onBackPressed() { Ui.toast(this,"Complete the challenge or snooze."); }
}
