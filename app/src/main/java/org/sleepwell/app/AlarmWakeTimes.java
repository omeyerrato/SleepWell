package org.sleepwell.app;

/** Shared editor choices; selecting a duration changes only the open alarm/nap draft. */
final class AlarmWakeTimes {
    static int[] options(){
        int[] values=new int[26];values[0]=0;values[1]=30;
        for(int n=2;n<values.length;n++)values[n]=(n-1)*60;
        return values;
    }
    private AlarmWakeTimes(){}
}
