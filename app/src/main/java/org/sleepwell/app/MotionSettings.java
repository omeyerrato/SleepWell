package org.sleepwell.app;

import android.content.*;
import java.util.*;

/** Reading never saves defaults; each recording pins this profile when its service starts. */
final class MotionSettings {
    static final String CUSTOM="motionCalibration";
    interface Factory{SharedPreferences open(Context context);}
    static Factory factory=Prefs::of;
    static final class Version{final Object level,custom;Version(SharedPreferences prefs){Map<String,?> values=prefs.getAll();level=values.get("motionSensitivity");custom=values.get(CUSTOM);}boolean same(Version other){return Objects.equals(level,other.level)&&Objects.equals(custom,other.custom);}}
    static MotionProfile read(SharedPreferences prefs){try{MotionProfile manual=new MotionProfile(prefs.getInt("motionSensitivity",1));if(!prefs.contains(CUSTOM))return manual;String raw=prefs.getString(CUSTOM,null);return new MotionProfile(MotionCalibration.read(raw));}catch(Exception error){throw new IllegalStateException("Saved motion sensitivity could not be read. Reset surface calibration or use manual sensitivity. Existing data was kept.",error);}}
    static MotionProfile load(Context c){return read(factory.open(c));}
    static synchronized void save(SharedPreferences prefs,int level,int expected){new MotionProfile(level);if(read(prefs).level!=expected)throw new IllegalStateException("Motion settings changed elsewhere. Reopen before saving.");writeManual(prefs,level);}
    static synchronized void save(SharedPreferences prefs,int level,Version expected){new MotionProfile(level);check(prefs,expected);writeManual(prefs,level);}
    private static void writeManual(SharedPreferences prefs,int level){if(!prefs.edit().putInt("motionSensitivity",level).remove(CUSTOM).commit())throw new IllegalStateException("Motion settings could not be saved. Try again.");}
    static synchronized void saveCalibration(SharedPreferences prefs,MotionCalibration value,Version expected){MotionCalibration.read(value.config());check(prefs,expected);if(!prefs.edit().putInt("motionSensitivity",1).putString(CUSTOM,value.config()).commit())throw new IllegalStateException("Surface calibration could not be saved. Try again.");}
    static synchronized void reset(SharedPreferences prefs,Version expected){check(prefs,expected);Object level=expected.level;int manual=level instanceof Integer&&(Integer)level>=0&&(Integer)level<=2?(Integer)level:1;if(!prefs.edit().putInt("motionSensitivity",manual).remove(CUSTOM).commit())throw new IllegalStateException("Surface calibration could not be reset. Try again.");}
    private static void check(SharedPreferences prefs,Version expected){if(expected==null||!expected.same(new Version(prefs)))throw new IllegalStateException("Motion settings changed elsewhere. Reopen before saving.");}
}
