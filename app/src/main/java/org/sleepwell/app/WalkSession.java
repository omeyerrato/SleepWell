package org.sleepwell.app;

/** Foreground listener ownership and occurrence fences; effects are injectable and silent in tests. */
final class WalkSession implements AutoCloseable {
    interface Sink {void sample(long timestamp,double value);}
    interface Port {WalkSteps.Mode mode();boolean allowed();long now();boolean begin(Sink sink);void close();}
    interface Effects {boolean current(String run,int unit);void progress(String run,int unit,int earned);void solved(String run,int unit);}
    enum State { WAITING, TRACKING, PERMISSION, UNSUPPORTED, ERROR, DONE, CLOSED }
    private final Port port;private final Effects effects;private final String run;private final int unit;private final WalkSteps steps;
    private int generation;private boolean active,delivered;private State state=State.WAITING;
    WalkSession(Port port,Effects effects,String run,int unit,int target,int earned){this.port=port;this.effects=effects;this.run=run;this.unit=unit;steps=new WalkSteps(target,earned);}
    State state(){return state;}int earned(){return steps.earned();}
    void resume(){close();int ticket=++generation;state=State.WAITING;if(!effects.current(run,unit))return;try{if(!port.allowed()){state=State.PERMISSION;return;}WalkSteps.Mode mode=port.mode();if(mode==WalkSteps.Mode.NONE){state=State.UNSUPPORTED;return;}if(steps.done()){state=State.DONE;if(!delivered){delivered=true;effects.solved(run,unit);}return;}steps.arm(mode,port.now());active=true;state=State.TRACKING;boolean listening=port.begin((time,value)->{if(!active||ticket!=generation||state!=State.TRACKING||!effects.current(run,unit))return;if(steps.sample(time,value)){effects.progress(run,unit,steps.earned());if(steps.done()){state=State.DONE;active=false;port.close();delivered=true;effects.solved(run,unit);}}});if(!listening&&state==State.TRACKING){active=false;port.close();state=State.ERROR;}}catch(SecurityException denied){active=false;port.close();state=State.PERMISSION;}catch(RuntimeException unavailable){active=false;port.close();state=State.ERROR;}}
    @Override public void close(){active=false;generation++;port.close();state=State.CLOSED;}
}
