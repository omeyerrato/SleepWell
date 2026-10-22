package org.sleepwell.app;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class SessionTimingTest {
    @Test public void recordedDurationSubtractsClippedAndOverlappingPausesOnce(){assertEquals(350,SessionTiming.recorded(100,1000,null,Arrays.asList(new long[]{200,400},new long[]{300,600},new long[]{-100,150},new long[]{900,1200})));}
    @Test public void monotonicDurationIsAuthoritativeEvenWhenWallClockChanges(){assertEquals(6000,SessionTiming.recorded(10000,12000,6000L,Collections.emptyList()));assertEquals(0,SessionTiming.recorded(12000,10000,null,Collections.emptyList()));}
    @Test public void noPausesPreservesLegacyDurationAndInput(){List<long[]> pauses=Arrays.asList(new long[]{10,20},new long[]{40,50});assertEquals(100,SessionTiming.recorded(100,200,null,pauses));assertEquals(10,pauses.get(0)[0]);}
}
