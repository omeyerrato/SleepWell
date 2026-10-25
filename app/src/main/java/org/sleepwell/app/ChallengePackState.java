package org.sleepwell.app;

import android.content.SharedPreferences;

/** Small one-unit checkpoint; a stale controller cannot overwrite its successor's progress. */
final class ChallengePackState {
    static String key(String run,int unit,int round){CountingPuzzle.seed(run,unit,round,"owner");return run+":"+unit+":"+round;}
    static TiltMaze maze(SharedPreferences prefs,String run,int unit,int round){String key=key(run,unit,round);TiltMaze maze=new TiltMaze(run,unit,round);if(key.equals(prefs.getString("tiltOwner","")))maze.restore(Double.longBitsToDouble(prefs.getLong("tiltX",0)),Double.longBitsToDouble(prefs.getLong("tiltY",0)));else if(!prefs.edit().putString("tiltOwner",key).putLong("tiltX",Double.doubleToLongBits(maze.x)).putLong("tiltY",Double.doubleToLongBits(maze.y)).putBoolean("tiltMath",false).commit())throw new IllegalStateException("Maze could not be saved");return maze;}
    static boolean save(SharedPreferences prefs,String key,TiltMaze maze){return key.equals(prefs.getString("tiltOwner",""))&&prefs.edit().putLong("tiltX",Double.doubleToLongBits(maze.x)).putLong("tiltY",Double.doubleToLongBits(maze.y)).commit();}
    static boolean fallback(SharedPreferences prefs,String run,int unit,int round){return key(run,unit,round).equals(prefs.getString("tiltOwner",""))&&prefs.getBoolean("tiltMath",false);}
    static boolean chooseFallback(SharedPreferences prefs,String key){return key.equals(prefs.getString("tiltOwner",""))&&prefs.edit().putBoolean("tiltMath",true).commit();}
}
