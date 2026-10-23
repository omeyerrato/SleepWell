package org.sleepwell.app;

/** One minute shorter per completed snooze, with a one-minute floor. */
final class SnoozePolicy {
    static int minutes(int base,boolean shorten,int used){return Math.max(1,Math.min(15,base)-(shorten?Math.max(0,Math.min(15,used)):0));}
}
