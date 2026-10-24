package org.sleepwell.app;

import android.content.SharedPreferences;

/** Two private keys are committed together; no alarm/routine schema or sensor preference changes. */
final class WalkFallbackSaved {
    static boolean chosen(SharedPreferences prefs,String run,int unit){try{return WalkFallback.chosen(prefs.getString("walkFallbackRun",""),prefs.getInt("walkFallbackUnit",-1),run,unit);}catch(ClassCastException corrupt){return false;}}
    static boolean save(SharedPreferences prefs,String run,int unit){if(!WalkFallback.valid(run,unit))throw new IllegalArgumentException("Invalid walking task owner");return prefs.edit().putString("walkFallbackRun",run).putInt("walkFallbackUnit",unit).commit();}
}
