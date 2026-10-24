package org.sleepwell.app;
import java.time.*;
import java.util.*;

/** User-authored windows, never an inferred circadian prescription. */
final class TravelCuePlan {
 final long revision,tripRevision;final boolean enabled,reviewed;final String home,destination;final LocalDate first;final int minute,minutes,nights;
 TravelCuePlan(long revision,long tripRevision,boolean enabled,boolean reviewed,String home,String destination,LocalDate first,int minute,int minutes,int nights){if(revision<0||tripRevision<0||!ZoneId.getAvailableZoneIds().contains(home)||!ZoneId.getAvailableZoneIds().contains(destination)||first==null||first.getYear()<2000||first.getYear()>2100||minute<0||minute>1439||!Set.of(15,30,60).contains(minutes)||nights<1||nights>14||enabled&&!reviewed)throw new IllegalArgumentException("Choose and review a valid cue window.");this.revision=revision;this.tripRevision=tripRevision;this.enabled=enabled;this.reviewed=reviewed;this.home=home;this.destination=destination;this.first=first;this.minute=minute;this.minutes=minutes;this.nights=nights;}
 static TravelCuePlan defaults(TravelPlan trip,long now){return new TravelCuePlan(0,trip.revision,false,false,trip.home,trip.destination,Instant.ofEpochMilli(now).atZone(ZoneId.of(trip.home)).toLocalDate(),22*60,60,1);}
 TravelCuePlan change(boolean enabled,boolean reviewed,LocalDate first,int minute,int minutes,int nights){return new TravelCuePlan(revision,tripRevision,enabled,reviewed,home,destination,first,minute,minutes,nights);}
 TravelCuePlan bind(TravelPlan trip,long revision){return new TravelCuePlan(revision,trip.revision,enabled,reviewed,trip.home,trip.destination,first,minute,minutes,nights);}
 void validateTrip(TravelPlan trip){if(!home.equals(trip.home)||!destination.equals(trip.destination)||tripRevision!=trip.revision)throw new IllegalArgumentException("The trip changed. Review the light-cue window again.");if(enabled&&!first.plusDays(nights-1).isBefore(trip.departure))throw new IllegalArgumentException("Choose cue nights before your departure date.");}
 Window next(long now){if(!enabled)return null;for(int n=0;n<nights;n++){LocalDate date=first.plusDays(n);long start=date.atTime(minute/60,minute%60).atZone(ZoneId.of(home)).toInstant().toEpochMilli();long end=Math.addExact(start,minutes*60000L);if(end>now)return new Window(start,end,date);}return null;}
 static final class Window {final long start,end;final LocalDate date;Window(long start,long end,LocalDate date){if(end<=start||end-start>3600000L)throw new IllegalArgumentException("Invalid cue window");this.start=start;this.end=end;this.date=date;}}
}
