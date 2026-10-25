package org.sleepwell.app;
import org.json.*;
import java.util.*;

final class AlarmInputsJson {
    static JSONObject encode(AlarmInputs settings){try{JSONObject result=new JSONObject().put("flip",settings.flip).put("keys",settings.keys.name());if(settings.chooseSnooze)result.put("chooseSnooze",true);if(settings.lockOrientation)result.put("lockOrientation",true);return result;}catch(JSONException impossible){throw new IllegalStateException(impossible);}}
    static AlarmInputs decode(Object raw)throws JSONException{
        if(!(raw instanceof JSONObject object)||object.length()<2||object.length()>4)throw new JSONException("Invalid alarm input settings");
        Set<String> fields=Set.of("flip","keys","chooseSnooze","lockOrientation");for(Iterator<String> it=object.keys();it.hasNext();)if(!fields.contains(it.next()))throw new JSONException("Unknown alarm input setting");
        if(!(object.get("flip") instanceof Boolean)||!(object.get("keys") instanceof String)||object.has("chooseSnooze")&&!(object.get("chooseSnooze") instanceof Boolean)||object.has("lockOrientation")&&!(object.get("lockOrientation") instanceof Boolean))throw new JSONException("Invalid alarm input settings");
        try{return new AlarmInputs(object.getBoolean("flip"),AlarmInputs.Keys.valueOf(object.getString("keys")),object.optBoolean("chooseSnooze",false),object.optBoolean("lockOrientation",false));}catch(IllegalArgumentException invalid){throw new JSONException("Choose supported alarm input settings");}
    }
}
