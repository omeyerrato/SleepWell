package org.sleepwell.app;

import android.content.*;
import org.json.*;
import java.util.*;

/** Original timed-cue settings, separate from any sleep-stage or lucid-dream claim. */
final class DreamCueSettings {
    static final Object LOCK=new Object();static final String FILE="timed_dream_cues";static final int[] HOURS={2,3,4,5,6},INTERVALS={20,30,45,60,90};
    final boolean enabled;final int hours,minutes,count;final long revision;
    DreamCueSettings(boolean enabled,int hours,int minutes,int count,long revision){if(Arrays.stream(HOURS).noneMatch(n->n==hours)||Arrays.stream(INTERVALS).noneMatch(n->n==minutes)||count<1||count>6||revision<0)throw new IllegalArgumentException("Review timed dream-cue settings.");if(enabled&&hours*3600000L+(count-1)*minutes*60000L>=12*3600000L)throw new IllegalArgumentException("Choose a schedule whose last cue is before the 12-hour recording limit.");this.enabled=enabled;this.hours=hours;this.minutes=minutes;this.count=count;this.revision=revision;}
    static DreamCueSettings load(Context c){SharedPreferences p=c.getSharedPreferences(FILE,Context.MODE_PRIVATE);String value=p.getString("settings","");if(value.isEmpty())return new DreamCueSettings(false,4,30,3,0);try{JSONObject j=new JSONObject(value);if(j.length()!=5)throw new JSONException("Unexpected settings");return new DreamCueSettings(bool(j,"enabled"),Math.toIntExact(SensorProvenance.whole(j,"hours")),Math.toIntExact(SensorProvenance.whole(j,"minutes")),Math.toIntExact(SensorProvenance.whole(j,"count")),SensorProvenance.whole(j,"revision"));}catch(Exception invalid){throw new IllegalStateException("Timed-cue settings are unreadable. No cues were armed.",invalid);}}
    static DreamCueSettings save(Context c,DreamCueSettings draft,long expected){synchronized(LOCK){DreamCueSettings old=load(c);if(old.revision!=expected)throw new IllegalStateException("Settings changed. Reload before saving.");if(old.enabled==draft.enabled&&old.hours==draft.hours&&old.minutes==draft.minutes&&old.count==draft.count)return old;if(old.revision==Long.MAX_VALUE)throw new IllegalStateException("Settings revision is exhausted.");DreamCueSettings next=new DreamCueSettings(draft.enabled,draft.hours,draft.minutes,draft.count,old.revision+1);try{String json=new JSONObject().put("enabled",next.enabled).put("hours",next.hours).put("minutes",next.minutes).put("count",next.count).put("revision",next.revision).toString();if(!c.getSharedPreferences(FILE,Context.MODE_PRIVATE).edit().putString("settings",json).commit())throw new IllegalStateException("Timed-cue settings could not be saved.");return next;}catch(JSONException impossible){throw new IllegalStateException(impossible);}}}
    long first(){return hours*3600000L;}long interval(){return minutes*60000L;}long due(int ordinal){if(ordinal<0||ordinal>=count)throw new IllegalArgumentException("Unknown cue");return first()+ordinal*interval();}
    private static boolean bool(JSONObject value,String key)throws JSONException{Object raw=value.get(key);if(!(raw instanceof Boolean))throw new JSONException("Invalid cue setting");return (Boolean)raw;}
}
