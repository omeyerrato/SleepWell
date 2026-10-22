package org.sleepwell.app;

import org.junit.Test;
import static org.junit.Assert.*;

public class ClipBudgetTest {
    @Test public void resumeCannotResetTheSessionClipLimit(){ClipBudget b=new ClipBudget();for(int n=0;n<60;n++){assertTrue(b.canStart(n*40000L));b.saved(n*40000L);}assertFalse(b.canStart(Long.MAX_VALUE));}
    @Test public void cooldownUsesElapsedTimeAcrossPauses(){ClipBudget b=new ClipBudget();assertTrue(b.canStart(0));b.saved(10000);assertFalse(b.canStart(39999));assertTrue(b.canStart(40000));}
}
