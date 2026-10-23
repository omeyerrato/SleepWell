package org.sleepwell.app;

import org.junit.Test;
import static org.junit.Assert.*;

public class SnoreCuePolicyTest {
    static final class Device implements SnoreCuePolicy.Device {int plays,closes;public void play(SnoreCuePolicy.Settings value){plays++;}public void close(){closes++;}}
    private SnoreCuePolicy.Settings settings(boolean on,int sensitivity){return new SnoreCuePolicy.Settings(on,SnoreCuePolicy.Mode.VIBRATION,1,1,sensitivity);}
    @Test public void disabledOrNonSnoringNeverAllocatesOutputs(){SnoreCuePolicy p=new SnoreCuePolicy(new SnoreCuePolicy.Budget(),()->{fail();return null;},()->fail());assertFalse(p.event("snoring",.9,0,settings(false,1)));assertFalse(p.event("speech",.99,0,settings(true,1)));assertFalse(p.event("noise",.99,0,settings(true,1)));p.close();}
    @Test public void sensitivityIsAScoreThresholdNotVolume(){Device d=new Device();SnoreCuePolicy p=new SnoreCuePolicy(new SnoreCuePolicy.Budget(),()->d,()->fail());assertFalse(p.event("snoring",.8,0,settings(true,0)));assertTrue(p.event("snoring",.8,0,settings(true,1)));assertFalse(p.event("snoring",Double.NaN,120000,settings(true,2)));assertFalse(p.event("snoring",1.1,120000,settings(true,2)));p.close();}
    @Test public void cooldownAndSessionCapSurviveRecorderRecreation(){SnoreCuePolicy.Budget budget=new SnoreCuePolicy.Budget();Device a=new Device(),b=new Device();SnoreCuePolicy first=new SnoreCuePolicy(budget,()->a,()->fail());assertTrue(first.event("snoring",.9,0,settings(true,1)));first.close();SnoreCuePolicy next=new SnoreCuePolicy(budget,()->b,()->fail());assertFalse(next.event("snoring",.9,119999,settings(true,1)));for(int n=1;n<30;n++)assertTrue(next.event("snoring",.9,n*120000L,settings(true,1)));assertFalse(next.event("snoring",.9,30*120000L,settings(true,1)));assertEquals(29,b.plays);next.close();}
    @Test public void closedControllerCannotEmitAndClosesAdapterOnlyOnce(){Device d=new Device();SnoreCuePolicy p=new SnoreCuePolicy(new SnoreCuePolicy.Budget(),()->d,()->fail());assertTrue(p.event("snoring",.9,0,settings(true,1)));p.close();p.close();assertFalse(p.event("snoring",.9,120000,settings(true,1)));assertEquals(1,d.closes);}
    @Test public void brokenAdapterIsReportedAndDoesNotRapidlyRetry(){int[] failures={0},loads={0};SnoreCuePolicy p=new SnoreCuePolicy(new SnoreCuePolicy.Budget(),()->{loads[0]++;throw new IllegalStateException();},()->failures[0]++);assertFalse(p.event("snoring",.9,0,settings(true,1)));assertFalse(p.event("snoring",.9,1000,settings(true,1)));assertEquals(1,loads[0]);assertEquals(1,failures[0]);p.close();}
    @Test public void malformedSettingsAreBounded(){SnoreCuePolicy.Settings s=new SnoreCuePolicy.Settings(true,null,100,-1,50);assertEquals(SnoreCuePolicy.Mode.VIBRATION,s.mode);assertEquals(3,s.intensity);assertEquals(1,s.repeats);assertEquals(2,s.sensitivity);}
}
