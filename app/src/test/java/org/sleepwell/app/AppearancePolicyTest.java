package org.sleepwell.app;

import org.junit.Test;
import static org.junit.Assert.*;

public class AppearancePolicyTest {
    @Test public void automaticPhasesIncludeEveryBoundaryAndMidnight(){
        int[][] cases={{0,0},{239,0},{240,1},{359,1},{360,2},{1079,2},{1080,0},{1439,0},{1440,0},{-1,0}};
        for(int[] c:cases)assertEquals("minute "+c[0],c[1],AppearancePolicy.phase(true,2,c[0]));
    }
    @Test public void fixedChoiceDoesNotDependOnClockAndInvalidValuesAreClamped(){
        for(int theme=0;theme<3;theme++)for(int minute:new int[]{0,300,720,1439})assertEquals(theme,AppearancePolicy.phase(false,theme,minute));
        assertEquals(0,AppearancePolicy.phase(false,-3,720));assertEquals(2,AppearancePolicy.phase(false,7,720));
    }
    @Test public void textScaleHasBoundedAccessibleChoices(){assertEquals(1,AppearancePolicy.textScale(0),0);assertEquals(1.15f,AppearancePolicy.textScale(1),0);assertEquals(1.3f,AppearancePolicy.textScale(2),0);assertEquals(1,AppearancePolicy.textScale(99),0);}
}
