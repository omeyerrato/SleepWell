package org.sleepwell.app;

/** Exactly-once recording boundary; sensor callbacks only collect evidence, the main tick starts. */
final class AutomaticTransition {
    private final AutomaticSleep evidence;
    private final Runnable start;private final java.util.function.BooleanSupplier allowed;
    private boolean closed;
    AutomaticTransition(AutomaticSleep evidence,Runnable start){this(evidence,start,()->true);}
    AutomaticTransition(AutomaticSleep evidence,Runnable start,java.util.function.BooleanSupplier allowed){this.evidence=evidence;this.start=start;this.allowed=allowed;}
    void sample(long elapsed,double movement,boolean screenInUse){if(!closed){if(allowed.getAsBoolean())evidence.sample(elapsed,movement,screenInUse);else evidence.hold(elapsed);}}
    AutomaticSleep.State tick(long elapsed,boolean screenInUse){
        if(closed)return evidence.state();
        AutomaticSleep.State state=allowed.getAsBoolean()?evidence.tick(elapsed,screenInUse):evidence.hold(elapsed);
        if(state==AutomaticSleep.State.START&&!allowed.getAsBoolean())state=evidence.hold(elapsed);
        if(state==AutomaticSleep.State.EXPIRED||state==AutomaticSleep.State.CANCELLED)closed=true;
        if(state==AutomaticSleep.State.START){closed=true;start.run();}
        return state;
    }
    void cancel(){closed=true;evidence.cancel();}
}
