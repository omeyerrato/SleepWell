package org.sleepwell.app;
import android.content.*;

final class AlarmDefaultsSettings {
    static final String KEY="behavior";
    static final class Read {final AlarmDefaults defaults;final String error;Read(AlarmDefaults defaults,String error){this.defaults=defaults;this.error=error;}Alarm create(int id){return defaults==null?new Alarm(id):defaults.create(id);}}
    interface Port {Read read();void save(AlarmDefaults defaults);}
    interface Factory {Port open(Context context);}
    static Factory factory=context->of(context.getSharedPreferences("alarm_defaults_v1",Context.MODE_PRIVATE));
    static Port of(SharedPreferences prefs){return new Port(){public Read read(){return AlarmDefaultsSettings.read(prefs);}public void save(AlarmDefaults value){synchronized(AlarmDefaultsSettings.class){SharedPreferences.Editor edit=prefs.edit();if(value==null)edit.remove(KEY);else edit.putString(KEY,AlarmDefaultsJson.encode(value));if(!edit.commit())throw new IllegalStateException("Defaults could not be saved. Try again.");}}};}
    static Read read(SharedPreferences prefs){try{return !prefs.contains(KEY)?new Read(null,""):new Read(AlarmDefaultsJson.decode(prefs.getString(KEY,null)),"");}catch(RuntimeException invalid){return new Read(null,"Saved defaults could not be read. Factory settings are being used.");}}
    static Read load(Context context){try{return factory.open(context.getApplicationContext()).read();}catch(RuntimeException failure){return new Read(null,"Saved defaults could not be read. Factory settings are being used.");}}
    // On uncertain references, retain the document grant rather than revoke a possibly saved template's access.
    static boolean references(Context context,String uri){try{Read saved=factory.open(context.getApplicationContext()).read();return !saved.error.isEmpty()||saved.defaults!=null&&saved.defaults.references(uri);}catch(RuntimeException uncertain){return true;}}
}
