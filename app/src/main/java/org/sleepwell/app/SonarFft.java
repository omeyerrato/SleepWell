package org.sleepwell.app;
/** In-place radix-two complex FFT; original implementation, no downloaded DSP/runtime dependency. */
final class SonarFft {
 static void transform(double[] real,double[] imag){int size=real.length;if(size!=imag.length||size<2||(size&(size-1))!=0)throw new IllegalArgumentException("FFT size");for(int a=1,b=0;a<size;a++){int bit=size>>1;for(;(b&bit)!=0;bit>>=1)b^=bit;b^=bit;if(a<b){double x=real[a];real[a]=real[b];real[b]=x;x=imag[a];imag[a]=imag[b];imag[b]=x;}}for(int span=2;span<=size;span<<=1){double angle=-2*Math.PI/span,wr=Math.cos(angle),wi=Math.sin(angle);for(int start=0;start<size;start+=span){double xr=1,xi=0;for(int n=0;n<span/2;n++){int a=start+n,b=a+span/2;double r=xr*real[b]-xi*imag[b],i=xr*imag[b]+xi*real[b];real[b]=real[a]-r;imag[b]=imag[a]-i;real[a]+=r;imag[a]+=i;double next=xr*wr-xi*wi;xi=xr*wi+xi*wr;xr=next;}}}}
}
