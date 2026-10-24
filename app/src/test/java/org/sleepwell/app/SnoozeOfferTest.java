package org.sleepwell.app;
import org.junit.Test;import static org.junit.Assert.*;

public final class SnoozeOfferTest {
    @Test public void immutableOfferPresentsBoundedCurrentShortenedDurations(){SnoozeOffer offer=new SnoozeOffer("run",2,9,true,5);int[] values=offer.values();assertArrayEquals(new int[]{1,3,5,7},values);values[3]=60;assertEquals(7,offer.select(3,"run",2,true,true,5,9,true));assertEquals(1,offer.select(0,"run",2,true,true,5,9,true));}
    @Test public void staleRunCountPausedOwnerAndExhaustedBudgetCannotAcceptAnOldMenu(){SnoozeOffer offer=new SnoozeOffer("run",0,60,false,2);assertEquals(0,offer.select(0,"other",0,true,true,2,60,false));assertEquals(0,offer.select(0,"run",1,true,true,2,60,false));assertEquals(0,offer.select(0,"run",0,false,true,2,60,false));assertEquals(0,offer.select(0,"run",0,true,false,2,60,false));assertEquals(0,offer.select(0,"run",0,true,true,0,60,false));}
    @Test public void invalidSelectionOrChangedMaximumDoesNotScheduleAnything(){SnoozeOffer offer=new SnoozeOffer("run",0,60,false,1);assertEquals(0,offer.select(-1,"run",0,true,true,1,60,false));assertEquals(0,offer.select(100,"run",0,true,true,1,60,false));assertEquals(0,offer.select(8,"run",0,true,true,1,15,false));assertEquals(60,offer.select(8,"run",0,true,true,1,60,false));assertThrows(IllegalArgumentException.class,()->new SnoozeOffer("",0,5,false,1));assertThrows(IllegalArgumentException.class,()->new SnoozeOffer("run",1,5,false,1));}
}
