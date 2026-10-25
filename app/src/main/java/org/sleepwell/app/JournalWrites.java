package org.sleepwell.app;
import android.content.Context;import java.util.*;import java.util.concurrent.*;import java.util.function.BooleanSupplier;
/** Captured private database owner and occurrence/unit fencing; no view/Activity is retained. */
final class JournalWrites {
 static final ExecutorService worker=Executors.newSingleThreadExecutor();static final Map<String,Operation> operations=new LinkedHashMap<>();
 static final class Operation {volatile boolean done;volatile String id="",error="";}
 static synchronized Operation start(Context c,String key,String text,int minimum,BooleanSupplier current){Operation old=operations.get(key);if(old!=null)return old;while(operations.size()>64){String remove=operations.entrySet().stream().filter(e->e.getValue().done).map(Map.Entry::getKey).findFirst().orElse(null);if(remove==null)throw new IllegalStateException("Journal writes are busy.");operations.remove(remove);}Operation op=new Operation();operations.put(key,op);Context app=c.getApplicationContext();DiaryStore.Factory factory=DiaryStore.factory;worker.execute(()->{try(DiaryStore store=factory.open(app)){op.id=store.challenge(key,text,minimum,System.currentTimeMillis(),current);}catch(Exception failure){op.error=!op.id.isEmpty()?"Entry saved, but confirmation needs retry. Your entry will not be duplicated.":failure.getMessage()==null?"Journal entry could not be saved.":failure.getMessage();}finally{op.done=true;}});return op;}
 static synchronized void forget(String key){Operation op=operations.get(key);if(op!=null&&op.done)operations.remove(key);}static void drain()throws Exception{worker.submit(()->{}).get(10,TimeUnit.SECONDS);}
 private JournalWrites(){}
}
