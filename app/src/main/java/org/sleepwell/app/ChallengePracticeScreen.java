package org.sleepwell.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import java.util.*;
import java.util.function.BooleanSupplier;

/** Real screen-task controllers; no hardware or configuration-only success substitutes. */
final class ChallengePracticeScreen implements AutoCloseable {
    static final Set<String> SUPPORTED;
    static{Set<String> types=new HashSet<>(RandomChallenges.SCREEN);types.addAll(MotionChallengePracticeActivity.TYPES);types.addAll(RoutineAudioPracticeActivity.TYPES);SUPPORTED=Set.copyOf(types);}
    private final Activity activity;
    private final ChallengePracticeSession session;
    private final ChallengePracticeSession.Ticket ticket;
    private final PracticeCheckpoints checkpoints;
    private final ChallengePracticeOwner owner;
    private final Runnable changed;
    private final Random random=new Random();
    private final Bundle draft;
    private MathTask math;
    private Integer choice;
    private EditText mathInput,counting;
    private TypedWakeTasks typed;
    private WakeCognitiveTask cognitive;
    private TypeCodeTask code;
    private JumpTask jump;
    private boolean closed;

    ChallengePracticeScreen(Activity activity,LinearLayout card,ChallengePracticeSession session,
                            PracticeCheckpoints checkpoints,BooleanSupplier resumed,Runnable changed,Runnable openChild){
        this.activity=activity;this.session=session;this.checkpoints=checkpoints;this.changed=changed;
        ticket=session.render();if(ticket==null)throw new IllegalArgumentException("This practice is complete.");
        owner=new ChallengePracticeOwner(activity,card,session,ticket,resumed);
        Bundle saved=session.draft();draft=saved==null?new Bundle():saved;
        ChallengeRoutine.Position position=session.position();ChallengeRoutine.Task task=position.task;
        if(!SUPPORTED.contains(task.type))throw new IllegalArgumentException(task.type+" is not available in routine preview yet.");
        if(draft.getBoolean("practiceFallback")){math(card,task.code==null?MathSettings.LEGACY:task.code.fallback);return;}
        if(mathSelected(position)){math(card,MathSettings.LEGACY);return;}
        switch(task.type){
            case "Math"->math(card,task.math);
            case "Memory"->cognitive=new WakeCognitiveTask.Memory(card,session.run,ticket.unit(),random,draft.getBundle("cognitive"),owner::current,this::complete);
            case "Sequence"->cognitive=new WakeCognitiveTask.Sequence(card,session.run,ticket.unit(),position.round,random,draft.getBundle("cognitive"),owner::current,this::complete);
            case "Retype"->{Bundle state=draft.getBundle("cognitive");String phrase=state==null?"":state.getString("phrase","");if(!phrase.matches("MORNING [0-9]{4}"))phrase="MORNING "+(1000+random.nextInt(9000));cognitive=new WakeCognitiveTask.Retype(card,session.run,ticket.unit(),phrase,true,state,owner::current,this::complete);}
            case "Counting"->counting=WakePuzzleView.counting(card,session.run,ticket.unit(),position.round,draft.getBundle("counting"),owner::current,this::complete,true);
            case "Flags"->WakePuzzleView.flags(card,session.run,ticket.unit(),position.round,owner::current,this::complete,true);
            case "Mirror text","Dream journal"->typed=new TypedWakeTasks(card,task.type,session.run,ticket.unit(),task.journalMinimum,draft.getBundle("typed"),owner::current,this::complete,true);
            case "Visual search"->{MoonSearch board=MoonSearchState.open(checkpoints,session.run,ticket.unit(),position.round,random);MoonSearchView.render(card,board,owner::current,this::complete);Design.button(card,"Use Math instead",false,()->owner.submit(()->{if(!checkpoints.edit().putString("practiceMathRun",session.run).putInt("practiceMathUnit",ticket.unit()).commit()){Ui.toast(activity,"The preview alternative could not be retained.");return;}refresh();}));}
            case "Type code"->code=new TypeCodeTask(card,checkpoints,session.run,ticket.unit(),task.rounds,draft.getBundle("typeCode"),owner::current,this::complete,this::refresh);
            case "Jumping"->jump=new JumpTask(card,checkpoints,session.run,ticket.unit(),position.round,owner::current,this::complete,this::refresh);
            case "Tilt maze","Light","Spin around","Shake","Walk","NFC","Speak","Laughter","Loud sound","Musical note","Spoken phrase"->{Design.text(card,task.type,24,600,Design.INK);Design.text(card,task.detail(),15,500,Design.BROWN);Design.text(card,"Open this step, then choose "+(RoutineAudioPracticeActivity.TYPES.contains(task.type)?"Listen":"Start")+" when ready. Returning from access settings never starts a check.",14,400,Design.MUTED);Design.button(card,"Open practice step",true,()->owner.submit(openChild));}
            default->throw new IllegalArgumentException("This task is not available in routine preview yet.");
        }
    }

    private boolean mathSelected(ChallengeRoutine.Position position){
        if(session.run.equals(checkpoints.getString("practiceMathRun",""))&&ticket.unit()==checkpoints.getInt("practiceMathUnit",-1))return true;
        return position.task.type.equals("Type code")&&TypeCodeTask.math(checkpoints,session.run,ticket.unit(),position.task.rounds)
                ||position.task.type.equals("Jumping")&&JumpCheckpoint.math(checkpoints,session.run,ticket.unit(),position.round);
    }

    private void math(LinearLayout card,MathSettings settings){
        try{math=MathTask.decode(draft.getString("math",""));if(!math.matches(settings))throw new IllegalArgumentException();}
        catch(RuntimeException invalid){math=MathTask.create(random,settings);draft.remove("choice");draft.remove("answer");}
        choice=draft.containsKey("choice")?draft.getInt("choice"):null;if(choice!=null&&!math.options.contains(choice))choice=null;
        WakeMathCard.Binding binding=WakeMathCard.render(card,math,choice,draft.getString("answer",""),true,value->{choice=value;refresh();},this::complete,owner::submit);
        mathInput=binding.input;
    }

    private void refresh(){if(closed||!session.owns(ticket))return;checkpoint();changed.run();}
    private void complete(){
        owner.submit(()->{if(closed)return;close();if(session.advance(ticket))changed.run();});
    }
    boolean checkpoint(){
        if(closed||!session.owns(ticket))return false;
        Bundle state=new Bundle(draft);
        if(math!=null){state.putString("math",math.encode());if(choice!=null)state.putInt("choice",choice);else state.remove("choice");if(mathInput!=null)state.putString("answer",mathInput.getText().toString());}
        if(counting!=null){Bundle value=new Bundle();ChallengeRoutine.Position at=session.position();value.putString("countKey",ChallengePackState.key(session.run,ticket.unit(),at.round));value.putString("countAnswer",counting.getText().toString());state.putBundle("counting",value);}
        if(typed!=null)state.putBundle("typed",typed.state());
        if(cognitive!=null)state.putBundle("cognitive",cognitive.state());
        if(code!=null)state.putBundle("typeCode",code.state());
        return session.checkpoint(ticket,state);
    }
    @Override public void close(){if(closed)return;closed=true;if(typed!=null)typed.close();if(cognitive!=null)cognitive.close();if(code!=null)code.close();if(jump!=null)jump.close();}
}
