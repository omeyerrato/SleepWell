package org.sleepwell.app;
import org.junit.Test;
import static org.junit.Assert.*;
public class SnoozePolicyTest {
    @Test public void shorteningUsesTheSameSequenceForDisplayAndScheduling(){assertEquals(5,SnoozePolicy.minutes(5,true,0));assertEquals(4,SnoozePolicy.minutes(5,true,1));assertEquals(3,SnoozePolicy.minutes(5,true,2));assertEquals(1,SnoozePolicy.minutes(5,true,100));}
    @Test public void disabledShorteningPreservesExistingSnoozeBehavior(){for(int used=0;used<10;used++)assertEquals(7,SnoozePolicy.minutes(7,false,used));}
    @Test public void invalidValuesCannotScheduleNegativeOrUnboundedDelay(){assertEquals(1,SnoozePolicy.minutes(-1,true,-10));assertEquals(15,SnoozePolicy.minutes(Integer.MAX_VALUE,false,0));assertEquals(1,SnoozePolicy.minutes(1,true,Integer.MAX_VALUE));}
}
