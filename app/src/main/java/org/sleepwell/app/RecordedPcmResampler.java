package org.sleepwell.app;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.function.BooleanSupplier;

/** Original offline PCM resampler. Complete real input support only; never pads or joins gaps. */
final class RecordedPcmResampler {
    static final int OUTPUT_RATE=16000, TAPS=193;
    private static final int RADIUS=(TAPS-1)/2;

    static final class Result {
        final short[] pcm;
        final long fromInputFrame, firstOutputFrame;
        final int inputRate;
        final long omittedStartMs, omittedEndMs;
        private Result(short[] pcm,int rate,long first,long end,long originalFrames){
            this.pcm=pcm;inputRate=rate;firstOutputFrame=first;
            fromInputFrame=first*rate/OUTPUT_RATE;
            // Whole milliseconds round down. The exact origin is firstOutputFrame / 16000 seconds.
            omittedStartMs=first*1000/OUTPUT_RATE;
            omittedEndMs=(originalFrames*OUTPUT_RATE-end*rate)*1000/(rate*(long)OUTPUT_RATE);
        }
    }

    /** active means continue; cancellation interrupts the whole conversion, never returns partial PCM. */
    static Result resample(short[] mono,int rate,BooleanSupplier active)throws IOException{
        if(mono==null||active==null||rate<8000||rate>96000||mono.length==0||mono.length>120L*rate)
            throw new IOException("Expected up to 120 seconds of decoded mono PCM16 at 8000–96000 Hz.");
        check(active);
        if(rate==OUTPUT_RATE){short[] copy=mono.clone();check(active);return new Result(copy,rate,0,mono.length,mono.length);}

        // Keep the original rational output grid. Trim centres that need any unavailable input tap.
        long full=mono.length*(long)OUTPUT_RATE/rate;
        long first=ceil(RADIUS*(long)OUTPUT_RATE,rate);
        long end=Math.min(full,ceil((mono.length-(long)RADIUS)*OUTPUT_RATE,rate));
        if(end<=first)throw new IOException("Recording is too short for complete resampling filter support.");
        int phaseCount=OUTPUT_RATE/gcd(rate,OUTPUT_RATE);
        float[][] phases=new float[phaseCount][];
        double cutoff=.45*Math.min(rate,OUTPUT_RATE)/rate;
        for(int phase=0;phase<phaseCount;phase++){
            check(active);phases[phase]=coefficients(cutoff,phase/(double)phaseCount);
        }
        short[] output=new short[(int)(end-first)];
        int divisor=OUTPUT_RATE/phaseCount;
        for(int n=0;n<output.length;n++){
            if((n&255)==0)check(active);
            long position=(first+n)*rate;
            int centre=(int)(position/OUTPUT_RATE),phase=(int)(position%OUTPUT_RATE)/divisor;
            float[] coefficients=phases[phase];double value=0;
            for(int tap=0;tap<TAPS;tap++)value+=mono[centre-RADIUS+tap]*coefficients[tap];
            // PCM16 rounding/saturation only: no input peak normalization or automatic gain.
            output[n]=(short)Math.max(Short.MIN_VALUE,Math.min(Short.MAX_VALUE,Math.round(value)));
        }
        check(active);
        return new Result(output,rate,first,end,mono.length);
    }

    private static float[] coefficients(double cutoff,double fraction){
        double[] raw=new double[TAPS];double sum=0;
        for(int tap=0;tap<TAPS;tap++){
            double distance=tap-RADIUS-fraction;
            if(Math.abs(distance)>RADIUS)continue;
            double sinc=distance==0?2*cutoff:Math.sin(2*Math.PI*cutoff*distance)/(Math.PI*distance);
            double angle=Math.PI*distance/RADIUS;
            double blackman=.42+.5*Math.cos(angle)+.08*Math.cos(2*angle);
            raw[tap]=sinc*blackman;sum+=raw[tap];
        }
        float[] values=new float[TAPS];
        // Fixed filter DC gain is unity; this does not inspect or normalize the source signal.
        for(int tap=0;tap<TAPS;tap++)values[tap]=(float)(raw[tap]/sum);
        return values;
    }
    private static long ceil(long value,long divisor){return Math.floorDiv(value+divisor-1,divisor);}
    private static int gcd(int first,int second){while(second!=0){int remainder=first%second;first=second;second=remainder;}return first;}
    private static void check(BooleanSupplier active)throws InterruptedIOException{
        if(Thread.currentThread().isInterrupted()||!active.getAsBoolean())throw new InterruptedIOException("Audio resampling was cancelled.");
    }
    private RecordedPcmResampler(){}
}
