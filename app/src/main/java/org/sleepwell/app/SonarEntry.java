package org.sleepwell.app;
import java.util.*;
/** Single-use in-process visible-entry admission. No persisted/background sonar start. */
final class SonarEntry {
 static final class Ticket {final String nonce;final long session,issued;final SonarConfig config;Ticket(String n,long s,long t,SonarConfig c){nonce=n;session=s;issued=t;config=c;}}
 private static Ticket pending;
 static synchronized Ticket issue(boolean visible,long elapsed,long session,SonarConfig config){if(!visible||elapsed<0||session<0||config==null)throw new IllegalStateException("Open tracking before starting contactless capture.");pending=new Ticket(UUID.randomUUID().toString(),session,elapsed,config);return pending;}
 static synchronized Ticket consume(String nonce,long elapsed,long expectedSession){Ticket value=pending;if(value==null||!Objects.equals(value.nonce,nonce))return null;pending=null;if(elapsed<value.issued||elapsed-value.issued>10000||value.session!=expectedSession)return null;return value;}
}
