package org.sleepwell.app;

import android.content.Intent;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import java.util.Set;

/** An explicit private camera check. Its receipt never authorizes an actual alarm. */
public final class RoutineCameraPracticeActivity extends ComponentActivity {
    static final int REQUEST=850;
    static final Set<String> TYPES=Set.of("QR code","Picture match","Smile","Pose","Camera light","Object recognition");
    private final Handler main=new Handler(Looper.getMainLooper());
    private PracticeChildTicket.Ticket ticket;
    private PracticeCameraAccess.Ports access;
    private PracticeCameraTasks task;
    private boolean resumed,returning,asked,accessReturn,updating;
    private int generation,pendingOutcome=Integer.MIN_VALUE;
    private String issue="",shown="";

    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);RecordUi.review(this);setResult(RESULT_CANCELED);
        if(saved!=null){asked=saved.getBoolean("cameraAsked");accessReturn=saved.getBoolean("accessReturn");pendingOutcome=saved.getInt("pendingOutcome",Integer.MIN_VALUE);}
        ticket=PracticeChildTicket.claim(this,TYPES.toArray(new String[0]));
        if(ticket!=null)try{access=PracticeCameraAccess.factory.create(this);task=PracticeCameraTasks.attach(this,ticket,access,this::visible,this::changed);}catch(RuntimeException unavailable){issue="Camera readiness could not be restored.";}
        getOnBackPressedDispatcher().addCallback(this,new OnBackPressedCallback(true){public void handleOnBackPressed(){returned(RESULT_CANCELED);}});
        render();
    }
    boolean cameraAsked(){return asked;}
    private boolean visible(){return Looper.myLooper()==Looper.getMainLooper()&&resumed&&!returning&&ticket!=null&&ticket.captureOwner(this,true);}
    private boolean cleanupVisible(){return resumed&&!returning&&ticket!=null&&ticket.cleanupOwner(this,true);}
    private Runnable action(Runnable work){int expected=generation;return ()->{if(expected==generation&&visible())work.run();};}
    private Runnable cleanupAction(Runnable work){int expected=generation;return ()->{if(expected==generation&&cleanupVisible())work.run();};}
    private PracticeCameraAccess.Permission permission(){try{return access.permission(ticket.task.type);}catch(RuntimeException unavailable){return PracticeCameraAccess.Permission.UNSUPPORTED;}}
    private String readiness(){try{return access.unavailable(ticket.task);}catch(RuntimeException unavailable){return "Camera readiness could not be checked.";}}
    private boolean busy(){try{return access==null||access.busy();}catch(RuntimeException unavailable){return true;}}
    private boolean releasing(){return task!=null&&!task.active()&&!task.released();}
    private String signature(){return (task==null?"unavailable":task.state()+":"+task.status()+":"+task.issue())+":"+issue+":"+pendingOutcome+":"+permission()+":"+busy();}
    private void enabled(View view,boolean enabled){RecordUi.enabled(view,enabled);view.setAlpha(enabled?1f:.45f);}
    private void render(){
        if(returning||isDestroyed())return;generation++;
        LinearLayout body=Design.page(this,false,null,null);RecordUi.header(body,"Camera practice",cleanupAction(()->returned(RESULT_CANCELED)));
        if(ticket==null||access==null||task==null){Design.text(body,issue.isEmpty()?"This practice request is unavailable. Return and open it again.":issue,15,400,Design.MUTED);Design.fixedAction(body,"Return to routine",this::finish);return;}
        Design.text(body,ticket.task.type,23,600,Design.INK);Design.space(body,8);
        Design.text(body,instruction(),14,400,Design.MUTED);Design.space(body,14);
        FrameLayout panel=new FrameLayout(this);panel.setBackground(Design.shape(this,Design.SOFT,20));panel.setClipToOutline(true);panel.setContentDescription("Routine camera preview");
        View preview=task.preview();
        if(preview!=null){if(preview.getParent() instanceof ViewGroup parent)parent.removeView(preview);panel.addView(preview,new FrameLayout.LayoutParams(-1,-1));}
        else{LinearLayout placeholder=Design.col(this);placeholder.setGravity(Gravity.CENTER);placeholder.addView(new Design.Icon(this,"camera",Design.BROWN),new LinearLayout.LayoutParams(Design.dp(this,58),Design.dp(this,58)));Design.space(placeholder,12);Design.text(placeholder,releasing()?"Camera cleanup pending":task.done()?"Check completed":"Camera stays off until Start",14,500,Design.MUTED).setGravity(Gravity.CENTER);panel.addView(placeholder,new FrameLayout.LayoutParams(-1,-1));}
        body.addView(panel,new LinearLayout.LayoutParams(-1,Design.dp(this,178)));Design.space(body,14);
        String status=pendingOutcome==RESULT_CANCELED?"Result discarded · releasing camera":task.status();
        TextView state=Design.text(body,status,16,600,Design.BROWN);state.setContentDescription("Routine camera status");state.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        String detail=!issue.isEmpty()?issue:task.issue();if(!detail.isEmpty()){Design.space(body,8);Design.text(body,detail,13,500,Design.BROWN);}
        PracticeCameraAccess.Permission permission=permission();boolean waiting=releasing()||ticket.cleanupPending()&&!task.active();
        if(!task.active()&&!waiting&&!task.done()){String reason=readiness();if(!reason.isEmpty()){Design.space(body,8);Design.text(body,reason,13,400,Design.MUTED);}}
        Design.space(body,8);Design.text(body,"Practice only · no camera image is saved.",12,400,Design.MUTED);
        Button alternative=Design.button(body,"Use Math instead",false,action(()->returned(PracticeChildTicket.FALLBACK)));
        enabled(alternative,!waiting&&!task.done()&&pendingOutcome==Integer.MIN_VALUE);
        LinearLayout footer=RecordUi.footer(body);footer.setBackgroundColor(Design.CREAM);
        if(waiting){Design.button(footer,"Retry camera cleanup",true,cleanupAction(()->{ticket.retryCleanup();update();render();}));}
        else if(task.active()){Design.button(footer,"Stop camera",true,cleanupAction(()->{ticket.suspendCapture(this);task.pause(this);issue="Camera stopped. Tap Start for a new attempt.";render();}));}
        else if(!task.done()&&pendingOutcome==Integer.MIN_VALUE){
            if(permission==PracticeCameraAccess.Permission.NEEDED||permission==PracticeCameraAccess.Permission.SETTINGS)Design.button(footer,permission==PracticeCameraAccess.Permission.NEEDED?"Allow camera":"Open camera settings",false,action(this::requestAccess));
            Button start=Design.button(footer,"Start",true,action(()->{issue="";task.start();update();render();}));enabled(start,permission==PracticeCameraAccess.Permission.READY&&!busy()&&readiness().isEmpty());
        }
        Design.button(footer,"Cancel practice step",false,cleanupAction(()->returned(RESULT_CANCELED)));
        shown=signature();schedule();
    }
    private String instruction(){return switch(ticket.task.type){
        case "QR code"->"Scan the enrolled "+MorningBarcode.label(ticket.task.code.format)+" at "+ticket.task.code.label+".";
        case "Picture match"->"Match "+ticket.task.picture.label+" and hold steady for three matching frames.";
        case "Smile"->"Center one face and hold a steady smile. This is an expression estimate.";
        case "Pose"->"Keep your upper body visible and hold both hands raised for 1.2 seconds.";
        case "Camera light"->"Aim at room light. Hold "+CameraLightHold.percent(ticket.task.rounds)+"% image brightness for 1.2 seconds.";
        case "Object recognition"->"Keep "+ticket.task.objectTarget.label()+" centered. This is an experimental object estimate.";
        default->"Tap Start when ready.";
    };}
    private void requestAccess(){if(!visible())return;try{accessReturn=true;access.request(ticket.task.type);asked=true;issue="Access requested. Tap Start when ready.";}catch(RuntimeException unavailable){issue="Camera access could not open. Retry or use Math.";}if(resumed&&hasWindowFocus())render();}
    private void changed(){if(resumed&&hasWindowFocus()&&!updating)update();}
    private void update(){
        if(!visible()||task==null||updating)return;updating=true;
        try{
            task.tick();
            if(!task.active()&&task.released())ticket.clean();
            if(pendingOutcome!=Integer.MIN_VALUE){if(task.released()&&ticket.clean())publish(pendingOutcome);}
            else if(task.done()&&task.released()&&!busy())publish(PracticeChildTicket.SUCCESS);
            if(!returning&&!shown.equals(signature()))render();
        }finally{updating=false;}
    }
    private void returned(int outcome){
        if(returning)return;if(ticket==null){finish();return;}
        if(!(outcome==RESULT_CANCELED?cleanupVisible():visible()))return;
        if(outcome!=RESULT_CANCELED&&busy()){issue="Finish the active alarm or sleep tracking first.";if(task!=null){ticket.suspendCapture(this);task.pause(this);}render();return;}
        if(outcome==PracticeChildTicket.SUCCESS&&(task==null||!task.done()))return;
        pendingOutcome=outcome;ticket.suspendCapture(this);if(task!=null)task.pause(this);
        if(task!=null&&!task.released()||!ticket.clean()){render();return;}publish(outcome);
    }
    private void publish(int outcome){
        if(!(outcome==RESULT_CANCELED?cleanupVisible():visible())||task==null||!task.released()||!ticket.clean())return;
        Intent result=PracticeChildTicket.publish(ticket,this,outcome,true,resumed,task.attempt);
        if(result==null){if(outcome==RESULT_CANCELED){returning=true;task.forget();finish();return;}issue="This practice result is no longer current. Cancel and open the step again.";render();return;}
        returning=true;main.removeCallbacksAndMessages(null);task.forget();setResult(outcome,result);finish();
    }
    private void schedule(){main.removeCallbacks(watch);if(resumed&&!returning)main.postDelayed(watch,100);}
    private final Runnable watch=new Runnable(){public void run(){if(!resumed||returning)return;update();schedule();}};
    @Override protected void onResume(){super.onResume();resumed=true;if(accessReturn){accessReturn=false;issue="Access checked. Tap Start when ready.";}if(hasWindowFocus()){update();render();}schedule();}
    private void pauseCapture(){if(ticket!=null)ticket.suspendCapture(this);if(task!=null)task.pause(this);}
    @Override protected void onPause(){resumed=false;main.removeCallbacksAndMessages(null);pauseCapture();super.onPause();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused){RecordUi.lightBars(this);if(resumed){update();render();}}else pauseCapture();}
    @Override protected void onSaveInstanceState(Bundle saved){super.onSaveInstanceState(saved);saved.putBoolean("cameraAsked",asked);saved.putBoolean("accessReturn",accessReturn);saved.putInt("pendingOutcome",pendingOutcome);}
    @Override protected void onDestroy(){main.removeCallbacksAndMessages(null);pauseCapture();if(task!=null)task.detach(this);super.onDestroy();}
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] grants){super.onRequestPermissionsResult(request,permissions,grants);if(request==PracticeCameraAccess.REQUEST){issue="Access checked. Tap Start when ready.";if(resumed&&hasWindowFocus())render();}}
}
