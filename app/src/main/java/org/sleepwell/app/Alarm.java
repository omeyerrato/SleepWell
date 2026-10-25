package org.sleepwell.app;

import org.json.*;
import java.time.*;
import java.util.*;

public class Alarm {
    public int id, hour=7, minute=0, smartWindow=20, volume=85, rampSeconds=30, snoozeMinutes=5, snoozeLimit=1, difficulty=3, awakeCheck=0, backupMinutes=3;
    public boolean enabled=false, vibrate=true, pin=false, extraLoud=false, sunrise=false, flashlight=false, shortenSnoozes=false;
    public int soundDelaySeconds=0,vibrationDelaySeconds=0;
    int snoozeTotalMinutes;boolean snoozeTotalInvalid;private Object invalidSnoozeTotal;
    public String label="A fresh start", challenge="Math", tone="daybreak";
    String toneDisplayName="";
    ChallengeRoutine routine;boolean routineInvalid;private Object invalidRoutine;
    NapPlan nap;boolean napInvalid;private Object invalidNap;
    String weeklyNapOwner="";boolean weeklyNapOwnerInvalid;private Object invalidWeeklyNapOwner;
    AlarmRepeat repeatRule=AlarmRepeat.WEEKLY;boolean repeatInvalid;private Object invalidRepeat;
    LocalDate date;boolean dateInvalid;private Object invalidDate;
    SortedSet<LocalDate> excludedDates=Collections.unmodifiableSortedSet(new TreeSet<>());boolean excludedDatesInvalid;private Object invalidExcludedDates;
    AlarmSoundMode soundMode=AlarmSoundMode.SINGLE;boolean soundModeInvalid;private Object invalidSoundMode;
    AlarmTiming timing=AlarmTiming.DEFAULT;boolean timingInvalid;private Object invalidTiming;
    AlarmInputs inputs=AlarmInputs.DEFAULT;boolean inputsInvalid;private Object invalidInputs;
    AlarmBedtime bedtime=AlarmBedtime.FOLLOW;boolean bedtimeInvalid;private Object invalidBedtime;
    SunriseSchedule solar;boolean solarInvalid;private Object invalidSolar;
    AlarmSkip skip;boolean skipInvalid;Object invalidSkip;String scheduleError="";
    ChallengeRoutine challenges(){return routine==null?ChallengeRoutine.legacy(challenge,difficulty):routine;}
    void setRoutine(ChallengeRoutine value){routine=value;routineInvalid=false;invalidRoutine=null;challenge=value.tasks.get(0).type;difficulty=value.tasks.get(0).rounds;}
    void setRepeat(AlarmRepeat value){repeatRule=value;repeatInvalid=false;invalidRepeat=null;}
    public Set<Integer> days = new HashSet<>();
    public Alarm(int id) { this.id=id; }
    public String time() { return solarInvalid?"Review sunrise":solar==null?String.format(Locale.getDefault(), "%02d:%02d", hour, minute):"Sunrise"; }
    public long next(long now) { return next(now,ZoneId.systemDefault()); }
    long next(long now,ZoneId zone) {return AlarmExcludedDates.next(this,now,zone);}
    long nextBase(long now,ZoneId zone) {if(repeatInvalid||skipInvalid||dateInvalid||solarInvalid)return 0;if(repeatRule.custom())return repeatRule.next(this,now,zone);if(solar!=null)return solar.next(hour,minute,days,date,now,zone,skip);if(date!=null){long at=AlarmDate.next(date,hour,minute,now,zone);return at>0&&(skip==null||!skip.excludes(at,zone))?at:0;}return AlarmSkip.next(hour,minute,days,now,zone,skip);}
    boolean occurs(long at,ZoneId zone){return !repeatInvalid&&repeatRule.validFor(this)&&repeatRule.accepts(Instant.ofEpochMilli(at).atZone(zone).toLocalDate())&&!excludedDatesInvalid&&!AlarmExcludedDates.excludes(this,at,zone)&&!dateInvalid&&!solarInvalid&&(solar!=null?solar.occurs(hour,minute,days,date,at,zone):date==null?AlarmSkip.occurs(hour,minute,days,at,zone):days.isEmpty()&&AlarmDate.at(date,hour,minute,zone)==at);}
    public JSONObject json() {
        JSONObject j=new JSONObject();
        try {
            j.put("id",id).put("hour",hour).put("minute",minute).put("enabled",enabled).put("label",label)
                .put("smartWindow",smartWindow).put("volume",volume).put("rampSeconds",rampSeconds)
                .put("snoozeMinutes",snoozeMinutes).put("snoozeLimit",snoozeLimit).put("difficulty",difficulty)
                .put("awakeCheck",awakeCheck).put("backupMinutes",backupMinutes).put("vibrate",vibrate).put("pin",pin).put("challenge",challenge).put("tone",tone)
                .put("extraLoud",extraLoud).put("sunrise",sunrise).put("flashlight",flashlight).put("soundDelaySeconds",soundDelaySeconds).put("vibrationDelaySeconds",vibrationDelaySeconds)
                .put("shortenSnoozes",shortenSnoozes).put("days",new JSONArray(days));
            if(snoozeTotalInvalid)j.put("snoozeTotalMinutes",invalidSnoozeTotal);else if(snoozeTotalMinutes!=0)j.put("snoozeTotalMinutes",snoozeTotalMinutes);
            if(tone.startsWith("content:")&&!toneDisplayName.isEmpty())j.put("toneDisplayName",cleanToneName(toneDisplayName));
            if(routine!=null)j.put("routine",ChallengeRoutineJson.encode(routine));else if(routineInvalid)j.put("routine",invalidRoutine);
            if(nap!=null)j.put("nap",NapPlanJson.encode(nap));else if(napInvalid)j.put("nap",invalidNap);
            if(weeklyNapOwnerInvalid)j.put("weeklyNapOwner",invalidWeeklyNapOwner);else if(!weeklyNapOwner.isEmpty())j.put("weeklyNapOwner",weeklyNapOwner);
            if(repeatInvalid)j.put("repeatRule",invalidRepeat);else if(repeatRule.custom())j.put("repeatRule",AlarmRepeat.encode(repeatRule));
            if(date!=null)j.put("date",date.toString());else if(dateInvalid)j.put("date",invalidDate);
            if(excludedDatesInvalid)j.put("excludedDates",invalidExcludedDates);else if(!excludedDates.isEmpty())j.put("excludedDates",AlarmExcludedDates.encode(excludedDates));
            if(soundModeInvalid)j.put("soundMode",invalidSoundMode);else if(!soundMode.equals(AlarmSoundMode.SINGLE))j.put("soundMode",AlarmSoundModeJson.encode(soundMode));
            if(timingInvalid)j.put("timing",invalidTiming);else if(!timing.equals(AlarmTiming.DEFAULT))j.put("timing",AlarmTimingJson.encode(timing));
            if(inputsInvalid)j.put("inputs",invalidInputs);else if(!inputs.equals(AlarmInputs.DEFAULT))j.put("inputs",AlarmInputsJson.encode(inputs));
            if(bedtimeInvalid)j.put("bedtimeReminder",invalidBedtime);else if(!bedtime.mode.equals("follow"))j.put("bedtimeReminder",AlarmBedtime.encode(bedtime));
            if(solar!=null)j.put("solar",SunriseScheduleJson.encode(solar));else if(solarInvalid)j.put("solar",invalidSolar);
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
        if(j.has("snoozeTotalMinutes"))try{a.snoozeTotalMinutes=SnoozeBudget.read(j.get("snoozeTotalMinutes"));}catch(JSONException|IllegalArgumentException invalid){a.snoozeTotalInvalid=true;a.invalidSnoozeTotal=j.opt("snoozeTotalMinutes");}
        if(j.has("routine"))try{a.routine=ChallengeRoutineJson.decode(j.getJSONArray("routine"));}catch(JSONException invalid){a.routineInvalid=true;a.invalidRoutine=j.opt("routine");}
        if(j.has("weeklyNapOwner")){Object raw=j.opt("weeklyNapOwner");if(raw instanceof String&&BackupRuntime.nonce((String)raw))a.weeklyNapOwner=(String)raw;else{a.weeklyNapOwnerInvalid=true;a.invalidWeeklyNapOwner=raw;}}
        if(j.has("nap"))try{a.nap=NapPlanJson.decode(j.getJSONObject("nap"));}catch(JSONException invalid){a.napInvalid=true;a.invalidNap=j.opt("nap");}
        JSONArray ds=j.optJSONArray("days"); if(ds!=null) for(int i=0;i<ds.length();i++) a.days.add(ds.optInt(i));
        if(j.has("soundMode"))try{a.soundMode=AlarmSoundModeJson.decode(j.get("soundMode"));}catch(JSONException invalid){a.soundMode=AlarmSoundMode.SINGLE;a.soundModeInvalid=true;a.invalidSoundMode=j.opt("soundMode");}
        if(j.has("timing"))try{a.timing=AlarmTimingJson.decode(j.get("timing"));if(a.nap!=null||a.napInvalid||a.backupMinutes<0||a.backupMinutes>15)throw new JSONException("Review this alarm timing");}catch(JSONException invalid){a.timing=AlarmTiming.DEFAULT;a.timingInvalid=true;a.invalidTiming=j.opt("timing");}
        if(j.has("inputs"))try{a.inputs=AlarmInputsJson.decode(j.get("inputs"));}catch(JSONException invalid){a.inputsInvalid=true;a.invalidInputs=j.opt("inputs");}
        if(j.has("bedtimeReminder"))try{a.bedtime=AlarmBedtime.decode(j.get("bedtimeReminder"));if(a.nap!=null||a.napInvalid)throw new JSONException("Nap timers do not have bedtime overrides");}catch(JSONException invalid){a.bedtimeInvalid=true;a.invalidBedtime=j.opt("bedtimeReminder");}
        if(j.has("date"))try{a.date=AlarmDate.read(j.get("date"));if(!a.days.isEmpty()||a.nap!=null||a.napInvalid)throw new IllegalArgumentException("A dated alarm cannot repeat or be a nap");}catch(JSONException|IllegalArgumentException invalid){a.date=null;a.dateInvalid=true;a.invalidDate=j.opt("date");}
        if(j.has("excludedDates"))try{a.excludedDates=AlarmExcludedDates.read(j.get("excludedDates"));if(a.nap!=null||a.napInvalid)throw new JSONException("A nap cannot use excluded calendar dates");}catch(JSONException invalid){a.excludedDates=Collections.unmodifiableSortedSet(new TreeSet<>());a.excludedDatesInvalid=true;a.invalidExcludedDates=j.opt("excludedDates");}
        if(j.has("solar"))try{a.solar=SunriseScheduleJson.decode(j.get("solar"));if(a.nap!=null||a.napInvalid)throw new JSONException("A nap cannot use sunrise time");}catch(JSONException invalid){a.solar=null;a.solarInvalid=true;a.invalidSolar=j.opt("solar");}
        if(j.has("repeatRule"))try{a.repeatRule=AlarmRepeat.decode(j.get("repeatRule"));if(a.repeatRule.custom())a.days=new HashSet<>(AlarmRepeat.days(j.get("days")));a.repeatRule.check(a);}catch(JSONException|IllegalArgumentException invalid){a.repeatInvalid=true;a.invalidRepeat=j.opt("repeatRule");}
        if(j.has("skip"))try{a.skip=AlarmSkipJson.decode(j.getJSONObject("skip"));if(a.nap!=null||a.napInvalid||a.days.isEmpty()&&a.enabled||!a.skip.matches(a.hour,a.minute,a.days,a.solar,a.date)||a.solarInvalid||a.repeatInvalid||!a.repeatRule.accepts(a.skip.date))throw new JSONException("Skip does not match alarm");}catch(JSONException invalid){a.skip=null;a.skipInvalid=true;a.invalidSkip=j.opt("skip");}
        Object error=j.opt("scheduleError");if(error instanceof String&&((String)error).length()<=240)a.scheduleError=(String)error;
        return a;
    }
    /** Cached document text is display-only; it grants no access and never chooses a tone. */
    static String cleanToneName(Object name){if(!(name instanceof String))return "";String value=((String)name).replaceAll("[\\p{Cntrl}\\p{Cf}\\p{Zl}\\p{Zp}]"," ").strip();return value.codePointCount(0,value.length())>100?value.substring(0,value.offsetByCodePoints(0,100)):value;}
}
