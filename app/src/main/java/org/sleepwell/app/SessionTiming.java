package org.sleepwell.app;

import java.util.*;

/** Conservative wall-span fallback for historical/imported records without monotonic duration. */
final class SessionTiming {
    static long recorded(long start,long end,Long active,List<long[]> pauses){
        if(active!=null)return Math.max(0,active);
        if(end<=start)return 0;
        List<long[]> intervals=new ArrayList<>();for(long[] p:pauses){long a=Math.max(start,p[0]),b=Math.min(end,p[1]);if(b>a)intervals.add(new long[]{a,b});}
        intervals.sort(Comparator.comparingLong(p->p[0]));long excluded=0,from=0,to=0;boolean open=false;
        for(long[] p:intervals){if(!open){from=p[0];to=p[1];open=true;}else if(p[0]<=to)to=Math.max(to,p[1]);else{excluded+=to-from;from=p[0];to=p[1];}}
        if(open)excluded+=to-from;return Math.max(0,end-start-excluded);
    }
}
