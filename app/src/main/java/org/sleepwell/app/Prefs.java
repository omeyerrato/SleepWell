package org.sleepwell.app;

import android.content.*;

public final class Prefs {
    private Prefs() {}
    public static SharedPreferences of(Context c) { return c.getSharedPreferences("sleepwell", Context.MODE_PRIVATE); }
    public static int number(Context c, String k, int fallback) { return of(c).getInt(k, fallback); }
    public static boolean flag(Context c, String k) { return of(c).getBoolean(k, false); }
    public static String text(Context c, String k, String fallback) { return of(c).getString(k, fallback); }
    public static void put(Context c, String k, Object value) {
        SharedPreferences.Editor e = of(c).edit();
        if (value instanceof Boolean) e.putBoolean(k, (Boolean) value);
        else if (value instanceof Integer) e.putInt(k, (Integer) value);
        else if (value instanceof Long) e.putLong(k, (Long) value);
        else e.putString(k, String.valueOf(value));
        e.apply();
    }
}
