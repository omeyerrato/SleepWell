package org.sleepwell.app;
import java.util.*;
/** Only this app's rule and expiry belong to this owner. No global DND restoration exists. */
final class TrackingDndLease {
 static final long WINDOW=15*60000L,HARD=12*3600000L;
 final String run,token,rule;final long deadline,hard;final int boot;final boolean active;
 TrackingDndLease(String run,String token,String rule,long deadline,long hard,int boot,boolean active){if(run==null||run.isBlank()||token==null||token.isBlank()||rule==null||rule.isBlank()||deadline<0||hard<deadline||boot<0)throw new IllegalArgumentException("Invalid tracking DND lease");this.run=run;this.token=token;this.rule=rule;this.deadline=deadline;this.hard=hard;this.boot=boot;this.active=active;}
 TrackingDndLease active(){return new TrackingDndLease(run,token,rule,deadline,hard,boot,true);}TrackingDndLease renewed(long value){return new TrackingDndLease(run,token,rule,value,hard,boot,active);}
}
