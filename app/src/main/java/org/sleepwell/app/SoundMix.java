package org.sleepwell.app;

import java.util.Set;

/** Persisted choices only. Restoring a mix never authorizes playback. */
final class SoundMix {
    static final String[] LIBRARY={"Rain","Ocean","Fireplace","Wind"};
    static final Set<String> SOURCES=Set.of("Rain","Ocean","Fireplace","Wind","Brown noise","My audio");
    String primary="Rain",uri="",title="My audio";
    int primaryGain=70,brownGain=20,timerMinutes=30,fadeMinutes=5;
    boolean withTracking;
    SoundMix copy(){SoundMix m=new SoundMix();m.primary=primary;m.uri=uri;m.title=title;m.primaryGain=primaryGain;m.brownGain=brownGain;m.timerMinutes=timerMinutes;m.fadeMinutes=fadeMinutes;m.withTracking=withTracking;return m;}
    void validate(){
        if(!SOURCES.contains(primary)||primaryGain<0||primaryGain>100||brownGain<0||brownGain>100||timerMinutes<1||timerMinutes>240||fadeMinutes<0||fadeMinutes>timerMinutes)
            throw new IllegalArgumentException("Choose valid sound levels and a timer from 1 to 240 minutes.");
        if(!uri.isEmpty()&&!uri.startsWith("content://"))throw new IllegalArgumentException("Choose an audio document from this phone.");
        if(primary.equals("My audio")&&uri.isEmpty())throw new IllegalArgumentException("Import an audio file first.");
    }
    String source(){return primary.equals("My audio")?uri:primary;}
    String label(){return primary.equals("My audio")?title:primary;}
    int brown(){return primary.equals("Brown noise")?0:brownGain;}
    static SoundMix legacy(String choice,int minutes){
        SoundMix m=new SoundMix();m.withTracking=choice.equals("Rain")||choice.equals("Brown noise");
        m.primary=choice.equals("Brown noise")?"Brown noise":"Rain";
        m.primaryGain=m.withTracking?20:70;m.brownGain=m.withTracking?0:20;
        m.timerMinutes=Math.max(1,Math.min(240,minutes));m.fadeMinutes=m.withTracking?0:Math.min(5,m.timerMinutes);return m;
    }
}
