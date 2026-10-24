package org.sleepwell.app;
import java.util.*;
/** Immutable temporal acceptance only. Model scores/margins and original evidence stay unchanged. */
final class SoundSensitivity {
 static final List<String> CATEGORIES=List.of("snoring","speech","cough","baby_cry","laughter");
 static final SoundSensitivity DEFAULT=new SoundSensitivity(Map.of("snoring",2,"speech",2,"cough",2,"baby_cry",2,"laughter",2));
 private final Map<String,Integer> windows;
 SoundSensitivity(Map<String,Integer> values){if(values==null||!values.keySet().equals(new HashSet<>(CATEGORIES)))throw new IllegalArgumentException("Choose a duration for each sound class");Map<String,Integer> copy=new LinkedHashMap<>();for(String key:CATEGORIES){Integer value=values.get(key);if(value==null||value<1||value>3)throw new IllegalArgumentException("Choose one to three accepted windows");copy.put(key,value);}windows=Collections.unmodifiableMap(copy);}
 int windows(String category){if(category.equals("noise"))return 2;Integer value=windows.get(category);if(value==null)throw new IllegalArgumentException("Unknown sound category");return value;}
 SoundSensitivity with(String key,int minimum){Map<String,Integer> copy=new LinkedHashMap<>(windows);if(!copy.containsKey(key))throw new IllegalArgumentException("Unknown sound category");copy.put(key,minimum);return new SoundSensitivity(copy);}
 static String label(String key){return switch(key){case "snoring"->"Snoring";case "speech"->"Speech";case "cough"->"Cough / throat clear";case "baby_cry"->"Baby crying";case "laughter"->"Laughter";default->throw new IllegalArgumentException("Unknown sound category");};}
 static String choice(int windows){if(windows<1||windows>3)throw new IllegalArgumentException("Invalid duration");return "About "+windows+" sec · "+(windows==1?"High":windows==2?"Default":"Low");}
 @Override public boolean equals(Object other){return other instanceof SoundSensitivity&&windows.equals(((SoundSensitivity)other).windows);}
 @Override public int hashCode(){return windows.hashCode();}
}
