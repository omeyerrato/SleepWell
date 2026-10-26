package org.sleepwell.app;
import android.app.job.*;import android.os.*;import java.util.*;import java.util.concurrent.atomic.AtomicBoolean;
/** Deferrable, explicitly consented calendar insertion; no foreground/sensor/output authority. */
public final class AutoCalendarService extends JobService {
 private final Map<JobParameters,AtomicBoolean> runs=new IdentityHashMap<>();private final Handler main=new Handler(Looper.getMainLooper());
 @Override public boolean onStartJob(JobParameters p){long revision=p.getExtras().getLong("revision",0);if(revision<=0)return false;AtomicBoolean cancelled=new AtomicBoolean();runs.put(p,cancelled);Runnable timeout=()->{if(runs.remove(p)==cancelled){AutoCalendar.interrupted(this,cancelled);jobFinished(p,false);}};main.postDelayed(timeout,90000);AutoCalendar.run(this,revision,cancelled,()->main.post(()->{main.removeCallbacks(timeout);if(runs.remove(p)!=null)jobFinished(p,false);}));return true;}
 @Override public boolean onStopJob(JobParameters p){AtomicBoolean cancelled=runs.remove(p);if(cancelled!=null)AutoCalendar.interrupted(this,cancelled);return false;}
 @Override public void onDestroy(){for(AtomicBoolean cancelled:runs.values())AutoCalendar.interrupted(this,cancelled);runs.clear();main.removeCallbacksAndMessages(null);super.onDestroy();}
}
