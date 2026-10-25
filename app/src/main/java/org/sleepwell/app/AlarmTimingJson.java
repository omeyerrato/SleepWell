package org.sleepwell.app;
import org.json.*;

final class AlarmTimingJson {
    static JSONObject encode(AlarmTiming timing){try{return new JSONObject().put("backupSeconds",timing.backupSeconds).put("backupRepeats",timing.backupRepeats).put("backupGapMinutes",timing.backupGapMinutes).put("timeoutMinutes",timing.timeoutMinutes);}catch(JSONException impossible){throw new IllegalStateException(impossible);}}
    static AlarmTiming decode(Object raw)throws JSONException{if(!(raw instanceof JSONObject object)||object.length()!=4)throw new JSONException("Invalid alarm timing");try{return new AlarmTiming(number(object,"backupSeconds"),number(object,"backupRepeats"),number(object,"backupGapMinutes"),number(object,"timeoutMinutes"));}catch(IllegalArgumentException invalid){throw new JSONException(invalid.getMessage());}}
    private static int number(JSONObject object,String key)throws JSONException{Object raw=object.get(key);if(!(raw instanceof Number))throw new JSONException("Timing must be an integer");double value=((Number)raw).doubleValue();if(!Double.isFinite(value)||value!=Math.rint(value)||value<0||value>Integer.MAX_VALUE)throw new JSONException("Invalid timing value");return (int)value;}
}
