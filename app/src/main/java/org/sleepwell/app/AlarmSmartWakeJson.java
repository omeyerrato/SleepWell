package org.sleepwell.app;

import org.json.*;

/** Strict optional alarm metadata. No strings, nulls, unknown fields or range coercion. */
final class AlarmSmartWakeJson {
    static JSONObject encode(AlarmSmartWake value){if(value==null)throw new IllegalArgumentException("Missing smart wake-up settings.");try{return new JSONObject().put("sensitivity",value.sensitivity).put("goal",value.goal);}catch(JSONException impossible){throw new IllegalStateException(impossible);}}
    static AlarmSmartWake decode(Object raw)throws JSONException{
        if(!(raw instanceof JSONObject object)||object.length()!=2||!object.has("sensitivity")||!object.has("goal"))throw new JSONException("Invalid smart wake-up settings");
        try{return new AlarmSmartWake(number(object,"sensitivity"),number(object,"goal"));}catch(IllegalArgumentException invalid){throw new JSONException(invalid.getMessage());}
    }
    private static int number(JSONObject object,String key)throws JSONException{Object raw=object.get(key);if(!(raw instanceof Number))throw new JSONException("Smart wake-up setting must be an integer");double value=((Number)raw).doubleValue();if(!Double.isFinite(value)||value!=Math.rint(value)||value<Integer.MIN_VALUE||value>Integer.MAX_VALUE)throw new JSONException("Invalid smart wake-up integer");return (int)value;}
}
