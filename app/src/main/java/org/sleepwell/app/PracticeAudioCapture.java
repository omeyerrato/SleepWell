package org.sleepwell.app;

import android.os.*;
import java.lang.ref.WeakReference;
import java.util.concurrent.*;

/** One ticket's real audio engine. No service, alarm quieting, sound, storage or synthetic success. */
final class PracticeAudioCapture implements AutoCloseable {
    enum State { IDLE,WORKING,CLOSING,BLOCKED,DONE,ERROR,CANCELLED }
    final PracticeChildTicket.Ticket ticket;final int attempt;
    private final PracticeAudioAccess.Ports access;
    private final WeakReference<RoutineAudioPracticeActivity> owner;
    private final Handler main=new Handler(Looper.getMainLooper());
    private final ExecutorService worker;
    private volatile boolean active=true;
    private SpeakRuntime speech;private NoteRuntime note;private PhraseRecognition phrase;
    // PhraseRecognition observes weakly; retain the actual callback for this attempt's lifetime.
    private final Runnable changed=this::changed;

    PracticeAudioCapture(RoutineAudioPracticeActivity owner,PracticeChildTicket.Ticket ticket,PracticeAudioAccess.Ports access,int attempt){
        this.ticket=ticket;this.access=access;this.attempt=attempt;this.owner=new WeakReference<>(owner);
        worker=ticket.task.type.equals(PhraseRule.TYPE)?null:Executors.newSingleThreadExecutor();
        ticket.bindCleanup(owner,this::close,this::released,this::retry);
    }
    /** Workers use an issued volatile lease; they never read Activity/View lifecycle state. */
    boolean current(){if(!active||!ticket.captureCurrent(attempt))return false;try{if(access.ownersBusy())return false;access.check();return true;}catch(RuntimeException unreadable){return false;}}
    private void check(){if(!current())throw new SecurityException("Practice attempt expired");}
    void start(){
        if(Looper.myLooper()!=Looper.getMainLooper()||!current())throw new IllegalStateException("Tap Listen from the current practice step.");
        if(ticket.task.type.equals(PhraseRule.TYPE)){
            android.content.Context app=owner.get().getApplicationContext();
            PhraseRecognition.Owner admission=new PhraseRecognition.Owner(){
                public android.content.Context context(){return app;}public String run(){return ticket.run;}public int unit(){return ticket.unit;}
                public int rounds(){return ticket.task.rounds;}public boolean preview(){return true;}public boolean current(){return PracticeAudioCapture.this.current();}
            };
            phrase=PhraseRecognition.begin(admission,access.phrase(),changed);
        }else if(ticket.task.type.equals("Musical note")){
            note=new NoteRuntime(new NoteRuntime.Port(){
                public long now(){return access.now();}public boolean current(){return PracticeAudioCapture.this.current();}public void check(){PracticeAudioCapture.this.check();}
                public NoteRuntime.Quiet quiet(Runnable ready){check();ready.run();return ()->{};}
                public NoteRuntime.Capture open()throws Exception{return access.note(PracticeAudioCapture.this::current);}
                public void execute(Runnable work){worker.execute(work);}public void idle()throws Exception{Thread.sleep(10);}public void changed(){PracticeAudioCapture.this.changed();}
            },ticket.run,ticket.unit,ticket.task.rounds);note.start();
        }else{
            speech=new SpeakRuntime(new SpeakRuntime.Port(){
                public long now(){return access.now();}public boolean current(){return PracticeAudioCapture.this.current();}public void check(){PracticeAudioCapture.this.check();}
                public SpeakRuntime.Quiet quiet(Runnable ready){check();ready.run();return ()->{};}
                public SpeakRuntime.Capture open()throws Exception{return access.speech(PracticeAudioCapture.this::current);}
                public SoundAnalysisStream.Model model(){return access.model();}public void execute(Runnable work){worker.execute(work);}
                public void idle()throws Exception{Thread.sleep(10);}public void changed(){PracticeAudioCapture.this.changed();}
            },AudioChallengeMode.from(ticket.task.type));speech.start();
        }
        changed();
    }
    State state(){
        if(phrase!=null)return switch(phrase.state){case CHECKING,QUIETING,LISTENING->State.WORKING;case CLOSING->State.CLOSING;case BLOCKED->State.BLOCKED;case MATCHED->State.DONE;case ERROR->State.ERROR;case CANCELLED->State.CANCELLED;};
        String state=note!=null?note.state.name():speech!=null?speech.state.name():"IDLE";
        return switch(state){case "QUIETING","LISTENING"->State.WORKING;case "CLOSING"->State.CLOSING;case "BLOCKED"->State.BLOCKED;case "DONE"->State.DONE;case "ERROR"->State.ERROR;case "CANCELLED"->State.CANCELLED;default->State.IDLE;};
    }
    String detail(){
        if(phrase!=null)return !phrase.error.isEmpty()?phrase.error:phrase.state==PhraseRecognition.State.LISTENING?"Listening · up to 15 seconds":phrase.state==PhraseRecognition.State.CHECKING?"Checking installed offline English…":phrase.state==PhraseRecognition.State.QUIETING?"Preparing the microphone…":"Closing the on-device recognizer…";
        if(note!=null)return !note.error.isEmpty()?note.error:note.state==NoteRuntime.State.LISTENING?"Target hold · "+note.accepted+"%":"Preparing the microphone…";
        if(speech!=null)return !speech.error.isEmpty()?speech.error:!speech.progress.isEmpty()?speech.progress:"Preparing the microphone…";
        return "Microphone stays off until Listen";
    }
    String heard(){return phrase==null?"":phrase.heard;}
    boolean done(){return state()==State.DONE;}
    private boolean engineReleased(){return phrase!=null?phrase.released():note!=null?note.released():speech==null||speech.released();}
    boolean released(){
        if(!engineReleased())return false;
        if(worker!=null){worker.shutdown();if(!worker.isTerminated())return false;}
        main.removeCallbacksAndMessages(null);return true;
    }
    void tick(){if(note!=null)note.tick();if(speech!=null)speech.tick();}
    void retry(){if(phrase!=null)phrase.retry();if(note!=null)note.retryCleanup();if(speech!=null)speech.retryCleanup();}
    private void changed(){main.post(()->{if(engineReleased()&&worker!=null)worker.shutdown();RoutineAudioPracticeActivity activity=owner.get();if(activity!=null&&!activity.isDestroyed())activity.captureChanged(this);});}
    @Override public void close(){active=false;if(phrase!=null&&!phrase.released())phrase.cancel();if(note!=null&&!note.released())note.cancel();if(speech!=null&&!speech.released())speech.cancel();if(released())main.removeCallbacksAndMessages(null);}
}
