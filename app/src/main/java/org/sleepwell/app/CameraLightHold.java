package org.sleepwell.app;
/** Relative image luma, not calibrated illuminance. Fresh continuously bright frames only. */
final class CameraLightHold {
 static int target(int level){return switch(level){case 1->160;case 2->190;case 3->220;default->throw new IllegalArgumentException("Choose a brightness level");};}
 static int percent(int level){return Math.round(target(level)*100f/255);}
 final int level;private long first=-1,last=-1;private int count;
 CameraLightHold(int level){target(level);this.level=level;}
 void reset(){first=last=-1;count=0;}
 boolean frame(double luma,long captured,long now){if(!Double.isFinite(luma)||luma<target(level)||luma>255||captured<0||captured>now||now-captured>800||captured<=last){reset();return false;}if(first<0||captured-last>400){first=captured;count=0;}last=captured;count++;return count>=6&&last-first>=1200;}
 int progress(){return first<0?0:Math.min(100,(int)((last-first)*100/1200));}
}
