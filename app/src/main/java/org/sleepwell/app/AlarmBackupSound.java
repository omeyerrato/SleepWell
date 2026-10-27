package org.sleepwell.app;

import org.json.*;
import java.util.Objects;

/** Portable built-in backup sound. Percent is the requested alarm level, not acoustic dB. */
final class AlarmBackupSound {
    static final java.util.List<String> TONES=java.util.List.of("daybreak","pulse","chime");
    static final AlarmBackupSound DEFAULT=new AlarmBackupSound("pulse",100);
    final String tone;final int volume;
    AlarmBackupSound(String tone,int volume){if(tone==null||!TONES.contains(tone)||volume<10||volume>100)throw new IllegalArgumentException("Choose a built-in backup sound and volume from 10 to 100%.");this.tone=tone;this.volume=volume;}
    AlarmBackupSound tone(String value){return new AlarmBackupSound(value,volume);}
    AlarmBackupSound volume(int value){return new AlarmBackupSound(tone,value);}
    JSONObject json(){try{return new JSONObject().put("tone",tone).put("volume",volume);}catch(JSONException impossible){throw new IllegalStateException(impossible);}}
    static AlarmBackupSound read(Object raw)throws JSONException{if(!(raw instanceof JSONObject object)||object.length()!=2||!(object.get("tone") instanceof String)||!(object.get("volume") instanceof Number number))throw new JSONException("Invalid backup sound");double volume=number.doubleValue();if(!Double.isFinite(volume)||volume!=Math.rint(volume)||volume<10||volume>100)throw new JSONException("Invalid backup volume");try{return new AlarmBackupSound(object.getString("tone"),(int)volume);}catch(IllegalArgumentException invalid){throw new JSONException(invalid.getMessage());}}
    @Override public boolean equals(Object other){return other instanceof AlarmBackupSound value&&tone.equals(value.tone)&&volume==value.volume;}
    @Override public int hashCode(){return Objects.hash(tone,volume);}
}
