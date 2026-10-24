package org.sleepwell.app;

import android.content.*;
import org.json.JSONObject;

final class SmartLightSettings {
    interface Factory {SharedPreferences preferences(Context context);}
    static Factory factory=c->c.getSharedPreferences("smart_light_v1",Context.MODE_PRIVATE);
    private static final Object LOCK=new Object();
    static SmartLightConfig load(Context context)throws Exception{return load(factory.preferences(context),new AutomationSecrets());}
    static SmartLightConfig load(SharedPreferences prefs,AutomationStore.Vault vault)throws Exception{String sealed=prefs.getString("config","");if(sealed.isEmpty())return new SmartLightConfig("","","light.bedroom",false,10,80,true,prefs.getLong("revision",0));JSONObject data=new JSONObject(vault.open(sealed));if(data.getInt("version")!=1)throw new IllegalArgumentException("Unknown lamp settings");SmartLightConfig result=new SmartLightConfig(data.getString("endpoint"),data.getString("token"),data.getString("entity"),prefs.getBoolean("enabled",false),data.getInt("minutes"),data.getInt("maximum"),data.getBoolean("restore"),prefs.getLong("revision",0));result.validate();return result;}
    static SmartLightConfig save(Context context,SmartLightConfig config)throws Exception{return save(factory.preferences(context),new AutomationSecrets(),config);}
    static SmartLightConfig save(SharedPreferences prefs,AutomationStore.Vault vault,SmartLightConfig config)throws Exception{config.validate();String sealed=vault.seal(new JSONObject().put("version",1).put("endpoint",config.endpoint).put("token",config.token).put("entity",config.entity).put("minutes",config.minutes).put("maximum",config.maximum).put("restore",config.restore).toString());synchronized(LOCK){if(prefs.getLong("revision",0)!=config.revision)throw new IllegalStateException("Lamp settings changed. Reopen and retry.");long revision=config.revision+1;if(!prefs.edit().putString("config",sealed).putBoolean("enabled",config.enabled).putLong("revision",revision).putString("status",config.enabled?"Ready for the next real alarm":"Off").remove("error").commit())throw new IllegalStateException("Lamp settings could not be saved.");return new SmartLightConfig(config.endpoint,config.token,config.entity,config.enabled,config.minutes,config.maximum,config.restore,revision);}}
    static void disconnect(Context context){synchronized(LOCK){SharedPreferences prefs=factory.preferences(context);if(!prefs.edit().remove("config").putBoolean("enabled",false).putLong("revision",prefs.getLong("revision",0)+1).putString("status","Disconnected").remove("error").commit())throw new IllegalStateException("Lamp could not be disconnected.");}}
    static boolean current(Context context,long revision){SharedPreferences prefs=factory.preferences(context);return prefs.getBoolean("enabled",false)&&prefs.getLong("revision",0)==revision;}
    static void status(Context context,String message){SharedPreferences.Editor edit=factory.preferences(context).edit().putString("status",message);if(message.contains("unavailable")||message.contains("could not")||message.contains("disconnected"))edit.putString("error",message);edit.apply();}
}
