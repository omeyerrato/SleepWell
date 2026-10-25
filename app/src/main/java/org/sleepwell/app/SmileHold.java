package org.sleepwell.app;
/** Four fresh, steadily framed smile detections. This is expression detection, not liveness. */
final class SmileHold {
 static final class Sample{final double left,top,right,bottom,smile,yaw,pitch,roll;final long at;Sample(double l,double t,double r,double b,double smile,double yaw,double pitch,double roll,long at){left=l;top=t;right=r;bottom=b;this.smile=smile;this.yaw=yaw;this.pitch=pitch;this.roll=roll;this.at=at;}boolean framed(){for(double n:new double[]{left,top,right,bottom,smile,yaw,pitch,roll})if(!Double.isFinite(n))return false;double w=right-left,h=bottom-top;return left>=.08&&top>=.08&&right<=.92&&bottom<=.92&&w>=.18&&h>=.18&&w<=.75&&h<=.75&&Math.abs(yaw)<=18&&Math.abs(pitch)<=20&&Math.abs(roll)<=18&&smile>=.80&&smile<=1;}}
 private Sample previous;private long first,last=-1;private int count;
 void reset(){previous=null;first=0;last=-1;count=0;}
 boolean frame(Sample sample,long now){if(sample==null||!sample.framed()||sample.at<0||sample.at>now||now-sample.at>800||sample.at<=last){reset();return false;}if(previous==null||sample.at-last>400||!stable(previous,sample)){count=0;first=sample.at;}previous=sample;last=sample.at;count++;return count>=4&&sample.at-first>=600;}
 int progress(){return Math.min(100,(int)Math.max(0,last-first)*100/600);}
 static boolean stable(Sample a,Sample b){double areaA=(a.right-a.left)*(a.bottom-a.top),areaB=(b.right-b.left)*(b.bottom-b.top);double overlap=Math.max(0,Math.min(a.right,b.right)-Math.max(a.left,b.left))*Math.max(0,Math.min(a.bottom,b.bottom)-Math.max(a.top,b.top));return overlap/(areaA+areaB-overlap)>=.65&&Math.abs((a.left+a.right-b.left-b.right)/2)<=.08&&Math.abs((a.top+a.bottom-b.top-b.bottom)/2)<=.08;}
}
