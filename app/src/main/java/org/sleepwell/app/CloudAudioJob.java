package org.sleepwell.app;

import android.content.*;
import android.os.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;

/** Process-retained explicit save; persisted receipts survive it, but uploads never resume automatically. */
final class CloudAudioJob {
    enum Phase {LOADING,READY,SAVING,CANCELLING,DONE,ERROR,CLEANUP}
    static final class State {
        final Phase phase;final LibraryAudioArchive.Plan plan;final CloudBackupSettings.Destination destination;final List<CloudAudioVersions.Entry> history;final String message;
        State(Phase phase,LibraryAudioArchive.Plan plan,CloudBackupSettings.Destination destination,List<CloudAudioVersions.Entry> history,String message){this.phase=phase;this.plan=plan;this.destination=destination;this.history=List.copyOf(history);this.message=message;}
        boolean busy(){return phase==Phase.LOADING||phase==Phase.SAVING||phase==Phase.CANCELLING;}boolean writing(){return phase==Phase.SAVING||phase==Phase.CANCELLING;}
    }
    private static final Map<Records.Factory,CloudAudioJob> jobs=new IdentityHashMap<>();
    static synchronized CloudAudioJob get(Context app,Records.Factory records){CloudAudioJob existing=jobs.get(records);if(existing!=null)return existing;CloudAudioJob created=new CloudAudioJob(app.getApplicationContext(),records,CloudAudioVersions.factory,CloudBackupSettings.factory);jobs.put(records,created);return created;}
    final Context app;final Records.Factory records;final CloudAudioVersions.Factory preferences;final CloudBackupSettings.Factory destinations;private final ExecutorService worker=Executors.newSingleThreadExecutor(r->new Thread(r,"cloud-audio-backup"));private final Handler main=new Handler(Looper.getMainLooper());
    private volatile State state=new State(Phase.LOADING,null,null,List.of(),"Reading private audio-version receipts…");private volatile boolean cancelled;private volatile CloudAudioSave activeSave;private volatile long generation;
    private CloudAudioJob(Context app,Records.Factory records,CloudAudioVersions.Factory prefs,CloudBackupSettings.Factory destinations){this.app=app;this.records=records;preferences=prefs;this.destinations=destinations;load();}
    static synchronized void versionsChanged(){CloudAudioJob owned=jobs.get(Records.PRODUCTION);if(owned!=null)owned.main.post(()->{if(!owned.state().busy()&&owned.state().phase!=Phase.CLEANUP)owned.refresh();});}
    static synchronized void historyErasing(){CloudAudioJob owned=jobs.get(Records.PRODUCTION);if(owned!=null&&owned.state().writing())owned.cancel();}
    State state(){return state;}
    private boolean scope(){return records==Records.PRODUCTION||preferences!=CloudAudioVersions.PRODUCTION&&destinations!=CloudBackupSettings.PRODUCTION;}
    private void publish(Phase phase,LibraryAudioArchive.Plan plan,CloudBackupSettings.Destination destination,List<CloudAudioVersions.Entry> history,String message){main.post(()->state=new State(phase,plan,destination,history,message));}
    void refresh(){if(state.busy()||state.phase==Phase.CLEANUP)return;state=new State(Phase.LOADING,null,state.destination,state.history,"Refreshing actual library counts…");load();}
    private void load(){long own=++generation;worker.execute(()->{LibraryAudioArchive.Plan plan=null;CloudBackupSettings.Destination destination=null;List<CloudAudioVersions.Entry> history=List.of();String error="";try{if(!scope())throw new IOException("Audio history must use the same private data scope as its destination.");history=CloudAudioVersions.inspect(preferences.open(app));destination=CloudBackupSettings.destination(destinations.preferences(app));try(Store store=records.open(app)){plan=LibraryAudioArchive.prepare(app,store,()->own==generation);}if(plan.audio.isEmpty())error="No saved audio recordings. Analysis-only events do not contain audio files.";if(destination.tree.isEmpty())error="Choose a remembered read/write folder in Cloud backup first.";else if(!CloudBackupProvider.granted(app.getContentResolver(),android.net.Uri.parse(destination.tree)))error="Folder access is unavailable. Re-select the destination in Cloud backup.";}catch(Exception failure){error=notice(failure);}if(own!=generation)return;publish(plan==null?Phase.ERROR:Phase.READY,plan,destination,history,error);});}
    void save(){State reviewed=state;if(!AutoCloudAudio.historyAllowed()){state=new State(Phase.ERROR,reviewed.plan,reviewed.destination,reviewed.history,"Finish history erasure, then refresh the audio review before saving.");return;}if(reviewed.busy()||reviewed.phase==Phase.CLEANUP||reviewed.plan==null||reviewed.plan.audio.isEmpty()||reviewed.destination==null||reviewed.destination.tree.isEmpty())return;cancelled=false;long own=++generation;state=new State(Phase.SAVING,reviewed.plan,reviewed.destination,reviewed.history,"Staging the complete reviewed library privately…");worker.execute(()->{try{saveOwned(reviewed,own);}catch(Exception failure){publish(Phase.ERROR,reviewed.plan,reviewed.destination,reviewed.history,notice(failure));}});}
    private void saveOwned(State reviewed,long own){CloudAudioSave work=new CloudAudioSave(app,records,preferences,destinations);activeSave=work;CloudAudioSave.Result result;try{result=work.save(reviewed.plan,reviewed.destination,UUID.randomUUID().toString(),()->!cancelled&&generation==own);}finally{activeSave=null;}List<CloudAudioVersions.Entry> history=reviewed.history;try{history=CloudAudioVersions.read(preferences.open(app));}catch(Exception ignored){}publish(!result.cleanup?Phase.CLEANUP:result.verified?Phase.DONE:Phase.ERROR,reviewed.plan,reviewed.destination,history,result.message);}
    void cancel(){if(!state.writing()||state.phase==Phase.CANCELLING)return;cancelled=true;state=new State(Phase.CANCELLING,state.plan,state.destination,state.history,"Cancelling; waiting for provider calls and stream cleanup…");CloudAudioSave current=activeSave;if(current!=null)current.cancel();}
    static String notice(Exception failure){String value=failure.getMessage();return value==null||value.isBlank()||value.length()>500?"Audio backup could not finish. Check the selected folder and library before retrying.":value;}
}
