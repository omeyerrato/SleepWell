package org.sleepwell.app;

import java.util.Arrays;

/** Original causal, stateful 48k -> 16k FIR. No invented boundary samples or gain normalization. */
final class SonarDownsampler {
    static final int INPUT_RATE=48000,OUTPUT_RATE=16000,TAPS=193,DELAY=96,MAX_INPUT=1920;
    private static final long MAX_SAMPLES=36L*3600*INPUT_RATE;
    private static final double[] COEFFICIENTS=coefficients();
    static final class Output {
        final short[] pcm;final long fromNanos,toNanos;
        Output(short[] pcm,long from,long to){this.pcm=pcm;fromNanos=from;toNanos=to;}
    }
    private final short[] history=new short[TAPS];private int next;private long received,origin;private boolean anchored;

    Output accept(short[] pcm,int count,long firstNanos){
        if(pcm==null||count<1||count>pcm.length||count>MAX_INPUT||firstNanos<0)throw new IllegalArgumentException("Invalid shared capture block");
        if(received>MAX_SAMPLES-count)throw new IllegalStateException("Shared sound duration exceeded");
        if(anchored){long expected=Math.addExact(origin,inputNanos(received)),difference=Math.subtractExact(firstNanos,expected);if(difference< -1||difference>1)throw new IllegalStateException("Shared sound capture is discontinuous");}
        // Validate the complete clock extent before consuming any sample.
        Math.addExact(anchored?origin:firstNanos,inputNanos(received+count));
        if(!anchored){origin=firstNanos;anchored=true;}
        short[] output=new short[(count+2)/3];int made=0;long firstCentre=-1;
        for(int n=0;n<count;n++){
            long index=received++;history[next]=pcm[n];next=(next+1)%TAPS;
            // Require all193 actual input samples; neither startup nor EOF is padded.
            if(index<TAPS-1||index%3!=0)continue;
            double value=0;int at=next-1;if(at<0)at=TAPS-1;
            for(int tap=0;tap<TAPS;tap++){value+=COEFFICIENTS[tap]*history[at];if(--at<0)at=TAPS-1;}
            if(firstCentre<0)firstCentre=index-DELAY;
            output[made++]=(short)Math.max(Short.MIN_VALUE,Math.min(Short.MAX_VALUE,Math.round(value)));
        }
        if(made==0)return null;
        long from=Math.addExact(origin,inputNanos(firstCentre));
        return new Output(Arrays.copyOf(output,made),from,Math.addExact(from,made*62500L));
    }
    private static long inputNanos(long samples){return Math.addExact(Math.multiplyExact(samples/INPUT_RATE,1000000000L),((samples%INPUT_RATE)*1000000000L+INPUT_RATE/2)/INPUT_RATE);}
    private static double[] coefficients(){double[] values=new double[TAPS];double sum=0,cutoff=7000d/INPUT_RATE;for(int n=0;n<TAPS;n++){int centre=n-DELAY;double sinc=centre==0?2*cutoff:Math.sin(2*Math.PI*cutoff*centre)/(Math.PI*centre),phase=2*Math.PI*n/(TAPS-1),window=.35875-.48829*Math.cos(phase)+.14128*Math.cos(2*phase)-.01168*Math.cos(3*phase);values[n]=sinc*window;sum+=values[n];}for(int n=0;n<TAPS;n++)values[n]/=sum;return values;}
}
