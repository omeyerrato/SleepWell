package org.sleepwell.app;

import android.content.*;

/** Small independent preference scope; opening the screen never writes defaults. */
final class RhythmSettings {
    interface Factory {SharedPreferences open(Context context);}
    static Factory factory=Prefs::of;
    static int read(SharedPreferences prefs){int value=prefs.getInt("rhythmWorkdays",31);if(value<0||value>127)throw new IllegalStateException("Your workday schedule could not be read. Existing data was kept.");return value;}
    static synchronized void save(SharedPreferences prefs,int mask,int expected){if(mask<0||mask>127)throw new IllegalArgumentException("Choose valid workdays.");if(read(prefs)!=expected)throw new IllegalStateException("Your workdays changed elsewhere. Reopen this page before saving.");if(!prefs.edit().putInt("rhythmWorkdays",mask).commit())throw new IllegalStateException("Workdays could not be saved. Try again.");}
}
