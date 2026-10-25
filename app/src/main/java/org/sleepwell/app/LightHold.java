package org.sleepwell.app;
/** Actual post-Start lux evidence. An on-change reading is a state until the listener reports a change. */
final class LightHold {
 static int target(int level){if(level<1||level>3)throw new IllegalArgumentException("Choose a supported light target");return new int[]{100,300,500}[level-1];}
 final int target;final long armed;final boolean changes;private long first=-1,last=-1;private int samples;private double lux=Double.NaN;
 LightHold(int level,long armed,boolean changes){target=target(level);this.armed=armed;this.changes=changes;}
 void reset(){first=last=-1;samples=0;lux=Double.NaN;}
 boolean sample(long at,long now,double value,boolean reliable){if(!reliable||!Double.isFinite(value)||value<0||at<armed||at>now||now-at>800000000L||at<=last){reset();return false;}if(!changes&&last>=0&&at-last>800000000L){first=-1;samples=0;}lux=value;last=at;if(value<target){first=-1;samples=0;return false;}if(first<0)first=at;samples++;return ready(now);}
 boolean ready(long now){if(first<0||last<0||now<last)return false;if(!changes&&now-last>800000000L){first=-1;samples=0;return false;}return now-first>=2000000000L&&(changes||samples>=3);}
 int progress(long now){return first<0?0:Math.min(100,(int)Math.max(0,(now-first)/1000000L)*100/2000);}
 long last(){return last;}double lux(){return lux;}
}
