package org.sleepwell.app;

import android.content.Context;
import android.net.Uri;
import android.os.*;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.*;

/** Retained private foreground preview. This worker never grants URI access or opens a chooser. */
final class ReportShareJob {
    enum Phase {PREPARING,READY,VERIFYING,VERIFIED,ERROR}
    static final class State {
        final Phase phase;final boolean notes;final ReportShareFiles.Snapshot snapshot;final String message;
        State(Phase phase,boolean notes,ReportShareFiles.Snapshot snapshot,String message){this.phase=phase;this.notes=notes;this.snapshot=snapshot;this.message=message;}
        boolean busy(){return phase==Phase.PREPARING||phase==Phase.VERIFYING;}
    }
    interface Observer {void changed(State value);}
    private final Context app;private final Records.Factory records;final long record;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();private final Handler main=new Handler(Looper.getMainLooper());
    private WeakReference<Observer> observer=new WeakReference<>(null);private volatile boolean closed;private volatile int generation;private long epoch;
    private State state;private ReportShareFiles.Snapshot owned;private boolean invalidated;
    ReportShareJob(Context context,long record,boolean notes,boolean interrupted){app=context.getApplicationContext();records=Records.factory;this.record=record;prepare(notes,interrupted?"The earlier preview was interrupted. Review this new copy before sharing.":"");}
    State state(){checkHistory();return state;}
    void observe(Observer target){observer=new WeakReference<>(target);checkHistory();target.changed(state);}
    void detach(Observer target){if(observer.get()==target)observer.clear();}
    private boolean live(int ticket,long expected){return !closed&&generation==ticket&&epoch==expected&&SleepSummaryFiles.current(expected);}
    private void publish(State next){if(closed)return;if(!SleepSummaryFiles.current(epoch)){checkHistory();return;}state=next;Observer target=observer.get();if(target!=null)target.changed(next);}
    void checkHistory(){if(!closed&&!invalidated&&!SleepSummaryFiles.current(epoch)){invalidated=true;generation++;state=new State(Phase.ERROR,state!=null&&state.notes,null,"Sleep history changed. Prepare a new preview before sharing.");worker.execute(()->{if(owned!=null){owned.discard();owned=null;}});Observer target=observer.get();if(target!=null)target.changed(state);}}
    void prepare(boolean notes){checkHistory();if(!closed&&!state.busy())prepare(notes,"");}
    private void prepare(boolean notes,String notice){
        int ticket=++generation;epoch=SleepSummaryFiles.epoch();long expected=epoch;invalidated=false;publish(new State(Phase.PREPARING,notes,null,"Preparing this night’s private preview…"));
        worker.execute(()->{try{if(owned!=null){owned.discard();owned=null;}long deadline=SystemClock.elapsedRealtime()+30000;ReportShareData data;
            try(Store store=records.open(app)){data=ReportShareData.read(store,record,notes,()->live(ticket,expected)&&SystemClock.elapsedRealtime()<deadline);}
            owned=ReportShareFiles.stage(app,data,expected,()->live(ticket,expected)&&SystemClock.elapsedRealtime()<deadline);ReportShareFiles.Snapshot prepared=owned;
            main.post(()->{if(live(ticket,expected))publish(new State(Phase.READY,notes,prepared,notice));else checkHistory();});
        }catch(Exception failure){String text=notice(failure);main.post(()->{if(live(ticket,expected))publish(new State(Phase.ERROR,notes,null,text));else checkHistory();});}});
    }
    private static String notice(Exception failure){String text=failure.getMessage();return text!=null&&text.length()<260&&(failure instanceof IOException||failure instanceof IllegalArgumentException||failure instanceof IllegalStateException)?text:"This preview could not be prepared. Nothing was shared.";}
    void verifyShare(){
        checkHistory();State before=state;if(closed||before.phase!=Phase.READY||before.snapshot==null)return;int ticket=++generation;long expected=epoch;
        publish(new State(Phase.VERIFYING,before.notes,before.snapshot,"Checking this exact preview against the saved record…"));
        worker.execute(()->{try{long deadline=SystemClock.elapsedRealtime()+30000;ReportShareData fresh;
            try(Store store=records.open(app)){fresh=ReportShareData.read(store,record,before.notes,()->live(ticket,expected)&&SystemClock.elapsedRealtime()<deadline);}
            if(!fresh.fingerprint.equals(before.snapshot.data.fingerprint)||!java.util.Arrays.equals(fresh.bytes,before.snapshot.data.bytes))throw new IOException("This record changed after preview. Prepare it again before sharing.");
            before.snapshot.verify(()->live(ticket,expected)&&SystemClock.elapsedRealtime()<deadline);
            ReportShareFiles.prepareAdmission(app,before.snapshot,()->live(ticket,expected)&&SystemClock.elapsedRealtime()<deadline);
            main.post(()->{if(live(ticket,expected))publish(new State(Phase.VERIFIED,before.notes,before.snapshot,""));else checkHistory();});
        }catch(Exception failure){String text=notice(failure);main.post(()->{if(live(ticket,expected))publish(new State(Phase.ERROR,before.notes,before.snapshot,text));else checkHistory();});}});
    }
    /** Called only by the visible owner following the explicit Share button. No grants are issued here. */
    Uri admit(State reviewed){checkHistory();if(closed||reviewed!=state||reviewed.phase!=Phase.VERIFIED||reviewed.snapshot==null)throw new IllegalStateException("Review the current preview before sharing.");return ReportShareFiles.admit(app,reviewed.snapshot);}
    void chooserOpened(State reviewed){if(closed||reviewed!=state||reviewed.phase!=Phase.VERIFIED)return;reviewed.snapshot.shared=true;publish(new State(Phase.READY,reviewed.notes,reviewed.snapshot,"Sharing options opened. Returning here does not confirm delivery."));}
    void chooserFailed(State reviewed){if(!closed&&reviewed.snapshot!=null)worker.execute(()->{try{ReportShareFiles.revoke(app,reviewed.snapshot);}catch(RuntimeException unavailable){/* Error stays explicit; durable expiry/history invalidation still fence reads. */}});if(!closed)publish(new State(Phase.ERROR,reviewed.notes,null,"Android sharing could not be opened. Nothing was sent. Prepare the preview again."));}
    void cancelShare(){checkHistory();if(closed||state.phase!=Phase.VERIFYING&&state.phase!=Phase.VERIFIED)return;ReportShareFiles.Snapshot pending=state.snapshot;generation++;worker.execute(()->ReportShareFiles.cancelPrepared(app,pending));publish(new State(Phase.READY,state.notes,state.snapshot,"Sharing paused when this screen lost focus. Review, then tap Share again."));}
    void close(){if(closed)return;closed=true;generation++;observer.clear();worker.execute(()->{if(owned!=null){owned.discard();owned=null;}});worker.shutdown();}
    void awaitClosed(long millis)throws InterruptedException{if(!worker.awaitTermination(millis,TimeUnit.MILLISECONDS))throw new IllegalStateException("Private report reader still active.");}
}
