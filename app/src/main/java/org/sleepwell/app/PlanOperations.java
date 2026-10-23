package org.sleepwell.app;

import android.content.Context;
import java.util.concurrent.*;

/** Persisted actions continue across Activity recreation without holding an Activity. */
final class PlanOperations {
    interface Task {String run(Context app) throws Exception;}
    static final class Operation {volatile boolean done;volatile String error,notice;}
    private static final ExecutorService worker=Executors.newSingleThreadExecutor();
    private static final ConcurrentMap<String,Operation> operations=new ConcurrentHashMap<>();
    static Operation start(Context c,String token,Task task){Operation created=new Operation(),existing=operations.putIfAbsent(token,created);if(existing!=null)return existing;Context app=c.getApplicationContext();worker.execute(()->{try{created.notice=task.run(app);}catch(Exception e){created.error=e.getMessage()==null?"Storage unavailable.":e.getMessage();}finally{created.done=true;}});return created;}
    static java.util.Set<String> tokens(){return new java.util.HashSet<>(operations.keySet());}
    static void drain(long timeoutMs) throws Exception {worker.submit(()->{}).get(timeoutMs,TimeUnit.MILLISECONDS);}
    static void forget(String token){operations.remove(token);}
}
