package org.sleepwell.app;

import org.json.*;import java.util.*;

final class SunriseScheduleJson {
    static JSONObject encode(SunriseSchedule schedule){try{return new JSONObject().put("version",1).put("latitude",schedule.latitude).put("longitude",schedule.longitude).put("offsetMinutes",schedule.offset).put("fallbackAcknowledged",true);}catch(JSONException impossible){throw new IllegalStateException(impossible);}}
    static SunriseSchedule decode(Object raw)throws JSONException{if(!(raw instanceof JSONObject value)||value.length()!=5)throw new JSONException("Invalid sunrise-time settings");Set<String> keys=Set.of("version","latitude","longitude","offsetMinutes","fallbackAcknowledged");for(Iterator<String> it=value.keys();it.hasNext();)if(!keys.contains(it.next()))throw new JSONException("Unknown sunrise-time field");try{Object version=value.get("version"),offset=value.get("offsetMinutes"),lat=value.get("latitude"),lon=value.get("longitude"),ack=value.get("fallbackAcknowledged");if(!(version instanceof Integer)||((Integer)version)!=1||!(offset instanceof Integer)||!(lat instanceof Number)||!(lon instanceof Number)||!(ack instanceof Boolean))throw new IllegalArgumentException();return new SunriseSchedule(((Number)lat).doubleValue(),((Number)lon).doubleValue(),(Integer)offset,(Boolean)ack);}catch(RuntimeException invalid){throw new JSONException("Invalid sunrise coordinates, offset or fallback acknowledgment");}}
}
