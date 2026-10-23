package org.sleepwell.app;

import android.content.Context;
import android.view.*;

/** Tags wrap by their measured width, including the Add control. */
final class RecordTagFlow extends ViewGroup {
    RecordTagFlow(Context context){super(context);}
    private int gap(){return Design.dp(getContext(),6);}
    @Override protected void onMeasure(int widthSpec,int heightSpec){int width=MeasureSpec.getSize(widthSpec),x=0,y=0,line=0;for(int n=0;n<getChildCount();n++){View child=getChildAt(n);measureChild(child,widthSpec,heightSpec);int w=child.getMeasuredWidth(),h=child.getMeasuredHeight();if(x>0&&x+w>width){y+=line;line=0;x=0;}x+=w+gap();line=Math.max(line,h);}setMeasuredDimension(width,resolveSize(y+line,heightSpec));}
    @Override protected void onLayout(boolean changed,int left,int top,int right,int bottom){int x=0,y=0,line=0;for(int n=0;n<getChildCount();n++){View child=getChildAt(n);int w=child.getMeasuredWidth(),h=child.getMeasuredHeight();if(x>0&&x+w>right-left){y+=line;x=0;line=0;}child.layout(x,y,x+w,y+h);x+=w+gap();line=Math.max(line,h);}}
    @Override protected LayoutParams generateDefaultLayoutParams(){return new LayoutParams(LayoutParams.WRAP_CONTENT,Design.dp(getContext(),48));}
}
