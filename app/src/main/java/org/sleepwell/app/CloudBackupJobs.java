package org.sleepwell.app;

import android.content.Context;
import android.os.*;
import java.util.*;
import java.util.concurrent.*;

final class CloudBackupJobs {
    interface Factory {CloudBackupJob.Work create(Context context,Records.Factory records);}
    static Factory factory=CloudBackupWork::new;
    private static final Map<Records.Factory,Entry> entries=new IdentityHashMap<>();private static final Handler main=new Handler(Looper.getMainLooper());
    private static final class Entry {final ExecutorService worker=Executors.newSingleThreadExecutor(r->new Thread(r,"cloud-backup-worker"));final CloudBackupJob job;Entry(Context context,Records.Factory records){job=new CloudBackupJob(factory.create(context.getApplicationContext(),records),worker,main::post);}}
    static synchronized CloudBackupJob get(Context context,Records.Factory records){Entry entry=entries.get(records);if(entry==null){entry=new Entry(context,records);entries.put(records,entry);}return entry.job;}
    static void drain(Records.Factory records,long timeout)throws Exception{Entry entry;synchronized(CloudBackupJobs.class){entry=entries.get(records);}if(entry!=null)entry.worker.submit(()->{}).get(timeout,TimeUnit.MILLISECONDS);}
    static synchronized void forget(Records.Factory records){Entry entry=entries.get(records);if(entry!=null&&entry.job.state().busy())throw new IllegalStateException("Provider work is still active");entries.remove(records);if(entry!=null)entry.worker.shutdown();}
}
