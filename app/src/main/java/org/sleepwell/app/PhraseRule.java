package org.sleepwell.app;

import java.text.Normalizer;
import java.util.Locale;

/** Original fixed prompts. Exact normalized text comparison, never speaker or liveness verification. */
final class PhraseRule {
    static final String TYPE="Spoken phrase";
    private static final String[] PHRASES={"Good morning I am awake","Today begins with a fresh start","I am ready to leave my bed"};
    static void rounds(int count){if(count<1||count>3)throw new IllegalArgumentException("Choose 1–3 spoken phrase rounds.");}
    static String target(String run,int unit){if(run==null||run.isBlank()||run.length()>128||unit<0)throw new IllegalArgumentException("Invalid phrase task.");return PHRASES[(int)Math.floorMod((long)run.hashCode()+unit,(long)PHRASES.length)];}
    static String normalize(String text){if(text==null||text.length()>240)return "";String normalized=Normalizer.normalize(text,Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);if(!normalized.matches("[a-z\\s\\p{Punct}]*"))return "";return normalized.replaceAll("[\\p{Punct}]"," ").strip().replaceAll("\\s+"," ");}
    static boolean matches(String target,String heard){String expected=normalize(target);return !expected.isEmpty()&&expected.equals(normalize(heard));}
    private PhraseRule(){}
}
