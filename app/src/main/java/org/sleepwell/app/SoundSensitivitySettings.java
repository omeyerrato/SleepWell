package org.sleepwell.app;
import android.content.*;
/** Separate namespace. Reading/saving never grants consent or constructs a model/audio/sensor owner. */
final class SoundSensitivitySettings {
 static final String KEY="profile";
 static final class Read {final SoundSensitivity policy;final String error;Read(SoundSensitivity policy,String error){if(policy==null||error==null)throw new IllegalArgumentException("Missing sound duration settings");this.policy=policy;this.error=error;}}
 interface Port {Read read();void save(SoundSensitivity policy);}
 interface Factory {Port open(Context context);}
 static Factory factory=context->of(context.getSharedPreferences("sound_detection_v1",Context.MODE_PRIVATE));
 static Port editor(Context context){Context app=context.getApplicationContext();Factory pinned=factory;try{return pinned.open(app);}catch(RuntimeException unavailable){return new Port(){private volatile Port recovered;public Read read(){Port ready=recovered;if(ready==null)throw unavailable;return ready.read();}public void save(SoundSensitivity policy){Port ready=pinned.open(app);ready.save(policy);recovered=ready;}};}}
 static Port of(SharedPreferences prefs){return new Port(){public Read read(){return SoundSensitivitySettings.read(prefs);}public void save(SoundSensitivity policy){if(!prefs.edit().putString(KEY,SoundSensitivityJson.encode(policy)).commit())throw new IllegalStateException("Sound sensitivity could not be saved. Try again.");}};}
 static Read read(SharedPreferences prefs){try{if(!prefs.contains(KEY))return new Read(SoundSensitivity.DEFAULT,"");return new Read(SoundSensitivityJson.decode(prefs.getString(KEY,null)),"");}catch(IllegalArgumentException|ClassCastException error){return new Read(SoundSensitivity.DEFAULT,"Saved settings are invalid. Review and save to enable analysis.");}}
 static SoundSensitivity snapshot(Context context){try{Read read=factory.open(context.getApplicationContext()).read();return read.error.isEmpty()?read.policy:SoundSensitivity.blocked();}catch(RuntimeException unavailable){return SoundSensitivity.blocked();}}
}
