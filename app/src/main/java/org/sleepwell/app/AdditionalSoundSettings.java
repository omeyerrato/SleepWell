package org.sleepwell.app;

import android.content.Context;

/** Device-local future-night consent only; never changes microphone, base labels or cue settings. */
final class AdditionalSoundSettings {
    static final String KEY = "additionalSoundAnalysis";
    static boolean requested(Context context) { return Prefs.flag(context, KEY); }
    static void save(Context context, boolean enabled) {
        if (!Prefs.of(context).edit().putBoolean(KEY, enabled).commit())
            throw new IllegalStateException("Additional sound settings could not be saved.");
    }
    private AdditionalSoundSettings() {}
}
