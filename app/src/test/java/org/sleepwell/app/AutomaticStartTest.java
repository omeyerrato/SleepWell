package org.sleepwell.app;

import org.junit.Test;
import java.time.*;
import java.util.*;
import static org.junit.Assert.*;

public class AutomaticStartTest {
    private final ZoneId zone=ZoneId.of("Asia/Riyadh");
    private long at(String date){return LocalDateTime.parse(date).atZone(zone).toInstant().toEpochMilli();}
    @Test public void bedtimeUsesTheSavedLocalScheduleAndRollsTomorrow(){assertEquals(at("2026-10-23T22:30"),AutomaticStart.schedule(AutomaticSleep.Mode.BEDTIME,at("2026-10-23T21:00"),zone,1350,List.of()));assertEquals(at("2026-10-24T22:30"),AutomaticStart.schedule(AutomaticSleep.Mode.BEDTIME,at("2026-10-23T23:00"),zone,1350,List.of()));}
    @Test public void stillnessStartsLookingNow(){long now=at("2026-10-23T21:00");assertEquals(now,AutomaticStart.schedule(AutomaticSleep.Mode.STILLNESS,now,zone,1350,List.of()));}
    @Test public void smartWindowNeedsARealEnabledAlarm(){try{AutomaticStart.schedule(AutomaticSleep.Mode.SMART_WINDOW,0,zone,1350,List.of());fail();}catch(IllegalArgumentException expected){}Alarm disabled=new Alarm(1);disabled.enabled=false;disabled.smartWindow=30;try{AutomaticStart.schedule(AutomaticSleep.Mode.SMART_WINDOW,0,zone,1350,List.of(disabled));fail();}catch(IllegalArgumentException expected){}}
}
