package org.sleepwell.app;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** Serial-worker-only lamp ownership. Phone alarm never waits for this state machine. */
final class SmartLightRun {
    static final class State {final boolean on,dimmable;final int brightness;State(boolean on,int brightness,boolean dimmable){this.on=on;this.brightness=brightness;this.dimmable=dimmable;}}
    interface Port {State read()throws Exception;void set(boolean on,int brightness)throws Exception;}
    private final SmartLightConfig config;private final Port port;private final BooleanSupplier alive;private final Consumer<String> status;
    private State original;private int last;private boolean touched,closed,stopped;private long lastTick=Long.MIN_VALUE;
    SmartLightRun(SmartLightConfig config,Port port,BooleanSupplier alive,Consumer<String> status){this.config=config;this.port=port;this.alive=alive;this.status=status;}
    void tick(long elapsed){if(closed||stopped)return;if(!alive.getAsBoolean()){close();return;}if(lastTick!=Long.MIN_VALUE&&elapsed-lastTick<5000)return;lastTick=elapsed;
        try{if(original==null){config.validate();original=port.read();if(!original.dimmable)throw new IllegalStateException("This entity does not support brightness.");last=original.on?original.brightness:0;if(original.on&&original.brightness>=config.maximum*255d/100){stopped=true;status.accept("Lamp already above target; unchanged");return;}}
            int next=Math.max(original.on?original.brightness:1,config.brightness(elapsed));if(touched&&next==last)return;State current=port.read();if(!matches(current,touched?last:original.on?original.brightness:0)){stopped=true;touched=false;status.accept("Lamp changed elsewhere; sunrise stopped");return;}if(!alive.getAsBoolean()){close();return;}last=next;touched=true;port.set(true,next);status.accept(next>=config.brightness(Long.MAX_VALUE)?"Sunrise maximum reached":"Bedroom sunrise active");
        }catch(Exception failure){stopped=true;status.accept("Bedroom light unavailable; phone alarm continues");close();}}
    private boolean matches(State state,int expected){return expected==0?!state.on:state.on&&Math.abs(state.brightness-expected)<=4;}
    void close(){if(closed)return;closed=true;if(original==null||!touched||!config.restore)return;try{State current=port.read();if(matches(current,last)){port.set(original.on,original.on?original.brightness:0);status.accept("Bedroom light restored");}else status.accept("Lamp changed elsewhere; left unchanged");}catch(Exception failure){status.accept("Bedroom light could not be restored; check the lamp");}}
}
