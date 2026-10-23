package org.sleepwell.app;

import java.time.*;
import java.time.temporal.WeekFields;
import java.util.*;

/** Locale calendar geometry only; browsing a month never changes the selected date. */
final class CalendarMonth {
    static DayOfWeek first(Locale locale){return WeekFields.of(locale).getFirstDayOfWeek();}
    static List<DayOfWeek> weekdays(Locale locale){List<DayOfWeek> days=new ArrayList<>();DayOfWeek first=first(locale);for(int n=0;n<7;n++)days.add(first.plus(n));return Collections.unmodifiableList(days);}
    static List<LocalDate> cells(YearMonth month,Locale locale){int blank=Math.floorMod(month.atDay(1).getDayOfWeek().getValue()-first(locale).getValue(),7);int count=((blank+month.lengthOfMonth()+6)/7)*7;List<LocalDate> cells=new ArrayList<>();for(int n=0;n<count;n++){int day=n-blank+1;cells.add(day<1||day>month.lengthOfMonth()?null:month.atDay(day));}return Collections.unmodifiableList(cells);}
}
