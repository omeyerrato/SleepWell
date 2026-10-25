package org.sleepwell.app;
import org.json.*;
import java.time.*;
import java.util.*;
final class AlarmSkipJson {
 static JSONObject encode(AlarmSkip skip){try{JSONObject value=new JSONObject().put("version",skip.solarKey.isEmpty()?1:2).put("nonce",skip.nonce).put("date",skip.date.toString()).put("hour",skip.hour).put("minute",skip.minute).put("days",new JSONArray(skip.days)).put("zone",skip.zone);if(!skip.solarKey.isEmpty())value.put("solarAt",skip.solarAt).put("solarKey",skip.solarKey);return value;}catch(JSONException impossible){throw new IllegalStateException(impossible);}}
 static AlarmSkip decode(JSONObject j)throws JSONException{
  int version=integer(j,"version");Set<String> keys=version==1?Set.of("version","nonce","date","hour","minute","days","zone"):version==2?Set.of("version","nonce","date","hour","minute","days","zone","solarAt","solarKey"):Set.of();if(j.length()!=keys.size())throw new JSONException("Invalid skip fields");for(Iterator<String> it=j.keys();it.hasNext();)if(!keys.contains(it.next()))throw new JSONException("Unknown skip field");
  try{if(version!=1&&version!=2)throw new IllegalArgumentException();Object raw=j.get("days");if(!(raw instanceof JSONArray ds)||ds.length()>7)throw new IllegalArgumentException();Set<Integer> days=new TreeSet<>();for(int n=0;n<ds.length();n++){Object d=ds.get(n);if(!(d instanceof Integer)||!days.add((Integer)d))throw new IllegalArgumentException();}String date=string(j,"date");LocalDate parsed=LocalDate.parse(date);if(!parsed.toString().equals(date))throw new IllegalArgumentException();long solarAt=version==2?absolute(j,"solarAt"):0;String solarKey=version==2?string(j,"solarKey"):"";if(version==2&&(solarAt<=0||solarKey.isEmpty()))throw new IllegalArgumentException();return new AlarmSkip(string(j,"nonce"),parsed,integer(j,"hour"),integer(j,"minute"),days,string(j,"zone"),solarAt,solarKey);}catch(RuntimeException invalid){throw new JSONException("Invalid skipped occurrence");}
 }
 private static long absolute(JSONObject j,String key)throws JSONException{Object value=j.get(key);if(!(value instanceof Long)&&!(value instanceof Integer))throw new JSONException("Invalid skip deadline");return ((Number)value).longValue();}
 private static int integer(JSONObject j,String key)throws JSONException{Object value=j.get(key);if(!(value instanceof Integer))throw new JSONException("Invalid skip integer");return (Integer)value;}
 private static String string(JSONObject j,String key)throws JSONException{Object value=j.get(key);if(!(value instanceof String))throw new JSONException("Invalid skip text");return (String)value;}
}
