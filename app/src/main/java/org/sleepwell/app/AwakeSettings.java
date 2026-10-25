package org.sleepwell.app;
import android.content.*;
final class AwakeSettings {
 static final String KEY="awakeEstimatesV1";
 interface Port {String read();void save(String value);}
 interface Factory {Port open(Context context);}
 static Factory factory=c->new Port(){public String read(){return Prefs.of(c).getString(KEY,null);}public void save(String value){if(!Prefs.of(c).edit().putString(KEY,value).commit())throw new IllegalStateException("Awake settings could not be saved.");}};
 static AwakePolicy read(Context c){String raw=factory.open(c.getApplicationContext()).read();return raw==null?AwakePolicy.OFF:AwakePolicy.decode(raw);}
}
