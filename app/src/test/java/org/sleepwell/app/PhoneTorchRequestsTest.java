package org.sleepwell.app;
import org.junit.Test;
import static org.junit.Assert.*;

public class PhoneTorchRequestsTest {
 @Test public void reservationBeforeQueuePreventsOldClientShutdown(){PhoneTorchRequests gate=new PhoneTorchRequests();assertTrue(gate.reserve());assertTrue(gate.reserve());gate.release();assertFalse(gate.closeIfIdle());assertFalse(gate.closed());gate.release();assertTrue(gate.closeIfIdle());}
 @Test public void shutdownBeforeReservationRejectsWithoutQueueingOrOutput(){PhoneTorchRequests old=new PhoneTorchRequests();assertTrue(old.reserve());old.release();assertTrue(old.closeIfIdle());assertFalse(old.reserve());PhoneTorchRequests replacement=new PhoneTorchRequests();assertTrue(replacement.reserve());assertEquals(0,old.count());assertEquals(1,replacement.count());}
 @Test public void failedQueueReleasesExactlyItsReservation(){PhoneTorchRequests gate=new PhoneTorchRequests();assertTrue(gate.reserve());FakeQueue queue=new FakeQueue(false);boolean[] failed={false};if(!queue.post(()->fail("Rejected callback must never run"))){gate.release();failed[0]=true;}assertTrue(failed[0]);assertEquals(0,gate.count());assertTrue(gate.closeIfIdle());assertFalse(gate.reserve());}
 @Test public void acceptedQueueStillOwnsReservationBeforeAnyCallbackRuns(){PhoneTorchRequests gate=new PhoneTorchRequests();assertTrue(gate.reserve());FakeQueue queue=new FakeQueue(true);assertTrue(queue.post(()->gate.release()));assertFalse(gate.closeIfIdle());queue.pending.run();assertTrue(gate.closeIfIdle());}
 @Test(expected=IllegalStateException.class) public void doubleReleaseFailsClosed(){PhoneTorchRequests gate=new PhoneTorchRequests();assertTrue(gate.reserve());gate.release();gate.release();}
 private static final class FakeQueue {final boolean accepts;Runnable pending;FakeQueue(boolean value){accepts=value;}boolean post(Runnable task){if(!accepts)return false;pending=task;return true;}}
}
