package org.sleepwell.app;
import java.time.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class TravelCuePlanTest {
 private long at(String s){return Instant.parse(s).toEpochMilli();}
 private TravelPlan trip(){TravelPlan t=new TravelPlan("Europe/London",LocalDate.of(2026,10,28));t.destination="Asia/Riyadh";t.revision=12;return t;}
 private TravelCuePlan plan(LocalDate first,int minute,int minutes,int nights){TravelPlan t=trip();return new TravelCuePlan(8,12,true,true,t.home,t.destination,first,minute,minutes,nights);}
 @Test public void disabledDefaultsNeverChooseOutputOrPersistReview(){TravelCuePlan p=TravelCuePlan.defaults(trip(),at("2026-10-23T12:00:00Z"));assertFalse(p.enabled);assertFalse(p.reviewed);assertNull(p.next(0));}
 @Test public void foldUsesEarlierOffsetOnceAndDoesNotRepeatFold(){TravelCuePlan p=plan(LocalDate.of(2026,10,25),90,30,2);TravelCuePlan.Window first=p.next(at("2026-10-24T00:00:00Z"));assertEquals(at("2026-10-25T00:30:00Z"),first.start);assertEquals(at("2026-10-25T01:00:00Z"),first.end);assertEquals(at("2026-10-26T01:30:00Z"),p.next(at("2026-10-25T01:10:00Z")).start);}
 @Test public void springGapAdvancesToValidTimeAndDurationIsElapsed(){TravelCuePlan p=plan(LocalDate.of(2026,3,29),90,60,1);TravelCuePlan.Window w=p.next(at("2026-03-28T00:00:00Z"));assertEquals(at("2026-03-29T01:30:00Z"),w.start);assertEquals(3600000,w.end-w.start);}
 @Test public void midWindowRetainsOriginalAnchorsAndAfterEndUsesNextNight(){TravelCuePlan p=plan(LocalDate.of(2026,10,23),1320,30,2);long start=at("2026-10-23T21:00:00Z");assertEquals(start,p.next(start+120000).start);assertEquals(start+1800000,p.next(start+120000).end);assertEquals(start+86400000,p.next(start+1800000).start);assertNull(p.next(start+86400000+1800000));}
 @Test public void windowUsesHomeNotDestinationOrDeviceZone(){TravelCuePlan p=plan(LocalDate.of(2026,10,23),1320,15,1);assertEquals(at("2026-10-23T21:00:00Z"),p.next(0).start);}
 @Test public void enabledWindowsMustFinishTheirNightsBeforeDeparture(){TravelCuePlan p=plan(LocalDate.of(2026,10,27),1320,60,2);try{p.validateTrip(trip());fail();}catch(IllegalArgumentException expected){}p.change(false,false,p.first,p.minute,p.minutes,p.nights).validateTrip(trip());}
 @Test public void tripIdentityAndBothZonesAreFenced(){TravelCuePlan p=plan(LocalDate.of(2026,10,23),1320,60,1);TravelPlan t=trip();p.validateTrip(t);t.revision++;reject(p,t);t=trip();t.home="UTC";reject(p,t);t=trip();t.destination="Asia/Tokyo";reject(p,t);}
 private void reject(TravelCuePlan p,TravelPlan t){try{p.validateTrip(t);fail();}catch(IllegalArgumentException expected){}}
 @Test public void bindingCreatesNewImmutableOwnerSnapshot(){TravelCuePlan p=plan(LocalDate.of(2026,10,23),1320,60,1);TravelPlan t=trip();t.revision=20;TravelCuePlan bound=p.bind(t,30);assertEquals(30,bound.revision);assertEquals(20,bound.tripRevision);assertEquals(8,p.revision);assertEquals(12,p.tripRevision);}
 @Test(expected=IllegalArgumentException.class) public void enabledUnreviewedRejected(){new TravelCuePlan(0,0,true,false,"UTC","UTC",LocalDate.of(2026,10,23),0,60,1);}
 @Test(expected=IllegalArgumentException.class) public void arbitraryDurationRejected(){plan(LocalDate.of(2026,10,23),0,45,1);}
 @Test(expected=IllegalArgumentException.class) public void unboundedNightsRejected(){plan(LocalDate.of(2026,10,23),0,60,15);}
 @Test(expected=IllegalArgumentException.class) public void unknownZoneRejected(){new TravelCuePlan(0,0,false,false,"Moon/Base","UTC",LocalDate.of(2026,10,23),0,60,1);}
}
