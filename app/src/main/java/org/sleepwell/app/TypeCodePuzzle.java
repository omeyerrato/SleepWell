package org.sleepwell.app;
import java.util.*;
/** Stable original wake-up code, not a security token or an anti-OCR claim. */
final class TypeCodePuzzle {
 static final String ALPHABET="ACDEFHJKMNPQRTUVWXY3479";final String code;final long seed;final int level;
 static int length(int level){if(level<1||level>3)throw new IllegalArgumentException("Choose Easy, Medium or Hard");return new int[]{5,6,8}[level-1];}
 TypeCodePuzzle(String run,int unit,int level){this.level=level;seed=CountingPuzzle.seed(run,unit,0,"type-code-v1:"+level);Random random=new Random(seed);StringBuilder value=new StringBuilder();for(int n=0;n<length(level);n++)value.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));code=value.toString();}
 boolean accepts(String input){return input!=null&&code.equals(input.trim().toUpperCase(Locale.ROOT));}
}
