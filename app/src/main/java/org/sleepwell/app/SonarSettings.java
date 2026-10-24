package org.sleepwell.app;
import android.content.*;import org.json.*;
/** Separate configuration namespace. Saving never opens audio or changes existing sound settings. */
final class SonarSettings {
 interface Factory {SharedPreferences open(Context c);}
 static Factory factory=c->c.getSharedPreferences("sonar_configuration_v1",Context.MODE_PRIVATE);
 static SonarConfig load(Context c){return read(factory.open(c));}
 static SonarConfig read(SharedPreferences p){try{String raw=p.getString("profile","");if(raw.isEmpty())return SonarConfig.defaults();if(raw.length()>1024)throw new JSONException("Profile size");JSONObject j=new JSONObject(raw);if(j.length()!=4||!number(j.get("version"),1)||!SonarConfig.ALGORITHM.equals(j.get("algorithm"))||!number(j.get("rate"),48000)||!(j.get("gain") instanceof Number))throw new JSONException("Unknown sonar profile");double gain=((Number)j.get("gain")).doubleValue();if(!Double.isFinite(gain)||gain<.01||gain>.100001)throw new JSONException("Gain range");return new SonarConfig((float)gain);}catch(JSONException|IllegalArgumentException|ClassCastException error){throw new IllegalStateException("Saved contactless settings are invalid. Test again before saving.",error);}}
 private static boolean number(Object value,int expected){return value instanceof Number&&((Number)value).doubleValue()==expected;}
 static void save(SharedPreferences prefs,SonarConfig config){try{String raw=new JSONObject().put("version",1).put("algorithm",SonarConfig.ALGORITHM).put("rate",48000).put("gain",config.gain).toString();if(!prefs.edit().putString("profile",raw).commit())throw new IllegalStateException("Contactless settings could not be saved.");}catch(JSONException impossible){throw new IllegalStateException(impossible);}}
}
