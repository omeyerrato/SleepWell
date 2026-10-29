package org.sleepwell.app;

import android.content.Context;
import java.util.*;
import java.util.concurrent.*;

/** Process-retained local deletion owner; no Activity, provider, player or open database is retained. */
final class AudioClearJob {
    static final class State {
        final boolean busy;final SoundClips.ClearPlan plan;final int pending;final String message;
        State(boolean busy,SoundClips.ClearPlan plan,int pending,String message){this.busy=busy;this.plan=plan;this.pending=pending;this.message=message;}
    }
    private static final Map<Records.Factory,AudioClearJob> jobs=new IdentityHashMap<>();
    static synchronized AudioClearJob get(Context context,Records.Factory factory){return jobs.computeIfAbsent(factory,key->new AudioClearJob(context.getApplicationContext(),key));}
    private final Context app;private final Records.Factory factory;private final ExecutorService worker=Executors.newSingleThreadExecutor(r->new Thread(r,"privacy-audio-clear"));
    private volatile State state=new State(false,null,0,"");
    private volatile SoundClips.ClearPlan pendingSelection;
    private AudioClearJob(Context app,Records.Factory factory){this.app=app;this.factory=factory;}
    State state(){return state;}
    synchronized void review(boolean keepFavorites){if(state.busy)return;State previous=state;state=new State(true,null,previous.pending,"Reading the actual local audio selection…");worker.execute(()->{
        try(Store store=factory.open(app)){SoundClips.ClearPlan plan;
            if(pendingSelection!=null){synchronized(SoundClips.LOCK){long admission=BackupRuntime.generation();BackupRuntime.requireIdle(app,admission);List<org.json.JSONObject> queued=new ArrayList<>();for(org.json.JSONObject expected:pendingSelection.clips){List<org.json.JSONObject> rows=store.rows("SELECT clip AS id,session,path,time FROM pending_clip_deletes WHERE clip=?",""+expected.optLong("id"));if(!rows.isEmpty()){SoundClips.identity(rows.get(0),expected);queued.add(rows.get(0));}}if(queued.isEmpty()){pendingSelection=null;plan=SoundClips.reviewClear(app,store,keepFavorites);}else plan=new SoundClips.ClearPlan(queued,true,true,admission);}}
            else plan=SoundClips.reviewClear(app,store,keepFavorites);
            state=new State(false,plan,plan.retry?plan.clips.size():0,plan.clips.size()+(plan.clips.size()==1?" recording selected.":" recordings selected.")+(plan.retry?" Only previously queued audio will be retried.":plan.keepFavorites?" Starred recordings and starred sound events are kept.":" Favorites are included."));
        }catch(Exception failure){state=new State(false,null,previous.pending,notice(failure));}
    });}
    synchronized void confirm(State reviewed){if(state!=reviewed||reviewed.busy||reviewed.plan==null||reviewed.plan.clips.isEmpty())return;pendingSelection=reviewed.plan;state=new State(true,reviewed.plan,reviewed.pending,"Removing selected local audio…");worker.execute(()->{
        try(Store store=factory.open(app)){int pending=SoundClips.clearReviewed(app,store,reviewed.plan);if(pending==0)pendingSelection=null;state=new State(false,reviewed.plan,pending,pending>0?pending+" selected recordings need cleanup. Retry when storage is available.":"Selected audio removed. Sleep records, notes, alarms and sound estimates remain.");}
        catch(Exception failure){state=new State(false,reviewed.plan,reviewed.pending,notice(failure));}
    });}
    private static String notice(Exception failure){String text=failure.getMessage();return text==null||text.isBlank()?"Audio deletion could not finish. Review and retry.":text;}
    static void drain(Records.Factory factory,long timeout)throws Exception{AudioClearJob job;synchronized(AudioClearJob.class){job=jobs.get(factory);}if(job!=null)job.worker.submit(()->{}).get(timeout,TimeUnit.MILLISECONDS);}
    static synchronized void forget(Records.Factory factory){AudioClearJob job=jobs.get(factory);if(job!=null&&job.state.busy)throw new IllegalStateException("Audio cleanup is still active");jobs.remove(factory);if(job!=null)job.worker.shutdown();}
}
