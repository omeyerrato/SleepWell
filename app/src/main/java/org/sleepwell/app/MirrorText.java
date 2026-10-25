package org.sleepwell.app;
import java.nio.charset.StandardCharsets;import java.security.*;import java.text.Normalizer;import java.util.*;
/** Per-occurrence/unit stable phrase; display mirroring belongs to the native text view. */
final class MirrorText {
 static String phrase(String occurrence,int unit){if(occurrence==null||occurrence.isBlank()||unit<0)throw new IllegalArgumentException("Missing challenge ownership");try{byte[] digest=MessageDigest.getInstance("SHA-256").digest(("mirror-v1/"+occurrence+"/"+unit).getBytes(StandardCharsets.UTF_8));String[] first={"RISE","BREATHE","BEGIN","GREET","NOTICE","WELCOME","FOLLOW","CHOOSE"},second={"THE QUIET MORNING","A BRAND NEW DAY","THE SOFT SUNLIGHT","A CALM CLEAR MIND","THE BRIGHT HORIZON","A FRESH START","THE EARLY LIGHT","THE WAKING WORLD"};return first[(digest[0]&255)%first.length]+" "+second[(digest[1]&255)%second.length]+" "+String.format(Locale.ROOT,"%04d",((digest[2]&255)*256+(digest[3]&255))%10000);}catch(NoSuchAlgorithmException impossible){throw new AssertionError(impossible);}}
 static boolean accepts(String phrase,String typed){return typed!=null&&Normalizer.normalize(typed.trim(),Normalizer.Form.NFC).equals(phrase);}
 private MirrorText(){}
}
