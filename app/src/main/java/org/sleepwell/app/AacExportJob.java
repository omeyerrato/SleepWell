package org.sleepwell.app;

import android.content.Context;
import android.net.Uri;
import android.os.*;
import java.io.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/** Retained review/SAF state. A picker result alone cannot revive a cancelled or recreated job. */
final class AacExportJob implements AutoCloseable {
    enum Phase {LOADING,READY,CHOOSING,WRITING,CANCELLING,DONE,ERROR}
    interface Observer {void changed();}
    private static final Executor CANCEL=new ThreadPoolExecutor(0,1,10,TimeUnit.SECONDS,new ArrayBlockingQueue<>(2),r->{Thread t=new Thread(r,"aac-export-cancel");t.setDaemon(true);return t;},new ThreadPoolExecutor.DiscardPolicy());
    final Context app;final Records.Factory records;final long record;final boolean favorites;final Set<Integer> selection=new LinkedHashSet<>();
    final ExecutorService worker=Executors.newSingleThreadExecutor();final Handler main=new Handler(Looper.getMainLooper());final AtomicReference<CancellationSignal> signal=new AtomicReference<>();final AtomicReference<ParcelFileDescriptor> descriptor=new AtomicReference<>();
    private WeakReference<Observer> observer=new WeakReference<>(null);volatile boolean closed;volatile int generation;Phase phase=Phase.LOADING;AacArchive.Plan plan;String message="";boolean bundle;
    AacExportJob(Context context,Records.Factory records,long record,boolean favorites){app=context.getApplicationContext();this.records=records;this.record=record;this.favorites=favorites;refresh();}
    void observe(Observer target){observer=new WeakReference<>(target);target.changed();}void detach(Observer target){if(observer.get()==target)observer.clear();}
    private void publish(){if(closed)return;Observer target=observer.get();if(target!=null)target.changed();}
    private boolean active(int request){return !closed&&generation==request&&!Thread.currentThread().isInterrupted();}
    boolean editable(){return plan!=null&&(phase==Phase.READY||phase==Phase.DONE||phase==Phase.ERROR);}
    void refresh(){if(closed||phase==Phase.WRITING||phase==Phase.CANCELLING||phase==Phase.CHOOSING)return;int request=++generation;phase=Phase.LOADING;message="Reading saved recording intervals…";plan=null;selection.clear();publish();worker.execute(()->{try(Store store=records.open(app)){AacArchive.Plan result=AacArchive.prepare(app,store,record,favorites,()->active(request));main.post(()->{if(!active(request))return;plan=result;for(int i=0;i<result.parts.size();i++)selection.add(i);phase=Phase.READY;message=result.parts.isEmpty()?"No audio remains inside the effective recorded periods.":"";publish();});}catch(Exception failure){main.post(()->{if(!active(request))return;phase=Phase.ERROR;message=reason(failure,"Recordings could not be prepared.");publish();});}});}
    void toggle(int index){if(!editable()||index<0||index>=plan.parts.size())return;if(!selection.remove(index))selection.add(index);phase=Phase.READY;message="";publish();}
    void selectAll(boolean value){if(!editable())return;selection.clear();if(value)for(int i=0;i<plan.parts.size();i++)selection.add(i);publish();}
    boolean choose(){if(!editable()||selection.isEmpty())return false;bundle=selection.size()>1;phase=Phase.CHOOSING;message="Choose a save location in Android.";publish();return true;}
    void cancelChoice(){if(closed||phase!=Phase.CHOOSING)return;phase=Phase.READY;message="No export was started.";publish();}
    void pickerError(){if(phase==Phase.CHOOSING){phase=Phase.ERROR;message="Android Files is unavailable. Enable a document picker and try again.";publish();}}
    void write(Uri destination){if(closed||phase!=Phase.CHOOSING)return;if(destination==null||!"content".equals(destination.getScheme())||destination.getAuthority()==null){phase=Phase.ERROR;message="Choose an Android document destination.";publish();return;}
        final int request=++generation;final AacArchive.Plan reviewed=plan;final List<Integer> selected=new ArrayList<>(selection);Collections.sort(selected);phase=Phase.WRITING;message="Preparing a private copy. Original WAV files are unchanged.";publish();
        worker.execute(()->{try{
            AacArchive.Snapshot snapshot;try(Store store=records.open(app)){snapshot=AacArchive.stage(app,store,reviewed,record,favorites,selected,()->active(request));}
            try(AacArchive.Snapshot staged=snapshot){staged.encode(()->active(request),done->main.post(()->{if(active(request)){message="Encoded "+done+" of "+selected.size()+" audio parts…";publish();}}));NightAudioArchive.active(()->active(request));
                CancellationSignal cancellation=new CancellationSignal();signal.set(cancellation);try{NightAudioArchive.active(()->active(request));ParcelFileDescriptor file=app.getContentResolver().openFileDescriptor(destination,"wt",cancellation);if(file==null)throw new IOException("The chosen destination is unavailable.");descriptor.set(file);try(OutputStream output=new ParcelFileDescriptor.AutoCloseOutputStream(file)){NightAudioArchive.active(()->active(request));staged.write(output,()->active(request));output.flush();}finally{descriptor.compareAndSet(file,null);}}finally{signal.compareAndSet(cancellation,null);}
            }
            main.post(()->{if(!active(request))return;phase=Phase.DONE;message="Export complete: "+selected.size()+" audio part"+(selected.size()==1?".":"s. Originals are unchanged.");publish();});
        }catch(Exception failure){main.post(()->{if(!active(request))return;phase=Phase.ERROR;message=reason(failure,"Export failed.")+" The destination may contain an empty or partial file. Original recordings are unchanged.";publish();});}
        finally{main.post(()->{if(!closed&&phase==Phase.CANCELLING){phase=Phase.READY;message="Export cancelled. Remove any empty or partial destination file.";publish();}});}});
    }
    void cancelExport(){if(closed||phase!=Phase.WRITING)return;generation++;phase=Phase.CANCELLING;message="Cancelling and closing the current export…";cancelProvider();publish();}
    private void cancelProvider(){CancellationSignal cancellation=signal.get();ParcelFileDescriptor file=descriptor.get();CANCEL.execute(()->{if(cancellation!=null)try{cancellation.cancel();}catch(RuntimeException ignored){}if(file!=null)try{file.close();}catch(IOException ignored){}});}
    private static String reason(Exception error,String fallback){return error instanceof IOException&&error.getMessage()!=null?error.getMessage():fallback;}
    @Override public void close(){if(closed)return;closed=true;generation++;observer.clear();cancelProvider();worker.shutdownNow();}
}
