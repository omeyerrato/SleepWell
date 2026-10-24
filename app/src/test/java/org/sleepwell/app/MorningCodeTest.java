package org.sleepwell.app;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
@org.junit.runner.RunWith(org.junit.runners.JUnit4.class)
public class MorningCodeTest {
 @Test public void hashIsExactUtf8NotTrimmedOrOpened(){String h=MorningCode.digest("bathroom:صباح الخير");assertEquals(64,h.length());assertEquals(h,MorningCode.digest("bathroom:صباح الخير"));assertFalse(MorningCode.same(h,MorningCode.digest("bathroom:صباح الخير ")));assertFalse(MorningCode.same(h,h.toUpperCase(Locale.ROOT)));assertFalse(MorningCode.same(h,null));assertTrue(MorningCode.validHash(MorningCode.digest("https://example.invalid/no-network")));}
 @Test public void payloadBoundsRejectEmptyMalformedAndExcessUtf8(){for(String bad:new String[]{null,""," ","\uD800","a".repeat(2049),"ش".repeat(1025)})assertThrows(IllegalArgumentException.class,()->MorningCode.digest(bad));assertTrue(MorningCode.validHash(MorningCode.digest("ش".repeat(1024))));}
 @Test public void labelAndFallbackAreBoundedAndCodeOwnsOnlyOneRoutineUnit(){MorningCode code=new MorningCode("  Bathroom  ",MorningCode.digest("morning"),new MathSettings(MathSettings.Level.HARD,8,true));assertEquals("Bathroom",code.label);for(String bad:new String[]{null," ","a".repeat(61),"Bath\nroom"})assertThrows(IllegalArgumentException.class,()->new MorningCode(bad,code.hash,code.fallback));assertThrows(IllegalArgumentException.class,()->new MorningCode("Bathroom","ABC",code.fallback));ChallengeRoutine r=new ChallengeRoutine(List.of(new ChallengeRoutine.Task("QR code",1,0,MathSettings.LEGACY,code),new ChallengeRoutine.Task("Retype",2)));assertEquals(3,r.units());assertEquals("Retype",r.at(r.advance(0,0)).task.type);assertEquals(1,r.advance(1,0));assertThrows(IllegalArgumentException.class,()->new ChallengeRoutine.Task("QR code",2,0,MathSettings.LEGACY,code));assertThrows(IllegalArgumentException.class,()->new ChallengeRoutine.Task("QR code",1));assertEquals("Math",ChallengeRoutine.legacy("QR code",3).at(0).task.type);}
}
