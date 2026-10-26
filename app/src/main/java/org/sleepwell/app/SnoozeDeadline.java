package org.sleepwell.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.provider.Settings;
import org.json.JSONObject;
import java.util.UUID;

/** Acknowledged scheduling hint for display only. Never authorizes delivery or reschedules a snooze. */
final class SnoozeDeadline {
    private static String key(int id){return "snoozeDisplayReceipt_"+id;}
    private static String budget(int id,String suffix){return "snoozeBudget_"+id+"_"+suffix;}
    private static long boot(Context context){return Settings.Global.getInt(context.getContentResolver(),Settings.Global.BOOT_COUNT,-1);}
    private static boolean nonce(String text){try{return UUID.fromString(text).toString().equals(text);}catch(RuntimeException invalid){return false;}}
    private static boolean valid(Alarm alarm){return alarm!=null&&alarm.id>0&&!alarm.nextOverrideInvalid&&!alarm.repeatInvalid&&alarm.repeatRule.validFor(alarm)&&!alarm.napInvalid&&!alarm.skipInvalid&&!alarm.routineInvalid&&!alarm.dateInvalid&&!alarm.solarInvalid&&!alarm.excludedDatesInvalid&&!alarm.snoozeTotalInvalid&&!alarm.weeklyNapOwnerInvalid;}
    private static boolean sameSource(Alarm source,Alarm saved){
        if(!valid(source)||!valid(saved)||source.id!=saved.id)return false;
        if(source.nap!=null)return source.nap.phase==NapPlan.Phase.CLAIMED&&!source.enabled&&source.json().toString().equals(saved.json().toString());
        // Main delivery consumes skip state and disables one-shot alarms before the
        // active occurrence is saved. This exact derived snapshot is the only allowed disable.
        Alarm consumed=Alarm.read(source.json());AlarmSkips.consume(consumed);if(consumed.days.isEmpty())consumed.enabled=false;
        return consumed.json().toString().equals(saved.json().toString());
    }
    /** Called only after Android accepted this SNOOZE schedule. Failure cannot roll it back. */
    static void scheduled(Context context,int id,long deadline){
        if(id<=0)return;
        try{synchronized(Scheduler.class){
            SharedPreferences prefs=Prefs.of(context);String run=prefs.getString("alarmRun",""),source=prefs.getString(budget(id,"snapshot"),"");
            long now=System.currentTimeMillis(),boot=boot(context),spent=prefs.getLong(budget(id,"spent"),-1);int count=prefs.getInt("snoozes_"+id,0);
            if(!nonce(run)||!prefs.getBoolean("ringing",false)||boot<0||deadline<=now||deadline-now>300L*60000||spent<0||spent>300L*60000||count<1||count>5||!run.equals(prefs.getString(budget(id,"owner"),""))||deadline!=prefs.getLong(budget(id,"pending"),0)||source.isEmpty()||source.length()>131072||!source.equals(prefs.getString("snoozed_"+id,""))||!source.equals(prefs.getString("active_alarm",""))){clear(context,id);return;}
            Alarm active=Alarm.read(new JSONObject(source));Alarm saved;try(Store store=Records.open(context)){saved=store.alarm(id);}
            if(!sameSource(active,saved)||active.id!=id){clear(context,id);return;}
            long occurrence=prefs.getLong("lastOccurrence_"+id,0);if(active.nap==null&&active.days.isEmpty()&&occurrence<=0){clear(context,id);return;}
            JSONObject receipt=new JSONObject().put("version",1).put("run",run).put("deadline",deadline).put("boot",boot).put("elapsed",SystemClock.elapsedRealtime()).put("scheduled",now).put("source",source).put("record",saved.json().toString()).put("count",count).put("spent",spent).put("occurrence",occurrence);
            if(!prefs.edit().putString(key(id),receipt.toString()).commit())clear(context,id);
        }}catch(Exception unknown){clear(context,id);}finally{changed(context);}
    }
    /** Read-only RTC projection: timezone changes affect formatting, never the saved wall deadline. */
    static long project(Context context,SharedPreferences prefs,Alarm current,long now){
        if(!valid(current))return 0;
        try{
            if(prefs.getBoolean("ringing",false))return 0;String raw=prefs.getString(key(current.id),"");if(raw.isEmpty()||raw.length()>393216)return 0;
            JSONObject receipt=new JSONObject(raw);if(receipt.length()!=11||SensorProvenance.whole(receipt,"version")!=1)return 0;
            String run=SensorProvenance.text(receipt,"run"),source=SensorProvenance.text(receipt,"source");long deadline=SensorProvenance.whole(receipt,"deadline"),scheduled=SensorProvenance.whole(receipt,"scheduled"),issued=SensorProvenance.whole(receipt,"elapsed"),savedBoot=SensorProvenance.whole(receipt,"boot"),spent=SensorProvenance.whole(receipt,"spent"),occurrence=SensorProvenance.whole(receipt,"occurrence"),count=SensorProvenance.whole(receipt,"count");
            if(!nonce(run)||deadline<=now||scheduled<=0||deadline<=scheduled||deadline-scheduled>300L*60000||savedBoot<0||savedBoot!=boot(context)||issued<0||issued>SystemClock.elapsedRealtime()||spent<0||spent>300L*60000||count<1||count>5)return 0;
            int id=current.id;
            if(!run.equals(prefs.getString("alarmRun",""))||!run.equals(prefs.getString(budget(id,"owner"),""))||deadline!=prefs.getLong(budget(id,"pending"),0)||count!=prefs.getInt("snoozes_"+id,0)||spent!=prefs.getLong(budget(id,"spent"),-1)||occurrence!=prefs.getLong("lastOccurrence_"+id,0))return 0;
            if(source.isEmpty()||source.length()>131072||!source.equals(prefs.getString(budget(id,"snapshot"),""))||!source.equals(prefs.getString("snoozed_"+id,""))||!source.equals(prefs.getString("active_alarm",""))||!current.json().toString().equals(SensorProvenance.text(receipt,"record")))return 0;
            Alarm active=Alarm.read(new JSONObject(source));if(active.id!=id||!sameSource(active,current))return 0;
            if(!current.enabled&&current.nap==null&&(!current.days.isEmpty()||occurrence<=0))return 0;
            return deadline;
        }catch(Exception invalid){return 0;}
    }
    /** After the owned alarm stops, only a fully validated successful system-schedule receipt is an event. */
    static String acceptedTransition(Context context,String expected){try{SharedPreferences prefs=Prefs.of(context);if(!nonce(expected)||!expected.equals(prefs.getString("alarmRun",""))||prefs.getBoolean("ringing",false))return "";Alarm source=Alarm.read(new JSONObject(prefs.getString("active_alarm","{}")));Alarm saved;try(Store store=Records.open(context)){saved=store.alarm(source.id);}long deadline=project(context,prefs,saved,System.currentTimeMillis());if(deadline<=0)return "";return expected+":"+prefs.getInt("snoozes_"+source.id,0)+":"+deadline;}catch(Exception unavailable){return "";}}
    /** A saved offer without a valid success receipt is uncertainty, never a projected deadline. */
    static boolean requested(SharedPreferences prefs,Alarm current,long now){
        if(!valid(current))return false;
        try{int id=current.id;String run=prefs.getString("alarmRun",""),source=prefs.getString(budget(id,"snapshot"),"");if(!nonce(run)||prefs.getBoolean("ringing",false)||!run.equals(prefs.getString(budget(id,"owner"),""))||prefs.getLong(budget(id,"pending"),0)<=now||source.isEmpty()||source.length()>131072||!source.equals(prefs.getString("snoozed_"+id,""))||!source.equals(prefs.getString("active_alarm","")))return false;return sameSource(Alarm.read(new JSONObject(source)),current);}catch(Exception invalid){return false;}
    }
    static void clear(Context context,int id){try{Prefs.of(context).edit().remove(key(id)).apply();}catch(RuntimeException ignored){/* Budget/run validation still rejects an obsolete hint. */}}
    static void changed(Context context){try{HomeSurfaces.changed(context);}catch(RuntimeException ignored){/* Widget delivery must never affect alarm scheduling. */}}
    private SnoozeDeadline(){}
}
