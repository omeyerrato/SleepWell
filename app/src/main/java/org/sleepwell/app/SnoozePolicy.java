package org.sleepwell.app;

/** One minute shorter per completed snooze, with a one-minute floor. */
final class SnoozePolicy {
    static int minutes(int base,boolean shorten,int used){return Math.max(1,Math.min(60,base)-(shorten?Math.max(0,Math.min(60,used)):0));}
    static int[] choices(int base,boolean shorten,int used){int maximum=minutes(base,shorten,used);java.util.SortedSet<Integer> values=new java.util.TreeSet<>();for(int value:new int[]{1,3,5,10,15,20,30,45,60})if(value<=maximum)values.add(value);values.add(maximum);return values.stream().mapToInt(Integer::intValue).toArray();}
    static boolean accepts(int requested,int base,boolean shorten,int used){return requested>=1&&requested<=minutes(base,shorten,used);}
}
