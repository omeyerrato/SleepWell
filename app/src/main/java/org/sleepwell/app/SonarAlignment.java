package org.sleepwell.app;
/** Bounded direct-reference correlation. A large microphone RMS cannot establish alignment. */
final class SonarAlignment {
 static final class Match {final double lag,correlation;Match(double lag,double correlation){this.lag=lag;this.correlation=correlation;}}
 static Match locate(short[] frame,double txFirst){if(frame.length!=SonarConfig.CHIRP||!Double.isFinite(txFirst))throw new IllegalArgumentException("Sonar alignment frame");double best=-1;int selected=0;for(int lag=-480;lag<=480;lag++){double score=score(frame,txFirst,lag);if(score>best){best=score;selected=lag;}}return new Match(selected,Math.max(0,Math.min(1,best)));}
 private static double score(short[] frame,double first,int lag){double sum=0,source=0,reference=0;for(int n=0;n<frame.length;n+=2){double x=frame[n]/32768d,y=SonarChirp.sample(first+n-lag);sum+=x*y;source+=x*x;reference+=y*y;}return source<1e-9?0:Math.abs(sum)/Math.sqrt(source*reference);}
}
