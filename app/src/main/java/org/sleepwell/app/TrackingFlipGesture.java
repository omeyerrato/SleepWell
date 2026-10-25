package org.sleepwell.app;
/** Fresh, stable gravity posture sequence; not an awake or sleep-stage classifier. */
final class TrackingFlipGesture {
 static final long MS=1000000L;final long armed;final int baseline;long last=-1,stable=-1,target=-1,fresh=-1;int baselineSamples,targetSamples;boolean ready,done;
 TrackingFlipGesture(long armed,int baseline){if(armed<0||Math.abs(baseline)!=1)throw new IllegalArgumentException("Invalid gesture admission");this.armed=armed;this.baseline=baseline;}
 boolean sample(long timestamp,long now,double x,double y,double z,boolean reliable){if(done)return false;if(!reliable||timestamp<armed||now<timestamp||now-timestamp>250*MS||!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z)){reset();return false;}if(timestamp<=last)return false;if(last>=0&&timestamp-last>300*MS)reset();last=timestamp;double length=Math.sqrt(x*x+y*y+z*z);boolean flat=Math.abs(x)<3&&Math.abs(y)<3&&length>=8.5&&length<=11;
  if(flat&&baseline*z>=8.5){target=-1;targetSamples=0;if(stable<0)stable=timestamp;baselineSamples++;if(timestamp-stable>=1000*MS&&baselineSamples>=5){ready=true;fresh=timestamp;}return false;}
  stable=-1;baselineSamples=0;if(ready&&fresh>=0&&timestamp-fresh<=10000*MS&&flat&&baseline*z<=-8.5){if(target<0)target=timestamp;targetSamples++;if(timestamp-target>=1200*MS&&targetSamples>=5){done=true;return true;}}else{target=-1;targetSamples=0;}return false;
 }
 void reset(){last=stable=target=fresh=-1;baselineSamples=targetSamples=0;ready=done=false;}
}
