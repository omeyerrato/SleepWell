package org.sleepwell.app;

import android.content.Context;
import android.os.*;
import java.util.*;
import java.util.concurrent.*;

/** One bounded worker per pinned storage scope; Activities only observe immutable state. */
final class BackupJobs {
    private static final Map<Records.Factory,Entry> entries=new IdentityHashMap<>();
    private static final Handler main=new Handler(Looper.getMainLooper());
    private static final class Entry {final ExecutorService worker=Executors.newSingleThreadExecutor(r->new Thread(r,"backup-document-worker"));final BackupJob job;Entry(Context context,Records.Factory records){job=new BackupJob(BackupDocuments.factory.create(context.getApplicationContext(),records),worker,main::post);}}
    static synchronized BackupJob get(Context context,Records.Factory records){Entry entry=entries.get(records);if(entry==null){entry=new Entry(context,records);entries.put(records,entry);}return entry.job;}
    static void drain(Records.Factory records,long timeout)throws Exception{Entry entry;synchronized(BackupJobs.class){entry=entries.get(records);}if(entry!=null)entry.worker.submit(()->{}).get(timeout,TimeUnit.MILLISECONDS);}
    static void forget(Records.Factory records){Entry entry;synchronized(BackupJobs.class){entry=entries.get(records);if(entry!=null&&entry.job.state().occupied)throw new IllegalStateException("Document work remains active.");entries.remove(records);}if(entry!=null)entry.worker.shutdown();}
    private BackupJobs(){}
}
