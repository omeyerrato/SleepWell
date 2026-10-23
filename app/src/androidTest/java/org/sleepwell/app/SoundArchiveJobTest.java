package org.sleepwell.app;

import android.app.Instrumentation;
import android.net.Uri;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.Assert.*;

/** Retained state behavior with explicitly gated fake work; never opens a document provider. */
public class SoundArchiveJobTest {
    private static final Uri DESTINATION=Uri.parse("content://org.sleepwell.verification/export.zip");
    private void phase(SoundArchiveJob job,SoundArchiveJob.Phase expected)throws Exception{long end=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);while(job.state().phase!=expected&&System.nanoTime()<end)Thread.sleep(10);assertEquals(expected,job.state().phase);}
    @Test public void duplicateDocumentResultsCannotResetWritingOrQueueAnotherExport()throws Exception{
        Instrumentation i=InstrumentationRegistry.getInstrumentation();SoundArchiveJob.Factory prior=SoundArchiveJob.factory;AtomicReference<SoundArchiveJob> current=new AtomicReference<>();CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1);AtomicInteger writes=new AtomicInteger();
        try(AudioArchiveConcurrencyTest.Fixture f=new AudioArchiveConcurrencyTest.Fixture()){
            i.runOnMainSync(()->{SoundArchiveJob.factory=(context,records,id,updated)->new SoundArchiveJob.Work(){public NightAudioArchive.Plan prepare(){return f.plan;}public void write(NightAudioArchive.Plan plan,Uri uri)throws Exception{writes.incrementAndGet();entered.countDown();assertTrue(release.await(5,TimeUnit.SECONDS));}};current.set(new SoundArchiveJob(f.context,context->{throw new AssertionError("Fake work must not reopen global records");},f.id,0));});SoundArchiveJob job=current.get();phase(job,SoundArchiveJob.Phase.READY);
            i.runOnMainSync(()->{assertTrue(job.choose());job.write(DESTINATION);});assertTrue(entered.await(3,TimeUnit.SECONDS));i.runOnMainSync(()->{job.write(DESTINATION);job.cancelChoice();assertEquals(SoundArchiveJob.Phase.WRITING,job.state().phase);assertFalse(job.choose());});assertEquals(1,writes.get());release.countDown();phase(job,SoundArchiveJob.Phase.DONE);i.runOnMainSync(()->{job.write(DESTINATION);assertEquals(SoundArchiveJob.Phase.DONE,job.state().phase);});assertEquals(1,writes.get());
        }finally{release.countDown();if(current.get()!=null){i.runOnMainSync(current.get()::close);assertTrue(current.get().awaitClosed(5000));}i.runOnMainSync(()->SoundArchiveJob.factory=prior);}
    }
    @Test public void closeCancelsWorkAndRejectsLateObservationResultsAndDestinations()throws Exception{
        Instrumentation i=InstrumentationRegistry.getInstrumentation();SoundArchiveJob.Factory prior=SoundArchiveJob.factory;AtomicReference<SoundArchiveJob> current=new AtomicReference<>();CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1),writing=new CountDownLatch(1),releaseWrite=new CountDownLatch(1);AtomicInteger calls=new AtomicInteger(),cancels=new AtomicInteger(),callbacks=new AtomicInteger();
        try(AudioArchiveConcurrencyTest.Fixture f=new AudioArchiveConcurrencyTest.Fixture()){
            i.runOnMainSync(()->{SoundArchiveJob.factory=(context,records,id,updated)->new SoundArchiveJob.Work(){public NightAudioArchive.Plan prepare()throws Exception{entered.countDown();boolean done=false;while(!done)try{done=release.await(5,TimeUnit.SECONDS);}catch(InterruptedException ignored){}return f.plan;}public void write(NightAudioArchive.Plan plan,Uri uri){calls.incrementAndGet();}public void cancel(){cancels.incrementAndGet();}};current.set(new SoundArchiveJob(f.context,Records.factory,f.id,0));current.get().observe(state->callbacks.incrementAndGet());});SoundArchiveJob job=current.get();assertTrue(entered.await(3,TimeUnit.SECONDS));int before=callbacks.get();i.runOnMainSync(()->{job.close();job.observe(state->callbacks.incrementAndGet());job.write(DESTINATION);assertFalse(job.choose());});assertEquals(1,cancels.get());release.countDown();assertTrue(job.awaitClosed(5000));i.waitForIdleSync();assertEquals(before,callbacks.get());assertEquals(0,calls.get());assertEquals(SoundArchiveJob.Phase.LOADING,job.state().phase);
            i.runOnMainSync(()->{SoundArchiveJob.factory=(context,records,id,updated)->new SoundArchiveJob.Work(){public NightAudioArchive.Plan prepare(){return f.plan;}public void write(NightAudioArchive.Plan plan,Uri uri)throws Exception{calls.incrementAndGet();writing.countDown();boolean done=false;while(!done)try{done=releaseWrite.await(5,TimeUnit.SECONDS);}catch(InterruptedException ignored){}}public void cancel(){cancels.incrementAndGet();}};current.set(new SoundArchiveJob(f.context,Records.factory,f.id,0));current.get().observe(state->callbacks.incrementAndGet());});SoundArchiveJob second=current.get();phase(second,SoundArchiveJob.Phase.READY);i.runOnMainSync(()->{assertTrue(second.choose());second.write(DESTINATION);});assertTrue(writing.await(3,TimeUnit.SECONDS));int whileWriting=callbacks.get();i.runOnMainSync(()->{second.close();second.observe(state->callbacks.incrementAndGet());second.write(DESTINATION);assertFalse(second.choose());});assertEquals(2,cancels.get());releaseWrite.countDown();assertTrue(second.awaitClosed(5000));i.waitForIdleSync();assertEquals(whileWriting,callbacks.get());assertEquals(1,calls.get());assertEquals(SoundArchiveJob.Phase.WRITING,second.state().phase);
        }finally{release.countDown();releaseWrite.countDown();if(current.get()!=null){i.runOnMainSync(current.get()::close);assertTrue(current.get().awaitClosed(5000));}i.runOnMainSync(()->SoundArchiveJob.factory=prior);}
    }
}
