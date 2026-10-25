package org.sleepwell.app;
import java.util.Arrays;
/** Relative off-direct echo change. It is neither accelerometer RMS nor a sleep-stage model. */
final class SonarDsp {
 private static final int FFT=2048,BASELINE=50;private final boolean captureIq;SonarDsp(){this(false);}SonarDsp(boolean captureIq){this.captureIq=captureIq;}private final double[] real=new double[FFT],imag=new double[FFT],previousR=new double[FFT],previousI=new double[FFT];
 private SonarAlignment.Match alignment;private int baseline,changes;private boolean qualified,previous;private long frames;private double referenceOrigin=Double.NaN;
 SonarFrame accept(short[] pcm,double txFirst){if(pcm.length!=SonarConfig.CHIRP||!Double.isFinite(txFirst))throw new IllegalArgumentException("Actual chirp frame required");long from=frames*40;frames++;long to=frames*40;int clipped=0;for(short x:pcm)if(Math.abs((int)x)>=32700)clipped++;if(clipped>pcm.length/100){previous=false;return missing(from,to,SonarFrame.Quality.CLIPPED,0);}
  if(alignment==null){alignment=SonarAlignment.locate(pcm,txFirst);referenceOrigin=txFirst;if(alignment.correlation<.1){alignment=null;return missing(from,to,SonarFrame.Quality.NO_ECHO,0);}}
  if(Math.abs((txFirst-referenceOrigin)%SonarConfig.CHIRP)>2&&Math.abs((txFirst-referenceOrigin)%SonarConfig.CHIRP-SonarConfig.CHIRP)>2){previous=false;return missing(from,to,SonarFrame.Quality.TIMING_LOST,0);}
  Arrays.fill(real,0);Arrays.fill(imag,0);for(int n=0;n<pcm.length;n++){double at=txFirst+n-alignment.lag,t=at%SonarConfig.CHIRP;if(t<0)t+=SonarConfig.CHIRP;t/=SonarConfig.RATE;double phase=2*Math.PI*(18500*t+.5*37500*t*t),v=pcm[n]/32768d*(.5-.5*Math.cos(2*Math.PI*n/(pcm.length-1)));real[n]=v*Math.cos(phase);imag[n]=-v*Math.sin(phase);}SonarFft.transform(real,imag);
  double direct=power(0)+power(1)+power(FFT-1),noise=0,target=0,total=0,delta=0;for(int b=45;b<90;b++)noise+=power(FFT-b);noise=Math.max(1e-12,noise/45);for(int b=3;b<=25;b++){int k=FFT-b;double p=power(k);target=Math.max(target,p);total+=p;if(previous){double r=real[k]-previousR[k],i=imag[k]-previousI[k];delta+=r*r+i*i;}}double snr=10*Math.log10(Math.max(1e-12,target/noise));boolean echo=snr>=10&&target>direct*.002&&total>1e-8;
  if(!echo){previous=false;changes=0;return missing(from,to,SonarFrame.Quality.NO_ECHO,snr);}boolean hadPrevious=previous;double change=previous?Math.sqrt(delta/Math.max(1e-9,total)):0;System.arraycopy(real,0,previousR,0,FFT);System.arraycopy(imag,0,previousI,0,FFT);previous=true;
  if(!hadPrevious){baseline++;return missing(from,to,SonarFrame.Quality.CALIBRATING,snr);}if(baseline++<BASELINE)return missing(from,to,SonarFrame.Quality.CALIBRATING,snr);if(!qualified){if(change>.03&&change<2)changes++;else changes=0;if(changes>=3)qualified=true;}if(!qualified)return missing(from,to,SonarFrame.Quality.NEEDS_MOVEMENT,snr);double[] iq=null;if(captureIq){iq=new double[46];for(int b=3;b<=25;b++){iq[(b-3)*2]=real[FFT-b];iq[(b-3)*2+1]=imag[FFT-b];}}return new SonarFrame(from,to,Math.min(4,change),SonarFrame.Quality.VALID,snr,alignment.correlation,iq);
 }
 private double power(int k){return real[k]*real[k]+imag[k]*imag[k];}
 private SonarFrame missing(long from,long to,SonarFrame.Quality quality,double snr){return new SonarFrame(from,to,null,quality,Double.isFinite(snr)?snr:0,alignment==null?0:alignment.correlation);}
}
