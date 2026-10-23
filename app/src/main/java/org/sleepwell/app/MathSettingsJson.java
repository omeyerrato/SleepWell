package org.sleepwell.app;

import org.json.*;

/** Strict, additive per-task configuration. No JSON coercion or silent downgrade of bad settings. */
final class MathSettingsJson {
    static JSONObject encode(MathSettings settings)throws JSONException{JSONArray operators=new JSONArray();for(MathSettings.Operator op:settings.enabled())operators.put(op.key);return new JSONObject().put("level",settings.level.key()).put("operators",operators).put("answer",settings.typed?"typed":"choices");}
    static MathSettings decode(Object value)throws JSONException{
        if(!(value instanceof JSONObject json)||json.length()!=3)throw new JSONException("Math settings require complexity, operators and answer mode.");
        Object grade=json.get("level"),answer=json.get("answer"),operators=json.get("operators");
        MathSettings.Level level=null;for(MathSettings.Level candidate:MathSettings.Level.values())if(candidate.key().equals(grade))level=candidate;
        if(level==null||!(answer instanceof String)||!answer.equals("typed")&&!answer.equals("choices")||!(operators instanceof JSONArray array)||array.length()<1||array.length()>4)throw new JSONException("Invalid arithmetic complexity, operators or answer mode.");
        int mask=0;for(int n=0;n<array.length();n++){Object name=array.get(n);MathSettings.Operator selected=null;for(MathSettings.Operator op:MathSettings.Operator.values())if(op.key.equals(name))selected=op;if(selected==null||(mask&selected.bit)!=0)throw new JSONException("Choose unique supported arithmetic operators.");mask|=selected.bit;}
        return new MathSettings(level,mask,answer.equals("typed"));
    }
    private MathSettingsJson(){}
}
