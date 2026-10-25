package org.sleepwell.app;

/** Conservative, opt-in phone-idle inference. It cannot tell a sleeping person from an idle phone. */
final class AutomaticSleep {
    enum Mode { MANUAL, BEDTIME, SMART_WINDOW, STILLNESS }
    enum State { WAITING, SCREEN_IN_USE, COLLECTING, QUIET, START, EXPIRED, CANCELLED }
    static final long MINUTE=60_000L, QUIET_REQUIRED=15*MINUTE, MAX_WAIT=4*60*MINUTE;
    private final Mode mode;
    private final long armed, scheduled;
    private long lastSample=-1, quietSince=-1;
    private int samples;
    private State state=State.WAITING;
    AutomaticSleep(Mode mode,long armed,long scheduled){
        if(mode==null||mode==Mode.MANUAL)throw new IllegalArgumentException("Choose an automatic start mode.");
        if(mode!=Mode.STILLNESS&&(scheduled<armed||scheduled-armed>MAX_WAIT))throw new IllegalArgumentException("Arm within four hours of the start time.");
        this.mode=mode;this.armed=armed;this.scheduled=scheduled;
    }
    State sample(long elapsed,double movement,boolean screenInUse){
        if(terminal()&&!(state==State.START&&mode==Mode.STILLNESS))return state;
        if(elapsed<armed||elapsed<lastSample)throw new IllegalArgumentException("Sensor time moved backwards.");
        if(elapsed-armed>=MAX_WAIT)return state=State.EXPIRED;
        boolean gap=lastSample<0||elapsed-lastSample>2000;
        lastSample=elapsed;
        if(mode!=Mode.STILLNESS){state=elapsed>=scheduled?State.START:State.WAITING;return state;}
        if(screenInUse){reset();return state=State.SCREEN_IN_USE;}
        if(!Double.isFinite(movement)||movement<0||movement>.08){reset();return state=State.COLLECTING;}
        if(gap){reset();quietSince=elapsed;}
        if(quietSince<0)quietSince=elapsed;
        samples++;
        long duration=elapsed-quietSince;
        // Five-Hz sensor requested; require >= 2 samples/s as well as no two-second gap.
        if(duration>=QUIET_REQUIRED&&samples>=duration/500)return state=State.START;
        return state=State.QUIET;
    }
    State tick(long elapsed,boolean screenInUse){
        // A qualifying sensor callback is only a proposal until the main-loop transition.
        // Recheck the current screen/signal so a just-unlocked phone cannot start recording.
        if(state==State.START&&mode==Mode.STILLNESS){
            if(screenInUse){reset();state=State.SCREEN_IN_USE;}
            else if(elapsed-lastSample>2000){reset();state=State.COLLECTING;}
        }
        if(terminal())return state;
        if(elapsed-armed>=MAX_WAIT)return state=State.EXPIRED;
        if(mode!=Mode.STILLNESS)return state=elapsed>=scheduled?State.START:State.WAITING;
        if(screenInUse){reset();return state=State.SCREEN_IN_USE;}
        if(lastSample<0||elapsed-lastSample>2000){reset();return state=State.COLLECTING;}
        return state;
    }
    State hold(long elapsed){if(state==State.EXPIRED||state==State.CANCELLED)return state;if(elapsed<armed||elapsed<lastSample)throw new IllegalArgumentException("Wait time moved backwards.");reset();lastSample=elapsed;return state=elapsed-armed>=MAX_WAIT?State.EXPIRED:State.WAITING;}
    void cancel(){if(!terminal())state=State.CANCELLED;}
    State state(){return state;}
    long quietMillis(long elapsed){return quietSince<0?0:Math.max(0,Math.min(QUIET_REQUIRED,elapsed-quietSince));}
    private void reset(){quietSince=-1;samples=0;}
    private boolean terminal(){return state==State.START||state==State.EXPIRED||state==State.CANCELLED;}
}
