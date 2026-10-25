package org.sleepwell.app;
/** Semantic-score hold, not motion/liveness/identity evidence. All1000 classes compete. */
final class ObjectHold {
 private final ObjectTarget target;private long first,last=-1;private int count;
 ObjectHold(ObjectTarget target){this.target=target;}
 void reset(){first=0;last=-1;count=0;}
 boolean frame(float[] scores,long captured,long now){if(scores==null||scores.length!=1000||captured<0||captured>now||now-captured>800||captured<=last){reset();return false;}float chosen=scores[target.index],other=0;for(int n=0;n<scores.length;n++){float value=scores[n];if(!Float.isFinite(value)||value<0||value>1){reset();return false;}if(n!=target.index)other=Math.max(other,value);}if(chosen<.70f||chosen-other<.15f){reset();return false;}if(last<0||captured-last>600){count=0;first=captured;}last=captured;count++;return count>=4&&captured-first>=900;}
 int progress(){return Math.min(100,(int)Math.max(0,last-first)*100/900);}
}
