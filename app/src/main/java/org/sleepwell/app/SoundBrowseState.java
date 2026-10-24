package org.sleepwell.app;
import android.content.*;
/** Optional browsing memory in its own namespace. It contains no sleep records or audio settings. */
final class SoundBrowseState {
 interface Factory {SharedPreferences open(Context context);}
 static Factory factory=context->context.getSharedPreferences("sound_browsing_v1",Context.MODE_PRIVATE);
 static String read(SharedPreferences prefs,boolean album){try{return SoundBrowseFilter.filter(prefs.getString(album?"albumCategory":"nightCategory","all"),!album);}catch(ClassCastException malformed){return "all";}}
 static int days(SharedPreferences prefs){try{int value=prefs.getInt("albumDays",0);return value==7||value==30?value:0;}catch(ClassCastException malformed){return 0;}}
 static void save(SharedPreferences prefs,boolean album,String category,int days){SharedPreferences.Editor edit=prefs.edit().putString(album?"albumCategory":"nightCategory",SoundBrowseFilter.filter(category,!album));if(album)edit.putInt("albumDays",days==7||days==30?days:0);edit.apply();}
 private SoundBrowseState(){}
}
