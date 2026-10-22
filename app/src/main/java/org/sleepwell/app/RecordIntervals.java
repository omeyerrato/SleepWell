package org.sleepwell.app;

import java.util.*;

/** Original interval arithmetic. Unknown sensor readings are never filled here. */
final class RecordIntervals {
    static final class Span {
        final long start,end;
        Span(long start,long end){this.start=start;this.end=end;}
    }
    static List<Span> union(List<Span> input){
        List<Span> sorted=new ArrayList<>();for(Span s:input)if(s.end>s.start)sorted.add(s);
        sorted.sort(Comparator.comparingLong(s->s.start));List<Span> out=new ArrayList<>();
        for(Span s:sorted){if(out.isEmpty()||s.start>out.get(out.size()-1).end)out.add(s);else{Span last=out.remove(out.size()-1);out.add(new Span(last.start,Math.max(last.end,s.end)));}}return out;
    }
    static List<Span> intersect(List<Span> input,long start,long end){List<Span> out=new ArrayList<>();for(Span s:input){long a=Math.max(start,s.start),b=Math.min(end,s.end);if(b>a)out.add(new Span(a,b));}return union(out);}
    static List<Span> subtract(List<Span> input,List<Span> excluded){
        List<Span> out=new ArrayList<>(),cuts=union(excluded);
        for(Span s:union(input)){long at=s.start;for(Span cut:cuts){if(cut.end<=at)continue;if(cut.start>=s.end)break;if(cut.start>at)out.add(new Span(at,Math.min(s.end,cut.start)));at=Math.max(at,cut.end);if(at>=s.end)break;}if(at<s.end)out.add(new Span(at,s.end));}return out;
    }
    static long length(List<Span> input){long total=0;for(Span s:union(input))total=Math.addExact(total,s.end-s.start);return total;}
    /** Monotonic progress is authoritative; an interrupted tail cannot invent extra active time. */
    static List<Span> limit(List<Span> input,Long maximum){
        List<Span> out=new ArrayList<>();long left=maximum==null?Long.MAX_VALUE:Math.max(0,maximum);
        for(Span s:union(input)){long take=Math.min(left,s.end-s.start);if(take>0)out.add(new Span(s.start,s.start+take));left-=take;if(left==0)break;}return out;
    }
}
