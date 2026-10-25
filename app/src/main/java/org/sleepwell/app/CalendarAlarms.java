package org.sleepwell.app;

import android.content.Context;
import org.json.*;
import java.time.*;

/** Calendar reads never run here. A saved receipt fences an already admitted adjustment. */
final class CalendarAlarms {
    static volatile CalendarPolicy.Snapshot snapshot;
    static CalendarPolicy.Plan plan(Context c,Alarm alarm,long base,long now){
        return plan(c,alarm,base,now,ZoneId.systemDefault());
    }
    static CalendarPolicy.Plan plan(Context c,Alarm alarm,long base,long now,ZoneId zone){
        if(alarm.days.isEmpty()||alarm.nap!=null||alarm.napInvalid||alarm.skipInvalid)return CalendarPolicy.ordinary(base,"");
        try{CalendarPolicy.Rules rules=CalendarSettings.load(c);CalendarPolicy.Plan planned=CalendarPolicy.plan(rules,snapshot,now,base,zone,value->alarm.next(value,zone));if(rules.enabled&&(snapshot==null||!snapshot.fresh(now,zone,rules)))CalendarSync.request(c);return planned;}
        catch(RuntimeException unavailable){return CalendarPolicy.ordinary(base,"Calendar settings need review");}
    }
    static long preview(Context c,Alarm alarm,long ordinary,long now,ZoneId zone){long installed=Prefs.of(c).getLong("deadline_"+alarm.id,0),original=base(c,alarm,installed);if(installed>now&&original>0&&AlarmSkips.eligible(alarm,original,Math.max(now,original),Prefs.of(c).getLong("lastOccurrence_"+alarm.id,0),zone))return installed;return plan(c,alarm,ordinary,now,zone).deadline;}
    static void scheduled(Context c,Alarm alarm,CalendarPolicy.Plan plan){
        android.content.SharedPreferences.Editor edit=CalendarSettings.prefs(c).edit();String key="receipt_"+alarm.id;
        if(!plan.changed()){if(!edit.remove(key).commit())throw new IllegalStateException("Calendar schedule could not be saved");return;}
        try{String receipt=new JSONObject().put("base",plan.base).put("deadline",plan.deadline).put("key",AlarmSkips.scheduleKey(alarm)).put("revision",plan.revision).toString();if(!edit.putString(key,receipt).commit())throw new IllegalStateException("Calendar schedule could not be saved");}catch(JSONException impossible){throw new IllegalStateException(impossible);}
    }
    private static long base(Context c,Alarm alarm,long deadline){try{
        String raw=CalendarSettings.prefs(c).getString("receipt_"+alarm.id,"");if(raw.isEmpty())return 0;JSONObject row=new JSONObject(raw);CalendarPolicy.Rules rules=CalendarSettings.load(c);
        long original=row.getLong("base");if(!rules.enabled||!rules.revision.equals(row.getString("revision"))||!AlarmSkips.scheduleKey(alarm).equals(row.getString("key"))||row.getLong("deadline")!=deadline||deadline>original||original-deadline>6*3600000L)return -1;return original;
    }catch(RuntimeException|JSONException invalid){return -1;}}
    static boolean eligible(Context c,Alarm alarm,long deadline,long now,long last,ZoneId zone){
        if(alarm==null)return false;long original=base(c,alarm,deadline);if(original==0)return AlarmSkips.eligible(alarm,deadline,now,last,zone);return original>0&&AlarmSkips.eligible(alarm,original,Math.max(now,original),last,zone)&&now>=deadline-alarm.smartWindow*60000L;
    }
    static long consumed(Context c,Alarm alarm,long deadline){long original=base(c,alarm,deadline);return original>0?original:deadline;}
    static long skipBase(Context c,Alarm alarm,long chosen,long now,ZoneId zone){long original=base(c,alarm,chosen);if(original>0&&alarm.occurs(original,zone))return original;long ordinary=alarm.next(Math.max(now,Prefs.of(c).getLong("lastOccurrence_"+alarm.id,0)),zone);CalendarPolicy.Plan planned=plan(c,alarm,ordinary,now,zone);if(planned.deadline!=chosen||!alarm.occurs(planned.base,zone))throw new AlarmSkips.Changed("This sunrise occurrence changed. Review its new time.");return planned.base;}
    static void recordSkip(Context c,Alarm saved,long chosen){if(saved.skip==null||chosen==saved.skip.at(ZoneId.systemDefault()))return;try{String value=new JSONObject().put("nonce",saved.skip.nonce).put("deadline",chosen).toString();if(!CalendarSettings.prefs(c).edit().putString("skipped_"+saved.id,value).commit())throw new IllegalStateException("Skipped calendar occurrence could not be saved");}catch(JSONException impossible){throw new IllegalStateException(impossible);}}
    static long skippedDeadline(Context c,Alarm alarm,long ordinary){try{JSONObject row=new JSONObject(CalendarSettings.prefs(c).getString("skipped_"+alarm.id,"{}"));return alarm.skip!=null&&alarm.skip.nonce.equals(row.optString("nonce"))?row.getLong("deadline"):ordinary;}catch(RuntimeException|JSONException unavailable){return ordinary;}}
    private CalendarAlarms(){}
}
