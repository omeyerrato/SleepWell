package org.sleepwell.app;

import java.util.function.BooleanSupplier;

final class AutomationDispatch {
    /** Sends at most one due event. Transport success never implies physical actuator success. */
    static boolean next(AutomationStore store,AutomationDelivery.Transport transport,long now,java.util.function.LongSupplier clock,BooleanSupplier cancelled){
        AutomationStore.Pending pending=store.next(now);if(pending==null)return false;
        if(cancelled.getAsBoolean())return false;
        if(!store.current(pending)){store.result(pending,"Rule changed; cancelled",false,now);return true;}
        if(now<pending.created||now-pending.created>=AutomationDelivery.MAX_AGE){store.result(pending,"Event expired",false,now);return true;}
        try{AutomationRule rule=store.rule(pending.rule);if(rule==null||cancelled.getAsBoolean()||!store.current(pending))return false;transport.send(rule,pending.event);if(!cancelled.getAsBoolean())store.result(pending,rule.transport==AutomationRule.Transport.INTENT?"Broadcast sent; receiver confirmation unavailable":"Delivered",false,clock.getAsLong());}
        catch(AutomationDelivery.Failure failure){if(!cancelled.getAsBoolean())store.result(pending,failure.getMessage(),failure.retry,clock.getAsLong());}
        catch(Exception failure){if(!cancelled.getAsBoolean())store.result(pending,"Saved destination unavailable; reopen rule",false,clock.getAsLong());}
        return true;
    }
}
