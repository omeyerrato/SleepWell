package org.sleepwell.app;
/** Fresh tonal target frames must persist continuously; gaps/off-note/noise reset the hold. */
final class NoteHold {
 final double target;final int tolerance;private long first=-1,last=-1;private int count;
 NoteHold(String run,int unit,int level){target=NotePitch.target(run,unit);tolerance=NotePitch.tolerance(level);}
 void reset(){first=last=-1;count=0;}
 boolean accept(double pitch,long at,long now){if(!Double.isFinite(pitch)||pitch<=0||Math.abs(NotePitch.cents(pitch,target))>tolerance||at<0||at>now||now-at>400||at<=last){reset();return false;}if(first<0||at-last>400){first=at;count=0;}last=at;count++;return count>=8&&last-first>=1200;}
 int progress(){return first<0?0:Math.min(100,(int)((last-first)*100/1200));}
}
