package org.sleepwell.app;

import org.json.*;
import java.time.*;
import java.util.*;

public class Alarm {
    public int id, hour=7, minute=0, smartWindow=20, volume=85, rampSeconds=30, snoozeMinutes=5, snoozeLimit=1, difficulty=3, awakeCheck=0, backupMinutes=3;
    public boolean enabled=false, vibrate=true, pin=false, extraLoud=false, sunrise=false, flashlight=false, shortenSnoozes=false;
    public int soundDelaySeconds=0,vibrationDelaySeconds=0;
    public String label="A fresh start", challenge="Math", tone="daybreak";
    String toneDisplayName="";
    ChallengeRoutine routine;boolean routineInvalid;private Object invalidRoutine;
    NapPlan nap;boolean napInvalid;private Object invalidNap;
    AlarmSkip skip;boolean skipInvalid;Object invalidSkip;String scheduleError="";
    ChallengeRoutine challenges(){return routine==null?ChallengeRoutine.legacy(challenge,difficulty):routine;}
    void setRoutine(ChallengeRoutine value){routine=value;routineInvalid=false;invalidRoutine=null;challenge=value.tasks.get(0).type;difficulty=value.tasks.get(0).rounds;}
    public Set<Integer> days = new HashSet<>();
    public Alarm(int id) { this.id=id; }
    public String time() { return String.format(Locale.getDefault(), "%02d:%02d", hour, minute); }
    public long next(long now) { return next(now,ZoneId.systemDefault()); }
    long next(long now,ZoneId zone) {return skipInvalid?0:AlarmSkip.next(hour,minute,days,now,zone,skip);}
    public JSONObject json() {
        JSONObject j=new JSONObject();
        try {
            j.put("id",id).put("hour",hour).put("minute",minute).put("enabled",enabled).put("label",label)
                .put("smartWindow",smartWindow).put("volume",volume).put("rampSeconds",rampSeconds)
                .put("snoozeMinutes",snoozeMinutes).put("snoozeLimit",snoozeLimit).put("difficulty",difficulty)
                .put("awakeCheck",awakeCheck).put("backupMinutes",backupMinutes).put("vibrate",vibrate).put("pin",pin).put("challenge",challenge).put("tone",tone)
                .put("extraLoud",extraLoud).put("sunrise",sunrise).put("flashlight",flashlight).put("soundDelaySeconds",soundDelaySeconds).put("vibrationDelaySeconds",vibrationDelaySeconds)
                .put("shortenSnoozes",shortenSnoozes).put("days",new JSONArray(days));
            if(tone.startsWith("content:")&&!toneDisplayName.isEmpty())j.put("toneDisplayName",cleanToneName(toneDisplayName));
            if(routine!=null)j.put("routine",ChallengeRoutineJson.encode(routine));else if(routineInvalid)j.put("routine",invalidRoutine);
            if(nap!=null)j.put("nap",NapPlanJson.encode(nap));else if(napInvalid)j.put("nap",invalidNap);
            if(skip!=null)j.put("skip",AlarmSkipJson.encode(skip));else if(skipInvalid)j.put("skip",invalidSkip);
            if(!scheduleError.isEmpty())j.put("scheduleError",scheduleError);
        } catch(JSONException e) { throw new IllegalStateException(e); }
        return j;
    }
    public static Alarm read(JSONObject j) {
        Alarm a=new Alarm(j.optInt("id")); a.hour=j.optInt("hour",7); a.minute=j.optInt("minute");
        a.enabled=j.optBoolean("enabled"); a.label=j.optString("label","A fresh start"); a.smartWindow=j.optInt("smartWindow",20);
        a.volume=j.optInt("volume",85); a.rampSeconds=j.optInt("rampSeconds",30); a.snoozeMinutes=j.optInt("snoozeMinutes",5);
        a.snoozeLimit=j.optInt("snoozeLimit",1); a.difficulty=j.optInt("difficulty",3); a.awakeCheck=j.optInt("awakeCheck",0);
        a.backupMinutes=j.optInt("backupMinutes",3);a.vibrate=j.optBoolean("vibrate",true); a.pin=j.optBoolean("pin"); a.challenge=j.optString("challenge","Math"); a.tone=j.optString("tone","daybreak");
        a.toneDisplayName=a.tone.startsWith("content:")?cleanToneName(j.opt("toneDisplayName")):"";
        a.extraLoud=j.optBoolean("extraLoud",false);a.sunrise=j.optBoolean("sunrise",false);a.flashlight=j.optBoolean("flashlight",false);a.soundDelaySeconds=j.optInt("soundDelaySeconds",0);a.vibrationDelaySeconds=j.optInt("vibrationDelaySeconds",0);
        a.shortenSnoozes=j.optBoolean("shortenSnoozes",false);
        if(j.has("routine"))try{a.routine=ChallengeRoutineJson.decode(j.getJSONArray("routine"));}catch(JSONException invalid){a.routineInvalid=true;a.invalidRoutine=j.opt("routine");}
        if(j.has("nap"))try{a.nap=NapPlanJson.decode(j.getJSONObject("nap"));}catch(JSONException invalid){a.napInvalid=true;a.invalidNap=j.opt("nap");}
        JSONArray ds=j.optJSONArray("days"); if(ds!=null) for(int i=0;i<ds.length();i++) a.days.add(ds.optInt(i));
        if(j.has("skip"))try{a.skip=AlarmSkipJson.decode(j.getJSONObject("skip"));if(a.nap!=null||a.napInvalid||a.days.isEmpty()&&a.enabled||!a.skip.matches(a.hour,a.minute,a.days))throw new JSONException("Skip does not match alarm");}catch(JSONException invalid){a.skip=null;a.skipInvalid=true;a.invalidSkip=j.opt("skip");}
        Object error=j.opt("scheduleError");if(error instanceof String&&((String)error).length()<=240)a.scheduleError=(String)error;
        return a;
    }
    /** Cached document text is display-only; it grants no access and never chooses a tone. */
    static String cleanToneName(Object name){if(!(name instanceof String))return "";String value=((String)name).replaceAll("[\\p{Cntrl}\\p{Cf}\\p{Zl}\\p{Zp}]"," ").strip();return value.codePointCount(0,value.length())>100?value.substring(0,value.offsetByCodePoints(0,100)):value;}
}
