package org.sleepwell.app;

import org.json.*;
import java.time.*;
import java.util.*;

public class Alarm {
    public int id, hour=7, minute=0, smartWindow=20, volume=85, rampSeconds=30, snoozeMinutes=5, snoozeLimit=1, difficulty=3, awakeCheck=0, backupMinutes=3;
    public boolean enabled=false, vibrate=true, pin=false;
    public String label="A fresh start", challenge="Math", tone="daybreak";
    public Set<Integer> days = new HashSet<>();
    public Alarm(int id) { this.id=id; }
    public String time() { return String.format(Locale.getDefault(), "%02d:%02d", hour, minute); }
    public long next(long now) { return SleepMath.nextOccurrence(hour,minute,days,now,ZoneId.systemDefault()); }
    public JSONObject json() {
        JSONObject j=new JSONObject();
        try {
            j.put("id",id).put("hour",hour).put("minute",minute).put("enabled",enabled).put("label",label)
                .put("smartWindow",smartWindow).put("volume",volume).put("rampSeconds",rampSeconds)
                .put("snoozeMinutes",snoozeMinutes).put("snoozeLimit",snoozeLimit).put("difficulty",difficulty)
                .put("awakeCheck",awakeCheck).put("backupMinutes",backupMinutes).put("vibrate",vibrate).put("pin",pin).put("challenge",challenge).put("tone",tone)
                .put("days",new JSONArray(days));
        } catch(JSONException e) { throw new IllegalStateException(e); }
        return j;
    }
    public static Alarm read(JSONObject j) {
        Alarm a=new Alarm(j.optInt("id")); a.hour=j.optInt("hour",7); a.minute=j.optInt("minute");
        a.enabled=j.optBoolean("enabled"); a.label=j.optString("label","A fresh start"); a.smartWindow=j.optInt("smartWindow",20);
        a.volume=j.optInt("volume",85); a.rampSeconds=j.optInt("rampSeconds",30); a.snoozeMinutes=j.optInt("snoozeMinutes",5);
        a.snoozeLimit=j.optInt("snoozeLimit",1); a.difficulty=j.optInt("difficulty",3); a.awakeCheck=j.optInt("awakeCheck",0);
        a.backupMinutes=j.optInt("backupMinutes",3);a.vibrate=j.optBoolean("vibrate",true); a.pin=j.optBoolean("pin"); a.challenge=j.optString("challenge","Math"); a.tone=j.optString("tone","daybreak");
        JSONArray ds=j.optJSONArray("days"); if(ds!=null) for(int i=0;i<ds.length();i++) a.days.add(ds.optInt(i));
        return a;
    }
}
