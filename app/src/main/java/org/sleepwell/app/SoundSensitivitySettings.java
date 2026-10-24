package org.sleepwell.app;
import android.content.*;
/** Separate namespace. Reading/saving never grants consent or constructs a model/audio/sensor owner. */
final class SoundSensitivitySettings {
 static final String KEY="profile";
 static final class Read {final SoundSensitivity policy;final String error;Read(SoundSensitivity policy,String error){if(policy==null||error==null)throw new IllegalArgumentException("Missing sound duration settings");this.policy=policy;this.error=error;}}
 interface Port {Read read();void save(SoundSensitivity policy);}
 interface Factory {Port open(Context context);}
 static Factory factory=context->of(context.getSharedPreferences("sound_detection_v1",Context.MODE_PRIVATE));
 static Port of(SharedPreferences prefs){return new Port(){public Read read(){return SoundSensitivitySettings.read(prefs);}public void save(SoundSensitivity policy){if(!prefs.edit().putString(KEY,SoundSensitivityJson.encode(policy)).commit())throw new IllegalStateException("Sound sensitivity could not be saved. Try again.");}};}
 static Read read(SharedPreferences prefs){try{if(!prefs.contains(KEY))return new Read(SoundSensitivity.DEFAULT,"");return new Read(SoundSensitivityJson.decode(prefs.getString(KEY,null)),"");}catch(IllegalArgumentException|ClassCastException error){return new Read(SoundSensitivity.DEFAULT,"Saved settings are invalid. Defaults are being used.");}}
 static SoundSensitivity snapshot(Context context){try{return factory.open(context.getApplicationContext()).read().policy;}catch(RuntimeException unavailable){return SoundSensitivity.DEFAULT;}}
}
