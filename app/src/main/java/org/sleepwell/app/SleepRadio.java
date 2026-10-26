package org.sleepwell.app;

import java.util.*;
import org.json.*;

/** Named user-entered public streams. Configuration never authorizes a network connection. */
final class SleepRadio {
    static final String NAME="Internet radio";static final int LIMIT=12;static final SleepRadio EMPTY=new SleepRadio(List.of(),"");
    static final class Station {
        final String id,name,url;
        Station(String id,String name,String url){if(id==null||!BackupRuntime.nonce(id)||name==null||name.isBlank()||!name.equals(name.strip())||name.length()>80||name.matches("(?s).*[\\p{Cc}\\p{Cf}\\p{Zl}\\p{Zp}].*"))throw new IllegalArgumentException("Choose a plain station name of 1–80 characters.");this.id=id;this.name=name;this.url=AlarmRadioUrl.parse(url);String path=java.net.URI.create(this.url).getPath();if(path!=null){String lower=path.toLowerCase(Locale.ROOT);for(String suffix:List.of(".m3u",".m3u8",".pls",".asx",".xspf",".mpd"))if(lower.endsWith(suffix))throw new IllegalArgumentException("Use a direct audio stream, not a playlist or adaptive-stream manifest.");}}
    }
    final List<Station> stations;final String selected;
    SleepRadio(List<Station> stations,String selected){if(stations==null||stations.size()>LIMIT||selected==null)throw new IllegalArgumentException("Save up to twelve radio stations.");Set<String> ids=new HashSet<>(),urls=new HashSet<>();for(Station station:stations)if(station==null||!ids.add(station.id)||!urls.add(station.url))throw new IllegalArgumentException("Each station and stream URL must be unique.");if(!selected.isEmpty()&&!ids.contains(selected))throw new IllegalArgumentException("Choose a saved radio station.");this.stations=List.copyOf(stations);this.selected=selected;}
    Station station(){return stations.stream().filter(s->s.id.equals(selected)).findFirst().orElse(null);}
    SleepRadio select(String id){return new SleepRadio(stations,id);}
    SleepRadio put(Station value){List<Station> next=new ArrayList<>(stations);int index=-1;for(int n=0;n<next.size();n++)if(next.get(n).id.equals(value.id))index=n;if(index<0)next.add(value);else next.set(index,value);return new SleepRadio(next,selected);}
    SleepRadio remove(String id){List<Station> next=new ArrayList<>(stations);next.removeIf(s->s.id.equals(id));return new SleepRadio(next,selected.equals(id)?"":selected);}
    String encode(){try{JSONArray values=new JSONArray();for(Station station:stations)values.put(new JSONObject().put("id",station.id).put("name",station.name).put("url",station.url));return new JSONObject().put("version",1).put("selected",selected).put("stations",values).toString();}catch(JSONException impossible){throw new IllegalStateException(impossible);}}
    static SleepRadio read(String text){if(text==null||text.length()>32768)throw new IllegalArgumentException("Saved radio stations are unreadable.");if(text.isEmpty())return EMPTY;try{JSONObject root=new JSONObject(text);if(root.length()!=3||SensorProvenance.whole(root,"version")!=1||!(root.get("selected") instanceof String))throw new JSONException("Invalid station library");JSONArray values=root.getJSONArray("stations");if(values.length()>LIMIT)throw new JSONException("Too many stations");List<Station> stations=new ArrayList<>();for(int n=0;n<values.length();n++){JSONObject value=values.getJSONObject(n);if(value.length()!=3)throw new JSONException("Invalid station");stations.add(new Station(SensorProvenance.text(value,"id"),SensorProvenance.text(value,"name"),SensorProvenance.text(value,"url")));}return new SleepRadio(stations,root.getString("selected"));}catch(JSONException|IllegalArgumentException invalid){throw new IllegalArgumentException("Saved radio stations are unreadable. No stream was opened.");}}
    String source(){Station value=station();if(value==null)throw new IllegalArgumentException("Choose a saved radio station first.");return "radio:"+value.url;}
}
