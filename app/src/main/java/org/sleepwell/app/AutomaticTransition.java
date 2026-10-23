package org.sleepwell.app;

/** Exactly-once recording boundary; sensor callbacks only collect evidence, the main tick starts. */
final class AutomaticTransition {
    private final AutomaticSleep evidence;
    private final Runnable start;
    private boolean closed;
    AutomaticTransition(AutomaticSleep evidence,Runnable start){this.evidence=evidence;this.start=start;}
    void sample(long elapsed,double movement,boolean screenInUse){if(!closed)evidence.sample(elapsed,movement,screenInUse);}
    AutomaticSleep.State tick(long elapsed,boolean screenInUse){
        if(closed)return evidence.state();
        AutomaticSleep.State state=evidence.tick(elapsed,screenInUse);
        if(state==AutomaticSleep.State.EXPIRED||state==AutomaticSleep.State.CANCELLED)closed=true;
        if(state==AutomaticSleep.State.START){closed=true;start.run();}
        return state;
    }
    void cancel(){closed=true;evidence.cancel();}
}
