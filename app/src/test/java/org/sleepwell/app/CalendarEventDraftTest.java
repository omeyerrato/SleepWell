package org.sleepwell.app;
import org.junit.Test;import static org.junit.Assert.*;
public final class CalendarEventDraftTest {
    @Test public void noteIsExcludedByDefaultAndOnlyIncludedByExplicitChoice(){CalendarEventDraft draft=new CalendarEventDraft(1000,28801000,27000000,"Asia/Riyadh","Private dream");assertFalse(draft.description(false).contains("Private dream"));assertTrue(draft.description(true).endsWith("Private dream"));assertTrue(draft.description(false).contains("7 h 30 min"));}
    @Test public void wholePeriodAndEffectiveCoverageRemainDifferent(){CalendarEventDraft draft=new CalendarEventDraft(0,3600000,0,"UTC",null);assertEquals(3600000,draft.end-draft.start);assertTrue(draft.description(false).contains("0 h 0 min"));assertTrue(draft.description(false).contains("including pauses"));}
    @Test public void invalidPeriodCoverageZoneAndOversizedNoteAreRejected(){for(long[] values:new long[][]{{-1,100,10},{10,10,0},{0,10,-1},{0,10,11},{0,253402300800000L,1}})assertThrows(IllegalArgumentException.class,()->new CalendarEventDraft(values[0],values[1],values[2],"UTC",""));assertThrows(java.time.DateTimeException.class,()->new CalendarEventDraft(0,10,1,"Invalid/Zone",""));assertThrows(IllegalArgumentException.class,()->new CalendarEventDraft(0,10,1,"UTC","x".repeat(20001)));}
}
