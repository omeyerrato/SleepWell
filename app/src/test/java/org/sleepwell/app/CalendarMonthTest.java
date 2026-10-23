package org.sleepwell.app;
import org.junit.Test;
import java.time.*;
import java.util.*;
import static org.junit.Assert.*;
public class CalendarMonthTest {
    @Test public void weekdaysFollowLocaleRatherThanHardcodedMonday(){assertEquals(DayOfWeek.SUNDAY,CalendarMonth.weekdays(Locale.US).get(0));assertEquals(DayOfWeek.MONDAY,CalendarMonth.weekdays(Locale.UK).get(0));assertEquals(7,new HashSet<>(CalendarMonth.weekdays(Locale.UK)).size());}
    @Test public void leapMonthHasEveryRealDayExactlyOnce(){List<LocalDate> cells=CalendarMonth.cells(YearMonth.of(2024,2),Locale.UK);assertEquals(35,cells.size());assertNull(cells.get(0));assertEquals(LocalDate.of(2024,2,1),cells.get(3));assertEquals(29,cells.stream().filter(Objects::nonNull).count());assertTrue(cells.contains(LocalDate.of(2024,2,29)));}
    @Test public void sixWeekMonthKeepsLastDays(){List<LocalDate> us=CalendarMonth.cells(YearMonth.of(2026,8),Locale.US);assertEquals(42,us.size());assertEquals(LocalDate.of(2026,8,1),us.get(6));assertEquals(LocalDate.of(2026,8,31),us.get(36));assertNull(us.get(41));}
    @Test public void cellListsCannotMutateSelectionOrMonth(){YearMonth month=YearMonth.of(2026,10);List<LocalDate> dates=CalendarMonth.cells(month,Locale.US);try{dates.set(0,LocalDate.now());fail();}catch(UnsupportedOperationException expected){}assertEquals(YearMonth.of(2026,10),month);assertEquals(31,dates.stream().filter(Objects::nonNull).count());}
}
