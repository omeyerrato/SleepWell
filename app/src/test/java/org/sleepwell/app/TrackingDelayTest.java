package org.sleepwell.app;
import org.junit.Test;import static org.junit.Assert.*;
public final class TrackingDelayTest {
 TrackingDelay plan(){return new TrackingDelay("nonce",1000,1700000000000L,2,5,false,true,true,true,TrackingDelay.Phase.WAITING,0,"");}
 @Test public void defaultOffAndOnlySupportedPresets(){assertTrue(TrackingDelay.allowed(0));for(int m:new int[]{1,5,10,15,30})assertTrue(TrackingDelay.allowed(m));for(int m:new int[]{-1,2,31,120})assertFalse(TrackingDelay.allowed(m));}
 @Test public void offCannotCreateWaitingPlan(){assertThrows(IllegalArgumentException.class,()->new TrackingDelay("n",0,0,0,0,false,false,false,false,TrackingDelay.Phase.WAITING,0,""));}
 @Test public void elapsedCountdownIsExactToMilliseconds(){TrackingDelay p=plan();assertEquals(300000,p.remaining(1000));assertEquals(1,p.remaining(p.deadline-1));assertEquals(0,p.remaining(p.deadline));}
 @Test public void wallChangesCannotShortenOrExtendDeadline(){TrackingDelay p=plan();assertEquals(301000,p.deadline);assertEquals(TrackingDelay.Phase.CLAIMED,p.claim(301000,2,false).phase);assertEquals(1700000000000L,p.wall);}
 @Test public void beforeDeadlineDoesNotClaim(){TrackingDelay p=plan();assertSame(p,p.claim(p.deadline-1,2,false));}
 @Test public void exactDeadlineClaimsWithoutRecording(){TrackingDelay p=plan().claim(301000,2,false);assertEquals(TrackingDelay.Phase.CLAIMED,p.phase);assertEquals(0,p.session);}
 @Test public void explicitStartNowCanClaimEarly(){assertEquals(TrackingDelay.Phase.CLAIMED,plan().claim(2000,2,true).phase);}
 @Test public void overdueDeliveryDoesNotCaptureLate(){TrackingDelay p=plan();assertEquals(TrackingDelay.Phase.INTERRUPTED,p.claim(p.deadline+TrackingDelay.LATE+1,2,false).phase);}
 @Test public void permittedSmallSchedulingDelayRemainsBounded(){TrackingDelay p=plan();assertEquals(TrackingDelay.Phase.CLAIMED,p.claim(p.deadline+TrackingDelay.LATE,2,false).phase);}
 @Test public void bootChangeInterruptsEvenIfElapsedLooksDue(){assertEquals(TrackingDelay.Phase.INTERRUPTED,plan().claim(301000,3,false).phase);}
 @Test public void backwardsClockInterrupts(){assertEquals(TrackingDelay.Phase.INTERRUPTED,plan().claim(999,2,true).phase);}
 @Test public void terminalPlanNeverReclaims(){for(TrackingDelay.Phase phase:new TrackingDelay.Phase[]{TrackingDelay.Phase.CANCELLED,TrackingDelay.Phase.INTERRUPTED,TrackingDelay.Phase.FAILED,TrackingDelay.Phase.STARTED}){TrackingDelay p=plan().state(phase,phase==TrackingDelay.Phase.STARTED?4:0,"");assertSame(p,p.claim(301000,2,true));}}
 @Test public void claimedPlanNeverReclaims(){TrackingDelay p=plan().claim(301000,2,false);assertSame(p,p.claim(301001,2,false));}
 @Test public void recordingIdentityOnlyExistsAfterCapture(){assertThrows(IllegalArgumentException.class,()->plan().state(TrackingDelay.Phase.WAITING,3,""));assertThrows(IllegalArgumentException.class,()->plan().state(TrackingDelay.Phase.STARTED,0,""));assertEquals(7,plan().state(TrackingDelay.Phase.STARTED,7,"").session);}
 @Test public void sonarNeverRequestsClipsOrLight(){assertThrows(IllegalArgumentException.class,()->new TrackingDelay("n",0,0,0,1,true,true,true,false,TrackingDelay.Phase.WAITING,0,""));assertThrows(IllegalArgumentException.class,()->new TrackingDelay("n",0,0,0,1,true,false,false,true,TrackingDelay.Phase.WAITING,0,""));}
 @Test public void nearLongLimitDoesNotOverflowLateBoundary(){TrackingDelay p=new TrackingDelay("n",Long.MAX_VALUE-60001,0,0,1,false,false,false,false,TrackingDelay.Phase.WAITING,0,"");assertEquals(TrackingDelay.Phase.CLAIMED,p.claim(Long.MAX_VALUE-1,0,false).phase);}
}
