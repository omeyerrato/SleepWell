package org.sleepwell.app;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Original English phrase task and preview. Opening, permission return and recreation never listen. */
public final class PhraseChallengeActivity extends Activity {
    static final int CHALLENGE=819,FALLBACK=4,PERMISSION=820;
    String run,request;int unit,rounds;boolean preview;private boolean resumed,attemptActive,returned,done,math;private PhraseRecognition operation;
    private final Handler main=new Handler(Looper.getMainLooper());private final Runnable changed=this::render;private String issue="",shown="";
    static Intent preview(Activity activity,int rounds){PhraseRule.rounds(rounds);return RecordUi.intent(activity,PhraseChallengeActivity.class).putExtra("mode","preview").putExtra("rounds",rounds).putExtra("request",UUID.randomUUID().toString());}
    static Intent challenge(Activity activity,String run,int unit,int rounds,String request){return RecordUi.intent(activity,PhraseChallengeActivity.class).putExtra("mode","challenge").putExtra("run",run).putExtra("unit",unit).putExtra("rounds",rounds).putExtra("request",request);}
    @Override public void onCreate(Bundle saved){super.onCreate(saved);RecordUi.review(this);
        try{Intent input=getIntent();if(input.getData()!=null||input.getClipData()!=null||input.getSelector()!=null)throw new IllegalArgumentException();String mode=input.getStringExtra("mode");if(!Set.of("preview","challenge").contains(mode))throw new IllegalArgumentException();preview=mode.equals("preview");request=input.getStringExtra("request");if(request==null||!UUID.fromString(request).toString().equals(request))throw new IllegalArgumentException();rounds=input.getIntExtra("rounds",-1);PhraseRule.rounds(rounds);run=preview?request:input.getStringExtra("run");unit=preview?saved==null?0:saved.getInt("previewUnit",0):input.getIntExtra("unit",-1);PhraseRule.target(run,unit);if(preview&&unit>=rounds)throw new IllegalArgumentException();if(!taskCurrent())throw new IllegalArgumentException();if(saved!=null){done=saved.getBoolean("done");math=saved.getBoolean("math");issue=saved.getString("issue","");}}
        catch(RuntimeException invalid){setResult(RESULT_CANCELED);finish();return;}
        if(!preview){setShowWhenLocked(true);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);}operation=PhraseRecognition.existing(run,unit,rounds,changed);render();
    }
    private boolean taskCurrent(){if(preview)return run!=null&&run.equals(request)&&unit>=0&&unit<rounds;try{if(!AlarmService.current(this,run)||Prefs.number(this,"challengeCompleted",-1)!=unit)return false;ChallengeRoutine.Position position=Alarm.read(new org.json.JSONObject(Prefs.text(this,"active_alarm","{}"))).challenges().at(unit);return position!=null&&position.task.type.equals(PhraseRule.TYPE)&&position.task.rounds==rounds;}catch(Exception invalid){return false;}}
    boolean admission(){return resumed&&hasWindowFocus()&&!isFinishing()&&!isDestroyed()&&taskCurrent();}
    private boolean allowed(){return checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED;}
    private boolean settings(){return !allowed()&&getPreferences(0).getBoolean("phrasePermissionRequested",false)&&!shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO);}
    private void permission(){if(!admission())return;try{if(settings())startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName())));else{getPreferences(0).edit().putBoolean("phrasePermissionRequested",true).apply();requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},PERMISSION);}}catch(RuntimeException denied){issue="Microphone settings could not open. Use Math instead.";render();}}
    private String signature(){return (operation==null?"IDLE":operation.state+":"+operation.error+":"+operation.heard)+":"+allowed()+":"+settings()+":"+issue+":"+unit+":"+done+":"+math;}
    private void accepted(){attemptActive=false;if(preview){operation=null;math=false;if(unit+1<rounds)unit++;else done=true;render();}else result(RESULT_OK);}
    private void render(){
        if(isFinishing()||isDestroyed())return;if(!taskCurrent()){cancel();finish();return;}
        if(operation!=null&&operation.state==PhraseRecognition.State.MATCHED&&operation.released()&&attemptActive&&admission()){accepted();return;}
        shown=signature();LinearLayout body=Design.page(this,false,null,null);RecordUi.label(this,body);RecordUi.header(body,preview?"Spoken phrase preview":"Say the morning phrase",this::finish);Design.space(body,16);
        if(done){Design.text(body,"All phrase rounds completed",25,600,Design.INK);Design.text(body,"Text matching only; no identity or liveness check.",13,400,Design.MUTED);Design.button(body,"Try again",true,()->{unit=0;done=math=false;operation=null;issue="";render();});return;}
        if(math){WakePuzzleView.math(body,run,unit,0,this::admission,()->{if(preview)accepted();else result(FALLBACK);});return;}
        if(preview)Design.text(body,"Round "+(unit+1)+" of "+rounds,13,500,Design.MUTED);
        Design.text(body,PhraseRule.target(run,unit),27,600,Design.BROWN);Design.space(body,12);Design.text(body,"Read these words aloud in English. Only the top final recognized text can match; this does not verify who is speaking.",14,400,Design.MUTED);Design.space(body,18);
        PhraseRecognition.State state=operation==null?null:operation.state;String status=state==null?"Microphone stays off until Listen":switch(state){case CHECKING->"Checking installed offline English…";case QUIETING->"Waiting for this alarm’s sound to stop…";case LISTENING->"Listening · up to 15 seconds";case CLOSING->"Closing the on-device recognizer…";case BLOCKED->"Recognizer cleanup needs attention";case MATCHED->attemptActive?"Phrase matched":"Previous attempt ended. Listen again.";case ERROR,CANCELLED->operation.error.isEmpty()?"Listening stopped":operation.error;};
        LinearLayout panel=Design.col(this);Design.pad(panel,18,20);panel.setBackground(Design.shape(this,Design.SOFT,18));TextView live=Design.text(panel,status,16,600,Design.BROWN);live.setContentDescription("Phrase recognition status");live.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);if(operation!=null&&!operation.heard.isEmpty()){Design.space(panel,8);Design.text(panel,"Heard: "+operation.heard,13,400,Design.MUTED);}body.addView(panel);Design.space(body,16);
        boolean busy=operation!=null&&!operation.released();String unavailable=busy?"":PhraseRecognition.unavailable(this,preview);if(!issue.isEmpty())Design.text(body,issue,13,500,NightDialTokens.NEGATIVE);else if(!unavailable.isEmpty())Design.text(body,unavailable,13,500,Design.MUTED);
        if(!allowed())Design.button(body,settings()?"Open microphone settings":"Allow microphone",false,this::permission);
        if(state==PhraseRecognition.State.BLOCKED)Design.button(body,"Retry recognizer cleanup",true,()->operation.retry());else if(busy)Design.button(body,"Stop listening",false,this::cancel);else{Button listen=Design.button(body,"Listen to phrase",true,()->{if(!admission())return;issue="";attemptActive=true;try{operation=PhraseRecognition.begin(this,changed);}catch(RuntimeException unavailableFailure){attemptActive=false;issue=unavailableFailure.getMessage()==null?"Listening unavailable. Use Math instead.":unavailableFailure.getMessage();}render();});RecordUi.enabled(listen,allowed()&&unavailable.isEmpty());}
        Design.space(body,8);Button alternative=Design.button(body,"Use Math instead",false,()->{if(!admission())return;if(operation!=null&&!operation.released()){cancel();issue="Wait for recognizer cleanup before continuing.";render();return;}if(preview){math=true;render();}else result(FALLBACK);});RecordUi.enabled(alternative,!busy);
        Design.space(body,12);Design.text(body,"On-device recognizer only · installed offline English required · no audio or recognized text is saved. No model download is requested.",12,400,Design.MUTED);if(!preview)Design.text(body,"Only this alarm’s sound pauses during listening; lights and vibration keep their settings.",12,400,Design.MUTED);
    }
    private void cancel(){attemptActive=false;if(operation!=null&&!operation.released())operation.cancel();}
    private void result(int result){if(returned||!admission()||operation!=null&&!operation.released())return;returned=true;setResult(result,new Intent().putExtra("run",run).putExtra("unit",unit).putExtra("rounds",rounds).putExtra("request",request).putExtra("captured",operation==null?-1:operation.captured));finish();}
    private final Runnable poll=new Runnable(){public void run(){if(!resumed)return;if(!taskCurrent()){cancel();finish();return;}if(!shown.equals(signature())||operation!=null&&operation.state==PhraseRecognition.State.MATCHED&&attemptActive)render();main.postDelayed(this,200);}};
    @Override protected void onResume(){super.onResume();resumed=true;main.post(poll);}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused){RecordUi.lightBars(this);if(run!=null)render();}else cancel();}
    @Override protected void onPause(){resumed=false;cancel();main.removeCallbacks(poll);super.onPause();}
    @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putInt("previewUnit",unit);out.putBoolean("done",done);out.putBoolean("math",math);out.putString("issue",issue);}
    @Override protected void onDestroy(){cancel();main.removeCallbacks(poll);super.onDestroy();}
    @Override public void onRequestPermissionsResult(int code,String[] permissions,int[] granted){super.onRequestPermissionsResult(code,permissions,granted);if(code==PERMISSION){issue=granted.length>0&&granted[0]==PackageManager.PERMISSION_GRANTED?"Tap Listen to begin.":"Microphone access is off. Use Math instead.";render();}}
}
