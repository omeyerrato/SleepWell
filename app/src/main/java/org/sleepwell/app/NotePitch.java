package org.sleepwell.app;
/** Original cumulative-mean-normalized difference pitch estimator for live mono 16 kHz PCM. */
final class NotePitch {
 static final int RATE=16000,SIZE=2048;final double[] difference=new double[202];
 double frequency(short[] samples){if(samples.length!=SIZE)throw new IllegalArgumentException("Invalid note window");double mean=0,energy=0;int clipped=0;for(short value:samples){mean+=value;if(Math.abs((int)value)>=32000)clipped++;}mean/=SIZE;for(short value:samples){double x=(value-mean)/32768;energy+=x*x;}if(Math.sqrt(energy/SIZE)<.01||clipped>SIZE/100)return Double.NaN;difference[0]=1;double running=0;for(int lag=1;lag<difference.length;lag++){double sum=0;for(int n=0;n<SIZE-202;n++){double delta=(double)samples[n]-samples[n+lag];sum+=delta*delta;}running+=sum;difference[lag]=running<=0?1:sum*lag/running;}for(int lag=16;lag<difference.length-1;lag++){if(difference[lag]>.12)continue;while(lag<difference.length-2&&difference[lag+1]<difference[lag])lag++;double before=difference[lag-1],center=difference[lag],after=difference[lag+1],denominator=2*(2*center-before-after);double refined=lag+(Math.abs(denominator)<1e-12?0:(after-before)/denominator);double frequency=RATE/refined;return Double.isFinite(frequency)&&frequency>=80&&frequency<=1000?frequency:Double.NaN;}return Double.NaN;}
 static int index(String run,int unit){return Math.floorMod((int)CountingPuzzle.seed(run,unit,0,"note-v1"),4);}
 static String name(String run,int unit){return new String[]{"C4","D4","E4","A4"}[index(run,unit)];}
 static double target(String run,int unit){return new double[]{261.625565,293.664768,329.627557,440}[index(run,unit)];}
 static int tolerance(int level){return switch(level){case 1->50;case 2->35;case 3->20;default->throw new IllegalArgumentException("Choose note difficulty");};}
 static double cents(double measured,double target){return 1200*Math.log(measured/target)/Math.log(2);}
}
