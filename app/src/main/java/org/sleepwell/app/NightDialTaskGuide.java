package org.sleepwell.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.widget.LinearLayout;

/** Original geometric instruction diagrams. Never a sensor reading, live task or saved result. */
final class NightDialTaskGuide extends View {
    private final String type;private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);private final Path path=new Path();
    static void add(LinearLayout body,String type){Context c=body.getContext();NightDialTaskGuide guide=new NightDialTaskGuide(c,type);body.addView(guide,new LinearLayout.LayoutParams(-1,Design.dp(c,178)));Design.space(body,18);}
    private NightDialTaskGuide(Context c,String type){super(c);this.type=type;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    private void color(int c,float stroke){p.setColor(c);p.setStrokeWidth(stroke);p.setStyle(stroke==0?Paint.Style.FILL:Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);}
    private void line(Canvas c,float x,float y,float xx,float yy,int ink,float stroke){color(ink,stroke);c.drawLine(x,y,xx,yy,p);}
    private void box(Canvas c,float x,float y,float w,float h,int ink,float stroke){color(ink,stroke);c.drawRoundRect(x,y,x+w,y+h,12,12,p);}
    private void circle(Canvas c,float x,float y,float r,int ink,float stroke){color(ink,stroke);c.drawCircle(x,y,r,p);}
    private void copy(Canvas c,String text,float x,float y,float size,int ink){color(ink,0);p.setTypeface(Design.font(getContext(),500));p.setTextAlign(Paint.Align.CENTER);p.setTextSize(size);c.drawText(text,x,y,p);}
    private void sun(Canvas c,float x,float y,float r,int ink){circle(c,x,y,r,ink,2);for(int n=0;n<8;n++){double a=Math.PI*n/4;line(c,x+(float)Math.cos(a)*(r+5),y+(float)Math.sin(a)*(r+5),x+(float)Math.cos(a)*(r+10),y+(float)Math.sin(a)*(r+10),ink,1.5f);}}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float scale=Math.min(getWidth()/320f,getHeight()/178f);c.save();c.translate((getWidth()-320*scale)/2,(getHeight()-178*scale)/2);c.scale(scale,scale);int amber=Design.BROWN,white=Design.INK,quiet=0xff49453e,ground=Design.SOFT;
        switch(type){
        case "Memory":{for(int n=0;n<6;n++){int x=52+(n%3)*76,y=12+(n/3)*64;box(c,x,y,64,54,ground,0);if(n==1||n==5){circle(c,x+32,y+27,12,amber,2);circle(c,x+32,y+27,4,amber,0);}else{color(quiet,1);path.reset();path.moveTo(x+32,y+13);path.lineTo(x+46,y+27);path.lineTo(x+32,y+41);path.lineTo(x+18,y+27);path.close();c.drawPath(path,p);}}break;}
        case "Sequence":for(int n=0;n<3;n++){if(n<2)line(c,94+n*74,75,116+n*74,75,quiet,1);box(c,42+n*78,42,64,68,n==1?0xff30281c:ground,0);copy(c,new String[]{"2","7","4"}[n],74+n*78,87,32,n==1?amber:white);}break;
        case "Counting":case "Visual search":for(int n=0;n<12;n++){float x=64+(n%4)*64,y=32+(n/4)*43;if(type.equals("Counting")){if(n%3==0)sun(c,x,y,7,amber);else if(n%2==0)box(c,x-7,y-7,14,14,quiet,1.5f);else circle(c,x,y,7,quiet,1.5f);}else{circle(c,x,y,11,n==6?amber:quiet,1.5f);color(Design.CREAM,0);c.drawCircle(x+5,y-4,10,p);line(c,x-4,y+3,x+1,y+4,n==6?amber:quiet,1);}}break;
        case "Retype":case "Mirror text":case "Spoken phrase":case "Dream journal":{box(c,47,16,226,120,ground,0);if(type.equals("Mirror text")){c.save();c.scale(-1,1,160,70);copy(c,"morning",160,76,30,amber);c.restore();}else if(type.equals("Dream journal")){for(int n=0;n<4;n++){line(c,70,45+n*21,n==3?181:248,45+n*21,n==0?amber:quiet,1.5f);}line(c,185,105,205,87,amber,3);}else{copy(c,"A quieter morning.",160,66,21,white);for(int n=0;n<4;n++)box(c,81+n*41,87,30,3,n==0?amber:quiet,0);}break;}
        case "Type code":c.save();c.rotate(-9,160,72);copy(c,"A4 N7",160,89,44,amber);c.restore();for(int n=0;n<4;n++)line(c,102+n*32,119,122+n*32,119,quiet,2);break;
        case "Tilt maze":{box(c,85,12,150,130,quiet,2);float[][] walls={{108,12,108,84},{108,84,166,84},{137,42,208,42},{208,42,208,114},{137,113,180,113}};for(float[] wall:walls)line(c,wall[0],wall[1],wall[2],wall[3],quiet,2);line(c,95,127,95,98,amber,2);circle(c,95,127,5,amber,0);circle(c,220,24,6,amber,1.5f);break;}
        case "Jumping":{line(c,42,119,278,119,quiet,1.5f);for(int n=0;n<3;n++)box(c,99+n*60,94,8,25,quiet,0);color(amber,1.5f);path.reset();path.moveTo(49,110);path.cubicTo(77,15,132,13,165,86);c.drawPath(path,p);circle(c,114,40,12,white,0);break;}
        case "Flags":line(c,105,18,105,142,quiet,3);color(amber,1.5f);path.reset();path.moveTo(106,22);path.cubicTo(146,2,178,44,218,23);path.lineTo(218,93);path.cubicTo(177,116,147,73,106,93);path.close();c.drawPath(path,p);line(c,118,47,207,69,quiet,1);break;
        case "Walk":for(int n=0;n<4;n++){c.save();c.translate(110+n*31,117-n*27);c.rotate(n%2==0?-20:20);color(n==3?amber:quiet,0);c.drawOval(-8,-15,8,15,p);c.drawOval(-6,19,6,28,p);c.restore();}break;
        case "Shake":case "Spin around":{box(c,131,24,58,108,amber,2);line(c,150,121,170,121,amber,2);if(type.equals("Shake")){for(int n=0;n<2;n++){float x=n==0?94:226;line(c,x,47,x+(n==0?-14:14),63,quiet,2);line(c,x+(n==0?-14:14),63,x,80,quiet,2);line(c,x,80,x+(n==0?-14:14),97,quiet,2);}}else{color(quiet,2);c.drawArc(74,15,246,145,-45,275,false,p);line(c,216,16,222,40,amber,2);line(c,222,40,197,37,amber,2);}break;}
        case "Pose":circle(c,160,42,13,amber,2);line(c,160,57,160,94,amber,2);line(c,160,65,126,42,amber,2);line(c,126,42,116,13,amber,2);line(c,160,65,194,42,amber,2);line(c,194,42,204,13,amber,2);line(c,160,94,137,137,quiet,2);line(c,160,94,183,137,quiet,2);break;
        case "Smile":circle(c,160,75,56,quiet,1.5f);circle(c,141,65,3,white,0);circle(c,179,65,3,white,0);color(amber,2);c.drawArc(133,67,187,104,15,150,false,p);break;
        case "Light":case "Camera light":sun(c,160,75,29,amber);if(type.equals("Camera light")){box(c,68,9,184,132,quiet,1);line(c,80,20,100,20,white,2);line(c,80,20,80,40,white,2);line(c,240,130,220,130,white,2);line(c,240,130,240,110,white,2);}break;
        case "NFC":box(c,90,32,62,96,quiet,2);circle(c,226,80,20,amber,2);color(amber,2);c.drawArc(156,46,200,114,-65,130,false,p);c.drawArc(151,31,220,129,-65,130,false,p);break;
        case "Musical note":for(int n=0;n<5;n++)line(c,68,43+n*16,252,43+n*16,quiet,1);circle(c,148,107,9,amber,0);line(c,157,105,157,29,amber,2);line(c,157,29,181,40,amber,3);break;
        default:for(int n=0;n<27;n++){float x=56+n*8;float amplitude=(float)(8+32*Math.pow(Math.sin(n*.28),2));line(c,x,75-amplitude,x,75+amplitude,n%5==0?amber:quiet,3);}break;
        }
        copy(c,"ILLUSTRATED GUIDE",160,170,9,Design.MUTED);c.restore();
    }
}
