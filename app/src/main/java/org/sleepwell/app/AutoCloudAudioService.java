package org.sleepwell.app;
import android.app.job.*;import android.os.*;import java.util.*;import java.util.concurrent.atomic.AtomicBoolean;
/** Android-deferrable, charging/unmetered whole-library save; no foreground-service or capture admission. */
public final class AutoCloudAudioService extends JobService {
 private final Map<JobParameters,AtomicBoolean> runs=new IdentityHashMap<>();private final Handler main=new Handler(Looper.getMainLooper());
 @Override public boolean onStartJob(JobParameters parameters){String token=parameters.getExtras().getString("owner",""),request=parameters.getExtras().getString("request","");if(token.isEmpty()||request.isEmpty())return false;AtomicBoolean cancelled=new AtomicBoolean();runs.put(parameters,cancelled);Runnable timeout=()->{if(runs.get(parameters)==cancelled)AutoCloudAudio.cancel(cancelled);};main.postDelayed(timeout,8*60000L);AutoCloudAudio.run(this,token,request,cancelled,()->main.post(()->{main.removeCallbacks(timeout);if(runs.remove(parameters)!=null)jobFinished(parameters,false);}));return true;}
 @Override public void onDestroy(){for(AtomicBoolean cancelled:runs.values())AutoCloudAudio.cancel(cancelled);runs.clear();main.removeCallbacksAndMessages(null);super.onDestroy();}
 @Override public boolean onStopJob(JobParameters parameters){AtomicBoolean cancelled=runs.remove(parameters);if(cancelled!=null)AutoCloudAudio.cancel(cancelled);return false;}
}
