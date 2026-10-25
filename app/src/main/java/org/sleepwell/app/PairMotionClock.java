package org.sleepwell.app;

/** Four elapsed-clock timestamps, selecting the lowest-RTT recent estimate. No wall-clock synchronization. */
final class PairMotionClock {
    private double offset;private long uncertainty=Long.MAX_VALUE,measured=-1;
    boolean sample(long t1,long t2,long t3,long t4){if(t1<0||t2<0||t3<t2||t4<t1)return false;long rtt=(t4-t1)-(t3-t2);if(rtt<0||rtt>2000)return false;long error=(rtt+1)/2;if(measured<0||t4-measured>60000||error<=uncertainty){offset=((double)t2-t1+(double)t3-t4)/2;uncertainty=error;measured=t4;}return true;}
    boolean fresh(long elapsed){return measured>=0&&elapsed>=measured&&elapsed-measured<=60000&&uncertainty<=250;}
    long local(long remote){double value=remote-offset;if(!Double.isFinite(value)||value<0||value>Long.MAX_VALUE)throw new IllegalArgumentException("Peer clock is unavailable");return Math.round(value);}
    long offset(){return Math.round(offset);}long uncertainty(){return uncertainty;}long age(long elapsed){return measured<0?Long.MAX_VALUE:Math.max(0,elapsed-measured);}
}
