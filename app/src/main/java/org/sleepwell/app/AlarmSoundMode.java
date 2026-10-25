package org.sleepwell.app;
import java.net.URI;
import java.util.*;

/** Alarm-only playback policy. The legacy single tone stays in Alarm.tone. */
final class AlarmSoundMode {
    enum Kind { SINGLE,SILENT,PLAYLIST,RADIO }
    static final AlarmSoundMode SINGLE=new AlarmSoundMode(Kind.SINGLE,List.of(),false), SILENT=new AlarmSoundMode(Kind.SILENT,List.of(),false);
    final String radioUrl;final Kind kind;final List<String> tracks;final boolean shuffle;final Map<String,String> names;
    AlarmSoundMode(Kind kind,List<String> tracks,boolean shuffle){this(kind,tracks,shuffle,Map.of());}
    AlarmSoundMode(Kind kind,List<String> tracks,boolean shuffle,Map<String,String> names){this(kind,tracks,shuffle,names,"");}
    AlarmSoundMode(Kind kind,List<String> tracks,boolean shuffle,Map<String,String> names,String radioUrl){if(radioUrl==null||kind!=Kind.RADIO&&!radioUrl.isEmpty())throw new IllegalArgumentException("Invalid radio source");this.radioUrl=kind==Kind.RADIO?AlarmRadioUrl.parse(radioUrl):"";if(kind==null||tracks==null||kind!=Kind.PLAYLIST&&(!tracks.isEmpty()||shuffle)||kind==Kind.PLAYLIST&&(tracks.size()<2||tracks.size()>8))throw new IllegalArgumentException("Choose two to eight playlist sounds");LinkedHashSet<String> unique=new LinkedHashSet<>();for(String track:tracks)if(!source(track)||!unique.add(track))throw new IllegalArgumentException("Invalid or repeated playlist sound");this.kind=kind;this.tracks=List.copyOf(tracks);this.shuffle=shuffle;Map<String,String> clean=new HashMap<>();if(names==null||names.size()>8)throw new IllegalArgumentException("Invalid sound names");for(Map.Entry<String,String> entry:names.entrySet()){String name=entry.getValue();if(!unique.contains(entry.getKey())||!entry.getKey().startsWith("content:")||name==null||name.isBlank()||name.length()>100||name.chars().anyMatch(Character::isISOControl))throw new IllegalArgumentException("Invalid sound name");clean.put(entry.getKey(),name);}this.names=Map.copyOf(clean);}
    static boolean source(String value){if(value==null||value.length()>2048)return false;if(Set.of("daybreak","pulse","chime","system").contains(value))return true;try{URI uri=URI.create(value);return "content".equals(uri.getScheme())&&uri.getRawAuthority()!=null&&!uri.getRawAuthority().isBlank()&&uri.getRawFragment()==null;}catch(IllegalArgumentException bad){return false;}}
    AlarmSoundMode changed(List<String> tracks,boolean shuffle){Map<String,String> retained=new HashMap<>(names);retained.keySet().retainAll(tracks);return new AlarmSoundMode(Kind.PLAYLIST,tracks,shuffle,retained);}
    AlarmSoundMode add(String source,String name){List<String> next=new ArrayList<>(tracks);next.add(source);Map<String,String> labels=new HashMap<>(names);if(source.startsWith("content:")&&name!=null&&!name.isBlank())labels.put(source,Alarm.cleanToneName(name));return new AlarmSoundMode(Kind.PLAYLIST,next,shuffle,labels);}
    String name(String source){return names.getOrDefault(source,AlarmTones.name(source));}
    static AlarmSoundMode radio(String url){return new AlarmSoundMode(Kind.RADIO,List.of(),false,Map.of(),AlarmRadioUrl.parse(url));}
    boolean radio(){return kind==Kind.RADIO;}
    boolean silent(){return kind==Kind.SILENT;}boolean playlist(){return kind==Kind.PLAYLIST;}
    boolean references(String uri){return tracks.contains(uri);}
    String title(){return radio()?"Internet radio":silent()?"Silent":playlist()?(shuffle?"Shuffled playlist":"Playlist")+" · "+tracks.size():"Single sound";}
    @Override public boolean equals(Object other){return other instanceof AlarmSoundMode that&&kind==that.kind&&shuffle==that.shuffle&&tracks.equals(that.tracks)&&names.equals(that.names)&&radioUrl.equals(that.radioUrl);}
    @Override public int hashCode(){return Objects.hash(kind,tracks,shuffle,names,radioUrl);}
}
