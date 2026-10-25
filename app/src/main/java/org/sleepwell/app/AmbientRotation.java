package org.sleepwell.app;

import java.util.*;
import org.json.*;

/** Time-based rotation of this app's existing bundled loops; no natural-duration or sleep claim. */
final class AmbientRotation {
    static final String NAME="Ambient rotation";
    static final List<String> SOURCES=List.of("Rain","Ocean","Fireplace","Wind","Brown noise");
    static final AmbientRotation DEFAULT=new AmbientRotation(List.of("Rain","Ocean","Fireplace","Wind"),5);
    final List<String> sounds;final int minutes;
    AmbientRotation(List<String> sounds,int minutes){if(sounds==null||sounds.size()<2||sounds.size()>SOURCES.size()||!Set.of(5,10,15).contains(minutes))throw new IllegalArgumentException("Choose two to five ambient sounds and a 5, 10 or 15 minute interval.");Set<String> unique=new HashSet<>();for(String sound:sounds)if(!SOURCES.contains(sound)||!unique.add(sound))throw new IllegalArgumentException("Choose each supported ambient sound at most once.");this.sounds=List.copyOf(sounds);this.minutes=minutes;}
    int index(long playingMillis){return (int)(Math.max(0,playingMillis)/(minutes*60000L)%sounds.size());}
    String encode(){try{return new JSONObject().put("version",1).put("minutes",minutes).put("sounds",new JSONArray(sounds)).toString();}catch(JSONException impossible){throw new IllegalStateException(impossible);}}
    String identity(){return "ambient:"+encode();}
    static AmbientRotation read(String encoded){if(encoded==null||encoded.length()>1024)throw new IllegalArgumentException("Saved ambient rotation is unreadable.");if(encoded.isEmpty())return DEFAULT;try{JSONObject value=new JSONObject(encoded);if(value.length()!=3||!(value.get("version") instanceof Number)||((Number)value.get("version")).doubleValue()!=1||!(value.get("minutes") instanceof Number))throw new IllegalArgumentException();double minutes=((Number)value.get("minutes")).doubleValue();if(!Double.isFinite(minutes)||minutes!=Math.rint(minutes)||minutes<5||minutes>15)throw new IllegalArgumentException();JSONArray selected=value.getJSONArray("sounds");if(selected.length()>SOURCES.size())throw new IllegalArgumentException();List<String> sounds=new ArrayList<>();for(int i=0;i<selected.length();i++){if(!(selected.get(i) instanceof String))throw new IllegalArgumentException();sounds.add(selected.getString(i));}return new AmbientRotation(sounds,(int)minutes);}catch(JSONException|IllegalArgumentException invalid){throw new IllegalArgumentException("Saved ambient rotation is unreadable. Choose its sounds again.");}}
}
