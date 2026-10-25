package org.sleepwell.app;

import android.content.Context;
import java.util.concurrent.*;

/** Database-first cleanup on a pinned storage scope, including private day confirmations. */
final class HistoryErase {
    private static final ExecutorService worker=Executors.newSingleThreadExecutor();
    static final HistoryEraseJobs<Records.Factory> jobs=new HistoryEraseJobs<>(worker);
    static HistoryEraseJobs.Job inspect(Context context,Records.Factory factory){return start(context,factory,HistoryEraseJobs.Kind.INSPECT);}
    static HistoryEraseJobs.Job start(Context context,Records.Factory factory,HistoryEraseJobs.Kind kind){
        Context app=context.getApplicationContext();return jobs.start(factory,kind,()->{boolean audioGate=false;try{
            if(kind==HistoryEraseJobs.Kind.ERASE&&factory==Records.PRODUCTION){if(TrackingService.running)throw new IllegalStateException("End tracking before deleting history.");audioGate=true;AutoCloudAudio.historyErasing(app);}
            try(Store store=factory.open(app)){
            if(kind==HistoryEraseJobs.Kind.ERASE){if(TrackingService.running)throw new IllegalStateException("End tracking before deleting history.");if(factory==Records.PRODUCTION)SleepSummaryFiles.historyChanging(app);SoundClips.eraseHistory(app,store,()->{if(factory==Records.PRODUCTION)erased(app);});}
            else if(kind==HistoryEraseJobs.Kind.CLEAN)SoundClips.recover(app,store);
            return store.rows("SELECT clip FROM pending_clip_deletes").size();
            }
        }finally{if(audioGate)AutoCloudAudio.historyEraseFinished();}});
    }
    private static void erased(Context app){
        RuntimeException failure=null;
        try{SleepSummaryFiles.historyChanging(app);}catch(RuntimeException cleanup){failure=cleanup;}
        try{SRIReviews.erase(app);}catch(RuntimeException cleanup){if(failure==null)failure=cleanup;else failure.addSuppressed(cleanup);}
        if(failure!=null)throw failure;
    }
    static void drain(long timeoutMs)throws Exception{worker.submit(()->{}).get(timeoutMs,TimeUnit.MILLISECONDS);}
}
