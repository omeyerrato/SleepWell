package org.sleepwell.app;

import org.json.*;

final class MorningCodeJson {
    static JSONObject encode(MorningCode code)throws JSONException{return new JSONObject().put("label",code.label).put("hash",code.hash).put("fallback",MathSettingsJson.encode(code.fallback));}
    static MorningCode decode(Object raw)throws JSONException{
        if(!(raw instanceof JSONObject value)||value.length()!=3||!(value.get("label") instanceof String label)||!(value.get("hash") instanceof String hash))throw new JSONException("Invalid morning code settings.");
        try{MorningCode code=new MorningCode(label,hash,MathSettingsJson.decode(value.get("fallback")));if(!code.label.equals(label))throw new JSONException("Morning code location must be trimmed.");return code;}catch(IllegalArgumentException invalid){throw new JSONException(invalid.getMessage());}
    }
    private MorningCodeJson(){}
}
