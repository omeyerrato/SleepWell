package org.sleepwell.app;

import org.json.*;

final class MorningCodeJson {
    static JSONObject encode(MorningCode code)throws JSONException{JSONObject value=new JSONObject().put("label",code.label).put("hash",code.hash).put("fallback",MathSettingsJson.encode(code.fallback));if(!MorningBarcode.QR.equals(code.format))value.put("format",code.format);return value;}
    static MorningCode decode(Object raw)throws JSONException{
        if(!(raw instanceof JSONObject value)||(value.length()!=3&&value.length()!=4)||!(value.get("label") instanceof String label)||!(value.get("hash") instanceof String hash))throw new JSONException("Invalid morning code settings.");
        String format=MorningBarcode.QR;if(value.length()==4){Object selected=value.get("format");if(!(selected instanceof String name)||!MorningBarcode.FORMATS.contains(name))throw new JSONException("Invalid barcode format.");format=name;}
        try{MorningCode code=new MorningCode(label,hash,MathSettingsJson.decode(value.get("fallback")),format);if(!code.label.equals(label))throw new JSONException("Morning code location must be trimmed.");return code;}catch(IllegalArgumentException invalid){throw new JSONException(invalid.getMessage());}
    }
    private MorningCodeJson(){}
}
