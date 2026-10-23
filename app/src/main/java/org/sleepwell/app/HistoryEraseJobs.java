package org.sleepwell.app;

import java.util.*;
import java.util.concurrent.Executor;

/** Retains results by storage-factory identity, without retaining an Activity or an open database. */
final class HistoryEraseJobs<K> {
    enum Kind { INSPECT, ERASE, CLEAN }
    interface Task {int run() throws Exception;}
    static final class Job {
        final Kind kind;volatile boolean done,erased;volatile int pending;volatile String error="";
        Job(Kind kind,boolean erased){this.kind=kind;this.erased=erased;}
    }
    private final Map<K,Job> jobs=new IdentityHashMap<>();private final Executor executor;
    HistoryEraseJobs(Executor executor){this.executor=executor;}
    synchronized Job get(K key){return jobs.get(key);}
    synchronized Job start(K key,Kind kind,Task task){
        Job previous=jobs.get(key);if(previous!=null&&!previous.done)return previous;
        Job job=new Job(kind,previous!=null&&previous.erased);job.pending=previous==null?0:previous.pending;jobs.put(key,job);
        try{executor.execute(()->{try{job.pending=task.run();if(kind==Kind.ERASE)job.erased=true;}catch(Exception failure){job.error=failure.getMessage()==null?"History cleanup is unavailable. Try again.":failure.getMessage();}finally{job.done=true;}});}catch(RuntimeException failure){job.error="History cleanup could not start. Try again.";job.done=true;}
        return job;
    }
    synchronized void forget(K key){Job job=jobs.get(key);if(job!=null&&!job.done)throw new IllegalStateException("History cleanup is still running");jobs.remove(key);}
}
