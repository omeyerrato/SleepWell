package org.sleepwell.app;

/** Original deterministic touch game. Distances are world units; no sensor input. */
final class JumpGame {
 static final int TARGET=3;static final double WIDTH=1000,HEIGHT=420,FLOOR=330,X=180,RADIUS=22,CONTACT_RADIUS=19,SPEED=260,GRAVITY=1350,IMPULSE=560,SPAWN=900;
 final long seed;int cleared;double obstacleX=SPAWN,height,velocity;boolean crashed;
 JumpGame(String run,int unit,int round,int cleared){seed=CountingPuzzle.seed(run,unit,round,"jump-v1");if(cleared<0||cleared>TARGET)throw new IllegalArgumentException("Invalid jump checkpoint");this.cleared=cleared;}
 double obstacleWidth(){return 52+Math.floorMod(seed+cleared*101,25);}
 double obstacleHeight(){return 48+Math.floorMod(seed/31+cleared*73,17);}
 boolean complete(){return cleared==TARGET;}
 boolean jump(){if(crashed||complete()||height>0||velocity!=0)return false;velocity=IMPULSE;return true;}
 void retry(){if(!complete()){obstacleX=SPAWN;height=velocity=0;crashed=false;}}
 void advance(long millis){if(millis<0||millis>100)throw new IllegalArgumentException("Advance a bounded live frame");if(crashed||complete())return;for(long left=millis;left>0&&!crashed&&!complete();){long step=Math.min(5,left);left-=step;double dt=step/1000d;obstacleX-=SPEED*dt;height+=velocity*dt-.5*GRAVITY*dt*dt;velocity-=GRAVITY*dt;if(height<=0){height=0;velocity=0;}if(collides(X,FLOOR-RADIUS-height,CONTACT_RADIUS,obstacleX,FLOOR-obstacleHeight(),obstacleWidth(),obstacleHeight())){crashed=true;return;}if(obstacleX+obstacleWidth()<X-RADIUS){cleared++;retry();}}}
 static boolean collides(double cx,double cy,double radius,double x,double y,double width,double height){if(!Double.isFinite(cx)||!Double.isFinite(cy)||!Double.isFinite(radius)||!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(width)||!Double.isFinite(height)||radius<=0||width<=0||height<=0)throw new IllegalArgumentException("Invalid collision geometry");double px=Math.max(x,Math.min(x+width,cx)),py=Math.max(y,Math.min(y+height,cy));double dx=cx-px,dy=cy-py;return dx*dx+dy*dy<radius*radius;}
}
