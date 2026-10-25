package org.sleepwell.app;
import java.util.*;
/** Single-use in-process visible-entry admission. No persisted/background sonar start. */
final class SonarEntry {
 static final class Ticket {final String nonce;final long session,issued;final SonarConfig config;final boolean analysisRequested,clipsRequested;Ticket(String n,long s,long t,SonarConfig c,boolean analysis,boolean clips){nonce=n;session=s;issued=t;config=c;analysisRequested=analysis;clipsRequested=clips;}Ticket(String n,long s,long t,SonarConfig c,boolean analysis){this(n,s,t,c,analysis,false);}}
 private static Ticket pending;
 static synchronized Ticket issue(boolean visible,long elapsed,long session,SonarConfig config){return issue(visible,elapsed,session,config,false);}
 static synchronized Ticket issue(boolean visible,long elapsed,long session,SonarConfig config,boolean analysisRequested){return issue(visible,elapsed,session,config,analysisRequested,false);}
 static synchronized Ticket issue(boolean visible,long elapsed,long session,SonarConfig config,boolean analysisRequested,boolean clipsRequested){if(!visible||elapsed<0||session<0||config==null||clipsRequested&&!analysisRequested)throw new IllegalStateException("Open tracking before starting contactless capture.");pending=new Ticket(UUID.randomUUID().toString(),session,elapsed,config,analysisRequested,clipsRequested);return pending;}
 static synchronized Ticket consume(String nonce,long elapsed,long expectedSession){Ticket value=pending;if(value==null||!Objects.equals(value.nonce,nonce))return null;pending=null;if(elapsed<value.issued||elapsed-value.issued>10000||value.session!=expectedSession)return null;return value;}
 static boolean sameRun(Ticket ticket,long session,SonarConfig config,boolean requested){return sameRun(ticket,session,config,requested,false);}
 static boolean sameRun(Ticket ticket,long session,SonarConfig config,boolean requested,boolean clips){return ticket!=null&&config!=null&&ticket.session==session&&ticket.config.gain==config.gain&&ticket.analysisRequested==requested&&ticket.clipsRequested==clips;}
}
