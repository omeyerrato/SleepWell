package org.sleepwell.app;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class CloseResourcesTest {
    @Test public void everyHandleClosesEvenWhenBufferAndModelCleanupFail(){List<String> closed=new ArrayList<>();AutoCloseable input=()->{closed.add("input");throw new IllegalStateException("buffer");},output=()->closed.add("output"),model=()->{closed.add("model");throw new IllegalStateException("model");},environment=()->closed.add("environment");try{CloseResources.close(List.of(input,output,model,environment));fail();}catch(IllegalStateException expected){assertEquals("buffer",expected.getCause().getMessage());assertEquals(1,expected.getSuppressed().length);}assertEquals(List.of("input","output","model","environment"),closed);}
    @Test public void absentAndSuccessfullyClosedResourcesAreAllowed(){int[] count={0};CloseResources.close(Arrays.asList(null,()->count[0]++));assertEquals(1,count[0]);}
}
