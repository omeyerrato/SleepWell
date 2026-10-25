package org.sleepwell.app;
/** Main-loop service start ownership; a retiring callback cannot stop a replacement. */
final class SpeakServiceSlot<T> {
    private T current;private int startId;
    boolean occupied(java.util.function.Predicate<T> released){return current!=null&&!released.test(current);}
    void adopt(T next,int id){current=next;startId=id;}
    boolean owns(T candidate,int id){return current==candidate&&startId==id;}
}
