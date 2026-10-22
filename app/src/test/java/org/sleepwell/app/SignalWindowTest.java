package org.sleepwell.app;

import org.junit.Test;
import static org.junit.Assert.*;

public class SignalWindowTest {
    @Test public void missingIsDifferentFromMeasuredSilence(){SignalWindow w=new SignalWindow();assertNull(w.take());w.add(0,100);assertEquals(0,w.take(),0);assertNull(w.take());}
    @Test public void usesSampleWeightedEnergyInsteadOfLastFrame(){SignalWindow w=new SignalWindow();w.add(1,1);w.add(0,3);assertEquals(.5,w.take(),1e-9);assertNull(w.take());}
}
