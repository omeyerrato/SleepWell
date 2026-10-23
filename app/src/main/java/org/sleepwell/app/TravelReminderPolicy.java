package org.sleepwell.app;

/** Pending-generation fence and bounded lateness. Never delivers a daylight prompt hours late. */
final class TravelReminderPolicy {
    static final long MAX_LATE=60*60*1000L;
    enum Decision{IGNORE,DELIVER,SKIP_LATE}
    static Decision due(boolean enabled,long revision,long expectedAt,long incomingRevision,long incomingAt,long delivered,long now){
        if(!enabled||expectedAt<=0||incomingRevision!=revision||incomingAt!=expectedAt||incomingAt<=delivered||now<incomingAt)return Decision.IGNORE;
        return now-incomingAt>MAX_LATE?Decision.SKIP_LATE:Decision.DELIVER;
    }
}
