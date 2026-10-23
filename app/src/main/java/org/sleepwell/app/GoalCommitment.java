package org.sleepwell.app;

import java.time.*;
import java.util.*;

/** Immutable target/date snapshot. Progress is derived from effective records. */
final class GoalCommitment {
    final String id,metric,zone,motivation;
    final LocalDate start,stopped;
    final int days,target,cutoff;
    GoalCommitment(String id,String metric,int target,LocalDate start,int days,String zone,int cutoff,String motivation,LocalDate stopped){this.id=id;this.metric=metric;this.target=target;this.start=start;this.days=days;this.zone=zone;this.cutoff=cutoff;this.motivation=motivation;this.stopped=stopped;validate();}
    void validate(){
        UUID.fromString(id);ZoneId.of(zone);
        if(!Set.of("duration","bedtime").contains(metric)||days!=14&&days!=30||cutoff<0||cutoff>=1440)throw new IllegalArgumentException("Goal details are invalid.");
        if(target<0||target>=1440||metric.equals("duration")&&(target<20||target>810))throw new IllegalArgumentException("Goal target is invalid.");
        if(motivation.length()>240||start.getYear()<2000||start.getYear()>2100)throw new IllegalArgumentException("Choose a date between 2000 and 2100 and a shorter motivation.");
        if(stopped!=null&&(stopped.isBefore(start.minusDays(1))||stopped.isAfter(end())))throw new IllegalArgumentException("Invalid stopped goal date.");
    }
    LocalDate end(){return start.plusDays(days-1);}
    LocalDate through(){return stopped==null?end():stopped;}
    boolean active(LocalDate today){return stopped==null&&!today.isAfter(end());}
    GoalCommitment stop(LocalDate day){return new GoalCommitment(id,metric,target,start,days,zone,cutoff,motivation,day.isBefore(start)?start.minusDays(1):day.isAfter(end())?end():day);}
}
