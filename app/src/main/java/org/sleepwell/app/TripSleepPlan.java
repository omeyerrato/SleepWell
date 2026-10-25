package org.sleepwell.app;

import java.time.*;
import java.util.*;
import org.json.*;

/** A user's dated pre-travel schedule, not an inferred circadian phase or treatment. */
final class TripSleepPlan {
    long revision,tripRevision;String home,destination;LocalDate departure;
    int bed,wake,days=3,step=30,lead=30;boolean earlier=true,reminders;
    static final class Night {
        final int index,shift;final long bed,wake,remind;
        Night(int index,int shift,long bed,long wake,int lead){this.index=index;this.shift=shift;this.bed=bed;this.wake=wake;remind=Math.subtractExact(bed,lead*60000L);}
    }
    static TripSleepPlan initial(TravelPlan trip,SleepPlan base){TripSleepPlan p=new TripSleepPlan();p.tripRevision=trip.revision;p.home=trip.home;p.destination=trip.destination;p.departure=trip.departure;p.bed=base.bedtime;p.wake=base.wake;return p;}
    boolean matches(TravelPlan trip){return tripRevision==trip.revision&&home.equals(trip.home)&&destination.equals(trip.destination)&&departure.equals(trip.departure);}
    boolean sameChoices(TripSleepPlan p){return p!=null&&tripRevision==p.tripRevision&&home.equals(p.home)&&destination.equals(p.destination)&&departure.equals(p.departure)&&bed==p.bed&&wake==p.wake&&days==p.days&&step==p.step&&lead==p.lead&&earlier==p.earlier&&reminders==p.reminders;}
    TripSleepPlan copy(){try{return decode(json());}catch(JSONException invalid){throw new IllegalStateException(invalid);}}
    void validate(){if(revision<0||tripRevision<=0||!ZoneId.getAvailableZoneIds().contains(home)||!ZoneId.getAvailableZoneIds().contains(destination)||departure==null||departure.getYear()<2000||departure.getYear()>2100||bed<0||bed>=1440||wake<0||wake>=1440||days<1||days>7||!Set.of(15,30,60).contains(step)||!Set.of(0,15,30,60).contains(lead))throw new IllegalArgumentException("Review the trip, times, days and shift.");int duration=Math.floorMod(wake-bed,1440);if(duration<20||duration>810)throw new IllegalArgumentException("Choose a baseline sleep window from20minutes to13h30minutes.");}
    List<Night> nights(){validate();List<Night> result=new ArrayList<>();ZoneId zone=ZoneId.of(home);long previousEnd=Long.MIN_VALUE;
        for(int n=0;n<days;n++){LocalDate date=departure.minusDays(days-n);int change=(earlier?-1:1)*(n+1)*step;LocalDateTime a=date.atTime(bed/60,bed%60).plusMinutes(change),b=date.plusDays(wake<=bed?1:0).atTime(wake/60,wake%60).plusMinutes(change);long from=resolve(a,zone),to=resolve(b,zone);if(to<=from||to-from>15*3600000L||from<previousEnd)throw new IllegalArgumentException("These dated sleep windows overlap or cross an unsupported clock change.");previousEnd=to;result.add(new Night(n,change,from,to,lead));}return List.copyOf(result);}
    private static long resolve(LocalDateTime local,ZoneId zone){List<ZoneOffset> offsets=zone.getRules().getValidOffsets(local);if(offsets.size()!=1)throw new IllegalArgumentException("A selected time is repeated or missing at a daylight-saving change. Choose different times or fewer days.");return local.toInstant(offsets.get(0)).toEpochMilli();}
    Night next(long now,long delivered){for(Night night:nights())if(night.remind>Math.max(now,delivered))return night;return null;}
    JSONObject json()throws JSONException{return new JSONObject().put("version",1).put("revision",revision).put("tripRevision",tripRevision).put("home",home).put("destination",destination).put("departure",departure.toString()).put("bed",bed).put("wake",wake).put("days",days).put("step",step).put("lead",lead).put("earlier",earlier).put("reminders",reminders);}
    static TripSleepPlan decode(JSONObject j)throws JSONException{Set<String> fields=Set.of("version","revision","tripRevision","home","destination","departure","bed","wake","days","step","lead","earlier","reminders");Set<String> actual=new HashSet<>();j.keys().forEachRemaining(actual::add);if(!actual.equals(fields)||number(j,"version")!=1)throw new JSONException("Unsupported travel sleep plan");try{TripSleepPlan p=new TripSleepPlan();p.revision=number(j,"revision");p.tripRevision=number(j,"tripRevision");p.home=text(j,"home");p.destination=text(j,"destination");p.departure=LocalDate.parse(text(j,"departure"));p.bed=Math.toIntExact(number(j,"bed"));p.wake=Math.toIntExact(number(j,"wake"));p.days=Math.toIntExact(number(j,"days"));p.step=Math.toIntExact(number(j,"step"));p.lead=Math.toIntExact(number(j,"lead"));if(!(j.get("earlier") instanceof Boolean)||!(j.get("reminders") instanceof Boolean))throw new JSONException("Invalid reminder choices");p.earlier=j.getBoolean("earlier");p.reminders=j.getBoolean("reminders");p.validate();return p;}catch(RuntimeException bad){throw new JSONException("The travel sleep plan is invalid: "+bad.getMessage());}}
    private static String text(JSONObject j,String key)throws JSONException{Object value=j.get(key);if(!(value instanceof String))throw new JSONException("Expected text for "+key);return (String)value;}
    private static long number(JSONObject j,String key)throws JSONException{Object value=j.get(key);if(!(value instanceof Number))throw new JSONException("Expected integer for "+key);double d=((Number)value).doubleValue();if(!Double.isFinite(d)||d!=Math.rint(d)||d<0||d>9007199254740991d)throw new JSONException("Invalid integer for "+key);return ((Number)value).longValue();}
}
