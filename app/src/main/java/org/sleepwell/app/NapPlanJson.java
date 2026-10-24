package org.sleepwell.app;

import org.json.*;
import java.util.Set;

/** Optional alarm metadata; invalid imports fail validation before replacing user data. */
final class NapPlanJson {
 static JSONObject encode(NapPlan p)throws JSONException{return new JSONObject().put("version",1).put("nonce",p.nonce).put("device",p.device).put("startWall",p.startWall).put("startElapsed",p.startElapsed).put("boot",p.boot).put("minutes",p.minutes).put("deleteAfter",p.deleteAfter).put("phase",p.phase.name()).put("error",p.error);}
 static NapPlan decode(JSONObject j)throws JSONException{Set<String> keys=Set.of("version","nonce","device","startWall","startElapsed","boot","minutes","deleteAfter","phase","error");if(j.length()!=keys.size())throw new JSONException("Invalid nap fields");java.util.Iterator<String> it=j.keys();while(it.hasNext())if(!keys.contains(it.next()))throw new JSONException("Unknown nap field");try{if(integer(j,"version")!=1||!(j.get("deleteAfter") instanceof Boolean))throw new IllegalArgumentException();return new NapPlan(string(j,"nonce"),string(j,"device"),integer(j,"startWall"),integer(j,"startElapsed"),integer(j,"boot"),Math.toIntExact(integer(j,"minutes")),j.getBoolean("deleteAfter"),NapPlan.Phase.valueOf(string(j,"phase")),string(j,"error"));}catch(RuntimeException invalid){throw new JSONException("Invalid nap timer");}}
 private static long integer(JSONObject j,String key)throws JSONException{Object n=j.get(key);if(!(n instanceof Integer)&&!(n instanceof Long))throw new JSONException("Nap integer required");return ((Number)n).longValue();}
 private static String string(JSONObject j,String key)throws JSONException{Object value=j.get(key);if(!(value instanceof String))throw new JSONException("Nap text required");return (String)value;}
 private NapPlanJson(){}
}
