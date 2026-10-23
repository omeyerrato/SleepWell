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
    private MathTask mathTask;private Integer selected;private TextView feedback;
    private SensorManager sensors;private TextView progress;private NfcAdapter nfc;
    private final Handler lightHandler=new Handler(Looper.getMainLooper());private AlarmSunrise sunrise;private String occurrence="";private long lightStarted;private boolean resumed;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);setShowWhenLocked(true);setTurnScreenOn(true);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,()->Ui.toast(this,"Complete the challenge or snooze."));
        if(!Prefs.flag(this,"ringing")) { finish();return; }
        try { alarm=Alarm.read(new JSONObject(Prefs.text(this,"active_alarm","{}"))); } catch(JSONException e) { finish();return; }
        routine=alarm.challenges();occurrence=Prefs.text(this,"alarmRun","");
        if(state!=null) { completed=state.getInt("completed");shakes=state.getInt("shakes"); }
        completed=Math.max(0,Prefs.number(this,"challengeCompleted",completed));
        if(state!=null && state.containsKey("selected"))selected=state.getInt("selected");
        restoreShakes();build();configureSunrise();
        if(alarm.pin) new AlertDialog.Builder(this).setTitle("Keep the challenge on screen?").setMessage("Android screen pinning asks you to confirm. You can unpin using Android's navigation controls.").setPositiveButton("Pin",(d,w)->startLockTask()).setNegativeButton("Later",null).show();
    }
    @Override public void onNewIntent(Intent i) {super.onNewIntent(i);setIntent(i);if(!Prefs.flag(this,"ringing")){finish();return;}if(!occurrence.equals(Prefs.text(this,"alarmRun",""))){if(nfc!=null){nfc.disableReaderMode(this);nfc=null;}try{alarm=Alarm.read(new JSONObject(Prefs.text(this,"active_alarm","{}")));}catch(JSONException error){finish();return;}routine=alarm.challenges();completed=Math.max(0,Prefs.number(this,"challengeCompleted",0));occurrence=Prefs.text(this,"alarmRun","");restoreShakes();selected=null;build();configureSunrise();if(resumed)lightTick.run();}}
    private void configureSunrise(){lightHandler.removeCallbacksAndMessages(null);closeSunrise();occurrence=Prefs.text(this,"alarmRun","");lightStarted=Prefs.of(this).getLong("alarmStartedElapsed",SystemClock.elapsedRealtime());final float original=getWindow().getAttributes().screenBrightness;sunrise=new AlarmSunrise(new AlarmWakePolicy(alarm),new AlarmSunrise.Port(){public void brightness(float value){WindowManager.LayoutParams attrs=getWindow().getAttributes();attrs.screenBrightness=value;getWindow().setAttributes(attrs);}public void restore(){WindowManager.LayoutParams attrs=getWindow().getAttributes();attrs.screenBrightness=original;getWindow().setAttributes(attrs);}});}
    private void closeSunrise(){if(sunrise!=null)try{sunrise.close();}catch(RuntimeException error){Prefs.put(this,"lastError","The alarm window brightness could not be restored.");}}
    private long lightElapsed(){return Math.max(0,SystemClock.elapsedRealtime()-lightStarted);}
    private final Runnable lightTick=new Runnable(){public void run(){if(!resumed||sunrise==null)return;try{if(!AlarmService.current(AlarmActivity.this,occurrence)){sunrise.pause();return;}sunrise.resume(lightElapsed());lightHandler.postDelayed(this,250);}catch(RuntimeException error){closeSunrise();Prefs.put(AlarmActivity.this,"lastError","Screen sunrise is unavailable. Sound and the challenge continue.");}}};
    @Override protected void onResume(){super.onResume();resumed=true;if(position!=null&&(position.task.type.equals("Shake")||position.task.type.equals("NFC")||position.task.type.equals("Walk")))build();lightTick.run();}
    @Override protected void onPause(){resumed=false;closeWalk();if(sensors!=null)sensors.unregisterListener(this);if(nfc!=null)nfc.disableReaderMode(this);lightHandler.removeCallbacksAndMessages(null);if(sunrise!=null)try{sunrise.pause();}catch(RuntimeException error){closeSunrise();}super.onPause();}
    @Override public void onSaveInstanceState(Bundle b) { super.onSaveInstanceState(b);b.putInt("completed",completed);b.putInt("shakes",shakes);if(selected!=null)b.putInt("selected",selected); }
    private void build() {
        closeWalk();handler.removeCallbacksAndMessages(null);if(sensors!=null)sensors.unregisterListener(this);if(nfc!=null){nfc.disableReaderMode(this);nfc=null;}position=routine.at(completed);if(position==null){dismiss();return;}final int expected=completed;final String expectedRun=occurrence;sensorExpected=expected;sensorRun=expectedRun;sensorArmedNanos=SystemClock.elapsedRealtimeNanos();
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
            case "Walk": walk(card,expectedRun,expected);break;
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
        if(used<alarm.snoozeLimit)Design.button(card,"Snooze "+SnoozePolicy.minutes(alarm.snoozeMinutes,alarm.shortenSnoozes,used)+" min · "+(alarm.snoozeLimit-used)+" left",false,this::snooze);
        if((getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE)!=0)Design.button(card,"Developer stop · debug build",false,()->new AlertDialog.Builder(this).setTitle("Stop this development alarm?")
            .setMessage("Developer control for this debug build. It is hidden in release builds.").setPositiveButton("Stop alarm",(d,w)->{alarm.awakeCheck=0;dismiss();}).setNegativeButton("Keep going",null).show());
    }
    private void math(LinearLayout card,String expectedRun,int expected){
        try{mathTask=MathTask.decode(Prefs.text(this,"mathTask",""));}catch(RuntimeException e){mathTask=MathTask.create(random);Prefs.put(this,"mathTask",mathTask.encode());}
        Design.text(card,"Solve to dismiss",22,600,Design.INK).setGravity(Gravity.CENTER);Design.space(card,12);
        TextView question=Design.text(card,mathTask.left+" + "+mathTask.right+" = ?",48,700,Design.INK);question.setGravity(Gravity.CENTER);question.setContentDescription("Math question "+mathTask.left+" plus "+mathTask.right);Design.space(card,20);
        for(int row=0;row<2;row++){LinearLayout r=Design.row(this);for(int col=0;col<2;col++){int option=mathTask.options.get(row*2+col);Button b=new Button(this);b.setStateListAnimator(null);b.setElevation(0);b.setText(""+option);b.setTextSize(38*Appearance.scale(this));b.setTypeface(Design.font(this,600));b.setTextColor(selected!=null&&selected==option?Design.CREAM:Design.INK);b.setSelected(selected!=null&&selected==option);b.setContentDescription("Answer "+option);b.setBackground(Design.touch(this,selected!=null&&selected==option?Design.BROWN:Design.SOFT,22));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,Ui.dp(this,78),1);lp.setMargins(Ui.dp(this,col==0?0:4),Ui.dp(this,4),Ui.dp(this,col==0?4:0),Ui.dp(this,4));r.addView(b,lp);b.setOnClickListener(v->{selected=option;build();});}card.addView(r);}
        feedback=Design.text(card,"Select an answer",13,400,Design.MUTED);feedback.setGravity(Gravity.CENTER);Design.space(card,6);
        Design.button(card,"Check answer",true,()->{if(selected==null){feedback.setText("Choose an answer first");return;}if(mathTask.accepts(selected)){advance(expectedRun,expected);}else{feedback.setText("Try again · alarm is still ringing");feedback.setTextColor(0xffb33120);feedback.announceForAccessibility("Try again");}});
    }
    private void closeWalk(){if(walk!=null){walk.close();walk=null;}}
    private void walk(LinearLayout card,String expectedRun,int expected){Design.text(card,"Walk into your morning",22,600,Design.INK);Design.space(card,12);int target=position.task.targetSteps;int earned=expectedRun.equals(Prefs.text(this,"walkRun",""))&&Prefs.number(this,"walkUnit",-1)==expected?Prefs.number(this,"walkEarned",0):0;TextView count=Design.text(card,Math.max(0,Math.min(target,earned))+" / "+target+" steps",30,700,Design.INK);Design.text(card,"Carry your phone. Walk in a safe, clear space.",13,400,Design.MUTED);if(!resumed)return;
        WalkSession.Port source;try{source=WalkPlatform.factory.create(this);}catch(RuntimeException unavailable){Design.text(card,"Step counting is unavailable. Solve math instead.",14,500,Design.MUTED);math(card,expectedRun,expected);return;}walk=new WalkSession(source,new WalkSession.Effects(){public boolean current(String run,int unit){return resumed&&run.equals(occurrence)&&unit==completed&&AlarmService.current(AlarmActivity.this,run);}public void progress(String run,int unit,int value){if(!current(run,unit))return;Prefs.of(AlarmActivity.this).edit().putString("walkRun",run).putInt("walkUnit",unit).putInt("walkEarned",value).apply();count.setText(value+" / "+target+" steps");}public void solved(String run,int unit){advance(run,unit);}},expectedRun,expected,target,earned);WalkSession session=walk;session.resume();if(walk!=session)return;String unavailable=switch(session.state()){case PERMISSION->"Physical activity access is off. Solve math instead.";case UNSUPPORTED->"This phone has no supported step sensor. Solve math instead.";case ERROR->"Step counting could not start. Solve math instead.";default->"";};if(!unavailable.isEmpty()){Design.space(card,12);Design.text(card,unavailable,14,500,Design.MUTED);math(card,expectedRun,expected);}}
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
    private void snooze() {
        synchronized(Scheduler.class){if(!AlarmService.current(this,occurrence)){finish();return;}
        try { Prefs.put(this,"snoozed_"+alarm.id,alarm.json().toString());Scheduler.at(this,alarm.id,Scheduler.SNOOZE,System.currentTimeMillis()+SnoozePolicy.minutes(alarm.snoozeMinutes,alarm.shortenSnoozes,Prefs.number(this,"snoozes_"+alarm.id,0))*60000L);
            Prefs.put(this,"snoozes_"+alarm.id,Prefs.number(this,"snoozes_"+alarm.id,0)+1);AlarmService.stop(this,occurrence);finish(); }
        catch(SecurityException e) {Ui.toast(this,e.getMessage());}
        }
    }
    private void dismiss() {
        synchronized(Scheduler.class){if(!AlarmService.current(this,occurrence)){finish();return;}
        if(alarm.awakeCheck>0 && alarm.id>0) {
            try { Scheduler.at(this,alarm.id,Scheduler.CHECK,System.currentTimeMillis()+alarm.awakeCheck*60000L);Prefs.put(this,"checkPending",alarm.id);Prefs.put(this,"check_alarm",alarm.json().toString()); }
            catch(SecurityException e) { Ui.toast(this,"Wake-up check could not be scheduled."); }
        }
        AlarmService.stop(this,occurrence);
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
    @Override protected void onDestroy() {lightHandler.removeCallbacksAndMessages(null);closeWalk();closeSunrise();handler.removeCallbacksAndMessages(null);if(sensors!=null)sensors.unregisterListener(this);if(nfc!=null)nfc.disableReaderMode(this);super.onDestroy();}
    // Android 11/12 fallback only. Newer versions use the registered OnBackInvokedCallback.
    @android.annotation.SuppressLint("GestureBackNavigation")
    @Override public void onBackPressed() { Ui.toast(this,"Complete the challenge or snooze."); }
}
