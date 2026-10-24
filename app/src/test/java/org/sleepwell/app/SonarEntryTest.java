package org.sleepwell.app;
import org.junit.Test;import static org.junit.Assert.*;
public class SonarEntryTest {
 @Test public void invisibleExpiredOrWrongSessionNeverAdmitsCapture(){assertThrows(IllegalStateException.class,()->SonarEntry.issue(false,100,0,SonarConfig.defaults()));SonarEntry.Ticket t=SonarEntry.issue(true,100,0,SonarConfig.defaults());assertNull(SonarEntry.consume(t.nonce,10101,0));t=SonarEntry.issue(true,100,5,SonarConfig.defaults());assertNull(SonarEntry.consume(t.nonce,200,6));}
 @Test public void ticketsAreOneUseAndCannotSurviveReplacementAba(){SonarEntry.Ticket first=SonarEntry.issue(true,100,0,SonarConfig.defaults()),second=SonarEntry.issue(true,101,0,new SonarConfig(.07f));assertNull(SonarEntry.consume(first.nonce,102,0));assertSame(second,SonarEntry.consume(second.nonce,102,0));assertNull(SonarEntry.consume(second.nonce,103,0));}
 @Test public void backwardElapsedAndCrossSessionResumeAreRejected(){SonarEntry.Ticket t=SonarEntry.issue(true,500,7,SonarConfig.defaults());assertNull(SonarEntry.consume(t.nonce,499,7));t=SonarEntry.issue(true,500,7,SonarConfig.defaults());assertSame(t,SonarEntry.consume(t.nonce,501,7));}
}
