package org.sleepwell.app;

import org.json.*;
import java.util.Set;

/** Optional alarm metadata; invalid imports fail validation before replacing user data. */
final class NapPlanJson {
 static JSONObject encode(NapPlan p)throws JSONException{JSONObject j=new JSONObject().put("version",p.exactSeconds>0?2:1).put("nonce",p.nonce).put("device",p.device).put("startWall",p.startWall).put("startElapsed",p.startElapsed).put("boot",p.boot).put("minutes",p.minutes).put("deleteAfter",p.deleteAfter).put("phase",p.phase.name()).put("error",p.error);if(p.exactSeconds>0)j.put("exactSeconds",p.exactSeconds);if(p.power())j.put("minimumRestMinutes",p.minimumRestMinutes);return j;}
 static NapPlan decode(JSONObject j)throws JSONException{Set<String> keys=Set.of("version","nonce","device","startWall","startElapsed","boot","minutes","deleteAfter","phase","error");boolean power=j.has("minimumRestMinutes"),exact=j.has("exactSeconds");if(j.length()!=keys.size()+(power?1:0)+(exact?1:0))throw new JSONException("Invalid nap fields");java.util.Iterator<String> it=j.keys();while(it.hasNext()){String key=it.next();if(!keys.contains(key)&&!"minimumRestMinutes".equals(key)&&!"exactSeconds".equals(key))throw new JSONException("Unknown nap field");}try{if(power&&integer(j,"minimumRestMinutes")<1)throw new IllegalArgumentException();if(integer(j,"version")!=(exact?2:1)||exact&&(power||integer(j,"exactSeconds")<1)||!(j.get("deleteAfter") instanceof Boolean))throw new IllegalArgumentException();return new NapPlan(string(j,"nonce"),string(j,"device"),integer(j,"startWall"),integer(j,"startElapsed"),integer(j,"boot"),Math.toIntExact(integer(j,"minutes")),j.getBoolean("deleteAfter"),NapPlan.Phase.valueOf(string(j,"phase")),string(j,"error"),power?Math.toIntExact(integer(j,"minimumRestMinutes")):0,exact?Math.toIntExact(integer(j,"exactSeconds")):0);}catch(RuntimeException invalid){throw new JSONException("Invalid nap timer");}}
 private static long integer(JSONObject j,String key)throws JSONException{Object n=j.get(key);if(!(n instanceof Integer)&&!(n instanceof Long))throw new JSONException("Nap integer required");return ((Number)n).longValue();}
 private static String string(JSONObject j,String key)throws JSONException{Object value=j.get(key);if(!(value instanceof String))throw new JSONException("Nap text required");return (String)value;}
 private NapPlanJson(){}
}
