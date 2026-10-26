package org.sleepwell.app;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Decoder acknowledgements for one playback/resume segment. No polling or UI events. */
final class AutomationPlayback {
    interface Events {void emit(AutomationEvent.Kind kind,String occurrence,long at);}
    static final class Segment {
        final String occurrence=UUID.randomUUID().toString();
        final Map<Object,Integer> pending=new IdentityHashMap<>();
        final Set<Object> playing=Collections.newSetFromMap(new IdentityHashMap<>());
        boolean started,closing,stopped;
    }
    private final Events events;
    AutomationPlayback(Events events){this.events=events;}
    Segment begin(){return new Segment();}
    synchronized void starting(Segment owner,Object layer){if(owner!=null&&!owner.stopped)owner.pending.put(layer,owner.pending.getOrDefault(layer,0)+1);}
    synchronized void settled(Segment owner,Object layer){if(owner==null)return;int count=owner.pending.getOrDefault(layer,0);if(count<=1)owner.pending.remove(layer);else owner.pending.put(layer,count-1);finish(owner);}
    synchronized void started(Segment owner,Object layer){
        if(owner==null||owner.stopped)return;
        owner.playing.add(layer);
        if(!owner.started&&!owner.closing){owner.started=true;events.emit(AutomationEvent.Kind.SLEEP_SOUND_STARTED,owner.occurrence,System.currentTimeMillis());}
    }
    synchronized void quiet(Segment owner,Object layer){if(owner==null)return;owner.playing.remove(layer);finish(owner);}
    synchronized void closing(Segment owner){if(owner==null)return;owner.closing=true;finish(owner);}
    private void finish(Segment owner){if(owner.started&&owner.closing&&!owner.stopped&&owner.playing.isEmpty()&&owner.pending.isEmpty()){owner.stopped=true;events.emit(AutomationEvent.Kind.SLEEP_SOUND_STOPPED,owner.occurrence,System.currentTimeMillis());}}
}
