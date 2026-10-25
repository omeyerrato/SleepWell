package org.sleepwell.app;
import org.json.*;
/** Additive per-task model/target identity; cannot silently downgrade an unknown model. */
final class ObjectTargetJson {
 static JSONObject encode(ObjectTarget target)throws JSONException{return new JSONObject().put("version",1).put("model",ObjectTarget.MODEL_ID).put("index",target.index);}
 static ObjectTarget decode(Object raw)throws JSONException{if(!(raw instanceof JSONObject j)||j.length()!=3||SensorProvenance.whole(j,"version")!=1||!ObjectTarget.MODEL_ID.equals(SensorProvenance.text(j,"model")))throw new JSONException("Unsupported object recognition settings");long index=SensorProvenance.whole(j,"index");if(index<0||index>999)throw new JSONException("Invalid model object index");try{return new ObjectTarget((int)index);}catch(IllegalArgumentException invalid){throw new JSONException(invalid.getMessage());}}
 private ObjectTargetJson(){}
}
