package org.sleepwell.app;

import android.app.Activity;
import android.content.Intent;
import android.os.*;
import android.widget.LinearLayout;
import java.util.*;
import org.json.JSONArray;

/** A cancellable draft exercise, never an alarm admission or a saved-alarm editor. */
public final class ChallengePracticeActivity extends Activity {
    static final int PREVIEW=846;
    private ChallengePracticeSession session;
    private PracticeCheckpoints checkpoints;
    private ChallengePracticeScreen screen;
    private String request,raw,issue="";
    private boolean resumed,returning;
    private int presentationGeneration;

    static Intent preview(Activity owner,ChallengeRoutine routine,String request,Bundle restored){
        Intent intent=RecordUi.intent(owner,ChallengePracticeActivity.class).putExtra("request",request)
                .putExtra("routine",ChallengeRoutineJson.encode(routine).toString());
        if(restored!=null)intent.putExtra("practiceState",new Bundle(restored));return intent;
    }

    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);RecordUi.review(this);setResult(RESULT_CANCELED);
        try{
            Intent input=getIntent();
            if(input.getData()!=null||input.getClipData()!=null||input.getSelector()!=null)throw new IllegalArgumentException("Unsupported preview input.");
            request=input.getStringExtra("request");
            if(request==null||!UUID.fromString(request).toString().equals(request))throw new IllegalArgumentException("Preview request ownership is unavailable.");
            raw=ChallengePracticeSession.canonical(input.getStringExtra("routine"));
            ChallengeRoutine configured=ChallengeRoutineJson.decode(new JSONArray(raw));
            List<String> missing=ChallengePracticeSession.unwired(configured,ChallengePracticeScreen.SUPPORTED);
            if(!missing.isEmpty())throw new IllegalArgumentException("Routine preview is not available for these tasks yet:\n"+String.join("\n",missing)+"\nYour routine has not changed.");
            Bundle restored=saved==null?input.getBundleExtra("practiceState"):saved.getBundle("practiceState");
            session=ChallengePracticeSession.open(this,raw,restored==null?null:restored.getBundle("session"),ChallengePracticeScreen.SUPPORTED);
            checkpoints=new PracticeCheckpoints(session.run,restored==null?null:restored.getBundle("checkpoints"));
        }catch(Exception invalid){issue=invalid.getMessage()==null?"This preview could not open. Your routine has not changed.":invalid.getMessage();session=null;}
        if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::cancel);
        render();
    }

    private void render(){
        closeScreen(true);presentationGeneration++;
        if(session==null){LinearLayout body=Design.page(this,false,null,null);RecordUi.header(body,"Routine preview",action(this::cancel));Design.space(body,16);Design.text(body,issue,15,400,Design.MUTED);Design.fixedAction(body,"Return to routine",action(this::cancel));return;}
        if(session.finished()){
            LinearLayout body=Design.page(this,false,null,null);RecordUi.header(body,"Routine preview",action(this::cancel));Design.space(body,20);
            Design.text(body,"Routine preview completed",26,600,Design.INK);Design.space(body,10);
            Design.text(body,"All "+session.routine.tasks.size()+" tasks completed in order. Practice does not change your alarm.",14,400,Design.MUTED);
            if(!session.selectionNotice.isEmpty()){Design.space(body,12);Design.text(body,session.selectionNotice,13,400,Design.MUTED);}
            Design.button(body,session.configured.randomCount>0?"Try another example selection":"Practice again",false,action(()->{
                session.invalidate();session=ChallengePracticeSession.open(this,raw,null,ChallengePracticeScreen.SUPPORTED);checkpoints=new PracticeCheckpoints(session.run,null);render();
            }));
            Design.fixedAction(body,"Done",action(()->returned(RESULT_OK)));return;
        }
        ChallengeRoutine.Position position=session.position();
        String caption="Task "+(position.index+1)+" of "+session.routine.tasks.size()+(position.task.units()>1?" · Round "+(position.round+1)+" of "+position.task.units():"");
        String clock=Design.clock(this,java.time.LocalTime.now().getHour()*60+java.time.LocalTime.now().getMinute());
        LinearLayout card=WakeChallengeUi.card(this,clock,"Routine preview",caption,position.index,session.routine.tasks.size(),action(this::cancel),true);
        Design.text(card,"Practice only · no alarm is running from this preview.",12,400,Design.MUTED);
        if(!session.selectionNotice.isEmpty())Design.button(card,"Example selection details",false,action(()->SleepPicker.message(this,"This preview selection",session.selectionNotice)));
        try{screen=new ChallengePracticeScreen(this,card,session,checkpoints,()->resumed,this::render);}
        catch(RuntimeException unavailable){
            session.invalidate();closeScreen(false);Design.text(card,"This practice task could not open. "+(unavailable.getMessage()==null?"Retry or return to your routine.":unavailable.getMessage()),14,400,Design.MUTED);
            Design.button(card,"Retry this task",true,action(this::render));
        }
        LinearLayout footer=RecordUi.footer((LinearLayout)card.getParent());footer.setBackgroundColor(Design.CREAM);
        Design.button(footer,"Cancel preview",false,action(this::cancel));
    }

    private Runnable action(Runnable work){int expected=presentationGeneration;return ()->{if(Looper.myLooper()!=Looper.getMainLooper()||expected!=presentationGeneration||!resumed||returning||isFinishing()||isDestroyed()||!hasWindowFocus()||!getWindow().getDecorView().isAttachedToWindow())return;work.run();};}
    private void closeScreen(boolean remember){
        if(screen==null)return;
        try{if(remember)screen.checkpoint();}finally{screen.close();screen=null;}
    }
    private Bundle state(){
        if(session==null)return null;
        if(screen!=null)screen.checkpoint();Bundle state=new Bundle();state.putBundle("session",session.save());state.putBundle("checkpoints",checkpoints.save());return state;
    }
    private void returned(int result){
        if(returning||isFinishing()||isDestroyed())return;
        Bundle state=state();returning=true;if(session!=null)session.invalidate();closeScreen(false);
        if(state!=null)setResult(result,new Intent().putExtra("request",request).putExtra("routine",raw).putExtra("practiceState",state));
        finish();
    }
    private void cancel(){if(Looper.myLooper()==Looper.getMainLooper()&&resumed&&hasWindowFocus())returned(RESULT_CANCELED);}
    @Override public void onBackPressed(){cancel();}
    @Override protected void onResume(){super.onResume();resumed=true;if(session!=null&&screen==null)render();}
    @Override protected void onPause(){closeScreen(true);if(session!=null)session.invalidate();resumed=false;super.onPause();}
    @Override protected void onSaveInstanceState(Bundle saved){super.onSaveInstanceState(saved);Bundle state=state();if(state!=null)saved.putBundle("practiceState",state);}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused){RecordUi.lightBars(this);if(resumed&&session!=null&&screen==null)render();}else{closeScreen(true);if(session!=null)session.invalidate();}}
    @Override protected void onDestroy(){closeScreen(false);if(session!=null)session.invalidate();super.onDestroy();}
}
