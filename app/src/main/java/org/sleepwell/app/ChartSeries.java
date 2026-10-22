package org.sleepwell.app;

import java.util.*;

/** Display-only averages of contiguous measured epochs; never interpolates missing intervals. */
final class ChartSeries {
    static final long MINUTE=60000;
    static final class Sample {
        final long time;final Long from;final Double value;
        Sample(long time,Double value){this(null,time,value);}
        Sample(Long from,long time,Double value){this.from=from;this.time=time;this.value=value;}
    }
    static final class Bar {
        final long from,to;final double value;final int readings;
        Bar(long from,long to,double value,int readings){this.from=from;this.to=to;this.value=value;this.readings=readings;}
    }
    static List<Bar> aggregate(List<Sample> source,long start,long end,int maxColumns){
        if(end<=start)return Collections.emptyList();
        long cadence=MINUTE;for(Sample sample:source)if(sample.from!=null&&sample.from>=start&&sample.time<=end&&sample.time>sample.from)cadence=Math.min(cadence,sample.time-sample.from);
        int columns=Math.max(1,Math.min(500,maxColumns));long step=Math.max(cadence,(long)Math.ceil((end-start)/(double)columns));
        List<Sample> samples=new ArrayList<>(source);samples.sort(Comparator.comparingLong(s->s.time));
        List<Bar> bars=new ArrayList<>();long from=0,to=0,index=-1,previousTime=start;double mean=0;int count=0;
        for(Sample sample:samples){
            if(sample.time<start||sample.time>end)continue;
            boolean valid=sample.value!=null&&Double.isFinite(sample.value)&&sample.value>=0;
            long sampleFrom=Math.max(previousTime,Math.max(start,sample.from==null?sample.time-MINUTE:Math.min(sample.time,sample.from))),bucket=Math.max(0,(sample.time-start-1)/step);previousTime=sample.time;
            if(count>0&&(!valid||bucket!=index||sampleFrom>to+(sample.from==null?1000:0))){
                bars.add(new Bar(from,to,mean,count));count=0;mean=0;
            }
            if(!valid)continue;
            if(count==0){from=sampleFrom;index=bucket;}
            to=sample.time;count++;mean+=(sample.value-mean)/count;
        }
        if(count>0)bars.add(new Bar(from,to,mean,count));return bars;
    }
}
