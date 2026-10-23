package org.sleepwell.app;

import java.util.*;
import java.util.function.LongSupplier;

/** Main-loop orchestration, with replaceable clock/outputs for silent lifecycle tests. */
final class AlarmPlayback {
    interface Ready {void ready();void failed();}
    interface Layer {void prepare(Ready callback);void gain(float gain);void start(Ready activated);void close();}
    interface Sink {Layer create(String tone);void volume(Layer layer,AlarmWakePolicy policy,boolean backup);void stopped();default void failed(){}}
    private final AlarmWakePolicy policy;private final Sink sink;private final LongSupplier elapsed,clock;
    private final Set<Layer> allocated=new LinkedHashSet<>();private final String tone;
    private Layer current,pending;private boolean ready,activating,closed,backup,fallback,started;private long loadingAt;private int generation;
    AlarmPlayback(Alarm alarm,Sink sink,LongSupplier elapsed,LongSupplier clock){policy=new AlarmWakePolicy(alarm);tone=alarm.tone;this.sink=sink;this.elapsed=elapsed;this.clock=clock;}
    void start(){if(started||closed)return;started=true;load(tone,false);}
    private void load(String source,boolean replacement){
        int request=++generation;ready=false;activating=false;loadingAt=clock.getAsLong();
        try{Layer layer=sink.create(source);allocated.add(layer);pending=layer;layer.prepare(new Ready(){
            public void ready(){if(closed||request!=generation){safeClose(layer);return;}ready=true;tick();}
            public void failed(){if(closed){safeClose(layer);return;}if(layer==current){safeClose(layer);current=null;if(pending!=null&&pending!=layer)return;failure(layer,false);return;}if(request==generation)failure(layer,replacement);else safeClose(layer);}
        });}catch(RuntimeException error){failure(pending,replacement);}
    }
    private void failure(Layer failed,boolean replacement){
        generation++;if(failed!=null)safeClose(failed);if(current==failed)current=null;pending=null;ready=false;activating=false;
        if(replacement&&current!=null){try{sink.volume(current,policy,backup);}catch(RuntimeException ignored){}apply();return;}
        if(!fallback){fallback=true;load("daybreak",false);}else sink.failed();
    }
    void tick(){
        if(closed)return;
        if(pending!=null&&(!ready||activating)&&clock.getAsLong()-loadingAt>=4000){failure(pending,current!=null);return;}
        if(current!=null)apply();
        if(pending==null||!ready||!policy.audible(elapsed.getAsLong(),backup))return;
        if(activating){try{pending.gain((float)policy.gain(elapsed.getAsLong(),backup));}catch(RuntimeException error){failure(pending,current!=null);}return;}
        Layer next=pending;Layer previous=current;
        try{
            next.gain((float)policy.gain(elapsed.getAsLong(),backup));
            sink.volume(next,policy,backup);
            activating=true;loadingAt=clock.getAsLong();int request=generation;
            next.start(new Ready(){public void ready(){if(closed||request!=generation||pending!=next){safeClose(next);return;}current=next;pending=null;activating=false;if(previous!=null&&previous!=next)safeClose(previous);}public void failed(){if(!closed&&request==generation)failure(next,previous!=null);else safeClose(next);}});
        }catch(RuntimeException error){failure(next,previous!=null);}
    }
    private void apply(){try{current.gain((float)policy.gain(elapsed.getAsLong(),backup));}catch(RuntimeException error){failure(current,false);}}
    void backup(){if(closed||backup||policy.quiet)return;backup=true;if(pending!=null){safeClose(pending);pending=null;}load("pulse",current!=null);}
    void stop(){if(closed)return;closed=true;generation++;for(Layer layer:allocated)safeClose(layer);current=pending=null;try{sink.stopped();}catch(RuntimeException ignored){}}
    private void safeClose(Layer layer){try{layer.close();}catch(RuntimeException ignored){}}
}
