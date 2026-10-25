package org.sleepwell.app;

import android.content.Context;import android.graphics.*;import android.view.View;

final class TiltMazeView extends View {
    final TiltMaze maze;final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    TiltMazeView(Context context,TiltMaze maze){super(context);this.maze=maze;setContentDescription("Tilt maze. Guide the purple ball through the cream corridors to the gold target.");setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);}
    @Override protected void onMeasure(int width,int height){int size=MeasureSpec.getSize(width);setMeasuredDimension(size,size);}
    @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);float cell=getWidth()/(float)maze.size;paint.setColor(Design.BROWN);canvas.drawRoundRect(0,0,getWidth(),getHeight(),16,16,paint);for(int y=0;y<maze.size;y++)for(int x=0;x<maze.size;x++)if(maze.open(x,y)){paint.setColor(Design.SOFT);canvas.drawRect(x*cell,y*cell,(x+1)*cell,(y+1)*cell,paint);}paint.setColor(0xffffb333);canvas.drawCircle((maze.size-1.5f)*cell,(maze.size-1.5f)*cell,cell*.3f,paint);paint.setColor(0xff9060e0);canvas.drawCircle((float)maze.x*cell,(float)maze.y*cell,(float)TiltMaze.RADIUS*cell,paint);}
}
