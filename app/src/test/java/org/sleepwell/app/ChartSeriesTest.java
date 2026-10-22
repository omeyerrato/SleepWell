package org.sleepwell.app;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class ChartSeriesTest {
    @Test public void explicitSecondPreviewBinsRetainActualCadence(){java.util.List<ChartSeries.Sample> samples=new java.util.ArrayList<>();for(int i=0;i<10;i++)samples.add(new ChartSeries.Sample((long)i*1000,(i+1)*1000,.1+i));assertEquals(10,ChartSeries.aggregate(samples,0,10000,50).size());}
    @Test public void explicitEpochStartsPreserveShortPausesInsideOneDisplayColumn(){
        List<ChartSeries.Bar> bars=ChartSeries.aggregate(Arrays.asList(new ChartSeries.Sample(0L,20000,0d),new ChartSeries.Sample(80000L,100000,1d)),0,120000,1);assertEquals(2,bars.size());assertEquals(20000,bars.get(0).to);assertEquals(80000,bars.get(1).from);assertEquals(0,bars.get(0).value,0);
        bars=ChartSeries.aggregate(Arrays.asList(new ChartSeries.Sample(0L,20000,1d),new ChartSeries.Sample(20500L,22000,2d)),0,120000,1);assertEquals(2,bars.size());assertEquals(20500,bars.get(1).from);
    }
    @Test public void finiteExtremeImportsDoNotOverflowTheDisplayMean(){
        List<ChartSeries.Bar> bars=ChartSeries.aggregate(Arrays.asList(new ChartSeries.Sample(60000,Double.MAX_VALUE),new ChartSeries.Sample(120000,Double.MAX_VALUE)),0,120000,1);
        assertEquals(1,bars.size());assertEquals(Double.MAX_VALUE,bars.get(0).value,0);assertTrue(Double.isFinite(bars.get(0).value));
        bars=ChartSeries.aggregate(Arrays.asList(new ChartSeries.Sample(60000,Double.MAX_VALUE),new ChartSeries.Sample(120000,0d)),0,120000,1);assertEquals(Double.MAX_VALUE/2,bars.get(0).value,0);
    }
    @Test public void denseNightBecomesBoundedAveragesWithoutChangingSource(){
        List<ChartSeries.Sample> samples=new ArrayList<>();for(int n=1;n<=480;n++)samples.add(new ChartSeries.Sample(n*60000L,(double)(n%8)));
        List<ChartSeries.Bar> bars=ChartSeries.aggregate(samples,0,480*60000L,60);
        assertEquals(60,bars.size());assertEquals(0,bars.get(0).from);assertEquals(480*60000L,bars.get(59).to);
        for(ChartSeries.Bar b:bars){assertEquals(8,b.readings);assertEquals(3.5,b.value,.000001);}assertEquals(480,samples.size());
    }
    @Test public void explicitMissingAndAbsentEpochsRemainGapsEvenInsideOneColumn(){
        List<ChartSeries.Sample> samples=Arrays.asList(new ChartSeries.Sample(60000,2d),new ChartSeries.Sample(120000,4d),new ChartSeries.Sample(180000,null),new ChartSeries.Sample(300000,6d),new ChartSeries.Sample(360000,8d));
        List<ChartSeries.Bar> bars=ChartSeries.aggregate(samples,0,600000,1);
        assertEquals(2,bars.size());assertEquals(120000,bars.get(0).to);assertEquals(240000,bars.get(1).from);assertEquals(3,bars.get(0).value,0);assertEquals(7,bars.get(1).value,0);
        assertEquals(2,ChartSeries.aggregate(Arrays.asList(samples.get(0),samples.get(4)),0,600000,1).size());
    }
    @Test public void zeroIsMeasuredAndInvalidOrOutOfSessionValuesAreExcluded(){
        List<ChartSeries.Sample> samples=Arrays.asList(new ChartSeries.Sample(-1,4d),new ChartSeries.Sample(60000,0d),new ChartSeries.Sample(120000,Double.NaN),new ChartSeries.Sample(180000,-2d),new ChartSeries.Sample(300001,10d));
        List<ChartSeries.Bar> bars=ChartSeries.aggregate(samples,0,300000,50);assertEquals(1,bars.size());assertEquals(0,bars.get(0).value,0);assertEquals(1,bars.get(0).readings);
    }
    @Test public void sortsInputAndHandlesShortOrEmptyRecordings(){
        List<ChartSeries.Sample> samples=Arrays.asList(new ChartSeries.Sample(90000,5d),new ChartSeries.Sample(60000,3d));
        List<ChartSeries.Bar> bars=ChartSeries.aggregate(samples,0,90000,50);assertEquals(2,bars.size());assertEquals(3,bars.get(0).value,0);assertEquals(5,bars.get(1).value,0);assertEquals(60000,bars.get(1).from); // A partial final epoch cannot overlap the prior minute.
        assertTrue(ChartSeries.aggregate(samples,0,0,0).isEmpty());assertTrue(ChartSeries.aggregate(Collections.emptyList(),0,60000,50).isEmpty());
    }
}
