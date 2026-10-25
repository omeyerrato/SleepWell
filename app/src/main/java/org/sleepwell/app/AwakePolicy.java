package org.sleepwell.app;
import org.json.*;
/** Versioned phone-only heuristic; no sleep-stage or medical inference. */
final class AwakePolicy {
 static final AwakePolicy OFF=new AwakePolicy(false,true,true,true);
 final boolean enabled,interaction,light,snooze;
 AwakePolicy(boolean enabled,boolean interaction,boolean light,boolean snooze){if(enabled&&!interaction&&!light&&!snooze)throw new IllegalArgumentException("Choose at least one awake signal.");this.enabled=enabled;this.interaction=interaction;this.light=light;this.snooze=snooze;}
 String encode(){try{return new JSONObject().put("version",1).put("enabled",enabled).put("interaction",interaction).put("light",light).put("snooze",snooze).toString();}catch(JSONException e){throw new IllegalStateException(e);}}
 static AwakePolicy decode(String raw){try{JSONObject j=new JSONObject(raw);if(j.length()!=5||!(j.get("version") instanceof Integer)||j.getInt("version")!=1)throw new JSONException("Invalid awake policy");for(String key:new String[]{"enabled","interaction","light","snooze"})if(!(j.get(key) instanceof Boolean))throw new JSONException("Invalid awake switch");return new AwakePolicy(j.getBoolean("enabled"),j.getBoolean("interaction"),j.getBoolean("light"),j.getBoolean("snooze"));}catch(JSONException bad){throw new IllegalArgumentException("Awake settings are unreadable. Review and save them.",bad);}}
}
