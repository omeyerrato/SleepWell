package org.sleepwell.app;

/** Fresh monotonic sample admission, still calibration and bounded relative tilt; no Android API. */
final class TiltMotion {
    private long armed,last,calibrationStart;private int samples;private double sumX,sumY,minX,maxX,minY,maxY,baseX,baseY,filteredX,filteredY;private boolean calibrated;
    TiltMotion(long armed){reset(armed);}
    void reset(long now){armed=now;last=0;calibrationStart=0;samples=0;sumX=sumY=0;minX=minY=Double.POSITIVE_INFINITY;maxX=maxY=Double.NEGATIVE_INFINITY;calibrated=false;}
    boolean calibrated(){return calibrated;}
    long last(){return last;}
    static double[] axes(double x,double y,int rotation){return switch(rotation){case 0->new double[]{x,y};case 1->new double[]{-y,x};case 2->new double[]{-x,-y};case 3->new double[]{y,-x};default->throw new IllegalArgumentException("Invalid rotation");};}
    double[] sample(long time,long now,double x,double y,double z,int rotation){if(time<armed||time<=last||time>now||now-time>250_000_000L||!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z))return null;double norm=Math.sqrt(x*x+y*y+z*z);if(norm<7||norm>12.5||Math.abs(z)<5)return null;double[] a=axes(x,y,rotation);long previous=last;last=time;
        if(!calibrated){if(calibrationStart==0)calibrationStart=time;sumX+=a[0];sumY+=a[1];samples++;minX=Math.min(minX,a[0]);maxX=Math.max(maxX,a[0]);minY=Math.min(minY,a[1]);maxY=Math.max(maxY,a[1]);if(maxX-minX>.7||maxY-minY>.7){reset(time);return null;}if(samples>=12&&time-calibrationStart>=600_000_000L){baseX=sumX/samples;baseY=sumY/samples;filteredX=baseX;filteredY=baseY;calibrated=true;}return null;}
        filteredX=.8*filteredX+.2*a[0];filteredY=.8*filteredY+.2*a[1];if(previous==0||time-previous>250_000_000L)return null;double seconds=Math.min(.05,(time-previous)/1e9),dx=-(filteredX-baseX),dy=filteredY-baseY;return new double[]{velocity(dx)*seconds,velocity(dy)*seconds};
    }
    private static double velocity(double value){return Math.abs(value)<.15?0:Math.max(-2.4,Math.min(2.4,value*.8));}
}
