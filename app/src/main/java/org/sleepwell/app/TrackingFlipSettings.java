package org.sleepwell.app;
import android.content.*;
/** Local opt-in; default off and captured only at admitted recording start. */
final class TrackingFlipSettings {
 interface Factory{SharedPreferences open(Context c);}static Factory factory=c->c.getSharedPreferences("sleepwell-tracking-flip",0);
 static boolean enabled(Context c){return factory.open(c).getBoolean("enabled",false);}
 private TrackingFlipSettings(){}
}
