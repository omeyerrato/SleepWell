package org.sleepwell.app;

import android.content.*;
import org.json.*;

/** Private atomic configuration and delivery receipt; never rewrites alarms or sleep records. */
final class TripSleepStore {
    static final Object LOCK=TravelPlans.LOCK;
    final SharedPreferences prefs;
    TripSleepStore(Context c){prefs=c.getSharedPreferences("travel_sleep_adjustment_v1",Context.MODE_PRIVATE);}
    TripSleepPlan load(){String raw=prefs.getString("plan","");if(raw.isEmpty())return null;try{return TripSleepPlan.decode(new JSONObject(raw));}catch(Exception invalid){throw new IllegalStateException("Saved travel sleep settings could not be read. Existing data was kept.",invalid);}}
    void save(TripSleepPlan draft,TravelPlan trip){TripSleepPlan old=load();if((old==null?0:old.revision)!=draft.revision)throw new IllegalStateException("This travel sleep plan changed. Reopen it before saving.");if(!draft.matches(trip))throw new IllegalStateException("The trip changed. Review its dates before saving.");draft.nights();TripSleepPlan next=draft.copy();next.revision=Math.max(System.currentTimeMillis(),Math.addExact(draft.revision,1));try{commit(prefs.edit().putString("plan",next.json().toString()).putLong("delivered",0).putString("status","Plan saved").remove("error"));draft.revision=next.revision;}catch(JSONException invalid){throw new IllegalStateException(invalid);}}
    String pending(){return prefs.getString("pending","");}
    void pending(String value){SharedPreferences.Editor edit=prefs.edit();if(value.isEmpty())edit.remove("pending");else edit.putString("pending",value);commit(edit);}
    long delivered(){return prefs.getLong("delivered",0);}
    void consume(long at,String status){commit(prefs.edit().putLong("delivered",at).putString("status",status).putString("lastDelivery",status).remove("pending"));}
    void status(String value){commit(prefs.edit().putString("status",value));}
    void error(String value){commit(prefs.edit().putString("error",value));}
    String error(){return prefs.getString("error","");}
    static void commit(SharedPreferences.Editor edit){if(!edit.commit())throw new IllegalStateException("Travel sleep settings could not be saved.");}
}
