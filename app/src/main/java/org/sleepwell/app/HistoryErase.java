package org.sleepwell.app;

import android.content.Context;
import java.util.concurrent.*;

/** Database-first cleanup on a pinned storage scope, including private day confirmations. */
final class HistoryErase {
    private static final ExecutorService worker=Executors.newSingleThreadExecutor();
    static final HistoryEraseJobs<Records.Factory> jobs=new HistoryEraseJobs<>(worker);
    static HistoryEraseJobs.Job inspect(Context context,Records.Factory factory){return start(context,factory,HistoryEraseJobs.Kind.INSPECT);}
    static HistoryEraseJobs.Job start(Context context,Records.Factory factory,HistoryEraseJobs.Kind kind){
        Context app=context.getApplicationContext();return jobs.start(factory,kind,()->{try(Store store=factory.open(app)){
            if(kind==HistoryEraseJobs.Kind.ERASE){if(TrackingService.running)throw new IllegalStateException("End tracking before deleting history.");SoundClips.eraseHistory(app,store,()->{if(factory==Records.PRODUCTION)SRIReviews.erase(app);});}
            else if(kind==HistoryEraseJobs.Kind.CLEAN)SoundClips.recover(app,store);
            return store.rows("SELECT clip FROM pending_clip_deletes").size();
        }});
    }
    static void drain(long timeoutMs)throws Exception{worker.submit(()->{}).get(timeoutMs,TimeUnit.MILLISECONDS);}
}
