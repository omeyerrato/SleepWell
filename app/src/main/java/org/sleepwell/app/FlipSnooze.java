package org.sleepwell.app;

/** Deliberate gravity posture, not a shake counter. Timestamps use elapsed-realtime nanoseconds. */
final class FlipSnooze {
    private static final long MS=1_000_000L;private final long armedAt;FlipSnooze(){this(0);}FlipSnooze(long armedAt){if(armedAt<0)throw new IllegalArgumentException("Invalid input admission clock");this.armedAt=armedAt;}private long last=-1,up=-1,down=-1;private boolean armed,done;
    boolean sample(long timestamp,long now,double x,double y,double z){if(done)return false;if(timestamp<armedAt||now<timestamp||now-timestamp>500*MS||!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z)){reset();return false;}if(timestamp<=last)return false;if(last>=0&&timestamp-last>500*MS)reset();last=timestamp;double magnitude=Math.sqrt(x*x+y*y+z*z);boolean stable=Math.abs(x)<4&&Math.abs(y)<4&&magnitude>=8&&magnitude<=12;
        if(stable&&z>=8){down=-1;if(up<0)up=timestamp;if(timestamp-up>=400*MS)armed=true;return false;}up=-1;if(armed&&stable&&z<=-8){if(down<0)down=timestamp;if(timestamp-down>=800*MS){done=true;return true;}}else down=-1;return false;
    }
    private void reset(){last=-1;up=-1;down=-1;armed=false;}
}
