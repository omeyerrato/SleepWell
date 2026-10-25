package org.sleepwell.app;
import java.util.*;import org.json.*;
/** Projection keeps ignored evidence and original reasons; only accepted spans affect duration. */
final class AwakeData {
 static final AwakeData UNKNOWN=new AwakeData("","legacy_unknown","",0,List.of());
 static final class Event {final long id,start,end;final String reason;final boolean ignored;Event(long id,long start,long end,String reason,boolean ignored){this.id=id;this.start=start;this.end=end;this.reason=reason;this.ignored=ignored;}}
 final String policy,state,error;final long revision;final List<Event> events;
 AwakeData(String policy,String state,String error,long revision,List<Event> events){this.policy=policy;this.state=state;this.error=error;this.revision=revision;this.events=List.copyOf(events);}
 List<RecordIntervals.Span> accepted(){List<RecordIntervals.Span> spans=new ArrayList<>();for(Event e:events)if(!e.ignored)spans.add(new RecordIntervals.Span(e.start,e.end));return spans;}
 String summary(){if(state.equals("legacy_unknown"))return "Awake estimates unavailable for this older record.";if(state.equals("off"))return "Awake estimates were off.";if(state.equals("error"))return "Awake estimates incomplete: "+error;if(state.equals("interrupted"))return "Awake estimation was interrupted; saved estimates remain reviewable.";if(!error.isEmpty())return "Limited awake evidence: "+error;return events.isEmpty()?"No awake estimates saved. Missing signals do not prove sleep.":"Phone-based estimates · review or ignore anytime.";}
 String portable(){try{JSONObject j=new JSONObject().put("policy",policy).put("state",state).put("error",error).put("revision",revision);JSONArray a=new JSONArray();for(Event e:events)a.put(new JSONObject().put("id",e.id).put("start",e.start).put("end",e.end).put("reason",e.reason).put("ignored",e.ignored));return j.put("events",a).toString();}catch(JSONException impossible){throw new IllegalStateException(impossible);}}
 static AwakeData decode(String raw){if(raw==null||raw.isEmpty())return UNKNOWN;try{JSONObject j=new JSONObject(raw);List<Event> events=new ArrayList<>();JSONArray a=j.getJSONArray("events");for(int n=0;n<a.length();n++){JSONObject e=a.getJSONObject(n);events.add(new Event(e.getLong("id"),e.getLong("start"),e.getLong("end"),e.getString("reason"),e.getBoolean("ignored")));}return new AwakeData(j.getString("policy"),j.getString("state"),j.getString("error"),j.getLong("revision"),events);}catch(JSONException bad){return new AwakeData("","error","Saved awake details unavailable.",0,List.of());}}
 static String reason(String key){return key.equals("phone_use")?"Phone use":key.equals("bright_light")?"Sustained room light":"Alarm snoozed";}
}
