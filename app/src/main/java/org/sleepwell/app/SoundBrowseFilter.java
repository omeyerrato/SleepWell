package org.sleepwell.app;
import java.util.*;
/** Browsing uses chosen labels; it never changes or validates model recognition policy. */
final class SoundBrowseFilter {
 static final List<String> CATEGORIES=List.of("snoring","speech","cough","baby_cry","laughter","noise");
 static final List<String> ALL=List.of("all","snoring","speech","cough","baby_cry","laughter","noise");
 static String category(String raw){return raw!=null&&CATEGORIES.contains(raw)?raw:"noise";}
 static String filter(String raw,boolean favorites){return raw!=null&&(ALL.contains(raw)||favorites&&"favorites".equals(raw))?raw:"all";}
 static boolean matches(String effective,boolean favorite,String filter){return "all".equals(filter)||"favorites".equals(filter)&&favorite||category(effective).equals(filter);}
 static String name(String key){return switch(key){case "all"->"All";case "snoring"->"Snoring";case "speech"->"Speech";case "cough"->"Cough";case "baby_cry"->"Baby crying";case "laughter"->"Laughter";case "favorites"->"Favorites";default->"Other sounds";};}
 static boolean extended(String key){return !List.of("all","snoring","speech").contains(key);}
 private SoundBrowseFilter(){}
}
