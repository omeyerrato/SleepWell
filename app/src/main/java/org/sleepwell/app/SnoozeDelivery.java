package org.sleepwell.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import org.json.JSONObject;
import java.util.UUID;

/** Private pending authority, independent of the optional home-screen display receipt. */
final class SnoozeDelivery {
    static final String RUN="snoozeRun",DEADLINE="snoozeDeadline";
    static final class Pending {
        final String run,snapshot;final long deadline,spent;final Alarm alarm;
        Pending(String run,String snapshot,long deadline,long spent,Alarm alarm){this.run=run;this.snapshot=snapshot;this.deadline=deadline;this.spent=spent;this.alarm=alarm;}
    }
    private static String key(int id,String suffix){return "snoozeBudget_"+id+"_"+suffix;}
    private static boolean nonce(String run){try{return UUID.fromString(run).toString().equals(run);}catch(RuntimeException invalid){return false;}}
    private static Pending owned(SharedPreferences prefs,int id){
        if(id<=0&&id!=-1)return null;
        try{
            String run=prefs.getString(key(id,"owner"),""),snapshot=prefs.getString(key(id,"snapshot"),"");
            long deadline=prefs.getLong(key(id,"pending"),0),spent=prefs.getLong(key(id,"spent"),-1);int count=prefs.getInt("snoozes_"+id,0);
            if(!nonce(run)||!run.equals(prefs.getString("alarmRun",""))||deadline<=0||spent<0||spent>300L*60000||count<1||count>5||snapshot.isEmpty()||snapshot.length()>131072||!snapshot.equals(prefs.getString("snoozed_"+id,""))||!snapshot.equals(prefs.getString("active_alarm","")))return null;
            Alarm alarm=Alarm.read(new JSONObject(snapshot));
            if(alarm.id!=id||alarm.snoozeTotalInvalid||count>alarm.snoozeLimit||!snapshot.equals(alarm.json().toString()))return null;
            return new Pending(run,snapshot,deadline,spent,alarm);
        }catch(Exception invalid){return null;}
    }
    /** Read-only provider/edit guard. Caller holds Scheduler.class and supplies the current saved row.
     * Includes overdue pending delivery: Android delay does not turn live authority into history. */
    static boolean pending(SharedPreferences prefs,Alarm current){
        if(current==null||current.id<=0)return false;Pending pending=owned(prefs,current.id);
        return pending!=null&&SnoozeSource.same(prefs,pending.alarm,current);
    }
    /** Prepare new immutable PendingIntent extras only from this run's existing reservation. */
    static Intent scheduled(Context context,int id,long deadline){
        SharedPreferences prefs=Prefs.of(context);Pending pending=owned(prefs,id);
        if(pending==null||pending.deadline!=deadline||!prefs.getBoolean("ringing",false)||!source(context,prefs,pending))throw new IllegalStateException("Snooze ownership changed. Alarm continues.");
        return new Intent(context,AlarmReceiver.class).setAction(Scheduler.SNOOZE).putExtra("id",id).putExtra(RUN,pending.run).putExtra(DEADLINE,deadline);
    }
    /** Caller holds Scheduler.class. Legacy intents with neither new extra still need all private proof. */
    static Alarm admit(Context context,int id,Intent intent,long now){
        try{
            SharedPreferences prefs=Prefs.of(context);Pending pending=owned(prefs,id);
            if(pending==null||prefs.getBoolean("ringing",false)||now<pending.deadline-1000||!source(context,prefs,pending))return null;
            if(intent!=null&&(intent.hasExtra(RUN)||intent.hasExtra(DEADLINE))){
                android.os.Bundle extras=intent.getExtras();Object run=extras.get(RUN),deadline=extras.get(DEADLINE);
                if(!(run instanceof String)||!(deadline instanceof Long)||!pending.run.equals(run)||pending.deadline!=((Long)deadline).longValue())return null;
            }
            return pending.alarm;
        }catch(RuntimeException invalid){return null;}
    }
    /** Rechecks the source immediately before the existing successful started() consumption. */
    static Pending continuation(Context context,Alarm alarm){
        Alarm admitted=admit(context,alarm.id,null,System.currentTimeMillis());
        return admitted!=null&&admitted.json().toString().equals(alarm.json().toString())?owned(Prefs.of(context),alarm.id):null;
    }
    private static boolean source(Context context,SharedPreferences prefs,Pending pending){
        if(pending.alarm.id>0){try(Store store=Records.open(context)){return SnoozeSource.same(prefs,pending.alarm,store.alarm(pending.alarm.id));}}
        // The existing -1 preview alarm is separate from positive saved alarms. Keep all quiet caps.
        Alarm a=pending.alarm;
        if(a.id!=-1||a.volume<0||a.volume>25||a.rampSeconds!=0||a.awakeCheck!=0||a.pin||a.backupMinutes!=0||a.vibrate||a.extraLoud||a.sunrise||a.flashlight||a.soundDelaySeconds!=0||a.inputsInvalid||!AlarmInputs.DEFAULT.equals(a.inputs)||a.soundModeInvalid||!AlarmSoundMode.SINGLE.equals(a.soundMode)||a.timingInvalid||!AlarmTiming.DEFAULT.equals(a.timing))return false;
        try{return RandomChallenges.configured(prefs,a).json().toString().equals(Alarm.read(new JSONObject(prefs.getString("test_alarm","{}"))).json().toString());}catch(Exception invalid){return false;}
    }
    private SnoozeDelivery(){}
}
