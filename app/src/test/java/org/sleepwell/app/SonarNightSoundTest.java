package org.sleepwell.app;

import org.junit.Test;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.Assert.*;

public final class SonarNightSoundTest {
 @Test public void firstDiscardedActualGroupStopsAnalysisAndCannotClaimComplete()throws Exception {
  long wall=1700000000000L,origin=1000000000L;List<SoundAnalysisStatus> statuses=new CopyOnWriteArrayList<>();AtomicInteger events=new AtomicInteger(),closed=new AtomicInteger(),calls=new AtomicInteger();ExecutorService storage=Executors.newSingleThreadExecutor();CountDownLatch ended=new CountDownLatch(1);AtomicReference<Throwable> failure=new AtomicReference<>();
  SonarNightSound sounds=new SonarNightSound(true,()->new SoundAnalysisStream.Model(){public float[] infer(float[] frame){int n=calls.getAndIncrement();float[] scores=new float[521];scores[n%3==2?494:0]=.9f;return scores;}public void close(){closed.incrementAndGet();}},new SonarNightSound.Port(){public void event(long at,long duration,String category,double score){assertEquals("speech",category);assertEquals(1950,duration);events.incrementAndGet();}public void status(SoundAnalysisStatus status){statuses.add(status);}},2);
  SonarNightSound.Segment segment=storage.submit(()->sounds.begin(wall,wall,origin,storage)).get();
  try{
   for(int n=0;n<225;n++){assertTrue(segment.worker.offer(new short[1920],1920,origin+n*40000000L,true));if(n%24==23){long expected=((n+1)*640-64)/15600;long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(3);while(segment.worker.frames()<expected&&System.nanoTime()<until)Thread.sleep(1);assertTrue(segment.worker.frames()>=expected);}}
   long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(3);while(segment.worker.frames()<9&&System.nanoTime()<until)Thread.sleep(1);assertEquals(9,segment.worker.frames());
   storage.submit(()->{try{sounds.captured(segment,wall+9000,9000);sounds.finish(segment,origin+9000000000L,ended::countDown,error->{failure.set(error);ended.countDown();});}catch(Exception error){failure.set(error);ended.countDown();}}).get();
   assertTrue(ended.await(3,TimeUnit.SECONDS));assertNull(failure.get());assertEquals(2,events.get());assertEquals(1,closed.get());assertEquals("limit",sounds.ended(false).issue);assertEquals("error",sounds.ended(false).state);assertEquals(9,sounds.ended(false).frames);assertFalse(statuses.isEmpty());assertEquals("limit",statuses.get(statuses.size()-1).issue);
  }finally{if(!segment.finishing){storage.submit(()->sounds.finish(segment,origin+9000000000L,ended::countDown,e->ended.countDown())).get();assertTrue(ended.await(3,TimeUnit.SECONDS));}storage.shutdown();assertTrue(storage.awaitTermination(3,TimeUnit.SECONDS));}
 }
}
