package org.sleepwell.app;

import java.util.ArrayDeque;

/** Optional common-mode rejection heuristic, not a validated body/sleep detector. Originals are retained. */
final class PairMotionFilter {
    private final ArrayDeque<double[]> pairs=new ArrayDeque<>();
    Double apply(double own,double peer,boolean enabled){pairs.add(new double[]{own,peer});while(pairs.size()>24)pairs.removeFirst();if(!enabled||pairs.size()<12)return null;double x=0,y=0;for(double[] p:pairs){x+=p[0];y+=p[1];}x/=pairs.size();y/=pairs.size();double xx=0,yy=0,xy=0;for(double[] p:pairs){double a=p[0]-x,b=p[1]-y;xx+=a*a;yy+=b*b;xy+=a*b;}if(xx<1e-8||yy<1e-8||xy/Math.sqrt(xx*yy)<.8)return null;double shared=Math.max(0,Math.min(own*.5,peer*(x/Math.max(y,.000001))*.5));return Math.max(own*.5,own-shared);}
    void gap(){pairs.clear();}
}
