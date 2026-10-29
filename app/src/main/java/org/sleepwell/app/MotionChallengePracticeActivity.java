package org.sleepwell.app;

import android.app.Activity;
import android.content.Intent;
import android.os.*;
import android.widget.LinearLayout;
import java.util.Set;

/** Private routine child. No alarm admission, service, output, or access grant is implied by launch. */
public final class MotionChallengePracticeActivity extends Activity {
    static final int REQUEST=847;
    static final Set<String> TYPES=Set.of("Tilt maze","Light","Spin around","Shake","Walk","NFC");
    private PracticeChildTicket.Ticket ticket;
    private PracticeMotionAccess.Ports access;
    private PracticeCheckpoints checkpoints;
    private PracticeMotionTasks task;
    private boolean resumed,returning;
    private int generation;
    private final Handler main=new Handler(Looper.getMainLooper());
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);RecordUi.review(this);setResult(RESULT_CANCELED);
        ticket=PracticeChildTicket.claim(this,TYPES.toArray(new String[0]));
        if(ticket!=null){try{access=PracticeMotionAccess.factory.create(this);checkpoints=new PracticeCheckpoints(ticket.run+":"+ticket.unit,ticket.checkpoint());}catch(RuntimeException failure){access=null;}}
        if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,()->returned(RESULT_CANCELED));
        render();
    }
    private boolean visible(){return Looper.myLooper()==Looper.getMainLooper()&&resumed&&!returning&&ticket!=null&&ticket.captureOwner(this,true);}
    private Runnable action(Runnable work){int expected=generation;return ()->{if(expected==generation&&visible())work.run();};}
    private Runnable cleanupAction(Runnable work){int expected=generation;return ()->{if(expected==generation&&ticket!=null&&ticket.cleanupOwner(this,resumed))work.run();};}
    private void remember(){if(ticket!=null&&checkpoints!=null)ticket.checkpoint(this,checkpoints.save());}
    private boolean closeTask(){main.removeCallbacksAndMessages(null);if(ticket==null)return true;boolean clean=ticket.clean();remember();if(clean)task=null;return clean;}
    private void render(){
        generation++;
        if(ticket==null||access==null){LinearLayout body=Design.page(this,false,null,null);RecordUi.header(body,"Practice step unavailable",()->finish());Design.text(body,"This preview step could not be restored. Return to the routine and try again. No alarm has changed.",15,400,Design.MUTED);Design.fixedAction(body,"Return to routine",()->finish());return;}
        if(!closeTask()){cleanupPage();return;}
        if(!ticket.belongs(ticket.task.type)){LinearLayout body=Design.page(this,false,null,null);RecordUi.header(body,"Practice step expired",null);Design.text(body,"This step no longer owns a current preview request. Return to the routine and start it again.",15,400,Design.MUTED);Design.fixedAction(body,"Cancel practice step",cleanupAction(()->returned(RESULT_CANCELED)));return;}
        LinearLayout card=Design.page(this,false,null,null);RecordUi.header(card,"Practice step",action(()->returned(RESULT_CANCELED)));
        Design.text(card,ticket.task.type+" · practice only",14,500,Design.BROWN);Design.space(card,12);
        boolean busy;try{busy=access.busy();}catch(RuntimeException unavailable){busy=true;}
        if(busy){Design.text(card,"An alarm or tracking session owns the device. This practice step stays off. Return and try after it finishes.",15,400,Design.MUTED);Design.button(card,"Check availability again",false,action(this::render));}
        else try{task=new PracticeMotionTasks(this,card,ticket,access,checkpoints,this::visible,()->returned(PracticeChildTicket.SUCCESS),()->returned(PracticeChildTicket.FALLBACK));}
        catch(RuntimeException unavailable){closeTask();Design.text(card,"This step could not open safely. Return or retry after cleanup.",15,400,Design.MUTED);Design.button(card,"Retry this step",false,action(this::render));}
        LinearLayout footer=RecordUi.footer(card);footer.setBackgroundColor(Design.CREAM);Design.button(footer,"Cancel practice step",false,cleanupAction(()->returned(RESULT_CANCELED)));
        if(resumed)main.postDelayed(watch,100);
    }
    private void cleanupPage(){
        LinearLayout body=Design.page(this,false,null,null);RecordUi.header(body,"Close practice capture",null);
        Design.text(body,"This step could not confirm that its own sensor or reader stopped. It cannot complete or choose Math until cleanup succeeds.",15,400,Design.MUTED);
        Design.button(body,"Retry practice cleanup",true,cleanupAction(()->{if(closeTask())render();}));
        Design.button(body,"Cancel practice step",false,cleanupAction(()->returned(RESULT_CANCELED)));
    }
    private final Runnable watch=new Runnable(){public void run(){if(!resumed||returning)return;boolean busy;try{busy=access==null||access.busy();}catch(RuntimeException failure){busy=true;}if((busy||!ticket.belongs(ticket.task.type))&&task!=null){ticket.suspendCapture(MotionChallengePracticeActivity.this);render();return;}main.postDelayed(this,100);}};
    private void returned(int result){
        if(returning||ticket==null||!(result==RESULT_CANCELED?ticket.cleanupOwner(this,resumed):visible()))return;
        int attempt=task==null?-1:task.attempt;
        if(!closeTask()){cleanupPage();return;}
        Intent proof=PracticeChildTicket.publish(ticket,this,result,true,resumed,attempt);
        if(proof==null){if(result==RESULT_CANCELED){returning=true;finish();}else render();return;}
        returning=true;main.removeCallbacksAndMessages(null);setResult(result,proof);finish();
    }
    @Override public void onBackPressed(){returned(RESULT_CANCELED);}
    @Override protected void onResume(){super.onResume();resumed=true;if(hasWindowFocus())render();}
    @Override protected void onPause(){if(ticket!=null)ticket.suspendCapture(this);closeTask();resumed=false;super.onPause();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused){RecordUi.lightBars(this);if(resumed&&task==null)render();}else{if(ticket!=null)ticket.suspendCapture(this);closeTask();}}
    @Override protected void onSaveInstanceState(Bundle saved){remember();super.onSaveInstanceState(saved);}
    @Override protected void onDestroy(){if(ticket!=null)ticket.suspendCapture(this);closeTask();super.onDestroy();}
}
