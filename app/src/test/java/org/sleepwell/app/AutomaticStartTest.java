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
    private Alarm smart(int id,int hour,int minute,int window){Alarm alarm=new Alarm(id);alarm.enabled=true;alarm.hour=hour;alarm.minute=minute;alarm.smartWindow=window;return alarm;}
    @Test public void smartLeadInIsFortyFiveMinutesBeforeTheWindow(){Alarm alarm=smart(1,6,30,30);assertEquals(at("2026-10-23T05:15"),AutomaticStart.schedule(AutomaticSleep.Mode.SMART_WINDOW,at("2026-10-23T04:00"),zone,1350,List.of(alarm)));}
    @Test public void alreadyOpenLeadInStartsNowInsteadOfWaitingTomorrow(){Alarm alarm=smart(1,6,30,30);long now=at("2026-10-23T05:45");assertEquals(now,AutomaticStart.schedule(AutomaticSleep.Mode.SMART_WINDOW,now,zone,1350,List.of(alarm)));}
    @Test public void passedAlarmUsesItsNextCalendarOccurrence(){Alarm alarm=smart(1,6,30,30);assertEquals(at("2026-10-24T05:15"),AutomaticStart.schedule(AutomaticSleep.Mode.SMART_WINDOW,at("2026-10-23T07:00"),zone,1350,List.of(alarm)));}
    @Test public void weeklyAlarmHonorsItsSelectedDays(){Alarm alarm=smart(1,6,30,30);alarm.days.add(6);assertEquals(at("2026-10-24T05:15"),AutomaticStart.schedule(AutomaticSleep.Mode.SMART_WINDOW,at("2026-10-23T04:00"),zone,1350,List.of(alarm)));}
    @Test public void earliestEnabledSmartWindowWins(){Alarm later=smart(1,8,30,30),earlier=smart(2,6,30,20),disabled=smart(3,5,0,30),fixed=smart(4,4,30,0);disabled.enabled=false;assertEquals(at("2026-10-23T05:25"),AutomaticStart.schedule(AutomaticSleep.Mode.SMART_WINDOW,at("2026-10-23T03:00"),zone,1350,List.of(later,disabled,fixed,earlier)));}
}
