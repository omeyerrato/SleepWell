package org.sleepwell.app;

import android.app.Activity;
import android.content.Intent;
import android.os.*;
import java.lang.ref.WeakReference;
import java.util.*;

/** Private preview receipts. None of these fields or modes authorizes an actual alarm result. */
final class PracticeChildTicket {
    static final String MODE="routine-preview";
    static final int SUCCESS=Activity.RESULT_OK,FALLBACK=Activity.RESULT_FIRST_USER+18;
    private static final Map<String,Ticket> issued=new LinkedHashMap<>();
    private static Ticket capture;
    private static final long LIFETIME=30*60*1000L;

    static final class Ticket {
        final String nonce,run,snapshot,configuration;
        final int unit,round;
        final ChallengeRoutine.Task task;
        final long deadline;
        private ChallengePracticeSession parent;
        private WeakReference<Activity> child=new WeakReference<>(null);
        private volatile boolean published,consumed,capturing;
        private volatile int attempt;
        private int cleanedAttempt=-1,earnedAttempt=-1;
        private int outcome=Activity.RESULT_CANCELED;
        private Bundle checkpoint;
        private Runnable cleanup,retry;
        private java.util.function.BooleanSupplier released;
        Ticket(ChallengePracticeSession parent){this.parent=parent;nonce=UUID.randomUUID().toString();run=parent.run;snapshot=parent.snapshot;unit=parent.completed();ChallengeRoutine.Position at=parent.position();task=at.task;round=at.round;configuration=key(task);deadline=SystemClock.elapsedRealtime()+LIFETIME;Bundle draft=parent.draft();checkpoint=draft==null?null:draft.getBundle("motionCheckpoint");}
        private boolean live(){return !consumed&&SystemClock.elapsedRealtime()<=deadline&&issued.get(nonce)==this&&parent.child(nonce,unit,configuration);}
        boolean captureOwner(Activity owner,boolean resumed){return main()&&resumed&&live()&&!published&&child.get()==owner&&!owner.isDestroyed()&&!owner.isFinishing()&&owner.hasWindowFocus()&&owner.getWindow().getDecorView().isAttachedToWindow();}
        boolean cleanupOwner(Activity owner,boolean resumed){return main()&&resumed&&child.get()==owner&&!published&&!owner.isDestroyed()&&!owner.isFinishing()&&owner.hasWindowFocus()&&owner.getWindow().getDecorView().isAttachedToWindow();}
        boolean belongs(String type){return main()&&live()&&task.type.equals(type);}
        int beginCapture(Activity owner,boolean resumed){requireMain();if(!captureOwner(owner,resumed)||earnedAttempt>=0||capture!=null&&capture!=this)return -1;capture=this;attempt++;cleanedAttempt=-1;capturing=true;return attempt;}
        /** Camera validators latch an earned immutable unit before asynchronous owned release. */
        boolean markEarned(Activity owner,boolean resumed,int expected){requireMain();if(!captureOwner(owner,resumed)||!captureCurrent(expected)||earnedAttempt>=0)return false;earnedAttempt=expected;return true;}
        boolean earned(int expected){requireMain();return earnedAttempt==expected&&expected>0;}
        /** Workers inspect only a main-thread-issued volatile lease, never Activity Views or parent state. */
        boolean captureCurrent(int expected){return capturing&&!published&&!consumed&&attempt==expected&&SystemClock.elapsedRealtime()<=deadline;}
        void suspendCapture(Activity owner){requireMain();if(child.get()==owner){capturing=false;attempt++;}}
        void bindCleanup(Activity owner,Runnable cleanup,java.util.function.BooleanSupplier released){bindCleanup(owner,cleanup,released,cleanup);}
        void bindCleanup(Activity owner,Runnable cleanup,java.util.function.BooleanSupplier released,Runnable retry){requireMain();if(child.get()!=owner||this.cleanup!=null)throw new IllegalStateException("Earlier practice capture must close first.");this.cleanup=cleanup;this.released=released;this.retry=retry;}
        boolean retryCleanup(){requireMain();try{if(retry!=null)retry.run();}catch(RuntimeException failure){return false;}return clean();}
        boolean clean(){requireMain();if(cleanup==null){if(capture==this)capture=null;return true;}try{cleanup.run();if(!released.getAsBoolean())return false;if(earnedAttempt>=0)cleanedAttempt=earnedAttempt;else if(capturing)cleanedAttempt=attempt;capturing=false;attempt++;cleanup=null;released=null;retry=null;if(capture==this)capture=null;return true;}catch(RuntimeException failure){return false;}}
        boolean cleanupPending(){requireMain();return cleanup!=null;}
        Bundle checkpoint(){requireMain();return checkpoint==null?null:new Bundle(checkpoint);}
        void checkpoint(Activity owner,Bundle state){requireMain();if(child.get()!=owner)return;Parcel parcel=Parcel.obtain();try{parcel.writeBundle(state);if(parcel.dataSize()>65536)throw new IllegalArgumentException("Practice step state is too large to retain.");checkpoint=state==null?null:new Bundle(state);}finally{parcel.recycle();}}
    }

    private static boolean main(){return Looper.myLooper()==Looper.getMainLooper();}
    private static void requireMain(){if(!main())throw new IllegalStateException("Practice receipts belong to their UI owner.");}
    private static String key(ChallengeRoutine.Task task){return ChallengeRoutineJson.encode(new ChallengeRoutine(List.of(task))).toString();}

    static Ticket issue(ChallengePracticeSession parent){
        requireMain();if(parent.position()==null)throw new IllegalArgumentException("This preview is complete.");
        for(Iterator<Ticket> values=issued.values().iterator();values.hasNext();){Ticket old=values.next();if((old.consumed||SystemClock.elapsedRealtime()>old.deadline)&&old.clean()){old.consumed=true;old.capturing=false;old.child.clear();values.remove();}}
        // Never evict or stop somebody else's live capture to make room for a new preview.
        if(issued.size()>=8)throw new IllegalStateException("Earlier practice requests are still closing. Return and close them first.");
        Ticket ticket=new Ticket(parent);if(!parent.issueChild(ticket.nonce,ticket.unit,ticket.configuration))throw new IllegalStateException("A preview step is already open.");issued.put(ticket.nonce,ticket);return ticket;
    }

    static Intent intent(Activity parent,Class<? extends Activity> child,Ticket ticket){
        requireMain();if(!ticket.live())throw new IllegalArgumentException("Practice request expired.");
        return RecordUi.intent(parent,child).putExtra("mode",MODE).putExtra("practiceNonce",ticket.nonce)
                .putExtra("practiceRun",ticket.run).putExtra("practiceUnit",ticket.unit).putExtra("practiceTask",ticket.configuration);
    }

    static Ticket claim(Activity child,String... supported){
        requireMain();Intent input=child.getIntent();
        if(input.getData()!=null||input.getClipData()!=null||input.getSelector()!=null||!MODE.equals(input.getStringExtra("mode")))return null;
        Ticket ticket=issued.get(input.getStringExtra("practiceNonce"));
        if(ticket==null||!ticket.live()||ticket.published||!ticket.run.equals(input.getStringExtra("practiceRun"))
                ||ticket.unit!=input.getIntExtra("practiceUnit",-1)||!ticket.configuration.equals(input.getStringExtra("practiceTask"))
                ||!Arrays.asList(supported).contains(ticket.task.type))return null;
        Activity previous=ticket.child.get();
        if(previous!=null&&previous!=child&&!previous.isDestroyed()&&!previous.isChangingConfigurations())return null;
        ticket.child=new WeakReference<>(child);return ticket;
    }

    /** Called only after the matching child has confirmed its own capture cleanup. */
    static Intent publish(Ticket ticket,Activity child,int outcome,boolean cleanupConfirmed,boolean resumed,int attempt){
        requireMain();
        if(ticket==null||!cleanupConfirmed||!ticket.clean()||!ticket.live()||ticket.published||ticket.child.get()!=child
                ||outcome!=SUCCESS&&outcome!=FALLBACK&&outcome!=Activity.RESULT_CANCELED)return null;
        // A success needs a current visible native child. Cancel may be delivered during teardown.
        if(outcome!=Activity.RESULT_CANCELED&&!ticket.captureOwner(child,resumed))return null;
        if(outcome==SUCCESS&&(attempt<1||ticket.cleanedAttempt!=attempt))return null;
        ticket.published=true;ticket.capturing=false;ticket.outcome=outcome;
        return new Intent().putExtra("practiceNonce",ticket.nonce).putExtra("practiceRun",ticket.run)
                .putExtra("practiceUnit",ticket.unit).putExtra("practiceTask",ticket.configuration);
    }

    /** Parent recreation changes the Java owner, never the authorized snapshot or pending unit. */
    static boolean attach(ChallengePracticeSession parent){
        requireMain();if(parent.pending().isEmpty())return true;Ticket ticket=issued.get(parent.pending());
        if(ticket==null||ticket.consumed||SystemClock.elapsedRealtime()>ticket.deadline||!ticket.run.equals(parent.run)
                ||!ticket.snapshot.equals(parent.snapshot)||!parent.child(ticket.nonce,ticket.unit,ticket.configuration)){
            if(ticket==null||ticket.clean())parent.abandonChild();return false;
        }
        ticket.parent=parent;return true;
    }

    /** No parent resumed/focus gate: an authenticated child result can precede parent onResume. */
    static boolean consume(ChallengePracticeSession parent,int result,Intent data){
        requireMain();if(data==null||data.getData()!=null||data.getClipData()!=null||data.getSelector()!=null)return false;
        Ticket ticket=issued.get(data.getStringExtra("practiceNonce"));
        if(ticket==null||!ticket.live()||!ticket.published||ticket.outcome!=result||!ticket.snapshot.equals(parent.snapshot)
                ||!ticket.run.equals(parent.run)||!ticket.run.equals(data.getStringExtra("practiceRun"))
                ||ticket.unit!=data.getIntExtra("practiceUnit",-1)||!ticket.configuration.equals(data.getStringExtra("practiceTask"))
                ||!parent.child(ticket.nonce,ticket.unit,ticket.configuration))return false;
        ticket.parent=parent;
        if(result==Activity.RESULT_CANCELED&&!parent.rememberChild(ticket.nonce,ticket.unit,ticket.configuration,ticket.checkpoint))return false;
        boolean accepted=parent.childReturned(ticket.nonce,ticket.unit,ticket.configuration,result==SUCCESS,result==FALLBACK);
        if(accepted){ticket.consumed=true;issued.remove(ticket.nonce);ticket.child.clear();}return accepted;
    }
    static Ticket pending(ChallengePracticeSession parent){requireMain();Ticket ticket=issued.get(parent.pending());return ticket!=null&&ticket.snapshot.equals(parent.snapshot)&&ticket.run.equals(parent.run)?ticket:null;}

    static void abandon(ChallengePracticeSession parent){
        requireMain();Ticket ticket=issued.get(parent.pending());
        if(ticket!=null&&ticket.run.equals(parent.run)&&ticket.snapshot.equals(parent.snapshot)){
            if(!ticket.clean())throw new IllegalStateException("Practice capture cleanup must finish first.");
            parent.rememberChild(ticket.nonce,ticket.unit,ticket.configuration,ticket.checkpoint);
            ticket.consumed=true;issued.remove(ticket.nonce);ticket.child.clear();
        }
        parent.abandonChild();
    }
    private PracticeChildTicket(){}
}
