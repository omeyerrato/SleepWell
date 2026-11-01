package org.sleepwell.app;

import android.content.Context;
import java.time.*;
import java.util.*;
import org.json.*;

/** Internal proposal only. The existing alarm editor still owns its final Save. */
final class TripAlarmShift {
    static final String EXTRA="tripAlarmShift";
    final String plan,zone,alarm;
    final long target,source,last;
    private TripAlarmShift(String plan,String zone,String alarm,long target,long source,long last){
        this.plan=plan;this.zone=zone;this.alarm=alarm;this.target=target;this.source=source;this.last=last;
    }
    static TripAlarmShift prepare(Context context,TripSleepPlan plan,Alarm alarm,long target){try(Store store=Records.open(context)){return prepare(context,plan,alarm,target,store);}}
    static TripAlarmShift prepare(Context context,TripSleepPlan plan,Alarm alarm,long target,Store store){
        try{
            ZoneId zone=ZoneId.systemDefault();long now=System.currentTimeMillis();
            if(alarm.nextOverride!=null||alarm.nextOverrideInvalid)throw new IllegalArgumentException("Review or undo the existing next-time override before applying a travel shift.");
            if(alarm.skip!=null||alarm.skipInvalid)throw new IllegalArgumentException("Undo the saved skipped occurrence before applying a travel shift.");
            long source=NextAlarmOverrides.source(context,alarm,now,zone);
            TripAlarmShift result=new TripAlarmShift(plan.json().toString(),zone.getId(),alarm.json().toString(),target,source,Prefs.of(context).getLong("lastOccurrence_"+alarm.id,0));
            result.check(context,alarm,store);return result;
        }catch(JSONException invalid){throw new IllegalArgumentException("The travel proposal could not be prepared.",invalid);}
    }
    JSONObject json(){try{return new JSONObject().put("version",1).put("plan",plan).put("zone",zone).put("alarm",alarm).put("target",target).put("source",source).put("last",last);}catch(JSONException invalid){throw new IllegalArgumentException(invalid);}}
    static TripAlarmShift read(String raw){
        try{JSONObject value=new JSONObject(raw);Set<String> fields=new HashSet<>();value.keys().forEachRemaining(fields::add);
            if(!fields.equals(Set.of("version","plan","zone","alarm","target","source","last"))||SensorProvenance.whole(value,"version")!=1)throw new JSONException("Unknown proposal");
            return new TripAlarmShift(SensorProvenance.text(value,"plan"),SensorProvenance.text(value,"zone"),SensorProvenance.text(value,"alarm"),SensorProvenance.whole(value,"target"),SensorProvenance.whole(value,"source"),SensorProvenance.whole(value,"last"));
        }catch(JSONException invalid){throw new IllegalArgumentException("This travel alarm proposal is unreadable. Choose the occurrence again.",invalid);}
    }
    void check(Context context,Alarm original){try(Store store=Records.open(context)){check(context,original,store);}}
    void check(Context context,Alarm original,Store store){
        try{
            long now=System.currentTimeMillis();ZoneId current=ZoneId.systemDefault();
            if(!current.getId().equals(zone)||target<=now+60000)throw new IllegalArgumentException("The phone clock or time zone changed, or the planned wake time passed. Choose it again.");
            LocalDate today=Instant.ofEpochMilli(now).atZone(current).toLocalDate();if(Instant.ofEpochMilli(target).atZone(current).toLocalDate().isAfter(today.plusDays(366))||Instant.ofEpochMilli(source).atZone(current).toLocalDate().isAfter(today.plusDays(366)))throw new IllegalArgumentException("Review this travel occurrence within the next 366 days.");
            TripSleepPlan saved=new TripSleepStore(context).load();
            if(saved==null||!saved.json().toString().equals(plan)||!saved.matches(TravelPlans.repository(context).load(now))||saved.nights().stream().noneMatch(night->night.wake==target))throw new IllegalArgumentException("The saved trip or sleep plan changed. Review it before applying a shift.");
            if(!original.json().toString().equals(alarm))throw new IllegalArgumentException("This alarm draft changed. Choose the travel occurrence again.");
            Alarm currentAlarm=store.alarm(original.id);if(currentAlarm==null||!currentAlarm.json().toString().equals(alarm))throw new IllegalArgumentException("The saved alarm changed. Reopen its travel occurrence.");
            if(original.nextOverride!=null||original.nextOverrideInvalid||source<=now||original.skip!=null||original.skipInvalid||Prefs.of(context).getLong("lastOccurrence_"+original.id,0)!=last||NextAlarmOverrides.source(context,original,now,current)!=source)throw new IllegalArgumentException("The next saved alarm occurrence changed. Review it again.");
            value(original).check(original,current);
        }catch(JSONException invalid){throw new IllegalArgumentException("The travel plan could not be checked.",invalid);}
    }
    NextAlarmOverride value(Alarm original){
        ZoneId current=ZoneId.of(zone);ZonedDateTime local=Instant.ofEpochMilli(target).atZone(current);
        if(AlarmDate.at(local.toLocalDate(),local.getHour(),local.getMinute(),current)!=target||current.getRules().getValidOffsets(local.toLocalDateTime()).size()!=1)throw new IllegalArgumentException("The planned wake time is repeated or missing in the phone time zone. Review another time.");
        return new NextAlarmOverride(UUID.randomUUID().toString(),NextAlarmOverride.baseKey(original),Instant.ofEpochMilli(source).atZone(current).toLocalDate(),local.toLocalDate(),local.getHour(),local.getMinute(),last,zone);
    }
    void checkDraft(Context context,Alarm original,Alarm candidate){try(Store store=Records.open(context)){checkDraft(context,original,candidate,store);}}
    void checkDraft(Context context,Alarm original,Alarm candidate,Store store){
        check(context,original,store);NextAlarmOverride value=candidate.nextOverride;
        if(value==null||value.at(ZoneId.of(zone))!=target||value.sourceAt(candidate,ZoneId.of(zone))!=source||value.last!=last)throw new IllegalArgumentException("The reviewed occurrence no longer matches the planned travel shift.");
        NextAlarmOverrides.draft(context,candidate,original);
    }
}
