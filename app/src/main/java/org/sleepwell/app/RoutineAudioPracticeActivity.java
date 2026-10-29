package org.sleepwell.app;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;

import android.content.Intent;
import android.os.*;
import android.widget.*;
import java.util.Set;

/** Private explicit-listening routine step. Authentic receipts never authorize an actual alarm. */
public final class RoutineAudioPracticeActivity extends ComponentActivity {
    static final int REQUEST=848;
    static final Set<String> TYPES=Set.of("Speak","Laughter","Loud sound","Musical note","Spoken phrase");
    private final Handler main=new Handler(Looper.getMainLooper());
    private PracticeChildTicket.Ticket ticket;
    private PracticeAudioAccess.Ports access;
    private PracticeAudioCapture capture;
    private boolean resumed,returning,closing,cleanupView;
    private int generation;private String issue="",shown="";
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);RecordUi.review(this);setResult(RESULT_CANCELED);
        ticket=PracticeChildTicket.claim(this,TYPES.toArray(new String[0]));
        if(ticket!=null)try{access=PracticeAudioAccess.factory.create(this);}catch(RuntimeException unavailable){access=null;}
        getOnBackPressedDispatcher().addCallback(this,new OnBackPressedCallback(true){@Override public void handleOnBackPressed(){returned(RESULT_CANCELED);}});
        render();
    }
    private boolean visible(){return Looper.myLooper()==Looper.getMainLooper()&&resumed&&!returning&&ticket!=null&&ticket.captureOwner(this,true);}
    private Runnable action(Runnable work){int expected=generation;return ()->{if(expected==generation&&visible())work.run();};}
    private Runnable cleanupAction(Runnable work){int expected=generation;return ()->{if(expected==generation&&ticket!=null&&ticket.cleanupOwner(this,resumed))work.run();};}
    private boolean busy(){try{return access==null||access.ownersBusy();}catch(RuntimeException unreadable){return true;}}
    private SpeakAccess.Permission permission(){try{return access.permission();}catch(RuntimeException unreadable){return SpeakAccess.Permission.SETTINGS;}}
    private String signature(){return generation+":"+(capture==null?"idle":capture.state()+":"+capture.detail()+":"+capture.heard())+":"+issue+":"+busy();}
    private void render(){
        generation++;cleanupView=false;
        if(ticket==null||access==null){LinearLayout body=Design.page(this,false,null,null);RecordUi.header(body,"Practice step unavailable",this::finish);Design.text(body,"This private listening step could not be restored. Return to the routine and retry. No alarm changed.",15,400,Design.MUTED);Design.fixedAction(body,"Return to routine",this::finish);return;}
        if(capture==null&&ticket.cleanupPending()&&!ticket.clean()){cleanupPage();return;}
        if(closing){if(!ticket.clean()){cleanupPage();return;}capture=null;closing=false;issue="Microphone stopped. Tap Listen for a new attempt.";}
        if(!ticket.belongs(ticket.task.type)){LinearLayout body=Design.page(this,false,null,null);RecordUi.header(body,"Practice step expired",null);Design.text(body,"This step no longer owns the current preview request. Return and open it again.",15,400,Design.MUTED);Design.fixedAction(body,"Cancel practice step",cleanupAction(()->returned(RESULT_CANCELED)));shown=signature();schedule();return;}
        LinearLayout body=Design.page(this,false,null,null);RecordUi.header(body,"Practice listening",action(()->returned(RESULT_CANCELED)));
        Design.text(body,ticket.task.type+" · practice only",14,500,Design.BROWN);Design.space(body,16);
        String type=ticket.task.type;
        if(type.equals(PhraseRule.TYPE)){
            Design.text(body,PhraseRule.target(ticket.run,ticket.unit),25,600,Design.INK);
            Design.text(body,"Read these English words aloud. Only the top final recognized text can match; this does not identify who is speaking.",14,400,Design.MUTED);
            Design.text(body,"Installed offline English required. No model download, audio or recognized text is saved.",12,400,Design.MUTED);
        }else if(type.equals("Musical note")){
            Design.text(body,"Hold "+NotePitch.name(ticket.run,ticket.unit)+" steadily",25,600,Design.INK);
            Design.text(body,"Pitch match within ±"+NotePitch.tolerance(ticket.task.rounds)+" cents for 1.2 seconds. Hum comfortably; this is a pitch estimate, not a voice or identity check.",14,400,Design.MUTED);
        }else{
            AudioChallengeMode mode=AudioChallengeMode.from(type);Design.text(body,mode.instruction,25,600,Design.INK);Design.text(body,mode.detail(),14,400,Design.MUTED);
        }
        Design.space(body,18);String status=capture==null?"Microphone stays off until Listen":capture.detail();
        TextView live=Design.text(body,status,17,600,Design.BROWN);live.setContentDescription("Routine listening status");live.setAccessibilityLiveRegion(android.view.View.ACCESSIBILITY_LIVE_REGION_POLITE);
        if(capture!=null&&!capture.heard().isEmpty())Design.text(body,"Heard: "+capture.heard(),14,400,Design.MUTED);
        if(!issue.isEmpty())Design.text(body,issue,14,500,Design.BROWN);
        boolean working=capture!=null&&!capture.released();SpeakAccess.Permission permission=permission();
        if(busy())Design.text(body,"An alarm or sleep tracking owns the device. Listening stays off.",14,500,Design.BROWN);
        if(permission!=SpeakAccess.Permission.ALLOWED)Design.button(body,permission==SpeakAccess.Permission.SETTINGS?"Open microphone settings":"Allow microphone",false,action(()->{try{access.request();}catch(RuntimeException unavailable){issue="Microphone access could not open. Retry or choose Math.";render();}}));
        if(capture!=null&&capture.state()==PracticeAudioCapture.State.BLOCKED)Design.button(body,"Retry microphone cleanup",true,cleanupAction(()->{closing=true;ticket.retryCleanup();render();}));
        else if(working)Design.button(body,"Stop listening",false,cleanupAction(()->{closing=true;capture.close();render();}));
        else{Button listen=Design.button(body,"Listen",true,action(this::listen));boolean enabled=permission==SpeakAccess.Permission.ALLOWED&&!busy();RecordUi.enabled(listen,enabled);listen.setAlpha(enabled?1f:.45f);}
        Design.button(body,"Use Math instead",false,action(()->returned(PracticeChildTicket.FALLBACK)));
        Design.space(body,10);Design.text(body,(type.equals("Musical note")?"Up to 20 seconds":"Up to 15 seconds")+" per attempt. Leaving or losing focus stops listening. Access and Settings return never start it.",12,400,Design.MUTED);
        LinearLayout footer=RecordUi.footer(body);footer.setBackgroundColor(Design.CREAM);Design.button(footer,"Cancel practice step",false,cleanupAction(()->returned(RESULT_CANCELED)));
        shown=signature();schedule();
    }
    private void listen(){
        if(!visible())return;
        try{
            if(capture!=null&&!capture.released()){issue="Finish this microphone cleanup before another attempt.";render();return;}
            if(!ticket.clean()){cleanupPage();return;}capture=null;
            String unavailable=access.unavailable(ticket.task.type);if(!unavailable.isEmpty()){issue=unavailable;render();return;}
            int attempt=ticket.beginCapture(this,true);if(attempt<0){issue="Another practice capture is still closing. Return and retry.";render();return;}
            issue="";capture=new PracticeAudioCapture(this,ticket,access,attempt);capture.start();
        }catch(RuntimeException|LinkageError unavailable){issue="Listening could not start safely. Retry or choose Math.";if(capture!=null)capture.close();}
        render();
    }
    private void cleanupPage(){
        generation++;cleanupView=true;LinearLayout body=Design.page(this,false,null,null);RecordUi.header(body,"Release microphone",null);
        Design.text(body,"This attempt has not confirmed its own microphone, recognizer or worker cleanup. It cannot complete or choose Math yet.",15,400,Design.MUTED);
        Design.button(body,"Retry microphone cleanup",true,cleanupAction(()->{closing=true;ticket.retryCleanup();render();}));
        Design.button(body,"Cancel practice step",false,cleanupAction(()->returned(RESULT_CANCELED)));shown=signature();schedule();
    }
    void captureChanged(PracticeAudioCapture expected){if(capture==expected&&resumed&&hasWindowFocus())update();}
    private void update(){
        if(!visible())return;
        if(cleanupView){if(ticket.clean()){capture=null;closing=false;issue="Microphone stopped. Tap Listen for a new attempt.";render();}return;}
        if((busy()||!ticket.belongs(ticket.task.type))&&capture!=null&&!closing){ticket.suspendCapture(this);closing=true;capture.close();issue="Listening stopped because this step no longer has capture ownership.";render();return;}
        if(capture!=null){capture.tick();if(!closing&&capture.done()&&capture.released()){returned(PracticeChildTicket.SUCCESS);return;}}
        if(closing||!shown.equals(signature()))render();
    }
    private void schedule(){main.removeCallbacks(watch);if(resumed&&!returning)main.postDelayed(watch,100);}
    private final Runnable watch=new Runnable(){public void run(){if(!resumed||returning)return;update();schedule();}};
    private void returned(int outcome){
        if(returning||ticket==null||!(outcome==RESULT_CANCELED?ticket.cleanupOwner(this,resumed):visible()))return;
        if(outcome!=RESULT_CANCELED&&busy()){ticket.suspendCapture(this);if(capture!=null){closing=true;capture.close();}issue="Finish the active alarm or tracking before continuing this preview.";render();return;}
        if(outcome==PracticeChildTicket.SUCCESS&&(capture==null||!capture.done()||!capture.current()||!capture.released()))return;
        int attempt=capture==null?-1:capture.attempt;
        if(!ticket.clean()){closing=true;cleanupPage();return;}
        Intent result=PracticeChildTicket.publish(ticket,this,outcome,true,resumed,attempt);
        if(result==null){if(outcome==RESULT_CANCELED){returning=true;finish();}else{capture=null;render();}return;}
        returning=true;main.removeCallbacksAndMessages(null);setResult(outcome,result);finish();
    }
    @Override protected void onResume(){super.onResume();resumed=true;if(hasWindowFocus())render();schedule();}
    @Override protected void onPause(){resumed=false;main.removeCallbacksAndMessages(null);if(ticket!=null)ticket.suspendCapture(this);if(capture!=null){closing=true;capture.close();}super.onPause();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused){RecordUi.lightBars(this);if(resumed)render();}else{if(ticket!=null)ticket.suspendCapture(this);if(capture!=null){closing=true;capture.close();}}}
    @Override protected void onDestroy(){main.removeCallbacksAndMessages(null);if(ticket!=null)ticket.suspendCapture(this);if(capture!=null)capture.close();super.onDestroy();}
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] grants){super.onRequestPermissionsResult(request,permissions,grants);if(request==SpeakAccess.PERMISSION){issue="Access was checked. Tap Listen when ready.";if(resumed&&hasWindowFocus())render();}}
}
