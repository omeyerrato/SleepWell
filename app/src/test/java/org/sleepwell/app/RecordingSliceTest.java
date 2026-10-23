package org.sleepwell.app;

import org.junit.Test;
import static org.junit.Assert.*;

public class RecordingSliceTest {
    @Test public void containedEventHasRealOffsetAndDuration(){RecordingSlice s=RecordingSlice.contained(20000,10048,11000,1950);assertEquals(1048,s.fromMs);assertEquals(1950,s.durationMs);}
    @Test public void boundaryTouchingEventsArePlayable(){assertEquals(0,RecordingSlice.contained(20000,10000,10000,10000).fromMs);assertEquals(9000,RecordingSlice.contained(20000,10000,19000,1000).fromMs);}
    @Test public void partialCoverageCannotInventPlayableSamples(){for(long[] v:new long[][]{{20000,10000,9999,1950},{20000,10000,19000,1001},{20000,0,19000,1000},{20000,10000,19000,0},{Long.MAX_VALUE,10000,Long.MAX_VALUE-100,1000}})try{RecordingSlice.contained(v[0],v[1],v[2],v[3]);fail();}catch(IllegalArgumentException expected){}}
}
