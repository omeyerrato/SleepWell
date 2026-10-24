package org.sleepwell.app;

import org.junit.Test;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import static org.junit.Assert.*;

public class BackupJobTest {
    static final class Queue implements Executor {final java.util.Queue<Runnable> tasks=new ArrayDeque<>();public void execute(Runnable r){tasks.add(r);}void run(){tasks.remove().run();}}
    static final class Work implements BackupJob.Work {int reads,writes,restores,cancels;BooleanSupplier active;boolean fail,failCancel;final BackupJob.Review review=new BackupJob.Review("{}","typed","one record",4);
        public BackupJob.Review read(String uri,BooleanSupplier token)throws Exception{reads++;active=token;if(fail)throw new java.io.IOException("Denied");return review;}
        public void write(BackupJob.Kind kind,String uri,BooleanSupplier token)throws Exception{writes++;active=token;if(fail)throw new java.io.IOException("Denied");}
        public String restore(BackupJob.Review value){assertSame(review,value);restores++;if(fail)throw new IllegalStateException("Stale");return "Restored";}
        public void cancel(){cancels++;if(failCancel)throw new IllegalStateException("Fixture cancel unavailable");}
    }
    static final class Fixture {final Work work=new Work();final Queue worker=new Queue(),ui=new Queue();final BackupJob job=new BackupJob(work,worker,ui);void read(){assertTrue(job.choose(BackupJob.Kind.IMPORT));job.document("content://fixture/source");worker.run();ui.run();}}
    @Test public void providerWorkOnlyRunsOnWorkerAndPublicationOnlyOnUi(){Fixture f=new Fixture();f.job.choose(BackupJob.Kind.JSON);f.job.document("content://fixture/target");assertEquals(0,f.work.writes);assertEquals(BackupJob.Phase.WRITING,f.job.state().phase);f.worker.run();assertEquals(1,f.work.writes);assertEquals(BackupJob.Phase.WRITING,f.job.state().phase);f.ui.run();assertEquals(BackupJob.Phase.DONE,f.job.state().phase);}
    @Test public void reviewNeverReplacesWithoutExplicitConfirmation(){Fixture f=new Fixture();f.read();assertEquals(BackupJob.Phase.REVIEW,f.job.state().phase);assertEquals(0,f.work.restores);f.job.cancel();f.job.confirm();assertEquals(0,f.worker.tasks.size());assertEquals(0,f.work.restores);}
    @Test public void duplicateConfirmCannotQueueASecondReplacement(){Fixture f=new Fixture();f.read();f.job.confirm();f.job.confirm();assertEquals(1,f.worker.tasks.size());assertFalse(f.job.choose(BackupJob.Kind.CSV));f.job.cancel();assertEquals(BackupJob.Phase.RESTORING,f.job.state().phase);f.worker.run();f.ui.run();assertEquals(1,f.work.restores);assertEquals(BackupJob.Phase.DONE,f.job.state().phase);}
    @Test public void cancellationFencesLateReviewAndPreventsOverlappingTransport(){Fixture f=new Fixture();f.job.choose(BackupJob.Kind.IMPORT);f.job.document("content://fixture/source");f.job.cancel();assertEquals(1,f.work.cancels);assertTrue(f.job.state().occupied);assertFalse(f.job.choose(BackupJob.Kind.JSON));f.worker.run();assertFalse(f.work.active.getAsBoolean());f.ui.run();assertEquals(BackupJob.Phase.CANCELLED,f.job.state().phase);assertNull(f.job.state().review);assertFalse(f.job.state().occupied);assertTrue(f.job.choose(BackupJob.Kind.JSON));}
    @Test public void failedImportHasNoRetainedDestructiveConfirmation(){Fixture f=new Fixture();f.work.fail=true;f.read();assertEquals(BackupJob.Phase.ERROR,f.job.state().phase);assertNull(f.job.state().review);f.job.confirm();assertEquals(0,f.work.restores);}
    @Test public void failedRestoreDropsStaleReviewForExplicitReimport(){Fixture f=new Fixture();f.read();f.work.fail=true;f.job.confirm();f.worker.run();f.ui.run();assertEquals(BackupJob.Phase.ERROR,f.job.state().phase);assertTrue(f.job.state().message.contains("Stale"));assertNull(f.job.state().review);assertEquals(1,f.work.restores);}
    @Test public void cancelledChooserAndInvalidUriDoNotOpenAnyProvider(){Fixture f=new Fixture();f.job.choose(BackupJob.Kind.JSON);f.job.choiceCancelled();f.job.document("content://fixture/late");assertTrue(f.worker.tasks.isEmpty());f.job.choose(BackupJob.Kind.IMPORT);f.job.document("file:///tmp/backup");assertEquals(BackupJob.Phase.ERROR,f.job.state().phase);assertTrue(f.worker.tasks.isEmpty());}
    @Test public void rejectedWorkerIsReportedWithoutPermanentlyBusyState(){Work work=new Work();BackupJob job=new BackupJob(work,r->{throw new java.util.concurrent.RejectedExecutionException();},Runnable::run);job.choose(BackupJob.Kind.JSON);job.document("content://fixture/output");assertEquals(BackupJob.Phase.ERROR,job.state().phase);assertFalse(job.state().occupied);assertEquals(0,work.writes);}
    @Test public void restartedRestoreDoesNotClaimAnUncommittedResultOrRunAgain(){Fixture f=new Fixture();f.job.interrupted(BackupJob.Phase.RESTORING);assertEquals(BackupJob.Phase.ERROR,f.job.state().phase);assertTrue(f.job.state().message.contains("Check your records"));f.job.confirm();assertTrue(f.worker.tasks.isEmpty());assertEquals(0,f.work.restores);}
    @Test public void recreatedOwnerCannotOverwriteRetainedWorkWithInterruptedState(){Fixture f=new Fixture();f.job.choose(BackupJob.Kind.JSON);f.job.document("content://fixture/output");f.job.interrupted(BackupJob.Phase.WRITING);assertEquals(BackupJob.Phase.WRITING,f.job.state().phase);assertEquals(1,f.worker.tasks.size());}
    @Test public void unavailableChooserIsARealErrorWithoutProviderWork(){Fixture f=new Fixture();f.job.choose(BackupJob.Kind.IMPORT);f.job.choiceFailed();assertEquals(BackupJob.Phase.ERROR,f.job.state().phase);assertTrue(f.worker.tasks.isEmpty());}
    @Test public void failedCancellationPortCannotRestoreALateReview(){Fixture f=new Fixture();f.work.failCancel=true;f.job.choose(BackupJob.Kind.IMPORT);f.job.document("content://fixture/input");f.job.cancel();assertTrue(f.job.state().occupied);assertEquals(BackupJob.Phase.CANCELLED,f.job.state().phase);f.worker.run();f.ui.run();assertNull(f.job.state().review);assertFalse(f.job.state().occupied);}
}
