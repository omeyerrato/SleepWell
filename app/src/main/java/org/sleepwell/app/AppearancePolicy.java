package org.sleepwell.app;

/** Local-clock color phases, deliberately independent of alarms and sleep estimates. */
final class AppearancePolicy {
    static final String[] THEMES={"Midnight","Pre-dawn","Dawn"};
    static final String[] SIZES={"Default","Large","Extra large"};
    static int phase(boolean automatic,int manual,int minuteOfDay) {
        if(!automatic)return Math.max(0,Math.min(2,manual));
        int minute=Math.floorMod(minuteOfDay,1440);
        return minute<240||minute>=1080?0:minute<360?1:2;
    }
    static float textScale(int size){return size==1?1.15f:size==2?1.3f:1f;}
}
