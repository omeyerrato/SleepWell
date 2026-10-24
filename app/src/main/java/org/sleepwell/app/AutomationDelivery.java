package org.sleepwell.app;

/** One bounded attempt. Repository owns durable retries and stale-revision fencing. */
final class AutomationDelivery {
    static final int MAX_ATTEMPTS=6;static final long MAX_AGE=24*3600000L;
    interface Transport { void send(AutomationRule rule,AutomationEvent event)throws Failure; }
    static final class Failure extends Exception {final boolean retry;Failure(String safeMessage,boolean retry){super(safeMessage);this.retry=retry;}}
    static long retryDelay(int attempt){return switch(attempt){case 1->60000;case 2->300000;case 3->900000;case 4->3600000;default->10800000;};}
    static boolean retry(boolean transientFailure,int attempts,long created,long now){return transientFailure&&attempts<MAX_ATTEMPTS&&now-created<MAX_AGE&&now>=created;}
}
