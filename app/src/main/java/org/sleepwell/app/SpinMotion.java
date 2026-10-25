package org.sleepwell.app;
/** Short continuous orientation turn, not a shake/step counter. Opposite movement removes progress. */
final class SpinMotion {
 private long last=-1;private double heading,total;private int direction;private boolean touched;
 void contact(boolean both){if(touched!=both){reset();touched=both;}}
 void reset(){last=-1;total=0;direction=0;}
 boolean sample(long stamp,long now,double angle,boolean upright){if(!touched||stamp<0||stamp>now||now-stamp>250_000_000L||!Double.isFinite(angle)||!upright||stamp<=last){reset();return false;}if(last<0){last=stamp;heading=angle;return false;}long elapsed=stamp-last;double delta=Math.atan2(Math.sin(angle-heading),Math.cos(angle-heading));heading=angle;last=stamp;if(elapsed>300_000_000L||Math.abs(delta)>Math.toRadians(18)||Math.abs(delta)/(elapsed/1e9)>Math.toRadians(180)){total=0;direction=0;return false;}if(direction==0){total+=delta;if(Math.abs(total)>=Math.toRadians(20))direction=total>0?1:-1;}else total+=delta;if(direction!=0&&total*direction<0){total=0;direction=0;}return direction!=0&&total*direction>=Math.PI*2;}
 int degrees(){return Math.max(0,Math.min(360,(int)Math.toDegrees(direction==0?Math.abs(total):total*direction)));}
 long last(){return last;}
}
