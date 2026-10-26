package org.sleepwell.app;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Deliberately contains no alarm names, recording IDs, audio, notes or locations. */
final class AutomationEvent {
    enum Kind { TRACKING_STARTED, TRACKING_STOPPED, ALARM_DISMISSED, TRACKING_PAUSED, TRACKING_RESUMED, ALARM_STARTED, ALARM_SNOOZED, WAKE_CHECK_CONFIRMED, WAKE_CHECK_MISSED, WIND_DOWN_REMINDER_POSTED, SLEEP_SOUND_STARTED, SLEEP_SOUND_STOPPED, ANTI_SNORE_SOUND_STARTED, SOUND_RECOGNIZED }
    final String id,soundClass; final Kind kind; final long time; final boolean test;
    AutomationEvent(String id,Kind kind,long time,boolean test){this(id,kind,time,test,"");}
    AutomationEvent(String id,Kind kind,long time,boolean test,String soundClass){
        if(id==null||!id.matches("[a-f0-9]{64}")||kind==null||time<=0)throw new IllegalArgumentException("Invalid automation event");
        if(soundClass==null||!soundClass.isEmpty()&&(kind!=Kind.SOUND_RECOGNIZED||!java.util.Set.of("snoring","speech","laughter","baby_cry","cough").contains(soundClass)))throw new IllegalArgumentException("Invalid public sound class");
        this.id=id;this.kind=kind;this.time=time;this.test=test;this.soundClass=soundClass;
    }
    static AutomationEvent occurrence(Kind kind,String occurrence,long now,boolean test){
        if(occurrence==null||occurrence.isEmpty())throw new IllegalArgumentException("Missing occurrence");
        try{byte[] bytes=MessageDigest.getInstance("SHA-256").digest((kind.name()+":"+occurrence).getBytes(StandardCharsets.UTF_8));StringBuilder id=new StringBuilder();for(byte b:bytes)id.append(String.format(java.util.Locale.ROOT,"%02x",b&255));return new AutomationEvent(id.toString(),kind,now,test);}catch(java.security.NoSuchAlgorithmException impossible){throw new IllegalStateException(impossible);}
    }
    AutomationEvent soundClass(String value){return new AutomationEvent(id,kind,time,test,value);}
    String json(){return "{\"schema\":1,\"event_id\":\""+id+"\",\"event\":\""+kind.name().toLowerCase(java.util.Locale.ROOT)+"\",\"occurred_at\":"+time+",\"test\":"+test+(soundClass.isEmpty()?"":",\"sound_class\":\""+soundClass+"\"")+"}";}
}
