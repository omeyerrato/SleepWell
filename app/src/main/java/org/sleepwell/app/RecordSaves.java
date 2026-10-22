package org.sleepwell.app;

import android.content.Context;
import java.time.ZoneId;
import java.util.concurrent.*;

/** Save ownership outlives an Activity. Persistent create tokens also cover process recreation. */
final class RecordSaves {
    private static final ExecutorService worker=Executors.newSingleThreadExecutor();
    private static final ConcurrentMap<String,Operation> operations=new ConcurrentHashMap<>();
    static final class Operation {volatile boolean done;volatile String error;}
    static Operation start(Context context,String token,String createToken,long id,RecordDraft draft,ZoneId zone,long revision){
        Operation operation=new Operation(),existing=operations.putIfAbsent(token,operation);if(existing!=null)return existing;
        Context app=context.getApplicationContext();Records.Factory factory=Records.factory;RecordDraft copy=draft.copy();
        worker.execute(()->{try(Store store=factory.open(app)){if(id==0)Records.create(store,copy,zone,System.currentTimeMillis(),createToken);else Records.save(store,id,copy,revision,System.currentTimeMillis());}catch(Exception e){operation.error=e.getMessage()==null?"Storage unavailable.":e.getMessage();}finally{operation.done=true;}});return operation;
    }
    static void forget(String token){operations.remove(token);}
}
