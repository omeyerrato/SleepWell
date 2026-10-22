package org.sleepwell.app;

/** Elapsed-realtime clock. Wall-clock changes cannot extend recording or sound limits. */
final class TrackingClock {
    private final long begun;private long activeStart,accumulated;private boolean paused;
    TrackingClock(long elapsed){begun=activeStart=elapsed;}
    long active(long elapsed){return accumulated+(paused?0:Math.max(0,elapsed-activeStart));}
    long lifetime(long elapsed){return Math.max(0,elapsed-begun);}
    long timestamp(long wallStart,long elapsed){return wallStart+lifetime(elapsed);}
    void pause(long elapsed){if(!paused){accumulated=active(elapsed);paused=true;}}
    void resume(long elapsed){if(paused){activeStart=elapsed;paused=false;}}
    boolean paused(){return paused;}
}
