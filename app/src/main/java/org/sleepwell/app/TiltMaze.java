package org.sleepwell.app;

import java.util.*;

/** Deterministic perfect corridor maze and continuous circle/wall collision, in cell coordinates. */
final class TiltMaze {
    static final double RADIUS=.18;final int size;private final boolean[][] path;double x=1.5,y=1.5;
    TiltMaze(String run,int unit,int round){Random random=new Random(CountingPuzzle.seed(run,unit,round,"tilt-v1"));int cells=round<3?3:4;size=cells*2+1;path=new boolean[size][size];boolean[][] visited=new boolean[cells][cells];carve(0,0,visited,random);}
    private void carve(int x,int y,boolean[][] visited,Random random){visited[y][x]=true;path[y*2+1][x*2+1]=true;List<int[]> directions=new ArrayList<>(List.of(new int[]{1,0},new int[]{-1,0},new int[]{0,1},new int[]{0,-1}));Collections.shuffle(directions,random);for(int[] d:directions){int nx=x+d[0],ny=y+d[1];if(nx<0||ny<0||nx>=visited.length||ny>=visited.length||visited[ny][nx])continue;path[y*2+1+d[1]][x*2+1+d[0]]=true;carve(nx,ny,visited,random);}}
    boolean open(int x,int y){return x>=0&&y>=0&&x<size&&y<size&&path[y][x];}
    boolean legal(double x,double y){if(!Double.isFinite(x)||!Double.isFinite(y)||x<RADIUS||y<RADIUS||x>size-RADIUS||y>size-RADIUS)return false;for(int row=(int)Math.floor(y-RADIUS);row<=(int)Math.floor(y+RADIUS);row++)for(int col=(int)Math.floor(x-RADIUS);col<=(int)Math.floor(x+RADIUS);col++)if(!open(col,row)){double nearestX=Math.max(col,Math.min(x,col+1)),nearestY=Math.max(row,Math.min(y,row+1));if(Math.hypot(x-nearestX,y-nearestY)<RADIUS-1e-8)return false;}return true;}
    void restore(double x,double y){if(!legal(x,y))throw new IllegalArgumentException("Saved maze position is invalid");this.x=x;this.y=y;}
    void move(double dx,double dy){if(!Double.isFinite(dx)||!Double.isFinite(dy)||Math.abs(dx)>.5||Math.abs(dy)>.5)return;int steps=Math.max(1,(int)Math.ceil(Math.max(Math.abs(dx),Math.abs(dy))/.04));for(int n=0;n<steps;n++){double nx=x+dx/steps,ny=y+dy/steps;if(legal(nx,y))x=nx;if(legal(x,ny))y=ny;}}
    boolean solved(){return Math.hypot(x-(size-1.5),y-(size-1.5))<.25;}
}
