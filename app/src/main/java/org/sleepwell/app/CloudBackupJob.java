package org.sleepwell.app;

import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

/** Retained explicit manual operation; cancelling never deletes a newly created provider document. */
final class CloudBackupJob {
    interface Work {CloudBackupCopy.Receipt save(BooleanSupplier active)throws Exception;void cancel();}
    enum Phase {IDLE,SAVING,CANCELLING,DONE,ERROR,CANCELLED}
    static final class State {final Phase phase;final String message;final CloudBackupCopy.Receipt receipt;State(Phase phase,String message,CloudBackupCopy.Receipt receipt){this.phase=phase;this.message=message;this.receipt=receipt;}boolean busy(){return phase==Phase.SAVING||phase==Phase.CANCELLING;}}
    private final Work work;private final Executor worker,ui;private volatile long generation;private volatile State state=new State(Phase.IDLE,"",null);
    CloudBackupJob(Work work,Executor worker,Executor ui){this.work=work;this.worker=worker;this.ui=ui;}
    State state(){return state;}
    void save(){if(state.busy())return;long own=++generation;state=new State(Phase.SAVING,"Creating and verifying a new backup version…",null);try{worker.execute(()->{CloudBackupCopy.Receipt result=null;boolean failed=false;try{result=work.save(()->generation==own);}catch(Exception e){failed=true;}CloudBackupCopy.Receipt receipt=result;boolean error=failed;ui.execute(()->{if(own!=generation){state=new State(Phase.CANCELLED,"Cancelled. A new or partial document may remain; no existing version was deleted.",null);return;}state=error?new State(Phase.ERROR,"Backup did not finish. Check folder access and connection, then retry. A partial version may remain.",null):new State(Phase.DONE,"New version saved and read-back verified. Your provider manages remote upload.",receipt);});});}catch(RuntimeException e){state=new State(Phase.ERROR,"Backup work could not start. Reopen and retry.",null);}}
    void cancel(){if(!state.busy()||state.phase==Phase.CANCELLING)return;generation++;state=new State(Phase.CANCELLING,"Cancelling provider access…",null);try{work.cancel();}catch(RuntimeException ignored){}}
}
