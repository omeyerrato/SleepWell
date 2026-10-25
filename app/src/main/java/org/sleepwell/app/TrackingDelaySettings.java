package org.sleepwell.app;
import android.content.*;
/** Separate local preference and per-admission journal. Never restored or rearmed from backup. */
final class TrackingDelaySettings {
 interface Factory{SharedPreferences open(Context c);}static Factory factory=c->c.getSharedPreferences("sleepwell-tracking-delay",0);final SharedPreferences prefs;TrackingDelaySettings(SharedPreferences prefs){this.prefs=prefs;}
 int minutes(){int value=prefs.getInt("minutes",0);if(!TrackingDelay.allowed(value))throw new IllegalStateException("Review the saved tracking delay.");return value;}
 void minutes(int value){if(!TrackingDelay.allowed(value))throw new IllegalArgumentException("Choose a supported delay");if(!prefs.edit().putInt("minutes",value).commit())throw new IllegalStateException("Tracking delay could not be saved.");}
 TrackingDelay read(){if(!prefs.contains("token"))return null;try{return new TrackingDelay(prefs.getString("token",""),prefs.getLong("elapsed",-1),prefs.getLong("wall",-1),prefs.getInt("boot",-1),prefs.getInt("duration",-1),prefs.getBoolean("sonar",false),prefs.getBoolean("sounds",false),prefs.getBoolean("clips",false),prefs.getBoolean("light",false),TrackingDelay.Phase.valueOf(prefs.getString("phase","")),prefs.getLong("session",0),prefs.getString("issue",""));}catch(RuntimeException bad){throw new IllegalStateException("The delayed-start status is unreadable. No recording will restart from it.",bad);}}
 void save(TrackingDelay p){if(!prefs.edit().putString("token",p.token).putLong("elapsed",p.elapsed).putLong("wall",p.wall).putInt("boot",p.boot).putInt("duration",p.minutes).putBoolean("sonar",p.sonar).putBoolean("sounds",p.sounds).putBoolean("clips",p.clips).putBoolean("light",p.light).putString("phase",p.phase.name()).putLong("session",p.session).putString("issue",p.issue).commit())throw new IllegalStateException("Delayed-start status could not be saved.");}
}
