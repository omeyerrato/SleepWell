package org.sleepwell.app;

import android.content.*;

/** Reading never saves defaults; each recording pins this profile when its service starts. */
final class MotionSettings {
    interface Factory{SharedPreferences open(Context context);}
    static Factory factory=Prefs::of;
    static MotionProfile read(SharedPreferences prefs){try{return new MotionProfile(prefs.getInt("motionSensitivity",1));}catch(Exception error){throw new IllegalStateException("Saved motion sensitivity could not be read. Existing data was kept.",error);}}
    static MotionProfile load(Context c){return read(factory.open(c));}
    static synchronized void save(SharedPreferences prefs,int level,int expected){new MotionProfile(level);if(read(prefs).level!=expected)throw new IllegalStateException("Motion settings changed elsewhere. Reopen before saving.");if(!prefs.edit().putInt("motionSensitivity",level).commit())throw new IllegalStateException("Motion settings could not be saved. Try again.");}
}
