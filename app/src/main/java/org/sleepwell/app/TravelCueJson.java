package org.sleepwell.app;
import org.json.*;
import java.time.*;
import java.util.*;
final class TravelCueJson {
 static JSONObject encode(TravelCuePlan p){try{return new JSONObject().put("version",1).put("revision",p.revision).put("tripRevision",p.tripRevision).put("enabled",p.enabled).put("reviewed",p.reviewed).put("home",p.home).put("destination",p.destination).put("first",p.first.toString()).put("minute",p.minute).put("minutes",p.minutes).put("nights",p.nights);}catch(JSONException impossible){throw new IllegalStateException(impossible);}}
 static TravelCuePlan decode(JSONObject j)throws JSONException{Set<String> keys=Set.of("version","revision","tripRevision","enabled","reviewed","home","destination","first","minute","minutes","nights");if(j.length()!=keys.size())throw new JSONException("Invalid cue fields");for(Iterator<String> it=j.keys();it.hasNext();)if(!keys.contains(it.next()))throw new JSONException("Unknown cue field");try{if(integer(j,"version")!=1)throw new IllegalArgumentException();String date=text(j,"first");LocalDate first=LocalDate.parse(date);if(!first.toString().equals(date))throw new IllegalArgumentException();return new TravelCuePlan(integer(j,"revision"),integer(j,"tripRevision"),bool(j,"enabled"),bool(j,"reviewed"),text(j,"home"),text(j,"destination"),first,Math.toIntExact(integer(j,"minute")),Math.toIntExact(integer(j,"minutes")),Math.toIntExact(integer(j,"nights")));}catch(RuntimeException invalid){throw new JSONException("Invalid light-cue configuration");}}
 static long integer(JSONObject j,String key)throws JSONException{Object v=j.get(key);if(!(v instanceof Integer)&&!(v instanceof Long))throw new JSONException("Invalid cue integer");return ((Number)v).longValue();}
 static String text(JSONObject j,String key)throws JSONException{Object v=j.get(key);if(!(v instanceof String))throw new JSONException("Invalid cue text");return (String)v;}
 static boolean bool(JSONObject j,String key)throws JSONException{Object v=j.get(key);if(!(v instanceof Boolean))throw new JSONException("Invalid cue flag");return (Boolean)v;}
}
