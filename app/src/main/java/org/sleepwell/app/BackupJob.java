package org.sleepwell.app;

import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

/** View-free state machine. All commands/publication use the supplied UI dispatcher. */
final class BackupJob {
    final String id=java.util.UUID.randomUUID().toString();
    enum Kind {JSON,CSV,IMPORT}
    enum Phase {IDLE,CHOOSING,READING,WRITING,REVIEW,RESTORING,DONE,ERROR,CANCELLED}
    static final class Review {final String document,fingerprint,summary;final long admission;Review(String document,String fingerprint,String summary,long admission){this.document=document;this.fingerprint=fingerprint;this.summary=summary;this.admission=admission;}}
    static final class State {final Phase phase;final Kind kind;final Review review;final String message;final boolean occupied;State(Phase phase,Kind kind,Review review,String message,boolean occupied){this.phase=phase;this.kind=kind;this.review=review;this.message=message;this.occupied=occupied;}boolean busy(){return occupied||phase==Phase.CHOOSING;}}
    interface Work {Review read(String uri,BooleanSupplier active)throws Exception;void write(Kind kind,String uri,BooleanSupplier active)throws Exception;String restore(Review review)throws Exception;void cancel();}
    private final Work work;private final Executor worker,ui;private volatile long generation;private volatile State state=new State(Phase.IDLE,null,null,"",false);
    BackupJob(Work work,Executor worker,Executor ui){this.work=work;this.worker=worker;this.ui=ui;}
    State state(){return state;}
    void interrupted(Phase previous){if(state.phase!=Phase.IDLE||previous==Phase.IDLE||previous==Phase.DONE||previous==Phase.ERROR||previous==Phase.CANCELLED)return;state=new State(Phase.ERROR,null,null,previous==Phase.RESTORING?"The app restarted during replacement. Check your records and alarms before importing again.":"The earlier document operation was interrupted. Choose a document again; exports may contain a partial file.",false);}
    boolean choose(Kind kind){if(state.busy())return false;generation++;state=new State(Phase.CHOOSING,kind,null,"Choose a document in Android.",false);return true;}
    void choiceCancelled(){if(state.phase==Phase.CHOOSING)state=new State(Phase.CANCELLED,state.kind,null,"No document selected. Your data is unchanged.",false);}
    void choiceFailed(){if(state.phase==Phase.CHOOSING)state=new State(Phase.ERROR,state.kind,null,"Document chooser unavailable. Try another storage app.",false);}
    void document(String uri){document(uri,null);}
    void document(String uri,String expectedSha){if(state.phase!=Phase.CHOOSING)return;if(expectedSha!=null&&!expectedSha.matches("[a-f0-9]{64}")){state=new State(Phase.ERROR,state.kind,null,"Saved backup verification is unavailable. Choose the document manually.",false);return;}if(uri==null||!uri.startsWith("content:")){state=new State(Phase.ERROR,state.kind,null,"Choose an Android document.",false);return;}Kind kind=state.kind;long own=generation;state=new State(kind==Kind.IMPORT?Phase.READING:Phase.WRITING,kind,null,kind==Kind.IMPORT?"Reading backup…":"Saving document…",true);
        submit(()->{Review review=null;String failure="";try{if(kind==Kind.IMPORT){review=work.read(uri,()->own==generation);if(expectedSha!=null&&!expectedSha.equals(CloudBackupCopy.sha(BackupIo.encode(review.document))))throw new IllegalStateException("This backup changed after verification. Choose it manually to review the new contents.");}else work.write(kind,uri,()->own==generation);}catch(Exception error){failure=notice(error);}Review prepared=review;String error=failure;ui.execute(()->{if(own!=generation){state=new State(Phase.CANCELLED,kind,null,kind==Kind.IMPORT?"Import cancelled. Your data is unchanged.":"Export cancelled. The destination may contain a partial file.",false);return;}state=!error.isEmpty()?new State(Phase.ERROR,kind,null,(kind==Kind.IMPORT?"Backup could not be read: ":"Export failed; the destination may contain a partial file. ")+error,false):kind==Kind.IMPORT?new State(Phase.REVIEW,kind,prepared,"Review before replacing local data.",false):new State(Phase.DONE,kind,null,kind==Kind.CSV?"CSV saved with sleep records, notes and tags.":"JSON saved with alarms, sleep records, notes and sound labels. Audio and app settings are excluded.",false);});});
    }
    void confirm(){State value=state;if(value.phase!=Phase.REVIEW||value.review==null)return;Review review=value.review;long own=++generation;state=new State(Phase.RESTORING,Kind.IMPORT,review,"Replacing local data…",true);submit(()->{String failure="",message="";try{message=work.restore(review);}catch(Exception error){failure=notice(error);}String error=failure,result=message;ui.execute(()->{if(own!=generation)return;state=new State(error.isEmpty()?Phase.DONE:Phase.ERROR,Kind.IMPORT,null,error.isEmpty()?result:"Restore needs attention: "+error,false);});});}
    void cancel(){if(state.phase==Phase.RESTORING)return;Kind kind=state.kind;boolean occupied=state.occupied;generation++;state=new State(Phase.CANCELLED,kind,null,occupied?"Cancelling document access…":"Cancelled. Your local data is unchanged.",occupied);if(occupied)try{work.cancel();}catch(RuntimeException failure){state=new State(Phase.CANCELLED,kind,null,"Cancellation requested. Waiting for the document provider to return.",true);}}
    private void submit(Runnable runnable){try{worker.execute(runnable);}catch(RuntimeException rejected){state=new State(Phase.ERROR,state.kind,null,"Document work could not start. Reopen Backup and try again.",false);}}
    private static String notice(Exception error){String text=error.getMessage();return text==null||text.isBlank()||text.length()>240?"Document access failed. Try again.":text;}
}
