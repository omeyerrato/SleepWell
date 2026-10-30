package org.sleepwell.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.util.concurrent.atomic.AtomicBoolean;

/** Visible room preview only. Opening/rotation never starts the optional microphone or writes a record. */
public final class RoomConditionsActivity extends ComponentActivity {
    interface Access {
        boolean microphone(Context context);
        default boolean rationale(Activity owner){return owner.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO);}
        default void request(Activity owner,ActivityResultLauncher<String> launcher){launcher.launch(Manifest.permission.RECORD_AUDIO);}
    }
    static RoomLightPort.Factory lights=RoomLightPort.PLATFORM;
    static RoomSoundLevel.Factory microphones=RoomAudio::open;
    static Access access=context->context.checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED;
    private static final String ASKED="roomMicrophoneAsked";
    private final Handler main=new Handler(Looper.getMainLooper());
    private RoomLightPort light;private RoomSoundLevel.Factory microphoneFactory;private Access permissions;
    private RoomLightHistory history;private RoomConditionsUi.LightChart chart;private RoomConditionsUi.SoundMeter meter;
    private RoomDimming dimming;private RoomSoundLevel sound;private AtomicBoolean admission=new AtomicBoolean();
    private TextView luxValue,lightStatus,soundValue,soundStatus,savedStatus;private Switch microphoneSwitch;private Button cleanup;
    private boolean resumed,lightStarted,updating,dimDraft,requesting;private Float lux;private String lightIssue="",message="";private AlertDialog rationale;
    private int permissionChoice,pendingPermissionChoice;private boolean requestAfterRationale;
    private ActivityResultLauncher<String> microphonePermission;
    private final Runnable tick=new Runnable(){public void run(){if(!resumed)return;update();main.postDelayed(this,250);}};

    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);RecordUi.review(this);microphoneFactory=microphones;permissions=access;dimming=new RoomDimming(this);
        history=new RoomLightHistory(SystemClock.elapsedRealtime(),saved);dimDraft=saved==null?RoomDimming.enabled(this):saved.getBoolean("roomDimDraft",RoomDimming.enabled(this));requesting=saved!=null&&saved.getBoolean("roomPermissionPending");
        try{light=lights.create(getApplicationContext());}catch(RuntimeException unavailable){lightIssue="Light sensor unavailable on this device.";}
        microphonePermission=registerForActivityResult(new ActivityResultContracts.RequestPermission(),granted->{requesting=false;message=granted?"Microphone allowed. Tap Microphone analysis to measure.":"Microphone access is off. Tap Microphone analysis to review access.";update();});
        render();if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::finish);
    }
    private void render(){
        LinearLayout body=Design.darkPage(this);body.setLayoutParams(new ScrollView.LayoutParams(-1,-2));Design.pad(body,20,12);
        LinearLayout header=Design.row(this);View back=Design.iconButton(this,"back","Back",this::finish);((Design.Icon)back).tint(Design.CREAM);header.addView(back,new LinearLayout.LayoutParams(Design.dp(this,48),Design.dp(this,48)));header.addView(Design.text(this,"Room conditions",25,650,Design.CREAM),new LinearLayout.LayoutParams(0,-2,1));body.addView(header);Design.space(body,12);
        Design.text(body,"See what your phone’s sensors detect in your sleep environment.",14,400,Design.LILAC);Design.space(body,12);
        LinearLayout lightCard=card(body);LinearLayout lightHeading=Design.row(this);icon(lightHeading,"bulb");lightHeading.addView(Design.text(this,"Ambient light",16,600,Design.CREAM),new LinearLayout.LayoutParams(0,-2,1));luxValue=Design.text(this,"—",28,600,Design.CREAM);luxValue.setContentDescription("Ambient light unavailable");lightHeading.addView(luxValue);lightCard.addView(lightHeading);lightStatus=Design.text(lightCard,"Waiting for light readings",12,400,Design.LILAC);lightStatus.setGravity(Gravity.END);lightStatus.setPadding(0,Design.dp(this,5),0,0);Design.space(lightCard,12);Design.text(lightCard,"Light while this screen is open",13,500,Design.CREAM);Design.space(lightCard,6);chart=new RoomConditionsUi.LightChart(this,history);lightCard.addView(chart,new LinearLayout.LayoutParams(-1,Design.dp(this,120)));
        Design.space(body,10);LinearLayout soundCard=card(body),soundHeading=Design.row(this);icon(soundHeading,"wave");soundHeading.addView(Design.text(this,"Sound level",16,600,Design.CREAM),new LinearLayout.LayoutParams(0,-2,1));soundValue=Design.text(this,"Not measuring",20,600,Design.CREAM);soundValue.setGravity(Gravity.END);soundValue.setMaxWidth(Design.dp(this,150));soundHeading.addView(soundValue);soundCard.addView(soundHeading);Design.space(soundCard,14);Design.text(soundCard,"Relative level (RMS)",12,400,Design.LILAC);Design.space(soundCard,10);meter=new RoomConditionsUi.SoundMeter(this);soundCard.addView(meter,new LinearLayout.LayoutParams(-1,Design.dp(this,18)));Design.space(soundCard,5);LinearLayout ends=Design.row(this);ends.addView(Design.text(this,"0% · Low",12,400,Design.LILAC),new LinearLayout.LayoutParams(0,-2,1));ends.addView(Design.text(this,"High · 100%",12,400,Design.LILAC));soundCard.addView(ends);Design.space(soundCard,8);rule(soundCard);
        microphoneSwitch=toggle(soundCard,"mic","Microphone analysis","Live level only · no audio saved",false,on->{if(updating)return;if(on)requestSound();else{stopSound();message="Not measuring. Microphone is off.";update();}});
        soundStatus=Design.text(soundCard,"Microphone is off. Nothing is recorded.",12,400,Design.LILAC);soundStatus.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        cleanup=Design.button(soundCard,"Retry microphone cleanup",false,()->{RoomSoundLevel owner=RoomSoundLevel.current();if(owner!=null)owner.retryCleanup();update();});cleanup.setBackground(Design.outline(this,0xff342b40,0xff8c7c9d,14));cleanup.setTextColor(Design.CREAM);cleanup.setVisibility(View.GONE);
        Design.space(soundCard,8);rule(soundCard);toggle(soundCard,"sun","Screen dimming","Dim Room conditions and Wind-down",dimDraft,on->dimDraft=on);
        LinearLayout footer=RecordUi.footer(body);savedStatus=Design.text(footer,"",12,400,Design.LILAC);savedStatus.setVisibility(View.GONE);Button save=Design.button(footer,"Use bedtime dimming",true,()->{if(!resumed||!hasWindowFocus())return;if(RoomDimming.save(this,dimDraft)){savedStatus.setText(dimDraft?"Screen dimming saved for Room conditions and Wind-down.":"Screen dimming is off.");dimming.apply(true);}else savedStatus.setText("Could not save dimming. Your previous setting was kept.");savedStatus.setVisibility(View.VISIBLE);});save.setBackground(Design.touch(this,Design.LILAC,16));save.setTextColor(Design.PLUM);update();
    }
    private LinearLayout card(LinearLayout body){LinearLayout card=Design.col(this);Design.pad(card,16,12);card.setBackground(Design.shape(this,0xff342b40,20));body.addView(card,new LinearLayout.LayoutParams(-1,-2));return card;}
    private void icon(LinearLayout row,String name){LinearLayout.LayoutParams size=new LinearLayout.LayoutParams(Design.dp(this,25),Design.dp(this,25));size.rightMargin=Design.dp(this,14);row.addView(new Design.Icon(this,name,Design.CREAM),size);}
    private void rule(LinearLayout body){View line=new View(this);line.setBackgroundColor(0xff62566d);body.addView(line,new LinearLayout.LayoutParams(-1,Design.dp(this,1)));}
    private Switch toggle(LinearLayout body,String glyph,String title,String description,boolean checked,java.util.function.Consumer<Boolean> changed){
        LinearLayout row=Design.row(this);row.setMinimumHeight(Design.dp(this,64));row.setPadding(0,Design.dp(this,8),0,Design.dp(this,8));icon(row,glyph);LinearLayout copy=Design.col(this);Design.text(copy,title,15,500,Design.CREAM);Design.space(copy,4);Design.text(copy,description,12,400,Design.LILAC);row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));InteractionFeedback.Toggle control=new InteractionFeedback.Toggle(this);control.setContentDescription(title);control.setMinHeight(Design.dp(this,48));control.setMinWidth(Design.dp(this,48));Design.styleSwitch(this,control,Design.VIOLET,0xff6c6078);control.setChecked(checked);control.setOnCheckedChangeListener((button,on)->{control.userChanged();changed.accept(on);});row.addView(control);body.addView(row);return control;
    }
    private boolean foreground(){return resumed&&!isFinishing()&&!isDestroyed()&&hasWindowFocus();}
    private boolean microphoneAllowed(){try{return permissions.microphone(this);}catch(RuntimeException denied){return false;}}
    private void requestSound(){
        updating=true;microphoneSwitch.setChecked(false);updating=false;if(!foreground()||requesting)return;
        RoomSoundLevel owner=RoomSoundLevel.current();if(owner!=null){message=owner.state==RoomSoundLevel.State.BLOCKED?owner.issue:"A previous microphone preview is still closing. Wait, then tap again.";update();return;}
        if(TrackingService.running||AlarmService.active||Prefs.flag(this,"ringing")||MicrophoneLease.occupied()){message="Microphone is in use. End the other capture or alarm before measuring.";update();return;}
        if(microphoneAllowed()){message="";admission=new AtomicBoolean(true);sound=RoomSoundLevel.start(getApplicationContext(),microphoneFactory,admission);if(sound==null){admission.set(false);message="A microphone preview is still closing. Tap again after cleanup.";}update();return;}
        if(permissions.rationale(this)){
            final int choice=++permissionChoice;requestAfterRationale=false;
            AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Microphone for room sound").setMessage("Allow an optional live sound-level preview while this screen is visible. Audio is not saved, classified or uploaded. You can keep it off.").setPositiveButton("Continue",(prompt,which)->{if(rationale!=prompt||!resumed||isFinishing()||isDestroyed()||choice!=permissionChoice||!rationale.getWindow().getDecorView().hasWindowFocus())return;pendingPermissionChoice=choice;requestAfterRationale=true;}).setNegativeButton("Not now",null).create();
            rationale=dialog;dialog.setOnCancelListener(ignored->cancelPermissionChoice());dialog.setOnDismissListener(ignored->{if(rationale!=dialog)return;rationale=null;if(!requestAfterRationale||pendingPermissionChoice!=permissionChoice)return;main.post(this::continuePermissionChoice);});dialog.show();ReportDialogs.style(dialog);
        }else if(Prefs.flag(this,ASKED)||Prefs.flag(this,"readinessMicrophoneAsked")||Prefs.flag(this,"snoreCueMicrophoneAsked")){
            message="Allow microphone access in Android settings, then tap Microphone analysis to measure.";try{startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,android.net.Uri.parse("package:"+getPackageName())));}catch(RuntimeException unavailable){message="Android could not open microphone settings. Try again later.";}update();
        }else askMicrophone();
    }
    private void cancelPermissionChoice(){permissionChoice++;requestAfterRationale=false;}
    private void continuePermissionChoice(){if(!requestAfterRationale||pendingPermissionChoice!=permissionChoice||rationale!=null||!foreground())return;requestAfterRationale=false;askMicrophone();}
    private void askMicrophone(){if(!foreground()||requesting)return;if(microphoneAllowed()){message="Microphone allowed. Tap Microphone analysis to measure.";update();return;}boolean previous=Prefs.flag(this,ASKED);try{Prefs.put(this,ASKED,true);requesting=true;permissions.request(this,microphonePermission);}catch(RuntimeException unavailable){requesting=false;Prefs.put(this,ASKED,previous);message="Android could not request microphone access. Your microphone remains off.";}update();}
    private void stopSound(){admission.set(false);if(sound!=null)sound.cancel();}
    private void startLight(){
        if(lightStarted||!foreground())return;
        if(light==null||!light.available()){lightIssue="Light sensor unavailable on this device.";update();return;}
        lightIssue="";lightStarted=true;
        try{boolean started=light.begin((at,value,reliable)->{if(!lightStarted||!foreground())return;history.sample(at,value,reliable,SystemClock.elapsedRealtime());lux=reliable&&Float.isFinite(value)&&value>=0?value:null;lightIssue=lux==null?"Light reading is unreliable. Keep the sensor uncovered.":"";update();});if(!started){lightStarted=false;lightIssue="Light sensor could not start. Reopen this screen to try again.";}}
        catch(RuntimeException unavailable){lightStarted=false;lightIssue="Light sensor could not start. Reopen this screen to try again.";try{light.close();}catch(RuntimeException ignored){}}
        update();
    }
    private void stopLight(){lightStarted=false;lux=null;if(light!=null)try{light.close();}catch(RuntimeException unavailable){lightIssue="Light sensor cleanup needs attention. Reopen SleepWell.";}}
    private void update(){
        if(luxValue==null)return;long now=SystemClock.elapsedRealtime();luxValue.setText(lux==null?"—":RoomConditionsUi.number(lux)+" lx");luxValue.setContentDescription(lux==null?"No current light reading":"Ambient light "+RoomConditionsUi.number(lux)+" lux");lightStatus.setText(!lightIssue.isEmpty()?lightIssue:lux==null?"Waiting for light readings":"Measured by the phone’s light sensor");chart.time(now);
        RoomSoundLevel owner=RoomSoundLevel.current();boolean live=sound!=null&&(sound.state==RoomSoundLevel.State.STARTING||sound.state==RoomSoundLevel.State.MEASURING)&&admission.get()&&foreground();Double reading=live&&sound.sampledAt>0&&now-sound.sampledAt<=1500?sound.level:null;
        updating=true;microphoneSwitch.setChecked(live);microphoneSwitch.setEnabled(!requesting&&!(owner!=null&&owner.state==RoomSoundLevel.State.CLOSING));updating=false;
        soundValue.setText(reading!=null?RoomConditionsUi.number(reading*100)+"%":live?"Waiting":"Not measuring");meter.level(reading);
        String issue=owner!=null&&owner.state==RoomSoundLevel.State.BLOCKED?owner.issue:sound!=null&&!sound.issue.isEmpty()?sound.issue:message;
        soundStatus.setText(issue);soundStatus.setVisibility(issue.isEmpty()?View.GONE:View.VISIBLE);cleanup.setVisibility(owner!=null&&owner.state==RoomSoundLevel.State.BLOCKED?View.VISIBLE:View.GONE);
    }
    @Override protected void onResume(){super.onResume();resumed=true;main.post(tick);if(hasWindowFocus()){startLight();dimming.apply(true);}}
    @Override protected void onPause(){resumed=false;cancelPermissionChoice();main.removeCallbacksAndMessages(null);if(rationale!=null)rationale.dismiss();stopSound();stopLight();dimming.restore();super.onPause();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(dimming==null)return;if(focused&&resumed){Design.focusedSystemBars(this,true);startLight();dimming.apply(true);continuePermissionChoice();}else{stopSound();stopLight();dimming.restore();}update();}
    @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);history.save(out);out.putBoolean("roomDimDraft",dimDraft);out.putBoolean("roomPermissionPending",requesting);}
    @Override protected void onDestroy(){main.removeCallbacksAndMessages(null);stopSound();stopLight();dimming.restore();if(rationale!=null)rationale.dismiss();super.onDestroy();}
}
