package org.sleepwell.app;

import org.junit.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

public class CloudBackupReviewTest {
    private BackupJob create(AtomicInteger restored){return new BackupJob(new BackupJob.Work(){public BackupJob.Review read(String uri,java.util.function.BooleanSupplier active){return new BackupJob.Review("{\"format\":6}","fingerprint","review",9);}public void write(BackupJob.Kind kind,String uri,java.util.function.BooleanSupplier active){throw new AssertionError();}public String restore(BackupJob.Review review){restored.incrementAndGet();return "restored";}public void cancel(){}},Runnable::run,Runnable::run);}
    @Test public void matchingCopyHashStillRequiresExplicitReviewAndConfirmation()throws Exception{AtomicInteger restored=new AtomicInteger();BackupJob job=create(restored);assertTrue(job.choose(BackupJob.Kind.IMPORT));job.document("content://fixture/new",CloudBackupCopy.sha(BackupIo.encode("{\"format\":6}")));assertEquals(BackupJob.Phase.REVIEW,job.state().phase);assertEquals(0,restored.get());job.confirm();assertEquals(1,restored.get());}
    @Test public void changedOrInvalidCopyHashCannotReachReplacement(){for(String sha:new String[]{"a".repeat(64),"invalid"}){AtomicInteger restored=new AtomicInteger();BackupJob job=create(restored);job.choose(BackupJob.Kind.IMPORT);job.document("content://fixture/new",sha);assertEquals(BackupJob.Phase.ERROR,job.state().phase);job.confirm();assertEquals(0,restored.get());}}
}
