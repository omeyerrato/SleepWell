package org.sleepwell.app;
import java.util.*;
/** The duration and audio choice are admitted once by the original visible Manual Start. */
final class TrackingDelayEntry {
 static final class Ticket{final TrackingDelay plan;Ticket(TrackingDelay plan){this.plan=plan;}}
 private static Ticket pending;
 static synchronized Ticket issue(boolean visible,long elapsed,long wall,int boot,int minutes,boolean sonar,boolean sounds,boolean clips,boolean light){if(!visible)throw new IllegalStateException("Open tracking before starting a delay.");pending=new Ticket(new TrackingDelay(UUID.randomUUID().toString(),elapsed,wall,boot,minutes,sonar,sounds,sounds&&clips,!sonar&&light,TrackingDelay.Phase.WAITING,0,""));return pending;}
 static synchronized TrackingDelay consume(String token,long now){Ticket ticket=pending;if(ticket==null||!ticket.plan.token.equals(token))return null;pending=null;if(now<ticket.plan.elapsed||now-ticket.plan.elapsed>10000)return null;return ticket.plan;}
 private TrackingDelayEntry(){}
}
