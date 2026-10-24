package org.sleepwell.app;

import android.app.job.*;
import android.os.CancellationSignal;
import java.util.concurrent.*;

/** System-owned calendar work. It never starts an alarm or reads microphone data. */
public final class CalendarSyncJob extends JobService {
    private final ConcurrentHashMap<JobParameters,CancellationSignal> work=new ConcurrentHashMap<>();
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    @Override public boolean onStartJob(JobParameters params){CancellationSignal cancel=new CancellationSignal();work.put(params,cancel);worker.execute(()->{try{CalendarSync.refresh(getApplicationContext(),cancel);}finally{work.remove(params);if(params.getJobId()==CalendarSync.CHANGES)try{if(CalendarSettings.load(this).enabled)CalendarSync.observe(this);}catch(RuntimeException ignored){}jobFinished(params,false);}});return true;}
    @Override public boolean onStopJob(JobParameters params){CancellationSignal cancel=work.remove(params);if(cancel!=null)cancel.cancel();return true;}
    @Override public void onDestroy(){for(CancellationSignal cancel:work.values())cancel.cancel();worker.shutdown();super.onDestroy();}
}
