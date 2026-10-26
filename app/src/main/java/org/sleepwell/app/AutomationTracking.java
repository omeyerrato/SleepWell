package org.sleepwell.app;

/** Capture events use the saved night and committed pause-row identities, never action requests. */
final class AutomationTracking {
    private final java.util.function.BiConsumer<AutomationEvent.Kind,String> emit;private String occurrence;private boolean ended;private long lastPaused,lastResumed;
    AutomationTracking(java.util.function.BiConsumer<AutomationEvent.Kind,String> emit){this.emit=emit;}
    synchronized void started(long record,long start){if(occurrence!=null||record<=0||start<=0)return;occurrence=record+":"+start;emit.accept(AutomationEvent.Kind.TRACKING_STARTED,occurrence);}
    synchronized void paused(long pause,long at){if(occurrence==null||ended||pause<=0||at<=0||pause==lastPaused)return;lastPaused=pause;emit.accept(AutomationEvent.Kind.TRACKING_PAUSED,occurrence+":pause:"+pause);}
    synchronized void resumed(long pause,long at){if(occurrence==null||ended||pause<=0||at<=0||pause==lastResumed)return;lastResumed=pause;emit.accept(AutomationEvent.Kind.TRACKING_RESUMED,occurrence+":pause:"+pause);}
    synchronized void stopped(){if(occurrence==null||ended)return;ended=true;emit.accept(AutomationEvent.Kind.TRACKING_STOPPED,occurrence);}
}
