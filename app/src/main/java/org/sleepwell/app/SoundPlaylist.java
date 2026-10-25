package org.sleepwell.app;

import java.net.URI;
import java.util.*;
import org.json.*;

/** Ordered local documents, never URLs. A saved queue is not authorization to play. */
final class SoundPlaylist {
    static final String NAME="Local playlist";static final int LIMIT=8;static final long MAX_BYTES=100*1024*1024L;
    static final SoundPlaylist EMPTY=new SoundPlaylist(List.of());
    static final class Track {
        final String uri,title;final long bytes,duration;
        Track(String uri,String title,long bytes,long duration){try{URI parsed=URI.create(uri);if(uri.length()>2048||!"content".equals(parsed.getScheme())||parsed.getRawAuthority()==null||parsed.getRawAuthority().isBlank()||parsed.getRawFragment()!=null)throw new IllegalArgumentException();}catch(RuntimeException bad){throw new IllegalArgumentException("Choose a local audio document.");}if(title==null||title.isBlank()||title.length()>80||title.chars().anyMatch(Character::isISOControl)||bytes<1||bytes>MAX_BYTES||duration<1||duration>7*86400000L)throw new IllegalArgumentException("Invalid audio document metadata.");this.uri=uri;this.title=title;this.bytes=bytes;this.duration=duration;}
    }
    final List<Track> tracks;
    SoundPlaylist(List<Track> tracks){if(tracks==null||tracks.size()>LIMIT)throw new IllegalArgumentException("Choose up to eight audio files.");Set<String> unique=new HashSet<>();for(Track track:tracks)if(track==null||!unique.add(track.uri))throw new IllegalArgumentException("This file is already in your playlist.");this.tracks=List.copyOf(tracks);}
    SoundPlaylist add(Track track){List<Track> next=new ArrayList<>(tracks);next.add(track);return new SoundPlaylist(next);}
    SoundPlaylist remove(int index){List<Track> next=new ArrayList<>(tracks);next.remove(index);return new SoundPlaylist(next);}
    SoundPlaylist move(int from,int to){List<Track> next=new ArrayList<>(tracks);Track moved=next.remove(from);next.add(to,moved);return new SoundPlaylist(next);}
    String encode(){try{JSONArray array=new JSONArray();for(Track track:tracks)array.put(new JSONObject().put("uri",track.uri).put("title",track.title).put("bytes",track.bytes).put("duration",track.duration));return new JSONObject().put("version",1).put("tracks",array).toString();}catch(JSONException impossible){throw new IllegalStateException(impossible);}}
    static SoundPlaylist read(String encoded){if(encoded==null||encoded.length()>32768)throw new IllegalArgumentException("Invalid saved playlist.");if(encoded.isEmpty())return EMPTY;try{JSONObject root=new JSONObject(encoded);if(root.length()!=2||number(root.get("version"),1)!=1)throw new IllegalArgumentException();JSONArray array=root.getJSONArray("tracks");if(array.length()>LIMIT)throw new IllegalArgumentException();List<Track> tracks=new ArrayList<>();for(int i=0;i<array.length();i++){JSONObject row=array.getJSONObject(i);if(row.length()!=4||!(row.get("uri") instanceof String)||!(row.get("title") instanceof String))throw new IllegalArgumentException();tracks.add(new Track(row.getString("uri"),row.getString("title"),number(row.get("bytes"),MAX_BYTES),number(row.get("duration"),7*86400000L)));}return new SoundPlaylist(tracks);}catch(JSONException|IllegalArgumentException invalid){throw new IllegalArgumentException("Saved playlist is unreadable. Rebuild it before playing.");}}
    private static long number(Object value,long maximum){if(!(value instanceof Number))throw new IllegalArgumentException();double n=((Number)value).doubleValue();if(!Double.isFinite(n)||n!=Math.rint(n)||n<1||n>maximum)throw new IllegalArgumentException();return ((Number)value).longValue();}
    String identity(){return "playlist:"+encode();}
}
