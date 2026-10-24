package org.sleepwell.app;
import org.json.*;
import java.time.*;
import java.util.*;
final class AlarmSkipJson {
 static JSONObject encode(AlarmSkip skip){try{return new JSONObject().put("version",1).put("nonce",skip.nonce).put("date",skip.date.toString()).put("hour",skip.hour).put("minute",skip.minute).put("days",new JSONArray(skip.days)).put("zone",skip.zone);}catch(JSONException impossible){throw new IllegalStateException(impossible);}}
 static AlarmSkip decode(JSONObject j)throws JSONException{
  Set<String> keys=Set.of("version","nonce","date","hour","minute","days","zone");if(j.length()!=keys.size())throw new JSONException("Invalid skip fields");for(Iterator<String> it=j.keys();it.hasNext();)if(!keys.contains(it.next()))throw new JSONException("Unknown skip field");
  try{if(integer(j,"version")!=1)throw new IllegalArgumentException();Object raw=j.get("days");if(!(raw instanceof JSONArray ds)||ds.length()>7)throw new IllegalArgumentException();Set<Integer> days=new TreeSet<>();for(int n=0;n<ds.length();n++){Object d=ds.get(n);if(!(d instanceof Integer)||!days.add((Integer)d))throw new IllegalArgumentException();}String date=string(j,"date");LocalDate parsed=LocalDate.parse(date);if(!parsed.toString().equals(date))throw new IllegalArgumentException();return new AlarmSkip(string(j,"nonce"),parsed,integer(j,"hour"),integer(j,"minute"),days,string(j,"zone"));}catch(RuntimeException invalid){throw new JSONException("Invalid skipped occurrence");}
 }
 private static int integer(JSONObject j,String key)throws JSONException{Object value=j.get(key);if(!(value instanceof Integer))throw new JSONException("Invalid skip integer");return (Integer)value;}
 private static String string(JSONObject j,String key)throws JSONException{Object value=j.get(key);if(!(value instanceof String))throw new JSONException("Invalid skip text");return (String)value;}
}
