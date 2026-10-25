package org.sleepwell.app;

import java.util.*;

/** Original exploratory I/Q periodicity analysis. A periodic reflection is not proof of breathing. */
final class EchoPeriodicity {
    static final String ALGORITHM="echo_iq_periodicity_v1";
    static final long WINDOW=60000;
    static final int BINS=23,SAMPLES=300;
    interface Sink {void window(long from,long to,Double rate,double periodicity,String quality)throws Exception;}
    private final Sink sink;private final double[][] real=new double[BINS][SAMPLES],imag=new double[BINS][SAMPLES];
    private long from,to,last;private int frames,points,down;private boolean invalid,anchored;private final double[] sumR=new double[BINS],sumI=new double[BINS];
    EchoPeriodicity(long start,Sink sink){from=start;to=Math.addExact(start,WINDOW);this.sink=sink;}
    void add(SonarObservation observation)throws Exception{
        long start=observation.from(),end=observation.to();if(!anchored){from=start;to=Math.addExact(from,WINDOW);anchored=true;}
        while(start>=to){emit();reset(to);}
        if(start<from||end>to){invalid=true;return;}
        if(last==0?start-from>40:start-last>40||start<last)invalid=true;
        last=end;frames++;double[] iq=observation.frame.iq;
        if(observation.frame.quality!=SonarFrame.Quality.VALID||iq==null||iq.length!=2*BINS){invalid=true;return;}
        for(int b=0;b<BINS;b++){if(!Double.isFinite(iq[2*b])||!Double.isFinite(iq[2*b+1])){invalid=true;return;}sumR[b]+=iq[2*b];sumI[b]+=iq[2*b+1];}
        if(++down==5){if(points>=SAMPLES){invalid=true;return;}for(int b=0;b<BINS;b++){real[b][points]=sumR[b]/5;imag[b][points]=sumI[b]/5;sumR[b]=sumI[b]=0;}points++;down=0;}
        if(end==to){emit();reset(to);}
    }
    private void reset(long next){from=next;to=Math.addExact(from,WINDOW);last=0;frames=points=down=0;invalid=false;Arrays.fill(sumR,0);Arrays.fill(sumI,0);}
    private void emit()throws Exception{
        if(invalid||points!=SAMPLES||frames!=1500||last<to-40){sink.window(from,to,null,0,"INSUFFICIENT_SIGNAL");return;}
        double best=0,rate=0;boolean ambiguity=false;
        for(int b=0;b<BINS;b++){
            double[] wave=project(real[b],imag[b]);if(wave==null)continue;
            Peak full=peak(wave,0,SAMPLES),first=peak(wave,0,SAMPLES/2),second=peak(wave,SAMPLES/2,SAMPLES);
            if(full==null||first==null||second==null||full.correlation<.75||first.correlation<.65||second.correlation<.65)continue;
            if(Math.abs(first.rate-second.rate)>Math.max(2,full.rate*.15)||Math.abs(first.rate-full.rate)>Math.max(2,full.rate*.15)||Math.abs(second.rate-full.rate)>Math.max(2,full.rate*.15))continue;
            if(best>0&&Math.abs(rate-full.rate)>Math.max(2,rate*.15)&&full.correlation>best-.05)ambiguity=true;
            if(full.correlation>best){if(best==0||full.correlation>best+.05)ambiguity=false;best=full.correlation;rate=full.rate;}
        }
        sink.window(from,to,best>0&&!ambiguity?rate:null,best,best==0?"NO_STABLE_PERIOD":ambiguity?"AMBIGUOUS":"PERIODIC");
    }
    /** Remove static I/Q offset and project a near-linear varying echo onto its principal axis. */
    private static double[] project(double[] r,double[] i){
        double mr=0,mi=0;for(int n=0;n<SAMPLES;n++){mr+=r[n];mi+=i[n];}mr/=SAMPLES;mi/=SAMPLES;
        double xx=0,yy=0,xy=0;for(int n=0;n<SAMPLES;n++){double x=r[n]-mr,y=i[n]-mi;xx+=x*x;yy+=y*y;xy+=x*y;}
        double total=xx+yy,discriminant=Math.hypot(xx-yy,2*xy);if(!Double.isFinite(total)||total<1e-8||(total+discriminant)/(2*total)<.8)return null;
        double angle=.5*Math.atan2(2*xy,xx-yy),cos=Math.cos(angle),sin=Math.sin(angle);double[] result=new double[SAMPLES];double sum=0,slope=0;
        for(int n=0;n<SAMPLES;n++){result[n]=(r[n]-mr)*cos+(i[n]-mi)*sin;sum+=result[n];slope+=(n-(SAMPLES-1)/2d)*result[n];}
        slope/=SAMPLES*(SAMPLES*SAMPLES-1)/12d;double mean=sum/SAMPLES;
        for(int n=0;n<SAMPLES;n++)result[n]-=mean+slope*(n-(SAMPLES-1)/2d);return result;
    }
    private static final class Peak {final double rate,correlation;Peak(double rate,double correlation){this.rate=rate;this.correlation=correlation;}}
    /** Positive local autocorrelation peaks in the explicitly supported 5–40 cycles/minute band. */
    private static Peak peak(double[] wave,int from,int to){
        int maxLag=Math.min(60,(to-from)/2);double[] correlation=new double[maxLag+2];
        for(int lag=7;lag<=maxLag+1;lag++){double ab=0,aa=0,bb=0;for(int n=from+lag;n<to;n++){double a=wave[n],b=wave[n-lag];ab+=a*b;aa+=a*a;bb+=b*b;}correlation[lag]=aa*bb>1e-16?ab/Math.sqrt(aa*bb):0;}
        double best=0;for(int lag=8;lag<=maxLag;lag++)if(correlation[lag]>=correlation[lag-1]&&correlation[lag]>correlation[lag+1])best=Math.max(best,correlation[lag]);if(best<=0)return null;
        for(int lag=8;lag<=maxLag;lag++)if(correlation[lag]>=best-.04&&correlation[lag]>=correlation[lag-1]&&correlation[lag]>correlation[lag+1]){double denominator=correlation[lag-1]-2*correlation[lag]+correlation[lag+1],offset=Math.abs(denominator)>1e-12?.5*(correlation[lag-1]-correlation[lag+1])/denominator:0;offset=Math.max(-.5,Math.min(.5,offset));double rate=300/(lag+offset);return rate>=5&&rate<=40?new Peak(rate,Math.min(1,correlation[lag])):null;}return null;
    }
}
