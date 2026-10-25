package org.sleepwell.app;
/** Original upper-body rule over actual ML Kit landmarks, not face probabilities or clinical judgment. */
final class PoseHold {
 // nose, left/right shoulder, left/right elbow, left/right wrist, left/right hip.
 static final class Sample{final double[] x,y,confidence;final long at;Sample(double[] x,double[] y,double[] confidence,long at){if(x.length!=9||y.length!=9||confidence.length!=9)throw new IllegalArgumentException("Nine upper-body landmarks required");this.x=x.clone();this.y=y.clone();this.confidence=confidence.clone();this.at=at;}
 boolean framed(){for(int n=0;n<9;n++)if(!Double.isFinite(x[n])||!Double.isFinite(y[n])||!Double.isFinite(confidence[n])||confidence[n]<.75||confidence[n]>1||x[n]<.04||x[n]>.96||y[n]<.04||y[n]>.96)return false;double shoulders=(y[1]+y[2])/2,hips=(y[7]+y[8])/2,torso=hips-shoulders,width=Math.abs(x[1]-x[2]);return torso>=.15&&torso<=.65&&width>=.10&&width<=.65&&Math.abs(y[1]-y[2])<=.10&&Math.abs(y[7]-y[8])<=.12&&Math.abs((x[1]+x[2]-x[7]-x[8])/2)<=.12&&Math.abs(x[7]-x[8])>=.08&&y[0]<shoulders-.02;}
 boolean raised(){if(!framed())return false;double shoulders=(y[1]+y[2])/2;return y[5]<y[0]-.02&&y[6]<y[0]-.02&&y[3]<shoulders+.03&&y[4]<shoulders+.03&&y[5]<y[3]-.02&&y[6]<y[4]-.02&&Math.abs(x[5]-x[6])>=Math.abs(x[1]-x[2])*.5;}}
 private Sample previous;private long first,last=-1;private int count;
 void reset(){previous=null;first=0;last=-1;count=0;}
 boolean frame(Sample sample,long now){if(sample==null||!sample.raised()||sample.at<0||sample.at>now||now-sample.at>800||sample.at<=last){reset();return false;}if(previous==null||sample.at-last>400||!stable(previous,sample)){count=0;first=sample.at;}previous=sample;last=sample.at;count++;return count>=6&&sample.at-first>=1200;}
 int progress(){return Math.min(100,(int)Math.max(0,last-first)*100/1200);}
 static boolean stable(Sample a,Sample b){for(int n=0;n<9;n++)if(Math.hypot(a.x[n]-b.x[n],a.y[n]-b.y[n])>.07)return false;return true;}
}
