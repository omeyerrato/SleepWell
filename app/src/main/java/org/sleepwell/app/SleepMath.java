package org.sleepwell.app;

import java.time.*;
import java.util.*;

/** Pure, testable decisions. Movement is an activity proxy, never an EEG sleep stage. */
public final class SleepMath {
    private SleepMath() {}
    public static long nextOccurrence(int hour, int minute, Set<Integer> weekdays, long now, ZoneId zone) {
        ZonedDateTime local = Instant.ofEpochMilli(now).atZone(zone);
        for (int i = 0; i < 9; i++) {
            LocalDate date = local.toLocalDate().plusDays(i);
            ZonedDateTime candidate = date.atTime(hour, minute).atZone(zone);
            if ((weekdays.isEmpty() || weekdays.contains(date.getDayOfWeek().getValue())) && candidate.toInstant().toEpochMilli() > now)
                return candidate.toInstant().toEpochMilli();
        }
        throw new IllegalArgumentException("No valid weekday");
    }
    public static boolean inBedtime(int minute, int bedtime, int wake) {
        if (bedtime == wake) return false;
        return bedtime < wake ? minute >= bedtime && minute < wake : minute >= bedtime || minute < wake;
    }
    public static boolean smartWake(long now, long deadline, int windowMinutes, double currentMovement, int recentActiveMinutes, int samples) {
        return windowMinutes > 0 && now >= deadline - windowMinutes * 60000L && now < deadline
            && samples >= 20 && currentMovement >= 0.12 && recentActiveMinutes >= 2;
    }
    public static double rms(short[] pcm, int count) {
        if (count <= 0) return 0;
        double sum = 0; for (int i = 0; i < count; i++) sum += (double) pcm[i] * pcm[i];
        return Math.sqrt(sum / count) / 32768.0;
    }
    public static int durationScore(long minutes, int goalMinutes) {
        return (int) Math.round(Math.max(0, Math.min(100, 100.0 * minutes / Math.max(1, goalMinutes))));
    }
    public static String duration(long millis) {
        long minutes = Math.max(0, millis / 60000); return minutes / 60 + "h " + minutes % 60 + "m";
    }
    public static String csv(String s) { return "\"" + s.replace("\"", "\"\"") + "\""; }
}
