package org.sleepwell.app;
/** Original phase-continuous FMCW reference: 770 complete cycles per 40ms chirp. */
final class SonarChirp {
 static double sample(double frame){double n=frame%SonarConfig.CHIRP;if(n<0)n+=SonarConfig.CHIRP;double t=n/SonarConfig.RATE;return Math.cos(2*Math.PI*(18500*t+.5*(1500/(SonarConfig.CHIRP/(double)SonarConfig.RATE))*t*t));}
 static void fill(short[] pcm,int offset,int count,long first){for(int n=0;n<count;n++)pcm[offset+n]=(short)Math.round(Short.MAX_VALUE*sample(first+n));}
}
