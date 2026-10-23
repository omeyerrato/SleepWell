package org.sleepwell.app;

import org.junit.Test;
import static org.junit.Assert.*;

public class TravelReminderPolicyTest {
    private TravelReminderPolicy.Decision due(boolean enabled,long revision,long at,long incomingRevision,long incomingAt,long delivered,long now){return TravelReminderPolicy.due(enabled,revision,at,incomingRevision,incomingAt,delivered,now);}
    @Test public void currentDueOccurrenceIsDeliverable(){assertEquals(TravelReminderPolicy.Decision.DELIVER,due(true,7,1000,7,1000,0,1000));}
    @Test public void earlyCallbackCannotDeliver(){assertEquals(TravelReminderPolicy.Decision.IGNORE,due(true,7,1000,7,1000,0,999));}
    @Test public void cancelledAndEditedPlansRejectOldCallbacks(){assertEquals(TravelReminderPolicy.Decision.IGNORE,due(false,7,1000,7,1000,0,1000));assertEquals(TravelReminderPolicy.Decision.IGNORE,due(true,8,1000,7,1000,0,1000));}
    @Test public void duplicateAndWrongOccurrencesCannotRepost(){assertEquals(TravelReminderPolicy.Decision.IGNORE,due(true,7,1000,7,1000,1000,1001));assertEquals(TravelReminderPolicy.Decision.IGNORE,due(true,7,2000,7,1000,0,2000));}
    @Test public void staleDaylightCuesAreSkippedRatherThanShownHoursLate(){assertEquals(TravelReminderPolicy.Decision.SKIP_LATE,due(true,7,1000,7,1000,0,1001+TravelReminderPolicy.MAX_LATE));}
    @Test public void normalInexactDelayIsToleratedWithinItsBound(){assertEquals(TravelReminderPolicy.Decision.DELIVER,due(true,7,1000,7,1000,0,1000+TravelReminderPolicy.MAX_LATE));}
}
