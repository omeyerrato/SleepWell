package org.sleepwell.app;

import java.time.*;
import java.util.*;

/** User-chosen civil-time reminders, not a circadian treatment or a flight-direction model. */
final class TravelPlan {
    String home,destination;LocalDate departure;int minute=9*60,days=3;boolean reminders,trackingReminders;long revision;
    TravelPlan(String home,LocalDate departure){this.home=home;this.departure=departure;destination="Europe/London";}
    TravelPlan copy(){TravelPlan p=new TravelPlan(home,departure);p.destination=destination;p.minute=minute;p.days=days;p.reminders=reminders;p.trackingReminders=trackingReminders;p.revision=revision;return p;}
    void validate(){
        if(!ZoneId.getAvailableZoneIds().contains(home)||!ZoneId.getAvailableZoneIds().contains(destination))throw new IllegalArgumentException("Choose a listed time zone.");
        if(departure==null||departure.getYear()<2000||departure.getYear()>2100)throw new IllegalArgumentException("Choose a departure date from 2000 to 2100.");
        if(minute<0||minute>=1440)throw new IllegalArgumentException("Choose a valid reminder time.");
        if(days<1||days>14)throw new IllegalArgumentException("Choose one to fourteen reminder days.");
    }
    long next(long now,long lastDelivered){
        validate();if(!reminders)return 0;long after=Math.max(now,lastDelivered);ZoneId zone=ZoneId.of(destination);
        for(int day=0;day<days;day++){
            // Java resolves a spring gap forward and a repeated fall time to its earlier offset.
            long at=departure.plusDays(day).atTime(minute/60,minute%60).atZone(zone).toInstant().toEpochMilli();
            if(at>after)return at;
        }
        return 0;
    }
    int differenceSeconds(long at){Instant instant=Instant.ofEpochMilli(at);return ZoneId.of(destination).getRules().getOffset(instant).getTotalSeconds()-ZoneId.of(home).getRules().getOffset(instant).getTotalSeconds();}
    long departureAnchor(){return departure.atTime(12,0).atZone(ZoneId.of(home)).toInstant().toEpochMilli();}
    static String city(String id){String[] pieces=id.split("/");return pieces[pieces.length-1].replace('_',' ');}
    static String offset(String id,long at){String value=ZoneId.of(id).getRules().getOffset(Instant.ofEpochMilli(at)).getId();return value.equals("Z")?"UTC":"UTC"+value;}
    static String difference(int seconds){int minutes=Math.abs(seconds)/60;String duration=minutes/60+" h"+(minutes%60==0?"":" "+minutes%60+" min");return seconds==0?"Same time":duration+(seconds>0?" ahead":" behind");}
    static List<String> zones(String query){String search=query.trim().toLowerCase(Locale.ROOT).replace(' ','_');List<String> result=new ArrayList<>();for(String id:ZoneId.getAvailableZoneIds())if(id.toLowerCase(Locale.ROOT).contains(search))result.add(id);Collections.sort(result);return result;}
}
