package org.sleepwell.app;

/** Foreground listener ownership and occurrence fences; effects are injectable and silent in tests. */
final class WalkSession implements AutoCloseable {
    interface Sink {void sample(long timestamp,double value);}
    interface Port {WalkSteps.Mode mode();boolean allowed();long now();boolean begin(Sink sink);void close();}
    interface Effects {boolean current(String run,int unit);void progress(String run,int unit,int earned);void solved(String run,int unit);default void unavailable(String run,int unit,State state){}}
    enum State { WAITING, TRACKING, PERMISSION, UNSUPPORTED, ERROR, DONE, CLOSED }
    private final Port port;private final Effects effects;private final String run;private final int unit;private final WalkSteps steps;
    private int generation;private boolean active,delivered,closeFailed;private State state=State.WAITING;
    WalkSession(Port port,Effects effects,String run,int unit,int target,int earned){this.port=port;this.effects=effects;this.run=run;this.unit=unit;steps=new WalkSteps(target,earned);}
    State state(){return state;}int earned(){return steps.earned();}boolean cleanupPending(){return closeFailed;}
    void resume(){close();int ticket=++generation;state=State.WAITING;if(closeFailed){state=State.ERROR;return;}if(!effects.current(run,unit))return;try{if(!port.allowed()){state=State.PERMISSION;return;}WalkSteps.Mode mode=port.mode();if(mode==WalkSteps.Mode.NONE){state=State.UNSUPPORTED;return;}if(steps.done()){deliver();return;}steps.arm(mode,port.now());active=true;state=State.TRACKING;boolean listening=port.begin((time,value)->{if(!active||ticket!=generation||state!=State.TRACKING)return;if(!effects.current(run,unit)){close();return;}try{if(!port.allowed()){unavailable(State.PERMISSION);return;}}catch(SecurityException denied){unavailable(State.PERMISSION);return;}catch(RuntimeException failure){unavailable(State.ERROR);return;}if(steps.sample(time,value)){effects.progress(run,unit,steps.earned());if(!effects.current(run,unit)){close();return;}if(steps.done()){active=false;generation++;closeFailed=!release();if(closeFailed){state=State.ERROR;if(effects.current(run,unit))effects.unavailable(run,unit,state);}else deliver();}}});if(!listening&&state==State.TRACKING){active=false;closeFailed=!release();state=State.ERROR;}}catch(SecurityException denied){active=false;closeFailed=!release();state=State.PERMISSION;}catch(RuntimeException unavailable){active=false;closeFailed=!release();state=State.ERROR;}}
    /** Earned steps are durable, but a still-owned listener may never earn advancement. */
    private void deliver(){state=State.DONE;if(!delivered&&effects.current(run,unit)){delivered=true;effects.solved(run,unit);}}
    private void unavailable(State value){active=false;generation++;closeFailed=!release();state=value;if(effects.current(run,unit))effects.unavailable(run,unit,value);}
    /** Keeps an unconfirmed handle for retry; no callback can revive after this boundary. */
    boolean stopForFallback(){active=false;generation++;closeFailed=!release();state=State.CLOSED;return !closeFailed;}
    @Override public void close(){stopForFallback();}
    private boolean release(){try{port.close();return true;}catch(RuntimeException unavailable){return false;}}
}
