package org.sleepwell.app;

/** One attempt per recorded night; only tracking's own pause may auto-resume its mix. */
final class SoundOwnership {
    private long attempted,owner;private boolean trackingPaused;
    boolean claim(long session,boolean occupied){if(session<=0||attempted==session)return false;attempted=session;if(occupied)return false;owner=session;trackingPaused=false;return true;}
    void manual(){owner=0;trackingPaused=false;}
    boolean pause(long session){if(owner!=session||owner==0)return false;trackingPaused=true;return true;}
    boolean resume(long session){if(owner!=session||!trackingPaused)return false;trackingPaused=false;return true;}
    boolean stop(long session){if(owner!=session||owner==0)return false;owner=0;trackingPaused=false;return true;}
}
