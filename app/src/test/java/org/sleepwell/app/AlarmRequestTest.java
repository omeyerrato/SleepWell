package org.sleepwell.app;
import org.junit.Test;import java.util.*;import static org.junit.Assert.*;
public final class AlarmRequestTest {
    @Test public void calendarWeekdaysMapToExistingIsoScheduleWithoutSharingInput(){List<Integer> days=new ArrayList<>(List.of(1,2,4,7));AlarmRequest r=new AlarmRequest(6,30,"Workdays",days,false);days.clear();assertEquals(Set.of(7,1,3,6),r.days);assertEquals(Integer.valueOf(6),r.hour);assertEquals(Integer.valueOf(30),r.minute);assertFalse(r.vibrate);}
    @Test public void noDaysMeansOneTimeAndMissingTimeRemainsExplicit(){AlarmRequest r=new AlarmRequest(null,null,null,null,null);assertTrue(r.days.isEmpty());assertNull(r.hour);assertNull(r.minute);assertNull(r.message);assertNull(r.vibrate);}
    @Test public void invalidTimesDaysAndOversizedMessagesAreRejected(){for(int hour:new int[]{-1,24})assertThrows(IllegalArgumentException.class,()->new AlarmRequest(hour,0,"",null,null));for(int minute:new int[]{-1,60})assertThrows(IllegalArgumentException.class,()->new AlarmRequest(0,minute,"",null,null));for(int day:new int[]{0,8})assertThrows(IllegalArgumentException.class,()->new AlarmRequest(0,0,"",List.of(day),null));assertThrows(IllegalArgumentException.class,()->new AlarmRequest(0,0,"x".repeat(121),null,null));}
    @Test public void labelsPreserveUserTextAndRemoveControlCharacters(){AlarmRequest r=new AlarmRequest(0,0,"  Morning\ncoffee\u202e  ",List.of(2,2),true);assertEquals("Morning coffee",r.message);assertEquals(Set.of(1),r.days);}
}
