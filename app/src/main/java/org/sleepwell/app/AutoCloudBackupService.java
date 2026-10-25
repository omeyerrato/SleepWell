package org.sleepwell.app;
import android.app.job.*;import android.os.*;import java.util.*;import java.util.concurrent.atomic.AtomicBoolean;
/** Deferrable metadata only; no account, network, sensor or output APIs. */
public final class AutoCloudBackupService extends JobService {
 final Map<JobParameters,AtomicBoolean> runs=new IdentityHashMap<>();final Handler main=new Handler(Looper.getMainLooper());
 @Override public boolean onStartJob(JobParameters p){String token=p.getExtras().getString("owner","");if(token.isEmpty())return false;AtomicBoolean cancelled=new AtomicBoolean();runs.put(p,cancelled);Runnable timeout=()->{if(runs.get(p)==cancelled){AutoCloudBackups.cancel(cancelled);}};main.postDelayed(timeout,90000);AutoCloudBackups.run(this,token,cancelled,()->main.post(()->{main.removeCallbacks(timeout);if(runs.remove(p)!=null)jobFinished(p,false);}));return true;}
 @Override public boolean onStopJob(JobParameters p){AtomicBoolean cancel=runs.remove(p);if(cancel!=null){AutoCloudBackups.cancel(cancel);}return false;}
}
