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
    private Alarm alarm; private final Random random=new Random();private final Handler handler=new Handler(Looper.getMainLooper());
    private int completed=0,target=0,shakes=0;private String answer="";private long lastShake=0;
    private MathTask mathTask;private Integer selected;private TextView feedback;
    private SensorManager sensors;private TextView progress;private NfcAdapter nfc;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);setShowWhenLocked(true);setTurnScreenOn(true);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,()->Ui.toast(this,"Complete the challenge, snooze, or use emergency dismiss."));
        if(!Prefs.flag(this,"ringing")) { finish();return; }
        try { alarm=Alarm.read(new JSONObject(Prefs.text(this,"active_alarm","{}"))); } catch(JSONException e) { finish();return; }
        if(state!=null) { completed=state.getInt("completed");shakes=state.getInt("shakes"); }
        completed=Prefs.number(this,"challengeCompleted",completed);
        if(state!=null && state.containsKey("selected"))selected=state.getInt("selected");
        build();
        if(alarm.pin) new AlertDialog.Builder(this).setTitle("Keep the challenge on screen?").setMessage("Android screen pinning asks you to confirm. You can unpin using Android's navigation controls.").setPositiveButton("Pin",(d,w)->startLockTask()).setNegativeButton("Later",null).show();
    }
    @Override public void onNewIntent(Intent i) { super.onNewIntent(i); setIntent(i); if(!Prefs.flag(this,"ringing")) finish(); }
    @Override public void onSaveInstanceState(Bundle b) { super.onSaveInstanceState(b);b.putInt("completed",completed);b.putInt("shakes",shakes);if(selected!=null)b.putInt("selected",selected); }
    private void build() {
        handler.removeCallbacksAndMessages(null);if(sensors!=null)sensors.unregisterListener(this);
        LinearLayout body=Design.page(this,false,null,null);body.setPadding(0,0,0,0);
        ((View)body.getParent().getParent()).setBackground(new android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xffffc677,0xffffd893}));
        Design.space(body,32);TextView time=Design.text(body,java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("h:mm a",Locale.getDefault())),38,700,Design.INK);time.setGravity(Gravity.CENTER);
        Design.space(body,4);Design.text(body,"Time to wake up",21,600,Design.INK).setGravity(Gravity.CENTER);
        Design.space(body,20);Design.text(body,"Challenge "+(completed+1)+" of "+alarm.difficulty,14,500,Design.INK).setGravity(Gravity.CENTER);
        LinearLayout dots=Design.row(this);dots.setGravity(Gravity.CENTER);for(int n=0;n<alarm.difficulty;n++){View dot=new View(this);dot.setBackground(Design.shape(this,n<=completed?Design.BROWN:0xffe8ad65,8));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(Ui.dp(this,34),Ui.dp(this,12));lp.setMargins(Ui.dp(this,4),Ui.dp(this,10),Ui.dp(this,4),0);dots.addView(dot,lp);}body.addView(dots);
        body.addView(new DawnScene(this),new LinearLayout.LayoutParams(-1,Ui.dp(this,112)));
        LinearLayout card=Design.col(this);card.setBackground(Design.shape(this,Design.CREAM,32));Design.pad(card,24,28);body.addView(card,new LinearLayout.LayoutParams(-1,-2));
        progress=Design.text(card,"",14,500,Design.MUTED);progress.setVisibility(View.GONE);
        switch(alarm.challenge) {
            case "Shake":
                target=alarm.difficulty*5;Ui.heading(card,"Move into your morning",24);progress.setText(shakes+" / "+target+" deliberate shakes");
                Ui.text(card,"Hold the phone securely. A gentle shake counts at most once every half second.");
                sensors=getSystemService(SensorManager.class);Sensor accel=sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                if(accel!=null) sensors.registerListener(this,accel,SensorManager.SENSOR_DELAY_GAME);else Ui.text(card,"This phone has no accelerometer. Use emergency dismiss below.");break;
            case "Memory": memory(card);break;
            case "Sequence": sequence(card);break;
            case "NFC":
                Ui.heading(card,"Get up. Tap your tag.",24);Ui.text(card,"Use the NFC tag you enrolled in Bedtime settings. Keep it across the room.");
                nfc=NfcAdapter.getDefaultAdapter(this);
                if(nfc==null || !nfc.isEnabled()) Ui.text(card,"NFC unavailable or switched off. Emergency dismiss is available below.");
                else nfc.enableReaderMode(this,tag->{String id=hex(tag.getId());if(id.equals(Prefs.text(this,"nfcTag",""))) runOnUiThread(this::dismiss);else runOnUiThread(()->Ui.toast(this,"That is not your enrolled tag."));},NfcAdapter.FLAG_READER_NFC_A|NfcAdapter.FLAG_READER_NFC_B|NfcAdapter.FLAG_READER_NFC_F|NfcAdapter.FLAG_READER_NFC_V|NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,null);break;
            default:
                if(!alarm.challenge.equals("Retype")){math(card);break;}
                Design.text(card,"Retype to dismiss",22,600,Design.INK).setGravity(Gravity.CENTER);
                answer=Prefs.text(this,"retypeTask","");if(answer.isEmpty()){answer="MORNING "+(1000+random.nextInt(9000));Prefs.put(this,"retypeTask",answer);}
                Design.space(card,16);Design.text(card,answer,26,700,Design.INK).setGravity(Gravity.CENTER);
                EditText input=Ui.input(card,"Your answer","",InputType.TYPE_CLASS_TEXT);Ui.button(card,"Check answer",()->{if(input.getText().toString().trim().equals(answer))advance();else input.setError("Try again");},true);
        }
        Design.space(card,12);Design.text(card,"Alarm stays on until you finish.",13,400,Design.MUTED).setGravity(Gravity.CENTER);
        int used=Prefs.number(this,"snoozes_"+alarm.id,0);
        if(used<alarm.snoozeLimit)Design.button(card,"Snooze "+alarm.snoozeMinutes+" min · "+(alarm.snoozeLimit-used)+" left",false,this::snooze);
        Design.button(card,"Emergency dismiss",false,()->new AlertDialog.Builder(this).setTitle("Stop this alarm?")
            .setMessage("Use this if a challenge is inaccessible or something urgent needs your attention.").setPositiveButton("Stop alarm",(d,w)->{alarm.awakeCheck=0;dismiss();}).setNegativeButton("Keep going",null).show());
    }
    private void math(LinearLayout card){
        try{mathTask=MathTask.decode(Prefs.text(this,"mathTask",""));}catch(RuntimeException e){mathTask=MathTask.create(random);Prefs.put(this,"mathTask",mathTask.encode());}
        Design.text(card,"Solve to dismiss",22,600,Design.INK).setGravity(Gravity.CENTER);Design.space(card,12);
        TextView question=Design.text(card,mathTask.left+" + "+mathTask.right+" = ?",48,700,Design.INK);question.setGravity(Gravity.CENTER);question.setContentDescription("Math question "+mathTask.left+" plus "+mathTask.right);Design.space(card,20);
        for(int row=0;row<2;row++){LinearLayout r=Design.row(this);for(int col=0;col<2;col++){int option=mathTask.options.get(row*2+col);Button b=new Button(this);b.setStateListAnimator(null);b.setElevation(0);b.setText(""+option);b.setTextSize(38);b.setTypeface(Design.font(this,600));b.setTextColor(selected!=null&&selected==option?Design.CREAM:Design.INK);b.setSelected(selected!=null&&selected==option);b.setContentDescription("Answer "+option);b.setBackground(Design.touch(this,selected!=null&&selected==option?Design.BROWN:Design.SOFT,22));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,Ui.dp(this,78),1);lp.setMargins(Ui.dp(this,col==0?0:4),Ui.dp(this,4),Ui.dp(this,col==0?4:0),Ui.dp(this,4));r.addView(b,lp);b.setOnClickListener(v->{selected=option;build();});}card.addView(r);}
        feedback=Design.text(card,"Select an answer",13,400,Design.MUTED);feedback.setGravity(Gravity.CENTER);Design.space(card,6);
        Design.button(card,"Check answer",true,()->{if(selected==null){feedback.setText("Choose an answer first");return;}if(mathTask.accepts(selected)){advance();}else{feedback.setText("Try again · alarm is still ringing");feedback.setTextColor(0xffb33120);feedback.announceForAccessibility("Try again");}});
    }
    private void advance() {completed++;Prefs.put(this,"challengeCompleted",completed);Prefs.put(this,"mathTask","");Prefs.put(this,"retypeTask","");selected=null;if(completed>=alarm.difficulty)dismiss();else build();}
    private void memory(LinearLayout card) {
        Ui.heading(card,"Find the matching pairs",24);List<String> values=new ArrayList<>(List.of("☀","☀","☽","☽","✿","✿"));Collections.shuffle(values,random);
        GridLayout grid=new GridLayout(this);grid.setColumnCount(3);card.addView(grid);final Button[] first={null};final String[] firstValue={null};final int[] pairs={0};final boolean[] waiting={false};
        for(String value:values) {
            Button b=new Button(this);b.setStateListAnimator(null);b.setElevation(0);b.setText("?");b.setContentDescription("Hidden memory card");b.setTextSize(25);b.setTextColor(Ui.BG);
            GridLayout.LayoutParams lp=new GridLayout.LayoutParams();lp.width=Ui.dp(this,76);lp.height=Ui.dp(this,68);lp.setMargins(3,3,3,3);grid.addView(b,lp);
            b.setOnClickListener(v->{if(waiting[0] || first[0]==b || !b.isEnabled())return;b.setText(value);b.setContentDescription(value);
                if(first[0]==null) { first[0]=b;firstValue[0]=value; }
                else if(value.equals(firstValue[0])) { first[0].setEnabled(false);b.setEnabled(false);first[0]=null;if(++pairs[0]==3)advance(); }
                else { waiting[0]=true;Button previous=first[0];first[0]=null;handler.postDelayed(()->{previous.setText("?");b.setText("?");previous.setContentDescription("Hidden memory card");b.setContentDescription("Hidden memory card");waiting[0]=false;},700); }
            });
        }
    }
    private void sequence(LinearLayout card) {
        Ui.heading(card,"Watch. Remember. Repeat.",22);StringBuilder seq=new StringBuilder();for(int i=0;i<3+Math.min(3,completed);i++)seq.append(1+random.nextInt(4));String expected=seq.toString();
        TextView prompt=Ui.heading(card,expected,32);handler.postDelayed(()->prompt.setText("Now repeat the sequence"),2500);
        StringBuilder entered=new StringBuilder();LinearLayout row=new LinearLayout(this);card.addView(row);
        for(int i=1;i<=4;i++) {int n=i;Button b=new Button(this);b.setStateListAnimator(null);b.setElevation(0);b.setText(""+i);b.setTextColor(Ui.BG);row.addView(b,new LinearLayout.LayoutParams(0,Ui.dp(this,56),1));
            b.setOnClickListener(v->{entered.append(n);prompt.setText(entered.toString());if(!expected.startsWith(entered.toString())){Ui.toast(this,"Watch once more");build();}else if(entered.toString().equals(expected))advance();});}
        Ui.button(card,"Show sequence again",()->{entered.setLength(0);prompt.setText(expected);handler.postDelayed(()->prompt.setText("Now repeat the sequence"),2500);});
    }
    static String hex(byte[] value) {StringBuilder b=new StringBuilder();for(byte x:value)b.append(String.format(Locale.ROOT,"%02X",x));return b.toString();}
    private void snooze() {
        try { Prefs.put(this,"snoozed_"+alarm.id,alarm.json().toString());Scheduler.at(this,alarm.id,Scheduler.SNOOZE,System.currentTimeMillis()+alarm.snoozeMinutes*60000L);
            Prefs.put(this,"snoozes_"+alarm.id,Prefs.number(this,"snoozes_"+alarm.id,0)+1);AlarmService.stop(this);finish(); }
        catch(SecurityException e) {Ui.toast(this,e.getMessage());}
    }
    private void dismiss() {
        if(alarm.awakeCheck>0 && alarm.id>0) {
            try { Scheduler.at(this,alarm.id,Scheduler.CHECK,System.currentTimeMillis()+alarm.awakeCheck*60000L);Prefs.put(this,"checkPending",alarm.id);Prefs.put(this,"check_alarm",alarm.json().toString()); }
            catch(SecurityException e) { Ui.toast(this,"Wake-up check could not be scheduled."); }
        }
        AlarmService.stop(this);
        try { stopLockTask(); } catch(IllegalStateException ignored) {}
        startActivity(new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));finish();
    }
    @Override public void onSensorChanged(SensorEvent e) {
        double mag=Math.sqrt(e.values[0]*e.values[0]+e.values[1]*e.values[1]+e.values[2]*e.values[2]);long now=SystemClock.elapsedRealtime();
        if(mag>15 && now-lastShake>500) { lastShake=now;shakes++;progress.setVisibility(View.VISIBLE);progress.setText(shakes+" / "+target+" deliberate shakes");if(shakes>=target)dismiss(); }
    }
    @Override public void onAccuracyChanged(Sensor sensor,int accuracy){}
    @Override protected void onDestroy() {handler.removeCallbacksAndMessages(null);if(sensors!=null)sensors.unregisterListener(this);if(nfc!=null)nfc.disableReaderMode(this);super.onDestroy();}
    // Android 11/12 fallback only. Newer versions use the registered OnBackInvokedCallback.
    @android.annotation.SuppressLint("GestureBackNavigation")
    @Override public void onBackPressed() { Ui.toast(this,"Complete the challenge, snooze, or use emergency dismiss."); }
}
