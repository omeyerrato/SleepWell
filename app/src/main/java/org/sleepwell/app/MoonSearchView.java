package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.util.function.*;

/** Shared by the live challenge and its silent, output-free preview. */
final class MoonSearchView {
    static void render(LinearLayout body,MoonSearch board,BooleanSupplier current,Runnable complete){Context c=body.getContext();Design.text(body,"Find the sleeping moon",22,600,Design.INK).setGravity(Gravity.CENTER);Design.space(body,8);Design.text(body,"Look for the moon with closed eyes.",14,400,Design.MUTED).setGravity(Gravity.CENTER);Design.space(body,14);TextView feedback=Design.text(c,"",13,500,Design.BROWN);feedback.setGravity(Gravity.CENTER);feedback.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        LinearLayout grid=Design.col(c);grid.setContentDescription("Visual search board");final boolean[] solved={false};
        for(int row=0;row<board.columns;row++){LinearLayout line=Design.row(c);for(int column=0;column<board.columns;column++){int cell=row*board.columns+column;FrameLayout tile=new FrameLayout(c);tile.setMinimumWidth(Design.dp(c,48));tile.setMinimumHeight(Design.dp(c,56));tile.setBackground(Design.touch(c,Design.SOFT,14));tile.setContentDescription((board.sleeping(cell)?"Sleeping moon":"Awake moon")+", row "+(row+1)+", column "+(column+1));tile.setFocusable(true);tile.setOnClickListener(v->{if(solved[0]||!current.getAsBoolean())return;if(board.sleeping(cell)){solved[0]=true;RecordUi.enabled(grid,false);feedback.setText("Found it");complete.run();}else feedback.setText("That moon is awake. Keep looking.");});Face face=new Face(c,board.sleeping(cell),board.face(cell));tile.addView(face,new FrameLayout.LayoutParams(-1,Design.dp(c,56),Gravity.CENTER));LinearLayout.LayoutParams space=new LinearLayout.LayoutParams(0,Design.dp(c,64),1);space.setMargins(Design.dp(c,2),Design.dp(c,3),Design.dp(c,2),Design.dp(c,3));line.addView(tile,space);}grid.addView(line,new LinearLayout.LayoutParams(-1,-2));}body.addView(grid);Design.space(body,8);body.addView(feedback,new LinearLayout.LayoutParams(-1,-2));
    }
    static final class Face extends View {
        final boolean asleep;final int variant;final Paint paint=new Paint(3);final Path path=new Path();
        Face(Context c,boolean asleep,int variant){super(c);this.asleep=asleep;this.variant=variant;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);float size=Math.min(getWidth(),getHeight()),cx=getWidth()/2f,cy=getHeight()/2f,r=size*.31f;paint.setStyle(Paint.Style.FILL);paint.setColor(variant%2==0?NightDialTokens.SECONDARY:NightDialTokens.TEXT);canvas.drawCircle(cx,cy,r,paint);paint.setColor(Design.INK);paint.setStrokeWidth(size*.025f);paint.setStrokeCap(Paint.Cap.ROUND);for(int eye:new int[]{-1,1}){float x=cx+eye*r*.34f;if(asleep){paint.setStyle(Paint.Style.STROKE);canvas.drawArc(x-r*.18f,cy-r*.2f,x+r*.18f,cy+r*.04f,0,180,false,paint);}else{paint.setStyle(Paint.Style.FILL);canvas.drawOval(x-r*.07f,cy-r*.18f,x+r*.07f,cy+r*.04f,paint);}}paint.setStyle(Paint.Style.STROKE);canvas.drawArc(cx-r*.22f,cy+r*.08f,cx+r*.22f,cy+r*.38f,0,180,false,paint);paint.setStyle(Paint.Style.FILL);paint.setColor(Design.BROWN);canvas.drawCircle(cx+r*.55f,cy-r*.65f,r*.07f,paint);}
    }
    private MoonSearchView(){}
}
