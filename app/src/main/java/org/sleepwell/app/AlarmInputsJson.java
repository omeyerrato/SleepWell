package org.sleepwell.app;
import org.json.*;

final class AlarmInputsJson {
    static JSONObject encode(AlarmInputs settings){try{return new JSONObject().put("flip",settings.flip).put("keys",settings.keys.name());}catch(JSONException impossible){throw new IllegalStateException(impossible);}}
    static AlarmInputs decode(Object raw)throws JSONException{if(!(raw instanceof JSONObject object)||object.length()!=2||!(object.get("flip") instanceof Boolean)||!(object.get("keys") instanceof String))throw new JSONException("Invalid alarm input settings");try{return new AlarmInputs(object.getBoolean("flip"),AlarmInputs.Keys.valueOf(object.getString("keys")));}catch(IllegalArgumentException invalid){throw new JSONException("Choose a supported volume-key action");}}
}
