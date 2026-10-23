package org.sleepwell.app;

import java.time.*;

/** One stable boundary instant: earlier offset on a fold, forward by the gap on a skip. */
final class SleepDay {
    static LocalDate label(long instant,ZoneId zone,int cutoff){
        ZonedDateTime local=Instant.ofEpochMilli(instant).atZone(zone);LocalDate date=local.toLocalDate();
        long boundary=date.atTime(cutoff/60,cutoff%60).atZone(zone).toInstant().toEpochMilli();
        return instant<boundary?date.minusDays(1):date;
    }
}
