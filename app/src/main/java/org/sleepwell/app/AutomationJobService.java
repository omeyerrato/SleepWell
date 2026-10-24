package org.sleepwell.app;

import android.app.job.*;
import android.os.Handler;
import android.os.Looper;
import java.util.IdentityHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/** Durable bounded queue, quota-controlled by Android. Never starts sensors or audio. */
public final class AutomationJobService extends JobService {
    private final IdentityHashMap<JobParameters,AtomicBoolean> runs=new IdentityHashMap<>();
    @Override public boolean onStartJob(JobParameters parameters){AtomicBoolean cancelled=new AtomicBoolean();runs.put(parameters,cancelled);Automations.runningJobs.incrementAndGet();Automations.io.execute(()->{boolean storageFailed=false;try(AutomationStore store=new AutomationStore(this)){AutomationDelivery.Transport transport=new AutomationTransports(this);long deadline=android.os.SystemClock.elapsedRealtime()+90000;for(int n=0;n<10&&!cancelled.get()&&android.os.SystemClock.elapsedRealtime()<deadline;n++)if(!AutomationDispatch.next(store,transport,System.currentTimeMillis(),System::currentTimeMillis,cancelled::get))break;}catch(Exception ignored){storageFailed=true;getSharedPreferences("automation_status",0).edit().putString("error","Automation storage needs attention. Reopen Automations.").apply();}finally{boolean retry=storageFailed;new Handler(Looper.getMainLooper()).post(()->{runs.remove(parameters);Automations.runningJobs.decrementAndGet();if(!cancelled.get())jobFinished(parameters,retry);if(!retry&&!cancelled.get())Automations.recover(getApplicationContext());});}});return true;}
    @Override public boolean onStopJob(JobParameters parameters){AtomicBoolean cancelled=runs.get(parameters);if(cancelled!=null)cancelled.set(true);return true;}
}
