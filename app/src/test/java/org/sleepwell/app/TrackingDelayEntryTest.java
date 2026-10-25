package org.sleepwell.app;
import org.junit.Test;import static org.junit.Assert.*;
public final class TrackingDelayEntryTest {
 TrackingDelayEntry.Ticket issue(){return TrackingDelayEntry.issue(true,1000,1700000000000L,1,5,false,true,true,true);}
 @Test public void requiresOriginalVisibleEntry(){assertThrows(IllegalStateException.class,()->TrackingDelayEntry.issue(false,0,0,0,1,false,false,false,false));}
 @Test public void validTicketIsSingleUseAndImmutable(){TrackingDelayEntry.Ticket t=issue();TrackingDelay p=TrackingDelayEntry.consume(t.plan.token,2000);assertSame(t.plan,p);assertTrue(p.sounds&&p.clips&&p.light);assertNull(TrackingDelayEntry.consume(t.plan.token,2000));}
 @Test public void expiredTicketCannotBecomeBackgroundAdmission(){TrackingDelayEntry.Ticket t=issue();assertNull(TrackingDelayEntry.consume(t.plan.token,11001));assertNull(TrackingDelayEntry.consume(t.plan.token,1001));}
 @Test public void wrongNonceCannotStealOriginalTicket(){TrackingDelayEntry.Ticket t=issue();assertNull(TrackingDelayEntry.consume("other",1000));assertSame(t.plan,TrackingDelayEntry.consume(t.plan.token,1000));}
 @Test public void newVisibleEntryInvalidatesOldTicket(){TrackingDelayEntry.Ticket first=issue(),second=issue();assertNull(TrackingDelayEntry.consume(first.plan.token,1000));assertSame(second.plan,TrackingDelayEntry.consume(second.plan.token,1000));}
 @Test public void sonarConsentDoesNotAcquirePhoneClipOrLightChoices(){TrackingDelayEntry.Ticket t=TrackingDelayEntry.issue(true,0,0,0,1,true,true,true,true);assertTrue(t.plan.sonar&&t.plan.sounds);assertFalse(t.plan.clips||t.plan.light);}
}
