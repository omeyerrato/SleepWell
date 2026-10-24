package org.sleepwell.app;

/** One mapping for the actual alarm sink and independent quiet previews. */
final class AlarmTones {
    static String asset(String tone){return "pulse".equals(tone)?"pulse.wav":"chime".equals(tone)?"chime.wav":"daybreak.wav";}
    static boolean bundled(String tone){return "daybreak".equals(tone)||"pulse".equals(tone)||"chime".equals(tone);}
    static String name(String tone){return tone!=null&&tone.startsWith("content:")?"My audio":"pulse".equals(tone)?"Pulse":"chime".equals(tone)?"Chime":"system".equals(tone)?"Device alarm":"Daybreak";}
    private AlarmTones(){}
}
