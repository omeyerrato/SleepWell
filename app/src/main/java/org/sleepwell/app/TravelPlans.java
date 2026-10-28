package org.sleepwell.app;

import android.content.*;
import org.json.*;
import java.time.*;

/** All trip state lives in one atomic private preference file; reading does not persist defaults. */
final class TravelPlans {
    static final Object LOCK=new Object();
    interface Factory{SharedPreferences open(Context context);}
    static Factory factory=Prefs::of;
    static Repository repository(Context c){return new Repository(factory.open(c));}
    static final class Repository {
        final SharedPreferences prefs;
        Repository(SharedPreferences prefs){this.prefs=prefs;}
        TravelPlan load(long now){
            String raw=prefs.getString("travelPlan","");if(raw.isEmpty())return new TravelPlan(ZoneId.systemDefault().getId(),Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate());
            try{JSONObject j=new JSONObject(raw);if(j.getInt("version")!=1)throw new IllegalStateException("Unknown travel plan version");TravelPlan p=new TravelPlan(j.getString("home"),LocalDate.parse(j.getString("departure")));p.destination=j.getString("destination");p.minute=j.getInt("minute");p.days=j.getInt("days");p.reminders=j.getBoolean("reminders");if(j.has("trackingReminders")&&!(j.get("trackingReminders") instanceof Boolean))throw new JSONException("Invalid tracking reminder preference");p.trackingReminders=j.optBoolean("trackingReminders",false);p.revision=j.getLong("revision");p.validate();return p;}catch(Exception e){throw new IllegalStateException("The saved travel plan could not be read. Existing data was kept.",e);}
        }
        void save(TravelPlan draft,long now){save(draft,null,now);}
        void save(TravelPlan draft,TravelCuePlan cues,long now){synchronized(LOCK){TravelPlan current=load(now);if(current.revision!=draft.revision)throw new IllegalArgumentException("Your trip changed while this page was open. Reopen it before saving.");draft.validate();TravelPlan copy=draft.copy();copy.revision=Math.max(Math.addExact(current.revision,1),now);
            try{JSONObject j=new JSONObject().put("version",1).put("home",copy.home).put("destination",copy.destination).put("departure",copy.departure.toString()).put("minute",copy.minute).put("days",copy.days).put("reminders",copy.reminders).put("trackingReminders",copy.trackingReminders).put("revision",copy.revision);SharedPreferences.Editor edit=prefs.edit().putString("travelPlan",j.toString()).putLong("travelDeliveredAt",0);
                if(cues!=null||prefs.contains(TravelCueStore.CONFIG)){TravelCuePlan old=new TravelCueStore(prefs).load(current,now);TravelCuePlan proposed=cues;if(proposed==null)proposed=new TravelCuePlan(old.revision,draft.revision,false,false,draft.home,draft.destination,old.first,old.minute,old.minutes,old.nights);if(proposed.revision!=old.revision)throw new IllegalArgumentException("Light-cue settings changed. Reopen the trip before saving.");proposed.validateTrip(draft);TravelTrackingReminders.validate(draft,proposed,now);TravelCuePlan saved=proposed.bind(copy,Math.max(Math.addExact(old.revision,1),now));edit.putString(TravelCueStore.CONFIG,TravelCueJson.encode(saved).toString());}
                if(copy.trackingReminders&&cues==null&&!prefs.contains(TravelCueStore.CONFIG))throw new IllegalArgumentException("Choose a light-cue window before enabling tracking reminders.");commit(edit);draft.revision=copy.revision;
            }catch(JSONException e){throw new IllegalStateException(e);}
        }}
        long delivered(){return prefs.getLong("travelDeliveredAt",0);}
        TravelReminders.Request pending(){long at=prefs.getLong("travelPendingAt",0);return at<=0?null:new TravelReminders.Request(prefs.getLong("travelPendingRevision",0),at);}
        String error(){String scheduling=prefs.getString("travelReminderError","");if(!scheduling.isEmpty())return scheduling;String delivery=prefs.getString("travelDeliveryError","");return delivery.isEmpty()?prefs.getString("travelTrackingReminderError",""):delivery;}
        void pending(TravelReminders.Request request,String error){SharedPreferences.Editor e=prefs.edit().putString("travelReminderError",error);if(request==null)e.remove("travelPendingAt").remove("travelPendingRevision");else e.putLong("travelPendingAt",request.at).putLong("travelPendingRevision",request.revision);commit(e);}
        void consumed(long at,String notice){commit(prefs.edit().putLong("travelDeliveredAt",at).putString("travelLastNotice",notice).remove("travelPendingAt").remove("travelPendingRevision"));}
        private void commit(SharedPreferences.Editor edit){if(!edit.commit())throw new IllegalStateException("Travel settings could not be stored. Try again.");}
    }
}
