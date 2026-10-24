package org.sleepwell.app;
import org.junit.Test;
import static org.junit.Assert.*;
public class SnoozePolicyTest {
    @Test public void shorteningUsesTheSameSequenceForDisplayAndScheduling(){assertEquals(5,SnoozePolicy.minutes(5,true,0));assertEquals(4,SnoozePolicy.minutes(5,true,1));assertEquals(3,SnoozePolicy.minutes(5,true,2));assertEquals(1,SnoozePolicy.minutes(5,true,100));}
    @Test public void disabledShorteningPreservesExistingSnoozeBehavior(){for(int used=0;used<10;used++)assertEquals(7,SnoozePolicy.minutes(7,false,used));}
    @Test public void invalidValuesCannotScheduleNegativeOrUnboundedDelay(){assertEquals(1,SnoozePolicy.minutes(-1,true,-10));assertEquals(60,SnoozePolicy.minutes(Integer.MAX_VALUE,false,0));assertEquals(1,SnoozePolicy.minutes(1,true,Integer.MAX_VALUE));}
    @Test public void choicesContainOnlyPositiveBoundedActualCurrentDurations(){assertArrayEquals(new int[]{1,3,5},SnoozePolicy.choices(5,false,0));assertArrayEquals(new int[]{1,3,5,7},SnoozePolicy.choices(9,true,2));assertArrayEquals(new int[]{1,3,5,10,15,20,30,45,60},SnoozePolicy.choices(60,false,0));assertArrayEquals(new int[]{1},SnoozePolicy.choices(1,true,5));assertTrue(SnoozePolicy.accepts(7,9,true,2));assertFalse(SnoozePolicy.accepts(8,9,true,2));assertFalse(SnoozePolicy.accepts(0,60,false,0));assertFalse(SnoozePolicy.accepts(61,60,false,0));}
    @Test public void everyLegacySupportedDurationRetainsExactOldDecisions(){for(int base=1;base<=15;base++)for(int used=0;used<=5;used++){assertEquals(base,SnoozePolicy.minutes(base,false,used));assertEquals(Math.max(1,base-used),SnoozePolicy.minutes(base,true,used));}}
}
