package org.sleepwell.app;

/** Per service lifetime; pause/resume does not create duplicate start/end events. */
final class AutomationTracking {
    private final java.util.function.BiConsumer<AutomationEvent.Kind,String> emit;private String occurrence;private boolean ended;
    AutomationTracking(java.util.function.BiConsumer<AutomationEvent.Kind,String> emit){this.emit=emit;}
    synchronized void started(long record,long start){if(occurrence!=null||record<=0||start<=0)return;occurrence=record+":"+start;emit.accept(AutomationEvent.Kind.TRACKING_STARTED,occurrence);}
    synchronized void stopped(){if(occurrence==null||ended)return;ended=true;emit.accept(AutomationEvent.Kind.TRACKING_STOPPED,occurrence);}
}
