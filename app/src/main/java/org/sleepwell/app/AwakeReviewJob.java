package org.sleepwell.app;
import android.content.Context;import android.os.*;import java.util.concurrent.*;
/** Retained immutable command and pinned private storage factory. */
final class AwakeReviewJob {
 interface Observer {void changed();}
 final Context app;final Records.Factory factory;final long id;final ExecutorService worker=Executors.newSingleThreadExecutor();final Handler main=new Handler(Looper.getMainLooper());Observer observer;RecordEntry record;String error="";boolean busy,closed;
 AwakeReviewJob(Context c,long id){app=c.getApplicationContext();factory=Records.factory;this.id=id;}
 void observe(Observer value){observer=value;value.changed();if(record==null&&!busy&&error.isEmpty())load();}void detach(Observer value){if(observer==value)observer=null;}private void notifyUi(){if(!closed&&observer!=null)observer.changed();}
 void load(){run(0,false,false);}
 void review(long event,boolean ignored){run(event,ignored,true);}
 private void run(long event,boolean ignored,boolean change){if(busy||closed)return;busy=true;error="";RecordEntry expected=record;notifyUi();worker.execute(()->{RecordEntry next=null;String failed="";try(Store store=factory.open(app)){if(change){if(expected==null)throw new IllegalArgumentException("Load this record first.");AwakeStore.review(store,id,event,expected.awake.revision,expected.updated,ignored);}next=Records.get(store,id);}catch(RuntimeException failure){failed=failure.getMessage()==null?"Awake review unavailable.":failure.getMessage();}RecordEntry result=next;String message=failed;main.post(()->{busy=false;if(result!=null)record=result;error=message;notifyUi();});});}
 void close(){closed=true;observer=null;worker.shutdown();}
 boolean await(long timeout)throws InterruptedException{worker.shutdown();return worker.awaitTermination(timeout,TimeUnit.MILLISECONDS);}
}
