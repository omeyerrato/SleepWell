package org.sleepwell.app;
import java.text.Normalizer;import java.util.*;
/** Explicit engagement threshold; cannot determine whether an entry is true or meaningful. */
final class JournalRule {
 static final int DEFAULT=40,MAX=5000;static final Set<Integer> MINIMUMS=Set.of(40,80,120,160,240);
 static String problem(String text,int minimum){if(!MINIMUMS.contains(minimum))throw new IllegalArgumentException("Unsupported journal minimum");if(text==null||text.codePointCount(0,text.length())>MAX)return "Use at most 5,000 characters.";String normalized=Normalizer.normalize(text.trim(),Normalizer.Form.NFC);long letters=normalized.codePoints().filter(Character::isLetterOrDigit).count();String[] split=normalized.split("[^\\p{L}\\p{N}]+");int words=0;Set<String> distinct=new HashSet<>();for(String word:split)if(word.codePointCount(0,word.length())>=2){words++;distinct.add(word.toLowerCase(Locale.ROOT));}if(letters<minimum)return "Write at least "+minimum+" letters or digits.";if(words<8)return "Use at least 8 words of two or more characters.";if(distinct.size()<5)return "Use at least 5 different words.";return "";}
 static String owner(String run,int unit){if(run==null||run.isBlank()||unit<0)throw new IllegalArgumentException("Missing journal challenge ownership");return HealthSleepStore.hash("journal-v1/"+run+"/"+unit);}
 private JournalRule(){}
}
