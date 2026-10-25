package org.sleepwell.app;
import java.time.*;import java.util.*;import org.json.*;
/** Explicit local dates chosen by the user, not a public-holiday feed. */
final class AlarmExcludedDates {
 static final int LIMIT=64;
 static SortedSet<LocalDate> read(Object raw)throws JSONException{if(!(raw instanceof JSONArray))throw new JSONException("Skipped dates must be an array");JSONArray rows=(JSONArray)raw;if(rows.length()>LIMIT)throw new JSONException("Use at most 64 skipped dates");SortedSet<LocalDate> result=new TreeSet<>();for(int n=0;n<rows.length();n++){LocalDate date;try{date=AlarmDate.read(rows.get(n));}catch(IllegalArgumentException invalid){throw new JSONException("Invalid skipped date");}if(!result.add(date))throw new JSONException("Skipped dates must be unique");}return Collections.unmodifiableSortedSet(result);}
 static JSONArray encode(Collection<LocalDate> dates){if(dates.size()>LIMIT)throw new IllegalArgumentException("Use at most 64 skipped dates.");JSONArray rows=new JSONArray();for(LocalDate date:new TreeSet<>(dates))rows.put(date.toString());return rows;}
 static boolean excludes(Alarm alarm,long at,ZoneId zone){return alarm.excludedDates.contains(Instant.ofEpochMilli(at).atZone(zone).toLocalDate());}
 static long next(Alarm alarm,long now,ZoneId zone){if(alarm.excludedDatesInvalid)return 0;if(alarm.excludedDates.isEmpty())return alarm.nextBase(now,zone);long after=now;for(int n=0;n<=LIMIT+1;n++){long candidate=alarm.nextBase(after,zone);if(candidate<=after)return 0;if(!excludes(alarm,candidate,zone))return candidate;if(alarm.date!=null)return 0;after=candidate;}throw new IllegalArgumentException("No allowed alarm date could be projected.");}
 static String key(Alarm alarm){return alarm.excludedDatesInvalid?":invalid-excluded:"+alarm.json().opt("excludedDates"):alarm.excludedDates.isEmpty()?"":":excluded:"+encode(alarm.excludedDates);}
 static String summary(Alarm alarm){return alarm.excludedDatesInvalid?"Review skipped dates":alarm.excludedDates.isEmpty()?"None selected":alarm.excludedDates.size()+" selected";}
 private AlarmExcludedDates(){}
}
