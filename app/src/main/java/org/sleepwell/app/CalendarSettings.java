package org.sleepwell.app;

import android.content.*;
import org.json.*;
import java.util.*;

/** Local calendar selection is deliberately separate from portable sleep backups. */
final class CalendarSettings {
    static java.util.function.Function<Context,SharedPreferences> factory=c->c.getApplicationContext().getSharedPreferences("calendar_rules_v1",Context.MODE_PRIVATE);
    static SharedPreferences prefs(Context c){return factory.apply(c);}
    static CalendarPolicy.Rules load(Context c){String raw=prefs(c).getString("rules","");return raw.isEmpty()?CalendarPolicy.Rules.off():decode(raw);}
    static void save(Context c,CalendarPolicy.Rules rules){if(!prefs(c).edit().putString("rules",encode(rules)).commit())throw new IllegalStateException("Calendar rules could not be saved");}
    static String encode(CalendarPolicy.Rules rules){try{return new JSONObject().put("version",1).put("enabled",rules.enabled).put("holidays",new JSONArray(new TreeSet<>(rules.holidays))).put("meetings",new JSONArray(new TreeSet<>(rules.meetings))).put("preparation",rules.preparation).put("advance",rules.advance).put("revision",rules.revision).toString();}catch(JSONException impossible){throw new IllegalStateException(impossible);}}
    static CalendarPolicy.Rules decode(String raw){try{
        if(raw==null||raw.length()>8192)throw new JSONException("Calendar settings too large");JSONObject j=new JSONObject(raw);
        if(j.length()!=7||!j.has("version")||!j.has("enabled")||!j.has("holidays")||!j.has("meetings")||!j.has("preparation")||!j.has("advance")||!j.has("revision")||integer(j.get("version"))!=1||!(j.get("enabled") instanceof Boolean)||!(j.get("revision") instanceof String))throw new JSONException("Invalid calendar settings");
        return new CalendarPolicy.Rules(j.getBoolean("enabled"),ids(j.getJSONArray("holidays")),ids(j.getJSONArray("meetings")),integer(j.get("preparation")),integer(j.get("advance")),j.getString("revision"));
    }catch(JSONException bad){throw new IllegalArgumentException("Calendar settings need review",bad);}}
    private static int integer(Object value){if(!(value instanceof Integer))throw new IllegalArgumentException("Expected integer calendar option");return(Integer)value;}
    private static Set<Long> ids(JSONArray values)throws JSONException{if(values.length()>64)throw new JSONException("Too many calendars");Set<Long> out=new HashSet<>();for(int n=0;n<values.length();n++){Object value=values.get(n);if(!(value instanceof Integer||value instanceof Long)||((Number)value).longValue()<0||!out.add(((Number)value).longValue()))throw new JSONException("Invalid selected calendar");}return out;}
    private CalendarSettings(){}
}
