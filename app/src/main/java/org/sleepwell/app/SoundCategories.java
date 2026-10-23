package org.sleepwell.app;

import java.util.Set;

/** Stored categories are sound estimates, never a diagnosis or a person's identity. */
final class SoundCategories {
    static final Set<String> VALUES=Set.of("snoring","speech","cough","baby_cry","laughter","noise");
    static boolean valid(String category){return category!=null&&VALUES.contains(category);}
    static String title(String category){switch(category){case "snoring":return "Snoring estimate";case "speech":return "Speech estimate";case "cough":return "Cough / throat clear estimate";case "baby_cry":return "Baby crying estimate";case "laughter":return "Laughter estimate";default:return "Other sound";}}
    private SoundCategories(){}
}
